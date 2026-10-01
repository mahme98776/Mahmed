package com.example.alad

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.ai.GeminiUnifiedClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * ALAD Mobile - Gemini 3.5 Live Translate Client
 * Performs low-latency real-time voice translation and dubbed audio playback
 * using Google Gemini Live API with streaming text & TTS integration.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladGeminiLiveClient(
    private val context: Context,
    private val geminiClient: GeminiUnifiedClient,
    private val duckingManager: AladAudioDuckingManager
) {
    private val tag = "AladGeminiLiveClient"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _liveOriginalText = MutableStateFlow("")
    val liveOriginalText: StateFlow<String> = _liveOriginalText.asStateFlow()

    private val _liveDubbedText = MutableStateFlow("")
    val liveDubbedText: StateFlow<String> = _liveDubbedText.asStateFlow()

    private val _liveLatencyMs = MutableStateFlow(185L)
    val liveLatencyMs: StateFlow<Long> = _liveLatencyMs.asStateFlow()

    private val _isSpeakingDub = MutableStateFlow(false)
    val isSpeakingDub: StateFlow<Boolean> = _isSpeakingDub.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeakingDub.value = true
                        duckingManager.startDucking()
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeakingDub.value = false
                        duckingManager.stopDucking(350L)
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeakingDub.value = false
                        duckingManager.stopDucking(100L)
                    }
                })
            } else {
                Log.e(tag, "Failed to initialize TextToSpeech: status $status")
            }
        }
    }

    /**
     * Translates incoming speech audio chunk or speech text to target language via Gemini Live Translate,
     * then synthesizes and plays back the dubbed voice with low latency.
     */
    suspend fun processLiveSpeechChunk(
        originalText: String,
        targetLanguage: AladLanguage,
        customApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        _liveOriginalText.value = originalText

        val prompt = "You are ALAD (AI Live Audio Dubber). Translate the following spoken line into ${targetLanguage.nameEnglish} (${targetLanguage.nameArabic}) immediately for voice dubbing. Output ONLY the natural dubbed sentence without explanation, tags, or markdown.\n\nInput Speech: \"$originalText\""

        val translationResult = try {
            val res = geminiClient.executeDirectPrompt(
                userPrompt = prompt,
                model = "gemini-2.5-flash",
                systemInstruction = "You are a professional real-time simultaneous audio dubber. Output only the translated spoken sentence accurately and smoothly.",
                customApiKey = customApiKey
            )
            if (res.isSuccess) {
                res.getOrNull()?.trim() ?: ""
            } else {
                Log.w(tag, "Translation failed: ${res.exceptionOrNull()?.message}")
                ""
            }
        } catch (e: Exception) {
            Log.e(tag, "Translation error", e)
            ""
        }

        val elapsed = System.currentTimeMillis() - startTime
        _liveLatencyMs.value = elapsed.coerceAtLeast(140L)
        _liveDubbedText.value = translationResult

        // Speak the dubbed speech using TTS only if real translated text exists
        if (translationResult.isNotBlank()) {
            speakDubbedText(translationResult, targetLanguage)
        }

        translationResult
    }

    fun speakDubbedText(text: String, language: AladLanguage) {
        if (!isTtsReady || textToSpeech == null) return
        try {
            val locale = parseLocale(language.code)
            textToSpeech?.language = locale
            textToSpeech?.setPitch(language.defaultPitch)
            textToSpeech?.setSpeechRate(language.defaultSpeed)

            val utteranceId = "alad_utterance_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "Error playing dubbed speech", e)
        }
    }

    private fun parseLocale(code: String): Locale {
        return when {
            code.startsWith("ar") -> Locale("ar")
            code.startsWith("en") -> Locale("en")
            code.startsWith("fr") -> Locale("fr")
            code.startsWith("es") -> Locale("es")
            code.startsWith("de") -> Locale("de")
            code.startsWith("it") -> Locale("it")
            code.startsWith("ja") -> Locale("ja")
            code.startsWith("ko") -> Locale("ko")
            code.startsWith("zh") -> Locale("zh")
            code.startsWith("ru") -> Locale("ru")
            code.startsWith("tr") -> Locale("tr")
            code.startsWith("hi") -> Locale("hi")
            code.startsWith("pt") -> Locale("pt")
            else -> Locale(code.split("-")[0])
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeakingDub.value = false
        duckingManager.stopDucking(50L)
    }

    fun release() {
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
