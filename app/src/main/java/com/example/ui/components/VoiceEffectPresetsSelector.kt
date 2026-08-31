package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEffectCategory
import com.example.audio.AudioEffectItem
import com.example.audio.AudioEffectParameters
import com.example.audio.AudioEffectsLibrary
import com.example.audio.VoiceEffect

/**
 * Preset data structure specifically highlighting primary requested voice effects
 * ('Robot', 'Echo', 'Deep Voice') alongside extended DSP presets.
 */
enum class VoicePresetType(
    val effectId: String,
    val titleEnglish: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val descriptionArabic: String,
    val iconEmoji: String,
    val colorPrimary: Color,
    val colorGradientEnd: Color,
    val voiceEffectEnum: VoiceEffect
) {
    ROBOT(
        effectId = "CYBER_ROBOT",
        titleEnglish = "Robot",
        titleArabic = "روبوت آلي",
        subtitleArabic = "تضمين حلقي معدني (Ring Modulation)",
        descriptionArabic = "نبرة سايبر إلكترونية مستقبلية مع رنين معدني عالي التردد لأفلام الخيال العلمي والشخصيات الآلية.",
        iconEmoji = "🤖",
        colorPrimary = Color(0xFF00E5FF),
        colorGradientEnd = Color(0xFF0D47A1),
        voiceEffectEnum = VoiceEffect.ROBOT
    ),
    ECHO(
        effectId = "STUDIO_ECHO",
        titleEnglish = "Echo",
        titleArabic = "استوديو وصدى",
        subtitleArabic = "تأخير تكراري (Delay & Echo)",
        descriptionArabic = "تردد وصدى تكراري إيقاعي مع تلاشٍ ناعم يمنح الصوت طابعاً إذاعياً ومسرحياً واسع المدى.",
        iconEmoji = "🔁",
        colorPrimary = Color(0xFF818CF8),
        colorGradientEnd = Color(0xFF4338CA),
        voiceEffectEnum = VoiceEffect.ECHO
    ),
    DEEP_VOICE(
        effectId = "CINEMATIC_DEEP",
        titleEnglish = "Deep Voice",
        titleArabic = "صوت جهوري سينمائي",
        subtitleArabic = "تضخيم الباس وتخفيض الطبقة",
        descriptionArabic = "طبقة عميقة وجهورية فخمة تعزز الترددات المنخفضة للأفلام الوثائقية والبطولية المهيبة.",
        iconEmoji = "🎬",
        colorPrimary = Color(0xFFF59E0B),
        colorGradientEnd = Color(0xFFB45309),
        voiceEffectEnum = VoiceEffect.DEEP
    ),
    CHIPMUNK(
        effectId = "CHIPMUNK_CARTOON",
        titleEnglish = "Chipmunk",
        titleArabic = "سنجاب وكرتون",
        subtitleArabic = "رفع التردد والسرعة",
        descriptionArabic = "صوت رفيع ومرح ومضحك مخصص للمشاهد الكرتونية والأنيمي والكوميديا السريعة.",
        iconEmoji = "🐿️",
        colorPrimary = Color(0xFFF43F5E),
        colorGradientEnd = Color(0xFF9F1239),
        voiceEffectEnum = VoiceEffect.CHIPMUNK
    ),
    VINTAGE_RADIO(
        effectId = "VINTAGE_RADIO",
        titleEnglish = "Vintage Radio",
        titleArabic = "مذياع كلاسيكي",
        subtitleArabic = "فلتر النطاق الترددي الضيق",
        descriptionArabic = "محاكاة صوت أجهزة اللاسلكي والراديو الكلاسيكي مع حشرجة وتأثيرات ترددية ضيقة.",
        iconEmoji = "📻",
        colorPrimary = Color(0xFF10B981),
        colorGradientEnd = Color(0xFF047857),
        voiceEffectEnum = VoiceEffect.RADIO
    ),
    NORMAL(
        effectId = "NORMAL",
        titleEnglish = "Normal / Clean",
        titleArabic = "صوت طبيعي خام",
        subtitleArabic = "بدون أي مؤثرات (Bypass)",
        descriptionArabic = "صوتك الحقيقي بجودته الأصلية ونقاء تسجيله الطبيعي دون معالجة أو تعديل.",
        iconEmoji = "🎙️",
        colorPrimary = Color(0xFF94A3B8),
        colorGradientEnd = Color(0xFF334155),
        voiceEffectEnum = VoiceEffect.NORMAL
    )
}

