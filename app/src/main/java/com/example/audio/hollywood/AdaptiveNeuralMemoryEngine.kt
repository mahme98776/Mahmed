package com.example.audio.hollywood

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserCreativePreferences(
    val favoriteDialect: String = "egyptian",
    val defaultDubbingVolume: Float = 1.0f,
    val defaultOriginalVideoVolume: Float = 0.25f,
    val defaultMusicVolume: Float = 0.4f,
    val preferredVoiceStyle: String = "CINEMATIC_WARM",
    val completedProjectsCount: Int = 1,
    val autoDenoiseEnabled: Boolean = true,
    val autoLipSyncEnabled: Boolean = true
)

/**
 * Adaptive Neural Long-Term Memory & User Preference Engine.
 * Remembers user creative habits, preferred voices, mixing ratios,
 * and dialect tastes so Alexa adapts proactively.
 */
class AdaptiveNeuralMemoryEngine(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("alexa_neural_memory_prefs", Context.MODE_PRIVATE)

    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserCreativePreferences> = _userPreferences.asStateFlow()

    private fun loadPreferences(): UserCreativePreferences {
        return UserCreativePreferences(
            favoriteDialect = prefs.getString("favorite_dialect", "egyptian") ?: "egyptian",
            defaultDubbingVolume = prefs.getFloat("default_dub_vol", 1.0f),
            defaultOriginalVideoVolume = prefs.getFloat("default_orig_vol", 0.25f),
            defaultMusicVolume = prefs.getFloat("default_music_vol", 0.4f),
            preferredVoiceStyle = prefs.getString("preferred_voice_style", "CINEMATIC_WARM") ?: "CINEMATIC_WARM",
            completedProjectsCount = prefs.getInt("completed_projects", 3),
            autoDenoiseEnabled = prefs.getBoolean("auto_denoise", true),
            autoLipSyncEnabled = prefs.getBoolean("auto_lip_sync", true)
        )
    }

    /**
     * Records a finished project and refines adaptive memory.
     */
    fun recordProjectCompletion(dialect: String, voiceStyle: String) {
        val current = _userPreferences.value
        val updated = current.copy(
            favoriteDialect = dialect,
            preferredVoiceStyle = voiceStyle,
            completedProjectsCount = current.completedProjectsCount + 1
        )
        prefs.edit()
            .putString("favorite_dialect", updated.favoriteDialect)
            .putString("preferred_voice_style", updated.preferredVoiceStyle)
            .putInt("completed_projects", updated.completedProjectsCount)
            .apply()
        _userPreferences.value = updated
    }

    /**
     * Updates mixing volume preferences learned from user adjustments.
     */
    fun updateMixPreferences(dubVol: Float, origVol: Float, musicVol: Float) {
        val updated = _userPreferences.value.copy(
            defaultDubbingVolume = dubVol,
            defaultOriginalVideoVolume = origVol,
            defaultMusicVolume = musicVol
        )
        prefs.edit()
            .putFloat("default_dub_vol", dubVol)
            .putFloat("default_orig_vol", origVol)
            .putFloat("default_music_vol", musicVol)
            .apply()
        _userPreferences.value = updated
    }
}
