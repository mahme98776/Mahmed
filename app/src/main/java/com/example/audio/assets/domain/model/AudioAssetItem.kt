package com.example.audio.assets.domain.model

import java.io.File

/**
 * التصنيف البرمجي لملفات الصوت في مجلد الأصول (assets).
 */
enum class AudioAssetCategory(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val iconEmoji: String
) {
    SOUNDTRACKS("soundtracks", "الموسيقى التصويرية", "خلفيات موسيقية وسينمائية ملحمية للمشاهد", "🎵"),
    VOICEOVERS("voiceovers", "التعليق الصوتي الجاهز", "نماذج صوتية مسجلة مسبقاً للدبلجة السريعة", "🎙️"),
    EFFECTS("effects", "المؤثرات الصوتية (SFX)", "انتقالات، أجراس، ضربات وتأثيرات حركية", "✨"),
    SAMPLES("samples", "عينات حوارية للتدريب", "حوارات تمثيلية لاختبار مزامنة الشفاه والنص", "🎬"),
    CUSTOM("custom", "ملفات مخصصة", "ملفات صوتية إضافية تمت إضافتها", "📁")
}

/**
 * كائن بيانات يمثل ملف صوتي من مجلد assets مع خصائصه.
 */
data class AudioAssetItem(
    val assetPath: String,              // e.g. "audio/soundtracks/ambient_cinematic.wav"
    val fileName: String,               // e.g. "ambient_cinematic.wav"
    val titleArabic: String,            // e.g. "خلفية سينمائية هادئة"
    val category: AudioAssetCategory,
    val sizeBytes: Long,
    val durationMs: Long = 0L,
    val format: String = "WAV",
    val isCached: Boolean = false,
    val cachedFile: File? = null
)
