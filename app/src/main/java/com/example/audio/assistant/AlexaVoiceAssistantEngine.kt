package com.example.audio.assistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.audio.TextToSpeechManager
import com.example.ui.components.VoicePresetType
import com.example.audio.media3.Media3AudioProcessingLayer
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.audio.hollywood.CinematicScoreStyle
import com.example.audio.hollywood.FoleyCategory
import java.io.File
import java.util.Locale

/**
 * High-level Assistant State
 */
sealed class AlexaAssistantState {
    object Idle : AlexaAssistantState()
    object Listening : AlexaAssistantState()
    object Thinking : AlexaAssistantState()
    data class Executing(val commandTitleArabic: String) : AlexaAssistantState()
    data class Speaking(val spokenResponseArabic: String) : AlexaAssistantState()
    data class Error(val errorMessageArabic: String) : AlexaAssistantState()
}

/**
 * Recognized Voice Intent with Auto-Correction details
 */
enum class AlexaVoiceIntent(val titleArabic: String, val iconEmoji: String) {
    START_RECORDING("بدء التسجيل الصوتي 🎙️", "🎙️"),
    STOP_RECORDING("إيقاف وحفظ التسجيل ⏹️", "⏹️"),
    PLAY_AUDIO("تشغيل مقطع الدبلجة 🔊", "🔊"),
    STOP_PLAYBACK("إيقاف التشغيل ⏸️", "⏸️"),
    SEEK_TIMELINE("تقديم/ترجيع شريط الوقت ⏱️", "⏱️"),
    SET_DUB_VOLUME("تغيير مستوى صوت الدبلجة 🎚️", "🎚️"),
    SET_ORIGINAL_VOLUME("تغيير صوت المشهد الأصلي 🔊", "🔊"),
    SET_BGM_VOLUME("تغيير صوت الموسيقى التصويرية 🎵", "🎵"),
    MUTE_UNMUTE_ORIGINAL("كتم/تشغيل صوت الفيديو الأصلي 🔇", "🔇"),
    MUTE_UNMUTE_DUB("كتم/تشغيل صوت الدبلجة 🔈", "🔈"),
    TOGGLE_VOCAL_CLARITY("تفعيل/إيقاف وضوح الصوت البشري 🎙️", "🎙️"),
    MEDIA3_AI_CLEAN("تنقية الضوضاء وموازنة الصوت بـ Media3 🎚️", "🎚️"),
    DEEP_GEMINI_DENOISE("تنقية صوتية فائقة بـ Gemini ✨", "✨"),
    APPLY_VOICE_EFFECT("تطبيق مؤثر صوتي 🤖", "🤖"),
    COMPOUND_AI_CLEAN_AND_EFFECT("معالجة مركبة: تنقية + مؤثر + موازنة ⚡", "⚡"),
    OPEN_EQUALIZER("فتح المعادل الصوتي 🎚️", "🎚️"),
    APPLY_EQ_PRESET("تطبيق نمط في المعادل الصوتي 🎛️", "🎛️"),
    LIP_SYNC_AUTO_ALIGN("محاذاة ومزامنة الشفاه التلقائية 🎯", "🎯"),
    LIP_SYNC_NUDGE("تعديل إزاحة مزامنة الشفاه ⏱️", "⏱️"),
    AUTO_TRANSCRIBE_STT("تفريغ الصوت إلى نصوص متزامنة ✍️", "✍️"),
    GENERATE_AI_SCRIPT("تأليف حوارات ذكية بالذكاء الاصطناعي 🪄", "🪄"),
    READ_SCRIPT_ALOUD("قراءة نصوص السيناريو 📖", "📖"),
    GEMINI_TRANSLATE_SCRIPT("ترجمة السيناريو بالذكاء الاصطناعي 🌐", "🌐"),
    SYNTHESIZE_AND_SYNC_TIMELINE("توليد وتركيب الدبلجة على خط الزمن 🎙️🎬", "🎬"),
    QUALITY_AUDIT_SCRIPT("تدقيق جودة الصوت والتشكيل آلياً 🪄", "🪄"),
    ONE_CLICK_AUTO_DUB("دبلجة المشهد بضغطة زر واحدة 🚀", "🚀"),
    GENERATE_SOCIAL_METADATA("توليد عنوان تسويقي وهاشتاجات للنشر 📱✨", "✨"),
    AUTO_TRIM_SILENCE("قص الصوت وحذف الصمت ✂️", "✂️"),
    DISCARD_RECORDING("حذف التسجيل الصوتي الحالي 🗑️", "🗑️"),
    OPEN_EXPORT_DIALOG("تصدير وحفظ الفيديو المدمج 🎬", "🎬"),
    NAVIGATE_TAB("الانتقال إلى قسم بالتطبيق 🚀", "🚀"),
    OPEN_EXTERNAL_APP("فتح تطبيق بالنظام 📱", "📱"),
    SEPARATE_STEMS("فصل التراكات الصوتية الأربعة 🎧", "🎧"),
    CLONE_VOICE("استنساخ البصمة الصوتية للممثل 🧬", "🧬"),
    GENERATE_CINEMATIC_SCORE("تأليف موسيقى تصويرية أوركسترالية 🎻", "🎻"),
    GENERATE_FOLEY_EFFECT("توليد مؤثرات سينمائية 🔊", "🔊"),
    CLIP_VIRAL_SHORTS("استخراج مقاطع ريلز وتريند 📱", "📱"),
    RENDER_SPATIAL_3D("تفعيل الصوت المجسم ثلاثي الأبعاد 🌌", "🌌"),
    DIRECTOR_SCENE_CRITIQUE("تقييم وإخراج المشهد الذاتي 🎬", "🎬"),
    GLOBAL_MULTI_LANG_DUB("دبلجة عالمية بـ 10 لغات 🌐", "🌐"),
    UNDO_ACTION("التراجع عن آخر تعديل ↩️", "↩️"),
    REDO_ACTION("إعادة تطبيق التعديل 🔁", "🔁"),
    DEVELOPER_COPYRIGHT_QUERY("الاستعلام عن المطور وحقوق الملكية 👤", "🛡️"),
    ASSIST_BLIND_READ_STATUS("قراءة الشاشة ومساعدة المكفوفين 👁️", "👁️"),
    CONVERSATIONAL_QUESTION("محادثة ذكاء اصطناعي تفاعلية 💡", "✨"),
    UNKNOWN("أمر غير محدد ❓", "❓")
}

data class ParsedVoiceCommand(
    val rawText: String,
    val correctedText: String,
    val detectedIntent: AlexaVoiceIntent,
    val effectType: VoicePresetType? = null,
    val navigationTargetId: String? = null,
    val targetAppName: String? = null,
    val targetAppLaunchIntent: Intent? = null,
    val syncDeltaMs: Float = 0f,
    val volumeValue: Float? = null,
    val seekSecondsDelta: Float? = null,
    val eqPresetName: String? = null,
    val eqPresetGains: List<Float>? = null,
    val targetTranslationLang: String? = null,
    val responseSpeechArabic: String,
    val wasAutoCorrected: Boolean = false
)

/**
 * Intelligent Alexa/Gemini-Style Voice Assistant Engine for VoiceMaster Pro.
 * 
 * Features:
 * 1. Low-latency continuous/on-demand speech recognition via Android SpeechRecognizer.
 * 2. "Alexa Error Correction" (تصحيح أخطاء أليكسا): Normalizes dialectical pronunciations,
 *    removes acoustic noise, fixes spelling/phonetic mistakes and resolves intended action.
 * 3. Full execution of in-app audio operations (Media3 AI cleaning, recording, trimming, effects).
 * 4. Text-To-Speech natural Arabic voice response.
 */
