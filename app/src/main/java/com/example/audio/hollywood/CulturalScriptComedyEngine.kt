package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdaptedDialogueLine(
    val originalText: String,
    val localizedText: String,
    val comedyPunchlineArabic: String?,
    val isRhymed: Boolean,
    val syllableMatchPercent: Int
)

/**
 * Cinematic Cultural Localization & Comedy Punchline Engine.
 * Adapts foreign jokes, idioms, and expressions into culturally resonant
 * Arabic punchlines with syllable-time matching.
 */
class CulturalScriptComedyEngine(private val context: Context) {

    private val _lastAdaptedLines = MutableStateFlow<List<AdaptedDialogueLine>>(emptyList())
    val lastAdaptedLines: StateFlow<List<AdaptedDialogueLine>> = _lastAdaptedLines.asStateFlow()

    private val commonIdiomMap = mapOf(
        "piece of cake" to "أسهل من شربة ماء يا عمنا! 🍰",
        "break a leg" to "بالتوفيق يا بطل، ولعها! 🔥",
        "under the weather" to "تعبان شوية وعندي هبوط",
        "spill the beans" to "انطق وقول اللي عندك وبلاش لف ودوران!",
        "bite the bullet" to "لازم نجمد ونتحمل اللي جاي",
        "hit the jackpot" to "ضربت معانا الحظ يا معلم! 💰",
        "call it a day" to "كفاية كده النهاردة ويلا نروّح"
    )

    fun adaptScriptLine(
        originalDialogue: String,
        dialect: String = "egyptian",
        injectComedyPunchline: Boolean = true
    ): AdaptedDialogueLine {
        var adapted = originalDialogue
        var punchline: String? = null

        val lower = originalDialogue.lowercase()
        for ((idiom, replacement) in commonIdiomMap) {
            if (lower.contains(idiom)) {
                adapted = adapted.replace(idiom, replacement, ignoreCase = true)
                punchline = replacement
                break
            }
        }

        if (injectComedyPunchline && punchline == null && dialect == "egyptian") {
            if (adapted.contains("؟")) {
                adapted = "$adapted .. هو احنا هنهزر ولا إيه؟ 😂"
            }
        }

        val originalWords = originalDialogue.split("\\s+".toRegex()).size
        val adaptedWords = adapted.split("\\s+".toRegex()).size
        val syllableMatch = ((1.0f - kotlin.math.abs(originalWords - adaptedWords).toFloat() / (originalWords + 1).toFloat()) * 100f).toInt().coerceIn(75, 100)

        val result = AdaptedDialogueLine(
            originalText = originalDialogue,
            localizedText = adapted,
            comedyPunchlineArabic = punchline,
            isRhymed = adapted.endsWith("نا") || adapted.endsWith("ها") || adapted.endsWith("ير"),
            syllableMatchPercent = syllableMatch
        )

        val current = _lastAdaptedLines.value.toMutableList()
        current.add(result)
        _lastAdaptedLines.value = current
        return result
    }
}
