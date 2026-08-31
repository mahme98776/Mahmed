package com.example.ui.components

import android.graphics.BitmapFactory
import android.widget.VideoView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import kotlin.math.sin

@Composable
fun VideoCanvasPlayer(
    clip: DubbingClip,
    currentSeconds: Float,
    isPlaying: Boolean,
    isRecording: Boolean,
    activeLine: ScriptLine?,
    isMutedOriginal: Boolean,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9.5f)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141218))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTogglePlay
                )
        ) {
            val isNativeVideo = clip.videoUri != null && !clip.videoUri.startsWith("sample://")

            if (isNativeVideo) {
                // Real Video View for imported video files
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(clip.videoUri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
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
                            if (Math.abs(videoView.currentPosition - targetMs) > 1200) {
                                videoView.seekTo(targetMs)
                            }
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Optional Thumbnail backdrop if not yet playing
                if (clip.thumbnailPath != null && File(clip.thumbnailPath).exists() && !isPlaying && currentSeconds < 0.5f) {
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
                            contentDescription = "Video Frame",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // Animated Canvas Scene based on category
                Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                when (clip.id) {
                    "cartoon_cat_bunny" -> {
                        // Sky & Cartoon hills
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFF93C5FD))
                            )
                        )
                        // Hills
                        val hillPath = Path().apply {
                            moveTo(0f, canvasH * 0.6f)
                            cubicTo(
                                canvasW * 0.25f, canvasH * 0.45f,
                                canvasW * 0.45f, canvasH * 0.65f,
                                canvasW * 0.7f, canvasH * 0.5f
                            )
                            cubicTo(
                                canvasW * 0.85f, canvasH * 0.4f,
                                canvasW * 0.95f, canvasH * 0.55f,
                                canvasW, canvasH * 0.5f
                            )
                            lineTo(canvasW, canvasH)
                            lineTo(0f, canvasH)
                            close()
                        }
                        drawPath(hillPath, Color(0xFFA6D4A8))

                        // Sun
                        drawCircle(
                            color = Color(0xFFFFD993),
                            radius = 28.dp.toPx(),
                            center = Offset(canvasW * 0.85f, canvasH * 0.25f)
                        )

                        // Cat & Bunny characters animated
                        val catJump = if (isPlaying) (sin(currentSeconds * 4.0) * 15f).toFloat() else 0f
                        val bunnyJump = if (isPlaying) (sin(currentSeconds * 5.0 + 1.0) * 20f).toFloat() else 0f

                        // Cat body (left)
                        drawCircle(
                            color = Color(0xFFF97316),
                            radius = 24.dp.toPx(),
                            center = Offset(canvasW * 0.3f, canvasH * 0.72f - catJump)
                        )
                        // Bunny body (right)
                        drawCircle(
                            color = Color(0xFFF1F5F9),
                            radius = 22.dp.toPx(),
                            center = Offset(canvasW * 0.7f, canvasH * 0.72f - bunnyJump)
                        )
                    }
                    "nature_lion" -> {
                        // Savanna Sunset
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF7C2D12), Color(0xFFEA580C), Color(0xFFFFD993))
                            )
                        )
                        // Sun silhouette
                        drawCircle(
                            color = Color(0xFFFEF08A),
                            radius = 45.dp.toPx(),
                            center = Offset(canvasW * 0.5f, canvasH * 0.45f)
                        )
                        // Ground silhouette
                        drawRect(
                            color = Color(0xFF1C1917),
                            topLeft = Offset(0f, canvasH * 0.78f),
                            size = Size(canvasW, canvasH * 0.22f)
                        )
                        // Tree silhouette
                        val treeX = canvasW * 0.2f
                        drawLine(
                            color = Color(0xFF1C1917),
                            start = Offset(treeX, canvasH * 0.8f),
                            end = Offset(treeX, canvasH * 0.5f),
                            strokeWidth = 8.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFF1C1917),
                            radius = 25.dp.toPx(),
                            center = Offset(treeX, canvasH * 0.48f)
                        )
                    }
                    "scifi_space" -> {
                        // Space galaxy
                        drawRect(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF2B2930), Color(0xFF1C1B1F), Color(0xFF141218)),
                                center = Offset(canvasW * 0.5f, canvasH * 0.4f),
                                radius = canvasW * 0.8f
                            )
                        )
                        // Stars
                        for (i in 0..25) {
                            val sx = (canvasW * ((i * 37) % 100) / 100f)
                            val sy = (canvasH * ((i * 73) % 100) / 100f)
                            val starGlow = if (isPlaying) (sin(currentSeconds * 3f + i) + 1f) * 1.5f else 2f
                            drawCircle(Color(0xFFE6E1E5), radius = starGlow, center = Offset(sx, sy))
                        }
                        // Mars planet
                        drawCircle(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFF2B8B5), Color(0xFF8C1D18))
                            ),
                            radius = 35.dp.toPx(),
                            center = Offset(canvasW * 0.8f, canvasH * 0.35f)
                        )
                    }
                    else -> {
                        // Sophisticated dark gradient
                        drawRect(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF2B2930), Color(0xFF332D41), Color(0xFF1C1B1F))
                            )
                        )
                    }
                }

                    // Grid vignette border
                    drawRoundRect(
                        color = Color(0xFF49454F).copy(alpha = 0.5f),
                        size = size,
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Top Status Bar Overlay (Recording Indicator, Clip Title, Mute)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recording status or Live pill
                if (isRecording) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF601410).copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, Color(0xFFF2B8B5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording",
                                tint = Color(0xFFF2B8B5),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "تسجيل مباشر REC",
                                color = Color(0xFFF2B8B5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1C1B1F).copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, Color(0xFF49454F))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = clip.coverEmoji,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = clip.category,
                                color = Color(0xFFE6E1E5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Title
                Text(
                    text = clip.title,
                    color = Color(0xFFE6E1E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 8.dp)
                )

                // Mute Original audio toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(32.dp).testTag("mute_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1C1B1F).copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, Color(0xFF49454F)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isMutedOriginal) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                tint = if (isMutedOriginal) Color(0xFFF2B8B5) else Color(0xFFE6E1E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Big Center Play/Pause button when paused
            if (!isPlaying && !isRecording) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2B2930).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clickable { onTogglePlay() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Subtitle / Dialogue Banner Overlay & Real-time Lip Movement Timing Guide
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 10.dp, end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activeLine != null) {
                    val lineDuration = (activeLine.endSeconds - activeLine.startSeconds).coerceAtLeast(0.1f)
                    val elapsedInLine = (currentSeconds - activeLine.startSeconds).coerceIn(0f, lineDuration)
                    val lineProgress = (elapsedInLine / lineDuration).coerceIn(0f, 1f)

                    // Timing & Lip-Sync Visual Guide Indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1F1D24).copy(alpha = 0.95f),
                        border = BorderStroke(1.5.dp, Color(0xFFD0BCFF).copy(alpha = 0.8f)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .testTag("lip_sync_timing_overlay")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            // Top row: Character, Timing Pill, and Lip-Sync Mouth Guide
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = activeLine.characterAvatar, fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = activeLine.characterName,
                                        color = Color(0xFFFFD993),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Real-time Lip Movement Guide Indicator
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPlaying || isRecording) Color(0xFF381E72) else Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, if (isPlaying || isRecording) Color(0xFFD0BCFF) else Color(0xFF49454F))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val lipOpenState = if (isPlaying || isRecording) {
                                            val wave = sin((currentSeconds - activeLine.startSeconds) * 14.0)
                                            if (wave > 0.3) "👄 نطق" else if (wave < -0.3) "👄 هدوء" else "👄 حركة"
                                        } else "👄 توقيت"

                                        Text(
                                            text = "$lipOpenState • ${(lineDuration - elapsedInLine).let { String.format("%.1fs", it) }}",
                                            color = Color(0xFFD0BCFF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            // Main Arabic dialogue text
                            Text(
                                text = activeLine.textArabic,
                                color = Color(0xFFFFFFFF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(4.dp))

                            // Lip-Sync Timing Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF49454F).copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(lineProgress)
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFFD993), Color(0xFFD0BCFF), Color(0xFF80CBC4))
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Progress Bar & Timecode
            val progress = (currentSeconds / clip.durationSeconds.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color(0xFF49454F).copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD0BCFF), Color(0xFFD0E4FF), Color(0xFFA6D4A8))
                            )
                        )
                )
            }
        }
    }
}
