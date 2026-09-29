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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceProfile
import com.example.audio.tts.CloudTtsConfig
import com.example.audio.tts.CloudTtsProvider

enum class VoiceCategoryFilter(val titleArabic: String, val iconEmoji: String) {
    ALL("جميع الأصوات", "🌐"),
    MALE_HEROIC("رجالي وبطولي", "🦸"),
    FEMALE_WARM("نسائي وناعم", "🌸"),
    ANIME_CARTOON("أنمي ومرح", "🧒"),
    DOCUMENTARY("وثائقي وإذاعي", "🎙️"),
    ENGLISH("English Dub", "🇺🇸")
}

/**
 * High-craft, Material 3 UI component allowing users to preview and select AI voice profiles
 * before initiating the automated video dubbing process.
 */
@Composable
fun AutoDubVoiceProfileSelector(
    voiceProfiles: List<VoiceProfile>,
    selectedVoiceId: String,
    currentlyPreviewingId: String?,
    isSpeaking: Boolean,
    cloudTtsConfig: CloudTtsConfig,
    onSelectVoice: (String) -> Unit,
    onPreviewVoice: (VoiceProfile) -> Unit,
    onStopPreview: () -> Unit,
    onOpenCloudTtsSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(VoiceCategoryFilter.ALL) }

    // Filter voice profiles by category
    val filteredProfiles = remember(voiceProfiles, selectedFilter) {
        when (selectedFilter) {
            VoiceCategoryFilter.ALL -> voiceProfiles
            VoiceCategoryFilter.MALE_HEROIC -> voiceProfiles.filter {
                it.id in listOf("hero_male", "natural_arabic_male", "male_narrator", "dramatic_villain", "en_action_hero")
            }
            VoiceCategoryFilter.FEMALE_WARM -> voiceProfiles.filter {
                it.id in listOf("heroine_female", "natural_arabic_female", "female_soft", "en_natural_female")
            }
            VoiceCategoryFilter.ANIME_CARTOON -> voiceProfiles.filter {
                it.id in listOf("cartoon_hero", "hero_male", "heroine_female", "cyber_bot", "en_action_hero")
            }
            VoiceCategoryFilter.DOCUMENTARY -> voiceProfiles.filter {
                it.id in listOf("epic_narrator", "male_narrator", "natural_arabic_male", "sports_hype", "en_natural_male")
            }
            VoiceCategoryFilter.ENGLISH -> voiceProfiles.filter {
                it.languageCode == "en" || it.id.startsWith("en_")
            }
        }
    }

    val activeSelectedProfile = remember(voiceProfiles, selectedVoiceId) {
        voiceProfiles.find { it.id == selectedVoiceId } ?: voiceProfiles.firstOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("auto_dub_voice_profile_selector_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF6366F1).copy(alpha = 0.6f),
                    Color(0xFF8B5CF6).copy(alpha = 0.4f),
                    Color(0xFFEC4899).copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "4. البصمات الصوتية الذكية (AI Voices)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "توليد فوري",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "عاين واستمع للأصوات واختر النبرة الأساسية لدبلجة المشهد",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cloud Engine Settings Pill
                IconButton(
                    onClick = onOpenCloudTtsSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        .testTag("btn_configure_cloud_tts_in_selector")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "إعدادات محرك الصوت",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Currently Selected Profile Banner
            if (activeSelectedProfile != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_selected_voice_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = activeSelectedProfile.emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                    .padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeSelectedProfile.titleArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF6366F1)
                                    ) {
                                        Text(
                                            text = "الصوت النشط للدبلجة ✓",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = activeSelectedProfile.subtitleArabic,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Quick Preview Button for Active Profile
                        val isThisPreviewing = currentlyPreviewingId == activeSelectedProfile.id && isSpeaking
                        Button(
                            onClick = {
                                if (isThisPreviewing) onStopPreview() else onPreviewVoice(activeSelectedProfile)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isThisPreviewing) Color(0xFFEF4444) else Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("preview_active_voice_button")
                        ) {
                            Icon(
                                imageVector = if (isThisPreviewing) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isThisPreviewing) "إيقاف" else "استماع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceCategoryFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "${filter.iconEmoji} ${filter.titleArabic}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF6366F1),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF6366F1) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Voice Profiles Horizontal / Vertical Grid Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredProfiles.forEach { profile ->
                    VoiceProfileItemCard(
                        profile = profile,
                        isSelected = profile.id == selectedVoiceId,
                        isPreviewing = currentlyPreviewingId == profile.id && isSpeaking,
                        onSelect = { onSelectVoice(profile.id) },
                        onTogglePreview = {
                            if (currentlyPreviewingId == profile.id && isSpeaking) {
                                onStopPreview()
                            } else {
                                onPreviewVoice(profile)
                            }
                        }
                    )
                }
            }

            // Cloud TTS Notice
            if (cloudTtsConfig.isEnabled && cloudTtsConfig.provider != CloudTtsProvider.DEVICE_TTS) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cloudTtsConfig.provider.iconEmoji, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محرك السحابي النشط: ${cloudTtsConfig.provider.titleArabic}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ستتم دبلجة الفيديو بأصوات نموذج Multilingual v2 فائق الدقة.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Voice Profile Item Card with preview wave animation and selection button.
 */
@Composable
private fun VoiceProfileItemCard(
    profile: VoiceProfile,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFF6366F1)
            isPreviewing -> Color(0xFF10B981)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        label = "voiceCardBorder"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFF6366F1).copy(alpha = 0.05f)
            isPreviewing -> Color(0xFF10B981).copy(alpha = 0.04f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        label = "voiceCardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("voice_profile_card_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected || isPreviewing) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar / Emoji
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
                            else
                                Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = profile.emoji, fontSize = 24.sp)
                }

                Spacer(Modifier.width(12.dp))

                // Profile Title & Description
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.titleArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "محدد",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = profile.subtitleArabic,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (profile.description.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = profile.description,
                            fontSize = 10.5.sp,
                            color = Color(0xFF6366F1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bottom Actions & Voice Specs (Pitch, Rate, Preview & Select buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Specifications badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = if (profile.languageCode == "en") "🇺🇸 English" else "🇸🇦 عربي فصيح",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "سرعة ${String.format(java.util.Locale.US, "%.1f", profile.speechRate)}x",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Equalizer wave animation if preview is playing
                    if (isPreviewing) {
                        AnimatedEqualizerWaveIndicator()
                    }
                }

                // Interactive Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Preview Button (Audition Voice)
                    OutlinedButton(
                        onClick = onTogglePreview,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isPreviewing) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPreviewing) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("preview_voice_btn_${profile.id}")
                    ) {
                        Icon(
                            imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPreviewing) "إيقاف" else "معاينة",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Select Button
                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF6366F1) else Color(0xFF6366F1).copy(alpha = 0.15f),
                            contentColor = if (isSelected) Color.White else Color(0xFF6366F1)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("select_voice_btn_${profile.id}")
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "محدد",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "اختيار الصوت",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Equalizer Wave Bars to provide visual tactile feedback during audio audition.
 */
@Composable
private fun AnimatedEqualizerWaveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizerWave")
    val height1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(height1.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(height2.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(height3.dp)
                .background(Color(0xFF10B981), CircleShape)
        )
    }
}
