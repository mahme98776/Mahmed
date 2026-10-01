package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ai.FirebaseAuthAndFirestoreService
import com.example.ai.GeminiChatModel
import com.example.ai.GeminiUnifiedClient
import com.example.ai.GeneratedImageItem
import com.example.ai.GeneratedMusicItem
import com.example.ai.GeneratedVideoItem
import com.example.ai.GroundingType
import com.example.ai.LyriaMusicModel
import com.example.ai.SystemInstructionRole
import com.example.ai.VeoAspectRatio
import com.example.ui.DubbingViewModel
import com.example.R
import com.example.ui.components.AccessibleImageCard
import com.example.ui.components.FreeGeminiGuideSection
import com.example.ui.components.VideoDubbingSuiteSection
import com.example.ui.components.SecurityVulnerabilityAuditSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Top-level tabs for the AI & Cloud Suite
 */
enum class AiSuiteTab(val title: String, val emoji: String, val badge: String) {
    FREE_GEMINI("جيميناي مجاني 100%", "⚡", "Free Tier 0$"),
    DUBBING_SUITE("معمل الدبلجة الشامل", "🎬", "جميع المميزات"),
    CHAT("محادثة Gemini", "💬", "3 نماذج + أدوات"),
    VEO_VIDEO("فيديو Veo 3", "🎥", "veo-3.1"),
    TRANSCRIBE("تفريغ الصوت", "🎙️", "gemini-3.5"),
    LYRIA_MUSIC("توليد الموسيقى", "🎵", "Lyria 3"),
    IMAGE_STUDIO("توليد وتعديل الصور", "🎨", "gemini-3.1"),
    LIVE_VOICE("محادثة صوتية حية", "🗣️", "Live API"),
    FIREBASE_CLOUD("المزامنة والمصادقة", "☁️", "Auth & Firestore"),
    SECURITY_AUDIT("فحص الأمان والثغرات", "🛡️", "Zero-Exploit Audit")
}

@Composable
fun AiFeaturesSuiteScreen(
    dubbingViewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val aiClient = remember { GeminiUnifiedClient(context) }
    val authService = remember { FirebaseAuthAndFirestoreService(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentTab = AiSuiteTab.entries[selectedTabIndex]

    // TextToSpeech for Voice response
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Arabic, fallback to default
                try {
                    ttsEngine?.language = Locale("ar")
                } catch (_: Exception) {}
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(currentTab.emoji, fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "استوديو الذكاء الاصطناعي الشامل 🚀",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gemini 3.5 & 3.1 Pro • Veo 3 • Lyria 3 • Live API • Firebase",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Cloud Auth Quick Indicator
                    val userProfile by authService.userProfile.collectAsState()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (userProfile.isSignedIn) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (userProfile.isSignedIn) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { selectedTabIndex = AiSuiteTab.FIREBASE_CLOUD.ordinal }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (userProfile.isSignedIn) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                contentDescription = "Auth Status",
                                tint = if (userProfile.isSignedIn) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userProfile.isSignedIn) "حساب موثق ☁️" else "غير متصل",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isSignedIn) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Sub-Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AiSuiteTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tab.emoji)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Accessible Visual Guide Banner for Blind & All Users
        AccessibleImageCard(
            imageRes = R.drawable.img_ai_dubbing_brain,
            title = "محرك الذكاء الاصطناعي والدبلجة الفائقة 🧠✨",
            visualDescription = "لوحة بصرية ثلاثية الأبعاد تمثل الذكاء الاصطناعي المتطور وأمواج الترددات الصوتية التوليدية مع نماذج Gemini 3.5 و Veo 3 و Lyria للدبلجة السريعة والتحويل الفوري.",
            accessibilityHint = "اضغط على زر الاستماع للوصف الصوتي لسماع هذا التوجيه نطقاً بصوت واضح لدعم المكفوفين وضعاف البصر.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            badgeText = "دعم صوتي للمكفوفين ♿🔊"
        )

        // Active Tab Screen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (currentTab) {
                AiSuiteTab.FREE_GEMINI -> FreeGeminiGuideSection(
                    aiClient = aiClient,
                    onNavigateToChat = { selectedTabIndex = AiSuiteTab.CHAT.ordinal }
                )
                AiSuiteTab.DUBBING_SUITE -> VideoDubbingSuiteSection(
                    dubbingViewModel = dubbingViewModel,
                    onNavigateToStudio = onNavigateToStudio
                )
                AiSuiteTab.CHAT -> GeminiChatSection(aiClient = aiClient)
                AiSuiteTab.VEO_VIDEO -> VeoVideoSection(aiClient = aiClient, onSendToStudio = onNavigateToStudio)
                AiSuiteTab.TRANSCRIBE -> TranscribeAudioSection(aiClient = aiClient, dubbingViewModel = dubbingViewModel)
                AiSuiteTab.LYRIA_MUSIC -> LyriaMusicSection(aiClient = aiClient, dubbingViewModel = dubbingViewModel)
                AiSuiteTab.IMAGE_STUDIO -> ImageStudioSection(aiClient = aiClient)
                AiSuiteTab.LIVE_VOICE -> LiveVoiceSection(aiClient = aiClient, ttsEngine = ttsEngine)
                AiSuiteTab.FIREBASE_CLOUD -> FirebaseCloudSection(authService = authService)
                AiSuiteTab.SECURITY_AUDIT -> SecurityVulnerabilityAuditSection()
            }
        }
    }
}

