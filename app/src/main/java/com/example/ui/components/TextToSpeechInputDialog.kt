package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.TtsEngineState
import com.example.audio.VoiceProfile
import com.example.model.ScriptLine
import com.example.ui.DubbingViewModel

/**
 * Text-to-Speech Script Input Dialog
 * Allows users to enter/edit custom script text and generate synthetic dubbed speech takes
 * using offline Android TTS or Cloud AI voices with custom pitch and speed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechInputDialog(
    viewModel: DubbingViewModel,
    targetLine: ScriptLine? = null,
    onDismiss: () -> Unit
) {
    val ttsManager = viewModel.ttsManager
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val ttsStatus by viewModel.ttsStatusInfo.collectAsState()

    var scriptText by remember {
        mutableStateOf(targetLine?.textArabic ?: "")
    }

    var selectedProfile by remember {
        mutableStateOf(
            ttsManager.voiceProfiles.find {
                if (targetLine?.voiceType != null) {
                    when (targetLine.voiceType) {
                        "HERO_MALE" -> it.id == "hero_male"
                        "HEROINE_FEMALE" -> it.id == "heroine_female"
                        "EPIC_NARRATOR" -> it.id == "epic_narrator"
                        "FEMALE" -> it.id == "natural_arabic_female"
                        "CARTOON" -> it.id == "cartoon_hero"
                        "DRAMATIC" -> it.id == "male_narrator"
                        else -> it.id == "natural_arabic_male"
                    }
                } else true
            } ?: ttsManager.voiceProfiles.first()
        )
    }

    var customPitch by remember { mutableStateOf(selectedProfile.pitch) }
    var customSpeed by remember { mutableStateOf(selectedProfile.speechRate) }
    var isGenerating by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            ttsManager.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .testTag("tts_input_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (targetLine != null) "تحويل النص إلى صوت للمشهد 🎬" else "تحويل النص إلى صوت دبلجة 🎙️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (targetLine != null) "المشهد: ${targetLine.characterName} (${String.format("%.1f", targetLine.startSeconds)}s - ${String.format("%.1f", targetLine.endSeconds)}s)" else "إنشاء مسار صوتي نقي ومطابق للنص",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            ttsManager.stop()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // TTS Engine Initialization Status Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (ttsStatus.state) {
                        TtsEngineState.READY_SUCCESS -> Color(0xFF1B5E20).copy(alpha = 0.12f)
                        TtsEngineState.INITIALIZING -> Color(0xFFE65100).copy(alpha = 0.12f)
                        TtsEngineState.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        TtsEngineState.UNINITIALIZED -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when (ttsStatus.state) {
                            TtsEngineState.READY_SUCCESS -> Color(0xFF2E7D32).copy(alpha = 0.4f)
                            TtsEngineState.INITIALIZING -> Color(0xFFEF6C00).copy(alpha = 0.4f)
                            TtsEngineState.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                            TtsEngineState.UNINITIALIZED -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            when (ttsStatus.state) {
                                TtsEngineState.READY_SUCCESS -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "محرك الصوت جاهز ونشط (SUCCESS) ✓",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = if (ttsStatus.isArabicSupported) "النطق العربي مدعوم ومطابق للأداء الطبيعي" else "يتم استخدام المحرك الافتراضي للنظام",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                TtsEngineState.INITIALIZING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFEF6C00)
                                    )
                                    Text(
                                        text = "جاري تهيئة محرك TextToSpeech (OnInitListener)...",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                TtsEngineState.FAILED -> {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "تعذر تهيئة محرك الصوت (فشل onInit)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = ttsStatus.errorMessage ?: "رمز الحالة غير ناجح",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                                TtsEngineState.UNINITIALIZED -> {
                                    Text(
                                        text = "محرك الصوت غير مهيأ",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (ttsStatus.state == TtsEngineState.FAILED) {
                            TextButton(
                                onClick = { viewModel.retryTtsInitialization() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("إعادة المحاولة 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Script Text Input Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "النص المراد تحويله إلى كلام ✍️",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Tashkeel / Auto-Format helper button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    if (scriptText.isNotBlank()) {
                                        // Quick simple diacritics enrichment hint
                                        scriptText = scriptText.trim()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                    Text("ضبط النطق الفصيح ✨", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = scriptText,
                            onValueChange = { scriptText = it },
                            placeholder = {
                                Text(
                                    "اكتب الحوار أو النص هنا باللغة العربية الفصحى ليقوم الذكاء الاصطناعي بنطقه بأعلى واقعية...",
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("tts_script_text_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    // Voice Profiles Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "اختر نبرة وشخصية الصوت 🎭",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(ttsManager.voiceProfiles) { profile ->
                                val isSelected = selectedProfile.id == profile.id
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 0.5.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            selectedProfile = profile
                                            customPitch = profile.pitch
                                            customSpeed = profile.speechRate
                                        }
                                        .testTag("voice_profile_${profile.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .width(130.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = profile.emoji, fontSize = 22.sp)
                                        Text(
                                            text = profile.titleArabic,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = profile.subtitleArabic,
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sliders: Pitch & Speed
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Pitch Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("نبرة الصوت (Pitch):", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${String.format("%.2f", customPitch)}x", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = customPitch,
                                    onValueChange = { customPitch = it },
                                    valueRange = 0.5f..1.8f,
                                    steps = 12,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Speech Rate Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("سرعة الإلقاء (Speech Rate):", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${String.format("%.2f", customSpeed)}x", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = customSpeed,
                                    onValueChange = { customSpeed = it },
                                    valueRange = 0.6f..1.8f,
                                    steps = 12,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Preview Live Button
                    OutlinedButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsManager.stop()
                            } else {
                                if (!ttsManager.isEngineReady()) {
                                    return@OutlinedButton
                                }
                                val dynamicProfile = selectedProfile.copy(
                                    pitch = customPitch,
                                    speechRate = customSpeed
                                )
                                ttsManager.speakText(
                                    text = scriptText.ifBlank { "مرحباً بكم في استوديو دبلجة فويس ماستر برو" },
                                    profile = dynamicProfile,
                                    languageCode = "ar"
                                )
                            }
                        },
                        enabled = ttsStatus.isSuccess,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("tts_preview_btn")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSpeaking) "إيقاف ⏹️" else "استماع 🎧", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Generate and Save as Audio Take Button
                    Button(
                        onClick = {
                            isGenerating = true
                            ttsManager.stop()
                            val dynamicProfile = selectedProfile.copy(
                                pitch = customPitch,
                                speechRate = customSpeed
                            )
                            val targetDuration = targetLine?.let { (it.endSeconds - it.startSeconds) }
                            viewModel.synthesizeCustomScriptToAudio(
                                text = scriptText,
                                profile = dynamicProfile,
                                targetLineId = targetLine?.id,
                                targetDurationSeconds = targetDuration
                            ) { success, _ ->
                                isGenerating = false
                                if (success) {
                                    onDismiss()
                                }
                            }
                        },
                        enabled = scriptText.isNotBlank() && !isGenerating && ttsStatus.isSuccess,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("tts_apply_audio_btn")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("توليد وتعيين الصوت 🎙️", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
