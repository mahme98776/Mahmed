package com.example.audio.assets.domain.model

/**
 * نتيجة شاملة لمعالجة وتحويل الصوت/النص محلياً أو تشغيل الوسائط
 */
sealed class ResourceResult<out T> {
    data class Success<out T>(val data: T) : ResourceResult<T>()
    data class Error(val messageArabic: String, val throwable: Throwable? = null, val code: Int? = null) : ResourceResult<Nothing>()
    object Loading : ResourceResult<Nothing>()
}

/**
 * حالة تشغيل مشغل الصوت (ExoPlayer) لملفات الأصول
 */
data class AssetPlaybackState(
    val isPlaying: Boolean = false,
    val currentItem: AudioAssetItem? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1.0f,
    val playbackSpeed: Float = 1.0f,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)
