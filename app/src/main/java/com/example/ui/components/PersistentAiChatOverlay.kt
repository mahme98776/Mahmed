package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.audio.ArabicPhoneticsEngine
import com.example.audio.VoiceEffect
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Overlay tabs for the Persistent AI Chat Overlay
 */
enum class AiOverlaySubTab(val titleArabic: String, val emoji: String) {
    CHAT("محادثة ذكية AI", "💬"),
    LANGUAGE_TOOLS("تشكيل ودعم لغوي", "✍️"),
    SHORTCUTS("اختصارات الميزات", "⚡")
}

/**
 * Quick action model for direct execution
 */
data class AiOverlayAction(
    val titleArabic: String,
    val iconEmoji: String = "⚡",
    val description: String = "",
    val execute: () -> Unit
)

/**
 * Message model for the persistent AI chat overlay
 */
data class OverlayChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val textArabic: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggestion: AiOverlayAction? = null
)

/**
 * Persistent AI Chat Overlay Component
 * Positioned right above the navigation bar to provide seamless, always-available
 * direct assistance, Arabic language support (Tashkeel / tone transformation),
 * and quick feature shortcuts.
 */
@Composable
fun PersistentAiChatOverlay(
    viewModel: DubbingViewModel,
    currentTab: AppTab,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Chat State
    var chatInputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val chatMessages = remember {
        mutableStateListOf(
            OverlayChatMessage(
                textArabic = "أهلاً بك! 👋 أنا مساعدك الذكي الدائم (AI Copilot). أقدم لك الدعم اللغوي العربي وتشكيل الحركات، وشرح الأدوات وتنفيذ أي ميزة بضغطة زر واحدة. كيف أساعدك الآن؟ ✨",
                isUser = false,
                actionSuggestion = AiOverlayAction("استكشاف الاختصارات السريعة ⚡") {
                    selectedSubTab = 2
                }
            )
        )
    }

    val chatListState = rememberLazyListState()

    // Language Tools State
    var rawArabicText by remember {
        mutableStateOf("مرحبا بكم في استوديو الدبلجة الذكي. نسعى دائما لتقديم افضل تجربة صوتية فصيحة ومعبرة.")
    }
    var processedTashkeelText by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("وثائقي فخم 📜") }
    var showCopiedToast by remember { mutableStateOf(false) }

    // Pulsing Animation for the Floating/Docked Badge
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Interactive AI Prompt Processor
    fun handleUserPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        chatMessages.add(OverlayChatMessage(textArabic = trimmed, isUser = true))
        chatInputText = ""
        isThinking = true

        coroutineScope.launch {
            delay(450)
            val lower = trimmed.lowercase()

            val response: OverlayChatMessage = when {
                // 1. Navigation & Screen shortcuts
                lower.contains("veo") || lower.contains("lyria") || lower.contains("شامل") || lower.contains("ذكاء") || lower.contains("suite") -> {
                    onNavigateToTab(AppTab.AI_SUITE)
                    OverlayChatMessage(
                        textArabic = "تم نقلك مباشرة إلى استوديو الذكاء الاصطناعي الشامل 🚀 (Gemini Chat • Veo 3 • Lyria 3 • Live API • تفريغ الصوت • Firebase).",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح استوديو الذكاء الاصطناعي 🚀") {
                            onNavigateToTab(AppTab.AI_SUITE)
                        }
                    )
                }
                lower.contains("فيديو") || lower.contains("دبلجة فيديو") || lower.contains("auto dub") -> {
                    onNavigateToTab(AppTab.VIDEO_DUB)
                    OverlayChatMessage(
                        textArabic = "تم نقلك مباشرة إلى شاشة 'دبلجة الفيديو التلقائية بالذكاء الاصطناعي' 🎬. يمكنك الآن استيراد الفيديو وتفريغ الصوت وترجمته!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح دبلجة الفيديو 🎬") {
                            onNavigateToTab(AppTab.VIDEO_DUB)
                        }
                    )
                }
                lower.contains("استوديو") || lower.contains("تسجيل") || lower.contains("studio") -> {
                    onNavigateToTab(AppTab.STUDIO)
                    OverlayChatMessage(
                        textArabic = "أهلاً بك في استوديو التسجيل متعدد المسارات 🎙️. مسارات الصوت والفيديو بانتظارك.",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("الانتقال للاستوديو 🎙️") {
                            onNavigateToTab(AppTab.STUDIO)
                        }
                    )
                }
                lower.contains("نص") || lower.contains("tts") || lower.contains("تحويل نص") -> {
                    onNavigateToTab(AppTab.AI_DUB)
                    OverlayChatMessage(
                        textArabic = "تم التوجه إلى قسم 'تحويل النص إلى صوت واقعي (AI TTS)' 🗣️. اختر النبرة التعبيرية واضغط توليد الصوت.",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح تحويل النص 🗣️") {
                            onNavigateToTab(AppTab.AI_DUB)
                        }
                    )
                }
                lower.contains("فورية") || lower.contains("نبرة") || lower.contains("instant") -> {
                    onNavigateToTab(AppTab.INSTANT_DUB)
                    OverlayChatMessage(
                        textArabic = "تم فتح 'الدبلجة الفورية وتغيير النبرة الحية' ⚡. تحدث في الميكروفون وسيتعرف النظام على طبقة صوتك ويحولها فورياً!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح الدبلجة الفورية ⚡") {
                            onNavigateToTab(AppTab.INSTANT_DUB)
                        }
                    )
                }
                lower.contains("دليل") || lower.contains("pdf") || lower.contains("شرح") || lower.contains("help") -> {
                    onNavigateToTab(AppTab.HELP_GUIDE)
                    OverlayChatMessage(
                        textArabic = "تم فتح 'الدليل الإرشادي الشامل ومولد كتيب الـ PDF' 📖. تجد هنا جميع الخطوات التفصيلية.",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح الدليل وPDF 📖") {
                            onNavigateToTab(AppTab.HELP_GUIDE)
                        }
                    )
                }

                // 2. Undo / Redo
                lower.contains("تراجع") || lower.contains("undo") -> {
                    if (viewModel.canUndo()) {
                        val undone = viewModel.undo()
                        OverlayChatMessage(
                            textArabic = "تم التراجع عن: ${undone?.actionDescription ?: "آخر تعديل"} بنجاح! ↩️",
                            isUser = false
                        )
                    } else {
                        OverlayChatMessage(
                            textArabic = "لا توجد تعديلات سابقة في سجل التراجع حالياً. 💡",
                            isUser = false
                        )
                    }
                }
                lower.contains("إعادة") || lower.contains("redo") -> {
                    if (viewModel.canRedo()) {
                        val redone = viewModel.redo()
                        OverlayChatMessage(
                            textArabic = "تمت إعادة تطبيق: ${redone?.actionDescription ?: "التعديل"} بنجاح! 🔁",
                            isUser = false
                        )
                    } else {
                        OverlayChatMessage(
                            textArabic = "سجل الإعادة فارغ حالياً. 💡",
                            isUser = false
                        )
                    }
                }

                // 3. Audio Effects Trigger
                lower.contains("روبوت") || lower.contains("robot") -> {
                    viewModel.selectVoicePreset(VoicePresetType.ROBOT)
                    OverlayChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الروبوت الآلي 🤖' بنجاح على مشروعك!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("تجربة صوت الروبوت 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.ROBOT)
                        }
                    )
                }
                lower.contains("صدى") || lower.contains("echo") -> {
                    viewModel.selectVoicePreset(VoicePresetType.ECHO)
                    OverlayChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الصدى والاستوديو 🎙️' لإعطاء عمق وارتداد فخم!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("سماع عينة الصدى 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.ECHO)
                        }
                    )
                }
                lower.contains("سينمائي") || lower.contains("جهوري") || lower.contains("deep") -> {
                    viewModel.selectVoicePreset(VoicePresetType.DEEP_VOICE)
                    OverlayChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الطبقة الجهورية السينمائية 🎬' لنبرة عميقة وقوية!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("سماع الصوت السينمائي 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.DEEP_VOICE)
                        }
                    )
                }
                lower.contains("سنجاب") || lower.contains("كرتون") || lower.contains("chipmunk") -> {
                    viewModel.selectVoicePreset(VoicePresetType.CHIPMUNK)
                    OverlayChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الصوت الكرتوني والمرح 🐿️' بنجاح!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("سماع الصوت الكرتوني 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.CHIPMUNK)
                        }
                    )
                }

                // 4. Trimming & Cutting
                lower.contains("قص") || lower.contains("trim") || lower.contains("صمت") -> {
                    onNavigateToTab(AppTab.STUDIO)
                    viewModel.openAudioTrimmerForCurrentTake()
                    OverlayChatMessage(
                        textArabic = "تم فتح 'أداة قص وتحرير الصوت' ✂️. يمكنك ضبط الحدود وحذف فترات الصمت التلقائي.",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("الانتقال للاستوديو والقص ✂️") {
                            onNavigateToTab(AppTab.STUDIO)
                            viewModel.openAudioTrimmerForCurrentTake()
                        }
                    )
                }

                // 5. Tashkeel / Language Request in Chat
                lower.contains("تشكيل") || lower.contains("حركات") || lower.contains("تنوين") -> {
                    selectedSubTab = 1
                    OverlayChatMessage(
                        textArabic = "انتقلت بك إلى تبويب 'التشكيل والدعم اللغوي' ✍️. يمكنك كتابة أي نص وسيقوم المحرك بضبط الحركات ومخارج الحروف فورياً!",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("فتح أداة التشكيل ✍️") {
                            selectedSubTab = 1
                        }
                    )
                }

                // Default Help Response
                else -> {
                    OverlayChatMessage(
                        textArabic = "أنا هنا لمساعدتك! يمكنك أن تطلب مني:\n• الانتقال لأي شاشة (استوديو، دبلجة فيديو، تحويل نص، دليل الاستخدام)\n• تشكيل وتصحيح النصوص العربية\n• تفعيل المؤثرات الصوتية (صدى، روبوت، سينمائي، كرتوني)\n• التراجع والإعادة، أو قص وحذف الصمت ✂️",
                        isUser = false,
                        actionSuggestion = AiOverlayAction("استعراض الاختصارات السريعة ⚡") {
                            selectedSubTab = 2
                        }
                    )
                }
            }

            chatMessages.add(response)
            isThinking = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_ai_chat_overlay_container")
    ) {
        // -------------------------------------------------------------
        // EXPANDED OVERLAY VIEW
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.68f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .shadow(16.dp),
                color = Color(0xFF191722),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Drag & Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF281C3F), Color(0xFF0F172A))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المساعد الذكي (AI Copilot)",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF34D399).copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "مباشر 🟢",
                                            color = Color(0xFF34D399),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "دعم لغوي عربي • اختصارات فورية • استشارات صوتية",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_ai_overlay_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "تصغير",
                                tint = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // Navigation Tabs inside Overlay (Chat / Language Tools / Shortcuts)
                    ScrollableTabRow(
                        selectedTabIndex = selectedSubTab,
                        containerColor = Color(0xFF221F2E),
                        contentColor = Color(0xFFD0BCFF),
                        edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                                color = Color(0xFF00E5FF),
                                height = 3.dp
                            )
                        },
                        divider = { HorizontalDivider(color = Color(0xFF3B364C)) }
                    ) {
                        AiOverlaySubTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedSubTab == index,
                                onClick = { selectedSubTab = index },
                                text = {
                                    Text(
                                        text = "${tab.emoji} ${tab.titleArabic}",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSubTab == index) Color(0xFF00E5FF) else Color(0xFFCAC4D0)
                                    )
                                },
                                modifier = Modifier.testTag("ai_subtab_$index")
                            )
                        }
                    }

                    // Content based on Selected SubTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        when (selectedSubTab) {
                            0 -> {
                                // -------------------------------------------------------------
                                // SUBTAB 0: LIVE CHAT & NLP
                                // -------------------------------------------------------------
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Chat Messages List
                                    LazyColumn(
                                        state = chatListState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(chatMessages, key = { it.id }) { msg ->
                                            OverlayMessageBubble(
                                                message = msg,
                                                onActionClick = {
                                                    msg.actionSuggestion?.execute?.invoke()
                                                }
                                            )
                                        }

                                        if (isThinking) {
                                            item {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(6.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = Color(0xFF00E5FF),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "المساعد الذكي يحلل طلبك... 💭",
                                                        color = Color(0xFFCAC4D0),
                                                        fontSize = 11.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // Quick Prompt Chips
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "🎙️ كيف أبدأ الدبلجة؟",
                                            "✨ تشكيل النص العربي",
                                            "🤖 تفعيل صوت الروبوت",
                                            "🎬 دبلجة فيديو تلقائية",
                                            "✂️ قص الصوت وحذف الصمت",
                                            "↩️ تراجع عن التعديل",
                                            "📖 فتح الدليل وPDF"
                                        ).forEach { chipText ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF292437),
                                                border = BorderStroke(1.dp, Color(0xFF4F378B)),
                                                modifier = Modifier.clickable { handleUserPrompt(chipText) }
                                            ) {
                                                Text(
                                                    text = chipText,
                                                    color = Color(0xFFEADDFF),
                                                    fontSize = 10.5.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    // Chat Input Box
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = chatInputText,
                                            onValueChange = { chatInputText = it },
                                            placeholder = {
                                                Text(
                                                    text = "اسأل أو اطلب تنفيذ أي أمر بالعربية...",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF938F99)
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("persistent_ai_chat_input"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF221F2E),
                                                unfocusedContainerColor = Color(0xFF221F2E),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFF00E5FF),
                                                unfocusedBorderColor = Color(0xFF49454F)
                                            ),
                                            maxLines = 2
                                        )

                                        Button(
                                            onClick = { handleUserPrompt(chatInputText) },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                            modifier = Modifier
                                                .height(50.dp)
                                                .testTag("persistent_ai_chat_send_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "إرسال",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // -------------------------------------------------------------
                                // SUBTAB 1: ARABIC LANGUAGE SUPPORT & TASHKEEL ENGINE
                                // -------------------------------------------------------------
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF211D2D)),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Subtitles,
                                                        contentDescription = null,
                                                        tint = Color(0xFF00E5FF),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "محرك التشكيل وضبط مخارج الحروف الفصيحة",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = "أدخل أي نص عربي لتحصل على تشكيل دقيق بالحركات التامة (فتحة، ضمة، كسرة، سكون، شدة) لضمان أداء دبلجة بشري طبيعي.",
                                                    color = Color(0xFFCAC4D0),
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )

                                                Spacer(Modifier.height(8.dp))

                                                OutlinedTextField(
                                                    value = rawArabicText,
                                                    onValueChange = { rawArabicText = it },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 70.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFF191722),
                                                        unfocusedContainerColor = Color(0xFF191722),
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedBorderColor = Color(0xFFD0BCFF),
                                                        unfocusedBorderColor = Color(0xFF49454F)
                                                    ),
                                                    placeholder = {
                                                        Text("اكتب أو الصق النص العربي هنا للتشكيل...", fontSize = 11.5.sp)
                                                    }
                                                )

                                                Spacer(Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            processedTashkeelText =
                                                                ArabicPhoneticsEngine.enrichArabicLetteringAndTashkeel(rawArabicText)
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AutoAwesome,
                                                            contentDescription = null,
                                                            tint = Color(0xFF00373E),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "تشكيل وضبط الحركات ✨",
                                                            color = Color(0xFF00373E),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                if (processedTashkeelText.isNotEmpty()) {
                                                    Spacer(Modifier.height(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color(0xFF182825),
                                                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Text(
                                                                text = "النص المشكول بدقة:",
                                                                color = Color(0xFF34D399),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Spacer(Modifier.height(4.dp))
                                                            Text(
                                                                text = processedTashkeelText,
                                                                color = Color(0xFFE8F5E9),
                                                                fontSize = 13.sp,
                                                                lineHeight = 20.sp
                                                            )
                                                            Spacer(Modifier.height(6.dp))
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                OutlinedButton(
                                                                    onClick = {
                                                                        clipboardManager.setText(AnnotatedString(processedTashkeelText))
                                                                        showCopiedToast = true
                                                                    },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399))
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.ContentCopy,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Spacer(Modifier.width(4.dp))
                                                                    Text("نسخ النص", fontSize = 10.5.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Acting & Voiceover Tones Guide
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF211D2D)),
                                            border = BorderStroke(1.dp, Color(0xFF4F378B))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Psychology,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD0BCFF),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "أنماط الإلقاء الصوتي العربي (Vocal Acting Tones)",
                                                        color = Color.White,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(Modifier.height(6.dp))

                                                val toneList = listOf(
                                                    "وثائقي فخم 📜" to "إلقاء رزين وهادئ مع وقفات تنفس مدروسة للأفلام الوثائقية والطبيعة.",
                                                    "درامي مشوق 🎭" to "تلوين صوتي عالي النبرات لنقل التوتر والمشاعر في المشاهد التمثيلية.",
                                                    "إعلاني حماسي ⚡" to "طاقة صوتية متدفقة ونبرة متفائلة واضحة لشد انتباه المستمع.",
                                                    "كرتوني مرح 🐿️" to "تغيير سريع في الطبقات واستخدام نبرات حادة للشخصيات الكوميدية."
                                                )

                                                toneList.forEach { (title, desc) ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFF292437),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 3.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Text(
                                                                text = title,
                                                                color = Color(0xFF00E5FF),
                                                                fontSize = 11.5.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = desc,
                                                                color = Color(0xFFCAC4D0),
                                                                fontSize = 10.5.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // -------------------------------------------------------------
                                // SUBTAB 2: INSTANT FEATURE SHORTCUTS HUB
                                // -------------------------------------------------------------
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "انتقل إلى أي ميزة أو نفّذ الأوامر الفورية بلمسة واحدة:",
                                            color = Color(0xFFCAC4D0),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Core App Navigation Shortcuts
                                    val screenShortcuts = listOf(
                                        Triple("🎙️ مكتبة الأصوات البشرية (4,800+)", "مكتبة ضخمة من الأصوات البشرية بشتى اللهجات", AppTab.VOICE_LIBRARY),
                                        Triple("🎬 دبلجة فيديو تلقائية AI", "تفريغ، ترجمة، ومزامنة الفيديو التلقائية", AppTab.VIDEO_DUB),
                                        Triple("🎙️ استوديو التسجيل والتحرير", "الاستوديو متعدد المسارات ومحرر الصوت", AppTab.STUDIO),
                                        Triple("🗣️ تحويل النص إلى صوت (TTS)", "توليد تعليق صوتي فصيح وطبيعي بالذكاء الاصطناعي", AppTab.AI_DUB),
                                        Triple("⚡ دبلجة فورية وتغيير النبرة", "التعرف الذكي الحي على طبقة الصوت والتحويل", AppTab.INSTANT_DUB),
                                        Triple("📊 معاينة ومعالجة متقدمة", "شاشة المقارنة المزدوجة وضبط التزامن", AppTab.PROCESSING_PREVIEW),
                                        Triple("📖 دليل الاستخدام وPDF", "كتيب الشرح الشامل وحفظ مستند الـ PDF", AppTab.HELP_GUIDE)
                                    )

                                    items(screenShortcuts) { (title, desc, targetTab) ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (currentTab == targetTab) Color(0xFF381E72) else Color(0xFF221F2E),
                                            border = BorderStroke(
                                                1.dp,
                                                if (currentTab == targetTab) Color(0xFF00E5FF) else Color(0xFF49454F).copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onNavigateToTab(targetTab)
                                                    isExpanded = false
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        color = Color.White,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = desc,
                                                        color = Color(0xFFCAC4D0),
                                                        fontSize = 10.5.sp
                                                    )
                                                }

                                                Text(
                                                    text = if (currentTab == targetTab) "أنت هنا 📍" else "انتقال ⬅️",
                                                    color = if (currentTab == targetTab) Color(0xFF00E5FF) else Color(0xFFD0BCFF),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Quick Sound Effect Triggers
                                    item {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "مؤثرات صوتية سريعة (Instant FX):",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    val voiceEffects = listOf(
                                        Triple("🤖 روبوت سايبر", VoicePresetType.ROBOT, Color(0xFF00E5FF)),
                                        Triple("🎙️ صدى استوديو", VoicePresetType.ECHO, Color(0xFF818CF8)),
                                        Triple("🎬 صوت سينمائي", VoicePresetType.DEEP_VOICE, Color(0xFFF59E0B)),
                                        Triple("🐿️ سنجاب كرتون", VoicePresetType.CHIPMUNK, Color(0xFFF43F5E))
                                    )

                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            voiceEffects.forEach { (name, preset, tint) ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0xFF2B2838),
                                                    border = BorderStroke(1.dp, tint.copy(alpha = 0.7f)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            viewModel.selectVoicePreset(preset)
                                                            viewModel.togglePresetPreview(preset)
                                                        }
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(text = name, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = "تطبيق وسماع 🔊", color = tint, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // DOCKED PERSISTENT CHAT BAR (ABOVE BOTTOM NAVIGATION)
        // -------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = Color(0xFF1E1B2A),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("persistent_ai_chat_bar")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Main Row of the Docked Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // AI Glowing Trigger Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(end = 6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E1B4B),
                            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                            modifier = Modifier
                                .size(36.dp)
                                .scale(if (isExpanded) 1f else pulseScale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "المساعد الذكي AI",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مساعد AI الذكي",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "✨ عربي",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "اسأل أو اطلب تنفيذ أي ميزة...",
                                color = Color(0xFFCAC4D0),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Direct Quick Action Icons (Language Tool / Shortcuts / Expand Toggle)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Language / Tashkeel Direct Icon
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF292437),
                            border = BorderStroke(1.dp, Color(0xFF4F378B)),
                            modifier = Modifier.clickable {
                                selectedSubTab = 1
                                isExpanded = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "✍️ تشكيل", color = Color(0xFFD0BCFF), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Shortcuts Direct Icon
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF292437),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                            modifier = Modifier.clickable {
                                selectedSubTab = 2
                                isExpanded = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚡ اختصارات", color = Color(0xFF00E5FF), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Expand / Collapse Chevron Button
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("toggle_ai_overlay_btn")
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isExpanded) "تصغير" else "توسيع المساعد",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                }

                // Horizontal Quick Feature Carousel when Docked
                if (!isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF241F32),
                            modifier = Modifier.clickable {
                                onNavigateToTab(AppTab.VIDEO_DUB)
                            }
                        ) {
                            Text(
                                text = "🎬 دبلجة فيديو AI",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF241F32),
                            modifier = Modifier.clickable {
                                onNavigateToTab(AppTab.STUDIO)
                            }
                        ) {
                            Text(
                                text = "🎙️ استوديو التسجيل",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF241F32),
                            modifier = Modifier.clickable {
                                onNavigateToTab(AppTab.AI_DUB)
                            }
                        ) {
                            Text(
                                text = "🗣️ تحويل نص لصوت",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF241F32),
                            modifier = Modifier.clickable {
                                onNavigateToTab(AppTab.INSTANT_DUB)
                            }
                        ) {
                            Text(
                                text = "⚡ دبلجة فورية",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF241F32),
                            modifier = Modifier.clickable {
                                onNavigateToTab(AppTab.HELP_GUIDE)
                            }
                        ) {
                            Text(
                                text = "📖 دليل & PDF",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        if (viewModel.canUndo()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4A3416),
                                modifier = Modifier.clickable { viewModel.undo() }
                            ) {
                                Text(
                                    text = "↩️ تراجع",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (viewModel.canRedo()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1B3D2F),
                                modifier = Modifier.clickable { viewModel.redo() }
                            ) {
                                Text(
                                    text = "🔁 إعادة",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Message Bubble in the Overlay Chat
 */
@Composable
private fun OverlayMessageBubble(
    message: OverlayChatMessage,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isUser) 14.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF4F378B) else Color(0xFF262232)
            ),
            border = BorderStroke(
                1.dp,
                if (message.isUser) Color(0xFFD0BCFF).copy(alpha = 0.5f) else Color(0xFF49454F).copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.textArabic,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                if (message.actionSuggestion != null) {
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${message.actionSuggestion.iconEmoji} ${message.actionSuggestion.titleArabic}",
                            color = Color(0xFF00373E),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
