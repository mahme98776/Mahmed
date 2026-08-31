package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.log10
import kotlin.math.sin

/**
 * State of the live recording audio signal
 */
data class RecordingAudioSignal(
    val rawAmplitude: Float = 0f, // 0.0f to 1.0f
    val amplitudeHistory: List<Float> = emptyList(),
    val durationMs: Long = 0L,
    val isRecording: Boolean = false,
    val detectedPitchHz: Float = 0f,
    val noiseFloorThreshold: Float = 0.06f,
    val clippingThreshold: Float = 0.92f,
    val amplitude: Float = rawAmplitude,
    val peakAmplitude: Float = rawAmplitude,
    val decibels: Float = -60f,
    val isClipping: Boolean = rawAmplitude >= clippingThreshold,
    val isSpeechDetected: Boolean = rawAmplitude > noiseFloorThreshold,
    val recordingDurationSeconds: Float = durationMs / 1000f
)

/**
 * Specialized Live Recording Waveform Visualizer
 * Provides instant, highly responsive visual feedback during the dubbing process:
 * 1. Live oscilloscope signal envelope & moving waveform bars
 * 2. True Decibel (dBFS) readout & 3-zone LED level meter (Safe, Optimal, Clipping)
 * 3. Over-modulation / Clipping Red Alert indicator
 * 4. Active Speech vs Ambient Noise detection
 * 5. Timecode ruler and duration tracking
 */
