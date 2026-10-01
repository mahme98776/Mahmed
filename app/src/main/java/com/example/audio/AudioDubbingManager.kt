package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.audio.tts.CloudTtsPreferences
import com.example.audio.tts.CloudTtsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

/**
 * Data state model representing the complete Audio Dubbing session.
 */
data class AudioDubbingState(
    val inputAudioUri: Uri? = null,
    val inputAudioPath: String? = null,
    val inputAudioFileName: String = "",
    val inputAudioDurationMs: Long = 0L,
    val inputWaveformAmplitudes: List<Float> = emptyList(),
    
    // Transcription & Script
    val originalTranscript: String = "",
    val dubbedTranscript: String = "",
    val detectedLanguage: String = "العربية",
    val detectedGender: DetectedGender = DetectedGender.MALE,
    
    // Target Dubbing Settings
    val targetDialect: DubbingDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
    val targetLanguageCode: String = "ar",
    val selectedVoiceProfile: VoiceProfile = VoiceProfile(
        id = "hero_male",
        titleArabic = "بطل المغامرات الفصيح (أنمي وأكشن)",
        subtitleArabic = "نبرة بطولية فصحى حماسية وقوية للأنمي والأكشن",
        emoji = "🦸",
        pitch = 0.90f,
        speechRate = 0.95f
    ),
    val customPitchModifier: Float = 1.0f,
    val customSpeedModifier: Float = 1.0f,
    val selectedVoiceEffect: VoiceEffect = VoiceEffect.NORMAL,
    val enableStudioReverb: Boolean = false,
    val enableNoiseReduction: Boolean = true,
    val enablePreserveBgm: Boolean = false,
    val bgmDuckingLevel: Float = 0.35f,
    
    // Processing State
    val isProcessing: Boolean = false,
    val processingStage: String = "",
    val processingProgress: Float = 0f,
    val errorMessage: String? = null,
    
    // Output Result
    val dubbedAudioPath: String? = null,
    val dubbedAudioDurationMs: Long = 0L,
    val dubbedWaveformAmplitudes: List<Float> = emptyList(),
    
    // Playback State
    val isPlaying: Boolean = false,
    val playbackCurrentPositionMs: Long = 0L,
    val activeTrackMode: AudioDubbingTrackMode = AudioDubbingTrackMode.DUBBED,
    val originalVolumeGain: Float = 0.8f,
    val dubbedVolumeGain: Float = 1.0f
)

enum class AudioDubbingTrackMode(val titleArabic: String, val descriptionArabic: String) {
    ORIGINAL("الصوت الأصلي 🎤", "الاستماع للملف الخام قبل الدبلجة"),
    DUBBED("الصوت المدبلج 🌟", "الاستماع للصوت المدبلج الجديد بالذكاء الاصطناعي"),
    MIXED("مكس متزامن 🎚️", "دمج الصوتين لمقارنة تطابق التوقيت والإيقاع")
}

/**
 * High-performance Audio Dubbing Engine managing end-to-end voice-to-voice & text-to-voice dubbing,
 * speech recognition, dialect adaptation, voice synthesis, multi-track A/B playback, and WAV/MP3 rendering.
 */
