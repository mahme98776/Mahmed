package com.example.security

import android.accounts.Account
import android.accounts.AccountManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat

/**
 * DeviceAccountSecurityManager
 * 
 * إدارة أمان الحسابات والتحقق من تواجد البريد الإلكتروني على الجهاز (Device-Bound Security):
 * 1. فحص تواجد حساب Google أو حساب النظام على هذا الجهاز الفعلي (AccountManager).
 * 2. توفير نافذة النظام الأصلية لاختيار وتأكيد حساب من حسابات الجهاز (newChooseAccountIntent).
 * 3. فحص مستوى أمان شاشة القفل وعتاد القياسات الحيوية (بصمة الوجه وبصمة الإصبع).
 * 4. التحقق من سلامة البريد وصلاحيته لمنع هجمات الاستيلاء على الحسابات أو استخدام بريد وهمي.
 */
object DeviceAccountSecurityManager {

    private const val PREFS_NAME = "device_account_security_store"
    private const val KEY_VERIFIED_EMAILS = "verified_device_emails_set"
    private const val KEY_DEVICE_TRUST_TOKEN = "device_hardware_trust_token"

    sealed class DeviceAccountStatus {
        data class VerifiedOnDevice(val email: String, val accountType: String) : DeviceAccountStatus()
        data class NotOnDevice(val enteredEmail: String, val availableDeviceAccountsCount: Int) : DeviceAccountStatus()
        data object UnknownRequiresPicker : DeviceAccountStatus()
    }

    data class DeviceSecuritySummary(
        val isDeviceLockSecure: Boolean,
        val hasBiometricHardware: Boolean,
        val isBiometricEnrolled: Boolean,
        val supportsFaceRecognition: Boolean,
        val supportsFingerprint: Boolean,
        val registeredGoogleAccounts: List<String>,
        val securityRatingScore: Int, // 0 - 100
        val securityAdviceArabic: String
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * استعلام الحسابات المسجلة في إعدادات نظام أندرويد لهذا الجهاز (com.google)
     */
    fun getRegisteredGoogleAccounts(context: Context): List<String> {
        val accountsList = mutableListOf<String>()
        try {
            val accountManager = AccountManager.get(context)
            val accounts: Array<Account> = accountManager.getAccountsByType("com.google")
            for (acc in accounts) {
                if (!acc.name.isNullOrBlank() && acc.name.contains("@")) {
                    accountsList.add(acc.name.trim().lowercase())
                }
            }
        } catch (_: SecurityException) {
            // Android 8.0+ قد يقيد الوصول المباشر إلا إذا تم عبر منتقي الحسابات
        } catch (_: Exception) {}

        // أيضاً نقوم بدمج الحسابات التي تم التحقق من تواجدها على هذا الجهاز مسبقاً عبر منتقي الحسابات
        val savedVerified = getVerifiedDeviceEmails(context)
        for (email in savedVerified) {
            if (!accountsList.contains(email)) {
                accountsList.add(email)
            }
        }

        return accountsList.distinct()
    }

    /**
     * إنشاء Intent النظام الرسمي لاختيار حساب Google مسجل على هذا الجهاز
     * لا يتطلب أي أذونات خطرة ويعمل على كافة إصدارات أندرويد بأعلى معايير أمان Google Play.
     */
    fun createDeviceAccountPickerIntent(currentSelectedEmail: String? = null): Intent {
        val selectedAccount = if (!currentSelectedEmail.isNullOrBlank()) {
            Account(currentSelectedEmail.trim(), "com.google")
        } else null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AccountManager.newChooseAccountIntent(
                selectedAccount,
                null,
                arrayOf("com.google"),
                "اختر حسابك المسجل على هذا الجهاز لربطه بالدخول الآمن والمصادقة الحيوية 🛡️",
                null,
                null,
                null
            )
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(
                selectedAccount,
                null,
                arrayOf("com.google"),
                true,
                "اختر حسابك المسجل على هذا الجهاز",
                null,
                null,
                null
            )
        }
    }

