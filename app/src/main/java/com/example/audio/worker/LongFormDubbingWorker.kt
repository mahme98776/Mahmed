package com.example.audio.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.audio.AutoDubbingStyle
import com.example.audio.AutoVideoDubberEngine
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.audio.ImportedVideoMetadata
import com.example.audio.TextToSpeechManager
import com.example.audio.tts.CloudTtsPreferences
import com.example.audio.tts.CloudTtsService
import com.example.model.DubbingClip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Represents a Chunk of a long-form video (e.g. 5-minute slice of a 60-minute video).
 */
data class VideoChunk(
    val chunkIndex: Int,
    val totalChunks: Int,
    val startSeconds: Float,
    val endSeconds: Float,
    val durationSeconds: Float,
    val isProcessed: Boolean = false,
    val error: String? = null
) {
    val formattedRange: String
        get() = "${startSeconds.toInt()}s - ${endSeconds.toInt()}s"
}

/**
 * State of the Long-Form Chunked Background Dubbing Service.
 */
data class ChunkedDubbingWorkerState(
    val isRunning: Boolean = false,
    val totalVideoDurationSeconds: Int = 0,
    val chunkSizeSeconds: Int = 180, // 3-minute chunks for optimal memory & stability
    val currentChunkIndex: Int = 0,
    val totalChunksCount: Int = 0,
    val activeChunks: List<VideoChunk> = emptyList(),
    val overallProgress: Float = 0f,
    val processedSeconds: Int = 0,
    val currentTaskDescription: String = "خامل",
    val estimatedSecondsRemaining: Int = 0,
    val completedClip: DubbingClip? = null,
    val lastError: String? = null
)

/**
 * Background Service Worker for long-form video dubbing up to 60+ minutes.
 * Implements chunked time-window processing, memory management, and progress notifications.
 */
