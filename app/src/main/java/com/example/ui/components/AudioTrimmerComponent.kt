package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modern Visual Audio Trimmer Dialog with Waveform Display
 */
@Composable
fun AudioTrimmerDialog(
    filePath: String,
    waveform: List<Float>,
    durationSeconds: Float,
    startSeconds: Float,
    endSeconds: Float,
    isPlaying: Boolean,
    playbackSeconds: Float,
    isLooping: Boolean,
    isLoading: Boolean,
    isProcessingTrim: Boolean,
    formatName: String = "M4A",
    silenceStartSeconds: Float = 0f,
    silenceEndSeconds: Float = durationSeconds,
    onStartChange: (Float) -> Unit,
    onEndChange: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLoop: () -> Unit,
    onSeek: (Float) -> Unit,
    onPickOtherAudio: () -> Unit,
    onApplyTrim: (Float, Float) -> Unit,
    onSaveAsNewFile: ((Float, Float) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val fileName = remember(filePath) {
        File(filePath).name.ifEmpty { "audio_track.m4a" }
    }

    val selectedDuration = (endSeconds - startSeconds).coerceAtLeast(0f)

    Dialog(
        onDismissRequest = { if (!isProcessingTrim) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 620.dp)
                .padding(vertical = 16.dp)
                .testTag("audio_trimmer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
            border = BorderStroke(1.dp, Color(0xFF49454F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "قص وضبط الملف الصوتي",
                                color = Color(0xFFE6E1E5),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "حدد المقطع المطلوب استخدامه في الدبلجة",
                                color = Color(0xFFCAC4D0),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessingTrim,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // File Info Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2B2930),
                    border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color(0xFF80CBC4),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = fileName,
                                color = Color(0xFFE6E1E5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF381E72)
                            ) {
                                Text(
                                    text = formatName,
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF141218)
                            ) {
                                Text(
                                    text = formatTime(durationSeconds),
                                    color = Color(0xFFFFD993),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Visual Waveform Display with Interactive Trimming Handles
                InteractiveWaveformTrimmer(
                    waveform = waveform,
                    durationSeconds = durationSeconds,
                    startSeconds = startSeconds,
                    endSeconds = endSeconds,
                    playbackSeconds = playbackSeconds,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    onStartChange = onStartChange,
                    onEndChange = onEndChange,
                    onSeek = onSeek
                )

                // Time Code Cards (Start, End, Selected Duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Time Box
                    TimeIndicatorBox(
                        title = "البداية ⏱️",
                        timeSeconds = startSeconds,
                        badgeColor = Color(0xFF4FD1C5),
                        modifier = Modifier.weight(1f)
                    )

                    // Selected Duration Box (Center Highlight)
                    TimeIndicatorBox(
                        title = "المدة المحددة ✂️",
                        timeSeconds = selectedDuration,
                        badgeColor = Color(0xFFFFD993),
                        modifier = Modifier.weight(1.2f),
                        isPrimary = true
                    )

                    // End Time Box
                    TimeIndicatorBox(
                        title = "النهاية ⏱️",
                        timeSeconds = endSeconds,
                        badgeColor = Color(0xFFF472B6),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fine-tuning Steppers Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF25232A),
                    border = BorderStroke(1.dp, Color(0xFF383540)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Fine Tuning
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "دقة البداية:",
                                color = Color(0xFF4FD1C5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                StepButton(label = "-0.5s") {
                                    onStartChange((startSeconds - 0.5f).coerceAtLeast(0f))
                                }
                                StepButton(label = "-0.1s") {
                                    onStartChange((startSeconds - 0.1f).coerceAtLeast(0f))
                                }
                                StepButton(label = "+0.1s") {
                                    onStartChange((startSeconds + 0.1f).coerceAtMost(endSeconds - 0.2f))
                                }
                                StepButton(label = "+0.5s") {
                                    onStartChange((startSeconds + 0.5f).coerceAtMost(endSeconds - 0.2f))
                                }
                            }
                        }

                        // End Fine Tuning
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "دقة النهاية:",
                                color = Color(0xFFF472B6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                StepButton(label = "-0.5s") {
                                    onEndChange((endSeconds - 0.5f).coerceAtLeast(startSeconds + 0.2f))
                                }
                                StepButton(label = "-0.1s") {
                                    onEndChange((endSeconds - 0.1f).coerceAtLeast(startSeconds + 0.2f))
                                }
                                StepButton(label = "+0.1s") {
                                    onEndChange((endSeconds + 0.1f).coerceAtMost(durationSeconds))
                                }
                                StepButton(label = "+0.5s") {
                                    onEndChange((endSeconds + 0.5f).coerceAtMost(durationSeconds))
                                }
                            }
                        }
                    }
                }

                // Quick Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(
                        label = "الكل",
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onStartChange(0f)
                            onEndChange(durationSeconds)
                        }
                    )

                    PresetChip(
                        label = "قص السكوت تلقائياً ⚡",
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1.5f),
                        highlight = true,
                        onClick = {
                            if (silenceEndSeconds > silenceStartSeconds) {
                                onStartChange(silenceStartSeconds)
                                onEndChange(silenceEndSeconds)
                            }
                        }
                    )

                    PresetChip(
                        label = "أول 5 ثواني",
                        modifier = Modifier.weight(1.2f),
                        onClick = {
                            onStartChange(0f)
                            onEndChange(5f.coerceAtMost(durationSeconds))
                        }
                    )
                }

                // Audio Playback & Preview Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Play/Pause Button
                            Surface(
                                shape = CircleShape,
                                color = if (isPlaying) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                                modifier = Modifier.size(44.dp)
                            ) {
                                IconButton(
                                    onClick = onTogglePlay,
                                    modifier = Modifier.fillMaxSize().testTag("trimmer_play_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف" else "معاينة المقطع المقصوص",
                                        tint = if (isPlaying) Color(0xFF601410) else Color(0xFF381E72),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isPlaying) "جارٍ الاستماع للمقطع المحدد..." else "معاينة المقطع المقصوص",
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${formatTime(playbackSeconds)} / ${formatTime(selectedDuration)}",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Loop toggle button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLooping) Color(0xFFD0BCFF).copy(alpha = 0.25f) else Color(0xFF1E1B24),
                            border = BorderStroke(1.dp, if (isLooping) Color(0xFFD0BCFF) else Color(0xFF49454F))
                        ) {
                            IconButton(
                                onClick = onToggleLoop,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "تكرار المعاينة",
                                    tint = if (isLooping) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Action Buttons (Pick Other Audio / Save as File / Trim & Apply to Dubbing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickOtherAudio,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF80CBC4)),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(48.dp)
                            .testTag("trimmer_pick_other_button"),
                        enabled = !isProcessingTrim
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "ملف آخر",
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onSaveAsNewFile != null) {
                        OutlinedButton(
                            onClick = { onSaveAsNewFile(startSeconds, endSeconds) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD993)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2E2419)),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("trimmer_save_file_button"),
                            enabled = !isProcessingTrim && selectedDuration > 0.3f
                        ) {
                            Text(text = "💾", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "حفظ كملف",
                                color = Color(0xFFFFD993),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onApplyTrim(startSeconds, endSeconds) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("trimmer_apply_button"),
                        enabled = !isProcessingTrim && selectedDuration > 0.3f
                    ) {
                        if (isProcessingTrim) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF381E72),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("جارٍ القص...", color = Color(0xFF381E72), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "قص وحفظ بالدبلجة",
                                color = Color(0xFF381E72),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Waveform Component with visual draggable trim handles & excluded regions shading
 */
@Composable
fun InteractiveWaveformTrimmer(
    waveform: List<Float>,
    durationSeconds: Float,
    startSeconds: Float,
    endSeconds: Float,
    playbackSeconds: Float,
    isPlaying: Boolean,
    isLoading: Boolean,
    onStartChange: (Float) -> Unit,
    onEndChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayWaveform = remember(waveform) {
        if (waveform.isEmpty()) {
            List(70) { 0.25f }
        } else {
            waveform
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141218)),
        border = BorderStroke(1.dp, Color(0xFF383540)),
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("interactive_waveform_trimmer")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val safeDuration = durationSeconds.coerceAtLeast(0.5f)

            // Start & End fractions (0.0 to 1.0)
            val startFraction = (startSeconds / safeDuration).coerceIn(0f, 1f)
            val endFraction = (endSeconds / safeDuration).coerceIn(0f, 1f)
            val playheadFraction = ((startSeconds + playbackSeconds) / safeDuration).coerceIn(0f, 1f)

            val startPx = startFraction * widthPx
            val endPx = endFraction * widthPx
            val playheadPx = playheadFraction * widthPx

            // Waveform Canvas Renderer
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(safeDuration) {
                        detectTapGestures { offset ->
                            val tappedFrac = (offset.x / size.width).coerceIn(0f, 1f)
                            val tappedSeconds = tappedFrac * safeDuration
                            if (tappedSeconds in startSeconds..endSeconds) {
                                onSeek(tappedSeconds - startSeconds)
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerY = canvasHeight / 2f

                // Draw Background Centerline
                drawLine(
                    color = Color(0xFF2B2930),
                    start = Offset(0f, centerY),
                    end = Offset(canvasWidth, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                val barCount = displayWaveform.size
                val spacing = 2.dp.toPx()
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceIn(2.dp.toPx(), 8.dp.toPx())

                // Draw Waveform Bars
                displayWaveform.forEachIndexed { i, amp ->
                    val x = i * (barWidth + spacing)
                    val sampleProgress = i.toFloat() / barCount.toFloat()

                    val isInsideSelection = sampleProgress in startFraction..endFraction
                    val isPastPlayhead = sampleProgress <= playheadFraction && isInsideSelection

                    val ampMultiplier = if (isPlaying && isInsideSelection) pulseAnim else 1.0f
                    val barHeight = ((amp * ampMultiplier) * (canvasHeight - 20.dp.toPx())).coerceAtLeast(3.dp.toPx())
                    val top = centerY - (barHeight / 2f)

                    val barColor = when {
                        !isInsideSelection -> Color(0xFF49454F).copy(alpha = 0.35f)
                        isPastPlayhead -> Color(0xFFFFD993) // Active playback progress
                        else -> Color(0xFFD0BCFF) // In selection
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Draw Excluded Segment Shading (Left of start)
                if (startPx > 0f) {
                    drawRect(
                        color = Color(0xFF000000).copy(alpha = 0.65f),
                        topLeft = Offset(0f, 0f),
                        size = Size(startPx, canvasHeight)
                    )
                }

                // Draw Excluded Segment Shading (Right of end)
                if (endPx < canvasWidth) {
                    drawRect(
                        color = Color(0xFF000000).copy(alpha = 0.65f),
                        topLeft = Offset(endPx, 0f),
                        size = Size(canvasWidth - endPx, canvasHeight)
                    )
                }

                // Draw Selection Highlight Border Top & Bottom
                drawLine(
                    color = Color(0xFFD0BCFF).copy(alpha = 0.6f),
                    start = Offset(startPx, 0f),
                    end = Offset(endPx, 0f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFD0BCFF).copy(alpha = 0.6f),
                    start = Offset(startPx, canvasHeight),
                    end = Offset(endPx, canvasHeight),
                    strokeWidth = 2.dp.toPx()
                )

                // Draw Playhead Line if within selection
                if (isPlaying || playbackSeconds > 0f) {
                    drawLine(
                        color = Color(0xFFFFD993),
                        start = Offset(playheadPx, 0f),
                        end = Offset(playheadPx, canvasHeight),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color(0xFFFFD993),
                        radius = 4.dp.toPx(),
                        center = Offset(playheadPx, 4.dp.toPx())
                    )
                }
            }

            // Left Start Trim Handle (Draggable)
            Box(
                modifier = Modifier
                    .offset { IntOffset(startPx.roundToInt() - 14.dp.roundToPx(), 0) }
                    .fillMaxHeight()
                    .width(28.dp)
                    .pointerInput(safeDuration, endSeconds) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newPx = (startPx + dragAmount.x).coerceIn(0f, endPx - 20f)
                            val newSec = (newPx / widthPx) * safeDuration
                            onStartChange(newSec)
                        }
                    }
                    .testTag("trim_handle_start"),
                contentAlignment = Alignment.Center
            ) {
                // Vertical boundary line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(Color(0xFF4FD1C5), RoundedCornerShape(1.5.dp))
                )
                // Center Knob
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF4FD1C5),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(width = 16.dp, height = 28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "◀",
                            color = Color(0xFF003731),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right End Trim Handle (Draggable)
            Box(
                modifier = Modifier
                    .offset { IntOffset(endPx.roundToInt() - 14.dp.roundToPx(), 0) }
                    .fillMaxHeight()
                    .width(28.dp)
                    .pointerInput(safeDuration, startSeconds) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newPx = (endPx + dragAmount.x).coerceIn(startPx + 20f, widthPx)
                            val newSec = (newPx / widthPx) * safeDuration
                            onEndChange(newSec)
                        }
                    }
                    .testTag("trim_handle_end"),
                contentAlignment = Alignment.Center
            ) {
                // Vertical boundary line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(Color(0xFFF472B6), RoundedCornerShape(1.5.dp))
                )
                // Center Knob
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF472B6),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(width = 16.dp, height = 28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "▶",
                            color = Color(0xFF5B002B),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeIndicatorBox(
    title: String,
    timeSeconds: Float,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) Color(0xFF2B2930) else Color(0xFF24222B),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = if (isPrimary) 0.8f else 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFFCAC4D0),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatTimeDetailed(timeSeconds),
                color = badgeColor,
                fontSize = if (isPrimary) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StepButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF383540),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        modifier = Modifier.height(26.dp)
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.padding(0.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFFE6E1E5),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) Color(0xFF381E72) else Color(0xFF2B2930),
        border = BorderStroke(1.dp, if (highlight) Color(0xFFD0BCFF) else Color(0xFF49454F)),
        modifier = modifier.height(34.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxSize()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlight) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (highlight) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTime(seconds: Float): String {
    val totalSec = seconds.toInt().coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun formatTimeDetailed(seconds: Float): String {
    val totalSec = seconds.coerceAtLeast(0f)
    val mins = (totalSec / 60).toInt()
    val secs = (totalSec % 60).toInt()
    val millis = ((totalSec - totalSec.toInt()) * 10).toInt()
    return String.format("%02d:%02d.%01d", mins, secs, millis)
}
