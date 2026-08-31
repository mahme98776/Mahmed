package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dubbing_projects")
data class DubbingProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val clipId: String, // e.g. "cartoon_cat_bunny", "nature_lion", "scifi_space", "comedy_chef", "football_match", "custom"
    val clipTitle: String,
    val durationSeconds: Int = 30,
    val originalVolume: Float = 0.3f,
    val dubVolume: Float = 1.0f,
    val bgmVolume: Float = 0.4f,
    val voiceEffect: String = "NORMAL", // NORMAL, DEEP, CHIPMUNK, ROBOT, ECHO, RADIO
    val bgmStyle: String = "CINEMATIC", // NONE, CINEMATIC, FUNNY, DRAMATIC, LOFI
    val scriptJson: String = "",
    val recordedAudioPath: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)
