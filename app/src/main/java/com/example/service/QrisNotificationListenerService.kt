package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.db.AppDatabase
import com.example.data.model.NotificationLog
import com.example.data.model.WebhookLog
import com.example.data.pref.SettingsDataStore
import com.example.data.repository.QrisRepository
import com.example.util.PayloadBuilder
import com.example.util.PaymentParser
import com.example.util.TtsManager
import com.example.util.WebhookClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class QrisNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: QrisRepository
    private var ttsManager: TtsManager? = null

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(applicationContext)
        repository = QrisRepository(AppDatabase.getDatabase(applicationContext))
        ttsManager = TtsManager(applicationContext)
        
        // Start persistent foreground service if listener starts
        ListenerForegroundService.startService(applicationContext)
    }

    override fun onDestroy() {
        ttsManager?.shutdown()
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("QrisListenerService", "Ready and connected to Notification Service")
        ListenerForegroundService.startService(applicationContext)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w("QrisListenerService", "Disconnected from Notification Service. Prompt rebind if possible.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, QrisNotificationListenerService::class.java))
            } catch (e: Exception) {
                Log.e("QrisListenerService", "Rebind request failed.", e)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Skip Group summaries
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        val packageName = sbn.packageName ?: return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        serviceScope.launch {
            processNotification(packageName, notification, extras, sbn.postTime)
        }
    }

    private suspend fun processNotification(
        packageName: String,
        notification: Notification,
        extras: Bundle,
        postTime: Long
    ) {
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val combinedText = "$title $text $bigText".lowercase()

        // Resolve human-readable App Name
        var appName = packageName
        try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            appName = pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            // Fallback to packageName
        }

        // Parse nominal amount
        val amount = PaymentParser.parseAmount(title, text, bigText)
        // Parse sender name
        val senderName = PaymentParser.parseSender(title, text, bigText)

        // Generate deduplication key
        val cleanString = "$packageName|$title|$text|${amount ?: "null"}|$senderName"
        val dedupeKey = md5(cleanString)
        val windowStart = System.currentTimeMillis() - 30 * 1000

        val activeProfiles = repository.getActiveProfiles()
        var shouldTriggerWorker = false

        if (activeProfiles.isNotEmpty()) {
            // Check matching profiles
            val matchedProfiles = mutableListOf<com.example.data.model.RoutingProfile>()
            for (profile in activeProfiles) {
                // Check package names filter
                if (profile.packages.isNotEmpty() && !profile.packages.contains(packageName)) {
                    continue
                }

                // Check negative keywords (abort immediately for this profile if found)
                var hasNeg = false
                val negKws = profile.negativeKeywords
                for (kw in negKws) {
                    if (kw.trim().isNotEmpty() && combinedText.contains(kw.lowercase().trim())) {
                        hasNeg = true
                        break
                    }
                }
                if (hasNeg) continue

                // Check positive keywords
                var hasPos = false
                val posKws = profile.positiveKeywords
                for (kw in posKws) {
                    if (kw.trim().isNotEmpty() && combinedText.contains(kw.lowercase().trim())) {
                        hasPos = true
                        break
                    }
                }
                if (posKws.isNotEmpty() && !hasPos) {
                    continue
                }

                matchedProfiles.add(profile)
            }

            if (matchedProfiles.isEmpty()) {
                Log.d("QrisListenerService", "No active routing profiles matched this notification.")
                return
            }

            // Decide profiles to route depending on "only first matched" option
            val routingModeOnlyFirst = settingsDataStore.isRoutingModeOnlyFirstFlow.first()
            val profilesToProcess = if (routingModeOnlyFirst) {
                listOf(matchedProfiles.first())
            } else {
                matchedProfiles
            }

            // Deduplicate per profile
            val finalProfilesToProcess = mutableListOf<com.example.data.model.RoutingProfile>()
            for (p in profilesToProcess) {
                val isDedupe = repository.getDedupeLog(p.id, dedupeKey, windowStart) != null
                if (isDedupe) {
                    Log.d("QrisListenerService", "Profile ${p.name} Deduplication hit. Skipping.")
                    continue
                }
                finalProfilesToProcess.add(p)
            }

            if (finalProfilesToProcess.isEmpty()) {
                Log.d("QrisListenerService", "All matching profiles hit deduplication check.")
                return
            }

            // Write notification log (saved once)
            val notificationLog = NotificationLog(
                packageName = packageName,
                appName = appName,
                title = title,
                text = text,
                bigText = bigText.ifEmpty { null },
                timestamp = postTime,
                amount = amount,
                dedupeKey = dedupeKey,
                sender = senderName
            )
            val logId = repository.insertNotificationLog(notificationLog)
            val savedLog = notificationLog.copy(id = logId)

            val deviceId = settingsDataStore.deviceIdFlow.first()

            // Run delivery and voice for every processed profile
            for (profile in finalProfilesToProcess) {
                // Record per-profile deduplication
                repository.insertDedupeLog(
                    com.example.data.model.ProfileDedupeLog(
                        profileId = profile.id,
                        dedupeKey = dedupeKey,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Speaki (TTS)
                val isSpeakEnabled = if (profile.ttsEnabled) true else settingsDataStore.isSpeakerEnabledFlow.first()
                if (isSpeakEnabled) {
                    val ttsTemplate = if (!profile.ttsTemplate.isNullOrBlank()) {
                        profile.ttsTemplate
                    } else {
                        settingsDataStore.speakerTemplateFlow.first()
                    }
                    val repeatCount = settingsDataStore.speakerRepeatFlow.first()
                    val ttsVolume = settingsDataStore.speakerVolumeFlow.first()

                    val speechText = if (amount != null) {
                        ttsTemplate
                            .replace("{amount}", amount.toString())
                            .replace("{app_name}", appName)
                            .replace("{sender}", senderName ?: "Pelanggan")
                    } else {
                        "Ada notifikasi pembayaran dari $appName"
                    }
                    ttsManager?.speak(speechText, repeatCount, ttsVolume)
                }

                // Push to webhooks
                val targetIds = profile.webhookTargetIds
                for (targetId in targetIds) {
                    val target = repository.getTargetById(targetId) ?: continue
                    if (!target.enabled) continue

                    val effectiveTarget = if (!profile.customTemplate.isNullOrBlank()) {
                        target.copy(customTemplate = profile.customTemplate)
                    } else {
                        target
                    }

                    val payload = PayloadBuilder.buildPayload(effectiveTarget, savedLog, deviceId)

                    val webhookLog = WebhookLog(
                        webhookTargetId = target.id,
                        notificationLogId = logId,
                        webhookTargetName = target.name,
                        webhookTargetUrl = target.url,
                        payload = payload,
                        status = "pending",
                        responseCode = null,
                        errorMessage = null,
                        retryCount = 0,
                        timestamp = System.currentTimeMillis()
                    )

                    val webhookLogId = repository.insertWebhookLog(webhookLog)

                    try {
                        val response = WebhookClient.executePost(target.url, target.secret, payload)
                        if (response.isSuccessful) {
                            repository.updateWebhookLog(
                                webhookLog.copy(
                                    id = webhookLogId,
                                    status = "sent",
                                    responseCode = response.code
                                )
                            )
                        } else {
                            repository.updateWebhookLog(
                                webhookLog.copy(
                                    id = webhookLogId,
                                    status = "pending",
                                    responseCode = response.code,
                                    errorMessage = response.errorMessage ?: "HTTP ${response.code}",
                                    retryCount = 1
                                )
                            )
                            shouldTriggerWorker = true
                        }
                    } catch (e: Exception) {
                        repository.updateWebhookLog(
                            webhookLog.copy(
                                id = webhookLogId,
                                status = "pending",
                                responseCode = 0,
                                errorMessage = e.message ?: e.toString(),
                                retryCount = 1
                            )
                        )
                        shouldTriggerWorker = true
                    }
                }
            }
        } else {
            // Fallback global legacy flow
            val allowedPackages = settingsDataStore.allowedPackagesFlow.first()
            if (!allowedPackages.contains(packageName)) {
                return
            }

            // Check negative keywords (abort immediately if found)
            val negativeKeywords = settingsDataStore.negativeKeywordsFlow.first()
            for (kw in negativeKeywords) {
                if (kw.trim().isNotEmpty() && combinedText.contains(kw.lowercase())) {
                    Log.d("QrisListenerService", "Dropped notification containing negative keyword: $kw")
                    return
                }
            }

            // Check positive keywords
            val positiveKeywords = settingsDataStore.positiveKeywordsFlow.first()
            var matchesPositive = false
            for (kw in positiveKeywords) {
                if (kw.trim().isNotEmpty() && combinedText.contains(kw.lowercase())) {
                    matchesPositive = true
                    break
                }
            }
            if (positiveKeywords.isNotEmpty() && !matchesPositive) {
                Log.d("QrisListenerService", "Dropped notification failing positive keywords match.")
                return
            }

            // Checked window deduplication
            val duplicate = repository.checkDeduplication(dedupeKey, windowStart)
            if (duplicate != null) {
                Log.d("QrisListenerService", "Deduplication hit. Dropping redundant notification: $cleanString")
                return
            }

            // Save incoming notification log
            val notificationLog = NotificationLog(
                packageName = packageName,
                appName = appName,
                title = title,
                text = text,
                bigText = bigText.ifEmpty { null },
                timestamp = postTime,
                amount = amount,
                dedupeKey = dedupeKey,
                sender = senderName
            )
            val logId = repository.insertNotificationLog(notificationLog)
            val savedLog = notificationLog.copy(id = logId)

            // Read out loud with Text-to-Speech
            val isSpeakerEnabled = settingsDataStore.isSpeakerEnabledFlow.first()
            if (isSpeakerEnabled) {
                val userTemplate = settingsDataStore.speakerTemplateFlow.first()
                val repeatCount = settingsDataStore.speakerRepeatFlow.first()
                val ttsVolume = settingsDataStore.speakerVolumeFlow.first()

                val speechText = if (amount != null) {
                    userTemplate
                        .replace("{amount}", amount.toString())
                        .replace("{app_name}", appName)
                        .replace("{sender}", senderName ?: "Pelanggan")
                } else {
                    "Ada notifikasi pembayaran dari $appName"
                }
                ttsManager?.speak(speechText, repeatCount, ttsVolume)
            }

            // Forward to all configured enabling target webhooks
            val deviceId = settingsDataStore.deviceIdFlow.first()
            val targets = repository.getActiveTargets()

            if (targets.isEmpty()) {
                return
            }

            for (target in targets) {
                val payload = PayloadBuilder.buildPayload(target, savedLog, deviceId)

                val webhookLog = WebhookLog(
                    webhookTargetId = target.id,
                    notificationLogId = logId,
                    webhookTargetName = target.name,
                    webhookTargetUrl = target.url,
                    payload = payload,
                    status = "pending",
                    responseCode = null,
                    errorMessage = null,
                    retryCount = 0,
                    timestamp = System.currentTimeMillis()
                )

                val webhookLogId = repository.insertWebhookLog(webhookLog)

                try {
                    val response = WebhookClient.executePost(target.url, target.secret, payload)
                    if (response.isSuccessful) {
                        repository.updateWebhookLog(
                            webhookLog.copy(
                                id = webhookLogId,
                                status = "sent",
                                responseCode = response.code
                            )
                        )
                    } else {
                        repository.updateWebhookLog(
                            webhookLog.copy(
                                id = webhookLogId,
                                status = "pending",
                                responseCode = response.code,
                                errorMessage = response.errorMessage ?: "HTTP ${response.code}",
                                retryCount = 1
                            )
                        )
                        shouldTriggerWorker = true
                    }
                } catch (e: Exception) {
                    repository.updateWebhookLog(
                        webhookLog.copy(
                            id = webhookLogId,
                            status = "pending",
                            responseCode = 0,
                            errorMessage = e.message ?: e.toString(),
                            retryCount = 1
                        )
                    )
                    shouldTriggerWorker = true
                }
            }
        }

        if (shouldTriggerWorker) {
            scheduleRetryWorker(applicationContext)
        }
    }

    private fun scheduleRetryWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val retryRequest = OneTimeWorkRequestBuilder<WebhookRetryWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "qris_webhook_retry_work",
            ExistingWorkPolicy.REPLACE,
            retryRequest
        )
    }

    private fun md5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
}
