package com.example.audio.hollywood

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GlobalDubbingTarget(
    val languageCode: String,
    val languageNameArabic: String,
    val flagEmoji: String,
    val defaultVoiceId: String,
    val isEnabled: Boolean = true
)

/**
 * Global Multi-Language Dubbing Matrix Engine.
 * Enables instant one-click or voice-driven parallel dubbing into 10 major global languages.
 */
class GlobalMultiLanguageMatrixEngine(private val context: Context) {

    val supportedLanguages = listOf(
        GlobalDubbingTarget("en-US", "الإنجليزية الأمريكية", "🇺🇸", "en-US-Journey-F"),
        GlobalDubbingTarget("es-ES", "الإسبانية", "🇪🇸", "es-ES-Neural2-A"),
        GlobalDubbingTarget("fr-FR", "الفرنسية", "🇫🇷", "fr-FR-Neural2-A"),
        GlobalDubbingTarget("de-DE", "الألمانية", "🇩🇪", "de-DE-Neural2-B"),
        GlobalDubbingTarget("ru-RU", "الروسية", "🇷🇺", "ru-RU-Wavenet-C"),
        GlobalDubbingTarget("ja-JP", "اليابانية (أنمي)", "🇯🇵", "ja-JP-Neural2-B"),
        GlobalDubbingTarget("zh-CN", "الصينية (ماندرين)", "🇨🇳", "cmn-CN-Wavenet-A"),
        GlobalDubbingTarget("hi-IN", "الهندية (بوليوود)", "🇮🇳", "hi-IN-Neural2-A"),
        GlobalDubbingTarget("tr-TR", "التركية (دراما)", "🇹🇷", "tr-TR-Wavenet-B"),
        GlobalDubbingTarget("ko-KR", "الكورية (K-Drama)", "🇰🇷", "ko-KR-Neural2-A")
    )

    private val _selectedTargets = MutableStateFlow(supportedLanguages.take(4).map { it.languageCode }.toSet())
    val selectedTargets: StateFlow<Set<String>> = _selectedTargets.asStateFlow()

    fun toggleLanguageTarget(code: String) {
        val current = _selectedTargets.value.toMutableSet()
        if (current.contains(code)) {
            if (current.size > 1) current.remove(code)
        } else {
            current.add(code)
        }
        _selectedTargets.value = current
    }

    fun selectAll() {
        _selectedTargets.value = supportedLanguages.map { it.languageCode }.toSet()
    }
}
