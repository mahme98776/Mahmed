package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DubbingClip

@Composable
fun ShareDubbingOptionsDialog(
    isVisible: Boolean,
    clipTitle: String,
    clipCategory: String,
    hasRecordedAudio: Boolean,
    onDismiss: () -> Unit,
    onShareVideo: () -> Unit,
    onShareAudio: () -> Unit,
    onOpenExportDialog: () -> Unit,
    onExportAudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                        color = Color(0xFF381E72),
                        border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "مشاركة وحفظ الفيديو الناتج",
                            color = Color(0xFFE6E1E5),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = clipTitle,
                            color = Color(0xFFCAC4D0),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFFCAC4D0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Merged Video Share Option (Recommended)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2240)),
                    border = BorderStroke(1.5.dp, Color(0xFFD0BCFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onShareVideo()
                        }
                        .testTag("share_option_merged_video")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6750A4),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "مشاركة كفيديو MP4 مدمج 🎬",
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFA6D4A8)
                                ) {
                                    Text(
                                        text = "الموصى به",
                                        color = Color(0xFF1B5E20),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "مشاركة المقطع كاملاً مدمجاً مع صوتك والترجمة وتأثيرات الصدى عبر WhatsApp و Telegram وغيرها",
                                color = Color(0xFFCAC4D0),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Audio Only Share Option
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D26)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onShareAudio()
                        }
                        .testTag("share_option_audio_mp3")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF381E72),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD993),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مشاركة كملف صوتي MP3 🎵",
                                color = Color(0xFFE6E1E5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مشاركة التسجيل الصوتي المنفصل بدقة نقية ومؤثرات الاستوديو",
                                color = Color(0xFF938F99),
                                fontSize = 10.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Export Synchronized Audio Track Directly to Storage
                if (onExportAudio != null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2B22)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onExportAudio()
                            }
                            .testTag("share_option_export_audio_track")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color(0xFFA6D4A8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "حفظ وتصدير مسار الصوت المتزامن 💾",
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تصدير المسار الصوتي الكامل المتزامن بدقة WAV/MP3 وحفظه في مجلد Music بالجهاز",
                                    color = Color(0xFFA6D4A8),
                                    fontSize = 10.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color(0xFFA6D4A8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Custom Export & Save Settings (Bitrate / Resolution)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D26)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenExportDialog()
                        }
                        .testTag("share_option_export_settings")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1B3B2B),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFFA6D4A8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تصدير مخصص وضبط الجودة ⚙️",
                                color = Color(0xFFE6E1E5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اختيار الدقة (1080p/720p/4K) ومعدل الإطارات وحفظ الفيديو في الذاكرة",
                                color = Color(0xFF938F99),
                                fontSize = 10.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Copy Info and Summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1B24),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val shareText = "دبلجة مشهد: $clipTitle\nالتصنيف: $clipCategory\nتمت الدبلجة بواسطة تطبيق استوديو دبلجة المقاطع العربي 🎬🎙️"
                            clipMgr.setPrimaryClip(ClipData.newPlainText("Dubbing Info", shareText))
                            Toast.makeText(context, "تم نسخ تفاصيل ومعلومات المشهد إلى الحافظة! 📋", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "نسخ تفاصيل ونصوص المشهد إلى الحافظة",
                            color = Color(0xFFCAC4D0),
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenExportDialog()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_export_dialog_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("تصدير وحفظ الفيديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color(0xFFCAC4D0))
            }
        },
        containerColor = Color(0xFF24222B),
        shape = RoundedCornerShape(20.dp)
    )
}
