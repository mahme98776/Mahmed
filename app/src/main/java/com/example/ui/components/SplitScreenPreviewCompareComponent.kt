package com.example.ui.components

import android.graphics.BitmapFactory
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.audio.AuditReport
import com.example.audio.ScriptAuditIssue
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import com.example.ui.SplitCompareViewMode
import java.io.File

/**
 * High-fidelity Split-Screen 'Preview & Compare' and Quality Audit Component.
 * Enables creators to watch the original video alongside the newly dubbed version
 * with ultra-precise lettering, synchronized multi-track playback, real-time error
 * inspection, and one-tap save to storage.
 */
@Composable
fun SplitScreenPreviewCompareComponent(
    clip: DubbingClip,
    scriptLines: List<ScriptLine>,
    activeLine: ScriptLine?,
    currentSeconds: Float,
    isPlaying: Boolean,
    originalVolume: Float,
    dubVolume: Float,
    bgmVolume: Float,
    isOriginalMuted: Boolean,
    isDubMuted: Boolean,
    compareMode: SplitCompareViewMode,
    splitFraction: Float,
    isAbFlipActive: Boolean,
    auditReport: AuditReport?,
    isAuditRunning: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onSetCompareMode: (SplitCompareViewMode) -> Unit,
    onSetSplitFraction: (Float) -> Unit,
    onToggleAbFlip: () -> Unit,
    onOriginalVolumeChange: (Float) -> Unit,
    onDubVolumeChange: (Float) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleOriginalMute: () -> Unit,
    onToggleDubMute: () -> Unit,
    onRunQualityAudit: () -> Unit,
    onAutoFixAndDiacritize: () -> Unit,
    onSaveToStorage: () -> Unit,
    onExportVideo: () -> Unit,
    onOpenInStudio: () -> Unit,
    onUpdateLine: (Int, String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = (clip.durationSeconds.toFloat()).coerceAtLeast(1f)
    var isLooping by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Comparison Video, 1: Error Audit & Lettering, 2: Audio Mixer
    var editingLineIndex by remember { mutableStateOf<Int?>(null) }
    var editingLineText by remember { mutableStateOf("") }
    var editingLineStart by remember { mutableStateOf(0f) }
    var editingLineEnd by remember { mutableStateOf(0f) }

    // 25 FPS frame precision
    val frameDuration = 0.040f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("split_screen_preview_compare_card")
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131118)),
        border = BorderStroke(1.5.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ----------------------------------------------------
            // 1. Header Bar: Split Screen Title & Mode Switcher
            // ----------------------------------------------------
            Surface(
                color = Color(0xFF1E1926),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.25f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Compare,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المعاينة والمقارنة المزدوجة (Split-Screen)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        border = BorderStroke(0.8.dp, Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = "مباشر ⚡",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "قارن الفيديو الأصلي مع النسخة المدبلجة بدقة الحروف وتشكيل النطق",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Save & Export Action Quick Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier
                                    .clickable { onSaveToStorage() }
                                    .testTag("compare_save_to_storage_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "حفظ التخزين 💾",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Mode Selection Segmented Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141118), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1. Side-by-Side Mode
                        CompareModeOption(
                            label = "جنباً إلى جنب 🔲",
                            isSelected = compareMode == SplitCompareViewMode.SIDE_BY_SIDE,
                            onClick = { onSetCompareMode(SplitCompareViewMode.SIDE_BY_SIDE) },
                            modifier = Modifier.weight(1f)
                        )

                        // 2. Interactive Split Wipe Slider
                        CompareModeOption(
                            label = "ممسحة مقارنة ↔️",
                            isSelected = compareMode == SplitCompareViewMode.SPLIT_SLIDER,
                            onClick = { onSetCompareMode(SplitCompareViewMode.SPLIT_SLIDER) },
                            modifier = Modifier.weight(1f)
                        )

                        // 3. Fast A/B Flip Toggle
                        CompareModeOption(
                            label = "تبديل فوري A/B ⇄",
                            isSelected = compareMode == SplitCompareViewMode.AB_FLIP,
                            onClick = { onSetCompareMode(SplitCompareViewMode.AB_FLIP) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // 2. Sub-Tabs Bar: [0: Video Screen | 1: Error Audit & Lettering | 2: Audio Balancer]
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181520))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubTabButton(
                    title = "🎬 شاشة المقارنة المتزامنة",
                    badge = null,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1.2f)
                )

                SubTabButton(
                    title = "🔍 تدقيق الأخطاء والتشكيل",
                    badge = auditReport?.let { "${it.accuracyScore}%" } ?: "تدقيق",
                    badgeColor = if ((auditReport?.accuracyScore ?: 100) >= 90) Color(0xFF10B981) else Color(0xFFF59E0B),
                    isSelected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        if (auditReport == null) onRunQualityAudit()
                    },
                    modifier = Modifier.weight(1.3f)
                )

                SubTabButton(
                    title = "🎚️ توازن الأصوات",
                    badge = null,
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            // ----------------------------------------------------
            // 3. Tab Contents
            // ----------------------------------------------------
            when (selectedTab) {
                0 -> {
                    // TAB 0: Active Split Viewport
                    ComparisonViewportContent(
                        clip = clip,
                        currentSeconds = currentSeconds,
                        isPlaying = isPlaying,
                        compareMode = compareMode,
                        splitFraction = splitFraction,
                        isAbFlipActive = isAbFlipActive,
                        activeLine = activeLine,
                        isOriginalMuted = isOriginalMuted,
                        isDubMuted = isDubMuted,
                        onTogglePlay = onTogglePlay,
                        onToggleAbFlip = onToggleAbFlip,
                        onSetSplitFraction = onSetSplitFraction
                    )
                }
                1 -> {
                    // TAB 1: Comprehensive Error Audit & Lettering Precision
                    ErrorAuditAndLetteringContent(
                        auditReport = auditReport,
                        isAuditRunning = isAuditRunning,
                        scriptLines = scriptLines,
                        onRunAudit = onRunQualityAudit,
                        onAutoFix = onAutoFixAndDiacritize,
                        onEditLine = { index ->
                            val l = scriptLines.getOrNull(index)
                            if (l != null) {
                                editingLineIndex = index
                                editingLineText = l.textArabic
                                editingLineStart = l.startSeconds
                                editingLineEnd = l.endSeconds
                            }
                        }
                    )
                }
                2 -> {
                    // TAB 2: Audio Balancer
                    AudioBalanceMiniMixer(
                        originalVolume = originalVolume,
                        dubVolume = dubVolume,
                        bgmVolume = bgmVolume,
                        isOriginalMuted = isOriginalMuted,
                        isDubMuted = isDubMuted,
                        onOriginalVolumeChange = onOriginalVolumeChange,
                        onDubVolumeChange = onDubVolumeChange,
                        onBgmVolumeChange = onBgmVolumeChange,
                        onToggleOriginalMute = onToggleOriginalMute,
                        onToggleDubMute = onToggleDubMute
                    )
                }
            }

            // ----------------------------------------------------
            // 4. Master Timeline & SMPTE Timecode Scrubber
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16131D))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏱️ ${formatSeconds(currentSeconds)}",
                            fontSize = 11.5.sp,
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
                                text = "SMPTE: ${formatSmpteTimecode(currentSeconds)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "المجموع: ${formatSeconds(duration)}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Slider(
                    value = currentSeconds.coerceIn(0f, duration),
                    onValueChange = { onSeek(it) },
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFA78BFA),
                        activeTrackColor = Color(0xFF7C3AED),
                        inactiveTrackColor = Color(0xFF2D2938)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("split_compare_seekbar")
                )
            }

            // ----------------------------------------------------
            // 5. Frame-by-Frame Transport Controls & Speed
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1A27))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -1 Frame
                MiniTransportButton(
                    text = "-1 إطار",
                    icon = Icons.Default.SkipPrevious,
                    onClick = { onSeek((currentSeconds - frameDuration).coerceAtLeast(0f)) },
                    testTag = "compare_frame_back_btn"
                )

                // -1 Sec
                MiniTransportButton(
                    text = "-1 ثانية",
                    icon = Icons.Default.FastRewind,
                    onClick = { onSeek((currentSeconds - 1.0f).coerceAtLeast(0f)) },
                    testTag = "compare_sec_back_btn"
                )

                // Big Central Play / Pause
                Surface(
                    shape = CircleShape,
                    color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF7C3AED),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onTogglePlay() }
                        .testTag("compare_play_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // +1 Sec
                MiniTransportButton(
                    text = "+1 ثانية",
                    icon = Icons.Default.FastForward,
                    onClick = { onSeek((currentSeconds + 1.0f).coerceAtMost(duration)) },
                    testTag = "compare_sec_fwd_btn"
                )

                // +1 Frame
                MiniTransportButton(
                    text = "+1 إطار",
                    icon = Icons.Default.SkipNext,
                    onClick = { onSeek((currentSeconds + frameDuration).coerceAtMost(duration)) },
                    testTag = "compare_frame_fwd_btn"
                )
            }

            // ----------------------------------------------------
            // 6. Action Execution Bar (Save, Export, Open Studio)
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141118))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveToStorage,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("split_save_storage_main_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("حفظ بالتخزين 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onExportVideo,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(46.dp)
                        .testTag("split_export_video_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تصدير 📤", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onOpenInStudio,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("split_open_studio_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                    border = BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("الاستوديو 🎛️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Line Edit Dialog
    if (editingLineIndex != null) {
        AlertDialog(
            onDismissRequest = { editingLineIndex = null },
            title = {
                Text("تعديل سطر الحوار والتشكيل ✏️", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("النص العربي مع تشكيل الحركات:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = editingLineText,
                        onValueChange = { editingLineText = it },
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
                            value = String.format("%.2f", editingLineStart),
                            onValueChange = { editingLineStart = it.toFloatOrNull() ?: editingLineStart },
                            label = { Text("البداية (ث)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = String.format("%.2f", editingLineEnd),
                            onValueChange = { editingLineEnd = it.toFloatOrNull() ?: editingLineEnd },
                            label = { Text("النهاية (ث)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingLineIndex?.let { idx ->
                            onUpdateLine(idx, editingLineText, editingLineStart, editingLineEnd)
                        }
                        editingLineIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("حفظ التعديل ✓", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingLineIndex = null }) {
                    Text("إلغاء", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1A27),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F0)
        )
    }
}

// ----------------------------------------------------
// VIEWPORT RENDERERS
// ----------------------------------------------------

@Composable
private fun ComparisonViewportContent(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    compareMode: SplitCompareViewMode,
    splitFraction: Float,
    isAbFlipActive: Boolean,
    activeLine: ScriptLine?,
    isOriginalMuted: Boolean,
    isDubMuted: Boolean,
    onTogglePlay: () -> Unit,
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
                    // Left Screen: Original Video
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)))
                    ) {
                        VideoOrCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isOriginalMuted,
                            colorA = Color(0xFF0F172A),
                            colorB = Color(0xFF1E3A8A)
                        )
                        // Label
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

                    // Center Divider Line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF7C3AED))
                    )

                    // Right Screen: Newly Dubbed Video
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)))
                    ) {
                        VideoOrCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isDubMuted,
                            colorA = Color(0xFF1E1B4B),
                            colorB = Color(0xFF4338CA)
                        )
                        // Label
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

                        // Subtitle overlay on dubbed side
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
                // ↔️ Interactive Curtain Wipe
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boxW = constraints.maxWidth.toFloat()

                    // Background: Dubbed video
                    Box(modifier = Modifier.fillMaxSize()) {
                        VideoOrCanvasScene(
                            clip = clip,
                            currentSeconds = currentSeconds,
                            isPlaying = isPlaying,
                            isNativeVideo = isNativeVideo,
                            isMuted = isDubMuted,
                            colorA = Color(0xFF1E1B4B),
                            colorB = Color(0xFF4338CA)
                        )
                    }

                    // Draggable Split Divider Line & Handle
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
                        // Vertical bar
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFFACC15))
                        )
                        // Floating Drag Handle
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

                    // Top Left / Right Badges
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
                    VideoOrCanvasScene(
                        clip = clip,
                        currentSeconds = currentSeconds,
                        isPlaying = isPlaying,
                        isNativeVideo = isNativeVideo,
                        isMuted = if (isAbFlipActive) isDubMuted else isOriginalMuted,
                        colorA = if (isAbFlipActive) Color(0xFF1E1B4B) else Color(0xFF0F172A),
                        colorB = if (isAbFlipActive) Color(0xFF4338CA) else Color(0xFF1E3A8A)
                    )

                    // Big Current State Badge
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

                    // Subtitle overlay if dubbed active
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
// TAB 1: ERROR AUDIT & LETTERING PRECISION
// ----------------------------------------------------

