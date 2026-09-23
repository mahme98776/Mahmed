package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sin

enum class VisualizerViewMode(val title: String) {
    WAVEFORM("المنحنى الطيفي"),
    SPECTRUM("طيف الترددات FFT"),
    VU_METER("مقياس الديسيبل dB"),
    HYBRID("شامل الميزات")
}

data class FrequencyBand(
    val hzLabel: String,
    val description: String,
    val baseMagnitude: Float,
    val color: Color
)

/**
 * High-performance, Recharts-inspired Audio Waveform & Frequency Spectrum Visualizer.
 * Provides rich visual insights into audio engineering, decibel levels, and frequency domain.
 */
@Composable
fun RechartsAudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    isLiveRecording: Boolean = false,
    currentAmplitude: Float = 0.5f,
    waveformHistory: List<Float> = emptyList(),
    playbackProgress: Float = 0.0f,
    onSeek: (Float) -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(VisualizerViewMode.HYBRID) }
    var hoveredFrequency by remember { mutableStateOf<FrequencyBand?>(null) }
    var scrubPosition by remember { mutableFloatStateOf(playbackProgress) }

    // Frequency bands modeled after professional audio 10-band octave visualizers
    val frequencyBands = remember {
        listOf(
            FrequencyBand("31Hz", "الترددات التحتية العميقة (Sub Bass)", 0.35f, Color(0xFF7C4DFF)),
            FrequencyBand("63Hz", "صوت جهير إيقاعي (Bass Punch)", 0.65f, Color(0xFF536DFE)),
            FrequencyBand("125Hz", "دفء الصوت البشري (Warmth)", 0.85f, Color(0xFF00B0FF)),
            FrequencyBand("250Hz", "الترددات المنخفضة الأساسية (Low Mid)", 0.90f, Color(0xFF00E5FF)),
            FrequencyBand("500Hz", "وضوح الكلمات ومخارج الحروف (Body)", 0.95f, Color(0xFF1DE9B6)),
            FrequencyBand("1kHz", "حضور الصوت والنطق (Vocal Core)", 0.88f, Color(0xFF00E676)),
            FrequencyBand("2kHz", "تطابق الشفاه والوضوح (Lip Clarity)", 0.82f, Color(0xFF76FF03)),
            FrequencyBand("4kHz", "حدة الحروف والصفير (Presence)", 0.70f, Color(0xFFFFEA00)),
            FrequencyBand("8kHz", "نقاء وبريق الصوت (Brilliance)", 0.55f, Color(0xFFFF9100)),
            FrequencyBand("16kHz", "الهواء الصوتي المفتوح (Air/Sparkle)", 0.40f, Color(0xFFFF3D00))
        )
    }

    // Dynamic wave animation for smooth continuous motion
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_loop"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_audio_waveform_visualizer"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C131F)
        ),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Visualizer title, live indicator, and mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "محلل الترددات والمنحنى الصوتي (Audio Spectrum)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isLiveRecording) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFF1744)
                                ) {
                                    Text(
                                        text = "LIVE REC ●",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "هندسة الصوت البصري المستوحاة من مخططات Recharts البيانية",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF88A0B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Mode Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VisualizerViewMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E5FF),
                            selectedLabelColor = Color(0xFF0C131F),
                            containerColor = Color(0xFF162235),
                            labelColor = Color(0xFFB0C4DE)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedMode == mode) Color(0xFF00E5FF) else Color(0xFF23364F),
                            enabled = true,
                            selected = selectedMode == mode
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Canvas Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070B12))
                    .border(BorderStroke(1.dp, Color(0xFF1E2D44)), RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val progress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            scrubPosition = progress
                            onSeek(progress)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val progress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            scrubPosition = progress
                            onSeek(progress)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // 1. Grid Background (Like Recharts CartesianGrid)
                    val gridColor = Color(0xFF152236)
                    val horizontalLines = 4
                    for (i in 1..horizontalLines) {
                        val y = h * (i.toFloat() / (horizontalLines + 1))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    val verticalLines = 8
                    for (i in 1..verticalLines) {
                        val x = w * (i.toFloat() / (verticalLines + 1))
                        drawLine(
                            color = gridColor.copy(alpha = 0.6f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Render Based on Selected Mode
                    when (selectedMode) {
                        VisualizerViewMode.WAVEFORM -> {
                            drawRechartsWaveform(
                                w = w,
                                h = h,
                                midY = midY,
                                phase = phase,
                                amplitude = currentAmplitude,
                                history = waveformHistory
                            )
                        }
                        VisualizerViewMode.SPECTRUM -> {
                            drawRechartsSpectrumBars(
                                w = w,
                                h = h,
                                phase = phase,
                                bands = frequencyBands,
                                amplitude = currentAmplitude
                            )
                        }
                        VisualizerViewMode.VU_METER -> {
                            drawVuDecibelMeter(
                                w = w,
                                h = h,
                                amplitude = currentAmplitude,
                                phase = phase
                            )
                        }
                        VisualizerViewMode.HYBRID -> {
                            // Hybrid: Draw soft background waveform area, overlaid with spectrum bars
                            drawRechartsWaveform(
                                w = w,
                                h = h,
                                midY = midY,
                                phase = phase,
                                amplitude = currentAmplitude * 0.7f,
                                history = waveformHistory,
                                alpha = 0.45f
                            )
                            drawRechartsSpectrumBars(
                                w = w,
                                h = h,
                                phase = phase,
                                bands = frequencyBands,
                                amplitude = currentAmplitude
                            )
                        }
                    }

                    // 3. Playhead Cursor (Scrubber)
                    val cursorX = w * scrubPosition
                    drawLine(
                        color = Color(0xFFFFD600),
                        start = Offset(cursorX, 0f),
                        end = Offset(cursorX, h),
                        strokeWidth = 2.5f
                    )
                    drawCircle(
                        color = Color(0xFFFFD600),
                        radius = 6f,
                        center = Offset(cursorX, 10f)
                    )
                }

                // Hover / Frequency Insight Pill
                if (hoveredFrequency != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1B2A3E).copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, hoveredFrequency!!.color),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "${hoveredFrequency!!.hzLabel} • ${hoveredFrequency!!.description}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Frequency Band Quick Interactive Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                frequencyBands.forEach { band ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (hoveredFrequency == band) band.color.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .clickable {
                                hoveredFrequency = if (hoveredFrequency == band) null else band
                            }
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = band.hzLabel,
                            fontSize = 9.sp,
                            color = if (hoveredFrequency == band) band.color else Color(0xFF6C829D),
                            fontWeight = if (hoveredFrequency == band) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Audio Engineering Status Bar (RMS, Peak, dB, Dynamic Range)
            val dbLevel = (-60f + (currentAmplitude.coerceIn(0.01f, 1f) * 60f)).coerceIn(-60f, 0f)
            val isClipping = dbLevel > -0.5f

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF111A29),
                border = BorderStroke(1.dp, Color(0xFF22344D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decibel Level
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مستوى الإشارة:",
                            fontSize = 11.sp,
                            color = Color(0xFF88A0B8)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f dB", dbLevel),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClipping) Color(0xFFFF1744) else Color(0xFF00E5FF)
                        )
                    }

                    // RMS Level
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RMS النقاء:",
                            fontSize = 11.sp,
                            color = Color(0xFF88A0B8)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${(currentAmplitude * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }

                    // Clipping Status
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isClipping) Color(0xFFFF1744).copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isClipping) Color(0xFFFF1744) else Color(0xFF00E676).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isClipping) "تنبيه تشويش PEAK ⚠️" else "إشارة مثالية النقاء ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClipping) Color(0xFFFF5252) else Color(0xFF00E676),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Canvas Drawing Helper Implementations
// -------------------------------------------------------------

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRechartsWaveform(
    w: Float,
    h: Float,
    midY: Float,
    phase: Float,
    amplitude: Float,
    history: List<Float>,
    alpha: Float = 1.0f
) {
    val pointsCount = 48
    val path = Path()
    val fillPath = Path()

    fillPath.moveTo(0f, h)
    path.moveTo(0f, midY)
    fillPath.lineTo(0f, midY)

    for (i in 0..pointsCount) {
        val x = (i.toFloat() / pointsCount) * w
        val normX = (i.toFloat() / pointsCount)
        
        // Combine harmonic sine waves with current amplitude for realistic audio pulse
        val wave1 = sin(normX * 12.0 + phase)
        val wave2 = sin(normX * 24.0 - phase * 1.5) * 0.5
        val wave3 = sin(normX * 6.0 + phase * 0.7) * 0.3
        val combined = (wave1 + wave2 + wave3) / 1.8

        val historyAmp = if (history.isNotEmpty()) {
            val histIndex = (normX * (history.size - 1)).toInt().coerceIn(0, history.size - 1)
            history[histIndex]
        } else {
            amplitude
        }

        val dynamicAmp = (historyAmp.coerceIn(0.15f, 1f)) * (h * 0.42f)
        val y = (midY + combined * dynamicAmp).toFloat().coerceIn(4f, h - 4f)

        path.lineTo(x, y)
        fillPath.lineTo(x, y)
    }

    fillPath.lineTo(w, h)
    fillPath.close()

    // 1. Gradient Fill (Recharts Area style)
    val fillBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00E5FF).copy(alpha = 0.55f * alpha),
            Color(0xFF7C4DFF).copy(alpha = 0.30f * alpha),
            Color(0xFF00E5FF).copy(alpha = 0.02f)
        ),
        startY = 0f,
        endY = h
    )
    drawPath(fillPath, brush = fillBrush)

    // 2. Stroke Line (Recharts Line style with glow)
    val strokeBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00E5FF).copy(alpha = alpha),
            Color(0xFF76FF03).copy(alpha = alpha),
            Color(0xFFFFD600).copy(alpha = alpha),
            Color(0xFFFF3D00).copy(alpha = alpha)
        )
    )
    drawPath(
        path = path,
        brush = strokeBrush,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRechartsSpectrumBars(
    w: Float,
    h: Float,
    phase: Float,
    bands: List<FrequencyBand>,
    amplitude: Float
) {
    val barCount = bands.size
    val totalSpacing = (barCount + 1) * 8f
    val barWidth = ((w - totalSpacing) / barCount).coerceAtLeast(8f)

    bands.forEachIndexed { index, band ->
        val x = 8f + index * (barWidth + 8f)
        
        // Calculate dynamic height based on frequency characteristics + phase perturbation
        val freqPerturb = (sin(phase + index * 0.8) * 0.18f).toFloat()
        val dynMagnitude = (band.baseMagnitude * amplitude + freqPerturb).coerceIn(0.08f, 0.96f)
        val barHeight = dynMagnitude * (h - 20f)
        val y = h - barHeight - 4f

        val barBrush = Brush.verticalGradient(
            colors = listOf(
                band.color,
                band.color.copy(alpha = 0.7f),
                band.color.copy(alpha = 0.2f)
            ),
            startY = y,
            endY = h
        )

        // Draw rounded bar
        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Draw peak indicator dot on top of bar
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = Offset(x + barWidth / 2f, y - 4f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVuDecibelMeter(
    w: Float,
    h: Float,
    amplitude: Float,
    phase: Float
) {
    val channels = 2 // Stereo L / R
    val channelHeight = (h - 32f) / channels

    listOf("القناة اليسرى (L)", "القناة اليمنى (R)").forEachIndexed { chIndex, _ ->
        val topY = 16f + chIndex * (channelHeight + 12f)
        val channelAmp = if (chIndex == 0) amplitude else (amplitude * (0.85f + (sin(phase) * 0.15f).toFloat()))
        val activeWidth = (w - 24f) * channelAmp.coerceIn(0.05f, 1f)

        // Background track
        drawRoundRect(
            color = Color(0xFF162335),
            topLeft = Offset(12f, topY),
            size = Size(w - 24f, channelHeight),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Multi-color VU Gradient (Green -> Yellow -> Red)
        val vuBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00E676),
                Color(0xFF76FF03),
                Color(0xFFFFEA00),
                Color(0xFFFF9100),
                Color(0xFFFF1744)
            ),
            startX = 12f,
            endX = w - 24f
        )

        drawRoundRect(
            brush = vuBrush,
            topLeft = Offset(12f, topY),
            size = Size(activeWidth, channelHeight),
            cornerRadius = CornerRadius(8f, 8f)
        )
    }
}
