package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.components.OnboardingHelpDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.DubbingTargetLanguage
import com.example.audio.DubbingPacing
import com.example.export.ExportResolution
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import com.example.ui.DubbingViewModel
import com.example.ui.components.VersionInfoCard
import com.example.BuildConfig

@Composable
fun SettingsScreen(
    viewModel: DubbingViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToUpdateCenter: () -> Unit = {},
    onNavigateToDeveloperPortal: () -> Unit = {},
    onNavigateToSecurityDashboard: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.currentAppLanguage.collectAsStateWithLifecycle()
    val exportConfig by viewModel.videoExportConfig.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedGeminiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val isServerRunning by viewModel.updateWebServer.isRunning.collectAsStateWithLifecycle()
    val lanUrl by viewModel.updateWebServer.lanUrl.collectAsStateWithLifecycle()
    val userProfile by viewModel.authService.userProfile.collectAsStateWithLifecycle()
    val isSyncing by viewModel.authService.isSyncing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.authService.syncStatus.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    var keyInputText by remember(savedGeminiKey) {
        mutableStateOf(if (savedGeminiKey.startsWith("AQ.Ab8") || savedGeminiKey == "MY_GEMINI_API_KEY") "" else savedGeminiKey)
    }
    var showExtractKeyDialog by remember { mutableStateOf(false) }
    var showAllLanguages by remember { mutableStateOf(false) }
    var showOnboardingDialogInSettings by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.settingsTitle(appLanguage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${appLanguage.flagEmoji} ${appLanguage.displayName} • ${if (isDarkMode) "الوضع الليلي 🌙" else "الوضع النهاري ☀️"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Google Account & Gemini Cloud Computing Card
        item {
            SettingsCategoryCard(
                title = "حساب Google والحوسبة السحابية لـ Gemini ☁️",
                icon = Icons.Default.Cloud,
                accentColor = Color(0xFF1A73E8)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4).copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (userProfile.isGeminiCloudSaved) "محفوظ ومزامن في حوسبة Gemini السحابية ☁️✓" else "جلسة محلية",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (userProfile.isGeminiCloudSaved) Color(0xFF34A853) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "⚡ الباقة السحابية: ${userProfile.geminiCloudTier}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "📁 عدد المشاريع السحابية: ${userProfile.totalCloudProjects} مشاريع محفوظة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (syncStatus.isNotBlank()) {
                                Text(
                                    text = "الحالة: $syncStatus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8)
                            )
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("واجهة حسابي (Profile)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.authService.saveUserToGeminiCloudComputing(userProfile)
                                    Toast.makeText(context, "تمت المزامنة وحفظ الحساب في سحابة Gemini بنجاح ☁️", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isSyncing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("مزامنة سحابية 🔄", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.authService.signOut()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("تسجيل الخروج / تبديل الحساب 🚪", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // 2. Interface Language Section (16 Supported Languages)
        item {
            SettingsCategoryCard(
                title = AppStrings.languageSection(appLanguage),
                icon = Icons.Default.Language,
                accentColor = Color(0xFF3B82F6)
            ) {
                Text(
                    text = "اختر لغة واجهة البرنامج (16 لغة مدعومة بالكامل مع تغيير الاتجاه التلقائي):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                val displayedLanguages = if (showAllLanguages) {
                    AppLanguage.entries
                } else {
                    AppLanguage.entries.take(6)
                }

                displayedLanguages.chunked(2).forEach { rowLangs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowLangs.forEach { langItem ->
                            val isSelected = langItem == appLanguage
                            Surface(
                                onClick = { viewModel.setAppLanguage(langItem) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_select_${langItem.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(langItem.flagEmoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = langItem.nativeName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = langItem.displayName,
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = { showAllLanguages = !showAllLanguages },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showAllLanguages) "عرض لغات أقل ▲" else "عرض كافة اللغات (${AppLanguage.entries.size} لغة) ▼",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Theme & Appearance Section
        item {
            SettingsCategoryCard(
                title = AppStrings.themeSection(appLanguage),
                icon = Icons.Default.Palette,
                accentColor = Color(0xFFEC4899)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الوضع الليلي والنهاري (Theme Mode)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isDarkMode) "الوضع الليلي نشط لحماية العين في الاستوديو 🌙" else "الوضع النهاري عالي السطوع والوضوح ☀️",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // 4. Default Dubbing & Audio Settings
        item {
            SettingsCategoryCard(
                title = "🎙️ إعدادات الذكاء الاصطناعي والدبلجة",
                icon = Icons.Default.Tune,
                accentColor = Color(0xFF10B981)
            ) {
                // Auto Ducking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تخفيض صوت الخلفية التلقائي (Smart Ducking)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "خفض صوت الفيديو الأصلي تلقائياً عند نطق الحوارات بالدبلجة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isAutoDuckingEnabled,
                        onCheckedChange = { viewModel.toggleAutoDucking(it) }
                    )
                }

                Divider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Auto Gender Detection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "التعرف الذكي على جنس المتحدث (Gender Detection)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "اكتشاف صوت الذكر/الأنثى/الطفل تلقائياً ومطابقة النبرة الصوتية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.autoVoiceRecognitionEnabled,
                        onCheckedChange = { viewModel.toggleAutoVoiceRecognition(it) }
                    )
                }

                Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // --- Jetpack DataStore Persistent Preferences ---
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "تفضيلات المستخدم المحفوظة محلياً عبر DataStore ⚡",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 1. Default Speech Rate (DataStore)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سرعة نطق الصوت الافتراضية (Speech Rate)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.2f", userSettings.speechRate)}x",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "يتم حفظ وتطبيق سرعة الصوت تلقائياً لجميع عمليات الدبلجة واسترجاعها عند فتح التطبيق",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Slider(
                        value = userSettings.speechRate,
                        onValueChange = { viewModel.updateSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 14
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.85f to "0.85x هادئ",
                            1.00f to "1.00x قياسي",
                            1.25f to "1.25x متوسط",
                            1.50f to "1.50x سريع"
                        ).forEach { (rate, label) ->
                            OutlinedButton(
                                onClick = { viewModel.updateSpeechRate(rate) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = if (Math.abs(userSettings.speechRate - rate) < 0.05f) {
                                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(label, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Divider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 2. Default Voice Pitch (DataStore)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نبرة الصوت الافتراضية (Voice Pitch)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.2f", userSettings.voicePitch)}x",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ضبط حدة أو عمق النبرة الصوتية وحفظها بشكل دائم في DataStore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Slider(
                        value = userSettings.voicePitch,
                        onValueChange = { viewModel.updateVoicePitch(it) },
                        valueRange = 0.6f..1.5f,
                        steps = 8
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.85f to "0.85x عميق",
                            1.00f to "1.00x طبيعي",
                            1.20f to "1.20x حاد"
                        ).forEach { (pitch, label) ->
                            OutlinedButton(
                                onClick = { viewModel.updateVoicePitch(pitch) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = if (Math.abs(userSettings.voicePitch - pitch) < 0.05f) {
                                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(label, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Action Buttons: Test Voice & Reset DataStore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val baseProfile = viewModel.ttsManager.voiceProfiles.firstOrNull() ?: com.example.audio.VoiceProfile(
                                id = "preview_voice",
                                titleArabic = "صوت الاستوديو الاحترافي",
                                subtitleArabic = "هندسة الصوت الذكية",
                                emoji = "🎙️",
                                pitch = 1.0f,
                                speechRate = 1.0f
                            )
                            val customProfile = baseProfile.copy(
                                pitch = userSettings.voicePitch,
                                speechRate = userSettings.speechRate
                            )
                            viewModel.ttsManager.speakText(
                                text = "مرحباً بك في فويس ماستر برو، استوديو هندسة الصوت والدبلجة الفورية بالذكاء الاصطناعي.",
                                profile = customProfile
                            )
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("تشغيل صوت الاستوديو 🔊", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetUserSettings() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("استعادة الافتراضي", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // Local / Remote Python Lingo Dubbing Server Settings Card
        item {
            com.example.ui.components.LingoServerSettingsCard(
                viewModel = viewModel,
                userSettings = userSettings
            )
        }

        // Help & Onboarding Walkthrough Category Card
        item {
            SettingsCategoryCard(
                title = "دليل الاستخدام والترحيب الشامل 💡",
                icon = Icons.Default.Info,
                accentColor = Color(0xFF00E5FF)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "دليل تفاعلي متكامل يشرح بالتفصيل خطوات استخدام التطبيق، هندسة الترددات الصوتية بالذكاء الاصطناعي، وخفض صوت الخلفية تلقائياً للمستخدمين الجدد.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                    Button(
                        onClick = { showOnboardingDialogInSettings = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_open_onboarding_from_settings"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0C1726))
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("فتح جولة الترحيب والتعليمات التفاعلية 🚀", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        // 5. Gemini AI Key & Cloud Config
        item {
            SettingsCategoryCard(
                title = "مفتاح الذكاء الاصطناعي (Gemini AI Key) 🤖🔑",
                icon = Icons.Default.Key,
                accentColor = Color(0xFF6750A4)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يُستخدم مفتاح الذكاء الاصطناعي لتشغيل خدمات التفريغ الصوتي الفائق STT، الترجمة الاحترافية، والذكاء الاصطناعي. هذا المفتاح خاص بك تماماً ومحفوظ محلياً فقط على جهازك بدون أي مشاركة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    // Step-by-step Guide Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF6750A4).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📖 خطوات الحصول على مفتاحك المجاني في دقيقة واحدة:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF381E72)
                            )
                            Text(
                                text = "1️⃣ اضغط على الزر أدناه لفتح منصة Google AI Studio الرسمية.\n2️⃣ سجل الدخول بحساب Google ثم انقر على «Create API key».\n3️⃣ انسخ المفتاح الذي يبدأ بـ AIza... والصقه في المربع أدناه ثم اضغط حفظ.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Direct Link & Extraction Dialog Button
                    Button(
                        onClick = {
                            showExtractKeyDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6EBB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("open_aistudio_apikey_btn")
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("🔑 استخراج مفتاح مجاني جديد (Google AI Studio)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = keyInputText,
                        onValueChange = { keyInputText = it },
                        label = { Text("أدخل مفتاح Gemini API الخاص بك") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateGeminiApiKey(keyInputText)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("save_gemini_key_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("حفظ وتفعيل المفتاح ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (keyInputText.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    keyInputText = ""
                                    viewModel.clearGeminiApiKey()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("حذف المفتاح 🗑️", fontSize = 11.sp)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (keyInputText.isNotBlank()) Color(0xFFD7E8CD) else Color(0xFFFDE8E8),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (keyInputText.isNotBlank()) "حالة المفتاح: مسجل ومحفوظ محلياً على جهازك 🔒🟢" else "حالة المفتاح: غير مضبوط (يمكنك إنشاء مفتاح مجاني بالزر أعلاه) ⚠️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (keyInputText.isNotBlank()) Color(0xFF1E4620) else Color(0xFF8A1F1D)
                            )
                        }
                    }
                }
            }
        }

        // 6. Video & Audio Export Settings
        item {
            SettingsCategoryCard(
                title = AppStrings.audioExportSection(appLanguage),
                icon = Icons.Default.Movie,
                accentColor = Color(0xFFF59E0B)
            ) {
                Text(
                    text = "دقة تصدير الفيديو الافتراضية:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ExportResolution.SD_480P to "480p SD",
                        ExportResolution.HD_720P to "720p HD ⚡",
                        ExportResolution.FHD_1080P to "1080p FHD"
                    ).forEach { (res, label) ->
                        val isSelected = exportConfig.resolution == res
                        Surface(
                            onClick = {
                                viewModel.updateVideoExportConfig(exportConfig.copy(resolution = res))
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Subtitles burning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حرق شريط الترجمة التلقائية على الفيديو (Subtitles)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "إضافة نصوص الحوارات المترجمة أسفل المشهد بدقة عالية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = exportConfig.burnSubtitles,
                        onCheckedChange = {
                            viewModel.updateVideoExportConfig(exportConfig.copy(burnSubtitles = it))
                        }
                    )
                }
            }
        }

        // 6. Cache, Storage & Reset Section
        item {
            SettingsCategoryCard(
                title = AppStrings.storageSection(appLanguage),
                icon = Icons.Default.CleaningServices,
                accentColor = Color(0xFFEF4444)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.clearAudioCache() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تنظيف الكاش 🧹",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.resetAllSettings() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "إعادة ضبط الإعدادات 🔄",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // 7. Version Info & Product Updates Center
        item {
            VersionInfoCard(
                viewModel = viewModel,
                onNavigateToUpdateCenter = onNavigateToUpdateCenter
            )
        }

        // 8. Unified Device Security & Developer Portal (Exclusive to Developer mahme98776@gmail.com)
        if (userProfile.email.trim().lowercase() == "mahme98776@gmail.com") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSecurityDashboard() }
                        .testTag("settings_security_dashboard_entry_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "أمان الأجهزة وبوابة التطوير 🛡️👑",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFFFD54F)
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF22C55E).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "خاص بالمطور ⚡",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF22C55E),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "لوحة التحكم الخاصة بالمطور: رادار صد الهجمات وتتبع أجهزة Firebase Auth، طباعة التقارير، وأدوات المطور وفحص المحركات",
                                fontSize = 10.5.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 14.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 9. About App & Intellectual Property Copyright
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "فويس ماستر برو | VoiceMaster Pro v2.6",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "استوديو الدبلجة وهندسة الصوت الاحترافي بالذكاء الاصطناعي مع معالجة ومزامنة الفيديو الفورية",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "⚖️ جميع حقوق الملكية الفكرية والنشر محفوظة © 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "محمد رضا محمود محمود سليمه من أسس هذا التطبيق (محمد سليمة)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showOnboardingDialogInSettings) {
        OnboardingHelpDialog(
            onDismiss = { showOnboardingDialogInSettings = false },
            onComplete = { dontShowAgain ->
                showOnboardingDialogInSettings = false
                if (dontShowAgain) {
                    coroutineScope.launch {
                        viewModel.userSettingsDataStore.updateHasSeenOnboarding(true)
                    }
                }
            }
        )
    }

    if (showExtractKeyDialog) {
        com.example.ui.components.ExtractGeminiApiKeyDialog(
            currentSavedKey = keyInputText,
            onSaveKey = {
                keyInputText = it
                viewModel.updateGeminiApiKey(it)
            },
            onClearKey = {
                keyInputText = ""
                viewModel.clearGeminiApiKey()
            },
            onDismiss = { showExtractKeyDialog = false },
            aiClient = viewModel.geminiUnifiedClient
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}