@Composable
private fun ErrorAuditAndLetteringContent(
    auditReport: AuditReport?,
    isAuditRunning: Boolean,
    scriptLines: List<ScriptLine>,
    onRunAudit: () -> Unit,
    onAutoFix: () -> Unit,
    onEditLine: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141118))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quality Score Overview Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E1A27),
            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isAuditRunning) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = "${auditReport?.accuracyScore ?: 100}%",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مؤشر دقة الحركات والإلقاء البشري",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = auditReport?.summaryTextArabic ?: "يتم تدقيق مخارج الحروف، التشكيل، والوقفات الطبيعية...",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Re-Audit Button
                    IconButton(onClick = onRunAudit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Sync, contentDescription = "إعادة الفحص", tint = Color(0xFFA78BFA))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // One-Tap Auto Fix Button
                Button(
                    onClick = onAutoFix,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("auto_fix_all_audit_errors_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "تصحيح وضبط التشكيل والتوقيتات آلياً بنقرة واحدة 🪄",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // List of Audited Dialogue Lines with Tashkeel Highlighting
        Text(
            text = "📝 حوارات المشهد مع التدقيق الحرفي وتشكيل النطق (${scriptLines.size} سطر):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2E8F0)
        )

        scriptLines.forEachIndexed { index, line ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E1B24),
                border = BorderStroke(1.dp, Color(0xFF374151)),
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
                        Text(line.characterAvatar, fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = line.characterName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD993)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "⏱️ ${String.format("%.1f", line.startSeconds)}ث - ${String.format("%.1f", line.endSeconds)}ث",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = line.textArabic,
                                fontSize = 12.5.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onEditLine(index) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: AUDIO BALANCER
// ----------------------------------------------------

@Composable
private fun AudioBalanceMiniMixer(
    originalVolume: Float,
    dubVolume: Float,
    bgmVolume: Float,
    isOriginalMuted: Boolean,
    isDubMuted: Boolean,
    onOriginalVolumeChange: (Float) -> Unit,
    onDubVolumeChange: (Float) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleOriginalMute: () -> Unit,
    onToggleDubMute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141118))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🎚️ ميكسر التوازن الصوتي المباشر بين الأصلي والمدبلج:",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 1. Original Video Audio Volume
        VolumeChannelRow(
            label = "صوت الفيديو الأصلي",
            emoji = "🎥",
            volume = originalVolume,
            isMuted = isOriginalMuted,
            color = Color(0xFF3B82F6),
            onVolumeChange = onOriginalVolumeChange,
            onToggleMute = onToggleOriginalMute
        )

        // 2. Dubbed AI Human Voices Volume
        VolumeChannelRow(
            label = "صوت الدبلجة البشرية",
            emoji = "🎙️",
            volume = dubVolume,
            isMuted = isDubMuted,
            color = Color(0xFF10B981),
            onVolumeChange = onDubVolumeChange,
            onToggleMute = onToggleDubMute
        )

        // 3. Background Music (BGM) Volume
        VolumeChannelRow(
            label = "المؤثرات والموسيقى التصويرية",
            emoji = "🎵",
            volume = bgmVolume,
            isMuted = false,
            color = Color(0xFFF59E0B),
            onVolumeChange = onBgmVolumeChange,
            onToggleMute = {}
        )
    }
}

