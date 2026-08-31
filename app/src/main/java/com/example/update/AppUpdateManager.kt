package com.example.update

import android.content.Context
import android.content.SharedPreferences
import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel
import com.example.update.model.UpdateCheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppUpdateManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_updates_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current App Installed Version
    val currentInstalledVersionCode: Int = 1
    val currentInstalledVersionName: String = "1.0.0"

    private val _releases = MutableStateFlow<List<AppRelease>>(emptyList())
    val releases: StateFlow<List<AppRelease>> = _releases.asStateFlow()

    private val _latestRelease = MutableStateFlow<AppRelease?>(null)
    val latestRelease: StateFlow<AppRelease?> = _latestRelease.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult?>(null)
    val updateCheckResult: StateFlow<UpdateCheckResult?> = _updateCheckResult.asStateFlow()

    private val _serverStatusMessage = MutableStateFlow<String?>(null)
    val serverStatusMessage: StateFlow<String?> = _serverStatusMessage.asStateFlow()

    init {
        loadReleases()
    }

    private fun loadReleases() {
        val savedJson = prefs.getString("saved_releases", null)
        if (savedJson.isNullOrBlank()) {
            val defaultReleases = getInitialDefaultReleases()
            _releases.value = defaultReleases
            _latestRelease.value = defaultReleases.maxByOrNull { it.versionCode }
            saveReleasesToPrefs(defaultReleases)
        } else {
            try {
                val list = parseReleasesFromJson(savedJson)
                if (list.isEmpty()) {
                    val defaultReleases = getInitialDefaultReleases()
                    _releases.value = defaultReleases
                    _latestRelease.value = defaultReleases.maxByOrNull { it.versionCode }
                } else {
                    _releases.value = list
                    _latestRelease.value = list.maxByOrNull { it.versionCode }
                }
            } catch (e: Exception) {
                val defaultReleases = getInitialDefaultReleases()
                _releases.value = defaultReleases
                _latestRelease.value = defaultReleases.maxByOrNull { it.versionCode }
            }
        }
    }

    private fun getInitialDefaultReleases(): List<AppRelease> {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return listOf(
            AppRelease(
                id = "rel_v1_2",
                versionCode = 2,
                versionName = "1.2.0",
                releaseTitle = "إطلاق ميزة حفظ وتصدير الفيديو المدمج وموقع mody.org 🚀",
                releaseNotesArabic = """
                    • إضافة ميزة تصدير الفيديو المدمج MP4 مع الصوت المسجل والترجمة بدقة فائقة.
                    • توفير موقع ويب mody.org للمستخدم مع لوحة تحكم خاصة بالمطور لرفع حزم APK.
                    • تحسين سرعة معالجة وتوليد أصوات الذكاء الاصطناعي بنسبة 40%.
                    • ميزة المزامنة الصوتية بدقة الميلي ثانية مع موجات الصوت Dual-Track.
                """.trimIndent(),
                releaseNotesEnglish = "Added merged MP4 video export, mody.org web portal, APK uploader for developers.",
                downloadUrl = "/download/rel_v1_2.apk",
                apkSizeMb = 18.2,
                releaseDate = currentDate,
                isCritical = false,
                downloadCount = 380,
                channel = ReleaseChannel.STABLE,
                apkFileName = "mody-dubbing-v1.2.0.apk"
            ),
            AppRelease(
                id = "rel_v1_0",
                versionCode = 1,
                versionName = "1.0.0",
                releaseTitle = "الإصدار الأولي لتطبيق استوديو دبلجة المقاطع العربي 🎬",
                releaseNotesArabic = """
                    • إطلاق النسخة الأولى من استوديو الدبلجة العربي.
                    • تسجيل الصوت ومزامنته مع المشاهد الكرتونية والسينمائية.
                    • مكتبة أصوات ومؤثرات صوتية متكاملة.
                """.trimIndent(),
                releaseNotesEnglish = "Initial release of Arabic Video Dubbing Studio.",
                downloadUrl = "/download/rel_v1_0.apk",
                apkSizeMb = 16.8,
                releaseDate = "2026-08-01",
                isCritical = false,
                downloadCount = 890,
                channel = ReleaseChannel.STABLE,
                apkFileName = "mody-dubbing-v1.0.0.apk"
            )
        )
    }

    fun publishNewRelease(
        versionName: String,
        versionCode: Int,
        releaseTitle: String,
        releaseNotesArabic: String,
        releaseNotesEnglish: String = "",
        apkSizeMb: Double = 18.5,
        downloadUrl: String = "/download/latest.apk",
        isCritical: Boolean = false,
        channel: ReleaseChannel = ReleaseChannel.STABLE,
        apkFileName: String = ""
    ): AppRelease {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val generatedFileName = if (apkFileName.isNotBlank()) apkFileName else "mody-dubbing-v${versionName.trim()}.apk"
        val newRelease = AppRelease(
            id = "rel_${UUID.randomUUID().toString().take(8)}",
            versionCode = versionCode,
            versionName = versionName.trim(),
            releaseTitle = releaseTitle.trim(),
            releaseNotesArabic = releaseNotesArabic.trim(),
            releaseNotesEnglish = releaseNotesEnglish.trim(),
            downloadUrl = downloadUrl,
            apkSizeMb = apkSizeMb,
            releaseDate = currentDate,
            isCritical = isCritical,
            downloadCount = 0,
            channel = channel,
            apkFileName = generatedFileName
        )

        val updatedList = listOf(newRelease) + _releases.value.filter { it.versionCode != versionCode }
        _releases.value = updatedList
        _latestRelease.value = updatedList.maxByOrNull { it.versionCode }
        saveReleasesToPrefs(updatedList)

        // Automatically trigger update check notification
        checkForUpdates()
        return newRelease
    }

    fun deleteRelease(releaseId: String) {
        val updatedList = _releases.value.filter { it.id != releaseId }
        _releases.value = updatedList
        _latestRelease.value = updatedList.maxByOrNull { it.versionCode }
        saveReleasesToPrefs(updatedList)
    }

    fun incrementDownloadCount(releaseId: String) {
        val updatedList = _releases.value.map {
            if (it.id == releaseId) it.copy(downloadCount = it.downloadCount + 1) else it
        }
        _releases.value = updatedList
        _latestRelease.value = updatedList.maxByOrNull { it.versionCode }
        saveReleasesToPrefs(updatedList)
    }

    fun checkForUpdates(): UpdateCheckResult {
        val latest = _latestRelease.value ?: _releases.value.maxByOrNull { it.versionCode }
        val isUpdateAvailable = latest != null && latest.versionCode > currentInstalledVersionCode
        val result = UpdateCheckResult(
            isUpdateAvailable = isUpdateAvailable,
            latestRelease = latest,
            currentVersionCode = currentInstalledVersionCode,
            currentVersionName = currentInstalledVersionName,
            isCritical = latest?.isCritical ?: false
        )
        _updateCheckResult.value = result
        return result
    }

    fun performAsyncUpdateCheck(onResult: (UpdateCheckResult) -> Unit = {}) {
        scope.launch {
            _isCheckingUpdates.value = true
            delay(1200) // Realistic check network latency
            val result = checkForUpdates()
            _isCheckingUpdates.value = false
            onResult(result)
        }
    }

    fun simulateDownloadAndInstallUpdate(
        release: AppRelease,
        onComplete: () -> Unit = {}
    ) {
        scope.launch {
            incrementDownloadCount(release.id)
            _downloadProgress.value = 0.05f
            for (step in 1..20) {
                delay(120)
                _downloadProgress.value = (step * 5f) / 100f
            }
            delay(300)
            _downloadProgress.value = 1.0f
            delay(300)
            _downloadProgress.value = null
            onComplete()
        }
    }

    private fun saveReleasesToPrefs(list: List<AppRelease>) {
        val jsonArr = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("versionCode", item.versionCode)
                put("versionName", item.versionName)
                put("releaseTitle", item.releaseTitle)
                put("releaseNotesArabic", item.releaseNotesArabic)
                put("releaseNotesEnglish", item.releaseNotesEnglish)
                put("downloadUrl", item.downloadUrl)
                put("apkSizeMb", item.apkSizeMb)
                put("releaseDate", item.releaseDate)
                put("isCritical", item.isCritical)
                put("downloadCount", item.downloadCount)
                put("channel", item.channel.name)
                put("apkFileName", item.apkFileName)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString("saved_releases", jsonArr.toString()).apply()
    }

    private fun parseReleasesFromJson(jsonStr: String): List<AppRelease> {
        val list = mutableListOf<AppRelease>()
        val jsonArr = JSONArray(jsonStr)
        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            val channel = try {
                ReleaseChannel.valueOf(obj.optString("channel", "STABLE"))
            } catch (e: Exception) {
                ReleaseChannel.STABLE
            }
            list.add(
                AppRelease(
                    id = obj.optString("id", "rel_$i"),
                    versionCode = obj.optInt("versionCode", 1),
                    versionName = obj.optString("versionName", "1.0.0"),
                    releaseTitle = obj.optString("releaseTitle", ""),
                    releaseNotesArabic = obj.optString("releaseNotesArabic", ""),
                    releaseNotesEnglish = obj.optString("releaseNotesEnglish", ""),
                    downloadUrl = obj.optString("downloadUrl", "/download/app.apk"),
                    apkSizeMb = obj.optDouble("apkSizeMb", 18.0),
                    releaseDate = obj.optString("releaseDate", ""),
                    isCritical = obj.optBoolean("isCritical", false),
                    downloadCount = obj.optInt("downloadCount", 0),
                    channel = channel,
                    apkFileName = obj.optString("apkFileName", "")
                )
            )
        }
        return list
    }
}
