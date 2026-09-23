package com.example.update.firebase

import android.content.Context
import com.example.BuildConfig
import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel
import com.example.update.model.UpdateCheckResult
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firebase Remote Config Update Manager
 * Checks Firebase Remote Config parameters in real-time to notify users when a new version of DubStudio / VoiceMaster Pro
 * is released on GitHub or Google Play Store.
 */
class FirebaseRemoteConfigUpdateManager(private val context: Context) {

    private val _realtimeUpdateResult = MutableStateFlow<UpdateCheckResult?>(null)
    val realtimeUpdateResult: StateFlow<UpdateCheckResult?> = _realtimeUpdateResult.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0 // Instant fetch for update checks
            }
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(
                mapOf(
                    KEY_LATEST_VERSION_CODE to BuildConfig.VERSION_CODE.toLong(),
                    KEY_LATEST_VERSION_NAME to BuildConfig.VERSION_NAME,
                    KEY_RELEASE_TITLE to "إصدار جديد متاح من فويس ماستر برو 🚀",
                    KEY_RELEASE_NOTES to "تحسينات مستمرة في دبلجة الفيديو وهندسة الصوت بالذكاء الاصطناعي.",
                    KEY_DOWNLOAD_URL to "https://github.com/mahme98776/VoiceMasterPro/releases/latest",
                    KEY_IS_CRITICAL to false,
                    KEY_STORE_URL to "https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            )
        }
    }

    companion object {
        const val KEY_LATEST_VERSION_CODE = "latest_version_code"
        const val KEY_LATEST_VERSION_NAME = "latest_version_name"
        const val KEY_RELEASE_TITLE = "release_title"
        const val KEY_RELEASE_NOTES = "release_notes"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_IS_CRITICAL = "is_critical"
        const val KEY_STORE_URL = "store_url"
    }

    /**
     * Starts listening to real-time Firebase Remote Config changes.
     */
    fun startRealtimeListener(
        scope: CoroutineScope,
        onUpdateDetected: (UpdateCheckResult) -> Unit = {}
    ) {
        if (_isListening.value) return
        _isListening.value = true

        try {
            remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    scope.launch(Dispatchers.IO) {
                        remoteConfig.activate().await()
                        val result = evaluateCurrentConfig()
                        if (result.isUpdateAvailable) {
                            _realtimeUpdateResult.value = result
                            withContext(Dispatchers.Main) {
                                onUpdateDetected(result)
                            }
                        }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    // Fallback to manual periodic check if realtime stream encounters connection error
                }
            })
        } catch (_: Exception) {
            // Realtime listener not supported on older versions, fallback to fetchAndActivate
        }

        // Also perform an initial proactive check
        scope.launch(Dispatchers.IO) {
            val initial = checkRemoteConfigUpdate()
            if (initial.isSuccess) {
                val res = initial.getOrNull()
                if (res != null && res.isUpdateAvailable) {
                    _realtimeUpdateResult.value = res
                    withContext(Dispatchers.Main) {
                        onUpdateDetected(res)
                    }
                }
            }
        }
    }

    /**
     * Evaluates current activated Remote Config values against local BuildConfig.
     */
    fun evaluateCurrentConfig(): UpdateCheckResult {
        val remoteCode = remoteConfig.getLong(KEY_LATEST_VERSION_CODE).toInt()
        val remoteName = remoteConfig.getString(KEY_LATEST_VERSION_NAME).ifBlank { BuildConfig.VERSION_NAME }
        val title = remoteConfig.getString(KEY_RELEASE_TITLE)
        val notes = remoteConfig.getString(KEY_RELEASE_NOTES)
        val downloadUrl = remoteConfig.getString(KEY_DOWNLOAD_URL)
        val isCritical = remoteConfig.getBoolean(KEY_IS_CRITICAL)
        val storeUrl = remoteConfig.getString(KEY_STORE_URL)

        val currentCode = BuildConfig.VERSION_CODE
        val currentName = BuildConfig.VERSION_NAME

        val isUpdateAvailable = remoteCode > currentCode || isVersionNewer(remoteName, currentName)

        val release = AppRelease(
            id = "firebase_rc_$remoteCode",
            versionCode = remoteCode,
            versionName = remoteName,
            releaseTitle = title.ifBlank { "تحديث فويس ماستر برو v$remoteName" },
            releaseNotesArabic = notes.ifBlank { "إصدار جديد متاح عبر السحابة مع تحسينات الأداء ومزامنة الفيديو." },
            releaseNotesEnglish = notes,
            downloadUrl = downloadUrl.ifBlank { storeUrl },
            apkSizeMb = 19.5,
            releaseDate = "الآن",
            isCritical = isCritical,
            downloadCount = 0,
            channel = ReleaseChannel.STABLE,
            apkFileName = "voicemaster-pro-v$remoteName.apk"
        )

        return UpdateCheckResult(
            isUpdateAvailable = isUpdateAvailable,
            latestRelease = release,
            currentVersionCode = currentCode,
            currentVersionName = currentName,
            isCritical = isCritical
        )
    }

    /**
     * Fetches and activates latest parameters from Firebase Remote Config.
     */
    suspend fun checkRemoteConfigUpdate(): Result<UpdateCheckResult> = withContext(Dispatchers.IO) {
        try {
            remoteConfig.fetchAndActivate().await()
            val result = evaluateCurrentConfig()
            _realtimeUpdateResult.value = result
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Simulates receiving a remote config update with higher version code for testing.
     */
    fun simulateRemoteUpdate(versionName: String = "2.8.0", versionCode: Int = 5, isCritical: Boolean = false): UpdateCheckResult {
        val simRelease = AppRelease(
            id = "firebase_sim_$versionCode",
            versionCode = versionCode,
            versionName = versionName,
            releaseTitle = "تحديث فويس ماستر برو السحابي v$versionName 🚀",
            releaseNotesArabic = "• تحسينات الأمان والتعامل مع أحدث خوارزميات الذكاء الاصطناعي.\n• استقرار أعلى في مزامنة مسارات الصوت مع الفيديو.",
            releaseNotesEnglish = "Cloud update with enhanced security and performance.",
            downloadUrl = "https://github.com/mahme98776/VoiceMasterPro/releases/latest",
            apkSizeMb = 21.0,
            releaseDate = "اليوم",
            isCritical = isCritical,
            downloadCount = 142,
            channel = ReleaseChannel.STABLE,
            apkFileName = "voicemaster-pro-v$versionName.apk"
        )

        val simResult = UpdateCheckResult(
            isUpdateAvailable = true,
            latestRelease = simRelease,
            currentVersionCode = BuildConfig.VERSION_CODE,
            currentVersionName = BuildConfig.VERSION_NAME,
            isCritical = isCritical
        )
        _realtimeUpdateResult.value = simResult
        return simResult
    }

    fun dismissUpdatePrompt() {
        _realtimeUpdateResult.value = null
    }

    private fun isVersionNewer(remote: String, current: String): Boolean {
        val rParts = remote.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(rParts.size, cParts.size)) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
