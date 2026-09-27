package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KaraokeWordToken(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val isHighlighted: Boolean = false,
    val colorHex: String = "#FFD700"
)

data class AnimatedSubtitleCue(
    val id: String,
    val lineText: String,
    val startMs: Long,
    val endMs: Long,
    val tokens: List<KaraokeWordToken>,
    val emojiBadge: String = "🔥"
)

/**
 * Hollywood Kinetic Karaoke Animated Subtitles Engine.
 * Generates dynamic, word-by-word highlighted captions designed to maximize
 * viewer engagement and watch time across social platforms.
 */
class KaraokeViralSubtitlesEngine(private val context: Context) {

    private val _subtitleCues = MutableStateFlow<List<AnimatedSubtitleCue>>(emptyList())
    val subtitleCues: StateFlow<List<AnimatedSubtitleCue>> = _subtitleCues.asStateFlow()

    fun generateKaraokeCuesFromDialogue(
        dialogue: String,
        startTimeSec: Float,
        endTimeSec: Float
    ): AnimatedSubtitleCue {
        val words = dialogue.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val totalDurationMs = ((endTimeSec - startTimeSec) * 1000f).toLong().coerceAtLeast(300L)
        val wordDurationMs = totalDurationMs / words.size.coerceAtLeast(1)

        val tokens = words.mapIndexed { index, w ->
            val wStart = (startTimeSec * 1000f).toLong() + (index * wordDurationMs)
            val wEnd = wStart + wordDurationMs
            val highlightColor = when (index % 3) {
                0 -> "#F59E0B" // Amber Gold
                1 -> "#10B981" // Emerald Green
                else -> "#3B82F6" // Neon Blue
            }
            KaraokeWordToken(
                word = w,
                startMs = wStart,
                endMs = wEnd,
                colorHex = highlightColor
            )
        }

        val cue = AnimatedSubtitleCue(
            id = "cue_${System.currentTimeMillis()}",
            lineText = dialogue,
            startMs = (startTimeSec * 1000f).toLong(),
            endMs = (endTimeSec * 1000f).toLong(),
            tokens = tokens,
            emojiBadge = if (dialogue.contains("!") || dialogue.contains("؟")) "⚡" else "🎬"
        )

        val list = _subtitleCues.value.toMutableList()
        list.add(cue)
        _subtitleCues.value = list
        return cue
    }
}