@Composable
fun RecordingWaveformVisualizer(
    signal: RecordingAudioSignal,
    modifier: Modifier = Modifier,
    barCount: Int = 42
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Compute decibel (dBFS) and clipping status
    val ampClamped = signal.rawAmplitude.coerceIn(0.001f, 1.0f)
    val dbFs = remember(ampClamped) {
        val calculated = 20 * log10(ampClamped)
        calculated.coerceIn(-60f, 0f)
    }

    val isClipping = signal.isRecording && signal.rawAmplitude >= signal.clippingThreshold
    val isSpeaking = signal.isRecording && signal.rawAmplitude > signal.noiseFloorThreshold

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recording_waveform_visualizer_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClipping) Color(0xFF331616) else Color(0xFF191720)
        ),
        border = BorderStroke(
            1.5.dp,
            when {
                isClipping -> Color(0xFFFF5252)
                signal.isRecording -> Color(0xFF00E5FF).copy(alpha = glowAlpha)
                else -> Color(0xFF3B3847)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Bar: Recording Status + Live dBFS + Peak Warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Indicator with Pulsing Mic
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (signal.isRecording) Color(0xFFFF5252).copy(alpha = 0.25f) else Color(0xFF2C2836),
                        border = BorderStroke(1.dp, if (signal.isRecording) Color(0xFFFF5252) else Color(0xFF49454F)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (signal.isRecording) Color(0xFFFF5252) else Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (signal.isRecording) "إشارة الميكروفون المباشرة 🎙️" else "محلل الإشارة الصوتية (جاهز)",
                            color = if (signal.isRecording) Color.White else Color(0xFFCAC4D0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Duration timecode
                        val seconds = (signal.durationMs / 1000).toInt()
                        val ms = ((signal.durationMs % 1000) / 100).toInt()
                        Text(
                            text = if (signal.isRecording) {
                                String.format("المدة: %02d:%02d.%d ثانية", seconds / 60, seconds % 60, ms)
                            } else {
                                "تحدث في الميكروفون لمعاينة سعة الصوت"
                            },
                            color = if (signal.isRecording) Color(0xFF00E5FF) else Color(0xFF938F99),
                            fontSize = 10.sp
                        )
                    }
                }

                // dBFS Meter & Clipping Warning Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(
                        visible = isClipping,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "تشويش (CLIP)",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // dBFS Indicator Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF131118),
                        border = BorderStroke(
                            1.dp,
                            when {
                                dbFs > -3f -> Color(0xFFFF5252)
                                dbFs > -12f -> Color(0xFFFFD54F)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                    ) {
                        Text(
                            text = String.format("%.1f dB", dbFs),
                            color = when {
                                dbFs > -3f -> Color(0xFFFF5252)
                                dbFs > -12f -> Color(0xFFFFD54F)
                                else -> Color(0xFF81C784)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Real-Time Oscilloscope Waveform Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .background(Color(0xFF100E15), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerY = canvasHeight / 2f

                    // Draw Horizontal Center Zero-Crossing Guide
                    drawLine(
                        color = Color(0xFF2C2836),
                        start = Offset(0f, centerY),
                        end = Offset(canvasWidth, centerY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Upper & Lower Threshold Guideline
                    val thresholdYTop = centerY - (centerY * 0.85f)
                    val thresholdYBottom = centerY + (centerY * 0.85f)
                    drawLine(
                        color = Color(0xFF3E2723),
                        start = Offset(0f, thresholdYTop),
                        end = Offset(canvasWidth, thresholdYTop),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF3E2723),
                        start = Offset(0f, thresholdYBottom),
                        end = Offset(canvasWidth, thresholdYBottom),
                        strokeWidth = 1.dp.toPx()
                    )

                    val history = signal.amplitudeHistory
                    val barSpacing = 2.dp.toPx()
                    val totalBars = barCount
                    val barWidth = ((canvasWidth - (totalBars - 1) * barSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

                    // Draw Dynamic Waveform Signal
                    for (i in 0 until totalBars) {
                        val historyIdx = if (history.isNotEmpty()) {
                            (history.size - totalBars + i).coerceIn(0, history.lastIndex)
                        } else 0

                        val histAmp = if (history.isNotEmpty()) history[historyIdx] else 0f
                        val liveWave = if (signal.isRecording) {
                            val harmonic = sin(phase + i * 0.35f) * 0.3f
                            (signal.rawAmplitude * 0.7f + histAmp * 0.3f + harmonic * signal.rawAmplitude).coerceIn(0.04f, 1.0f)
                        } else {
                            0.04f + sin(i * 0.2f) * 0.02f
                        }

                        val barHeight = (liveWave * (canvasHeight - 10.dp.toPx())).coerceAtLeast(3.dp.toPx())
                        val x = i * (barWidth + barSpacing)
                        val y = centerY - (barHeight / 2f)

                        val barColor = when {
                            !signal.isRecording -> Color(0xFF3E3A4B)
                            liveWave > 0.88f -> Color(0xFFFF5252) // Clipping
                            liveWave > 0.55f -> Color(0xFFFFD54F) // Optimal strong speech
                            liveWave > 0.15f -> Color(0xFF00E5FF) // Clear voice
                            else -> Color(0xFF4CAF50).copy(alpha = 0.6f) // Ambient quiet
                        }

                        val brush = Brush.verticalGradient(
                            colors = listOf(
                                barColor.copy(alpha = 0.9f),
                                barColor,
                                barColor.copy(alpha = 0.9f)
                            ),
                            startY = y,
                            endY = y + barHeight
                        )

                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }

                    // Draw Continuous Smooth Oscilloscope Sine Curve overlay
                    if (signal.isRecording && signal.rawAmplitude > 0.05f) {
                        val curvePath = Path()
                        val stepX = canvasWidth / 30f
                        curvePath.moveTo(0f, centerY)

                        for (step in 0..30) {
                            val curX = step * stepX
                            val ampMod = sin(phase * 2f + step * 0.5f) * (signal.rawAmplitude * centerY * 0.8f)
                            val curY = centerY + ampMod
                            curvePath.lineTo(curX, curY)
                        }

                        drawPath(
                            path = curvePath,
                            color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Bottom Meter: 12-Step Fast LED Bar + Speech / Silence Envelope Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech Envelope Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when {
                                    !signal.isRecording -> Color(0xFF555060)
                                    isSpeaking -> Color(0xFF4CAF50)
                                    else -> Color(0xFFFFB74D)
                                },
                                CircleShape
                            )
                    )
                    Text(
                        text = when {
                            !signal.isRecording -> "الميكروفون في وضع الاستعداد"
                            isSpeaking -> "التقاط صوت فصيح ونقي 🟢"
                            else -> "سكوت (مستوى الضوضاء هادئ) 🟡"
                        },
                        color = Color(0xFFCAC4D0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 12-Segment LED Gradient VU Meter
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeLedCount = ((signal.rawAmplitude.coerceIn(0f, 1f)) * 12).toInt()
                    repeat(12) { ledIndex ->
                        val isLit = ledIndex < activeLedCount
                        val ledColor = when {
                            !isLit -> Color(0xFF282533)
                            ledIndex >= 10 -> Color(0xFFFF5252) // Danger Red
                            ledIndex >= 7 -> Color(0xFFFFD54F) // Mid Yellow
                            else -> Color(0xFF4CAF50) // Safe Green
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 5.dp, height = if (ledIndex >= 10) 9.dp else 7.dp)
                                .background(ledColor, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}
