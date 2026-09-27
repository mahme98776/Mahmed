package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DialogueEmotion(
    val labelArabic: String,
    val pitchModulation: Float,
    val speechRateModifier: Float,
    val dynamicsCompressionRatio: Float,
    val emoji: String
) {
    HEROIC_EPIC("حماسي وبطولي", 1.12f, 1.08f, 0.85f, "⚔️"),
    ANGRY_OUTBURST("غضب وصراخ", 1.25f, 1.15f, 0.70f, "🔥"),
    WHISPER_SECRET("همس وسرية", 0.88f, 0.90f, 0.95f, "🤫"),
    SAD_EMOTIONAL("حزن وبكاء", 0.92f, 0.82f, 0.90f, "😢"),
    JOYFUL_LAUGH("فرح واحتفال", 1.18f, 1.12f, 0.80f, "🎉"),
    TENSE_SUSPENSE("ترقب وخوف", 1.05f, 0.95f, 0.75f, "⚡"),
    CALM_NEUTRAL("هادئ وطبيعي", 1.00f, 1.00f, 1.00f, "🎙️");
}

/**
 * Emotion & Sentimental Transfer Engine.
 * Infuses synthesized TTS dialogue with human dramatic performance
 * by analyzing semantic cues, punctuation, and scene context.
 */
class EmotionSentimentalTransferEngine(private val context: Context) {

    private val _lastDetectedEmotion = MutableStateFlow(DialogueEmotion.CALM_NEUTRAL)
    val lastDetectedEmotion: StateFlow<DialogueEmotion> = _lastDetectedEmotion.asStateFlow()

    /**
     * Determines emotion from dialogue text and punctuation semantics.
     */
    fun analyzeEmotionFromText(text: String): DialogueEmotion {
        val lower = text.trim()
        val emotion = when {
            lower.contains("!") && (lower.contains("انتبه") || lower.contains("اهرب") || lower.contains("ابتعد") || lower.contains("سأقضي")) ->
                DialogueEmotion.ANGRY_OUTBURST
            lower.contains("يا له من نصر") || lower.contains("سننتصر") || lower.contains("معاً للأبد") || lower.contains("إلى الأمام") ->
                DialogueEmotion.HEROIC_EPIC
            lower.contains("أرجوك") && (lower.contains("لماذا") || lower.contains("مات") || lower.contains("فقدت") || lower.contains("وداعاً")) ->
                DialogueEmotion.SAD_EMOTIONAL
            lower.contains("اششش") || lower.contains("اخفض صوتك") || lower.contains("سر") || lower.contains("لا يسمعنا") ->
                DialogueEmotion.WHISPER_SECRET
            lower.contains("رائع") || lower.contains("هههه") || lower.contains("مبروك") || lower.contains("سعيد") ->
                DialogueEmotion.JOYFUL_LAUGH
            lower.contains("ما هذا الصوت") || lower.contains("أشعر بالخطر") || lower.contains("من هناك") ->
                DialogueEmotion.TENSE_SUSPENSE
            lower.count { it == '!' } >= 2 ->
                DialogueEmotion.HEROIC_EPIC
            else ->
                DialogueEmotion.CALM_NEUTRAL
        }

        _lastDetectedEmotion.value = emotion
        return emotion
    }
}
