/**
 * تطبيق فويس ماستر برو | VoiceMaster Pro
 * استوديو الدبلجة وهندسة الصوت بالذكاء الاصطناعي
 * 
 * المالك والمبتكر وصاحب كافة حقوق النشر والملكية الفكرية:
 * محمد رضا محمود محمود السيد سليمة
 * مصر - محافظة المنوفية - مركز شبين الكوم - شارع القفاص
 * جميع الحقوق محفوظة © 2026
 */
package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ui.components.OnboardingHelpDialog
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.DubbingViewModel
import com.example.ui.components.AlexaVoiceAssistantModal
import com.example.ui.components.PersistentAiChatOverlay
import com.example.ui.screens.AladLiveDubbingScreen
import com.example.ui.screens.AiDubbingScreen
import com.example.ui.screens.AppGuideAndPdfScreen
import com.example.ui.screens.HumanVoiceLibraryScreen
import com.example.ui.screens.InstantDubbingScreen
import com.example.ui.screens.ProjectsListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.VideoAutoDubberScreen
import com.example.ui.screens.VideoProcessingPreviewScreen
import com.example.ui.screens.VideoAudioSyncLayoutScreen
import com.example.ui.screens.AppUpdateCenterScreen
import com.example.ui.screens.AudioDubbingScreen
import com.example.ui.screens.DeveloperPortalScreen
import com.example.ui.screens.GeminiOneClickDubbingScreen
import com.example.ui.screens.SecurityDashboardScreen
import com.example.ui.screens.AiFeaturesSuiteScreen
import com.example.ui.screens.GoogleSignInGateScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.YouTubeAutoDubbingScreen
import com.example.ui.theme.MyApplicationTheme
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Security

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import com.example.security.AppShieldDefenseEngine
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

