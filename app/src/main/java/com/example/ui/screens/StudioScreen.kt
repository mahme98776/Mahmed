package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.AudioWaveformTrack
import com.example.ui.components.AudioTrimmerDialog
import com.example.ui.components.AudioEffectsLibrarySheet
import com.example.ui.components.ExportProjectDialog
import com.example.ui.components.SplitScreenPreviewCompareComponent
import com.example.ui.components.VolumeNormalizationSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppTab
import com.example.ui.DubbingViewModel
import com.example.ui.components.AiCopilotAssistantModal
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.IntegratedVideoPlayerComponent
import com.example.ui.components.MixerControlSheet
import com.example.ui.components.OnboardingTourManager
import com.example.ui.components.OnboardingTourOverlay
import com.example.ui.components.RecordingAudioSignal
import com.example.ui.components.RecordingWaveformVisualizer
import com.example.ui.components.SoundboardPad
import com.example.ui.components.TeleprompterItem
import com.example.ui.components.VideoCanvasPlayer
import com.example.ui.components.VoiceLibrarySheet
import com.example.ui.components.ShareDubbingOptionsDialog

@Composable
fun StudioScreen(
    viewModel: DubbingViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProcessingPreview: () -> Unit = {},
    onNavigateToSync: () -> Unit = {},
    onNavigateToGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportDialogState by viewModel.exportDialogState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showOnboardingTour by remember { mutableStateOf(!OnboardingTourManager.isTourCompleted(context)) }
    var showCopilotModal by remember { mutableStateOf(false) }
    var showVoiceLibrarySheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showStudioShareDialog by remember { mutableStateOf(false) }
    var projectTitleInput by remember { mutableStateOf("") }
    var showAddLineDialog by remember { mutableStateOf(false) }
    var newLineSpeaker by remember { mutableStateOf("") }
    var newLineText by remember { mutableStateOf("") }
    var isFramePrecisionPlayerActive by remember { mutableStateOf(false) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importAudioFromUri(it) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importVideoForAutoDubbing(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecordingCountdown()
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Header
                item {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = Color(0xFF381E72),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "استوديو الدبلجة",
                                    color = Color(0xFFE6E1E5),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = state.currentClip.title,
                                color = Color(0xFFCAC4D0),
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Voice Library (4,800+ Human Voices)
                            IconButton(
                                onClick = { showVoiceLibrarySheet = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_voice_library_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.85f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🎙️", fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Mixer Button
                            IconButton(
                                onClick = { viewModel.setShowMixer(true) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("mixer_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "ميكسر الصوت",
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Audio Effects Library Button
                            IconButton(
                                onClick = { viewModel.openAudioEffectsLibrary() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("audio_effects_library_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.8f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🎛️", fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Split-Screen Preview & Compare Button
                            IconButton(
                                onClick = { viewModel.openSplitScreenCompare(true) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_split_compare_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Compare,
                                            contentDescription = "معاينة ومقارنة منقسمة",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Dedicated Video-Audio Waveform Precision Sync Button
                            IconButton(
                                onClick = onNavigateToSync,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_video_audio_sync_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "مزامنة الفيديو والصوت الدقيقة",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Dedicated Pre-Export Processing & Preview Hub Button
                            IconButton(
                                onClick = onNavigateToProcessingPreview,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_processing_preview_hub_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = "مركز المعالجة والمعاينة قبل التصدير",
                                            tint = Color(0xFFC084FC),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // AI Copilot Assistant Button
                            IconButton(
                                onClick = { showCopilotModal = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_ai_copilot_assistant_btn")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = "المساعد الذكي الفوري",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Interactive Onboarding Tour Button
                            IconButton(
                                onClick = { showOnboardingTour = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("reopen_onboarding_tour_btn")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = "جولة الاستخدام الإرشادية",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // App Guide & PDF Manual Button
                            IconButton(
                                onClick = onNavigateToGuide,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("open_guide_and_pdf_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "دليل الاستخدام وكتيب PDF",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Import & Dub Video Button
                            IconButton(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("import_video_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.8f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = "استيراد ودبلجة فيديو",
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Import & Trim Audio Button
                            IconButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("import_audio_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF80CBC4).copy(alpha = 0.6f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCut,
                                            contentDescription = "استيراد وقص صوت",
                                            tint = Color(0xFF80CBC4),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Save Project Button
                            IconButton(
                                onClick = {
                                    projectTitleInput = state.currentProject?.title ?: "دبلجة ${state.currentClip.title}"
                                    showSaveDialog = true
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("save_project_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFA6D4A8).copy(alpha = 0.5f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "حفظ المشروع",
                                            tint = Color(0xFFA6D4A8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Share Button
                            IconButton(
                                onClick = { viewModel.shareProject() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("share_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFD0E4FF).copy(alpha = 0.5f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "مشاركة",
                                            tint = Color(0xFFD0E4FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Export to MP4/MP3 Button
                            IconButton(
                                onClick = { viewModel.openExportDialog() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("studio_export_project_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "تصدير MP4 أو MP3",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            // Global Dark/Light Theme Toggle
                            val isDarkThemeActive by viewModel.isDarkMode.collectAsStateWithLifecycle()
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("studio_theme_toggle_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2B2930),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.9f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isDarkThemeActive) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "تبديل المظهر ليلي/نهاري",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Video Player Mode Switcher Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFramePrecisionPlayerActive) "🎬 مشغل المعاينة والتنقل إطاراً بإطار" else "🎥 كانفاس الاستوديو المباشر",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFramePrecisionPlayerActive) Color(0xFF6366F1).copy(alpha = 0.2f) else Color(0xFF2B2930),
                            border = BorderStroke(1.dp, if (isFramePrecisionPlayerActive) Color(0xFF818CF8) else Color(0xFF49454F)),
                            modifier = Modifier
                                .clickable { isFramePrecisionPlayerActive = !isFramePrecisionPlayerActive }
                                .testTag("toggle_precision_player_mode_btn")
                        ) {
                            Text(
                                text = if (isFramePrecisionPlayerActive) "العودة للكانفاس ↺" else "مشغل إطار بإطار 🎞️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFramePrecisionPlayerActive) Color(0xFFC7D2FE) else Color(0xFFCAC4D0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Video / Canvas Scene Player
                item {
                    val activeLine = state.scriptLines.getOrNull(state.activeLineIndex)
                    if (isFramePrecisionPlayerActive) {
                        IntegratedVideoPlayerComponent(
                            clip = state.currentClip,
                            currentSeconds = state.currentPlaybackSeconds,
                            isPlaying = state.isPlaying,
                            activeLine = activeLine,
                            isMutedOriginal = state.isMutedOriginal,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekTo(it) },
                            onToggleMute = { viewModel.toggleMuteOriginal() },
                            modifier = Modifier.testTag("studio_integrated_video_player")
                        )
                    } else {
                        VideoCanvasPlayer(
                            clip = state.currentClip,
                            currentSeconds = state.currentPlaybackSeconds,
                            isPlaying = state.isPlaying,
                            isRecording = state.isRecording,
                            activeLine = activeLine,
                            isMutedOriginal = state.isMutedOriginal,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onToggleMute = { viewModel.toggleMuteOriginal() },
                            onSeek = { viewModel.seekTo(it) }
                        )
                    }
                }

                // Recording & Live Waveform Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        border = BorderStroke(1.dp, if (state.isRecording) Color(0xFFF2B8B5) else Color(0xFF49454F))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Timecode & Voice Effect Badge & Instant Gender Dub Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time code
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF141218).copy(alpha = 0.6f)
                                ) {
                                    val currentSec = state.currentPlaybackSeconds.toInt()
                                    val totalSec = state.currentClip.durationSeconds
                                    Text(
                                        text = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60),
                                        color = Color(0xFFCAC4D0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Instant Gender Dubbing / Voice Mode Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (state.isRecording) Color(state.lastDetectedGender.colorHex).copy(alpha = 0.25f) else Color(0xFF4A4458),
                                    border = BorderStroke(
                                        1.dp,
                                        if (state.isRecording) Color(state.lastDetectedGender.colorHex) else Color(0xFFD0BCFF).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.clickable {
                                        val nextMode = when (viewModel.instantConfig.value.mode) {
                                            com.example.audio.InstantDubbingMode.AUTO_DETECT -> com.example.audio.InstantDubbingMode.MALE_TO_FEMALE
                                            com.example.audio.InstantDubbingMode.MALE_TO_FEMALE -> com.example.audio.InstantDubbingMode.FEMALE_TO_MALE
                                            com.example.audio.InstantDubbingMode.FEMALE_TO_MALE -> com.example.audio.InstantDubbingMode.TO_CARTOON
                                            com.example.audio.InstantDubbingMode.TO_CARTOON -> com.example.audio.InstantDubbingMode.TO_CYBER_ROBOT
                                            com.example.audio.InstantDubbingMode.TO_CYBER_ROBOT -> com.example.audio.InstantDubbingMode.AUTO_DETECT
                                        }
                                        viewModel.setInstantDubbingMode(nextMode)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (state.isRecording && state.lastDetectedGender != com.example.audio.DetectedGender.SILENCE) {
                                                "${state.lastDetectedGender.emoji} ${state.lastDetectedGender.titleArabic} • تحويل فوري"
                                            } else {
                                                "دبلجة فورية: ${viewModel.instantConfig.value.mode.titleArabic}"
                                            },
                                            color = if (state.isRecording) Color(state.lastDetectedGender.colorHex) else Color(0xFFEADDFF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Real-time Recording Audio Signal Visualizer (Live Oscilloscope + Level Meter + Clipping Alert)
                            if (state.isRecording) {
                                val dbFs = -60f + (state.liveAmplitude * 60f)
                                val isOvermod = state.liveAmplitude > 0.88f
                                val isSpeech = state.liveAmplitude > 0.08f
                                RecordingWaveformVisualizer(
                                    signal = RecordingAudioSignal(
                                        amplitude = state.liveAmplitude,
                                        peakAmplitude = state.liveAmplitude * 1.15f,
                                        decibels = dbFs,
                                        isClipping = isOvermod,
                                        isSpeechDetected = isSpeech,
                                        recordingDurationSeconds = state.currentPlaybackSeconds
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            // Interactive Audio Waveform with Silence Detection & Volume Meter
                            val totalClipSec = state.currentClip.durationSeconds.toFloat().coerceAtLeast(1f)
                            val progressFrac = (state.currentPlaybackSeconds / totalClipSec).coerceIn(0f, 1f)
                            AudioWaveformTrack(
                                waveformHistory = state.waveformHistory,
                                currentAmplitude = state.liveAmplitude,
                                isRecording = state.isRecording,
                                isPlaying = state.isPlaying,
                                progressFraction = progressFrac,
                                totalDurationSeconds = totalClipSec,
                                onSeek = { viewModel.seekTo(it) }
                            )

                            Spacer(Modifier.height(12.dp))

                            // Action Controls (Record Button, Play/Pause, Rewind, Change Scene)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rewind / Reset Button
                                IconButton(
                                    onClick = { viewModel.seekTo(0f) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4A4458),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Replay,
                                                contentDescription = "إعادة للبداية",
                                                tint = Color(0xFFCAC4D0),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Main Record / Stop Mic Button (Center Highlighted)
                                Surface(
                                    shape = CircleShape,
                                    color = if (state.isRecording) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clickable {
                                            if (state.isRecording) {
                                                viewModel.stopRecording()
                                            } else {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED
                                                if (hasPermission) {
                                                    viewModel.startRecordingCountdown()
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        }
                                        .testTag("record_dub_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = if (state.isRecording) "إيقاف التسجيل" else "بدء الدبلجة",
                                            tint = if (state.isRecording) Color(0xFF601410) else Color(0xFF381E72),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                // Play / Pause Button
                                IconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4A4458),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                                tint = Color(0xFFD0E4FF),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Audio Action Buttons (Effects Library, Import, Trim Take)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Audio Effect Chip Button
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF381E72),
                                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.8f)),
                                    modifier = Modifier
                                        .clickable { viewModel.openAudioEffectsLibrary() }
                                        .testTag("quick_effect_chip_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = state.selectedAudioEffectItem.iconEmoji, fontSize = 13.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "مؤثر: ${state.selectedAudioEffectItem.titleArabic.split(" ")[0]} 🎛️",
                                            color = Color(0xFFD0BCFF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2E2442),
                                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .clickable { viewModel.openVolumeNormalizationSheet() }
                                        .testTag("quick_normalize_volume_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Balance,
                                            contentDescription = null,
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "موازنة ⚖️",
                                            color = Color(0xFFD0BCFF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF25232A),
                                    border = BorderStroke(1.dp, Color(0xFF80CBC4).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { audioPickerLauncher.launch("audio/*") }
                                        .testTag("quick_import_audio_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = Color(0xFF80CBC4),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "استيراد صوت",
                                            color = Color(0xFF80CBC4),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (state.recordedAudioPath != null) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2B2930),
                                        border = BorderStroke(1.dp, Color(0xFF90CAF9).copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .clickable { viewModel.openAudioTrimmerForCurrentTake() }
                                            .testTag("quick_trim_audio_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCut,
                                                contentDescription = null,
                                                tint = Color(0xFF90CAF9),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = "قص ✂️",
                                                color = Color(0xFF90CAF9),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Undo & Redo Quick Buttons
                                if (state.canUndo) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2B2930),
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .clickable { viewModel.undo() }
                                            .testTag("quick_undo_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Undo,
                                                contentDescription = "تراجع",
                                                tint = Color(0xFFFFB74D),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = "تراجع",
                                                color = Color(0xFFFFB74D),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (state.canRedo) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2B2930),
                                        border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .clickable { viewModel.redo() }
                                            .testTag("quick_redo_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Redo,
                                                contentDescription = "إعادة",
                                                tint = Color(0xFF81C784),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = "إعادة",
                                                color = Color(0xFF81C784),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Instruction Hint
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (state.isRecording) "جارٍ تسجيل صوتك للدبلجة.. تحدث مع المشهد!" else "اضغط على زر الميكروفون للبدء بالدبلجة مع المشهد",
                                color = if (state.isRecording) Color(0xFFF2B8B5) else Color(0xFF938F99),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Recorded Audio Take & Trimming Card
                if (state.recordedAudioPath != null && !state.isRecording) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recorded_audio_take_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF23202A)),
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF381E72),
                                            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.8f)),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCut,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD0BCFF),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "مقطع صوتي مسجل جاهز 🎙️",
                                                color = Color(0xFFE6E1E5),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "حدد واقتطع الأجزاء المطلوبة واحفظها بكل سهولة",
                                                color = Color(0xFFCAC4D0),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Action Buttons: Auto-Balance & Trimmer
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF381E72),
                                            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                                            modifier = Modifier
                                                .clickable { viewModel.quickAutoBalanceMix() }
                                                .testTag("take_quick_normalize_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Balance,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD0BCFF),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "موازنة ⚖️",
                                                    color = Color(0xFFD0BCFF),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.openAudioTrimmerForCurrentTake() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("open_trimmer_card_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCut,
                                                contentDescription = null,
                                                tint = Color(0xFF381E72),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "قص وضبط ✂️",
                                                color = Color(0xFF381E72),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Save & Export Merged Final Video with Recorded Audio Card
                item {
                    val hasRecordedAudio = state.recordedAudioPath != null && java.io.File(state.recordedAudioPath).exists()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_export_share_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221F2D)),
                        border = BorderStroke(1.5.dp, if (hasRecordedAudio) Color(0xFFD0BCFF) else Color(0xFF49454F))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        color = if (hasRecordedAudio) Color(0xFF6750A4) else Color(0xFF38334C),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = if (hasRecordedAudio) Color(0xFFD0BCFF) else Color(0xFF938F99),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "تصدير وحفظ الفيديو النهائي المدمج 🎬",
                                            color = Color(0xFFE6E1E5),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (hasRecordedAudio) "جاهز للتصدير والمشاركة المباشرة مع الصوت المسجل" else "قم بتسجيل الصوت لتصدير ومشاركة الفيديو المدمج",
                                            color = Color(0xFFCAC4D0),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (hasRecordedAudio) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFA6D4A8)
                                    ) {
                                        Text(
                                            text = "جاهز ⚡",
                                            color = Color(0xFF1B5E20),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.openExportDialog() },
                                    enabled = hasRecordedAudio,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD0BCFF),
                                        contentColor = Color(0xFF381E72),
                                        disabledContainerColor = Color(0xFF2B2930),
                                        disabledContentColor = Color(0xFF938F99)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp)
                                        .testTag("studio_save_export_video_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("حفظ وتصدير الفيديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showStudioShareDialog = true },
                                    enabled = hasRecordedAudio,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4F378B),
                                        contentColor = Color(0xFFEADDFF),
                                        disabledContainerColor = Color(0xFF2B2930),
                                        disabledContentColor = Color(0xFF938F99)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (hasRecordedAudio) Color(0xFFD0BCFF) else Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("studio_share_video_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("خيارات المشاركة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Soundboard Quick FX
                item {
                    SoundboardPad(
                        onPlayEffect = { viewModel.playSoundEffect(it) }
                    )
                }

                // Teleprompter / Script Header with Gemini AI Script Generation
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color(0xFFFFD993),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "سيناريو المشهد (التلقين الصوتي)",
                                color = Color(0xFFE6E1E5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Gemini AI Script Button
                            Button(
                                onClick = { viewModel.generateGeminiArabicScript() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E72)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                                modifier = Modifier.testTag("gemini_generate_script_btn")
                            ) {
                                if (state.isGeneratingAiDub) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFD0BCFF),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("توليد...", color = Color(0xFFD0BCFF), fontSize = 10.sp)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD993), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Gemini AI 🤖", color = Color(0xFFD0BCFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            Button(
                                onClick = { showAddLineDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4458)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("إضافة", color = Color(0xFFD0BCFF), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Script Lines
                itemsIndexed(state.scriptLines) { index, line ->
                    val isActive = index == state.activeLineIndex
                    TeleprompterItem(
                        line = line,
                        isActive = isActive,
                        onSpeak = { viewModel.speakScriptLine(line) },
                        onClick = { viewModel.seekTo(line.startSeconds) },
                        onNudge = { dStart, dEnd -> viewModel.nudgeScriptLineTimestamp(index, dStart, dEnd) },
                        onAutoFit = { viewModel.autoFitScriptLineDuration(index) }
                    )
                }

                item {
                    Spacer(Modifier.height(30.dp))
                }
            }
        }

        // Countdown Overlay (3, 2, 1)
        AnimatedVisibility(
            visible = state.countdownNumber > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141218).copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.countdownNumber}",
                        color = Color(0xFFFFD993),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "استعد للكلام مع المشهد!",
                        color = Color(0xFFE6E1E5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Mixer Bottom Sheet
        if (state.showMixerSheet) {
            MixerControlSheet(
                originalVolume = state.originalVolume,
                dubVolume = state.dubVolume,
                bgmVolume = state.bgmVolume,
                selectedVoiceEffect = state.selectedVoiceEffect,
                selectedBgmStyle = state.selectedBgmStyle,
                isOriginalMuted = state.isMutedOriginal,
                isDubMuted = state.isMutedDub,
                isSoloOriginal = state.isSoloOriginal,
                isSoloDub = state.isSoloDub,
                isVocalClarityActive = state.isVocalClarityActive,
                isPlaying = state.isPlaying,
                onOriginalVolumeChange = { viewModel.setOriginalVolume(it) },
                onDubVolumeChange = { viewModel.setDubVolume(it) },
                onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                onToggleOriginalMute = { viewModel.toggleMuteOriginal() },
                onToggleDubMute = { viewModel.toggleMuteDub() },
                onToggleSoloOriginal = { viewModel.toggleSoloOriginal() },
                onToggleSoloDub = { viewModel.toggleSoloDub() },
                onToggleVocalClarity = { viewModel.toggleVocalClarity() },
                onVoiceEffectSelect = { viewModel.setVoiceEffect(it) },
                onBgmStyleSelect = { viewModel.setBgmStyle(it) },
                onTogglePlaybackPreview = { viewModel.togglePlayPause() },
                onOpenEffectsLibrary = {
                    viewModel.setShowMixer(false)
                    viewModel.openAudioEffectsLibrary()
                },
                onOpenNormalization = {
                    viewModel.setShowMixer(false)
                    viewModel.openVolumeNormalizationSheet()
                },
                onQuickAutoBalance = {
                    viewModel.quickAutoBalanceMix()
                },
                onResetDefaults = {
                    viewModel.resetVolumesToDefault()
                },
                onDismiss = { viewModel.setShowMixer(false) }
            )
        }

        // Human Voice Library Modal Sheet (4,800+ Voices)
        if (showVoiceLibrarySheet) {
            VoiceLibrarySheet(
                viewModel = viewModel,
                onDismiss = { showVoiceLibrarySheet = false },
                onSelectVoice = { voiceModel ->
                    showVoiceLibrarySheet = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم تفعيل الصوت: ${voiceModel.nameArabic} (${voiceModel.dialect.titleArabic})")
                    }
                }
            )
        }

        // Save Project Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                containerColor = Color(0xFF2B2930),
                title = {
                    Text("حفظ مشروع الدبلجة", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text("أدخل عنواناً لمشروع الدبلجة لحفظه في قائمة مشاريعك:", color = Color(0xFFCAC4D0), fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = projectTitleInput,
                            onValueChange = { projectTitleInput = it },
                            placeholder = { Text("عنوان المشروع", color = Color(0xFF938F99)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedTextColor = Color(0xFFE6E1E5),
                                unfocusedTextColor = Color(0xFFE6E1E5)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveCurrentProject(projectTitleInput)
                            showSaveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text("حفظ", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("إلغاء", color = Color(0xFFCAC4D0))
                    }
                }
            )
        }

        // Add Line Dialog
        if (showAddLineDialog) {
            AlertDialog(
                onDismissRequest = { showAddLineDialog = false },
                containerColor = Color(0xFF2B2930),
                title = {
                    Text("إضافة جملة حوارية جديدة", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newLineSpeaker,
                            onValueChange = { newLineSpeaker = it },
                            label = { Text("اسم المتحدث / الشخصية", color = Color(0xFFCAC4D0)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedTextColor = Color(0xFFE6E1E5),
                                unfocusedTextColor = Color(0xFFE6E1E5)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newLineText,
                            onValueChange = { newLineText = it },
                            label = { Text("النص العربي للدبلجة", color = Color(0xFFCAC4D0)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedTextColor = Color(0xFFE6E1E5),
                                unfocusedTextColor = Color(0xFFE6E1E5)
                            ),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLineText.isNotBlank()) {
                                val currentPos = state.currentPlaybackSeconds
                                viewModel.addScriptLine(
                                    characterName = newLineSpeaker,
                                    textArabic = newLineText,
                                    startSec = currentPos,
                                    endSec = (currentPos + 4f).coerceAtMost(state.currentClip.durationSeconds.toFloat())
                                )
                                newLineSpeaker = ""
                                newLineText = ""
                                showAddLineDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                    ) {
                        Text("إضافة", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddLineDialog = false }) {
                        Text("إلغاء", color = Color(0xFFCAC4D0))
                    }
                }
            )
        }

        // Visual Audio Trimmer Dialog
        if (state.showAudioTrimmer) {
            AudioTrimmerDialog(
                filePath = state.trimmerAudioPath ?: "",
                waveform = state.trimmerWaveform,
                durationSeconds = state.trimmerDurationSeconds,
                startSeconds = state.trimmerStartSeconds,
                endSeconds = state.trimmerEndSeconds,
                isPlaying = state.isTrimmerPlaying,
                playbackSeconds = state.trimmerPlaybackSeconds,
                isLooping = state.isTrimmerLooping,
                isLoading = state.isTrimmerLoading,
                isProcessingTrim = state.isProcessingTrim,
                formatName = state.trimmerFormatName,
                silenceStartSeconds = state.trimmerSilenceStart,
                silenceEndSeconds = state.trimmerSilenceEnd,
                onStartChange = { viewModel.setTrimmerStart(it) },
                onEndChange = { viewModel.setTrimmerEnd(it) },
                onTogglePlay = { viewModel.toggleTrimmerPlayback() },
                onToggleLoop = { viewModel.toggleTrimmerLoop() },
                onSeek = { viewModel.seekTrimmerPlayback(it) },
                onPickOtherAudio = { audioPickerLauncher.launch("audio/*") },
                onApplyTrim = { start, end -> viewModel.applyTrimmedAudio(start, end) },
                onSaveAsNewFile = { start, end -> viewModel.saveTrimmedAudioAsStandalone(start, end) },
                onDismiss = { viewModel.closeAudioTrimmer() }
            )
        }

        // Real-time Audio Effects Library Bottom Sheet
        if (state.showEffectsLibrary) {
            AudioEffectsLibrarySheet(
                selectedEffect = state.selectedAudioEffectItem,
                currentParams = state.customEffectParams,
                isPlayingPreview = state.isEffectsPreviewPlaying,
                isProcessing = state.isProcessingEffectDsp,
                hasRecordedAudio = state.recordedAudioPath != null,
                onSelectEffect = { viewModel.selectAudioEffect(it) },
                onUpdateParams = { viewModel.updateCustomEffectParams(it) },
                onTogglePreview = { viewModel.toggleEffectsPreviewPlayback() },
                onApplyToTake = { viewModel.applyEffectToCurrentTake() },
                onCleanupStatic = { viewModel.cleanUpRecordedAudioStatic() },
                onDismiss = { viewModel.closeAudioEffectsLibrary() }
            )
        }

        // Auto Volume Normalization Bottom Sheet
        if (state.showNormalizationSheet) {
            VolumeNormalizationSheet(
                isAnalyzing = state.isAnalyzingAudioLevels,
                currentOriginalVol = state.originalVolume,
                currentDubVol = state.dubVolume,
                currentBgmVol = state.bgmVolume,
                selectedMode = state.normalizationMode,
                isAutoDuckingEnabled = state.isAutoDuckingEnabled,
                normalizationResult = state.normalizationResult,
                voiceProfile = state.voiceLoudnessProfile,
                backgroundProfile = state.backgroundLoudnessProfile,
                hasRecordedAudio = state.recordedAudioPath != null,
                onSelectMode = { viewModel.setNormalizationMode(it) },
                onToggleAutoDucking = { viewModel.toggleAutoDucking(it) },
                onApplyBalancedVolumes = { orig, dub, bgm ->
                    viewModel.applyAutoVolumeBalance(orig, dub, bgm)
                },
                onResetDefaults = { viewModel.resetVolumesToDefault() },
                onDismiss = { viewModel.closeVolumeNormalizationSheet() }
            )
        }

        // Export Dialog
        if (exportDialogState.isVisible) {
            val videoExportConfig by viewModel.videoExportConfig.collectAsStateWithLifecycle()
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
        if (showStudioShareDialog) {
            ShareDubbingOptionsDialog(
                isVisible = showStudioShareDialog,
                clipTitle = state.currentClip.title,
                clipCategory = state.currentClip.category,
                hasRecordedAudio = state.recordedAudioPath != null,
                onDismiss = { showStudioShareDialog = false },
                onShareVideo = {
                    viewModel.quickExportAndShareMergedVideo()
                },
                onShareAudio = {
                    viewModel.shareRecordedAudioOnly()
                },
                onOpenExportDialog = {
                    viewModel.openExportDialog()
                }
            )
        }

        // Split-Screen Preview & Compare Dialog
        if (state.showSplitScreenCompare) {
            AlertDialog(
                onDismissRequest = { viewModel.openSplitScreenCompare(false) },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.openSplitScreenCompare(false) }) {
                        Text("إغلاق المعاينة ✕", color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    SplitScreenPreviewCompareComponent(
                        clip = state.currentClip,
                        scriptLines = state.scriptLines,
                        activeLine = state.scriptLines.getOrNull(state.activeLineIndex),
                        currentSeconds = state.currentPlaybackSeconds,
                        isPlaying = state.isPlaying,
                        originalVolume = state.originalVolume,
                        dubVolume = state.dubVolume,
                        bgmVolume = state.bgmVolume,
                        isOriginalMuted = state.isMutedOriginal,
                        isDubMuted = state.isMutedDub,
                        compareMode = state.splitCompareViewMode,
                        splitFraction = state.splitDividerFraction,
                        isAbFlipActive = state.isAbFlipActive,
                        auditReport = state.auditReport,
                        isAuditRunning = state.isAuditRunning,
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
                            viewModel.saveDubbedProjectToStorage()
                            viewModel.openSplitScreenCompare(false)
                        },
                        onExportVideo = {
                            viewModel.openExportDialog()
                            viewModel.openSplitScreenCompare(false)
                        },
                        onOpenInStudio = {
                            viewModel.openSplitScreenCompare(false)
                        },
                        onUpdateLine = { index, text, start, end ->
                            viewModel.updateScriptLineDirectly(index, text, start, end)
                        }
                    )
                },
                containerColor = Color(0xFF131118)
            )
        }

        // Interactive Onboarding Tooltip & Spotlight Tour Overlay
        OnboardingTourOverlay(
            isVisible = showOnboardingTour,
            onDismiss = { showOnboardingTour = false },
            onFinishTour = { showOnboardingTour = false }
        )

        // Floating AI Copilot Assistant Trigger Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp, end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                onClick = { showCopilotModal = true },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1B4B),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                shadowElevation = 8.dp,
                modifier = Modifier.testTag("floating_ai_copilot_badge_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "المساعد الذكي AI",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "مساعد AI الذكي 🤖",
                        color = Color(0xFFE0F7FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // AI Copilot Smart Assistant Modal
        if (showCopilotModal) {
            AiCopilotAssistantModal(
                viewModel = viewModel,
                onDismiss = { showCopilotModal = false },
                onNavigateToTab = { tab ->
                    showCopilotModal = false
                    when (tab) {
                        AppTab.CLIPS -> onNavigateToLibrary()
                        AppTab.STUDIO -> {}
                        AppTab.RECORDING -> {}
                        AppTab.VOICE_LIBRARY -> { showVoiceLibrarySheet = true }
                        AppTab.SYNC_STUDIO -> onNavigateToSync()
                        AppTab.PROCESSING_PREVIEW -> onNavigateToProcessingPreview()
                        AppTab.INSTANT_DUB -> {}
                        AppTab.AI_DUB -> {}
                        AppTab.VIDEO_DUB -> {}
                        AppTab.PROJECTS -> onNavigateToLibrary()
                        AppTab.SOUNDBOARD -> {}
                        AppTab.SETTINGS -> {}
                        AppTab.HELP_GUIDE -> onNavigateToGuide()
                        AppTab.UPDATE_CENTER -> {}
                    }
                },
                onOpenProcessingCenter = {
                    showCopilotModal = false
                    onNavigateToProcessingPreview()
                }
            )
        }
    }
}
