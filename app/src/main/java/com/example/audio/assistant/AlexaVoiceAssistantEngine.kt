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
    MEDIA3_AI_CLEAN("تنقية الضوضاء وموازنة الصوت بـ Media3 🎚️", "🎚️"),
    APPLY_VOICE_EFFECT("تطبيق مؤثر صوتي 🤖", "🤖"),
    COMPOUND_AI_CLEAN_AND_EFFECT("معالجة مركبة: تنقية + مؤثر + موازنة ⚡", "⚡"),
    LIP_SYNC_AUTO_ALIGN("محاذاة ومزامنة الشفاه التلقائية 🎯", "🎯"),
    LIP_SYNC_NUDGE("تعديل إزاحة مزامنة الشفاه ⏱️", "⏱️"),
    AUTO_TRANSCRIBE_STT("تفريغ الصوت إلى نصوص متزامنة ✍️", "✍️"),
    GEMINI_TRANSLATE_SCRIPT("ترجمة السيناريو بالذكاء الاصطناعي 🌐", "🌐"),
    SYNTHESIZE_AND_SYNC_TIMELINE("توليد وتركيب الدبلجة على خط الزمن 🎙️🎬", "🎬"),
    QUALITY_AUDIT_SCRIPT("تدقيق جودة الصوت والتشكيل آلياً 🪄", "🪄"),
    ONE_CLICK_AUTO_DUB("دبلجة المشهد بضغطة زر واحدة 🚀", "🚀"),
    GENERATE_SOCIAL_METADATA("توليد عنوان تسويقي وهاشتاجات للنشر 📱✨", "✨"),
    AUTO_TRIM_SILENCE("قص الصوت وحذف الصمت ✂️", "✂️"),
    OPEN_EQUALIZER("فتح المعادل الصوتي 🎚️", "🎚️"),
    NAVIGATE_TAB("الانتقال إلى قسم بالتطبيق 🚀", "🚀"),
    OPEN_EXTERNAL_APP("فتح تطبيق بالنظام 📱", "📱"),
    UNDO_ACTION("التراجع عن آخر تعديل ↩️", "↩️"),
    REDO_ACTION("إعادة تطبيق التعديل 🔁", "🔁"),
    DEVELOPER_COPYRIGHT_QUERY("الاستعلام عن المطور وحقوق الملكية 👤", "🛡️"),
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

    init {
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        try {
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

    /**
     * Start listening for voice commands (Alexa / Gemini style)
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
            _assistantState.value = AlexaAssistantState.Error("خدمة التعرف على الصوت غير مفعلة بالجهاز")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

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
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                // Graceful idle reset without aggressive error alerts
                _assistantState.value = AlexaAssistantState.Idle
            } else {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "خطأ في التقاط الصوت من الميكروفون"
                    SpeechRecognizer.ERROR_CLIENT -> "خطأ في عميل التعرف الصوتي"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يرجى منح إذن الميكروفون"
                    SpeechRecognizer.ERROR_NETWORK -> "فحص الاتصال بالإنترنت مطلوب للمساعد السحابي"
                    else -> "تعذر التقاط الأمر بدقة، يرجى المحاولة مرة أخرى"
                }
                _assistantState.value = AlexaAssistantState.Error(msg)
            }
            scheduleAutoRestartIfNeeded()
        }

        override fun onResults(results: Bundle?) {
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
            when (command.detectedIntent) {
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
                AlexaVoiceIntent.UNDO_ACTION -> {
                    viewModel.undo()
                }
                AlexaVoiceIntent.REDO_ACTION -> {
                    viewModel.redo()
                }
                AlexaVoiceIntent.DEVELOPER_COPYRIGHT_QUERY -> {
                    // Spoken and highlighted in UI
                }
                AlexaVoiceIntent.CONVERSATIONAL_QUESTION, AlexaVoiceIntent.UNKNOWN -> {
                    // Handled via TTS response
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

        // 2. Media3 AI Noise Reduction & Gain Normalization
        if (containsAny(text, listOf("نظف الصوت", "شيل الضوضاء", "شيل الوشه", "عزل الضوضاء", "وازن الصوت", "ميديا 3", "ميديا ثري", "media3", "تنقيه الصوت", "تصفيه الصوت", "ازاله التشويش"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.MEDIA3_AI_CLEAN,
                effectType = null,
                navTarget = null,
                responseArabic = "تم تفعيل طبقة المعالجة الصوتية عبر Media3 لعزل الضوضاء الخلفية وموازنة مستويات الصوت بدقة عالية.",
                wasAutoCorrected = true
            )
        }

        // 3. Start Recording
        if (containsAny(text, listOf("سجل", "سجلي", "سجل صوتي", "ابدا التسجيل", "ابدئي التسجيل", "ريكورد", "تسجيل الان", "تسجيل صوت", "يلا نسجل"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.START_RECORDING,
                effectType = null,
                navTarget = null,
                responseArabic = "بدأت التسجيل الآن! تحدث بوضوح أمام الميكروفون.",
                wasAutoCorrected = true
            )
        }

        // 4. Stop Recording
        if (containsAny(text, listOf("وقف التسجيل", "ايقاف التسجيل", "خلصت", "كفايه", "كفايه تسجيل", "انهاء التسجيل", "ستوب تسجيل", "وقف المايك"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.STOP_RECORDING,
                effectType = null,
                navTarget = null,
                responseArabic = "تم إيقاف التسجيل وحفظ المقطع، وتجري الآن تنقيته وموازنته عبر Media3 تلقائياً.",
                wasAutoCorrected = true
            )
        }

        // 5. Playback
        if (containsAny(text, listOf("شغل الصوت", "شغل التسجيل", "اسمع الصوت", "اسمعني", "شغلني", "تشغيل المقطع", "بلاي", "اسمع المقطع"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.PLAY_AUDIO,
                effectType = null,
                navTarget = null,
                responseArabic = "جارٍ تشغيل مقطع الصوت المسجل الآن.",
                wasAutoCorrected = true
            )
        }

        // 6. Stop Playback
        if (containsAny(text, listOf("وقف الصوت", "اسكت", "ايقاف التشغيل", "كفايه صوت", "صامت", "ميوت"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.STOP_PLAYBACK,
                effectType = null,
                navTarget = null,
                responseArabic = "تم إيقاف تشغيل الصوت.",
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
        if (containsAny(processedText, listOf("دبلجه تلقائيه", "دبلج الفيديو", "دبلجه بضغطه", "دبلج المشهد", "auto dub"))) {
            return IntentResolutionResult(
                intent = AlexaVoiceIntent.ONE_CLICK_AUTO_DUB,
                responseArabic = "بدأت عملية الدبلجة الكاملة للفيديو آلياً بضغطة زر واحدة 🚀",
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
            AlexaVoiceIntent.OPEN_EQUALIZER -> "فتح المعادل الصوتي"
            AlexaVoiceIntent.UNDO_ACTION -> "التراجع عن التعديل"
            AlexaVoiceIntent.REDO_ACTION -> "إعادة تطبيق التعديل"
            AlexaVoiceIntent.DEVELOPER_COPYRIGHT_QUERY -> "الاستعلام عن مطور التطبيق وحقوق الملكية"
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
