package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

/**
 * Local Room Entity representing a saved microphone recording.
 */
@Entity(tableName = "voice_recordings")
data class VoiceRecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationSeconds: Float,
    val fileSizeBytes: Long = 0L,
    val voiceEffect: String = "NORMAL",
    val detectedGender: String = "UNKNOWN",
    val associatedScript: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
