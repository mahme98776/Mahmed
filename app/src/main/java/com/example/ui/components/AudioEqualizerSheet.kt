package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DubbingViewModel
import com.example.ui.EqualizerBand
import java.util.Locale

/**
 * Professional Studio-Grade 7-Band Audio Equalizer for Videos and Voice Dubbing.
 * 
 * Controls frequency bands:
 * - 60 Hz (Sub-Bass)
 * - 170 Hz (Bass)
 * - 310 Hz (Low-Mid)
 * - 600 Hz (Mid)
 * - 1 kHz (High-Mid / Vocals)
 * - 3 kHz (Presence / Brilliance)
 * - 12 kHz (Treble / Air)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEqualizerSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: DubbingViewModel,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val eqState = viewModel.uiState.value.equalizerState

    val presets = listOf(
        "Flat (متوازن)" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
        "صوت بشري نقي (Voice)" to listOf(-3f, -1f, 2f, 4f, 4f, 3f, 1f),
        "بيس سينمائي (Bass)" to listOf(6f, 5f, 3f, 1f, 0f, -1f, -2f),
        "بودكاست وإذاعة" to listOf(-2f, 2f, 3f, 4f, 3f, 1f, 0f),
        "دراما وسينما" to listOf(4f, 2f, -1f, 1f, 3f, 4f, 3f),
        "نقاء الترددات العليا" to listOf(-2f, -1f, 0f, 1f, 3f, 5f, 6f),
        "أكوستيك دافئ" to listOf(3f, 2f, 1f, 2f, 2f, 3f, 2f)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("audio_equalizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        Color(0xFF7C4DFF).copy(alpha = 0.25f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "معادل الصوت",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "المعادل الصوتي الاحترافي (Equalizer)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تحكم في الترددات الصوتية ونقاء الصوت للفيديو والدبلجة",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bypass / A-B Audition Bar
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (eqState.isBypassed) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp),
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
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = if (eqState.isBypassed) MaterialTheme.colorScheme.error else Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (eqState.isBypassed) "المعادل متوقف (صوت خام Bypass)" else "المعادل مفعل (EQ Active)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اضغط للمقارنة الفورية وسماع الفرق بين الصوت الأصلي والمعالج",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = !eqState.isBypassed,
                        onCheckedChange = { viewModel.toggleEqualizerBypass() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Presets Horizontal Chips
            Text(
                text = "الإعدادات الجاهزة (Presets):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (name, gains) ->
                    val isSelected = eqState.selectedPresetName == name
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.applyEqualizerPreset(name, gains) },
                        label = {
                            Text(
                                text = name,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                            selectedLabelColor = Color(0xFF00E5FF)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF00E5FF) else MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Color(0xFF00E5FF),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Real-time Graphic Frequency Response Curve Display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF21262D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerResponseCurve(
                        bands = eqState.bands,
                        isBypassed = eqState.isBypassed,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // 7 Vertical Band Sliders
            Text(
                text = "ترددات الصوت (Bands -12dB إلى +12dB):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                eqState.bands.forEach { band ->
                    EqualizerVerticalBandItem(
                        band = band,
                        isBypassed = eqState.isBypassed,
                        onGainChange = { newGain ->
                            viewModel.updateEqualizerBandGain(band.id, newGain)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Acoustic Enhancements: Bass Boost & Spatial Reverb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bass Boost Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تعزيز البيس",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${(eqState.bassBoostFraction * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Slider(
                            value = eqState.bassBoostFraction,
                            onValueChange = { viewModel.setEqualizerBassBoost(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Spatial Virtualizer Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = null,
                                tint = Color(0xFF7C4DFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "المحيط الصوتي",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${(eqState.spatialVirtualizerFraction * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C4DFF)
                            )
                        }
                        Slider(
                            value = eqState.spatialVirtualizerFraction,
                            onValueChange = { viewModel.setEqualizerSpatialVirtualizer(it) },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF7C4DFF),
                                activeTrackColor = Color(0xFF7C4DFF)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Master Gain Fader
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "الكسب الرئيسي (Master Gain):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = eqState.masterGainDb,
                    onValueChange = { viewModel.setEqualizerMasterGain(it) },
                    valueRange = -6f..6f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f).height(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${String.format(Locale.US, "%+.1f", eqState.masterGainDb)} dB",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetEqualizerToFlat() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("إعادة ضبط (Flat)", fontSize = 12.5.sp)
                }

                Button(
                    onClick = {
                        viewModel.showToast("تم تطبيق إعدادات المعادل الصوتي على الفيديو بنجاح! 🎚️✨")
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.3f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF002028)
                    )
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("اعتماد وتطبيق على الفيديو", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/**
 * Single Vertical Slider Column for a Frequency Band
 */
