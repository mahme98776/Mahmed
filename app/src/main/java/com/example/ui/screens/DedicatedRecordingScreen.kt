package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.VoiceEffect
import com.example.ui.DubbingViewModel
import com.example.ui.components.VoicePresetType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sin

/**
 * Dedicated Recording Screen with Real-Time Waveform Visualizer (Compose Canvas)
 * and Start/Stop Microphone Input Controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DedicatedRecordingScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Microphone Permission Handling
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "يلزم منح إذن الميكروفون لبدء التسجيل 🎙️", Toast.LENGTH_LONG).show()
        }
    }

    // Recording Session Local States
    val isRecording = uiState.isRecording
    val liveAmp = uiState.liveAmplitude
    val waveformHistory = uiState.waveformHistory
    val recordedAudioPath = uiState.recordedAudioPath

    // Local duration timer
    var recordingDurationSeconds by remember { mutableFloatStateOf(0f) }
    var isPlaybackPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var selectedEffect by remember { mutableStateOf(VoiceEffect.NORMAL) }
    var micGainBoost by remember { mutableFloatStateOf(1.2f) }
    var enableNoiseGate by remember { mutableStateOf(true) }
    var teleprompterText by remember {
        mutableStateOf("أيها الأبطال! حان وقت الدفاع عن العدالة والنور.. لن نستسلم أبداً مهما اشتدت الصعاب!")
    }
    var showTeleprompter by remember { mutableStateOf(true) }

    // Update timer during recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSeconds = 0f
            while (isActive && isRecording) {
                delay(50)
                recordingDurationSeconds += 0.05f
            }
        }
    }

    // Cleanup on screen exit
    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.uiState.value.isRecording) {
                viewModel.stopRecording()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRecording) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "كابينة التسجيل المباشر 🎙️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRecording) "🔴 جارٍ التسجيل الصوتي الحي..." else "جاهز لتسجيل صوت الدبلجة بدقة عالية",
                                fontSize = 11.sp,
                                color = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHelp,
                        modifier = Modifier.testTag("recording_help_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "دليل الاستخدام",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission Alert Banner (if permission not granted)
            if (!hasMicPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("يلزم إذن الوصول إلى الميكروفون", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("اضغط على الزر لتفعيل الميكروفون وبدء التسجيل والمزامنة.", color = Color(0xFFFCA5A5), fontSize = 11.5.sp)
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("سماح", color = Color(0xFF7F1D1D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Real-Time Waveform Visualizer Card (Compose Canvas)
            RealTimeWaveformCanvasCard(
                isRecording = isRecording,
                liveAmplitude = (liveAmp * micGainBoost).coerceIn(0f, 1f),
                waveformHistory = waveformHistory,
                durationSeconds = recordingDurationSeconds,
                enableNoiseGate = enableNoiseGate
            )

            // Primary Start/Stop Microphone Recording Button
            PrimaryRecordingActionControls(
                isRecording = isRecording,
                hasMicPermission = hasMicPermission,
                onStartRecording = {
                    if (!hasMicPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.startRecordingCountdown()
                    }
                },
                onStopRecording = {
                    viewModel.stopRecording()
                    Toast.makeText(context, "تم حفظ المقطع الصوتي بنجاح! 🎙️✨", Toast.LENGTH_SHORT).show()
                }
            )

            // Post-Recording Playback & Actions (if audio file exists)
            if (recordedAudioPath != null && File(recordedAudioPath).exists()) {
                RecordedAudioPlayerCard(
                    audioPath = recordedAudioPath,
                    selectedEffect = selectedEffect,
                    onSelectEffect = { effect ->
                        selectedEffect = effect
                        viewModel.setVoiceEffect(effect)
                    },
                    onUseInStudio = {
                        onNavigateToStudio()
                        Toast.makeText(context, "تم تطبيق الصوت في استوديو الدبلجة بنجاح 🎬", Toast.LENGTH_SHORT).show()
                    },
                    onRetake = {
                        viewModel.startRecordingCountdown()
                    }
                )
            }

            // Live Teleprompter / Script Prompter
            ScriptPrompterSection(
                teleprompterText = teleprompterText,
                onTextChange = { teleprompterText = it },
                showTeleprompter = showTeleprompter,
                onToggleTeleprompter = { showTeleprompter = !showTeleprompter }
            )

            // Live Mic Audio Parameters & Filters
            MicAudioParametersCard(
                micGainBoost = micGainBoost,
                onGainChange = { micGainBoost = it },
                enableNoiseGate = enableNoiseGate,
                onNoiseGateToggle = { enableNoiseGate = it }
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Compose Canvas Real-Time Waveform Visualizer
 */
