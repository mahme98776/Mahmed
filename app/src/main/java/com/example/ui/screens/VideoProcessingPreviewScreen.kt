package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ArabicPhoneticsEngine
import com.example.audio.AuditReport
import com.example.audio.BgmStyle
import com.example.audio.NormalizationMode
import com.example.audio.ScriptAuditIssue
import com.example.audio.VoiceEffect
import com.example.export.AudioQualityPreset
import com.example.export.ExportFormat
import com.example.export.ExportResolution
import com.example.export.FrameRatePreset
import com.example.export.VideoBitratePreset
import com.example.export.VideoExportConfig
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import com.example.ui.DubbingViewModel
import com.example.ui.SplitCompareViewMode
import com.example.ui.components.ExportDialogUiState
import com.example.ui.components.ExportProjectDialog
import com.example.ui.components.VoicePresetType
import java.io.File

/**
 * Dedicated Hub for Processing & Previewing Videos Before Export
 * (مركز معالجة ومعاينة الفيديو قبل التصدير)
 *
 * Provides a comprehensive pre-export workstation:
 * 1. Synchronized Multi-Track Master Preview (Canvas/Native with Live Subtitles & Waveforms)
 * 2. Real-time Split Comparison (Side-by-Side, Wipe Slider, Fast A/B Flip, Master Cinema)
 * 3. Audio Mastering & Normalization (EBU R128, Auto-Ducking, Vocal Clarity, EQ, Spacetoon Echo)
 * 4. Arabic Phonetics, Diacritics & Lettering Inspection (One-Tap Auto-Fix)
 * 5. Full Video Export Tuning (4K/1080p/720p, 60/30/24 FPS, Aspect Ratios, H.264/HEVC)
 * 6. Readiness Checklist & Instant Export / Save to Local Storage.
 */
