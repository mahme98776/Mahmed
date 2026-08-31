package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ArabicPhoneticsEngine
import com.example.model.ScriptLine
import java.util.Locale

@Composable
fun TeleprompterItem(
    line: ScriptLine,
    isActive: Boolean,
    onSpeak: () -> Unit,
    onClick: () -> Unit,
    onNudge: ((Float, Float) -> Unit)? = null,
    onAutoFit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val duration = (line.endSeconds - line.startSeconds).coerceAtLeast(0.1f)
    val pacing = ArabicPhoneticsEngine.evaluateLipSyncPacing(line.textArabic, duration)

    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFD0BCFF) else Color(0xFF49454F),
        animationSpec = tween(300),
        label = "border_color"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF332D41) else Color(0xFF2B2930),
        animationSpec = tween(300),
        label = "container_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("teleprompter_item_${line.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Character name & Timestamps & Lip-Sync Match & TTS preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4A4458),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = line.characterAvatar, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = line.characterName,
                        color = if (isActive) Color(0xFFFFD993) else Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Lip-Sync Match Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (pacing.pacingStatus) {
                            ArabicPhoneticsEngine.PacingStatus.PERFECT -> Color(0xFF2E7D32).copy(alpha = 0.25f)
                            ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_FAST,
                            ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_SLOW -> Color(0xFFFF8F00).copy(alpha = 0.25f)
                            else -> Color(0xFFC62828).copy(alpha = 0.25f)
                        },
                        border = BorderStroke(
                            0.5.dp,
                            when (pacing.pacingStatus) {
                                ArabicPhoneticsEngine.PacingStatus.PERFECT -> Color(0xFF4CAF50)
                                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_FAST,
                                ArabicPhoneticsEngine.PacingStatus.SLIGHTLY_SLOW -> Color(0xFFFFB74D)
                                else -> Color(0xFFFF5252)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pacing.statusEmoji, fontSize = 9.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${pacing.matchPercentage}% تطابق",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Time badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF141218).copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fث - %.1fث", line.startSeconds, line.endSeconds),
                            color = Color(0xFF938F99),
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Speak line button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استماع",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Arabic Dialogue Line
            Text(
                text = line.textArabic,
                color = if (isActive) Color(0xFFE6E1E5) else Color(0xFFCAC4D0),
                fontSize = if (isActive) 15.sp else 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 22.sp
            )

            // Optional Original line (English / source)
            if (line.textOriginal.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = line.textOriginal,
                    color = Color(0xFF938F99),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Interactive Precision Micro-Adjust Bar (When active)
            if (isActive && (onNudge != null || onAutoFit != null)) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onNudge != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                onClick = { onNudge(-50f, 0f) },
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF141218).copy(alpha = 0.7f),
                                border = BorderStroke(0.5.dp, Color(0xFF49454F))
                            ) {
                                Text("◀ -50ms", fontSize = 9.sp, color = Color(0xFFD0BCFF), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }

                            Surface(
                                onClick = { onNudge(50f, 0f) },
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF141218).copy(alpha = 0.7f),
                                border = BorderStroke(0.5.dp, Color(0xFF49454F))
                            ) {
                                Text("+50ms ▶", fontSize = 9.sp, color = Color(0xFFD0BCFF), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    if (onAutoFit != null) {
                        Surface(
                            onClick = onAutoFit,
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF381E72),
                            border = BorderStroke(0.5.dp, Color(0xFFD0BCFF))
                        ) {
                            Text(
                                "✨ مواءمة الشفاه الصوتية",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
