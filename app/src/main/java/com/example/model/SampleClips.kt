package com.example.model

data class DubbingClip(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "كرتون", "وثائقي", "خيال علمي", "كوميدي", "رياضة", "مستورد"
    val durationSeconds: Int,
    val coverEmoji: String,
    val primaryColor: Long,
    val scriptLines: List<ScriptLine>,
    val videoUri: String? = null,
    val isImportedVideo: Boolean = false,
    val thumbnailPath: String? = null
)

object SampleClipsRepository {
    val clips: List<DubbingClip> = listOf(
        DubbingClip(
            id = "anime_full_episode",
            title = "حلقة أنمي كاملة (٢٢ دقيقة): معركة كوكب الأمل",
            description = "حلقة أنمي ياباني كاملة مدبلجة على طريقة سبيستون ومركز الزهرة مع حوارات متعددة وفصول قتالية متتالية.",
            category = "أنمي ياباني كامل",
            durationSeconds = 1320, // 22 minutes
            coverEmoji = "⚔️🎬",
            primaryColor = 0xFF6366F1,
            scriptLines = listOf(
                ScriptLine(
                    id = "ep_1",
                    characterName = "بطل الأنمي (حسام)",
                    characterAvatar = "🦸",
                    textArabic = "أيها الأصدقاء، لقد حانت لحظة الحسم.. لن نسمح لقوى الظلام بالسيطرة على مستقبلنا!",
                    textOriginal = "Friends, the decisive moment has arrived.. we will not allow darkness to take our future!",
                    startSeconds = 1.0f,
                    endSeconds = 7.0f,
                    voiceType = "SPACETOON_HERO"
                ),
                ScriptLine(
                    id = "ep_2",
                    characterName = "المنافس الغامض (كاي)",
                    characterAvatar = "🦹",
                    textArabic = "هاهاها! مهما بلغت شجاعتكم، فإن حصن الظلال عصي على الاختراق!",
                    textOriginal = "Hahaha! No matter your courage, the shadow fortress cannot be breached!",
                    startSeconds = 8.0f,
                    endSeconds = 14.0f,
                    voiceType = "DRAMATIC"
                ),
                ScriptLine(
                    id = "ep_3",
                    characterName = "البطلة (سلمى)",
                    characterAvatar = "🌸",
                    textArabic = "اتحدوا يا أبطال! درع الأمل يتوهج بطاقة الصداقة والمحبة النقية!",
                    textOriginal = "Unite heroes! The shield of hope glows with pure friendship energy!",
                    startSeconds = 15.0f,
                    endSeconds = 21.0f,
                    voiceType = "SPACETOON_HEROINE"
                ),
                ScriptLine(
                    id = "ep_4",
                    characterName = "راوي سبيستون الأسطوري",
                    characterAvatar = "🌟",
                    textArabic = "وهكذا تشتعل المعركة الكبرى.. هل سينجح أبطالنا في إنقاذ الكوكب؟ تابعوا معنا!",
                    textOriginal = "And so the grand battle blazes.. will our heroes save the planet? Stay tuned!",
                    startSeconds = 22.0f,
                    endSeconds = 28.0f,
                    voiceType = "SPACETOON_NARRATOR"
                )
            )
        ),
        DubbingClip(
            id = "kdrama_full_hour",
            title = "فيلم ودراما كاملة (٦٠ دقيقة / ساعة): أسرار قصر سيول",
            description = "عمل درامي كوري كامل مدبلج إلى الفصحى الراقية مع تقطيع آلي ذكي وتوليد حوارات متناغمة لكامل الفيلم.",
            category = "دراما كورية ساعة",
            durationSeconds = 3600, // 60 minutes (1 Hour)
            coverEmoji = "🇰🇷👑",
            primaryColor = 0xFFEC4899,
            scriptLines = listOf(
                ScriptLine(
                    id = "kfilm_1",
                    characterName = "الوزير (مين هو)",
                    characterAvatar = "👨‍💼",
                    textArabic = "في هذا القصر العتيق، كل جدار يخبئ سراً دفيناً حان وقت كشفه للعالم.",
                    textOriginal = "In this ancient palace, every wall conceals a deep secret waiting to be revealed.",
                    startSeconds = 1.0f,
                    endSeconds = 7.5f,
                    voiceType = "ARABIC_MALE"
                ),
                ScriptLine(
                    id = "kfilm_2",
                    characterName = "الأميرة (يون سو)",
                    characterAvatar = "👩‍💼",
                    textArabic = "لقد عشت طويلاً أبحث عن الحقيقة، ولن أتراجع مهما كلفني الثمن!",
                    textOriginal = "I lived for so long searching for the truth, and I will never back down whatever the cost!",
                    startSeconds = 8.5f,
                    endSeconds = 15.0f,
                    voiceType = "SPACETOON_HEROINE"
                ),
                ScriptLine(
                    id = "kfilm_3",
                    characterName = "الوزير (مين هو)",
                    characterAvatar = "👨‍💼",
                    textArabic = "سأكون درعكِ وحليفكِ المخلص.. معاً سنعيد العدالة إلى هذا العرش.",
                    textOriginal = "I shall be your shield and loyal ally.. together we will restore justice to this throne.",
                    startSeconds = 16.0f,
                    endSeconds = 23.5f,
                    voiceType = "ARABIC_MALE"
                )
            )
        ),
        DubbingClip(
            id = "anime_spacetoon_hero",
            title = "أنمي الأبطال: عزم شباب المستقبل (سبيستون)",
            description = "مواجهة أنمي ياباني أسطورية على طريقة مركز الزهرة وسبيستون: طاقة الإرادة وهزيمة قوى الظلام.",
            category = "أنمي ياباني",
            durationSeconds = 27,
            coverEmoji = "⚔️🌟",
            primaryColor = 0xFF8B5CF6,
            scriptLines = listOf(
                ScriptLine(
                    id = "spacetoon_1",
                    characterName = "بطل الأنمي (حسام)",
                    characterAvatar = "🦸",
                    textArabic = "لن أسمح لكم بنشر الظلام في كوكبنا.. طاقة الرياح الصاعقة، انطلقي!",
                    textOriginal = "I will not let darkness take over our world.. Thunder wind energy, unleash!",
                    startSeconds = 1.0f,
                    endSeconds = 7.0f,
                    voiceType = "SPACETOON_HERO"
                ),
                ScriptLine(
                    id = "spacetoon_2",
                    characterName = "المنافس الغامض (كاي)",
                    characterAvatar = "🦹",
                    textArabic = "هاهاها! مهما حاولت يا حسام، قوتك وحدك لن تكفي للتصدي لدرع الظلال!",
                    textOriginal = "Hahaha! No matter how hard you try Hussam, your power alone is not enough!",
                    startSeconds = 8.0f,
                    endSeconds = 14.5f,
                    voiceType = "DRAMATIC"
                ),
                ScriptLine(
                    id = "spacetoon_3",
                    characterName = "البطلة (سلمى)",
                    characterAvatar = "🌸",
                    textArabic = "أنت لست وحدك يا حسام! قلوبنا وعزيمتنا تتحد معاً لتحقيق النصر!",
                    textOriginal = "You are not alone Hussam! Our hearts and will unite for victory!",
                    startSeconds = 15.5f,
                    endSeconds = 21.0f,
                    voiceType = "SPACETOON_HEROINE"
                ),
                ScriptLine(
                    id = "spacetoon_4",
                    characterName = "راوي سبيستون الأسطوري",
                    characterAvatar = "🌟",
                    textArabic = "وهكذا يثبت الأبطال أن الصداقة والإرادة الصادقة تصنع المعجزات دائماً!",
                    textOriginal = "And so our heroes prove that true friendship and will always create miracles!",
                    startSeconds = 22.0f,
                    endSeconds = 26.5f,
                    voiceType = "SPACETOON_NARRATOR"
                )
            )
        ),
        DubbingClip(
            id = "korean_drama_seoul",
            title = "دراما سيول: حكاية لقاء تحت المطر (K-Drama)",
            description = "مشهد كوري درامي مشحون بالمشاعر والاعتراف الصادق مدبلج بالعربية الفصحى السينمائية.",
            category = "دراما كورية",
            durationSeconds = 26,
            coverEmoji = "🇰🇷🌧️",
            primaryColor = 0xFFEC4899,
            scriptLines = listOf(
                ScriptLine(
                    id = "kdrama_1",
                    characterName = "مين هو (البطل)",
                    characterAvatar = "👨‍💼",
                    textArabic = "أرجوكِ انتطري ولا تذهبي.. لم أكن أعلم أن هذا اللقاء سيكون الأخير بيننا.",
                    textOriginal = "Please wait, don't leave.. I never knew this encounter would be our last.",
                    startSeconds = 1.0f,
                    endSeconds = 7.5f,
                    voiceType = "ARABIC_MALE"
                ),
                ScriptLine(
                    id = "kdrama_2",
                    characterName = "يون سو (البطلة)",
                    characterAvatar = "👩‍💼",
                    textArabic = "لقد حاولت كثيراً أن أنسى كل الوعود التي قطعناها تحت هذا المطر، لكن قلبي عاجز!",
                    textOriginal = "I tried so hard to forget all the promises we made in the rain, but my heart failed!",
                    startSeconds = 8.5f,
                    endSeconds = 16.5f,
                    voiceType = "SPACETOON_HEROINE"
                ),
                ScriptLine(
                    id = "kdrama_3",
                    characterName = "مين هو (البطل)",
                    characterAvatar = "👨‍💼",
                    textArabic = "إذن لن نبتعد مجدداً.. مهما اشتدت العواصف، سأظل بجانبكِ ولن أترك يدكِ أبداً!",
                    textOriginal = "Then we shall never part again.. no matter the storms, I will stay and never let go!",
                    startSeconds = 17.5f,
                    endSeconds = 25.5f,
                    voiceType = "ARABIC_MALE"
                )
            )
        ),
        DubbingClip(
            id = "cartoon_cat_bunny",
            title = "مغامرة القط بندق والأرنب سمسم",
            description = "مشهد كرتوني كوميدي بين قط محتال وأرنب ذكي في الغابة السحرية.",
            category = "كرتون",
            durationSeconds = 24,
            coverEmoji = "🐱🐰",
            primaryColor = 0xFF8B5CF6,
            scriptLines = listOf(
                ScriptLine(
                    id = "line_1",
                    characterName = "القط بندق",
                    characterAvatar = "🐱",
                    textArabic = "هاهاها! لن تستطيع الإمساك بهذه الجزرة الذهبية قبلي!",
                    textOriginal = "Hahaha! You will never catch this golden carrot before me!",
                    startSeconds = 1.0f,
                    endSeconds = 5.0f,
                    voiceType = "CARTOON"
                ),
                ScriptLine(
                    id = "line_2",
                    characterName = "الأرنب سمسم",
                    characterAvatar = "🐰",
                    textArabic = "انتظر فقط يا بندق! لدي خطة سرية وسأصل أولاً!",
                    textOriginal = "Just wait Bunduq! I have a secret plan and I will reach first!",
                    startSeconds = 5.5f,
                    endSeconds = 10.0f,
                    voiceType = "CARTOON"
                ),
                ScriptLine(
                    id = "line_3",
                    characterName = "القط بندق",
                    characterAvatar = "🐱",
                    textArabic = "يا إلهي، ما هذا الفخ السحري؟! لقد علقت قدمي!",
                    textOriginal = "Oh no, what magic trap is this?! My foot is stuck!",
                    startSeconds = 11.0f,
                    endSeconds = 16.0f,
                    voiceType = "CARTOON"
                ),
                ScriptLine(
                    id = "line_4",
                    characterName = "الأرنب سمسم",
                    characterAvatar = "🐰",
                    textArabic = "أخبرتك يا صديقي.. الذكاء دائماً يغلب السرعة والمكر!",
                    textOriginal = "I told you friend.. intelligence always beats speed and cunning!",
                    startSeconds = 17.0f,
                    endSeconds = 23.0f,
                    voiceType = "CARTOON"
                )
            )
        ),
        DubbingClip(
            id = "nature_lion",
            title = "أسرار السافانا: ملك الغابة",
            description = "مقطع وثائقي مهيب عن حياة الأسود في سهول السافانا الإفريقية.",
            category = "وثائقي",
            durationSeconds = 28,
            coverEmoji = "🦁🌿",
            primaryColor = 0xFFD97706,
            scriptLines = listOf(
                ScriptLine(
                    id = "doc_1",
                    characterName = "المعلّق الوثائقي",
                    characterAvatar = "🎙️",
                    textArabic = "مع شروق شمس إفريقيا الحارقة، يستيقظ ملك الغابة لمراقبة مملكته الشاسعة.",
                    textOriginal = "As the scorching African sun rises, the king of the jungle awakens to watch over his vast kingdom.",
                    startSeconds = 1.0f,
                    endSeconds = 7.5f,
                    voiceType = "DRAMATIC"
                ),
                ScriptLine(
                    id = "doc_2",
                    characterName = "المعلّق الوثائقي",
                    characterAvatar = "🎙️",
                    textArabic = "خطوات واثقة ونظرات حادة ترصد كل حركة عبر الأعشاب الذهبية.",
                    textOriginal = "Confident strides and sharp gaze track every single movement through the golden grass.",
                    startSeconds = 8.5f,
                    endSeconds = 14.5f,
                    voiceType = "DRAMATIC"
                ),
                ScriptLine(
                    id = "doc_3",
                    characterName = "المعلّق الوثائقي",
                    characterAvatar = "🎙️",
                    textArabic = "هنا في قلب البرية، البقاء للأقوى.. والهدوء الذي يسبق العاصفة.",
                    textOriginal = "Here in the heart of the wild, survival belongs to the strongest.. the calm before the storm.",
                    startSeconds = 15.5f,
                    endSeconds = 26.0f,
                    voiceType = "DRAMATIC"
                )
            )
        ),
        DubbingClip(
            id = "scifi_space",
            title = "الرحلة إلى المجهول: مدار المريخ",
            description = "مشهد خيال علمي سينمائي لاتصال رواد الفضاء بمركز القيادة الأرضي.",
            category = "خيال علمي",
            durationSeconds = 25,
            coverEmoji = "🚀🌌",
            primaryColor = 0xFF06B6D4,
            scriptLines = listOf(
                ScriptLine(
                    id = "sci_1",
                    characterName = "القائد طارق",
                    characterAvatar = "👨‍🚀",
                    textArabic = "قاعدة الأرض، هل تسمعونني؟ لقد رصدنا إشارة غريبة قادمة من الكهف المريخي!",
                    textOriginal = "Ground base, do you copy? We have detected an unusual signal from the Martian cavern!",
                    startSeconds = 1.0f,
                    endSeconds = 7.0f,
                    voiceType = "ARABIC_MALE"
                ),
                ScriptLine(
                    id = "sci_2",
                    characterName = "مركز التحكم",
                    characterAvatar = "📡",
                    textArabic = "نسمعك بوضوح يا قبطان.. توخّ الحذر ولا تقترب حتى نقوم بتحليل التردد!",
                    textOriginal = "We hear you clearly Captain.. proceed with caution and do not approach until frequency analysis is complete!",
                    startSeconds = 8.0f,
                    endSeconds = 14.5f,
                    voiceType = "ARABIC_FEMALE"
                ),
                ScriptLine(
                    id = "sci_3",
                    characterName = "القائد طارق",
                    characterAvatar = "👨‍🚀",
                    textArabic = "الأمر مذهل للغاية.. الأضواء تنبض بنمط متناسق، يبدو أنه جهاز كوني قديم!",
                    textOriginal = "It is breathtaking.. the lights are pulsing rhythmically, looks like an ancient cosmic device!",
                    startSeconds = 15.5f,
                    endSeconds = 24.0f,
                    voiceType = "ARABIC_MALE"
                )
            )
        ),
        DubbingClip(
            id = "comedy_chef",
            title = "الشيف المتهور: أشهى بيتزا في العالم",
            description = "مشهد طبخ كوميدي مرح حيث يخرج العجين عن السيطرة في المطبخ.",
            category = "كوميدي",
            durationSeconds = 22,
            coverEmoji = "🍕👨‍🍳",
            primaryColor = 0xFFEF4444,
            scriptLines = listOf(
                ScriptLine(
                    id = "chef_1",
                    characterName = "الشيف ماريو",
                    characterAvatar = "👨‍🍳",
                    textArabic = "مرحباً بكم يا عشاق الطعام! اليوم سنصنع أكبر قطعة بيتزا في التاريخ!",
                    textOriginal = "Welcome food lovers! Today we will make the biggest pizza slice in history!",
                    startSeconds = 1.0f,
                    endSeconds = 6.0f,
                    voiceType = "ENTHUSIASTIC"
                ),
                ScriptLine(
                    id = "chef_2",
                    characterName = "الشيف ماريو",
                    characterAvatar = "👨‍🍳",
                    textArabic = "انظروا إلى هذه الرمية الهوائية الاحترافية.. واحد، اثنان، ثلااااثة!",
                    textOriginal = "Look at this professional aerial dough toss.. one, two, threeee!",
                    startSeconds = 7.0f,
                    endSeconds = 12.0f,
                    voiceType = "ENTHUSIASTIC"
                ),
                ScriptLine(
                    id = "chef_3",
                    characterName = "الشيف ماريو",
                    characterAvatar = "👨‍🍳",
                    textArabic = "وااااه! لقد التصقت العجينة بالسقف وسقطت على رأسي مباشرة!",
                    textOriginal = "Waaah! The dough got stuck to the ceiling and fell right onto my head!",
                    startSeconds = 13.0f,
                    endSeconds = 20.0f,
                    voiceType = "CARTOON"
                )
            )
        ),
        DubbingClip(
            id = "football_match",
            title = "الدقيقة 90: هدف الفوز الأسطوري",
            description = "تعليق رياضي حماسي ومثير في اللحظات الأخيرة من المباراة النهائية.",
            category = "رياضة",
            durationSeconds = 26,
            coverEmoji = "⚽🔥",
            primaryColor = 0xFF10B981,
            scriptLines = listOf(
                ScriptLine(
                    id = "foot_1",
                    characterName = "المعلق الرياضي",
                    characterAvatar = "🎙️",
                    textArabic = "الكرة الآن في منتصف الملعب، تمريرة ساحرة بين المدافعين وانفراد تام!",
                    textOriginal = "Ball is now in midfield, magical through pass between defenders and a clean breakaway!",
                    startSeconds = 1.0f,
                    endSeconds = 7.0f,
                    voiceType = "ENTHUSIASTIC"
                ),
                ScriptLine(
                    id = "foot_2",
                    characterName = "المعلق الرياضي",
                    characterAvatar = "🎙️",
                    textArabic = "يسدد بقوة لا تصدق في الزاوية التسعين.. يا رباااااااه! هدف خيالي!",
                    textOriginal = "He strikes with unbelievable power into the top corner.. Oh my goodness! A fantastical goal!",
                    startSeconds = 8.0f,
                    endSeconds = 16.0f,
                    voiceType = "ENTHUSIASTIC"
                ),
                ScriptLine(
                    id = "foot_3",
                    characterName = "المعلق الرياضي",
                    characterAvatar = "🎙️",
                    textArabic = "الملعب يشتعل جنوناً واحتفالات لا تتوقف! هدف سيكتبه التاريخ بحروف من ذهب!",
                    textOriginal = "The stadium erupts into pure frenzy! A goal that history will write in letters of gold!",
                    startSeconds = 17.0f,
                    endSeconds = 25.0f,
                    voiceType = "ENTHUSIASTIC"
                )
            )
        ),
        DubbingClip(
            id = "spacetoon_full_episode",
            title = "حلقة أنمي كاملة (٢٢ دقيقة) - معركة حماة الكوكب",
            description = "حلقة أنمي سبيستون ومركز الزهرة ملحمية كاملة (1320 ثانية) مع حوارات متعددة الشخصيات وتقسيم تلقائي.",
            category = "أنمي وسبيستون",
            durationSeconds = 1320,
            coverEmoji = "⚡⚔️",
            primaryColor = 0xFF8B5CF6,
            scriptLines = listOf(
                ScriptLine(
                    id = "ep_1",
                    characterName = "راوي سبيستون",
                    characterAvatar = "🎙️",
                    textArabic = "في أعماق الفضاء السحيق، يواصل أبطالنا رحلتهم الأسطورية نحو كوكب الأمل المنشود.",
                    textOriginal = "In the deep void of outer space, our brave heroes continue their legendary journey.",
                    startSeconds = 2.0f,
                    endSeconds = 8.0f,
                    voiceType = "SPACETOON_NARRATOR"
                ),
                ScriptLine(
                    id = "ep_2",
                    characterName = "البطل وسيم",
                    characterAvatar = "⚔️",
                    textArabic = "لن نستسلم أبداً ما دامت قلوبنا تنبض بالحق والشجاعة والإصرار!",
                    textOriginal = "We will never surrender as long as our hearts beat with justice and courage!",
                    startSeconds = 9.0f,
                    endSeconds = 15.0f,
                    voiceType = "SPACETOON_HERO"
                ),
                ScriptLine(
                    id = "ep_3",
                    characterName = "البطلة ريم",
                    characterAvatar = "🛡️",
                    textArabic = "استعدوا جميعاً! إن إشارات دروع الطاقة تعود إلى العمل بكامل كفاءتها!",
                    textOriginal = "Get ready everyone! Energy shield sensors are returning to full power!",
                    startSeconds = 16.0f,
                    endSeconds = 22.0f,
                    voiceType = "SPACETOON_HEROINE"
                )
            )
        ),
        DubbingClip(
            id = "kdrama_full_movie",
            title = "فيلم ودراما كورية كاملة (ساعة كاملة / 60 دقيقة)",
            description = "فيلم دراما كورية كامل (3600 ثانية) بدبلجة سينمائية راقية وتقسيم ذكي متواصل بالخلفية.",
            category = "دراما كورية (K-Drama)",
            durationSeconds = 3600,
            coverEmoji = "🌸🎬",
            primaryColor = 0xFFEC4899,
            scriptLines = listOf(
                ScriptLine(
                    id = "kd_1",
                    characterName = "مين هو (البطل)",
                    characterAvatar = "🤵",
                    textArabic = "كنت أظن أن تلك الذكريات قد طواها النسيان، حتى التقينا مجدداً تحت قطرات المطر.",
                    textOriginal = "I thought those memories were forgotten, until we met again beneath the rain.",
                    startSeconds = 2.0f,
                    endSeconds = 9.0f,
                    voiceType = "DRAMATIC"
                ),
                ScriptLine(
                    id = "kd_2",
                    characterName = "سيو يون (البطلة)",
                    characterAvatar = "👩‍💼",
                    textArabic = "الوقت يغير كل شيء، لكن المشاعر الصادقة لا تبهت أبداً يا مين هو.",
                    textOriginal = "Time changes everything, but genuine feelings never fade away, Min-ho.",
                    startSeconds = 10.0f,
                    endSeconds = 17.0f,
                    voiceType = "DRAMATIC"
                )
            )
        ),
        DubbingClip(
            id = "epic_movie_90min",
            title = "فيلم وثائقي وسينمائي ملحمي (ساعة ونصف / 90 دقيقة)",
            description = "عرض سينمائي طويل جداً (5400 ثانية / ساعة ونصف) مدعوم بنظام الدبلجة المتسلسلة في الخلفية.",
            category = "سينما ووثائقي",
            durationSeconds = 5400,
            coverEmoji = "🌍👑",
            primaryColor = 0xFF3B82F6,
            scriptLines = listOf(
                ScriptLine(
                    id = "doc_1",
                    characterName = "المعلق الوثائقي",
                    characterAvatar = "🎙️",
                    textArabic = "على مر العصور، شهدت هذه الأرض صراعات وإنجازات شكلت ملامح حضارتنا المعاصرة.",
                    textOriginal = "Throughout the ages, this land witnessed struggles and triumphs shaping civilization.",
                    startSeconds = 2.0f,
                    endSeconds = 10.0f,
                    voiceType = "ARABIC_MALE"
                )
            )
        )
    )

    fun getClipById(id: String): DubbingClip {
        return clips.find { it.id == id } ?: clips.first()
    }
}
