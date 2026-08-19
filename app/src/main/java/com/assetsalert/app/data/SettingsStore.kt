package com.assetsalert.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("assets_alert_settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val CUSTOM_SOUND_URI = stringPreferencesKey("custom_sound_uri")
        val TWELVE_DATA_KEY = stringPreferencesKey("twelve_data_api_key")
        val POLL_SECONDS = intPreferencesKey("poll_interval_seconds")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }
    suspend fun setHasSeenOnboarding(v: Boolean) = context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = v }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE] ?: true }
    val customSoundUri: Flow<String?> = context.dataStore.data.map { it[Keys.CUSTOM_SOUND_URI] }
    val twelveDataApiKey: Flow<String?> = context.dataStore.data.map { it[Keys.TWELVE_DATA_KEY] }
    val pollIntervalSeconds: Flow<Int> = context.dataStore.data.map { it[Keys.POLL_SECONDS] ?: 30 }

    suspend fun setDarkMode(v: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = v }
    suspend fun setCustomSoundUri(v: String?) = context.dataStore.edit {
        if (v == null) it.remove(Keys.CUSTOM_SOUND_URI) else it[Keys.CUSTOM_SOUND_URI] = v
    }
    suspend fun setTwelveDataApiKey(v: String) = context.dataStore.edit { it[Keys.TWELVE_DATA_KEY] = v }
    suspend fun setPollIntervalSeconds(v: Int) = context.dataStore.edit { it[Keys.POLL_SECONDS] = v }

    suspend fun currentTwelveDataApiKey(): String? = context.dataStore.data.map { it[Keys.TWELVE_DATA_KEY] }.first()
    suspend fun currentPollIntervalSeconds(): Int = context.dataStore.data.map { it[Keys.POLL_SECONDS] ?: 30 }.first()
}
