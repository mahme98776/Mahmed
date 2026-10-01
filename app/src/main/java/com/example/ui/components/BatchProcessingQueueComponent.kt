package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AutoDubbingStyle
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.audio.batch.BatchItemStatus
import com.example.audio.batch.BatchProcessingSessionState
import com.example.audio.batch.BatchVideoItem
import com.example.model.DubbingClip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchProcessingQueueComponent(
    batchState: BatchProcessingSessionState,
    onImportVideos: (List<Uri>) -> Unit,
    onAddSamplePack: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onUpdateItemLanguage: (String, DubbingTargetLanguage) -> Unit,
    onUpdateItemStyle: (String, AutoDubbingStyle) -> Unit,
    onUpdateItemPacing: (String, DubbingPacing) -> Unit,
    onApplyGlobalLanguage: (DubbingTargetLanguage) -> Unit,
    onApplyGlobalStyle: (AutoDubbingStyle) -> Unit,
    onStartBatch: () -> Unit,
    onPauseBatch: () -> Unit,
    onResumeBatch: () -> Unit,
    onCancelBatch: () -> Unit,
    onRetryFailed: () -> Unit,
    onClearQueue: () -> Unit,
    onClearCompleted: () -> Unit,
    onPreviewClipInStudio: (DubbingClip) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBulkSettingsDialog by remember { mutableStateOf(false) }

    // Multi-video picker launcher
    val multipleVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportVideos(uris)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("batch_processing_queue_component")
    ) {
        // 1. Header Metrics Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
            border = BorderStroke(1.dp, Color(0xFF383544)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "طابور المعالجة بالدُفعة (Batch Dubbing)",
                                color = Color(0xFFF1F5F9),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = batchState.sessionStatusMessage,
                                color = if (batchState.isRunning) Color(0xFF818CF8) else Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Live Status Pill
                    if (batchState.isRunning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(
                                    Color(0xFF6366F1).copy(alpha = if (batchState.isPaused) 0.2f else pulseGlow * 0.35f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            if (batchState.isPaused) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                Text("متوقف مؤقتاً", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color(0xFF818CF8))
                                Text("جاري المعالجة", color = Color(0xFFC7D2FE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF2B2836)
                        ) {
                            Text(
                                text = "${batchState.queue.size} مقاطع في القائمة",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Stats Row: Total, Completed, Failed, Total Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BatchStatMetric(
                        title = "الإجمالي",
                        value = "${batchState.totalCount}",
                        icon = "🎬",
                        color = Color(0xFF93C5FD)
                    )
                    BatchStatMetric(
                        title = "مكتمل",
                        value = "${batchState.completedCount}",
                        icon = "✅",
                        color = Color(0xFF86EFAC)
                    )
                    BatchStatMetric(
                        title = "متبقي",
                        value = "${batchState.pendingCount}",
                        icon = "⏳",
                        color = Color(0xFFFDE047)
                    )
                    BatchStatMetric(
                        title = "المدة الكلية",
                        value = batchState.formattedTotalDuration,
                        icon = "⏱️",
                        color = Color(0xFFC4B5FD)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Dual Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التقدم الإجمالي للدُفعة",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(batchState.overallProgressFraction * 100).toInt()}%",
                            color = Color(0xFF818CF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { batchState.overallProgressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6366F1),
                        trackColor = Color(0xFF282436)
                    )
                }
            }
        }

        // 2. Action Toolbar: Add videos, Sample pack, Bulk settings, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر استيراد الفيديوهات
            Button(
                onClick = { multipleVideoLauncher.launch("video/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_import_multiple_videos")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("استيراد مقاطع فيديو من الجهاز", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            IconButton(
                onClick = { showBulkSettingsDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF272433), RoundedCornerShape(10.dp))
                    .testTag("btn_bulk_settings")
            ) {
                Icon(Icons.Default.Tune, contentDescription = "ضبط جماعي", tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onClearQueue,
                enabled = batchState.queue.isNotEmpty(),
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF272433), RoundedCornerShape(10.dp))
                    .testTag("btn_clear_queue")
            ) {
                Icon(Icons.Default.ClearAll, contentDescription = "مسح القائمة", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
            }
        }

        // 3. Queue Items List
        if (batchState.queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF211F2B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "قائمة معالجة الدُفعة فارغة حالياً",
                        color = Color(0xFFE2E8F0),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قم باستيراد مقاطع الفيديو من جهازك لمعالجتها ودبلجتها دفعة واحدة تلقائياً بالذكاء الاصطناعي.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = { multipleVideoLauncher.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("استيراد مقاطع فيديو الآن 🎬", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                itemsIndexed(
                    items = batchState.queue,
                    key = { _, item -> item.id }
                ) { index, item ->
                    BatchVideoItemCard(
                        item = item,
                        index = index,
                        totalItems = batchState.queue.size,
                        isCurrentProcessing = batchState.currentItemId == item.id,
                        onRemove = { onRemoveItem(item.id) },
                        onMoveUp = { onMoveItem(index, index - 1) },
                        onMoveDown = { onMoveItem(index, index + 1) },
                        onUpdateLanguage = { onUpdateItemLanguage(item.id, it) },
                        onUpdateStyle = { onUpdateItemStyle(item.id, it) },
                        onUpdatePacing = { onUpdateItemPacing(item.id, it) },
                        onPreviewClip = { item.resultClip?.let { onPreviewClipInStudio(it) } }
                    )
                }
            }
        }

        // 4. Sticky Bottom Execution Bar
        Surface(
            color = Color(0xFF191722),
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFF2C283B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (batchState.isRunning) {
                    if (batchState.isPaused) {
                        Button(
                            onClick = onResumeBatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_resume_batch")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("استئناف المعالجة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onPauseBatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_pause_batch")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF1E1B24))
                            Spacer(Modifier.width(6.dp))
                            Text("إيقاف مؤقت", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B24), fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = onCancelBatch,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_cancel_batch")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    val hasFailed = batchState.failedCount > 0
                    if (hasFailed) {
                        OutlinedButton(
                            onClick = onRetryFailed,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_retry_failed_batch")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إعادة الفاشلة", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onStartBatch,
                        enabled = batchState.queue.isNotEmpty() && batchState.queue.any { it.status != BatchItemStatus.COMPLETED },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            disabledContainerColor = Color(0xFF332F42)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_start_batch_processing")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (batchState.queue.isEmpty()) "أضف مقاطع لبدء الدُفعة" else "بدء معالجة الدُفعة بالكامل (${batchState.pendingCount})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Bulk Settings Dialog
    if (showBulkSettingsDialog) {
        BulkSettingsDialog(
            currentGlobalLanguage = batchState.globalTargetLanguage ?: DubbingTargetLanguage.ARABIC,
            currentGlobalStyle = batchState.globalDubbingStyle ?: AutoDubbingStyle.DOCUMENTARY,
            onApplyLanguage = {
                onApplyGlobalLanguage(it)
                showBulkSettingsDialog = false
            },
            onApplyStyle = {
                onApplyGlobalStyle(it)
                showBulkSettingsDialog = false
            },
            onDismiss = { showBulkSettingsDialog = false }
        )
    }
}

@Composable
private fun BatchStatMetric(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF16141D),
        border = BorderStroke(1.dp, Color(0xFF2C283B)),
        modifier = modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = icon, fontSize = 10.sp)
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.5.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BatchVideoItemCard(
    item: BatchVideoItem,
    index: Int,
    totalItems: Int,
    isCurrentProcessing: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onUpdateLanguage: (DubbingTargetLanguage) -> Unit,
    onUpdateStyle: (AutoDubbingStyle) -> Unit,
    onUpdatePacing: (DubbingPacing) -> Unit,
    onPreviewClip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLangMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrentProcessing -> Color(0xFF818CF8)
            item.status == BatchItemStatus.COMPLETED -> Color(0xFF10B981)
            item.status == BatchItemStatus.FAILED -> Color(0xFFEF4444)
            else -> Color(0xFF2D2A3B)
        },
        label = "item_border"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentProcessing) Color(0xFF231F30) else Color(0xFF1A1822)
        ),
        border = BorderStroke(if (isCurrentProcessing) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Index number, Title, Status badge, Reorder & Delete controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF2C283B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color(0xFFF1F5F9),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⏱️ ${item.formattedDuration}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Text(text = "•", color = Color(0xFF475569), fontSize = 10.sp)
                            Text(
                                text = "${item.status.emoji} ${item.status.titleArabic}",
                                color = when (item.status) {
                                    BatchItemStatus.COMPLETED -> Color(0xFF34D399)
                                    BatchItemStatus.PROCESSING -> Color(0xFF818CF8)
                                    BatchItemStatus.FAILED -> Color(0xFFF87171)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Control Icons: Move Up, Move Down, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0 && item.status == BatchItemStatus.QUEUED) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "أعلى", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (index < totalItems - 1 && item.status == BatchItemStatus.QUEUED) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "أسفل", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (item.status != BatchItemStatus.PROCESSING) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Configuration Chips Row (Target Language, Style, Pacing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Chip
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF252230),
                        border = BorderStroke(1.dp, Color(0xFF3B374D)),
                        modifier = Modifier.clickable(enabled = item.status == BatchItemStatus.QUEUED) {
                            showLangMenu = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = item.targetLanguage.flagEmoji, fontSize = 12.sp)
                            Text(
                                text = item.targetLanguage.displayNameArabic.split(" ").first(),
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.status == BatchItemStatus.QUEUED) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        DubbingTargetLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text("${lang.flagEmoji} ${lang.displayNameArabic}")
                                },
                                onClick = {
                                    onUpdateLanguage(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                // Style Chip
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF252230),
                        border = BorderStroke(1.dp, Color(0xFF3B374D)),
                        modifier = Modifier.clickable(enabled = item.status == BatchItemStatus.QUEUED) {
                            showStyleMenu = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = item.dubbingStyle.emoji, fontSize = 12.sp)
                            Text(
                                text = item.dubbingStyle.titleArabic.split(" ").first(),
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.status == BatchItemStatus.QUEUED) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showStyleMenu,
                        onDismissRequest = { showStyleMenu = false }
                    ) {
                        AutoDubbingStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = {
                                    Text("${style.emoji} ${style.titleArabic}")
                                },
                                onClick = {
                                    onUpdateStyle(style)
                                    showStyleMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Completed Preview Quick Action
                if (item.status == BatchItemStatus.COMPLETED && item.resultClip != null) {
                    Button(
                        onClick = onPreviewClip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("معاينة بالاستوديو", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Progress Bar during Processing
            if (item.status == BatchItemStatus.PROCESSING) {
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.statusMessage,
                            color = Color(0xFF818CF8),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${(item.progressFraction * 100).toInt()}%",
                            color = Color(0xFF818CF8),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF818CF8),
                        trackColor = Color(0xFF2C283B)
                    )
                }
            }
        }
    }
}

@Composable
private fun BulkSettingsDialog(
    currentGlobalLanguage: DubbingTargetLanguage,
    currentGlobalStyle: AutoDubbingStyle,
    onApplyLanguage: (DubbingTargetLanguage) -> Unit,
    onApplyStyle: (AutoDubbingStyle) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(currentGlobalLanguage) }
    var selectedStyle by remember { mutableStateOf(currentGlobalStyle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF818CF8))
                Text("تطبيق إعدادات موحدة على كل الطابور", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "اختر اللغة أو النمط لتطبيقهما على جميع مقاطع الفيديو الموجودة في قائمة الانتظار:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.5.sp
                )

                // Language selection
                Column {
                    Text("لغة الدبلجة الموحدة:", color = Color(0xFF94A3B8), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            DubbingTargetLanguage.ARABIC,
                            DubbingTargetLanguage.ENGLISH,
                            DubbingTargetLanguage.SPANISH,
                            DubbingTargetLanguage.FRENCH
                        ).forEach { lang ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedLang == lang) Color(0xFF6366F1) else Color(0xFF231F30),
                                border = BorderStroke(1.dp, if (selectedLang == lang) Color(0xFF818CF8) else Color(0xFF38354A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLang = lang }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = lang.flagEmoji, fontSize = 16.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = lang.displayNameArabic.split(" ").first(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Style selection
                Column {
                    Text("نمط الدبلجة الموحد:", color = Color(0xFF94A3B8), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            AutoDubbingStyle.DOCUMENTARY,
                            AutoDubbingStyle.CINEMATIC_DRAMA,
                            AutoDubbingStyle.CARTOON_FUN
                        ).forEach { style ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedStyle == style) Color(0xFF6366F1) else Color(0xFF231F30),
                                border = BorderStroke(1.dp, if (selectedStyle == style) Color(0xFF818CF8) else Color(0xFF38354A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStyle = style }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = style.emoji, fontSize = 16.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = style.titleArabic.split(" ").first(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyLanguage(selectedLang)
                    onApplyStyle(selectedStyle)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Text("تطبيق على الكل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E1B24)
    )
}
