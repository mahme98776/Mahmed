package com.example.alad

/**
 * ALAD Mobile - AI Live Audio Dubbing Language Catalog
 * Supports real-time translation and voice dubbing across 78 international languages
 * All Intellectual Property & Publishing Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
data class AladLanguage(
    val code: String,
    val nameEnglish: String,
    val nameArabic: String,
    val flagEmoji: String,
    val dialectCode: String = code,
    val defaultPitch: Float = 1.0f,
    val defaultSpeed: Float = 1.0f
)

object AladLanguageCatalog {
    val supportedLanguages: List<AladLanguage> = listOf(
        AladLanguage("ar", "Arabic (Standard)", "العربية (فصحى)", "🇸🇦", "ar-SA"),
        AladLanguage("ar-EG", "Arabic (Egyptian)", "العربية (اللهجة المصرية)", "🇪🇬", "ar-EG"),
        AladLanguage("ar-SA", "Arabic (Saudi / Gulf)", "العربية (اللهجة الخليجية)", "🇸🇦", "ar-SA"),
        AladLanguage("ar-SY", "Arabic (Levantine)", "العربية (اللهجة الشامية)", "🇸🇾", "ar-SY"),
        AladLanguage("ar-MA", "Arabic (Maghrebi)", "العربية (المغربية)", "🇲🇦", "ar-MA"),
        AladLanguage("en", "English (US)", "الإنجليزية (الأمريكية)", "🇺🇸", "en-US"),
        AladLanguage("en-GB", "English (UK)", "الإنجليزية (البريطانية)", "🇬🇧", "en-GB"),
        AladLanguage("en-AU", "English (Australian)", "الإنجليزية (الأسترالية)", "🇦🇺", "en-AU"),
        AladLanguage("en-IN", "English (Indian)", "الإنجليزية (الهندية)", "🇮🇳", "en-IN"),
        AladLanguage("es", "Spanish (Spain)", "الإسبانية (إسبانيا)", "🇪🇸", "es-ES"),
        AladLanguage("es-MX", "Spanish (Latin America)", "الإسبانية (أمريكا اللاتينية)", "🇲🇽", "es-MX"),
        AladLanguage("fr", "French (France)", "الفرنسية (فرنسا)", "🇫🇷", "fr-FR"),
        AladLanguage("fr-CA", "French (Canada)", "الفرنسية (كندا)", "🇨🇦", "fr-CA"),
        AladLanguage("de", "German", "الألمانية", "🇩🇪", "de-DE"),
        AladLanguage("it", "Italian", "الإيطالية", "🇮🇹", "it-IT"),
        AladLanguage("ja", "Japanese", "اليابانية", "🇯🇵", "ja-JP"),
        AladLanguage("ko", "Korean", "الكورية", "🇰🇷", "ko-KR"),
        AladLanguage("zh", "Chinese (Mandarin Simplified)", "الصينية (ماندرين مبسطة)", "🇨🇳", "zh-CN"),
        AladLanguage("zh-TW", "Chinese (Traditional)", "الصينية (تقليدية)", "🇹🇼", "zh-TW"),
        AladLanguage("ru", "Russian", "الروسية", "🇷🇺", "ru-RU"),
        AladLanguage("tr", "Turkish", "التركية", "🇹🇷", "tr-TR"),
        AladLanguage("pt", "Portuguese (Brazil)", "البرتغالية (البرازيل)", "🇧🇷", "pt-BR"),
        AladLanguage("pt-PT", "Portuguese (Portugal)", "البرتغالية (البرتغال)", "🇵🇹", "pt-PT"),
        AladLanguage("hi", "Hindi", "الهندية", "🇮🇳", "hi-IN"),
        AladLanguage("ur", "Urdu", "الأوردية", "🇵🇰", "ur-PK"),
        AladLanguage("fa", "Persian (Farsi)", "الفارسية", "🇮🇷", "fa-IR"),
        AladLanguage("id", "Indonesian", "الإندونيسية", "🇮🇩", "id-ID"),
        AladLanguage("ms", "Malay", "الملايوية", "🇲🇾", "ms-MY"),
        AladLanguage("vi", "Vietnamese", "الفيتنامية", "🇻🇳", "vi-VN"),
        AladLanguage("th", "Thai", "التايلاندية", "🇹🇭", "th-TH"),
        AladLanguage("nl", "Dutch", "الهولندية", "🇳🇱", "nl-NL"),
        AladLanguage("pl", "Polish", "البولندية", "🇵🇱", "pl-PL"),
        AladLanguage("uk", "Ukrainian", "الأوكرانية", "🇺🇦", "uk-UA"),
        AladLanguage("sv", "Swedish", "السويدية", "🇸🇪", "sv-SE"),
        AladLanguage("no", "Norwegian", "النرويجية", "🇳🇴", "no-NO"),
        AladLanguage("da", "Danish", "الدانماركية", "🇩🇰", "da-DK"),
        AladLanguage("fi", "Finnish", "الفنلندية", "🇫🇮", "fi-FI"),
        AladLanguage("el", "Greek", "اليونانية", "🇬🇷", "el-GR"),
        AladLanguage("cs", "Czech", "التشيكية", "🇨🇿", "cs-CZ"),
        AladLanguage("ro", "Romanian", "الرومانية", "🇷🇴", "ro-RO"),
        AladLanguage("hu", "Hungarian", "المجرية", "🇭🇺", "hu-HU"),
        AladLanguage("he", "Hebrew", "العبرية", "🇮🇱", "he-IL"),
        AladLanguage("bn", "Bengali", "البنغالية", "🇧🇩", "bn-BD"),
        AladLanguage("ta", "Tamil", "التاميلية", "🇮🇳", "ta-IN"),
        AladLanguage("te", "Telugu", "التيلجو", "🇮🇳", "te-IN"),
        AladLanguage("mr", "Marathi", "الماراثية", "🇮🇳", "mr-IN"),
        AladLanguage("gu", "Gujarati", "الغوجاراتية", "🇮🇳", "gu-IN"),
        AladLanguage("kn", "Kannada", "الكانادا", "🇮🇳", "kn-IN"),
        AladLanguage("ml", "Malayalam", "المالايالامية", "🇮🇳", "ml-IN"),
        AladLanguage("pa", "Punjabi", "البنجابية", "🇮🇳", "pa-IN"),
        AladLanguage("fil", "Filipino (Tagalog)", "الفلبينية", "🇵🇭", "fil-PH"),
        AladLanguage("sw", "Swahili", "السواحيلية", "🇰🇪", "sw-KE"),
        AladLanguage("af", "Afrikaans", "الأفريقانية", "🇿🇦", "af-ZA"),
        AladLanguage("am", "Amharic", "الأمهرية", "🇪🇹", "am-ET"),
        AladLanguage("az", "Azerbaijani", "الأذربيجانية", "🇦🇿", "az-AZ"),
        AladLanguage("bg", "Bulgarian", "البلغارية", "🇧🇬", "bg-BG"),
        AladLanguage("ca", "Catalan", "الكتالونية", "🇪🇸", "ca-ES"),
        AladLanguage("hr", "Croatian", "الكرواتية", "🇭🇷", "hr-HR"),
        AladLanguage("sk", "Slovak", "السلوفاكية", "🇸🇰", "sk-SK"),
        AladLanguage("sl", "Slovenian", "السلوفينية", "🇸🇮", "sl-SI"),
        AladLanguage("sr", "Serbian", "الصربية", "🇷🇸", "sr-RS"),
        AladLanguage("lt", "Lithuanian", "الليتوانية", "🇱🇹", "lt-LT"),
        AladLanguage("lv", "Latvian", "اللاتفية", "🇱🇻", "lv-LV"),
        AladLanguage("et", "Estonian", "الإستونية", "🇪🇪", "et-EE"),
        AladLanguage("is", "Icelandic", "الأيسلندية", "🇮🇸", "is-IS"),
        AladLanguage("ka", "Georgian", "الجورجية", "🇬🇪", "ka-GE"),
        AladLanguage("hy", "Armenian", "الأرمنية", "🇦🇲", "hy-AM"),
        AladLanguage("ne", "Nepali", "النيبالية", "🇳🇵", "ne-NP"),
        AladLanguage("si", "Sinhala", "السنهالية", "🇱🇰", "si-LK"),
        AladLanguage("my", "Burmese", "البورمية", "🇲🇲", "my-MM"),
        AladLanguage("km", "Khmer", "الخميرية", "🇰🇭", "km-KH"),
        AladLanguage("lo", "Lao", "اللاوية", "🇱🇦", "lo-LA"),
        AladLanguage("mn", "Mongolian", "المنغولية", "🇲🇳", "mn-MN"),
        AladLanguage("kk", "Kazakh", "الكازاخستانية", "🇰🇿", "kk-KZ"),
        AladLanguage("uz", "Uzbek", "الأوزبكية", "🇺🇿", "uz-UZ"),
        AladLanguage("ku", "Kurdish", "الكردية", "🇮🇶", "ku-IQ"),
        AladLanguage("ps", "Pashto", "البشتو", "🇦🇫", "ps-AF"),
        AladLanguage("so", "Somali", "الصومالية", "🇸🇴", "so-SO")
    )

    fun findByCode(code: String): AladLanguage {
        return supportedLanguages.firstOrNull { it.code.equals(code, ignoreCase = true) || it.dialectCode.equals(code, ignoreCase = true) }
            ?: supportedLanguages[0] // Arabic default
    }
}