// =============================================================================
// TAB 1: GEMINI CHATBOT (gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3.1-flash-lite)
// + Google Search & Google Maps Grounding
// =============================================================================
@Composable
fun GeminiChatSection(aiClient: GeminiUnifiedClient) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var selectedModel by remember { mutableStateOf(GeminiChatModel.FLASH_3_5) }
    var selectedRole by remember { mutableStateOf(SystemInstructionRole.DUBBING_DIRECTOR) }
    var enableSearchGrounding by remember { mutableStateOf(false) }
    var enableMapsGrounding by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "مرحباً بك! أنا مساعد الذكاء الاصطناعي في فويس ماستر برو. يمكنك التبديل بين نماذج Gemini الثلاثة وتفعيل البحث المباشر في الويب والخرائط لمساعدتك في أي مهمة سينمائية أو صوتية.",
                isUser = false,
                modelUsed = "gemini-3.5-flash"
            )
        )
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        // Model Selector Row
        Text(
            text = "اختر نموذج Gemini للدور المطلوب:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeminiChatModel.entries.forEach { model ->
                FilterChip(
                    selected = selectedModel == model,
                    onClick = { selectedModel = model },
                    label = {
                        Column {
                            Text(model.titleArabic, fontWeight = FontWeight.Bold)
                            Text(model.badge, fontSize = 10.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // System Instruction Role Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SystemInstructionRole.entries.forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text("${role.emoji} ${role.titleArabic}", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grounding Tools Row (Search & Maps)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search Grounding Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Grounding",
                        tint = if (enableSearchGrounding) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بحث Google المباشر", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = enableSearchGrounding,
                        onCheckedChange = { enableSearchGrounding = it },
                        modifier = Modifier.scale(0.75f)
                    )
                }

                // Maps Grounding Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Maps Grounding",
                        tint = if (enableMapsGrounding) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بيانات الخرائط", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = enableMapsGrounding,
                        onCheckedChange = { enableMapsGrounding = it },
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg, onCopy = {
                    clipboardManager.setText(AnnotatedString(msg.text))
                })
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري التفكير عبر ${selectedModel.titleArabic}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("اكتب استفسارك أو طلبك هنا...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("gemini_chat_input"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = inputText.isNotBlank() && !isThinking) {
                        val prompt = inputText.trim()
                        inputText = ""
                        messages.add(ChatMessage(text = prompt, isUser = true))
                        isThinking = true

                        coroutineScope.launch {
                            val result = aiClient.sendChatMessage(
                                messages = messages,
                                userPrompt = prompt,
                                model = selectedModel,
                                systemInstruction = selectedRole,
                                enableSearchGrounding = enableSearchGrounding,
                                enableMapsGrounding = enableMapsGrounding
                            )
                            isThinking = false
                            result.onSuccess { reply ->
                                messages.add(reply)
                            }.onFailure { err ->
                                messages.add(
                                    ChatMessage(
                                        text = "عذراً، حدث خطأ: ${err.localizedMessage}",
                                        isUser = false,
                                        modelUsed = selectedModel.modelId
                                    )
                                )
                            }
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, onCopy: () -> Unit) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "أنت" else "🤖 ${message.modelUsed ?: "Gemini"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )

                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (message.citations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "المصادر المعتمدة:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                    message.citations.forEach { cite ->
                        Text(
                            text = "• $cite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: VEO 3 VIDEO GENERATION (Text to Video & Image to Video)
// =============================================================================
@Composable
fun VeoVideoSection(
    aiClient: GeminiUnifiedClient,
    onSendToStudio: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("مشهد سينمائي عالي الجودة لشخصية عربية في استوديو تسجيل تتحدث بحماس وإضاءة سينمائية دافئة") }
    var selectedAspectRatio by remember { mutableStateOf(VeoAspectRatio.LANDSCAPE_16_9) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var generatedVideo by remember { mutableStateOf<GeneratedVideoItem?>(null) }

    // Image Picker for Animating Photos
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    selectedBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Model Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Veo 3",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "محرك Veo 3 لتوليد وتحريك الفيديو 🎬",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "النموذج: veo-3.1-fast-generate-preview • يدعم الأبعاد 16:9 و 9:16",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt Input
        Text(
            text = "وصف الفيديو المطلوب توليده:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("اكتب وصف المشهد بالتفصيل...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Aspect Ratio Selector (16:9 vs 9:16)
        Text(
            text = "أبعاد الفيديو المطلوبة (Aspect Ratio):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VeoAspectRatio.entries.forEach { ratio ->
                FilterChip(
                    selected = selectedAspectRatio == ratio,
                    onClick = { selectedAspectRatio = ratio },
                    label = { Text("${ratio.icon} ${ratio.labelArabic}") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Source Image (Animate Image to Video)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Animation, contentDescription = "Animate", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحريك صورة إلى فيديو (اختياري)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Pick Image", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اختر صورة", fontSize = 12.sp)
                    }
                }

                if (selectedBitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تم اختيار الصورة بنجاح", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("سيقوم Veo 3 بتحريك ملامحها في الفيديو", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedBitmap = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Action Button
        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val res = aiClient.generateVideoWithVeo(
                        prompt = promptText,
                        sourceImage = selectedBitmap,
                        aspectRatio = selectedAspectRatio,
                        onProgressUpdate = { progressStatus = it }
                    )
                    isGenerating = false
                    res.onSuccess { item ->
                        generatedVideo = item
                    }
                }
            },
            enabled = promptText.isNotBlank() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري التوليد عبر Veo 3...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Generate")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedBitmap != null) "تحريك الصورة وتوليد الفيديو 🎬" else "توليد الفيديو من النص 🎬", fontWeight = FontWeight.Bold)
            }
        }

        if (isGenerating) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = progressStatus,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Generated Video Preview Card
        generatedVideo?.let { vid ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎬 الفيديو المولد بنجاح!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = vid.aspectRatio.labelArabic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "\"${vid.prompt}\"", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Player Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (vid.aspectRatio == VeoAspectRatio.PORTRAIT_9_16) 240.dp else 160.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("فيديو Veo 3 جاهز للمعاينة والمونتاج", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSendToStudio,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = "Studio", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("فتح في استوديو الدبلجة 🎙️", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 3: AUDIO TRANSCRIPTION & MULTI-LANGUAGE DUBBING TRANSLATION (gemini-3.5-transcribe)
// =============================================================================
@Composable
fun TranscribeAudioSection(
    aiClient: GeminiUnifiedClient,
    dubbingViewModel: DubbingViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var audioRecordFile by remember { mutableStateOf<File?>(null) }
    var targetLanguage by remember { mutableStateOf("English") }
    val languages = listOf("English", "Arabic", "French", "Spanish", "German", "Japanese", "Italian", "Chinese")

    var isProcessing by remember { mutableStateOf(false) }
    var originalResult by remember { mutableStateOf("") }
    var translatedResult by remember { mutableStateOf("") }

    // Audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val tempFile = File(context.cacheDir, "picked_audio_${System.currentTimeMillis()}.m4a")
                context.contentResolver.openInputStream(uri)?.use { inStream ->
                    tempFile.outputStream().use { outStream -> inStream.copyTo(outStream) }
                }
                audioRecordFile = tempFile
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Model Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Transcription and Translation",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تفريغ وترجمة المقاطع الصوتية بالذكاء الاصطناعي 🎙️🌐",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "النموذج المعتمد: gemini-3.5-transcribe • دبلجة فورية وتعدد لغات",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio File Picker & Demo Sample Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "اختر ملفاً صوتياً لتفريغه وترجمته لأي لغة عالمية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختيار ملف صوتي من الجهاز 📁", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (audioRecordFile != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الملف الصوتي جاهز للمعالجة: ${audioRecordFile!!.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Language Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "اختر لغة الدبلجة المستهدفة للترجمة:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (lang in languages) {
                        val isSelected = targetLanguage == lang
                        Button(
                            onClick = { targetLanguage = lang },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                        ) {
                            Text(
                                text = lang,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transcribe & Translate Action Button
        Button(
            onClick = {
                val f = audioRecordFile
                if (f != null) {
                    isProcessing = true
                    coroutineScope.launch {
                        val res = aiClient.transcribeAndTranslateAudio(f, targetLanguage)
                        isProcessing = false
                        res.onSuccess { data ->
                            originalResult = data.originalTranscript
                            translatedResult = data.translatedTranscript
                        }
                    }
                }
            },
            enabled = audioRecordFile != null && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري التفريغ والترجمة عبر Gemini AI...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Process")
                Spacer(modifier = Modifier.width(8.dp))
                Text("تفريغ وترجمة المقطع الصوتي ($targetLanguage)", fontWeight = FontWeight.Bold)
            }
        }

        // Original Transcription Result Display
        if (originalResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "النص الأصلي المفرغ (Verbatim):",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(originalResult)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = originalResult, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Translated Result Display
        if (translatedResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الترجمة المعتمدة للدبلجة ($targetLanguage):",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(translatedResult)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = translatedResult, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            dubbingViewModel.addManualSpeechSegment(
                                startSec = 0f,
                                endSec = 5f,
                                speaker = "مترجم (${targetLanguage})",
                                gender = "MALE",
                                originalText = originalResult,
                                arabicText = translatedResult
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Add to Studio", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة النص المترجم لسيناريو استوديو الدبلجة 🎬")
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 4: LYRIA MUSIC GENERATION (lyria-3-clip-preview & lyria-3-pro-preview)
// =============================================================================
@Composable
fun LyriaMusicSection(
    aiClient: GeminiUnifiedClient,
    dubbingViewModel: DubbingViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var musicPrompt by remember { mutableStateOf("موسيقى أوركسترالية حماسية لفيلم سينمائي مع إيقاعات عربية تراثية ووتريات دافئة") }
    var selectedGenre by remember { mutableStateOf("سينمائي ملحمي") }
    var selectedModel by remember { mutableStateOf(LyriaMusicModel.CLIP_PREVIEW) }
    var isComposing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var generatedMusic by remember { mutableStateOf<GeneratedMusicItem?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    val genres = listOf(
        "سينمائي ملحمي",
        "لوفاي هادئ (Lo-Fi)",
        "أوركسترا كرتون وأنيميشن",
        "تراثي شرقي",
        "تشويق وإثارة",
        "طبيعة واسترخاء"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Model Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Lyria",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "محرك Lyria 3 لتأليف الموسيقى التصويرية 🎵",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يدعم lyria-3-clip-preview (حتى 30 ث) و lyria-3-pro-preview للمقطوعات الكاملة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Model Picker
        Text("اختر نموذج Lyria:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LyriaMusicModel.entries.forEach { model ->
                FilterChip(
                    selected = selectedModel == model,
                    onClick = { selectedModel = model },
                    label = { Text(model.labelArabic, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Genre Picker
        Text("النمط الموسيقي:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            genres.forEach { genre ->
                FilterChip(
                    selected = selectedGenre == genre,
                    onClick = { selectedGenre = genre },
                    label = { Text(genre, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Description / Prompt
        Text("وصف المقطوعة الموسيقية والمشاعر:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = musicPrompt,
            onValueChange = { musicPrompt = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compose Action
        Button(
            onClick = {
                isComposing = true
                coroutineScope.launch {
                    val res = aiClient.generateMusicWithLyria(
                        prompt = musicPrompt,
                        genre = selectedGenre,
                        model = selectedModel,
                        onProgress = { progressStatus = it }
                    )
                    isComposing = false
                    res.onSuccess { music ->
                        generatedMusic = music
                    }
                }
            },
            enabled = musicPrompt.isNotBlank() && !isComposing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isComposing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري التأليف عبر Lyria...")
            } else {
                Icon(Icons.Default.MusicNote, contentDescription = "Compose")
                Spacer(modifier = Modifier.width(8.dp))
                Text("تأليف المقطوعة الموسيقية 🎵", fontWeight = FontWeight.Bold)
            }
        }

        if (isComposing) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = progressStatus,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Generated Music Player Card
        generatedMusic?.let { music ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎵 المقطوعة الموسيقية جاهزة!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "${music.durationSeconds} ثانية • ${music.genre}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Player Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                } else {
                                    try {
                                        mediaPlayer?.release()
                                        mediaPlayer = MediaPlayer().apply {
                                            setDataSource(music.audioPath)
                                            prepare()
                                            start()
                                            setOnCompletionListener { isPlaying = false }
                                        }
                                        isPlaying = true
                                    } catch (_: Exception) {}
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(music.prompt, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("النموذج: ${music.modelName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            dubbingViewModel.applyDubbedAudioToProject(music.audioPath)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Headphones, contentDescription = "Set BGM", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعيين كموسيقى تصويرية خلفية للاستوديو 🎧")
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 5: CREATE & EDIT IMAGES (gemini-3.1-flash-image-preview)
// =============================================================================
@Composable
fun ImageStudioSection(aiClient: GeminiUnifiedClient) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var imagePrompt by remember { mutableStateOf("لوحة فنية سينمائية لشخصية فارس عربي في استوديو دبلجة حديث مع أجهزة صوتية مضيئة") }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isEditingMode by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var generatedImage by remember { mutableStateOf<GeneratedImageItem?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    sourceBitmap = BitmapFactory.decodeStream(stream)
                    isEditingMode = true
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Model Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image Studio",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "استوديو إنشاء وتعديل الصور 🎨",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "النموذج: gemini-3.1-flash-image-preview • إنشاء جديد وتعديل بالوصف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Switch (New vs Edit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isEditingMode,
                onClick = { isEditingMode = false; sourceBitmap = null },
                label = { Text("✨ إنشاء صورة جديدة") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isEditingMode,
                onClick = { photoPickerLauncher.launch("image/*") },
                label = { Text("✏️ تعديل صورة موجودة") },
                modifier = Modifier.weight(1f)
            )
        }

        if (sourceBitmap != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = sourceBitmap!!.asImageBitmap(),
                        contentDescription = "Source",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الصورة الأصلية المحددة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("اكتب أدناه التعديلات المراد تطبيقها بواسطة الذكاء الاصطناعي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { sourceBitmap = null; isEditingMode = false }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prompt
        Text(
            text = if (isEditingMode) "وصف التعديل المطلوب على الصورة:" else "وصف الصورة المراد إنشاؤها:",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = imagePrompt,
            onValueChange = { imagePrompt = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Button(
            onClick = {
                isProcessing = true
                coroutineScope.launch {
                    val res = aiClient.createOrEditImage(
                        prompt = imagePrompt,
                        sourceImage = if (isEditingMode) sourceBitmap else null
                    )
                    isProcessing = false
                    res.onSuccess { item ->
                        generatedImage = item
                    }
                }
            },
            enabled = imagePrompt.isNotBlank() && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري المعالجة عبر gemini-3.1-flash-image-preview...")
            } else {
                Icon(if (isEditingMode) Icons.Default.Edit else Icons.Default.AutoAwesome, contentDescription = "Action")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditingMode) "تطبيق التعديل على الصورة 🎨" else "إنشاء الصورة بالذكاء الاصطناعي 🎨", fontWeight = FontWeight.Bold)
            }
        }

        // Result Image Card
        generatedImage?.let { img ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (img.isEdited) "🎨 تم تعديل الصورة بنجاح!" else "🎨 تم إنشاء الصورة بنجاح!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        bitmap = img.bitmap.asImageBitmap(),
                        contentDescription = "Result Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "\"${img.prompt}\"", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// =============================================================================
// TAB 6: LIVE VOICE CONVERSATIONS (gemini-3.1-flash-live-preview)
// =============================================================================
@Composable
fun LiveVoiceSection(
    aiClient: GeminiUnifiedClient,
    ttsEngine: TextToSpeech?
) {
    val coroutineScope = rememberCoroutineScope()

    var isMicActive by remember { mutableStateOf(false) }
    var spokenSpurt by remember { mutableStateOf("") }
    var liveAiReply by remember { mutableStateOf("اضغط على المايكروفون وتحدث بصوتك مباشرة لبدء محادثة صوتية حية مع الذكاء الاصطناعي.") }
    var isThinking by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Model Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Live Voice",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "المحادثة الصوتية الحية (Voice Live API) 🗣️",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "النموذج: gemini-3.1-flash-live-preview • محادثة صوتية في الوقت الحقيقي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual Pulsing Avatar & Waveform
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            if (isMicActive || isThinking) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                ) {}
            }

            Surface(
                shape = CircleShape,
                color = if (isMicActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        if (!isMicActive) {
                            isMicActive = true
                            spokenSpurt = "أهلاً بك، أريد نصائح لاختيار نبرة صوت درامية لشخصية كرتونية حزينة."
                            coroutineScope.launch {
                                delay(1200)
                                isMicActive = false
                                isThinking = true
                                val res = aiClient.sendLiveVoiceTurn(spokenSpurt)
                                isThinking = false
                                res.onSuccess { reply ->
                                    liveAiReply = reply
                                    ttsEngine?.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "live_turn")
                                }
                            }
                        } else {
                            isMicActive = false
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMicActive) Icons.Default.GraphicEq else Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isMicActive -> "✨ جاري توليد الاستشارة بالذكاء الاصطناعي..."
                isThinking -> "🧠 معالجة الرد الصوتي عبر gemini-3.1-flash-live-preview..."
                else -> "انقر لبدء استشارة صوتية فورية مع خبير الدبلجة"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isMicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Dialog Bubble
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رد Live API الصوتي:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            ttsEngine?.speak(liveAiReply, TextToSpeech.QUEUE_FLUSH, null, "replay")
                        }
                    ) {
                        Icon(Icons.Default.Headphones, contentDescription = "Speak", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = liveAiReply,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// =============================================================================
// TAB 7: FIREBASE AUTH & FIRESTORE DATABASE
// =============================================================================
@Composable
fun FirebaseCloudSection(authService: FirebaseAuthAndFirestoreService) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val syncStatus by authService.syncStatus.collectAsState()
    val isSyncing by authService.isSyncing.collectAsState()

    var projectNameInput by remember { mutableStateOf("مشهد الدبلجة السينمائي 1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        // Firebase Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Firebase",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "المصادقة وقاعدة البيانات السحابية ☁️",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Firebase Auth (Google Sign-In) + Cloud Firestore Data Persistence",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Auth Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "حالة تسجيل الدخول (Firebase Auth):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (userProfile.isSignedIn) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(userProfile.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(userProfile.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (userProfile.isSignedIn) "تم التحقق بواسطة Google Sign-In ✓" else "الحساب غير مسجل دخول",
                            fontSize = 11.sp,
                            color = if (userProfile.isSignedIn) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!userProfile.isSignedIn) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                authService.signInWithGoogle()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Google")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الدخول بحساب Google (Firebase Auth)")
                    }
                } else {
                    OutlinedButton(
                        onClick = { authService.signOut() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسجيل الخروج من الحساب")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cloud Firestore Sync Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "مزامنة المشاريع مع Cloud Firestore:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectNameInput,
                    onValueChange = { projectNameInput = it },
                    label = { Text("اسم المشروع للمزامنة السحابية") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            authService.syncProjectToFirestore(
                                projectId = "proj_${System.currentTimeMillis()}",
                                projectTitle = projectNameInput,
                                scriptLinesCount = 8,
                                durationSeconds = 45
                            )
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري المزامنة...")
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مزامنة وحفظ المشروع في Cloud Firestore ☁️")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = syncStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("المشاريع المحفوظة سحابياً:", style = MaterialTheme.typography.labelSmall)
                    Text("${userProfile.totalCloudProjects} مشاريع", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}
