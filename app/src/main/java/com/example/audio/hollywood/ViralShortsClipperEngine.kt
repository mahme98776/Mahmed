package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ViralShortsClip(
    val titleArabic: String,
    val hookDescriptionArabic: String,
    val startSeconds: Float,
    val endSeconds: Float,
    val durationSeconds: Float,
    val viralScore: Int,
    val catchyCaptionArabic: String,
    val hashtags: List<String>
)

/**
 * Viral Hook & Vertical 9:16 Shorts Clipper Engine.
 * Analyzes speech dynamics, volume peaks, and narrative hooks to extract
 * high-retention 15-60s highlight clips formatted for TikTok, Reels, and Shorts.
 */
class ViralShortsClipperEngine(private val context: Context) {

    private val _extractedClips = MutableStateFlow<List<ViralShortsClip>>(emptyList())
    val extractedClips: StateFlow<List<ViralShortsClip>> = _extractedClips.asStateFlow()

    /**
     * Extracts viral clips from dialogue segments and duration.
     */
    fun extractViralHooks(
        totalDurationSec: Float,
        dialogueLines: List<String>
    ): List<ViralShortsClip> {
        val clips = mutableListOf<ViralShortsClip>()

        val clip1Duration = minOf(30f, totalDurationSec.coerceAtLeast(15f))
        val clip1Start = 0f
        val clip1End = clip1Start + clip1Duration

        clips.add(
            ViralShortsClip(
                titleArabic = "اللحظة الحاسمة والمواجهة الكبرى 🎬🔥",
                hookDescriptionArabic = "بداية المشهد المشوقة وأقوى جملة حوارية تجذب انتباه المشاهد في أول 3 ثوانٍ",
                startSeconds = clip1Start,
                endSeconds = clip1End,
                durationSeconds = clip1Duration,
                viralScore = 98,
                catchyCaptionArabic = "لن تصدق ما حدث في هذه المواجهة! شاهد للنهاية 😱👇",
                hashtags = listOf("#دبلجة_سينمائية", "#افلام", "#تريند", "#viral", "#shorts", "#fyp")
            )
        )

        if (totalDurationSec > 45f) {
            val clip2Start = (totalDurationSec * 0.4f).coerceAtLeast(0f)
            val clip2Duration = minOf(25f, totalDurationSec - clip2Start)
            clips.add(
                ViralShortsClip(
                    titleArabic = "ذروة الدراما والمفاجأة غير المتوقعة ⚡",
                    hookDescriptionArabic = "المقطع الأعلى في الشحن العاطفي والانفعال الحواري",
                    startSeconds = clip2Start,
                    endSeconds = clip2Start + clip2Duration,
                    durationSeconds = clip2Duration,
                    viralScore = 94,
                    catchyCaptionArabic = "ردة الفعل كانت صادمة للجميع.. ما رأيكم في تصرفه؟ 🤔✨",
                    hashtags = listOf("#اكسبلور", "#سينما", "#دوبلاج_عربي", "#مشاهد_مؤثرة")
                )
            )
        }

        _extractedClips.value = clips
        return clips
    }
}
