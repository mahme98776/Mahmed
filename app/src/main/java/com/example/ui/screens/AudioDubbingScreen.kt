package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioDubbingTrackMode
import com.example.audio.DemoAudioPreset
import com.example.audio.DubbingDialect
import com.example.audio.VoiceProfile
import com.example.ui.DubbingViewModel
import com.example.ui.components.AudioWaveformVisualizer
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun AudioDubbingScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioDubbingManager = viewModel.audioDubbingManager
    val state by audioDubbingManager.state.collectAsStateWithLifecycle()

    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var savedStoragePath by remember { mutableStateOf<String?>(null) }
    var isSavingToStorage by remember { mutableStateOf(false) }
    var showDemoPickerSheet by remember { mutableStateOf(false) }

    // Audio File Picker Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "audio_input.wav"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                }
            } catch (_: Exception) {}
            audioDubbingManager.loadAudioFromUri(it, fileName)
            Toast.makeText(context, "تم تحميل المقطع: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioDubbingManager.stopPlayback()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF090D16)
                    )
                )
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner & Title
        item {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_dubbing_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7))))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF818CF8)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "استوديو دبلجة الصوت 🎙️",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "تحويل واستبدال الأصوات واللهجات بالذكاء الاصطناعي",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "AI Voice Engine",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "يمكنك دبلجة أي تسجيل صوتي، ملف MP3/WAV، أو مقطع كلامي إلى كافة اللهجات العربية والأصوات السينمائية مع ضبط النبرة والسرعة والمؤثرات.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 2. Audio Input Selection Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. مصدر الملف الصوتي الأصلي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_import_audio_file"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("استيراد ملف صوتي 📂", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDemoPickerSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pick_demo_audio"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("نماذج صوتية جاهزة 🎵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display Current Input Details
                    if (state.inputAudioPath != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = state.inputAudioFileName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", state.inputAudioDurationMs / 1000f)} ثانية",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4ADE80),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (state.inputWaveformAmplitudes.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    AudioWaveformVisualizer(
                                        amplitude = if (state.isPlaying && state.activeTrackMode == AudioDubbingTrackMode.ORIGINAL) 0.85f else 0.2f,
                                        isActive = state.isPlaying && state.activeTrackMode == AudioDubbingTrackMode.ORIGINAL,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Transcript & Dubbing Text Editor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. نص الكلام والدبلجة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF38BDF8)
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val pastedText = clip.getItemAt(0).text.toString()
                                        audioDubbingManager.setDubbedTranscript(pastedText)
                                        Toast.makeText(context, "تم لصق النص", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "لصق", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Dubbed Script", state.dubbedTranscript)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.dubbedTranscript,
                        onValueChange = { audioDubbingManager.setDubbedTranscript(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dubbed_script_text"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = {
                            Text("اكتب النص المراد نطقه ودبلجته بالصوت الجديد...", color = Color(0xFF64748B), fontSize = 12.sp)
                        },
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }
        }

        // 4. Dialect & Voice Profile Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. اختيار اللهجة ونبرة الدبلجة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(Modifier.height(10.dp))

                    // Dialect Chips
                    Text("اللهجة المستهدفة:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                    Spacer(Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(audioDubbingManager.supportedDialects) { dialect ->
                            val isSelected = state.targetDialect == dialect
                            FilterChip(
                                selected = isSelected,
                                onClick = { audioDubbingManager.setTargetDialect(dialect) },
                                label = {
                                    Text(
                                        text = "${dialect.flagEmoji} ${dialect.displayNameArabic.substringBefore("(").trim()}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6366F1),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFFCBD5E1)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF475569),
                                    selectedBorderColor = Color(0xFF818CF8)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Voice Casting / Characters
                    Text("شخصية الصوت والممثل الصوتي:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(audioDubbingManager.supportedVoiceProfiles) { profile ->
                            val isSelected = state.selectedVoiceProfile.id == profile.id
                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { audioDubbingManager.setVoiceProfile(profile) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF312E81) else Color(0xFF0F172A)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFA855F7) else Color(0xFF334155)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(profile.emoji, fontSize = 20.sp)
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = profile.titleArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = profile.subtitleArabic,
                                        fontSize = 9.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Studio Sound Customization (Pitch, Speed, Reverb, Noise Reduction)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. هندسة الصوت والمؤثرات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(Modifier.height(12.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("نبرة الصوت (Pitch):", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                        Text("${String.format(Locale.US, "%.2f", state.customPitchModifier)}x", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Slider(
                        value = state.customPitchModifier,
                        onValueChange = { audioDubbingManager.setPitchModifier(it) },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7),
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )

                    // Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سرعة الإلقاء (Speed):", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                        Text("${String.format(Locale.US, "%.2f", state.customSpeedModifier)}x", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                    }
                    Slider(
                        value = state.customSpeedModifier,
                        onValueChange = { audioDubbingManager.setSpeedModifier(it) },
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFA855F7),
                            activeTrackColor = Color(0xFF7E22CE),
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Noise Reduction & Reverb Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("عزل الضوضاء بالذكاء الاصطناعي (AI Denoiser)", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("إزالة الشوشرة وتنقية ترددات الصوت", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = state.enableNoiseReduction,
                            onCheckedChange = { audioDubbingManager.toggleNoiseReduction(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF22C55E),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("صدى استوديو درامي (Studio Reverb)", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("إعطاء عمق سينمائي لمشاهد الأكشن والوثائقي", fontSize = 9.5.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = state.enableStudioReverb,
                            onCheckedChange = { audioDubbingManager.toggleStudioReverb(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFA855F7),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        // 6. Action Button: Start Dubbing
        item {
            if (state.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (state.isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF6366F1))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF818CF8),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = state.processingStage.ifBlank { "جاري معالجة الصوت..." },
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.processingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFA855F7),
                            trackColor = Color(0xFF334155)
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        audioDubbingManager.startAudioDubbing { success ->
                            if (success) {
                                Toast.makeText(context, "تمت دبلجة الصوت بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_execute_audio_dubbing"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("بدء دبلجة الصوت بالذكاء الاصطناعي 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 7. A/B Audio Player & Multi-Track Studio
        if (state.dubbedAudioPath != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_dubbed_player_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("المقطع الصوتي المدبلج جاهز ✨", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
                            }

                            Text(
                                text = "${String.format(Locale.US, "%.1f", state.dubbedAudioDurationMs / 1000f)} ثانية",
                                fontSize = 11.5.sp,
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Track Selector (Original vs Dubbed vs Mixed)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AudioDubbingTrackMode.entries.forEach { mode ->
                                val isSelected = state.activeTrackMode == mode
                                Button(
                                    onClick = { audioDubbingManager.setTrackMode(mode) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF22C55E) else Color(0xFF1E293B)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = mode.titleArabic,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Waveform for Dubbed Track
                        if (state.dubbedWaveformAmplitudes.isNotEmpty()) {
                            AudioWaveformVisualizer(
                                amplitude = if (state.isPlaying && (state.activeTrackMode == AudioDubbingTrackMode.DUBBED || state.activeTrackMode == AudioDubbingTrackMode.MIXED)) 0.9f else 0.25f,
                                isActive = state.isPlaying && (state.activeTrackMode == AudioDubbingTrackMode.DUBBED || state.activeTrackMode == AudioDubbingTrackMode.MIXED),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Playback Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { audioDubbingManager.seekToPosition(0L) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "إعادة", tint = Color(0xFF94A3B8))
                            }

                            Spacer(Modifier.width(12.dp))

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF22C55E),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable { audioDubbingManager.togglePlayback() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (state.isPlaying) "إيقاف" else "تشغيل",
                                        tint = Color.Black,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    val newMode = if (state.activeTrackMode == AudioDubbingTrackMode.ORIGINAL) AudioDubbingTrackMode.DUBBED else AudioDubbingTrackMode.ORIGINAL
                                    audioDubbingManager.setTrackMode(newMode)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل A/B", tint = Color(0xFF38BDF8))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Studio Integration & Export Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    state.dubbedAudioPath?.let { path ->
                                        viewModel.applyDubbedAudioToProject(path)
                                        Toast.makeText(context, "تم إرسال الصوت المدبلج للاستوديو بنجاح! 🎬", Toast.LENGTH_SHORT).show()
                                        onNavigateToStudio()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("إرسال للاستوديو 🎬", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    state.dubbedAudioPath?.let { path ->
                                        isSavingToStorage = true
                                        viewModel.saveDubbedAudioFileToStorage(
                                            audioPath = path,
                                            suggestedTitle = "دبلجة_${state.selectedVoiceProfile.id}_${System.currentTimeMillis() % 10000}"
                                        ) { res ->
                                            isSavingToStorage = false
                                            if (res is com.example.export.ExportResult.Success) {
                                                savedStoragePath = res.filePath
                                                exportedFilePath = path
                                                showExportSuccessDialog = true
                                            }
                                        }
                                    }
                                },
                                enabled = !isSavingToStorage,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                if (isSavingToStorage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("حفظ في الذاكرة 💾", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    state.dubbedAudioPath?.let { path ->
                                        exportedFilePath = path
                                        showExportSuccessDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("مشاركة 📤", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
        }
    }

    // Demo Audio Presets Dialog
    if (showDemoPickerSheet) {
        AlertDialog(
            onDismissRequest = { showDemoPickerSheet = false },
            title = {
                Text("اختر نموذجاً صوتياً جاهزاً للدبلجة ✨", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    audioDubbingManager.demoAudioPresets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioDubbingManager.loadDemoPreset(preset)
                                    showDemoPickerSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(preset.titleArabic, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF38BDF8))
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = preset.sampleText,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showDemoPickerSheet = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Export & Share Dialog
    if (showExportSuccessDialog && exportedFilePath != null) {
        val file = File(exportedFilePath!!)
        AlertDialog(
            onDismissRequest = { 
                showExportSuccessDialog = false 
                savedStoragePath = null
            },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("تم تجهيز وحفظ الملف الصوتي! 💾", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اسم الملف: ${file.name}", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                    if (savedStoragePath != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("محفوظ في وحدة التخزين:\n$savedStoragePath", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Text("حجم الملف: ${(file.length() / 1024)} KB", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                    Text("جودة الصوت: 48kHz Stereo Mastered", fontSize = 11.5.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (savedStoragePath == null) {
                        Button(
                            onClick = {
                                isSavingToStorage = true
                                viewModel.saveDubbedAudioFileToStorage(
                                    audioPath = exportedFilePath!!,
                                    suggestedTitle = "دبلجة_${state.selectedVoiceProfile.id}_${System.currentTimeMillis() % 10000}"
                                ) { res ->
                                    isSavingToStorage = false
                                    if (res is com.example.export.ExportResult.Success) {
                                        savedStoragePath = res.filePath
                                    }
                                }
                            },
                            enabled = !isSavingToStorage,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            if (isSavingToStorage) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("حفظ محلياً 💾", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/wav"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الصوت المدبلج"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "الملف جاهز في مجلد التطبيق: ${file.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مشاركة 📤", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showExportSuccessDialog = false 
                    savedStoragePath = null
                }) {
                    Text("تم")
                }
            }
        )
    }
}