enum class AppTab(
    val titleArabic: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    STUDIO("استوديو الفيديو", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_studio"),
    ALAD_LIVE_DUB("دبلجة التطبيقات ALAD 🔴", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver, "nav_alad_live_dub"),
    AI_SUITE("استوديو الذكاء الاصطناعي 🚀", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_suite"),
    YOUTUBE_AUTO_DUB("دبلجة يوتيوب 🔴", Icons.Filled.PlayArrow, Icons.Filled.PlayArrow, "nav_youtube_auto_dub"),
    VOICE_LIBRARY("مكتبة الأصوات", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver, "nav_voice_library"),
    VIDEO_DUB("دبلجة فيديو AI", Icons.Filled.Videocam, Icons.Outlined.Videocam, "nav_video_dub"),
    SYNC_STUDIO("مزامنة دقيقة", Icons.Filled.Sync, Icons.Outlined.Sync, "nav_video_sync"),
    PROCESSING_PREVIEW("معاينة ومعالجة", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_processing_preview"),
    INSTANT_DUB("دبلجة فورية AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_instant_dub"),
    AI_DUB("نص لصوت AI", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "nav_ai_dub"),
    GEMINI_ONE_CLICK("دبلجة Gemini بضغطة ⚡", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_gemini_one_click"),
    AUDIO_DUB("دبلجة وهندسة الصوت 🎚️", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "nav_audio_dub"),
    ASSET_MANAGER("إدارة أصول الصوت و ExoPlayer 🎚️", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_asset_manager"),
    PROJECTS("مشاريعي", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_projects"),
    PROFILE("حسابي", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
    SETTINGS("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
    HELP_GUIDE("دليل & PDF", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_help_guide"),
    UPDATE_CENTER("مركز التحديثات والويب", Icons.Filled.SystemUpdate, Icons.Outlined.SystemUpdate, "nav_update_center"),
    DEVELOPER_PORTAL("بوابة المطور 🔐", Icons.Filled.Security, Icons.Outlined.Security, "nav_developer_portal"),
    SECURITY_DASHBOARD("لوحة الأمان 🛡️", Icons.Filled.Security, Icons.Outlined.Security, "nav_security_dashboard")
}

class MainActivity : FragmentActivity() {
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
    val context = LocalContext.current
    val securityStatus by AppShieldDefenseEngine.securityFlow.collectAsStateWithLifecycle()
    val remoteConfigUpdate by viewModel.updateManager.firebaseConfigManager.realtimeUpdateResult.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AppShieldDefenseEngine.performComprehensiveIntegrityCheck(context)
    }

    var selectedTab by remember { mutableStateOf(AppTab.VIDEO_DUB) }
    var showMoreToolsSheet by remember { mutableStateOf(false) }
    var showAlexaVoiceAssistant by remember { mutableStateOf(false) }

    val userProfile by viewModel.authService.userProfile.collectAsStateWithLifecycle()
    var hasPassedGate by remember { mutableStateOf(userProfile.isSignedIn) }

    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    var showFirstTimeOnboardingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userSettings.hasSeenOnboarding, hasPassedGate, userProfile.isSignedIn) {
        if ((hasPassedGate || userProfile.isSignedIn) && !userSettings.hasSeenOnboarding) {
            showFirstTimeOnboardingDialog = true
        }
    }

    // Gate Screen: Require authentication with verification before entering
    if (!hasPassedGate && !userProfile.isSignedIn) {
        GoogleSignInGateScreen(
            authService = viewModel.authService,
            onSignInSuccess = {
                hasPassedGate = true
            }
        )
        return
    }

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
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    showAlexaVoiceAssistant = true
                    viewModel.ttsManager.speakText("أنا أستمع إليك الآن، تفضل بالأمر الصوتي.", utteranceId = "alexa_wake_prompt")
                },
                shape = CircleShape,
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF0D47A1),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .padding(bottom = 54.dp)
                    .testTag("floating_alexa_assistant_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "المساعد الصوتي الذكي أليكسا وجيمناي",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
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
                                onClick = {
                                    selectedTab = tab
                                    viewModel.ttsManager.speakText("تبويب ${tab.titleArabic}", utteranceId = "nav_tab_speak")
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = "تبويب ${tab.titleArabic}${if (isSelected) "، محدد حالياً" else ""}"
                                    }
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
                                        fontSize = 10.sp,
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
                            onClick = {
                                showMoreToolsSheet = true
                                viewModel.ttsManager.speakText("فتح المزيد من الأدوات والاستوديوهات", utteranceId = "nav_more_speak")
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSecondaryTabSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "زر فتح قائمة المزيد من الأدوات والاستوديوهات"
                                }
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
                                    fontSize = 10.sp,
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
                    onNavigateToLibrary = { selectedTab = AppTab.PROJECTS },
                    onNavigateToProcessingPreview = { selectedTab = AppTab.PROCESSING_PREVIEW },
                    onNavigateToSync = { selectedTab = AppTab.SYNC_STUDIO },
                    onNavigateToGuide = { selectedTab = AppTab.HELP_GUIDE }
                )
                AppTab.ALAD_LIVE_DUB -> AladLiveDubbingScreen(
                    viewModel = viewModel
                )
                AppTab.AI_SUITE -> AiFeaturesSuiteScreen(
                    dubbingViewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.VOICE_LIBRARY -> HumanVoiceLibraryScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToTts = { selectedTab = AppTab.AI_DUB }
                )
                AppTab.VIDEO_DUB -> VideoAutoDubberScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToYouTubeDub = { selectedTab = AppTab.YOUTUBE_AUTO_DUB }
                )
                AppTab.YOUTUBE_AUTO_DUB -> YouTubeAutoDubbingScreen(
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
                    onNavigateToLibrary = { selectedTab = AppTab.PROJECTS }
                )
                AppTab.INSTANT_DUB -> InstantDubbingScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.AI_DUB -> AiDubbingScreen(
                    viewModel = viewModel
                )
                AppTab.GEMINI_ONE_CLICK -> GeminiOneClickDubbingScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.VIDEO_DUB },
                    onSendToStudio = { selectedTab = AppTab.STUDIO },
                    onNavigateToSecurity = { selectedTab = AppTab.SECURITY_DASHBOARD }
                )
                AppTab.AUDIO_DUB -> AudioDubbingScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { selectedTab = AppTab.STUDIO }
                )
                AppTab.ASSET_MANAGER -> com.example.audio.assets.presentation.AudioAssetsManagerScreen(
                    onNavigateBack = { selectedTab = AppTab.STUDIO }
                )
                AppTab.PROJECTS -> ProjectsListScreen(
                    viewModel = viewModel,
                    onOpenProject = { selectedTab = AppTab.STUDIO }
                )
                AppTab.PROFILE -> UserProfileScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.SETTINGS },
                    onSignOut = {
                        viewModel.authService.signOut()
                        hasPassedGate = false
                    }
                )
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToUpdateCenter = { selectedTab = AppTab.UPDATE_CENTER },
                    onNavigateToDeveloperPortal = { selectedTab = AppTab.SECURITY_DASHBOARD },
                    onNavigateToSecurityDashboard = { selectedTab = AppTab.SECURITY_DASHBOARD },
                    onNavigateToProfile = { selectedTab = AppTab.PROFILE },
                    onSignOut = {
                        viewModel.authService.signOut()
                        hasPassedGate = false
                    }
                )
                AppTab.HELP_GUIDE -> AppGuideAndPdfScreen(
                    onNavigateToTab = { targetTab -> selectedTab = targetTab }
                )
                AppTab.UPDATE_CENTER -> AppUpdateCenterScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.SETTINGS }
                )
                AppTab.DEVELOPER_PORTAL -> SecurityDashboardScreen(
                    viewModel = viewModel,
                    onBack = { selectedTab = AppTab.SETTINGS }
                )
                AppTab.SECURITY_DASHBOARD -> SecurityDashboardScreen(
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
                        Spacer(Modifier.height(10.dp))

                        // Interactive Onboarding & Help Walkthrough Banner
                        Surface(
                            onClick = {
                                showMoreToolsSheet = false
                                showFirstTimeOnboardingDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("open_onboarding_help_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Help,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "دليل الاستخدام والترحيب الشامل 💡",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "شرح تفاعلي لكيفية استخدام ميزات الدبلجة بالذكاء الاصطناعي للمستخدمين الجدد",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "فتح ↗",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        val secondaryTabsList = listOf(
                            Triple(AppTab.ASSET_MANAGER, "إدارة أصول الصوت و ExoPlayer 🎚️", "تشغيل وإدارة ملفات الصوت من assets مع TTS و STT أوفلاين بمعمارية Clean Architecture"),
                            Triple(AppTab.ALAD_LIVE_DUB, "دبلجة التطبيقات ALAD 🔴", "دبلجة مباشرة وفورية لأي تطبيق (YouTube, Netflix, Spotify) بـ 78 لغة مع زر عائم وتهدئة صوتية ذكية"),
                            Triple(AppTab.YOUTUBE_AUTO_DUB, "دبلجة يوتيوب الآلية 🔴", "تحميل ترجمات يوتيوب وترجمتها وتوليد أصوات متوافقة مع الزمن (youtube-auto-dubbing)"),
                            Triple(AppTab.PROFILE, "حسابي الشخصي (Profile) 👤", "بيانات الحساب، الأجهزة المتصلة، مجلد التخزين، وربط السوشيال ميديا"),
                            Triple(AppTab.AI_SUITE, "استوديو الذكاء الاصطناعي 🚀", "Gemini 3.5 & Pro • Veo 3 • Lyria • Live Voice • تفريغ الصوت • Firebase"),
                            Triple(AppTab.GEMINI_ONE_CLICK, "دبلجة Gemini بضغطة زر ⚡", "صوت ➔ نص ➔ ترجمة ➔ صوت واقعي بضغطة واحدة"),
                            Triple(AppTab.SECURITY_DASHBOARD, "رادار الأمان وبوابة التطوير 🛡️", "تتبع أجهزة Firebase Auth، فحص Gemini API، وأدوات المطور المدمجة"),
                            Triple(AppTab.AUDIO_DUB, "دبلجة الصوت بالذكاء الاصطناعي 🎚️", "استيراد ملفات الصوت ودبلجتها إلى اللهجات والأصوات المختلفة"),
                            Triple(AppTab.SYNC_STUDIO, "مزامنة الفيديو والصوت 🎚️", "شاشة مخصصة لمزامنة الفيديو مع موجة الصوت بدقة"),
                            Triple(AppTab.UPDATE_CENTER, "تحديثات المنتج والويب", "فحص التحديثات وبوابة المطور والمستخدم"),
                            Triple(AppTab.INSTANT_DUB, "دبلجة فورية AI", "دبلجة صوتية مباشرة وسريعة"),
                            Triple(AppTab.AI_DUB, "تحويل النص لصوت AI", "توليد أصوات واقعية من النصوص"),
                            Triple(AppTab.PROCESSING_PREVIEW, "معاينة ومعالجة الفيديو", "فلاتر وتعديل مسارات الصوت"),
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

            // Realtime Firebase Remote Config Update Dialog Prompt
            if (remoteConfigUpdate != null && remoteConfigUpdate!!.isUpdateAvailable) {
                val release = remoteConfigUpdate!!.latestRelease
                AlertDialog(
                    onDismissRequest = {
                        if (!remoteConfigUpdate!!.isCritical) {
                            viewModel.updateManager.firebaseConfigManager.dismissUpdatePrompt()
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = release?.releaseTitle ?: "تحديث جديد متوفر عبر السحابة 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الإصدار المتوفر:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("v${release?.versionName ?: "2.8.0"}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Text(
                                text = release?.releaseNotesArabic ?: "يتوفر تحديث جديد لتطبيق فويس ماستر برو يتضمن أحدث تحسينات الذكاء الاصطناعي والأمان.",
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val url = release?.downloadUrl?.ifBlank { "https://github.com/mahme98776/VoiceMasterPro/releases/latest" }
                                    ?: "https://github.com/mahme98776/VoiceMasterPro/releases/latest"
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(browserIntent)
                                } catch (_: Exception) {}
                                viewModel.updateManager.firebaseConfigManager.dismissUpdatePrompt()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تحديث الآن 🚀", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        if (!remoteConfigUpdate!!.isCritical) {
                            OutlinedButton(
                                onClick = { viewModel.updateManager.firebaseConfigManager.dismissUpdatePrompt() }
                            ) {
                                Text("تذكيري لاحقاً")
                            }
                        }
                    }
                )
            }

            // Interactive Onboarding & Help Walkthrough Dialog for New Users
            if (showFirstTimeOnboardingDialog) {
                OnboardingHelpDialog(
                    onDismiss = { showFirstTimeOnboardingDialog = false },
                    onComplete = { dontShowAgain ->
                        showFirstTimeOnboardingDialog = false
                        if (dontShowAgain) {
                            viewModel.viewModelScope.launch {
                                viewModel.userSettingsDataStore.updateHasSeenOnboarding(true)
                            }
                        }
                    }
                )
            }

            // Alexa & Gemini Smart Voice Assistant Modal with Media3 Audio Processing
            AlexaVoiceAssistantModal(
                isVisible = showAlexaVoiceAssistant,
                onDismiss = { showAlexaVoiceAssistant = false },
                viewModel = viewModel,
                onNavigateToTab = { targetTab -> selectedTab = targetTab },
                media3ProcessingLayer = viewModel.media3ProcessingLayer
            )
        }
    }
}
