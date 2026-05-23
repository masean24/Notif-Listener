package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.util.WebhookClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebhookRetryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val webhookLogDao = database.webhookLogDao()
        val targetDao = database.webhookTargetDao()

        val pendingLogs = webhookLogDao.getPendingLogs()
        if (pendingLogs.isEmpty()) {
            return@withContext Result.success()
        }

        var anyFailed = false

        for (log in pendingLogs) {
            val target = targetDao.getTargetById(log.webhookTargetId)
            val secret = target?.secret ?: ""

            try {
                val response = WebhookClient.executePost(log.webhookTargetUrl, secret, log.payload)
                if (response.isSuccessful) {
                    val updatedLog = log.copy(
                        status = "sent",
                        responseCode = response.code,
                        errorMessage = null,
                        retryCount = log.retryCount + 1
                    )
                    webhookLogDao.updateWebhookLog(updatedLog)
                } else {
                    val newRetryCount = log.retryCount + 1
                    val newStatus = if (newRetryCount >= 5) "failed" else "pending"
                    val updatedLog = log.copy(
                        status = newStatus,
                        responseCode = response.code,
                        errorMessage = response.errorMessage ?: "HTTP ${response.code}",
                        retryCount = newRetryCount
                    )
                    webhookLogDao.updateWebhookLog(updatedLog)
                    anyFailed = true
                }
            } catch (e: Exception) {
                val newRetryCount = log.retryCount + 1
                val newStatus = if (newRetryCount >= 5) "failed" else "pending"
                val updatedLog = log.copy(
                    status = newStatus,
                    responseCode = 0,
                    errorMessage = e.message ?: e.toString(),
                    retryCount = newRetryCount
                )
                webhookLogDao.updateWebhookLog(updatedLog)
                anyFailed = true
            }
        }

        if (anyFailed) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
