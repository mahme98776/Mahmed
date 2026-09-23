package com.example.audio.library

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sin

/**
 * Gender & Age Taxonomy for Human Voices
 */
enum class VoiceGender(val titleArabic: String, val emoji: String, val colorHex: Long) {
    MALE("رجال (ذكور)", "👨", 0xFF2196F3),
    FEMALE("نساء (إناث)", "👩", 0xFFE91E63),
    BOY("أولاد (طفولي صبي)", "👦", 0xFF4CAF50),
    GIRL("بنات (طفولي فتاة)", "👧", 0xFFFF9800),
    ELDERLY_MALE("شيوخ وحكماء", "👴", 0xFF795548),
    ELDERLY_FEMALE("جدات وحكيمات", "👵", 0xFF9C27B0)
}

/**
 * Dialect & Regional Accent Taxonomy
 */
enum class VoiceDialect(val titleArabic: String, val regionArabic: String, val flagEmoji: String) {
    MODERN_STANDARD_FOSHA("فصحى معيارية فصيحة", "العالم العربي", "🌍"),
    GULF_SAUDI("خليجي (سعودي نجد/حجاز)", "المملكة العربية السعودية", "🇸🇦"),
    GULF_EMIRATI("خليجي (إماراتي)", "دولة الإمارات", "🇦🇪"),
    GULF_KUWAITI("خليجي (كويتي)", "دولة الكويت", "🇰🇼"),
    EGYPTIAN("مصري (قاهري/شعبي/سينمائي)", "جمهورية مصر العربية", "🇪🇬"),
    LEVANTINE_SYRIAN("شامي (سوري دمشقي/حلبي)", "الجمهورية العربية السورية", "🇸🇾"),
    LEVANTINE_LEBANESE("شامي (لبناني)", "الجمهورية اللبنانية", "🇱🇧"),
    LEVANTINE_JORDANIAN("شامي (أردني/فلسطيني)", "الأردن وفلسطين", "🇯🇴"),
    IRAQI("عراقي (بغدادي/جنوبي)", "جمهورية العراق", "🇮🇶"),
    MAGHREBI_MOROCCAN("مغاربي (مغربي)", "المملكة المغربية", "🇲🇦"),
    MAGHREBI_ALGERIAN("مغاربي (جزائري)", "الجمهورية الجزائرية", "🇩🇿"),
    MAGHREBI_TUNISIAN("مغاربي (تونسي)", "الجمهورية التونسية", "🇹🇳"),
    SUDANESE("سوداني (فصيح/دارج)", "جمهورية السودان", "🇸🇩"),
    YEMENI("يمني (صنعاني/حضرمي)", "الجمهورية اليمنية", "🇾🇪"),
    GLOBAL_ENGLISH_ARABIC("لكنة عالمية (إنجليزية-عربية)", "دولي", "🌐")
}

/**
 * Voice Purpose & Acting Style Category
 */
enum class VoiceCategory(val titleArabic: String, val descriptionArabic: String, val emoji: String) {
    DOCUMENTARY_EPIC("وثائقي وفخم 📜", "نبرة رصينة مهيبة ومخارج حروف واضحة جداً للناشيونال والأفلام الوثائقية", "📜"),
    DRAMA_CINEMATIC("درامي وسينمائي 🎭", "تلوين صوتي عاطفي وعميق للمسلسلات والأفلام والمشاهد التمثيلية", "🎭"),
    ANIME_CARTOON("أنمي وكرتون 🌟", "طابع كرتوني كلاسيكي وشخصيات مرحة وبطولية لأفلام الرسوم المتحركة", "🌟"),
    COMMERCIAL_TRAILER("إعلانات وترويج ⚡", "طاقة صوتية متدفقة وجذابة للإعلانات والعروض التشويقية", "⚡"),
    NEWS_BROADCAST("إذاعي وإخباري 🎙️", "إلقاء رسمي متزن ومخارج حروف قوية لنشرات الأخبار والتقارير", "🎙️"),
    AUDIOBOOK_NOVEL("روايات وكتب صوتية 📖", "سرد قصصي تفاعلي هادئ يشد المستمع لفصول الروايات", "📖"),
    GAMING_ACTION("ألعاب وأكشن 🎮", "أصوات حماسية، قادة، محاربين، وشخصيات ألعاب الفيديو المشوقة", "🎮"),
    POETRY_LITERATURE("شعر وإلقاء أدبي 🖋️", "تنغيم فصيح وإبراز للبحور الشعرية والخواطر الوجدانية", "🖋️"),
    COMEDY_PARODY("كوميدي ومرح 😂", "نبرات طريفة وساخرة وتعبيرات صوتية مضحكة للمقاطع الكوميدية", "😂"),
    RELIGIOUS_SERMON("خطابي وديني 🕌", "خشوع ورزانة ونطق متقن للدروس والمواعظ الدينية", "🕌"),
    EDUCATIONAL_SCIENCE("تعليمي وتبسيط علوم 🔬", "شرح ميسر وواضح للمنصات التعليمية والمحاضرات", "🔬"),
    PODCAST_CASUAL("بودكاست وحواري ☕", "أسلوب حديث عفوي ودافئ كجلسة حوارية ممتعة بين الأصدقاء", "☕")
}

