package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthenticationHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.FirebaseAuthAndFirestoreService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * شاشة الدخول والتسجيل المشفرة لـ Firebase Authentication و Gemini Cloud Computing
 * تتيح:
 * 1. تسجيل الدخول لمستخدم مسجل سابقاً.
 * 2. إنشاء حساب جديد مع التحقق الصارم برمز التأكيد (OTP) المرسل للبريد.
 * 3. المتابعة كزائر أو الدخول التلقائي للمطور المعتمد.
 */
@Composable
fun GoogleSignInGateScreen(
    authService: FirebaseAuthAndFirestoreService,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncStatus by authService.syncStatus.collectAsState()
    val isSyncing by authService.isSyncing.collectAsState()

    // Tab state: 0 = Sign In, 1 = Create Account
    var selectedAuthMode by remember { mutableIntStateOf(0) }

    // Form inputs
    var customEmail by remember { mutableStateOf("") }
    var customDisplayName by remember { mutableStateOf("") }
    var customPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Firebase Email Action URL activation state (replaces in-app OTP)
    var isAwaitingEmailActivation by remember { mutableStateOf(false) }

    // Forgot Password OTP State & Anti-Bypass Security
    var isForgotPasswordMode by remember { mutableStateOf(false) }
    var resetStep by remember { mutableIntStateOf(0) } // 0 = send OTP to email, 1 = enter OTP and new password
    var resetOtpEmail by remember { mutableStateOf("") }
    var resetEnteredOtp by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetConfirmPassword by remember { mutableStateOf("") }
    var isResetPasswordVisible by remember { mutableStateOf(false) }
    var isResetConfirmVisible by remember { mutableStateOf(false) }

    // Developer Secret Voice Passphrase Authentication
    var showDeveloperVoiceDialog by remember { mutableStateOf(false) }
    var developerVoiceSpokenText by remember { mutableStateOf("") }
    var developerVoiceStatusMessage by remember { mutableStateOf("") }
    var isVoiceListening by remember { mutableStateOf(false) }
    var isVoiceSpokenMasked by remember { mutableStateOf(true) }
    var failedVoiceAttempts by remember { mutableIntStateOf(0) }
    var voiceLockoutUntilMs by remember { mutableLongStateOf(0L) }

    var loginStepMessage by remember { mutableStateOf("") }
    var isSuccessDone by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Biometric Authentication (Face / Fingerprint)
    var enableBiometricOnRegister by remember { mutableStateOf(true) }
    val isBiometricHardwareAvailable = remember {
        BiometricAuthenticationHelper.canAuthenticate(context) == BiometricAuthenticationHelper.BiometricStatus.Available
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isVoiceListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                developerVoiceSpokenText = spoken
                developerVoiceStatusMessage = "تم التقاط الصوت: \"$spoken\""
            } else {
                developerVoiceStatusMessage = "لم يتم التقاط أي صوت، يرجى المحاولة مجدداً."
            }
        } else {
            developerVoiceStatusMessage = "تم إلغاء التسجيل الصوتي أو لم يتم التعرف على الصوت."
        }
    }

    fun startListeningForVoicePassphrase() {
        developerVoiceStatusMessage = "جاري الاستماع للتسجيل الصوتي... 🎙️"
        isVoiceListening = true
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "انطق العبارة الصوتية السرية للمطور 👑🎙️")
            }
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            isVoiceListening = false
            developerVoiceStatusMessage = "جهازك لا يدعم فتح نافذة الصوت المباشرة، يمكنك كتابة أو تعديل العبارة باليد."
        }
    }

    fun executeDeveloperVoiceLogin(phrase: String) {
        val now = System.currentTimeMillis()
        if (now < voiceLockoutUntilMs) {
            val remainingSec = ((voiceLockoutUntilMs - now) / 1000).coerceAtLeast(1)
            developerVoiceStatusMessage = "🔒 حظر أمني مؤقت! يرجى الانتظار $remainingSec ثانية قبل المحاولة مجدداً."
            return
        }

        val cleanEmail = customEmail.trim().ifEmpty { "mahme98776@gmail.com" }
        coroutineScope.launch {
            developerVoiceStatusMessage = "جاري التحقق من البصمة الصوتية مع العبارة السرية للمطور..."
            val result = authService.authenticateDeveloperWithVoicePhrase(cleanEmail, phrase)
            if (result.isSuccess) {
                failedVoiceAttempts = 0
                developerVoiceStatusMessage = "تم التحقق الصوتي بنجاح تام! مرحباً بالمهندس محمد سليمة 👑✓"
                isSuccessDone = true
                Toast.makeText(context, "تم تأكيد هوية المطور صوتياً بنجاح! أهلاً بك يا باشمهندس محمد سليمة", Toast.LENGTH_LONG).show()
                delay(800)
                showDeveloperVoiceDialog = false
                onSignInSuccess()
            } else {
                failedVoiceAttempts++
                if (failedVoiceAttempts >= 3) {
                    voiceLockoutUntilMs = System.currentTimeMillis() + (180 * 1000) // 3 minutes lockout
                    com.example.security.AppShieldDefenseEngine.recordBreachAttempt(
                        context = context,
                        incidentType = "محاولة تخمين البصمة الصوتية للمطور (Voice Passphrase Brute-Force)",
                        severity = "حرجة 🚨",
                        defenseAction = "تم فرض حظر دفاعي مؤقت لمدة 3 دقائق وقفل الواجهة",
                        deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                    )
                    developerVoiceStatusMessage = "🚨 تم استنفاد المحاولات وتفعيل بروتوكول الحماية الأمنية! تم فرض حظر لمدة 3 دقائق."
                } else {
                    val remaining = 3 - failedVoiceAttempts
                    developerVoiceStatusMessage = "❌ العبارة الصوتية غير صحيحة! متبقي لديك $remaining محاولة فقط قبل الحظر الأمني."
                }
            }
        }
    }

    fun handleSignIn() {
        val trimmedEmail = customEmail.trim()
        val trimmedPass = customPassword.trim()

        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            inputError = "يرجى إدخال عنوان بريد إلكتروني صحيح"
            return
        }

        // Developer Voice Authentication: email entered and password left empty
        if (trimmedEmail.lowercase() == "mahme98776@gmail.com" && trimmedPass.isEmpty()) {
            inputError = null
            showDeveloperVoiceDialog = true
            startListeningForVoicePassphrase()
            return
        }

        if (trimmedPass.length < 6) {
            inputError = "كلمة المرور يجب أن تكون 6 أحرف/أرقام على الأقل"
            return
        }

        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري التحقق من هوية الحساب في Firebase..."
            val result = authService.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
            if (result.isSuccess) {
                loginStepMessage = "تم تسجيل الدخول وتجهيز مجلد التخزين بنجاح 📁✓"
                isSuccessDone = true
                Toast.makeText(context, "أهلاً بك! تم تأكيد تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                delay(500)
                onSignInSuccess()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "فشل تسجيل الدخول. تأكد من كلمة المرور وتفعيل الحساب"
            }
        }
    }

    fun handleRequestAccountCreation() {
        val trimmedEmail = customEmail.trim()
        val trimmedName = customDisplayName.trim().ifBlank { trimmedEmail.substringBefore("@") }
        val trimmedPass = customPassword.trim()

        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            inputError = "يرجى إدخال عنوان بريد إلكتروني صالح لإنشاء الحساب"
            return
        }
        if (trimmedEmail.lowercase() == "mahme98776@gmail.com") {
            inputError = "⚠️ هذا البريد الإلكتروني مخصص حصرياً للمطور المعتمد (محمد سليمة). لا يمكن لأي مستخدم آخر تسجيله!"
            return
        }
        if (trimmedPass.length < 6) {
            inputError = "يرجى كتابة كلمة مرور قوية مكونة من 6 خانات على الأقل"
            return
        }

        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري التحقق من تفرد البريد في Firestore وقاعدة البيانات..."
            val isTaken = authService.isEmailRegisteredInFirestoreOrLocal(trimmedEmail)
            if (isTaken) {
                loginStepMessage = ""
                inputError = "⛔ هذا البريد الإلكتروني مسجل مسبقاً في قاعدة بيانات المستخدمين (Firestore)! كل بريد مخصص لحساب واحد فقط ولا يمكن إنشاء حساب آخر به. يرجى تسجيل الدخول بدلاً من ذلك."
                return@launch
            }

            loginStepMessage = "جاري إنشاء الحساب وإرسال رابط التفعيل (Firebase Email Action URL)..."
            val result = authService.registerWithEmailActionUrl(trimmedEmail, trimmedName, trimmedPass)
            if (result.isSuccess) {
                isAwaitingEmailActivation = true
                loginStepMessage = "تم إرسال رابط التفعيل إلى $trimmedEmail 📧"

                // Launch mail client intent so user can activate their account
                try {
                    val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_EMAIL)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(emailIntent)
                } catch (_: Exception) {
                    try {
                        val webMailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webMailIntent)
                    } catch (_: Exception) {}
                }

                Toast.makeText(context, "تم إرسال رابط التفعيل إلى بريدك! انقر على الرابط في صندوق الوارد لتفعيل الحساب.", Toast.LENGTH_LONG).show()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "تعذر إنشاء الحساب وإرسال رابط التفعيل"
            }
        }
    }

    fun handleCheckEmailActivation() {
        val trimmedEmail = customEmail.trim()
        val trimmedPass = customPassword.trim()
        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري التحقق من نقر رابط التفعيل في البريد الإلكتروني..."
            val result = authService.checkEmailActivationStatus(trimmedEmail, trimmedPass)
            if (result.isSuccess) {
                // If user selected biometric enrollment, save it now
                if (enableBiometricOnRegister) {
                    BiometricAuthenticationHelper.setBiometricEnrollment(context, trimmedEmail, trimmedPass, true)
                }
                loginStepMessage = "تم التحقق وتنشيط الحساب في Firestore ومجلد التخزين بنجاح 📁✨"
                isSuccessDone = true
                Toast.makeText(context, "مبروك! تم تفعيل حسابك بالكامل بنجاح 🚀", Toast.LENGTH_SHORT).show()
                delay(600)
                onSignInSuccess()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "لم يتم تفعيل الحساب بعد! يرجى النقر على رابط التفعيل في بريدك الإلكتروني أولاً."
            }
        }
    }

    fun handleBiometricAuthentication() {
        val trimmedEmail = customEmail.trim()
        if (trimmedEmail.isEmpty()) {
            inputError = "يرجى كتابة البريد الإلكتروني للتحقق من البصمة أو الوجه"
            return
        }

        val activity = context as? FragmentActivity
        if (activity == null) {
            inputError = "جهازك لا يدعم المصادقة الحيوية التفاعلية حالياً"
            return
        }

        val savedPass = BiometricAuthenticationHelper.getSavedPasswordForBiometric(context, trimmedEmail)
        val isDev = trimmedEmail.lowercase() == "mahme98776@gmail.com"

        if (!BiometricAuthenticationHelper.isBiometricConfiguredForUser(context, trimmedEmail) && !isDev) {
            inputError = "لم يتم تعيين البصمة أو الوجه لهذا الحساب بعد. يرجى تسجيل الدخول بكلمة المرور وتفعيلها من الإعدادات."
            return
        }

        inputError = null
        BiometricAuthenticationHelper.authenticate(
            activity = activity,
            title = if (isDev) "مصادقة المطور المعتمد (بصمة / وجه) 👑" else "المصادقة الحيوية (بصمة / وجه) 🛡️",
            subtitle = "أكد هويتك عبر مستشعر البصمة أو الكاميرا لمتابعة الدخول الفوري",
            onSuccess = {
                coroutineScope.launch {
                    loginStepMessage = "تم التحقق الحيوي بنجاح! جاري تسجيل الدخول الآمن..."
                    if (isDev) {
                        // Developer biometric direct authentication
                        val devResult = authService.authenticateDeveloperWithVoicePhrase(
                            email = "mahme98776@gmail.com",
                            voicePhrase = "حضر الولد من مكان بعيد للتعلم html وبايثون وجافا سكريبت و سي اس اس هاشتاج اغلق الكلام"
                        )
                        if (devResult.isSuccess) {
                            isSuccessDone = true
                            Toast.makeText(context, "تم تأكيد هوية المطور المعتمد بنجاح بالبصمة/الوجه 👑", Toast.LENGTH_SHORT).show()
                            delay(400)
                            onSignInSuccess()
                        } else {
                            loginStepMessage = ""
                            inputError = devResult.exceptionOrNull()?.message
                        }
                    } else if (!savedPass.isNullOrEmpty()) {
                        val result = authService.signInWithEmailAndPassword(trimmedEmail, savedPass)
                        if (result.isSuccess) {
                            isSuccessDone = true
                            Toast.makeText(context, "تم تسجيل الدخول بالبصمة/الوجه بنجاح! 🛡️✓", Toast.LENGTH_SHORT).show()
                            delay(400)
                            onSignInSuccess()
                        } else {
                            loginStepMessage = ""
                            inputError = result.exceptionOrNull()?.message ?: "فشل تسجيل الدخول بالبصمة"
                        }
                    } else {
                        loginStepMessage = ""
                        inputError = "يرجى تسجيل الدخول بكلمة المرور أولاً لتحديث بيانات البصمة."
                    }
                }
            },
            onError = { errMsg ->
                inputError = "فشل التحقق الحيوي: $errMsg"
            }
        )
    }

    fun handleResendEmailActionUrl() {
        val trimmedEmail = customEmail.trim()
        val trimmedPass = customPassword.trim()
        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري إعادة إرسال رابط التفعيل إلى البريد..."
            val result = authService.resendEmailActionUrl(trimmedEmail, trimmedPass)
            if (result.isSuccess) {
                loginStepMessage = "تمت إعادة إرسال رابط التفعيل بنجاح 📧"
                Toast.makeText(context, "تمت إعادة إرسال رابط التفعيل! تفقد صندوق الوارد أو الرسائل غير المرغوب فيها (Spam).", Toast.LENGTH_LONG).show()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "تعذر إعادة إرسال الرابط"
            }
        }
    }

    fun handleSendPasswordResetOtp() {
        val emailToReset = resetOtpEmail.trim().ifEmpty { customEmail.trim() }
        if (!emailToReset.contains("@") || !emailToReset.contains(".")) {
            inputError = "يرجى إدخال عنوان بريد إلكتروني صحيح لإرسال رمز التحقق."
            return
        }
        if (emailToReset.lowercase() == "mahme98776@gmail.com") {
            inputError = "⛔ حساب المطور المعتمد (محمد سليمة) محمي تشفيرياً ولا يمكن استعادة كلمة مروره عبر هذا النموذج!"
            return
        }

        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري إنشاء رمز التحقق المشفر وإرساله للبريد..."
            val result = authService.generatePasswordResetOtp(emailToReset)
            if (result.isSuccess) {
                val otpCode = result.getOrNull() ?: ""
                resetOtpEmail = emailToReset
                resetStep = 1
                resetEnteredOtp = ""
                loginStepMessage = "تم إرسال رمز التحقق (6 أرقام) إلى $emailToReset 📧"

                // Dispatch email intent with the OTP code
                try {
                    val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$emailToReset")
                        putExtra(Intent.EXTRA_SUBJECT, "رمز التحقق لإعادة تعيين كلمة المرور - فويس ماستر برو")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            """
                            أهلاً بك،
                            
                            رمز التحقق الخاص بك لإعادة تعيين كلمة المرور في تطبيق فويس ماستر برو هو:
                            $otpCode
                            
                            ⚠️ ملاحظة هامة:
                            - هذا الرمز صالح لمدة 10 دقائق فقط.
                            - يجب كتابة هذا الرمز يدوياً داخل التطبيق لتأكيد هويتك وتعيين كلمة المرور الجديدة.
                            - لن تتمكن من الدخول أو تغيير كلمة المرور إلا بعد إدخال الرمز الصحيح.
                            
                            مع تحيات،
                            فريق فويس ماستر برو | استوديو الدبلجة الذكية
                            المطور: محمد سليمة
                            """.trimIndent()
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(mailtoIntent)
                } catch (_: Exception) {
                    try {
                        val emailAppIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_EMAIL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(emailAppIntent)
                    } catch (_: Exception) {}
                }

                Toast.makeText(context, "تم إرسال رمز التحقق إلى بريدك الإلكتروني! تفقد صندوق الوارد.", Toast.LENGTH_LONG).show()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "تعذر إرسال رمز التحقق"
            }
        }
    }

    fun handleSendDirectFirebasePasswordResetEmail() {
        val emailToReset = resetOtpEmail.trim().ifEmpty { customEmail.trim() }
        if (!emailToReset.contains("@") || !emailToReset.contains(".")) {
            inputError = "يرجى إدخال عنوان بريد إلكتروني صحيح لإرسال رابط إعادة تعيين كلمة المرور."
            return
        }
        if (emailToReset.lowercase() == "mahme98776@gmail.com") {
            inputError = "⛔ حساب المطور المعتمد (محمد سليمة) محمي تشفيرياً ولا يمكن إعادة تعيين كلمة مروره عبر هذا النموذج!"
            return
        }

        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري إرسال رابط إعادة تعيين كلمة المرور من Firebase Auth..."
            val result = authService.sendFirebasePasswordResetEmail(emailToReset)
            if (result.isSuccess) {
                loginStepMessage = "تم إرسال رابط إعادة تعيين كلمة المرور إلى $emailToReset بنجاح 📧✓"
                Toast.makeText(context, "تم إرسال رابط إعادة تعيين كلمة المرور من Firebase Auth إلى بريدك! تفقد صندوق الوارد.", Toast.LENGTH_LONG).show()
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "تعذر إرسال رابط إعادة التعيين من Firebase Auth"
            }
        }
    }

    fun handleVerifyOtpAndReset() {
        val cleanEmail = resetOtpEmail.trim()
        val cleanOtp = resetEnteredOtp.trim()
        val newPass = resetNewPassword.trim()
        val confirmPass = resetConfirmPassword.trim()

        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            inputError = "❌ يرجى كتابة رمز التحقق المكون من 6 أرقام بيدك كاملاً كما استلمته في البريد."
            return
        }
        if (newPass.length < 6) {
            inputError = "❌ كلمة المرور الجديدة يجب أن تكون 6 خانات على الأقل."
            return
        }
        if (newPass != confirmPass) {
            inputError = "❌ كلمتا المرور غير متطابقتين! يرجى التأكد من كتابتهما بشكل متطابق."
            return
        }

        inputError = null
        coroutineScope.launch {
            loginStepMessage = "جاري التحقق من الرمز المدخل يدوياً مع الرمز المشفر..."
            val result = authService.verifyOtpAndResetPassword(cleanEmail, cleanOtp, newPass)
            if (result.isSuccess) {
                loginStepMessage = "تم التحقق من الرمز بنجاح وتحديث كلمة المرور! جاري تسجيل الدخول 🚀✓"
                val signInResult = authService.signInWithEmailAndPassword(cleanEmail, newPass)
                if (signInResult.isSuccess) {
                    isSuccessDone = true
                    Toast.makeText(context, "تم تغيير كلمة المرور بنجاح وتسجيل الدخول إلى حسابك!", Toast.LENGTH_SHORT).show()
                    delay(500)
                    onSignInSuccess()
                } else {
                    isForgotPasswordMode = false
                    customEmail = cleanEmail
                    customPassword = newPass
                    loginStepMessage = ""
                    Toast.makeText(context, "تم تحديث كلمة المرور بنجاح! يمكنك الآن تسجيل الدخول بكلمة مرورك الجديدة.", Toast.LENGTH_LONG).show()
                }
            } else {
                loginStepMessage = ""
                inputError = result.exceptionOrNull()?.message ?: "رمز التحقق غير مطابق! تم رفض الدخول."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("google_signin_gate_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Brand & Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF4285F4),
                                    Color(0xFFEA4335),
                                    Color(0xFFFBBC05),
                                    Color(0xFF34A853)
                                )
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Gemini Cloud Google",
                            tint = Color(0xFF1A73E8),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "بوابة تسجيل الحساب الآمن",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Firebase Auth • الحفظ السحابي ومجلد التخزين المشفر",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                // Auth Mode Switcher (تسجيل الدخول / إنشاء حساب جديد)
                if (!isAwaitingEmailActivation && !isForgotPasswordMode) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Surface(
                                onClick = {
                                    selectedAuthMode = 0
                                    inputError = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedAuthMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "تسجيل الدخول",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedAuthMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    selectedAuthMode = 1
                                    inputError = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedAuthMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "إنشاء حساب جديد ✨",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedAuthMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else if (isForgotPasswordMode) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LockReset,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (resetStep == 0) "استعادة الحساب برمز التحقق (OTP) 🔑" else "التحقق وتعيين كلمة المرور الجديدة 🔐",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(
                                onClick = {
                                    isForgotPasswordMode = false
                                    inputError = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "إلغاء",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Cloud Features Info Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CloudFeatureRow(
                            icon = Icons.Filled.VerifiedUser,
                            text = "تأمين الحساب برابط تفعيل رسمي مباشر (Firebase Email Action URL)"
                        )
                        CloudFeatureRow(
                            icon = Icons.Filled.FolderSpecial,
                            text = "إنشاء مجلد تخزين سحابي ومحلي مخصص لحسابك ومشاريعك"
                        )
                        CloudFeatureRow(
                            icon = Icons.Filled.Speed,
                            text = "تفعيل حصة Gemini السحابية الفائقة للمشاريع والصوتيات"
                        )
                    }
                }

                // Progress Indicator
                AnimatedVisibility(visible = isSyncing || isSuccessDone) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF1A73E8)
                        )
                        Text(
                            text = loginStepMessage.ifEmpty { syncStatus },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isForgotPasswordMode) {
                            // Forgot Password Screen with Strict OTP Manual Entry
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (resetStep == 0) {
                                    // STEP 0: Enter email to request 6-digit verification code
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Key,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "استعادة كلمة المرور برمز التحقق (OTP)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "أدخل بريدك الإلكتروني المسجل في النظام وسنرسل لك فوراً رمز تحقق سرياً مكوناً من 6 أرقام لتأكيد هويتك وتعيين كلمة مرور جديدة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )

                                    OutlinedTextField(
                                        value = resetOtpEmail,
                                        onValueChange = {
                                            resetOtpEmail = it
                                            inputError = null
                                        },
                                        label = { Text("البريد الإلكتروني المسجل") },
                                        placeholder = { Text("example@gmail.com") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth().testTag("input_reset_email"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    inputError?.let { err ->
                                        Text(
                                            text = "⚠️ $err",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { handleSendPasswordResetOtp() },
                                        enabled = !isSyncing,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_send_reset_otp"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1A73E8),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("إرسال رمز التحقق السري (OTP) 🔑", fontWeight = FontWeight.Bold)
                                    }

                                    FilledTonalButton(
                                        onClick = { handleSendDirectFirebasePasswordResetEmail() },
                                        enabled = !isSyncing,
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_send_firebase_reset_email"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("إرسال رابط إعادة التعيين (Firebase Auth) 🔗", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            isForgotPasswordMode = false
                                            inputError = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("الرجوع إلى تسجيل الدخول ↩️", style = MaterialTheme.typography.labelMedium)
                                    }
                                } else {
                                    // STEP 1: Enter 6-digit OTP MANUALLY + Enter New Password
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Filled.MarkEmailRead,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "تم إرسال رمز التحقق إلى بريدك:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(
                                                text = resetOtpEmail,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "🔒 تنبيه أمني مشدد: يجب عليك كتابة رمز التحقق المكون من 6 أرقام بيدك أدناه. لن يتم الدخول إطلاقاً إذا لم يكن الرمز مطابقاً تماماً للرمز المرسل في البريد.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )

                                            // Direct Mail app opener (Opening mail does NOT automatically log in or bypass)
                                            OutlinedButton(
                                                onClick = {
                                                    try {
                                                        val mailIntent = Intent(Intent.ACTION_MAIN).apply {
                                                            addCategory(Intent.CATEGORY_APP_EMAIL)
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(mailIntent)
                                                    } catch (_: Exception) {
                                                        try {
                                                            val webMailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/")).apply {
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            }
                                                            context.startActivity(webMailIntent)
                                                        } catch (_: Exception) {}
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("فتح صندوق البريد لمعرفة الرمز 📩", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // OTP Code Input Field (MUST be typed by hand)
                                    OutlinedTextField(
                                        value = resetEnteredOtp,
                                        onValueChange = {
                                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                                resetEnteredOtp = it
                                                inputError = null
                                            }
                                        },
                                        label = { Text("اكتب رمز التحقق هنا بيدك (6 أرقام)") },
                                        placeholder = { Text("مثال: 482910") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        modifier = Modifier.fillMaxWidth().testTag("input_reset_otp_code"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // New Password Field
                                    OutlinedTextField(
                                        value = resetNewPassword,
                                        onValueChange = {
                                            resetNewPassword = it
                                            inputError = null
                                        },
                                        label = { Text("كلمة المرور الجديدة (6 خانات على الأقل)") },
                                        placeholder = { Text("أدخل كلمة المرور الجديدة") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { isResetPasswordVisible = !isResetPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isResetPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isResetPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth().testTag("input_reset_new_password"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Confirm New Password Field
                                    OutlinedTextField(
                                        value = resetConfirmPassword,
                                        onValueChange = {
                                            resetConfirmPassword = it
                                            inputError = null
                                        },
                                        label = { Text("تأكيد كلمة المرور الجديدة") },
                                        placeholder = { Text("أعد كتابة كلمة المرور الجديدة") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { isResetConfirmVisible = !isResetConfirmVisible }) {
                                                Icon(
                                                    imageVector = if (isResetConfirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isResetConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth().testTag("input_reset_confirm_password"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Error text
                                    inputError?.let { err ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = err,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Submit Button (Strict check: must match sent OTP)
                                    Button(
                                        onClick = { handleVerifyOtpAndReset() },
                                        enabled = !isSyncing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_verify_otp_submit"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF34A853),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("تأكيد الرمز وتعيين كلمة المرور والدخول 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { handleSendPasswordResetOtp() },
                                            enabled = !isSyncing
                                        ) {
                                            Text("إعادة إرسال رمز جديد 🔄", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }

                                        TextButton(
                                            onClick = {
                                                isForgotPasswordMode = false
                                                inputError = null
                                            }
                                        ) {
                                            Text("إلغاء والعودة ↩️", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        } else if (!isAwaitingEmailActivation) {
                            // Email Field
                            OutlinedTextField(
                                value = customEmail,
                                onValueChange = {
                                    customEmail = it
                                    inputError = null
                                },
                                label = { Text("البريد الإلكتروني") },
                                placeholder = { Text("example@gmail.com") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("input_google_email"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Live email uniqueness check in registration mode
                            if (selectedAuthMode == 1 && customEmail.contains("@") && customEmail.contains(".")) {
                                val isEmailTaken = authService.isEmailAlreadyRegistered(customEmail.trim())
                                if (isEmailTaken) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Filled.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = "هذا البريد الإلكتروني مسجل مسبقاً!",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "قاعدة النظام الصارمة: كل بريد مخصص لحساب واحد فقط ولا يمكن إنشاء حساب آخر به. يرجى تسجيل الدخول بدلاً من ذلك.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    selectedAuthMode = 0
                                                    inputError = null
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                            ) {
                                                Text(
                                                    "الانتقال لتسجيل الدخول بهذا الحساب",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Display Name if in Registration Mode
                            if (selectedAuthMode == 1) {
                                OutlinedTextField(
                                    value = customDisplayName,
                                    onValueChange = { customDisplayName = it },
                                    label = { Text("الاسم الكامل / اسم العرض") },
                                    placeholder = { Text("مثال: اسمك الكريم") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_google_display_name"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Password Field
                            OutlinedTextField(
                                value = customPassword,
                                onValueChange = {
                                    customPassword = it
                                    inputError = null
                                },
                                label = { Text("كلمة المرور المشفرة") },
                                placeholder = { 
                                    if (selectedAuthMode == 0 && customEmail.trim().lowercase() == "mahme98776@gmail.com") 
                                        Text("أدخل كلمة المرور أو اتركها فارغة للبصمة الصوتية 🎙️")
                                    else 
                                        Text("أدخل كلمة المرور (6 خانات على الأقل)") 
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedAuthMode == 0 && customEmail.trim().lowercase() == "mahme98776@gmail.com" && customPassword.isEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    showDeveloperVoiceDialog = true
                                                    startListeningForVoicePassphrase()
                                                },
                                                modifier = Modifier.testTag("btn_dev_voice_mic")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Mic,
                                                    contentDescription = "التسجيل الصوتي للمطور",
                                                    tint = Color(0xFFE91E63)
                                                )
                                            }
                                        }
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور"
                                            )
                                        }
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("input_google_password"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Developer Voice Authentication Card (when email is developer email and password is empty)
                            if (selectedAuthMode == 0 && customEmail.trim().lowercase() == "mahme98776@gmail.com" && customPassword.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFCE4EC),
                                    border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Mic, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(22.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "بوابة تسجيل الدخول الصوتي للمطور 👑🎙️",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC2185B)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "تم رصد بريد المطور المعتمد وترك كلمة المرور فارغة! يمكنك تسجيل الدخول فوراً عبر نطق العبارة الصوتية السرية.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF880E4F)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                showDeveloperVoiceDialog = true
                                                startListeningForVoicePassphrase()
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("btn_trigger_dev_voice"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63), contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("تسجيل صوتي للمطور (نطق العبارة السرية) 🎙️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // "Forgot Password?" Button and Biometric Check in Login Mode
                            if (selectedAuthMode == 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick Biometric (Fingerprint/Face) Button
                                    TextButton(
                                        onClick = { handleBiometricAuthentication() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        modifier = Modifier.testTag("btn_biometric_login")
                                    ) {
                                        Icon(
                                            Icons.Filled.Fingerprint,
                                            contentDescription = "الدخول بالبصمة أو الوجه",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF00897B)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "الدخول بالبصمة / الوجه 🛡️",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00897B)
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            isForgotPasswordMode = true
                                            resetStep = 0
                                            resetOtpEmail = customEmail.trim()
                                            resetEnteredOtp = ""
                                            resetNewPassword = ""
                                            resetConfirmPassword = ""
                                            inputError = null
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.LockReset,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "نسيت كلمة المرور؟ 🔑",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                // Biometric enrollment checkbox during Account Creation
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        enableBiometricOnRegister = !enableBiometricOnRegister
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = enableBiometricOnRegister,
                                            onCheckedChange = { enableBiometricOnRegister = it },
                                            modifier = Modifier.testTag("chk_enable_biometric_register")
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "تفعيل الدخول ببصمة الإصبع أو الوجه لهذا الحساب 🛡️",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "يسمح لك بالدخول الفوري بأمان تام بدون إعادة كتابة كلمة المرور في كل مرة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Error text
                            inputError?.let { err ->
                                Text(
                                    text = "⚠️ $err",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Submit Button
                            Button(
                                onClick = {
                                    if (selectedAuthMode == 0) {
                                        handleSignIn()
                                    } else {
                                        handleRequestAccountCreation()
                                    }
                                },
                                enabled = !isSyncing,
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_google_signin_primary"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A73E8),
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedAuthMode == 0) Icons.Filled.Login else Icons.Filled.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedAuthMode == 0) "تسجيل الدخول إلى حسابك" else "إنشاء الحساب وإرسال رابط التفعيل 📧",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            // Firebase Email Action URL Verification Screen
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F0FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MarkEmailRead,
                                        contentDescription = null,
                                        tint = Color(0xFF1A73E8),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Text(
                                    text = "رابط تفعيل الحساب (Firebase Action URL)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "تم إرسال رابط التفعيل الرسمي بنجاح إلى:\n${customEmail.trim()}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "📩 لحماية أمان النظام ومنع أي تسجيل غير مصرح به، تم إرسال رابط التفعيل إلى صندوق الوارد الخاص بك.\n\nخطوات التفعيل:\n1. افتح تطبيق البريد الإلكتروني.\n2. انقر فوق رابط التفعيل الوارد من Firebase.\n3. عد إلى هنا واضغط على زر التحقق أدناه لتنشيط الحساب والدخول فوراً.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Start,
                                            lineHeight = 18.sp
                                        )

                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_MAIN).apply {
                                                        addCategory(Intent.CATEGORY_APP_EMAIL)
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    try {
                                                        val webMailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/")).apply {
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(webMailIntent)
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("فتح تطبيق البريد الإلكتروني 📧", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                inputError?.let { err ->
                                    Text(
                                        text = "⚠️ $err",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Check Verification and Sign In Button
                                Button(
                                    onClick = { handleCheckEmailActivation() },
                                    enabled = !isSyncing,
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_check_email_activation"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF34A853),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("التحقق من التفعيل وتسجيل الدخول 🚀", fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { handleResendEmailActionUrl() },
                                        enabled = !isSyncing
                                    ) {
                                        Text("إعادة إرسال الرابط 🔄", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }

                                    TextButton(
                                        onClick = {
                                            isAwaitingEmailActivation = false
                                            inputError = null
                                        }
                                    ) {
                                        Text("الرجوع وتعديل البيانات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // Security & Developer Note
                Text(
                    text = "🔒 حقوق النشر والملكية الفكرية محفوظة بالكامل للمطور المعتمد: محمد سليمة | استوديو الدبلجة الذكية 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )
            }
        }
    }

    // Developer Secret Voice Dialog
    if (showDeveloperVoiceDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeveloperVoiceDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "بصمة التسجيل الصوتي للمطور 👑🎙️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نظام التحقق الصوتي الخاص بالمهندس محمد سليمة عند إدخال البريد وترك كلمة المرور فارغة.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131F2E),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "بصمة العبارة الصوتية المشفرة (Zero-Knowledge):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "🛡️ تم تفعيل درع الحماية ضد التلصص (Anti-Shoulder Surfing). العبارة السرية مشفرة تماماً ولا يتم عرضها كنص صريح لضمان أعلى درجات الأمان. يرجى التحدث بالميكروفون الآن.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isVoiceListening) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFE91E63)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "جاري الاستماع الآن... يرجى التحدث 🎙️",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE91E63),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = developerVoiceSpokenText,
                        onValueChange = { developerVoiceSpokenText = it },
                        label = { Text("النص الصوتي الملتقط") },
                        placeholder = { Text("انطق العبارة أو اكتبها للتأكيد") },
                        modifier = Modifier.fillMaxWidth().testTag("input_dev_voice_text"),
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = if (isVoiceSpokenMasked) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = { isVoiceSpokenMasked = !isVoiceSpokenMasked }) {
                                Icon(
                                    imageVector = if (isVoiceSpokenMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isVoiceSpokenMasked) "إظهار النص" else "إخفاء النص",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    if (developerVoiceStatusMessage.isNotBlank()) {
                        Text(
                            text = developerVoiceStatusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (developerVoiceStatusMessage.contains("نجاح") || developerVoiceStatusMessage.contains("أهلاً") || developerVoiceStatusMessage.contains("تم التقاط")) Color(0xFF34A853) else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { startListeningForVoicePassphrase() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFE91E63))
                        Spacer(Modifier.width(6.dp))
                        Text("إعادة تشغيل الميكروفون 🎙️", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        executeDeveloperVoiceLogin(developerVoiceSpokenText)
                    },
                    modifier = Modifier.testTag("btn_confirm_dev_voice_login"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("التحقق والدخول 🚀", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeveloperVoiceDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun CloudFeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF34A853),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
