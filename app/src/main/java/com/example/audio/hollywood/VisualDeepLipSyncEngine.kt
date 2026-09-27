package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phoneme-to-Viseme mouth aperture classification for visual lip-sync.
 */
enum class VisemeShape(
    val code: String,
    val openRatio: Float,
    val widthRatio: Float,
    val labelArabic: String
) {
    REST("sil", 0.0f, 0.5f, "سكون"),
    AA("aa", 0.95f, 0.7f, "فتح عريض (أ، آ، ع)"),
    EE("ee", 0.45f, 0.95f, "ابتسامة ضيقة (ي، إ، ت)"),
    OO("oo", 0.75f, 0.35f, "استدارة مضمومة (و، أُو)"),
    B_M_P("bmp", 0.0f, 0.45f, "إطباق شفتين (ب، م)"),
    F_V("fv", 0.2f, 0.6f, "أسنان على الشفة (ف)"),
    TH_DH("th", 0.35f, 0.7f, "طرف لسان (ث، ذ، ظ)"),
    SH_CH("sh", 0.55f, 0.65f, "تكوير متقدم (ش، ج)");
}

data class LipSyncKeyframe(
    val timeOffsetSec: Float,
    val viseme: VisemeShape,
    val openness: Float,
    val characterSpeaker: String
)

/**
 * Visual AI Deep Lip-Sync Simulation & Viseme Mapping Engine.
 * Converts dialogue text and audio timing into precision 60fps mouth keyframes
 * to ensure hyper-realistic visual sync with video frames.
 */
class VisualDeepLipSyncEngine(private val context: Context) {

    private val _currentKeyframes = MutableStateFlow<List<LipSyncKeyframe>>(emptyList())
    val currentKeyframes: StateFlow<List<LipSyncKeyframe>> = _currentKeyframes.asStateFlow()

    private val _currentOpenness = MutableStateFlow(0f)
    val currentOpenness: StateFlow<Float> = _currentOpenness.asStateFlow()

    private val _currentViseme = MutableStateFlow(VisemeShape.REST)
    val currentViseme: StateFlow<VisemeShape> = _currentViseme.asStateFlow()

    /**
     * Generates a timeline of viseme keyframes from Arabic dialogue text and duration.
     */
    fun computeLipSyncKeyframes(
        text: String,
        startTimeSec: Float,
        endTimeSec: Float,
        speakerName: String
    ): List<LipSyncKeyframe> {
        val duration = (endTimeSec - startTimeSec).coerceAtLeast(0.3f)
        val cleanedText = text.replace(Regex("[،.؟!:\"']"), "").trim()
        if (cleanedText.isEmpty()) return emptyList()

        val keyframes = mutableListOf<LipSyncKeyframe>()
        val timeStep = duration / cleanedText.length.toFloat()

        for (i in cleanedText.indices) {
            val char = cleanedText[i]
            val t = startTimeSec + i * timeStep

            val viseme = when (char) {
                'ا', 'أ', 'إ', 'آ', 'ع', 'ه', 'ح' -> VisemeShape.AA
                'ي', 'ى', 'ئ', 'ت', 'د', 'ز', 'س' -> VisemeShape.EE
                'و', 'ؤ', 'ق', 'خ', 'غ' -> VisemeShape.OO
                'ب', 'م' -> VisemeShape.B_M_P
                'ف' -> VisemeShape.F_V
                'ث', 'ذ', 'ظ' -> VisemeShape.TH_DH
                'ش', 'ج', 'ص', 'ض' -> VisemeShape.SH_CH
                ' ' -> VisemeShape.REST
                else -> VisemeShape.AA
            }

            keyframes.add(
                LipSyncKeyframe(
                    timeOffsetSec = t,
                    viseme = viseme,
                    openness = viseme.openRatio,
                    characterSpeaker = speakerName
                )
            )
        }

        // Close mouth at the end
        keyframes.add(
            LipSyncKeyframe(
                timeOffsetSec = endTimeSec,
                viseme = VisemeShape.REST,
                openness = 0f,
                characterSpeaker = speakerName
            )
        )

        _currentKeyframes.value = keyframes
        return keyframes
    }

    /**
     * Updates current real-time mouth position based on player time in seconds.
     */
    fun updatePlaybackPosition(currentTimeSec: Float) {
        val frames = _currentKeyframes.value
        if (frames.isEmpty()) {
            _currentOpenness.value = 0f
            _currentViseme.value = VisemeShape.REST
            return
        }

        val activeFrame = frames.lastOrNull { it.timeOffsetSec <= currentTimeSec }
            ?: frames.firstOrNull()

        if (activeFrame != null) {
            _currentOpenness.value = activeFrame.openness
            _currentViseme.value = activeFrame.viseme
        } else {
            _currentOpenness.value = 0f
            _currentViseme.value = VisemeShape.REST
        }
    }
}
