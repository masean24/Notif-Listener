package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.NotificationLog
import com.example.data.model.WebhookLog
import com.example.data.model.WebhookTarget
import com.example.data.model.RoutingProfile
import com.example.data.pref.SettingsDataStore
import com.example.data.repository.QrisRepository
import com.example.util.PayloadBuilder
import com.example.util.TtsManager
import com.example.util.WebhookClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QrisRepository(AppDatabase.getDatabase(application))
    private val dataStore = SettingsDataStore(application)
    private var ttsTester: TtsManager? = null

    // Settings preferences
    val deviceId = dataStore.deviceIdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val isSpeakerEnabled = dataStore.isSpeakerEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val speakerRepeat = dataStore.speakerRepeatFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val speakerVolume = dataStore.speakerVolumeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)
    val speakerTemplate = dataStore.speakerTemplateFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Pembayaran masuk {amount} rupiah dari {app_name}")
    val allowedPackages = dataStore.allowedPackagesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val positiveKeywords = dataStore.positiveKeywordsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val negativeKeywords = dataStore.negativeKeywordsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val isRoutingModeOnlyFirst = dataStore.isRoutingModeOnlyFirstFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Lists state
    val targets = repository.allTargetsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repository.allNotificationLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val webhookLogs = repository.allWebhookLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val profiles = repository.allProfilesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics state
    val totalNotificationsCount = repository.totalNotificationsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val successWebhookCount = repository.successWebhookCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val failedWebhookCount = repository.failedWebhookCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingWebhookCount = repository.pendingWebhookCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenueAmount = logs.map { list ->
        list.sumOf { it.amount ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val revenueByApp = logs.map { list ->
        list.groupBy { it.appName }
            .mapValues { entry -> entry.value.sumOf { it.amount ?: 0 } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Test webhook output
    private val _testSenderResult = MutableStateFlow<String?>(null)
    val testSenderResult: StateFlow<String?> = _testSenderResult.asStateFlow()

    init {
        ttsTester = TtsManager(application)
    }

    override fun onCleared() {
        ttsTester?.shutdown()
        super.onCleared()
    }

    // Write parameters
    fun setSpeakerEnabled(enabled: Boolean) = viewModelScope.launch { dataStore.setSpeakerEnabled(enabled) }
    fun setSpeakerRepeat(repeat: Int) = viewModelScope.launch { dataStore.setSpeakerRepeat(repeat) }
    fun setSpeakerVolume(volume: Float) = viewModelScope.launch { dataStore.setSpeakerVolume(volume) }
    fun setSpeakerTemplate(template: String) = viewModelScope.launch { dataStore.setSpeakerTemplate(template) }
    
    fun saveAllowedPackages(packages: List<String>) = viewModelScope.launch { dataStore.saveAllowedPackages(packages) }
    fun savePositiveKeywords(keywords: List<String>) = viewModelScope.launch { dataStore.savePositiveKeywords(keywords) }
    fun saveNegativeKeywords(keywords: List<String>) = viewModelScope.launch { dataStore.saveNegativeKeywords(keywords) }
    fun setRoutingModeOnlyFirst(onlyFirst: Boolean) = viewModelScope.launch { dataStore.setRoutingModeOnlyFirst(onlyFirst) }

    // Target DB operations
    fun insertTarget(target: WebhookTarget) = viewModelScope.launch { repository.insertTarget(target) }
    fun updateTarget(target: WebhookTarget) = viewModelScope.launch { repository.updateTarget(target) }
    fun deleteTarget(target: WebhookTarget) = viewModelScope.launch { repository.deleteTarget(target) }

    // Profile DB operations
    fun insertProfile(profile: RoutingProfile) = viewModelScope.launch { repository.insertProfile(profile) }
    fun updateProfile(profile: RoutingProfile) = viewModelScope.launch { repository.updateProfile(profile) }
    fun deleteProfile(profile: RoutingProfile) = viewModelScope.launch { repository.deleteProfile(profile) }

    // Logs clearing
    fun clearLogs() = viewModelScope.launch { repository.clearLogs() }

    // Playback and delivery simulation testers
    fun testSpeak() {
        val template = speakerTemplate.value
        val volume = speakerVolume.value
        val repeat = speakerRepeat.value
        val text = template.replace("{amount}", "15000").replace("{app_name}", "GoPay")
        ttsTester?.speak(text, repeat, volume)
    }

    fun testWebhook(target: WebhookTarget) {
        viewModelScope.launch {
            _testSenderResult.value = "Mengirim..."
            val dummyLog = NotificationLog(
                packageName = "com.gojek.app",
                appName = "GoPay",
                title = "Pembayaran Berhasil",
                text = "Anda menerima pembayaran sebesar Rp150.000 dari AHMAD BUDI",
                bigText = "Menjumlahkan saldo QRIS sebesar Rp150.000 dari GoPay.",
                timestamp = System.currentTimeMillis(),
                amount = 150000,
                dedupeKey = "test-payload-key",
                sender = "AHMAD BUDI"
            )
            val pay = PayloadBuilder.buildPayload(target, dummyLog, deviceId.value)
            
            val response = withContext(Dispatchers.IO) {
                WebhookClient.executePost(target.url, target.secret, pay)
            }
            if (response.isSuccessful) {
                _testSenderResult.value = "Sukses (HTTP ${response.code})"
            } else {
                _testSenderResult.value = "Gagal: ${response.errorMessage ?: "HTTP ${response.code}"}"
            }
        }
    }

    suspend fun getLogsForNotification(notificationId: Long): List<WebhookLog> {
        return repository.getLogsForNotification(notificationId)
    }

    fun clearTestResult() {
        _testSenderResult.value = null
    }

    fun resendWebhook(delivery: WebhookLog, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val secret = repository.getTargetById(delivery.webhookTargetId)?.secret ?: ""
                    val response = WebhookClient.executePost(
                        delivery.webhookTargetUrl,
                        secret,
                        delivery.payload
                    )
                    
                    val updatedLog = delivery.copy(
                        status = if (response.isSuccessful) "sent" else "failed",
                        responseCode = response.code,
                        errorMessage = response.errorMessage ?: if (response.isSuccessful) null else "HTTP ${response.code}",
                        retryCount = delivery.retryCount + 1,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.updateWebhookLog(updatedLog)
                } catch (e: Exception) {
                    val updatedLog = delivery.copy(
                        status = "failed",
                        responseCode = 0,
                        errorMessage = e.message ?: e.toString(),
                        retryCount = delivery.retryCount + 1,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.updateWebhookLog(updatedLog)
                }
            }
            onComplete()
        }
    }

    // Provider Factory
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QrisViewModel(application) as T
        }
    }
}
