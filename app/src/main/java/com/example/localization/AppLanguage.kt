package com.example.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val isRtl: Boolean
) {
    ARABIC("ar", "العربية (Arabic)", "العربية", "🇸🇦", true),
    ENGLISH("en", "الإنجليزية (English)", "English", "🇺🇸", false),
    SPANISH("es", "الإسبانية (Spanish)", "Español", "🇪🇸", false),
    FRENCH("fr", "الفرنسية (French)", "Français", "🇫🇷", false),
    GERMAN("de", "الألمانية (German)", "Deutsch", "🇩🇪", false),
    TURKISH("tr", "التركية (Turkish)", "Türkçe", "🇹🇷", false),
    RUSSIAN("ru", "الروسية (Russian)", "Русский", "🇷🇺", false),
    CHINESE("zh", "الصينية (Chinese)", "中文", "🇨🇳", false),
    JAPANESE("ja", "اليابانية (Japanese)", "日本語", "🇯🇵", false),
    KOREAN("ko", "الكورية (Korean)", "한국어", "🇰🇷", false),
    ITALIAN("it", "الإيطالية (Italian)", "Italiano", "🇮🇹", false),
    PORTUGUESE("pt", "البرتغالية (Portuguese)", "Português", "🇧🇷", false),
    HINDI("hi", "الهندية (Hindi)", "हिन्दी", "🇮🇳", false),
    INDONESIAN("id", "الإندونيسية (Indonesian)", "Bahasa Indonesia", "🇮🇩", false),
    PERSIAN("fa", "الفارسية (Persian)", "فارسی", "🇮🇷", true),
    URDU("ur", "الأردية (Urdu)", "اردو", "🇵🇰", true);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }
    }
}
