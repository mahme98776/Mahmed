package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.BgmStyle
import com.example.audio.VoiceEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MixerControlSheet(
    originalVolume: Float,
    dubVolume: Float,
    bgmVolume: Float,
    selectedVoiceEffect: VoiceEffect,
    selectedBgmStyle: BgmStyle,
    isOriginalMuted: Boolean = false,
    isDubMuted: Boolean = false,
    isSoloOriginal: Boolean = false,
    isSoloDub: Boolean = false,
    isVocalClarityActive: Boolean = false,
    isPlaying: Boolean = false,
    onOriginalVolumeChange: (Float) -> Unit,
    onDubVolumeChange: (Float) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleOriginalMute: () -> Unit = {},
    onToggleDubMute: () -> Unit = {},
    onToggleSoloOriginal: () -> Unit = {},
    onToggleSoloDub: () -> Unit = {},
    onToggleVocalClarity: () -> Unit = {},
    onVoiceEffectSelect: (VoiceEffect) -> Unit,
    onBgmStyleSelect: (BgmStyle) -> Unit,
    onTogglePlaybackPreview: (() -> Unit)? = null,
    onOpenEffectsLibrary: () -> Unit = {},
    onOpenNormalization: () -> Unit = {},
    onQuickAutoBalance: () -> Unit = {},
    onResetDefaults: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1B24),
        contentColor = Color(0xFFE6E1E5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ميكسر واستوديو التحكم الصوتي",
                            color = Color(0xFFE6E1E5),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحكم مستقل في مستويات صوت المشهد والدبلجة",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFFCAC4D0))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Dedicated Multi-Channel Volume Mixer Component
            VolumeMixerCard(
                originalVolume = originalVolume,
                dubVoiceVolume = dubVolume,
                bgmVolume = bgmVolume,
                isOriginalMuted = isOriginalMuted,
                isDubMuted = isDubMuted,
                isSoloOriginal = isSoloOriginal,
                isSoloDub = isSoloDub,
                isVocalClarityActive = isVocalClarityActive,
                isPlaying = isPlaying,
                onOriginalVolumeChange = onOriginalVolumeChange,
                onDubVoiceVolumeChange = onDubVolumeChange,
                onBgmVolumeChange = onBgmVolumeChange,
                onToggleOriginalMute = onToggleOriginalMute,
                onToggleDubMute = onToggleDubMute,
                onToggleSoloOriginal = onToggleSoloOriginal,
                onToggleSoloDub = onToggleSoloDub,
                onToggleVocalClarity = onToggleVocalClarity,
                onTogglePlaybackPreview = onTogglePlaybackPreview,
                onQuickAutoBalance = onQuickAutoBalance,
                onResetDefaults = onResetDefaults
            )

            Spacer(Modifier.height(16.dp))

            // Voice Effects Presets Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مؤثر مغير الصوت (Voice Changer)",
                    color = Color(0xFFCAC4D0),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF381E72),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                    modifier = Modifier
                        .clickable { onOpenEffectsLibrary() }
                        .testTag("open_full_fx_lib_from_mixer")
                ) {
                    Text(
                        text = "المكتبة الشاملة (16 مؤثر) 🎛️",
                        color = Color(0xFFD0BCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceEffect.values().forEach { effect ->
                    val isSelected = selectedVoiceEffect == effect
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2B2930),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                        modifier = Modifier
                            .clickable { onVoiceEffectSelect(effect) }
                            .testTag("voice_effect_${effect.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = effect.titleArabic,
                                color = if (isSelected) Color(0xFF381E72) else Color(0xFFE6E1E5),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = effect.subtitleArabic,
                                color = if (isSelected) Color(0xFF381E72).copy(alpha = 0.8f) else Color(0xFF938F99),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ambient BGM Selection
            Text(
                text = "نمط الموسيقى التصويرية",
                color = Color(0xFFCAC4D0),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BgmStyle.values().forEach { style ->
                    val isSelected = selectedBgmStyle == style
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF4A4458) else Color(0xFF2B2930),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onBgmStyleSelect(style) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = style.iconEmoji, fontSize = 18.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = style.titleArabic,
                                color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
