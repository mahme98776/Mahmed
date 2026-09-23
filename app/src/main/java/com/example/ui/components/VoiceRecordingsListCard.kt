package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.VoiceRecordingEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مكون قائمة لعرض التسجيلات الصوتية المسجلة المخزنة في قاعدة بيانات Room
 * مع توفير زر تشغيل ومعاينة لكل ملف باستخدام MediaPlayer
 * وصندوق حوار (AlertDialog) لتأكيد الحذف قبل إتمام العملية لضمان عدم ضياع البيانات.
 */
@Composable
fun VoiceRecordingsListCard(
    recordings: List<VoiceRecordingEntity>,
    currentlyPlayingId: Long?,
    onTogglePlay: (VoiceRecordingEntity) -> Unit,
    onDelete: (VoiceRecordingEntity) -> Unit,
    onUseInStudio: (VoiceRecordingEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var recordingToDelete by remember { mutableStateOf<VoiceRecordingEntity?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_voice_recordings_room_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B2E)),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
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
                        shape = CircleShape,
                        color = Color(0xFF9C27B0).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFCE93D8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "مكتبة التسجيلات الصوتية (Room DB)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFF3E5F5)
                        )
                        Text(
                            text = "تسجيلات محفوظة محلياً متاحة دوماً للمعاينة والدبلجة",
                            fontSize = 10.5.sp,
                            color = Color(0xFFB39DDB)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF9C27B0).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${recordings.size} تسجيل",
                        color = Color(0xFFE1BEE7),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Empty State
            if (recordings.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141220),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = null,
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "لا توجد تسجيلات صوتية مسجلة بعد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFD1C4E9)
                        )
                        Text(
                            text = "عند تسجيل أي مقطع صوتي يتم حفظه فورياً في قاعدة بيانات Room مع مسار الملف لضمان بقائه متاحاً بعد إعادة فتح التطبيق.",
                            fontSize = 11.sp,
                            color = Color(0xFF9575CD),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // List of Recordings
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recordings.forEach { recording ->
                        VoiceRecordingRowItem(
                            recording = recording,
                            isPlaying = currentlyPlayingId == recording.id,
                            onTogglePlay = { onTogglePlay(recording) },
                            onRequestDelete = { recordingToDelete = recording },
                            onUseInStudio = { onUseInStudio(recording) }
                        )
                    }
                }
            }
        }
    }

    // AlertDialog لتأكيد حذف المقطع الصوتي
    if (recordingToDelete != null) {
        val targetRec = recordingToDelete!!
        AlertDialog(
            modifier = Modifier.testTag("delete_voice_recording_dialog"),
            onDismissRequest = { recordingToDelete = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "تأكيد حذف المقطع الصوتي 🗑️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف هذا المقطع الصوتي بشكل نهائي؟",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Details Card of the item to delete
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = targetRec.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "⏱️ المدة: ${String.format(Locale.getDefault(), "%.1f", targetRec.durationSeconds)} ثانية • الحجم: ${(targetRec.fileSizeBytes / 1024)} KB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "📁 المسار: ${targetRec.filePath}",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "⚠️ تنبيه: ستتم إزالة الملف من ذاكرة الجهاز ومن قاعدة بيانات Room، ولن يمكن استرجاعه بعد ذلك.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(targetRec)
                        recordingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_delete_recording_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "نعم، حذف المقطع",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { recordingToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_delete_recording_button")
                ) {
                    Text(
                        text = "إلغاء",
                        fontSize = 12.5.sp
                    )
                }
            }
        )
    }
}

/**
 * عنصر فردي لكل تسجيل صوتي مع زر تشغيل MediaPlayer وزر الدبلجة وزر الحذف
 */
@Composable
fun VoiceRecordingRowItem(
    recording: VoiceRecordingEntity,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onRequestDelete: () -> Unit,
    onUseInStudio: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_playing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val dateFormatted = remember(recording.timestamp) {
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(recording.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isPlaying) Color(0xFF2A203F) else Color(0xFF161424),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFFAB47BC) else Color(0xFF3B3353)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_recording_row_${recording.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Title, Date, and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = if (isPlaying) Color(0xFFE1BEE7) else Color(0xFFEDE7F6),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ ${String.format(Locale.getDefault(), "%.1f", recording.durationSeconds)}ث",
                            fontSize = 10.5.sp,
                            color = Color(0xFFCE93D8),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "💾 ${(recording.fileSizeBytes / 1024)} KB",
                            fontSize = 10.sp,
                            color = Color(0xFFB39DDB)
                        )
                        Text(
                            text = "📅 $dateFormatted",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                // Effect / Gender Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, Color(0xFFBA68C8).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = recording.voiceEffect,
                        color = Color(0xFFE1BEE7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Path & Script preview
            if (recording.associatedScript.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F0E17),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "النص: ${recording.associatedScript}",
                        fontSize = 10.5.sp,
                        color = Color(0xFFD1C4E9),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button with MediaPlayer
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFF7B1FA2) else Color(0xFF4A148C)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("play_recording_btn_${recording.id}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف المعاينة" else "تشغيل ومعاينة المقطع",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "إيقاف المعاينة" else "معاينة المقطع 🎧",
                        fontSize = 11.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (isPlaying) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = pulseAlpha))
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Use in Studio button
                    Button(
                        onClick = onUseInStudio,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("use_in_studio_btn_${recording.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "استخدام للدبلجة",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "دبلجة 🎬",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete Button - Opens the AlertDialog
                    IconButton(
                        onClick = onRequestDelete,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("delete_recording_btn_${recording.id}")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD32F2F).copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, Color(0xFFEF5350).copy(alpha = 0.4f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف المقطع الصوتي",
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
