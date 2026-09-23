package com.example.update.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel
import com.example.update.model.UpdateCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Online Cloud & GitHub OTA Update Provider
 * Supports fetching releases directly from GitHub Releases API or custom version.json endpoints.
 */
class OnlineUpdateProvider(private val context: Context) {

    private val prefs = context.getSharedPreferences("online_update_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GITHUB_OWNER = "github_owner"
        private const val KEY_GITHUB_REPO = "github_repo"
        private const val KEY_CUSTOM_API_URL = "custom_api_url"
        private const val KEY_AUTO_CHECK_ENABLED = "auto_check_enabled"
        
        // Defaults
        const val DEFAULT_GITHUB_OWNER = "mahme98776"
        const val DEFAULT_GITHUB_REPO = "VoiceMasterPro"
    }

    var githubOwner: String
        get() = prefs.getString(KEY_GITHUB_OWNER, DEFAULT_GITHUB_OWNER) ?: DEFAULT_GITHUB_OWNER
        set(value) = prefs.edit().putString(KEY_GITHUB_OWNER, value.trim()).apply()

    var githubRepo: String
        get() = prefs.getString(KEY_GITHUB_REPO, DEFAULT_GITHUB_REPO) ?: DEFAULT_GITHUB_REPO
        set(value) = prefs.edit().putString(KEY_GITHUB_REPO, value.trim()).apply()

    var customApiUrl: String
        get() = prefs.getString(KEY_CUSTOM_API_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_API_URL, value.trim()).apply()

    var isAutoCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CHECK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_CHECK_ENABLED, value).apply()

    /**
     * Check GitHub Releases API for the latest release
     */
    suspend fun checkGitHubRelease(
        owner: String = githubOwner,
        repo: String = githubRepo,
        currentVersionCode: Int,
        currentVersionName: String
    ): Result<UpdateCheckResult> = withContext(Dispatchers.IO) {
        try {
            if (owner.isBlank() || repo.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("اسم المستخدم أو المستودع فارغ"))
            }

            val endpoint = if (customApiUrl.isNotBlank()) {
                customApiUrl
            } else {
                "https://api.github.com/repos/$owner/$repo/releases/latest"
            }

            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "VoiceMasterPro-AndroidApp")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val release = parseGitHubReleaseJson(jsonText)
                
                val isNewer = isVersionNewer(release.versionName, release.versionCode, currentVersionName, currentVersionCode)
                val checkResult = UpdateCheckResult(
                    isUpdateAvailable = isNewer,
                    latestRelease = release,
                    currentVersionCode = currentVersionCode,
                    currentVersionName = currentVersionName,
                    isCritical = release.isCritical
                )
                Result.success(checkResult)
            } else if (responseCode == 404) {
                Result.failure(Exception("لم يتم العثور على إصدارات منشورة في مستودع GitHub ($owner/$repo) بعد."))
            } else {
                val err = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Result.failure(Exception("استجابة الخادم: $responseCode - $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse GitHub Release JSON into AppRelease
     */
    private fun parseGitHubReleaseJson(jsonString: String): AppRelease {
        val root = JSONObject(jsonString)
        val tagName = root.optString("tag_name", "v1.0.0").removePrefix("v").removePrefix("V")
        val name = root.optString("name", "تحديث جديد")
        val body = root.optString("body", "تحسينات عامة وإصلاحات في الأداء.")
        val publishedAt = root.optString("published_at", "")
        
        var apkDownloadUrl = ""
        var apkFileName = "voicemaster-pro-v$tagName.apk"
        var apkSizeMb = 19.5

        val assets = root.optJSONArray("assets")
        if (assets != null && assets.length() > 0) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val assetName = asset.optString("name", "")
                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    apkDownloadUrl = asset.optString("browser_download_url", "")
                    apkFileName = assetName
                    val sizeBytes = asset.optLong("size", 0L)
                    if (sizeBytes > 0) {
                        apkSizeMb = (sizeBytes / (1024.0 * 1024.0) * 10).toInt() / 10.0
                    }
                    break
                }
            }
        }

        // Parse version code from tag or version string (e.g., 2.7.0 -> 270)
        val derivedVersionCode = parseVersionNameToCode(tagName)
        val formattedDate = formatDateString(publishedAt)

        return AppRelease(
            id = "gh_${root.optLong("id", System.currentTimeMillis())}",
            versionCode = derivedVersionCode,
            versionName = tagName,
            releaseTitle = name.ifBlank { "إصدار فويس ماستر برو v$tagName" },
            releaseNotesArabic = body,
            releaseNotesEnglish = body,
            downloadUrl = apkDownloadUrl.ifBlank { root.optString("html_url", "") },
            apkSizeMb = apkSizeMb,
            releaseDate = formattedDate,
            isCritical = body.contains("critical", ignoreCase = true) || body.contains("إجباري"),
            downloadCount = 0,
            channel = if (root.optBoolean("prerelease", false)) ReleaseChannel.BETA else ReleaseChannel.STABLE,
            apkFileName = apkFileName
        )
    }

    /**
     * Download APK file with progress callback
     */
    suspend fun downloadApk(
        downloadUrl: String,
        targetFileName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            // Handle HTTP Redirects
            var currentConnection = connection
            var redirectCount = 0
            while (currentConnection.responseCode in listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, 307, 308) && redirectCount < 5) {
                val newUrl = currentConnection.getHeaderField("Location")
                currentConnection.disconnect()
                currentConnection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 30000
                }
                redirectCount++
            }

            val fileLength = currentConnection.contentLength
            val downloadsDir = File(context.cacheDir, "apk_updates").apply { if (!exists()) mkdirs() }
            val outputFile = File(downloadsDir, targetFileName)

            currentConnection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            val progress = (total.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            onProgress(1.0f)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prompt Android System Package Installer to install downloaded APK
     */
    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("ملف التحديث غير موجود على الجهاز"))
            }

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isVersionNewer(
        remoteName: String,
        remoteCode: Int,
        currentName: String,
        currentCode: Int
    ): Boolean {
        if (remoteCode > currentCode) return true
        val remoteParts = remoteName.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentName.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(remoteParts.size, currentParts.size)) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun parseVersionNameToCode(versionName: String): Int {
        val parts = versionName.split(".").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            1 -> parts[0] * 100
            2 -> parts[0] * 100 + parts[1] * 10
            3 -> parts[0] * 100 + parts[1] * 10 + parts[2]
            else -> 100
        }
    }

    private fun formatDateString(isoString: String): String {
        return try {
            if (isoString.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else {
                isoString.take(10)
            }
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}
