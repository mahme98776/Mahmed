package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.update.web.WebPortalHtmlTemplates

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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("⚡ فحص التحديثات", "🌐 موقع mody.org", "👨‍💻 لوحة المطور HTML")

    var showPublishDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "مركز تحديثات mody.org 🚀",
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
                                    text = "mody.org",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF818CF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "صفحة ويب للمستخدم لمعرفة وتنزيل الإصدار + لوحة HTML للمطور",
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
                        modifier = Modifier.testTag("update_center_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "فحص التحديثات")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Web Server Live Status Bar Card
            WebServerStatusBarCard(
                isServerRunning = isServerRunning,
                lanUrl = lanUrl,
                onToggleServer = { viewModel.toggleUpdateWebServer() },
                onCopyUrl = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("mody.org Server URL", lanUrl)
                    clipboard.setPrimaryClip(clip)
                },
                onOpenBrowser = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(lanUrl))
                    context.startActivity(browserIntent)
                },
                onShareUrl = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "رابط موقع تحديثات mody.org")
                        putExtra(Intent.EXTRA_TEXT, "قم بزيارة موقع mody.org لتحميل أحدث إصدار من استوديو الدبلجة العربي:\n$lanUrl")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة موقع mody.org"))
                }
            )

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
                                fontSize = 12.5.sp
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
                        onCheckNow = { viewModel.checkForAppUpdates() },
                        onDownloadAndInstall = { rel ->
                            updateManager.simulateDownloadAndInstallUpdate(rel)
                        }
                    )
                    1 -> UserWebPortalTab(
                        userPortalUrl = "$lanUrl/user",
                        onOpenExternal = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$lanUrl/user"))
                            context.startActivity(browserIntent)
                        }
                    )
                    2 -> DeveloperDashboardTab(
                        releases = releases,
                        developerPortalUrl = "$lanUrl/developer",
                        onOpenPublishDialog = { showPublishDialog = true },
                        onDeleteRelease = { id -> updateManager.deleteRelease(id) },
                        onOpenDeveloperWebInBrowser = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$lanUrl/developer"))
                            context.startActivity(browserIntent)
                        }
                    )
                }
            }
        }
    }

    // Publish New Release Dialog (from In-App UI)
    if (showPublishDialog) {
        PublishReleaseDialog(
            currentMaxVersionCode = releases.maxOfOrNull { it.versionCode } ?: 1,
            onDismiss = { showPublishDialog = false },
            onPublish = { versionName, versionCode, title, notes, channel, sizeMb, isCritical, apkFileName ->
                updateManager.publishNewRelease(
                    versionName = versionName,
                    versionCode = versionCode,
                    releaseTitle = title,
                    releaseNotesArabic = notes,
                    apkSizeMb = sizeMb,
                    isCritical = isCritical,
                    channel = channel,
                    apkFileName = apkFileName
                )
                showPublishDialog = false
            }
        )
    }
}

/**
 * Web Server Status & Quick Action Card
 */
