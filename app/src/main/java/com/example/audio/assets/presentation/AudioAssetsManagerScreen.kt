package com.example.audio.assets.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.assets.domain.model.AudioAssetCategory
import com.example.audio.assets.domain.model.AudioAssetItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioAssetsManagerScreen(
    viewModel: AudioAssetsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val sttState by viewModel.sttState.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "إدارة أصول الصوت و ExoPlayer 🎚️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Clean Architecture • معالجة أوفلاين بدون إنترنت 100%",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssets() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الأصول")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Persistent ExoPlayer mini player bar when media is loaded
            if (playbackState.currentItem != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = playbackState.currentItem?.category?.iconEmoji ?: "🎵",
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = playbackState.currentItem?.titleArabic ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatDuration(playbackState.currentPositionMs)} / ${formatDuration(playbackState.durationMs)} • ExoPlayer",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier.testTag("exo_play_pause_button")
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل أو إيقاف مؤقت",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.stopPlayback() },
                                    modifier = Modifier.testTag("exo_stop_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "إيقاف",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Playback timeline slider
                        val maxDur = playbackState.durationMs.coerceAtLeast(1L).toFloat()
                        val currentPos = playbackState.currentPositionMs.coerceIn(0L, playbackState.durationMs).toFloat()

                        Slider(
                            value = currentPos,
                            onValueChange = { newPos ->
                                viewModel.seekTo(newPos.toLong())
                            },
                            valueRange = 0f..maxDur,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Notice / Error Banner
            AnimatedVisibility(visible = uiState.userNoticeMessage != null || uiState.errorMessage != null) {
                Surface(
                    color = if (uiState.errorMessage != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.errorMessage ?: uiState.userNoticeMessage ?: "",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.errorMessage != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearNotice() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tabs for navigation between Asset Management and Offline AI Services
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("أصول الصوت (Assets)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("نص لصوت أوفلاين (TTS)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("صوت لنص أوفلاين (STT)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTabIndex) {
                0 -> AssetsListView(viewModel, uiState, playbackState)
                1 -> OfflineTtsView(viewModel, uiState)
                2 -> OfflineSttView(viewModel, uiState, sttState)
            }
        }
    }
}

@Composable
private fun AssetsListView(
    viewModel: AudioAssetsViewModel,
    uiState: AudioAssetsManagerUiState,
    playbackState: com.example.audio.assets.domain.model.AssetPlaybackState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Category Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("بحث في ملفات الأصول الصوتية...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("asset_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Category horizontal chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("الكل (${uiState.assets.size})", fontSize = 12.sp) }
                    )
                }
                items(AudioAssetCategory.values()) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text("${cat.iconEmoji} ${cat.titleArabic}", fontSize = 12.sp) }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد ملفات صوتية مطابقة في مجلد الأصول.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filteredAssets) { asset ->
                    val isPlayingThis = playbackState.currentItem?.assetPath == asset.assetPath && playbackState.isPlaying

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayingThis) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isPlayingThis) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playAsset(asset) }
                            .testTag("asset_item_${asset.fileName}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isPlayingThis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = asset.titleArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${asset.fileName} • ${asset.sizeBytes / 1024} KB • ${asset.category.titleArabic}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { viewModel.transcribeAssetAudio(asset) },
                                    modifier = Modifier.testTag("transcribe_asset_btn_${asset.fileName}")
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("تحويل لنص", fontSize = 11.sp)
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
private fun OfflineTtsView(
    viewModel: AudioAssetsViewModel,
    uiState: AudioAssetsManagerUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "تحويل النص إلى صوت محلي 100% (Offline TTS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.ttsInputText,
                        onValueChange = { viewModel.setTtsInputText(it) },
                        label = { Text("أدخل النص المراد تحويله إلى صوت") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("offline_tts_text_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Language Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedLanguageCode == "ar",
                            onClick = { viewModel.setLanguage("ar") },
                            label = { Text("العربية 🇸🇦") }
                        )
                        FilterChip(
                            selected = uiState.selectedLanguageCode == "en",
                            onClick = { viewModel.setLanguage("en") },
                            label = { Text("الإنجليزية 🇺🇸") }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Pitch & Speed Sliders
                    Text(
                        text = "طبقة الصوت (Pitch): ${String.format("%.2f", uiState.ttsPitch)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = uiState.ttsPitch,
                        onValueChange = { viewModel.setTtsPitch(it) },
                        valueRange = 0.5f..1.5f
                    )

                    Text(
                        text = "سرعة النطق (Speed): ${String.format("%.2f", uiState.ttsSpeed)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = uiState.ttsSpeed,
                        onValueChange = { viewModel.setTtsSpeed(it) },
                        valueRange = 0.5f..1.5f
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.speakTtsDirectly() },
                            modifier = Modifier.weight(1f).testTag("offline_tts_speak_btn")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("نطق مباشر")
                        }

                        androidx.compose.material3.Button(
                            onClick = { viewModel.generateOfflineTtsWav() },
                            enabled = !uiState.isTtsSynthesizing,
                            modifier = Modifier.weight(1f).testTag("offline_tts_gen_wav_btn")
                        ) {
                            if (uiState.isTtsSynthesizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("توليد WAV وحفظه")
                            }
                        }
                    }
                }
            }
        }

        if (uiState.lastGeneratedWavFile != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("آخر ملف WAV تم توليده:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text(
                                text = uiState.lastGeneratedWavFile!!.name,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.FilledTonalButton(
                            onClick = { viewModel.playFile(uiState.lastGeneratedWavFile!!, "WAV") }
                        ) {
                            Text("تشغيل")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineSttView(
    viewModel: AudioAssetsViewModel,
    uiState: AudioAssetsManagerUiState,
    sttState: com.example.audio.assets.domain.service.OfflineSttState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "التعرف على الصوت محلياً (Offline STT)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Microphone Action Button
                    Surface(
                        onClick = {
                            if (sttState.isListening) {
                                viewModel.stopOfflineVoiceRecognition()
                            } else {
                                viewModel.startOfflineVoiceRecognition()
                            }
                        },
                        shape = CircleShape,
                        color = if (sttState.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("offline_stt_mic_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (sttState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "الميكروفون",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (sttState.isListening) "جاري الاستماع محلياً... تحدث الآن" else "انقر لبدء التحدث والتعرف على الصوت محلياً",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = if (sttState.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    // Live partial / recognized text
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "النص الملتقط محلياً:",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (sttState.partialText.isNotBlank()) sttState.partialText else if (uiState.sttTranscribedResult.isNotBlank()) uiState.sttTranscribedResult else "(في انتظار الصوت...)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