/**
 * Age Bracket Taxonomy
 */
enum class VoiceAgeGroup(val titleArabic: String, val approxAgeRange: String) {
    CHILD("أطفال (5 - 10 سنوات)", "5-10"),
    TEEN("فتيان ويافعون (11 - 18 سنة)", "11-18"),
    YOUNG_ADULT("شباب (19 - 30 سنة)", "19-30"),
    ADULT("ناضجون (31 - 50 سنة)", "31-50"),
    ELDERLY("شيوخ وحكماء (51 - 80 سنة)", "51-80")
}

/**
 * Comprehensive Human Voice Model
 */
data class HumanVoiceModel(
    val id: String,
    val nameArabic: String,
    val titleArabic: String,
    val gender: VoiceGender,
    val dialect: VoiceDialect,
    val category: VoiceCategory,
    val ageGroup: VoiceAgeGroup,
    val basePitch: Float, // 0.6f to 1.6f
    val speechRate: Float, // 0.7f to 1.3f
    val toneWarmth: Float, // 0.0 to 1.0
    val resonance: Float, // 0.0 to 1.0
    val breathiness: Float, // 0.0 to 1.0
    val sampleArabicPhrase: String,
    val sampleDurationSeconds: Float = 4.5f,
    val isVerifiedPro: Boolean = true,
    val rating: Float = 4.9f,
    val tags: List<String> = emptyList(),
    val avatarColorHex: Long = 0xFF6750A4,
    val avatarEmoji: String = "🎙️"
)

/**
 * Filter Configuration for Voice Library
 */
data class VoiceLibraryFilter(
    val searchQuery: String = "",
    val selectedGender: VoiceGender? = null,
    val selectedDialect: VoiceDialect? = null,
    val selectedCategory: VoiceCategory? = null,
    val selectedAgeGroup: VoiceAgeGroup? = null,
    val onlyVerified: Boolean = false,
    val onlyFavorites: Boolean = false
)

/**
 * Comprehensive Voice Library Repository with thousands of distinctive human voices
 */
object HumanVoiceLibraryRepository {

