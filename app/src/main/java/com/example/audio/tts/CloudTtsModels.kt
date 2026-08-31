package com.example.audio.tts

enum class CloudTtsProvider(
    val titleArabic: String,
    val descriptionArabic: String,
    val iconEmoji: String
) {
    GOOGLE_CLOUD_TTS(
        titleArabic = "Google Cloud Text-to-Speech",
        descriptionArabic = "أصوات WaveNet و Neural2 فصيحة وطبيعية جداً بدقة عالية",
        iconEmoji = "🌐"
    ),
    ELEVEN_LABS(
        titleArabic = "ElevenLabs AI Voice",
        descriptionArabic = "توليد أصوات بشرية فائقة الواقعية بنبرات سينمائية وعاطفية",
        iconEmoji = "🎙️"
    ),
    DEVICE_TTS(
        titleArabic = "محرك الجهاز المحلي (On-Device)",
        descriptionArabic = "يعمل بدون إنترنت وبسرعة فورية مباشرة على المعالج",
        iconEmoji = "📱"
    )
}

data class CloudVoiceInfo(
    val id: String,
    val name: String,
    val gender: String, // "MALE", "FEMALE"
    val provider: CloudTtsProvider,
    val descriptionArabic: String,
    val languageCode: String = "ar-XA",
    val sampleTextArabic: String = "مرحباً بكم في عالم الدبلجة الصوتية الذكية بالذكاء الاصطناعي."
)

data class CloudTtsConfig(
    val provider: CloudTtsProvider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
    val elevenLabsApiKey: String = "",
    val elevenLabsVoiceId: String = "21m00Tcm4TlvDq8ikWAM", // Rachel
    val elevenLabsModelId: String = "eleven_multilingual_v2",
    val googleCloudApiKey: String = "",
    val googleCloudVoiceName: String = "ar-XA-Wavenet-B",
    val googleCloudLanguageCode: String = "ar-XA",
    val speakingRate: Float = 1.0f,
    val pitch: Float = 0.0f,
    val stability: Float = 0.50f,
    val similarityBoost: Float = 0.75f,
    val isEnabled: Boolean = true
)

object CloudVoiceCatalog {

    val elevenLabsVoices = listOf(
        CloudVoiceInfo(
            id = "21m00Tcm4TlvDq8ikWAM",
            name = "Rachel (راشيل)",
            gender = "FEMALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "صوت نسائي هادئ وواضح مع نطق عربي ممتاز في نموذج Multilingual v2"
        ),
        CloudVoiceInfo(
            id = "ErXwobaYiN019PkySvjV",
            name = "Antoni (أنطوني)",
            gender = "MALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "صوت رجالي متزن ورصين، رائع للشروحات والوثائقيات"
        ),
        CloudVoiceInfo(
            id = "VR6AewLTigWG4xSOukaG",
            name = "Arnold (أرنولد)",
            gender = "MALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "صوت وثائقي عميق وفخم ذو نبرة وقورة"
        ),
        CloudVoiceInfo(
            id = "pNInz6obpgDQGcFmaJgB",
            name = "Adam (آدم)",
            gender = "MALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "نبرة حوارية دافئة وطبيعية جداً للمحادثات"
        ),
        CloudVoiceInfo(
            id = "EXAVITQu4vr4xnSDxMaL",
            name = "Bella (بيلا)",
            gender = "FEMALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "صوت شاعري وناعم ومفعم بالمشاعر"
        ),
        CloudVoiceInfo(
            id = "TxGEqnHWrfWFTfGW9XjX",
            name = "Josh (جوش)",
            gender = "MALE",
            provider = CloudTtsProvider.ELEVEN_LABS,
            descriptionArabic = "صوت شبابي وحيوي سريع الاستجابة"
        )
    )

    val googleCloudVoices = listOf(
        CloudVoiceInfo(
            id = "ar-XA-Wavenet-B",
            name = "WaveNet Male 1 (صوت وثائقي فخم)",
            gender = "MALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "صوت ذكوري فصيح بنقاء استوديو WaveNet للوثائقيات",
            languageCode = "ar-XA"
        ),
        CloudVoiceInfo(
            id = "ar-XA-Wavenet-A",
            name = "WaveNet Female 1 (صوت نسائي دافئ)",
            gender = "FEMALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "صوت نسائي عربي فصيح بنبرة واضحة ولطيفة",
            languageCode = "ar-XA"
        ),
        CloudVoiceInfo(
            id = "ar-XA-Wavenet-C",
            name = "WaveNet Male 2 (صوت إخباري متزن)",
            gender = "MALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "نبرة إذاعية رصينة للأخبار والشروحات",
            languageCode = "ar-XA"
        ),
        CloudVoiceInfo(
            id = "ar-XA-Wavenet-D",
            name = "WaveNet Female 2 (صوت سردي معبر)",
            gender = "FEMALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "نبرة ناعمة ومعبرة للقصص والروايات",
            languageCode = "ar-XA"
        ),
        CloudVoiceInfo(
            id = "ar-XA-Standard-B",
            name = "Standard Arabic Male (قياسي)",
            gender = "MALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "صوت رجالي كلاسيكي قياسي",
            languageCode = "ar-XA"
        ),
        CloudVoiceInfo(
            id = "ar-XA-Standard-A",
            name = "Standard Arabic Female (قياسي)",
            gender = "FEMALE",
            provider = CloudTtsProvider.GOOGLE_CLOUD_TTS,
            descriptionArabic = "صوت أنثوي كلاسيكي قياسي",
            languageCode = "ar-XA"
        )
    )

    fun getVoicesForProvider(provider: CloudTtsProvider): List<CloudVoiceInfo> {
        return when (provider) {
            CloudTtsProvider.ELEVEN_LABS -> elevenLabsVoices
            CloudTtsProvider.GOOGLE_CLOUD_TTS -> googleCloudVoices
            CloudTtsProvider.DEVICE_TTS -> emptyList()
        }
    }
}
