package com.example.update.play

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Core In-App Update Engine
 * Supports both Immediate and Flexible update modes to seamlessly transition
 * users from older releases to newer versions with zero manual friction.
 */
class PlayInAppUpdateManager(private val context: Context) {

    private val tag = "PlayInAppUpdate"
    val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _isUpdateDownloaded = MutableStateFlow(false)
    val isUpdateDownloaded: StateFlow<Boolean> = _isUpdateDownloaded.asStateFlow()

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    private val _downloadProgressPercentage = MutableStateFlow(0)
    val downloadProgressPercentage: StateFlow<Int> = _downloadProgressPercentage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val installStateUpdatedListener = InstallStateUpdatedListener { state: InstallState ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                if (totalBytes > 0) {
                    val percent = ((bytesDownloaded * 100) / totalBytes).toInt()
                    _downloadProgressPercentage.value = percent
                    _statusMessage.value = "جارٍ تنزيل التحديث في الخلفية: $percent%"
                }
            }
            InstallStatus.DOWNLOADED -> {
                _isUpdateDownloaded.value = true
                _statusMessage.value = "تم تنزيل التحديث بنجاح! جاهز للتثبيت الفوري 🚀"
            }
            InstallStatus.INSTALLING -> {
                _statusMessage.value = "جارٍ تثبيت التحديث الجديد..."
            }
            InstallStatus.INSTALLED -> {
                _isUpdateDownloaded.value = false
                _statusMessage.value = "تم اكتمال التثبيت بنجاح 🎉"
            }
            InstallStatus.FAILED -> {
                _statusMessage.value = "تعذر إكمال التثبيت: رمز الخطأ ${state.installErrorCode()}"
            }
            InstallStatus.CANCELED -> {
                _statusMessage.value = "تم إلغاء عملية التحديث من قبل المستخدم"
            }
            else -> {}
        }
    }

    init {
        try {
            appUpdateManager.registerListener(installStateUpdatedListener)
        } catch (e: Exception) {
            Log.w(tag, "Failed to register Play In-App Update listener: ${e.message}")
        }
    }

    /**
     * Checks for available updates and starts immediate or flexible flow.
     */
    fun checkAndStartUpdate(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>? = null,
        preferImmediate: Boolean = false,
        requestCode: Int = 1001,
        onUpdateNotAvailable: () -> Unit = {}
    ) {
        try {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                val availability = appUpdateInfo.updateAvailability()
                Log.d(tag, "Play In-App Update availability: $availability, availableVersionCode: ${appUpdateInfo.availableVersionCode()}")

                if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                    _isUpdateAvailable.value = true
                    
                    // Determine update type
                    val updateType = if (preferImmediate && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        AppUpdateType.IMMEDIATE
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        AppUpdateType.FLEXIBLE
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        AppUpdateType.IMMEDIATE
                    } else {
                        null
                    }

                    if (updateType != null) {
                        val options = AppUpdateOptions.newBuilder(updateType).build()
                        if (launcher != null) {
                            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
                        } else {
                            @Suppress("DEPRECATION")
                            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateType, activity, requestCode)
                        }
                    } else {
                        onUpdateNotAvailable()
                    }
                } else {
                    _isUpdateAvailable.value = false
                    onUpdateNotAvailable()
                }
            }.addOnFailureListener { error ->
                Log.w(tag, "Play In-App Update check failed: ${error.message}")
                _statusMessage.value = "تعذر الاتصال بـ Google Play: ${error.message}"
                onUpdateNotAvailable()
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception during Play In-App Update check: ${e.message}")
            onUpdateNotAvailable()
        }
    }

    /**
     * Resume pending immediate update or complete flexible update in onResume()
     */
    fun resumeUpdateIfNeeded(
        activity: Activity,
        requestCode: Int = 1001
    ) {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    _isUpdateDownloaded.value = true
                    _statusMessage.value = "التحديث جاهز للتثبيت! اضغط لإعادة التشغيل الآن."
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // Resume immediate update
                    @Suppress("DEPRECATION")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        requestCode
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to resume Play update: ${e.message}")
        }
    }

    /**
     * Completes flexible update by restarting the application
     */
    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            Log.e(tag, "Failed to complete update: ${e.message}")
        }
    }

    fun onDestroy() {
        try {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        } catch (_: Exception) {}
    }
}
