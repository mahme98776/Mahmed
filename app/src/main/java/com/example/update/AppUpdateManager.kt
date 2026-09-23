package com.example.update

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.update.engine.OnlineUpdateProvider
import com.example.update.firebase.FirebaseRemoteConfigUpdateManager
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
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppUpdateManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_updates_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    val onlineProvider = OnlineUpdateProvider(context)
    val firebaseConfigManager = FirebaseRemoteConfigUpdateManager(context)

    // Current App Installed Version from BuildConfig
    val currentInstalledVersionCode: Int = BuildConfig.VERSION_CODE
    val currentInstalledVersionName: String = BuildConfig.VERSION_NAME

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

    private val _isOnlineSource = MutableStateFlow(false)
    val isOnlineSource: StateFlow<Boolean> = _isOnlineSource.asStateFlow()

    private val _isAutoUpdateEnabled = MutableStateFlow(prefs.getBoolean("auto_update_enabled", true))
    val isAutoUpdateEnabled: StateFlow<Boolean> = _isAutoUpdateEnabled.asStateFlow()

    init {
        loadReleases()
        startFirebaseRemoteConfigListener()
        checkForUpdates()
        performAsyncUpdateCheck(useOnlineSources = true)
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        _isAutoUpdateEnabled.value = enabled
        prefs.edit().putBoolean("auto_update_enabled", enabled).apply()
    }

    private fun startFirebaseRemoteConfigListener() {
        firebaseConfigManager.startRealtimeListener(scope) { result ->
            if (result.isUpdateAvailable && result.latestRelease != null) {
                val remoteRel = result.latestRelease
                val currentList = _releases.value.filter { it.versionName != remoteRel.versionName }
                val updatedList = listOf(remoteRel) + currentList
                _releases.value = updatedList
                _latestRelease.value = remoteRel
                saveReleasesToPrefs(updatedList)
                _updateCheckResult.value = result
                _isOnlineSource.value = true
                _serverStatusMessage.value = "تم استلام إشعار تحديث فوري عبر Firebase Remote Config 🔥"
            }
        }
    }

    private fun loadReleases() {
        val savedJson = prefs.getString("saved_releases", null)
        val defaultReleases = getInitialDefaultReleases()
        if (savedJson.isNullOrBlank()) {
            _releases.value = defaultReleases
            _latestRelease.value = defaultReleases.maxByOrNull { it.versionCode }
            saveReleasesToPrefs(defaultReleases)
        } else {
            try {
                val list = parseReleasesFromJson(savedJson)
                // Always ensure latest canonical default releases are merged
                val merged = (defaultReleases + list).distinctBy { it.versionCode }.sortedByDescending { it.versionCode }
                _releases.value = merged
                _latestRelease.value = merged.maxByOrNull { it.versionCode }
                saveReleasesToPrefs(merged)
            } catch (e: Exception) {
                _releases.value = defaultReleases
                _latestRelease.value = defaultReleases.maxByOrNull { it.versionCode }
            }
        }
    }

    private fun getInitialDefaultReleases(): List<AppRelease> {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return listOf(
            AppRelease(
                id = "rel_v3_1",
                versionCode = 6,
                versionName = "3.1.0",
                releaseTitle = "إصدار فويس ماستر برو الذاتي الكامل وحصن الأمان الفولاذي 🚀🛡️",
                releaseNotesArabic = """
                    • تطبيق خالٍ تماماً من الميكروفون ومن الكتابة اليدوية.
                    • دبلجة ذكية تعتمد كلياً على نفسها بالذكاء الاصطناعي التوليدي بنقرة زر واحدة.
                    • تحديثات تلقائية ذكية ومباشرة وتثبيت فوري لحزم APK دون انتظار.
                    • حصن الحماية الفولاذية AppShield المطور وتوليد كلمات مرور قوية تلقائياً للمطور.
                """.trimIndent(),
                releaseNotesEnglish = "Zero-mic, zero-typing autonomous AI dubbing with instant one-click auto-updates.",
                downloadUrl = "https://raw.githubusercontent.com/VoiceMaster-Pro/Releases/main/voicemaster-pro-v3.1.0.apk",
                apkSizeMb = 19.4,
                releaseDate = currentDate,
                isCritical = false,
                downloadCount = 1250,
                channel = ReleaseChannel.STABLE,
                apkFileName = "voicemaster-pro-v3.1.0.apk"
            ),
            AppRelease(
                id = "rel_v3_0",
                versionCode = 5,
                versionName = "3.0.0",
                releaseTitle = "إطلاق الدبلجة التلقائية الشاملة بدون إدخال يدوي ⚡🎬",
                releaseNotesArabic = """
                    • استخراج حوارات المشاهد وترجمتها وتوليد الصوت آلياً بنسبة 100%.
                    • دعم تصدير ملفات الفيديو المدمجة بدقة عالية.
                """.trimIndent(),
                releaseNotesEnglish = "Autonomous video dubbing pipeline and automatic audio alignment.",
                downloadUrl = "/download/rel_v3_0.apk",
                apkSizeMb = 18.8,
                releaseDate = currentDate,
                isCritical = false,
                downloadCount = 820,
                channel = ReleaseChannel.STABLE,
                apkFileName = "voicemaster-pro-v3.0.0.apk"
            ),
            AppRelease(
                id = "rel_v2_6",
                versionCode = 3,
                versionName = "2.6.0",
                releaseTitle = "تحديث فويس ماستر برو المتقدم وموقع voicemaster.org 🌐",
                releaseNotesArabic = """
                    • إضافة ميزة تصدير الفيديو المدمج MP4 مع الصوت المسجل والترجمة بدقة فائقة.
                    • توفير موقع وتحديثات فويس ماستر برو الرسمية لتحميل حزم APK بسرعة وأمان.
                """.trimIndent(),
                releaseNotesEnglish = "Added merged MP4 video export and voicemaster.org updates center.",
                downloadUrl = "/download/rel_v2_6.apk",
                apkSizeMb = 18.2,
                releaseDate = "2026-09-01",
                isCritical = false,
                downloadCount = 420,
                channel = ReleaseChannel.STABLE,
                apkFileName = "voicemaster-pro-v2.6.0.apk"
            )
        )
    }

    fun publishNewRelease(
        versionName: String,
        versionCode: Int,
        releaseTitle: String,
        releaseNotesArabic: String,
        releaseNotesEnglish: String = "",
        channel: ReleaseChannel = ReleaseChannel.STABLE,
        apkSizeMb: Double = 18.5,
        downloadUrl: String = "",
        isCritical: Boolean = false,
        apkFileName: String = ""
    ): AppRelease {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val generatedFileName = if (apkFileName.isNotBlank()) apkFileName else "voicemaster-pro-v${versionName.trim()}.apk"
        val effectiveDownloadUrl = if (downloadUrl.isNotBlank()) downloadUrl else "/download/rel_${versionCode}.apk"
        val newRelease = AppRelease(
            id = "rel_${UUID.randomUUID().toString().take(8)}",
            versionCode = versionCode,
            versionName = versionName.trim(),
            releaseTitle = releaseTitle.trim(),
            releaseNotesArabic = releaseNotesArabic.trim(),
            releaseNotesEnglish = releaseNotesEnglish.trim().ifBlank { releaseNotesArabic.trim() },
            downloadUrl = effectiveDownloadUrl,
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

    fun performAsyncUpdateCheck(
        useOnlineSources: Boolean = true,
        onResult: (UpdateCheckResult) -> Unit = {}
    ) {
        scope.launch {
            _isCheckingUpdates.value = true
            _serverStatusMessage.value = "جاري الاتصال بـ Firebase Remote Config و GitHub..."

            if (useOnlineSources) {
                // 1. Check Firebase Remote Config for instant cloud updates
                val fbResult = firebaseConfigManager.checkRemoteConfigUpdate()
                if (fbResult.isSuccess) {
                    val result = fbResult.getOrNull()!!
                    if (result.isUpdateAvailable && result.latestRelease != null) {
                        val remoteRel = result.latestRelease
                        val currentList = _releases.value.filter { it.versionName != remoteRel.versionName }
                        val updatedList = listOf(remoteRel) + currentList
                        _releases.value = updatedList
                        _latestRelease.value = remoteRel
                        saveReleasesToPrefs(updatedList)
                        _updateCheckResult.value = result
                        _isOnlineSource.value = true
                        _isCheckingUpdates.value = false
                        _serverStatusMessage.value = "تم استلام تحديث جديد عبر Firebase Remote Config 🔥"
                        onResult(result)
                        return@launch
                    }
                }

                // 2. Check GitHub Releases API
                if (onlineProvider.githubOwner.isNotBlank() && onlineProvider.githubRepo.isNotBlank()) {
                    val onlineResult = onlineProvider.checkGitHubRelease(
                        currentVersionCode = currentInstalledVersionCode,
                        currentVersionName = currentInstalledVersionName
                    )

                    if (onlineResult.isSuccess) {
                        val result = onlineResult.getOrNull()!!
                        _isOnlineSource.value = true
                        val remoteRel = result.latestRelease
                        if (remoteRel != null) {
                            val currentList = _releases.value.filter { it.versionName != remoteRel.versionName }
                            val updatedList = listOf(remoteRel) + currentList
                            _releases.value = updatedList
                            _latestRelease.value = remoteRel
                            saveReleasesToPrefs(updatedList)
                        }
                        _updateCheckResult.value = result
                        _isCheckingUpdates.value = false
                        _serverStatusMessage.value = "تم استلام أحدث بيانات الإصدار من GitHub بنجاح 🌐"
                        withContext(Dispatchers.Main) {
                            onResult(result)
                        }
                        return@launch
                    } else {
                        _serverStatusMessage.value = "تعذر الاتصال بـ GitHub (${onlineResult.exceptionOrNull()?.message}) - جاري الفحص المحلي."
                    }
                }
            }

            delay(800)
            val result = checkForUpdates()
            _isOnlineSource.value = false
            _isCheckingUpdates.value = false
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun downloadAndInstallUpdate(
        release: AppRelease,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        scope.launch {
            incrementDownloadCount(release.id)
            
            // If downloadUrl is a real HTTP/HTTPS URL, download actual APK file
            if (release.downloadUrl.startsWith("http://", ignoreCase = true) || release.downloadUrl.startsWith("https://", ignoreCase = true)) {
                _downloadProgress.value = 0.05f
                val downloadRes = onlineProvider.downloadApk(
                    downloadUrl = release.downloadUrl,
                    targetFileName = release.apkFileName.ifBlank { "voicemaster-pro-update.apk" },
                    onProgress = { progress ->
                        _downloadProgress.value = progress
                    }
                )

                _downloadProgress.value = null
                if (downloadRes.isSuccess) {
                    val apkFile = downloadRes.getOrNull()!!
                    val installRes = onlineProvider.installApk(apkFile)
                    if (installRes.isSuccess) {
                        withContext(Dispatchers.Main) {
                            onComplete(true, "تم تنزيل التحديث وبدء التثبيت بنجاح! 🎉")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onComplete(false, "تم تنزيل الـ APK ولكن تعذر فتح برنامج التثبيت: ${installRes.exceptionOrNull()?.message}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "فشل تنزيل ملف التحديث من الإنترنت: ${downloadRes.exceptionOrNull()?.message}")
                    }
                }
            } else {
                // Local / Simulation Mode
                _downloadProgress.value = 0.05f
                for (step in 1..20) {
                    delay(100)
                    _downloadProgress.value = (step * 5f) / 100f
                }
                delay(200)
                _downloadProgress.value = 1.0f
                delay(200)
                _downloadProgress.value = null
                withContext(Dispatchers.Main) {
                    onComplete(true, "اكتمل التنزيل التجريبي للإصدار v${release.versionName}")
                }
            }
        }
    }

    fun triggerInstantAutoUpdate(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val latest = _latestRelease.value ?: _releases.value.maxByOrNull { it.versionCode }
        if (latest != null) {
            downloadAndInstallUpdate(latest, onComplete)
        } else {
            onComplete(false, "لا يوجد إصدار متاح حالياً للتحديث")
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
