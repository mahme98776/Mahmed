package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NormalizationMode
import com.example.audio.TrackLoudnessProfile
import com.example.audio.VolumeNormalizationResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VolumeNormalizationSheet(
    isAnalyzing: Boolean,
    currentOriginalVol: Float,
    currentDubVol: Float,
    currentBgmVol: Float,
    selectedMode: NormalizationMode,
    isAutoDuckingEnabled: Boolean,
    normalizationResult: VolumeNormalizationResult?,
    voiceProfile: TrackLoudnessProfile?,
    backgroundProfile: TrackLoudnessProfile?,
    hasRecordedAudio: Boolean,
    onSelectMode: (NormalizationMode) -> Unit,
    onToggleAutoDucking: (Boolean) -> Unit,
    onApplyBalancedVolumes: (Float, Float, Float) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1A24),
        contentColor = Color(0xFFE6E1E5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF381E72),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.8f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "موازنة مستوى الصوت التلقائية",
                            color = Color(0xFFE6E1E5),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto Volume Normalization & Ducking ⚖️",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_normalization_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFFCAC4D0)
                    )
                }
            }

            // Real-Time Loudness Level Meter Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282430)),
                border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تحليل مستويات الطاقة الصوتية (LUFS / dBFS)",
                                color = Color(0xFFE6E1E5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isAnalyzing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Color(0xFFD0BCFF),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("جاري القياس...", color = Color(0xFFD0BCFF), fontSize = 10.sp)
                            }
                        }
                    }

                    // Original Audio Track Meter
                    val originalPeak = backgroundProfile?.peakDb ?: -16.0f
                    val originalRms = backgroundProfile?.rmsDb ?: -20.0f
                    LoudnessMeterRow(
                        label = "صوت الفيديو / الموسيقى الخلفية",
                        icon = Icons.Default.MusicNote,
                        iconTint = Color(0xFF80CBC4),
                        peakDb = originalPeak,
                        rmsDb = originalRms,
                        volumePercent = (currentOriginalVol * 100).toInt(),
                        tag = "bg_audio_meter"
                    )

                    // Dubbed Voice Meter
                    val voicePeak = voiceProfile?.peakDb ?: -12.0f
                    val voiceRms = voiceProfile?.rmsDb ?: -16.0f
                    LoudnessMeterRow(
                        label = if (hasRecordedAudio) "صوت الدبلجة المضاف (صوت AI / ملف)" else "مسار صوت الدبلجة",
                        icon = Icons.Default.GraphicEq,
                        iconTint = Color(0xFFD0BCFF),
                        peakDb = voicePeak,
                        rmsDb = voiceRms,
                        volumePercent = (currentDubVol * 100).toInt(),
                        tag = "voice_audio_meter"
                    )

                    // Dynamic Difference Gauge
                    if (normalizationResult != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF381E72).copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "فارق التمايز الصوتي: ${String.format("%.1f", normalizationResult.loudnessDiffDb)} dB",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "أولوية الصوت: +${String.format("%.1f", selectedMode.targetVoiceProminenceDb)} dB ✨",
                                    color = Color(0xFFFFD993),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Normalization Modes Selector
            Text(
                text = "اختر نمط الموازنة التلقائية 🎚️",
                color = Color(0xFFCAC4D0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NormalizationMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF381E72) else Color(0xFF282430),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectMode(mode) }
                            .testTag("norm_mode_${mode.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = mode.iconEmoji, fontSize = 15.sp)
                                Text(
                                    text = mode.titleArabic,
                                    color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFFE6E1E5),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = mode.descriptionArabic,
                                color = if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.85f) else Color(0xFF938F99),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            // Auto-Ducking Switch Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282430)),
                border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "التخفيض التلقائي للخلفية أثناء التحدث (Auto-Ducking)",
                            color = Color(0xFFE6E1E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يخفض صوت الموسيقى تلقائياً عندما يتحدث الممثل لضمان نقاء الكلمات",
                            color = Color(0xFFCAC4D0),
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isAutoDuckingEnabled,
                        onCheckedChange = onToggleAutoDucking,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF381E72),
                            checkedTrackColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.testTag("auto_ducking_switch")
                    )
                }
            }

            // Normalization Results & Calculated Targets Card
            if (normalizationResult != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2538)),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD993),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "المستويات المحسوبة المقترحة للمكس 🎛️",
                                color = Color(0xFFFFD993),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Calculated Volume Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VolumeBadge(
                                label = "صوت الدبلجة",
                                valuePercent = (normalizationResult.calculatedDubVolume * 100).toInt(),
                                color = Color(0xFFD0BCFF),
                                modifier = Modifier.weight(1f)
                            )
                            VolumeBadge(
                                label = "صوت المشهد",
                                valuePercent = (normalizationResult.calculatedOriginalVolume * 100).toInt(),
                                color = Color(0xFF80CBC4),
                                modifier = Modifier.weight(1f)
                            )
                            VolumeBadge(
                                label = "موسيقى BGM",
                                valuePercent = (normalizationResult.calculatedBgmVolume * 100).toInt(),
                                color = Color(0xFFFFD993),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = normalizationResult.summaryArabic,
                            color = Color(0xFFE6E1E5),
                            fontSize = 11.sp
                        )

                        Text(
                            text = normalizationResult.recommendationArabic,
                            color = Color(0xFFCAC4D0),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onResetDefaults,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF79747E)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reset_normalization_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFCAC4D0),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "إعادة ضبط",
                        color = Color(0xFFCAC4D0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val targetDub = normalizationResult?.calculatedDubVolume ?: 1.15f
                        val targetOrig = normalizationResult?.calculatedOriginalVolume ?: 0.25f
                        val targetBgm = normalizationResult?.calculatedBgmVolume ?: 0.25f
                        onApplyBalancedVolumes(targetOrig, targetDub, targetBgm)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                    modifier = Modifier
                        .weight(2.2f)
                        .height(48.dp)
                        .testTag("apply_normalization_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "تطبيق الموازنة التلقائية ⚡",
                        color = Color(0xFF381E72),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LoudnessMeterRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    peakDb: Float,
    rmsDb: Float,
    volumePercent: Int,
    tag: String
) {
    val normalizedPeak = ((peakDb + 40f) / 40f).coerceIn(0f, 1f)
    val animatedPeak by animateFloatAsState(
        targetValue = normalizedPeak,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "peak"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = label, color = Color(0xFFE6E1E5), fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "الحالي: $volumePercent%",
                    color = iconTint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(${String.format("%.1f", rmsDb)} dB)",
                    color = Color(0xFF938F99),
                    fontSize = 10.sp
                )
            }
        }

        // dB Meter Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1A24))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPeak)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFFFFEB3B),
                                if (peakDb > -2f) Color(0xFFF44336) else Color(0xFFFF9800)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun VolumeBadge(
    label: String,
    valuePercent: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1A24),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = Color(0xFFCAC4D0), fontSize = 10.sp)
            Spacer(Modifier.height(2.dp))
            Text(text = "$valuePercent%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