@Composable
private fun EqualizerVerticalBandItem(
    band: EqualizerBand,
    isBypassed: Boolean,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Gain value badge
        Text(
            text = if (isBypassed) "0dB" else "${String.format(Locale.US, "%+.0f", band.gainDb)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBypassed) 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else if (band.gainDb > 0) Color(0xFF00E5FF)
            else if (band.gainDb < 0) Color(0xFFFF5252)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Custom Slider Track using simple interactive canvas slider
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            // Slider
            Slider(
                value = if (isBypassed) 0f else band.gainDb,
                onValueChange = onGainChange,
                valueRange = -12f..12f,
                enabled = !isBypassed,
                colors = SliderDefaults.colors(
                    thumbColor = if (band.gainDb >= 0) Color(0xFF00E5FF) else Color(0xFF7C4DFF),
                    activeTrackColor = if (band.gainDb >= 0) Color(0xFF00E5FF) else Color(0xFF7C4DFF),
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    // Rotate to make vertical
                    .rotate(-90f)
            )
        }

        // Frequency Label
        Text(
            text = band.frequencyLabel,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Graphic Bezier Visualizer Curve across all bands
 */
@Composable
private fun EqualizerResponseCurve(
    bands: List<EqualizerBand>,
    isBypassed: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw grid lines
        drawLine(
            color = Color(0xFF21262D),
            start = Offset(0f, centerY - (height * 0.35f)),
            end = Offset(width, centerY - (height * 0.35f)),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFF30363D),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF21262D),
            start = Offset(0f, centerY + (height * 0.35f)),
            end = Offset(width, centerY + (height * 0.35f)),
            strokeWidth = 1f
        )

        if (bands.isEmpty()) return@Canvas

        val stepX = width / (bands.size + 1)
        val points = mutableListOf<Offset>()
        points.add(Offset(0f, centerY))

        bands.forEachIndexed { index, band ->
            val x = stepX * (index + 1)
            val normalizedGain = if (isBypassed) 0f else (band.gainDb / 12f).coerceIn(-1f, 1f)
            // -12dB is bottom (higher Y), +12dB is top (lower Y)
            val y = centerY - (normalizedGain * (height * 0.42f))
            points.add(Offset(x, y))
        }
        points.add(Offset(width, centerY))

        // Build smooth curve path
        val path = Path()
        path.moveTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2f
            path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        // Fill area under curve
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isBypassed) Color(0xFF484F58).copy(alpha = 0.2f) else Color(0xFF00E5FF).copy(alpha = 0.35f),
                    if (isBypassed) Color(0xFF21262D).copy(alpha = 0.05f) else Color(0xFF7C4DFF).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw glowing line
        drawPath(
            path = path,
            color = if (isBypassed) Color(0xFF8B949E) else Color(0xFF00E5FF),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Draw node circles for each band
        for (i in 1..bands.size) {
            val pt = points[i]
            drawCircle(
                color = if (isBypassed) Color(0xFF8B949E) else Color(0xFF00E5FF),
                radius = 4f,
                center = pt
            )
            drawCircle(
                color = Color(0xFF0D1117),
                radius = 2f,
                center = pt
            )
        }
    }
}
