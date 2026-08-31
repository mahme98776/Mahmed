package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

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

class TextToSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    val voiceProfiles = listOf(
        VoiceProfile("natural_arabic_male", "قارئ فصيح طبيعي (فخامة ووضوح)", "نطق عربي متزن وإلقاء طبيعي واقعي جداً", "🎙️", 0.92f, 0.90f, description = "مثالي للمقاطع الوثائقية والبودكاست والنصوص الأدبية"),
        VoiceProfile("natural_arabic_female", "قارئة فصيحة ناعمة (طبيعية ودافئة)", "مخارج حروف عربية دقيقة ونبرة هادئة", "👩", 1.05f, 0.93f, description = "مناسب للقصص، الإعلانات، والشروحات التعليمية"),
        VoiceProfile("spacetoon_hero_male", "بطل سبيستون الفصيح (طابع زياد وطارق)", "نبرة بطولية فصحى حماسية وقوية للأنمي والأكشن", "🦸", 0.90f, 0.95f, description = "نبرة سبيستونية كلاسيكية ملحمية لأبطال المستقبل"),
        VoiceProfile("spacetoon_heroine_female", "بطلة سبيستون الدافئة (طابع رشا وأمل)", "نبرة أنثوية عربية عذبة ومؤثرة تفيض بالمشاعر", "🌸", 1.10f, 0.94f, description = "للشخصيات الكرتونية الرئيسية والفتيات الشجاعات"),
        VoiceProfile("spacetoon_anime_narrator", "راوي سبيستون الأسطوري", "سرد فصيح مهيب لمقدمات الكرتون والأنمي", "🌟", 0.82f, 0.88f, description = "للمقدمات ونهايات الحلقات والحكم الملهمة"),
        VoiceProfile("male_narrator", "صوت جهوري إذاعي", "نبرة عميقة وقوية ذات طابع وثائقي", "📻", 0.78f, 0.86f, description = "للمقدمات الحماسية والتعليق الصوتي الإذاعي"),
        VoiceProfile("female_soft", "صوت سردي عاطفي", "نبرة هادئة وملهمة للمشاهد الدرامية", "✨", 1.12f, 0.92f, description = "للحوارات الوجدانية والخواطر"),
        VoiceProfile("cartoon_hero", "شخصية كرتونية مرحة", "نبرة سريعة ونشطة لرسوم الأطفال والأنمي", "🧒", 1.55f, 1.15f, description = "لأفلام الكرتون ودبلجة المشاهد الطريفة"),
        VoiceProfile("dramatic_villain", "شخصية سينمائية غامضة", "نبرة خشنة ومثيرة لأفلام الإثارة والأكشن", "🦹", 0.68f, 0.82f, description = "للمشاهد السينمائية وشخصيات الأشرار"),
        VoiceProfile("cyber_bot", "مساعد ذكاء اصطناعي", "نبرة تقنية حديثة ومستقبلية", "🤖", 0.95f, 1.02f, description = "للرسائل الصوتية الآلية والمقاطع التكنولوجية"),
        VoiceProfile("sports_hype", "معلق رياضي حماسي", "إيقاع سريع وعالي للأحداث الرياضية", "⚡", 1.18f, 1.25f, description = "لملخصات المباريات واللحظات الحماسية")
    )

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try setting Arabic locale, fallback to default if unavailable
                val arabicLocale = Locale("ar")
                val result = tts?.setLanguage(arabicLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
                _isInitialized.value = true
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _currentUtteranceId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentUtteranceId.value = null
            }
        })
    }

    fun resolveLocale(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            "ar" -> Locale("ar")
            "en" -> Locale.US
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            "de" -> Locale.GERMANY
            "tr" -> Locale("tr", "TR")
            "ru" -> Locale("ru", "RU")
            "hi" -> Locale("hi", "IN")
            "ja" -> Locale.JAPAN
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "it" -> Locale.ITALY
            "pt" -> Locale("pt", "BR")
            "ko" -> Locale.KOREA
            "id" -> Locale("id", "ID")
            "fa" -> Locale("fa", "IR")
            "ur" -> Locale("ur", "PK")
            else -> Locale(languageCode)
        }
    }

    fun speakText(
        text: String,
        profile: VoiceProfile = voiceProfiles.first(),
        languageCode: String = "ar",
        utteranceId: String = "utt_${System.currentTimeMillis()}",
        onDone: () -> Unit = {}
    ) {
        if (!_isInitialized.value) return
        stop()

        tts?.apply {
            try {
                setLanguage(resolveLocale(languageCode))
            } catch (_: Exception) {}

            setPitch(profile.pitch)
            setSpeechRate(profile.speechRate)
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun synthesizeToFile(
        text: String,
        profile: VoiceProfile,
        languageCode: String = "ar",
        targetDurationSeconds: Float? = null,
        speechRateMultiplier: Float = 1.0f,
        outputFileName: String = "tts_${System.currentTimeMillis()}.wav",
        onComplete: (String?) -> Unit
    ) {
        if (!_isInitialized.value) {
            onComplete(null)
            return
        }

        val audioDir = File(context.cacheDir, "dubbing_tts")
        if (!audioDir.exists()) audioDir.mkdirs()
        val destFile = File(audioDir, outputFileName)

        tts?.apply {
            try {
                setLanguage(resolveLocale(languageCode))
            } catch (_: Exception) {}

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

            setPitch(profile.pitch)
            setSpeechRate(effectiveRate)
            val params = Bundle()
            val utteranceId = "synth_${System.currentTimeMillis()}"
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

            val result = synthesizeToFile(text, params, destFile, utteranceId)
            if (result == TextToSpeech.SUCCESS) {
                onComplete(destFile.absolutePath)
            } else {
                onComplete(null)
            }
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
