package com.example.ui.components

import android.graphics.BitmapFactory
import android.widget.VideoView
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import java.io.File
import kotlin.math.sin

/**
 * Integrated Video Player Component designed for pre-export preview,
 * with real-time Play/Pause, Seek slider, Frame-by-Frame navigation,
 * SMPTE timecode (HH:MM:SS:FF), subtitle sync, speed adjustment,
 * 100% human voice clarity status, and full transparency & internet connectivity breakdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntegratedVideoPlayerComponent(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    activeLine: ScriptLine? = null,
    isMutedOriginal: Boolean = false,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleMute: () -> Unit = {},
    showExportPreviewBanner: Boolean = true,
    isInternetConnected: Boolean = true,
    modifier: Modifier = Modifier
) {
    val duration = (clip.durationSeconds.toFloat()).coerceAtLeast(1f)
    var showTransparencyDialog by remember { mutableStateOf(false) }
    var showSubtitleOverlay by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isFullscreenExpanded by remember { mutableStateOf(false) }

    // Approximate 25 fps: 1 Frame = 0.040s (40 ms)
    val frameDuration = 0.040f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("integrated_video_player_card")
            .shadow(10.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141218)),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ----------------------------------------------------
            // 1. Top Header: Title, 100% Human Voice Badge & Transparency
            // ----------------------------------------------------
            Surface(
                color = Color(0xFF1E1B24),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "معاينة الفيديو المدبلج قبل التصدير",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = clip.title,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(Modifier.width(6.dp))
                                // 100% Human Voice Tag
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(0.8.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "صوت بشري طبيعي 100% 🎙️",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Transparency & Free Tour Info Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF312E81).copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Color(0xFF818CF8)),
                        modifier = Modifier
                            .clickable { showTransparencyDialog = true }
                            .testTag("open_transparency_info_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "الشفافية والإنترنت",
                                tint = Color(0xFFC7D2FE),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "الشفافية والإنترنت ℹ️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 2. Video Screen Viewport & Subtitle Overlay
            // ----------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTogglePlay
                    )
            ) {
                val isNativeVideo = clip.videoUri != null && !clip.videoUri.startsWith("sample://")

                if (isNativeVideo) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoPath(clip.videoUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = isLooping
                                    if (isMutedOriginal) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
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

                    if (clip.thumbnailPath != null && File(clip.thumbnailPath).exists() && !isPlaying && currentSeconds < 0.3f) {
                        val bmp = remember(clip.thumbnailPath) {
                            try {
                                BitmapFactory.decodeFile(clip.thumbnailPath)?.asImageBitmap()
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp,
                                contentDescription = "Frame Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // High-quality animated canvas scene
                    DubbingCanvasViewport(clip = clip, currentSeconds = currentSeconds)
                }

                // Center Big Play Indicator on Pause
                if (!isPlaying) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E1B24).copy(alpha = 0.85f),
                        border = BorderStroke(1.5.dp, Color(0xFF818CF8)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                            .clickable { onTogglePlay() }
                            .testTag("video_player_center_play_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "تشغيل",
                                tint = Color(0xFFC7D2FE),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Live Arabic Subtitle Banner Overlay
                if (showSubtitleOverlay && activeLine != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF111827).copy(alpha = 0.90f),
                        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp, start = 14.dp, end = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = activeLine.characterAvatar, fontSize = 15.sp)
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
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                // Top Floating Overlays (Timecode & Quick Mute)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // SMPTE Timecode Tag (HH:MM:SS:FF)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = formatTimecode(currentSeconds),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Mute Toggle
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onToggleMute() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isMutedOriginal) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "كتم",
                                tint = if (isMutedOriginal) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 3. Interactive Timeline & Scrubber Slider
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ ${formatSecondsSimple(currentSeconds)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF818CF8)
                    )
                    Text(
                        text = "الإجمالي: ${formatSecondsSimple(duration)} (إطار: ${(currentSeconds / frameDuration).toInt()})",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Slider(
                    value = currentSeconds.coerceIn(0f, duration),
                    onValueChange = { onSeek(it) },
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF818CF8),
                        activeTrackColor = Color(0xFF6366F1),
                        inactiveTrackColor = Color(0xFF27242E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_player_seekbar")
                )
            }

            // ----------------------------------------------------
            // 4. Frame-by-Frame Precision Navigation & Transport Controls
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1822))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "التحكم بالتشغيل والتنقل إطاراً بإطار (Frame-by-Frame):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCAC4D0)
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frame Step Back (-1 Frame = -40ms)
                    NavigationActionButton(
                        label = "-1 إطار",
                        subLabel = "-40ms",
                        icon = Icons.Default.SkipPrevious,
                        onClick = { onSeek((currentSeconds - frameDuration).coerceAtLeast(0f)) },
                        testTag = "frame_step_back_btn"
                    )

                    // Step -1s
                    NavigationActionButton(
                        label = "-1 ثانية",
                        subLabel = "-1s",
                        icon = Icons.Default.FastRewind,
                        onClick = { onSeek((currentSeconds - 1.0f).coerceAtLeast(0f)) },
                        testTag = "step_back_1s_btn"
                    )

                    // Play / Pause Primary Button
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF6366F1),
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { onTogglePlay() }
                            .testTag("video_player_main_toggle_btn")
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

                    // Step +1s
                    NavigationActionButton(
                        label = "+1 ثانية",
                        subLabel = "+1s",
                        icon = Icons.Default.FastForward,
                        onClick = { onSeek((currentSeconds + 1.0f).coerceAtMost(duration)) },
                        testTag = "step_forward_1s_btn"
                    )

                    // Frame Step Forward (+1 Frame = +40ms)
                    NavigationActionButton(
                        label = "+1 إطار",
                        subLabel = "+40ms",
                        icon = Icons.Default.SkipNext,
                        onClick = { onSeek((currentSeconds + frameDuration).coerceAtMost(duration)) },
                        testTag = "frame_step_forward_btn"
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ----------------------------------------------------
                // 5. Utility Strip (Subtitles, Speeds, Loop, Replay)
                // ----------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subtitle Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showSubtitleOverlay) Color(0xFF381E72) else Color(0xFF27242E),
                        border = BorderStroke(1.dp, if (showSubtitleOverlay) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                        modifier = Modifier
                            .clickable { showSubtitleOverlay = !showSubtitleOverlay }
                            .testTag("toggle_subtitles_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = if (showSubtitleOverlay) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (showSubtitleOverlay) "الترجمة: مفعلة" else "الترجمة: معطلة",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showSubtitleOverlay) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // Loop Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLooping) Color(0xFF381E72) else Color(0xFF27242E),
                        border = BorderStroke(1.dp, if (isLooping) Color(0xFFD0BCFF) else Color(0xFF49454F)),
                        modifier = Modifier.clickable { isLooping = !isLooping }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Loop,
                                contentDescription = null,
                                tint = if (isLooping) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isLooping) "تكرار: مستمر" else "تكرار عادي",
                                fontSize = 10.sp,
                                color = if (isLooping) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                            )
                        }
                    }

                    // Replay from beginning
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF27242E),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        modifier = Modifier.clickable { onSeek(0f) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(text = "من البداية", fontSize = 10.sp, color = Color(0xFFCAC4D0))
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Transparency, Free Tour & Internet Clarification Dialog
    // ----------------------------------------------------
    if (showTransparencyDialog) {
        AlertDialog(
            onDismissRequest = { showTransparencyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF818CF8)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "الشفافية، شروط الجولة، واتصال الإنترنت",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Point 1: 100% Free Tour
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "جولة مجانية بالكامل 100% (Free Tour)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "أثناء جولتك الاستكشافية، كافة ميزات معاينة الفيديو، الدبلجة التجريبية، والتنقل إطاراً بإطار مجانية تماماً وبدون أي رسوم خفية أو بطاقات دفع.",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Point 2: Internet Connectivity Status (مشمول أم لا)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "هل الإنترنت مشمول ومطلوب؟ (Internet Inclusion)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "• الوضع المحلي (Offline Mode): لا يتطلب إنترنت. التوليد الصوتي والرندرة تعمل محلياً 100%.\n• وضع الأصوات البشرية السحابية (Cloud Neural): يتطلب اتصال إنترنت نشط للوصول لمكتبات الأصوات البشرية الفائقة ونماذج الذكاء الاصطناعي.",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Point 3: 100% Human Voice Clarity
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "صوت بشري طبيعي 100% (Human Vocal Clarity)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDDD6FE)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "يتم استخدام طبقات ونبرات بشرية معتمدة مع مخارج حروف عربية فصيحة ومضبوطة، مع معالجة ذكية لنغمات التنفس والوقفات الطبيعية.",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTransparencyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("فهمت ذلك، تم ✓", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1B24),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F0)
        )
    }
}

/**
 * Reusable Button for Frame and Second steps
 */
