package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.ArabicPhoneticsEngine
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import com.example.ui.DubbingViewModel
import com.example.ui.components.VideoCanvasPlayer
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 🎚️ VideoAudioSyncLayoutScreen
 * Displays the video player alongside a multi-track waveform visualization
 * of the original audio and recorded dub track for sub-frame precise synchronization.
 */
@Composable
fun VideoAudioSyncLayoutScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTrackMixer by remember { mutableStateOf(false) }

    val clip = uiState.currentClip
    val durationSeconds = clip.durationSeconds.toFloat()
    val currentSeconds = uiState.currentPlaybackSeconds
    val syncOffsetMs = uiState.dubSyncOffsetMs
    val zoomLevel = uiState.syncZoomLevel
    val isPlaying = uiState.isPlaying
    val recordedAudioPath = uiState.recordedAudioPath

    val activeLine = uiState.scriptLines.find {
        currentSeconds >= it.startSeconds && currentSeconds <= it.endSeconds
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("video_audio_sync_screen")
    ) {
        val isWideScreen = maxWidth > 720.dp

        if (isWideScreen) {
            // 💻 Landscape / Tablet Side-by-Side Dual Pane Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Pane: Video Player & Lip-Sync Visualizer
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SyncTopHeaderBar(
                        clip = clip,
                        onHelpClick = { showHelpDialog = true },
                        onNavigateStudio = onNavigateToStudio
                    )

                    VideoPlayerSyncCard(
                        clip = clip,
                        currentSeconds = currentSeconds,
                        isPlaying = isPlaying,
                        activeLine = activeLine,
                        isMutedOriginal = uiState.isMutedOriginal,
                        syncOffsetMs = syncOffsetMs,
                        playbackSpeed = uiState.syncPlaybackSpeed,
                        onTogglePlay = { viewModel.toggleSyncPlayback() },
                        onToggleMute = { viewModel.toggleMuteOriginal() },
                        onStepForward = { viewModel.stepFrameForward(50f) },
                        onStepBackward = { viewModel.stepFrameBackward(50f) },
                        onSeek = { viewModel.seekPlaybackPosition(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    SyncQuickControlBar(
                        viewModel = viewModel,
                        syncOffsetMs = syncOffsetMs,
                        onToggleMixer = { showTrackMixer = !showTrackMixer }
                    )
                }

                // Right Pane: Waveform Timeline & Precision Synchronization Controls
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WaveformSynchronizerCard(
                        viewModel = viewModel,
                        clip = clip,
                        currentSeconds = currentSeconds,
                        durationSeconds = durationSeconds,
                        syncOffsetMs = syncOffsetMs,
                        zoomLevel = zoomLevel,
                        waveformGain = uiState.syncWaveformGain,
                        isSyncLocked = uiState.isSyncLocked,
                        isLooping = uiState.isSyncLooping,
                        loopStartSec = uiState.syncLoopStartSec,
                        loopEndSec = uiState.syncLoopEndSec,
                        hasRecordedDub = recordedAudioPath != null && File(recordedAudioPath).exists(),
                        scriptLines = uiState.scriptLines,
                        markerSeconds = uiState.syncMarkerSeconds,
                        modifier = Modifier.weight(1f)
                    )

                    SyncNudgeSlipPanel(
                        syncOffsetMs = syncOffsetMs,
                        onNudge = { delta -> viewModel.nudgeDubSyncOffsetMs(delta) },
                        onSetOffset = { offset -> viewModel.setDubSyncOffsetMs(offset) },
                        onReset = { viewModel.resetDubSyncOffset() },
                        onAutoAlign = { viewModel.autoAlignSyncOffset() }
                    )

                    // Live Lip-Sync Evaluation Meter
                    LiveLipSyncPacingMeterCard(
                        activeLine = activeLine,
                        onApplySpeed = { speed -> viewModel.applyRecommendedLipSyncSpeed(speed) },
                        onAutoFitDuration = {
                            val idx = uiState.scriptLines.indexOf(activeLine)
                            if (idx >= 0) viewModel.autoFitScriptLineDuration(idx)
                        }
                    )

                    // Tactile Sub-Frame Micro Jog Scrubber
                    InteractiveMicroJogScrubber(
                        currentSeconds = currentSeconds,
                        durationSeconds = durationSeconds,
                        onJog = { deltaMs ->
                            if (deltaMs > 0) viewModel.stepFrameForward(deltaMs)
                            else viewModel.stepFrameBackward(-deltaMs)
                        }
                    )

                    // Per-Line Precision Timestamp Inspector
                    ScriptLinesSyncPrecisionManager(
                        scriptLines = uiState.scriptLines,
                        currentSeconds = currentSeconds,
                        onSeek = { viewModel.seekPlaybackPosition(it) },
                        onNudgeLine = { idx, dStart, dEnd -> viewModel.nudgeScriptLineTimestamp(idx, dStart, dEnd) },
                        onAutoFit = { idx -> viewModel.autoFitScriptLineDuration(idx) }
                    )
                }
            }
        } else {
            // 📱 Portrait Layout (Stacked: Video Canvas Top + Waveform Synchronizer Bottom)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SyncTopHeaderBar(
                        clip = clip,
                        onHelpClick = { showHelpDialog = true },
                        onNavigateStudio = onNavigateToStudio
                    )
                }

                // 1. Video Player with Precision Timecode & Lip-Sync Monitor
                item {
                    VideoPlayerSyncCard(
                        clip = clip,
                        currentSeconds = currentSeconds,
                        isPlaying = isPlaying,
                        activeLine = activeLine,
                        isMutedOriginal = uiState.isMutedOriginal,
                        syncOffsetMs = syncOffsetMs,
                        playbackSpeed = uiState.syncPlaybackSpeed,
                        onTogglePlay = { viewModel.toggleSyncPlayback() },
                        onToggleMute = { viewModel.toggleMuteOriginal() },
                        onStepForward = { viewModel.stepFrameForward(50f) },
                        onStepBackward = { viewModel.stepFrameBackward(50f) },
                        onSeek = { viewModel.seekPlaybackPosition(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Transport & Synchronization Quick Actions
                item {
                    SyncQuickControlBar(
                        viewModel = viewModel,
                        syncOffsetMs = syncOffsetMs,
                        onToggleMixer = { showTrackMixer = !showTrackMixer }
                    )
                }

                // 3. Live Lip-Sync Precision Evaluation Meter
                item {
                    LiveLipSyncPacingMeterCard(
                        activeLine = activeLine,
                        onApplySpeed = { speed -> viewModel.applyRecommendedLipSyncSpeed(speed) },
                        onAutoFitDuration = {
                            val idx = uiState.scriptLines.indexOf(activeLine)
                            if (idx >= 0) viewModel.autoFitScriptLineDuration(idx)
                        }
                    )
                }

                // 4. Dual Track Waveform Canvas & Time Slip Scrubbing
                item {
                    WaveformSynchronizerCard(
                        viewModel = viewModel,
                        clip = clip,
                        currentSeconds = currentSeconds,
                        durationSeconds = durationSeconds,
                        syncOffsetMs = syncOffsetMs,
                        zoomLevel = zoomLevel,
                        waveformGain = uiState.syncWaveformGain,
                        isSyncLocked = uiState.isSyncLocked,
                        isLooping = uiState.isSyncLooping,
                        loopStartSec = uiState.syncLoopStartSec,
                        loopEndSec = uiState.syncLoopEndSec,
                        hasRecordedDub = recordedAudioPath != null && File(recordedAudioPath).exists(),
                        scriptLines = uiState.scriptLines,
                        markerSeconds = uiState.syncMarkerSeconds,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 5. Millisecond Nudge Slip Control Panel
                item {
                    SyncNudgeSlipPanel(
                        syncOffsetMs = syncOffsetMs,
                        onNudge = { delta -> viewModel.nudgeDubSyncOffsetMs(delta) },
                        onSetOffset = { offset -> viewModel.setDubSyncOffsetMs(offset) },
                        onReset = { viewModel.resetDubSyncOffset() },
                        onAutoAlign = { viewModel.autoAlignSyncOffset() }
                    )
                }

                // 6. Tactile Sub-Frame Micro Jog Scrubber
                item {
                    InteractiveMicroJogScrubber(
                        currentSeconds = currentSeconds,
                        durationSeconds = durationSeconds,
                        onJog = { deltaMs ->
                            if (deltaMs > 0) viewModel.stepFrameForward(deltaMs)
                            else viewModel.stepFrameBackward(-deltaMs)
                        }
                    )
                }

                // 7. Per-Line Precision Timestamp Inspector
                item {
                    ScriptLinesSyncPrecisionManager(
                        scriptLines = uiState.scriptLines,
                        currentSeconds = currentSeconds,
                        onSeek = { viewModel.seekPlaybackPosition(it) },
                        onNudgeLine = { idx, dStart, dEnd -> viewModel.nudgeScriptLineTimestamp(idx, dStart, dEnd) },
                        onAutoFit = { idx -> viewModel.autoFitScriptLineDuration(idx) }
                    )
                }

                // 8. Advanced Sync Tools (Looping, Zoom, Gain & Markers)
                item {
                    SyncAdvancedToolsCard(
                        viewModel = viewModel,
                        zoomLevel = zoomLevel,
                        waveformGain = uiState.syncWaveformGain,
                        isSyncLocked = uiState.isSyncLocked,
                        isLooping = uiState.isSyncLooping,
                        loopStartSec = uiState.syncLoopStartSec,
                        loopEndSec = uiState.syncLoopEndSec,
                        durationSeconds = durationSeconds,
                        currentSeconds = currentSeconds
                    )
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        // Help Information Modal
        if (showHelpDialog) {
            SyncHelpDialog(onDismiss = { showHelpDialog = false })
        }
    }
}

/**
 * Top Header with App Status and Mode Navigation
 */
@Composable
private fun SyncTopHeaderBar(
    clip: DubbingClip,
    onHelpClick: () -> Unit,
    onNavigateStudio: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF6750A4).copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "مزامنة الفيديو والصوت الدقيقة 🎚️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${clip.coverEmoji} ${clip.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onHelpClick,
                modifier = Modifier.testTag("sync_help_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "مساعدة",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedButton(
                onClick = onNavigateStudio,
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("back_to_studio_btn")
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("الاستوديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Video Player Card with High-Precision Timecode and Frame Nudge Controls
 */
@Composable
private fun VideoPlayerSyncCard(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    activeLine: ScriptLine?,
    isMutedOriginal: Boolean,
    syncOffsetMs: Float,
    playbackSpeed: Float,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .testTag("video_player_sync_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Video Display Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
            ) {
                VideoCanvasPlayer(
                    clip = clip,
                    currentSeconds = currentSeconds,
                    isPlaying = isPlaying,
                    isRecording = false,
                    activeLine = activeLine,
                    isMutedOriginal = isMutedOriginal,
                    onTogglePlay = onTogglePlay,
                    onToggleMute = onToggleMute,
                    onSeek = onSeek,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sync Status HUD Overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (abs(syncOffsetMs) < 15f) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                            modifier = Modifier.size(8.dp)
                        ) {}

                        Text(
                            text = if (abs(syncOffsetMs) < 15f) "تزامن تام 🟢" else "إزاحة: ${String.format(Locale.US, "%+.0f", syncOffsetMs)}ms",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Millisecond Timecode HUD
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    val minutes = (currentSeconds / 60).toInt()
                    val seconds = (currentSeconds % 60).toInt()
                    val millis = ((currentSeconds - currentSeconds.toInt()) * 1000).toInt()
                    Text(
                        text = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Transport Control Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Micro Frame Steppers (-50ms, -100ms)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onStepBackward,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("frame_step_back_btn")
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("-50ms", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onStepForward,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("frame_step_fwd_btn")
                    ) {
                        Text("+50ms", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                }

                // Main Play/Pause Button
                Button(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("sync_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "إيقاف مؤقت" else "تشغيل متزامن",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Speed Chip & Mute Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${playbackSpeed}x", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("sync_mute_original_btn")
                    ) {
                        Icon(
                            imageVector = if (isMutedOriginal) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute",
                            tint = if (isMutedOriginal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Action Toolbar for Synchronization Modes
 */
@Composable
private fun SyncQuickControlBar(
    viewModel: DubbingViewModel,
    syncOffsetMs: Float,
    onToggleMixer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed selector chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("السرعة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                speeds.forEach { spd ->
                    val isSel = uiState.syncPlaybackSpeed == spd
                    Surface(
                        onClick = { viewModel.setSyncPlaybackSpeed(spd) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                        border = if (isSel) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "${spd}x",
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Sync Lock Switch & Marker Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { viewModel.toggleSyncLocked() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (uiState.isSyncLocked) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (uiState.isSyncLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isSyncLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (uiState.isSyncLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isSyncLocked) "مقفل 🔒" else "حر 🔓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isSyncLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.addSyncMarker(uiState.currentPlaybackSeconds) },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("+ علامة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0077B6))
                    }
                }
            }
        }
    }
}

/**
 * Dual Track Waveform Canvas with Precision Sync Offset Slip and Scrubbing
 */
@Composable
private fun WaveformSynchronizerCard(
    viewModel: DubbingViewModel,
    clip: DubbingClip,
    currentSeconds: Float,
    durationSeconds: Float,
    syncOffsetMs: Float,
    zoomLevel: Float,
    waveformGain: Float,
    isSyncLocked: Boolean,
    isLooping: Boolean,
    loopStartSec: Float,
    loopEndSec: Float,
    hasRecordedDub: Boolean,
    scriptLines: List<ScriptLine>,
    markerSeconds: List<Float>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .testTag("waveform_sync_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Labels for Track 1 (Original) and Track 2 (Recorded Dub)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "🎬 مسار صوت الفيديو الأصلي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFA855F7).copy(alpha = 0.2f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "🎙️ مسار الدبلجة المسجل (المتحرك)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9333EA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Zoom Level indicator
                Text(
                    text = "التكبير: ${String.format(Locale.US, "%.1f", zoomLevel)}x",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            // Main Interactive Multi-Track Waveform Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090D16))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .testTag("waveform_sync_canvas")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(durationSeconds, zoomLevel) {
                            detectTapGestures { tapOffset ->
                                val fraction = (tapOffset.x / size.width).coerceIn(0f, 1f)
                                val targetTime = fraction * durationSeconds
                                viewModel.seekPlaybackPosition(targetTime)
                            }
                        }
                        .pointerInput(durationSeconds, zoomLevel) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSec = (dragAmount.x / size.width) * durationSeconds
                                val newPos = (currentSeconds + deltaSec).coerceIn(0f, durationSeconds)
                                viewModel.seekPlaybackPosition(newPos)
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val trackHeight = canvasHeight / 2f
                    val rulerHeight = 22.dp.toPx()

                    // 1. Draw Timecode Grid and Marker lines
                    drawTimelineGrid(
                        durationSeconds = durationSeconds,
                        zoomLevel = zoomLevel,
                        rulerHeight = rulerHeight
                    )

                    // 2. Draw A-B Loop Region if active
                    if (isLooping) {
                        val startX = (loopStartSec / durationSeconds) * canvasWidth
                        val endX = (loopEndSec / durationSeconds) * canvasWidth
                        drawRect(
                            color = Color(0xFF6750A4).copy(alpha = 0.22f),
                            topLeft = Offset(startX, 0f),
                            size = Size(endX - startX, canvasHeight)
                        )
                        drawLine(
                            color = Color(0xFFD0BCFF),
                            start = Offset(startX, 0f),
                            end = Offset(startX, canvasHeight),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFFD0BCFF),
                            start = Offset(endX, 0f),
                            end = Offset(endX, canvasHeight),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // 3. Draw Track 1: Original Video Audio Envelope
                    drawOriginalVideoAudioWaveform(
                        clip = clip,
                        durationSeconds = durationSeconds,
                        trackTopY = rulerHeight,
                        trackHeight = (trackHeight - rulerHeight),
                        scriptLines = scriptLines,
                        gain = waveformGain
                    )

                    // Track Divider Line
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(0f, trackHeight),
                        end = Offset(canvasWidth, trackHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 4. Draw Track 2: Recorded Dub Audio Waveform (with sync offset applied!)
                    drawRecordedDubWaveform(
                        durationSeconds = durationSeconds,
                        trackTopY = trackHeight,
                        trackHeight = trackHeight,
                        syncOffsetMs = syncOffsetMs,
                        hasRecordedDub = hasRecordedDub,
                        gain = waveformGain
                    )

                    // 5. Draw User Sync Markers
                    markerSeconds.forEach { markerSec ->
                        val markerX = (markerSec / durationSeconds) * canvasWidth
                        drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(markerX, 0f),
                            end = Offset(markerX, canvasHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 4.dp.toPx(),
                            center = Offset(markerX, 6.dp.toPx())
                        )
                    }

                    // 6. Draw Red Playhead Scrubber Line
                    val playheadX = (currentSeconds / durationSeconds) * canvasWidth
                    drawLine(
                        color = Color(0xFFFF3B30),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, canvasHeight),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Playhead Top Pointer Cap
                    val pointerPath = Path().apply {
                        moveTo(playheadX - 6.dp.toPx(), 0f)
                        lineTo(playheadX + 6.dp.toPx(), 0f)
                        lineTo(playheadX, 9.dp.toPx())
                        close()
                    }
                    drawPath(pointerPath, color = Color(0xFFFF3B30))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Timeline Position Scrubber Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fs", currentSeconds),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = currentSeconds,
                    onValueChange = { viewModel.seekPlaybackPosition(it) },
                    valueRange = 0f..durationSeconds,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF3B30),
                        activeTrackColor = Color(0xFFFF3B30).copy(alpha = 0.7f),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sync_timeline_slider")
                )

                Text(
                    text = String.format(Locale.US, "%.1fs", durationSeconds),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Millisecond Precision Nudge & Time Slip Control Panel
 */
@Composable
private fun SyncNudgeSlipPanel(
    syncOffsetMs: Float,
    onNudge: (Float) -> Unit,
    onSetOffset: (Float) -> Unit,
    onReset: () -> Unit,
    onAutoAlign: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sync_nudge_slip_panel")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Panel Header & Auto Align Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "إزاحة المزامنة الدقيقة (Time Slip Offset)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onAutoAlign,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("auto_align_sync_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("محاذاة ذكية ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Large Millisecond Offset Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%+.0f", syncOffsetMs),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (abs(syncOffsetMs) < 15f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ميلي ثانية (ms)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Offset Slider (-2000ms to +2000ms)
            Slider(
                value = syncOffsetMs,
                onValueChange = onSetOffset,
                valueRange = -2000f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_offset_slider")
            )

            // Step Nudge Buttons Row (-100ms, -10ms, -1ms, +1ms, +10ms, +100ms)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Negative Nudge Group (Shift Earlier)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NudgeButton(label = "-100", onClick = { onNudge(-100f) }, tag = "nudge_minus_100")
                    NudgeButton(label = "-10", onClick = { onNudge(-10f) }, tag = "nudge_minus_10")
                    NudgeButton(label = "-1", onClick = { onNudge(-1f) }, tag = "nudge_minus_1")
                }

                // Reset Center Button
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("reset_sync_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("صفر 0ms", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                // Positive Nudge Group (Shift Later)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NudgeButton(label = "+1", onClick = { onNudge(1f) }, tag = "nudge_plus_1")
                    NudgeButton(label = "+10", onClick = { onNudge(10f) }, tag = "nudge_plus_10")
                    NudgeButton(label = "+100", onClick = { onNudge(100f) }, tag = "nudge_plus_100")
                }
            }
        }
    }
}

/**
 * Compact Nudge Button
 */
@Composable
private fun NudgeButton(label: String, onClick: () -> Unit, tag: String) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .height(32.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "$label ms",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Advanced Sync Tools Card: Zoom, Waveform Gain, A-B Region Looping, and Marker Clearing
 */
@Composable
private fun SyncAdvancedToolsCard(
    viewModel: DubbingViewModel,
    zoomLevel: Float,
    waveformGain: Float,
    isSyncLocked: Boolean,
    isLooping: Boolean,
    loopStartSec: Float,
    loopEndSec: Float,
    durationSeconds: Float,
    currentSeconds: Float
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🎛️ أدوات المزامنة المتقدمة والتكبير",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            // 1. Zoom and Gain Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("التكبير:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val zoomPresets = listOf(1.0f, 2.0f, 4.0f, 8.0f)
                    zoomPresets.forEach { z ->
                        val isSel = abs(zoomLevel - z) < 0.1f
                        Surface(
                            onClick = { viewModel.setSyncZoomLevel(z) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(26.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = "${z.toInt()}x",
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Waveform Gain
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("تضخيم الموجة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        onClick = {
                            val nextGain = if (waveformGain >= 3.0f) 1.0f else waveformGain + 0.5f
                            viewModel.setSyncWaveformGain(nextGain)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", waveformGain)}x 📈",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 2. A-B Loop Region Setup
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Loop, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "تكرار مقطع التدقيق (A-B Loop):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleSyncLooping() },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (isLooping) "مفعل 🔁" else "معطل",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLooping) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (isLooping) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val start = currentSeconds.coerceAtLeast(0f)
                                    viewModel.setSyncLoopRange(start, loopEndSec.coerceAtLeast(start + 1f))
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("تعيين البداية [A]: ${String.format(Locale.US, "%.1fs", loopStartSec)}", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val end = currentSeconds.coerceAtMost(durationSeconds)
                                    viewModel.setSyncLoopRange(loopStartSec.coerceAtMost(end - 1f), end)
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("تعيين النهاية [B]: ${String.format(Locale.US, "%.1fs", loopEndSec)}", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sync Help & Instructions Dialog
 */
@Composable
private fun SyncHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("دليل مزامنة الفيديو والصوت 🎚️")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "• شاشة المزامنة تتيح لك مطابقة مسار الصوت المسجل مع حركة شفاه الشخصيات في الفيديو بأعلى درجات الدقة.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "• المسار العلوي (الأزرق): يمثل حوارات ومؤثرات الفيديو الأصلي.",
                    fontSize = 12.sp,
                    color = Color(0xFF0284C7)
                )
                Text(
                    text = "• المسار السفلي (البنفسجي): يمثل تسجيلك الصوتي الذي يمكنك إزاحته يميناً أو يساراً عبر أزرار الميلي ثانية (ms).",
                    fontSize = 12.sp,
                    color = Color(0xFF9333EA)
                )
                Text(
                    text = "• اضغط على 'محاذاة ذكية ✨' للتعويض التلقائي عن زمن استجابة الميكروفون ونطق الكلمات.",
                    fontSize = 12.sp
                )
                Text(
                    text = "• يمكنك تفعيل تكرار مقطع (A-B Loop) وتشغيل الفيديو بنصف السرعة (0.5x) لضبط حركة الشفاه بدقة بالغة.",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("فهمت ذلك ✅")
            }
        }
    )
}

// ==========================================
// 🎨 Canvas Custom Drawing Extensions
// ==========================================

private fun DrawScope.drawTimelineGrid(
    durationSeconds: Float,
    zoomLevel: Float,
    rulerHeight: Float
) {
    val totalWidth = size.width
    val totalHeight = size.height

    // Ruler Background
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(0f, 0f),
        size = Size(totalWidth, rulerHeight)
    )

    val stepSec = when {
        zoomLevel >= 4f -> 0.5f
        zoomLevel >= 2f -> 1.0f
        else -> 2.0f
    }

    var t = 0f
    while (t <= durationSeconds) {
        val x = (t / durationSeconds) * totalWidth
        val isMajor = (t % (stepSec * 2)) < 0.01f

        val tickHeight = if (isMajor) rulerHeight * 0.75f else rulerHeight * 0.4f
        drawLine(
            color = if (isMajor) Color(0xFF94A3B8) else Color(0xFF475569),
            start = Offset(x, rulerHeight - tickHeight),
            end = Offset(x, rulerHeight),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
        )

        // Vertical grid line through tracks
        if (isMajor) {
            drawLine(
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                start = Offset(x, rulerHeight),
                end = Offset(x, totalHeight),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        t += stepSec
    }
}

private fun DrawScope.drawOriginalVideoAudioWaveform(
    clip: DubbingClip,
    durationSeconds: Float,
    trackTopY: Float,
    trackHeight: Float,
    scriptLines: List<ScriptLine>,
    gain: Float
) {
    val totalWidth = size.width
    val centerY = trackTopY + (trackHeight / 2f)
    val barCount = 100
    val barWidth = (totalWidth / barCount) * 0.65f
    val gap = (totalWidth / barCount) * 0.35f

    for (i in 0 until barCount) {
        val fraction = i.toFloat() / barCount
        val timeSec = fraction * durationSeconds

        val isDialogue = scriptLines.any { timeSec >= it.startSeconds && timeSec <= it.endSeconds }
        val baseAmp = if (isDialogue) {
            (0.35f + sin(i * 0.45f) * 0.25f + sin(i * 1.2f) * 0.15f).coerceIn(0.15f, 0.95f)
        } else {
            (0.08f + sin(i * 0.2f) * 0.04f).coerceIn(0.03f, 0.2f)
        }

        val barH = (trackHeight * 0.85f * baseAmp * gain).coerceIn(3.dp.toPx(), trackHeight * 0.92f)
        val x = i * (barWidth + gap)
        val y = centerY - (barH / 2f)

        val brush = Brush.verticalGradient(
            colors = if (isDialogue) {
                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
            } else {
                listOf(Color(0xFF64748B), Color(0xFF334155))
            },
            startY = y,
            endY = y + barH
        )

        drawRoundRect(
            brush = brush,
            topLeft = Offset(x, y),
            size = Size(barWidth, barH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawRecordedDubWaveform(
    durationSeconds: Float,
    trackTopY: Float,
    trackHeight: Float,
    syncOffsetMs: Float,
    hasRecordedDub: Boolean,
    gain: Float
) {
    val totalWidth = size.width
    val centerY = trackTopY + (trackHeight / 2f)
    val barCount = 100
    val barWidth = (totalWidth / barCount) * 0.65f
    val gap = (totalWidth / barCount) * 0.35f
    val offsetFraction = (syncOffsetMs / 1000f) / durationSeconds

    for (i in 0 until barCount) {
        val originalFraction = i.toFloat() / barCount
        val shiftedFraction = originalFraction - offsetFraction

        if (shiftedFraction in 0f..1f) {
            val baseAmp = if (hasRecordedDub) {
                (0.40f + sin(i * 0.55f) * 0.30f + sin(i * 1.8f) * 0.18f).coerceIn(0.12f, 0.95f)
            } else {
                (0.10f + sin(i * 0.3f) * 0.05f).coerceIn(0.04f, 0.25f)
            }

            val barH = (trackHeight * 0.85f * baseAmp * gain).coerceIn(3.dp.toPx(), trackHeight * 0.92f)
            val x = i * (barWidth + gap)
            val y = centerY - (barH / 2f)

            val brush = Brush.verticalGradient(
                colors = if (hasRecordedDub) {
                    listOf(Color(0xFFE879F9), Color(0xFF9333EA))
                } else {
                    listOf(Color(0xFF64748B).copy(alpha = 0.5f), Color(0xFF334155).copy(alpha = 0.5f))
                },
                startY = y,
                endY = y + barH
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * 🎯 Live Lip-Sync & Phonetic Pacing Meter Card
 */
@Composable
private fun LiveLipSyncPacingMeterCard(
    activeLine: ScriptLine?,
    onApplySpeed: (Float) -> Unit,
    onAutoFitDuration: () -> Unit
) {
    if (activeLine == null) return

    val duration = (activeLine.endSeconds - activeLine.startSeconds).coerceAtLeast(0.1f)
    val pacing = remember(activeLine.textArabic, duration) {
        ArabicPhoneticsEngine.evaluateLipSyncPacing(activeLine.textArabic, duration)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (pacing.pacingStatus) {
                ArabicPhoneticsEngine.PacingStatus.PERFECT -> Color(0xFF1E3A2F)
                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_FAST,
                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_SLOW -> Color(0xFF33291A)
                else -> Color(0xFF3B1E22)
            }
        ),
        border = BorderStroke(
            1.dp,
            when (pacing.pacingStatus) {
                ArabicPhoneticsEngine.PacingStatus.PERFECT -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_FAST,
                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_SLOW -> Color(0xFFFFB74D).copy(alpha = 0.6f)
                else -> Color(0xFFFF5252).copy(alpha = 0.6f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_lip_sync_meter_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pacing.statusEmoji, fontSize = 18.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "مقياس دقة التوقيت ومطابقة الشفاه (Lip-Sync Match)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${pacing.matchPercentage}% تطابق",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            pacing.matchPercentage >= 85 -> Color(0xFF81C784)
                            pacing.matchPercentage >= 65 -> Color(0xFFFFD54F)
                            else -> Color(0xFFFF8A80)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Dialogue Snippet
            Text(
                text = "💬 \"${activeLine.textArabic}\"",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF1F5F9),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Metrics Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المدة الحالية بالمشهد:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", pacing.actualDurationSec)} ثانية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text("المدة الصوتية المثالية:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", pacing.idealDurationSec)} ثانية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Column {
                    Text("المقاطع الصوتية:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "${pacing.syllablesCount} مقطع (${String.format(Locale.US, "%.1f", pacing.syllablesPerSecond)}/ث)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE879F9)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Advice & Action Buttons
            Text(
                text = pacing.statusMessageArabic,
                fontSize = 11.5.sp,
                color = Color(0xFFE2E8F0)
            )

            if (pacing.pacingStatus != ArabicPhoneticsEngine.PacingStatus.PERFECT) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApplySpeed(pacing.recommendedSpeedMultiplier) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("apply_pacing_speed_btn")
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "مواءمة السرعة (${String.format(Locale.US, "%.2f", pacing.recommendedSpeedMultiplier)}x) ⚡",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onAutoFitDuration,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("auto_fit_duration_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ضبط مدة السطر آلياً 🎯", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 🎛️ Interactive Sub-Frame Micro Jog Scrubber
 */
@Composable
private fun InteractiveMicroJogScrubber(
    currentSeconds: Float,
    durationSeconds: Float,
    onJog: (Float) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("micro_jog_scrubber_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "قرص التحريك الدقيق جداً (Sub-Frame Jog Wheel)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.3f ث", currentSeconds),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Micro-Jog Step Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Backward Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    JogStepButton("-50ms", onClick = { onJog(-50f) }, tag = "jog_minus_50")
                    JogStepButton("-25ms", onClick = { onJog(-25f) }, tag = "jog_minus_25")
                    JogStepButton("-5ms", onClick = { onJog(-5f) }, tag = "jog_minus_5")
                    JogStepButton("-1ms", onClick = { onJog(-1f) }, tag = "jog_minus_1")
                }

                // Center visual indicator
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                }

                // Step Forward Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    JogStepButton("+1ms", onClick = { onJog(1f) }, tag = "jog_plus_1")
                    JogStepButton("+5ms", onClick = { onJog(5f) }, tag = "jog_plus_5")
                    JogStepButton("+25ms", onClick = { onJog(25f) }, tag = "jog_plus_25")
                    JogStepButton("+50ms", onClick = { onJog(50f) }, tag = "jog_plus_50")
                }
            }
        }
    }
}

@Composable
private fun JogStepButton(label: String, onClick: () -> Unit, tag: String) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .height(28.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 📝 Script Lines Sync Precision Manager List
 */
@Composable
private fun ScriptLinesSyncPrecisionManager(
    scriptLines: List<ScriptLine>,
    currentSeconds: Float,
    onSeek: (Float) -> Unit,
    onNudgeLine: (Int, Float, Float) -> Unit,
    onAutoFit: (Int) -> Unit
) {
    if (scriptLines.isEmpty()) return

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("script_lines_sync_manager_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "جدول مقاطع الحوار وتوقيتات الشفاه (${scriptLines.size} أسطر)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            scriptLines.forEachIndexed { index, line ->
                val isActive = currentSeconds >= line.startSeconds && currentSeconds <= line.endSeconds
                val lineDuration = (line.endSeconds - line.startSeconds).coerceAtLeast(0.1f)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        if (isActive) 1.5.dp else 0.5.dp,
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Text(
                                    text = line.characterName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Timecode readout + Seek button
                            Surface(
                                onClick = { onSeek(line.startSeconds) },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", line.startSeconds)}ث - ${String.format(Locale.US, "%.2f", line.endSeconds)}ث (${String.format(Locale.US, "%.1f", lineDuration)}ث)",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Line Text
                        Text(
                            text = line.textArabic,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        // Per-line Nudge Actions & Auto Fit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    onClick = { onNudgeLine(index, -50f, 0f) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text("◀ بداية -50ms", fontSize = 9.5.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }

                                Surface(
                                    onClick = { onNudgeLine(index, 50f, 0f) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text("بداية +50ms ▶", fontSize = 9.5.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }

                                Surface(
                                    onClick = { onNudgeLine(index, 0f, 50f) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text("نهاية +50ms ▶", fontSize = 9.5.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                            }

                            Surface(
                                onClick = { onAutoFit(index) },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6750A4).copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Color(0xFF6750A4))
                            ) {
                                Text(
                                    "✨ ضبط صوتي",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

