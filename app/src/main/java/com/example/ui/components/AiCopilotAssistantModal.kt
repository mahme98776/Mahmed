package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AppTab
import com.example.audio.AudioEffectsLibrary
import com.example.audio.BgmStyle
import com.example.audio.VoiceEffect
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Message model for the Arabic In-App AI Copilot.
 */
data class CopilotChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val textArabic: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggestion: CopilotAction? = null
)

/**
 * Executable in-app actions suggested or triggered by the Copilot.
 */
data class CopilotAction(
    val titleArabic: String,
    val iconEmoji: String = "⚡",
    val execute: () -> Unit
)

/**
 * Preset quick prompts in Arabic for the Copilot.
 */
data class QuickPrompt(
    val title: String,
    val promptText: String,
    val emoji: String
)

val defaultQuickPrompts = listOf(
    QuickPrompt("🎙️ كيف أبدأ الدبلجة؟", "كيف أبدأ بتسجيل صوتي ودبلجة المشهد خطوة بخطوة؟", "🎙️"),
    QuickPrompt("✂️ قص وحذف الصمت", "كيف أستخدم أداة قص الصوت والتنقية الذكية؟", "✂️"),
    QuickPrompt("🎛️ تفعيل صوت روبوت", "فعل لي مؤثر صوت الروبوت الآلي الآن", "🤖"),
    QuickPrompt("🎬 المعاينة والمقارنة", "انقلني إلى مركز المعالجة والمعاينة المزدوجة", "🎬"),
    QuickPrompt("🔄 تراجع عن التعديل", "تراجع عن آخر تعديل قمت به", "↩️"),
    QuickPrompt("📖 فتح الدليل وPDF", "افتح لي دليل الاستخدام وكتيب الـ PDF", "📖")
)

/**
 * Floating AI Copilot Assistant & Action Executor
 */
