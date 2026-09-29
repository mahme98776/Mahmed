package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsGenerator
import com.example.audio.AutoDubbingStep
import com.example.audio.AutoDubbingStyle
import com.example.audio.DubbingDialect
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.model.DubbingClip
import com.example.model.SampleClipsRepository
import com.example.model.ScriptLine
import com.example.audio.tts.CloudTtsProvider
import com.example.audio.tts.CloudVoiceCatalog
import androidx.compose.material.icons.filled.GraphicEq
import com.example.ui.components.CloudTtsConfigDialog
import com.example.ui.components.AutoDubVoiceProfileSelector
import com.example.ui.components.ExportProjectDialog
import com.example.ui.components.ShareDubbingOptionsDialog
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import com.example.ui.DubbingViewModel
import com.example.ui.SplitCompareViewMode
import com.example.ui.components.IntegratedVideoPlayerComponent
import com.example.ui.components.SplitScreenPreviewCompareComponent
import com.example.ui.components.VideoCanvasPlayer
import com.example.ui.components.HollywoodProductionSuiteSection
import com.example.ui.components.VolumeMixerCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoAutoDubberScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToYouTubeDub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val autoDubState by viewModel.autoDubberState.collectAsState()
    val longFormWorkerState by viewModel.longFormWorkerState.collectAsState()
    val studioUiState by viewModel.uiState.collectAsState()
    val exportDialogState by viewModel.exportDialogState.collectAsState()
    val videoExportConfig by viewModel.videoExportConfig.collectAsState()
    var originalDuckLevel by remember { mutableStateOf(0.25f) }
    var dubVoiceBoost by remember { mutableStateOf(1.20f) }
    var isOriginalMuted by remember { mutableStateOf(false) }
    var isDubMuted by remember { mutableStateOf(false) }
    var isSoloOriginal by remember { mutableStateOf(false) }
    var isSoloDub by remember { mutableStateOf(false) }
    var isVocalClarityActive by remember { mutableStateOf(false) }
    var showEditLineModalIndex by remember { mutableStateOf<Int?>(null) }
    var showPythonCodeModal by remember { mutableStateOf(false) }
    var showCloudTtsDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showVattSubtitlesDialog by remember { mutableStateOf(false) }

    val vattState by viewModel.vattState.collectAsState()
    val cloudTtsConfig by viewModel.cloudTtsConfig.collectAsState()
    val currentlyPreviewingVoiceId by viewModel.currentlyPreviewingVoiceId.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    // Video File Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "imported_video_${System.currentTimeMillis()}.mp4"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIdx)
                    }
                }
            } catch (_: Exception) {}

            viewModel.importVideoForAutoDubbing(uri, fileName)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Single Clip Auto-Dubber View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Header Banner & Easy-Mode Quick Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                    Color(0xFF3B82F6).copy(alpha = 0.20f),
                                    Color(0xFF10B981).copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎬", fontSize = 26.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مدبلج الفيديو الذكي",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "سهل وسريع ⚡",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "دبلجة ودمج صوتي تلقائي بـ 3 خطوات سهلة وسريعة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // 3-Step Simple Guide Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val steps = listOf(
                            Triple("1️⃣", "اختر الفيديو", autoDubState.importedVideo != null),
                            Triple("2️⃣", "حدد اللهجة/الصوت", true),
                            Triple("3️⃣", "دبلجة فورية!", autoDubState.isProcessing)
                        )
                        steps.forEach { (num, text, active) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                border = BorderStroke(
                                    1.dp,
                                    if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(num, fontSize = 11.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = text,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1.5. YouTube Auto-Dubbing Quick Banner (Inspired by Mikk0git/youtube-auto-dubbing)
        item {
            Card(
                onClick = { onNavigateToYouTubeDub() },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.45f)),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF0000).copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF0000),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "دبلجة يوتيوب الآلية (YouTube Auto-Dubbing) 🔴",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "جلب ترجمات يوتيوب تلقائياً، ترجمتها، وتوليد أصوات متوافقة مع الزمن وسرعة الكلام",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "فتح",
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Hollywood Studio Autonomous Master Suite
        item {
            HollywoodProductionSuiteSection(viewModel = viewModel)
        }

        // 2. Video Import Area / File Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.5.dp,
                            if (autoDubState.importedVideo != null) Color(0xFF10B981) else Color(0xFF8B5CF6).copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (autoDubState.importedVideo != null)
                        Color(0xFF10B981).copy(alpha = 0.06f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "1. استيراد الفيديو من الجهاز",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (autoDubState.importedVideo != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "تم الاستيراد",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (autoDubState.importedVideo != null) {
                        val video = autoDubState.importedVideo!!
                        // Video Info Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "⏱️ ${video.formattedDuration}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "📐 ${video.width}x${video.height}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { videoPickerLauncher.launch("video/*") },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("change_video_button")
                                        ) {
                                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("تغيير الفيديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (video.isLongVideo) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF8B5CF6),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "⚡ فيديو طويل (${video.formattedDuration}) • سيتم تقسيمه ودبلجته تلقائياً بالكامل بدون انقطاع",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8B5CF6)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty Drop-zone
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { videoPickerLauncher.launch("video/*") }
                                .padding(vertical = 26.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "اضغط لاختيار فيديو من جهازك",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "يدعم مقاطع MP4, MKV, MOV, WebM وغيرها",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Quick Demo Presets
                    Text(
                        text = "أو اختر مشهداً جاهزاً للتجربة الفورية السريعة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(SampleClipsRepository.clips) { sampleClip ->
                            val isSelected = autoDubState.importedVideo?.title == sampleClip.title
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .clickable { viewModel.loadSampleVideoForAutoDubbing(sampleClip) }
                                    .testTag("sample_video_${sampleClip.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(sampleClip.coverEmoji, fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = sampleClip.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        val durationText = when {
                                            sampleClip.durationSeconds >= 3600 -> "${sampleClip.durationSeconds / 3600} ساعة (${sampleClip.durationSeconds / 60} دقيقة)"
                                            sampleClip.durationSeconds >= 60 -> "${sampleClip.durationSeconds / 60} دقيقة (${sampleClip.durationSeconds} ثانية)"
                                            else -> "${sampleClip.durationSeconds} ثانية"
                                        }
                                        Text(
                                            text = "$durationText • ${sampleClip.category}",
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

        // 3. Target Language Selection Dropdown Menu
        item {
            var languageDropdownExpanded by remember { mutableStateOf(false) }
            val currentLang = autoDubState.selectedLanguage

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_selection_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "2. تحديد لغة الدبلجة المستهدفة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${currentLang.flagEmoji} ${currentLang.nativeName}",
                                color = Color(0xFF3B82F6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "اختر اللغة التي تريد للذكاء الاصطناعي ترجمة وتوليد أصوات الدبلجة إليها تلقائياً:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    // Language Selector Dropdown Menu Trigger
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.5.dp,
                                if (languageDropdownExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { languageDropdownExpanded = !languageDropdownExpanded }
                                .testTag("language_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = currentLang.flagEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${currentLang.displayNameArabic} (${currentLang.nativeName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "رمز اللغة: ${currentLang.code.uppercase()} • نطق محلي دقيق",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (languageDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (languageDropdownExpanded) "إغلاق القائمة" else "فتح قائمة اللغات",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Dropdown Menu Items
                        DropdownMenu(
                            expanded = languageDropdownExpanded,
                            onDismissRequest = { languageDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.90f)
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("language_dropdown_menu")
                        ) {
                            DubbingTargetLanguage.values().forEach { languageOption ->
                                val isSelected = languageOption == currentLang
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(languageOption.flagEmoji, fontSize = 20.sp)
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = languageOption.displayNameArabic,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = languageOption.nativeName,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setAutoDubbingLanguage(languageOption)
                                        languageDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("lang_item_${languageOption.name}")
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Quick Chips for popular languages
                    Text(
                        text = "أو اختر لغة بنقرة واحدة سريعة:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(DubbingTargetLanguage.values()) { lang ->
                            val isSelected = lang == currentLang
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .clickable { viewModel.setAutoDubbingLanguage(lang) }
                                    .testTag("lang_chip_${lang.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(lang.flagEmoji, fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Dialect Selector for Arabic Dubbing
                    if (currentLang == DubbingTargetLanguage.ARABIC) {
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text("🗣️", fontSize = 16.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "لهجة ونبرة الأداء الصوتي:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${autoDubState.selectedDialect.flagEmoji} ${autoDubState.selectedDialect.displayNameArabic.split(" ")[0]}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(DubbingDialect.values()) { dialect ->
                                        val isSelected = autoDubState.selectedDialect == dialect
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier
                                                .clickable { viewModel.setAutoDubbingDialect(dialect) }
                                                .testTag("dialect_chip_${dialect.code}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(dialect.flagEmoji, fontSize = 13.sp)
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = dialect.displayNameArabic,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "💡 ${autoDubState.selectedDialect.descriptionArabic}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = "مثال منطوق: \"${autoDubState.selectedDialect.samplePhrase}\"",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Dubbing Style & Voice Genre Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "3. اختيار نمط ونبرة الدبلجة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoDubbingStyle.values().forEach { style ->
                            val isSelected = autoDubState.selectedStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setAutoDubbingStyle(style) },
                                label = {
                                    Text(
                                        text = "${style.emoji} ${style.titleArabic}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("style_chip_${style.name}")
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 النمط المختار: ${autoDubState.selectedStyle.descriptionArabic}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Dubbing Pacing / Interval Selection (Especially for long videos)
                    Text(
                        text = "⏱️ نمط وتوزيع الفواصل الزمنية (Pacing):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DubbingPacing.values().forEach { pacing ->
                            val isSelected = autoDubState.selectedPacing == pacing
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAutoDubbingPacing(pacing) }
                                    .testTag("pacing_${pacing.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = pacing.titleArabic,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${pacing.segmentIntervalSec.toInt()}ث لكل مقطع",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Dedicated Multi-Channel Audio Volume Mixer Card
                    VolumeMixerCard(
                        originalVolume = originalDuckLevel,
                        dubVoiceVolume = dubVoiceBoost,
                        bgmVolume = 0.25f,
                        isOriginalMuted = isOriginalMuted,
                        isDubMuted = isDubMuted,
                        isSoloOriginal = isSoloOriginal,
                        isSoloDub = isSoloDub,
                        isVocalClarityActive = isVocalClarityActive,
                        isPlaying = studioUiState.isPlaying,
                        onOriginalVolumeChange = {
                            originalDuckLevel = it
                            viewModel.setAutoDubDuckedVolumes(originalDuckLevel, dubVoiceBoost)
                        },
                        onDubVoiceVolumeChange = {
                            dubVoiceBoost = it
                            viewModel.setAutoDubDuckedVolumes(originalDuckLevel, dubVoiceBoost)
                        },
                        onToggleOriginalMute = {
                            isOriginalMuted = !isOriginalMuted
                        },
                        onToggleDubMute = {
                            isDubMuted = !isDubMuted
                        },
                        onToggleSoloOriginal = {
                            isSoloOriginal = !isSoloOriginal
                            if (isSoloOriginal) isSoloDub = false
                        },
                        onToggleSoloDub = {
                            isSoloDub = !isSoloDub
                            if (isSoloDub) isSoloOriginal = false
                        },
                        onToggleVocalClarity = {
                            isVocalClarityActive = !isVocalClarityActive
                        },
                        onTogglePlaybackPreview = {
                            viewModel.togglePlayPause()
                        },
                        onQuickAutoBalance = {
                            originalDuckLevel = 0.25f
                            dubVoiceBoost = 1.15f
                            viewModel.setAutoDubDuckedVolumes(originalDuckLevel, dubVoiceBoost)
                        },
                        onResetDefaults = {
                            originalDuckLevel = 0.25f
                            dubVoiceBoost = 1.15f
                            isOriginalMuted = false
                            isDubMuted = false
                            isSoloOriginal = false
                            isSoloDub = false
                            isVocalClarityActive = false
                            viewModel.setAutoDubDuckedVolumes(0.25f, 1.15f)
                        }
                    )
                }
            }
        }

        // SFX Soundboard & Auto-Save to Drafts Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("soundboard_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🔊", fontSize = 18.sp)
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "لوحة المؤثرات الصوتية والمكساج الحي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "مؤثرات صوتية حية فورية ومكساج احترافي",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "حفظ تلقائي للمسودة 💾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))


                    // Notice on Auto-Save & Gender Detection
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧬", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "التعرف الذكي على نبرات المتحدثين: يحدد جنس المتحدث آلياً (صوت رجالي طبيعي 👨 / صوت نسائي طبيعي 👩) ويحفظ النتيجة والمسودة في قاعدة بيانات الغرفة تلقائياً للمعاينة في أي وقت.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. AI-Generated Voice Profiles Preview & Selection (معاينة واختيار البصمات الصوتية للدبلجة)
        item {
            AutoDubVoiceProfileSelector(
                voiceProfiles = viewModel.ttsManager.voiceProfiles,
                selectedVoiceId = autoDubState.selectedVoiceProfileId,
                currentlyPreviewingId = currentlyPreviewingVoiceId,
                isSpeaking = isSpeaking,
                cloudTtsConfig = cloudTtsConfig,
                onSelectVoice = { profileId ->
                    viewModel.setSelectedAutoDubVoiceProfileId(profileId)
                },
                onPreviewVoice = { profile ->
                    viewModel.previewVoiceProfile(profile)
                },
                onStopPreview = {
                    viewModel.stopVoiceProfilePreview()
                },
                onOpenCloudTtsSettings = {
                    showCloudTtsDialog = true
                }
            )
        }

        // 5. Action Button: Start Auto Dubbing
        item {
            val isReady = autoDubState.importedVideo != null
            Button(
                onClick = { viewModel.startAutoVideoDubbing() },
                enabled = isReady && !autoDubState.isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(if (isReady) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                    .testTag("start_auto_dub_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                )
            ) {
                if (autoDubState.isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "جارٍ المعالجة والدبلجة (${(autoDubState.progressFraction * 100).toInt()}%)...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (autoDubState.importedVideo?.isLongVideo == true)
                            "🚀 بدء الدبلجة التلقائية للفيديو الطويل (${autoDubState.importedVideo?.formattedDuration})"
                        else
                            "🚀 بدء الدبلجة التلقائية الذكية (Auto Dub)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // VATT Engine (Video Audio Translation Tool) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .testTag("vatt_engine_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "VATT Engine",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "محرك VATT لدبلجة وترجمة الفيديو",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "VATT AI ⚡",
                                            color = Color(0xFF0284C7),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "استخراج الصوت • تفريغ ASR • ترجمة سياقية • ملفات SRT/VTT • مطابقة السرعة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Features of VATT Pipeline
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "مميزات محرك VATT (Video Audio Translation Tool):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "• استخراج الصوت النقي ومطابقة التوقيت بدقة الأجزاء من الثانية\n• إنشاء ملفات ترجمة قياسية SRT و WebVTT قابلة للتصدير والنسخ\n• ضبط سرعة نطق الدبلجة آلياً (Time-Stretch) لتلائم طول المشهد الأصلي\n• مكساج ذكي يخفض صوت الخلفية تلقائياً وقت الكلام (Audio Ducking)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // VATT Running progress
                    if (vattState.isRunning) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = vattState.currentStage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = "${(vattState.progress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { vattState.progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0284C7)
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    // VATT Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val currentVideo = autoDubState.importedVideo
                                val currentClip = studioUiState.currentClip
                                val videoUri = currentVideo?.uriString?.let { Uri.parse(it) }
                                    ?: currentClip.videoUri?.let { Uri.parse(it) }
                                    ?: Uri.parse("file://${context.cacheDir.absolutePath}/sample_demo.mp4")
                                val videoTitle = currentVideo?.title ?: currentClip.title
                                val duration = currentVideo?.durationSeconds ?: currentClip.durationSeconds
                                viewModel.executeVattAutoDubbing(videoUri, videoTitle, duration)
                            },
                            enabled = !vattState.isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("btn_run_vatt")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (vattState.isRunning) "جارٍ المعالجة بمحرك VATT..." else "تشغيل معالجة VATT ⚡",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (vattState.srtContent.isNotBlank() || vattState.lastResult != null) {
                            OutlinedButton(
                                onClick = { showVattSubtitlesDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                                modifier = Modifier.testTag("btn_view_vatt_subtitles")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ملفات SRT/VTT 📑", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            vattState.lastResult?.dubbedAudioPath?.let { audioPath ->
                                Button(
                                    onClick = {
                                        viewModel.saveDubbedAudioFileToStorage(
                                            audioPath = audioPath,
                                            suggestedTitle = "vatt_dubbed_audio_${System.currentTimeMillis() % 10000}"
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.testTag("btn_save_vatt_audio")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("حفظ الصوت 💾", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Comprehensive Visual Progress Bar & Tracking HUD
        if (autoDubState.isProcessing || autoDubState.currentStep == AutoDubbingStep.COMPLETED) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED)
                            Color(0xFF064E3B).copy(alpha = 0.12f)
                        else
                            Color(0xFF4C1D95).copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (autoDubState.currentStep == AutoDubbingStep.COMPLETED) Color(0xFF10B981) else Color(0xFF8B5CF6)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header Bar: Title and Percentage Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED) Color(0xFF10B981) else Color(0xFF8B5CF6),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (autoDubState.isProcessing) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED)
                                            "🎉 اكتملت دبلجة الفيديو بالكامل بنجاح!"
                                        else
                                            "⚡ شريط تقدم الدبلجة الحية المباشرة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (autoDubState.totalVideoSeconds > 0) {
                                        Text(
                                            text = "مدة الفيديو الكلية: ${autoDubState.importedVideo?.formattedDuration ?: "${autoDubState.totalVideoSeconds} ث"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Big Percentage Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED) Color(0xFF10B981) else Color(0xFF8B5CF6),
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "${(autoDubState.progressFraction * 100).toInt()}%",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Large Visual Progress Bar with Glow Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(autoDubState.progressFraction.coerceIn(0f, 1f))
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (autoDubState.currentStep == AutoDubbingStep.COMPLETED)
                                                listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
                                            else
                                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
                                        )
                                    )
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Real-time Metrics Grid (4 Key Indicators)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Processed Video Duration Metric
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⏱️ مدة المعالجة",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = autoDubState.formattedProgressTime,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // 2. Dubbed Audio Segments Counter
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🧩 المقاطع المنجزة",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${autoDubState.currentSegmentIndex} / ${autoDubState.totalSegments} مقطع",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            // 3. Estimated Time Remaining
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "⏳ المتبقي تقريباً",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED) "مكتمل ✓" else "~${autoDubState.estimatedSecondsRemaining} ثانية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (autoDubState.currentStep == AutoDubbingStep.COMPLETED) Color(0xFF10B981) else Color(0xFFD97706)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Multi-Stage Processing Stepper Timeline
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "مراحل الدبلجة التلقائية للفيديو:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val steps = listOf(
                                    Triple(1, "فحص وتقطيع الفواصل الزمنية للفيديو", autoDubState.progressFraction >= 0.12f),
                                    Triple(2, "توليد السيناريو والحوارات الفصيحة", autoDubState.progressFraction >= 0.30f),
                                    Triple(3, "توليد الأصوات بالذكاء الاصطناعي ومطابقة النبرات", autoDubState.progressFraction >= 0.90f),
                                    Triple(4, "المكساج التلقائي وخفض صوت الفيديو الأصلي (Ducking)", autoDubState.progressFraction >= 1.0f)
                                )

                                steps.forEach { (stepNum, title, isCompleted) ->
                                    val isCurrent = when (stepNum) {
                                        1 -> autoDubState.currentStep == AutoDubbingStep.ANALYZING_SCENES || autoDubState.currentStep == AutoDubbingStep.IMPORTING_VIDEO
                                        2 -> autoDubState.currentStep == AutoDubbingStep.GENERATING_SCRIPT
                                        3 -> autoDubState.currentStep == AutoDubbingStep.SYNTHESIZING_VOICE
                                        4 -> autoDubState.currentStep == AutoDubbingStep.BALANCING_MIX
                                        else -> false
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isCompleted -> Color(0xFF10B981)
                                                isCurrent -> Color(0xFF8B5CF6)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isCompleted) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                } else if (isCurrent) {
                                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                                                } else {
                                                    Text("$stepNum", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isCompleted -> Color(0xFF10B981)
                                                isCurrent -> Color(0xFF8B5CF6)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isCurrent) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "⚡ قيد التنفيذ",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF8B5CF6),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Real-time Status Message Bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 ${autoDubState.statusMessage}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        // Background Worker Status for Long Videos (up to 60+ minutes)
                        if (longFormWorkerState.isRunning || (autoDubState.importedVideo?.isLongVideo == true && autoDubState.isProcessing)) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                tint = Color(0xFF6366F1),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "⚙️ معالجة الأجزاء (Chunked Processing Pipeline)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6366F1)
                                            )
                                        }

                                        if (longFormWorkerState.totalChunksCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF6366F1).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "جزء ${longFormWorkerState.currentChunkIndex + 1} / ${longFormWorkerState.totalChunksCount}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6366F1),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "معالجة مجزأة ومقاومة لانقطاع الذاكرة للفيديوهات الطويلة (+60 دقيقة) بدقة مزامنة متناهية.",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (longFormWorkerState.activeChunks.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(longFormWorkerState.activeChunks) { chk ->
                                                val isCur = chk.chunkIndex == longFormWorkerState.currentChunkIndex && longFormWorkerState.isRunning
                                                val isDone = chk.isProcessed || chk.chunkIndex < longFormWorkerState.currentChunkIndex
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = when {
                                                        isDone -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                        isCur -> Color(0xFF8B5CF6).copy(alpha = 0.3f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    },
                                                    border = BorderStroke(
                                                        1.dp,
                                                        when {
                                                            isDone -> Color(0xFF10B981)
                                                            isCur -> Color(0xFF8B5CF6)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (isDone) "✓ " else if (isCur) "⚡ " else "⏳ ",
                                                            fontSize = 10.sp
                                                        )
                                                        Text(
                                                            text = "جزء ${chk.chunkIndex + 1} (${chk.formattedRange})",
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isCur || isDone) FontWeight.Bold else FontWeight.Normal,
                                                            color = when {
                                                                isDone -> Color(0xFF10B981)
                                                                isCur -> Color(0xFF8B5CF6)
                                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                            }
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
        }

        // 6. Dubbed Output Player & Result Details (Split-Screen Preview & Compare)
        if (autoDubState.resultClip != null) {
            val dubbedClip = autoDubState.resultClip!!

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎬 معاينة الفيديو المدبلج والمكساج النهائي:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mode Switch Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "مقارنة منقسمة متزامنة 🔲",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Split-Screen Preview & Compare Component with Synchronized Playback, Error Audit & Lettering
            item {
                SplitScreenPreviewCompareComponent(
                    clip = dubbedClip,
                    scriptLines = studioUiState.scriptLines,
                    activeLine = studioUiState.scriptLines.getOrNull(studioUiState.activeLineIndex),
                    currentSeconds = studioUiState.currentPlaybackSeconds,
                    isPlaying = studioUiState.isPlaying,
                    originalVolume = studioUiState.originalVolume,
                    dubVolume = studioUiState.dubVolume,
                    bgmVolume = studioUiState.bgmVolume,
                    isOriginalMuted = studioUiState.isMutedOriginal,
                    isDubMuted = studioUiState.isMutedDub,
                    compareMode = studioUiState.splitCompareViewMode,
                    splitFraction = studioUiState.splitDividerFraction,
                    isAbFlipActive = studioUiState.isAbFlipActive,
                    auditReport = studioUiState.auditReport,
                    isAuditRunning = studioUiState.isAuditRunning,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onSetCompareMode = { viewModel.setSplitCompareViewMode(it) },
                    onSetSplitFraction = { viewModel.setSplitDividerFraction(it) },
                    onToggleAbFlip = { viewModel.toggleAbFlip() },
                    onOriginalVolumeChange = { viewModel.setOriginalVolume(it) },
                    onDubVolumeChange = { viewModel.setDubVolume(it) },
                    onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                    onToggleOriginalMute = { viewModel.toggleMuteOriginal() },
                    onToggleDubMute = { viewModel.toggleMuteDub() },
                    onRunQualityAudit = { viewModel.runScriptQualityAudit() },
                    onAutoFixAndDiacritize = { viewModel.autoFixAndDiacritizeAllScriptLines() },
                    onSaveToStorage = {
                        viewModel.applyDubbedClipToStudio(dubbedClip)
                        viewModel.saveDubbedProjectToStorage()
                    },
                    onExportVideo = {
                        viewModel.applyDubbedClipToStudio(dubbedClip)
                        viewModel.openExportDialog()
                    },
                    onOpenInStudio = {
                        viewModel.applyDubbedClipToStudio(dubbedClip)
                        onNavigateToStudio()
                    },
                    onUpdateLine = { index, text, start, end ->
                        viewModel.updateScriptLineDirectly(index, text, start, end)
                    },
                    modifier = Modifier.testTag("split_screen_preview_compare_main")
                )
            }

            // Real-time Volume Mixer for the Output
            item {
                VolumeMixerCard(
                    originalVolume = studioUiState.originalVolume,
                    dubVoiceVolume = studioUiState.dubVolume,
                    bgmVolume = studioUiState.bgmVolume,
                    isOriginalMuted = studioUiState.isMutedOriginal,
                    isDubMuted = studioUiState.isMutedDub,
                    isSoloOriginal = studioUiState.isSoloOriginal,
                    isSoloDub = studioUiState.isSoloDub,
                    isVocalClarityActive = studioUiState.isVocalClarityActive,
                    isPlaying = studioUiState.isPlaying,
                    onOriginalVolumeChange = { viewModel.setOriginalVolume(it) },
                    onDubVoiceVolumeChange = { viewModel.setDubVolume(it) },
                    onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                    onToggleOriginalMute = { viewModel.toggleMuteOriginal() },
                    onToggleDubMute = { viewModel.toggleMuteDub() },
                    onToggleSoloOriginal = { viewModel.toggleSoloOriginal() },
                    onToggleSoloDub = { viewModel.toggleSoloDub() },
                    onToggleVocalClarity = { viewModel.toggleVocalClarity() },
                    onTogglePlaybackPreview = { viewModel.togglePlayPause() },
                    onQuickAutoBalance = { viewModel.quickAutoBalanceMix() },
                    onResetDefaults = { viewModel.resetVolumesToDefault() }
                )
            }

            // 6. Action & Save / Export / Share Section for Merged Dubbed Video
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_and_share_section_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221F2D)),
                    border = BorderStroke(1.5.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                    color = Color(0xFF6750A4),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "حفظ وتصدير الفيديو النهائي المدمج",
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "دمج مقطع الفيديو مع الصوت المسجل بدقة عالية مع خيارات المشاركة",
                                        color = Color(0xFFCAC4D0),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFA6D4A8)
                            ) {
                                Text(
                                    text = "MP4 جاهز 🎬",
                                    color = Color(0xFF1B5E20),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Primary Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary Direct Merge & Save Video to Storage
                            Button(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    viewModel.mergeOriginalVideoWithDubbedAudio(
                                        clip = dubbedClip,
                                        customTitle = "دبلجة_${dubbedClip.title}"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp)
                                    .testTag("direct_merge_and_save_storage_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("دمج وحفظ في الجهاز", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Primary Save & Export Video Button (with options)
                            Button(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    viewModel.openExportDialog()
                                },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("save_and_export_final_video_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                )
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("تصدير متقدم", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Action Buttons: Share & Export Synchronized Audio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    viewModel.exportSynchronizedDubbedAudioTrack(dubbedClip)
                                },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(44.dp)
                                    .testTag("export_synchronized_audio_track_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("تصدير مسار الصوت 🎵", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    showShareDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("share_merged_video_options_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F378B),
                                    contentColor = Color(0xFFEADDFF)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("خيارات المشاركة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Secondary Row: Save Project & Open in Studio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    viewModel.saveCurrentProject(dubbedClip.title)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("save_project_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("حفظ بالمعرض", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.applyDubbedClipToStudio(dubbedClip)
                                    onNavigateToStudio()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("open_in_studio_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("الاستوديو المتقدم", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // 7. Python Automation Script Card (Ready to Copy & Run)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("python_script_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "كود Python للدبلجة التلقائية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "مُستخرج بالتوقيتات والنصوص وجاهز للتشغيل",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Python + FFmpeg",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "تم إنشاء كود برمجي متكامل يحتوي على كافة المقاطع (${dubbedClip.scriptLines.size} مقاطع) مع توقيتاتها ونصوصها المترجمة، مدمجاً مع مكتبة gTTS و FFmpeg لدبلجة الفيديو فوراً على حاسوبك بضغطة زر واحدة.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showPythonCodeModal = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("view_python_code_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("عرض ونسخ الكود", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val generatedCode = generatePythonScript(dubbedClip)
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Python Dubbing Script", generatedCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ كود Python بالكامل إلى الحافظة! 📋", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("quick_copy_python_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("نسخ سريع للكود", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }
            }

            // 8. Synchronized Arabic Dialogue List (Editable & Playable)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📜 سيناريو الحوار المدبلج (${dubbedClip.scriptLines.size} مقاطع):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "اضغط لسماع أي مقطع",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        dubbedClip.scriptLines.forEachIndexed { index, line ->
                            val isActive = studioUiState.activeLineIndex == index
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isActive)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(line.characterAvatar, fontSize = 18.sp)
                                        }
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = line.characterName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Gender Badge
                                            val genderLabel = when (line.speakerGender) {
                                                "FEMALE" -> "👩 أنثى"
                                                "CHILD" -> "🧒 طفل"
                                                else -> "👨 ذكر"
                                            }
                                            val genderColor = when (line.speakerGender) {
                                                "FEMALE" -> Color(0xFFEC4899)
                                                "CHILD" -> Color(0xFFF59E0B)
                                                else -> Color(0xFF3B82F6)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = genderColor.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, genderColor.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = "$genderLabel (${line.genderConfidence}%)",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = genderColor,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                            // Timing Badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                Text(
                                                    text = "⏱️ ${line.startSeconds.toInt()}ث - ${line.endSeconds.toInt()}ث",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = line.textArabic,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            viewModel.previewSpeechForLine(line)
                                        },
                                        modifier = Modifier.testTag("preview_line_${index}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "استماع",
                                            tint = MaterialTheme.colorScheme.primary
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

    // Python Script Viewer & Direct Copy Dialog
    if (showPythonCodeModal && autoDubState.resultClip != null) {
        val clip = autoDubState.resultClip!!
        val pythonCode = generatePythonScript(clip)

        AlertDialog(
            onDismissRequest = { showPythonCodeModal = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("كود Python الجاهز للتشغيل", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = { showPythonCodeModal = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Text(
                        text = "يحتوي هذا الكود على جميع المقاطع والتوقيتات المستخرجة (${clip.scriptLines.size} مقاطع). انسخه وشغّله مباشرة في جهازك:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            item {
                                Text(
                                    text = pythonCode,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("Python Dubbing Script", pythonCode)
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(context, "تم نسخ الكود كاملاً! جاهز للتشغيل 🚀", Toast.LENGTH_LONG).show()
                        showPythonCodeModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("نسخ الكود بالكامل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPythonCodeModal = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Cloud TTS Configuration Dialog (ElevenLabs / Google Cloud / Device TTS)
    if (showCloudTtsDialog) {
        CloudTtsConfigDialog(
            config = cloudTtsConfig,
            onSaveConfig = { newConfig ->
                viewModel.updateCloudTtsConfig(newConfig)
            },
            onTestConnection = { callback ->
                viewModel.testCloudTtsConnection { success, message ->
                    callback(success, message)
                }
            },
            onDismiss = { showCloudTtsDialog = false }
        )
    }

    // Export Project & Video Quality Dialog
    if (exportDialogState.isVisible) {
        ExportProjectDialog(
            state = exportDialogState,
            initialVideoConfig = videoExportConfig,
            onDismiss = { viewModel.closeExportDialog() },
            onStartExport = { format, title, config ->
                viewModel.startExport(format, title, config)
            },
            onOpenFile = { result ->
                viewModel.openExportedFile(result)
            },
            onShareFile = { result ->
                viewModel.shareExportedFile(result)
            }
        )
    }

    // Share Dubbing Options Dialog
    if (showShareDialog) {
        val currentClip = studioUiState.currentClip
        ShareDubbingOptionsDialog(
            isVisible = showShareDialog,
            clipTitle = currentClip.title,
            clipCategory = currentClip.category,
            hasRecordedAudio = studioUiState.recordedAudioPath != null,
            onDismiss = { showShareDialog = false },
            onShareVideo = {
                viewModel.quickExportAndShareMergedVideo()
            },
            onShareAudio = {
                viewModel.shareRecordedAudioOnly()
            },
            onOpenExportDialog = {
                viewModel.openExportDialog()
            },
            onExportAudio = {
                viewModel.exportSynchronizedDubbedAudioTrack(currentClip)
            }
        )
    }

    // VATT Subtitles & Timed Transcript Dialog (SRT / WebVTT)
    if (showVattSubtitlesDialog) {
        var selectedSubtitleTab by remember { mutableStateOf(0) } // 0 = SRT, 1 = WebVTT
        val contentToShow = if (selectedSubtitleTab == 0) vattState.srtContent else vattState.vttContent

        AlertDialog(
            onDismissRequest = { showVattSubtitlesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ملفات ترجمة VATT المتزامنة 📑",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Subtitle Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedSubtitleTab == 0) Color(0xFF0284C7).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedSubtitleTab == 0) BorderStroke(1.5.dp, Color(0xFF0284C7)) else null,
                            modifier = Modifier.weight(1f).clickable { selectedSubtitleTab = 0 }
                        ) {
                            Text(
                                text = "صيغة SRT (.srt)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (selectedSubtitleTab == 0) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedSubtitleTab == 1) Color(0xFF0284C7).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedSubtitleTab == 1) BorderStroke(1.5.dp, Color(0xFF0284C7)) else null,
                            modifier = Modifier.weight(1f).clickable { selectedSubtitleTab = 1 }
                        ) {
                            Text(
                                text = "صيغة WebVTT (.vtt)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (selectedSubtitleTab == 1) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            item {
                                Text(
                                    text = contentToShow.ifBlank { "لم يتم إنشاء ملف الترجمة بعد، قم بتشغيل معالجة VATT أولاً." },
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFF1F5F9),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("VATT Subtitles", contentToShow)
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(context, "تم نسخ ملف الترجمة بنجاح! 📋", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("نسخ ملف الترجمة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showVattSubtitlesDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق", fontSize = 12.sp)
                }
            }
        )
    }
}

/**
 * Helper to dynamically generate a clean, executable Python script with
 * exact segment timestamps, original speech and Arabic translations.
 */
fun generatePythonScript(clip: DubbingClip): String {
    val d = "$"
    val segmentsBuilder = StringBuilder()
    clip.scriptLines.forEachIndexed { idx, line ->
        val origClean = line.textOriginal.replace("\"", "\\\"")
        val arabClean = line.textArabic.replace("\"", "\\\"")
        segmentsBuilder.append(
            """    {
        "id": ${idx + 1},
        "start": ${line.startSeconds},
        "end": ${line.endSeconds},
        "character": "${line.characterName}",
        "original": "$origClean",
        "arabic": "$arabClean"
    }"""
        )
        if (idx < clip.scriptLines.size - 1) {
            segmentsBuilder.append(",\n")
        }
    }

    return """
import os
import subprocess
from gtts import gTTS

# ---------------------------------------------------------
# 1. التوقيتات والنصوص المستخرجة تلقائياً من الفيديو (${clip.title})
# ---------------------------------------------------------
DUBBING_SEGMENTS = [
$segmentsBuilder
]

INPUT_VIDEO = "input_video.mp4"
OUTPUT_VIDEO = "output_dubbed_video.mp4"
TEMP_DIR = "dub_temp"

os.makedirs(TEMP_DIR, exist_ok=True)

# ---------------------------------------------------------
# 2. توليد الأصوات العربية الفصيحة بدقة لكل مقطع زمني (TTS)
# ---------------------------------------------------------
audio_filters = []
audio_inputs = []

print("🚀 بدء توليد المقاطع الصوتية العربية بالذكاء الاصطناعي...")

for idx, seg in enumerate(DUBBING_SEGMENTS):
    audio_path = os.path.join(TEMP_DIR, "seg_" + str(seg["id"]) + ".mp3")
    
    # توليد الصوت باللغة العربية الفصحى
    tts = gTTS(text=seg["arabic"], lang="ar", slow=False)
    tts.save(audio_path)
    
    delay_ms = int(seg["start"] * 1000)
    audio_inputs.extend(["-i", audio_path])
    
    # تأخير الصوت ليطابق بداية المشهد بدقة
    audio_filters.append("[" + str(idx + 1) + ":a]adelay=" + str(delay_ms) + "|" + str(delay_ms) + "[a" + str(idx) + "]")

# دمج المقاطع الصوتية في تراك صوتي واحد
mix_inputs = "".join(["[a" + str(i) + "]" for i in range(len(DUBBING_SEGMENTS))])
filter_complex = ";".join(audio_filters) + ";" + mix_inputs + "amix=inputs=" + str(len(DUBBING_SEGMENTS)) + ":dropout_transition=2[outa]"

# ---------------------------------------------------------
# 3. دمج الصوت العربي فوق الفيديو وكتم الصوت الأجنبي القديم
# ---------------------------------------------------------
print("🎬 جاري دمج الصوت العربي مع الفيديو الأصلي عبر FFmpeg...")

ffmpeg_cmd = [
    "ffmpeg", "-y",
    "-i", INPUT_VIDEO,
    *audio_inputs,
    "-filter_complex", filter_complex,
    "-map", "0:v",
    "-map", "[outa]",
    "-c:v", "copy",
    "-c:a", "aac",
    "-b:a", "192k",
    OUTPUT_VIDEO
]

subprocess.run(ffmpeg_cmd, check=True)

print("🎉 تم الانتهاء بنجاح! تم حفظ الفيديو المدبلج في: " + OUTPUT_VIDEO)
""".trimIndent()
}
