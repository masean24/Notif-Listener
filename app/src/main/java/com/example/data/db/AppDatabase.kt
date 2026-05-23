package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.NotificationLog
import com.example.data.model.WebhookLog
import com.example.data.model.WebhookTarget
import com.example.data.model.RoutingProfile
import com.example.data.model.ProfileDedupeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookTargetDao {
    @Query("SELECT * FROM webhook_targets ORDER BY id ASC")
    fun getAllTargetsFlow(): Flow<List<WebhookTarget>>

    @Query("SELECT * FROM webhook_targets WHERE enabled = 1")
    suspend fun getActiveTargets(): List<WebhookTarget>

    @Query("SELECT * FROM webhook_targets WHERE id = :id")
    suspend fun getTargetById(id: Int): WebhookTarget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: WebhookTarget): Long

    @Update
    suspend fun updateTarget(target: WebhookTarget)

    @Delete
    suspend fun deleteTarget(target: WebhookTarget)
}

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE dedupeKey = :dedupeKey AND timestamp >= :windowStart LIMIT 1")
    suspend fun getLogByDedupeKeyInWindow(dedupeKey: String, windowStart: Long): NotificationLog?

    @Query("SELECT COUNT(*) FROM notification_logs")
    fun getCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearLogs()
}

@Dao
interface WebhookLogDao {
    @Query("SELECT * FROM webhook_logs ORDER BY timestamp DESC")
    fun getAllWebhookLogsFlow(): Flow<List<WebhookLog>>

    @Query("SELECT * FROM webhook_logs WHERE status = 'pending' AND retryCount < 5")
    suspend fun getPendingLogs(): List<WebhookLog>

    @Query("SELECT * FROM webhook_logs WHERE notificationLogId = :notificationId")
    suspend fun getLogsForNotification(notificationId: Long): List<WebhookLog>

    @Query("SELECT COUNT(*) FROM webhook_logs WHERE status = 'sent'")
    fun getSuccessCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM webhook_logs WHERE status = 'failed'")
    fun getFailedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM webhook_logs WHERE status = 'pending'")
    fun getPendingCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhookLog(log: WebhookLog): Long

    @Update
    suspend fun updateWebhookLog(log: WebhookLog)

    @Query("DELETE FROM webhook_logs")
    suspend fun clearAllDeliveryLogs()
}

@Dao
interface RoutingProfileDao {
    @Query("SELECT * FROM routing_profiles ORDER BY priority DESC")
    fun getAllProfilesFlow(): Flow<List<RoutingProfile>>

    @Query("SELECT * FROM routing_profiles WHERE enabled = 1 ORDER BY priority DESC")
    suspend fun getActiveProfiles(): List<RoutingProfile>

    @Query("SELECT * FROM routing_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): RoutingProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: RoutingProfile): Long

    @Update
    suspend fun updateProfile(profile: RoutingProfile)

    @Delete
    suspend fun deleteProfile(profile: RoutingProfile)
}

@Dao
interface ProfileDedupeLogDao {
    @Query("SELECT * FROM profile_dedupe_logs WHERE profileId = :profileId AND dedupeKey = :dedupeKey AND timestamp >= :windowStart LIMIT 1")
    suspend fun getDedupeLog(profileId: Int, dedupeKey: String, windowStart: Long): ProfileDedupeLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDedupeLog(log: ProfileDedupeLog)

    @Query("DELETE FROM profile_dedupe_logs")
    suspend fun clearAllDedupeLogs()
}

@Database(
    entities = [WebhookTarget::class, NotificationLog::class, WebhookLog::class, RoutingProfile::class, ProfileDedupeLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webhookTargetDao(): WebhookTargetDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun webhookLogDao(): WebhookLogDao
    abstract fun routingProfileDao(): RoutingProfileDao
    abstract fun profileDedupeLogDao(): ProfileDedupeLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qris_bridge_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
