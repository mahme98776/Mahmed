package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.DubbingDialect
import com.example.audio.gemini.VideoAudioTranscriptionSegment
import com.example.ui.DubbingViewModel
import com.example.ui.VideoSpeechToTextUiState
import java.util.Locale

/**
 * 🎙️ VideoSpeechToTextComponent
 * مكون تفريغ الكلام المنطوق وحساب التوقيتات داخل الفيديو (Speech to Text & Timestamps).
 * يكتشف لحظات الكلام بالدقة الزمنية (أجزاء الثانية)، مع تحديد المتحدث،
 * وإمكانية الانتقال الفوري للتوقيت داخل الفيديو، والتعديل، والتصدير كـ SRT/VTT.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoSpeechToTextComponent(
    viewModel: DubbingViewModel,
    modifier: Modifier = Modifier,
    onSeekToVideoSeconds: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val sttState by viewModel.videoSpeechToTextState.collectAsState()
    val studioUiState by viewModel.uiState.collectAsState()
    val autoDubState by viewModel.autoDubberState.collectAsState()

    var showContextPromptField by remember { mutableStateOf(false) }
    var contextPromptInput by remember { mutableStateOf("") }
    var editingSegment by remember { mutableStateOf<VideoAudioTranscriptionSegment?>(null) }
    var showAddManualDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDiagnosticsLogs by remember { mutableStateOf(false) }

    // Filter segments based on search query and speaker filter
    val filteredSegments = remember(sttState.segments, sttState.searchQuery, sttState.speakerFilter) {
        sttState.segments.filter { segment ->
            val matchesQuery = if (sttState.searchQuery.isBlank()) true else {
                segment.originalSpeech.contains(sttState.searchQuery, ignoreCase = true) ||
                segment.arabicDubbedAdaptation.contains(sttState.searchQuery, ignoreCase = true) ||
                segment.speaker.contains(sttState.searchQuery, ignoreCase = true)
            }
            val matchesSpeaker = if (sttState.speakerFilter == null) true else {
                segment.speaker == sttState.speakerFilter
            }
            matchesQuery && matchesSpeaker
        }
    }

    val uniqueSpeakers = remember(sttState.segments) {
        sttState.segments.map { it.speaker }.distinct()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_speech_to_text_main_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎙️", fontSize = 22.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تفريغ الكلام وتوقيتات الفيديو",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "STT + Timestamps ⏱️",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "كشف لحظات الكلام وتحديد المتحدث والتوقيت بدقة أجزاء الثانية",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stat Badges Bar
            if (sttState.segments.isNotEmpty()) {
                val totalSpokenDuration = remember(sttState.segments) {
                    sttState.segments.sumOf { it.durationSeconds.toDouble() }.toFloat()
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${sttState.segments.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("مقطع كلامي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f ث", totalSpokenDuration),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF10B981)
                            )
                            Text("إجمالي زمن الكلام", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uniqueSpeakers.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF8B5CF6)
                            )
                            Text("شخصيات متحدثة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Dialect Selector Pills
            Text(
                text = "اللهجة المستهدفة لتطابق الكلام والدبلجة:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DubbingDialect.values().forEach { dialect ->
                    val isSelected = sttState.selectedDialect == dialect
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSpeechToTextDialect(dialect) },
                        label = {
                            Text(
                                text = "${dialect.flagEmoji} ${dialect.displayNameArabic}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier.testTag("stt_dialect_${dialect.name}")
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action: Start Transcription or Reload
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val videoUri = autoDubState.importedVideo?.uriString?.let { android.net.Uri.parse(it) }
                        viewModel.transcribeVideoForTimestamps(
                            videoUri = videoUri,
                            targetDialect = sttState.selectedDialect,
                            customPromptContext = contextPromptInput
                        )
                    },
                    enabled = !sttState.isTranscribing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_stt_transcription_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    if (sttState.isTranscribing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "جاري تفريغ وحساب التوقيتات...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (sttState.segments.isEmpty()) "بدء تفريغ الكلام والتوقيتات" else "إعادة تفريغ الكلام 🔄",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Progress Indicator during transcription
            if (sttState.isTranscribing) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { sttState.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6366F1)
                )
                if (sttState.statusMessage.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "⏳ ${sttState.statusMessage}",
                        fontSize = 11.sp,
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Diagnostics & Telemetry Inspection Panel
            if (sttState.result != null) {
                Spacer(Modifier.height(10.dp))
                val result = sttState.result!!
                val isSuccess = result.isFromLiveGeminiApi
                val isFallback = result.isFallbackUsed
                val statusBg = if (isSuccess) Color(0xFF10B981) else if (isFallback) Color(0xFFF59E0B) else Color(0xFFEF4444)
                val badgeText = if (isSuccess) "سحابة Gemini متصلة بنجاح 🟢 (HTTP ${result.httpStatusCode ?: 200})"
                                else if (isFallback) "الدبلجة الاحتياطية على الجهاز نشطة 📱"
                                else "تنبيه تشخيص الاتصال ⚠️"

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBg.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, statusBg.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDiagnosticsLogs = !showDiagnosticsLogs }
                        .testTag("gemini_diagnostics_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(statusBg, CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = badgeText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showDiagnosticsLogs) "إخفاء السجلات 🔼" else "عرض التشخيص 🔽",
                                    fontSize = 11.sp,
                                    color = statusBg,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!result.failureReason.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "ملاحظة: ${result.failureReason}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = showDiagnosticsLogs) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Divider(color = statusBg.copy(alpha = 0.2f))
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الزمن: ${result.extractionLatencyMs} ms | الحمولة: ${result.requestPayloadBytes / 1024} KB",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            val report = buildString {
                                                appendLine("=== تقرير تشخيص Gemini API ===")
                                                appendLine("الحالة: $badgeText")
                                                appendLine("كود الاستجابة: ${result.httpStatusCode}")
                                                appendLine("زمن الاتصال: ${result.extractionLatencyMs} ms")
                                                appendLine("المقاطع: ${result.totalSpokenSegments}")
                                                appendLine("--- سجلات التتبع ---")
                                                result.diagnosticLogs.forEach { log ->
                                                    appendLine("[${log.formattedTime}] [${log.level}] ${log.title} -> ${log.details}")
                                                }
                                            }
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Diagnostics", report))
                                            Toast.makeText(context, "تم نسخ تقرير التشخيص للحافظة 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("نسخ السجل 📋", fontSize = 11.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                val logsToShow = if (result.diagnosticLogs.isNotEmpty()) result.diagnosticLogs else emptyList()
                                if (logsToShow.isEmpty()) {
                                    Text("لا توجد سجلات تتبع إضافية مسجلة لهذه العملية.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        logsToShow.forEach { logItem ->
                                            val logColor = when (logItem.level) {
                                                "ERROR" -> Color(0xFFEF4444)
                                                "WARN" -> Color(0xFFF59E0B)
                                                "SUCCESS" -> Color(0xFF10B981)
                                                else -> Color(0xFF3B82F6)
                                            }
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text(
                                                    text = logItem.formattedTime,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "[${logItem.level}]",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = logColor,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = logItem.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (logItem.details.isNotBlank()) {
                                                        Text(
                                                            text = logItem.details,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }
            }

            // Segments Results Area
            if (sttState.segments.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                // Search & Filter Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = sttState.searchQuery,
                        onValueChange = { viewModel.setSpeechToTextSearchQuery(it) },
                        placeholder = { Text("بحث في الكلمات المنطوقة...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (sttState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSpeechToTextSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح البحث", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("stt_search_field")
                    )

                    // Add Manual Segment Button
                    IconButton(
                        onClick = { showAddManualDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .testTag("add_manual_segment_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة توقيت يدوي", tint = Color(0xFF10B981))
                    }

                    // Export SRT/VTT Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .testTag("export_subtitles_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "تصدير ملف ترجمة", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Copy All with Timestamps
                    IconButton(
                        onClick = {
                            val text = viewModel.getFormattedTranscriptText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Transcribed Video Speech", text))
                            Toast.makeText(context, "تم نسخ نصوص وتوقيتات الفيديو للحافظة 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("copy_all_stt_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الكل بالتوقيتات", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Speaker Filter Chips
                if (uniqueSpeakers.size > 1) {
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 44.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = sttState.speakerFilter == null,
                                    onClick = { viewModel.setSpeechToTextSpeakerFilter(null) },
                                    label = { Text("جميع المتحدثين (${sttState.segments.size})", fontSize = 11.sp) }
                                )
                                uniqueSpeakers.forEach { speaker ->
                                    val isSelected = sttState.speakerFilter == speaker
                                    val count = sttState.segments.count { it.speaker == speaker }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setSpeechToTextSpeakerFilter(if (isSelected) null else speaker)
                                        },
                                        label = { Text("$speaker ($count)", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Action Bar: Apply All to Studio Timeline
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
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
                            Text("🎬", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تطبيق على خط زمن الاستوديو",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "استيراد كل المقاطع بدقة أجزاء الثانية للدبلجة",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.applySpeechSegmentsToStudioTimeline(replaceExisting = false)
                                Toast.makeText(context, "تم إدراج ${sttState.segments.size} مقطع في جدول الدبلجة! 🎬", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("apply_all_segments_to_studio_button")
                        ) {
                            Text("إدراج بالاستوديو ➕", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Transcribed Speech Segments List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredSegments.forEachIndexed { index, segment ->
                        val isVideoPlayingThisSegment = studioUiState.currentPlaybackSeconds >= segment.startSeconds &&
                                studioUiState.currentPlaybackSeconds <= segment.endSeconds

                        TranscribedSegmentCard(
                            segment = segment,
                            index = index + 1,
                            isActiveInVideo = isVideoPlayingThisSegment,
                            isPlayingAudio = sttState.currentlyPlayingSegmentId == segment.id,
                            onSeekToSegment = {
                                viewModel.seekToSpeechSegment(segment)
                                onSeekToVideoSeconds?.invoke(segment.startSeconds)
                            },
                            onPlaySpeechAudio = { viewModel.playSpeechSegmentAudio(segment) },
                            onEdit = { editingSegment = segment },
                            onDelete = { viewModel.deleteSpeechSegment(segment.id) },
                            onAddToStudio = {
                                viewModel.addSingleSpeechSegmentToStudio(segment)
                                Toast.makeText(context, "تمت إضافة المقطع إلى خط زمن الاستوديو 🎬", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Edit Speech Segment & Timing
    editingSegment?.let { segment ->
        EditSpeechSegmentDialog(
            segment = segment,
            onDismiss = { editingSegment = null },
            onSave = { updated ->
                viewModel.updateSpeechSegment(updated)
                editingSegment = null
                Toast.makeText(context, "تم تحديث النص والتوقيت بنجاح ⏱️", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Add Manual Speech Segment
    if (showAddManualDialog) {
        AddManualSpeechSegmentDialog(
            currentVideoSeconds = studioUiState.currentPlaybackSeconds,
            onDismiss = { showAddManualDialog = false },
            onAdd = { start, end, speaker, gender, original, arabic ->
                viewModel.addManualSpeechSegment(
                    startSec = start,
                    endSec = end,
                    speaker = speaker,
                    gender = gender,
                    originalText = original,
                    arabicText = arabic
                )
                showAddManualDialog = false
                Toast.makeText(context, "تمت إضافة المقطع الزمني بنجاح ➕", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Export Subtitles (SRT / VTT)
    if (showExportDialog) {
        ExportSubtitlesDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false }
        )
    }
}

/**
 * 🎴 TranscribedSegmentCard
 * بطاقة عرض مقطع الكلام المنطوق مع توقيته الزمني بدقة متناهية.
 */
