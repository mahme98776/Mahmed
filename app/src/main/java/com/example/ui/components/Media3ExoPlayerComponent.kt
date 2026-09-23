package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

/**
 * مكون واجهة مشغل وسائط متكامل وعالي الأداء مدعوم بمكتبة Media3 ExoPlayer.
 * يدعم تشغيل ملفات الفيديو والصوت المستوردة مع تحكم كامل بالزمن ومستوى الصوت والسرعة والترجمة.
 */
@OptIn(UnstableApi::class)
@Composable
fun Media3ExoPlayerComponent(
    mediaUri: String?,
    title: String = "مشغل الوسائط ExoPlayer",
    subtitle: String? = null,
    isPlaying: Boolean = false,
    currentSeconds: Float = 0f,
    activeSubtitleText: String? = null,
    characterName: String? = null,
    isMuted: Boolean = false,
    onTogglePlay: () -> Unit = {},
    onSeekToSeconds: (Float) -> Unit = {},
    onToggleMute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Internal state tracking
    var isMediaLoading by remember { mutableStateOf(true) }
    var hasMediaError by remember { mutableStateOf<String?>(null) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var isAudioOnlyMode by remember { mutableStateOf(false) }

    // Identify if media is purely audio
    LaunchedEffect(mediaUri) {
        if (mediaUri != null) {
            val lower = mediaUri.lowercase(Locale.ROOT)
            isAudioOnlyMode = lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                    lower.endsWith(".m4a") || lower.endsWith(".aac") ||
                    lower.endsWith(".ogg") || lower.startsWith("audio/")
        }
    }

    // ExoPlayer lifecycle management
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = isPlaying
        }
    }

    // Set listener for state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isMediaLoading = true
                    }
                    Player.STATE_READY -> {
                        isMediaLoading = false
                        totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                        hasMediaError = null
                    }
                    Player.STATE_ENDED -> {
                        isMediaLoading = false
                    }
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isMediaLoading = false
                hasMediaError = "خطأ في تشغيل الوسائط: ${error.localizedMessage ?: "تنسيق غير مدعوم"}"
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Synchronize media item when URI changes
    LaunchedEffect(mediaUri) {
        if (mediaUri.isNullOrBlank()) {
            exoPlayer.clearMediaItems()
            hasMediaError = "لم يتم تحديد مسار فيديو أو صوت صالح"
            isMediaLoading = false
            return@LaunchedEffect
        }

        try {
            isMediaLoading = true
            hasMediaError = null
            val parsedUri = if (mediaUri.startsWith("/") || mediaUri.startsWith("file://")) {
                val cleanPath = mediaUri.removePrefix("file://")
                Uri.fromFile(File(cleanPath))
            } else if (mediaUri.startsWith("content://") || mediaUri.startsWith("http://") || mediaUri.startsWith("https://")) {
                Uri.parse(mediaUri)
            } else if (mediaUri.startsWith("asset:///")) {
                Uri.parse(mediaUri)
            } else {
                Uri.parse(mediaUri)
            }

            val mediaItem = MediaItem.fromUri(parsedUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (isPlaying) {
                exoPlayer.play()
            }
        } catch (e: Exception) {
            hasMediaError = "تعذر تحميل الوسائط: ${e.localizedMessage}"
            isMediaLoading = false
        }
    }

    // Synchronize play / pause state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (!exoPlayer.isPlaying) {
                exoPlayer.play()
            }
        } else {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            }
        }
    }

    // Synchronize volume / mute
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Synchronize external seek
    LaunchedEffect(currentSeconds) {
        val targetMs = (currentSeconds * 1000).toLong()
        if (Math.abs(exoPlayer.currentPosition - targetMs) > 1200) {
            exoPlayer.seekTo(targetMs)
        }
    }

    // Periodic time progress tracker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(200)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("media3_exoplayer_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1B29))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4F46E5).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF818CF8)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAudioOnlyMode) Icons.Default.MusicNote else Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isAudioOnlyMode) "مشغل صوت Media3 ExoPlayer 🎚️" else "مشغل فيديو عالي الدقة Media3 ExoPlayer 🎬",
                            fontSize = 10.5.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                // Speed toggle chip
                Surface(
                    onClick = {
                        playbackSpeed = when (playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.75f
                            else -> 1.0f
                        }
                        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF312E81).copy(alpha = 0.4f),
                    border = BorderStroke(0.8.dp, Color(0xFF6366F1)),
                    modifier = Modifier.testTag("exoplayer_speed_toggle_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFFC7D2FE),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${playbackSpeed}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC7D2FE)
                        )
                    }
                }
            }

            // Viewport Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            showControls = !showControls
                        }
                    )
            ) {
                if (mediaUri.isNullOrBlank() || hasMediaError != null) {
                    // Empty or Error Placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFF4B5563),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = hasMediaError ?: "يرجى استيراد ملف فيديو أو صوت لعرضه عبر ExoPlayer",
                                color = if (hasMediaError != null) Color(0xFFF87171) else Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isAudioOnlyMode) {
                    // Audio Mode Visualizer Backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4338CA).copy(alpha = 0.35f),
                                border = BorderStroke(2.dp, Color(0xFF818CF8)),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "مسار صوتي نشط • ExoPlayer Media3 Engine",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // Native Video View via AndroidView wrapping Media3 PlayerView
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false // We provide rich custom Jetpack Compose controls below
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = { view ->
                            if (view.player != exoPlayer) {
                                view.player = exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Loading Indicator
                if (isMediaLoading && mediaUri != null && hasMediaError == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF818CF8),
                            modifier = Modifier.size(42.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Active Subtitle Line Overlay
                if (!activeSubtitleText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (showControls) 48.dp else 16.dp)
                            .fillMaxWidth(0.9f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.82f),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.7f)),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!characterName.isNullOrBlank()) {
                                    Text(
                                        text = characterName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD54F)
                                    )
                                }
                                Text(
                                    text = activeSubtitleText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Center Play Button Overlay on Pause
                if (!isPlaying && !isMediaLoading && mediaUri != null && hasMediaError == null) {
                    Surface(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        color = Color(0xFF1E1B4B).copy(alpha = 0.85f),
                        border = BorderStroke(1.5.dp, Color(0xFF818CF8)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .testTag("exoplayer_center_play_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Controls & Seekbar Footer
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1B29))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Time text and progress slider
                    val durSec = if (totalDurationMs > 0) totalDurationMs / 1000f else 10f
                    val currentSec = (currentPositionMs / 1000f).coerceIn(0f, durSec)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                        Slider(
                            value = currentSec,
                            onValueChange = { newSec ->
                                currentPositionMs = (newSec * 1000).toLong()
                                exoPlayer.seekTo(currentPositionMs)
                                onSeekToSeconds(newSec)
                            },
                            valueRange = 0f..durSec,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF818CF8),
                                activeTrackColor = Color(0xFF6366F1),
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(22.dp)
                                .testTag("exoplayer_timeline_slider")
                        )
                        Text(
                            text = formatTime(totalDurationMs),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Media Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Mute & Volume
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("exoplayer_mute_btn")
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (isMuted) "إلغاء الكتم" else "كتم الصوت",
                                    tint = if (isMuted) Color(0xFFEF4444) else Color(0xFFC7D2FE),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Center: Navigation & Playback Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rewind 5s
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    onSeekToSeconds(newPos / 1000f)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "ترجيع 5 ثوانٍ",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Play / Pause Main Button
                            Surface(
                                onClick = onTogglePlay,
                                shape = CircleShape,
                                color = Color(0xFF4F46E5),
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("exoplayer_footer_play_toggle")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Forward 5s
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition + 5000L).coerceAtMost(exoPlayer.duration)
                                    exoPlayer.seekTo(newPos)
                                    onSeekToSeconds(newPos / 1000f)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "تقديم 5 ثوانٍ",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Right: Replay from start
                        IconButton(
                            onClick = {
                                exoPlayer.seekTo(0L)
                                onSeekToSeconds(0f)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "إعادة التشغيل من البداية",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
