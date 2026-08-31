package com.example.audio.batch

import com.example.audio.AutoDubbingStyle
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.audio.ImportedVideoMetadata
import com.example.model.DubbingClip
import java.util.Locale
import java.util.UUID

enum class BatchItemStatus(
    val titleArabic: String,
    val emoji: String
) {
    QUEUED("في قائمة الانتظار", "⏳"),
    PROCESSING("جاري الدبلجة والمعالجة", "⚡"),
    COMPLETED("اكتملت الدبلجة بنجاح", "✅"),
    FAILED("تعذر الإكمال", "❌"),
    PAUSED("متوقف مؤقتاً", "⏸️"),
    SKIPPED("تم التخطي", "⏭️")
}

data class BatchVideoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val videoMetadata: ImportedVideoMetadata,
    val sourceLanguage: DubbingTargetLanguage = DubbingTargetLanguage.ENGLISH,
    val targetLanguage: DubbingTargetLanguage = DubbingTargetLanguage.ARABIC,
    val dubbingStyle: AutoDubbingStyle = AutoDubbingStyle.DOCUMENTARY,
    val pacing: DubbingPacing = DubbingPacing.BALANCED,
    val status: BatchItemStatus = BatchItemStatus.QUEUED,
    val progressFraction: Float = 0f,
    val statusMessage: String = "في قائمة الانتظار للبدء",
    val processedSeconds: Int = 0,
    val totalSeconds: Int = videoMetadata.durationSeconds,
    val estimatedSecondsRemaining: Int = 0,
    val resultClip: DubbingClip? = null,
    val errorMessage: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() = videoMetadata.formattedDuration

    val formattedProgressTime: String
        get() {
            val processedMin = processedSeconds / 60
            val processedSec = processedSeconds % 60
            val totalMin = totalSeconds / 60
            val totalSec = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d / %02d:%02d", processedMin, processedSec, totalMin, totalSec)
        }
}

data class BatchProcessingSessionState(
    val queue: List<BatchVideoItem> = emptyList(),
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentItemIndex: Int = -1,
    val currentItemId: String? = null,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val overallProgressFraction: Float = 0f,
    val sessionStatusMessage: String = "جاهز لبدء معالجة الدُفعة",
    val globalTargetLanguage: DubbingTargetLanguage? = null,
    val globalDubbingStyle: AutoDubbingStyle? = null,
    val globalPacing: DubbingPacing? = null,
    val totalQueueDurationSeconds: Int = 0,
    val processedQueueDurationSeconds: Int = 0
) {
    val totalCount: Int
        get() = queue.size

    val pendingCount: Int
        get() = queue.count { it.status == BatchItemStatus.QUEUED }

    val formattedTotalDuration: String
        get() {
            val minutes = totalQueueDurationSeconds / 60
            val seconds = totalQueueDurationSeconds % 60
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
}
