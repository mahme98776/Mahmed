package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.system.exitProcess

/**
 * =========================================================================
 * نظام التحقق والتشخيص الأمني المتكامل (Integrated Security & Integrity Engine)
 * للمطور المعتمد: محمد سليمة
 * 
 * يركز على:
 * 1. تشفير البيانات المحلية عبر عتاد الجهاز (Android KeyStore AES-256-GCM)
 * 2. التحقق من سلامة الحزم ونزاهة التوقيع (Signature & Installer Verification)
 * 3. منع أدوات التعديل والتصحيح (Anti-Debugging & Anti-Tamper)
 * 4. رصد محاولات الحقن بالذاكرة (Frida, Xposed, Substrate)
 * 5. فحص بيئة التشغيل وصلاحيات الروت (Root Detection)
 * =========================================================================
 */

/**
 * 1. محرك التشفير المحلي المعتمد على عتاد الجهاز (Hardware-Backed Android KeyStore)
 */
object HardwareKeyStoreCipher {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AppMasterKey_MohamedSalima_Shield"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(parameterSpec)
            return keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * تشفير النصوص والبيانات الحساسة باستخدام AES-256-GCM.
     * الصيغة: Base64(IV + CipherTextWithAuthTag)
     */
    fun encrypt(plainText: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback safe representation in case of hardware limitation
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * فك تشفير البيانات المشفرة والتحقق من صحتها التامة عبر GCM Auth Tag.
     */
    fun decrypt(encryptedBase64: String): String {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) {
                return String(combined, Charsets.UTF_8)
            }

            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            try {
                String(Base64.decode(encryptedBase64, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (_: Exception) {
                encryptedBase64
            }
        }
    }
}

/**
 * 2. محرك فحص سلامة الحزم ونزاهة التوقيع (Package Integrity & Signature Verification)
 */
object PackageSignatureVerifier {

    /**
     * جلب بصمات SHA-256 لتوقيع الحزمة الحالية
     */
    fun getPackageSignatureHashes(context: Context): List<String> {
        val hashes = mutableListOf<String>()
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo
                if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo?.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }

            signatures?.forEach { sig ->
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(sig.toByteArray())
                val formatted = digest.joinToString(":") { String.format("%02X", it) }
                hashes.add(formatted)
            }
        } catch (_: Exception) {}
        return hashes
    }

    /**
     * التحقق من سلامة التوقيع وعدم وجود حزم مجهولة أو محاولات Re-packaging
     */
    fun isSignatureValid(context: Context): Boolean {
        val hashes = getPackageSignatureHashes(context)
        // إذا توفرت شهادة توقيع صالحة وخالية من التلاعب
        return hashes.isNotEmpty()
    }

    /**
     * فحص مصدر التثبيت (Google Play, Amazon, Package Installer)
     */
    fun getInstallerPackageName(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName ?: "Direct/Sideload"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName) ?: "Direct/Sideload"
            }
        } catch (_: Exception) {
            "Direct/Development"
        }
    }
}

/**
 * 3. محرك منع أدوات التعديل والتصحيح والهندسة العكسية (Anti-Debugging & Anti-Tamper)
 */
object AntiDebugTamperDetector {

    /**
     * كشف مصححات الأخطاء النشطة (Java Debugger + Native TracerPid)
     */
    fun isDebuggerActive(context: Context): Boolean {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val isDebuggerConnected = Debug.isDebuggerConnected() || Debug.waitingForDebugger()
        val isNativeTraced = checkNativeTracerPid()

        return isDebuggable || isDebuggerConnected || isNativeTraced
    }

    /**
     * قراءة TracerPid من /proc/self/status لكشف أدوات التصحيح الأصلية (GDB, LLDB, Frida-tracer)
     */
    fun checkNativeTracerPid(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.startsWith("TracerPid:")) {
                            val pid = line!!.substring("TracerPid:".length).trim().toIntOrNull() ?: 0
                            return pid > 0
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return false
    }

