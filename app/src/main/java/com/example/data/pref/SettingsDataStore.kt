package com.example.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qris_bridge_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        private val SPEAKER_ENABLED_KEY = booleanPreferencesKey("speaker_enabled")
        private val SPEAKER_REPEAT_KEY = intPreferencesKey("speaker_repeat")
        private val SPEAKER_VOLUME_KEY = floatPreferencesKey("speaker_volume")
        private val SPEAKER_TEMPLATE_KEY = stringPreferencesKey("speaker_template")
        private val ALLOWED_PACKAGES_KEY = stringPreferencesKey("allowed_packages")
        private val POSITIVE_KEYWORDS_KEY = stringPreferencesKey("positive_keywords")
        private val NEGATIVE_KEYWORDS_KEY = stringPreferencesKey("negative_keywords")
        private val ROUTING_MODE_ONLY_FIRST_KEY = booleanPreferencesKey("routing_mode_only_first")
        
        val DEFAULT_PACKAGES = listOf(
            "com.gojek.app",
            "id.dana",
            "com.shopeepay.id",
            "com.bca",
            "com.bankmandiri.mandirionline",
            "com.linkaja",
            "id.co.bi.qr"
        )

        val DEFAULT_POSITIVE_KEYWORDS = listOf(
            "pembayaran diterima",
            "transfer masuk",
            "saldo masuk",
            "uang masuk",
            "berhasil menerima",
            "payment received",
            "received",
            "berhasil"
        )

        val DEFAULT_NEGATIVE_KEYWORDS = listOf(
            "promo",
            "cashback",
            "voucher",
            "diskon",
            "otp",
            "kode",
            "password",
            "login"
        )
    }

    val deviceIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val id = preferences[DEVICE_ID_KEY]
        if (id.isNullOrEmpty()) {
            val newId = UUID.randomUUID().toString()
            saveDeviceId(newId)
            newId
        } else {
            id
        }
    }

    suspend fun saveDeviceId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = id
        }
    }

    val isSpeakerEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SPEAKER_ENABLED_KEY] ?: true
    }

    suspend fun setSpeakerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SPEAKER_ENABLED_KEY] = enabled
        }
    }

    val speakerRepeatFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SPEAKER_REPEAT_KEY] ?: 1
    }

    suspend fun setSpeakerRepeat(repeat: Int) {
        context.dataStore.edit { preferences ->
            preferences[SPEAKER_REPEAT_KEY] = repeat.coerceIn(1, 3)
        }
    }

    val speakerVolumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SPEAKER_VOLUME_KEY] ?: 1.0f
    }

    suspend fun setSpeakerVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPEAKER_VOLUME_KEY] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    val speakerTemplateFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SPEAKER_TEMPLATE_KEY] ?: "Pembayaran masuk {amount} rupiah dari {app_name}"
    }

    suspend fun setSpeakerTemplate(template: String) {
        context.dataStore.edit { preferences ->
            preferences[SPEAKER_TEMPLATE_KEY] = template
        }
    }

    val allowedPackagesFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[ALLOWED_PACKAGES_KEY]
        if (raw == null) {
            DEFAULT_PACKAGES
        } else if (raw.trim().isEmpty()) {
            emptyList()
        } else {
            raw.split(",")
        }
    }

    suspend fun saveAllowedPackages(packages: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[ALLOWED_PACKAGES_KEY] = packages.joinToString(",")
        }
    }

    val positiveKeywordsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[POSITIVE_KEYWORDS_KEY]
        if (raw == null) {
            DEFAULT_POSITIVE_KEYWORDS
        } else if (raw.trim().isEmpty()) {
            emptyList()
        } else {
            raw.split(",")
        }
    }

    suspend fun savePositiveKeywords(keywords: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[POSITIVE_KEYWORDS_KEY] = keywords.joinToString(",")
        }
    }

    val negativeKeywordsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[NEGATIVE_KEYWORDS_KEY]
        if (raw == null) {
            DEFAULT_NEGATIVE_KEYWORDS
        } else if (raw.trim().isEmpty()) {
            emptyList()
        } else {
            raw.split(",")
        }
    }

    suspend fun saveNegativeKeywords(keywords: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[NEGATIVE_KEYWORDS_KEY] = keywords.joinToString(",")
        }
    }

    val isRoutingModeOnlyFirstFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ROUTING_MODE_ONLY_FIRST_KEY] ?: false
    }

    suspend fun setRoutingModeOnlyFirst(onlyFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ROUTING_MODE_ONLY_FIRST_KEY] = onlyFirst
        }
    }
}
