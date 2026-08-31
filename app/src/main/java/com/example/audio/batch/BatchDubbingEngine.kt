package com.example.audio.batch

import android.content.Context
import com.example.audio.AutoDubbingStyle
import com.example.audio.AutoVideoDubberEngine
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.audio.ImportedVideoMetadata
import com.example.model.DubbingClip
import com.example.model.SampleClipsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BatchDubbingEngine(
    private val context: Context,
    private val autoVideoDubber: AutoVideoDubberEngine
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var batchJob: Job? = null

    private val _state = MutableStateFlow(BatchProcessingSessionState())
    val state: StateFlow<BatchProcessingSessionState> = _state.asStateFlow()

    fun addVideoToQueue(
        metadata: ImportedVideoMetadata,
        sourceLanguage: DubbingTargetLanguage = DubbingTargetLanguage.ENGLISH,
        targetLanguage: DubbingTargetLanguage = _state.value.globalTargetLanguage ?: DubbingTargetLanguage.ARABIC,
        style: AutoDubbingStyle = _state.value.globalDubbingStyle ?: AutoDubbingStyle.DOCUMENTARY,
        pacing: DubbingPacing = _state.value.globalPacing ?: DubbingPacing.BALANCED
    ): BatchVideoItem {
        val newItem = BatchVideoItem(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            videoMetadata = metadata,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            dubbingStyle = style,
            pacing = pacing,
            status = BatchItemStatus.QUEUED,
            statusMessage = "جاهز في قائمة الانتظار ⏳"
        )

        val updatedQueue = _state.value.queue + newItem
        recalculateQueueMetrics(updatedQueue)
        return newItem
    }

    fun addMultipleVideosToQueue(metadataList: List<ImportedVideoMetadata>) {
        val newItems = metadataList.map { metadata ->
            BatchVideoItem(
                id = UUID.randomUUID().toString(),
                title = metadata.title,
                videoMetadata = metadata,
                sourceLanguage = DubbingTargetLanguage.ENGLISH,
                targetLanguage = _state.value.globalTargetLanguage ?: DubbingTargetLanguage.ARABIC,
                dubbingStyle = _state.value.globalDubbingStyle ?: AutoDubbingStyle.DOCUMENTARY,
                pacing = _state.value.globalPacing ?: DubbingPacing.BALANCED,
                status = BatchItemStatus.QUEUED,
                statusMessage = "جاهز في قائمة الانتظار ⏳"
            )
        }

        val updatedQueue = _state.value.queue + newItems
        recalculateQueueMetrics(updatedQueue)
    }

    fun addSamplePackToQueue() {
        val sampleClips = SampleClipsRepository.clips
        val newItems = sampleClips.mapIndexed { index, clip ->
            val style = when (clip.category) {
                "وثائقي" -> AutoDubbingStyle.DOCUMENTARY
                "سينمائي", "خيال علمي" -> AutoDubbingStyle.CINEMATIC_DRAMA
                "كرتون" -> AutoDubbingStyle.CARTOON_FUN
                else -> AutoDubbingStyle.DOCUMENTARY
            }

            val targetLang = when (index % 4) {
                0 -> DubbingTargetLanguage.ARABIC
                1 -> DubbingTargetLanguage.SPANISH
                2 -> DubbingTargetLanguage.FRENCH
                else -> DubbingTargetLanguage.ENGLISH
            }

            val metadata = ImportedVideoMetadata(
                uriString = clip.videoUri ?: "android.resource://${context.packageName}/sample_${clip.id}",
                localFilePath = null,
                title = clip.title,
                durationSeconds = clip.durationSeconds,
                width = 1280,
                height = 720,
                hasAudio = true,
                thumbnailPath = clip.thumbnailPath
            )

            BatchVideoItem(
                id = "sample_batch_${clip.id}_${System.currentTimeMillis()}",
                title = clip.title,
                videoMetadata = metadata,
                sourceLanguage = DubbingTargetLanguage.ENGLISH,
                targetLanguage = targetLang,
                dubbingStyle = style,
                pacing = DubbingPacing.BALANCED,
                status = BatchItemStatus.QUEUED,
                statusMessage = "مشهد تجريبي جاهز للمعالجة 🎬"
            )
        }

        val updatedQueue = _state.value.queue + newItems
        recalculateQueueMetrics(updatedQueue)
    }

    fun removeVideoFromQueue(itemId: String) {
        val currentQueue = _state.value.queue
        val targetItem = currentQueue.find { it.id == itemId }
        
        // If currently processing, cancel the ongoing dubbing first
        if (targetItem?.status == BatchItemStatus.PROCESSING) {
            cancelBatchProcessing()
        }

        val updatedQueue = currentQueue.filterNot { it.id == itemId }
        recalculateQueueMetrics(updatedQueue)
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentQueue = _state.value.queue.toMutableList()
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices && fromIndex != toIndex) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            recalculateQueueMetrics(currentQueue)
        }
    }

    fun clearQueue() {
        if (_state.value.isRunning) {
            cancelBatchProcessing()
        }
        recalculateQueueMetrics(emptyList())
    }

    fun clearCompleted() {
        val updatedQueue = _state.value.queue.filterNot { it.status == BatchItemStatus.COMPLETED }
        recalculateQueueMetrics(updatedQueue)
    }

    fun updateItemConfig(
        itemId: String,
        targetLanguage: DubbingTargetLanguage? = null,
        dubbingStyle: AutoDubbingStyle? = null,
        pacing: DubbingPacing? = null,
        sourceLanguage: DubbingTargetLanguage? = null
    ) {
        val updatedQueue = _state.value.queue.map { item ->
            if (item.id == itemId && item.status != BatchItemStatus.PROCESSING) {
                item.copy(
                    targetLanguage = targetLanguage ?: item.targetLanguage,
                    dubbingStyle = dubbingStyle ?: item.dubbingStyle,
                    pacing = pacing ?: item.pacing,
                    sourceLanguage = sourceLanguage ?: item.sourceLanguage
                )
            } else {
                item
            }
        }
        recalculateQueueMetrics(updatedQueue)
    }

    fun applyGlobalLanguage(language: DubbingTargetLanguage) {
        _state.value = _state.value.copy(globalTargetLanguage = language)
        val updatedQueue = _state.value.queue.map { item ->
            if (item.status == BatchItemStatus.QUEUED) {
                item.copy(targetLanguage = language)
            } else {
                item
            }
        }
        recalculateQueueMetrics(updatedQueue)
    }

    fun applyGlobalStyle(style: AutoDubbingStyle) {
        _state.value = _state.value.copy(globalDubbingStyle = style)
        val updatedQueue = _state.value.queue.map { item ->
            if (item.status == BatchItemStatus.QUEUED) {
                item.copy(dubbingStyle = style)
            } else {
                item
            }
        }
        recalculateQueueMetrics(updatedQueue)
    }

    fun applyGlobalPacing(pacing: DubbingPacing) {
        _state.value = _state.value.copy(globalPacing = pacing)
        val updatedQueue = _state.value.queue.map { item ->
            if (item.status == BatchItemStatus.QUEUED) {
                item.copy(pacing = pacing)
            } else {
                item
            }
        }
        recalculateQueueMetrics(updatedQueue)
    }

    fun startBatchProcessing(
        onClipCompleted: ((DubbingClip) -> Unit)? = null
    ) {
        if (_state.value.isRunning && !_state.value.isPaused) return

        batchJob?.cancel()
        batchJob = scope.launch {
            _state.value = _state.value.copy(
                isRunning = true,
                isPaused = false,
                sessionStatusMessage = "بدأت معالجة الدُفعة..."
            )

            val queue = _state.value.queue.toMutableList()

            for (index in queue.indices) {
                val item = queue[index]
                if (item.status == BatchItemStatus.COMPLETED) {
                    continue
                }

                // Check pause state
                while (_state.value.isPaused) {
                    delay(500)
                }

                // If stopped/cancelled externally
                if (!_state.value.isRunning) {
                    break
                }

                // Update current processing item
                _state.value = _state.value.copy(
                    currentItemIndex = index,
                    currentItemId = item.id,
                    sessionStatusMessage = "معالجة الفيديو (${index + 1}/${queue.size}): ${item.title}"
                )

                queue[index] = item.copy(
                    status = BatchItemStatus.PROCESSING,
                    statusMessage = "جاري التحليل وتوليد السيناريو...",
                    progressFraction = 0.05f
                )
                _state.value = _state.value.copy(queue = queue.toList())

                try {
                    // Call the core auto video dubber pipeline for this individual video item
                    val dubbedClip = autoVideoDubber.startAutoDubbingPipeline(
                        customVideo = item.videoMetadata,
                        style = item.dubbingStyle,
                        pacing = item.pacing,
                        language = item.targetLanguage
                    )

                    if (dubbedClip != null) {
                        queue[index] = item.copy(
                            status = BatchItemStatus.COMPLETED,
                            statusMessage = "اكتملت الدبلجة بنجاح 🎉 (${dubbedClip.scriptLines.size} مقطع)",
                            progressFraction = 1.0f,
                            processedSeconds = item.totalSeconds,
                            resultClip = dubbedClip,
                            errorMessage = null
                        )
                        onClipCompleted?.invoke(dubbedClip)
                    } else {
                        queue[index] = item.copy(
                            status = BatchItemStatus.FAILED,
                            statusMessage = "تعذر إكمال الدبلجة للمقطع",
                            errorMessage = "خطأ أثناء المعالجة",
                            progressFraction = 0f
                        )
                    }
                } catch (e: Exception) {
                    queue[index] = item.copy(
                        status = BatchItemStatus.FAILED,
                        statusMessage = "خطأ: ${e.localizedMessage}",
                        errorMessage = e.localizedMessage,
                        progressFraction = 0f
                    )
                }

                recalculateQueueMetrics(queue.toList())
                delay(300)
            }

            val finalQueue = _state.value.queue
            val completed = finalQueue.count { it.status == BatchItemStatus.COMPLETED }
            val failed = finalQueue.count { it.status == BatchItemStatus.FAILED }

            _state.value = _state.value.copy(
                isRunning = false,
                isPaused = false,
                currentItemIndex = -1,
                currentItemId = null,
                sessionStatusMessage = "اكتملت معالجة الدُفعة! ($completed مكتمل • $failed فشل) 🎉"
            )
        }
    }

    fun pauseBatchProcessing() {
        if (_state.value.isRunning) {
            _state.value = _state.value.copy(
                isPaused = true,
                sessionStatusMessage = "تم إيقاف معالجة الدُفعة مؤقتاً ⏸️"
            )
        }
    }

    fun resumeBatchProcessing() {
        if (_state.value.isPaused) {
            _state.value = _state.value.copy(
                isPaused = false,
                sessionStatusMessage = "استئناف معالجة الدُفعة ▶️"
            )
        }
    }

    fun cancelBatchProcessing() {
        batchJob?.cancel()
        batchJob = null

        val updatedQueue = _state.value.queue.map { item ->
            if (item.status == BatchItemStatus.PROCESSING) {
                item.copy(
                    status = BatchItemStatus.QUEUED,
                    statusMessage = "تم إلغاء المعالجة وإعادته للانتظار",
                    progressFraction = 0f
                )
            } else {
                item
            }
        }

        _state.value = _state.value.copy(
            queue = updatedQueue,
            isRunning = false,
            isPaused = false,
            currentItemIndex = -1,
            currentItemId = null,
            sessionStatusMessage = "تم إلغاء معالجة الدُفعة ⏹️"
        )
        recalculateQueueMetrics(updatedQueue)
    }

    fun retryFailedItems(onClipCompleted: ((DubbingClip) -> Unit)? = null) {
        val updatedQueue = _state.value.queue.map { item ->
            if (item.status == BatchItemStatus.FAILED) {
                item.copy(
                    status = BatchItemStatus.QUEUED,
                    statusMessage = "في قائمة الانتظار لإعادة المحاولة ⏳",
                    progressFraction = 0f,
                    errorMessage = null
                )
            } else {
                item
            }
        }
        recalculateQueueMetrics(updatedQueue)
        startBatchProcessing(onClipCompleted)
    }

    private fun recalculateQueueMetrics(queue: List<BatchVideoItem>) {
        val totalSec = queue.sumOf { it.totalSeconds }
        val completed = queue.count { it.status == BatchItemStatus.COMPLETED }
        val failed = queue.count { it.status == BatchItemStatus.FAILED }
        
        val overallProgress = if (queue.isNotEmpty()) {
            val sumProgress = queue.sumOf { it.progressFraction.toDouble() }
            (sumProgress / queue.size).toFloat()
        } else {
            0f
        }

        val processedSec = queue.filter { it.status == BatchItemStatus.COMPLETED }.sumOf { it.totalSeconds }

        _state.value = _state.value.copy(
            queue = queue,
            completedCount = completed,
            failedCount = failed,
            totalQueueDurationSeconds = totalSec,
            processedQueueDurationSeconds = processedSec,
            overallProgressFraction = overallProgress
        )
    }
}
