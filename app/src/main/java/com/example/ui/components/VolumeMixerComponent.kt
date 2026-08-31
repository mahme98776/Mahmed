package com.example.ui.components

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.log10
import kotlin.math.sin

/**
 * Modern Multi-Channel Volume Mixer UI allowing independent level adjustments,
 * solo/mute controls, live peak VU indicators, smart ducking depth, and balance presets
 * for the original background video audio and the newly generated AI voice track.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VolumeMixerCard(
    originalVolume: Float,
    dubVoiceVolume: Float,
    bgmVolume: Float = 0.25f,
    isOriginalMuted: Boolean = false,
    isDubMuted: Boolean = false,
    isSoloOriginal: Boolean = false,
    isSoloDub: Boolean = false,
    isVocalClarityActive: Boolean = false,
    isAutoDuckingEnabled: Boolean = true,
    isPlaying: Boolean = false,
    onOriginalVolumeChange: (Float) -> Unit,
    onDubVoiceVolumeChange: (Float) -> Unit,
    onBgmVolumeChange: ((Float) -> Unit)? = null,
    onToggleOriginalMute: () -> Unit,
    onToggleDubMute: () -> Unit,
    onToggleSoloOriginal: () -> Unit = {},
    onToggleSoloDub: () -> Unit = {},
    onToggleVocalClarity: () -> Unit = {},
    onToggleAutoDucking: ((Boolean) -> Unit)? = null,
    onTogglePlaybackPreview: (() -> Unit)? = null,
    onQuickAutoBalance: (() -> Unit)? = null,
    onResetDefaults: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Calculate effective audio levels accounting for mute/solo
    val effectiveOriginal = when {
        isSoloDub -> 0f
        isOriginalMuted -> 0f
        else -> originalVolume
    }

    val effectiveDub = when {
        isSoloOriginal -> 0f
        isDubMuted -> 0f
        else -> dubVoiceVolume * (if (isVocalClarityActive) 1.15f else 1.0f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("volume_mixer_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 1. Mixer Header with Title, Status & Playback Audition Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ميكسر التحكم الصوتي المستقل",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                        Text(
                            text = "موازنة صوت الفيديو الأصلي مع صوت الدبلجة والذكاء الاصطناعي",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (onTogglePlaybackPreview != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPlaying) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF6366F1).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (isPlaying) Color(0xFFEF4444) else Color(0xFF818CF8)),
                        modifier = Modifier
                            .clickable { onTogglePlaybackPreview() }
                            .testTag("mixer_audition_play_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف" else "معاينة",
                                tint = if (isPlaying) Color(0xFFFCA5A5) else Color(0xFFC7D2FE),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isPlaying) "إيقاف" else "معاينة 🎧",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlaying) Color(0xFFFCA5A5) else Color(0xFFC7D2FE)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Dual Channels (Track 1: Original Audio, Track 2: AI Voice)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ----------------------------------------------------
                // CHANNEL 1: Original Background Video Audio
                // ----------------------------------------------------
                MixerChannelStrip(
                    title = "صوت الفيديو والخلفية الأصلية",
                    subtitle = "المؤثرات الصوتية والموسيقى في المقطع الأصلي",
                    trackIcon = Icons.Default.VolumeUp,
                    accentColor = Color(0xFF38BDF8),
                    volume = originalVolume,
                    effectiveVolume = effectiveOriginal,
                    isMuted = isOriginalMuted,
                    isSolo = isSoloOriginal,
                    isPlaying = isPlaying,
                    sliderTestTag = "original_volume_slider",
                    muteTestTag = "mute_original_audio_btn",
                    soloTestTag = "solo_original_audio_btn",
                    onVolumeChange = onOriginalVolumeChange,
                    onToggleMute = onToggleOriginalMute,
                    onToggleSolo = onToggleSoloOriginal,
                    onReset = { onOriginalVolumeChange(0.25f) },
                    extraBadge = if (isAutoDuckingEnabled && effectiveDub > 0.05f) {
                        "تخفيض ذكي آلي -12dB"
                    } else null
                )

                // ----------------------------------------------------
                // CHANNEL 2: AI Dubbed Voice Track
                // ----------------------------------------------------
                MixerChannelStrip(
                    title = "صوت الدبلجة والذكاء الاصطناعي (AI Voice)",
                    subtitle = "مسار الصوت البشري المترجم والمولد بالذكاء الاصطناعي",
                    trackIcon = Icons.Default.RecordVoiceOver,
                    accentColor = Color(0xFFD0BCFF),
                    volume = dubVoiceVolume,
                    effectiveVolume = effectiveDub,
                    isMuted = isDubMuted,
                    isSolo = isSoloDub,
                    isPlaying = isPlaying,
                    sliderTestTag = "ai_voice_volume_slider",
                    muteTestTag = "mute_ai_voice_btn",
                    soloTestTag = "solo_ai_voice_btn",
                    onVolumeChange = onDubVoiceVolumeChange,
                    onToggleMute = onToggleDubMute,
                    onToggleSolo = onToggleSoloDub,
                    onReset = { onDubVoiceVolumeChange(1.15f) },
                    isVocalClarityActive = isVocalClarityActive,
                    onToggleVocalClarity = onToggleVocalClarity,
                    extraBadge = if (isVocalClarityActive) "+3dB معزز نبرة الكلام ✨" else null
                )

                // ----------------------------------------------------
                // CHANNEL 3 (Optional): Ambient BGM & SFX
                // ----------------------------------------------------
                if (onBgmVolumeChange != null) {
                    MixerChannelStrip(
                        title = "موسيقى ومؤثرات إضافية (BGM)",
                        subtitle = "موسيقى تصويرية محيطية مضافة للمشهد",
                        trackIcon = Icons.Default.MusicNote,
                        accentColor = Color(0xFFFFD993),
                        volume = bgmVolume,
                        effectiveVolume = if (isSoloOriginal || isSoloDub) 0f else bgmVolume,
                        isMuted = bgmVolume <= 0.01f,
                        isSolo = false,
                        isPlaying = isPlaying,
                        sliderTestTag = "bgm_volume_slider",
                        muteTestTag = "mute_bgm_btn",
                        soloTestTag = "solo_bgm_btn",
                        onVolumeChange = onBgmVolumeChange,
                        onToggleMute = {
                            if (bgmVolume > 0.01f) onBgmVolumeChange(0f) else onBgmVolumeChange(0.35f)
                        },
                        onToggleSolo = {},
                        onReset = { onBgmVolumeChange(0.35f) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Quick Mixing Presets Bar
            Text(
                text = "قوالب التوازن والمكساج السريعة:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCAC4D0)
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset 1: Voice Focus
                MixerPresetChip(
                    title = "🎙️ تركيز صوتي",
                    subtitle = "15% خلفية / 130% دبلجة",
                    isSelected = (originalVolume in 0.10f..0.20f) && (dubVoiceVolume in 1.25f..1.35f) && !isOriginalMuted,
                    onClick = {
                        onOriginalVolumeChange(0.15f)
                        onDubVoiceVolumeChange(1.30f)
                        if (isOriginalMuted) onToggleOriginalMute()
                        if (isDubMuted) onToggleDubMute()
                    },
                    testTag = "preset_voice_focus"
                )

                // Preset 2: Cinematic Mix
                MixerPresetChip(
                    title = "🎬 مكساج سينمائي",
                    subtitle = "35% خلفية / 110% دبلجة",
                    isSelected = (originalVolume in 0.30f..0.40f) && (dubVoiceVolume in 1.05f..1.15f) && !isOriginalMuted,
                    onClick = {
                        onOriginalVolumeChange(0.35f)
                        onDubVoiceVolumeChange(1.10f)
                        if (isOriginalMuted) onToggleOriginalMute()
                        if (isDubMuted) onToggleDubMute()
                    },
                    testTag = "preset_cinematic"
                )

                // Preset 3: Balanced
                MixerPresetChip(
                    title = "⚖️ توازن قياسي",
                    subtitle = "25% خلفية / 100% دبلجة",
                    isSelected = (originalVolume in 0.20f..0.28f) && (dubVoiceVolume in 0.95f..1.05f) && !isOriginalMuted,
                    onClick = {
                        onOriginalVolumeChange(0.25f)
                        onDubVoiceVolumeChange(1.00f)
                        if (isOriginalMuted) onToggleOriginalMute()
                        if (isDubMuted) onToggleDubMute()
                    },
                    testTag = "preset_balanced"
                )

                // Preset 4: Voice Only (Original Muted)
                MixerPresetChip(
                    title = "🔇 صوت الدبلجة فقط",
                    subtitle = "0% كتم / 100% دبلجة",
                    isSelected = (originalVolume <= 0.02f || isOriginalMuted),
                    onClick = {
                        onOriginalVolumeChange(0.0f)
                        onDubVoiceVolumeChange(1.00f)
                    },
                    testTag = "preset_voice_only"
                )
            }

            Spacer(Modifier.height(14.dp))

            // 4. Utility Bottom Action Bar (Smart Auto-Balance & Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onQuickAutoBalance != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF381E72),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                        modifier = Modifier
                            .clickable { onQuickAutoBalance() }
                            .testTag("mixer_smart_auto_balance_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = null,
                                tint = Color(0xFFFFD993),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "موازنة ذكية بالذكاء الاصطناعي ⚡",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD993)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF25232A),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .clickable { onResetDefaults() }
                        .testTag("mixer_reset_defaults_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "استعادة الافتراضي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable strip for an individual audio channel with fader, level readout in dB,
 * solo/mute controls, and a simulated live VU level meter.
 */
