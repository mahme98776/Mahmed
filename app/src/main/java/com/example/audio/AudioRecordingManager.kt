package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioRecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var amplitudeJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _amplitudeHistory = MutableStateFlow<List<Float>>(emptyList())
    val amplitudeHistory: StateFlow<List<Float>> = _amplitudeHistory.asStateFlow()

    private val _recordedDurationMs = MutableStateFlow(0L)
    val recordedDurationMs: StateFlow<Long> = _recordedDurationMs.asStateFlow()

    private var currentOutputFile: File? = null

    fun startRecording(coroutineScope: CoroutineScope, onStart: (String) -> Unit) {
        stopPlayback()
        val audioDir = File(context.cacheDir, "dubbing_audio")
        if (!audioDir.exists()) audioDir.mkdirs()

        val fileName = "dub_take_${System.currentTimeMillis()}.m4a"
        val outputFile = File(audioDir, fileName)
        currentOutputFile = outputFile

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordedDurationMs.value = 0L
            _amplitudeHistory.value = emptyList()
            onStart(outputFile.absolutePath)

            // Amplitude & Duration polling loop
            val startTime = System.currentTimeMillis()
            val historyList = mutableListOf<Float>()
            amplitudeJob = coroutineScope.launch(Dispatchers.Default) {
                while (isActive && _isRecording.value) {
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _currentAmplitude.value = normalized
                        _recordedDurationMs.value = System.currentTimeMillis() - startTime
                        
                        historyList.add(normalized)
                        if (historyList.size > 80) {
                            historyList.removeAt(0)
                        }
                        _amplitudeHistory.value = historyList.toList()
                    } catch (_: Exception) {}
                    delay(50)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    fun stopRecording(): String? {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _isRecording.value = false
        _currentAmplitude.value = 0f

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        return currentOutputFile?.absolutePath
    }

    private var presetReverb: android.media.audiofx.PresetReverb? = null

    fun playAudio(
        filePath: String,
        voiceEffect: VoiceEffect = VoiceEffect.NORMAL,
        volume: Float = 1.0f,
        onComplete: () -> Unit = {}
    ) {
        val effectItem = AudioEffectsLibrary.fromVoiceEffect(voiceEffect)
        playAudioWithEffectParams(
            filePath = filePath,
            params = effectItem.defaultParams,
            volume = volume,
            onComplete = onComplete
        )
    }

    fun playAudioWithEffectParams(
        filePath: String,
        params: AudioEffectParameters,
        volume: Float = 1.0f,
        onComplete: () -> Unit = {}
    ) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setVolume(volume, volume)

                // Optional Preset Reverb
                if (params.reverbRoomSize > 0.1f) {
                    try {
                        val reverb = android.media.audiofx.PresetReverb(0, audioSessionId).apply {
                            preset = when {
                                params.reverbRoomSize > 0.8f -> android.media.audiofx.PresetReverb.PRESET_LARGEROOM
                                params.reverbRoomSize > 0.4f -> android.media.audiofx.PresetReverb.PRESET_MEDIUMROOM
                                else -> android.media.audiofx.PresetReverb.PRESET_SMALLROOM
                            }
                            enabled = true
                        }
                        presetReverb = reverb
                        attachAuxEffect(reverb.id)
                        setAuxEffectSendLevel(params.dryWetMix.coerceIn(0f, 1f))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val pParams = playbackParams
                    pParams.pitch = params.pitchMultiplier.coerceIn(0.5f, 2.0f)
                    pParams.speed = params.speedMultiplier.coerceIn(0.5f, 2.0f)
                    playbackParams = pParams
                }
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    onComplete()
                }
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun playAudioWithCustomPitch(
        filePath: String,
        pitchMultiplier: Float,
        speedMultiplier: Float,
        volume: Float = 1.0f,
        onComplete: () -> Unit = {}
    ) {
        playAudioWithEffectParams(
            filePath = filePath,
            params = AudioEffectParameters(
                pitchMultiplier = pitchMultiplier,
                speedMultiplier = speedMultiplier
            ),
            volume = volume,
            onComplete = onComplete
        )
    }

    fun stopPlayback() {
        try {
            presetReverb?.enabled = false
            presetReverb?.release()
            presetReverb = null

            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
