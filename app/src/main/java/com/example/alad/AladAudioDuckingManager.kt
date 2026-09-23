package com.example.alad

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ALAD Mobile - Intelligent Audio Ducking Manager
 * Dynamically adjusts system media audio volume and audio focus so that the dubbed voice
 * cuts through clearly while external applications (YouTube, Spotify, Netflix) are active.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladAudioDuckingManager(private val context: Context) {
    private val tag = "AladAudioDucking"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isDuckingActive = MutableStateFlow(false)
    val isDuckingActive: StateFlow<Boolean> = _isDuckingActive.asStateFlow()

    private val _duckingPercentage = MutableStateFlow(75) // 75% reduction (background at 25%)
    val duckingPercentage: StateFlow<Int> = _duckingPercentage.asStateFlow()

    private var previousMediaVolume: Int? = null
    private var focusRequest: AudioFocusRequest? = null
    private var restoreJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setDuckingPercentage(percentage: Int) {
        _duckingPercentage.value = percentage.coerceIn(0, 100)
    }

    /**
     * Start ducking: lower active app volume or request transient audio focus with ducking.
     */
    fun startDucking() {
        restoreJob?.cancel()
        if (_isDuckingActive.value) return

        try {
            _isDuckingActive.value = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(tag, "Audio focus change: $focusChange")
                    }
                    .build()

                focusRequest = request
                audioManager?.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }

            // Direct volume compensation if needed
            audioManager?.let { am ->
                val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (previousMediaVolume == null) {
                    previousMediaVolume = currentVol
                }
                val factor = (100 - _duckingPercentage.value).coerceAtLeast(10) / 100f
                val targetVol = (currentVol * factor).toInt().coerceAtLeast(1)
                try {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                } catch (e: Exception) {
                    Log.w(tag, "Could not adjust stream volume directly: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start ducking", e)
        }
    }

    /**
     * Stop ducking and smoothly restore background audio volume after dub speech ends.
     */
    fun stopDucking(delayMs: Long = 400L) {
        restoreJob?.cancel()
        restoreJob = scope.launch {
            delay(delayMs)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.abandonAudioFocus(null)
                }

                // Restore previous volume if saved
                previousMediaVolume?.let { origVol ->
                    try {
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, origVol, 0)
                    } catch (e: Exception) {
                        Log.w(tag, "Could not restore stream volume: ${e.message}")
                    }
                    previousMediaVolume = null
                }
                _isDuckingActive.value = false
            } catch (e: Exception) {
                Log.e(tag, "Failed to stop ducking", e)
                _isDuckingActive.value = false
            }
        }
    }

    fun release() {
        restoreJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
        previousMediaVolume?.let { origVol ->
            try {
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, origVol, 0)
            } catch (_: Exception) {}
        }
        _isDuckingActive.value = false
    }
}