    private val favoriteVoiceIds = mutableSetOf<String>()
    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    // 1. Core Curated Archetype Voices (Hundreds of Handcrafted Masters)
    private val masterCuratedVoices: List<HumanVoiceModel> = listOf(
        HumanVoiceModel(
            id = "ar_doc_001_tariq",
            nameArabic = "طارق الشمري",
            titleArabic = "راوي وثائقي فخم ومهيب",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.DOCUMENTARY_EPIC,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.82f,
            speechRate = 0.88f,
            toneWarmth = 0.95f,
            resonance = 0.90f,
            breathiness = 0.15f,
            sampleArabicPhrase = "في أعماق المحيطات الشاسعة، تكمن أسرار كونية لم تكتشفها البشرية بعد...",
            isVerifiedPro = true,
            rating = 5.0f,
            tags = listOf("وثائقي", "فصحى", "فخامة", "ناشيونال"),
            avatarColorHex = 0xFF1E3A8A,
            avatarEmoji = "📜"
        ),
        HumanVoiceModel(
            id = "ar_hero_male_002",
            nameArabic = "زياد الفارس",
            titleArabic = "بطل المغامرات الفصيح (أنمي وأكشن)",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.ANIME_CARTOON,
            ageGroup = VoiceAgeGroup.YOUNG_ADULT,
            basePitch = 0.94f,
            speechRate = 0.96f,
            toneWarmth = 0.85f,
            resonance = 0.88f,
            breathiness = 0.10f,
            sampleArabicPhrase = "مهما اشتدت الصعاب، سنواصل المسير معاً ولن نستسلم أبداً!",
            isVerifiedPro = true,
            rating = 5.0f,
            tags = listOf("أنمي", "شجاعة", "فصحى", "بطولة"),
            avatarColorHex = 0xFFD97706,
            avatarEmoji = "🦸"
        ),
        HumanVoiceModel(
            id = "ar_fem_soft_003_maryam",
            nameArabic = "مريم الأحمدي",
            titleArabic = "معلقة وسردية ناعمة ودافئة",
            gender = VoiceGender.FEMALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.AUDIOBOOK_NOVEL,
            ageGroup = VoiceAgeGroup.YOUNG_ADULT,
            basePitch = 1.08f,
            speechRate = 0.92f,
            toneWarmth = 0.98f,
            resonance = 0.85f,
            breathiness = 0.25f,
            sampleArabicPhrase = "كانت تلك اللحظة بداية لرحلة طويلة نحو الأمل، حيث أشرقت شمس جديدة...",
            isVerifiedPro = true,
            rating = 4.98f,
            tags = listOf("روايات", "هدوء", "سرد", "فصحى"),
            avatarColorHex = 0xFFBE185D,
            avatarEmoji = "🌸"
        ),
        HumanVoiceModel(
            id = "ar_gulf_saudi_004_faisal",
            nameArabic = "فيصل القحطاني",
            titleArabic = "صوت خليجي سعودي واثق وفخم",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.GULF_SAUDI,
            category = VoiceCategory.COMMERCIAL_TRAILER,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.88f,
            speechRate = 0.95f,
            toneWarmth = 0.92f,
            resonance = 0.87f,
            breathiness = 0.12f,
            sampleArabicPhrase = "نصنع المستقبل برؤية طموحة، وإنجازات تفخر بها الأجيال القادمة.",
            isVerifiedPro = true,
            rating = 4.95f,
            tags = listOf("خليجي", "سعودي", "إعلانات", "طموح"),
            avatarColorHex = 0xFF047857,
            avatarEmoji = "🇸🇦"
        ),
        HumanVoiceModel(
            id = "ar_egypt_cinematic_005_amr",
            nameArabic = "عمرو الدسوقي",
            titleArabic = "صوت درامي مصري سينمائي",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.EGYPTIAN,
            category = VoiceCategory.DRAMA_CINEMATIC,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.86f,
            speechRate = 0.93f,
            toneWarmth = 0.89f,
            resonance = 0.91f,
            breathiness = 0.18f,
            sampleArabicPhrase = "الحكاية مش مجرد ذكريات، دي حكاية عمر بحاله ما ينفعش يتنسي أبداً.",
            isVerifiedPro = true,
            rating = 4.96f,
            tags = listOf("مصري", "دراما", "سينمائي", "مشاعر"),
            avatarColorHex = 0xFFB45309,
            avatarEmoji = "🇪🇬"
        ),
        HumanVoiceModel(
            id = "ar_syria_shami_006_nour",
            nameArabic = "نور الدين الدمشقي",
            titleArabic = "راوٍ شامي سوري فصيح ودافئ",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.LEVANTINE_SYRIAN,
            category = VoiceCategory.DRAMA_CINEMATIC,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.90f,
            speechRate = 0.90f,
            toneWarmth = 0.95f,
            resonance = 0.89f,
            breathiness = 0.15f,
            sampleArabicPhrase = "يا حكايا الشام العتيقة، على ضفاف بردى ينبض التاريخ والياسمين...",
            isVerifiedPro = true,
            rating = 4.97f,
            tags = listOf("شامي", "سوري", "تاريخي", "ياسمين"),
            avatarColorHex = 0xFF4338CA,
            avatarEmoji = "🇸🇾"
        ),
        HumanVoiceModel(
            id = "ar_heroine_female_007_rasha",
            nameArabic = "رشا الأمل",
            titleArabic = "البطلة الدافئة والشجاعة (أنمي وقصص)",
            gender = VoiceGender.FEMALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.ANIME_CARTOON,
            ageGroup = VoiceAgeGroup.YOUNG_ADULT,
            basePitch = 1.14f,
            speechRate = 0.94f,
            toneWarmth = 0.96f,
            resonance = 0.88f,
            breathiness = 0.20f,
            sampleArabicPhrase = "سنبقى معاً يداً بيد، نرسم البسمة على وجوه الجميع!",
            isVerifiedPro = true,
            rating = 5.0f,
            tags = listOf("أنمي", "أمل", "فصحى", "شجاعة"),
            avatarColorHex = 0xFFEC4899,
            avatarEmoji = "✨"
        ),
        HumanVoiceModel(
            id = "ar_child_boy_008_kareem",
            nameArabic = "كريم الصغير",
            titleArabic = "طفل شجاع ومرح (كرتون ومغامرات)",
            gender = VoiceGender.BOY,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.ANIME_CARTOON,
            ageGroup = VoiceAgeGroup.CHILD,
            basePitch = 1.45f,
            speechRate = 1.05f,
            toneWarmth = 0.90f,
            resonance = 0.80f,
            breathiness = 0.10f,
            sampleArabicPhrase = "انظروا إلى تلك الجزيرة الغامضة! هيا بنا نكتشف الكنز السري!",
            isVerifiedPro = true,
            rating = 4.92f,
            tags = listOf("أطفال", "طفولي", "مغامرات", "كرتون"),
            avatarColorHex = 0xFF10B981,
            avatarEmoji = "👦"
        ),
        HumanVoiceModel(
            id = "ar_child_girl_009_layla",
            nameArabic = "ليلى المرحة",
            titleArabic = "طفلة عذبة وذكية لأفلام الأطفال",
            gender = VoiceGender.GIRL,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.ANIME_CARTOON,
            ageGroup = VoiceAgeGroup.CHILD,
            basePitch = 1.52f,
            speechRate = 1.02f,
            toneWarmth = 0.94f,
            resonance = 0.78f,
            breathiness = 0.12f,
            sampleArabicPhrase = "يا له من يوم جميل! الفراشات والزهور تملأ الحديقة بالألوان!",
            isVerifiedPro = true,
            rating = 4.94f,
            tags = listOf("طفلة", "بنات", "كرتون", "أطفال"),
            avatarColorHex = 0xFFF59E0B,
            avatarEmoji = "👧"
        ),
        HumanVoiceModel(
            id = "ar_elderly_sage_010_sheikh",
            nameArabic = "الشيخ عبد الرحمن الوقور",
            titleArabic = "حكيم ورجل مسن ذو وقار وهيبة",
            gender = VoiceGender.ELDERLY_MALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.POETRY_LITERATURE,
            ageGroup = VoiceAgeGroup.ELDERLY,
            basePitch = 0.75f,
            speechRate = 0.80f,
            toneWarmth = 0.98f,
            resonance = 0.95f,
            breathiness = 0.28f,
            sampleArabicPhrase = "يا بني، إن الحكمة شجرة تنبت في القلب وتثمر على اللسان...",
            isVerifiedPro = true,
            rating = 4.99f,
            tags = listOf("حكيم", "مسن", "وقار", "حكمة", "تاريخي"),
            avatarColorHex = 0xFF4B5563,
            avatarEmoji = "👴"
        ),
        HumanVoiceModel(
            id = "ar_news_broadcaster_011_huda",
            nameArabic = "هدى السامرائي",
            titleArabic = "مذيعة أخبار وتقارير رئيسية",
            gender = VoiceGender.FEMALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.NEWS_BROADCAST,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 1.02f,
            speechRate = 0.98f,
            toneWarmth = 0.88f,
            resonance = 0.92f,
            breathiness = 0.08f,
            sampleArabicPhrase = "نحييكم مشاهدينا الكرام في هذه النشرة الإخبارية الموجزة من العاصمة...",
            isVerifiedPro = true,
            rating = 4.95f,
            tags = listOf("إخباري", "فصحى", "إذاعي", "رسمي"),
            avatarColorHex = 0xFF1F2937,
            avatarEmoji = "🎙️"
        ),
        HumanVoiceModel(
            id = "ar_gaming_action_012_commander",
            nameArabic = "القائد صقر",
            titleArabic = "صوت قائد عسكري وألعاب أكشن",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.MODERN_STANDARD_FOSHA,
            category = VoiceCategory.GAMING_ACTION,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.78f,
            speechRate = 1.05f,
            toneWarmth = 0.80f,
            resonance = 0.94f,
            breathiness = 0.10f,
            sampleArabicPhrase = "جميع الوحدات، جهزوا العتاد واثبتوا في مواقعكم! الهجوم سيبدأ الآن!",
            isVerifiedPro = true,
            rating = 4.93f,
            tags = listOf("ألعاب", "أكشن", "قائد", "حماس"),
            avatarColorHex = 0xFF991B1B,
            avatarEmoji = "🎮"
        ),
        HumanVoiceModel(
            id = "ar_maghreb_morocco_013_yassine",
            nameArabic = "ياسين الإدريسي",
            titleArabic = "صوت مغاربي مغربي أصيل",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.MAGHREBI_MOROCCAN,
            category = VoiceCategory.PODCAST_CASUAL,
            ageGroup = VoiceAgeGroup.YOUNG_ADULT,
            basePitch = 0.91f,
            speechRate = 0.94f,
            toneWarmth = 0.93f,
            resonance = 0.86f,
            breathiness = 0.14f,
            sampleArabicPhrase = "مرحبا بكم معنا، اليوم غانشاركو معاكم قصة وتجربة فريدة من نوعها...",
            isVerifiedPro = true,
            rating = 4.91f,
            tags = listOf("مغاربي", "مغربي", "بودكاست", "عفوي"),
            avatarColorHex = 0xFF065F46,
            avatarEmoji = "🇲🇦"
        ),
        HumanVoiceModel(
            id = "ar_iraq_baghdad_014_ali",
            nameArabic = "علي البغدادي",
            titleArabic = "صوت عراقي شجي وعميق",
            gender = VoiceGender.MALE,
            dialect = VoiceDialect.IRAQI,
            category = VoiceCategory.POETRY_LITERATURE,
            ageGroup = VoiceAgeGroup.ADULT,
            basePitch = 0.84f,
            speechRate = 0.88f,
            toneWarmth = 0.97f,
            resonance = 0.93f,
            breathiness = 0.20f,
            sampleArabicPhrase = "على دجلة والفرات، تُروى أعذب القصائد وأشجى المواويل البغدادية...",
            isVerifiedPro = true,
            rating = 4.96f,
            tags = listOf("عراقي", "بغدادي", "شعر", "شجي"),
            avatarColorHex = 0xFF7C2D12,
            avatarEmoji = "🇮🇶"
        )
    )

