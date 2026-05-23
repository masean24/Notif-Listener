package com.example.util

import com.example.data.model.NotificationLog
import com.example.data.model.WebhookTarget
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ResponseInfo(
    val code: Int,
    val body: String?,
    val isSuccessful: Boolean,
    val errorMessage: String? = null
)

object WebhookClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun executePost(url: String, secret: String, jsonPayload: String): ResponseInfo {
        val requestBody = jsonPayload.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("X-Webhook-Secret", secret)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                ResponseInfo(
                    code = response.code,
                    body = response.body?.string(),
                    isSuccessful = response.isSuccessful,
                    errorMessage = if (!response.isSuccessful) "HTTP ${response.code}" else null
                )
            }
        } catch (e: Exception) {
            ResponseInfo(
                code = 0,
                body = null,
                isSuccessful = false,
                errorMessage = e.message ?: e.toString()
            )
        }
    }
}

object PayloadBuilder {

    fun buildPayload(
        target: WebhookTarget,
        log: NotificationLog,
        deviceId: String
    ): String {
        val customTemplate = target.customTemplate
        if (!customTemplate.isNullOrBlank()) {
            try {
                val formatted = replacePlaceholders(customTemplate, log, deviceId)
                // Validate if it is valid JSON
                JSONObject(formatted)
                return formatted
            } catch (e: Exception) {
                // If template formatting fails, we fall back to simple/extended format defaults.
            }
        }

        return if (target.payloadMode == "extended") {
            val json = JSONObject()
            json.put("event", "payment_notification")
            json.put("message", log.text)
            json.put("app_package", log.packageName)
            json.put("app_name", log.appName)
            json.put("title", log.title)
            json.put("text", log.text)
            json.put("big_text", log.bigText ?: "")
            if (log.amount != null) {
                json.put("amount", log.amount)
            } else {
                json.put("amount", JSONObject.NULL)
            }
            json.put("sender", log.sender ?: "")
            json.put("currency", "IDR")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            json.put("timestamp", sdf.format(Date(log.timestamp)))
            json.put("device_id", deviceId)
            json.put("dedupe_key", log.dedupeKey)
            
            json.toString()
        } else {
            // "simple" / MengQRIS mode
            val json = JSONObject()
            json.put("message", log.bigText ?: log.text)
            json.toString()
        }
    }

    private fun replacePlaceholders(
        template: String,
        log: NotificationLog,
        deviceId: String
    ): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        return template
            .replace("{app_name}", log.appName)
            .replace("{app_package}", log.packageName)
            .replace("{title}", log.title)
            .replace("{text}", log.text)
            .replace("{big_text}", log.bigText ?: "")
            .replace("{amount}", log.amount?.toString() ?: "0")
            .replace("{sender}", log.sender ?: "")
            .replace("{timestamp}", sdf.format(Date(log.timestamp)))
            .replace("{device_id}", deviceId)
            .replace("{dedupe_key}", log.dedupeKey)
    }
}
