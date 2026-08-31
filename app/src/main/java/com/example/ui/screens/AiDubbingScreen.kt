package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.VoiceProfile
import com.example.ui.DubbingViewModel

@Composable
fun AiDubbingScreen(
    viewModel: DubbingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsStateWithLifecycle()
    val isInitialized by viewModel.ttsManager.isInitialized.collectAsStateWithLifecycle()

    var customTextInput by remember {
        mutableStateOf("مرحباً بكم في استوديو تحويل النصوص إلى كلام واقعي وطبيعي. يمكنكم كتابة أي نص عربي وسيقوم النظام بنطقه بفصاحة وتناغم صوتي عالي الجودة.")
    }
    var selectedProfile by remember {
        mutableStateOf(viewModel.ttsManager.voiceProfiles.first())
    }
    var customPitch by remember { mutableFloatStateOf(selectedProfile.pitch) }
    var customSpeed by remember { mutableFloatStateOf(selectedProfile.speechRate) }
    var showAdvancedTuning by remember { mutableStateOf(false) }

    // Sample Texts for Quick Testing
    val sampleTexts = listOf(
        "🎙️ مقطع وثائقي" to "في أعماق المحيطات الشاسعة، تعيش كائنات غامضة لم ترَ ضوء الشمس منذ ملايين السنين...",
        "🎬 مشهد درامي" to "لقد حان وقت القرار النهائي! لن نتراجع بعد كل التضحيات التي قدمناها من أجل الحقيقة.",
        "✨ حكمة وتحفيز" to "النجاح لا يأتي بالمصادفة، بل هو ثمرة الإصرار والعمل الدؤوب يوماً بعد يوم.",
        "🧒 كرتون ومرح" to "يا رفاق! جهزوا أنفسكم، لدينا اليوم مغامرة أسطورية لا تُنسى في الغابة المسحورة!"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD0BCFF),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تحويل النصوص إلى كلام طبيعي 🎙️",
                        color = Color(0xFFE6E1E5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "محرك نطق فصيح واقعي 100% يعمل بدون إنترنت ومجاناً",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Quick Input Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اكتب أو الصق النص العربي:",
                            color = Color(0xFFE6E1E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Paste Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clipText.isNullOrBlank()) {
                                        customTextInput = clipText
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "لصق",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Copy Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("text", customTextInput)
                                    clipboard?.setPrimaryClip(clip)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customTextInput,
                        onValueChange = { customTextInput = it },
                        placeholder = { Text("أدخل النص المراد تحويله لصوت طبيعي...", color = Color(0xFF938F99)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedTextColor = Color(0xFFE6E1E5),
                            unfocusedTextColor = Color(0xFFE6E1E5),
                            focusedContainerColor = Color(0xFF1F1D24),
                            unfocusedContainerColor = Color(0xFF1F1D24)
                        ),
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tts_text_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    // Quick Sample Chips
                    Text(
                        text = "نصوص جاهزة للتجربة السريعة:",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleTexts.take(2).forEach { (title, content) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF383540),
                                border = BorderStroke(1.dp, Color(0xFF49454F)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customTextInput = content }
                            ) {
                                Text(
                                    text = title,
                                    color = Color(0xFFD0E4FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleTexts.drop(2).forEach { (title, content) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF383540),
                                border = BorderStroke(1.dp, Color(0xFF49454F)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { customTextInput = content }
                            ) {
                                Text(
                                    text = title,
                                    color = Color(0xFFFFD993),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voice Profiles Selection
        item {
            Text(
                text = "اختر المعلق الصوتي والنبرة الطبيعية:",
                color = Color(0xFFE6E1E5),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.ttsManager.voiceProfiles.forEach { profile ->
                    val isSelected = selectedProfile.id == profile.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF433E52) else Color(0xFF24222B),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedProfile = profile
                                customPitch = profile.pitch
                                customSpeed = profile.speechRate
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.25f) else Color(0xFF332F3D),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = profile.emoji, fontSize = 20.sp)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = profile.titleArabic,
                                        color = if (isSelected) Color(0xFFFFD993) else Color(0xFFE6E1E5),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = profile.subtitleArabic,
                                        color = Color(0xFFCAC4D0),
                                        fontSize = 11.sp
                                    )
                                    if (profile.description.isNotBlank()) {
                                        Text(
                                            text = profile.description,
                                            color = Color(0xFF938F99),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // Quick Play Audio Button
                            IconButton(
                                onClick = {
                                    selectedProfile = profile
                                    customPitch = profile.pitch
                                    customSpeed = profile.speechRate
                                    viewModel.ttsManager.speakText(
                                        text = customTextInput,
                                        profile = profile.copy(pitch = customPitch, speechRate = customSpeed)
                                    )
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF3A3644),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "استماع",
                                            tint = if (isSelected) Color(0xFF381E72) else Color(0xFFEADDFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Advanced Voice Fine-Tuning Drawer / Expander
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF24222B)),
                border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedTuning = !showAdvancedTuning },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "التحكم المتقدم بطبقة النبرة وسرعة القراءة",
                                color = Color(0xFFE6E1E5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                customPitch = selectedProfile.pitch
                                customSpeed = selectedProfile.speechRate
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة ضبط",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAdvancedTuning) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            // Pitch Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("طبقة الصوت (Pitch):", color = Color(0xFFCAC4D0), fontSize = 12.sp)
                                Text("${String.format("%.2f", customPitch)}x", color = Color(0xFFD0BCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = customPitch,
                                onValueChange = { customPitch = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD0BCFF),
                                    activeTrackColor = Color(0xFFD0BCFF),
                                    inactiveTrackColor = Color(0xFF49454F)
                                )
                            )

                            // Speed Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("سرعة الإلقاء (Speed):", color = Color(0xFFCAC4D0), fontSize = 12.sp)
                                Text("${String.format("%.2f", customSpeed)}x", color = Color(0xFFD0E4FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = customSpeed,
                                onValueChange = { customSpeed = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD0E4FF),
                                    activeTrackColor = Color(0xFFD0E4FF),
                                    inactiveTrackColor = Color(0xFF49454F)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Primary Audio Actions: Play, Synthesize to File, and Apply to Studio
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play / Stop Master Button
                Button(
                    onClick = {
                        if (isSpeaking) {
                            viewModel.ttsManager.stop()
                        } else {
                            viewModel.ttsManager.speakText(
                                text = customTextInput,
                                profile = selectedProfile.copy(pitch = customPitch, speechRate = customSpeed)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                        contentColor = if (isSpeaking) Color(0xFF601410) else Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("tts_play_master_button")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isSpeaking) Color(0xFF601410) else Color(0xFF381E72),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isSpeaking) "إيقاف القراءة الصوتية" else "نطق النص بصوت طبيعي 🎧",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Action Row: Save Audio File / Apply to Scene
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export/Save Audio Button
                    Button(
                        onClick = {
                            viewModel.synthesizeTextToDubbingTake(
                                customTextInput,
                                selectedProfile.copy(pitch = customPitch, speechRate = customSpeed)
                            )
                        },
                        enabled = !state.isGeneratingAiDub,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A4458),
                            contentColor = Color(0xFFEADDFF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("tts_export_button")
                    ) {
                        if (state.isGeneratingAiDub) {
                            CircularProgressIndicator(
                                color = Color(0xFFEADDFF),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFFEADDFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "اعتماد كدبلجة للمشهد 🎬",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Share Dubbed Take if Available
                    Button(
                        onClick = { viewModel.shareProject() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF383540),
                            contentColor = Color(0xFFD0E4FF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("tts_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFFD0E4FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "مشاركة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Full Script Auto-Dubbing for Current Movie Clip
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF26232D)),
                border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "دبلجة سيناريو المشهد الحالي آلياً",
                                color = Color(0xFFE6E1E5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.currentClip.title} (${state.scriptLines.size} أسطر حوارية)",
                                color = Color(0xFFD0BCFF),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.generateAiTtsDubbing() },
                            enabled = !state.isGeneratingAiDub,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF381E72),
                                contentColor = Color(0xFFD0BCFF)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isGeneratingAiDub) {
                                CircularProgressIndicator(
                                    color = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("دبلجة المشهد كاملاً", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(30.dp))
        }
    }
}