@Composable
fun RealTimeWaveformCanvasCard(
    isRecording: Boolean,
    liveAmplitude: Float,
    waveformHistory: List<Float>,
    durationSeconds: Float,
    enableNoiseGate: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_glow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Calculate decibels from amplitude
    val effectiveAmp = if (enableNoiseGate && liveAmplitude < 0.04f) 0.01f else liveAmplitude
    val db = if (effectiveAmp > 0.001f) (20 * log10(effectiveAmp)).coerceIn(-60f, 0f) else -60f
    val isClipping = liveAmplitude > 0.88f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("waveform_canvas_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E17)),
        border = BorderStroke(
            1.5.dp,
            if (isRecording) {
                if (isClipping) Color(0xFFEF4444) else Color(0xFF8B5CF6)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Status, Clipping Alert, and Duration Timecode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isRecording) Color(0xFFEF4444) else Color(0xFF6B7280),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = if (isRecording) "LIVE INPUT" else "STANDBY",
                        color = if (isRecording) Color(0xFFF87171) else Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (isClipping && isRecording) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, Color(0xFFEF4444))
                        ) {
                            Text(
                                text = "OVERLOAD / CLIP",
                                color = Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Digital SMPTE-style Timer Display
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1B4B),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
                ) {
                    val minutes = (durationSeconds / 60).toInt()
                    val seconds = (durationSeconds % 60).toInt()
                    val millis = ((durationSeconds * 100) % 100).toInt()
                    val timeStr = String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, millis)

                    Text(
                        text = timeStr,
                        color = if (isRecording) Color(0xFF38BDF8) else Color(0xFFA5B4FC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Real-Time Canvas Waveform Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF090814))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("realtime_waveform_canvas")
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // 1. Draw Background Grid Lines
                    val gridSteps = 4
                    for (i in 1..gridSteps) {
                        val y = (h / (gridSteps + 1)) * i
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw Center Zero Line
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.4f),
                        start = Offset(0f, centerY),
                        end = Offset(w, centerY),
                        strokeWidth = 1.5f
                    )

                    // 3. Draw Historical Moving Waveform Bars
                    val bars = 36
                    val barWidth = (w / bars) * 0.65f
                    val barSpacing = (w / bars) * 0.35f

                    val history = waveformHistory.takeLast(bars)
                    for (i in 0 until bars) {
                        val x = i * (barWidth + barSpacing) + (barWidth / 2)
                        val amp = if (i < history.size) history[i] else (0.05f + 0.03f * sin(phase + i * 0.3f))
                        val barHeight = (amp * (h * 0.75f)).coerceAtLeast(4f)

                        val barBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC084FC),
                                Color(0xFF38BDF8),
                                Color(0xFFF59E0B)
                            ),
                            startY = centerY - barHeight / 2,
                            endY = centerY + barHeight / 2
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x - barWidth / 2, centerY - barHeight / 2),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }

                    // 4. Draw Smooth Continuous Sine Wave Envelope (Real-time Overlay)
                    val path = Path()
                    val points = 60
                    var isFirst = true

                    for (i in 0..points) {
                        val px = (w / points) * i
                        val norm = i.toFloat() / points
                        val waveOffset = sin(norm * 12f + phase) * (liveAmplitude * (h * 0.35f))
                        val py = centerY + waveOffset

                        if (isFirst) {
                            path.moveTo(px, py)
                            isFirst = false
                        } else {
                            path.lineTo(px, py)
                        }
                    }

                    // Draw Glowing Neon Stroke
                    drawPath(
                        path = path,
                        color = if (isRecording) Color(0xFF00E5FF).copy(alpha = 0.85f) else Color(0xFF6366F1).copy(alpha = 0.4f),
                        style = Stroke(
                            width = if (isRecording) 3.5f else 1.5f,
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Ambient Center Icon Watermark
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isRecording && waveformHistory.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MicNone,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("اضغط على زر التسجيل أدناه لبدء التقاط الصوت", color = Color(0xFF64748B), fontSize = 11.sp)
                        }
                    }
                }
            }

            // dBFS Level Meter & Decibel Reading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL: ${String.format(Locale.US, "%.1f", db)} dBFS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = when {
                        db > -3f -> Color(0xFFEF4444)
                        db > -12f -> Color(0xFFFBBF24)
                        else -> Color(0xFF10B981)
                    }
                )

                // Multi-Segment LED Level Bar (Compose Canvas)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val progressWidth = size.width * ((db + 60f) / 60f).coerceIn(0f, 1f)
                        val meterBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF10B981), // Safe Green (-60 to -18 dB)
                                Color(0xFFFBBF24), // Optimal Amber (-18 to -6 dB)
                                Color(0xFFEF4444)  // Clipping Red (-6 to 0 dB)
                            )
                        )
                        drawRoundRect(
                            brush = meterBrush,
                            topLeft = Offset.Zero,
                            size = Size(progressWidth, size.height),
                            cornerRadius = CornerRadius(5f, 5f)
                        )
                    }
                }

                Text(
                    text = if (isRecording) "🔴 REC" else "PAUSED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Color(0xFFEF4444) else Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * Primary Recording Controls with High Touch-Target Start/Stop Button
 */
@Composable
fun PrimaryRecordingActionControls(
    isRecording: Boolean,
    hasMicPermission: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_button_pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 78f,
        targetValue = 92f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_size"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(104.dp)
        ) {
            // Animated Outer Glowing Ring when recording
            if (isRecording) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF4444).copy(alpha = 0.25f),
                    modifier = Modifier.size(pulseSize.dp)
                ) {}
            }

            // Primary Start/Stop Circular Button
            Surface(
                shape = CircleShape,
                color = if (isRecording) Color(0xFFEF4444) else Color(0xFF7C3AED),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isRecording) {
                            onStopRecording()
                        } else {
                            onStartRecording()
                        }
                    }
                    .testTag("start_stop_recording_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isRecording) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "إيقاف التسجيل",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "بدء التسجيل",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }

        // Action Text Label
        Text(
            text = if (isRecording) "اضغط لإيقاف وحفظ مقطع الصوت ⏹️" else "اضغط على الميكروفون لبدء التسجيل 🎙️",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Recorded Audio Player & Effects Selector Card
 */
@Composable
fun RecordedAudioPlayerCard(
    audioPath: String,
    selectedEffect: VoiceEffect,
    onSelectEffect: (VoiceEffect) -> Unit,
    onUseInStudio: () -> Unit,
    onRetake: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recorded_audio_player_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Text("تم التقاط مقطع صوتي بنجاح", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text("جاهز للمكساج", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            // Voice Effects Quick Presets Filter Row
            Text("✨ مؤثرات الدبلجة الصوتية الفورية (DSP):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            val effects = listOf(
                Pair(VoiceEffect.NORMAL, "صوت طبيعي"),
                Pair(VoiceEffect.DEEP, "سينمائي ضخم 🎬"),
                Pair(VoiceEffect.CHIPMUNK, "كرتون ومرح 🐿️"),
                Pair(VoiceEffect.ROBOT, "روبوت آلي 🤖"),
                Pair(VoiceEffect.ECHO, "استوديو وصدى 🎙️"),
                Pair(VoiceEffect.FEMALE_VOICE, "صوت أنثوي ✨")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(effects) { (effect, label) ->
                    FilterChip(
                        selected = selectedEffect == effect,
                        onClick = { onSelectEffect(effect) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Quick Actions: Use in Studio, Retake
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUseInStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("apply_to_studio_button")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تطبيق بالاستوديو 🎬", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRetake,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("إعادة تسجيل", fontSize = 11.5.sp)
                }
            }
        }
    }
}

/**
 * Teleprompter / Script Guide Box
 */
@Composable
fun ScriptPrompterSection(
    teleprompterText: String,
    onTextChange: (String) -> Unit,
    showTeleprompter: Boolean,
    onToggleTeleprompter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                    Text("نص الإلقاء والقراءة (Teleprompter)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
                Switch(
                    checked = showTeleprompter,
                    onCheckedChange = { onToggleTeleprompter() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF59E0B))
                )
            }

            if (showTeleprompter) {
                OutlinedTextField(
                    value = teleprompterText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teleprompter_input"),
                    placeholder = { Text("اكتب أو الصق النص الذي تريد دبلجته وقراءته هنا...") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B)
                    )
                )
            }
        }
    }
}

/**
 * Microphone Audio Parameters (Gain, Noise Gate)
 */
@Composable
fun MicAudioParametersCard(
    micGainBoost: Float,
    onGainChange: (Float) -> Unit,
    enableNoiseGate: Boolean,
    onNoiseGateToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text("إعدادات معالجة الميكروفون المباشرة", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }

            // Gain Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مستوى تضخيم حساسية الميكروفون (Gain):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(micGainBoost * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = micGainBoost,
                    onValueChange = onGainChange,
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Noise Gate Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("بوابة عزل الضوضاء التلقائية (Noise Gate)", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    Text("تجاهل همسات وأصوات الخلفية أثناء الصمت", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = enableNoiseGate,
                    onCheckedChange = onNoiseGateToggle
                )
            }
        }
    }
}