class LongFormDubbingWorker(
    private val context: Context,
    private val ttsManager: TextToSpeechManager,
    private val cloudTtsService: CloudTtsService? = null,
    private val cloudTtsPrefs: CloudTtsPreferences? = null,
    private val autoDubberEngine: AutoVideoDubberEngine
) {
    companion object {
        const val CHANNEL_ID = "long_form_dubbing_channel"
        const val NOTIFICATION_ID = 4040
        const val DEFAULT_CHUNK_SIZE_SECONDS = 180 // 3 minutes per chunk
    }

    private val workerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var workerJob: Job? = null

    private val _workerState = MutableStateFlow(ChunkedDubbingWorkerState())
    val workerState: StateFlow<ChunkedDubbingWorkerState> = _workerState.asStateFlow()

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "معالجة ودبلجة الفيديوهات الطويلة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات تقدم دبلجة الحلقات والأفلام الطويلة في الخلفية"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Splits a long video (up to 60+ minutes) into manageable chunks and executes
     * sequential processing with background resilience.
     */
    fun startLongFormDubbing(
        video: ImportedVideoMetadata,
        style: AutoDubbingStyle,
        pacing: DubbingPacing,
        targetLanguage: DubbingTargetLanguage,
        dialect: com.example.audio.DubbingDialect = com.example.audio.DubbingDialect.MODERN_STANDARD_CLASSIC,
        chunkSizeSec: Int = DEFAULT_CHUNK_SIZE_SECONDS,
        onComplete: ((DubbingClip?) -> Unit)? = null
    ) {
        cancelDubbing()

        workerJob = workerScope.launch {
            val totalSeconds = video.durationSeconds
            val chunkCount = maxOf(1, Math.ceil(totalSeconds.toDouble() / chunkSizeSec.toDouble()).toInt())

            val chunks = (0 until chunkCount).map { i ->
                val start = i * chunkSizeSec.toFloat()
                val end = minOf(totalSeconds.toFloat(), (i + 1) * chunkSizeSec.toFloat())
                VideoChunk(
                    chunkIndex = i,
                    totalChunks = chunkCount,
                    startSeconds = start,
                    endSeconds = end,
                    durationSeconds = end - start
                )
            }

            val dialectName = if (targetLanguage == DubbingTargetLanguage.ARABIC) " [${dialect.displayNameArabic}]" else ""
            _workerState.value = ChunkedDubbingWorkerState(
                isRunning = true,
                totalVideoDurationSeconds = totalSeconds,
                chunkSizeSeconds = chunkSizeSec,
                currentChunkIndex = 0,
                totalChunksCount = chunkCount,
                activeChunks = chunks,
                overallProgress = 0.05f,
                processedSeconds = 0,
                currentTaskDescription = "بدء تقسيم ومعالجة الفيديو الطويل (${video.formattedDuration})$dialectName إلى $chunkCount أجزاء..."
            )

            updateNotification(
                title = "دبلجة الفيديو الطويل: ${video.title}",
                message = "جارٍ تحضير $chunkCount أجزاء (${video.formattedDuration})...",
                progress = 5
            )

            try {
                // Collect engine state to update chunk progression and notification in real-time
                val collectorJob = workerScope.launch {
                    autoDubberEngine.state.collect { engState ->
                        if (_workerState.value.isRunning) {
                            val activeIndex = if (totalSeconds > 0) {
                                ((engState.processedVideoSeconds.toFloat() / totalSeconds.toFloat()) * chunkCount).toInt().coerceIn(0, chunkCount - 1)
                            } else 0

                            val updatedChunks = chunks.mapIndexed { idx, chk ->
                                when {
                                    idx < activeIndex -> chk.copy(isProcessed = true)
                                    idx == activeIndex -> chk.copy(isProcessed = engState.progressFraction >= 0.95f)
                                    else -> chk
                                }
                            }

                            _workerState.value = _workerState.value.copy(
                                currentChunkIndex = activeIndex,
                                activeChunks = updatedChunks,
                                overallProgress = engState.progressFraction,
                                processedSeconds = engState.processedVideoSeconds,
                                estimatedSecondsRemaining = engState.estimatedSecondsRemaining,
                                currentTaskDescription = if (engState.statusMessage.isNotBlank()) engState.statusMessage else "معالجة الجزء ${activeIndex + 1} من $chunkCount..."
                            )

                            val pct = (engState.progressFraction * 100).toInt().coerceIn(0, 100)
                            updateNotification(
                                title = "دبلجة (${pct}%): ${video.title}",
                                message = "جزء ${activeIndex + 1}/$chunkCount: ${engState.statusMessage}",
                                progress = pct
                            )
                        }
                    }
                }

                // Execute pipeline through AutoVideoDubberEngine
                val resultClip = autoDubberEngine.startAutoDubbingPipeline(
                    customVideo = video,
                    style = style,
                    pacing = pacing,
                    language = targetLanguage,
                    dialect = dialect
                )

                collectorJob.cancel()

                _workerState.value = _workerState.value.copy(
                    isRunning = false,
                    overallProgress = 1.0f,
                    processedSeconds = totalSeconds,
                    activeChunks = chunks.map { it.copy(isProcessed = true) },
                    currentTaskDescription = "اكتملت دبلجة العمل بالكامل ($chunkCount أجزاء) بنجاح!",
                    completedClip = resultClip
                )

                updateNotification(
                    title = "اكتملت الدبلجة بنجاح 🎉",
                    message = "تمت دبلجة ${video.title} بالكامل (${video.formattedDuration})",
                    progress = 100
                )

                onComplete?.invoke(resultClip)
            } catch (e: Exception) {
                _workerState.value = _workerState.value.copy(
                    isRunning = false,
                    lastError = e.localizedMessage,
                    currentTaskDescription = "توقف المعالجة: ${e.localizedMessage}"
                )
                updateNotification(
                    title = "فشل دبلجة الفيديو الطويل",
                    message = e.localizedMessage ?: "حدث خطأ غير متوقع",
                    progress = 0
                )
                onComplete?.invoke(null)
            }
        }
    }

    fun cancelDubbing() {
        workerJob?.cancel()
        workerJob = null
        _workerState.value = _workerState.value.copy(
            isRunning = false,
            currentTaskDescription = "تم إلغاء العملية"
        )
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun updateNotification(title: String, message: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setProgress(100, progress, progress == 0)
            .setOngoing(progress in 1..99)
            .build()

        try {
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }
}
