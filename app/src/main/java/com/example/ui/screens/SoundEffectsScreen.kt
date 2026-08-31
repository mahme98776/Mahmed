package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.BgmStyle
import com.example.audio.SoundEffectsGenerator
import com.example.audio.VoiceEffect
import com.example.ui.DubbingViewModel
import com.example.ui.components.SoundboardPad
import com.example.ui.components.VoiceEffectPresetsSelector
import com.example.ui.components.VoicePresetType

@Composable
fun SoundEffectsScreen(
    viewModel: DubbingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD993),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF141218),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مكتبة الأصوات والمؤثرات (Soundboard)",
                        color = Color(0xFFE6E1E5),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مؤثرات سينمائية وضحكات وتصفيق لتضمينها في الدبلجة",
                        color = Color(0xFFCAC4D0),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Full Soundboard items
        item {
            SoundboardPad(onPlayEffect = { viewModel.playSoundEffect(it) })
        }

        // Voice Changers Presets Component ('Robot', 'Echo', 'Deep Voice', etc.)
        item {
            VoiceEffectPresetsSelector(
                selectedPreset = state.selectedVoicePreset,
                onSelectPreset = { preset ->
                    viewModel.selectVoicePreset(preset)
                },
                isPlayingPreview = state.isEffectsPreviewPlaying,
                isProcessing = state.isProcessingEffectDsp,
                onTogglePreview = {
                    viewModel.togglePresetPreview()
                },
                onApplyEffect = {
                    viewModel.applyVoicePreset(state.selectedVoicePreset)
                },
                showTuningControls = true,
                customParams = state.customEffectParams,
                onUpdateCustomParams = { newParams ->
                    viewModel.updateCustomEffectParams(newParams)
                }
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
        }
    }
}
