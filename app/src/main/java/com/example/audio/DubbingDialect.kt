package com.example.audio

/**
 * Arabic Regional Dialects and Linguistic Registers for Dubbing and Voice Acting.
 */
enum class DubbingDialect(
    val code: String,
    val displayNameArabic: String,
    val nativeRegion: String,
    val flagEmoji: String,
    val descriptionArabic: String,
    val promptInstruction: String,
    val samplePhrase: String
) {
    MODERN_STANDARD_CLASSIC(
        code = "msa_classic",
        displayNameArabic = "الفصحى الكلاسيكية (سبيستون والزهراء)",
        nativeRegion = "العالم العربي / مركز الزهرة",
        flagEmoji = "🌟",
        descriptionArabic = "فصحى درامية أصيلة بنبرة بطولية ومخارج حروف واضحة جداً",
        promptInstruction = "Strict Modern Standard Arabic with dramatic Spacetoon/Venus center classic vocal cadence, heroic diction, and eloquent classical grammar (فصحى سبيستون الكلاسيكية النقية).",
        samplePhrase = "سنبذل كل ما نملك من طاقة لحماية كوكب الأمل!"
    ),
    MODERN_STANDARD_CONTEMPORARY(
        code = "msa_contemporary",
        displayNameArabic = "الفصحى السينمائية المعاصرة",
        nativeRegion = "العالم العربي / منصات البث",
        flagEmoji = "🎬",
        descriptionArabic = "فصحى ميسرة وأنيقة تستخدم في الأفلام والمسلسلات العالمية المترجمة والمدبلجة",
        promptInstruction = "Contemporary Modern Standard Arabic (فصحى سينمائية حديثة), fluent, realistic, natural flow, and cinematic dialogues suitable for modern international dramas and streaming shows.",
        samplePhrase = "لم أكن أعلم أن هذا اللقاء سيكون بداية لكل شيء."
    ),
    EGYPTIAN(
        code = "egyptian",
        displayNameArabic = "اللهجة المصرية السينمائية",
        nativeRegion = "مصر (الدراما والسينما والكرتون)",
        flagEmoji = "🇪🇬",
        descriptionArabic = "اللهجة المصرية المحبوبة لرسوم الكرتون وأفلام الأنيميشن والكوميديا والدراما",
        promptInstruction = "Authentic Egyptian Arabic (اللهجة المصرية السينمائية الكوميدية والدرامية), natural, expressive, witty, and rich in beloved animated movie expressions (مثل دبلجة ديزني الكلاسيكية المصرية).",
        samplePhrase = "إيه ده يا عم؟ إحنا لازم نتحرك بسرعة قبل ما الوقت يفوت!"
    ),
    LEVANTINE_SYRIAN(
        code = "levantine",
        displayNameArabic = "اللهجة الشامية / السورية",
        nativeRegion = "بلاد الشام (سوريا ولبنان)",
        flagEmoji = "🇸🇾",
        descriptionArabic = "اللهجة الشامية العريقة المستخدمة في الدراما والمسلسلات الاجتماعية الشهيرة",
        promptInstruction = "Natural Levantine / Syrian Arabic (اللهجة الشامية السورية واللبنانية), smooth, heartfelt, emotionally resonant, and authentic colloquial expressions for modern dramas.",
        samplePhrase = "لك شو صار معك هنيك؟ طمني والله قلبي عم يوجعني عليك."
    ),
    GULF_KHALIJI(
        code = "gulf",
        displayNameArabic = "اللهجة الخليجية",
        nativeRegion = "الخليج العربي",
        flagEmoji = "🇸🇦",
        descriptionArabic = "اللهجة الخليجية الأصيلة بمفرداتها المتميزة وإيقاعها الوقور",
        promptInstruction = "Authentic Gulf / Khaleeji Arabic (اللهجة الخليجية الأصيلة), natural expressions, polite and resonant intonation suitable for contemporary regional cinema and narratives.",
        samplePhrase = "يا خوي لا تشيل هم، كل شي بيترتب بإذن الله والموضوع هين."
    ),
    MAGHREBI(
        code = "maghrebi",
        displayNameArabic = "اللهجة المغاربية البيضاء",
        nativeRegion = "المغرب العربي",
        flagEmoji = "🇲🇦",
        descriptionArabic = "لهجة مغاربية سلسة ومفهومة للبرامج والإنتاجات المغاربية",
        promptInstruction = "Accessible Maghrebi Arabic (لهجة مغاربية بيضاء وسلسة), culturally attuned with clear colloquial expressions.",
        samplePhrase = "صافي يا خويا دابا غادي نبداو كلشي من الأول ونخدمو مزيان."
    ),
    IRAQI(
        code = "iraqi",
        displayNameArabic = "اللهجة العراقية",
        nativeRegion = "العراق",
        flagEmoji = "🇮🇶",
        descriptionArabic = "اللهجة العراقية المشحونة بالشجن والعمق التعبيري للمشاهد القوية",
        promptInstruction = "Authentic Iraqi Arabic (اللهجة العراقية الأصيلة), deeply expressive, warm, and resonant colloquial dialogue.",
        samplePhrase = "حبيبي شكو ماكو؟ صدگني ما راح أعوفك وحدك بهالظرف."
    )
}
