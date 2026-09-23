package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.audio.gemini.DubbingStage
import com.example.audio.gemini.DubbingTargetLanguage
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiOneClickDubbingScreen(
    viewModel: DubbingViewModel,
    onBack: () -> Unit = {},
    onSendToStudio: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dubbingState by viewModel.geminiOneClickDubber.dubbingState.collectAsState()
    val apiDiagnostic by viewModel.geminiOneClickDubber.apiDiagnostic.collectAsState()
    val savedApiKey by viewModel.geminiApiKey.collectAsState()

    var customKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var showApiKeyConfig by remember { mutableStateOf(false) }
    var isCheckingApi by remember { mutableStateOf(false) }

    var isRecordingMic by remember { mutableStateOf(false) }
    var customScriptText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(DubbingTargetLanguage.ENGLISH) }
    var isLanguageDropdownOpen by remember { mutableStateOf(false) }
    var selectedVoiceGender by remember { mutableStateOf("HERO_MALE") }
    var isPlayingDubbedAudio by remember { mutableStateOf(false) }

    // Auto-verify API on first launch if not tested yet
    LaunchedEffect(Unit) {
        if (apiDiagnostic == null) {
            isCheckingApi = true
            viewModel.geminiOneClickDubber.testGeminiApiConnection()
            isCheckingApi = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "دبلجة Gemini بضغطة زر ⚡",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Audio -> Text -> Translate -> Speech",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            coroutineScope.launch {
                                isCheckingApi = true
                                viewModel.geminiOneClickDubber.testGeminiApiConnection(customKeyInput)
                                isCheckingApi = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "فحص الاتصال الفعلي بـ Gemini",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. LIVE GEMINI API DIAGNOSTIC & VERIFICATION CARD
            // =========================================================================
            item {
                val isApiSuccess = apiDiagnostic?.isSuccess == true
                val cardBorderColor by animateColorAsState(
                    targetValue = when {
                        isCheckingApi -> Color(0xFF38BDF8)
                        isApiSuccess -> Color(0xFF22C55E)
                        else -> Color(0xFFEF4444)
                    },
                    label = "apiBorderColor"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_diagnostic_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, cardBorderColor.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isApiSuccess) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCheckingApi) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        } else if (isApiSuccess) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isCheckingApi) "جاري فحص الاتصال الفعلي بـ Gemini..."
                                        else if (isApiSuccess) "اتصال Gemini API شغال ونشط 🟢"
                                        else "حالة Gemini API: بحاجة لفحص أو تحديث 🔴",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isApiSuccess) Color(0xFF22C55E) else Color(0xFFF87171)
                                    )
                                    Text(
                                        text = apiDiagnostic?.details?.ifBlank { "موديل: gemini-3.5-flash" }
                                            ?: "اضغط زر الفحص للتأكد من استجابة الخادم ومفتاح الـ API",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isCheckingApi = true
                                        viewModel.geminiOneClickDubber.testGeminiApiConnection(customKeyInput)
                                        isCheckingApi = false
                                    }
                                },
                                enabled = !isCheckingApi
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "إعادة الفحص الآن",
                                    tint = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showApiKeyConfig) "إخفاء إعدادات المفتاح 🔼" else "إدخال أو تعديل مفتاح Gemini API 🔑",
                                fontSize = 11.5.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { showApiKeyConfig = !showApiKeyConfig }
                            )

                            if (apiDiagnostic != null && apiDiagnostic!!.latencyMs > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = "الاستجابة: ${apiDiagnostic!!.latencyMs}ms",
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = showApiKeyConfig) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                OutlinedTextField(
                                    value = customKeyInput,
                                    onValueChange = { customKeyInput = it },
                                    label = { Text("Gemini API Key (AI Studio)") },
                                    placeholder = { Text("ألصق مفتاح الذكاء الاصطناعي هنا...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("gemini_custom_key_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateGeminiApiKey(customKeyInput)
                                            coroutineScope.launch {
                                                isCheckingApi = true
                                                viewModel.geminiOneClickDubber.testGeminiApiConnection(customKeyInput)
                                                isCheckingApi = false
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("حفظ المفتاح وفحص الاتصال", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. INPUT CONFIGURATION CARD (AUDIO RECORDER & TARGET LANGUAGE)
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. إدخال النص أو اختيار سيناريو جاهز للدبلجة ✍️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))

                        // Quick Script Templates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        customScriptText = "في أعماق الفضاء، حيث تلتقي النجوم، تبدأ مغامرة لا تنتهي من الاكتشاف والإثارة!"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("سيناريو سينمائي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        customScriptText = "مرحباً بكم يا أصدقائي! اليوم لدينا مغامرة كرتونية مدهشة وسر كبير سنكتشفه معاً!"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("سيناريو كرتوني", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Text input
                        OutlinedTextField(
                            value = customScriptText,
                            onValueChange = { customScriptText = it },
                            label = { Text("أو اكتب نصاً يدوياً للاختبار السريع (اختياري)") },
                            placeholder = { Text("اكتب جملة لتجربة التحويل والترجمة والنطق فوراً...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("one_click_fallback_text_field"),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Target Language Selector Dropdown
                        Text(
                            text = "اللغة المستهدفة للدبلجة:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))

                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isLanguageDropdownOpen = true }
                                    .testTag("one_click_language_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedLanguage.flagEmoji, fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = selectedLanguage.displayNameArabic,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text("تغيير ▾", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            DropdownMenu(
                                expanded = isLanguageDropdownOpen,
                                onDismissRequest = { isLanguageDropdownOpen = false }
                            ) {
                                DubbingTargetLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(lang.flagEmoji, fontSize = 16.sp)
                                                Spacer(Modifier.width(8.dp))
                                                Text(lang.displayNameArabic, fontSize = 12.5.sp)
                                            }
                                        },
                                        onClick = {
                                            selectedLanguage = lang
                                            isLanguageDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Voice Gender Selection Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "HERO_MALE" to "صوت رجالي 👨",
                                "HEROINE_FEMALE" to "صوت نسائي 👩",
                                "CHILD" to "صوت طفل/كرتون 🧒"
                            ).forEach { (code, label) ->
                                val isSelected = selectedVoiceGender == code
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedVoiceGender = code }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. THE BIG ONE-CLICK ACTION BUTTON
            // =========================================================================
            item {
                val isProcessing = dubbingState.stage in listOf(
                    DubbingStage.EXTRACTING_SPEECH,
                    DubbingStage.TRANSLATING_TEXT,
                    DubbingStage.GENERATING_SPEECH
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.geminiOneClickDubber.executeOneClickDubbing(
                                fallbackText = customScriptText,
                                targetLanguage = selectedLanguage,
                                speakerVoice = selectedVoiceGender,
                                customApiKey = customKeyInput
                            )
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("one_click_execute_dubbing_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isProcessing) listOf(Color(0xFF475569), Color(0xFF64748B))
                                    else listOf(Color(0xFF0284C7), Color(0xFF7C3AED), Color(0xFFE11D48))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "جاري المعالجة التلقائية الشاملة...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "⚡ دبلجة Gemini شاملة بضغطة زر واحدة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 4. LIVE STEP-BY-STEP PROGRESS STEPPER
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حالة مراحل الدبلجة التلقائية:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "${(dubbingState.progressPercent * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { dubbingState.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155),
                        )
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = dubbingState.statusMessage,
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        // Step Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StepChip(
                                stepNumber = "1",
                                title = "استخراج النص",
                                isDone = dubbingState.stage in listOf(DubbingStage.TRANSLATING_TEXT, DubbingStage.GENERATING_SPEECH, DubbingStage.COMPLETED),
                                isActive = dubbingState.stage == DubbingStage.EXTRACTING_SPEECH
                            )
                            StepChip(
                                stepNumber = "2",
                                title = "الترجمة الفورية",
                                isDone = dubbingState.stage in listOf(DubbingStage.GENERATING_SPEECH, DubbingStage.COMPLETED),
                                isActive = dubbingState.stage == DubbingStage.TRANSLATING_TEXT
                            )
                            StepChip(
                                stepNumber = "3",
                                title = "توليد النطق",
                                isDone = dubbingState.stage == DubbingStage.COMPLETED,
                                isActive = dubbingState.stage == DubbingStage.GENERATING_SPEECH
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 5. RESULTS & INSTANT PLAYBACK CARD
            // =========================================================================
            if (dubbingState.originalTranscript.isNotBlank() || dubbingState.translatedTranscript.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("one_click_dubbing_results_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "مخرجات الدبلجة الجاهزة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                dubbingState.dubbedAudioPath?.let { audioPath ->
                                    Button(
                                        onClick = {
                                            if (isPlayingDubbedAudio) {
                                                viewModel.geminiOneClickDubber.stopPlayback()
                                                isPlayingDubbedAudio = false
                                            } else {
                                                isPlayingDubbedAudio = true
                                                viewModel.geminiOneClickDubber.playAudio(audioPath) {
                                                    isPlayingDubbedAudio = false
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPlayingDubbedAudio) Color(0xFFEF4444) else Color(0xFF22C55E)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingDubbedAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isPlayingDubbedAudio) "إيقاف الصوت" else "تشغيل الصوت 🔊", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Original Speech
                            if (dubbingState.originalTranscript.isNotBlank()) {
                                Text(
                                    text = "النص الصوتي الأصلي المستخرج (STT):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dubbingState.originalTranscript,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("original", dubbingState.originalTranscript))
                                                Toast.makeText(context, "تم نسخ النص الأصلي 📋", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Translated Speech
                            if (dubbingState.translatedTranscript.isNotBlank()) {
                                Text(
                                    text = "النص المترجم والمكيف بالذكاء الاصطناعي (${dubbingState.targetLanguage.displayNameArabic}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF0284C7))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = dubbingState.translatedTranscript,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE2E8F0),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("translated", dubbingState.translatedTranscript))
                                                Toast.makeText(context, "تم نسخ النص المترجم 📋", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Action buttons: Send to Studio / Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dubbingState.dubbedAudioPath?.let { audioPath ->
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val file = File(audioPath)
                                                val uri: Uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "audio/wav"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "مشاركة الصوت المدبلج"))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "تعذر فتح نافذة المشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("مشاركة الملف", fontSize = 11.5.sp)
                                    }
                                }

                                Button(
                                    onClick = onSendToStudio,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("فتح في الاستوديو", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepChip(
    stepNumber: String,
    title: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = when {
            isDone -> Color(0xFF22C55E).copy(alpha = 0.2f)
            isActive -> Color(0xFF38BDF8).copy(alpha = 0.2f)
            else -> Color(0xFF1E293B)
        },
        border = BorderStroke(
            1.dp,
            when {
                isDone -> Color(0xFF22C55E)
                isActive -> Color(0xFF38BDF8)
                else -> Color(0xFF334155)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isDone -> Color(0xFF22C55E)
                    isActive -> Color(0xFF38BDF8)
                    else -> Color(0xFF475569)
                },
                modifier = Modifier.size(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isDone) "✓" else stepNumber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive || isDone) Color(0xFFE2E8F0) else Color(0xFF94A3B8)
            )
        }
    }
}