@Composable
fun AiCopilotAssistantModal(
    isVisible: Boolean = true,
    onDismiss: () -> Unit,
    viewModel: DubbingViewModel,
    onNavigateToTab: (AppTab) -> Unit,
    onOpenProcessingCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            CopilotChatMessage(
                textArabic = "مرحباً بك! 👋 أنا مساعدك الذكي داخل الاستوديو (AI Copilot). أستطيع تنفيذ أي أمر تريده في التطبيق، التحكم بالمسارات، تطبيق المؤثرات، التراجع، وتقديم النصائح الصوتية باللغة العربية. كيف أساعدك الآن؟ ✨",
                isUser = false
            )
        )
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun processUserPrompt(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        messages.add(CopilotChatMessage(textArabic = trimmed, isUser = true))
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            delay(500) // Realistic interactive response feeling
            val lower = trimmed.lowercase()

            val response: CopilotChatMessage = when {
                // 1. Undo / Redo Commands
                lower.contains("تراجع") || lower.contains("undo") -> {
                    if (viewModel.canUndo()) {
                        val undone = viewModel.undo()
                        CopilotChatMessage(
                            textArabic = "تم التراجع عن: ${undone?.actionDescription ?: "آخر تعديل"} بنجاح! ↩️",
                            isUser = false
                        )
                    } else {
                        CopilotChatMessage(
                            textArabic = "لا توجد تعديلات سابقة للتراجع عنها حالياً. يمكنك إجراء تعديل على المؤثرات أو الصوت أولاً! 💡",
                            isUser = false
                        )
                    }
                }
                lower.contains("إعادة") || lower.contains("تقدم") || lower.contains("redo") -> {
                    if (viewModel.canRedo()) {
                        val redone = viewModel.redo()
                        CopilotChatMessage(
                            textArabic = "تمت إعادة تطبيق: ${redone?.actionDescription ?: "التعديل"} بنجاح! 🔁",
                            isUser = false
                        )
                    } else {
                        CopilotChatMessage(
                            textArabic = "لا يوجد تعديل محفوظ في سجل الإعادة (Redo). 💡",
                            isUser = false
                        )
                    }
                }

                // 2. Trimming & Audio Cutting
                lower.contains("قص") || lower.contains("trim") || lower.contains("صمت") || lower.contains("تقطيع") -> {
                    viewModel.openAudioTrimmerForCurrentTake()
                    CopilotChatMessage(
                        textArabic = "لقد فتحت لك نافذة 'أداة قص وتحرير الصوت' ✂️. يمكنك ضبط مقابض البداية والنهاية، أو الضغط على زر 'حذف الصمت' للتنقية التلقائية!",
                        isUser = false,
                        actionSuggestion = CopilotAction("فتح نافذة القص ✂️") {
                            viewModel.openAudioTrimmerForCurrentTake()
                        }
                    )
                }

                // 3. Applying Voice Effects directly
                lower.contains("روبوت") || lower.contains("robot") -> {
                    viewModel.selectVoicePreset(VoicePresetType.ROBOT)
                    CopilotChatMessage(
                        textArabic = "تم تطبيق مؤثر 'الروبوت الآلي 🤖' بنجاح على مشروعك الحالي!",
                        isUser = false,
                        actionSuggestion = CopilotAction("تجربة وسماع الصوت 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.ROBOT)
                        }
                    )
                }
                lower.contains("صدى") || lower.contains("echo") || lower.contains("reverb") -> {
                    viewModel.selectVoicePreset(VoicePresetType.ECHO)
                    CopilotChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الصدى والاستوديو 🎙️' لإعطاء عمق وارتداد صوتي فخم!",
                        isUser = false,
                        actionSuggestion = CopilotAction("سماع عينة الصدى 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.ECHO)
                        }
                    )
                }
                lower.contains("كرتون") || lower.contains("سنجاب") || lower.contains("أنمي") || lower.contains("chipmunk") -> {
                    viewModel.selectVoicePreset(VoicePresetType.CHIPMUNK)
                    CopilotChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الصوت الكرتوني والمرح 🐿️' بنجاح!",
                        isUser = false,
                        actionSuggestion = CopilotAction("سماع الصوت الكرتوني 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.CHIPMUNK)
                        }
                    )
                }
                lower.contains("سينمائي") || lower.contains("جهوري") || lower.contains("فخم") || lower.contains("deep") -> {
                    viewModel.selectVoicePreset(VoicePresetType.DEEP_VOICE)
                    CopilotChatMessage(
                        textArabic = "تم تفعيل مؤثر 'الطبقة الجهورية السينمائية 🎬' لنبرة عميقة وقوية للأفلام الوثائقية!",
                        isUser = false,
                        actionSuggestion = CopilotAction("سماع الصوت السينمائي 🔊") {
                            viewModel.togglePresetPreview(VoicePresetType.DEEP_VOICE)
                        }
                    )
                }
                lower.contains("مذياع") || lower.contains("راديو") || lower.contains("radio") -> {
                    viewModel.selectVoicePreset(VoicePresetType.VINTAGE_RADIO)
                    CopilotChatMessage(
                        textArabic = "تم تفعيل مؤثر 'المذياع الكلاسيكي 📻' بنجاح!",
                        isUser = false
                    )
                }
                lower.contains("طبيعي") || lower.contains("عادي") || lower.contains("إلغاء المؤثر") -> {
                    viewModel.selectVoicePreset(VoicePresetType.NORMAL)
                    CopilotChatMessage(
                        textArabic = "تمت إعادة ضبط الصوت إلى النمط الطبيعي النقي بدون مؤثرات. ✨",
                        isUser = false
                    )
                }

                // 4. Navigation & Tool Activation
                lower.contains("معاينة") || lower.contains("معالجة") || lower.contains("مقارنة") || lower.contains("preview") -> {
                    CopilotChatMessage(
                        textArabic = "هنالك مركز مخصص للمعاينة والمعالجة قبل التصدير. يمكنك المقارنة جنباً إلى جنب (Side-by-Side) وضبط مخارج الحروف. سأنقلك إليه الآن!",
                        isUser = false,
                        actionSuggestion = CopilotAction("الانتقال لمركز المعاينة 🎬") {
                            onNavigateToTab(AppTab.PROCESSING_PREVIEW)
                            onDismiss()
                        }
                    )
                }
                lower.contains("دليل") || lower.contains("pdf") || lower.contains("كتيب") || lower.contains("مساعدة") -> {
                    CopilotChatMessage(
                        textArabic = "يمكنك تصفح الدليل الشامل أو توليد كتيب PDF رسمي من 3 صفحات وتنزيله ومشاركته عبر واتساب!",
                        isUser = false,
                        actionSuggestion = CopilotAction("فتح شاشة الدليل وPDF 📖") {
                            onNavigateToTab(AppTab.HELP_GUIDE)
                            onDismiss()
                        }
                    )
                }
                lower.contains("مؤثرات") || lower.contains("soundboard") || lower.contains("تصفيق") || lower.contains("ضحك") -> {
                    CopilotChatMessage(
                        textArabic = "تفضل بالانتقال إلى لوحة المؤثرات الصوتية الحية (Soundboard) لتشغيل أصوات التفاعل والضحك والتصفيق أثناء الدبلجة.",
                        isUser = false,
                        actionSuggestion = CopilotAction("فتح لوحة المؤثرات 🎛️") {
                            onNavigateToTab(AppTab.SOUNDBOARD)
                            onDismiss()
                        }
                    )
                }
                lower.contains("دبلجة فيديو") || lower.contains("استيراد فيديو") || lower.contains("فيديو") -> {
                    CopilotChatMessage(
                        textArabic = "تفضل بالانتقال إلى قسم دبلجة الفيديو لاستيراد أي مقطع فيديو من هاتفك وتحويل حواره تلقائياً.",
                        isUser = false,
                        actionSuggestion = CopilotAction("فتح دبلجة الفيديو 📹") {
                            onNavigateToTab(AppTab.VIDEO_DUB)
                            onDismiss()
                        }
                    )
                }
                lower.contains("نص لصوت") || lower.contains("tts") || lower.contains("كتابة") -> {
                    CopilotChatMessage(
                        textArabic = "يمكنك تحويل أي نص عربي مكتوب إلى أداء صوتي مدبلج بأصوات الذكاء الاصطناعي الفصيحة.",
                        isUser = false,
                        actionSuggestion = CopilotAction("فتح تحويل النص لصوت 🗣️") {
                            onNavigateToTab(AppTab.AI_DUB)
                            onDismiss()
                        }
                    )
                }

                // 5. General Advice & Step by Step Guide
                lower.contains("كيف") || lower.contains("خطوات") || lower.contains("شرح") -> {
                    CopilotChatMessage(
                        textArabic = """
                            إليك طريقة الدبلجة السريعة في 4 خطوات:
                            1️⃣ اضغط على زر الميكروفون الدائري 🎙️ لبدء التسجيل مع العداد التنازلي.
                            2️⃣ اقرأ الحوار الملون في شاشة التلقين النصي.
                            3️⃣ بعد الانتهاء، استخدم أداة القص ✂️ أو اختر مؤثراً صوتياً 🎛️.
                            4️⃣ عاين المقطع واضغط تصدير 🎬 لحفظه ومشاركته!
                        """.trimIndent(),
                        isUser = false,
                        actionSuggestion = CopilotAction("الانتقال للاستوديو 🎙️") {
                            onNavigateToTab(AppTab.STUDIO)
                            onDismiss()
                        }
                    )
                }

                // Default Fallback with intelligence
                else -> {
                    CopilotChatMessage(
                        textArabic = "أنا معك خطوة بخطوة! يمكنني تغيير نبرة الصوت، قص التسجيلات، التراجع عن التعديلات، أو التوجيه لأي شاشة في التطبيق. جرب أن تطلب مني: 'طبق صوت روبوت' أو 'تراجع عن التعديل' أو 'افتح المعاينة'. ✨",
                        isUser = false
                    )
                }
            }

            isThinking = false
            messages.add(response)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("ai_copilot_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191622)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFD0BCFF), Color(0xFF381E72))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD0BCFF).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "المساعد الذكي (AI Copilot)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF34D399).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "عربي 🟢",
                                        color = Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "تحكم شامل في التطبيق وتنفيذ الأوامر فورياً",
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFFCAC4D0)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Undo / Redo Quick Status Bar inside Copilot
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF231F2E),
                    border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "سجل الأوامر (Undo/Redo):",
                                color = Color(0xFFEADDFF),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Undo Chip Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (viewModel.canUndo()) Color(0xFF4F378B) else Color(0xFF2B2930),
                                modifier = Modifier.clickable(enabled = viewModel.canUndo()) {
                                    val undone = viewModel.undo()
                                    messages.add(
                                        CopilotChatMessage(
                                            textArabic = "تم التراجع عن: ${undone?.actionDescription ?: "التعديل"} ↩️",
                                            isUser = false
                                        )
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = "تراجع",
                                        tint = if (viewModel.canUndo()) Color.White else Color(0xFF79747E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "تراجع",
                                        color = if (viewModel.canUndo()) Color.White else Color(0xFF79747E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Redo Chip Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (viewModel.canRedo()) Color(0xFF4F378B) else Color(0xFF2B2930),
                                modifier = Modifier.clickable(enabled = viewModel.canRedo()) {
                                    val redone = viewModel.redo()
                                    messages.add(
                                        CopilotChatMessage(
                                            textArabic = "تمت إعادة: ${redone?.actionDescription ?: "التعديل"} 🔁",
                                            isUser = false
                                        )
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redo,
                                        contentDescription = "إعادة",
                                        tint = if (viewModel.canRedo()) Color.White else Color(0xFF79747E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "إعادة",
                                        color = if (viewModel.canRedo()) Color.White else Color(0xFF79747E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Chat Messages Scroll List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageBubble(message = msg)
                    }

                    if (isThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFFD0BCFF),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "جاري التفكير وتنفيذ الأمر... 💭",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Quick Prompt Suggestion Bubbles
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 70.dp)
                        .padding(vertical = 4.dp)
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            defaultQuickPrompts.take(3).forEach { qp ->
                                QuickPromptChip(
                                    quickPrompt = qp,
                                    onClick = { processUserPrompt(qp.promptText) }
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            defaultQuickPrompts.drop(3).forEach { qp ->
                                QuickPromptChip(
                                    quickPrompt = qp,
                                    onClick = { processUserPrompt(qp.promptText) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Input Text Field and Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text("اكتب أمرك أو سؤالك بالعربية هنا...", fontSize = 12.5.sp, color = Color(0xFF938F99))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_copilot_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF25232E),
                            unfocusedContainerColor = Color(0xFF25232E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F)
                        ),
                        maxLines = 2
                    )

                    Button(
                        onClick = { processUserPrompt(inputText) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("ai_copilot_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: CopilotChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF4F378B) else Color(0xFF2B2836)
            ),
            border = BorderStroke(
                1.dp,
                if (message.isUser) Color(0xFFD0BCFF).copy(alpha = 0.5f) else Color(0xFF49454F).copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.textArabic,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Optional Action Button inside bubble
                if (message.actionSuggestion != null) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = message.actionSuggestion.execute,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${message.actionSuggestion.iconEmoji} ${message.actionSuggestion.titleArabic}",
                            color = Color(0xFF003314),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPromptChip(
    quickPrompt: QuickPrompt,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF2D283B),
        border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.8f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = quickPrompt.title,
            color = Color(0xFFEADDFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