@Composable
private fun VolumeChannelRow(
    label: String,
    emoji: String,
    volume: Float,
    isMuted: Boolean,
    color: Color,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1A27),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = if (isMuted) "مكتوم 🔇" else "${(volume * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) Color(0xFFEF4444) else color
                    )
                }
                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = color,
                        activeTrackColor = color,
                        inactiveTrackColor = Color(0xFF2D2938)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onToggleMute, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "كتم",
                    tint = if (isMuted) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// HELPER COMPOSABLES
// ----------------------------------------------------

@Composable
private fun CompareModeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) Color(0xFF7C3AED) else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun SubTabButton(
    title: String,
    badge: String?,
    badgeColor: Color = Color(0xFF10B981),
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF2D2438) else Color(0xFF1E1A27),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF7C3AED) else Color(0xFF374151)),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                maxLines = 1
            )
            if (badge != null) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(0.6.dp, badgeColor)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniTransportButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF272332),
        border = BorderStroke(1.dp, Color(0xFF494255)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(3.dp))
            Text(text = text, fontSize = 9.5.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VideoOrCanvasScene(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    isNativeVideo: Boolean,
    isMuted: Boolean,
    colorA: Color,
    colorB: Color
) {
    if (isNativeVideo) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(clip.videoUri)
                    setOnPreparedListener { mp ->
                        if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                        if (isPlaying) start()
                    }
                }
            },
            update = { videoView ->
                try {
                    if (isPlaying && !videoView.isPlaying) {
                        videoView.start()
                    } else if (!isPlaying && videoView.isPlaying) {
                        videoView.pause()
                    }
                    val targetMs = (currentSeconds * 1000).toInt()
                    if (Math.abs(videoView.currentPosition - targetMs) > 1000) {
                        videoView.seekTo(targetMs)
                    }
                } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(listOf(colorA, colorB))
            )

            val waveOffset = (currentSeconds * 25f) % w
            for (i in 0..8) {
                val x = (i * w / 8f + waveOffset) % w
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }
        }
    }
}

private fun formatSmpteTimecode(seconds: Float): String {
    val totalMs = (seconds * 1000).toInt()
    val mins = (totalMs / 60000) % 60
    val secs = (totalMs / 1000) % 60
    val frames = ((totalMs % 1000) / 40) % 25
    return "%02d:%02d:%02d".format(mins, secs, frames)
}

private fun formatSeconds(seconds: Float): String {
    val totalSecs = seconds.toInt()
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val ms = ((seconds - totalSecs) * 10).toInt()
    return "%02d:%02d.%d".format(mins, secs, ms)
}
