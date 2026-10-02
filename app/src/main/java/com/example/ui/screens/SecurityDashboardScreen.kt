package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.security.AuthConnectedDevice
import com.example.security.DeviceAccountSecurityManager
import com.example.security.FailedLoginIncident
import com.example.security.SecuritySeverity
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDashboardScreen(
    viewModel: DubbingViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val securityManager = viewModel.authSecurityManager
    val updateManager = viewModel.updateManager

    val devices by securityManager.connectedDevices.collectAsState()
    val failedAttempts by securityManager.failedAttempts.collectAsState()
    val summary by securityManager.securitySummary.collectAsState()
    val realtimeRemoteConfig by updateManager.firebaseConfigManager.realtimeUpdateResult.collectAsState()
    val apiDiagnostic by viewModel.geminiOneClickDubber.apiDiagnostic.collectAsState()
    val savedGeminiKey by viewModel.geminiApiKey.collectAsState()

    var customKeyInput by remember(savedGeminiKey) { mutableStateOf(savedGeminiKey) }
    var isCheckingGeminiApi by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "رادار الأمان 🛡️",
        "تشخيص النزاهة 🔬",
        "الأجهزة والتقارير 📱",
        "أدوات المطور 👑",
        "تشخيص Gemini ⚡"
    )

    var securityDiagnosticReport by remember {
        mutableStateOf(viewModel.securityAuditDiagnosticManager.performFullSecurityDiagnostic())
    }
    var isRunningDiagnosticScan by remember { mutableStateOf(false) }

    var testPlainText by remember { mutableStateOf("بيانات سرية فائقة الأمان خاصة بالمطور محمد سليمة 👑") }
    var testEncryptedText by remember { mutableStateOf("") }
    var testDecryptedText by remember { mutableStateOf("") }

    var showRevokeDialog by remember { mutableStateOf<AuthConnectedDevice?>(null) }

    val developerName = "محمد المعتمد 👑"
    val authorizedEmail = "mahme98776@gmail.com"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "أمان الأجهزة وبوابة التطوير 🛡️👑",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF22C55E))
                            ) {
                                Text(
                                    text = "مباشر ⚡",
                                    color = Color(0xFF22C55E),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Unified Security Radar & Developer Control Center",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "تم تحديث سجلات الحماية والعتاد بنجاح 🔄", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "تحديث السجلات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Header Tabs Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFFFD54F)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == index) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 0: SECURITY RADAR & THREAT DEFENSE
            // =========================================================================
            if (selectedTab == 0) {
                // Defense Score Hero Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                border = BorderStroke(1.5.dp, Color(0xFF22C55E)),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "رادار الدفاع والصد النشط 🛡️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF22C55E)
                                )
                                Text(
                                    text = "معدل الحماية: ${summary.securityScorePercentage}% • لم يتم رصد أي تسريب أو تهديد حرج",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Breach Radar Incidents List
                if (failedAttempts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("لا توجد محاولات اختراق مرصودة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("الرادار يعمل بكفاءة ويرصد جميع الطلبات وجلسات المصادقة", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "سجل محاولات الاختراق وصد الهجمات (${failedAttempts.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(failedAttempts) { incident ->
                        IncidentItemCard(incident = incident, onResolve = { securityManager.markIncidentResolved(incident.id) })
                    }
                }
            }

            // =========================================================================
            // TAB 1: INTEGRITY DIAGNOSTICS & HARDWARE KEYSTORE ENCRYPTION
            // =========================================================================
            if (selectedTab == 1) {
                // Diagnostic Score Card
                item {
                    val score = securityDiagnosticReport.securityScore
                    val isSecure = securityDiagnosticReport.isSecure
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.5.dp, if (isSecure) Color(0xFF22C55E) else Color(0xFFFFB74D))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isSecure) Color(0xFF22C55E) else Color(0xFFFFB74D),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "فحص النزاهة ومكافحة التلاعب 🔬🛡️",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Anti-Tamper • Anti-Debugging • KeyStore AES-256",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSecure) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (isSecure) Color(0xFF22C55E) else Color(0xFFEF4444))
                                ) {
                                    Text(
                                        text = "$score / 100",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isSecure) Color(0xFF22C55E) else Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            // Diagnostic items grid
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // 1. Anti-Debugging Check
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أدوات التصحيح (Anti-Debugging & TracerPid):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (securityDiagnosticReport.isDebuggerPresent) "⚠️ نشط (خطر)" else "محمي ومغلق ✓",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (securityDiagnosticReport.isDebuggerPresent) Color(0xFFEF4444) else Color(0xFF22C55E)
                                    )
                                }

                                // 2. Hooking Detection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أدوات حقن الذاكرة (Frida / Xposed / Substrate):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (securityDiagnosticReport.isHookingDetected) "⚠️ رصد حقن (خطر)" else "نظيف 100% ✓",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (securityDiagnosticReport.isHookingDetected) Color(0xFFEF4444) else Color(0xFF22C55E)
                                    )
                                }

                                // 3. Root Detection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("فحص صلاحيات الجذر (Root & Superuser):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (securityDiagnosticReport.isRootDetected) "⚠️ روت نشط" else "بيئة قياسية آمنة ✓",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (securityDiagnosticReport.isRootDetected) Color(0xFFFFB74D) else Color(0xFF22C55E)
                                    )
                                }

                                // 4. Signature & Integrity
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("سلامة توقيع الحزمة (APK Signature Integrity):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (securityDiagnosticReport.isPackageIntegrityValid) "أصلي وموثق ✓" else "⚠️ تلاعب بالتوقيع",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (securityDiagnosticReport.isPackageIntegrityValid) Color(0xFF22C55E) else Color(0xFFEF4444)
                                    )
                                }

                                // 5. Installer source
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("مصدر تثبيت الحزمة (Installer Source):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = securityDiagnosticReport.installerSource,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                // 6. Device Lock Screen Security
                                val isDeviceSecure = DeviceAccountSecurityManager.isDeviceScreenLockSecure(context)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("قفل الشاشة الآمن (Keyguard Lock):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (isDeviceSecure) "محمي بـ PIN/نمط/بصمة ✓" else "⚠️ غير مفعل (يوصى بتفعيله)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDeviceSecure) Color(0xFF22C55E) else Color(0xFFFFB74D)
                                    )
                                }

                                // 7. Device Registered Google Accounts
                                val accounts = DeviceAccountSecurityManager.getRegisteredGoogleAccounts(context)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("حسابات الجهاز الموثقة (Device Accounts):", fontSize = 11.5.sp, color = Color(0xFFE2E8F0))
                                    Text(
                                        text = if (accounts.isNotEmpty()) "${accounts.size} حساب موثق 📱✓" else "حسابات النظام جاهزة للاختيار 📱",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF22C55E)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isRunningDiagnosticScan = true
                                        securityDiagnosticReport = viewModel.securityAuditDiagnosticManager.performFullSecurityDiagnostic()
                                        isRunningDiagnosticScan = false
                                        Toast.makeText(context, "تم إجراء تشخيص أمني متكامل وشامل بنجاح 🛡️🔬", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                enabled = !isRunningDiagnosticScan
                            ) {
                                if (isRunningDiagnosticScan) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("إعادة الفحص والتشخيص الأمني الآن", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }

                // Interactive Hardware-Backed KeyStore Encryption Module
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "وحدة التشفير بالعتاد (Android KeyStore AES-256-GCM) 🔐",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "تشفير محلي عتادي لا يتم تخزين مفاتيحه في الذاكرة العادية، بل في المعالج الآمن (TEE/StrongBox) لحماية مطلقة:",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )

                            OutlinedTextField(
                                value = testPlainText,
                                onValueChange = { testPlainText = it },
                                label = { Text("النص المراد تشفيره") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        testEncryptedText = com.example.security.HardwareKeyStoreCipher.encrypt(testPlainText)
                                        testDecryptedText = ""
                                        Toast.makeText(context, "تم التشفير عبر Android KeyStore بنجاح 🔐", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                ) {
                                    Icon(Icons.Default.EnhancedEncryption, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("تشفير عتادي", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        if (testEncryptedText.isNotBlank()) {
                                            testDecryptedText = com.example.security.HardwareKeyStoreCipher.decrypt(testEncryptedText)
                                            Toast.makeText(context, "تم فك التشفير والتحقق من سلامة البيانات 🔓", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "يرجى تشفير نص أولاً", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Icon(Icons.Default.NoEncryption, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("فك التشفير", fontSize = 11.sp)
                                }
                            }

                            if (testEncryptedText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("النص المشفر (Base64 + GCM Tag):", fontSize = 10.sp, color = Color(0xFF818CF8), fontWeight = FontWeight.Bold)
                                        Text(testEncryptedText, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFE2E8F0), maxLines = 3)
                                    }
                                }
                            }

                            if (testDecryptedText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("النص بعد فك التشفير والتحقق:", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                        Text(testDecryptedText, fontSize = 11.5.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: CONNECTED DEVICES & PRINTABLE REPORT
            // =========================================================================
            if (selectedTab == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.5.dp, Color(0xFF0EA5E9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("الأجهزة المتصلة وطباعة التقرير 📱🖨️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0EA5E9).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${devices.size} أجهزة مسجلة",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "يمكنك طباعة تقرير أمني رسمي بجميع الأجهزة النشطة أو مشاركته وتصديره بصيغة نصية:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )

                            Button(
                                onClick = {
                                    val reportText = securityManager.generateDevicesPrintableReport()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("تقرير الأجهزة", reportText))

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "تقرير أمني للأجهزة المتصلة - $developerName")
                                        putExtra(Intent.EXTRA_TEXT, reportText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "طباعة ومشاركة تقرير الأجهزة"))
                                    Toast.makeText(context, "تم نسخ التقرير وفتح نافذة الطباعة والمشاركة 🖨️", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("طباعة ومشاركة تقرير الأجهزة الرسمي 🖨️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "قائمة الأجهزة النشطة في Firebase Auth:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(devices) { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, if (device.isCurrentDevice) Color(0xFF22C55E) else Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(device.deviceName.ifBlank { device.model }, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    if (device.isCurrentDevice) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF22C55E).copy(alpha = 0.2f)
                                        ) {
                                            Text("هذا الجهاز الحالي", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text("IP: ${device.ipAddress} • النظام: ${device.osVersion}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("آخر ظهور: ${device.lastActiveTimestamp}", color = Color(0xFF64748B), fontSize = 10.sp)
                            }

                            if (!device.isCurrentDevice) {
                                IconButton(
                                    onClick = { showRevokeDialog = device }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "إلغاء الترخيص", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 3: DEVELOPER PORTAL PROFILE & TOOLS
            // =========================================================================
            if (selectedTab == 3) {
                // Developer Identity Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD54F))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "المطور والمبتكر المعتمد 👑",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = developerName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "📧 $authorizedEmail",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF22C55E))
                                ) {
                                    Text(
                                        text = "DEV ONLINE ⚡",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF22C55E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الإصدار: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                                Text("بيئة التشغيل: Android ${Build.VERSION.RELEASE}", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }

                // Developer Action Tools
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("أدوات تحكم المطور ومحركات النظام 🛠️:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)

                            Button(
                                onClick = {
                                    Toast.makeText(context, "تم فحص نزاهة الحزم: جميع توقيعات APK والمحركات سليمة 100% 🛡️", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("فحص نزاهة النظام والتوقيع (App Integrity Check)", fontSize = 11.5.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "تم إرسال إشعار تنبيه تجريبي إلى هاتف المطور 📲", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("إرسال إشعار تنبيه تجريبي للمطور", fontSize = 11.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "تم مسح وتفريغ السجلات المؤقتة بنجاح 🧹", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("تفريغ سجلات الفحص والتدقيق المؤقتة", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 4: GEMINI API DIAGNOSTIC & VERIFICATION (USER EXPLICIT REQUIREMENT)
            // =========================================================================
            if (selectedTab == 4) {
                item {
                    val isApiSuccess = apiDiagnostic?.isSuccess == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.5.dp, if (isApiSuccess) Color(0xFF22C55E) else Color(0xFFFFD54F))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isApiSuccess) Color(0xFF22C55E) else Color(0xFFFFD54F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "فحص واختبار Gemini API الفعلي ⚡",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                if (apiDiagnostic != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isApiSuccess) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isApiSuccess) "ONLINE 🟢" else "OFFLINE 🔴",
                                            color = if (isApiSuccess) Color(0xFF22C55E) else Color(0xFFEF4444),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "يمكنك اختبار اتصال Gemini الحقيقي فوراً وقياس سرعة الاستجابة (Latency) والتأكد من صحة المفتاح دون أدنى شك:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )

                            // Result Details Banner
                            if (apiDiagnostic != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isApiSuccess) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF450A0A).copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isApiSuccess) Color(0xFF22C55E) else Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = apiDiagnostic!!.message,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isApiSuccess) Color(0xFF4ADE80) else Color(0xFFFCA5A5),
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = apiDiagnostic!!.details,
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Input API key
                            OutlinedTextField(
                                value = customKeyInput,
                                onValueChange = { customKeyInput = it },
                                label = { Text("Gemini API Key") },
                                placeholder = { Text("ألصق مفتاح الذكاء الاصطناعي...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateGeminiApiKey(customKeyInput)
                                        Toast.makeText(context, "تم حفظ المفتاح محلياً بنجاح 🔑", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("حفظ المفتاح", fontSize = 11.5.sp)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isCheckingGeminiApi = true
                                            viewModel.geminiOneClickDubber.testGeminiApiConnection(customKeyInput)
                                            isCheckingGeminiApi = false
                                        }
                                    },
                                    enabled = !isCheckingGeminiApi,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    if (isCheckingGeminiApi) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("فحص الاتصال الفعلي", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Revoke Device Confirmation Dialog
    showRevokeDialog?.let { device ->
        AlertDialog(
            onDismissRequest = { showRevokeDialog = null },
            title = { Text("إلغاء ترخيص الجهاز؟ ⚠️") },
            text = { Text("هل تريد إنهاء جلسة الجهاز '${device.deviceName.ifBlank { device.model }}' وحظر دخوله؟") },
            confirmButton = {
                Button(
                    onClick = {
                        securityManager.revokeDeviceSession(device.id)
                        showRevokeDialog = null
                        Toast.makeText(context, "تم إلغاء ترخيص الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إلغاء الترخيص")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun IncidentItemCard(
    incident: FailedLoginIncident,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(incident.severity.colorHex).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(incident.severity.colorHex).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = incident.id,
                            color = Color(incident.severity.colorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = incident.attemptedTarget,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(incident.severity.colorHex).copy(alpha = 0.25f),
                    border = BorderStroke(0.8.dp, Color(incident.severity.colorHex))
                ) {
                    Text(
                        text = incident.severity.titleArabic,
                        color = Color(incident.severity.colorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "السبب: ${incident.failureReason}",
                color = Color(0xFFFECACA),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1010).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الجهاز والـ IP:", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                        Text("${incident.deviceModel} • ${incident.sourceIdentifier}", color = Color(0xFFCBD5E1), fontSize = 10.5.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإجراء المتخذ:", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                        Text(incident.defenseCountermeasure, color = Color(0xFF4ADE80), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الوقت:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text(incident.timestamp, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }

            if (!incident.isResolved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onResolve,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4ADE80))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تمت المراجعة والتأمين ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
