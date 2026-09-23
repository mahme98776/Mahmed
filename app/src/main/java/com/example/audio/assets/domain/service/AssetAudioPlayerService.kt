package com.example.audio.assets.domain.service

import com.example.audio.assets.domain.model.AssetPlaybackState
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * واجهة خدمة تشغيل الصوت عبر ExoPlayer لملفات الأصول والملفات المحلية.
 */
interface AssetAudioPlayerService {
    val playbackState: StateFlow<AssetPlaybackState>

    suspend fun playAsset(item: AudioAssetItem): ResourceResult<Unit>
    suspend fun playFile(file: File, titleArabic: String = file.name): ResourceResult<Unit>
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)
    fun setPlaybackSpeed(speed: Float)
    fun release()
}
