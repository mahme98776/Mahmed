package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Enhanced Waveform Visualizer for real-time audio signal feedback during dubbing & playback.
 */
@Composable
fun AudioWaveformVisualizer(
    amplitude: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 34,
    showGlowLine: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val totalWidth = size.width
        val maxHeight = size.height
        val centerY = maxHeight / 2f
        val barWidth = (totalWidth / barCount) * 0.62f
        val gap = (totalWidth / barCount) * 0.38f

        // Draw Baseline
        drawLine(
            color = if (isActive) Color(0xFF38354A) else Color(0xFF282533),
            start = Offset(0f, centerY),
            end = Offset(totalWidth, centerY),
            strokeWidth = 1.dp.toPx()
        )

        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val wave = if (isActive) {
                val harmonic1 = sin(phase + i * 0.38f) * 0.35f
                val harmonic2 = sin(phase * 1.5f + i * 0.7f) * 0.2f
                val dynamicAmp = (amplitude * 0.75f + harmonic1 + harmonic2).coerceIn(0.10f, 1.0f)
                dynamicAmp
            } else {
                0.08f + (sin(i * 0.4f) * 0.03f)
            }

            val barH = (maxHeight * wave * (if (isActive) pulseGlow else 1f)).coerceAtLeast(3.dp.toPx())
            val x = i * (barWidth + gap)
            val y = centerY - (barH / 2f)

            val isClipping = isActive && amplitude > 0.88f
            val brush = Brush.verticalGradient(
                colors = when {
                    !isActive -> listOf(Color(0xFF475569), Color(0xFF334155))
                    isClipping -> listOf(Color(0xFFFF5252), Color(0xFFFF7A00), Color(0xFFFF5252))
                    else -> listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF8B5CF6),
                        Color(0xFFEC4899)
                    )
                },
                startY = y,
                endY = y + barH
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }

        // Draw Smooth Signal Envelope Curve
        if (isActive && showGlowLine && amplitude > 0.05f) {
            val curvePath = Path()
            val step = totalWidth / 24f
            curvePath.moveTo(0f, centerY)

            for (s in 0..24) {
                val curX = s * step
                val sinMod = sin(phase * 2f + s * 0.45f) * (amplitude * centerY * 0.75f)
                curvePath.lineTo(curX, centerY + sinMod)
            }

            drawPath(
                path = curvePath,
                color = if (amplitude > 0.88f) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFF00E5FF).copy(alpha = 0.5f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
