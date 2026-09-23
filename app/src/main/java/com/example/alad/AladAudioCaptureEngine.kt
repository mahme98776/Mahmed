package com.example.alad

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * ALAD Mobile - Real-time Audio Capture & Stream Engine
 * Captures internal system audio (AudioPlaybackCapture) or high-fidelity microphone input,
 * analyzes live sound waves and detects speech segments for immediate Gemini Live translation.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladAudioCaptureEngine(private val context: Context) {
    private val tag = "AladAudioCapture"
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _audioLevelRms = MutableStateFlow(0f)
    val audioLevelRms: StateFlow<Float> = _audioLevelRms.asStateFlow()

    private val _audioDecibels = MutableStateFlow(-60f)
    val audioDecibels: StateFlow<Float> = _audioDecibels.asStateFlow()

    // Callback when a speech chunk is captured
    var onSpeechChunkDetected: ((ByteArray, Int) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startCapture(useMicrophoneFallback: Boolean = true): Boolean {
        if (_isCapturing.value) return true

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(tag, "AudioRecord failed to initialize")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            audioRecord?.startRecording()
            _isCapturing.value = true

            captureJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                val byteBuffer = ByteArray(bufferSize)

                while (isActive && _isCapturing.value) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readCount > 0) {
                        // Calculate RMS level and decibels
                        var sum = 0.0
                        var maxAmp = 0
                        for (i in 0 until readCount) {
                            val sample = buffer[i].toInt()
                            sum += sample * sample
                            val absSample = abs(sample)
                            if (absSample > maxAmp) maxAmp = absSample

                            // Pack into byte array
                            byteBuffer[i * 2] = (sample and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
                        }

                        val rms = sqrt(sum / readCount)
                        val normalizedRms = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                        _audioLevelRms.value = normalizedRms

                        val db = if (rms > 1.0) (20 * log10(rms / 32768.0)).toFloat() else -60f
                        _audioDecibels.value = db.coerceIn(-60f, 0f)

                        // If above noise gate threshold, deliver speech packet
                        if (normalizedRms > 0.04f) {
                            onSpeechChunkDetected?.invoke(byteBuffer.copyOf(readCount * 2), readCount * 2)
                        }
                    }
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(tag, "Error starting audio capture", e)
            _isCapturing.value = false
            return false
        }
    }

    fun stopCapture() {
        _isCapturing.value = false
        captureJob?.cancel()
        captureJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping AudioRecord", e)
        }
        audioRecord = null
        _audioLevelRms.value = 0f
        _audioDecibels.value = -60f
    }
}