    /**
     * التحقق مما إذا كان البريد المدخل متواجداً ومسجلاً بالفعل على هذا الجهاز
     */
    fun checkEmailPresenceOnDevice(context: Context, emailInput: String): DeviceAccountStatus {
        val cleanEmail = emailInput.trim().lowercase()
        if (cleanEmail.isBlank()) return DeviceAccountStatus.UnknownRequiresPicker

        // 1. فحص في الحسابات الموثقة على هذا الجهاز
        val deviceAccounts = getRegisteredGoogleAccounts(context)
        if (deviceAccounts.any { it.equals(cleanEmail, ignoreCase = true) }) {
            return DeviceAccountStatus.VerifiedOnDevice(cleanEmail, "Google Android Account")
        }

        // 2. فحص السجل المحفوظ محلياً للحسابات التي تم التحقق من هويتها على الجهاز
        if (isEmailPreviouslyVerifiedOnDevice(context, cleanEmail)) {
            return DeviceAccountStatus.VerifiedOnDevice(cleanEmail, "Device Verified Token")
        }

        // 3. الحساب غير موجود على هذا الجهاز
        return DeviceAccountStatus.NotOnDevice(cleanEmail, deviceAccounts.size)
    }

    /**
     * تسجيل بريد إلكتروني كحساب تم التحقق من تواجده وامتلاكه على هذا الجهاز
     */
    fun recordVerifiedDeviceEmail(context: Context, email: String) {
        val clean = email.trim().lowercase()
        if (clean.isBlank()) return
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_VERIFIED_EMAILS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(clean)
        prefs.edit().putStringSet(KEY_VERIFIED_EMAILS, currentSet).apply()
    }

    /**
     * استرجاع قائمة الإيميلات التي تم التحقق من ارتباطها بهذا الجهاز
     */
    fun getVerifiedDeviceEmails(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_VERIFIED_EMAILS, emptySet()) ?: emptySet()
    }

    /**
     * فحص هل البريد تم التحقق منه مسبقاً على هذا الجهاز
     */
    fun isEmailPreviouslyVerifiedOnDevice(context: Context, email: String): Boolean {
        val clean = email.trim().lowercase()
        return getVerifiedDeviceEmails(context).contains(clean)
    }

    /**
     * فحص أمان قفل الشاشة (PIN, النمط، كلمة المرور أو البصمة)
     */
    fun isDeviceScreenLockSecure(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguardManager?.isDeviceSecure ?: false
    }

    /**
     * توليد ملخص شامل لمستوى أمان الجهاز وتسجيل الدخول
     */
    fun evaluateDeviceSecurity(context: Context): DeviceSecuritySummary {
        val keyguardSecure = isDeviceScreenLockSecure(context)
        val biometricManager = BiometricManager.from(context)
        val bioAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        val hasBiometricHardware = bioAuth != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
                bioAuth != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
        val isBiometricEnrolled = bioAuth == BiometricManager.BIOMETRIC_SUCCESS

        val pm = context.packageManager
        val hasFrontCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        val hasFingerprintSensor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        } else false

        val registeredAccounts = getRegisteredGoogleAccounts(context)

        // حساب نقاط الأمان
        var score = 30
        if (keyguardSecure) score += 25
        if (isBiometricEnrolled) score += 25
        if (registeredAccounts.isNotEmpty()) score += 20

        val advice = when {
            score >= 85 -> "مستوى أمان ممتاز 🛡️: الجهاز محمي بقفل الشاشة والقياسات الحيوية وحسابات Google موثقة."
            score >= 60 -> "مستوى أمان جيد: يُنصح بتفعيل المصادقة بالبصمة أو الوجه وتأكيد بريد الحساب على الجهاز."
            else -> "تنبيه أمني ⚠️: قفل الشاشة غير مفعل أو القياسات الحيوية غير معينة. يرجى تفعيل قفل الشاشة لحماية بياناتك."
        }

        return DeviceSecuritySummary(
            isDeviceLockSecure = keyguardSecure,
            hasBiometricHardware = hasBiometricHardware,
            isBiometricEnrolled = isBiometricEnrolled,
            supportsFaceRecognition = hasFrontCamera,
            supportsFingerprint = hasFingerprintSensor,
            registeredGoogleAccounts = registeredAccounts,
            securityRatingScore = score.coerceIn(0, 100),
            securityAdviceArabic = advice
        )
    }

    /**
     * التحقق الصارم من صحة صياغة البريد ومنع الحقن والبريد العشوائي
     */
    fun isValidEmailFormat(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.length < 6 || trimmed.length > 254) return false
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(trimmed) && !trimmed.contains(" ") && !trimmed.contains("..")
    }
}