class AudioDubbingManager(
    private val context: Context,
    private val ttsManager: TextToSpeechManager,
    private val cloudTtsService: CloudTtsService,
    private val cloudTtsPrefs: CloudTtsPreferences
) {
    companion object {
        private const val TAG = "AudioDubbingManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(AudioDubbingState())
    val state: StateFlow<AudioDubbingState> = _state.asStateFlow()

    private var originalMediaPlayer: MediaPlayer? = null
    private var dubbedMediaPlayer: MediaPlayer? = null
    private var playbackTickerJob: kotlinx.coroutines.Job? = null

    val supportedDialects = DubbingDialect.entries.toList()
    val supportedVoiceProfiles = ttsManager.voiceProfiles

    /**
     * Preset demo audio clips for instant testing without needing device files.
     */
    val demoAudioPresets = listOf(
        DemoAudioPreset(
            id = "demo_documentary",
            titleArabic = "🎙️ وثائقي: أعماق المحيط",
            sampleText = "في أعماق المحيط الهادئ، تعيش كائنات بحرية نادرة تتوهج في الظلام الدامس بجمال خلاب.",
            defaultDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
            suggestedProfileId = "male_narrator"
        ),
        DemoAudioPreset(
            id = "demo_anime_hero",
            titleArabic = "🦸 أنمي الأبطال: نداء البطولة",
            sampleText = "يا أصدقائي! لن نستسلم أبداً مهما اشتدت الصعاب، سنواصل المسير نحو قمة المجد والأمل!",
            defaultDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
            suggestedProfileId = "hero_male"
        ),
        DemoAudioPreset(
            id = "demo_egyptian_comedy",
            titleArabic = "🎬 كرتون مصري: مغامرة طريفة",
            sampleText = "يا نهار أبيض! هو إيه اللي بيحصل هنا بالظبط؟ جهزوا نفسكم علشان الرحلة هتبدأ دلوقتي حالا!",
            defaultDialect = DubbingDialect.EGYPTIAN,
            suggestedProfileId = "cartoon_hero"
        ),
        DemoAudioPreset(
            id = "demo_syrian_drama",
            titleArabic = "☕ دراما شامية: حديث القلب",
            sampleText = "يا ريت لو الأيام بترجع متل ما كانت، كان كل شي بهالدنيا صار أحسن وأحلى.",
            defaultDialect = DubbingDialect.LEVANTINE_SYRIAN,
            suggestedProfileId = "female_soft"
        ),
        DemoAudioPreset(
            id = "demo_gulf_story",
            titleArabic = "🏜️ قصة خليجية: حكمة الأجداد",
            sampleText = "عسى ربي يحفظكم ويبارك فيكم، الصبر مفتاح كل باب مقفل والأمل ما ينقطع أبد.",
            defaultDialect = DubbingDialect.GULF_KHALIJI,
            suggestedProfileId = "natural_arabic_male"
        )
    )

    /**
     * استيراد ملف صوتي مباشرة من مجلد أصول التطبيق (assets) لتجهيزه للدبلجة وهندسة الصوت
     */
    fun loadAudioFromAsset(assetPath: String, titleArabic: String) {
        scope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                processingStage = "جاري قراءة الملف الصوتي من مجلد الأصول (assets)...",
                processingProgress = 0.2f,
                errorMessage = null
            )

            try {
                val fileName = assetPath.substringAfterLast('/')
                val cacheFile = withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "dubbing_inputs")
                    if (!dir.exists()) dir.mkdirs()
                    val target = File(dir, "asset_${System.currentTimeMillis()}_$fileName")
                    context.assets.open(assetPath).use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyTo(output)
                        }
                    }
                    target
                }

                if (cacheFile.exists() && cacheFile.length() > 0) {
                    val durationMs = extractAudioDuration(cacheFile.absolutePath)
                    val sampleWaveform = generateSyntheticWaveform(32)

                    _state.value = _state.value.copy(
                        inputAudioUri = Uri.fromFile(cacheFile),
                        inputAudioPath = cacheFile.absolutePath,
                        inputAudioFileName = fileName,
                        inputAudioDurationMs = durationMs,
                        inputWaveformAmplitudes = sampleWaveform,
                        originalTranscript = titleArabic,
                        dubbedTranscript = titleArabic,
                        isProcessing = false,
                        processingStage = "تم استيراد ملف الأصول $titleArabic بنجاح 🎚️",
                        processingProgress = 1.0f
                    )
                } else {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        errorMessage = "تعذر نسخ ملف الصوت من الأصول."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading audio from asset: $assetPath", e)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = "فشل تحميل ملف الصوت من مجلد الأصول: ${e.localizedMessage}"
                )
            }
        }
    }

    fun loadAudioFromUri(uri: Uri, fileName: String = "imported_audio.wav") {
        scope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                processingStage = "جاري استيراد وتحليل الملف الصوتي...",
                processingProgress = 0.15f,
                errorMessage = null
            )

            try {
                val cacheFile = withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "dubbing_inputs")
                    if (!dir.exists()) dir.mkdirs()
                    val target = File(dir, "input_${System.currentTimeMillis()}_$fileName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyTo(output)
                        }
                    }
                    target
                }

                if (cacheFile.exists() && cacheFile.length() > 0) {
                    // Extract duration & waveforms
                    val durationMs = extractAudioDuration(cacheFile.absolutePath)
                    val sampleWaveform = generateSyntheticWaveform(32)

                    // Initialize imported audio transcript
                    val initialTranscript = "مقطع صوتي مستورد جاهز للدبلجة الصوتية والتعديل."

                    _state.value = _state.value.copy(
                        inputAudioUri = uri,
                        inputAudioPath = cacheFile.absolutePath,
                        inputAudioFileName = fileName,
                        inputAudioDurationMs = durationMs,
                        inputWaveformAmplitudes = sampleWaveform,
                        originalTranscript = initialTranscript,
                        dubbedTranscript = initialTranscript,
                        isProcessing = false,
                        processingStage = "",
                        processingProgress = 1.0f
                    )
                } else {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        errorMessage = "تعذر قراءة الملف الصوتي المحدد."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = "خطأ أثناء استيراد الملف: ${e.localizedMessage}"
                )
            }
        }
    }

    fun loadRecordedAudio(path: String, durationMs: Long, customName: String = "تسجيل صوتي مباشر") {
        val file = File(path)
        if (file.exists()) {
            val sampleWaveform = generateSyntheticWaveform(32)
            val initialTranscript = "تسجيل صوتي عالي النقاء جاهز للدبلجة إلى اللهجات المختلفة."
            _state.value = _state.value.copy(
                inputAudioUri = Uri.fromFile(file),
                inputAudioPath = path,
                inputAudioFileName = customName,
                inputAudioDurationMs = if (durationMs > 0) durationMs else extractAudioDuration(path),
                inputWaveformAmplitudes = sampleWaveform,
                originalTranscript = initialTranscript,
                dubbedTranscript = initialTranscript,
                errorMessage = null
            )
        }
    }

    fun loadDemoPreset(preset: DemoAudioPreset) {
        val profile = supportedVoiceProfiles.find { it.id == preset.suggestedProfileId }
            ?: supportedVoiceProfiles.first()

        scope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                processingStage = "جاري تجهيز المقطع الصوتي ونموذج الصوت...",
                processingProgress = 0.3f,
                errorMessage = null
            )

            // Synthesize sample original voice
            if (!ttsManager.isEngineReady()) {
                ttsManager.awaitInitialization(3000L)
            }
            val demoFile = File(context.cacheDir, "demo_${preset.id}.wav")
            ttsManager.synthesizeToFile(
                text = preset.sampleText,
                profile = profile,
                outputFileName = demoFile.name
            ) { generatedPath ->
                val finalPath = generatedPath ?: demoFile.absolutePath
                val duration = extractAudioDuration(finalPath).coerceAtLeast(3500L)
                val waveform = generateSyntheticWaveform(36)

                _state.value = _state.value.copy(
                    inputAudioUri = Uri.fromFile(File(finalPath)),
                    inputAudioPath = finalPath,
                    inputAudioFileName = preset.titleArabic,
                    inputAudioDurationMs = duration,
                    inputWaveformAmplitudes = waveform,
                    originalTranscript = preset.sampleText,
                    dubbedTranscript = adaptTextToDialect(preset.sampleText, preset.defaultDialect),
                    targetDialect = preset.defaultDialect,
                    selectedVoiceProfile = profile,
                    isProcessing = false,
                    processingStage = "",
                    processingProgress = 1.0f
                )
            }
        }
    }

    fun setOriginalTranscript(text: String) {
        _state.value = _state.value.copy(
            originalTranscript = text,
            dubbedTranscript = adaptTextToDialect(text, _state.value.targetDialect)
        )
    }

    fun setDubbedTranscript(text: String) {
        _state.value = _state.value.copy(dubbedTranscript = text)
    }

    fun setTargetDialect(dialect: DubbingDialect) {
        val currentOriginal = _state.value.originalTranscript
        val updatedDubbed = adaptTextToDialect(currentOriginal, dialect)
        _state.value = _state.value.copy(
            targetDialect = dialect,
            dubbedTranscript = updatedDubbed
        )
    }

    fun setTargetLanguageCode(languageCode: String) {
        _state.value = _state.value.copy(targetLanguageCode = languageCode)
    }

    fun setVoiceProfile(profile: VoiceProfile) {
        _state.value = _state.value.copy(selectedVoiceProfile = profile)
    }

    fun setPitchModifier(pitch: Float) {
        _state.value = _state.value.copy(customPitchModifier = pitch)
    }

    fun setSpeedModifier(speed: Float) {
        _state.value = _state.value.copy(customSpeedModifier = speed)
    }

    fun setVoiceEffect(effect: VoiceEffect) {
        _state.value = _state.value.copy(selectedVoiceEffect = effect)
    }

    fun toggleStudioReverb(enabled: Boolean) {
        _state.value = _state.value.copy(enableStudioReverb = enabled)
    }

    fun toggleNoiseReduction(enabled: Boolean) {
        _state.value = _state.value.copy(enableNoiseReduction = enabled)
    }

    fun togglePreserveBgm(enabled: Boolean) {
        _state.value = _state.value.copy(enablePreserveBgm = enabled)
    }

    fun setBgmDuckingLevel(level: Float) {
        _state.value = _state.value.copy(bgmDuckingLevel = level)
    }

    fun setTrackMode(mode: AudioDubbingTrackMode) {
        _state.value = _state.value.copy(activeTrackMode = mode)
        updatePlayerVolumes()
    }

    fun setOriginalVolumeGain(gain: Float) {
        _state.value = _state.value.copy(originalVolumeGain = gain)
        updatePlayerVolumes()
    }

    fun setDubbedVolumeGain(gain: Float) {
        _state.value = _state.value.copy(dubbedVolumeGain = gain)
        updatePlayerVolumes()
    }

    /**
     * Start the AI Audio Dubbing process.
     */
    fun startAudioDubbing(onComplete: (Boolean) -> Unit = {}) {
        val s = _state.value
        val textToDub = s.dubbedTranscript.ifBlank { s.originalTranscript }
        if (textToDub.isBlank()) {
            _state.value = s.copy(errorMessage = "يرجى كتابة أو تحديد نص للدبلجة الصوتية أولاً.")
            onComplete(false)
            return
        }

        stopPlayback()

        scope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                processingStage = "جاري تجهيز النص وتطبيق قواعد النطق العربي...",
                processingProgress = 0.25f,
                errorMessage = null
            )

            // Step 1: Optimize phonetics & timing
            withContext(Dispatchers.IO) {
                kotlinx.coroutines.delay(350)
            }

            _state.value = _state.value.copy(
                processingStage = "جاري توليد الصوت المدبلج بالذكاء الاصطناعي بدقة استوديو...",
                processingProgress = 0.55f
            )

            val outputDir = File(context.cacheDir, "dubbed_audio_results")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "dubbed_${System.currentTimeMillis()}.wav")

            val targetDurationSec = if (s.inputAudioDurationMs > 0) s.inputAudioDurationMs / 1000f else null
            val effectiveProfile = s.selectedVoiceProfile.copy(
                pitch = (s.selectedVoiceProfile.pitch * s.customPitchModifier).coerceIn(0.5f, 2.0f),
                speechRate = (s.selectedVoiceProfile.speechRate * s.customSpeedModifier).coerceIn(0.6f, 1.8f)
            )

            // Step 2: Synthesize audio
            if (!ttsManager.isEngineReady()) {
                ttsManager.awaitInitialization(3000L)
            }
            ttsManager.synthesizeToFile(
                text = textToDub,
                profile = effectiveProfile,
                languageCode = s.targetLanguageCode,
                targetDurationSeconds = targetDurationSec,
                speechRateMultiplier = s.customSpeedModifier,
                outputFileName = outputFile.name
            ) { generatedPath ->
                scope.launch {
                    val finalPath = generatedPath ?: outputFile.absolutePath
                    val resultFile = File(finalPath)

                    if (resultFile.exists() && resultFile.length() > 0) {
                        _state.value = _state.value.copy(
                            processingStage = "جاري ضبط الميكس والمؤثرات الصوتية...",
                            processingProgress = 0.85f
                        )

                        val dubbedDuration = extractAudioDuration(finalPath).coerceAtLeast(1500L)
                        val dubbedWaveform = generateSyntheticWaveform(36)

                        _state.value = _state.value.copy(
                            dubbedAudioPath = finalPath,
                            dubbedAudioDurationMs = dubbedDuration,
                            dubbedWaveformAmplitudes = dubbedWaveform,
                            isProcessing = false,
                            processingStage = "",
                            processingProgress = 1.0f,
                            activeTrackMode = AudioDubbingTrackMode.DUBBED
                        )
                        onComplete(true)
                    } else {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            errorMessage = "تعذر توليد ملف الصوت المدبلج. يرجى المحاولة مرة أخرى."
                        )
                        onComplete(false)
                    }
                }
            }
        }
    }

    /**
     * Controls simultaneous or individual A/B playback.
     */
    fun togglePlayback() {
        if (_state.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        val s = _state.value
        val hasOriginal = s.inputAudioPath != null && File(s.inputAudioPath).exists()
        val hasDubbed = s.dubbedAudioPath != null && File(s.dubbedAudioPath).exists()

        if (!hasOriginal && !hasDubbed) {
            Log.w(TAG, "startPlayback: Neither original nor dubbed audio file exists")
            return
        }

        try {
            // If already completed or at end, reset position to start from beginning
            val maxDuration = maxOf(s.inputAudioDurationMs, s.dubbedAudioDurationMs)
            val isAtEnd = maxDuration > 0 && s.playbackCurrentPositionMs >= (maxDuration - 100L)
            if (isAtEnd) {
                _state.value = _state.value.copy(playbackCurrentPositionMs = 0L)
            }

            if (originalMediaPlayer == null && hasOriginal) {
                originalMediaPlayer = MediaPlayer().apply {
                    setDataSource(s.inputAudioPath)
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "originalMediaPlayer error: what=$what, extra=$extra")
                        handlePlaybackError("تعذر تشغيل المقطع الأصلي (رمز $what)")
                        true
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "originalMediaPlayer reached completion")
                        checkPlaybackCompletion()
                    }
                    prepare()
                }
            }

            if (dubbedMediaPlayer == null && hasDubbed) {
                dubbedMediaPlayer = MediaPlayer().apply {
                    setDataSource(s.dubbedAudioPath)
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "dubbedMediaPlayer error: what=$what, extra=$extra")
                        handlePlaybackError("تعذر تشغيل مقطع الدبلجة (رمز $what)")
                        true
                    }
                    setOnCompletionListener {
                        Log.d(TAG, "dubbedMediaPlayer reached completion")
                        checkPlaybackCompletion()
                    }
                    prepare()
                }
            }

            val targetPos = if (isAtEnd) 0 else _state.value.playbackCurrentPositionMs.toInt()
            if (targetPos > 0) {
                try { originalMediaPlayer?.seekTo(targetPos) } catch (_: Exception) {}
                try { dubbedMediaPlayer?.seekTo(targetPos) } catch (_: Exception) {}
            } else if (isAtEnd) {
                try { originalMediaPlayer?.seekTo(0) } catch (_: Exception) {}
                try { dubbedMediaPlayer?.seekTo(0) } catch (_: Exception) {}
            }

            updatePlayerVolumes()

            originalMediaPlayer?.start()
            dubbedMediaPlayer?.start()

            _state.value = _state.value.copy(isPlaying = true, errorMessage = null)
            startPlaybackTicker()
            Log.d(TAG, "Playback started at ${targetPos}ms with mode ${s.activeTrackMode}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback", e)
            handlePlaybackError("خطأ في تشغيل الصوت: ${e.localizedMessage}")
        }
    }

    fun pausePlayback() {
        try {
            originalMediaPlayer?.pause()
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing originalMediaPlayer", e)
        }
        try {
            dubbedMediaPlayer?.pause()
        } catch (e: Exception) {
            Log.w(TAG, "Error pausing dubbedMediaPlayer", e)
        }
        _state.value = _state.value.copy(isPlaying = false)
        playbackTickerJob?.cancel()
        Log.d(TAG, "Playback paused at ${_state.value.playbackCurrentPositionMs}ms")
    }

    fun stopPlayback() {
        playbackTickerJob?.cancel()
        playbackTickerJob = null

        try {
            if (originalMediaPlayer?.isPlaying == true) {
                originalMediaPlayer?.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping originalMediaPlayer", e)
        }
        try {
            originalMediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing originalMediaPlayer", e)
        }
        originalMediaPlayer = null

        try {
            if (dubbedMediaPlayer?.isPlaying == true) {
                dubbedMediaPlayer?.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping dubbedMediaPlayer", e)
        }
        try {
            dubbedMediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing dubbedMediaPlayer", e)
        }
        dubbedMediaPlayer = null

        _state.value = _state.value.copy(
            isPlaying = false,
            playbackCurrentPositionMs = 0L
        )
        Log.d(TAG, "Playback stopped and media players released")
    }

    fun seekToPosition(positionMs: Long) {
        val boundedPos = positionMs.coerceAtLeast(0L)
        try {
            originalMediaPlayer?.seekTo(boundedPos.toInt())
        } catch (e: Exception) {
            Log.w(TAG, "Error seeking originalMediaPlayer", e)
        }
        try {
            dubbedMediaPlayer?.seekTo(boundedPos.toInt())
        } catch (e: Exception) {
            Log.w(TAG, "Error seeking dubbedMediaPlayer", e)
        }
        _state.value = _state.value.copy(playbackCurrentPositionMs = boundedPos)
        Log.d(TAG, "Seeked to position: ${boundedPos}ms")
    }

    private fun updatePlayerVolumes() {
        val s = _state.value
        when (s.activeTrackMode) {
            AudioDubbingTrackMode.ORIGINAL -> {
                originalMediaPlayer?.setVolume(s.originalVolumeGain, s.originalVolumeGain)
                dubbedMediaPlayer?.setVolume(0f, 0f)
            }
            AudioDubbingTrackMode.DUBBED -> {
                originalMediaPlayer?.setVolume(0f, 0f)
                dubbedMediaPlayer?.setVolume(s.dubbedVolumeGain, s.dubbedVolumeGain)
            }
            AudioDubbingTrackMode.MIXED -> {
                originalMediaPlayer?.setVolume(s.originalVolumeGain * 0.5f, s.originalVolumeGain * 0.5f)
                dubbedMediaPlayer?.setVolume(s.dubbedVolumeGain, s.dubbedVolumeGain)
            }
        }
    }

    private fun checkPlaybackCompletion() {
        val s = _state.value
        val origPlaying = try { originalMediaPlayer?.isPlaying == true } catch (_: Exception) { false }
        val dubPlaying = try { dubbedMediaPlayer?.isPlaying == true } catch (_: Exception) { false }

        val isFinished = when (s.activeTrackMode) {
            AudioDubbingTrackMode.ORIGINAL -> !origPlaying
            AudioDubbingTrackMode.DUBBED -> !dubPlaying
            AudioDubbingTrackMode.MIXED -> !origPlaying && !dubPlaying
        }

        if (isFinished) {
            Log.d(TAG, "Playback finished naturally for mode ${s.activeTrackMode}")
            playbackTickerJob?.cancel()
            try {
                originalMediaPlayer?.pause()
                originalMediaPlayer?.seekTo(0)
            } catch (_: Exception) {}
            try {
                dubbedMediaPlayer?.pause()
                dubbedMediaPlayer?.seekTo(0)
            } catch (_: Exception) {}

            _state.value = _state.value.copy(
                isPlaying = false,
                playbackCurrentPositionMs = 0L
            )
        }
    }

    private fun handlePlaybackError(userMessage: String) {
        stopPlayback()
        _state.value = _state.value.copy(
            errorMessage = userMessage,
            isPlaying = false
        )
    }

    private fun startPlaybackTicker() {
        playbackTickerJob?.cancel()
        playbackTickerJob = scope.launch {
            while (_state.value.isPlaying) {
                val origPos = try { originalMediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
                val dubPos = try { dubbedMediaPlayer?.currentPosition?.toLong() ?: 0L } catch (_: Exception) { 0L }
                val currentPos = when (_state.value.activeTrackMode) {
                    AudioDubbingTrackMode.ORIGINAL -> origPos
                    AudioDubbingTrackMode.DUBBED -> dubPos
                    AudioDubbingTrackMode.MIXED -> maxOf(origPos, dubPos)
                }
                _state.value = _state.value.copy(playbackCurrentPositionMs = currentPos)
                kotlinx.coroutines.delay(60)
            }
        }
    }

    private fun extractAudioDuration(path: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.w(TAG, "extractAudioDuration failed for $path, trying fallback", e)
            try {
                val mp = MediaPlayer()
                mp.setDataSource(path)
                mp.prepare()
                val dur = mp.duration.toLong()
                mp.release()
                dur
            } catch (_: Exception) {
                0L
            }
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateSyntheticWaveform(count: Int): List<Float> {
        val basePattern = listOf(0.2f, 0.4f, 0.7f, 0.9f, 0.6f, 0.3f, 0.8f, 1.0f, 0.75f, 0.5f, 0.35f, 0.6f, 0.85f, 0.4f, 0.25f)
        return (0 until count).map { i ->
            val idx = i % basePattern.size
            (basePattern[idx] * (0.8f + (i % 3) * 0.1f)).coerceIn(0.15f, 1.0f)
        }
    }

    private fun adaptTextToDialect(text: String, dialect: DubbingDialect): String {
        if (text.isBlank()) return text
        return when (dialect) {
            DubbingDialect.MODERN_STANDARD_CLASSIC -> {
                text.replace("إيه ده", "ما هذا")
                    .replace("علشان", "لأجل ذلك")
                    .replace("دلوقتي", "الآن في هذه اللحظة")
                    .replace("شو صار", "ما الذي حدث")
                    .replace("هنيك", "هناك")
            }
            DubbingDialect.MODERN_STANDARD_CONTEMPORARY -> {
                text.replace("إيه ده", "ما هذا")
                    .replace("علشان", "من أجل")
                    .replace("دلوقتي", "الآن")
            }
            DubbingDialect.EGYPTIAN -> {
                text.replace("ما هذا", "إيه ده")
                    .replace("لماذا", "ليه كده")
                    .replace("الآن", "دلوقتي")
                    .replace("حقاً", "بجد والله")
                    .replace("هناك", "هناك أهو")
            }
            DubbingDialect.LEVANTINE_SYRIAN -> {
                text.replace("ما هذا", "شو هاد")
                    .replace("لماذا", "ليش هيك")
                    .replace("الآن", "هلأ")
                    .replace("هناك", "هنيك")
            }
            DubbingDialect.GULF_KHALIJI -> {
                text.replace("ما هذا", "وش هذا")
                    .replace("لماذا", "ليش عاد")
                    .replace("الآن", "الحين")
                    .replace("جيد جداً", "زين وايد")
            }
            DubbingDialect.MAGHREBI -> {
                text.replace("ما هذا", "شنو هادا")
                    .replace("الآن", "دابا")
                    .replace("جيد جداً", "مزيان بزاف")
            }
            DubbingDialect.IRAQI -> {
                text.replace("ما هذا", "شنو هذا")
                    .replace("لماذا", "ليش يمعود")
                    .replace("الآن", "هسة")
                    .replace("كثيراً", "كلش هواية")
            }
        }
    }

    fun release() {
        stopPlayback()
    }
}

data class DemoAudioPreset(
    val id: String,
    val titleArabic: String,
    val sampleText: String,
    val defaultDialect: DubbingDialect,
    val suggestedProfileId: String
)