@Composable
fun WebServerStatusBarCard(
    isServerRunning: Boolean,
    lanUrl: String,
    onToggleServer: () -> Unit,
    onCopyUrl: () -> Unit,
    onOpenBrowser: () -> Unit,
    onShareUrl: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("web_server_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isServerRunning) Color(0xFF10B981).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isServerRunning) Color(0xFF10B981) else Color(0xFFEF4444))
                            .then(if (isServerRunning) Modifier.alpha(pulseAlpha) else Modifier)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isServerRunning) "خادم mody.org: قيد التشغيل 🟢" else "خادم mody.org: متوقف 🔴",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isServerRunning) lanUrl else "اضغط تشغيل لتمكين تحميل الـ APK وتصفح mody.org",
                            fontSize = 11.sp,
                            color = if (isServerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onToggleServer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServerRunning) Color(0xFF381E72) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isServerRunning) "إيقاف" else "تشغيل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isServerRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenBrowser,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("فتح mody.org", fontSize = 10.5.sp)
                    }

                    OutlinedButton(
                        onClick = onCopyUrl,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("نسخ الرابط", fontSize = 10.5.sp)
                    }

                    OutlinedButton(
                        onClick = onShareUrl,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 10.5.sp)
                    }
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
    onCheckNow: () -> Unit,
    onDownloadAndInstall: (AppRelease) -> Unit
) {
    val isUpdateAvailable = latestRelease != null && latestRelease.versionCode > currentVersionCode

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        text = if (isUpdateAvailable) "يتوفر تحديث جديد على mody.org! 🎉" else "أنت تستخدم أحدث إصدار من التطبيق ✅",
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
                            Text("أحدث إصدار في mody.org", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                            Text(
                                text = if (latestRelease != null) "v${latestRelease.versionName} (#${latestRelease.versionCode})" else "---",
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
                            .fillMaxWidth(0.7f)
                            .height(44.dp)
                            .testTag("check_for_updates_button")
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("جارٍ فحص mody.org...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("فحص التحديثات الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                    text = "✨ الجديد في هذا الإصدار على mody.org:",
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
                                    Text("جارٍ تنزيل الحزمة (${latestRelease.apkFileName})...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onDownloadAndInstall(latestRelease) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("download_update_button")
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("تنزيل وتثبيت التحديث (${latestRelease.apkSizeMb} MB)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
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
                            Text("v${rel.versionName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("(#${rel.versionCode})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(rel.channel.badgeColor).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = rel.channel.titleAr,
                                    color = Color(rel.channel.badgeColor),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(rel.releaseDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(rel.releaseTitle, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(rel.releaseNotesArabic, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text("📁 ${rel.apkFileName.ifBlank { "mody-app.apk" }}  •  📦 ${rel.apkSizeMb} MB  •  📥 ${rel.downloadCount} تنزيل", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/**
 * Tab 2: User Web Portal Preview & Launch (mody.org)
 */
@Composable
fun UserWebPortalTab(
    userPortalUrl: String,
    onOpenExternal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🌐 صفحة المستخدم لمعرفة الإصدار وتنزيل APK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                ) {
                    Text("mody.org", color = Color(0xFF818CF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            Button(
                onClick = onOpenExternal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("فتح بالمتصفح", fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
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
                        loadUrl(userPortalUrl)
                    }
                },
                update = { webView ->
                    webView.loadUrl(userPortalUrl)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Tab 3: Developer Dashboard & HTML Portal (mody.org/developer)
 */
@Composable
fun DeveloperDashboardTab(
    releases: List<AppRelease>,
    developerPortalUrl: String,
    onOpenPublishDialog: () -> Unit,
    onDeleteRelease: (String) -> Unit,
    onOpenDeveloperWebInBrowser: () -> Unit
) {
    var isShowingWebView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👨‍💻 صفحة المطور الخاصة (HTML Developer Portal)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("إضافة الإصدار ورفع ملفات الـ APK من جهاز المطور بسهولة", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { isShowingWebView = !isShowingWebView },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isShowingWebView) "الواجهة الأصلية" else "صفحة الـ HTML", fontSize = 11.sp)
                }

                Button(
                    onClick = onOpenPublishDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("publish_new_release_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("إضافة إصدار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isShowingWebView) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
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
                            loadUrl(developerPortalUrl)
                        }
                    },
                    update = { webView ->
                        webView.loadUrl(developerPortalUrl)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Developer Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إجمالي تنزيلات المستخدمين", fontSize = 11.sp, color = Color(0xFFC7D2FE))
                                Text("${releases.sumOf { it.downloadCount }} 📥", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14332B))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("حزم APK المنشورة", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                                Text("${releases.size} 🚀", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "📋 قائمة الإصدارات المتاحة على mody.org:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(releases) { rel ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("v${rel.versionName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("(#${rel.versionCode})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(rel.channel.badgeColor).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = rel.channel.titleAr,
                                            color = Color(rel.channel.badgeColor),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(rel.releaseTitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Text("📁 ${rel.apkFileName.ifBlank { "mody-app.apk" }}  •  📅 ${rel.releaseDate}  •  📦 ${rel.apkSizeMb} MB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(
                                onClick = { onDeleteRelease(rel.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Publish New Release Dialog
 */
@Composable
fun PublishReleaseDialog(
    currentMaxVersionCode: Int,
    onDismiss: () -> Unit,
    onPublish: (versionName: String, versionCode: Int, title: String, notes: String, channel: ReleaseChannel, sizeMb: Double, isCritical: Boolean, apkFileName: String) -> Unit
) {
    var versionName by remember { mutableStateOf("1.${currentMaxVersionCode + 1}.0") }
    var versionCode by remember { mutableIntStateOf(currentMaxVersionCode + 1) }
    var releaseTitle by remember { mutableStateOf("") }
    var releaseNotes by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(ReleaseChannel.STABLE) }
    var apkSizeMb by remember { mutableStateOf("18.5") }
    var apkFileName by remember { mutableStateOf("mody-dubbing-v1.${currentMaxVersionCode + 1}.0.apk") }
    var isCritical by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🚀 إضافة إصدار جديد إلى mody.org", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = { 
                            versionName = it
                            apkFileName = "mody-dubbing-v${it.trim()}.apk"
                        },
                        label = { Text("رقم الإصدار") },
                        placeholder = { Text("1.3.0") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = versionCode.toString(),
                        onValueChange = { versionCode = it.toIntOrNull() ?: versionCode },
                        label = { Text("كود البناء") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = apkFileName,
                    onValueChange = { apkFileName = it },
                    label = { Text("اسم ملف الـ APK المحمّل") },
                    placeholder = { Text("mody-dubbing-v1.3.0.apk") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = releaseTitle,
                    onValueChange = { releaseTitle = it },
                    label = { Text("عنوان التحديث") },
                    placeholder = { Text("ميزات تصدير الفيديو والذكاء الاصطناعي") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    label = { Text("سجل التغييرات بالعربية (Changelog)") },
                    placeholder = { Text("• إضافة ميزة...\n• تحسين أداء الصوت...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCritical = !isCritical }
                ) {
                    Checkbox(
                        checked = isCritical,
                        onCheckedChange = { isCritical = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("تحديث إجباري ملزم لجميع المستخدمين ⚠️", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (versionName.isNotBlank() && releaseTitle.isNotBlank()) {
                        onPublish(
                            versionName,
                            versionCode,
                            releaseTitle,
                            releaseNotes.ifBlank { "تحسينات عامة على الأداء والميزات." },
                            selectedChannel,
                            apkSizeMb.toDoubleOrNull() ?: 18.5,
                            isCritical,
                            apkFileName.ifBlank { "mody-dubbing-v${versionName.trim()}.apk" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Text("نشر على mody.org 📡", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
