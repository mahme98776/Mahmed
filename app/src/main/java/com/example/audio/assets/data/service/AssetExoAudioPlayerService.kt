package com.example.audio.assets.data.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.audio.assets.domain.model.AssetPlaybackState
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import com.example.audio.assets.domain.repository.AudioAssetsRepository
import com.example.audio.assets.domain.service.AssetAudioPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * خدمة تشغيل متقدمة تعتمد على ExoPlayer (Media3) المتوافقة مع Clean Architecture
 * وتوفر إدارة ذاكرة صارمة (Memory Leak Prevention) ومزامنة وقت حية.
 */
class AssetExoAudioPlayerService(
    private val context: Context,
    private val audioAssetsRepository: AudioAssetsRepository
) : AssetAudioPlayerService {

    private val tag = "AssetExoPlayerService"
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private var exoPlayer: ExoPlayer? = null
    private var progressTrackingJob: Job? = null

    private val _playbackState = MutableStateFlow(AssetPlaybackState())
    override val playbackState: StateFlow<AssetPlaybackState> = _playbackState.asStateFlow()

    init {
        initExoPlayer()
    }

    private fun initExoPlayer() {
        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context)
                    .build()
                    .apply {
                        addListener(createPlayerListener())
                    }
                Log.i(tag, "ExoPlayer initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize ExoPlayer", e)
            _playbackState.value = _playbackState.value.copy(
                errorMessage = "تعذر تهيئة مشغل الصوت ExoPlayer: ${e.localizedMessage}"
            )
        }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            val isBuffering = state == Player.STATE_BUFFERING
            val isEnded = state == Player.STATE_ENDED

            if (isEnded) {
                stopProgressTracker()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = _playbackState.value.durationMs,
                    isBuffering = false
                )
            } else {
                _playbackState.value = _playbackState.value.copy(
                    isBuffering = isBuffering,
                    durationMs = exoPlayer?.duration?.coerceAtLeast(0L) ?: _playbackState.value.durationMs
                )
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(tag, "ExoPlayer playback error: ${error.errorCodeName}", error)
            stopProgressTracker()
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = "خطأ في تشغيل الصوت: ${error.localizedMessage ?: error.errorCodeName}"
            )
        }
    }

    override suspend fun playAsset(item: AudioAssetItem): ResourceResult<Unit> = withContext(Dispatchers.Main) {
        try {
            initExoPlayer()
            val player = exoPlayer ?: return@withContext ResourceResult.Error("مشغل الصوت غير متاح")

            // First resolve asset to file via repository for maximum stability and speed
            val fileRes = audioAssetsRepository.copyAssetToCache(item.assetPath)
            val uri = when (fileRes) {
                is ResourceResult.Success -> Uri.fromFile(fileRes.data)
                is ResourceResult.Error -> Uri.parse("asset:///${item.assetPath}")
                is ResourceResult.Loading -> Uri.parse("asset:///${item.assetPath}")
            }

            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _playbackState.value = _playbackState.value.copy(
                currentItem = item,
                isPlaying = true,
                errorMessage = null
            )
            Log.i(tag, "Playing asset: ${item.titleArabic} via uri: $uri")
            ResourceResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error playing asset: ${item.assetPath}", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                errorMessage = "تعذر تشغيل الملف الصوتي: ${e.localizedMessage}"
            )
            ResourceResult.Error(
                messageArabic = "تعذر تشغيل الملف الصوتي: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    override suspend fun playFile(file: File, titleArabic: String): ResourceResult<Unit> = withContext(Dispatchers.Main) {
        try {
            initExoPlayer()
            val player = exoPlayer ?: return@withContext ResourceResult.Error("مشغل الصوت غير متاح")

            val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _playbackState.value = _playbackState.value.copy(
                isPlaying = true,
                errorMessage = null
            )
            ResourceResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error playing file: ${file.name}", e)
            ResourceResult.Error(
                messageArabic = "تعذر تشغيل الملف: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    override fun pause() {
        exoPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    override fun resume() {
        exoPlayer?.play()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
    }

    override fun stop() {
        stopProgressTracker()
        exoPlayer?.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentPositionMs = 0L
        )
    }

    override fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceAtLeast(0L)
        exoPlayer?.seekTo(bounded)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = bounded)
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        exoPlayer?.volume = clamped
        _playbackState.value = _playbackState.value.copy(volume = clamped)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 2.0f)
        exoPlayer?.playbackParameters = PlaybackParameters(clamped)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = clamped)
    }

    override fun release() {
        stopProgressTracker()
        try {
            exoPlayer?.release()
            exoPlayer = null
            Log.i(tag, "ExoPlayer resources successfully released")
        } catch (e: Exception) {
            Log.w(tag, "Error releasing ExoPlayer", e)
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackingJob = serviceScope.launch {
            while (isActive) {
                val current = exoPlayer?.currentPosition ?: 0L
                val dur = exoPlayer?.duration?.coerceAtLeast(0L) ?: _playbackState.value.durationMs
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = current,
                    durationMs = dur
                )
                delay(100) // 100ms update interval for smooth seekbar updates
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }
}