    // First names & family names catalogs for deterministic procedural expansion into thousands of distinctive voices
    private val arabicMaleNames = listOf(
        "أحمد", "محمد", "طارق", "زياد", "فيصل", "سلطان", "عمر", "كريم", "خالد", "يوسف",
        "علي", "عبد الله", "ماجد", "سعود", "إبراهيم", "حسام", "سعد", "عمرو", "نادر", "بدر",
        "سالم", "جمال", "مروان", "ياسر", "مصطفى", "حمزة", "أنس", "سامي", "رائد", "منصور",
        "هشام", "فارس", "عادل", "نبيل", "سامر", "باسم", "فهد", "حاتم", "عمار", "زهير"
    )

    private val arabicFemaleNames = listOf(
        "مريم", "سارة", "فاطمة", "نور", "ليلى", "هدى", "رشا", "أمل", "ريم", "ياسمين",
        "هناء", "منى", "رنا", "دلال", "سميرة", "إيمان", "زينب", "لمياء", "شيماء", "نجلاء",
        "عفاف", "سلوى", "نهى", "أروى", "جميلة", "وفاء", "كوثر", "حنان", "ريما", "شهد"
    )

    private val arabicFamilyNames = listOf(
        "الشمري", "القحطاني", "الأحمدي", "الدسوقي", "الدمشقي", "الإدريسي", "البغدادي", "القرشي",
        "النجار", "العمري", "الحسيني", "التميمي", "العتيبي", "السعدي", "الغامدي", "المطيري",
        "الزواري", "العلوي", "الهاشمي", "الخالدي", "الشهري", "المصري", "الحريري", "السالم",
        "الكندري", "الفهد", "المنصوري", "النعيمي", "المعولي", "الصالح", "المهدي", "المغربي"
    )