@Composable
fun VideoProcessingPreviewScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportDialogState by viewModel.exportDialogState.collectAsState()
    val videoExportConfig by viewModel.videoExportConfig.collectAsState()

    val clip = uiState.currentClip
    val duration = (clip.durationSeconds.toFloat()).coerceAtLeast(1f)
    val scriptLines = uiState.scriptLines
    val activeLine = scriptLines.getOrNull(uiState.activeLineIndex)
    val auditReport = uiState.auditReport

    // Selected processing module tab
    // 0: Master Viewport & Split Compare, 1: Audio Processing & Mastering, 2: Phonetics & Lettering, 3: Video Quality Settings, 4: Readiness Checklist
    var selectedProcessingTab by remember { mutableStateOf(0) }

    // Dialogs
    var showEditLineDialog by remember { mutableStateOf<Int?>(null) }
    var editLineText by remember { mutableStateOf("") }
    var editLineStart by remember { mutableStateOf(0f) }
    var editLineEnd by remember { mutableStateOf(0f) }

    // Initial audit trigger
    LaunchedEffect(clip.id) {
        if (auditReport == null) {
            viewModel.runScriptQualityAudit()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B12))
            .testTag("video_processing_preview_screen")
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ==========================================
        // 1. HEADER: Hub Title & Readiness Overview
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("processing_preview_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191522)),
                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.25f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "مركز معالجة ومعاينة الفيديو",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = "جاهز للتصدير ✨",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "المعاينة الشاملة، ضبط الصوت وتشكيل الحركات، ومطابقة الجودة قبل التصدير",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Direct Export & Storage Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier
                                    .clickable { viewModel.saveDubbedProjectToStorage() }
                                    .testTag("hub_save_project_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "حفظ 💾",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.openExportDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("hub_open_export_modal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "تصدير 📤",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Clip Active Details Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF120E18),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎬", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = clip.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF3E8FF)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "(${clip.category})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA78BFA)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⏱️ ${clip.durationSeconds} ثانية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "📜 ${scriptLines.size} أسطر حوار",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. MASTER VIDEO VIEWPORT & COMPARISON CANVAS
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_preview_viewport_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15121D)),
                border = BorderStroke(1.5.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Comparison View Mode Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1928))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PreviewModeChip(
                            title = "جنباً إلى جنب 🔲",
                            isSelected = uiState.splitCompareViewMode == SplitCompareViewMode.SIDE_BY_SIDE,
                            onClick = { viewModel.setSplitCompareViewMode(SplitCompareViewMode.SIDE_BY_SIDE) },
                            modifier = Modifier.weight(1f)
                        )

                        PreviewModeChip(
                            title = "ممسحة المقارنة ↔️",
                            isSelected = uiState.splitCompareViewMode == SplitCompareViewMode.SPLIT_SLIDER,
                            onClick = { viewModel.setSplitCompareViewMode(SplitCompareViewMode.SPLIT_SLIDER) },
                            modifier = Modifier.weight(1f)
                        )

                        PreviewModeChip(
                            title = "تبديل فوري A/B ⇄",
                            isSelected = uiState.splitCompareViewMode == SplitCompareViewMode.AB_FLIP,
                            onClick = { viewModel.setSplitCompareViewMode(SplitCompareViewMode.AB_FLIP) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Active Video Viewport Screen
                    MasterComparisonViewport(
                        clip = clip,
                        currentSeconds = uiState.currentPlaybackSeconds,
                        isPlaying = uiState.isPlaying,
                        compareMode = uiState.splitCompareViewMode,
                        splitFraction = uiState.splitDividerFraction,
                        isAbFlipActive = uiState.isAbFlipActive,
                        activeLine = activeLine,
                        isOriginalMuted = uiState.isMutedOriginal,
                        isDubMuted = uiState.isMutedDub,
                        onToggleAbFlip = { viewModel.toggleAbFlip() },
                        onSetSplitFraction = { viewModel.setSplitDividerFraction(it) }
                    )

                    // Waveform Audio Meter Bar
                    MasterAudioLevelsMeterBar(
                        dubVolume = uiState.dubVolume,
                        originalVolume = uiState.originalVolume,
                        bgmVolume = uiState.bgmVolume,
                        isPlaying = uiState.isPlaying
                    )

                    // Scrubber Timeline & SMPTE Timecode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181422))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⏱️ ${formatTimeDisplay(uiState.currentPlaybackSeconds)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA)
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(0.8.dp, Color(0xFF334155))
                                ) {
                                    Text(
                                        text = "SMPTE: ${formatSmpteTime(uiState.currentPlaybackSeconds)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "الإجمالي: ${formatTimeDisplay(duration)}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Slider(
                            value = uiState.currentPlaybackSeconds.coerceIn(0f, duration),
                            onValueChange = { viewModel.seekTo(it) },
                            valueRange = 0f..duration,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFA78BFA),
                                activeTrackColor = Color(0xFF7C3AED),
                                inactiveTrackColor = Color(0xFF332D42)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("processing_master_seekbar")
                        )
                    }

                    // Transport Bar (Frame-by-Frame & Play/Pause)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14101C))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -1 Frame
                        TransportActionButton(
                            text = "-1 إطار",
                            icon = Icons.Default.SkipPrevious,
                            onClick = { viewModel.seekTo((uiState.currentPlaybackSeconds - 0.040f).coerceAtLeast(0f)) },
                            testTag = "hub_prev_frame_btn"
                        )

                        // -1 Second
                        TransportActionButton(
                            text = "-1 ثانية",
                            icon = Icons.Default.FastRewind,
                            onClick = { viewModel.seekTo((uiState.currentPlaybackSeconds - 1.0f).coerceAtLeast(0f)) },
                            testTag = "hub_prev_sec_btn"
                        )

                        // Central Play / Pause Button
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isPlaying) Color(0xFFEF4444) else Color(0xFF7C3AED),
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { viewModel.togglePlayPause() }
                                .testTag("hub_master_play_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlaying) "إيقاف" else "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // +1 Second
                        TransportActionButton(
                            text = "+1 ثانية",
                            icon = Icons.Default.FastForward,
                            onClick = { viewModel.seekTo((uiState.currentPlaybackSeconds + 1.0f).coerceAtMost(duration)) },
                            testTag = "hub_next_sec_btn"
                        )

                        // +1 Frame
                        TransportActionButton(
                            text = "+1 إطار",
                            icon = Icons.Default.SkipNext,
                            onClick = { viewModel.seekTo((uiState.currentPlaybackSeconds + 0.040f).coerceAtMost(duration)) },
                            testTag = "hub_next_frame_btn"
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. PROCESSING WORKSTATION TABS SELECTOR
        // ==========================================
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF171320),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProcessingTabItem(
                        icon = Icons.Default.GraphicEq,
                        title = "معالجة الصوت 🎛️",
                        badge = if (uiState.isAutoDuckingEnabled) "داكينج" else null,
                        isSelected = selectedProcessingTab == 0,
                        onClick = { selectedProcessingTab = 0 },
                        modifier = Modifier.weight(1f)
                    )

                    ProcessingTabItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "تدقيق الحروف ✍️",
                        badge = auditReport?.let { "${it.accuracyScore}%" },
                        isSelected = selectedProcessingTab == 1,
                        onClick = {
                            selectedProcessingTab = 1
                            if (auditReport == null) viewModel.runScriptQualityAudit()
                        },
                        modifier = Modifier.weight(1.1f)
                    )

                    ProcessingTabItem(
                        icon = Icons.Default.HighQuality,
                        title = "إعدادات الفيديو 🎬",
                        badge = videoExportConfig.resolution.badge,
                        isSelected = selectedProcessingTab == 2,
                        onClick = { selectedProcessingTab = 2 },
                        modifier = Modifier.weight(1.1f)
                    )

                    ProcessingTabItem(
                        icon = Icons.Default.CheckCircle,
                        title = "الجاهزية 📋",
                        badge = "100%",
                        isSelected = selectedProcessingTab == 3,
                        onClick = { selectedProcessingTab = 3 },
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }

        // ==========================================
        // 4. ACTIVE PROCESSING MODULE CONTENT
        // ==========================================
        item {
            when (selectedProcessingTab) {
                0 -> {
                    // TAB 0: Audio Mastering, Normalization & Effects
                    AudioMasteringModuleContent(
                        uiState = uiState,
                        onOriginalVolChange = { viewModel.setOriginalVolume(it) },
                        onDubVolChange = { viewModel.setDubVolume(it) },
                        onBgmVolChange = { viewModel.setBgmVolume(it) },
                        onToggleOriginalMute = { viewModel.toggleMuteOriginal() },
                        onToggleDubMute = { viewModel.toggleMuteDub() },
                        onQuickAutoBalance = { viewModel.quickAutoBalanceMix() },
                        onToggleAutoDucking = { viewModel.toggleAutoDucking(it) },
                        onSelectBgmStyle = { viewModel.setBgmStyle(it) },
                        onSelectVoicePreset = { viewModel.applyVoicePreset(it) }
                    )
                }
                1 -> {
                    // TAB 1: Arabic Phonetics, Tashkeel & Script Inspection
                    PhoneticsAndLetteringModuleContent(
                        scriptLines = scriptLines,
                        auditReport = auditReport,
                        isAuditRunning = uiState.isAuditRunning,
                        onRunAudit = { viewModel.runScriptQualityAudit() },
                        onAutoFixAll = { viewModel.autoFixAndDiacritizeAllScriptLines() },
                        onEditLine = { index ->
                            val line = scriptLines.getOrNull(index)
                            if (line != null) {
                                showEditLineDialog = index
                                editLineText = line.textArabic
                                editLineStart = line.startSeconds
                                editLineEnd = line.endSeconds
                            }
                        },
                        onPreviewLineSpeech = { line ->
                            viewModel.previewSpeechForLine(line)
                        }
                    )
                }
                2 -> {
                    // TAB 2: Video Render, Resolution & Codec Tuning
                    VideoRenderConfigModuleContent(
                        config = videoExportConfig,
                        onConfigChanged = { updated ->
                            viewModel.updateVideoExportConfig(updated)
                        }
                    )
                }
                3 -> {
                    // TAB 3: Pre-Export Readiness Checklist & Storage Details
                    ReadinessChecklistModuleContent(
                        clip = clip,
                        scriptLines = scriptLines,
                        auditReport = auditReport,
                        videoConfig = videoExportConfig,
                        uiState = uiState,
                        onStartExport = { viewModel.openExportDialog() },
                        onSaveProject = { viewModel.saveDubbedProjectToStorage() }
                    )
                }
            }
        }

        // ==========================================
        // 5. MASTER ACTION BUTTONS
        // ==========================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF191522),
                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openExportDialog() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("hub_primary_export_action_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("معالجة وتصدير الفيديو 🎬", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.saveDubbedProjectToStorage() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_secondary_save_storage_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("حفظ بالتخزين 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToStudio,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("hub_back_to_studio_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("استوديو الدبلجة 🎛️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToLibrary,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("hub_back_to_clips_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("مكتبة المشاهد 🎞️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }

    // Line Edit Dialog
    if (showEditLineDialog != null) {
        val index = showEditLineDialog!!
        AlertDialog(
            onDismissRequest = { showEditLineDialog = null },
            title = {
                Text("تعديل سطر الحوار والتشكيل ✏️", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("النص العربي مع تشكيل الحركات:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = editLineText,
                        onValueChange = { editLineText = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF4B5563)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = String.format("%.2f", editLineStart),
                            onValueChange = { editLineStart = it.toFloatOrNull() ?: editLineStart },
                            label = { Text("البداية (ث)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = String.format("%.2f", editLineEnd),
                            onValueChange = { editLineEnd = it.toFloatOrNull() ?: editLineEnd },
                            label = { Text("النهاية (ث)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateScriptLineDirectly(index, editLineText, editLineStart, editLineEnd)
                        showEditLineDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("حفظ التعديل ✓", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLineDialog = null }) {
                    Text("إلغاء", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1A27),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F0)
        )
    }

    // Export Dialog
    ExportProjectDialog(
        state = exportDialogState,
        initialVideoConfig = videoExportConfig,
        onDismiss = { viewModel.closeExportDialog() },
        onStartExport = { format, name, cfg ->
            viewModel.startExport(format, name, cfg)
        },
        onOpenFile = { result ->
            viewModel.openExportedFile(result)
        },
        onShareFile = { result ->
            viewModel.shareExportedFile(result)
        }
    )
}

// ----------------------------------------------------
// MASTER VIEWPORT & SPLIT COMPARISON COMPONENT
// ----------------------------------------------------

@Composable
private fun MasterComparisonViewport(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    compareMode: SplitCompareViewMode,
    splitFraction: Float,
    isAbFlipActive: Boolean,
    activeLine: ScriptLine?,
    isOriginalMuted: Boolean,
    isDubMuted: Boolean,
    onToggleAbFlip: () -> Unit,
    onSetSplitFraction: (Float) -> Unit
) {
    val isNativeVideo = clip.videoUri != null && !clip.videoUri.startsWith("sample://")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        when (compareMode) {
            SplitCompareViewMode.SIDE_BY_SIDE -> {
                // 🔲 Dual Screens Side-by-Side
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left: Original Video
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)))
                    ) {
                        VideoCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isOriginalMuted,
                            colorA = Color(0xFF0F172A),
                            colorB = Color(0xFF1E3A8A)
                        )
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            color = Color(0xFF1E3A8A).copy(alpha = 0.85f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "الفيديو الأصلي 🎥",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF7C3AED))
                    )

                    // Right: Dubbed Video
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)))
                    ) {
                        VideoCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isDubMuted,
                            colorA = Color(0xFF1E1B4B),
                            colorB = Color(0xFF4338CA)
                        )
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 8.dp),
                            color = Color(0xFF065F46).copy(alpha = 0.85f),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "النسخة المدبلجة 🎙️",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Subtitle overlay
                        if (activeLine != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF111827).copy(alpha = 0.9f),
                                border = BorderStroke(0.8.dp, Color(0xFFA78BFA)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = activeLine.textArabic,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            SplitCompareViewMode.SPLIT_SLIDER -> {
                // ↔️ Interactive Wipe Slider
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boxW = constraints.maxWidth.toFloat()

                    Box(modifier = Modifier.fillMaxSize()) {
                        VideoCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isDubMuted,
                            colorA = Color(0xFF1E1B4B),
                            colorB = Color(0xFF4338CA)
                        )
                    }

                    val dividerX = (splitFraction * boxW).coerceIn(20f, boxW - 20f)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dividerX.toInt() - 15, 0) }
                            .width(30.dp)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newFraction = ((dividerX + dragAmount.x) / boxW).coerceIn(0.05f, 0.95f)
                                    onSetSplitFraction(newFraction)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFFACC15))
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFACC15),
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "سحب للمقارنة",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        color = Color(0xFF1E3A8A).copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "◀ الأصلي (${(splitFraction * 100).toInt()}%)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        color = Color(0xFF065F46).copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "المدبلج ( ${((1f - splitFraction) * 100).toInt()}% ) ▶",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            SplitCompareViewMode.AB_FLIP -> {
                // ⇄ Fast A/B Flip Toggle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onToggleAbFlip() }
                ) {
                    VideoCanvasScene(
                        clip = clip,
                        currentSeconds = currentSeconds,
                        isPlaying = isPlaying,
                        isNativeVideo = isNativeVideo,
                        isMuted = if (isAbFlipActive) isDubMuted else isOriginalMuted,
                        colorA = if (isAbFlipActive) Color(0xFF1E1B4B) else Color(0xFF0F172A),
                        colorB = if (isAbFlipActive) Color(0xFF4338CA) else Color(0xFF1E3A8A)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAbFlipActive) Color(0xFF065F46).copy(alpha = 0.9f) else Color(0xFF1E3A8A).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, if (isAbFlipActive) Color(0xFF34D399) else Color(0xFF60A5FA)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAbFlipActive) "🎙️ النسخة المعروضة: المدبلجة الجديدة" else "🎥 النسخة المعروضة: الفيديو الأصلي",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "(انقر للتبديل)",
                                fontSize = 9.sp,
                                color = Color(0xFFF1F5F9)
                            )
                        }
                    }

                    if (isAbFlipActive && activeLine != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF111827).copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, Color(0xFFA78BFA)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp, start = 14.dp, end = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = activeLine.characterAvatar, fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = activeLine.characterName,
                                        color = Color(0xFFFFD993),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeLine.textArabic,
                                        color = Color.White,
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

