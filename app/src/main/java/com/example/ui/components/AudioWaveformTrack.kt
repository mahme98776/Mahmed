package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Audio Track Waveform Segment data
 */
data class WaveformSample(
    val amplitude: Float, // 0.0f to 1.0f
    val isSilent: Boolean = amplitude < 0.08f
)

/**
 * Comprehensive Audio Waveform Visualizer for Recording & Editing:
 * - Displays dynamic recording waveform with volume thresholds
 * - Identifies and highlights silent segments (below threshold) vs active speech
 * - Displays scrubbable interactive playback timeline with progress playhead
 * - Shows peak dB / volume meter
 */
@Composable
fun AudioWaveformTrack(
    waveformHistory: List<Float>,
    currentAmplitude: Float,
    isRecording: Boolean,
    isPlaying: Boolean,
    progressFraction: Float, // 0.0 to 1.0
    totalDurationSeconds: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    silenceThreshold: Float = 0.08f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePulse"
    )

    // Compute active vs silent segments stats
    val displaySamples = remember(waveformHistory, currentAmplitude, isRecording) {
        if (waveformHistory.isEmpty()) {
            // Generate standard placeholder bars
            List(40) { index ->
                val base = (sin(index * 0.35) * 0.4f + 0.5f).toFloat().coerceIn(0.04f, 0.9f)
                val isSil = base < silenceThreshold
                WaveformSample(base, isSil)
            }
        } else {
            waveformHistory.map { amp ->
                WaveformSample(
                    amplitude = amp.coerceIn(0.02f, 1.0f),
                    isSilent = amp < silenceThreshold
                )
            }
        }
    }

    val silentSegmentsCount = displaySamples.count { it.isSilent }
    val activeSpeechCount = displaySamples.size - silentSegmentsCount
    val speechPercentage = if (displaySamples.isNotEmpty()) {
        ((activeSpeechCount.toFloat() / displaySamples.size.toFloat()) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_waveform_track_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24222B)),
        border = BorderStroke(
            1.dp,
            if (isRecording) Color(0xFFF2B8B5) else if (isPlaying) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Status & Legend Bar (Speech vs Silence Indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isRecording) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) "تسجيل مباشر (تحليل الموجات)" else "موجات الصوت والسكوت 📊",
                        color = Color(0xFFE6E1E5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Silence vs Speech Legend Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Active Speech Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E1B24),
                        border = BorderStroke(1.dp, Color(0xFFA6D4A8).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFA6D4A8), CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "كلام: $speechPercentage%",
                                color = Color(0xFFA6D4A8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Silence Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E1B24),
                        border = BorderStroke(1.dp, Color(0xFFF2B8B5).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFE06C75), CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "سكوت (فراغ)",
                                color = Color(0xFFF2B8B5),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Interactive Canvas Waveform Renderer with Playhead & Tap-to-Seek
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFF1A1820), RoundedCornerShape(12.dp))
                    .pointerInput(totalDurationSeconds) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(fraction * totalDurationSeconds)
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerY = canvasHeight / 2f

                    // Draw Center Baseline
                    drawLine(
                        color = Color(0xFF383540),
                        start = Offset(0f, centerY),
                        end = Offset(canvasWidth, centerY),
                        strokeWidth = 1.dp.toPx()
                    )

                    val sampleCount = displaySamples.size.coerceAtLeast(1)
                    val barSpacing = 2.5.dp.toPx()
                    val totalSpacing = barSpacing * (sampleCount - 1)
                    val barWidth = ((canvasWidth - totalSpacing) / sampleCount).coerceIn(2.dp.toPx(), 8.dp.toPx())

                    // Draw Waveform Bars
                    displaySamples.forEachIndexed { i, sample ->
                        val x = i * (barWidth + barSpacing)
                        val barFraction = if (isRecording && i == displaySamples.lastIndex) {
                            (currentAmplitude * wavePulse).coerceIn(0.05f, 1f)
                        } else {
                            sample.amplitude
                        }

                        val barHeight = (barFraction * (canvasHeight - 8.dp.toPx())).coerceAtLeast(3.dp.toPx())
                        val top = centerY - (barHeight / 2f)

                        val barProgress = i.toFloat() / sampleCount.toFloat()
                        val isPassed = barProgress <= progressFraction

                        val barColor = when {
                            sample.isSilent -> {
                                // Silent / Gap Section color
                                if (isPassed) Color(0xFF8C4A52) else Color(0xFF4A2830)
                            }
                            isPassed -> {
                                // Active played audio
                                Color(0xFFD0BCFF)
                            }
                            isRecording -> {
                                // Live recording audio
                                Color(0xFFFFD993)
                            }
                            else -> {
                                // Future speech
                                Color(0xFF7D7787)
                            }
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }

                    // Draw Scrubber / Playhead Indicator Line
                    val playheadX = (progressFraction * canvasWidth).coerceIn(0f, canvasWidth)
                    drawLine(
                        color = Color(0xFFFFD993),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, canvasHeight),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Draw Playhead Top Knob
                    drawCircle(
                        color = Color(0xFFFFD993),
                        radius = 4.dp.toPx(),
                        center = Offset(playheadX, 4.dp.toPx())
                    )
                }
            }

            // Bottom Volume Level Meter & Timing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Volume Indicator Meter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val volumeDb = if (isRecording) {
                        (currentAmplitude * 100).toInt()
                    } else if (isPlaying) {
                        ((displaySamples.getOrNull((progressFraction * displaySamples.size).toInt())?.amplitude ?: 0.3f) * 100).toInt()
                    } else 0

                    Icon(
                        imageVector = if (volumeDb > 10) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = null,
                        tint = if (volumeDb > 65) Color(0xFFFFB4AB) else Color(0xFFCAC4D0),
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = "مستوى الصوت: $volumeDb dB",
                        color = if (volumeDb > 70) Color(0xFFFFB4AB) else Color(0xFFCAC4D0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // 5-step LED Volume Level Meter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        repeat(5) { step ->
                            val threshold = (step + 1) * 20
                            val isActiveStep = volumeDb >= threshold
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = 5.dp)
                                    .background(
                                        when {
                                            !isActiveStep -> Color(0xFF383540)
                                            step == 4 -> Color(0xFFF2B8B5) // Peak Red
                                            step >= 3 -> Color(0xFFFFD993) // Yellow
                                            else -> Color(0xFFA6D4A8) // Green
                                        },
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }

                // Interactive Hint
                Text(
                    text = "انقر على الموجة للانتقال السريع ⏱️",
                    color = Color(0xFF938F99),
                    fontSize = 10.sp
                )
            }
        }
    }
}
