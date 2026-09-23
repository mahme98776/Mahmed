package com.example.audio.stt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.model.ScriptLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * State representation for Android Speech-To-Text processing.
 */
sealed class SttState {
    object Idle : SttState()
    object Initializing : SttState()
    object Listening : SttState()
    object Processing : SttState()
    data class Success(val recognizedText: String, val confidenceScores: List<Float> = emptyList()) : SttState()
    data class Error(val errorMessage: String, val errorCode: Int? = null) : SttState()
}

/**
 * Robust Speech-To-Text Manager for Android.
 * Leverages [android.speech.SpeechRecognizer] for offline & online transcription,
 * with intelligent timing estimation for synchronized dubbing workflows
 * and full accessibility support for blind users.
 */
class SpeechToTextManager(private val context: Context) {

    private val tag = "SpeechToTextManager"
    private var speechRecognizer: SpeechRecognizer? = null

    private val _sttState = MutableStateFlow<SttState>(SttState.Idle)
    val sttState: StateFlow<SttState> = _sttState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var onResultCallback: ((String) -> Unit)? = null

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                Log.i(tag, "SpeechRecognizer created successfully")
            } else {
                Log.w(tag, "SpeechRecognizer is not natively available on this device/ROM")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize SpeechRecognizer", e)
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _sttState.value = SttState.Listening
            _isListening.value = true
            Log.d(tag, "STT: Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            _sttState.value = SttState.Listening
            _isListening.value = true
            Log.d(tag, "STT: Beginning of speech detected")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _sttState.value = SttState.Processing
            _isListening.value = false
            Log.d(tag, "STT: End of speech, processing...")
        }

        override fun onError(error: Int) {
            _isListening.value = false
            val errorMsg = mapErrorCodeToMessage(error)
            Log.w(tag, "STT Error code: $error - $errorMsg")
            _sttState.value = SttState.Error(errorMsg, error)
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.toList() ?: emptyList()
            val text = matches?.firstOrNull()?.trim() ?: ""

            if (text.isNotBlank()) {
                _sttState.value = SttState.Success(text, scores)
                _partialText.value = text
                onResultCallback?.invoke(text)
                Log.i(tag, "STT Success: $text")
            } else {
                _sttState.value = SttState.Error("لم يتم التقاط أي نص واضح، يرجى المحاولة مرة أخرى")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?.trim() ?: ""
            if (text.isNotBlank()) {
                _partialText.value = text
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Starts voice recognition with specified language code.
     */
    fun startListening(
        languageCode: String = "ar-SA",
        onResult: (String) -> Unit
    ) {
        onResultCallback = onResult
        _partialText.value = ""
        _sttState.value = SttState.Initializing

        if (speechRecognizer == null) {
            initRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            _sttState.value = SttState.Error("خدمة التعرف على الصوت غير مدعومة مباشرة في هذا النظام، يمكنك كتابة أو لصق النص.")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Exception while starting SpeechRecognizer", e)
            _sttState.value = SttState.Error("حدث خطأ أثناء بدء الاستماع: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
        } catch (e: Exception) {
            Log.w(tag, "Error stopping SpeechRecognizer", e)
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
            _isListening.value = false
            _sttState.value = SttState.Idle
        } catch (e: Exception) {
            Log.w(tag, "Error cancelling SpeechRecognizer", e)
        }
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
        } catch (e: Exception) {
            Log.w(tag, "Error releasing SpeechRecognizer", e)
        }
    }

    /**
     * Splits full transcribed text into chronologically synchronized [ScriptLine] items
     * mapped precisely across the video clip duration.
     */
    fun buildSynchronizedScriptLines(
        rawText: String,
        totalClipDurationSeconds: Int,
        characterName: String = "المتحدث الرئيسي",
        characterAvatar: String = "🎙️"
    ): List<ScriptLine> {
        val cleaned = rawText.trim()
        if (cleaned.isBlank()) return emptyList()

        // Split by punctuation marks: period, comma, question mark, newline, semicolon
        val rawSentences = cleaned.split(Regex("[.\\n،؟?,;!]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val sentences = if (rawSentences.isEmpty()) listOf(cleaned) else rawSentences
        val count = sentences.size
        val safeDuration = totalClipDurationSeconds.coerceAtLeast(3).toFloat()

        // Distribute timestamps proportionally
        val step = safeDuration / count.toFloat()

        return sentences.mapIndexed { index, sentence ->
            val startSec = (index * step).coerceAtLeast(0f)
            val endSec = ((index + 1) * step).coerceAtMost(safeDuration)
            ScriptLine(
                id = UUID.randomUUID().toString(),
                characterName = characterName,
                characterAvatar = characterAvatar,
                textArabic = sentence,
                textOriginal = sentence,
                startSeconds = String.format(Locale.US, "%.1f", startSec).toFloat(),
                endSeconds = String.format(Locale.US, "%.1f", endSec).toFloat(),
                voiceType = "ARABIC_MALE",
                isDubbed = false,
                customAudioPath = null
            )
        }
    }

    private fun mapErrorCodeToMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "خطأ في تسجيل الصوت"
            SpeechRecognizer.ERROR_CLIENT -> "خطأ في اتصال تطبيق التعرف على الصوت"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يتطلب إذن الوصول إلى الميكروفون"
            SpeechRecognizer.ERROR_NETWORK -> "خطأ في شبكة الإنترنت أثناء التعرف على الصوت"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "انتهت مهلة الاتصال بالشبكة"
            SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على كلمات مفهومة"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "خدمة التعرف على الصوت مشغولة حالياً"
            SpeechRecognizer.ERROR_SERVER -> "خطأ في خادم التعرف على الصوت"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "لم يتم استلام أي صوت في الوقت المحدد"
            else -> "تعذر التعرف على الصوت (كود: $errorCode)"
        }
    }
}
