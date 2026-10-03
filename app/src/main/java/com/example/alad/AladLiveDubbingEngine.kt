package com.example.alad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.ai.GeminiUnifiedClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ALAD Mobile - Master AI Live Audio Dubbing Controller
 * Coordinates live internal/mic audio capture, real-time continuous speech recognition,
 * Gemini Live Translate streaming into any global language or dialect, dynamic audio ducking,
 * and low-latency voice synthesis.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladLiveDubbingEngine(
    private val context: Context,
    private val geminiClient: GeminiUnifiedClient
) {
    private val tag = "AladLiveDubbingEngine"
    private val scope = CoroutineScope(Dispatchers.Main)

    val duckingManager = AladAudioDuckingManager(context)
    val captureEngine = AladAudioCaptureEngine(context)
    val liveClient = AladGeminiLiveClient(context, geminiClient, duckingManager)

    private val _isLiveDubbing = MutableStateFlow(false)
    val isLiveDubbing: StateFlow<Boolean> = _isLiveDubbing.asStateFlow()

    private val _targetLanguage = MutableStateFlow(AladLanguageCatalog.findByCode("ar"))
    val targetLanguage: StateFlow<AladLanguage> = _targetLanguage.asStateFlow()

    private val _selectedTargetApp = MutableStateFlow("YouTube")
    val selectedTargetApp: StateFlow<String> = _selectedTargetApp.asStateFlow()

    private val _audioSourceMode = MutableStateFlow(AladAudioSource.INTERNAL_AND_MIC)
    val audioSourceMode: StateFlow<AladAudioSource> = _audioSourceMode.asStateFlow()

    private val _liveStatusMessage = MutableStateFlow("المحرك جاهز للدبلجة الحية التلقائية 🚀")
    val liveStatusMessage: StateFlow<String> = _liveStatusMessage.asStateFlow()

    private val _detectedSourceLanguage = MutableStateFlow("تحديد تلقائي (Auto-Detect)")
    val detectedSourceLanguage: StateFlow<String> = _detectedSourceLanguage.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var continuousRestartJob: Job? = null
    private var isRecognizerListening = false

    enum class AladAudioSource(val titleArabic: String, val descArabic: String) {
        INTERNAL_AND_MIC("التقاط صوت النظام الداخلي + الميكروفون", "يدبلج التطبيقات الخارجية مباشرة مثل يوتيوب ونتفليكس مع عزل الضوضاء"),
        MIC_ONLY("الميكروفون عالي النقاء فقط", "لالتقاط صوت السماعة الخارجية أو المكالمات والغرف الصوتية")
    }

    fun setTargetLanguage(language: AladLanguage) {
        _targetLanguage.value = language
        _liveStatusMessage.value = "تم تحديد لغة الدبلجة: ${language.nameArabic} ${language.flagEmoji}"
    }

    fun setTargetApp(appName: String) {
        _selectedTargetApp.value = appName
    }

    fun setAudioSource(source: AladAudioSource) {
        _audioSourceMode.value = source
    }

    /**
     * Start real live AI audio dubbing session
     */
    fun startLiveDubbing(): Boolean {
        if (_isLiveDubbing.value) return true

        val success = captureEngine.startCapture()
        if (success) {
            _isLiveDubbing.value = true
            _liveStatusMessage.value = "جارٍ الاستماع والدبلجة التلقائية الفورية إلى ${_targetLanguage.value.nameArabic} 🎙️✨"
            Log.d(tag, "ALAD Live Dubbing Started for ${_selectedTargetApp.value} -> ${_targetLanguage.value.nameArabic}")
            startContinuousSpeechRecognition()
        } else {
            _liveStatusMessage.value = "تعذر بدء التقاط الصوت، يرجى التأكد من إذن الميكروفون"
        }
        return success
    }

    /**
     * Stop real live dubbing session
     */
    fun stopLiveDubbing() {
        _isLiveDubbing.value = false
        continuousRestartJob?.cancel()
        continuousRestartJob = null
        stopSpeechRecognition()
        captureEngine.stopCapture()
        liveClient.stopSpeaking()
        duckingManager.release()
        _liveStatusMessage.value = "تم إيقاف الدبلجة الحية. المحرك في وضع الاستعداد ⏸️"
        Log.d(tag, "ALAD Live Dubbing Stopped")
    }

    private fun startContinuousSpeechRecognition() {
        scope.launch(Dispatchers.Main) {
            if (speechRecognizer == null) {
                initSpeechRecognizer()
            }
            startListeningIntent()
        }
    }

    private fun initSpeechRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isRecognizerListening = true
                    }

                    override fun onBeginningOfSpeech() {
                        _liveStatusMessage.value = "تم رصد صوت بشري... جارٍ الترجمة الحية ⚡"
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isRecognizerListening = false
                    }

                    override fun onError(error: Int) {
                        isRecognizerListening = false
                        handleRecognizerError(error)
                    }

                    override fun onResults(results: Bundle?) {
                        isRecognizerListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenPhrase = matches?.firstOrNull() ?: ""
                        if (spokenPhrase.isNotBlank()) {
                            processSpokenPhrase(spokenPhrase)
                        }
                        scheduleNextRecognition()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            // Update live stream text preview
                            _liveStatusMessage.value = "التقاط فوري: \"$partial\""
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing SpeechRecognizer", e)
        }
    }

    private fun startListeningIntent() {
        if (!_isLiveDubbing.value) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                // Multi-dialect support for Arabic and Global languages
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(
                    "ar-SA", "ar-EG", "ar-SY", "ar-MA", "en-US", "fr-FR", "es-ES", "de-DE", "tr-TR", "zh-CN"
                ))
            }
            speechRecognizer?.startListening(intent)
            isRecognizerListening = true
        } catch (e: Exception) {
            Log.e(tag, "Error starting speech recognition intent", e)
            scheduleNextRecognition(1500L)
        }
    }

    private fun processSpokenPhrase(phrase: String) {
        scope.launch(Dispatchers.IO) {
            try {
                _liveStatusMessage.value = "ترجمة ودبلجة: \"$phrase\" ⚡"
                val result = liveClient.processLiveSpeechChunk(
                    originalText = phrase,
                    targetLanguage = _targetLanguage.value
                )
                if (result.isNotBlank()) {
                    _liveStatusMessage.value = "تمت الدبلجة: \"$result\" 🗣️✨"
                }
            } catch (e: Exception) {
                Log.e(tag, "Error processing spoken phrase in liveClient", e)
            }
        }
    }

    private fun handleRecognizerError(error: Int) {
        if (!_isLiveDubbing.value) return
        scheduleNextRecognition(if (error == SpeechRecognizer.ERROR_NO_MATCH) 300L else 1000L)
    }

    private fun scheduleNextRecognition(delayMs: Long = 400L) {
        if (!_isLiveDubbing.value) return
        continuousRestartJob?.cancel()
        continuousRestartJob = scope.launch(Dispatchers.Main) {
            delay(delayMs)
            if (_isLiveDubbing.value && !isRecognizerListening) {
                startListeningIntent()
            }
        }
    }

    private fun stopSpeechRecognition() {
        try {
            isRecognizerListening = false
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(tag, "Error stopping SpeechRecognizer", e)
        }
    }

    fun release() {
        stopLiveDubbing()
        liveClient.release()
    }
}