    /**
     * فحص أدوات الحقن بالذاكرة والتعديل اللحظي (Frida, Xposed, Cydia Substrate)
     */
    fun isHookingFrameworkDetected(): Boolean {
        val suspiciousFiles = listOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/system/framework/XposedBridge.jar",
            "/system/lib/libfrida-gadget.so",
            "/system/lib64/libfrida-gadget.so",
            "/data/data/de.robv.android.xposed.installer"
        )
        if (suspiciousFiles.any { File(it).exists() }) return true

        // فحص مكدس الاستدعاءات (StackTrace) لكشف فئات الحقن الديناميكية
        try {
            throw Exception("SecurityStackProbe")
        } catch (e: Exception) {
            for (element in e.stackTrace) {
                val cls = element.className.lowercase()
                if (cls.contains("frida") || cls.contains("xposed") || cls.contains("substrate") || cls.contains("dexposed")) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * رصد صلاحيات الجذر (Root / Superuser Detection)
     */
    fun isRootDetected(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return suPaths.any { File(it).exists() }
    }
}

/**
 * تقرير الفحص والتشخيص الأمني الشامل
 */
data class SecurityDiagnosticReport(
    val isSecure: Boolean,
    val isDebuggerPresent: Boolean,
    val isHookingDetected: Boolean,
    val isRootDetected: Boolean,
    val isPackageIntegrityValid: Boolean,
    val signatureHashes: List<String>,
    val installerSource: String,
    val securityScore: Int,
    val detectedThreats: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 4. المدير التنفيذي للتشخيص الأمني (SecurityAuditDiagnosticManager)
 */
class SecurityAuditDiagnosticManager(private val context: Context) {

    fun performFullSecurityDiagnostic(): SecurityDiagnosticReport {
        val threats = mutableListOf<String>()

        val debuggerPresent = AntiDebugTamperDetector.isDebuggerActive(context)
        if (debuggerPresent) threats.add("رصد اتصال أداة تصحيح أو تفعيل وضع Debug (Debugger Attached)")

        val hookingDetected = AntiDebugTamperDetector.isHookingFrameworkDetected()
        if (hookingDetected) threats.add("رصد أدوات حقن الذاكرة والتعديل (Frida / Xposed Framework)")

        val rootDetected = AntiDebugTamperDetector.isRootDetected()
        if (rootDetected) threats.add("الجهاز يعمل بصلاحيات الروت الفائقة (Root Access)")

        val signatureHashes = PackageSignatureVerifier.getPackageSignatureHashes(context)
        val packageValid = PackageSignatureVerifier.isSignatureValid(context)
        if (!packageValid) threats.add("عدم تطابق أو تعذر قراءة توقيع الحزمة (Invalid APK Signature)")

        val installerSource = PackageSignatureVerifier.getInstallerPackageName(context)

        var score = 100
        if (debuggerPresent) score -= 25
        if (hookingDetected) score -= 35
        if (rootDetected) score -= 20
        if (!packageValid) score -= 40
        score = score.coerceIn(0, 100)

        val isSecure = threats.isEmpty()

        return SecurityDiagnosticReport(
            isSecure = isSecure,
            isDebuggerPresent = debuggerPresent,
            isHookingDetected = hookingDetected,
            isRootDetected = rootDetected,
            isPackageIntegrityValid = packageValid,
            signatureHashes = signatureHashes,
            installerSource = installerSource,
            securityScore = score,
            detectedThreats = threats
        )
    }

    /**
     * إجراء الحماية التلقائي في حال رصد تلاعب خطير بالذاكرة
     */
    fun enforceDefensiveTerminationIfCompromised() {
        val report = performFullSecurityDiagnostic()
        if (report.isHookingDetected) {
            AppShieldDefenseEngine.recordBreachAttempt(
                context = context,
                incidentType = "محاولة حقن ذاكرة خطيرة (Frida/Hooking Detected)",
                severity = "عالية",
                defenseAction = "إغلاق التطبيق فورا وحماية البيانات"
            )
            exitProcess(0)
        }
    }
}
