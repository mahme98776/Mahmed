package com.example.update.model

enum class ReleaseChannel(val titleAr: String, val badgeColor: Long) {
    STABLE("مستقرة (Stable)", 0xFF10B981),
    BETA("تجريبية (Beta)", 0xFFF59E0B),
    NIGHTLY("تطويرية (Nightly)", 0xFF8B5CF6)
}

data class AppRelease(
    val id: String,
    val versionCode: Int,
    val versionName: String,
    val releaseTitle: String,
    val releaseNotesArabic: String,
    val releaseNotesEnglish: String = "",
    val downloadUrl: String,
    val apkSizeMb: Double,
    val releaseDate: String,
    val isCritical: Boolean = false,
    val downloadCount: Int = 0,
    val channel: ReleaseChannel = ReleaseChannel.STABLE,
    val minAndroidVersion: String = "Android 7.0+",
    val sha256Checksum: String = "",
    val apkFileName: String = ""
)

data class UpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val latestRelease: AppRelease?,
    val currentVersionCode: Int,
    val currentVersionName: String,
    val isCritical: Boolean = false
)
