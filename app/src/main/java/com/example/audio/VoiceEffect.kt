package com.example.audio

enum class VoiceEffect(
    val id: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val iconName: String,
    val pitchMultiplier: Float,
    val speedMultiplier: Float,
    val isRobot: Boolean = false,
    val isEcho: Boolean = false
) {
    NORMAL(
        id = "NORMAL",
        titleArabic = "صوت طبيعي",
        subtitleArabic = "صوتك الحقيقي بدون تعديل",
        iconName = "mic",
        pitchMultiplier = 1.0f,
        speedMultiplier = 1.0f
    ),
    DEEP(
        id = "DEEP",
        titleArabic = "سينمائي ضخم",
        subtitleArabic = "طبقة عميقة وفخمة للوثائقيات",
        iconName = "record_voice_over",
        pitchMultiplier = 0.72f,
        speedMultiplier = 0.9f
    ),
    CHIPMUNK(
        id = "CHIPMUNK",
        titleArabic = "كرتون / سنجاب",
        subtitleArabic = "صوت رفيع ومرح للشخصيات الكرتونية",
        iconName = "face",
        pitchMultiplier = 1.65f,
        speedMultiplier = 1.15f
    ),
    ROBOT(
        id = "ROBOT",
        titleArabic = "روبوت آلي",
        subtitleArabic = "نبرة سايبر إلكترونية لأفلام الخيال",
        iconName = "smart_toy",
        pitchMultiplier = 0.85f,
        speedMultiplier = 0.95f,
        isRobot = true
    ),
    ECHO(
        id = "ECHO",
        titleArabic = "استوديو وصدى",
        subtitleArabic = "تردد صوتي سينمائي فسيح",
        iconName = "surround_sound",
        pitchMultiplier = 1.05f,
        speedMultiplier = 1.0f,
        isEcho = true
    ),
    FEMALE_VOICE(
        id = "FEMALE_VOICE",
        titleArabic = "تحويل لصوت أنثوي",
        subtitleArabic = "طبقة ناعمة ورقيقة تحول صوت الذكر لأنثى",
        iconName = "female",
        pitchMultiplier = 1.50f,
        speedMultiplier = 1.05f
    ),
    MALE_VOICE(
        id = "MALE_VOICE",
        titleArabic = "تحويل لصوت رجالي",
        subtitleArabic = "طبقة عميقة وجهورية تحول صوت الأنثى لذكر",
        iconName = "male",
        pitchMultiplier = 0.70f,
        speedMultiplier = 0.95f
    ),
    AUTO_GENDER(
        id = "AUTO_GENDER",
        titleArabic = "تحويل تلقائي ذكي",
        subtitleArabic = "يكتشف نبرتك ويحولها تلقائياً للجنس المعاكس",
        iconName = "auto_awesome",
        pitchMultiplier = 1.0f,
        speedMultiplier = 1.0f
    ),
    RADIO(
        id = "RADIO",
        titleArabic = "مذياع كلاسيكي",
        subtitleArabic = "مؤثر راديو قديم مع فلتر الترددات",
        iconName = "radio",
        pitchMultiplier = 1.15f,
        speedMultiplier = 1.05f
    )
}

enum class BgmStyle(
    val id: String,
    val titleArabic: String,
    val iconEmoji: String
) {
    NONE(
        id = "NONE",
        titleArabic = "بدون موسيقى",
        iconEmoji = "🔇"
    ),
    CINEMATIC(
        id = "CINEMATIC",
        titleArabic = "سينمائي هادئ",
        iconEmoji = "🎬"
    ),
    FUNNY(
        id = "FUNNY",
        titleArabic = "كوميدي مرح",
        iconEmoji = "🎪"
    ),
    DRAMATIC(
        id = "DRAMATIC",
        titleArabic = "دراما وتشويق",
        iconEmoji = "🎻"
    ),
    LOFI(
        id = "LOFI",
        titleArabic = "إيقاع ناعم",
        iconEmoji = "🎧"
    ),
    SPACETOON(
        id = "SPACETOON",
        titleArabic = "ألحان سبيستون الأسطورية",
        iconEmoji = "🌟"
    )
}
