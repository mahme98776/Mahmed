package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DubbingProject
import com.example.export.AudioQualityPreset
import com.example.export.ExportFormat
import com.example.export.ExportResolution
import com.example.export.ExportResult
import com.example.export.FrameRatePreset
import com.example.export.VideoBitratePreset
import com.example.export.VideoExportConfig
import com.example.model.DubbingClip

data class ExportDialogUiState(
    val isVisible: Boolean = false,
    val project: DubbingProject? = null,
    val clip: DubbingClip? = null,
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val successResult: ExportResult.Success? = null,
    val errorMessage: String? = null,
    val initialFormat: ExportFormat = ExportFormat.MP4_VIDEO
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportProjectDialog(
    state: ExportDialogUiState,
    initialVideoConfig: VideoExportConfig = VideoExportConfig(),
    onDismiss: () -> Unit,
    onStartExport: (ExportFormat, String, VideoExportConfig) -> Unit,
    onOpenFile: (ExportResult.Success) -> Unit,
    onShareFile: (ExportResult.Success) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val defaultTitle = remember(state.project, state.clip) {
        state.project?.title ?: state.clip?.let { "دبلجة_${it.title}" } ?: "مشروع_دبلجة"
    }
    var fileNameInput by remember(defaultTitle) { mutableStateOf(defaultTitle) }
    var selectedFormat by remember(state.initialFormat) { mutableStateOf(state.initialFormat) }
    var videoConfig by remember { mutableStateOf(initialVideoConfig) }
    var showAdvancedQualitySettings by remember { mutableStateOf(true) }

    val clipDuration = state.clip?.durationSeconds ?: state.project?.durationSeconds ?: 10
    val estimatedSizeMb = remember(videoConfig, clipDuration) {
        videoConfig.calculateEstimatedSizeMb(clipDuration)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "export_spin")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AlertDialog(
        onDismissRequest = {
            if (!state.isExporting) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (state.successResult != null) Color(0xFFA6D4A8) else Color(0xFFD0BCFF),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                state.successResult != null -> Icons.Default.CheckCircle
                                state.errorMessage != null -> Icons.Default.ErrorOutline
                                else -> Icons.Default.FileDownload
                            },
                            contentDescription = null,
                            tint = if (state.successResult != null) Color(0xFF1B5E20) else Color(0xFF381E72),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = when {
                            state.successResult != null -> "اكتمل التصدير بنجاح! 🎉"
                            state.isExporting -> "جاري تصدير ومعالجة الملف..."
                            state.errorMessage != null -> "حدث خطأ أثناء التصدير"
                            else -> "تصدير وضبط جودة الفيديو 🎬"
                        },
                        color = Color(0xFFE6E1E5),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.clip?.title ?: "مشروع الدبلجة",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    // Success View
                    state.successResult != null -> {
                        val result = state.successResult
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                            border = BorderStroke(1.dp, Color(0xFFA6D4A8).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (result.format == ExportFormat.MP4_VIDEO) Color(0xFFD0BCFF) else Color(0xFFFFD993),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (result.format == ExportFormat.MP4_VIDEO) Icons.Default.Movie else Icons.Default.Audiotrack,
                                                contentDescription = null,
                                                tint = Color(0xFF381E72),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = result.fileName,
                                            color = Color(0xFFE6E1E5),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (result.format == ExportFormat.MP4_VIDEO)
                                                "فيديو MP4 بدقة ${videoConfig.resolution.badge} (${videoConfig.getBitrateMbpsFormatted()})"
                                            else if (result.fileName.endsWith(".wav", ignoreCase = true))
                                                "مسار صوتي متزامن WAV Master (44.1kHz)"
                                            else "مسار صوتي مدبلج MP3 عالي النقاء",
                                            color = Color(0xFFA6D4A8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(2.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2B2930),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = Color(0xFFCAC4D0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "المسار: ${result.filePath}",
                                            color = Color(0xFFCAC4D0),
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = if (result.format == ExportFormat.MP4_VIDEO)
                                        "الفيديو متاح الآن في مجلد الأفلام (Movies/VoiceMasterPro) ومعرض الهاتف."
                                    else
                                        "المسار الصوتي متاح الآن في مجلد الموسيقى (Music/VoiceMasterPro) وتطبيقات الصوت.",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Exporting In-Progress View
                    state.isExporting -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(pulseScale)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF8B5CF6).copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF381E72),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (selectedFormat == ExportFormat.MP4_VIDEO) Icons.Default.Movie else Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (selectedFormat == ExportFormat.MP4_VIDEO)
                                        "جاري ترميز ومعالجة فيديو ${videoConfig.resolution.badge}..."
                                    else "جاري تصدير الصوت الممزوج...",
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "يرجى الانتظار، يتم استخدام مسرع العتاد MediaCodec",
                                    color = Color(0xFF938F99),
                                    fontSize = 11.sp
                                )
                            }

                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFD0BCFF),
                                trackColor = Color(0xFF49454F),
                                strokeCap = StrokeCap.Round
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state.statusMessage,
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(state.progress * 100).toInt()}%",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Error View
                    state.errorMessage != null -> {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1E22)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFF2B8B5)
                                )
                                Text(
                                    text = state.errorMessage,
                                    color = Color(0xFFF2B8B5),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Options & Quality Configuration View
                    else -> {
                        Text(
                            text = "اختر صيغة الملف:",
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Format selection cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // MP4 Video Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFormat = ExportFormat.MP4_VIDEO }
                                    .testTag("export_format_mp4"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedFormat == ExportFormat.MP4_VIDEO) Color(0xFF381E72).copy(alpha = 0.5f) else Color(0xFF1E1B24)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selectedFormat == ExportFormat.MP4_VIDEO) Color(0xFFD0BCFF) else Color(0xFF49454F)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = if (selectedFormat == ExportFormat.MP4_VIDEO) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFD993)
                                        ) {
                                            Text(
                                                text = videoConfig.resolution.badge,
                                                color = Color(0xFF4A2800),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "فيديو MP4 🎬",
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "مشهد كامل مع الترجمة وموجات الصوت",
                                        color = Color(0xFF938F99),
                                        fontSize = 9.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            // MP3 Audio Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFormat = ExportFormat.MP3_AUDIO }
                                    .testTag("export_format_mp3"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedFormat == ExportFormat.MP3_AUDIO) Color(0xFF381E72).copy(alpha = 0.5f) else Color(0xFF1E1B24)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (selectedFormat == ExportFormat.MP3_AUDIO) Color(0xFFD0BCFF) else Color(0xFF49454F)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = if (selectedFormat == ExportFormat.MP3_AUDIO) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFA6D4A8)
                                        ) {
                                            Text(
                                                text = "صوت نقي",
                                                color = Color(0xFF1B5E20),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "مسار صوتي متزامن 🎵",
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "حفظ كملف صوتي مستقل في الجهاز (Music)",
                                        color = Color(0xFF938F99),
                                        fontSize = 9.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }

                        // Video Quality & Configuration Panel (Active when MP4 is selected)
                        AnimatedVisibility(
                            visible = selectedFormat == ExportFormat.MP4_VIDEO,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("video_quality_config_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D26)),
                                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Header with toggle
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showAdvancedQualitySettings = !showAdvancedQualitySettings },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = null,
                                                tint = Color(0xFF818CF8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "جودة ودقة الفيديو (Resolution / Bitrate)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE6E1E5)
                                            )
                                        }

                                        Icon(
                                            imageVector = if (showAdvancedQualitySettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFFCAC4D0)
                                        )
                                    }

                                    if (showAdvancedQualitySettings) {
                                        Spacer(Modifier.height(10.dp))

                                        // 1. Resolution Selection Chips
                                        Text(
                                            text = "1. دقة أبعاد الفيديو (Resolution):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFCAC4D0)
                                        )
                                        Spacer(Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ExportResolution.values().forEach { res ->
                                                val isSelected = videoConfig.resolution == res
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            videoConfig = videoConfig.copy(resolution = res)
                                                        }
                                                        .testTag("resolution_chip_${res.name.lowercase()}"),
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) Color(0xFF4338CA) else Color(0xFF282531),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) Color(0xFFA5B4FC) else Color(0xFF3F3B4B)
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = res.badge,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else Color(0xFFE6E1E5)
                                                        )
                                                        Text(
                                                            text = "${res.width}x${res.height}",
                                                            fontSize = 8.sp,
                                                            color = if (isSelected) Color(0xFFE0E7FF) else Color(0xFF938F99)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // 2. Bitrate Preset Selection
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "2. معدل البت والضغط (Bitrate):",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFCAC4D0)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF6366F1).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "⚡ ${videoConfig.getBitrateMbpsFormatted()}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFA5B4FC),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            VideoBitratePreset.values().forEach { preset ->
                                                val isSelected = videoConfig.bitratePreset == preset
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            videoConfig = videoConfig.copy(bitratePreset = preset)
                                                        }
                                                        .testTag("bitrate_chip_${preset.name.lowercase()}"),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) Color(0xFF312E81) else Color(0xFF282531),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) Color(0xFF818CF8) else Color(0xFF3F3B4B)
                                                    )
                                                ) {
                                                    Text(
                                                        text = preset.labelArabic,
                                                        fontSize = 9.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else Color(0xFFCAC4D0),
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // 3. Framerate & Audio Bitrate Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // FrameRate
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "3. سلاسة الإطارات (FPS):",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFCAC4D0)
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    FrameRatePreset.values().forEach { fps ->
                                                        val isSelected = videoConfig.frameRatePreset == fps
                                                        Surface(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable {
                                                                    videoConfig = videoConfig.copy(frameRatePreset = fps)
                                                                }
                                                                .testTag("fps_chip_${fps.fps}"),
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF282531),
                                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFA5B4FC) else Color(0xFF3F3B4B))
                                                        ) {
                                                            Text(
                                                                text = "${fps.fps} FPS",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Color.White else Color(0xFFCAC4D0),
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Audio Quality
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "4. جودة الصوت المدمج:",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFCAC4D0)
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    AudioQualityPreset.values().forEach { aud ->
                                                        val isSelected = videoConfig.audioQuality == aud
                                                        Surface(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable {
                                                                    videoConfig = videoConfig.copy(audioQuality = aud)
                                                                }
                                                                .testTag("audio_quality_chip_${aud.bitrateKbps}"),
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF282531),
                                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFA5B4FC) else Color(0xFF3F3B4B))
                                                        ) {
                                                            Text(
                                                                text = "${aud.bitrateKbps}k",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Color.White else Color(0xFFCAC4D0),
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // 4. Video Toggles (Subtitles & Waveforms)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Subtitles,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD993),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "حرق الترجمة التلقائية بالفيديو",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFE6E1E5)
                                                )
                                            }
                                            Switch(
                                                checked = videoConfig.burnSubtitles,
                                                onCheckedChange = { videoConfig = videoConfig.copy(burnSubtitles = it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF381E72),
                                                    checkedTrackColor = Color(0xFFD0BCFF)
                                                ),
                                                modifier = Modifier
                                                    .scale(0.8f)
                                                    .testTag("burn_subtitles_switch")
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD0BCFF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "إظهار موجات الصوت المتحركة",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFE6E1E5)
                                                )
                                            }
                                            Switch(
                                                checked = videoConfig.showWaveform,
                                                onCheckedChange = { videoConfig = videoConfig.copy(showWaveform = it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF381E72),
                                                    checkedTrackColor = Color(0xFFD0BCFF)
                                                ),
                                                modifier = Modifier
                                                    .scale(0.8f)
                                                    .testTag("show_waveform_switch")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Estimated File Size & Summary Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF282531),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("estimated_size_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "📊", fontSize = 14.sp)
                                    Column {
                                        Text(
                                            text = "الحجم التقديري للملف:",
                                            fontSize = 10.sp,
                                            color = Color(0xFFCAC4D0)
                                        )
                                        Text(
                                            text = if (selectedFormat == ExportFormat.MP4_VIDEO)
                                                "~${estimatedSizeMb} MB (${clipDuration} ثانية)"
                                            else
                                                "~${String.format(java.util.Locale.US, "%.1f", clipDuration * 0.024f)} MB (${clipDuration} ثانية)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD993)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF381E72)
                                ) {
                                    Text(
                                        text = if (selectedFormat == ExportFormat.MP4_VIDEO)
                                            "${videoConfig.resolution.badge} • ${videoConfig.frameRatePreset.fps}fps"
                                        else "MP3 Audio",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // File name input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "اسم الملف المُصدّر:",
                                color = Color(0xFFCAC4D0),
                                fontSize = 11.sp
                            )
                            OutlinedTextField(
                                value = fileNameInput,
                                onValueChange = { fileNameInput = it },
                                placeholder = { Text("أدخل اسم الملف...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color(0xFF49454F),
                                    focusedTextColor = Color(0xFFE6E1E5),
                                    unfocusedTextColor = Color(0xFFE6E1E5)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_filename_input")
                            )
                        }

                        // Destination info badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1B24),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD993),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedFormat == ExportFormat.MP4_VIDEO)
                                        "سيتم الحفظ في: Movies/DubbingStudio على ذاكرة الجهاز"
                                    else
                                        "سيتم الحفظ في: Music/DubbingStudio على ذاكرة الجهاز",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                // Success: Open & Share Buttons
                state.successResult != null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onShareFile(state.successResult) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF)),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("مشاركة", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onOpenFile(state.successResult) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("فتح الملف", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // Error: Retry
                state.errorMessage != null -> {
                    Button(
                        onClick = { onStartExport(selectedFormat, fileNameInput, videoConfig) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إعادة المحاولة", fontWeight = FontWeight.Bold)
                    }
                }

                // Normal / Idle
                !state.isExporting -> {
                    Button(
                        onClick = {
                            if (fileNameInput.isNotBlank()) {
                                onStartExport(selectedFormat, fileNameInput, videoConfig)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_start_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (selectedFormat == ExportFormat.MP4_VIDEO)
                                "تصدير فيديو ${videoConfig.resolution.badge}"
                            else "تصدير مسار الصوت المتزامن 🎵",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!state.isExporting) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (state.successResult != null) "إغلاق" else "إلغاء",
                        color = Color(0xFFCAC4D0)
                    )
                }
            }
        },
        containerColor = Color(0xFF24222B),
        shape = RoundedCornerShape(24.dp)
    )
}
