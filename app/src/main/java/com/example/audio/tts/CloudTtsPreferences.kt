package com.example.audio.tts

import android.content.Context
import android.content.SharedPreferences

class CloudTtsPreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cloud_tts_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROVIDER = "tts_provider"
        private const val KEY_ELEVEN_LABS_KEY = "eleven_labs_api_key"
        private const val KEY_ELEVEN_LABS_VOICE_ID = "eleven_labs_voice_id"
        private const val KEY_GOOGLE_CLOUD_KEY = "google_cloud_api_key"
        private const val KEY_GOOGLE_CLOUD_VOICE = "google_cloud_voice_name"
        private const val KEY_SPEAKING_RATE = "tts_speaking_rate"
        private const val KEY_PITCH = "tts_pitch"
        private const val KEY_STABILITY = "tts_stability"
        private const val KEY_SIMILARITY = "tts_similarity"
        private const val KEY_IS_ENABLED = "tts_is_enabled"
    }

    fun loadConfig(): CloudTtsConfig {
        val providerStr = prefs.getString(KEY_PROVIDER, CloudTtsProvider.GOOGLE_CLOUD_TTS.name)
        val provider = try {
            CloudTtsProvider.valueOf(providerStr ?: CloudTtsProvider.GOOGLE_CLOUD_TTS.name)
        } catch (_: Exception) {
            CloudTtsProvider.GOOGLE_CLOUD_TTS
        }

        val aiPrefs = context.getSharedPreferences("app_ai_prefs", Context.MODE_PRIVATE)
        val savedAiKey = aiPrefs.getString("gemini_api_key", "") ?: ""

        val rawGoogleKey = prefs.getString(KEY_GOOGLE_CLOUD_KEY, "") ?: ""
        val effectiveGoogleKey = if (rawGoogleKey.isNotBlank()) rawGoogleKey else savedAiKey

        return CloudTtsConfig(
            provider = provider,
            elevenLabsApiKey = prefs.getString(KEY_ELEVEN_LABS_KEY, "") ?: "",
            elevenLabsVoiceId = prefs.getString(KEY_ELEVEN_LABS_VOICE_ID, "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM",
            googleCloudApiKey = effectiveGoogleKey,
            googleCloudVoiceName = prefs.getString(KEY_GOOGLE_CLOUD_VOICE, "ar-XA-Wavenet-B") ?: "ar-XA-Wavenet-B",
            speakingRate = prefs.getFloat(KEY_SPEAKING_RATE, 1.0f),
            pitch = prefs.getFloat(KEY_PITCH, 0.0f),
            stability = prefs.getFloat(KEY_STABILITY, 0.50f),
            similarityBoost = prefs.getFloat(KEY_SIMILARITY, 0.75f),
            isEnabled = prefs.getBoolean(KEY_IS_ENABLED, true)
        )
    }

    fun saveConfig(config: CloudTtsConfig) {
        prefs.edit().apply {
            putString(KEY_PROVIDER, config.provider.name)
            putString(KEY_ELEVEN_LABS_KEY, config.elevenLabsApiKey)
            putString(KEY_ELEVEN_LABS_VOICE_ID, config.elevenLabsVoiceId)
            putString(KEY_GOOGLE_CLOUD_KEY, config.googleCloudApiKey)
            putString(KEY_GOOGLE_CLOUD_VOICE, config.googleCloudVoiceName)
            putFloat(KEY_SPEAKING_RATE, config.speakingRate)
            putFloat(KEY_PITCH, config.pitch)
            putFloat(KEY_STABILITY, config.stability)
            putFloat(KEY_SIMILARITY, config.similarityBoost)
            putBoolean(KEY_IS_ENABLED, config.isEnabled)
            apply()
        }

        // Also ensure AI preferences share the Google Cloud / Gemini API key permanently
        if (config.googleCloudApiKey.isNotBlank()) {
            val aiPrefs = context.getSharedPreferences("app_ai_prefs", Context.MODE_PRIVATE)
            aiPrefs.edit().putString("gemini_api_key", config.googleCloudApiKey.trim()).apply()
        }
    }
}
