package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.DubbingViewModel
import com.example.ui.components.PersistentAiChatOverlay
import com.example.ui.screens.AiDubbingScreen
import com.example.ui.screens.AppGuideAndPdfScreen
import com.example.ui.screens.ClipsLibraryScreen
import com.example.ui.screens.HumanVoiceLibraryScreen
import com.example.ui.screens.InstantDubbingScreen
import com.example.ui.screens.ProjectsListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SoundEffectsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.VideoAutoDubberScreen
import com.example.ui.screens.VideoProcessingPreviewScreen
import com.example.ui.screens.VideoAudioSyncLayoutScreen
import com.example.ui.screens.AppUpdateCenterScreen
import com.example.ui.screens.DedicatedRecordingScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Sync

enum class AppTab(
    val titleArabic: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    STUDIO("الاستوديو", Icons.Filled.Mic, Icons.Outlined.Mic, "nav_studio"),
    RECORDING("تسجيل مباشر", Icons.Filled.Mic, Icons.Outlined.Mic, "nav_recording"),
    VOICE_LIBRARY("مكتبة الأصوات", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver, "nav_voice_library"),
    VIDEO_DUB("دبلجة فيديو AI", Icons.Filled.Videocam, Icons.Outlined.Videocam, "nav_video_dub"),
    SYNC_STUDIO("مزامنة دقيقة", Icons.Filled.Sync, Icons.Outlined.Sync, "nav_video_sync"),
    PROCESSING_PREVIEW("معاينة ومعالجة", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_processing_preview"),
    INSTANT_DUB("دبلجة فورية AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_instant_dub"),
    AI_DUB("نص لصوت AI", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "nav_ai_dub"),
    CLIPS("المشاهد", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_clips"),
    PROJECTS("مشاريعي", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_projects"),
    SOUNDBOARD("المؤثرات", Icons.Filled.Headphones, Icons.Outlined.Headphones, "nav_soundboard"),
    SETTINGS("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
    HELP_GUIDE("دليل & PDF", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_help_guide"),
    UPDATE_CENTER("مركز التحديثات والويب", Icons.Filled.SystemUpdate, Icons.Outlined.SystemUpdate, "nav_update_center")
}

class MainActivity : ComponentActivity() {
    private val dubbingViewModel: DubbingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by dubbingViewModel.isDarkMode.collectAsStateWithLifecycle()
            val appLanguage by dubbingViewModel.currentAppLanguage.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                // Dynamic LayoutDirection support: RTL for Arabic, Persian, Urdu; LTR for other languages
                val layoutDirection = if (appLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    MainScreen(
                        viewModel = dubbingViewModel,
                        isDarkMode = isDarkMode,
                        appLanguage = appLanguage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DubbingViewModel,
    isDarkMode: Boolean = true,
    appLanguage: AppLanguage = AppLanguage.ARABIC
) {
    var selectedTab by remember { mutableStateOf(AppTab.VIDEO_DUB) }
    var showMoreToolsSheet by remember { mutableStateOf(false) }

    // Primary 5 tabs for clean, uncomplicated and direct navigation
    val primaryTabs = listOf(
        AppTab.VIDEO_DUB,
        AppTab.STUDIO,
        AppTab.VOICE_LIBRARY,
        AppTab.PROJECTS,
        AppTab.SETTINGS
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Column(
                modifier = Modifier
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                PersistentAiChatOverlay(
                    viewModel = viewModel,
                    currentTab = selectedTab,
                    onNavigateToTab = { targetTab -> selectedTab = targetTab }
                )

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_navigation_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Primary Main Tabs
                        primaryTabs.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Surface(
                                onClick = { selectedTab = tab },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(tab.testTag)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                                        contentDescription = tab.titleArabic,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    val tabLabel = when (tab) {
                                        AppTab.VIDEO_DUB -> AppStrings.tabVideoDub(appLanguage)
                                        AppTab.STUDIO -> AppStrings.tabStudio(appLanguage)
                                        AppTab.VOICE_LIBRARY -> AppStrings.tabVoices(appLanguage)
                                        AppTab.PROJECTS -> AppStrings.tabProjects(appLanguage)
                                        AppTab.SETTINGS -> AppStrings.tabSettings(appLanguage)
                                        else -> tab.titleArabic
                                    }
                                    Text(
                                        text = tabLabel,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // More Tools Button
                        val isSecondaryTabSelected = selectedTab !in primaryTabs
                        Surface(
                            onClick = { showMoreToolsSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSecondaryTabSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_more_tools_button")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "المزيد",
                                    tint = if (isSecondaryTabSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (isSecondaryTabSelected) selectedTab.titleArabic else AppStrings.moreTools(appLanguage),
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSecondaryTabSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSecondaryTabSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.STUDIO -> StudioScreen(
                    viewModel = viewModel,
                    onNavigateToLibrary = { selectedTab = AppTab.CLIPS },
                    onNavigateToProcessingPreview = { selectedTab = AppTab.PROCESSING_PREVIEW },
                    onNavigateToSync = { selectedTab = AppTab.SYNC_STUDIO },
                    onNavigateToGuide = { selectedTab = AppTab.HELP_GUIDE }
                )
                AppTab.RECORDING -> DedicatedRecordingScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToHelp = { selectedTab = AppTab.HELP_GUIDE },
                    onBack = { selectedTab = AppTab.STUDIO }
                )
                AppTab.VOICE_LIBRARY -> HumanVoiceLibraryScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToTts = { selectedTab = AppTab.AI_DUB }
                )
                AppTab.VIDEO_DUB -> VideoAutoDubberScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.SYNC_STUDIO -> VideoAudioSyncLayoutScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.PROCESSING_PREVIEW -> VideoProcessingPreviewScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToLibrary = { selectedTab = AppTab.CLIPS }
                )
                AppTab.INSTANT_DUB -> InstantDubbingScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.AI_DUB -> AiDubbingScreen(
                    viewModel = viewModel
                )
                AppTab.CLIPS -> ClipsLibraryScreen(
                    viewModel = viewModel,
                    onClipSelected = { selectedTab = AppTab.STUDIO }
                )
                AppTab.PROJECTS -> ProjectsListScreen(
                    viewModel = viewModel,
                    onOpenProject = { selectedTab = AppTab.STUDIO }
                )
                AppTab.SOUNDBOARD -> SoundEffectsScreen(
                    viewModel = viewModel
                )
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToUpdateCenter = { selectedTab = AppTab.UPDATE_CENTER }
                )
                AppTab.HELP_GUIDE -> AppGuideAndPdfScreen(
                    onNavigateToTab = { targetTab -> selectedTab = targetTab }
                )
                AppTab.UPDATE_CENTER -> AppUpdateCenterScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.SETTINGS }
                )
            }

            // More Tools Quick Bottom Sheet Modal
            if (showMoreToolsSheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showMoreToolsSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "⚡ جميع أدوات الدبلجة والصوت",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(14.dp))

                        val secondaryTabsList = listOf(
                            Triple(AppTab.RECORDING, "تسجيل مباشر بالميكروفون 🎙️", "كابينة تسجيل مخصصة مع شاشة موجات حية"),
                            Triple(AppTab.SYNC_STUDIO, "مزامنة الفيديو والصوت 🎚️", "شاشة مخصصة لمزامنة الفيديو مع موجة الصوت بدقة"),
                            Triple(AppTab.UPDATE_CENTER, "تحديثات المنتج والويب", "فحص التحديثات وبوابة المطور والمستخدم"),
                            Triple(AppTab.INSTANT_DUB, "دبلجة فورية AI", "دبلجة صوتية مباشرة وسريعة"),
                            Triple(AppTab.AI_DUB, "تحويل النص لصوت AI", "توليد أصوات واقعية من النصوص"),
                            Triple(AppTab.PROCESSING_PREVIEW, "معاينة ومعالجة الفيديو", "فلاتر وتعديل مسارات الصوت"),
                            Triple(AppTab.SOUNDBOARD, "المؤثرات الصوتية", "مؤثرات سبيستون وأصوات سينمائية"),
                            Triple(AppTab.CLIPS, "مكتبة المشاهد", "فيديوهات وعينات جاهزة للتجربة"),
                            Triple(AppTab.HELP_GUIDE, "دليل الاستخدام & PDF", "شرح تفصيلي وتصدير تقارير")
                        )

                        secondaryTabsList.chunked(2).forEach { rowTabs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowTabs.forEach { (tab, title, desc) ->
                                    Surface(
                                        onClick = {
                                            selectedTab = tab
                                            showMoreToolsSheet = false
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selectedTab == tab) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(84.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = tab.filledIcon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = title,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = desc,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
