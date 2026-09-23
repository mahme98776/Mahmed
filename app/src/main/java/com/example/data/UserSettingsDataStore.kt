package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "voice_master_user_settings")

/**
 * Encapsulates persisted user preferences retrieved locally via Jetpack DataStore.
 */
data class UserSettings(
    val languageCode: String = "ar",
    val speechRate: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val isDarkMode: Boolean = true,
    val audioDuckingVolume: Float = 0.25f,
    val selectedVoiceId: String = "male_fusha_standard",
    val hasSeenOnboarding: Boolean = false,
    val serverIp: String = "192.168.1.3",
    val serverPort: Int = 8000
)

/**
 * Repository responsible for reading and writing user preferences
 * safely and asynchronously using Jetpack DataStore Preferences.
 */
class UserSettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val LANGUAGE_CODE = stringPreferencesKey("pref_language_code")
        val SPEECH_RATE = floatPreferencesKey("pref_speech_rate")
        val VOICE_PITCH = floatPreferencesKey("pref_voice_pitch")
        val DARK_MODE = booleanPreferencesKey("pref_dark_mode")
        val AUDIO_DUCKING_VOLUME = floatPreferencesKey("pref_audio_ducking_volume")
        val SELECTED_VOICE_ID = stringPreferencesKey("pref_selected_voice_id")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("pref_has_seen_onboarding")
        val SERVER_IP = stringPreferencesKey("pref_server_ip")
        val SERVER_PORT = androidx.datastore.preferences.core.intPreferencesKey("pref_server_port")
    }

    val userSettingsFlow: Flow<UserSettings> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                languageCode = preferences[PreferencesKeys.LANGUAGE_CODE] ?: "ar",
                speechRate = preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
                voicePitch = preferences[PreferencesKeys.VOICE_PITCH] ?: 1.0f,
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: true,
                audioDuckingVolume = preferences[PreferencesKeys.AUDIO_DUCKING_VOLUME] ?: 0.25f,
                selectedVoiceId = preferences[PreferencesKeys.SELECTED_VOICE_ID] ?: "male_fusha_standard",
                hasSeenOnboarding = preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false,
                serverIp = preferences[PreferencesKeys.SERVER_IP] ?: "192.168.1.3",
                serverPort = preferences[PreferencesKeys.SERVER_PORT] ?: 8000
            )
        }

    suspend fun updateLanguage(code: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = code
        }
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SPEECH_RATE] = rate.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun updateVoicePitch(pitch: Float) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE_PITCH] = pitch.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun updateAudioDuckingVolume(volume: Float) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_DUCKING_VOLUME] = volume.coerceIn(0.05f, 0.9f)
        }
    }

    suspend fun updateSelectedVoiceId(voiceId: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_VOICE_ID] = voiceId
        }
    }

    suspend fun updateHasSeenOnboarding(hasSeen: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = hasSeen
        }
    }

    suspend fun updateServerConfiguration(ip: String, port: Int) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_IP] = ip.trim()
            preferences[PreferencesKeys.SERVER_PORT] = port.coerceIn(1, 65535)
        }
    }

    suspend fun resetToDefaults() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = "ar"
            preferences[PreferencesKeys.SPEECH_RATE] = 1.0f
            preferences[PreferencesKeys.VOICE_PITCH] = 1.0f
            preferences[PreferencesKeys.DARK_MODE] = true
            preferences[PreferencesKeys.AUDIO_DUCKING_VOLUME] = 0.25f
            preferences[PreferencesKeys.SELECTED_VOICE_ID] = "male_fusha_standard"
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = false
            preferences[PreferencesKeys.SERVER_IP] = "192.168.1.3"
            preferences[PreferencesKeys.SERVER_PORT] = 8000
        }
    }
}
