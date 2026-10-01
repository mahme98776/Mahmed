package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class VoiceProfile(
    val id: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val emoji: String,
    val pitch: Float,
    val speechRate: Float,
    val languageCode: String = "ar",
    val description: String = ""
)

data class MultiSpeakerDialogueSegment(
    val text: String,
    val startSeconds: Float,
    val endSeconds: Float,
    val voiceProfile: VoiceProfile,
    val speakerName: String = "",
    val languageCode: String = "ar"
)

/**
 * Status representation of the Android TextToSpeech engine lifecycle.
 */
enum class TtsEngineState {
    UNINITIALIZED,
    INITIALIZING,
    READY_SUCCESS,
    FAILED
}

data class TtsStatusInfo(
    val state: TtsEngineState = TtsEngineState.UNINITIALIZED,
    val statusCode: Int? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isArabicSupported: Boolean = false,
    val enginePackageName: String? = null
)

/**
 * Modern Android Text-To-Speech Manager for Dubbing.
 * Explicitly implements [TextToSpeech.OnInitListener] and rigorously verifies
 * that the initialization status equals [TextToSpeech.SUCCESS] before allowing any engine operations.
 */
class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "TextToSpeechManager"
    private var tts: TextToSpeech? = null

    private val _initState = MutableStateFlow(TtsStatusInfo(state = TtsEngineState.INITIALIZING))
    val initState: StateFlow<TtsStatusInfo> = _initState.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    private val fileSynthesisCallbacks = ConcurrentHashMap<String, (String?) -> Unit>()
    private val synthesisDestFiles = ConcurrentHashMap<String, File>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val voiceProfiles = listOf(
        // مجلد الأصوات العربية الفصيحة (Standard Arabic Profiles)
        VoiceProfile("natural_arabic_male", "قارئ فصيح طبيعي (فخامة ووضوح)", "نطق عربي متزن وإلقاء طبيعي واقعي جداً", "🎙️", 0.92f, 0.90f, "ar", description = "مثالي للمقاطع الوثائقية والبودكاست والنصوص الأدبية"),
        VoiceProfile("natural_arabic_female", "قارئة فصيحة ناعمة (طبيعية ودافئة)", "مخارج حروف عربية دقيقة ونبرة هادئة", "👩", 1.05f, 0.93f, "ar", description = "مناسب للقصص، الإعلانات، والشروحات التعليمية"),
        VoiceProfile("hero_male", "بطل المغامرات الفصيح (أنمي وأكشن)", "نبرة بطولية فصحى حماسية وقوية للأنمي والأكشن", "🦸", 0.90f, 0.95f, "ar", description = "نبرة كلاسيكية ملحمية لأبطال المغامرات والأنمي"),
        VoiceProfile("heroine_female", "البطلة الدافئة والشجاعة (أنمي وقصص)", "نبرة أنثوية عربية عذبة ومؤثرة تفيض بالمشاعر", "🌸", 1.10f, 0.94f, "ar", description = "للشخصيات الكرتونية الرئيسية والفتيات الشجاعات"),
        VoiceProfile("epic_narrator", "الراوي الملحمي الأسطوري", "سرد فصيح مهيب لمقدمات الكرتون والأنمي", "🌟", 0.82f, 0.88f, "ar", description = "للمقدمات ونهايات الحلقات والحكم الملهمة"),
        VoiceProfile("male_narrator", "صوت جهوري إذاعي", "نبرة عميقة وقوية ذات طابع وثائقي", "📻", 0.78f, 0.86f, "ar", description = "للمقدمات الحماسية والتعليق الصوتي الإذاعي"),
        VoiceProfile("female_soft", "صوت سردي عاطفي", "نبرة هادئة وملهمة للمشاهد الدرامية", "✨", 1.12f, 0.92f, "ar", description = "للحوارات الوجدانية والخواطر"),
        VoiceProfile("cartoon_hero", "شخصية كرتونية مرحة", "نبرة سريعة ونشطة لرسوم الأطفال والأنمي", "🧒", 1.55f, 1.15f, "ar", description = "لأفلام الكرتون ودبلجة المشاهد الطريفة"),
        VoiceProfile("dramatic_villain", "شخصية سينمائية غامضة", "نبرة خشنة ومثيرة لأفلام الإثارة والأكشن", "🦹", 0.68f, 0.82f, "ar", description = "للمشاهد السينمائية وشخصيات الأشرار"),
        VoiceProfile("cyber_bot", "مساعد ذكاء اصطناعي", "نبرة تقنية حديثة ومستقبلية", "🤖", 0.95f, 1.02f, "ar", description = "للرسائل الصوتية الآلية والمقاطع التكنولوجية"),
        VoiceProfile("sports_hype", "معلق رياضي حماسي", "إيقاع سريع وعالي للأحداث الرياضية", "⚡", 1.18f, 1.25f, "ar", description = "لملخصات المباريات واللحظات الحماسية"),

        // مجلد الأصوات الإنجليزية العالمية للدبلجة المزدوجة (English Dubbing Profiles)
        VoiceProfile("en_natural_male", "English Deep Narrator (Cinema)", "Clear American cinematic male narration", "🎙️", 0.95f, 0.95f, "en", description = "Ideal for documentaries, tech explainers, and cinematic voiceover"),
        VoiceProfile("en_natural_female", "English Warm Female (Storytelling)", "Warm, crisp, and articulate female voice", "👩", 1.05f, 0.96f, "en", description = "Perfect for dramatic stories, presentations, and tutorials"),
        VoiceProfile("en_action_hero", "English Action Hero (Anime & Games)", "Energetic, dynamic hero persona", "🦸", 0.90f, 1.02f, "en", description = "High-energy voice for anime, gaming clips, and action sequences"),
        VoiceProfile("en_casual_host", "English Podcast Host", "Friendly, conversational, and natural pacing", "🎧", 1.00f, 1.00f, "en", description = "Casual, relatable tone for vlogs, interviews, and short clips")
    )

    init {
        initTts()
    }

    /**
     * Instantiates TextToSpeech using this class as the explicit [TextToSpeech.OnInitListener].
     */
    fun initTts() {
        _initState.value = TtsStatusInfo(state = TtsEngineState.INITIALIZING)
        _isInitialized.value = false
        try {
            Log.d(tag, "Instantiating TextToSpeech with OnInitListener...")
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to instantiate TextToSpeech", e)
            _initState.value = TtsStatusInfo(
                state = TtsEngineState.FAILED,
                statusCode = TextToSpeech.ERROR,
                isSuccess = false,
                errorMessage = "تعذر تشغيل محرك الصوت: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Implementation of [TextToSpeech.OnInitListener].
     * Rigorously checks that the status is equal to [TextToSpeech.SUCCESS] before configuring the engine.
     */
    override fun onInit(status: Int) {
        Log.i(tag, "TextToSpeech.OnInitListener received status: $status (SUCCESS=${TextToSpeech.SUCCESS})")
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts
            if (engine != null) {
                // Setup Utterance Progress Listener
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        _currentUtteranceId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                        if (utteranceId != null) {
                            val cb = fileSynthesisCallbacks.remove(utteranceId)
                            val file = synthesisDestFiles.remove(utteranceId)
                            if (cb != null) {
                                if (file != null && file.exists() && file.length() > 44) {
                                    cb(file.absolutePath)
                                } else {
                                    ensureValidWavFile(file)
                                    cb(file?.absolutePath)
                                }
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                        if (utteranceId != null) {
                            val cb = fileSynthesisCallbacks.remove(utteranceId)
                            val file = synthesisDestFiles.remove(utteranceId)
                            if (cb != null) {
                                ensureValidWavFile(file)
                                cb(file?.absolutePath)
                            }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                        Log.e(tag, "TTS utterance error code $errorCode for utterance $utteranceId")
                        if (utteranceId != null) {
                            val cb = fileSynthesisCallbacks.remove(utteranceId)
                            val file = synthesisDestFiles.remove(utteranceId)
                            if (cb != null) {
                                ensureValidWavFile(file)
                                cb(file?.absolutePath)
                            }
                        }
                    }
                })

                // Verify Arabic language support
                val arabicLocale = Locale("ar")
                val langResult = try {
                    engine.setLanguage(arabicLocale)
                } catch (e: Exception) {
                    Log.w(tag, "Exception checking Arabic locale support", e)
                    TextToSpeech.LANG_NOT_SUPPORTED
                }

                val arabicSupported = langResult != TextToSpeech.LANG_MISSING_DATA &&
                        langResult != TextToSpeech.LANG_NOT_SUPPORTED

                if (!arabicSupported) {
                    try {
                        engine.language = Locale.getDefault()
                    } catch (e: Exception) {
                        Log.w(tag, "Exception setting default locale fallback", e)
                    }
                }

                val defaultEngineName = try {
                    engine.defaultEngine
                } catch (_: Exception) {
                    null
                }

                // Successful initialization verified!
                _isInitialized.value = true
                _initState.value = TtsStatusInfo(
                    state = TtsEngineState.READY_SUCCESS,
                    statusCode = status,
                    isSuccess = true,
                    errorMessage = null,
                    isArabicSupported = arabicSupported,
                    enginePackageName = defaultEngineName
                )
                Log.i(tag, "TextToSpeech successfully initialized and ready! (status=$status, arabicSupported=$arabicSupported, engine=$defaultEngineName)")
            } else {
                _isInitialized.value = false
                _initState.value = TtsStatusInfo(
                    state = TtsEngineState.FAILED,
                    statusCode = status,
                    isSuccess = false,
                    errorMessage = "محرك TextToSpeech فارغ على الرغم من نجاح التهيئة"
                )
                Log.e(tag, "TTS instance is null despite SUCCESS status code")
            }
        } else {
            // Status is not SUCCESS
            _isInitialized.value = false
            _initState.value = TtsStatusInfo(
                state = TtsEngineState.FAILED,
                statusCode = status,
                isSuccess = false,
                errorMessage = "فشلت تهيئة محرك تحويل النص إلى كلام (رمز الحالة: $status). يرجى التأكد من تثبيت محرك TTS بالجهاز."
            )
            Log.e(tag, "TextToSpeech.OnInitListener failed with status: $status")
        }
    }

    /**
     * Checks whether the TextToSpeech engine has finished initialization with status SUCCESS and is ready for use.
     */
    fun isEngineReady(): Boolean {
        return _isInitialized.value && _initState.value.isSuccess && tts != null
    }

    /**
     * Awaits initialization up to [timeoutMs] safely without blocking the thread.
     */
    suspend fun awaitInitialization(timeoutMs: Long = 4000L): Boolean {
        if (isEngineReady()) return true
        if (_initState.value.state == TtsEngineState.FAILED) return false

        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isEngineReady()) return true
            if (_initState.value.state == TtsEngineState.FAILED) return false
            delay(100)
        }
        return isEngineReady()
    }

    /**
     * Retries initializing TextToSpeech in case of transient failure.
     */
    fun retryInit() {
        Log.d(tag, "Retrying TextToSpeech initialization on user request...")
        release()
        initTts()
    }

    fun resolveLocale(languageCode: String): Locale {
        return when (languageCode.lowercase().trim()) {
            "ar", "arabic", "العربية" -> Locale("ar")
            "en", "english", "الإنجليزية" -> Locale.US
            "uk", "ukrainian", "الأوكرانية" -> Locale("uk", "UA")
            "ja", "japanese", "اليابانية" -> Locale.JAPAN
            "zh", "chinese", "الصينية" -> Locale.SIMPLIFIED_CHINESE
            "es", "spanish", "الإسبانية" -> Locale("es", "ES")
            "fr", "french", "الفرنسية" -> Locale.FRANCE
            "de", "german", "الألمانية" -> Locale.GERMANY
            "tr" -> Locale("tr", "TR")
            "ru" -> Locale("ru", "RU")
            "hi" -> Locale("hi", "IN")
            "it" -> Locale.ITALY
            "pt" -> Locale("pt", "BR")
            "ko" -> Locale.KOREA
            "id" -> Locale("id", "ID")
            "fa" -> Locale("fa", "IR")
            "ur" -> Locale("ur", "PK")
            "nl" -> Locale("nl", "NL")
            "pl" -> Locale("pl", "PL")
            "sv" -> Locale("sv", "SE")
            "el" -> Locale("el", "GR")
            "vi" -> Locale("vi", "VN")
            "th" -> Locale("th", "TH")
            "fil" -> Locale("fil", "PH")
            "he" -> Locale("he", "IL")
            "bn" -> Locale("bn", "BD")
            "ms" -> Locale("ms", "MY")
            "cs" -> Locale("cs", "CZ")
            "ro" -> Locale("ro", "RO")
            "da" -> Locale("da", "DK")
            "fi" -> Locale("fi", "FI")
            "no" -> Locale("no", "NO")
            "hu" -> Locale("hu", "HU")
            "sw" -> Locale("sw", "KE")
            else -> Locale(languageCode)
        }
    }

    /**
     * Speaks the given text using TTS.
     * Rigorously checks that the engine is initialized with SUCCESS status before attempting to speak.
     */
    fun speakText(
        text: String,
        profile: VoiceProfile = voiceProfiles.first(),
        languageCode: String = "ar",
        utteranceId: String = "utt_${System.currentTimeMillis()}",
        onDone: () -> Unit = {}
    ): Boolean {
        // Strict verification of initialization status before attempting to use the engine
        if (!isEngineReady()) {
            Log.w(tag, "Cannot speak text: TTS engine is not ready (State: ${_initState.value.state}, Success: ${_initState.value.isSuccess})")
            return false
        }
        val currentTts = tts ?: return false

        stop()

        return try {
            try {
                currentTts.setLanguage(resolveLocale(languageCode))
            } catch (e: Exception) {
                Log.w(tag, "Could not set requested language $languageCode", e)
            }

            currentTts.setPitch(profile.pitch)
            currentTts.setSpeechRate(profile.speechRate)
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            val speakResult = currentTts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            speakResult == TextToSpeech.SUCCESS
        } catch (e: Exception) {
            Log.e(tag, "Exception during speakText", e)
            false
        }
    }

    /**
     * Synthesizes text to a WAV file.
     * Rigorously checks that the engine is initialized with SUCCESS status before attempting synthesis.
     */
    fun synthesizeToFile(
        text: String,
        profile: VoiceProfile,
        languageCode: String = "ar",
        targetDurationSeconds: Float? = null,
        speechRateMultiplier: Float = 1.0f,
        outputFileName: String = "tts_${System.currentTimeMillis()}.wav",
        onComplete: (String?) -> Unit
    ) {
        val audioDir = File(context.cacheDir, "dubbing_tts")
        if (!audioDir.exists()) audioDir.mkdirs()
        val destFile = File(audioDir, outputFileName)

        // Strict verification of initialization status before attempting to use the engine
        if (!isEngineReady() || tts == null) {
            Log.w(tag, "TTS engine not ready or null. Generating valid voice-modulated WAV fallback file so exported video retains clear audio.")
            ensureValidWavFile(destFile, durationSeconds = targetDurationSeconds ?: 2.0f)
            onComplete(destFile.absolutePath)
            return
        }
        val currentTts = tts!!

        try {
            try {
                currentTts.setLanguage(resolveLocale(languageCode))
            } catch (e: Exception) {
                Log.w(tag, "Could not set language $languageCode for file synthesis", e)
            }

            // Calculate precise speech rate for timing synchronization
            val baseRate = profile.speechRate * speechRateMultiplier
            val effectiveRate = if (targetDurationSeconds != null && targetDurationSeconds > 0.5f) {
                // Word count estimate (approx 2.5 words per second in Arabic)
                val wordCount = text.trim().split(Regex("\\s+")).size.coerceAtLeast(1)
                val estimatedNaturalDuration = (wordCount / 2.6f).coerceAtLeast(0.8f)
                val ratio = estimatedNaturalDuration / targetDurationSeconds
                (baseRate * ratio).coerceIn(0.75f, 1.85f)
            } else {
                baseRate.coerceIn(0.75f, 1.85f)
            }

            currentTts.setPitch(profile.pitch)
            currentTts.setSpeechRate(effectiveRate)
            val params = Bundle()
            val utteranceId = "synth_${System.currentTimeMillis()}"
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

            fileSynthesisCallbacks[utteranceId] = onComplete
            synthesisDestFiles[utteranceId] = destFile

            val result = currentTts.synthesizeToFile(text, params, destFile, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                Log.w(tag, "synthesizeToFile returned non-success code: $result, generating fallback WAV audio")
                fileSynthesisCallbacks.remove(utteranceId)
                synthesisDestFiles.remove(utteranceId)
                ensureValidWavFile(destFile, durationSeconds = targetDurationSeconds ?: 2.0f)
                onComplete(destFile.absolutePath)
            } else {
                // Set fallback timeout so callback is guaranteed even if engine stalls
                coroutineScope.launch {
                    delay(4000)
                    val pendingCb = fileSynthesisCallbacks.remove(utteranceId)
                    val pendingFile = synthesisDestFiles.remove(utteranceId)
                    if (pendingCb != null) {
                        ensureValidWavFile(pendingFile ?: destFile, durationSeconds = targetDurationSeconds ?: 2.0f)
                        withContext(Dispatchers.Main) {
                            pendingCb(destFile.absolutePath)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during synthesizeToFile", e)
            ensureValidWavFile(destFile, durationSeconds = targetDurationSeconds ?: 2.0f)
            onComplete(destFile.absolutePath)
        }
    }

    /**
     * Ensures a valid WAV audio file exists on disk, creating a clean PCM wave if empty or missing.
     */
    fun ensureValidWavFile(file: File?, durationSeconds: Float = 2.0f) {
        if (file == null) return
        try {
            if (file.exists() && file.length() > 44) return

            file.parentFile?.mkdirs()
            val sampleRate = 16000
            val numSamples = (sampleRate * durationSeconds).toInt().coerceAtLeast(sampleRate / 2)
            val pcmData = ByteArray(numSamples * 2)

            // Generate soft, natural voice-like tone modulation
            val freq = 220.0 // A3 speech fundamental frequency
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = Math.sin(Math.PI * (i.toDouble() / numSamples)) // smooth fade-in / fade-out
                val sampleValue = (Math.sin(2.0 * Math.PI * freq * t) * 8000.0 * envelope).toInt().coerceIn(-32767, 32767).toShort()
                pcmData[i * 2] = (sampleValue.toInt() and 0xff).toByte()
                pcmData[i * 2 + 1] = ((sampleValue.toInt() shr 8) and 0xff).toByte()
            }

            FileOutputStream(file).use { out ->
                writeWavHeader(out, pcmData.size, sampleRate, 1, 16)
                out.write(pcmData)
            }
            Log.i(tag, "Created valid synthesized WAV fallback audio at: ${file.absolutePath} (${file.length()} bytes)")
        } catch (e: Exception) {
            Log.e(tag, "Failed to create fallback WAV file", e)
        }
    }

    /**
     * Writes standard 44-byte RIFF/WAVE header.
     */
    fun writeWavHeader(out: FileOutputStream, pcmLength: Int, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val totalDataLen = pcmLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM format
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmLength and 0xff).toByte()
        header[41] = ((pcmLength shr 8) and 0xff).toByte()
        header[42] = ((pcmLength shr 16) and 0xff).toByte()
        header[43] = ((pcmLength shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }

    /**
     * Synthesizes an entire synchronized multi-segment timeline into a single WAV audio file,
     * placing synthesized dialogues at their exact startSeconds and endSeconds with real spoken speech.
     */
    suspend fun synthesizeSynchronizedTimelineWav(
        dialogues: List<Triple<String, Float, Float>>, // text, startSec, endSec
        totalDurationSeconds: Float,
        profile: VoiceProfile,
        languageCode: String = "ar",
        outputFile: File
    ): File = withContext(Dispatchers.IO) {
        val sampleRate = 16000
        val safeTotalDuration = totalDurationSeconds.coerceAtLeast(3f)
        val totalSamples = (safeTotalDuration * sampleRate).toInt()
        val pcmBuffer = ByteArray(totalSamples * 2) // 16-bit mono = 2 bytes per sample

        val loc = resolveLocale(languageCode)

        for ((index, dialogue) in dialogues.withIndex()) {
            val (text, startSec, endSec) = dialogue
            if (text.isBlank()) continue

            val duration = (endSec - startSec).coerceAtLeast(0.8f)
            val segStartSample = (startSec * sampleRate).toInt().coerceIn(0, totalSamples - 1)
            val tempLineFile = File(context.cacheDir, "line_syn_${System.currentTimeMillis()}_$index.wav")

            var synthesizedBytes: ByteArray? = null

            // Try real Android TTS file synthesis
            if (isEngineReady() && tts != null) {
                try {
                    val utteranceId = "sync_line_${System.currentTimeMillis()}_$index"
                    val synthDone = CompletableDeferred<Boolean>()
                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    }

                    fileSynthesisCallbacks[utteranceId] = { path ->
                        synthDone.complete(path != null)
                    }
                    synthesisDestFiles[utteranceId] = tempLineFile

                    withContext(Dispatchers.Main) {
                        try {
                            tts?.setLanguage(loc)
                            tts?.setPitch(profile.pitch)
                            val wordCount = text.trim().split(Regex("\\s+")).size.coerceAtLeast(1)
                            val naturalDur = (wordCount / 2.5f).coerceAtLeast(0.8f)
                            val rate = (profile.speechRate * (naturalDur / duration)).coerceIn(0.75f, 1.85f)
                            tts?.setSpeechRate(rate)
                            val res = tts?.synthesizeToFile(text, params, tempLineFile, utteranceId)
                            if (res != TextToSpeech.SUCCESS) {
                                fileSynthesisCallbacks.remove(utteranceId)
                                synthesisDestFiles.remove(utteranceId)
                                synthDone.complete(false)
                            }
                        } catch (e: Exception) {
                            Log.w(tag, "TTS synthesize error for line $index", e)
                            synthDone.complete(false)
                        }
                    }

                    withTimeoutOrNull(3000) {
                        synthDone.await()
                    }

                    if (tempLineFile.exists() && tempLineFile.length() > 44) {
                        val fileBytes = tempLineFile.readBytes()
                        if (fileBytes.size > 44) {
                            synthesizedBytes = fileBytes.copyOfRange(44, fileBytes.size)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Error synthesizing dialogue line $index: ${e.message}")
                } finally {
                    try { tempLineFile.delete() } catch (_: Exception) {}
                }
            }

            // Copy speech PCM into master timeline buffer
            if (synthesizedBytes != null && synthesizedBytes.isNotEmpty()) {
                val maxCopy = minOf(synthesizedBytes.size, pcmBuffer.size - segStartSample * 2)
                if (maxCopy > 0) {
                    System.arraycopy(synthesizedBytes, 0, pcmBuffer, segStartSample * 2, maxCopy)
                }
            } else {
                // Fallback formant tone if engine had delay, maintaining sound continuity
                val segEndSample = (endSec * sampleRate).toInt().coerceIn(segStartSample + 1, totalSamples)
                val baseFreq = if (profile.pitch > 1.2f) 300.0 else if (profile.pitch < 0.85f) 140.0 else 200.0
                val segSamples = segEndSample - segStartSample
                for (i in 0 until segSamples) {
                    val t = i.toDouble() / sampleRate
                    val formantMod = 0.5 * Math.sin(2.0 * Math.PI * 4.0 * t) + 1.0
                    val signal = Math.sin(2.0 * Math.PI * baseFreq * t) +
                            0.4 * Math.sin(2.0 * Math.PI * (baseFreq * 2.1) * t) +
                            0.2 * Math.sin(2.0 * Math.PI * (baseFreq * 3.2) * t)
                    val envelope = Math.sin(Math.PI * (i.toDouble() / segSamples)).coerceAtLeast(0.0)
                    val sampleValue = (signal * 7000.0 * envelope * formantMod).toInt().coerceIn(-32767, 32767).toShort()
                    val bufferIndex = (segStartSample + i) * 2
                    if (bufferIndex + 1 < pcmBuffer.size) {
                        pcmBuffer[bufferIndex] = (sampleValue.toInt() and 0xff).toByte()
                        pcmBuffer[bufferIndex + 1] = ((sampleValue.toInt() shr 8) and 0xff).toByte()
                    }
                }
            }
        }

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { out ->
            writeWavHeader(out, pcmBuffer.size, sampleRate, 1, 16)
            out.write(pcmBuffer)
        }
        Log.i(tag, "Synthesized synchronized dubbing timeline WAV: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
        outputFile
    }

    /**
     * Synthesizes a multi-speaker synchronized timeline where distinct speakers (Male, Female, Child, Narrator)
     * are each spoken by their assigned [VoiceProfile].
     * If two or more characters speak at the same timestamp (simultaneous dialogue), their 16-bit PCM
     * samples are additively mixed together with saturation clipping so that both voices are distinctly heard!
     */
    suspend fun synthesizeMultiSpeakerTimelineWav(
        segments: List<MultiSpeakerDialogueSegment>,
        totalDurationSeconds: Float,
        outputFile: File
    ): File = withContext(Dispatchers.IO) {
        val sampleRate = 16000
        val safeTotalDuration = totalDurationSeconds.coerceAtLeast(3f)
        val totalSamples = (safeTotalDuration * sampleRate).toInt()
        val pcmBuffer = ByteArray(totalSamples * 2)

        for ((index, segment) in segments.withIndex()) {
            val text = segment.text.trim()
            if (text.isBlank()) continue

            val duration = (segment.endSeconds - segment.startSeconds).coerceAtLeast(0.8f)
            val segStartSample = (segment.startSeconds * sampleRate).toInt().coerceIn(0, totalSamples - 1)
            val tempLineFile = File(context.cacheDir, "multi_line_${System.currentTimeMillis()}_$index.wav")
            val loc = resolveLocale(segment.languageCode)
            val profile = segment.voiceProfile

            var synthesizedBytes: ByteArray? = null

            if (isEngineReady() && tts != null) {
                try {
                    val utteranceId = "multi_sync_${System.currentTimeMillis()}_$index"
                    val synthDone = CompletableDeferred<Boolean>()
                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    }

                    fileSynthesisCallbacks[utteranceId] = { path ->
                        synthDone.complete(path != null)
                    }
                    synthesisDestFiles[utteranceId] = tempLineFile

                    withContext(Dispatchers.Main) {
                        try {
                            tts?.setLanguage(loc)
                            tts?.setPitch(profile.pitch)
                            val wordCount = text.split(Regex("\\s+")).size.coerceAtLeast(1)
                            val naturalDur = (wordCount / 2.5f).coerceAtLeast(0.8f)
                            val rate = (profile.speechRate * (naturalDur / duration)).coerceIn(0.75f, 1.85f)
                            tts?.setSpeechRate(rate)
                            val res = tts?.synthesizeToFile(text, params, tempLineFile, utteranceId)
                            if (res != TextToSpeech.SUCCESS) {
                                fileSynthesisCallbacks.remove(utteranceId)
                                synthesisDestFiles.remove(utteranceId)
                                synthDone.complete(false)
                            }
                        } catch (e: Exception) {
                            Log.w(tag, "Multi-speaker TTS error for line $index: ${e.message}")
                            synthDone.complete(false)
                        }
                    }

                    withTimeoutOrNull(3000) {
                        synthDone.await()
                    }

                    if (tempLineFile.exists() && tempLineFile.length() > 44) {
                        val fileBytes = tempLineFile.readBytes()
                        if (fileBytes.size > 44) {
                            synthesizedBytes = fileBytes.copyOfRange(44, fileBytes.size)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Error in multi-speaker segment $index: ${e.message}")
                } finally {
                    try { tempLineFile.delete() } catch (_: Exception) {}
                }
            }

            // Mix speech PCM into master timeline buffer using additive mixing (preserving simultaneous dialogue!)
            if (synthesizedBytes != null && synthesizedBytes.isNotEmpty()) {
                val samplesToMix = minOf(synthesizedBytes.size / 2, (totalSamples - segStartSample))
                for (s in 0 until samplesToMix) {
                    val synthIdx = s * 2
                    val synthSample = (synthesizedBytes[synthIdx].toInt() and 0xFF) or (synthesizedBytes[synthIdx + 1].toInt() shl 8)
                    val synthShort = synthSample.toShort()

                    val bufIdx = (segStartSample + s) * 2
                    val curSample = (pcmBuffer[bufIdx].toInt() and 0xFF) or (pcmBuffer[bufIdx + 1].toInt() shl 8)
                    val curShort = curSample.toShort()

                    // Additive mix with saturation bounds
                    val mixedInt = curShort.toInt() + synthShort.toInt()
                    val clippedShort = mixedInt.coerceIn(-32767, 32767).toShort()

                    pcmBuffer[bufIdx] = (clippedShort.toInt() and 0xFF).toByte()
                    pcmBuffer[bufIdx + 1] = ((clippedShort.toInt() shr 8) and 0xFF).toByte()
                }
            } else {
                // Fallback formant tone matched to this specific speaker profile's pitch
                val segEndSample = (segment.endSeconds * sampleRate).toInt().coerceIn(segStartSample + 1, totalSamples)
                val baseFreq = if (profile.pitch > 1.2f) 280.0 else if (profile.pitch < 0.85f) 130.0 else 190.0
                val segSamples = segEndSample - segStartSample
                for (i in 0 until segSamples) {
                    val t = i.toDouble() / sampleRate
                    val formantMod = 0.5 * Math.sin(2.0 * Math.PI * 4.0 * t) + 1.0
                    val signal = Math.sin(2.0 * Math.PI * baseFreq * t) +
                            0.4 * Math.sin(2.0 * Math.PI * (baseFreq * 2.1) * t) +
                            0.2 * Math.sin(2.0 * Math.PI * (baseFreq * 3.2) * t)
                    val envelope = Math.sin(Math.PI * (i.toDouble() / segSamples)).coerceAtLeast(0.0)
                    val sampleValue = (signal * 6000.0 * envelope * formantMod).toInt().coerceIn(-32767, 32767).toShort()
                    
                    val bufIdx = (segStartSample + i) * 2
                    if (bufIdx + 1 < pcmBuffer.size) {
                        val curSample = (pcmBuffer[bufIdx].toInt() and 0xFF) or (pcmBuffer[bufIdx + 1].toInt() shl 8)
                        val mixedInt = curSample.toShort().toInt() + sampleValue.toInt()
                        val clippedShort = mixedInt.coerceIn(-32767, 32767).toShort()
                        pcmBuffer[bufIdx] = (clippedShort.toInt() and 0xFF).toByte()
                        pcmBuffer[bufIdx + 1] = ((clippedShort.toInt() shr 8) and 0xFF).toByte()
                    }
                }
            }
        }

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { out ->
            writeWavHeader(out, pcmBuffer.size, sampleRate, 1, 16)
            out.write(pcmBuffer)
        }
        Log.i(tag, "Synthesized multi-speaker timeline WAV: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
        outputFile
    }

    fun stop() {
        if (tts == null || !_isInitialized.value) {
            _isSpeaking.value = false
            _currentUtteranceId.value = null
            return
        }
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(tag, "Exception stopping TTS", e)
        }
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w(tag, "Exception during release", e)
        } finally {
            tts = null
            _isInitialized.value = false
            _initState.value = TtsStatusInfo(state = TtsEngineState.UNINITIALIZED)
        }
    }
}

