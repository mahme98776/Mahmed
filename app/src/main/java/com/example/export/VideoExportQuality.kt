package com.example.export

import java.util.Locale

/**
 * Video resolution presets with width, height, standard bitrate, and display details.
 */
enum class ExportResolution(
    val labelArabic: String,
    val badge: String,
    val width: Int,
    val height: Int,
    val descriptionArabic: String,
    val baseBitrateBps: Int
) {
    UHD_4K(
        labelArabic = "4K فائقة الوضوح",
        badge = "2160p UHD",
        width = 3840,
        height = 2160,
        descriptionArabic = "أعلى جودة سينمائية فائقة الوضوح والتفاصيل للشاشات الكبيرة",
        baseBitrateBps = 16_000_000
    ),
    FHD_1080P(
        labelArabic = "Full HD عالية الدقة",
        badge = "1080p FHD",
        width = 1920,
        height = 1080,
        descriptionArabic = "الدقة المثالية والقياسية لليوتيوب والتلفزيون ومواقع التواصل",
        baseBitrateBps = 8_000_000
    ),
    HD_720P(
        labelArabic = "HD القياسية السريعة",
        badge = "720p HD",
        width = 1280,
        height = 720,
        descriptionArabic = "توازن ممتاز بين الجودة وسرعة المعالجة (الخيار الافتراضي الموصى به)",
        baseBitrateBps = 3_500_000
    ),
    SD_480P(
        labelArabic = "SD المضغوطة للمشاركة",
        badge = "480p SD",
        width = 854,
        height = 480,
        descriptionArabic = "حجم ملف خفيف جداً لتوفير البيانات والإرسال الفوري عبر واتساب",
        baseBitrateBps = 1_500_000
    )
}

/**
 * Video bitrate presets affecting the clarity and final file size.
 */
enum class VideoBitratePreset(
    val labelArabic: String,
    val multiplier: Float,
    val descriptionArabic: String
) {
    ULTRA(
        labelArabic = "فائق (Ultra)",
        multiplier = 1.25f,
        descriptionArabic = "أعلى تفاصيل ونقاء لوني بدون فقدان في الضغط"
    ),
    HIGH(
        labelArabic = "عالي (High)",
        multiplier = 1.0f,
        descriptionArabic = "جودة ممتازة موصى بها مع توازن مثالي"
    ),
    STANDARD(
        labelArabic = "متوسط (Standard)",
        multiplier = 0.75f,
        descriptionArabic = "حجم أصغر مع الحفاظ على وضوح نصوص الترجمة"
    ),
    ECO(
        labelArabic = "اقتصادي (Eco)",
        multiplier = 0.5f,
        descriptionArabic = "أصغر مساحة تخزين ممكنة للمشاركة السريعة"
    )
}

/**
 * Framerate preset (frames per second).
 */
enum class FrameRatePreset(
    val labelArabic: String,
    val fps: Int,
    val descriptionArabic: String
) {
    FPS_60(
        labelArabic = "60 FPS",
        fps = 60,
        descriptionArabic = "سلاسة حركية فائقة جداً"
    ),
    FPS_30(
        labelArabic = "30 FPS",
        fps = 30,
        descriptionArabic = "التردد القياسي السلس والموصى به"
    ),
    FPS_24(
        labelArabic = "24 FPS",
        fps = 24,
        descriptionArabic = "طابع سينمائي كلاسيكي (Cinematic)"
    )
}

/**
 * Audio quality presets for multiplexed audio track.
 */
enum class AudioQualityPreset(
    val labelArabic: String,
    val bitrateKbps: Int,
    val descriptionArabic: String
) {
    STUDIO(
        labelArabic = "320 kbps (ستوديو ماستر)",
        bitrateKbps = 320,
        descriptionArabic = "أقصى نقاء وتفاصيل صوتية احترافية"
    ),
    HIGH(
        labelArabic = "192 kbps (عالي النقاء)",
        bitrateKbps = 192,
        descriptionArabic = "جودة عالية واضحة جداً للدبلجة"
    ),
    STANDARD(
        labelArabic = "128 kbps (قياسي)",
        bitrateKbps = 128,
        descriptionArabic = "حجم متوازن مناسب لمعظم الاستخدامات"
    )
}

/**
 * Complete video export configuration.
 */
data class VideoExportConfig(
    val resolution: ExportResolution = ExportResolution.HD_720P,
    val bitratePreset: VideoBitratePreset = VideoBitratePreset.HIGH,
    val frameRatePreset: FrameRatePreset = FrameRatePreset.FPS_30,
    val audioQuality: AudioQualityPreset = AudioQualityPreset.HIGH,
    val burnSubtitles: Boolean = true,
    val showWaveform: Boolean = true,
    val includeOriginalAudioDuck: Boolean = true,
    val customBitrateMbps: Float? = null
) {
    /**
     * Calculate final video bitrate in bits per second (bps)
     */
    fun getCalculatedBitrateBps(): Int {
        if (customBitrateMbps != null && customBitrateMbps > 0f) {
            return (customBitrateMbps * 1_000_000).toInt()
        }
        return (resolution.baseBitrateBps * bitratePreset.multiplier).toInt()
    }

    /**
     * Get bitrate formatted in Mbps for UI
     */
    fun getBitrateMbpsFormatted(): String {
        val mbps = getCalculatedBitrateBps() / 1_000_000f
        return String.format(Locale.US, "%.1f Mbps", mbps)
    }

    /**
     * Estimate final video file size in Megabytes (MB) based on clip duration in seconds.
     */
    fun calculateEstimatedSizeMb(durationSeconds: Int): Float {
        val totalBitrateBps = getCalculatedBitrateBps() + (audioQuality.bitrateKbps * 1000)
        val totalBits = totalBitrateBps.toDouble() * durationSeconds.coerceAtLeast(1)
        val totalBytes = totalBits / 8.0
        val sizeMb = totalBytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.2f", sizeMb).toFloat()
    }
}
