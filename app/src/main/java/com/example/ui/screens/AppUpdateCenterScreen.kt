package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.DubbingViewModel
import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateCenterScreen(
    viewModel: DubbingViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val updateManager = viewModel.updateManager
    val webServer = viewModel.updateWebServer

    val isServerRunning by webServer.isRunning.collectAsState()
    val lanUrl by webServer.lanUrl.collectAsState()

    val releases by updateManager.releases.collectAsState()
    val latestRelease by updateManager.latestRelease.collectAsState()
    val isChecking by updateManager.isCheckingUpdates.collectAsState()
    val downloadProgress by updateManager.downloadProgress.collectAsState()
    val serverStatusMessage by updateManager.serverStatusMessage.collectAsState()
    val isOnlineSource by updateManager.isOnlineSource.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("⚡ فحص التحديثات", "⚙️ إعدادات GitHub", "🌐 موقع voicemaster.org")

    var showConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "مركز تحديثات voicemaster.org 🚀",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF6366F1))
                            ) {
                                Text(
                                    text = "OTA Online",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "فحص وتنزيل أحدث إصدارات فويس ماستر برو مباشرة من GitHub وسيرفر التحديثات",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("update_center_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.checkForAppUpdates() },
                        enabled = !isChecking
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "فحص التحديثات")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> InAppOtaCheckTab(
                        viewModel = viewModel,
                        currentVersionCode = updateManager.currentInstalledVersionCode,
                        currentVersionName = updateManager.currentInstalledVersionName,
                        latestRelease = latestRelease,
                        allReleases = releases,
                        isChecking = isChecking,
                        downloadProgress = downloadProgress,
                        serverStatusMessage = serverStatusMessage,
                        isOnlineSource = isOnlineSource,
                        onCheckNow = { viewModel.checkForAppUpdates() },
                        onDownloadAndInstall = { rel ->
                            updateManager.downloadAndInstallUpdate(rel) { success, msg ->
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onOpenSettings = { selectedTabIndex = 1 }
                    )
                    1 -> GitHubConfigTab(
                        viewModel = viewModel,
                        onSave = {
                            Toast.makeText(context, "تم حفظ إعدادات GitHub بنجاح!", Toast.LENGTH_SHORT).show()
                            selectedTabIndex = 0
                            viewModel.checkForAppUpdates()
                        }
                    )
                    2 -> UserWebPortalTab(
                        isServerRunning = isServerRunning,
                        lanUrl = lanUrl,
                        onToggleServer = { viewModel.toggleUpdateWebServer() },
                        onCopyUrl = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("voicemaster.org Server URL", lanUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الرابط!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenBrowser = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(lanUrl))
                            context.startActivity(browserIntent)
                        },
                        onShareUrl = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "رابط موقع تحديثات voicemaster.org")
                                putExtra(Intent.EXTRA_TEXT, "قم بزيارة موقع voicemaster.org لتحميل أحدث إصدار من فويس ماستر برو:\n$lanUrl")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة موقع voicemaster.org"))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tab 1: In-App OTA Update Check & Download
 */
@Composable
fun InAppOtaCheckTab(
    viewModel: DubbingViewModel,
    currentVersionCode: Int,
    currentVersionName: String,
    latestRelease: AppRelease?,
    allReleases: List<AppRelease>,
    isChecking: Boolean,
    downloadProgress: Float?,
    serverStatusMessage: String?,
    isOnlineSource: Boolean,
    onCheckNow: () -> Unit,
    onDownloadAndInstall: (AppRelease) -> Unit,
    onOpenSettings: () -> Unit
) {
    val isUpdateAvailable = latestRelease != null && (
        latestRelease.versionCode > currentVersionCode ||
        latestRelease.versionName != currentVersionName
    )

    val onlineProvider = viewModel.updateManager.onlineProvider

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Online Source Status Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "مصدر التحديثات: GitHub (${onlineProvider.githubOwner}/${onlineProvider.githubRepo})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (serverStatusMessage != null) {
                                Text(
                                    text = serverStatusMessage,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = onOpenSettings,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تعديل ⚙️", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }

        // Status Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUpdateAvailable) Color(0xFF261938) else Color(0xFF14241B)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isUpdateAvailable) Color(0xFFD0BCFF) else Color(0xFF81C784)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isUpdateAvailable) Color(0xFF7C4DFF).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isUpdateAvailable) Icons.Default.NewReleases else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isUpdateAvailable) Color(0xFFD0BCFF) else Color(0xFF81C784),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isUpdateAvailable) "يتوفر تحديث جديد عبر الإنترنت! 🎉" else "أنت تستخدم أحدث إصدار من التطبيق ✅",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("الإصدار المثبت حالياً", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                            Text("v$currentVersionName (#$currentVersionCode)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("أحدث إصدار أونلاين", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                            Text(
                                text = if (latestRelease != null) "v${latestRelease.versionName}" else "---",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUpdateAvailable) Color(0xFF00E676) else Color.White
                            )
                        }
                    }

                    Button(
                        onClick = onCheckNow,
                        enabled = !isChecking && downloadProgress == null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(44.dp)
                            .testTag("check_for_updates_button")
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("جارٍ فحص GitHub...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("فحص التحديثات عبر الإنترنت", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Available Update Card
        if (isUpdateAvailable && latestRelease != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("available_update_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "v${latestRelease.versionName}",
                                        color = Color(0xFF7C4DFF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = latestRelease.releaseTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = latestRelease.releaseDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Changelog Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✨ الجديد في هذا الإصدار عبر الإنترنت:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = latestRelease.releaseNotesArabic,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Download Progress Indicator
                        if (downloadProgress != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("جارٍ تنزيل ملف الـ APK المباشر...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("${(downloadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF7C4DFF),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onDownloadAndInstall(latestRelease) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("download_update_button")
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("تنزيل وتثبيت التحديث مباشرة 📥", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onlineProvider.requestUninstallCurrentVersion()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("إلغاء القديم لتثبيت نظيف 🗑️", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    if (!onlineProvider.canRequestPackageInstalls()) {
                                        OutlinedButton(
                                            onClick = {
                                                onlineProvider.openInstallPermissionSettings()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB300)),
                                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("إذن التثبيت ⚙️", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Release History Timeline Header
        item {
            Text(
                text = "📜 سجل جميع الإصدارات المنشورة (${allReleases.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allReleases) { rel ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text("v${rel.versionName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            if (rel.versionCode == currentVersionCode) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ) {
                                    Text("المثبت حالياً", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF81C784), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(rel.releaseDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(rel.releaseTitle, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(rel.releaseNotesArabic, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text("📁 ${rel.apkFileName.ifBlank { "voicemaster-pro.apk" }}  •  📦 ${rel.apkSizeMb} MB", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/**
 * Tab 2: GitHub Online Repository Configuration Tab
 */
@Composable
fun GitHubConfigTab(
    viewModel: DubbingViewModel,
    onSave: () -> Unit
) {
    val onlineProvider = viewModel.updateManager.onlineProvider
    var ownerText by remember { mutableStateOf(onlineProvider.githubOwner) }
    var repoText by remember { mutableStateOf(onlineProvider.githubRepo) }
    var tokenText by remember { mutableStateOf(onlineProvider.githubToken) }
    var customUrlText by remember { mutableStateOf(onlineProvider.customApiUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "ربط التحديثات السحابية عبر GitHub 🌐",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "عند رفع أي ملف APK جديد في قسم Releases في مستودع GitHub الخاص بك، سيلتقطه التطبيق تلقائياً ويعرضه للمستخدمين للتنزيل والتثبيت الفوري.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = ownerText,
                        onValueChange = { ownerText = it },
                        label = { Text("اسم المستخدم على GitHub (Owner)") },
                        placeholder = { Text("mahme98776") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = repoText,
                        onValueChange = { repoText = it },
                        label = { Text("اسم المستودع (Repository Name)") },
                        placeholder = { Text("Mahmed") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tokenText,
                        onValueChange = { tokenText = it },
                        label = { Text("رمز الوصول الشخصي للمستودع الخاص (GitHub Token / PAT)") },
                        placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx (اختياري للمستودعات الخاصة)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customUrlText,
                        onValueChange = { customUrlText = it },
                        label = { Text("رابط مخصص بديل (اختياري Custom URL)") },
                        placeholder = { Text("https://example.com/api/version.json") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.saveGitHubUpdateConfig(ownerText, repoText, customUrlText, tokenText)
                            onSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("حفظ الإعدادات وتحديث الفحص", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💡 كيف تنشر تحديثاً جديداً في 3 خطوات:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("1. ادخل على مستودعك في GitHub واضغط على Releases ثم Draft a new release.", fontSize = 11.5.sp)
                    Text("2. اكتب رقم الإصدار في Tag (مثال: v2.7.0) واكتب عنوان وملاحظات التحديث.", fontSize = 11.5.sp)
                    Text("3. ارفع ملف الـ APK واضغط Publish Release — سيتعرف عليه التطبيق فوراً!", fontSize = 11.5.sp)
                }
            }
        }
    }
}

/**
 * Tab 3: Local Web Portal (voicemaster.org)
 */
@Composable
fun UserWebPortalTab(
    isServerRunning: Boolean,
    lanUrl: String,
    onToggleServer: () -> Unit,
    onCopyUrl: () -> Unit,
    onOpenBrowser: () -> Unit,
    onShareUrl: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Server Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isServerRunning) "خادم voicemaster.org: يعمل 🟢" else "خادم voicemaster.org: متوقف 🔴",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isServerRunning) lanUrl else "قم بتشغيل الخادم لمشاركة رابط التنزيل المباشر",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Switch(
                        checked = isServerRunning,
                        onCheckedChange = { onToggleServer() }
                    )
                }

                if (isServerRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenBrowser,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("فتح بالمتصفح", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onCopyUrl,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("نسخ الرابط", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onShareUrl,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("مشاركة", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Live Web View
        if (isServerRunning) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            webViewClient = object : WebViewClient() {
                                override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                                    view?.let {
                                        val parent = it.parent as? android.view.ViewGroup
                                        parent?.removeView(it)
                                        it.destroy()
                                    }
                                    return true
                                }
                            }
                            loadUrl("$lanUrl/user")
                        }
                    },
                    update = { webView ->
                        val target = "$lanUrl/user"
                        if (webView.url != target) {
                            webView.loadUrl(target)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
