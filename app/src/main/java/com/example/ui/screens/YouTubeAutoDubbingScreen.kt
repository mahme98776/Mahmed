package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.audio.youtube.YouTubeDubSegment
import com.example.audio.youtube.YouTubeDubbingStep
import com.example.ui.DubbingViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeAutoDubbingScreen(
    viewModel: DubbingViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val progressState by viewModel.youTubeAutoDubberEngine.progressState.collectAsState()

    var youtubeUrlInput by remember { mutableStateOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }
    var customSrtText by remember { mutableStateOf("") }
    var showCustomSrtBox by remember { mutableStateOf(false) }

    var selectedTargetLang by remember { mutableStateOf("ar") }
    var selectedVoiceId by remember { mutableStateOf("natural_arabic_male") }
    var bgDuckVolume by remember { mutableStateOf(0.20f) }
    var speechRateMultiplier by remember { mutableStateOf(1.0f) }

    var playingSegmentIdx by remember { mutableStateOf<Int?>(null) }

    val languages = listOf(
        Pair("ar", "العربية الفصحى 🇸🇦"),
        Pair("uk", "الأوكرانية (Українська) 🇺🇦"),
        Pair("en", "الإنجليزية 🇺🇸"),
        Pair("es", "الإسبانية 🇪🇸"),
        Pair("fr", "الفرنسية 🇫🇷"),
        Pair("de", "الألمانية 🇩🇪"),
        Pair("ja", "اليابانية 🇯🇵"),
        Pair("tr", "التركية 🇹🇷")
    )

    val sampleClips = listOf(
        Triple("tech", "💡 مراجعة تقنية وذكاء اصطناعي", "https://www.youtube.com/watch?v=tech_ai_nextgen"),
        Triple("nature", "🦁 فيلم وثائقي وطبيعة برية", "https://www.youtube.com/watch?v=nature_wildlife_doc"),
        Triple("gaming", "🎮 لحظات لعب وتحديات", "https://www.youtube.com/watch?v=gaming_boss_fight")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("youtube_auto_dubbing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF0000).copy(alpha = 0.22f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.18f),
                                    Color(0xFF3B82F6).copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF0000),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "YouTube",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "دبلجة يوتيوب الآلية الذكية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFF0000).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFFFF0000))
                                ) {
                                    Text(
                                        text = "YouTube Auto-Dubbing",
                                        color = Color(0xFFFF0000),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "تحميل الترجمات من يوتيوب، الترجمة بدقة، توليد الأصوات، وضبط المزامنة الزمنية التلقائية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. YouTube URL Input & Preset Clips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. أدخل رابط فيديو يوتيوب:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = youtubeUrlInput,
                        onValueChange = { youtubeUrlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_url_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFFF0000))
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val pasted = clip.getItemAt(0).text.toString()
                                        if (pasted.isNotBlank()) youtubeUrlInput = pasted
                                    }
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "لصق الرابط", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        placeholder = { Text("https://www.youtube.com/watch?v=...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "أو اختر عينة مقطع للتجربة الفورية:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleClips.forEach { (_, title, url) ->
                            FilterChip(
                                selected = youtubeUrlInput == url,
                                onClick = { youtubeUrlInput = url },
                                label = { Text(title, fontSize = 11.5.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showCustomSrtBox = !showCustomSrtBox },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (showCustomSrtBox) "إخفاء محرر ملف الترجمة SRT" else "رفع أو لصق ملف ترجمة مخصص (.SRT / .VTT)",
                            fontSize = 12.sp
                        )
                    }

                    if (showCustomSrtBox) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customSrtText,
                            onValueChange = { customSrtText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("ألصق محتوى ملف الترجمة SRT هنا...\n1\n00:00:01,000 --> 00:00:04,000\nHello world!", fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // 3. Settings & Options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. إعدادات لغة الدبلجة والنبرة الصوتية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    Text("لغة الدبلجة المستهدفة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { (code, name) ->
                            val isSelected = selectedTargetLang == code
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTargetLang = code },
                                label = { Text(name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("النبرة والصوت المعتمد:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.ttsManager.voiceProfiles.take(6).forEach { profile ->
                            val isSelected = selectedVoiceId == profile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedVoiceId = profile.id },
                                label = { Text("${profile.emoji} ${profile.titleArabic}", fontSize = 11.5.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تخفيض صوت خلفية يوتيوب (Ducking):", style = MaterialTheme.typography.labelMedium)
                        Text("${(bgDuckVolume * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = bgDuckVolume,
                        onValueChange = { bgDuckVolume = it },
                        valueRange = 0.05f..0.50f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مضاعف سرعة الصوت للتوافق الزمني:", style = MaterialTheme.typography.labelMedium)
                        Text("${String.format("%.2f", speechRateMultiplier)}x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = speechRateMultiplier,
                        onValueChange = { speechRateMultiplier = it },
                        valueRange = 0.85f..1.35f,
                        steps = 10
                    )
                }
            }
        }

        // 4. Primary Execute Button
        item {
            val isProcessing = progressState.step != YouTubeDubbingStep.IDLE &&
                    progressState.step != YouTubeDubbingStep.COMPLETED &&
                    progressState.step != YouTubeDubbingStep.ERROR

            Button(
                onClick = {
                    scope.launch {
                        viewModel.youTubeAutoDubberEngine.startAutoDubbing(
                            youtubeUrl = youtubeUrlInput,
                            customSrtContent = customSrtText.ifBlank { null },
                            targetLanguage = selectedTargetLang,
                            voiceProfileId = selectedVoiceId,
                            speechSpeedMultiplier = speechRateMultiplier,
                            bgDuckVolume = bgDuckVolume
                        )
                    }
                },
                enabled = !isProcessing && youtubeUrlInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_youtube_dubbing_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0000)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("جاري معالجة ودبلجة فيديو يوتيوب...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("بدء الدبلجة الآلية الشاملة ليوتيوب 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }

        // 5. Dynamic Progress Card
        if (progressState.step != YouTubeDubbingStep.IDLE) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (progressState.step == YouTubeDubbingStep.COMPLETED) {
                            Color(0xFF10B981).copy(alpha = 0.15f)
                        } else if (progressState.step == YouTubeDubbingStep.ERROR) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        }
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp,
                        if (progressState.step == YouTubeDubbingStep.COMPLETED) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(progressState.step.stepBadge, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = progressState.step.stepNameArabic,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            if (progressState.totalSegments > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${(progressState.progressFraction * 100).toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progressState.progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (progressState.step == YouTubeDubbingStep.COMPLETED) Color(0xFF10B981) else Color(0xFFFF0000),
                        )

                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = progressState.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (progressState.isFinished && progressState.exportedSrtPath != null) {
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val srtFile = File(progressState.exportedSrtPath!!)
                                        if (srtFile.exists()) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("SRT", srtFile.readText()))
                                            Toast.makeText(context, "تم نسخ محتوى ملف الترجمة SRT 📄", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("نسخ وحفظ SRT", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        // Transfer YouTube segments into Studio Timeline
                                        viewModel.importYouTubeSegmentsToStudio(progressState.segments)
                                        Toast.makeText(context, "تم نقل المقاطع بنجاح إلى استوديو الدبلجة 🎬", Toast.LENGTH_SHORT).show()
                                        onNavigateToStudio()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("فتح بالاستوديو 🎬", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Subtitles & Timing Inspector
        if (progressState.segments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شرائح الحوار المترجمة والمتزامنة (${progressState.segments.size} شريحة):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (progressState.exportedAudioPath != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.youTubeAutoDubberEngine.playSegmentAudio(progressState.exportedAudioPath) {
                                    playingSegmentIdx = null
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("تشغيل المسار كاملاً", fontSize = 11.sp)
                        }
                    }
                }
            }

            items(progressState.segments) { seg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "#${seg.index} | ${seg.formattedStartTime} ➔ ${seg.formattedEndTime} (${String.format("%.1f", seg.durationSeconds)}s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (seg.audioPath != null) {
                                val isThisPlaying = playingSegmentIdx == seg.index
                                IconButton(
                                    onClick = {
                                        if (isThisPlaying) {
                                            viewModel.youTubeAutoDubberEngine.stopPlayback()
                                            playingSegmentIdx = null
                                        } else {
                                            playingSegmentIdx = seg.index
                                            viewModel.youTubeAutoDubberEngine.playSegmentAudio(seg.audioPath) {
                                                playingSegmentIdx = null
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Play Segment",
                                        tint = Color(0xFFFF0000)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "الأصل (يوتيوب): ${seg.sourceText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (seg.translatedText.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "الدبلجة المتزامنة: ${seg.translatedText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