/**
 * Reusable, high-craft Jetpack Compose UI component for selecting and previewing
 * voice effect presets like 'Robot', 'Echo', and 'Deep Voice'.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceEffectPresetsSelector(
    selectedPreset: VoicePresetType,
    onSelectPreset: (VoicePresetType) -> Unit,
    modifier: Modifier = Modifier,
    isPlayingPreview: Boolean = false,
    isProcessing: Boolean = false,
    onTogglePreview: () -> Unit = {},
    onApplyEffect: () -> Unit = {},
    showTuningControls: Boolean = true,
    customParams: AudioEffectParameters? = null,
    onUpdateCustomParams: ((AudioEffectParameters) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_effect_presets_selector"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B24)
        ),
        border = BorderStroke(1.dp, Color(0xFF383544))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(selectedPreset.colorPrimary, selectedPreset.colorGradientEnd)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedPreset.iconEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(
                            text = "مؤثرات الصوت وفلاتر الدوبلاج",
                            color = Color(0xFFF1F5F9),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اختر من الفلاتر الجاهزة: Robot, Echo, Deep Voice",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Processing Indicator or Active Badge
                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF818CF8)
                        )
                        Text(
                            text = "معالجة DSP...",
                            color = Color(0xFFC7D2FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = selectedPreset.colorPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, selectedPreset.colorPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(selectedPreset.colorPrimary, CircleShape)
                            )
                            Text(
                                text = selectedPreset.titleEnglish,
                                color = selectedPreset.colorPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Presets Horizontal Scroller / Selector Cards
            Text(
                text = "المؤثرات الجاهزة (Voice Presets):",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(VoicePresetType.values()) { preset ->
                    val isSelected = selectedPreset == preset
                    val animatedBorderColor by animateColorAsState(
                        targetValue = if (isSelected) preset.colorPrimary else Color(0xFF332F42),
                        label = "border"
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF282436) else Color(0xFF191722),
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor),
                        modifier = Modifier
                            .width(135.dp)
                            .clickable { onSelectPreset(preset) }
                            .testTag("preset_${preset.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        color = if (isSelected) preset.colorPrimary.copy(alpha = 0.2f) else Color(0xFF221F2F),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.iconEmoji,
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = preset.titleEnglish,
                                color = if (isSelected) preset.colorPrimary else Color(0xFFE2E8F0),
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = preset.titleArabic,
                                color = if (isSelected) Color(0xFFCBD5E1) else Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(6.dp))

                            if (isSelected) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier
                                        .background(preset.colorPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = preset.colorPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "مفعل",
                                        color = preset.colorPrimary,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .height(18.dp)
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "اختيار",
                                        color = Color(0xFF64748B),
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Selected Preset Highlight Box with Detailed Info
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF15131C),
                border = BorderStroke(1.dp, selectedPreset.colorPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${selectedPreset.iconEmoji} ${selectedPreset.titleArabic} (${selectedPreset.titleEnglish})",
                                    color = selectedPreset.colorPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = selectedPreset.subtitleArabic,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = selectedPreset.descriptionArabic,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp
                            )
                        }

                        // Play / Stop Preview Button
                        Button(
                            onClick = onTogglePreview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingPreview) Color(0xFFEF4444) else selectedPreset.colorPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("preview_preset_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.VolumeUp,
                                    contentDescription = "معاينة الصوت",
                                    tint = if (isPlayingPreview) Color.White else Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isPlayingPreview) "إيقاف" else "تجربة",
                                    color = if (isPlayingPreview) Color.White else Color(0xFF0F172A),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // DSP Parameters Badges
                    val effectItem = remember(selectedPreset) {
                        AudioEffectsLibrary.findById(selectedPreset.effectId)
                    }
                    val params = customParams ?: effectItem.defaultParams

                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pitch badge
                        DspChip(
                            label = "الطبقة: ${params.getPitchFormatted()}",
                            icon = "🎚️"
                        )

                        // Reverb badge
                        if (params.reverbRoomSize > 0.05f) {
                            DspChip(
                                label = "صدى: ${(params.reverbRoomSize * 100).toInt()}%",
                                icon = "🏛️"
                            )
                        }

                        // Echo delay badge
                        if (params.echoDelayMs > 0) {
                            DspChip(
                                label = "ايكو: ${params.echoDelayMs}ms",
                                icon = "🔁"
                            )
                        }

                        // Robot Ring modulation badge
                        if (params.robotModulationHz > 0f) {
                            DspChip(
                                label = "تضمين آلي: ${params.robotModulationHz.toInt()}Hz",
                                icon = "🤖"
                            )
                        }

                        // Distortion / Grit badge
                        if (params.distortionDrive > 0.05f) {
                            DspChip(
                                label = "تشبع صوتي: ${(params.distortionDrive * 100).toInt()}%",
                                icon = "📻"
                            )
                        }
                    }
                }
            }

            // Optional Real-time DSP Parameter Tuning Sliders
            if (showTuningControls && onUpdateCustomParams != null) {
                val currentP = customParams ?: AudioEffectsLibrary.findById(selectedPreset.effectId).defaultParams
                var showSliders by remember { mutableStateOf(false) }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSliders = !showSliders }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تعديل المعلمات",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "تعديل تفاصيل المؤثر (Fine Tuning DSP)",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = if (showSliders) "إخفاء ▲" else "تعديل ▼",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                AnimatedVisibility(visible = showSliders) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFF131118), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // Pitch Multiplier Slider
                        Text(
                            text = "مضاعف النغمة (Pitch Multiplier): ${String.format("%.2f", currentP.pitchMultiplier)}x",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Slider(
                            value = currentP.pitchMultiplier,
                            onValueChange = { onUpdateCustomParams(currentP.copy(pitchMultiplier = it)) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = selectedPreset.colorPrimary,
                                activeTrackColor = selectedPreset.colorPrimary,
                                inactiveTrackColor = Color(0xFF332F42)
                            ),
                            modifier = Modifier.testTag("pitch_slider")
                        )

                        // Dry / Wet Mix Slider
                        Text(
                            text = "نسبة قوة المؤثر (Dry/Wet Mix): ${(currentP.dryWetMix * 100).toInt()}%",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Slider(
                            value = currentP.dryWetMix,
                            onValueChange = { onUpdateCustomParams(currentP.copy(dryWetMix = it)) },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = selectedPreset.colorPrimary,
                                activeTrackColor = selectedPreset.colorPrimary,
                                inactiveTrackColor = Color(0xFF332F42)
                            ),
                            modifier = Modifier.testTag("drywet_slider")
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action Buttons: Apply Preset to Project/Recording
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApplyEffect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedPreset.colorPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_preset_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تطبيق",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "اعتماد مؤثر ${selectedPreset.titleEnglish}",
                            color = Color(0xFF0F172A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DspChip(
    label: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF221F2F),
        border = BorderStroke(1.dp, Color(0xFF3B374E)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 10.sp)
            Text(
                text = label,
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