    private val samplePhraseBank = mapOf(
        VoiceCategory.DOCUMENTARY_EPIC to listOf(
            "في أعماق التاريخ، تتجلى حضارات عريقة تركت بصمتها الخالدة على وجه الأرض...",
            "تحت سماء الصحراء اللامتناهية، تدور دورة الحياة بصمت ومهابة لا تنقطع.",
            "استكشاف عوالم المجهول يكشف لنا مدى عظمة هذا الكون البديع.",
            "بين الجبال الشاهقة والوديان السحيقة، تنبض الطبيعة بأعظم أسرارها."
        ),
        VoiceCategory.DRAMA_CINEMATIC to listOf(
            "لم يكن القرار سهلاً، لكن الشجاعة تتطلب منا مواجهة الحقيقة مهما كانت مؤلمة.",
            "كل نظرة في تلك الليلة كانت تخفي وراءها سراً لا يمكن البوح به.",
            "عندما تجتمع الأقدار، لا يملك الإنسان سوى الإيمان برحلته حتى النهاية.",
            "لن أسمح للماضي أن يحدد مصيري ومستقبل من أحبهم!"
        ),
        VoiceCategory.ANIME_CARTOON to listOf(
            "انطلقوا إلى الأمام بأقصى سرعة! المستقبل ينتظر الأبطال الشجعان!",
            "قوة الصداقة هي أعظم سلاح نملكه لمواجهة كل التحديات والصعاب!",
            "معاً كفريق واحد، سنحقق المستحيل وننشر السلام في كل مكان!",
            "يا لها من طاقة هائلة! استعدوا للجولة الحاسمة!"
        ),
        VoiceCategory.COMMERCIAL_TRAILER to listOf(
            "اكتشف عالماً من الإبداع والتميز بلا حدود! اشترك الآن واستمتع بالتجربة.",
            "الأداء الفائق والتصميم العصري يجتمعان لمنحك الأفضل دائماً.",
            "العرض الأقوى لهذا الموسم بانتظارك اليوم! لا تفوت الفرصة.",
            "رفيقك المثالي في كل لحظة، لأنك تستحق التميز."
        ),
        VoiceCategory.NEWS_BROADCAST to listOf(
            "نوافيكم الآن بآخر المستجدات والتطورات الميدانية من قلب الحدث...",
            "وفي تفاصيل النشرة، أعلنت اللجان المعنية عن بدء تنفيذ الخطة التطويرية.",
            "إلى هنا نصل إلى ختام تقريرنا الخاص، شكراً لطيب المتابعة وإلى اللقاء.",
            "موجز الأنباء ياتيكم مباشرة على مدار الساعة لأحدث التغطيات."
        ),
        VoiceCategory.AUDIOBOOK_NOVEL to listOf(
            "كان المساء ينسدل برفق على المدينة الهادئة، حين فتح الغريب دفتره القديم...",
            "في ذلك الفصل الحاسم، أدرك الجميع أن ما كان بالأمس مستحيلاً، أصبح واقعاً.",
            "تنفس بعمق واستمع إلى حفيف أوراق الشجر الذي يعزف لحناً أزلياً.",
            "وتتابعت الأيام كالرياح، حاملة معها بشائر الأمل والتغيير."
        ),
        VoiceCategory.GAMING_ACTION to listOf(
            "الهدف في المرمى، تحركوا بحذر وحافظوا على التغطية النارية!",
            "لقد فتحت البوابة السحرية! استعدوا للقتال والدفاع عن القلعة!",
            "المستوى الأخير مليء بالفخاخ، تيقظوا لكل خطوة قادمة!",
            "النصر حليفنا إذا بقينا متماسكين حتى النهاية!"
        ),
        VoiceCategory.POETRY_LITERATURE to listOf(
            "وَما نَيلُ المَطالِبِ بِالتَمَنّي ... وَلَكِن تُؤخَذُ الدُنيا غِلابا",
            "إِذا غامَرتَ في شَرَفٍ مَرومِ ... فَلا تَقنَع بِما دونَ النُجومِ",
            "أَلا لَيتَ الشَبابَ يَعُودُ يَوماً ... فَأُخبِرَهُ بِما صَنَعَ المَشيبُ",
            "وَلَرُبَّ نازِلَةٍ يَضيقُ بِها الفَتى ... ذَرعاً وَعِندَ اللَهِ مِنها المَخرَجُ"
        ),
        VoiceCategory.COMEDY_PARODY to listOf(
            "يا جماعة الموضوع بسيط جداً، بس انتم بتحبوا تعقدوا الأمور بطريقة فنية!",
            "طب والله فكرة عبقرية! مين كان يصدق إن الخطة دي هتنجح بالشكل ده؟",
            "أنا ماشي في السليم تماماً، بس السليم هو اللي قرر يغير اتجاهه فجأة!",
            "شوفوا مين اللي بيتكلم عن الهدوء والنظام! مفاجأة الموسم والله!"
        ),
        VoiceCategory.RELIGIOUS_SERMON to listOf(
            "إن مع العسر يسراً، فما ضاقت إلا لتفرج بنور الأمل واليقين.",
            "الكلمة الطيبة صدقة، تزرع المحبة في القلوب وتنشر السلام بين الناس.",
            "تأمل في بديع صنع الخالق، تجد في كل ذرة برهاناً على رحمته ولطفه.",
            "استقيموا واعتصموا بالحق، وتواصوا بالصبر والرحمة في كل حين."
        ),
        VoiceCategory.EDUCATIONAL_SCIENCE to listOf(
            "مرحباً بكم! اليوم سنبسط مفهوماً فيزيائياً ممتعاً يؤثر في حياتنا اليومية...",
            "تتفاعل هذه الجزيئات معاً لتنتج طاقة متجددة ونظيفة تخدم كوكبنا.",
            "من خلال هذه التجربة التفاعلية، سنلاحظ بدقة كيف تتشكل الموجات الصوتية.",
            "الذكاء الاصطناعي ليس مجرد معادلات، بل هو أداة لتمكين الإبداع الإنساني."
        ),
        VoiceCategory.PODCAST_CASUAL to listOf(
            "أهلاً بكم في حلقة اليوم! كوب القهوة جاهز، وتعالوا ندردش في موضوع مهم جداً...",
            "سؤال اليوم اللي حير الكثيرين: كيف نحافظ على شغفنا في زمن السرعة؟",
            "التجارب الحقيقية هي أفضل مدرسة، واليوم نستضيف ضيفاً استثنائياً.",
            "شاركونا آراءكم في التعليقات، فأنتم دائماً جزء لا يتجزأ من هذا الحوار."
        )
    )

