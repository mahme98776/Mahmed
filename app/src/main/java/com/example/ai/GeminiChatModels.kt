package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Models and data structures for Gemini, Veo, Lyria, and Grounding API operations.
 */

enum class GeminiChatModel(val modelId: String, val titleArabic: String, val descriptionArabic: String, val badge: String) {
    FLASH_3_5("gemini-3.5-flash", "Gemini 3.5 Flash", "المهام العامة، السرعة، والتحليل الذكي المتزن", "⚡ عام وسريع"),
    PRO_3_1("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "المهام المعقدة، التفكير العميق، وكتابة السيناريوهات الدرامية", "🧠 مهام معقدة"),
    FLASH_LITE_3_1("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite", "الاستجابة فائقة السرعة والمهام الفورية الخفيفة", "🚀 فائق السرعة")
}

enum class SystemInstructionRole(val titleArabic: String, val prompt: String, val emoji: String) {
    DUBBING_DIRECTOR(
        "مخرج دبلجة وتمثيل صوتي",
        "أنت مخرج دبلجة محترف واستشاري أداء صوتي في تطبيق VoiceMaster Pro. تساعد المستخدم في اختيار النبرات، مواءمة السيناريو، ضبط مخارج الحروف، ومطابقة حركة الشفاه (Lip-sync). أجب بلغة عربية فصيحة وأسلوب مشجع.",
        "🎬"
    ),
    SCREENWRITER(
        "كاتب سيناريو وحوارات سينمائية",
        "أنت مؤلف وكاتب سيناريوهات متخصص في دبلجة الأفلام ومسلسلات الأنمي. تصيغ الحوارات بدقة وجمالية لتناسب التوقيتات بالثواني، وتراعي الإيقاع الدرامي واللهجة المطلوبة.",
        "✍️"
    ),
    SOUND_ENGINEER(
        "مهندس صوت ومكساج محترف",
        "أنت مهندس صوت خبير في استوديو فويس ماستر برو. تقدم نصائح عملية في عزل الضوضاء، تسوية الترددات (EQ)، ضغط الصوت (Compression)، ومكساج المؤثرات والموسيقى الخلفية (Ducking).",
        "🎛️"
    ),
    GENERAL_CREATIVE(
        "مساعد ذكاء اصطناعي شامل",
        "أنت مساعد الذكاء الاصطناعي الذكي في تطبيق VoiceMaster Pro. تجيب عن استفسارات المستخدم بدقة، وتبحث في الويب والخرائط عند طلب معلومات حديثة.",
        "🤖"
    )
}

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val citations: List<String> = emptyList(),
    val groundedType: GroundingType = GroundingType.NONE
)

enum class GroundingType {
    NONE,
    SEARCH,
    MAPS
}

enum class VeoAspectRatio(val ratioStr: String, val labelArabic: String, val icon: String) {
    LANDSCAPE_16_9("16:9", "أفقي سينمائي (16:9)", "🖥️"),
    PORTRAIT_9_16("9:16", "عمودي ريلز وتيك توك (9:16)", "📱")
}

data class GeneratedVideoItem(
    val id: String = System.currentTimeMillis().toString(),
    val prompt: String,
    val videoUri: String,
    val localFilePath: String? = null,
    val sourceImageBitmap: Bitmap? = null,
    val aspectRatio: VeoAspectRatio = VeoAspectRatio.LANDSCAPE_16_9,
    val modelName: String = "veo-3.1-fast-generate-preview",
    val timestamp: Long = System.currentTimeMillis()
)

enum class LyriaMusicModel(val modelId: String, val labelArabic: String, val maxDurationSec: Int) {
    CLIP_PREVIEW("lyria-3-clip-preview", "مقطع صوتي سريع (حتى 30 ثانية)", 30),
    PRO_PREVIEW("lyria-3-pro-preview", "مقطوعة موسيقية كاملة (Pro)", 120)
}

data class GeneratedMusicItem(
    val id: String = System.currentTimeMillis().toString(),
    val prompt: String,
    val genre: String,
    val audioPath: String,
    val durationSeconds: Int,
    val modelName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeneratedImageItem(
    val id: String = System.currentTimeMillis().toString(),
    val prompt: String,
    val bitmap: Bitmap,
    val isEdited: Boolean = false,
    val originalBitmap: Bitmap? = null,
    val modelName: String = "gemini-3.1-flash-image-preview",
    val timestamp: Long = System.currentTimeMillis()
)

fun Bitmap.toBase64Jpeg(quality: Int = 85): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

fun Bitmap.toBase64Png(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
