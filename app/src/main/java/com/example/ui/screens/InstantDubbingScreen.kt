package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.components.AudioWaveformTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.DetectedGender
import com.example.audio.InstantDubbingMode
import com.example.ui.DubbingViewModel
import com.example.ui.components.AudioWaveformVisualizer
import kotlinx.coroutines.launch

@Composable
fun InstantDubbingScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val genderAnalysis by viewModel.genderAnalysisResult.collectAsStateWithLifecycle()
    val instantConfig by viewModel.instantConfig.collectAsStateWithLifecycle()
    val isInstantPlaying by viewModel.isInstantPlaying.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.genderDetector.isAnalyzing.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var testTakePath by remember { mutableStateOf<String?>(null) }
    var isRecordingTestTake by remember { mutableStateOf(false) }

    // Start live analysis on screen enter, stop on exit
    DisposableEffect(Unit) {
        viewModel.startLiveVoiceAnalysis()
        onDispose {
            viewModel.stopLiveVoiceAnalysis()
            viewModel.stopInstantDubbedTakePlayback()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            // Screen Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD0BCFF),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الدبلجة الفورية والتعرف الصوتي",
                            color = Color(0xFFE6E1E5),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "التعرف الذكي على جنس الصوت وتحويله لحظياً (ذكر ⇄ أنثى)",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                }

                // Restart / Toggle Analysis Button
                IconButton(
                    onClick = {
                        if (isAnalyzing) {
                            viewModel.stopLiveVoiceAnalysis()
                        } else {
                            viewModel.startLiveVoiceAnalysis()
                        }
                    },
                    modifier = Modifier.testTag("toggle_voice_analysis_btn")
                ) {
                    Icon(
                        imageVector = if (isAnalyzing) Icons.Default.Refresh else Icons.Default.GraphicEq,
                        contentDescription = "تحليل",
                        tint = if (isAnalyzing) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                    )
                }
            }
        }

        // Live Voice Gender Detection & Analysis Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "محلل الصوت والترددات الحية",
                                color = Color(0xFFE6E1E5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAnalyzing) Color(0xFF4A4458) else Color(0xFF36343B)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (isAnalyzing) Color(0xFFA6D4A8) else Color(0xFF938F99),
                                            CircleShape
                                        )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isAnalyzing) "الميكروفون نشط" else "متوقف",
                                    color = if (isAnalyzing) Color(0xFFA6D4A8) else Color(0xFFCAC4D0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Gender Identification Highlight Hero Box
                    val genderColor by animateColorAsState(
                        targetValue = Color(genderAnalysis.detectedGender.colorHex),
                        label = "genderColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF211F26),
                        border = BorderStroke(1.dp, genderColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            // Animated Gender Avatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .scale(if (genderAnalysis.rmsLevel > 0.1f) pulseScale else 1.0f)
                                    .background(genderColor.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text(
                                    text = genderAnalysis.detectedGender.emoji,
                                    fontSize = 28.sp
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = genderAnalysis.detectedGender.titleArabic,
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (genderAnalysis.confidence > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = genderColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${genderAnalysis.confidence}% دقة",
                                                color = genderColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = genderAnalysis.detectedGender.descriptionArabic,
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                // Pitch Frequency in Hz
                                if (genderAnalysis.pitchHz > 0f) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "التردد الأساسي (F0): ",
                                            color = Color(0xFF938F99),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${genderAnalysis.pitchHz.toInt()} هرتز (Hz)",
                                            color = Color(0xFFD0BCFF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Audio Signal Waveform
                    AudioWaveformVisualizer(
                        amplitude = genderAnalysis.rmsLevel,
                        isActive = isAnalyzing && genderAnalysis.rmsLevel > 0.04f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Automatic Voice Morphing Modes (التحويل التلقائي ذكي)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر وضع التحويل والدبلجة الفورية:",
                        color = Color(0xFFE6E1E5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتم تطبيق التحويل لحظياً عند التحدث أو تسجيل الدبلجة",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InstantDubbingMode.values().forEach { mode ->
                            val isSelected = instantConfig.mode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF4A4458) else Color(0xFF211F26),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setInstantDubbingMode(mode) }
                                    .testTag("dub_mode_${mode.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(text = mode.iconEmoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.titleArabic,
                                            color = if (isSelected) Color(0xFFFFD993) else Color(0xFFE6E1E5),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = mode.subtitleArabic,
                                            color = Color(0xFFCAC4D0),
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color(0xFF381E72),
                                                    modifier = Modifier.size(12.dp)
                                                )
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

        // Live Voice Transformation Test & Controls Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "اختبار وتسجيل صوت الدبلجة المحوّل",
                                color = Color(0xFFE6E1E5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Calculated Pitch factor
                        val calculatedPitch = viewModel.instantDubbingEngine.calculateEffectivePitch(genderAnalysis.detectedGender)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4A4458)
                        ) {
                            Text(
                                text = "معامل الطبقة: ${String.format("%.2f", calculatedPitch)}x",
                                color = Color(0xFFD0BCFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Audio Waveform with Silent Segment Detection & dB Volume Levels
                    AudioWaveformTrack(
                        waveformHistory = uiState.waveformHistory,
                        currentAmplitude = if (isRecordingTestTake) uiState.liveAmplitude else if (isInstantPlaying) 0.6f else 0.05f,
                        isRecording = isRecordingTestTake,
                        isPlaying = isInstantPlaying,
                        progressFraction = if (isInstantPlaying) 0.5f else 0f,
                        totalDurationSeconds = 10f,
                        onSeek = { /* test playback */ },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Pitch Fine Tuning Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ضبط دقيق لطبقة الصوت (Pitch Shift):",
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${String.format("%.2f", instantConfig.customPitchShift)}x",
                            color = Color(0xFFE6E1E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = instantConfig.customPitchShift,
                        onValueChange = { viewModel.setInstantPitchShift(it) },
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD0BCFF),
                            activeTrackColor = Color(0xFFD0BCFF),
                            inactiveTrackColor = Color(0xFF49454F)
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    // Sample Audio & Test Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Record Real Voice Button
                        Button(
                            onClick = {
                                viewModel.startRecordingCountdown()
                                Toast.makeText(context, "تحدث الآن لتسجيل صوتك الحقيقي واختبار التحويل 🎙️", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("record_instant_dub_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تسجيل صوت حقيقي 🎙️",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Play Transformed Sample Button
                        Button(
                            onClick = {
                                val path = testTakePath ?: uiState.recordedAudioPath
                                if (path != null) {
                                    if (isInstantPlaying) {
                                        viewModel.stopInstantDubbedTakePlayback()
                                    } else {
                                        viewModel.playInstantDubbedTake(path)
                                    }
                                }
                            },
                            enabled = (testTakePath != null || uiState.recordedAudioPath != null) && !isRecordingTestTake,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInstantPlaying) Color(0xFFFFD993) else Color(0xFF4A4458),
                                contentColor = if (isInstantPlaying) Color(0xFF141218) else Color(0xFFE6E1E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("play_transformed_sample_btn")
                        ) {
                            Icon(
                                imageVector = if (isInstantPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isInstantPlaying) "إيقاف" else "استمع للصوت المحوّل",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Apply to Studio Scene button
                    if (testTakePath != null) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                testTakePath?.let { path ->
                                    viewModel.applyInstantTakeToCurrentStudioTake(path)
                                    onNavigateToStudio()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFA6D4A8),
                                contentColor = Color(0xFF0F3818)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("apply_instant_take_to_studio_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "استخدم هذا المقطع في استوديو الدبلجة الحالي 🎬",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Teleprompter helper for current scene
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "حوارات المشهد الحالي للتمرين (${uiState.currentClip.title})",
                                color = Color(0xFFE6E1E5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    uiState.scriptLines.take(3).forEach { line ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF211F26),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(text = line.characterAvatar, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = line.characterName,
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = line.textArabic,
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