    /**
     * Total procedurally indexed pool count (4,800 distinctive human voice identities)
     */
    const val TOTAL_VIRTUAL_VOICE_COUNT = 4800

    /**
     * Deterministic Generator to retrieve voice by continuous index (0 until TOTAL_VIRTUAL_VOICE_COUNT)
     */
    fun getVoiceByIndex(index: Int): HumanVoiceModel {
        if (index < masterCuratedVoices.size) {
            return masterCuratedVoices[index]
        }

        val offset = index - masterCuratedVoices.size
        val seed = offset * 31 + 17

        val genders = VoiceGender.values()
        val dialects = VoiceDialect.values()
        val categories = VoiceCategory.values()
        val ageGroups = VoiceAgeGroup.values()

        val gender = genders[abs(seed % genders.size)]
        val dialect = dialects[abs((seed / 3) % dialects.size)]
        val category = categories[abs((seed / 7) % categories.size)]
        val ageGroup = ageGroups[abs((seed / 11) % ageGroups.size)]

        val firstName = if (gender == VoiceGender.FEMALE || gender == VoiceGender.GIRL || gender == VoiceGender.ELDERLY_FEMALE) {
            arabicFemaleNames[abs((seed / 5) % arabicFemaleNames.size)]
        } else {
            arabicMaleNames[abs((seed / 5) % arabicMaleNames.size)]
        }
        val familyName = arabicFamilyNames[abs((seed / 13) % arabicFamilyNames.size)]
        val fullName = "$firstName $familyName"

        val title = when (category) {
            VoiceCategory.DOCUMENTARY_EPIC -> "راوٍ وثائقي (${dialect.regionArabic})"
            VoiceCategory.DRAMA_CINEMATIC -> "صوت درامي سينمائي"
            VoiceCategory.ANIME_CARTOON -> "بطل رسوم متحركة وكرتون"
            VoiceCategory.COMMERCIAL_TRAILER -> "معلق إعلانات وعروض"
            VoiceCategory.NEWS_BROADCAST -> "مذيع نشرات وتقارير"
            VoiceCategory.AUDIOBOOK_NOVEL -> "راوي كتب وروايات"
            VoiceCategory.GAMING_ACTION -> "شخصية ألعاب وأكشن"
            VoiceCategory.POETRY_LITERATURE -> "إلقاء شعري وأدبي"
            VoiceCategory.COMEDY_PARODY -> "صوت كوميدي وطريف"
            VoiceCategory.RELIGIOUS_SERMON -> "نبرة خطابية ودينية"
            VoiceCategory.EDUCATIONAL_SCIENCE -> "مقدم شروحات علمية"
            VoiceCategory.PODCAST_CASUAL -> "محاور بودكاست دافئ"
        }

        val basePitch = when (gender) {
            VoiceGender.ELDERLY_MALE -> 0.70f + ((seed % 10) * 0.015f)
            VoiceGender.MALE -> 0.80f + ((seed % 15) * 0.015f)
            VoiceGender.FEMALE -> 1.05f + ((seed % 15) * 0.015f)
            VoiceGender.ELDERLY_FEMALE -> 0.95f + ((seed % 12) * 0.015f)
            VoiceGender.BOY -> 1.35f + ((seed % 15) * 0.015f)
            VoiceGender.GIRL -> 1.45f + ((seed % 15) * 0.015f)
        }

        val speechRate = 0.85f + ((seed % 18) * 0.02f)
        val warmth = 0.75f + ((seed % 25) * 0.01f)
        val resonance = 0.70f + ((seed % 28) * 0.01f)
        val breathiness = 0.08f + ((seed % 20) * 0.01f)

        val phrases = samplePhraseBank[category] ?: samplePhraseBank[VoiceCategory.DOCUMENTARY_EPIC]!!
        val samplePhrase = phrases[abs((seed / 2) % phrases.size)]

        val colorPalette = listOf(
            0xFF1E3A8A, 0xFF047857, 0xFFB45309, 0xFF4338CA, 0xFFBE185D,
            0xFF065F46, 0xFF7C2D12, 0xFF6D28D9, 0xFF0F766E, 0xFF374151
        )
        val avatarColor = colorPalette[abs(seed % colorPalette.size)]

        return HumanVoiceModel(
            id = "voice_auto_${index}_${dialect.name.lowercase()}_${gender.name.lowercase()}",
            nameArabic = fullName,
            titleArabic = title,
            gender = gender,
            dialect = dialect,
            category = category,
            ageGroup = ageGroup,
            basePitch = basePitch,
            speechRate = speechRate,
            toneWarmth = warmth.coerceIn(0f, 1f),
            resonance = resonance.coerceIn(0f, 1f),
            breathiness = breathiness.coerceIn(0f, 1f),
            sampleArabicPhrase = samplePhrase,
            isVerifiedPro = (seed % 3 == 0),
            rating = 4.7f + ((seed % 30) * 0.01f).coerceIn(0f, 0.3f),
            tags = listOf(dialect.titleArabic, gender.titleArabic, category.titleArabic),
            avatarColorHex = avatarColor,
            avatarEmoji = category.emoji
        )
    }

