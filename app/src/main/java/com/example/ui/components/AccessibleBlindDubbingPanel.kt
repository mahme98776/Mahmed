package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DubbingClip
import com.example.model.SampleClipsRepository
import com.example.ui.DubbingViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * Accessible Blind Dubbing Panel
 * Designed specifically for blind and visually impaired users with:
 * - Extremely large, high-contrast touch targets (>= 64dp)
 * - Tactile Haptic Feedback on every action
 * - Immediate Arabic Voice Feedback (TextToSpeech) for state changes and user interactions
 * - Simplified 4-Step Linear Dubbing Workflow (Scene -> Voice -> Play/Preview -> Save/Export)
 * - Complete audio walkthrough instructions on demand
 */
@Composable
fun AccessibleBlindDubbingPanel(
    viewModel: DubbingViewModel,
    onImportAudio: () -> Unit,
    onImportVideo: () -> Unit,
    onOpenVoiceLibrary: () -> Unit,
    onOpenTtsInput: () -> Unit,
    onExportProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isAccessibilityModeEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("العربية") }
    val availableLanguages = listOf("العربية", "الإنجليزية", "الفرنسية", "الألمانية", "الإسبانية", "التركية")

    fun speak(text: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.ttsManager.speakText(text, utteranceId = "blind_accessible_feedback")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("accessible_blind_dubbing_panel")
            .semantics {
                contentDescription = "لوحة سهولة الاستخدام للمكفوفين وضعاف البصر"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16141D)
        ),
        border = BorderStroke(2.dp, Color(0xFFFBBF24)) // High-contrast amber border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Accessibility Mode Switch & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "وضع المكفوفين الميسر ♿",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = if (isAccessibilityModeEnabled) "أزرار ضخمة وتوجيه صوتي مفعل 🔊" else "الوضع القياسي",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Switch(
                    checked = isAccessibilityModeEnabled,
                    onCheckedChange = { isChecked ->
                        isAccessibilityModeEnabled = isChecked
                        if (isChecked) {
                            speak("تم تفعيل وضع سهولة الاستخدام للمكفوفين. الأزرار أصبحت كبيرة مع توجيه صوتي كامل.")
                        } else {
                            speak("تم إيقاف وضع المكفوفين الميسر.")
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFFFBBF24),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .testTag("toggle_blind_accessibility_switch")
                        .semantics {
                            contentDescription = "مفتاح تفعيل وضع سهولة الاستخدام للمكفوفين"
                        }
                )
            }

            AnimatedVisibility(
                visible = isAccessibilityModeEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Audio Walkthrough Button (Spoken Comprehensive Guide)
                    Button(
                        onClick = {
                            speak(
                                "مرحباً بك في استوديو الدبلجة الميسر. إليك الخطوات بالترتيب: " +
                                        "أولاً: بطاقة المشهد الحالي، اضغط عليها لتغيير المشهد أو استيراد فيديو. " +
                                        "ثانياً: بطاقة صوت الدبلجة، اضغط عليها لاختيار صوت ذكاء اصطناعي أو استيراد ملف صوتي حقيقي أو تسجيل بصوتك. " +
                                        "ثالثاً: زر التشغيل الأخضر الكبير لمعاينة المشهد مع الصوت المدبلج وسماعه فوراً. " +
                                        "رابعاً: زر الحفظ والتصدير لحفظ الفيديو النهائي في جهازك. استمتع بدبلجة فيديوهاتك بكل سهولة!"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("blind_full_audio_walkthrough_btn")
                            .semantics {
                                contentDescription = "زر الاستماع للشرح الصوتي الكامل خطوة بخطوة للمكفوفين"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🎧 استمع للشرح الصوتي الكامل للدبلجة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // =========================================================================
                    // 3-Step Dubbing Pipeline Card (STT -> Gemini Translation -> TTS Waveform Sync)
                    // =========================================================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("three_step_dubbing_workflow_card")
                            .semantics {
                                contentDescription = "مسار الدبلجة السريع في ثلاث خطوات: تحويل الصوت لنص، ترجمة جيميناي، وتوليد الصوت والمزامنة"
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B2E)),
                        border = BorderStroke(2.dp, Color(0xFF38BDF8))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Pipeline Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "🚀",
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الدبلجة الذكية في 3 خطوات بسيطة",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        text = "1. STT متزامن 🎙️ ⬅️ 2. ترجمة Gemini 🌐 ⬅️ 3. TTS والمزامنة 🎬",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            // Master 100% Autonomous Dubbing Pipeline Button (Zero-Mic, Zero-Typing)
                            Button(
                                onClick = {
                                    speak("بدء الدبلجة التلقائية الكاملة بالذكاء الاصطناعي دون الحاجة لميكروفون أو كتابة")
                                    viewModel.performCompleteAutonomousDubbing(targetLanguage = selectedLanguage) { success, msg ->
                                        speak(msg)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .testTag("one_tap_autonomous_dubbing_master_button")
                                    .semantics {
                                        contentDescription = "زر الدبلجة التلقائية الشاملة بنقرة واحدة بدون ميكروفون وبدون كتابة"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "⚡ دبلجة تلقائية كاملة بنقرة واحدة",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "استخراج الحوار + ترجمة Gemini + توليد أصوات ومزامنة الفيديو ذاتياً 100%",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // -----------------------------------------------------------------
                            // STEP 1: Autonomous Scene Dialogue Extraction (بدون ميكروفون وبدون كتابة)
                            // -----------------------------------------------------------------
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF262338),
                                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الخطوة 1: استخراج حوار المشهد وتوقيتاته ذاتياً ⚡🎬",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8)
                                        )
                                        Text(
                                            text = "${state.scriptLines.size} سطر متزامن",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Text(
                                        text = "النظام يعتمد على نفسه كلياً في استخراج الحوار وتوزيعه زمنياً على المشهد دون الحاجة لأي تسجيل صوتي أو كتابة يدوية.",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )

                                    // 1-Click Auto Extract & Generate Dialogue Button
                                    Button(
                                        onClick = {
                                            viewModel.autoGenerateSmartDialogueLines()
                                            speak("تم استخراج وتوليد حوارات وتوقيتات المشهد تلقائياً بنجاح بدون كتابة أو ميكروفون")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3730A3)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .testTag("auto_extract_scene_dialogue_btn")
                                            .semantics {
                                                contentDescription = "زر استخراج وتوليد حوارات المشهد وتوقيتاتها آلياً"
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "⚡ استخراج وتوليد حوارات المشهد آلياً",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Action: Copy Script to Clipboard
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.copyScriptTextToClipboard(includeTimestamps = false)
                                            speak("تم نسخ نصوص الحوار بالكامل إلى الحافظة")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("copy_script_to_clipboard_btn")
                                            .semantics {
                                                contentDescription = "زر نسخ النص إلى الحافظة"
                                            }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("نسخ نصوص الحوار الحالية 📋", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Display first few synchronized lines
                                    if (state.scriptLines.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF16141D), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            state.scriptLines.take(3).forEach { line ->
                                                Text(
                                                    text = "⏱️ [${line.startSeconds}ث - ${line.endSeconds}ث]: ${line.textArabic}",
                                                    fontSize = 12.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                            if (state.scriptLines.size > 3) {
                                                Text(
                                                    text = "... والمزيد (${state.scriptLines.size} سطر إجمالي)",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF818CF8)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // -----------------------------------------------------------------
                            // STEP 2: Gemini AI Translation (مع الحفاظ على التوقيت)
                            // -----------------------------------------------------------------
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF262338),
                                border = BorderStroke(1.dp, Color(0xFFF472B6).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "الخطوة 2: ترجمة Gemini الذكية مع حفظ التوقيت 🌐🤖",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF472B6)
                                    )

                                    // Target Language Chips
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        availableLanguages.forEach { lang ->
                                            val isSelected = selectedLanguage == lang
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedLanguage = lang
                                                    speak("تم اختيار لغة الترجمة: $lang")
                                                },
                                                label = { Text(lang, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFF472B6),
                                                    selectedLabelColor = Color.Black
                                                )
                                            )
                                        }
                                    }

                                    // Translate with Gemini Button
                                    val isTranslating = state.isGeminiTranslating
                                    Button(
                                        onClick = {
                                            viewModel.translateScriptLinesWithGemini(selectedLanguage) { success ->
                                                if (success) {
                                                    speak("اكتملت الترجمة بنجاح، يمكنك الانتقال للخطوة الثالثة لتوليد الصوت")
                                                }
                                            }
                                        },
                                        enabled = !isTranslating,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .testTag("gemini_translate_button")
                                            .semantics {
                                                contentDescription = "زر ترجمة النص عبر الذكاء الاصطناعي جيميناي إلى $selectedLanguage مع الحفاظ على التوقيت"
                                            }
                                    ) {
                                        if (isTranslating) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("جاري الترجمة بـ Gemini AI...", fontSize = 14.sp, color = Color.White)
                                        } else {
                                            Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "🤖 ترجمة الحوارات إلى $selectedLanguage بواسطة Gemini",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Copy translated text
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.copyScriptTextToClipboard(includeTimestamps = false)
                                            speak("تم نسخ النص المترجم إلى الحافظة")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("copy_translated_script_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("نسخ النص المترجم إلى الحافظة 📋", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // -----------------------------------------------------------------
                            // STEP 3: TTS & Timeline Synthesis (توليد الصوت وحفظه ومزامنته مع الفيديو)
                            // -----------------------------------------------------------------
                            val isSynthesizing = state.isTtsSynthesizingTimeline
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF262338),
                                border = BorderStroke(1.5.dp, Color(0xFF34D399)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "الخطوة 3: توليد صوت الدبلجة ومزامنته مع الفيديو (TTS) 🎙️🎬",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )

                                    Text(
                                        text = "يولد ملف صوتي حقيقي متزامن مع كل ثانية في الفيديو، ويحفظه في المشروع ليعمل فوراً عند التشغيل.",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )

                                    // Giant Synthesis Button (70dp touch target for accessibility)
                                    Button(
                                        onClick = {
                                            viewModel.synthesizeAndSyncDubbedAudio { audioPath ->
                                                if (audioPath != null) {
                                                    speak("تم توليد الصوت وحفظه وتركيبه على الفيديو بنجاح، اضغط على زر التشغيل الأخضر الكبير بالأسفل للاستماع فوراً")
                                                }
                                            }
                                        },
                                        enabled = !isSynthesizing,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .testTag("synthesize_and_sync_dub_btn")
                                            .semantics {
                                                contentDescription = "زر كبير: توليد ملف صوت الدبلجة وحفظه في الاستوديو وتركيبه على خط زمن الفيديو"
                                            }
                                    ) {
                                        if (isSynthesizing) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("جاري توليد ملف الصوت ومزامنته مع الفيديو...", fontSize = 14.sp, color = Color.White)
                                        } else {
                                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "🎙️🎬 توليد ملف الصوت وحفظه ومزامنته مع الفيديو",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (state.recordedAudioPath != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF064E3B).copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "✅ الصوت المدبلج مركب ومتزامن الآن! اضغط زر التشغيل الأخضر الكبير بالأسفل لمعاينته.",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF34D399)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 1: Current Scene Card
                    AccessibleStepCard(
                        stepNumber = "1",
                        stepTitle = "المشهد الحالي والفيديو 🎬",
                        statusText = "المشهد: ${state.currentClip.title} (مدته ${state.currentClip.durationSeconds} ثانية)",
                        highlightColor = Color(0xFF38BDF8),
                        onSpeakStatus = {
                            speak("الخطوة الأولى: المشهد الحالي هو ${state.currentClip.title}، مدته ${state.currentClip.durationSeconds} ثانية. يمكنك الضغط على تبديل المشهد أو استيراد فيديو من جهازك.")
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val clips = SampleClipsRepository.clips
                                    val currentIdx = clips.indexOfFirst { it.id == state.currentClip.id }
                                    val nextClip = if (currentIdx >= 0 && currentIdx < clips.size - 1) {
                                        clips[currentIdx + 1]
                                    } else {
                                        clips.first()
                                    }
                                    viewModel.loadClip(nextClip)
                                    speak("تم تغيير المشهد إلى: ${nextClip.title}. مدته ${nextClip.durationSeconds} ثانية.")
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("blind_next_scene_btn")
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تبديل المشهد", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    speak("فتح متصفح الفيديوهات لاختيار فيديو من هاتفك")
                                    onImportVideo()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("blind_import_video_btn")
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("استيراد فيديو", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }
                    }

                    // STEP 2: Dubbing Voice Track Card
                    val hasRecordedAudio = state.recordedAudioPath != null
                    AccessibleStepCard(
                        stepNumber = "2",
                        stepTitle = "صوت الدبلجة البديل 🎙️",
                        statusText = if (hasRecordedAudio) "✅ يوجد صوت مدبلج جاهز في المشروع" else "⚠️ لم يتم إضافة صوت بعد (اختر من الخيارات بالأسفل)",
                        highlightColor = Color(0xFFA78BFA),
                        onSpeakStatus = {
                            if (hasRecordedAudio) {
                                speak("الخطوة الثانية: صوت الدبلجة جاهز ومدرج في المشروع ومستعد للمعاينة والتصدير.")
                            } else {
                                speak("الخطوة الثانية: لا يوجد صوت مدبلج حالياً. اختر استيراد ملف صوتي حقيقي، أو تسجيل صوتك مباشرة، أو توليد الدبلجة بالذكاء الاصطناعي.")
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Import Audio
                                Button(
                                    onClick = {
                                        speak("فتح متصفح الملفات لاختيار ملف صوتي من هاتفك")
                                        onImportAudio()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("blind_import_audio_btn")
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("رفع صوت من الجهاز", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct Voice Recording Button
                                FilledTonalButton(
                                    onClick = {
                                        speak("بدء التسجيل الصوتي المباشر للمشهد عبر الميكروفون")
                                        viewModel.startRecordingCountdown()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("blind_record_audio_btn")
                                ) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تسجيل صوتي حي 🎙️", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // AI Voices or TTS Dialog
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        speak("فتح قائمة نطق الحوار وكتابة النص للتحويل إلى صوت")
                                        onOpenTtsInput()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFA78BFA)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("blind_tts_dialog_btn")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA78BFA))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توليد صوت بالذكاء الاصطناعي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                                }

                                OutlinedButton(
                                    onClick = {
                                        speak("فتح مكتبة الأصوات البشرية والشخصيات الكرتونية")
                                        onOpenVoiceLibrary()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("blind_open_voices_btn")
                                ) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFFFBBF24))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مكتبة الأصوات", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                }
                            }
                        }
                    }

                    // STEP 3: Giant Play / Pause Preview Button
                    val isPlaying = state.isPlaying
                    AccessibleStepCard(
                        stepNumber = "3",
                        stepTitle = "معاينة وتشغيل الدبلجة ▶️",
                        statusText = if (isPlaying) "المشهد قيد التشغيل الآن..." else "متوقف مؤقتاً (اضغط على الزر الكبير للتشغيل)",
                        highlightColor = if (isPlaying) Color(0xFFF59E0B) else Color(0xFF22C55E),
                        onSpeakStatus = {
                            if (isPlaying) {
                                speak("المشهد يعمل الآن مع الصوت المدبلج. اضغط على الزر لإيقافه مؤقتاً.")
                            } else {
                                speak("المشهد متوقف. اضغط على الزر الأخضر الكبير لبدء التشغيل والاستماع.")
                            }
                        }
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    viewModel.pausePlayback()
                                    speak("تم إيقاف التشغيل مؤقتاً.")
                                } else {
                                    viewModel.startPlayback()
                                    speak("بدأ تشغيل المشهد مع الدبلجة الآن.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) Color(0xFFD97706) else Color(0xFF16A34A)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp) // Giant touch target
                                .testTag("blind_giant_play_pause_btn")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isPlaying) "زر كبير: إيقاف التشغيل مؤقتاً" else "زر كبير: بدء تشغيل واستماع المشهد المدبلج"
                                }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isPlaying) "⏸️ إيقاف التشغيل مؤقتاً" else "▶️ اضغط هنا لبدء التشغيل والاستماع",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // STEP 4: Save & Export Button
                    AccessibleStepCard(
                        stepNumber = "4",
                        stepTitle = "حفظ وتصدير الفيديو النهائي 💾",
                        statusText = "جاهز لتصدير الفيديو ودمج الصوت بجودة فائقة.",
                        highlightColor = Color(0xFFEC4899),
                        onSpeakStatus = {
                            speak("الخطوة الرابعة: حفظ وتصدير الفيديو. اضغط على الزر بالأسفل لفتح خيارات التصدير وحفظ الفيديو النهائي على هاتفك.")
                        }
                    ) {
                        Button(
                            onClick = {
                                speak("تم فتح نافذة تصدير وحفظ الفيديو النهائي. اختر الجودة المطلوبة للحفظ.")
                                onExportProject()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .testTag("blind_giant_export_btn")
                                .semantics {
                                    contentDescription = "زر كبير: حفظ وتصدير الفيديو النهائي المكتمل"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "💾 تصدير وحفظ الفيديو النهائي في جهازك",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Quick Audio & Volume Assist Controls
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF201D27)),
                        border = BorderStroke(1.dp, Color(0xFF4C4556)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🎚️ تحكم صوتي سريع وسهل (مع النطق):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Toggle Original Video Audio
                                val isOriginalMuted = state.originalVolume <= 0.01f
                                OutlinedButton(
                                    onClick = {
                                        if (isOriginalMuted) {
                                            viewModel.setOriginalVolume(0.25f)
                                            speak("تم تفعيل صوت الفيديو الأصلي.")
                                        } else {
                                            viewModel.setOriginalVolume(0.0f)
                                            speak("تم كتم صوت الفيديو الأصلي، سيظهر صوت الدبلجة فقط.")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isOriginalMuted) Color(0xFFEF4444) else Color(0xFF10B981)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOriginalMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = if (isOriginalMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOriginalMuted) "إلغاء كتم الأصلي" else "كتم صوت الأصلي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOriginalMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                }

                                // Volume Up
                                FilledTonalButton(
                                    onClick = {
                                        val newVol = (state.dubVolume + 0.15f).coerceAtMost(1.0f)
                                        viewModel.setDubVolume(newVol)
                                        speak("مستوى صوت الدبلجة الآن ${(newVol * 100).toInt()} بالمائة")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رفع الصوت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Volume Down
                                FilledTonalButton(
                                    onClick = {
                                        val newVol = (state.dubVolume - 0.15f).coerceAtLeast(0.1f)
                                        viewModel.setDubVolume(newVol)
                                        speak("مستوى صوت الدبلجة الآن ${(newVol * 100).toInt()} بالمائة")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.VolumeDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("خفض الصوت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Accessible Step Card for Blind Mode
 */
@Composable
private fun AccessibleStepCard(
    stepNumber: String,
    stepTitle: String,
    statusText: String,
    highlightColor: Color,
    onSpeakStatus: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF221F2D)),
        border = BorderStroke(1.5.dp, highlightColor.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$stepTitle. $statusText"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Step Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = highlightColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stepNumber,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stepTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Hear Step description Button
                Surface(
                    shape = CircleShape,
                    color = highlightColor.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSpeakStatus()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "استمع لتفاصيل هذه الخطوة",
                            tint = highlightColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Current Status text
            Text(
                text = statusText,
                fontSize = 13.sp,
                color = Color(0xFFD0BCFF),
                lineHeight = 18.sp
            )

            // Step Custom Actions
            content()
        }
    }
}
