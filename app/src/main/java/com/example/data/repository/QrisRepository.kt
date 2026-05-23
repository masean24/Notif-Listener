package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.NotificationLog
import com.example.data.model.WebhookLog
import com.example.data.model.WebhookTarget
import com.example.data.model.RoutingProfile
import com.example.data.model.ProfileDedupeLog
import kotlinx.coroutines.flow.Flow

class QrisRepository(private val db: AppDatabase) {

    val allTargetsFlow: Flow<List<WebhookTarget>> = db.webhookTargetDao().getAllTargetsFlow()
    val allNotificationLogsFlow: Flow<List<NotificationLog>> = db.notificationLogDao().getAllLogsFlow()
    val allWebhookLogsFlow: Flow<List<WebhookLog>> = db.webhookLogDao().getAllWebhookLogsFlow()
    val allProfilesFlow: Flow<List<RoutingProfile>> = db.routingProfileDao().getAllProfilesFlow()
    
    val totalNotificationsCount: Flow<Int> = db.notificationLogDao().getCountFlow()
    val successWebhookCount: Flow<Int> = db.webhookLogDao().getSuccessCountFlow()
    val failedWebhookCount: Flow<Int> = db.webhookLogDao().getFailedCountFlow()
    val pendingWebhookCount: Flow<Int> = db.webhookLogDao().getPendingCountFlow()

    suspend fun getActiveTargets(): List<WebhookTarget> = db.webhookTargetDao().getActiveTargets()
    suspend fun getTargetById(id: Int): WebhookTarget? = db.webhookTargetDao().getTargetById(id)
    suspend fun insertTarget(target: WebhookTarget): Long = db.webhookTargetDao().insertTarget(target)
    suspend fun updateTarget(target: WebhookTarget) = db.webhookTargetDao().updateTarget(target)
    suspend fun deleteTarget(target: WebhookTarget) = db.webhookTargetDao().deleteTarget(target)

    suspend fun getActiveProfiles(): List<RoutingProfile> = db.routingProfileDao().getActiveProfiles()
    suspend fun getProfileById(id: Int): RoutingProfile? = db.routingProfileDao().getProfileById(id)
    suspend fun insertProfile(profile: RoutingProfile): Long = db.routingProfileDao().insertProfile(profile)
    suspend fun updateProfile(profile: RoutingProfile) = db.routingProfileDao().updateProfile(profile)
    suspend fun deleteProfile(profile: RoutingProfile) = db.routingProfileDao().deleteProfile(profile)

    suspend fun getDedupeLog(profileId: Int, dedupeKey: String, windowStart: Long): ProfileDedupeLog? =
        db.profileDedupeLogDao().getDedupeLog(profileId, dedupeKey, windowStart)
    suspend fun insertDedupeLog(log: ProfileDedupeLog) = db.profileDedupeLogDao().insertDedupeLog(log)
    suspend fun clearAllDedupeLogs() = db.profileDedupeLogDao().clearAllDedupeLogs()

    suspend fun checkDeduplication(dedupeKey: String, windowStart: Long): NotificationLog? =
        db.notificationLogDao().getLogByDedupeKeyInWindow(dedupeKey, windowStart)

    suspend fun insertNotificationLog(log: NotificationLog): Long =
        db.notificationLogDao().insertLog(log)

    suspend fun clearLogs() {
        db.notificationLogDao().clearLogs()
        db.webhookLogDao().clearAllDeliveryLogs()
        db.profileDedupeLogDao().clearAllDedupeLogs()
    }

    suspend fun getPendingWebhookLogs(): List<WebhookLog> = db.webhookLogDao().getPendingLogs()
    suspend fun insertWebhookLog(log: WebhookLog): Long = db.webhookLogDao().insertWebhookLog(log)
    suspend fun updateWebhookLog(log: WebhookLog) = db.webhookLogDao().updateWebhookLog(log)
    suspend fun getLogsForNotification(notificationId: Long): List<WebhookLog> = db.webhookLogDao().getLogsForNotification(notificationId)
}