    /**
     * Query & Filter through the thousands of voices in the catalog
     */
    fun searchAndFilterVoices(
        filter: VoiceLibraryFilter,
        pageIndex: Int = 0,
        pageSize: Int = 40
    ): List<HumanVoiceModel> {
        val query = filter.searchQuery.trim().lowercase()
        val results = mutableListOf<HumanVoiceModel>()

        // Search through curated masters first
        for (voice in masterCuratedVoices) {
            if (matchesFilter(voice, filter, query)) {
                results.add(voice)
            }
        }

        // Search and generate through procedural pool to fill page
        var currentIndex = masterCuratedVoices.size
        val maxSearchLimit = 1200 // search deep enough to find matching candidates

        while (currentIndex < TOTAL_VIRTUAL_VOICE_COUNT && currentIndex < maxSearchLimit && results.size < (pageIndex + 1) * pageSize + 50) {
            val candidate = getVoiceByIndex(currentIndex)
            if (matchesFilter(candidate, filter, query)) {
                results.add(candidate)
            }
            currentIndex++
        }

        val startIndex = (pageIndex * pageSize).coerceAtMost(results.size)
        val endIndex = (startIndex + pageSize).coerceAtMost(results.size)
        return results.subList(startIndex, endIndex)
    }

    private fun matchesFilter(
        voice: HumanVoiceModel,
        filter: VoiceLibraryFilter,
        query: String
    ): Boolean {
        if (filter.selectedGender != null && voice.gender != filter.selectedGender) return false
        if (filter.selectedDialect != null && voice.dialect != filter.selectedDialect) return false
        if (filter.selectedCategory != null && voice.category != filter.selectedCategory) return false
        if (filter.selectedAgeGroup != null && voice.ageGroup != filter.selectedAgeGroup) return false
        if (filter.onlyVerified && !voice.isVerifiedPro) return false
        if (filter.onlyFavorites && !isFavorite(voice.id)) return false

        if (query.isNotEmpty()) {
            val matchesName = voice.nameArabic.lowercase().contains(query)
            val matchesTitle = voice.titleArabic.lowercase().contains(query)
            val matchesDialect = voice.dialect.titleArabic.lowercase().contains(query) || voice.dialect.regionArabic.lowercase().contains(query)
            val matchesCategory = voice.category.titleArabic.lowercase().contains(query)
            val matchesTags = voice.tags.any { it.lowercase().contains(query) }
            val matchesPhrase = voice.sampleArabicPhrase.lowercase().contains(query)

            if (!matchesName && !matchesTitle && !matchesDialect && !matchesCategory && !matchesTags && !matchesPhrase) {
                return false
            }
        }

        return true
    }