@Composable
fun TranscribedSegmentCard(
    segment: VideoAudioTranscriptionSegment,
    index: Int,
    isActiveInVideo: Boolean,
    isPlayingAudio: Boolean,
    onSeekToSegment: () -> Unit,
    onPlaySpeechAudio: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToStudio: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActiveInVideo) Color(0xFF10B981) else Color(0xFF6366F1).copy(alpha = 0.25f),
        animationSpec = tween(300),
        label = "segment_border_color"
    )

    val cardBg = if (isActiveInVideo) {
        Color(0xFF10B981).copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(if (isActiveInVideo) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSeekToSegment() }
            .testTag("segment_card_${segment.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Bar: Index + Precise Time Range + Duration + Speaker Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Index badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$index",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Exact Video Timestamp Pill (Start ➔ End)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isActiveInVideo) Color(0xFF10B981) else Color(0xFF6366F1),
                        modifier = Modifier
                            .clickable { onSeekToSegment() }
                            .testTag("segment_seek_pill_${segment.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "انتقال للتوقيت",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = segment.formattedTimeRange,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    // Duration pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fث", segment.durationSeconds),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Speaker Identification Tag
                val genderEmoji = when (segment.speakerGender) {
                    "FEMALE" -> "👩"
                    "CHILD" -> "🧒"
                    "NARRATOR" -> "🎙️"
                    else -> "👨"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$genderEmoji ${segment.speaker}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Live Playing Indicator
            if (isActiveInVideo) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "يُعرض الآن في شاشة الفيديو 🔴",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Original Spoken Speech
            if (segment.originalSpeech.isNotBlank()) {
                Text(
                    text = "💬 المنطوق الأصلي: ${segment.originalSpeech}",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            // Arabic Dubbed Translation / Adaptation
            if (segment.arabicDubbedAdaptation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "🌐 الدبلجة العربية: ${segment.arabicDubbedAdaptation}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Segment Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek & Play Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onSeekToSegment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("انتقال للفيديو", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalIconButton(
                        onClick = onPlaySpeechAudio,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = "استماع للنطق",
                            tint = if (isPlayingAudio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Edit, Add to studio, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onAddToStudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "إضافة لخط زمن الاستوديو",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل النص والتوقيت",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف المقطع",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ✏️ EditSpeechSegmentDialog
 * نافذة حوار لتعديل نص المقطع المنطوق وتوقيت البداية والنهاية واسم المتحدث.
 */
@Composable
fun EditSpeechSegmentDialog(
    segment: VideoAudioTranscriptionSegment,
    onDismiss: () -> Unit,
    onSave: (VideoAudioTranscriptionSegment) -> Unit
) {
    var originalText by remember { mutableStateOf(segment.originalSpeech) }
    var arabicText by remember { mutableStateOf(segment.arabicDubbedAdaptation) }
    var speakerName by remember { mutableStateOf(segment.speaker) }
    var startSecondsText by remember { mutableStateOf(String.format(Locale.US, "%.2f", segment.startSeconds)) }
    var endSecondsText by remember { mutableStateOf(String.format(Locale.US, "%.2f", segment.endSeconds)) }
    var speakerGender by remember { mutableStateOf(segment.speakerGender) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("تعديل النص وتوقيت الكلام", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(14.dp))

                // Time Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startSecondsText,
                        onValueChange = { startSecondsText = it },
                        label = { Text("بداية الكلام (ثوانٍ)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endSecondsText,
                        onValueChange = { endSecondsText = it },
                        label = { Text("نهاية الكلام (ثوانٍ)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Speaker Name & Gender
                OutlinedTextField(
                    value = speakerName,
                    onValueChange = { speakerName = it },
                    label = { Text("اسم المتحدث", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // Original Spoken Text
                OutlinedTextField(
                    value = originalText,
                    onValueChange = { originalText = it },
                    label = { Text("النص المنطوق الأصلي", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp, max = 110.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Arabic Dubbed Translation
                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text("الدبلجة العربية", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp, max = 110.dp)
                )

                Spacer(Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val start = startSecondsText.toFloatOrNull() ?: segment.startSeconds
                            val end = endSecondsText.toFloatOrNull() ?: segment.endSeconds
                            val updated = segment.copy(
                                startSeconds = start.coerceAtLeast(0f),
                                endSeconds = maxOf(start + 0.5f, end),
                                speaker = speakerName.trim().ifEmpty { "المتحدث" },
                                speakerGender = speakerGender,
                                originalSpeech = originalText.trim(),
                                arabicDubbedAdaptation = arabicText.trim(),
                                isEditedByUser = true
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ التعديلات")
                    }
                }
            }
        }
    }
}

/**
 * ➕ AddManualSpeechSegmentDialog
 * نافذة حوار لإدراج مقطع كلامي وتوقيت مخصص يدوياً.
 */
@Composable
fun AddManualSpeechSegmentDialog(
    currentVideoSeconds: Float,
    onDismiss: () -> Unit,
    onAdd: (start: Float, end: Float, speaker: String, gender: String, original: String, arabic: String) -> Unit
) {
    var startSecondsText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentVideoSeconds)) }
    var endSecondsText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentVideoSeconds + 3.0f)) }
    var speakerName by remember { mutableStateOf("المتحدث 1") }
    var speakerGender by remember { mutableStateOf("MALE") }
    var originalText by remember { mutableStateOf("") }
    var arabicText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(Modifier.width(8.dp))
                    Text("إضافة توقيت كلام يدوي للفيديو", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startSecondsText,
                        onValueChange = { startSecondsText = it },
                        label = { Text("البداية (ث)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endSecondsText,
                        onValueChange = { endSecondsText = it },
                        label = { Text("النهاية (ث)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = speakerName,
                    onValueChange = { speakerName = it },
                    label = { Text("اسم المتحدث", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = originalText,
                    onValueChange = { originalText = it },
                    label = { Text("النص المنطوق الأصلي", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = arabicText,
                    onValueChange = { arabicText = it },
                    label = { Text("الدبلجة العربية", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val start = startSecondsText.toFloatOrNull() ?: currentVideoSeconds
                            val end = endSecondsText.toFloatOrNull() ?: (start + 3.0f)
                            onAdd(
                                start.coerceAtLeast(0f),
                                maxOf(start + 0.5f, end),
                                speakerName.trim().ifEmpty { "المتحدث" },
                                speakerGender,
                                originalText.trim(),
                                arabicText.trim().ifEmpty { originalText.trim() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إضافة المقطع")
                    }
                }
            }
        }
    }
}

/**
 * 💾 ExportSubtitlesDialog
 * نافذة حوار لتصدير نصوص الكلام والتوقيتات بصيغة ملفات الترجمة القياسية SRT أو WebVTT.
 */
@Composable
fun ExportSubtitlesDialog(
    viewModel: DubbingViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("تصدير ملف الترجمة والتوقيتات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "اختر صيغة الملف لحفظ الترجمة المتزامنة مع الفيديو مباشرة:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Option 1: SRT Subtitle File
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val file = viewModel.exportTranscriptsAsSrtFile()
                            if (file != null) {
                                Toast.makeText(context, "تم تصدير ملف SRT بنجاح:\n${file.name} 📄", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SRT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ملف ترجمة قياسي SubRip (.srt)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("متوافق مع يوتيوب، بريمير، فيس بوك، ومشغلات الفيديو", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Option 2: WebVTT File
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val file = viewModel.exportTranscriptsAsVttFile()
                            if (file != null) {
                                Toast.makeText(context, "تم تصدير ملف VTT بنجاح:\n${file.name} 🌐", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6366F1).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("VTT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF6366F1))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ملف ترجمة الويب WebVTT (.vtt)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("متوافق مع متصفحات الويب والمنصات الحديثة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}
