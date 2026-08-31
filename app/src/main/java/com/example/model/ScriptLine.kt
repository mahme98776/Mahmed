package com.example.model

data class ScriptLine(
    val id: String,
    val characterName: String,
    val characterAvatar: String, // emoji or icon key
    val textArabic: String,
    val textOriginal: String = "",
    val startSeconds: Float,
    val endSeconds: Float,
    val voiceType: String = "ARABIC_MALE", // ARABIC_MALE, ARABIC_FEMALE, CARTOON, DRAMATIC, ENTHUSIASTIC
    val isDubbed: Boolean = false,
    val customAudioPath: String? = null,
    val speakerGender: String = "MALE", // MALE, FEMALE, CHILD
    val genderConfidence: Int = 90
)