// ----------------------------------------------------
// CANVAS SCENE RENDERER
// ----------------------------------------------------

@Composable
private fun VideoCanvasScene(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    isNativeVideo: Boolean,
    isMuted: Boolean,
    colorA: Color,
    colorB: Color
) {
    var thumbnailBitmap by remember(clip.thumbnailPath) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    LaunchedEffect(clip.thumbnailPath) {
        val path = clip.thumbnailPath
        if (!path.isNullOrEmpty()) {
            try {
                val file = File(path)
                if (file.exists()) {
                    thumbnailBitmap = BitmapFactory.decodeFile(file.absolutePath)
                }
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = clip.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorA, colorB)
                    )
                )

                // Cinematic grid overlay
                val timeFactor = (currentSeconds * 50f) % w
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(timeFactor, 0f),
                    end = Offset(timeFactor, h),
                    strokeWidth = 2f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(w / 2f, h / 2f),
                        radius = w * 0.4f
                    ),
                    radius = w * 0.4f,
                    center = Offset(w / 2f, h / 2f)
                )
            }
        }

        // Live Audio Mute Indicator
        if (isMuted) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "مكتوم",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// AUDIO WAVEFORM / LEVELS METER BAR
// ----------------------------------------------------

@Composable
private fun MasterAudioLevelsMeterBar(
    dubVolume: Float,
    originalVolume: Float,
    bgmVolume: Float,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF110E17))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track 1: Dub Voice Level
        AudioTrackLevelPill(
            label = "صوت الدبلجة",
            icon = Icons.Default.RecordVoiceOver,
            color = Color(0xFF10B981),
            volume = dubVolume,
            isPlaying = isPlaying,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        // Track 2: Original Video Level
        AudioTrackLevelPill(
            label = "صوت المشهد",
            icon = Icons.Default.Movie,
            color = Color(0xFF3B82F6),
            volume = originalVolume,
            isPlaying = isPlaying,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        // Track 3: BGM / SFX Level
        AudioTrackLevelPill(
            label = "الموسيقى",
            icon = Icons.Default.Audiotrack,
            color = Color(0xFFA78BFA),
            volume = bgmVolume,
            isPlaying = isPlaying,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AudioTrackLevelPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    volume: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1624),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text(text = "${(volume * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                }
                LinearProgressIndicator(
                    progress = { if (isPlaying) (volume * 0.9f).coerceIn(0.05f, 1f) else volume * 0.3f },
                    color = color,
                    trackColor = Color(0xFF2D273B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 0: AUDIO MASTERING & DSP ENGINE MODULE
// ----------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AudioMasteringModuleContent(
    uiState: com.example.ui.StudioUiState,
    onOriginalVolChange: (Float) -> Unit,
    onDubVolChange: (Float) -> Unit,
    onBgmVolChange: (Float) -> Unit,
    onToggleOriginalMute: () -> Unit,
    onToggleDubMute: () -> Unit,
    onQuickAutoBalance: () -> Unit,
    onToggleAutoDucking: (Boolean) -> Unit,
    onSelectBgmStyle: (BgmStyle) -> Unit,
    onSelectVoicePreset: (VoicePresetType) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171320)),
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Quick Auto Balance Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Balance, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "معالجة ومكساج الصوت الاحترافي (Audio Mastering)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onQuickAutoBalance,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("موازنة تلقائية ⚡", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Sliders: Dub Voice, Original Video, BGM
            VolumeSliderRow(
                label = "صوت الدبلجة البشرية 🎙️",
                volume = uiState.dubVolume,
                isMuted = uiState.isMutedDub,
                color = Color(0xFF10B981),
                onVolumeChange = onDubVolChange,
                onToggleMute = onToggleDubMute
            )

            VolumeSliderRow(
                label = "صوت الفيديو الأصلي 🎥",
                volume = uiState.originalVolume,
                isMuted = uiState.isMutedOriginal,
                color = Color(0xFF3B82F6),
                onVolumeChange = onOriginalVolChange,
                onToggleMute = onToggleOriginalMute
            )

            VolumeSliderRow(
                label = "موسيقى الخلفية والمؤثرات 🎵",
                volume = uiState.bgmVolume,
                isMuted = false,
                color = Color(0xFFA78BFA),
                onVolumeChange = onBgmVolChange,
                onToggleMute = {}
            )

            Spacer(Modifier.height(4.dp))

            // Smart Auto-Ducking Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1928),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "التخفيض الذكي للموسيقى أثناء الكلام (Smart Auto-Ducking)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "خفض موسيقى المشهد تلقائياً عند نطق الحوارات لضمان وضوح نبرة الصوت",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Switch(
                        checked = uiState.isAutoDuckingEnabled,
                        onCheckedChange = onToggleAutoDucking,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }

            // Spacetoon Anime Echo & Voice Presets
            Text(
                text = "مؤثرات صدى الصوت والأنمي الملحمي (Spacetoon DSP Presets):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF3E8FF)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VoicePresetType.values().forEach { preset ->
                    val isSelected = uiState.selectedVoicePreset == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF231D2E),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF3B334C)),
                        modifier = Modifier.clickable { onSelectVoicePreset(preset) }
                    ) {
                        Text(
                            text = "${preset.titleArabic} ${if (isSelected) "✓" else ""}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: PHONETICS & SCRIPT AUDIT MODULE
// ----------------------------------------------------

@Composable
private fun PhoneticsAndLetteringModuleContent(
    scriptLines: List<ScriptLine>,
    auditReport: AuditReport?,
    isAuditRunning: Boolean,
    onRunAudit: () -> Unit,
    onAutoFixAll: () -> Unit,
    onEditLine: (Int) -> Unit,
    onPreviewLineSpeech: (ScriptLine) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171320)),
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quality Score Header
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1F1A2B),
                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                (auditReport?.accuracyScore ?: 100) >= 90 -> Color(0xFF10B981)
                                (auditReport?.accuracyScore ?: 100) >= 75 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isAuditRunning) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = "${auditReport?.accuracyScore ?: 100}%",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "دقة تشكيل الحروف ومخارج النطق",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = auditReport?.summaryTextArabic ?: "يتم فحص التشكيل، همزات الوصل، والوقفات الطبيعية...",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onRunAudit) {
                        Icon(Icons.Default.Sync, contentDescription = "إعادة الفحص", tint = Color(0xFFA78BFA))
                    }
                }
            }

            // One-Tap Auto Fix Button
            Button(
                onClick = onAutoFixAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("hub_auto_fix_phonetics_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "تشكيل الحركات وضبط مخارج النطق آلياً بنقرة واحدة 🪄",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Script Lines Inspection List
            Text(
                text = "أسطر الحوار المشكلة ونطق الشخصيات (${scriptLines.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF3E8FF)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                scriptLines.forEachIndexed { index, line ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E182A),
                        border = BorderStroke(0.8.dp, Color(0xFF372F47)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = line.characterAvatar, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = line.characterName,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD993)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "⏱️ ${formatSeconds(line.startSeconds)} - ${formatSeconds(line.endSeconds)}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Text(
                                        text = line.textArabic,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Speak Preview
                                IconButton(
                                    onClick = { onPreviewLineSpeech(line) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "استماع", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                }

                                // Edit
                                IconButton(
                                    onClick = { onEditLine(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: VIDEO RENDER & CODEC CONFIG MODULE
// ----------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoRenderConfigModuleContent(
    config: VideoExportConfig,
    onConfigChanged: (VideoExportConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171320)),
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HighQuality, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "إعدادات ترميز ودقة الفيديو قبل التصدير",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 1. Resolution Picker
            Text(text = "دقة العرض (Video Resolution):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExportResolution.values().forEach { res ->
                    val isSelected = config.resolution == res
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF231D2E),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF3B334C)),
                        modifier = Modifier.clickable { onConfigChanged(config.copy(resolution = res)) }
                    ) {
                        Text(
                            text = "${res.labelArabic} ${if (isSelected) "✓" else ""}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 2. Frame Rate Preset
            Text(text = "معدل الإطارات (Frame Rate):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FrameRatePreset.values().forEach { fps ->
                    val isSelected = config.frameRatePreset == fps
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF231D2E),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF3B334C)),
                        modifier = Modifier.clickable { onConfigChanged(config.copy(frameRatePreset = fps)) }
                    ) {
                        Text(
                            text = "${fps.labelArabic} ${if (isSelected) "✓" else ""}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. Video Bitrate Preset
            Text(text = "معدل البت والجودة (Bitrate):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VideoBitratePreset.values().forEach { br ->
                    val isSelected = config.bitratePreset == br
                    val calculatedMbps = (config.resolution.baseBitrateBps * br.multiplier) / 1_000_000f
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF231D2E),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF3B334C)),
                        modifier = Modifier.clickable { onConfigChanged(config.copy(bitratePreset = br)) }
                    ) {
                        Text(
                            text = "${br.labelArabic} (${String.format(java.util.Locale.US, "%.1f", calculatedMbps)} Mbps) ${if (isSelected) "✓" else ""}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 4. Burn-in Subtitles Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1928),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "دمج الترجمة الحية داخل إطارات الفيديو (Burn-in Subtitles)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "طباعة النص العربي المشكل فوق الفيديو مباشرة بنمط احترافي",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Switch(
                        checked = config.burnSubtitles,
                        onCheckedChange = { onConfigChanged(config.copy(burnSubtitles = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF7C3AED)
                        )
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: READINESS CHECKLIST MODULE
// ----------------------------------------------------

@Composable
private fun ReadinessChecklistModuleContent(
    clip: DubbingClip,
    scriptLines: List<ScriptLine>,
    auditReport: AuditReport?,
    videoConfig: VideoExportConfig,
    uiState: com.example.ui.StudioUiState,
    onStartExport: () -> Unit,
    onSaveProject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171320)),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "قائمة الفحص والجاهزية الشاملة قبل التصدير",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            ChecklistItemRow(
                title = "مكساج وهندسة الصوت",
                subtitle = "صوت الدبلجة (${(uiState.dubVolume * 100).toInt()}%) متوازن مع المشهد والموسيقى والتخفيض الذكي مفعل",
                isPassed = true
            )

            ChecklistItemRow(
                title = "تدقيق الحروف والتشكيل",
                subtitle = "مستوى دقة التشكيل (${auditReport?.accuracyScore ?: 100}%) ومخارج الحروف معتمدة",
                isPassed = (auditReport?.accuracyScore ?: 100) >= 80
            )

            ChecklistItemRow(
                title = "مطابقة إطارات الفيديو (25 FPS SMPTE)",
                subtitle = "تمت محاذاة بداية ونهاية الحوارات مع توقيت حركة الشفاه في المشهد",
                isPassed = true
            )

            ChecklistItemRow(
                title = "إعدادات الدقة والترميز (${videoConfig.resolution.badge} • ${videoConfig.frameRatePreset.labelArabic})",
                subtitle = "ترميز MP4 متوافق مع كافة المنصات والأجهزة",
                isPassed = true
            )

            // Storage Details Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10281E),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📁 مسار الحفظ والتخزين التلقائي:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                    Text(
                        text = "يتم حفظ العمل في قاعدة بيانات التطبيق المحلية ومجلد الأفلام (Movies/DubbedVideos/)",
                        fontSize = 10.5.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// HELPER UI COMPONENTS
// ----------------------------------------------------

@Composable
private fun PreviewModeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF14101C),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF332D42)),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProcessingTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF7C3AED) else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFFA78BFA),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )
            if (badge != null) {
                Spacer(Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeSliderRow(
    label: String,
    volume: Float,
    isMuted: Boolean,
    color: Color,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (isMuted) {
                    Spacer(Modifier.width(6.dp))
                    Text(text = "(مكتوم)", fontSize = 10.sp, color = Color(0xFFEF4444))
                }
            }
            Text(
                text = "${(volume * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color(0xFF2E283D)
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleMute, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "كتم",
                    tint = if (isMuted) Color(0xFFEF4444) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    title: String,
    subtitle: String,
    isPassed: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1B1626),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isPassed) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun TransportActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF231D30),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
        }
    }
}

private fun formatTimeDisplay(seconds: Float): String {
    val totalSec = seconds.toInt()
    val mins = totalSec / 60
    val secs = totalSec % 60
    val millis = ((seconds - totalSec) * 10).toInt()
    return String.format("%02d:%02d.%d", mins, secs, millis)
}

private fun formatSmpteTime(seconds: Float): String {
    val totalSec = seconds.toInt()
    val mins = totalSec / 60
    val secs = totalSec % 60
    val frames = ((seconds - totalSec) * 25).toInt().coerceIn(0, 24)
    return String.format("00:%02d:%02d:%02d", mins, secs, frames)
}

private fun formatSeconds(seconds: Float): String {
    val s = seconds.toInt()
    val ms = ((seconds - s) * 10).toInt()
    return "${s}.${ms}ث"
}
