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
    URDU("ur", "الأردية (Urdu)", "اردو", "🇵🇰", true),
    DUTCH("nl", "الهولندية (Dutch)", "Nederlands", "🇳🇱", false),
    POLISH("pl", "البولندية (Polish)", "Polski", "🇵🇱", false),
    SWEDISH("sv", "السويدية (Swedish)", "Svenska", "🇸🇪", false),
    UKRAINIAN("uk", "الأوكرانية (Ukrainian)", "Українська", "🇺🇦", false),
    GREEK("el", "اليونانية (Greek)", "Ελληνικά", "🇬🇷", false),
    VIETNAMESE("vi", "الفيتنامية (Vietnamese)", "Tiếng Việt", "🇻🇳", false),
    THAI("th", "التايلاندية (Thai)", "ไทย", "🇹🇭", false),
    FILIPINO("fil", "الفلبينية (Filipino)", "Tagalog", "🇵🇭", false),
    HEBREW("he", "العبرية (Hebrew)", "עברית", "🇮🇱", true),
    BENGALI("bn", "البنغالية (Bengali)", "বাংলা", "🇧🇩", false),
    MALAY("ms", "الملايوية (Malay)", "Bahasa Melayu", "🇲🇾", false),
    CZECH("cs", "التشيكية (Czech)", "Čeština", "🇨🇿", false),
    ROMANIAN("ro", "الرومانية (Romanian)", "Română", "🇷🇴", false),
    DANISH("da", "الدانماركية (Danish)", "Dansk", "🇩🇰", false),
    FINNISH("fi", "الفنلندية (Finnish)", "Suomi", "🇫🇮", false),
    NORWEGIAN("no", "النرويجية (Norwegian)", "Norsk", "🇳🇴", false),
    HUNGARIAN("hu", "المجرية (Hungarian)", "Magyar", "🇭🇺", false),
    SWAHILI("sw", "السواحيلية (Swahili)", "Kiswahili", "🇰🇪", false),
    AMHARIC("am", "الأمهرية (Amharic)", "አማርኛ", "🇪🇹", false),
    SOMALI("so", "الصومالية (Somali)", "Soomaali", "🇸🇴", false),
    HAUSA("ha", "الهوسا (Hausa)", "Harshen Hausa", "🇳🇬", true),
    YORUBA("yo", "اليوروبا (Yoruba)", "Èdè Yorùbá", "🇳🇬", false),
    IGBO("ig", "الإيغبو (Igbo)", "Asụsụ Igbo", "🇳🇬", false),
    OROMO("om", "الأورومو (Oromo)", "Afaan Oromoo", "🇪🇹", false),
    TIGRINYA("ti", "التغرينية (Tigrinya)", "ትግርኛ", "🇪🇷", false),
    ZULU("zu", "الزولو (Zulu)", "isiZulu", "🇿🇦", false),
    XHOSA("xh", "الخوسا (Xhosa)", "isiXhosa", "🇿🇦", false),
    AFRIKAANS("af", "الأفريقانية (Afrikaans)", "Afrikaans", "🇿🇦", false),
    MALAGASY("mg", "الملغاشية (Malagasy)", "Malagasy", "🇲🇬", false),
    CATALAN("ca", "الكتالونية (Catalan)", "Català", "🇪🇸", false),
    BASQUE("eu", "الباسكية (Basque)", "Euskara", "🇪🇸", false),
    GALICIAN("gl", "الجاليكية (Galician)", "Galego", "🇪🇸", false),
    IRISH("ga", "الأيرلندية (Irish)", "Gaeilge", "🇮🇪", false),
    WELSH("cy", "الويلزية (Welsh)", "Cymraeg", "🏴󠁧󠁢󠁷󠁬󠁳󠁿", false),
    ICELANDIC("is", "الأيسلندية (Icelandic)", "Íslenska", "🇮🇸", false),
    MALTESE("mt", "المالطية (Maltese)", "Malti", "🇲🇹", false),
    CROATIAN("hr", "الكرواتية (Croatian)", "Hrvatski", "🇭🇷", false),
    SERBIAN("sr", "الصربية (Serbian)", "Српски", "🇷🇸", false),
    BOSNIAN("bs", "البوسنية (Bosnian)", "Bosanski", "🇧🇦", false),
    BULGARIAN("bg", "البلغارية (Bulgarian)", "Български", "🇧🇬", false),
    SLOVAK("sk", "السلوفاكية (Slovak)", "Slovenčina", "🇸🇰", false),
    SLOVENIAN("sl", "السلوفينية (Slovenian)", "Slovenščina", "🇸🇮", false),
    MACEDONIAN("mk", "المقدونية (Macedonian)", "Македонски", "🇲🇰", false),
    ALBANIAN("sq", "الألبانية (Albanian)", "Shqip", "🇦🇱", false),
    LITHUANIAN("lt", "الليتوانية (Lithuanian)", "Lietuvių", "🇱🇹", false),
    LATVIAN("lv", "اللاتفية (Latvian)", "Latviešu", "🇱🇻", false),
    ESTONIAN("et", "الإستونية (Estonian)", "Eesti", "🇪🇪", false),
    BELARUSIAN("be", "البيلاروسية (Belarusian)", "Беларуская", "🇧🇾", false),
    KAZAKH("kk", "الكازاخية (Kazakh)", "Қазақша", "🇰🇿", false),
    UZBEK("uz", "الأوزبكية (Uzbek)", "Oʻzbekcha", "🇺🇿", false),
    AZERBAIJANI("az", "الأذربيجانية (Azerbaijani)", "Azərbaycan", "🇦🇿", false),
    GEORGIAN("ka", "الجورجية (Georgian)", "ქართული", "🇬🇪", false),
    ARMENIAN("hy", "الأرمينية (Armenian)", "Հայերեն", "🇦🇲", false),
    MONGOLIAN("mn", "المنغولية (Mongolian)", "Монгол", "🇲🇳", false),
    TURKMEN("tk", "التركمانية (Turkmen)", "Türkmençe", "🇹🇲", false),
    TAJIK("tg", "الطاجيكية (Tajik)", "Тоҷикӣ", "🇹🇯", false),
    KURDISH("ku", "الكردية (Kurdish)", "کوردی", "🇮🇶", true),
    PASHTO("ps", "البشتوية (Pashto)", "پښتو", "🇦🇫", true),
    TAMIL("ta", "التاميلية (Tamil)", "தமிழ்", "🇮🇳", false),
    TELUGU("te", "التيلوغوية (Telugu)", "తెలుగు", "🇮🇳", false),
    MARATHI("mr", "المراثية (Marathi)", "मराठी", "🇮🇳", false),
    GUJARATI("gu", "الغوجاراتية (Gujarati)", "ગુજરાતી", "🇮🇳", false),
    KANNADA("kn", "الكانادا (Kannada)", "ಕನ್ನಡ", "🇮🇳", false),
    MALAYALAM("ml", "المالايالامية (Malayalam)", "മലയാളം", "🇮🇳", false),
    PUNJABI("pa", "البنجابية (Punjabi)", "ਪੰਜਾਬੀ", "🇮🇳", false),
    SINHALA("si", "السنهالية (Sinhala)", "සිංහල", "🇱🇰", false),
    NEPALI("ne", "النيبالية (Nepali)", "नेपाली", "🇳🇵", false),
    BURMESE("my", "البورمية (Burmese)", "မြန်မာစာ", "🇲🇲", false),
    KHMER("km", "الخميرية (Khmer)", "ភាសាខ្មែរ", "🇰🇭", false),
    LAO("lo", "اللاوية (Lao)", "ພາສາລາວ", "🇱🇦", false),
    JAVANESE("jv", "الجاوية (Javanese)", "Basa Jawa", "🇮🇩", false),
    SUNDANESE("su", "السوندية (Sundanese)", "Basa Sunda", "🇮🇩", false),
    CEBUANO("ceb", "السيبوانية (Cebuano)", "Sinugboanon", "🇵🇭", false),
    ESPERANTO("eo", "الإسبرانتو (Esperanto)", "Esperanto", "🌐", false),
    LATIN("la", "اللاتينية (Latin)", "Latina", "🏛️", false),
    YIDDISH("yi", "اليديشية (Yiddish)", "ייִדיש", "✡️", true);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }

        fun searchLanguages(query: String): List<AppLanguage> {
            if (query.isBlank()) return entries
            val q = query.trim().lowercase()
            return entries.filter {
                it.code.lowercase().contains(q) ||
                it.displayName.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q)
            }
        }
    }
}
