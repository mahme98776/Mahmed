package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.security.AuthConnectedDevice
import com.example.security.BiometricAuthenticationHelper
import com.example.security.DeviceAccountSecurityManager
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * شاشة حسابي (My Profile)
 * تعرض اسم المستخدم الحالي، البريد الإلكتروني، الحصة السحابية، مجلد التخزين المحلي والملفات،
 * قائمة الأجهزة المرتبطة بحسابه، مع توضيح كيفية ربط الحساب بمنصات التواصل الاجتماعي (Social Media Integration).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: DubbingViewModel,
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.authService.userProfile.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.authSecurityManager.connectedDevices.collectAsStateWithLifecycle()
    val syncStatus by viewModel.authService.syncStatus.collectAsStateWithLifecycle()
    val isSyncing by viewModel.authService.isSyncing.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var showSocialInfoDialog by remember { mutableStateOf(false) }
    var selectedSocialPlatform by remember { mutableStateOf<String?>(null) }

    // Biometrics & Password Management State
    var isBiometricEnrolled by remember(userProfile.email) {
        mutableStateOf(BiometricAuthenticationHelper.isBiometricConfiguredForUser(context, userProfile.email))
    }
    val biometricStatus = remember { BiometricAuthenticationHelper.canAuthenticate(context) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showBiometricSetupDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordErrorMsg by remember { mutableStateOf<String?>(null) }
    var isPasswordProcessing by remember { mutableStateOf(false) }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }

    // User dedicated storage directory calculation
    val userStorageDir = remember(userProfile.uid) {
        File(context.filesDir, "users/${userProfile.uid}/storage").apply { mkdirs() }
    }
    val totalFilesInStorage = remember(userProfile.uid) {
        userStorageDir.listFiles()?.size ?: 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "حسابي الشخصي (Profile)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_profile")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.authService.signOut()
                            onSignOut()
                        },
                        modifier = Modifier.testTag("btn_signout_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Main User Identity Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_identity_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Avatar with Status Glow
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF1A73E8),
                                            Color(0xFF4285F4),
                                            Color(0xFF34A853)
                                        )
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }

                        // Display Name and Verified Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userProfile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "حساب موثق",
                                tint = Color(0xFF1A73E8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Email with copy button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = userProfile.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("User Email", userProfile.email))
                                        Toast.makeText(context, "تم نسخ البريد الإلكتروني ✓", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "نسخ البريد",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // UID tag & Sync status
                        Text(
                            text = "معرف الأمان السحابي (UID): ${userProfile.uid}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        // Cloud status pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (userProfile.isGeminiCloudSaved) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudDone,
                                    contentDescription = null,
                                    tint = if (userProfile.isGeminiCloudSaved) Color(0xFF137333) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (userProfile.isGeminiCloudSaved) "متصل ومزامن في سحابة Gemini & Firebase ☁️✓" else "جلسة سحابية جاهزة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userProfile.isGeminiCloudSaved) Color(0xFF137333) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. User Storage Folder Card (مجلد التخزين المخصص للمستخدم)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_storage_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.FolderSpecial,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "مجلد التخزين السحابي والمحلي الخاص بك 📁",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "مساحة مشفرة مخصصة لحسابك لحفظ الدبلجات والمشاريع",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "مسار التخزين المخصص:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = userStorageDir.absolutePath,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الملفات المحفوظة: $totalFilesInStorage ملفات",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "المشاريع السحابية: ${userProfile.totalCloudProjects}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("User Storage Path", userStorageDir.absolutePath))
                                            Toast.makeText(context, "تم نسخ مسار مجلد التخزين المخصص 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("نسخ المسار", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.authService.syncWithFirestore(userProfile)
                                                Toast.makeText(context, "تمت مزامنة بيانات المجلد مع السحابة ☁️", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("مزامنة السحابة", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Security, Biometrics (Face/Fingerprint) & Password Management Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_biometric_security_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "الأمان والمصادقة الحيوية وكلمة المرور 🛡️",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "إدارة بصمة الإصبع أو الوجه وتغيير كلمة مرور الحساب",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Biometrics (Face & Fingerprint) Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Face,
                                        contentDescription = null,
                                        tint = if (isBiometricEnrolled) Color(0xFF34A853) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "المصادقة ببصمة الإصبع أو الوجه",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (isBiometricEnrolled) "المصادقة الحيوية مفعلة بنجاح للدخول السريع والآمن ✓"
                                    else "تسجيل الدخول الفوري دون كتابة كلمة المرور في كل مرة",
                                    fontSize = 11.5.sp,
                                    color = if (isBiometricEnrolled) Color(0xFF1B8738) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isBiometricEnrolled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        showBiometricSetupDialog = true
                                    } else {
                                        BiometricAuthenticationHelper.setBiometricEnrollment(
                                            context = context,
                                            email = userProfile.email,
                                            passwordPlain = "",
                                            enable = false
                                        )
                                        isBiometricEnrolled = false
                                        Toast.makeText(context, "تم إلغاء تفعيل البصمة الحيوية 🔒", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("switch_profile_biometric")
                            )
                        }

                        // Test Biometric Button if enrolled
                        if (isBiometricEnrolled) {
                            OutlinedButton(
                                onClick = {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricAuthenticationHelper.authenticate(
                                            activity = activity,
                                            email = userProfile.email,
                                            title = "اختبار المصادقة الحيوية 🛡️",
                                            subtitle = "المس مستشعر البصمة أو انظر إلى الكاميرا",
                                            onSuccess = {
                                                Toast.makeText(context, "تم التحقق من البصمة / الوجه بنجاح تام! هوية مؤكدة ✓", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { error ->
                                                Toast.makeText(context, "فشل الاختبار: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "خاصية البصمة مفعلة وجاهزة بجهازك ✓", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("اختبار استجابة البصمة / بصمة الوجه الآن", fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Password Change Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.LockReset,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "تغيير كلمة مرور الحساب",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "تحديث كلمة السر المشفرة لتعزيز حماية حسابك ومشاريعك",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    currentPasswordInput = ""
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                    passwordErrorMsg = null
                                    showPasswordChangeDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_change_password_dialog")
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("تغيير", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. Social Media Integration Guide Card (ربط الحساب بمنصات التواصل الاجتماعي)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_social_media_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFF1DA1F2).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1DA1F2).copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        tint = Color(0xFF1DA1F2),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ربط الحساب بالسوشيال ميديا والإنترنت 🌐",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "كيفية مزامنة مشاريعك ومشاركتها مع YouTube, TikTok, Facebook",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "يمكنك ربط حسابك في فويس ماستر برو مباشرة بمنصات التواصل الاجتماعي لنشر وتصدير الدبلجات بضغطة واحدة:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Social Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialPlatformButton(
                                title = "YouTube",
                                color = Color(0xFFFF0000),
                                icon = Icons.Filled.PlayArrow,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedSocialPlatform = "YouTube"
                                    showSocialInfoDialog = true
                                }
                            )
                            SocialPlatformButton(
                                title = "TikTok",
                                color = Color(0xFF000000),
                                icon = Icons.Filled.Movie,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedSocialPlatform = "TikTok"
                                    showSocialInfoDialog = true
                                }
                            )
                            SocialPlatformButton(
                                title = "Facebook",
                                color = Color(0xFF1877F2),
                                icon = Icons.Filled.Public,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedSocialPlatform = "Facebook"
                                    showSocialInfoDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // 4. Connected Devices Section (الأجهزة المتصلة بالحساب)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "الأجهزة المرتبطة بحسابك (${connectedDevices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.authService.syncWithFirestore(userProfile)
                                Toast.makeText(context, "تم التحقق ومزامنة الأجهزة النشطة مع Firebase 🔄✓", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تحديث", fontSize = 12.sp)
                    }
                }
            }

            // List of connected devices
            items(connectedDevices, key = { it.id }) { device ->
                ConnectedDeviceItemCard(
                    device = device,
                    onBlockDevice = {
                        viewModel.authSecurityManager.blockDevice(device.id)
                        Toast.makeText(context, "تم فصل وتجميد وصول الجهاز: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "من أنشأ هذا البرنامج هو: محمد سليمة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "مؤسس ومبتكر التطبيق: محمد رضا محمود محمود سليمه",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "جميع حقوق النشر والملكية الفكرية محفوظة © 2026",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Social Integration Dialog Explanation
    if (showSocialInfoDialog && selectedSocialPlatform != null) {
        val platform = selectedSocialPlatform!!
        AlertDialog(
            onDismissRequest = { showSocialInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "ربط الحساب بـ $platform",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "خطوات ربط ونشر الدبلجات إلى $platform:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "1. افتح مشروع الفيديو المدبلج الخاص بك في الاستوديو.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "2. اضغط على زر 'تصدير ومشاركة الفيديو' (Export & Share).",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "3. اختر $platform من قائمة المشاركة التلقائية؛ حيث يتم رفع المقطع المدبلج مباشرة عبر حساب Google المرتبط.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "4. يتم حفظ رابط الفيديو المشترك في سجل حسابك السحابي تلقائياً.",
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val webUrl = when (platform) {
                                "YouTube" -> "https://studio.youtube.com"
                                "TikTok" -> "https://www.tiktok.com/upload"
                                else -> "https://facebook.com"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("فتح المنصة الآن 🌐")
                    }
                    Button(onClick = { showSocialInfoDialog = false }) {
                        Text("فهمت ذلك ✓")
                    }
                }
            }
        )
    }

    // Biometric (Face/Fingerprint) Activation Dialog
    if (showBiometricSetupDialog) {
        var setupPasswordInput by remember { mutableStateOf("") }
        var setupErrorMsg by remember { mutableStateOf<String?>(null) }
        var isSetupTesting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBiometricSetupDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تفعيل بصمة الإصبع أو الوجه 🛡️",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "قم بتأكيد كلمة مرور حسابك الحالية لربطها بمستشعر البصمة أو ميزة التعرف على الوجه على هذا الجهاز:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = setupPasswordInput,
                        onValueChange = {
                            setupPasswordInput = it
                            setupErrorMsg = null
                        },
                        label = { Text("كلمة المرور الحالية") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("input_setup_bio_password"),
                        singleLine = true
                    )

                    if (setupErrorMsg != null) {
                        Text(
                            text = setupErrorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    // Live Device Account Verification Feedback
                    val presenceStatus = remember(userProfile.email) {
                        DeviceAccountSecurityManager.checkEmailPresenceOnDevice(context, userProfile.email)
                    }
                    if (presenceStatus is DeviceAccountSecurityManager.DeviceAccountStatus.VerifiedOnDevice) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("حساب موثق ومسجل على هذا الجهاز 📱✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, Color(0xFFFFA000)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("تنبيه: البريد غير مقترن بنظام الجهاز، سيتم التشفير وربط البصمة محلياً 🔒", fontSize = 10.5.sp, color = Color(0xFFBF360C))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "يتم تشفير وتخزين المفتاح بأمان فائق داخل مخزن الأمان الحيوي للنظام (Hardware Keystore).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = setupPasswordInput.trim()
                        if (pass.length < 6) {
                            setupErrorMsg = "يرجى كتابة كلمة مرور صحيحة (6 خانات على الأقل)"
                            return@Button
                        }

                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            isSetupTesting = true
                            BiometricAuthenticationHelper.authenticate(
                                activity = activity,
                                email = userProfile.email,
                                title = "تأكيد بصمة الإصبع أو الوجه 🛡️",
                                subtitle = "المس المستشعر للتأكيد والربط بالحساب",
                                onSuccess = {
                                    isSetupTesting = false
                                    BiometricAuthenticationHelper.setBiometricEnrollment(
                                        context = context,
                                        email = userProfile.email,
                                        passwordPlain = pass,
                                        enable = true
                                    )
                                    isBiometricEnrolled = true
                                    showBiometricSetupDialog = false
                                    Toast.makeText(context, "تم تفعيل المصادقة الحيوية (البصمة/الوجه) بنجاح تام 🛡️✓", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    isSetupTesting = false
                                    setupErrorMsg = "فشل التحقق الحيوي: $err"
                                }
                            )
                        } else {
                            BiometricAuthenticationHelper.setBiometricEnrollment(
                                context = context,
                                email = userProfile.email,
                                passwordPlain = pass,
                                enable = true
                            )
                            isBiometricEnrolled = true
                            showBiometricSetupDialog = false
                            Toast.makeText(context, "تم تفعيل البصمة بنجاح لهذا الحساب ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_enable_bio")
                ) {
                    Text("المتابعة وتأكيد البصمة ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricSetupDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Change Account Password Dialog
    if (showPasswordChangeDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isPasswordProcessing) {
                    showPasswordChangeDialog = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تغيير كلمة مرور الحساب 🔐",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "أدخل كلمة المرور الحالية ثم اختر كلمة مرور جديدة قوية لحماية حسابك ومشاريعك:",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Current Password
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = {
                            currentPasswordInput = it
                            passwordErrorMsg = null
                        },
                        label = { Text("كلمة المرور الحالية") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    imageVector = if (showCurrentPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("input_current_password"),
                        singleLine = true,
                        enabled = !isPasswordProcessing
                    )

                    // New Password
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            passwordErrorMsg = null
                        },
                        label = { Text("كلمة المرور الجديدة") },
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_password"),
                        singleLine = true,
                        enabled = !isPasswordProcessing
                    )

                    // Confirm New Password
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            passwordErrorMsg = null
                        },
                        label = { Text("تأكيد كلمة المرور الجديدة") },
                        leadingIcon = { Icon(Icons.Filled.CheckCircleOutline, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_password"),
                        singleLine = true,
                        enabled = !isPasswordProcessing
                    )

                    if (passwordErrorMsg != null) {
                        Text(
                            text = passwordErrorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (isPasswordProcessing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("جاري التحديث والتشفير في Firebase والسحابة...", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val curr = currentPasswordInput.trim()
                        val newP = newPasswordInput.trim()
                        val conf = confirmPasswordInput.trim()

                        if (curr.isEmpty()) {
                            passwordErrorMsg = "يرجى كتابة كلمة المرور الحالية"
                            return@Button
                        }
                        if (newP.length < 6) {
                            passwordErrorMsg = "كلمة المرور الجديدة يجب أن تكون 6 خانات على الأقل"
                            return@Button
                        }
                        if (newP != conf) {
                            passwordErrorMsg = "كلمة المرور الجديدة غير متطابقة مع خانة التأكيد"
                            return@Button
                        }

                        isPasswordProcessing = true
                        passwordErrorMsg = null

                        coroutineScope.launch {
                            val result = viewModel.authService.changePassword(
                                email = userProfile.email,
                                currentPasswordPlain = curr,
                                newPasswordPlain = newP,
                                isDeveloperBypass = false
                            )

                            isPasswordProcessing = false
                            if (result.isSuccess) {
                                showPasswordChangeDialog = false
                                Toast.makeText(context, "تم تغيير وتشفير كلمة مرور الحساب بنجاح 🔐✓", Toast.LENGTH_LONG).show()
                            } else {
                                passwordErrorMsg = result.exceptionOrNull()?.message ?: "فشل تغيير كلمة المرور. تحقق من كلمة المرور الحالية."
                            }
                        }
                    },
                    enabled = !isPasswordProcessing,
                    modifier = Modifier.testTag("btn_submit_change_password")
                ) {
                    Text("حفظ التغيير 🔐")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordChangeDialog = false },
                    enabled = !isPasswordProcessing
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun SocialPlatformButton(
    title: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ConnectedDeviceItemCard(
    device: AuthConnectedDevice,
    onBlockDevice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("device_item_${device.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isBlocked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (device.isCurrentDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (device.isCurrentDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (device.model.contains("Emulator") || device.model.contains("Desktop")) Icons.Filled.Computer else Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = if (device.isCurrentDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = device.deviceName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (device.isCurrentDevice) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "جهازك الحالي",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${device.model} • ${device.osVersion}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "آخر نشاط: ${device.lastActiveTimestamp} • IP: ${device.ipAddress}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            if (!device.isCurrentDevice && !device.isBlocked) {
                IconButton(
                    onClick = onBlockDevice,
                    modifier = Modifier.testTag("btn_block_device_${device.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "فصل الجهاز",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
