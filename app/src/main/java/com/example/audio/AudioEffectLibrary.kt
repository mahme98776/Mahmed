package com.example.audio

import kotlin.math.roundToInt

enum class AudioEffectCategory(
    val id: String,
    val titleArabic: String,
    val iconEmoji: String
) {
    ALL("ALL", "الكل", "✨"),
    REVERB_SPACE("REVERB_SPACE", "الصدى والمكان", "🏛️"),
    PITCH_VOICE("PITCH_VOICE", "طبقات الصوت", "🎚️"),
    SCI_FI_ROBOT("SCI_FI_ROBOT", "روبوت وفضاء", "🤖"),
    VINTAGE_FILTERS("VINTAGE_FILTERS", "فلاتر ومؤثرات", "📻")
}

data class AudioEffectParameters(
    val pitchSemitones: Int = 0, // -12 to +12 semitones
    val pitchMultiplier: Float = 1.0f, // 0.5f to 2.0f
    val speedMultiplier: Float = 1.0f, // 0.5f to 2.0f
    val reverbRoomSize: Float = 0.0f, // 0.0f to 1.0f (Small room to Cathedral)
    val reverbDecay: Float = 1.0f, // 0.1s to 5.0s
    val echoDelayMs: Int = 0, // 0 to 1000ms
    val echoFeedback: Float = 0.0f, // 0.0f to 0.9f
    val robotModulationHz: Float = 0.0f, // 0 to 300Hz (0 = off)
    val robotResonance: Float = 0.5f, // 0.0f to 1.0f
    val distortionDrive: Float = 0.0f, // 0.0f to 1.0f
    val lowPassCutoffHz: Float = 20000f, // 500Hz to 20000Hz
    val highPassCutoffHz: Float = 20f, // 20Hz to 2000Hz
    val chorusDepth: Float = 0.0f, // 0.0f to 1.0f
    val dryWetMix: Float = 1.0f // 0.0f (pure dry) to 1.0f (full wet)
) {
    fun getPitchFormatted(): String {
        return when {
            pitchSemitones > 0 -> "+$pitchSemitones نصف نغمة (${String.format("%.2f", pitchMultiplier)}x)"
            pitchSemitones < 0 -> "$pitchSemitones نصف نغمة (${String.format("%.2f", pitchMultiplier)}x)"
            else -> "طبيعي (1.0x)"
        }
    }
}

data class AudioEffectItem(
    val id: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val descriptionArabic: String,
    val category: AudioEffectCategory,
    val iconEmoji: String,
    val defaultParams: AudioEffectParameters,
    val tags: List<String> = emptyList()
)

object AudioEffectsLibrary {