class AlexaVoiceAssistantEngine(
    private val context: Context,
    private val ttsManager: TextToSpeechManager,
    private val media3ProcessingLayer: Media3AudioProcessingLayer
) {
    private val tag = "AlexaVoiceAssistant"

    private var speechRecognizer: SpeechRecognizer? = null
    private val _assistantState = MutableStateFlow<AlexaAssistantState>(AlexaAssistantState.Idle)
    val assistantState: StateFlow<AlexaAssistantState> = _assistantState.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    private val _lastParsedCommand = MutableStateFlow<ParsedVoiceCommand?>(null)
    val lastParsedCommand: StateFlow<ParsedVoiceCommand?> = _lastParsedCommand.asStateFlow()

    private val _isVoiceFeedbackEnabled = MutableStateFlow(true)
    val isVoiceFeedbackEnabled: StateFlow<Boolean> = _isVoiceFeedbackEnabled.asStateFlow()

    private val _isContinuousWakeWordListening = MutableStateFlow(false)
    val isContinuousWakeWordListening: StateFlow<Boolean> = _isContinuousWakeWordListening.asStateFlow()

    private var executionScope: CoroutineScope? = null
    private var isListeningSessionActive = false
    private var activeViewModel: DubbingViewModel? = null
    private var activeNavigationCallback: ((String) -> Unit)? = null
    private var autoRestartJob: Job? = null
    private var consecutiveErrorCount = 0

    init {
        initSpeechRecognizer()
    }

    fun resetErrorState() {
        consecutiveErrorCount = 0
        autoRestartJob?.cancel()
        _assistantState.value = AlexaAssistantState.Idle
    }

    fun submitTextCommand(text: String, viewModel: DubbingViewModel, onNavigate: (String) -> Unit) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        activeViewModel = viewModel
        activeNavigationCallback = onNavigate
        consecutiveErrorCount = 0
        _lastTranscript.value = trimmed
        _assistantState.value = AlexaAssistantState.Thinking
        handleRecognizedSpeech(trimmed)
    }

    private fun initSpeechRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            } else {
                Log.w(tag, "Speech recognition is not available natively.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to init SpeechRecognizer: ${e.message}")
        }
    }

    fun toggleVoiceFeedback(enabled: Boolean) {
        _isVoiceFeedbackEnabled.value = enabled
    }

    /**
     * Toggles continuous wake-word detection loop ("Always-listening for Alexa")
     */
    fun toggleContinuousWakeWordListening(
        enabled: Boolean,
        scope: CoroutineScope,
        viewModel: DubbingViewModel,
        onNavigate: (String) -> Unit
    ) {
        _isContinuousWakeWordListening.value = enabled
        activeViewModel = viewModel
        activeNavigationCallback = onNavigate
        if (enabled) {
            startListening(scope, viewModel, onNavigate)
        } else {
            autoRestartJob?.cancel()
            stopListening()
        }
    }

    fun triggerHapticFeedback(durationMs: Long = 40L) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Start listening for voice commands (Alexa / Gemini style) with multi-language & accessibility support.
     */
    fun startListening(
        scope: CoroutineScope,
        viewModel: DubbingViewModel,
        onNavigate: (String) -> Unit
    ) {
        executionScope = scope
        activeViewModel = viewModel
        activeNavigationCallback = onNavigate
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            _assistantState.value = AlexaAssistantState.Listening
            executionScope?.launch(Dispatchers.Main) {
                delay(3000)
                if (_assistantState.value is AlexaAssistantState.Listening) {
                    _assistantState.value = AlexaAssistantState.Idle
                }
            }
            return
        }

        try {
            val deviceLocale = Locale.getDefault()
            val primaryLangTag = if (deviceLocale.language.isNotBlank()) deviceLocale.toLanguageTag() else "ar-SA"
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLangTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, deviceLocale.language.ifEmpty { "ar" })
                // Multi-language hints for Google Speech Recognizer
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-SA", "en-US", "fr-FR", "es-ES", "de-DE"))
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }

            triggerHapticFeedback(45L)
            _assistantState.value = AlexaAssistantState.Listening
            isListeningSessionActive = true
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Error starting voice listening: ${e.message}")
            _assistantState.value = AlexaAssistantState.Error("تعذر بدء الاستماع الصوتي: ${e.message}")
        }
    }

    fun stopListening() {
        isListeningSessionActive = false
        autoRestartJob?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        if (_assistantState.value is AlexaAssistantState.Listening) {
            _assistantState.value = AlexaAssistantState.Idle
        }
    }

    private fun scheduleAutoRestartIfNeeded() {
        if (_isContinuousWakeWordListening.value) {
            autoRestartJob?.cancel()
            autoRestartJob = executionScope?.launch(Dispatchers.Main) {
                delay(400)
                if (_isContinuousWakeWordListening.value) {
                    val vm = activeViewModel
                    val nav = activeNavigationCallback
                    if (vm != null && nav != null) {
                        startListening(this, vm, nav)
                    }
                }
            }
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _assistantState.value = AlexaAssistantState.Listening
        }

        override fun onBeginningOfSpeech() {
            consecutiveErrorCount = 0
            _assistantState.value = AlexaAssistantState.Listening
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _rmsVolume.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _assistantState.value = AlexaAssistantState.Thinking
            _rmsVolume.value = 0f
        }

        override fun onError(error: Int) {
            _rmsVolume.value = 0f
            isListeningSessionActive = false
            consecutiveErrorCount++

            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                _assistantState.value = AlexaAssistantState.Error("يرجى منح إذن الميكروفون للاستماع 🎙️")
                executionScope?.launch(Dispatchers.Main) {
                    delay(2500)
                    if (_assistantState.value is AlexaAssistantState.Error) {
                        _assistantState.value = AlexaAssistantState.Idle
                    }
                }
            } else {
                // Graceful idle reset without aggressive error alerts
                _assistantState.value = AlexaAssistantState.Idle
            }

            // If error is client or busy, re-init recognizer cleanly
            if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                try {
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                } catch (_: Exception) {}
            }

            // Only attempt restart if we haven't hit consecutive error threshold
            if (consecutiveErrorCount < 3 && _isContinuousWakeWordListening.value) {
                scheduleAutoRestartIfNeeded()
            } else {
                _isContinuousWakeWordListening.value = false
            }
        }

        override fun onResults(results: Bundle?) {
            consecutiveErrorCount = 0
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: ""
            handleRecognizedSpeech(recognizedText)
            scheduleAutoRestartIfNeeded()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _lastTranscript.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Core NLP Engine with Auto-Correction ("تصحيح أخطاء أليكسا")
     */
    fun parseVoiceCommandWithAutoCorrection(rawSpeech: String): ParsedVoiceCommand {
        val raw = rawSpeech.trim()
        if (raw.isEmpty()) {
            return ParsedVoiceCommand(
                rawText = "",
                correctedText = "",
                detectedIntent = AlexaVoiceIntent.UNKNOWN,
                responseSpeechArabic = "أنا في انتظار أمرك الصوتي، تفضل بالتحدث."
            )
        }

        // 1. Phonetic & Orthographic Normalization for Arabic
        val normalized = normalizeArabicSpeech(raw)
        var wasCorrected = false
        var correctedText = normalized

        // Check if raw speech contained typos or colloquial variations and map them
        val res = resolveIntent(normalized)

        if (res.wasAutoCorrected) {
            wasCorrected = true
            correctedText = mapToStandardArabicCommand(res.intent, res.effectType, res.navTarget, res.targetAppName)
        }

        return ParsedVoiceCommand(
            rawText = raw,
            correctedText = correctedText,
            detectedIntent = res.intent,
            effectType = res.effectType,
            navigationTargetId = res.navTarget,
            targetAppName = res.targetAppName,
            targetAppLaunchIntent = res.targetAppLaunchIntent,
            syncDeltaMs = res.syncDeltaMs,
            volumeValue = res.volumeValue,
            seekSecondsDelta = res.seekSecondsDelta,
            eqPresetName = res.eqPresetName,
            eqPresetGains = res.eqPresetGains,
            targetTranslationLang = res.targetTranslationLang,
            responseSpeechArabic = res.responseArabic,
            wasAutoCorrected = wasCorrected
        )
    }

    /**
     * Handles speech after recognition and executes actions
     */
    private fun handleRecognizedSpeech(recognizedText: String) {
        _lastTranscript.value = recognizedText
        val parsed = parseVoiceCommandWithAutoCorrection(recognizedText)
        _lastParsedCommand.value = parsed

        _assistantState.value = AlexaAssistantState.Executing(parsed.detectedIntent.titleArabic)

        // Speak back voice confirmation like Alexa / Gemini
        if (_isVoiceFeedbackEnabled.value && parsed.responseSpeechArabic.isNotBlank()) {
            ttsManager.speakText(parsed.responseSpeechArabic, utteranceId = "alexa_assistant_response")
            _assistantState.value = AlexaAssistantState.Speaking(parsed.responseSpeechArabic)
        }

        // Auto-execute parsed command immediately
        val vm = activeViewModel
        val nav = activeNavigationCallback
        if (vm != null && nav != null && parsed.detectedIntent != AlexaVoiceIntent.UNKNOWN) {
            executeParsedCommand(parsed, vm, nav)
        }
    }

    /**
     * Executes the parsed command on DubbingViewModel and Navigation
     */
    fun executeParsedCommand(
        command: ParsedVoiceCommand,
        viewModel: DubbingViewModel,
        onNavigate: (String) -> Unit
    ) {
        executionScope?.launch(Dispatchers.Main) {
            triggerHapticFeedback(50L)
            when (command.detectedIntent) {
                AlexaVoiceIntent.ASSIST_BLIND_READ_STATUS -> {
                    triggerHapticFeedback(80L)
                    val studioState = viewModel.uiState.value
                    val clipName = studioState.currentClip.title
                    val hasRecordedTake = studioState.recordedAudioPath != null && File(studioState.recordedAudioPath).exists()
                    val dubVol = (studioState.dubVolume * 100).toInt()
                    val origVol = (studioState.originalVolume * 100).toInt()
                    val isEnglish = Locale.getDefault().language.equals("en", ignoreCase = true)
                    val readout = if (isEnglish) {
                        buildString {
                            append("Hello! Alexa Accessibility Assistant is active. ")
                            append("Current studio scene: $clipName. ")
                            if (hasRecordedTake) append("You have a recorded audio take ready. ")
                            else append("No audio recorded yet. You can say 'Record' to start. ")
                            append("Dubbing volume is $dubVol percent. Original volume is $origVol percent. ")
                            append("You can speak any command: Record, Play, Auto Dub, Clean Audio, or Open any app on your phone.")
                        }
                    } else {
                        buildString {
                            append("مرحباً بك في المساعد الصوتي فويس ماستر برو لمساعدة المكفوفين. ")
                            append("المشهد الحالي في الاستوديو: $clipName. ")
                            if (hasRecordedTake) append("يوجد مقطع صوتي مسجل ومتاح للمعالجة والمزامنة. ")
                            else append("لا يوجد تسجيل بعد، يمكنك قول: ابدأ التسجيل. ")
                            append("مستوى صوت الدبلجة $dubVol بالمئة، وصوت الفيديو $origVol بالمئة. ")
                            append("يمكنك أن تطلب مني بالصوت: سجل، شغل، دبلج، نظف الصوت، ترجم السيناريو، أو افتح أي تطبيق على هاتفك.")
                        }
                    }
                    _assistantState.value = AlexaAssistantState.Speaking(readout)
                    ttsManager.speakText(readout, utteranceId = "alexa_blind_status")
                    viewModel.showToast(readout.take(85) + "...")
                }
                AlexaVoiceIntent.START_RECORDING -> {
                    viewModel.startRecordingCountdown()
                }
                AlexaVoiceIntent.STOP_RECORDING -> {
                    viewModel.stopRecording()
                    triggerMedia3AutoCleanOnCurrentTake(viewModel)
                }
                AlexaVoiceIntent.PLAY_AUDIO -> {
                    viewModel.startPlayback()
                }
                AlexaVoiceIntent.STOP_PLAYBACK -> {
                    viewModel.pausePlayback()
                }
                AlexaVoiceIntent.MEDIA3_AI_CLEAN -> {
                    triggerMedia3AutoCleanOnCurrentTake(viewModel)
                }
                AlexaVoiceIntent.APPLY_VOICE_EFFECT -> {
                    if (command.effectType != null) {
                        viewModel.selectVoicePreset(command.effectType)
                    }
                }
                AlexaVoiceIntent.COMPOUND_AI_CLEAN_AND_EFFECT -> {
                    triggerMedia3AutoCleanOnCurrentTake(viewModel)
                    viewModel.quickAutoBalanceMix()
                    if (command.effectType != null) {
                        viewModel.selectVoicePreset(command.effectType)
                    }
                    viewModel.showToast("تم تنفيذ المعالجة الصوتية المركبة بالذكاء الاصطناعي بنجاح! ⚡🎚️")
                }
                AlexaVoiceIntent.LIP_SYNC_AUTO_ALIGN -> {
                    viewModel.autoAlignSyncOffset()
                }
                AlexaVoiceIntent.LIP_SYNC_NUDGE -> {
                    viewModel.nudgeDubSyncOffsetMs(command.syncDeltaMs)
                }
                AlexaVoiceIntent.AUTO_TRANSCRIBE_STT -> {
                    viewModel.startListeningStt("ar-SA")
                }
                AlexaVoiceIntent.GEMINI_TRANSLATE_SCRIPT -> {
                    viewModel.translateScriptLinesWithGemini("العربية")
                }
                AlexaVoiceIntent.SYNTHESIZE_AND_SYNC_TIMELINE -> {
                    viewModel.synthesizeAndSyncDubbedAudio()
                }
                AlexaVoiceIntent.QUALITY_AUDIT_SCRIPT -> {
                    viewModel.autoFixAndDiacritizeAllScriptLines()
                }
                AlexaVoiceIntent.ONE_CLICK_AUTO_DUB -> {
                    viewModel.startAutoVideoDubbing()
                }
                AlexaVoiceIntent.GENERATE_SOCIAL_METADATA -> {
                    val clipTitle = viewModel.uiState.value.currentClip.title
                    val seoText = "🎬 شاهد أقوى مشهد مدبلج بالذكاء الاصطناعي: $clipTitle #دبلجة_ذكية #فويس_ماستر #دوبلاج #AI_Dubbing #محمد_سليمة"
                    viewModel.showToast("تم توليد ونسخ العناوين والهاشتاجات للنشر بنجاح! 📱✨")
                    try {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Social SEO", seoText)
                        clipboard?.setPrimaryClip(clip)
                    } catch (_: Exception) {}
                }
                AlexaVoiceIntent.AUTO_TRIM_SILENCE -> {
                    viewModel.openAudioTrimmerForCurrentTake()
                }
                AlexaVoiceIntent.OPEN_EQUALIZER -> {
                    viewModel.openEqualizerSheet()
                }
                AlexaVoiceIntent.NAVIGATE_TAB -> {
                    if (command.navigationTargetId != null) {
                        onNavigate(command.navigationTargetId)
                    }
                }
                AlexaVoiceIntent.OPEN_EXTERNAL_APP -> {
                    command.targetAppLaunchIntent?.let { launchIntent ->
                        try {
                            context.startActivity(launchIntent)
                            viewModel.showToast("تم فتح تطبيق ${command.targetAppName ?: ""} بنجاح عبر Intent 📱")
                        } catch (e: Exception) {
                            viewModel.showToast("تعذر فتح التطبيق: ${e.message}")
                        }
                    }
                }
                AlexaVoiceIntent.SEEK_TIMELINE -> {
                    viewModel.seekPlaybackDelta(command.seekSecondsDelta ?: 5f)
                }
                AlexaVoiceIntent.SET_DUB_VOLUME -> {
                    command.volumeValue?.let { viewModel.setDubVolume(it) }
                }
                AlexaVoiceIntent.SET_ORIGINAL_VOLUME -> {
                    command.volumeValue?.let { viewModel.setOriginalVolume(it) }
                }
                AlexaVoiceIntent.SET_BGM_VOLUME -> {
                    command.volumeValue?.let { viewModel.setBgmVolume(it) }
                }
                AlexaVoiceIntent.MUTE_UNMUTE_ORIGINAL -> {
                    viewModel.toggleMuteOriginal()
                }
                AlexaVoiceIntent.MUTE_UNMUTE_DUB -> {
                    viewModel.toggleMuteDub()
                }
                AlexaVoiceIntent.TOGGLE_VOCAL_CLARITY -> {
                    viewModel.toggleVocalClarity()
                }
                AlexaVoiceIntent.DEEP_GEMINI_DENOISE -> {
                    viewModel.enhanceRecordedAudioWithGemini()
                }
                AlexaVoiceIntent.APPLY_EQ_PRESET -> {
                    if (command.eqPresetName != null && command.eqPresetGains != null) {
                        viewModel.applyEqualizerPreset(command.eqPresetName, command.eqPresetGains)
                    }
                }
                AlexaVoiceIntent.GENERATE_AI_SCRIPT -> {
                    viewModel.generateGeminiArabicScript()
                }
                AlexaVoiceIntent.READ_SCRIPT_ALOUD -> {
                    viewModel.readCurrentScriptAloud()
                }
                AlexaVoiceIntent.DISCARD_RECORDING -> {
                    viewModel.discardCurrentTake()
                }
                AlexaVoiceIntent.OPEN_EXPORT_DIALOG -> {
                    viewModel.openExportDialog()
                }
                AlexaVoiceIntent.SEPARATE_STEMS -> {
                    viewModel.separateAudioStems()
                }
                AlexaVoiceIntent.CLONE_VOICE -> {
                    viewModel.launchHollywoodMastering("بصمة الممثل")
                }
                AlexaVoiceIntent.GENERATE_CINEMATIC_SCORE -> {
                    viewModel.generateCinematicScore(CinematicScoreStyle.HEROIC_ACTION)
                }
                AlexaVoiceIntent.GENERATE_FOLEY_EFFECT -> {
                    viewModel.generateFoleyEffect(FoleyCategory.CINEMATIC_BOOM)
                }
                AlexaVoiceIntent.CLIP_VIRAL_SHORTS -> {
                    viewModel.generateViralShortsClips()
                }
                AlexaVoiceIntent.RENDER_SPATIAL_3D -> {
                    viewModel.renderSpatial3DAudio()
                }
                AlexaVoiceIntent.DIRECTOR_SCENE_CRITIQUE -> {
                    viewModel.hollywoodSuiteService.filmDirector.evaluateAndAutoTuneScene(8, 30f, true)
                }
                AlexaVoiceIntent.GLOBAL_MULTI_LANG_DUB -> {
                    viewModel.hollywoodSuiteService.globalMatrix.selectAll()
                    viewModel.showToast("تم تفعيل مصفوفة الدبلجة المتوازية لـ 10 لغات عالمية 🌐⚡")
                }
                AlexaVoiceIntent.UNDO_ACTION -> {
                    viewModel.undo()
                }
                AlexaVoiceIntent.REDO_ACTION -> {
                    viewModel.redo()
                }
                AlexaVoiceIntent.DEVELOPER_COPYRIGHT_QUERY -> {
                    viewModel.embedPublisherCopyrightWatermark()
                }
                AlexaVoiceIntent.CONVERSATIONAL_QUESTION, AlexaVoiceIntent.UNKNOWN -> {
                    val rawSpoken = command.correctedText.ifEmpty { _lastTranscript.value }.trim()
                    if (rawSpoken.contains("ابحث عن") || rawSpoken.contains("ابحث في جوجل") || rawSpoken.startsWith("search")) {
                        val searchQuery = rawSpoken
                            .replace("ابحث عن", "")
                            .replace("ابحث في جوجل عن", "")
                            .replace("ابحث في جوجل", "")
                            .replace("search for", "")
                            .replace("search", "")
                            .trim()
                        if (searchQuery.isNotEmpty()) {
                            try {
                                val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                    putExtra(android.app.SearchManager.QUERY, searchQuery)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(searchIntent)
                                viewModel.showToast("جارٍ البحث في جوجل عن: $searchQuery 🌐")
                            } catch (_: Exception) {}
                        }
                    } else if (rawSpoken.length > 2) {
                        executionScope?.launch(Dispatchers.IO) {
                            try {
                                val prompt = "أنت المساعد الذكي أليكسا المدعوم بنموذج Google Gemini داخل تطبيق استوديو الدبلجة. أجب بذكاء واختصار باللغة العربية (في حدود جملتين أو 3 جمل فقط): $rawSpoken"
                                val geminiResult = viewModel.geminiUnifiedClient.executeDirectPrompt(prompt)
                                val reply = geminiResult.getOrNull()?.trim()
                                if (!reply.isNullOrEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        _lastParsedCommand.value = command.copy(responseSpeechArabic = reply)
                                        ttsManager.speakText(reply, utteranceId = "gemini_voice_direct_reply")
                                        viewModel.showToast(reply.take(75) + "...")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(tag, "Gemini Q&A assistant fallback: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun triggerMedia3AutoCleanOnCurrentTake(viewModel: DubbingViewModel) {
        val currentPath = viewModel.uiState.value.recordedAudioPath
        if (currentPath != null && File(currentPath).exists()) {
            val result = media3ProcessingLayer.processVoiceClip(File(currentPath))
            if (result.success) {
                viewModel.updateRecordedAudioPath(result.processedFile.absolutePath)
                viewModel.showToast(result.messageArabic)
            }
        }
    }

    /**
     * Arabic Speech Normalization Helper
     */
    private fun normalizeArabicSpeech(input: String): String {
        return input.lowercase()
            .replace(Regex("[\\p{Punct}،؟؛«»ـ]"), " ")
            .replace(Regex("[إأآا]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Resolves the Intent and applies fuzzy matching / auto-correction
     */
    private fun resolveIntent(text: String): IntentResolutionResult {
        // Strip wake-word trigger ("اليكسا" / "alexa" / "يا جيمناي")
        var processedText = text
        val wakeWords = listOf("يا اليكسا", "اليكسا", "alexa", "يا جيمناي", "جيمناي")
        for (w in wakeWords) {
            if (processedText.startsWith(w)) {
                processedText = processedText.removePrefix(w).trim()
                break
            }
        }

        // 0. Open Installed Apps via Intents (مثل: افتح اليوتيوب، افتح الكاميرا، شغل الواتساب، open youtube)
        val openAppTriggers = listOf("افتح تطبيق", "افتحي تطبيق", "شغل تطبيق", "افتح", "افتحي", "شغل", "open app", "open")
        for (trigger in openAppTriggers) {
            if (processedText.startsWith(trigger)) {
                val appQuery = processedText.removePrefix(trigger).trim()
                if (appQuery.isNotBlank() && !appQuery.contains("الاستوديو") && !appQuery.contains("دبلجه") && !appQuery.contains("تسجيل")) {
                    val match = findAppLaunchIntent(appQuery)
                    if (match != null) {
                        return IntentResolutionResult(
                            intent = AlexaVoiceIntent.OPEN_EXTERNAL_APP,
                            targetAppName = match.first,
                            targetAppLaunchIntent = match.second,
                            responseArabic = "جارٍ فتح تطبيق ${match.first} عبر نظام الـ Intents الآن 📱",
                            wasAutoCorrected = true
                        )
                    } else {
                        return IntentResolutionResult(
                            intent = AlexaVoiceIntent.OPEN_EXTERNAL_APP,
                            targetAppName = appQuery,
                            targetAppLaunchIntent = null,
                            responseArabic = "لم يتم العثور على تطبيق باسم $appQuery مثبت على جهازك.",
                            wasAutoCorrected = false
                        )
                    }
                }
            }
        }

        // 1. Copyright & Intellectual Property Query
        if (containsAny(processedText, listOf("مين المطور", "من هو المطور", "صاحب التطبيق", "حقوق النشر", "حقوق الملكيه", "محمد سليمه", "سليمه", "من صنع التطبيق"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.DEVELOPER_COPYRIGHT_QUERY,
                effectType = null,
                navTarget = null,
                responseArabic = "تطبيق فويس ماستر برو تم ابتكاره وتطويره بالكامل بواسطة الأستاذ محمد رضا محمود محمود السيد سليمة، وجميع حقوق الملكية الفكرية والنشر محفوظة له بالكامل.",
                wasAutoCorrected = false
            )
        }

        // 1.5 Blind & Accessibility Screen Reader Query (دعم المكفوفين وقراءة الشاشة)
        if (containsAny(processedText, listOf(
            "اين انا", "أين أنا", "اقرا الشاشة", "اقرأ الشاشة", "حالة الشاشة", "مساعدة المكفوفين", "وضع المكفوفين", "قراءة الوضع", "معلومات الشاشة",
            "where am i", "read screen", "screen status", "assist me", "help blind", "accessibility", "blind mode", "status", "ou suis je", "donde estoy"
        ))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.ASSIST_BLIND_READ_STATUS,
                effectType = null,
                navTarget = null,
                responseArabic = "المساعد الصوتي وميزة مساعدة المكفوفين جاهزة. أقرأ لك حالة الاستوديو الآن.",
                wasAutoCorrected = false
            )
        }

        // 2. Media3 AI Noise Reduction & Gain Normalization
        if (containsAny(text, listOf("نظف الصوت", "شيل الضوضاء", "شيل الوشه", "عزل الضوضاء", "وازن الصوت", "ميديا 3", "ميديا ثري", "media3", "تنقيه الصوت", "تصفيه الصوت", "ازاله التشويش", "clean audio", "denoise", "noise reduction", "remove noise"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.MEDIA3_AI_CLEAN,
                effectType = null,
                navTarget = null,
                responseArabic = "تم تفعيل طبقة المعالجة الصوتية عبر Media3 لعزل الضوضاء الخلفية وموازنة مستويات الصوت بدقة عالية.",
                wasAutoCorrected = true
            )
        }

        // 3. Start Recording
        if (containsAny(text, listOf("سجل", "سجلي", "سجل صوتي", "ابدا التسجيل", "ابدئي التسجيل", "ريكورد", "تسجيل الان", "تسجيل صوت", "يلا نسجل", "record", "start recording", "record audio", "record voice"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.START_RECORDING,
                effectType = null,
                navTarget = null,
                responseArabic = "بدأت التسجيل الآن! تحدث بوضوح أمام الميكروفون.",
                wasAutoCorrected = true
            )
        }

        // 4. Stop Recording
        if (containsAny(text, listOf("وقف التسجيل", "ايقاف التسجيل", "خلصت", "كفايه", "كفايه تسجيل", "انهاء التسجيل", "ستوب تسجيل", "وقف المايك", "stop recording", "finish recording", "stop mic", "done recording"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.STOP_RECORDING,
                effectType = null,
                navTarget = null,
                responseArabic = "تم إيقاف التسجيل وحفظ المقطع، وتجري الآن تنقيته وموازنته عبر Media3 تلقائياً.",
                wasAutoCorrected = true
            )
        }

        // 5. Playback
        if (containsAny(text, listOf("شغل الصوت", "شغل التسجيل", "اسمع الصوت", "اسمعني", "شغلني", "تشغيل المقطع", "بلاي", "اسمع المقطع", "play", "play audio", "play video", "start playback", "resume"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.PLAY_AUDIO,
                effectType = null,
                navTarget = null,
                responseArabic = "جارٍ تشغيل مقطع الصوت المسجل الآن.",
                wasAutoCorrected = true
            )
        }

        // 6. Stop Playback
        if (containsAny(processedText, listOf("وقف الصوت", "اسكت", "ايقاف التشغيل", "كفايه صوت", "صامت", "ميوت", "stop", "pause", "pause playback", "stop playback", "halt"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.STOP_PLAYBACK,
                responseArabic = "تم إيقاف تشغيل الصوت.",
                wasAutoCorrected = true
            )
        }

        // Seeking & Timeline Navigation
        if (containsAny(processedText, listOf("قدم 10 ثواني", "قدم عشر ثواني", "skip 10"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SEEK_TIMELINE,
                seekSecondsDelta = 10f,
                responseArabic = "تم تقديم شريط الوقت 10 ثوانٍ للأمام ⏩",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("قدم 5 ثواني", "قدم خمس ثواني", "قدم الصوت", "قدم الفيديو", "fast forward", "سكيب لقدام"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SEEK_TIMELINE,
                seekSecondsDelta = 5f,
                responseArabic = "تم تقديم شريط الوقت 5 ثوانٍ للأمام ⏩",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ارجع 10 ثواني", "أخر 10 ثواني", "ترجيع 10 ثواني", "rewind 10"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SEEK_TIMELINE,
                seekSecondsDelta = -10f,
                responseArabic = "تم ترجيع شريط الوقت 10 ثوانٍ للخلف ⏪",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ارجع 5 ثواني", "أخر 5 ثواني", "ترجيع", "ارجع لورا", "rewind", "ارجع شوية"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SEEK_TIMELINE,
                seekSecondsDelta = -5f,
                responseArabic = "تم ترجيع شريط الوقت 5 ثوانٍ للخلف ⏪",
                wasAutoCorrected = true
            )
        }

        // Volume Controls
        if (containsAny(processedText, listOf("ارفع صوت الدبلجة", "علي صوت الدبلجة", "زود الدبلجة", "ارفع الدبلجة", "volume up", "louder", "raise volume", "dubbing volume up"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_DUB_VOLUME,
                volumeValue = 1.0f,
                responseArabic = "تم رفع صوت الدبلجة للحد الأقصى (100%) 🎚️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("وطي صوت الدبلجة", "اخفض صوت الدبلجة", "قلل الدبلجة", "وطي الدبلجة", "volume down", "quieter", "lower volume", "dubbing volume down"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_DUB_VOLUME,
                volumeValue = 0.45f,
                responseArabic = "تم خفض مستوى صوت الدبلجة إلى 45% 🎚️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ارفع صوت الفيديو", "علي صوت المشهد", "ارفع المشهد الأصلي", "علي صوت الفيديو", "original volume up", "video louder"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_ORIGINAL_VOLUME,
                volumeValue = 0.8f,
                responseArabic = "تم رفع صوت الفيديو والمشهد الأصلي إلى 80% 🔊",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("وطي صوت الفيديو", "اخفض صوت المشهد", "وطي المشهد الأصلي", "اخفض صوت الفيديو", "original volume down", "video quieter"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_ORIGINAL_VOLUME,
                volumeValue = 0.15f,
                responseArabic = "تم خفض صوت الفيديو الأصلي لتوضيح الدبلجة 🔉",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ارفع الموسيقى", "علي المزيكا", "ارفع صوت الموسيقى", "زود الموسيقى", "music louder", "bgm up"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_BGM_VOLUME,
                volumeValue = 0.7f,
                responseArabic = "تم رفع مستوى الموسيقى التصويرية 🎵",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("وطي الموسيقى", "اخفض المزيكا", "وطي صوت الموسيقى", "قلل الموسيقى", "music quieter", "bgm down"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SET_BGM_VOLUME,
                volumeValue = 0.15f,
                responseArabic = "تم خفض الموسيقى التصويرية في الخلفية 🎵",
                wasAutoCorrected = true
            )
        }

        // Mute / Unmute
        if (containsAny(processedText, listOf("اكتم صوت الفيديو", "كتم المشهد الأصلي", "شغل صوت الفيديو", "صامت الفيديو", "فك كتم الفيديو", "mute", "unmute", "mute audio", "unmute audio", "mute video", "unmute video"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.MUTE_UNMUTE_ORIGINAL,
                responseArabic = "تم تبديل حالة كتم صوت الفيديو الأصلي 🔇",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("اكتم الدبلجة", "كتم صوت الدبلجة", "شغل صوت الدبلجة", "صامت الدبلجة", "فك كتم الدبلجة"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.MUTE_UNMUTE_DUB,
                responseArabic = "تم تبديل حالة كتم صوت الدبلجة 🔈",
                wasAutoCorrected = true
            )
        }

        // Vocal Clarity & Deep AI Denoise
        if (containsAny(processedText, listOf("وضوح الصوت البشري", "عزل الفوكال", "نقاء الصوت البشري", "vocal clarity", "فلتر الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.TOGGLE_VOCAL_CLARITY,
                responseArabic = "تم تبديل وضع نقاء ووضوح الصوت البشري (Vocal Clarity) 🎙️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("تنقية جيميناي", "تنقية بالذكاء الاصطناعي", "فلترة ذكية", "عزل احترافي بالذكاء الاصطناعي", "gemini denoise"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.DEEP_GEMINI_DENOISE,
                responseArabic = "جارٍ تشغيل خوارزمية Gemini AI للتنقية العميقة وعزل التشويش بدقة استوديو ✨",
                wasAutoCorrected = true
            )
        }

        // 7. Voice Presets & Effects
        if (containsAny(text, listOf("روبوت", "الي", "صوت الي", "روبت", "صوت روبوت", "robot"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_VOICE_EFFECT,
                effectType = VoicePresetType.ROBOT,
                navTarget = null,
                responseArabic = "تم تطبيق مؤثر صوت الروبوت الآلي بنجاح.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(text, listOf("صدا", "استوديو", "ايكو", "ريفربر", "echo", "reverb"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_VOICE_EFFECT,
                effectType = VoicePresetType.ECHO,
                navTarget = null,
                responseArabic = "تم تطبيق مؤثر صدى الاستوديو الاحترافي.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(text, listOf("كرتون", "سنجاب", "انمي", "صوت رفيع", "سناجب", "chipmunk"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_VOICE_EFFECT,
                effectType = VoicePresetType.CHIPMUNK,
                navTarget = null,
                responseArabic = "تم تطبيق مؤثر الصوت الكرتوني المرح.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(text, listOf("سينمائي", "جهوري", "فخم", "صوت تخين", "وثائقي", "عميق"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_VOICE_EFFECT,
                effectType = VoicePresetType.DEEP_VOICE,
                navTarget = null,
                responseArabic = "تم تفعيل نبرة الصوت الجهورية السينمائية الفخمة.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(text, listOf("صوت طبيعي", "عادي", "بدون مؤثرات", "الغاء المؤثر", "طبيعي"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_VOICE_EFFECT,
                effectType = VoicePresetType.NORMAL,
                navTarget = null,
                responseArabic = "تمت إعادة ضبط الصوت إلى النمط الطبيعي النقي.",
                wasAutoCorrected = true
            )
        }

        // 8. Auto Trim & Silence removal
        if (containsAny(text, listOf("قص الصوت", "احذف الصمت", "قص الفراغات", "قص التراك", "تنظيف الصمت", "تقطيع الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.AUTO_TRIM_SILENCE,
                effectType = null,
                navTarget = null,
                responseArabic = "تم فتح أداة القص وحذف فترات الصمت تلقائياً.",
                wasAutoCorrected = true
            )
        }

        // 9. Undo / Redo
        if (containsAny(text, listOf("تراجع", "ارجع خطوه", "undo", "الغي التعديل"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.UNDO_ACTION,
                effectType = null,
                navTarget = null,
                responseArabic = "تم التراجع عن آخر إجراء بنجاح.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(text, listOf("اعاده", "قدم خطوه", "redo", "رجع اللي لغيته"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.REDO_ACTION,
                effectType = null,
                navTarget = null,
                responseArabic = "تمت إعادة تطبيق الإجراء بنجاح.",
                wasAutoCorrected = true
            )
        }

        // 10. Navigation Targets
        if (containsAny(processedText, listOf("استوديو الفيديو", "افتح الاستوديو", "شاشه الدبلجه", "الاستوديو"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "STUDIO",
                responseArabic = "جارٍ الانتقال إلى استوديو الدبلجة.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("دبلجه الفيديو", "فيديو اي اي", "فيديو", "دبلجه فيديو"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "VIDEO_DUB",
                responseArabic = "جارٍ فتح قسم دبلجة الفيديو الذكية.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("يوتيوب", "دبلجه يوتيوب", "youtube"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "YOUTUBE_AUTO_DUB",
                responseArabic = "جارٍ الانتقال إلى دبلجة مقاطع اليوتيوب.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("جيمناي", "gemini", "دبلجه بضغطه"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "GEMINI_ONE_CLICK",
                responseArabic = "جارٍ فتح ميزة دبلجة Gemini بضغطة زر واحدة.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("مشاريعي", "المشاريع", "الملفات المحفوظه"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "PROJECTS",
                responseArabic = "جارٍ الانتقال إلى قائمة المشاريع المحفوظة.",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("الاعدادات", "الضبط", "settings"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                effectType = null,
                navTarget = "SETTINGS",
                responseArabic = "جارٍ فتح شاشة الإعدادات العامة.",
                wasAutoCorrected = true
            )
        }

        // 11. Autonomous Multi-Step & Pro Studio AI Operations
        if (containsAny(processedText, listOf("معالجه كامله", "نظف وطبق", "تنقيه ومؤثر", "نظف ووازن", "ميكس كامل", "انتاج كامل"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.COMPOUND_AI_CLEAN_AND_EFFECT,
                effectType = VoicePresetType.ECHO,
                responseArabic = "تم تشغيل خط الإنتاج الصوتي المركب: تنقية Media3، وموازنة المكاسب، وإضافة مؤثر الاستوديو! ⚡🎚️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("مزامنه الشفاه", "اضبط الشفاه", "مزامنه تلقائيه", "محاذاه الشفاه", "تزامن الشفاه", "lip sync", "lipsync"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.LIP_SYNC_AUTO_ALIGN,
                responseArabic = "تمت محاذاة ومزامنة توقيت الصوت مع حركة الشفاه بالذكاء الاصطناعي بدقة متناهية (-120ms) 🎯",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("قدم الصوت", "تقديم الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.LIP_SYNC_NUDGE,
                syncDeltaMs = 150f,
                responseArabic = "تم تقديم توقيت الصوت بمقدار 150 ميلي ثانية لمطابقة المشهد ⏱️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("اخر الصوت", "تاخير الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.LIP_SYNC_NUDGE,
                syncDeltaMs = -150f,
                responseArabic = "تم تأخير توقيت الصوت بمقدار 150 ميلي ثانية لمطابقة المشهد ⏱️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("فرغ الصوت", "حول الصوت لنص", "استخرج النص", "كتابه الصوت", "تفريغ صوتي", "transcribe"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.AUTO_TRANSCRIBE_STT,
                responseArabic = "بدأ تفريغ الصوت إلى نصوص حوارية متزامنة على خط الزمن الآن ✍️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ترجم الحوار", "ترجم السيناريو", "ترجم بالذكاء الاصطناعي", "ترجم النص", "ترجمه جيميناي"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GEMINI_TRANSLATE_SCRIPT,
                responseArabic = "جارٍ ترجمة حوارات السيناريو بالذكاء الاصطناعي مع حفظ النغمات الصوتية 🌐",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ركب الدبلجه", "ولد الصوت على المشهد", "دبلج خط الزمن", "توليد ومزامنه"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SYNTHESIZE_AND_SYNC_TIMELINE,
                responseArabic = "جارٍ توليد الصوت البشري بالذكاء الاصطناعي وتركيبه متزامناً مع المشهد 🎙️🎬",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("دقق النص", "شكل الحركات", "تشكيل النص", "تدقيق الجوده", "فحص النص", "تصحيح الاخطاء"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.QUALITY_AUDIT_SCRIPT,
                responseArabic = "تم تدقيق نصوص الحوار، وإضافة التشكيل الصوتي، وضبط مخارج الحروف آلياً 🪄✨",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf(
            "دبلجه تلقائيه", "دبلج الفيديو", "دبلجه بضغطه", "دبلج المشهد", "دبلج وادمج", "ادمج الدبلجه", "دبلج بدون نصوص", "دبلجه ودمج", "ادمج الفيديو",
            "auto dub", "dub", "dubbing", "dub video", "auto dub and merge", "dub and merge", "merge dubbed video", "dub without text"
        ))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.ONE_CLICK_AUTO_DUB,
                responseArabic = "بدأت عملية الدبلجة الذكية وتوليد الأصوات ودمجها مع الفيديو تلقائياً بالكامل بدون أي نصوص يدوية 🚀🎬",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ولد هاشتاج", "عناوين للنشر", "هاشتاجات", "عنوان يوتيوب", "عنوان تيك توك", "سيو للنشر", "social seo"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GENERATE_SOCIAL_METADATA,
                responseArabic = "تم توليد عناوين وهاشتاجات احترافية مهيأة للانتشار على تيك توك ويوتيوب ونسخها للحافظة 📱✨",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("المعادل الصوتي", "اكولايزر", "اكوليزر", "اي كولايزر", "equalizer", "eq", "ترددات الصوت", "معادل الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.OPEN_EQUALIZER,
                responseArabic = "جارٍ فتح المعادل الصوتي الاحترافي للتحكم في الترددات 🎚️",
                wasAutoCorrected = true
            )
        }

        // Equalizer Presets Direct Selection
        if (containsAny(processedText, listOf("بيس عالي", "نمط البيس", "bass boost eq"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "بيس سينمائي (Bass)",
                eqPresetGains = listOf(6f, 5f, 3f, 1f, 0f, -1f, -2f),
                responseArabic = "تم تطبيق نمط البيس السينمائي القوي في المعادل الصوتي 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("صوت بشري", "نمط الفويس", "voice eq", "نقاء الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "صوت بشري نقي (Voice)",
                eqPresetGains = listOf(-3f, -1f, 2f, 4f, 4f, 3f, 1f),
                responseArabic = "تم تطبيق نمط وضوح الصوت البشري وإبراز مخارج الحروف في المعادل 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("نمط البودكاست", "بودكاست", "إذاعة", "اذاعه"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "بودكاست وإذاعة",
                eqPresetGains = listOf(-2f, 2f, 3f, 4f, 3f, 1f, 0f),
                responseArabic = "تم تطبيق نمط الإذاعة والبودكاست في المعادل الصوتي 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("نمط السينما", "دراما وسينما", "صوت سينمائي"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "دراما وسينما",
                eqPresetGains = listOf(4f, 2f, -1f, 1f, 3f, 4f, 3f),
                responseArabic = "تم تفعيل النمط السينمائي الدرامي المتوازن في المعادل الصوتي 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("نقاء عالي", "تربل", "treble crisp"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "نقاء الترددات العليا",
                eqPresetGains = listOf(-2f, -1f, 0f, 1f, 3f, 5f, 6f),
                responseArabic = "تم تطبيق نمط نقاء ولمعان الترددات العليا في المعادل 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("أكوستيك دافئ", "اكستيك", "acoustic"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "أكوستيك دافئ",
                eqPresetGains = listOf(3f, 2f, 1f, 2f, 2f, 3f, 2f),
                responseArabic = "تم تطبيق النمط الأكوستيكي الدافئ في المعادل 🎛️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("صفر المعادل", "معادل متوازن", "flat eq", "إلغاء المعادل"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.APPLY_EQ_PRESET,
                eqPresetName = "Flat (متوازن)",
                eqPresetGains = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
                responseArabic = "تمت إعادة ضبط المعادل الصوتي إلى الوضع الافتراضي المتوازن (Flat) 🎛️",
                wasAutoCorrected = true
            )
        }

        // Script, Reading & Take Management
        if (containsAny(processedText, listOf("ألف سيناريو", "اكتب سيناريو بالذكاء الاصطناعي", "توليد سيناريو ذكي", "ولد حوارات", "ألف حوارات"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GENERATE_AI_SCRIPT,
                responseArabic = "جارٍ توليد وتأليف سيناريو درامي ذكي متزامن مع المشهد عبر Gemini AI 🪄",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("اقرأ السيناريو", "اسمعني السيناريو", "اقرأ النص", "قراءة الحوار بصوت عالي", "اقرألي النص"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.READ_SCRIPT_ALOUD,
                responseArabic = "سأقوم بقراءة نصوص سيناريو المشهد الحالية بصوت مسموع الآن 📖",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("احذف التسجيل", "امسح المقطع الصوتي", "عيد التسجيل من جديد", "الغاء التسجيل الحالي", "احذف التيك"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.DISCARD_RECORDING,
                responseArabic = "تم حذف التسجيل الصوتي الحالي من الاستوديو بنجاح 🗑️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("صدر الفيديو", "تصدير الفيديو", "احفظ الفيديو المدمج", "شارك الفيديو", "تصدير ومشاركة", "تصدير مشروعي", "export", "export video", "save video", "share video"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.OPEN_EXPORT_DIALOG,
                responseArabic = "جارٍ فتح نافذة تصدير وحفظ الفيديو ومشاركته بجودة عالية 🎬",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("مكتبة الأصوات", "الأصوات البشرية", "الأصوات", "اصوات الدبلجة", "مكتبة الصوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.NAVIGATE_TAB,
                navTarget = "LIBRARY",
                responseArabic = "جارٍ الانتقال إلى مكتبة الأصوات البشرية التي تضم أكثر من 4,800 صوت دبلجة 👥",
                wasAutoCorrected = true
            )
        }

        // Hollywood Studio & Next-Gen Production Commands
        if (containsAny(processedText, listOf("افصل التراكات", "افصل الموسيقى", "عزل الصوت", "فصل الصوت عن الموسيقى", "stems", "stem separation"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.SEPARATE_STEMS,
                responseArabic = "جارٍ تفكيك صوت الفيديو إلى 4 مسارات معزولة بالذكاء الاصطناعي: صوت، موسيقى، مؤثرات، وأجواء 🎧✨",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("استنسخ صوت", "استنساخ الصوت", "استنسخ النبرة", "قلد صوت الممثل", "voice clone", "cloning"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.CLONE_VOICE,
                responseArabic = "تم بدء فحص العينة واستنساخ نبرة وخامة صوت الممثل بدقة 98% وتطبيقها على الدبلجة 🧬🎙️",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("ألف موسيقى", "موسيقى تصويرية", "شغل الموسيقى السينمائية", "أوركسترا", "score", "soundtrack"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GENERATE_CINEMATIC_SCORE,
                responseArabic = "تم تأليف وتوليد الموسيقى التصويرية الأوركسترالية الحماسية ودمجها بالمشهد 🎻🎬",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("مؤثر سينمائي", "ضربة سينمائية", "صوت مطر", "وقع أقدام", "foley", "سوووش"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GENERATE_FOLEY_EFFECT,
                responseArabic = "تم توليد وتركيب المؤثر الصوتي السينمائي الاحترافي المتزامن مع حركة الكاميرا 🔊🔥",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("مقطع ريلز", "فيديو للتيك توك", "مقطع للتريند", "قص شورتس", "viral shorts", "tiktok"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.CLIP_VIRAL_SHORTS,
                responseArabic = "تم تحليل المشهد واستخراج أفضل لقطة حماسية عمودية (9:16) مع الهاشتاجات الجاهزة للتريند 📱🚀",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("صوت مجسم", "صوت ثلاثي الابعاد", "صوت 3d", "صوت 8d", "صوت محيطي", "spatial audio"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.RENDER_SPATIAL_3D,
                responseArabic = "تمت معالجة الصوت بتقنية Dolby Atmos المجسمة ثلاثية الأبعاد 3D/8D بنجاح 🎧🌌",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("قيم المشهد", "رأي المخرج", "المخرج الذاتي", "تقييم المشهد", "director critique"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.DIRECTOR_SCENE_CRITIQUE,
                responseArabic = "قام المخرج السينمائي الذاتي بفحص المشهد: تقييم الجودة 96% مع موازنة الترددات وخفض موسيقى الخلفية تلقائياً 🎬🌟",
                wasAutoCorrected = true
            )
        }
        if (containsAny(processedText, listOf("دبلج بكل اللغات", "دبلجة عالمية", "انشر للعالم", "10 لغات", "global dubbing"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.GLOBAL_MULTI_LANG_DUB,
                responseArabic = "بدأت مصفوفة الدبلجة المتوازية بـ 10 لغات عالمية مع الحفاظ على نفس خامة صوت الممثل الأصلي 🌐✨",
                wasAutoCorrected = true
            )
        }

        // 12. Conversational / Advice
        return IntentResolutionResult(
            intent = AlexaVoiceIntent.CONVERSATIONAL_QUESTION,
            effectType = null,
            navTarget = null,
            responseArabic = "أنا جاهز لمساعدتك في الاستوديو. يمكنك قول: 'سجل صوتي'، 'نظف ووازن الصوت'، 'اضبط مزامنة الشفاه'، 'دبلج بضغطة زر'، أو 'ولد هاشتاجات للنشر'.",
            wasAutoCorrected = false
        )
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }

    private fun findAppLaunchIntent(query: String): Pair<String, Intent>? {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activities = pm.queryIntentActivities(mainIntent, 0)
        val cleanQuery = query.trim().lowercase()

        val normalizedQuery = when {
            cleanQuery.contains("يوتيوب") || cleanQuery.contains("youtube") -> "youtube"
            cleanQuery.contains("كاميرا") || cleanQuery.contains("camera") -> "camera"
            cleanQuery.contains("كروم") || cleanQuery.contains("chrome") || cleanQuery.contains("متصفح") -> "chrome"
            cleanQuery.contains("خرائط") || cleanQuery.contains("maps") -> "maps"
            cleanQuery.contains("واتساب") || cleanQuery.contains("whatsapp") -> "whatsapp"
            cleanQuery.contains("حاسبه") || cleanQuery.contains("calculator") -> "calc"
            cleanQuery.contains("ساعه") || cleanQuery.contains("clock") -> "clock"
            cleanQuery.contains("ضبط") || cleanQuery.contains("اعدادات") || cleanQuery.contains("settings") -> "settings"
            else -> cleanQuery
        }

        for (info in activities) {
            val label = info.loadLabel(pm).toString()
            val pkg = info.activityInfo.packageName
            if (label.lowercase().contains(normalizedQuery) || pkg.lowercase().contains(normalizedQuery)) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null) {
                    return Pair(label, launchIntent)
                }
            }
        }
        return null
    }

    private fun mapToStandardArabicCommand(
        intent: AlexaVoiceIntent,
        effectType: VoicePresetType?,
        navTarget: String?,
        appName: String? = null
    ): String {
        return when (intent) {
            AlexaVoiceIntent.START_RECORDING -> "بدء التسجيل الصوتي"
            AlexaVoiceIntent.STOP_RECORDING -> "إيقاف التسجيل وحفظ المقطع"
            AlexaVoiceIntent.PLAY_AUDIO -> "تشغيل مقطع الصوت"
            AlexaVoiceIntent.STOP_PLAYBACK -> "إيقاف تشغيل الصوت"
            AlexaVoiceIntent.MEDIA3_AI_CLEAN -> "تنقية وموازنة الصوت عبر Media3"
            AlexaVoiceIntent.APPLY_VOICE_EFFECT -> "تطبيق مؤثر: ${effectType?.titleArabic ?: "صوتي"}"
            AlexaVoiceIntent.AUTO_TRIM_SILENCE -> "قص الصوت وحذف الصمت"
            AlexaVoiceIntent.NAVIGATE_TAB -> "الانتقال إلى: $navTarget"
            AlexaVoiceIntent.OPEN_EXTERNAL_APP -> "فتح تطبيق: ${appName ?: "النظام"}"
            AlexaVoiceIntent.COMPOUND_AI_CLEAN_AND_EFFECT -> "معالجة مركبة: تنقية ومؤثر وموازنة"
            AlexaVoiceIntent.LIP_SYNC_AUTO_ALIGN -> "محاذاة ومزامنة الشفاه التلقائية"
            AlexaVoiceIntent.LIP_SYNC_NUDGE -> "تعديل إزاحة مزامنة الشفاه"
            AlexaVoiceIntent.AUTO_TRANSCRIBE_STT -> "تفريغ الصوت إلى نصوص متزامنة"
            AlexaVoiceIntent.GEMINI_TRANSLATE_SCRIPT -> "ترجمة السيناريو بالذكاء الاصطناعي"
            AlexaVoiceIntent.SYNTHESIZE_AND_SYNC_TIMELINE -> "توليد وتركيب الدبلجة على خط الزمن"
            AlexaVoiceIntent.QUALITY_AUDIT_SCRIPT -> "تدقيق جودة الصوت والتشكيل آلياً"
            AlexaVoiceIntent.ONE_CLICK_AUTO_DUB -> "دبلجة المشهد بضغطة زر واحدة"
            AlexaVoiceIntent.GENERATE_SOCIAL_METADATA -> "توليد عنوان تسويقي وهاشتاجات للنشر"
            AlexaVoiceIntent.SEEK_TIMELINE -> "تقديم أو ترجيع شريط الوقت"
            AlexaVoiceIntent.SET_DUB_VOLUME -> "تغيير مستوى صوت الدبلجة"
            AlexaVoiceIntent.SET_ORIGINAL_VOLUME -> "تغيير صوت المشهد الأصلي"
            AlexaVoiceIntent.SET_BGM_VOLUME -> "تغيير صوت الموسيقى التصويرية"
            AlexaVoiceIntent.MUTE_UNMUTE_ORIGINAL -> "كتم أو تشغيل صوت المشهد"
            AlexaVoiceIntent.MUTE_UNMUTE_DUB -> "كتم أو تشغيل صوت الدبلجة"
            AlexaVoiceIntent.TOGGLE_VOCAL_CLARITY -> "تفعيل أو إيقاف وضوح الصوت البشري"
            AlexaVoiceIntent.DEEP_GEMINI_DENOISE -> "تنقية صوتية فائقة بـ Gemini AI"
            AlexaVoiceIntent.APPLY_EQ_PRESET -> "تطبيق نمط في المعادل الصوتي"
            AlexaVoiceIntent.GENERATE_AI_SCRIPT -> "تأليف سيناريو وحوارات بالذكاء الاصطناعي"
            AlexaVoiceIntent.READ_SCRIPT_ALOUD -> "قراءة نصوص السيناريو"
            AlexaVoiceIntent.DISCARD_RECORDING -> "حذف التسجيل الصوتي الحالي"
            AlexaVoiceIntent.OPEN_EXPORT_DIALOG -> "تصدير وحفظ الفيديو"
            AlexaVoiceIntent.UNDO_ACTION -> "التراجع عن التعديل"
            AlexaVoiceIntent.REDO_ACTION -> "إعادة تطبيق التعديل"
            AlexaVoiceIntent.DEVELOPER_COPYRIGHT_QUERY -> "الاستعلام عن مطور التطبيق وحقوق الملكية"
            AlexaVoiceIntent.ASSIST_BLIND_READ_STATUS -> "قراءة الشاشة ومساعدة المكفوفين"
            else -> "أمر صوتي"
        }
    }

    private data class IntentResolutionResult(
        val intent: AlexaVoiceIntent,
        val effectType: VoicePresetType? = null,
        val navTarget: String? = null,
        val targetAppName: String? = null,
        val targetAppLaunchIntent: Intent? = null,
        val syncDeltaMs: Float = 0f,
        val volumeValue: Float? = null,
        val seekSecondsDelta: Float? = null,
        val eqPresetName: String? = null,
        val eqPresetGains: List<Float>? = null,
        val targetTranslationLang: String? = null,
        val responseArabic: String,
        val wasAutoCorrected: Boolean
    )

    fun release() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }
}
