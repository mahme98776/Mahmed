package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppTab
import com.example.audio.assistant.AlexaAssistantState
import com.example.audio.assistant.AlexaVoiceAssistantEngine
import com.example.audio.assistant.AlexaVoiceIntent
import com.example.audio.media3.Media3AudioProcessingLayer
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.launch

/**
 * Alexa & Gemini style Voice Assistant Dialog with Auto-Correction
 * and Media3 Audio Processing Integration.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlexaVoiceAssistantModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: DubbingViewModel,
    onNavigateToTab: (AppTab) -> Unit,
    media3ProcessingLayer: Media3AudioProcessingLayer,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val assistantEngine = remember {
        AlexaVoiceAssistantEngine(
            context = context,
            ttsManager = viewModel.ttsManager,
            media3ProcessingLayer = media3ProcessingLayer
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            assistantEngine.stopListening()
            assistantEngine.release()
        }
    }

    val assistantState by assistantEngine.assistantState.collectAsStateWithLifecycle()
    val rmsVolume by assistantEngine.rmsVolume.collectAsStateWithLifecycle()
    val lastTranscript by assistantEngine.lastTranscript.collectAsStateWithLifecycle()
    val lastParsedCommand by assistantEngine.lastParsedCommand.collectAsStateWithLifecycle()
    val isVoiceFeedbackEnabled by assistantEngine.isVoiceFeedbackEnabled.collectAsStateWithLifecycle()
    val isContinuousWakeWordListening by assistantEngine.isContinuousWakeWordListening.collectAsStateWithLifecycle()
    var manualTextQuery by remember { mutableStateOf("") }

    val isListening = assistantState is AlexaAssistantState.Listening || isContinuousWakeWordListening

    // Pulsating ambient animation for the glowing Orb
    val infiniteTransition = rememberInfiniteTransition(label = "AlexaGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    fun handleNavigation(targetId: String) {
        when (targetId) {
            "STUDIO" -> onNavigateToTab(AppTab.STUDIO)
            "VIDEO_DUB" -> onNavigateToTab(AppTab.VIDEO_DUB)
            "YOUTUBE_AUTO_DUB" -> onNavigateToTab(AppTab.YOUTUBE_AUTO_DUB)
            "GEMINI_ONE_CLICK" -> onNavigateToTab(AppTab.GEMINI_ONE_CLICK)
            "PROJECTS" -> onNavigateToTab(AppTab.PROJECTS)
            "SETTINGS" -> onNavigateToTab(AppTab.SETTINGS)
            else -> onNavigateToTab(AppTab.STUDIO)
        }
        onDismiss()
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantEngine.startListening(
                scope = coroutineScope,
                viewModel = viewModel,
                onNavigate = { handleNavigation(it) }
            )
        } else {
            viewModel.showToast("يتطلب المساعد الصوتي إذن الميكروفون (RECORD_AUDIO) للاستماع للأوامر 🎙️")
        }
    }

    fun startListeningWithPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            assistantEngine.startListening(
                scope = coroutineScope,
                viewModel = viewModel,
                onNavigate = { handleNavigation(it) }
            )
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Dialog(
        onDismissRequest = {
            assistantEngine.stopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .testTag("alexa_voice_assistant_modal"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المساعد الصوتي الذكي (Alexa & Gemini)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "التعرف الذكي وتصحيح الأخطاء ومعالجة Media3",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            assistantEngine.stopListening()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Interactive Alexa / Gemini Glowing Animated Voice Orb
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            if (isListening) {
                                assistantEngine.stopListening()
                            } else {
                                startListeningWithPermission()
                            }
                        }
                ) {
                    // Outer Ripple Halo based on voice RMS volume
                    val dynamicScale = (1.0f + (rmsVolume * 0.45f)) * (if (isListening) pulseScale else 1.0f)
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(dynamicScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isListening) {
                                        listOf(
                                            Color(0xFF00E5FF).copy(alpha = 0.55f),
                                            Color(0xFF7C4DFF).copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    }
                                )
                            )
                    )

                    // Core Orb
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(
                            width = if (isListening) 3.dp else 1.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF651FFF),
                                    Color(0xFFFF4081),
                                    Color(0xFF00E5FF)
                                )
                            )
                        ),
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isListening) {
                                        listOf(Color(0xFF0D47A1), Color(0xFF4A148C))
                                    } else {
                                        listOf(Color(0xFF263238), Color(0xFF37474F))
                                    }
                                )
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "زر التحدث الصوتي",
                                tint = if (isListening) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Status Indicator or Error Recovery Banner
                if (assistantState is AlexaAssistantState.Error) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = (assistantState as AlexaAssistantState.Error).errorMessageArabic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { assistantEngine.resetErrorState() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("إعادة المحاولة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(
                        text = when (assistantState) {
                            is AlexaAssistantState.Listening -> "🔴 أستمع إليك الآن... تفضل بالأمر الصوتي"
                            is AlexaAssistantState.Thinking -> "⏳ جارٍ التحليل وتصحيح نبرة الأمر..."
                            is AlexaAssistantState.Executing -> "⚡ تنفيذ: ${(assistantState as AlexaAssistantState.Executing).commandTitleArabic}"
                            is AlexaAssistantState.Speaking -> "🗣️ الرد الصوتي نشط..."
                            is AlexaAssistantState.Idle -> "اضغط على الدائرة للتحدث أو اكتب أمرك بالأسفل 👇"
                            else -> "المساعد جاهز للاستماع إليك"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) Color(0xFF00B0FF) else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Direct Text Command Input (Fallback & Accessibility)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualTextQuery,
                        onValueChange = { manualTextQuery = it },
                        placeholder = { Text("اكتب أي أمر هنا للمساعد...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (manualTextQuery.isNotBlank()) {
                                assistantEngine.submitTextCommand(manualTextQuery, viewModel) { handleNavigation(it) }
                                manualTextQuery = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "تنفيذ الأمر", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Quick Action Suggestion Chips
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = { assistantEngine.submitTextCommand("دبلج المشهد بضغطة زر", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "دبلجة 🚀",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("ترجم السيناريو للعربية", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ترجمة 🌐",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("نقي الصوت وموازنة", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "تنقية 🎙️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("صدري الفيديو النهائي", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "تصدير 💾",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = { assistantEngine.submitTextCommand("شغلي المشهد", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "تشغيل ▶️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("استنسخي صوت الممثل", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "استنساخ 🧬",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("افصلي التراكات الصوتية", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "فصل التراكات 🎧",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }

                    Surface(
                        onClick = { assistantEngine.submitTextCommand("مقطع ريلز للتريند", viewModel) { handleNavigation(it) } },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ريلز وتريند 📱",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }

                // Live Transcription & Alexa Error-Correction Card
                if (lastTranscript.isNotBlank() || lastParsedCommand != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "النص الملتقط: \"$lastTranscript\"",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (lastParsedCommand?.wasAutoCorrected == true) {
                                    Badge(containerColor = Color(0xFF00C853)) {
                                        Text("تم التصحيح آلياً ✅", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }

                            lastParsedCommand?.let { cmd ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "الأمر المفهوم: ${cmd.correctedText} (${cmd.detectedIntent.titleArabic})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        assistantEngine.executeParsedCommand(cmd, viewModel) { handleNavigation(it) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("تأكيد وتنفيذ الأمر فوراً ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Direct Media3 Noise Suppression & Gain Normalization Button
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            val path = viewModel.uiState.value.recordedAudioPath
                            if (path != null && java.io.File(path).exists()) {
                                val result = media3ProcessingLayer.processVoiceClip(java.io.File(path))
                                if (result.success) {
                                    viewModel.updateRecordedAudioPath(result.processedFile.absolutePath)
                                    viewModel.showToast(result.messageArabic)
                                    viewModel.ttsManager.speakText("تم عزل الضوضاء وموازنة درجات الصوت بـ Media3 بنجاح", utteranceId = "media3_success")
                                }
                            } else {
                                viewModel.showToast("يرجى تسجيل مقطع صوتي أولاً لتطبيق معالجة Media3! 🎙️")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E88E5).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("media3_ai_clean_action_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "طبقة معالجة الصوت بالذكاء الاصطناعي (Media3 Layer)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "عزل الضجيج الخلفي وموازنة الـ Gain تلقائياً لكافة المقاطع",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("تنفيذ 🎚️", fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5), fontSize = 11.5.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Quick Action Voice Suggestions
                Text(
                    text = "أوامر صوتية سريعة يمكنك نطقها أو الضغط عليها:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickVoiceCommands = listOf(
                        "⚡ إنتاج كامل وميكس" to "معالجة كاملة: تنقية ومؤثر وموازنة",
                        "🎯 مزامنة الشفاه آلياً" to "أليكسا اضبطي مزامنة الشفاه تلقائياً",
                        "🪄 تدقيق الحركات والنص" to "أليكسا دققي جودة النص والحركات",
                        "✍️ تفريغ الصوت لنص" to "أليكسا حولي الصوت لنص متزامن",
                        "🚀 دبلجة بضغطة زر" to "أليكسا ابدئي الدبلجة التلقائية بضغطة واحدة",
                        "📱 هاشتاجات للنشر" to "أليكسا ولدي عناوين وهاشتاجات للنشر",
                        "🎙️ ابدأ التسجيل" to "سجل صوتي الآن",
                        "⏹️ إيقاف التسجيل" to "وقف التسجيل",
                        "🔊 تشغيل الصوت" to "شغل المقطع المسجل",
                        "🤖 مؤثر الروبوت" to "طبق مؤثر الروبوت الآلي",
                        "🎙️ صدى الاستوديو" to "تفعيل صدى الاستوديو",
                        "✂️ قص وحذف الصمت" to "قص الصوت واحذف الصمت",
                        "🎚️ تنقية Media3" to "نظف الصوت بـ Media3 ووازن الـ Gain",
                        "🎚️ المعادل الصوتي" to "أليكسا افتحي المعادل الصوتي",
                        "🎬 دبلجة الفيديو" to "افتح دبلجة الفيديو",
                        "📱 أليكسا افتحي اليوتيوب" to "اليكسا افتحي اليوتيوب",
                        "👤 المطور وحقوق الملكية" to "من هو المطور وحقوق النشر؟"
                    )

                    quickVoiceCommands.forEach { (label, phrase) ->
                        Surface(
                            onClick = {
                                val parsed = assistantEngine.parseVoiceCommandWithAutoCorrection(phrase)
                                assistantEngine.executeParsedCommand(parsed, viewModel) { handleNavigation(it) }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Continuous Wake-Word Detection Switch ("Always-listening for Alexa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "الاستماع المستمر لكلمة 'Alexa' (Wake-Word Mode)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تنشيط تلقائي وفتح التطبيقات بمجرد نداء أليكسا",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isContinuousWakeWordListening,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    assistantEngine.toggleContinuousWakeWordListening(
                                        enabled = true,
                                        scope = coroutineScope,
                                        viewModel = viewModel,
                                        onNavigate = { handleNavigation(it) }
                                    )
                                    com.example.audio.assistant.AlexaBackgroundWakeWordService.start(context)
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                assistantEngine.toggleContinuousWakeWordListening(
                                    enabled = false,
                                    scope = coroutineScope,
                                    viewModel = viewModel,
                                    onNavigate = { handleNavigation(it) }
                                )
                                com.example.audio.assistant.AlexaBackgroundWakeWordService.stop(context)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Voice Feedback Switch & Creator Copyright
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "نطق الردود صوتياً (TTS)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = isVoiceFeedbackEnabled,
                        onCheckedChange = { assistantEngine.toggleVoiceFeedback(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Official Intellectual Property Notice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "حقوق النشر والملكية الفكرية محفوظة: محمد رضا محمود محمود السيد سليمة © 2026",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