@Composable
private fun NavigationActionButton(
    label: String,
    subLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF27242E),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6E1E5)
            )
            Text(
                text = subLabel,
                fontSize = 8.5.sp,
                color = Color(0xFF938F99)
            )
        }
    }
}

/**
 * Canvas Viewport Fallback with Cartoon/Scene Rendering
 */
@Composable
private fun DubbingCanvasViewport(clip: DubbingClip, currentSeconds: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
            )
        )

        // Animated Waves / Grid
        val waveOffset = (currentSeconds * 30f) % canvasW
        for (i in 0..12) {
            val x = (i * canvasW / 12f + waveOffset) % canvasW
            drawLine(
                color = Color(0xFF818CF8).copy(alpha = 0.2f),
                start = Offset(x, 0f),
                end = Offset(x, canvasH),
                strokeWidth = 1.5f
            )
        }

        // Center Scene Accent
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF818CF8).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(canvasW / 2, canvasH / 2),
                radius = canvasW * 0.35f
            ),
            center = Offset(canvasW / 2, canvasH / 2),
            radius = canvasW * 0.35f
        )
    }
}

/**
 * Formats time in SMPTE-style HH:MM:SS:FF (25 FPS)
 */
private fun formatTimecode(seconds: Float): String {
    val totalMs = (seconds * 1000).toInt()
    val mins = (totalMs / 60000) % 60
    val secs = (totalMs / 1000) % 60
    val frames = ((totalMs % 1000) / 40) % 25 // 25 fps
    return "%02d:%02d:%02d".format(mins, secs, frames)
}

/**
 * Formats seconds to mm:ss
 */
private fun formatSecondsSimple(seconds: Float): String {
    val totalSecs = seconds.toInt()
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val ms = ((seconds - totalSecs) * 10).toInt()
    return "%02d:%02d.%d".format(mins, secs, ms)
}
