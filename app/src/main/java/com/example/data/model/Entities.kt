package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webhook_targets")
data class WebhookTarget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val secret: String,
    val payloadMode: String = "simple", // "simple" or "extended"
    val customTemplate: String? = null,
    val enabled: Boolean = true
)

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val bigText: String?,
    val timestamp: Long,
    val amount: Int?,
    val dedupeKey: String,
    val sender: String? = null
)

@Entity(tableName = "webhook_logs")
data class WebhookLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val webhookTargetId: Int,
    val notificationLogId: Long,
    val webhookTargetName: String,
    val webhookTargetUrl: String,
    val payload: String,
    val status: String, // "pending", "sent", "failed"
    val responseCode: Int?,
    val errorMessage: String?,
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "routing_profiles")
data class RoutingProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val enabled: Boolean = true,
    val packagesRaw: String = "", // Comma-separated package names
    val positiveKeywordsRaw: String = "", // Comma-separated positive keywords
    val negativeKeywordsRaw: String = "", // Comma-separated negative keywords
    val webhookTargetIdsRaw: String = "", // Comma-separated target webhook IDs (e.g. "1,2,5")
    val customTemplate: String? = null, // Optional payload template specific to this profile
    val ttsEnabled: Boolean = false, // If true, use profile's custom TTS template
    val ttsTemplate: String? = null, // Optional custom speaker template specific to this profile
    val priority: Int = 0 // Higher value is higher priority
) {
    val packages: List<String>
        get() = if (packagesRaw.isBlank()) emptyList() else packagesRaw.split(",")

    val positiveKeywords: List<String>
        get() = if (positiveKeywordsRaw.isBlank()) emptyList() else positiveKeywordsRaw.split(",")

    val negativeKeywords: List<String>
        get() = if (negativeKeywordsRaw.isBlank()) emptyList() else negativeKeywordsRaw.split(",")

    val webhookTargetIds: List<Int>
        get() = if (webhookTargetIdsRaw.isBlank()) emptyList() else webhookTargetIdsRaw.split(",").mapNotNull { it.toIntOrNull() }
}

@Entity(tableName = "profile_dedupe_logs", primaryKeys = ["profileId", "dedupeKey"])
data class ProfileDedupeLog(
    val profileId: Int,
    val dedupeKey: String,
    val timestamp: Long
)