    val effects: List<AudioEffectItem> = listOf(
        // 1. Natural / Bypass
        AudioEffectItem(
            id = "NORMAL",
            titleArabic = "صوت طبيعي (بدون مؤثر)",
            subtitleArabic = "صوتك الخام بدون أي تعديل أو فلاتر",
            descriptionArabic = "تسجيل نقي وواضح يحافظ على جودة ونبرة صوتك الأصلية بالكامل.",
            category = AudioEffectCategory.ALL,
            iconEmoji = "🎙️",
            defaultParams = AudioEffectParameters(),
            tags = listOf("طبيعي", "أصلي", "normal", "raw", "clean")
        ),

        // 2. Studio Reverb / Hall
        AudioEffectItem(
            id = "STUDIO_REVERB",
            titleArabic = "صدى استوديو وقاعة (Reverb)",
            subtitleArabic = "تردد صوتي ناعم يحاكي قاعات التسجيل الكبرى",
            descriptionArabic = "يضيف عمقاً وامتلاءً صوتياً دافئاً كأنك تسجل داخل أستوديو عازل أو قاعة سينما فخمة.",
            category = AudioEffectCategory.REVERB_SPACE,
            iconEmoji = "🏛️",
            defaultParams = AudioEffectParameters(
                reverbRoomSize = 0.65f,
                reverbDecay = 2.2f,
                dryWetMix = 0.45f
            ),
            tags = listOf("صدى", "ريفيرب", "قاعة", "استوديو", "reverb", "hall")
        ),

        // 3. Cathedral / Deep Space Reverb
        AudioEffectItem(
            id = "CATHEDRAL_REVERB",
            titleArabic = "صدى الكاتدرائية الفسيح",
            subtitleArabic = "تردد ممتد وكبير جداً للأجواء المهيبة والأسطورية",
            descriptionArabic = "محاكاة معمارية لارتداد صوتي طويل يستمر لعدة ثوانٍ لأفلام الخيال والدراما الملحمية.",
            category = AudioEffectCategory.REVERB_SPACE,
            iconEmoji = "⛪",
            defaultParams = AudioEffectParameters(
                reverbRoomSize = 0.95f,
                reverbDecay = 4.5f,
                dryWetMix = 0.60f
            ),
            tags = listOf("كاتدرائية", "صدى عميق", "ملحمي", "cathedral", "space")
        ),

        // 4. Studio Delay Echo
        AudioEffectItem(
            id = "STUDIO_ECHO",
            titleArabic = "تأخير وتكرار صوتي (Echo)",
            subtitleArabic = "تكرار إيقاعي متتالي للكلمات مع التلاشي",
            descriptionArabic = "يولد صدى تكرارياً متتالياً بنبضات محددة يعطي طابعاً إذاعياً ومسرحياً جذاباً.",
            category = AudioEffectCategory.REVERB_SPACE,
            iconEmoji = "🔁",
            defaultParams = AudioEffectParameters(
                echoDelayMs = 280,
                echoFeedback = 0.48f,
                dryWetMix = 0.50f
            ),
            tags = listOf("ايكو", "صدى", "تكرار", "تأخير", "echo", "delay")
        ),

        // 5. Deep Cave Echo
        AudioEffectItem(
            id = "CAVE_ECHO",
            titleArabic = "صدى الكهف الجبلي",
            subtitleArabic = "ارتدادات متفرقة ومزدوجة من الصخور والجبال",
            descriptionArabic = "تأثير صدى مركب يجمع بين التأخير الطويل والتردد المظلم لمشاهد المغامرات والطبيعة.",
            category = AudioEffectCategory.REVERB_SPACE,
            iconEmoji = "⛰️",
            defaultParams = AudioEffectParameters(
                echoDelayMs = 450,
                echoFeedback = 0.65f,
                reverbRoomSize = 0.80f,
                reverbDecay = 3.5f,
                dryWetMix = 0.55f
            ),
            tags = listOf("كهف", "جبل", "مغامرة", "cave", "mountain")
        ),

        // 6. Pitch Shifter (Custom Semitone Tuning)
        AudioEffectItem(
            id = "PITCH_SHIFTER",
            titleArabic = "مغير النغمة والطبقة (Pitch Shift)",
            subtitleArabic = "رفع أو خفض طبقة الصوت بالأنصاف النغمية بدقة",
            descriptionArabic = "تحكم مرن بالطبقة الصوتية لتنعيم الصوت أو جعله أعمق وأغلظ دون تغيير السرعة.",
            category = AudioEffectCategory.PITCH_VOICE,
            iconEmoji = "🎚️",
            defaultParams = AudioEffectParameters(
                pitchSemitones = 3,
                pitchMultiplier = 1.19f,
                speedMultiplier = 1.0f
            ),
            tags = listOf("بتش", "نغمة", "طبقة", "pitch", "semitones")
        ),

        // 7. Cyber Robot / Vocoder
        AudioEffectItem(
            id = "CYBER_ROBOT",
            titleArabic = "روبوت آلي وسايبر (Cyber Robot)",
            subtitleArabic = "تضمين معدني وتردد اصطناعي للشخصيات الآلية",
            descriptionArabic = "تأثير رنين معدني عالي التردد يحول نبرة صوتك إلى صوت روبوت ذكاء اصطناعي قادم من المستقبل.",
            category = AudioEffectCategory.SCI_FI_ROBOT,
            iconEmoji = "🤖",
            defaultParams = AudioEffectParameters(
                robotModulationHz = 110f,
                robotResonance = 0.75f,
                pitchMultiplier = 0.88f,
                distortionDrive = 0.25f,
                dryWetMix = 0.85f
            ),
            tags = listOf("روبوت", "سايبر", "الي", "فوكودر", "robot", "vocoder", "cyber")
        ),

        // 8. Cartoon Chipmunk
        AudioEffectItem(
            id = "CHIPMUNK_CARTOON",
            titleArabic = "كرتون وسنجاب مرح (Chipmunk)",
            subtitleArabic = "صوت رفيع وسريع وكوميدي لشخصيات الرسوم المتحركة",
            descriptionArabic = "يرفع الترددات الصوتية بدرجات عالية ليمنحك صوت شخصيات الأنيمي والأطفال الكرتونية.",
            category = AudioEffectCategory.PITCH_VOICE,
            iconEmoji = "🐿️",
            defaultParams = AudioEffectParameters(
                pitchSemitones = 8,
                pitchMultiplier = 1.65f,
                speedMultiplier = 1.15f
            ),
            tags = listOf("سنجاب", "كرتون", "انمي", "كوميدي", "chipmunk", "cartoon")
        ),

        // 9. Deep Cinematic Monster
        AudioEffectItem(
            id = "CINEMATIC_DEEP",
            titleArabic = "سينمائي ضخم ووحش (Deep Hero)",
            subtitleArabic = "طبقة عميقة وجهورية للوثائقيات والشخصيات المرعبة",
            descriptionArabic = "يخفض التردد مع تعزيز الباس (Bass Boost) ليمنح المتحدث هيبة وفخامة صوتية سينمائية.",
            category = AudioEffectCategory.PITCH_VOICE,
            iconEmoji = "🎬",
            defaultParams = AudioEffectParameters(
                pitchSemitones = -6,
                pitchMultiplier = 0.72f,
                speedMultiplier = 0.92f,
                reverbRoomSize = 0.40f,
                dryWetMix = 0.75f
            ),
            tags = listOf("سينمائي", "ضخم", "وحش", "قرار", "عميق", "deep", "monster")
        ),

        // 10. Female Voice Transformer
        AudioEffectItem(
            id = "FEMALE_VOICE",
            titleArabic = "تحويل لصوت أنثوي (Female Voice)",
            subtitleArabic = "طبقة صوت نسائية ناعمة ورقيقة",
            descriptionArabic = "يعالج الترددات الأساسية لتقريب صوت المتحدث إلى النبرة والجواب الأنثوي الناعم.",
            category = AudioEffectCategory.PITCH_VOICE,
            iconEmoji = "👩",
            defaultParams = AudioEffectParameters(
                pitchSemitones = 6,
                pitchMultiplier = 1.45f,
                speedMultiplier = 1.04f
            ),
            tags = listOf("انثى", "نسائي", "بنت", "female", "girl")
        ),

        // 11. Male Voice Transformer
        AudioEffectItem(
            id = "MALE_VOICE",
            titleArabic = "تحويل لصوت رجالي (Male Voice)",
            subtitleArabic = "طبقة صوت ذكورية جهورية وفخمة",
            descriptionArabic = "يعالج الترددات الأساسية لتقريب صوت المتحدثة إلى النبرة والقرار الرجالي الرصين.",
            category = AudioEffectCategory.PITCH_VOICE,
            iconEmoji = "👨",
            defaultParams = AudioEffectParameters(
                pitchSemitones = -5,
                pitchMultiplier = 0.74f,
                speedMultiplier = 0.96f
            ),
            tags = listOf("رجل", "رجالي", "ذكر", "male", "man")
        ),

        // 12. Vintage Radio / Walkie-Talkie
        AudioEffectItem(
            id = "VINTAGE_RADIO",
            titleArabic = "مذياع ولاسلكي قديم (Radio / Walkie)",
            subtitleArabic = "فلتر النطاق الترددي الضيق مع حشرجة صوتية كلاسيكية",
            descriptionArabic = "يقص الترددات المنخفضة والعالية (Bandpass Filter) لمحاكاة مكالمات اللاسلكي وراديو الخمسينيات.",
            category = AudioEffectCategory.VINTAGE_FILTERS,
            iconEmoji = "📻",
            defaultParams = AudioEffectParameters(
                highPassCutoffHz = 450f,
                lowPassCutoffHz = 3200f,
                distortionDrive = 0.35f,
                pitchMultiplier = 1.05f
            ),
            tags = listOf("راديو", "لاسلكي", "قديم", "radio", "walkie", "vintage")
        ),

        // 13. Megaphone / PA Speaker
        AudioEffectItem(
            id = "MEGAPHONE",
            titleArabic = "مكبر صوت يدوي (Megaphone)",
            subtitleArabic = "رنين معدني مشبع للتعليمات والنداءات العامة",
            descriptionArabic = "محاكاة صوت مكبرات الصوت المحمولة مع تشبع صوتي بارز وترددات وسطى حادة.",
            category = AudioEffectCategory.VINTAGE_FILTERS,
            iconEmoji = "📢",
            defaultParams = AudioEffectParameters(
                highPassCutoffHz = 600f,
                lowPassCutoffHz = 2800f,
                distortionDrive = 0.60f,
                dryWetMix = 0.90f
            ),
            tags = listOf("مكبر", "ميجافون", "نداء", "megaphone", "speaker")
        ),

        // 14. Space Alien Invader
        AudioEffectItem(
            id = "SPACE_ALIEN",
            titleArabic = "كائن فضائي غريب (Space Alien)",
            subtitleArabic = "تعديل صوتي مزدوج مع اهتزاز فضائي",
            descriptionArabic = "تأثير مركب يعطي شعور الكائنات الفضائية والمخلوقات الخيالية في أفلام الفضاء.",
            category = AudioEffectCategory.SCI_FI_ROBOT,
            iconEmoji = "👽",
            defaultParams = AudioEffectParameters(
                robotModulationHz = 48f,
                robotResonance = 0.85f,
                pitchMultiplier = 1.30f,
                echoDelayMs = 180,
                echoFeedback = 0.35f,
                dryWetMix = 0.80f
            ),
            tags = listOf("فضائي", "كائن", "فضاء", "alien", "sci-fi")
        ),

        // 15. Multi-voice Chorus
        AudioEffectItem(
            id = "CHORUS_ENSEMBLE",
            titleArabic = "كورس وجماعي (Ensemble Chorus)",
            subtitleArabic = "مضاعفة الصوت وتكثيفه وكأنه كورال متعدد الأصوات",
            descriptionArabic = "يقوم بمضاعفة الصوت مع إزاحات دقيقة في الوقت والنغمة ليعطي شعور المجموعة الغنائية.",
            category = AudioEffectCategory.VINTAGE_FILTERS,
            iconEmoji = "👥",
            defaultParams = AudioEffectParameters(
                chorusDepth = 0.70f,
                reverbRoomSize = 0.40f,
                echoDelayMs = 45,
                echoFeedback = 0.25f,
                dryWetMix = 0.65f
            ),
            tags = listOf("كورس", "كورال", "جماعي", "chorus", "ensemble")
        ),

        // 16. Telephone Call Lo-Fi
        AudioEffectItem(
            id = "PHONE_CALL",
            titleArabic = "مكالمة هاتفية (Telephone Lo-Fi)",
            subtitleArabic = "نبرة صوت عبر خط الهاتف النقال أو الأرضي",
            descriptionArabic = "عزل دقيق للنطاق الترددي الصوتي لمحاكاة المكالمات الهاتفية في الحوارات الدرامية.",
            category = AudioEffectCategory.VINTAGE_FILTERS,
            iconEmoji = "☎️",
            defaultParams = AudioEffectParameters(
                highPassCutoffHz = 350f,
                lowPassCutoffHz = 3400f,
                distortionDrive = 0.15f
            ),
            tags = listOf("هاتف", "تلفون", "مكالمة", "phone", "telephone", "lofi")
        )
    )

    fun findById(id: String): AudioEffectItem {
        return effects.find { it.id.equals(id, ignoreCase = true) } ?: effects.first()
    }

    /**
     * Map old VoiceEffect enum to modern AudioEffectItem
     */
    fun fromVoiceEffect(effect: VoiceEffect): AudioEffectItem {
        return when (effect) {
            VoiceEffect.NORMAL -> findById("NORMAL")
            VoiceEffect.DEEP -> findById("CINEMATIC_DEEP")
            VoiceEffect.CHIPMUNK -> findById("CHIPMUNK_CARTOON")
            VoiceEffect.ROBOT -> findById("CYBER_ROBOT")
            VoiceEffect.ECHO -> findById("STUDIO_ECHO")
            VoiceEffect.FEMALE_VOICE -> findById("FEMALE_VOICE")
            VoiceEffect.MALE_VOICE -> findById("MALE_VOICE")
            VoiceEffect.AUTO_GENDER -> findById("NORMAL")
            VoiceEffect.RADIO -> findById("VINTAGE_RADIO")
        }
    }
}