@Composable
private fun MixerChannelStrip(
    title: String,
    subtitle: String,
    trackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    volume: Float,
    effectiveVolume: Float,
    isMuted: Boolean,
    isSolo: Boolean,
    isPlaying: Boolean,
    sliderTestTag: String,
    muteTestTag: String,
    soloTestTag: String,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onReset: () -> Unit,
    isVocalClarityActive: Boolean = false,
    onToggleVocalClarity: (() -> Unit)? = null,
    extraBadge: String? = null
) {
    val dbValue = volumeToDbString(volume)
    val percentage = (volume * 100).toInt()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF27242E),
        border = BorderStroke(
            1.dp,
            if (isSolo) Color(0xFFFFD993) else if (isMuted) Color(0xFFEF4444).copy(alpha = 0.5f) else accentColor.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Track Header: Title, Level & dB value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = trackIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) Color(0xFF94A3B8) else Color(0xFFF1F5F9)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Volume percentage & dB readout badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMuted) Color(0xFF332025) else accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isMuted) Color(0xFFEF4444) else accentColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMuted) "مكتوم (Mute)" else "$percentage% ($dbValue)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) Color(0xFFFCA5A5) else accentColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Live VU Meter & Fader Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Dynamic VU Meter Bar
                MixerVuMeter(
                    level = if (isPlaying && !isMuted) effectiveVolume else 0f,
                    accentColor = accentColor,
                    modifier = Modifier
                        .width(44.dp)
                        .height(20.dp)
                )

                Spacer(Modifier.width(8.dp))

                // Interactive Fader Slider
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..if (accentColor == Color(0xFFD0BCFF)) 2.0f else 1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isMuted) Color(0xFF94A3B8) else accentColor,
                        activeTrackColor = if (isMuted) Color(0xFF475569) else accentColor,
                        inactiveTrackColor = Color(0xFF332E3C)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(sliderTestTag)
                )
            }

            // Quick Control Buttons (Mute, Solo, Clarity Enhancer, Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Mute Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMuted) Color(0xFF7F1D1D) else Color(0xFF2E2B35),
                        border = BorderStroke(1.dp, if (isMuted) Color(0xFFEF4444) else Color(0xFF49454F)),
                        modifier = Modifier
                            .clickable { onToggleMute() }
                            .testTag(muteTestTag)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeMute,
                                contentDescription = if (isMuted) "إلغاء الكتم" else "كتم",
                                tint = if (isMuted) Color.White else Color(0xFFCAC4D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (isMuted) "مكتوم 🔇" else "كتم",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMuted) Color.White else Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // Solo Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSolo) Color(0xFF78350F) else Color(0xFF2E2B35),
                        border = BorderStroke(1.dp, if (isSolo) Color(0xFFFFD993) else Color(0xFF49454F)),
                        modifier = Modifier
                            .clickable { onToggleSolo() }
                            .testTag(soloTestTag)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "استماع منفرد",
                                tint = if (isSolo) Color(0xFFFFD993) else Color(0xFFCAC4D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (isSolo) "منفرد (Solo) 🎧" else "عزل (Solo)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSolo) Color(0xFFFFD993) else Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // Vocal Clarity Button (For Voice Channel)
                    if (onToggleVocalClarity != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isVocalClarityActive) Color(0xFF381E72) else Color(0xFF2E2B35),
                            border = BorderStroke(1.dp, if (isVocalClarityActive) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                            modifier = Modifier
                                .clickable { onToggleVocalClarity() }
                                .testTag("voice_clarity_toggle_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isVocalClarityActive) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "وضوح النبرة ✨",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVocalClarityActive) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                                )
                            }
                        }
                    }
                }

                // Reset Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2E2B35),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onReset() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "استعادة",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Extra Info Badge (e.g. Ducking or Clarity active)
            if (extraBadge != null) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ $extraBadge",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated VU Level Meter simulation with multi-segment LEDs (Green -> Amber -> Red).
 */
