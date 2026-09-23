package com.example.audio.assets.domain.service

import com.example.audio.assets.domain.model.ResourceResult
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * واجهة خدمة تحويل النص إلى كلام محلياً وبدون إنترنت بنسبة 100%.
 */
interface OfflineTtsService {
    val isInitialized: StateFlow<Boolean>
    val isSpeaking: StateFlow<Boolean>

    suspend fun speakText(
        text: String,
        languageCode: String = "ar",
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): ResourceResult<Unit>

    suspend fun synthesizeToWavFile(
        text: String,
        outputFile: File,
        languageCode: String = "ar",
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): ResourceResult<File>

    fun stop()
    fun release()
}

/**
 * حالة التعرف على الصوت محلياً
 */
data class OfflineSttState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val partialText: String = "",
    val finalText: String = "",
    val confidence: Float = 0f,
    val errorMessage: String? = null
)

/**
 * واجهة خدمة التعرف على الصوت وتحويله لنص محلياً بالكامل.
 */
interface OfflineSttService {
    val state: StateFlow<OfflineSttState>

    fun startListening(
        languageCode: String = "ar-SA",
        onResult: (String) -> Unit
    ): ResourceResult<Unit>

    fun stopListening()
    fun cancel()

    suspend fun transcribeAudioFile(
        audioFile: File,
        languageCode: String = "ar-SA"
    ): ResourceResult<String>

    fun release()
}