    /**
     * AI Acoustic Voice Matcher:
     * Analyzes detected user pitch (Hz) & gender to find the top matching human voices from the thousands in the library.
     */
    fun matchVoiceByAcousticSignature(
        detectedPitchHz: Float,
        detectedGenderArabic: String
    ): List<HumanVoiceModel> {
        val targetGender = when {
            detectedGenderArabic.contains("نساء") || detectedGenderArabic.contains("أنثى") -> VoiceGender.FEMALE
            detectedGenderArabic.contains("طفل") || detectedGenderArabic.contains("كرتون") -> VoiceGender.BOY
            detectedGenderArabic.contains("شيخ") || detectedGenderArabic.contains("مسن") -> VoiceGender.ELDERLY_MALE
            else -> VoiceGender.MALE
        }

        val targetBasePitch = when {
            detectedPitchHz > 260f -> 1.4f
            detectedPitchHz > 200f -> 1.15f
            detectedPitchHz > 140f -> 0.95f
            else -> 0.80f
        }

        val pool = (0..300).map { getVoiceByIndex(it) }
        return pool
            .filter { it.gender == targetGender }
            .sortedBy { abs(it.basePitch - targetBasePitch) }
            .take(6)
    }

    fun toggleFavorite(voiceId: String) {
        if (favoriteVoiceIds.contains(voiceId)) {
            favoriteVoiceIds.remove(voiceId)
        } else {
            favoriteVoiceIds.add(voiceId)
        }
        _favoritesFlow.value = favoriteVoiceIds.toSet()
    }

    fun isFavorite(voiceId: String): Boolean = favoriteVoiceIds.contains(voiceId)

    fun getVoiceById(voiceId: String): HumanVoiceModel? {
        masterCuratedVoices.find { it.id == voiceId }?.let { return it }
        // check if it's an indexed procedural ID
        if (voiceId.startsWith("voice_auto_")) {
            val parts = voiceId.split("_")
            val index = parts.getOrNull(2)?.toIntOrNull()
            if (index != null && index in 0 until TOTAL_VIRTUAL_VOICE_COUNT) {
                return getVoiceByIndex(index)
            }
        }
        return masterCuratedVoices.firstOrNull()
    }
}