@Composable
private fun MixerVuMeter(
    level: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vu_anim")
    val waveMod by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    val activeFraction = (level * waveMod).coerceIn(0f, 1f)
    val totalSegments = 8
    val litSegments = (activeFraction * totalSegments).toInt()

    Row(
        modifier = modifier
            .background(Color(0xFF1E1B24), RoundedCornerShape(4.dp))
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSegments) { index ->
            val isLit = index < litSegments
            val segmentColor = when {
                index >= totalSegments - 1 -> Color(0xFFEF4444) // Red peak
                index >= totalSegments - 3 -> Color(0xFFF59E0B) // Amber
                else -> Color(0xFF10B981) // Green
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isLit) segmentColor else segmentColor.copy(alpha = 0.18f))
            )
        }
    }
}

/**
 * Preset Chip for One-Tap Audio Balance Styles
 */
@Composable
private fun MixerPresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF381E72) else Color(0xFF27242E),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFFE6E1E5)
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.8f) else Color(0xFF938F99)
            )
        }
    }
}

/**
 * Helper to format audio volume to approximate decibel readout.
 */
private fun volumeToDbString(vol: Float): String {
    if (vol <= 0.001f) return "-∞ dB"
    val db = 20.0 * log10(vol.toDouble())
    return if (db >= 0) "+%.1f dB".format(db) else "%.1f dB".format(db)
}
