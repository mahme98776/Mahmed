package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AudioEffectsLibrarySheet(
    selectedEffect: AudioEffectItem,
    currentParams: AudioEffectParameters,
    isPlayingPreview: Boolean,
    isProcessing: Boolean,
    hasRecordedAudio: Boolean,
    onSelectEffect: (AudioEffectItem) -> Unit,
    onUpdateParams: (AudioEffectParameters) -> Unit,
    onTogglePreview: () -> Unit,
    onApplyToTake: () -> Unit,
    onCleanupStatic: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf(AudioEffectCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showCustomTuner by remember { mutableStateOf(true) }

    val filteredEffects = remember(selectedCategory, searchQuery) {
        AudioEffectsLibrary.effects.filter { item ->
            val matchesCategory = (selectedCategory == AudioEffectCategory.ALL || item.category == selectedCategory)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.titleArabic.contains(searchQuery, ignoreCase = true) ||
                item.subtitleArabic.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1B1F),
        contentColor = Color(0xFFE6E1E5),
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF381E72)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎛️", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مكتبة المؤثرات الصوتية الحية",
                            color = Color(0xFFE6E1E5),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تطبيق فوري لصدى الصوت، النبرات، والروبوت",
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_effects_library_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFFCAC4D0)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Search Bar & Filter Categories
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المؤثرات (صدى، روبوت، بتش...)", fontSize = 12.sp, color = Color(0xFF938F99)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFF49454F),
                        focusedContainerColor = Color(0xFF2B2930),
                        unfocusedContainerColor = Color(0xFF2B2930)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AudioEffectCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2B2930),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .testTag("category_chip_${category.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.iconEmoji, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = category.titleArabic,
                                color = if (isSelected) Color(0xFF381E72) else Color(0xFFE6E1E5),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main Scrollable Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Effects Grid / Flow
                item {
                    Text(
                        text = "المؤثرات المتاحة (${filteredEffects.size})",
                        color = Color(0xFFD0BCFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                }

                items(filteredEffects) { effect ->
                    val isSelected = selectedEffect.id == effect.id
                    EffectItemCard(
                        effect = effect,
                        isSelected = isSelected,
                        onSelect = {
                            onSelectEffect(effect)
                            onUpdateParams(effect.defaultParams)
                        }
                    )
                }

                // Live Parameter Tuning Controls Section
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF25232A)),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "لوحة الضبط الحي للمؤثر: ${selectedEffect.titleArabic}",
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButtonWithIcon(
                                    text = "استعادة الافتراضي",
                                    icon = Icons.Default.Refresh,
                                    onClick = { onUpdateParams(selectedEffect.defaultParams) }
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Pitch Semitone Tuning Slider
                            PitchSliderControl(
                                currentParams = currentParams,
                                onParamsChange = onUpdateParams
                            )

                            Spacer(Modifier.height(10.dp))

                            // Speed Multiplier Slider
                            SpeedSliderControl(
                                currentParams = currentParams,
                                onParamsChange = onUpdateParams
                            )

                            // Show context-specific parameters
                            if (selectedEffect.category == AudioEffectCategory.REVERB_SPACE ||
                                currentParams.reverbRoomSize > 0.05f ||
                                currentParams.echoDelayMs > 0
                            ) {
                                Spacer(Modifier.height(10.dp))
                                ReverbAndEchoControls(
                                    currentParams = currentParams,
                                    onParamsChange = onUpdateParams
                                )
                            }

                            if (selectedEffect.category == AudioEffectCategory.SCI_FI_ROBOT ||
                                currentParams.robotModulationHz > 0f
                            ) {
                                Spacer(Modifier.height(10.dp))
                                RobotControls(
                                    currentParams = currentParams,
                                    onParamsChange = onUpdateParams
                                )
                            }

                            if (selectedEffect.category == AudioEffectCategory.VINTAGE_FILTERS ||
                                currentParams.distortionDrive > 0f ||
                                currentParams.highPassCutoffHz > 50f
                            ) {
                                Spacer(Modifier.height(10.dp))
                                FilterAndDistortionControls(
                                    currentParams = currentParams,
                                    onParamsChange = onUpdateParams
                                )
                            }

                            // Dry / Wet Mix Slider
                            Spacer(Modifier.height(10.dp))
                            DryWetMixControl(
                                currentParams = currentParams,
                                onParamsChange = onUpdateParams
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Bottom Action & Preview Bar
            Surface(
                color = Color(0xFF25232A),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Preview Live Waveform Indicator
                    if (isPlayingPreview) {
                        AnimatedSoundwaveBar()
                        Spacer(Modifier.height(10.dp))
                    }

                    // Noise Reduction / Clean Static Quick Action
                    if (hasRecordedAudio) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2B2930),
                            border = BorderStroke(1.dp, Color(0xFF80CBC4).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCleanupStatic() }
                                .testTag("cleanup_noise_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF80CBC4),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "تنظيف الضوضاء والتشويش (Noise Cleanup) 🧹",
                                            color = Color(0xFF80CBC4),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "إزالة الهسيس والضجيج الخلفي باستخدام خوارزمية ذكية",
                                            color = Color(0xFFCAC4D0),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF80CBC4).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "تنظيف ⚡",
                                        color = Color(0xFF80CBC4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instant Audition Button
                        Button(
                            onClick = onTogglePreview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingPreview) Color(0xFFFFB4AB) else Color(0xFF4A4458),
                                contentColor = if (isPlayingPreview) Color(0xFF690005) else Color(0xFFE6E1E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("preview_effect_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.Headphones,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isPlayingPreview) "إيقاف المعاينة" else "استمع للتأثير 🎧",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Apply to Recording Take Button
                        Button(
                            onClick = onApplyToTake,
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("apply_effect_btn")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = Color(0xFF381E72),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("جاري المعالجة...", fontSize = 12.sp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (hasRecordedAudio) "تطبيق على التسجيل ✨" else "اختيار كصوت افتراضي ✨",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectItemCard(
    effect: AudioEffectItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF381E72) else Color(0xFF2B2930),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("effect_card_${effect.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF4F378B) else Color(0xFF36343B)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = effect.iconEmoji, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = effect.titleArabic,
                        color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFFE6E1E5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD0BCFF)
                        ) {
                            Text(
                                text = "مفعل",
                                color = Color(0xFF381E72),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = effect.subtitleArabic,
                    color = if (isSelected) Color(0xFFE8DEF8) else Color(0xFFCAC4D0),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = effect.descriptionArabic,
                    color = if (isSelected) Color(0xFFCCC2DC) else Color(0xFF938F99),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // Selection Radio / Check
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PitchSliderControl(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎚️ طبقة الصوت (Pitch Shift):",
                color = Color(0xFFE6E1E5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currentParams.getPitchFormatted(),
                color = Color(0xFFD0BCFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentParams.pitchSemitones.toFloat(),
            onValueChange = { semitones ->
                val semi = semitones.roundToInt()
                val multiplier = (2.0.pow(semi.toDouble() / 12.0)).toFloat().coerceIn(0.5f, 2.0f)
                onParamsChange(
                    currentParams.copy(
                        pitchSemitones = semi,
                        pitchMultiplier = multiplier
                    )
                )
            },
            valueRange = -12f..12f,
            steps = 23,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFD0BCFF),
                activeTrackColor = Color(0xFFD0BCFF),
                inactiveTrackColor = Color(0xFF49454F)
            ),
            modifier = Modifier.testTag("pitch_slider")
        )
    }
}

@Composable
private fun SpeedSliderControl(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ سرعة الإلقاء (Speed):",
                color = Color(0xFFE6E1E5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.2f", currentParams.speedMultiplier)}x",
                color = Color(0xFFFFD993),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentParams.speedMultiplier,
            onValueChange = { speed ->
                onParamsChange(currentParams.copy(speedMultiplier = (speed * 20).roundToInt() / 20f))
            },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD993),
                activeTrackColor = Color(0xFFFFD993),
                inactiveTrackColor = Color(0xFF49454F)
            ),
            modifier = Modifier.testTag("speed_slider")
        )
    }
}

@Composable
private fun ReverbAndEchoControls(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        // Reverb Room Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🏛️ حجم الغرفة والارتداد (Reverb):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
            Text("${(currentParams.reverbRoomSize * 100).toInt()}%", color = Color(0xFF80CBC4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.reverbRoomSize,
            onValueChange = { onParamsChange(currentParams.copy(reverbRoomSize = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF80CBC4),
                activeTrackColor = Color(0xFF80CBC4),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )

        // Echo Delay Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🔁 زمن تأخير الإيكو (Echo Delay):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
            Text("${currentParams.echoDelayMs} ms", color = Color(0xFF90CAF9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.echoDelayMs.toFloat(),
            onValueChange = { onParamsChange(currentParams.copy(echoDelayMs = it.toInt())) },
            valueRange = 0f..800f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF90CAF9),
                activeTrackColor = Color(0xFF90CAF9),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )

        // Echo Feedback
        if (currentParams.echoDelayMs > 20) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔄 عدد التكرارات (Feedback):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
                Text("${(currentParams.echoFeedback * 100).toInt()}%", color = Color(0xFF90CAF9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentParams.echoFeedback,
                onValueChange = { onParamsChange(currentParams.copy(echoFeedback = it)) },
                valueRange = 0f..0.85f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF90CAF9),
                    activeTrackColor = Color(0xFF90CAF9),
                    inactiveTrackColor = Color(0xFF49454F)
                )
            )
        }
    }
}

@Composable
private fun RobotControls(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🤖 تردد تضمين الروبوت (Modulation):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
            Text("${currentParams.robotModulationHz.toInt()} Hz", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.robotModulationHz,
            onValueChange = { onParamsChange(currentParams.copy(robotModulationHz = it)) },
            valueRange = 0f..280f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF80AB),
                activeTrackColor = Color(0xFFFF80AB),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("⚙️ رنين المعدن (Resonance):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
            Text("${(currentParams.robotResonance * 100).toInt()}%", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.robotResonance,
            onValueChange = { onParamsChange(currentParams.copy(robotResonance = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF80AB),
                activeTrackColor = Color(0xFFFF80AB),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )
    }
}

@Composable
private fun FilterAndDistortionControls(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("📻 تشبع وحشرجة الراديو (Distortion):", color = Color(0xFFE6E1E5), fontSize = 12.sp)
            Text("${(currentParams.distortionDrive * 100).toInt()}%", color = Color(0xFFFFCC80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.distortionDrive,
            onValueChange = { onParamsChange(currentParams.copy(distortionDrive = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFCC80),
                activeTrackColor = Color(0xFFFFCC80),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )
    }
}

@Composable
private fun DryWetMixControl(
    currentParams: AudioEffectParameters,
    onParamsChange: (AudioEffectParameters) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🎛️ نسبة المؤثر (Dry / Wet Mix):", color = Color(0xFFE6E1E5), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("${(currentParams.dryWetMix * 100).toInt()}%", color = Color(0xFFD0BCFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = currentParams.dryWetMix,
            onValueChange = { onParamsChange(currentParams.copy(dryWetMix = it)) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFD0BCFF),
                activeTrackColor = Color(0xFFD0BCFF),
                inactiveTrackColor = Color(0xFF49454F)
            )
        )
    }
}

@Composable
private fun AnimatedSoundwaveBar() {
    val transition = rememberInfiniteTransition(label = "wave")
    val heights = (0..18).map { i ->
        val anim by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(400 + (i * 45) % 350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
        anim
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("معاينة حية جارية...", color = Color(0xFF80CBC4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((20 * h).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF80CBC4), Color(0xFFD0BCFF))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun TextButtonWithIcon(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF36343B),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFCAC4D0), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = text, color = Color(0xFFCAC4D0), fontSize = 10.sp)
        }
    }
}
