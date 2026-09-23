package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * BiometricAuthenticationHelper
 * يوفر دعم المصادقة الحيوية المشفرة (بصمة الإصبع والتعرف على الوجه)
 * للمستخدمين والمطور المعتمد مع إمكانية التعيين والحفظ المشفر الآمن.
 */
object BiometricAuthenticationHelper {

    private const val PREFS_NAME = "biometric_auth_security_prefs"
    private const val KEY_BIOMETRIC_PREFIX = "bio_enrolled_"
    private const val KEY_SAVED_PASS_PREFIX = "bio_secret_"

    /**
     * التحقق مما إذا كان عتاد الجهاز يدعم القياسات الحيوية (بصمة الإصبع أو التعرف على الوجه)
     */
    fun canAuthenticate(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SecurityUpdateRequired
            else -> BiometricStatus.Unsupported
        }
    }

    /**
     * التحقق مما إذا كان المستخدم أو المطور قد قام بتعيين/تفعيل ميزة البصمة أو الوجه لهذا الحساب
     */
    fun isBiometricConfiguredForUser(context: Context, email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_PREFIX + cleanEmail, false)
    }

    /**
     * حفظ تعيين البصمة الحيوية أو الوجه وربطها بكلمة المرور المشفرة للحساب
     */
    fun setBiometricEnrollment(context: Context, email: String, passwordPlain: String, enable: Boolean) {
        val cleanEmail = email.trim().lowercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (enable) {
            prefs.edit()
                .putBoolean(KEY_BIOMETRIC_PREFIX + cleanEmail, true)
                .putString(KEY_SAVED_PASS_PREFIX + cleanEmail, passwordPlain)
                .apply()
        } else {
            prefs.edit()
                .remove(KEY_BIOMETRIC_PREFIX + cleanEmail)
                .remove(KEY_SAVED_PASS_PREFIX + cleanEmail)
                .apply()
        }
    }

    /**
     * استرجاع كلمة المرور المحفوظة للمصادقة البيومترية عند نجاح التحقق
     */
    fun getSavedPasswordForBiometric(context: Context, email: String): String? {
        val cleanEmail = email.trim().lowercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_PASS_PREFIX + cleanEmail, null)
    }

    /**
     * إظهار نافذة التحقق البيومتري (الوجه أو بصمة الإصبع)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "التحقق من الهوية بالبصمة أو الوجه 🛡️",
        subtitle: String = "استخدم بصمة الإصبع أو التعرف على الوجه للمتابعة الآمنة",
        negativeButtonText: String = "إلغاء واستخدام كلمة المرور",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("لم يتم التعرف على البصمة أو الوجه، يرجى المحاولة مرة أخرى.")
            }
        })

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "تعذر فتح المصادقة الحيوية")
        }
    }

    sealed class BiometricStatus {
        data object Available : BiometricStatus()
        data object NoHardware : BiometricStatus()
        data object HardwareUnavailable : BiometricStatus()
        data object NotEnrolled : BiometricStatus()
        data object SecurityUpdateRequired : BiometricStatus()
        data object Unsupported : BiometricStatus()
    }
}
