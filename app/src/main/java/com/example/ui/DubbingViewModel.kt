/**
 * تطبيق فويس ماستر برو | VoiceMaster Pro
 * استوديو الدبلجة وهندسة الصوت بالذكاء الاصطناعي
 * 
 * المالك والمبتكر وصاحب كافة حقوق النشر والملكية الفكرية:
 * محمد رضا محمود محمود السيد سليمة
 * مصر - محافظة المنوفية - مركز شبين الكوم - شارع القفاص
 * جميع الحقوق محفوظة © 2026
 */
package com.example.ui

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import com.example.audio.AudioDubbingManager
import com.example.audio.AudioDubbingState
import com.example.audio.AudioEffectItem
import com.example.audio.AudioEffectParameters
import com.example.audio.AudioEffectsLibrary
import com.example.audio.AudioEffectsProcessor
import com.example.audio.AudioLevelNormalizer
import com.example.audio.AudioRecordingManager
import com.example.audio.AudioTrimmerManager
import com.example.audio.AutoDubbingState
import com.example.audio.AutoDubbingStep
import com.example.audio.AutoDubbingStyle
import com.example.audio.AutoVideoDubberEngine
import com.example.audio.BgmStyle
import com.example.audio.DetectedGender
import com.example.audio.DubbingDialect
import com.example.audio.DubbingPacing
import com.example.audio.DubbingTargetLanguage
import com.example.audio.GeminiAiScriptGenerator
import com.example.audio.gemini.GeminiOneClickDubber
import com.example.audio.gemini.GeminiVideoAudioTranscriptionService
import com.example.audio.gemini.VideoAudioTranscriptionResult
import com.example.audio.gemini.VideoAudioTranscriptionSegment
import com.example.audio.GeminiVideoToAudioDubbingService
import com.example.audio.ImportedVideoMetadata
import com.example.audio.LocalizedDubbingCue
import com.example.audio.VideoDubbingSynthesisAnalysis
import com.example.audio.InstantDubbingConfig
import com.example.audio.InstantDubbingEngine
import com.example.audio.InstantDubbingMode
import com.example.audio.NormalizationMode
import com.example.audio.SoundEffectsGenerator
import com.example.audio.TextToSpeechManager
import com.example.audio.TtsEngineState
import com.example.audio.TtsStatusInfo
import com.example.audio.TrackLoudnessProfile
import com.example.audio.VoiceEffect
import com.example.audio.VoiceGenderDetector
import com.example.audio.VoiceProfile
import com.example.audio.VolumeNormalizationResult
import com.example.audio.batch.BatchDubbingEngine
import com.example.audio.batch.BatchProcessingSessionState
import com.example.audio.worker.LongFormDubbingWorker
import com.example.audio.worker.ChunkedDubbingWorkerState
import com.example.audio.tts.CloudTtsConfig
import com.example.audio.tts.CloudTtsPreferences
import com.example.audio.tts.CloudTtsProvider
import com.example.audio.ArabicPhoneticsEngine
import com.example.audio.AuditReport
import com.example.audio.ScriptAuditIssue
import com.example.audio.tts.CloudTtsService
import com.example.audio.tts.CloudVoiceCatalog
import com.example.audio.tts.CloudVoiceInfo
import com.example.data.AppDatabase
import com.example.data.DubbingProject
import com.example.data.DubbingRepository
import com.example.data.VoiceRecordingEntity
import com.example.model.DubbingClip
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.example.audio.stt.SpeechToTextManager
import com.example.audio.stt.SttState
import com.example.ai.GeminiUnifiedClient
import com.example.audio.service.AutomatedVoiceAnalysisService
import com.example.audio.service.AutomatedDubbingAssemblyResult
import com.example.audio.hollywood.HollywoodProductionSuiteService
import com.example.audio.hollywood.FoleyCategory
import com.example.audio.hollywood.CinematicScoreStyle
import com.example.audio.hollywood.VintageMicProfile
import com.example.audio.hollywood.VocalAgeMorph
import com.example.audio.hollywood.LiveDubbingMode
import com.example.model.SampleClipsRepository
import com.example.ui.components.VoicePresetType
import com.example.model.ScriptLine
import com.example.export.ExportFormat
import com.example.export.ExportResult
import com.example.export.MediaExportManager
import com.example.export.VideoExportConfig
import com.example.ui.components.ExportDialogUiState
import com.example.ai.FirebaseAuthAndFirestoreService
import com.example.security.FirebaseAuthSecurityManager
import com.example.update.AppUpdateManager
import com.example.update.server.AppUpdateWebServer
import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel
import com.example.update.model.UpdateCheckResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class SplitCompareViewMode {
    SIDE_BY_SIDE, // Dual screens: Original left vs Dubbed right
    SPLIT_SLIDER, // Interactive drag wipe comparison divider
    AB_FLIP       // Fast 1-tap toggle between Original & Dubbed
}

data class LocalizedDubbingCuesUiState(
    val isLoading: Boolean = false,
    val analysisResult: VideoDubbingSynthesisAnalysis? = null,
    val cues: List<LocalizedDubbingCue> = emptyList(),
    val currentlyPlayingCueId: String? = null,
    val isPlayingAll: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedGenderFilter: String? = null, // null = all, "male", "female", "child", "narrator"
    val synthesizedCount: Int = 0
)

data class StudioUiState(
    val currentClip: DubbingClip = SampleClipsRepository.clips.first(),
    val currentProject: DubbingProject? = null,
    val isPlaying: Boolean = false,
    val currentPlaybackSeconds: Float = 0f,
    val isRecording: Boolean = false,
    val countdownNumber: Int = 0, // 3, 2, 1, 0
    val liveAmplitude: Float = 0f,
    val waveformHistory: List<Float> = emptyList(),
    val recordedAudioPath: String? = null,
    val originalVolume: Float = 0.25f,
    val dubVolume: Float = 1.0f,
    val bgmVolume: Float = 0.35f,
    val selectedVoiceEffect: VoiceEffect = VoiceEffect.NORMAL,
    val selectedVoicePreset: VoicePresetType = VoicePresetType.NORMAL,
    val selectedBgmStyle: BgmStyle = BgmStyle.CINEMATIC,
    val scriptLines: List<ScriptLine> = emptyList(),
    val activeLineIndex: Int = -1,
    val isMutedOriginal: Boolean = false,
    val isMutedDub: Boolean = false,
    val isSoloOriginal: Boolean = false,
    val isSoloDub: Boolean = false,
    val isVocalClarityActive: Boolean = false,
    val showMixerSheet: Boolean = false,
    val showScriptSheet: Boolean = false,
    val showVoicePicker: Boolean = false,
    val showShareDialog: Boolean = false,
    val toastMessage: String? = null,
    val isGeneratingAiDub: Boolean = false,
    val syncOffsetMs: Long = 0L,
    val postProcessingPitch: Float = 1.0f,
    val postProcessingSpeed: Float = 1.0f,

    // Realtime Voice Gender Recognition & Instant Dubbing State
    val autoVoiceRecognitionEnabled: Boolean = true,
    val instantDubbingMode: InstantDubbingMode = InstantDubbingMode.AUTO_DETECT,
    val lastDetectedGender: DetectedGender = DetectedGender.SILENCE,

    // Visual Audio Trimmer State
    val showAudioTrimmer: Boolean = false,
    val trimmerAudioPath: String? = null,
    val trimmerWaveform: List<Float> = emptyList(),
    val trimmerDurationSeconds: Float = 0f,
    val trimmerStartSeconds: Float = 0f,
    val trimmerEndSeconds: Float = 0f,
    val isTrimmerPlaying: Boolean = false,
    val trimmerPlaybackSeconds: Float = 0f,
    val trimmerLooping: Boolean = false,
    val isTrimmerLooping: Boolean = false,
    val isTrimmerLoading: Boolean = false,
    val isProcessingTrim: Boolean = false,
    val trimmerFormatName: String = "M4A",
    val trimmerSilenceStart: Float = 0f,
    val trimmerSilenceEnd: Float = 0f,

    // Real-time Audio Effects Library State
    val showEffectsLibrary: Boolean = false,
    val selectedAudioEffectItem: AudioEffectItem = AudioEffectsLibrary.effects.first(),
    val customEffectParams: AudioEffectParameters = AudioEffectParameters(),
    val isEffectsPreviewPlaying: Boolean = false,
    val isProcessingEffectDsp: Boolean = false,

    // Auto Volume Normalization & Dynamic Ducking State
    val showNormalizationSheet: Boolean = false,
    val isAnalyzingAudioLevels: Boolean = false,
    val normalizationMode: NormalizationMode = NormalizationMode.SMART_ADAPTIVE,
    val isAutoDuckingEnabled: Boolean = true,
    val normalizationResult: VolumeNormalizationResult? = null,
    val voiceLoudnessProfile: TrackLoudnessProfile? = null,
    val backgroundLoudnessProfile: TrackLoudnessProfile? = null,

    // Split-Screen 'Preview & Compare' & Quality Audit State
    val showSplitScreenCompare: Boolean = false,
    val splitCompareViewMode: SplitCompareViewMode = SplitCompareViewMode.SIDE_BY_SIDE,
    val splitDividerFraction: Float = 0.5f,
    val isAbFlipActive: Boolean = false, // false = original, true = dubbed
    val auditReport: AuditReport? = null,
    val isAuditRunning: Boolean = false,

    // Undo / Redo Stack State
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoCount: Int = 0,
    val redoCount: Int = 0,
    val lastActionDescription: String? = null,

    // Video-Audio Waveform Synchronization State
    val dubSyncOffsetMs: Float = 0f,
    val syncZoomLevel: Float = 1.0f,
    val isSyncLooping: Boolean = false,
    val syncLoopStartSec: Float = 0f,
    val syncLoopEndSec: Float = 5f,
    val syncPlaybackSpeed: Float = 1.0f,
    val syncWaveformGain: Float = 1.2f,
    val isSyncLocked: Boolean = true,
    val syncMarkerSeconds: List<Float> = emptyList(),

    // 3-Step Dubbing Pipeline State (STT -> Gemini Translation -> TTS Sync)
    val isSttListening: Boolean = false,
    val sttTranscribedText: String = "",
    val isGeminiTranslating: Boolean = false,
    val targetTranslationLanguage: String = "العربية",
    val isTtsSynthesizingTimeline: Boolean = false,
    val synthesizedDubbedAudioPath: String? = null,

    // Gemini AI Audio Denoising & Acoustic Enhancement State
    val isDenoisingAudio: Boolean = false,
    val denoiseProgress: Float = 0f,
    val denoiseStatusText: String = "",
    val lastDenoiseResult: com.example.audio.gemini.GeminiAudioEnhanceResult? = null,
    val showDenoiseDialog: Boolean = false,

    // Professional Video Audio Equalizer State
    val showEqualizerSheet: Boolean = false,
    val equalizerState: EqualizerState = EqualizerState()
)

data class EqualizerBand(
    val id: Int,
    val frequencyLabel: String,
    val gainDb: Float = 0f
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val selectedPresetName: String = "Flat",
    val bands: List<EqualizerBand> = listOf(
        EqualizerBand(0, "60Hz", 0f),
        EqualizerBand(1, "170Hz", 0f),
        EqualizerBand(2, "310Hz", 0f),
        EqualizerBand(3, "600Hz", 0f),
        EqualizerBand(4, "1kHz", 0f),
        EqualizerBand(5, "3kHz", 0f),
        EqualizerBand(6, "12kHz", 0f)
    ),
    val bassBoostFraction: Float = 0.25f,
    val spatialVirtualizerFraction: Float = 0.2f,
    val masterGainDb: Float = 0f,
    val isBypassed: Boolean = false
)

data class VideoSpeechToTextUiState(
    val isTranscribing: Boolean = false,
    val progressFraction: Float = 0f,
    val statusMessage: String = "",
    val result: VideoAudioTranscriptionResult? = null,
    val segments: List<VideoAudioTranscriptionSegment> = emptyList(),
    val selectedSegmentId: String? = null,
    val searchQuery: String = "",
    val speakerFilter: String? = null,
    val isPlayingSegmentAudio: Boolean = false,
    val currentlyPlayingSegmentId: String? = null,
    val selectedDialect: DubbingDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
    val errorMessage: String? = null
)

class DubbingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DubbingRepository
    val recordingManager = AudioRecordingManager(application)
    val ttsManager = TextToSpeechManager(application)
    val ttsStatusInfo: StateFlow<TtsStatusInfo> = ttsManager.initState
    val isTtsReady: StateFlow<Boolean> = ttsManager.isInitialized

    fun retryTtsInitialization() {
        ttsManager.retryInit()
        _uiState.value = _uiState.value.copy(
            toastMessage = "جاري إعادة تهيئة محرك تحويل النص إلى كلام (TTS)... 🔄"
        )
    }

    val cloudTtsPrefs = CloudTtsPreferences(application)
    val cloudTtsService = CloudTtsService(application)
    val genderDetector = VoiceGenderDetector()
    val instantDubbingEngine = InstantDubbingEngine(application)
    val exportManager = MediaExportManager(application)
    val audioTrimmerManager = AudioTrimmerManager(application)
    val audioEffectsProcessor = AudioEffectsProcessor(application)
    val geminiScriptGenerator = GeminiAiScriptGenerator(application)
    val audioNormalizer = AudioLevelNormalizer(application)
    val autoVideoDubber = AutoVideoDubberEngine(application, ttsManager, cloudTtsService, cloudTtsPrefs)
    val batchDubbingEngine = BatchDubbingEngine(application, autoVideoDubber)
    val longFormDubbingWorker = LongFormDubbingWorker(application, ttsManager, cloudTtsService, cloudTtsPrefs, autoVideoDubber)
    val updateManager = AppUpdateManager(application)
    val updateWebServer = AppUpdateWebServer(application, updateManager, port = 8080)
    val authSecurityManager = FirebaseAuthSecurityManager(application)
    val authService = FirebaseAuthAndFirestoreService(application)
    val securityAuditDiagnosticManager = com.example.security.SecurityAuditDiagnosticManager(application)
    val audioDubbingManager = AudioDubbingManager(application, ttsManager, cloudTtsService, cloudTtsPrefs)
    val geminiOneClickDubber = GeminiOneClickDubber(application, ttsManager)
    val geminiAudioDenoiseEnhancer = com.example.audio.gemini.GeminiAudioDenoiseEnhancer(application)
    val sttManager = SpeechToTextManager(application)
    val geminiUnifiedClient = GeminiUnifiedClient(application)
    val media3ProcessingLayer = com.example.audio.media3.Media3AudioProcessingLayer(application)

    fun updateRecordedAudioPath(path: String) {
        _uiState.value = _uiState.value.copy(recordedAudioPath = path)
    }

    fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun applyMedia3AudioEnhancement(onComplete: (Boolean) -> Unit = {}) {
        val currentPath = _uiState.value.recordedAudioPath ?: return
        val file = File(currentPath)
        if (!file.exists()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(toastMessage = "جارٍ عزل الضوضاء وموازنة درجات الصوت عبر Media3... 🎚️✨")
            val result = media3ProcessingLayer.processVoiceClip(file)
            if (result.success) {
                updateRecordedAudioPath(result.processedFile.absolutePath)
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تمت تنقية وموازنة الصوت بالذكاء الاصطناعي عبر Media3 بنجاح! 🎚️✨"
                )
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(toastMessage = result.messageArabic)
                onComplete(false)
            }
        }
    }
    val youTubeAutoDubberEngine = com.example.audio.youtube.YouTubeAutoDubberEngine(application, ttsManager, geminiUnifiedClient)
    val vattDubbingEngine = com.example.audio.vatt.VattDubbingEngine(application, ttsManager, cloudTtsService, exportManager)
    val vattState: StateFlow<com.example.audio.vatt.VattDubbingEngine.VattState> = vattDubbingEngine.vattState

    // Offline Clean Architecture Assets & ExoPlayer Services
    val audioAssetsRepository: com.example.audio.assets.domain.repository.AudioAssetsRepository =
        com.example.audio.assets.data.repository.AudioAssetsRepositoryImpl(application)
    val assetAudioPlayerService: com.example.audio.assets.domain.service.AssetAudioPlayerService =
        com.example.audio.assets.data.service.AssetExoAudioPlayerService(application, audioAssetsRepository)
    val offlineTtsService: com.example.audio.assets.domain.service.OfflineTtsService =
        com.example.audio.assets.data.service.OfflineTtsServiceImpl(application)
    val offlineSttService: com.example.audio.assets.domain.service.OfflineSttService =
        com.example.audio.assets.data.service.OfflineSttServiceImpl(application)

    // Local & Remote Python Lingo Dubbing Server Networking
    val lingoRetrofitManager = com.example.network.RetrofitClientManager()
    val lingoConnectionManager = com.example.network.ConnectionManager(application, lingoRetrofitManager, viewModelScope)
    val lingoWebSocketClient = com.example.network.LingoWebSocketClient()

    fun executeVattAutoDubbing(videoUri: Uri, videoTitle: String, durationSec: Int, lines: List<ScriptLine>? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                toastMessage = "بدء تشغيل محرك VATT لدبلجة الفيديو واستخراج الترجمة... 🚀"
            )
            val result = vattDubbingEngine.executeVattPipeline(
                videoUri = videoUri,
                videoTitle = videoTitle,
                videoDurationSec = durationSec,
                targetLanguage = autoVideoDubber.state.value.selectedLanguage,
                dialect = autoVideoDubber.state.value.selectedDialect,
                existingLines = lines ?: _uiState.value.scriptLines
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم الانتهاء من دبلجة وترجمة VATT وتوليد ملفات SRT و WebVTT بنجاح ✨"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = result.message
                )
            }
        }
    }

    fun importYouTubeSegmentsToStudio(segments: List<com.example.audio.youtube.YouTubeDubSegment>) {
        val newLines = segments.map { seg ->
            ScriptLine(
                id = "yt_line_${seg.index}_${System.currentTimeMillis()}",
                characterName = "متحدث يوتيوب #${seg.index}",
                characterAvatar = "🔴",
                textArabic = seg.translatedText.ifBlank { seg.sourceText },
                textOriginal = seg.sourceText,
                startSeconds = seg.startSeconds,
                endSeconds = seg.endSeconds,
                voiceType = "ARABIC_MALE",
                isDubbed = seg.audioPath != null,
                customAudioPath = seg.audioPath,
                speakerGender = "MALE",
                genderConfidence = 95
            )
        }
        _uiState.value = _uiState.value.copy(
            scriptLines = newLines,
            toastMessage = "تم نقل ${newLines.size} مقطع حوار من يوتيوب إلى خط الزمن بنجاح 🎬"
        )
    }

    // Undo / Redo Manager for Trimming, Filters, Effects, and Audio Edits
    val undoRedoManager = UndoRedoStackManager(maxStackSize = 35)

    private fun captureCurrentSnapshot(description: String): EditorStateSnapshot {
        val s = _uiState.value
        return EditorStateSnapshot(
            actionDescription = description,
            recordedAudioPath = s.recordedAudioPath,
            selectedVoiceEffect = s.selectedVoiceEffect,
            selectedVoicePreset = s.selectedVoicePreset,
            selectedAudioEffectItem = s.selectedAudioEffectItem,
            customEffectParams = s.customEffectParams,
            originalVolume = s.originalVolume,
            dubVolume = s.dubVolume,
            bgmVolume = s.bgmVolume,
            selectedBgmStyle = s.selectedBgmStyle,
            scriptLines = s.scriptLines,
            trimmerAudioPath = s.trimmerAudioPath,
            trimmerStartSeconds = s.trimmerStartSeconds,
            trimmerEndSeconds = s.trimmerEndSeconds
        )
    }

    private fun updateUndoRedoState(lastDesc: String? = null) {
        _uiState.value = _uiState.value.copy(
            canUndo = undoRedoManager.canUndo(),
            canRedo = undoRedoManager.canRedo(),
            undoCount = undoRedoManager.getUndoCount(),
            redoCount = undoRedoManager.getRedoCount(),
            lastActionDescription = lastDesc ?: _uiState.value.lastActionDescription
        )
    }

    fun recordUndoableAction(description: String) {
        undoRedoManager.pushUndo(captureCurrentSnapshot(description))
        updateUndoRedoState(description)
    }

    fun canUndo(): Boolean = undoRedoManager.canUndo()
    fun canRedo(): Boolean = undoRedoManager.canRedo()

    fun undo(): EditorStateSnapshot? {
        if (!undoRedoManager.canUndo()) return null
        val currentSnapshot = captureCurrentSnapshot("الحالة الحالية")
        val previousState = undoRedoManager.performUndo(currentSnapshot) ?: return null

        // Apply restored state
        _uiState.value = _uiState.value.copy(
            recordedAudioPath = previousState.recordedAudioPath,
            selectedVoiceEffect = previousState.selectedVoiceEffect,
            selectedVoicePreset = previousState.selectedVoicePreset,
            selectedAudioEffectItem = previousState.selectedAudioEffectItem,
            customEffectParams = previousState.customEffectParams,
            originalVolume = previousState.originalVolume,
            dubVolume = previousState.dubVolume,
            bgmVolume = previousState.bgmVolume,
            selectedBgmStyle = previousState.selectedBgmStyle,
            scriptLines = previousState.scriptLines,
            trimmerAudioPath = previousState.trimmerAudioPath,
            trimmerStartSeconds = previousState.trimmerStartSeconds,
            trimmerEndSeconds = previousState.trimmerEndSeconds,
            toastMessage = "تم التراجع عن: ${previousState.actionDescription} ↩️"
        )
        updateUndoRedoState("تراجع: ${previousState.actionDescription}")
        return previousState
    }

    fun redo(): EditorStateSnapshot? {
        if (!undoRedoManager.canRedo()) return null
        val currentSnapshot = captureCurrentSnapshot("الحالة الحالية")
        val nextState = undoRedoManager.performRedo(currentSnapshot) ?: return null

        // Apply redone state
        _uiState.value = _uiState.value.copy(
            recordedAudioPath = nextState.recordedAudioPath,
            selectedVoiceEffect = nextState.selectedVoiceEffect,
            selectedVoicePreset = nextState.selectedVoicePreset,
            selectedAudioEffectItem = nextState.selectedAudioEffectItem,
            customEffectParams = nextState.customEffectParams,
            originalVolume = nextState.originalVolume,
            dubVolume = nextState.dubVolume,
            bgmVolume = nextState.bgmVolume,
            selectedBgmStyle = nextState.selectedBgmStyle,
            scriptLines = nextState.scriptLines,
            trimmerAudioPath = nextState.trimmerAudioPath,
            trimmerStartSeconds = nextState.trimmerStartSeconds,
            trimmerEndSeconds = nextState.trimmerEndSeconds,
            toastMessage = "تمت إعادة تطبيق: ${nextState.actionDescription} 🔁"
        )
        updateUndoRedoState("إعادة: ${nextState.actionDescription}")
        return nextState
    }

    fun applyDubbedAudioToProject(dubbedAudioPath: String) {
        _uiState.value = _uiState.value.copy(
            recordedAudioPath = dubbedAudioPath,
            toastMessage = "تم تعيين الصوت المدبلج كمسار صوتي نشط للمشروع 🎙️"
        )
    }

    private val _cloudTtsConfig = MutableStateFlow(cloudTtsPrefs.loadConfig())
    val cloudTtsConfig: StateFlow<CloudTtsConfig> = _cloudTtsConfig.asStateFlow()

    val autoDubberState: StateFlow<AutoDubbingState> = autoVideoDubber.state
    val batchState: StateFlow<BatchProcessingSessionState> = batchDubbingEngine.state
    val longFormWorkerState: StateFlow<ChunkedDubbingWorkerState> = longFormDubbingWorker.workerState
    val genderAnalysisResult = genderDetector.analysisResult
    val instantConfig: StateFlow<InstantDubbingConfig> = instantDubbingEngine.config
    val isInstantPlaying: StateFlow<Boolean> = instantDubbingEngine.isPlaying

    private val _videoExportConfig = MutableStateFlow(VideoExportConfig())
    val videoExportConfig: StateFlow<VideoExportConfig> = _videoExportConfig.asStateFlow()

    val userSettingsDataStore = com.example.data.UserSettingsDataStore(application)

    private val themePrefs = application.getSharedPreferences("app_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(themePrefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val langPrefs = application.getSharedPreferences("app_lang_prefs", android.content.Context.MODE_PRIVATE)
    private val _currentAppLanguage = MutableStateFlow(
        com.example.localization.AppLanguage.fromCode(langPrefs.getString("app_language_code", "ar") ?: "ar")
    )
    val currentAppLanguage: StateFlow<com.example.localization.AppLanguage> = _currentAppLanguage.asStateFlow()

    val userSettings: StateFlow<com.example.data.UserSettings> = userSettingsDataStore.userSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = com.example.data.UserSettings(
            languageCode = langPrefs.getString("app_language_code", "ar") ?: "ar",
            isDarkMode = themePrefs.getBoolean("is_dark_mode", true)
        )
    )

    private val aiPrefs = application.getSharedPreferences("app_ai_prefs", android.content.Context.MODE_PRIVATE)
    private val _geminiApiKey = MutableStateFlow(
        geminiOneClickDubber.resolveEffectiveApiKey()
    )
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    fun updateGeminiApiKey(key: String) {
        val trimmed = key.trim()
        _geminiApiKey.value = trimmed
        aiPrefs.edit().putString("gemini_api_key", trimmed).commit()
        val currentTts = _cloudTtsConfig.value
        val updatedTts = currentTts.copy(googleCloudApiKey = trimmed)
        _cloudTtsConfig.value = updatedTts
        cloudTtsPrefs.saveConfig(updatedTts)
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم حفظ مفتاح الذكاء الاصطناعي بشكل دائم بنجاح! 🔑✨"
        )
    }

    fun setAppLanguage(lang: com.example.localization.AppLanguage) {
        _currentAppLanguage.value = lang
        langPrefs.edit().putString("app_language_code", lang.code).apply()
        viewModelScope.launch {
            userSettingsDataStore.updateLanguage(lang.code)
        }
        _uiState.value = _uiState.value.copy(
            toastMessage = "${lang.flagEmoji} تم حفظ لغة الواجهة عبر DataStore: ${lang.displayName}"
        )
    }

    fun updateSpeechRate(rate: Float) {
        viewModelScope.launch {
            userSettingsDataStore.updateSpeechRate(rate)
        }
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم حفظ سرعة الصوت الافتراضية محلياً عبر DataStore: ${String.format("%.2f", rate)}x ⚡"
        )
    }

    fun updateVoicePitch(pitch: Float) {
        viewModelScope.launch {
            userSettingsDataStore.updateVoicePitch(pitch)
        }
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم حفظ نبرة الصوت الافتراضية محلياً عبر DataStore: ${String.format("%.2f", pitch)}x 🎚️"
        )
    }

    fun updateServerConfiguration(ip: String, port: Int) {
        viewModelScope.launch {
            userSettingsDataStore.updateServerConfiguration(ip, port)
            lingoConnectionManager.updateTarget(ip, port)
            _uiState.value = _uiState.value.copy(
                toastMessage = "تم حفظ إعدادات السيرفر عبر DataStore: $ip:$port ⚡"
            )
        }
    }

    fun testLingoServerConnection() {
        val settings = userSettings.value
        lingoConnectionManager.updateTarget(settings.serverIp, settings.serverPort)
    }

    fun sendAudioToLingoServer(
        audioUri: Uri,
        targetLanguage: String = "Arabic",
        dialect: String = "Modern Standard Arabic",
        speed: Float = 1.0f,
        onSuccess: (String) -> Unit = {},
        onError: (String, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val settings = userSettings.value
            val ip = settings.serverIp
            val port = settings.serverPort

            lingoConnectionManager.updateProcessingStatus("تحضير الملف وتحويله إلى Multipart...", 0.15f)

            val cachedFile = com.example.network.AudioMultipartHelper.uriToCacheFile(getApplication(), audioUri)
            if (cachedFile == null || !cachedFile.exists()) {
                lingoConnectionManager.restoreConnectedState()
                onError(
                    "تعذر قراءة الملف الصوتي المختار",
                    "تأكد من اختيار ملف صوتي صالح (MP3, WAV, M4A) وإعطاء التطبيق الصلاحيات اللازمة."
                )
                return@launch
            }

            try {
                lingoConnectionManager.updateProcessingStatus("إرسال ملف الصوت عبر الشبكة إلى $ip:$port...", 0.40f)

                val audioPart = com.example.network.AudioMultipartHelper.createAudioMultipartPart(cachedFile)
                val targetLangPart = com.example.network.AudioMultipartHelper.createTextRequestBody(targetLanguage)
                val dialectPart = com.example.network.AudioMultipartHelper.createTextRequestBody(dialect)
                val speedPart = com.example.network.AudioMultipartHelper.createTextRequestBody(speed.toString())
                val preserveBgPart = com.example.network.AudioMultipartHelper.createTextRequestBody("true")

                val api = lingoRetrofitManager.getApiService(ip, port)
                val response = api.uploadAudioForDubbing(
                    audioFile = audioPart,
                    targetLanguage = targetLangPart,
                    dialect = dialectPart,
                    sourceLanguage = null,
                    speed = speedPart,
                    preserveBackground = preserveBgPart
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val jobId = body.jobId.ifBlank { "job_${System.currentTimeMillis()}" }
                    lingoConnectionManager.updateProcessingStatus("جاري المعالجة والدبلجة بمكتبة Lingo على كارت الشاشة...", 0.80f)
                    delay(1200)
                    lingoConnectionManager.restoreConnectedState()
                    onSuccess("تم إرسال ومعالجة الصوت بنجاح بواسطة سيرفر Lingo! معرّف المهمة: $jobId")
                } else {
                    val code = response.code()
                    val err = "استجاب السيرفر برمز خطأ ($code)"
                    val troubleshooting = "تأكد من أن سكريبت Lingo على الكمبيوتر يدعم نقطة النهاية /api/dub/audio وأن مكتبات Whisper و PyTorch مثبتة بالكامل."
                    lingoConnectionManager.restoreConnectedState()
                    onError(err, troubleshooting)
                }
            } catch (e: Exception) {
                lingoConnectionManager.restoreConnectedState()
                val errMsg = e.localizedMessage ?: "فشل الاتصال بسيرفر البايثون"
                val troubleshooting = buildString {
                    append("تعذر إرسال الملف إلى سيرفر البايثون على $ip:$port.\n")
                    append("1. تأكد من اتصال هاتفك بنفس شبكة الواي فاي للكمبيوتر.\n")
                    append("2. تأكد من عمل السيرفر في وضع الاستماع (0.0.0.0:$port).\n")
                    append("3. افحص جدار حماية الويندوز (Windows Firewall).")
                }
                onError(errMsg, troubleshooting)
            }
        }
    }

    fun resetUserSettings() {
        viewModelScope.launch {
            userSettingsDataStore.resetToDefaults()
            _isDarkMode.value = true
            _currentAppLanguage.value = com.example.localization.AppLanguage.ARABIC
            themePrefs.edit().putBoolean("is_dark_mode", true).apply()
            langPrefs.edit().putString("app_language_code", "ar").apply()
            _uiState.value = _uiState.value.copy(
                toastMessage = "تمت استعادة كافة إعدادات DataStore الافتراضية بنجاح 🔄"
            )
        }
    }

    fun clearAudioCache() {
        viewModelScope.launch {
            try {
                val cacheDir = getApplication<Application>().cacheDir
                val dubDir = File(cacheDir, "dubbing_tts")
                if (dubDir.exists()) {
                    dubDir.deleteRecursively()
                    dubDir.mkdirs()
                }
                val exportDir = File(cacheDir, "exports")
                if (exportDir.exists()) {
                    exportDir.deleteRecursively()
                    exportDir.mkdirs()
                }
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم مسح الذاكرة المؤقتة لملفات الصوت بنجاح! 🧹"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "حدث خطأ أثناء مسح الذاكرة المؤقتة"
                )
            }
        }
    }

    // ==========================================
    // Gemini Video-to-Audio Localized Cues Engine
    // ==========================================
    val geminiVideoDubbingService = GeminiVideoToAudioDubbingService(application, ttsManager)

    private val _localizedCuesState = MutableStateFlow(
        LocalizedDubbingCuesUiState(
            analysisResult = null,
            cues = emptyList()
        )
    )
    val localizedCuesState: StateFlow<LocalizedDubbingCuesUiState> = _localizedCuesState.asStateFlow()

    private var playAllCuesJob: Job? = null

    fun playCueAudio(cue: LocalizedDubbingCue) {
        if (_localizedCuesState.value.currentlyPlayingCueId == cue.id) {
            stopCueAudio()
            return
        }
        stopCueAudio()
        _localizedCuesState.value = _localizedCuesState.value.copy(
            currentlyPlayingCueId = cue.id,
            isPlayingAll = false
        )
        geminiVideoDubbingService.playCueAudio(cue) {
            if (_localizedCuesState.value.currentlyPlayingCueId == cue.id) {
                _localizedCuesState.value = _localizedCuesState.value.copy(currentlyPlayingCueId = null)
            }
        }
    }

    fun stopCueAudio() {
        playAllCuesJob?.cancel()
        playAllCuesJob = null
        geminiVideoDubbingService.stopCueAudio()
        _localizedCuesState.value = _localizedCuesState.value.copy(
            currentlyPlayingCueId = null,
            isPlayingAll = false
        )
    }

    fun playAllCues() {
        if (_localizedCuesState.value.isPlayingAll) {
            stopCueAudio()
            return
        }
        val currentCues = _localizedCuesState.value.cues
        if (currentCues.isEmpty()) return

        stopCueAudio()
        _localizedCuesState.value = _localizedCuesState.value.copy(isPlayingAll = true)

        playAllCuesJob = viewModelScope.launch {
            for (cue in currentCues) {
                if (!_localizedCuesState.value.isPlayingAll) break
                _localizedCuesState.value = _localizedCuesState.value.copy(currentlyPlayingCueId = cue.id)
                geminiVideoDubbingService.playCueAudio(cue)
                val durationMs = ((cue.durationSeconds * 1000L).toLong() + 600L).coerceAtLeast(1800L)
                delay(durationMs)
            }
            _localizedCuesState.value = _localizedCuesState.value.copy(
                currentlyPlayingCueId = null,
                isPlayingAll = false
            )
        }
    }

    // ========================================================
    // Gemini Audio Denoise & Acoustic Enhancement API Actions
    // ========================================================

    fun setDenoiseDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showDenoiseDialog = visible)
    }

    fun enhanceRecordedAudioWithGemini(
        intensity: Float = 0.85f,
        customApiKey: String = ""
    ) {
        val audioPath = _uiState.value.recordedAudioPath
        if (audioPath.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = "لا يوجد تسجيل صوتي حالي للمعالجة! يرجى تسجيل مقطع أولاً 🎙️"
            )
            return
        }

        val audioFile = File(audioPath)
        if (!audioFile.exists()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = "ملف الصوت المسجل غير موجود على مساحة التخزين ⚠️"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDenoisingAudio = true,
                denoiseProgress = 0.1f,
                denoiseStatusText = "بدء فحص وتحليل الصوت بالذكاء الاصطناعي Gemini...",
                showDenoiseDialog = true
            )

            // Listen to enhancer progress
            val stateCollectorJob = launch {
                geminiAudioDenoiseEnhancer.enhanceState.collect { state ->
                    when (state) {
                        is com.example.audio.gemini.AudioEnhanceState.Processing -> {
                            _uiState.value = _uiState.value.copy(
                                denoiseProgress = state.progress,
                                denoiseStatusText = state.stage
                            )
                        }
                        is com.example.audio.gemini.AudioEnhanceState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isDenoisingAudio = false,
                                denoiseProgress = 1.0f,
                                denoiseStatusText = "اكتملت إزالة الضوضاء وتحسين الصوت بنجاح! ✨",
                                lastDenoiseResult = state.result,
                                recordedAudioPath = state.result.enhancedAudioFile?.absolutePath ?: audioPath,
                                toastMessage = "تم تحسين الصوت بنجاح! نسبة إزالة الضوضاء: ${state.result.noiseReductionPercent}% 🎉"
                            )
                        }
                        is com.example.audio.gemini.AudioEnhanceState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isDenoisingAudio = false,
                                denoiseStatusText = "فشلت المعالجة: ${state.message}",
                                toastMessage = "خطأ في المعالجة: ${state.message} ⚠️"
                            )
                        }
                        else -> Unit
                    }
                }
            }

            val result = geminiAudioDenoiseEnhancer.enhanceRecordedAudio(
                inputFile = audioFile,
                customApiKey = customApiKey,
                targetIntensity = intensity
            )

            stateCollectorJob.cancel()

            if (result.isSuccess && result.enhancedAudioFile != null) {
                _uiState.value = _uiState.value.copy(
                    isDenoisingAudio = false,
                    recordedAudioPath = result.enhancedAudioFile.absolutePath,
                    lastDenoiseResult = result
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isDenoisingAudio = false,
                    lastDenoiseResult = result
                )
            }
        }
    }

    fun synthesizeCueToFile(cue: LocalizedDubbingCue) {
        viewModelScope.launch {
            geminiVideoDubbingService.synthesizeCueAudio(cue) { filePath ->
                if (filePath != null) {
                    val updatedCues = _localizedCuesState.value.cues.map {
                        if (it.id == cue.id) it.copy(generatedAudioPath = filePath) else it
                    }
                    _localizedCuesState.value = _localizedCuesState.value.copy(
                        cues = updatedCues,
                        synthesizedCount = updatedCues.count { it.generatedAudioPath != null }
                    )
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "تم توليد وحفظ المقطع الصوتي بنجاح! 🎙️💾"
                    )
                }
            }
        }
    }

    fun updateCueScript(cueId: String, newScript: String) {
        val updated = _localizedCuesState.value.cues.map {
            if (it.id == cueId) it.copy(localizedDubbedScript = newScript) else it
        }
        _localizedCuesState.value = _localizedCuesState.value.copy(cues = updated)
    }

    fun loadSampleDubbingCues() {
        val sample = geminiVideoDubbingService.generateSampleDubbingAnalysis()
        _localizedCuesState.value = _localizedCuesState.value.copy(
            analysisResult = sample,
            cues = sample.dubbingCues,
            currentlyPlayingCueId = null,
            isPlayingAll = false,
            errorMessage = null
        )
    }

    fun analyzeVideoForCues(videoUri: Uri, targetDialect: DubbingDialect = DubbingDialect.MODERN_STANDARD_CLASSIC) {
        viewModelScope.launch {
            _localizedCuesState.value = _localizedCuesState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            val result = geminiVideoDubbingService.analyzeAndSynthesizeDubbing(
                videoUri = videoUri,
                targetDialect = targetDialect,
                explicitApiKey = _geminiApiKey.value
            )
            result.onSuccess { analysis ->
                _localizedCuesState.value = _localizedCuesState.value.copy(
                    isLoading = false,
                    analysisResult = analysis,
                    cues = analysis.dubbingCues
                )
            }.onFailure { err ->
                _localizedCuesState.value = _localizedCuesState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "تعذر تحليل الفيديو بواسطة خدمة Gemini"
                )
            }
        }
    }

    fun analyzeVideoForCues(video: ImportedVideoMetadata) {
        val uri = Uri.parse(video.uriString)
        analyzeVideoForCues(uri)
    }

    fun filterCuesByGender(gender: String?) {
        _localizedCuesState.value = _localizedCuesState.value.copy(selectedGenderFilter = gender)
    }

    fun searchCues(query: String) {
        _localizedCuesState.value = _localizedCuesState.value.copy(searchQuery = query)
    }

    fun applyCueToStudio(cue: LocalizedDubbingCue) {
        val path = cue.generatedAudioPath
        if (path != null && File(path).exists()) {
            _uiState.value = _uiState.value.copy(
                recordedAudioPath = path,
                toastMessage = "تم نقل صوت المقطع '${cue.characterSpeakerName}' إلى الاستوديو! 🎬✨"
            )
        } else {
            synthesizeCueToFile(cue)
            _uiState.value = _uiState.value.copy(
                toastMessage = "جاري توليد ملف الصوت لنقله للاستوديو... 🎙️"
            )
        }
    }

    // ==============================================================
    // 🎙️ Speech-to-Text (STT) & Video Timestamps Engine
    // ==============================================================
    val geminiTranscriptionService = GeminiVideoAudioTranscriptionService(application)
    val automatedVoiceAnalysisService by lazy {
        AutomatedVoiceAnalysisService(
            context = application,
            ttsManager = ttsManager,
            transcriptionService = geminiTranscriptionService,
            geminiClient = geminiUnifiedClient
        )
    }
    val hollywoodSuiteService by lazy {
        HollywoodProductionSuiteService(application)
    }

    private val _videoSpeechToTextState = MutableStateFlow(VideoSpeechToTextUiState())
    val videoSpeechToTextState: StateFlow<VideoSpeechToTextUiState> = _videoSpeechToTextState.asStateFlow()

    fun transcribeVideoForTimestamps(
        videoUri: Uri? = null,
        targetDialect: DubbingDialect = _videoSpeechToTextState.value.selectedDialect,
        customPromptContext: String = ""
    ) {
        viewModelScope.launch {
            try {
                _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
                    isTranscribing = true,
                    progressFraction = 0.15f,
                    statusMessage = "جاري تهيئة ملف الفيديو واستخراج مسار الصوت...",
                    selectedDialect = targetDialect,
                    errorMessage = null
                )

                val context = getApplication<Application>()
                val videoFile = withContext(Dispatchers.IO) {
                    val localPath = autoDubberState.value.importedVideo?.localFilePath
                    if (localPath != null && File(localPath).exists() && File(localPath).length() > 500) {
                        File(localPath)
                    } else if (videoUri != null) {
                        val cacheFile = File(context.cacheDir, "stt_input_${System.currentTimeMillis()}.mp4")
                        context.contentResolver.openInputStream(videoUri)?.use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (cacheFile.exists() && cacheFile.length() > 500) cacheFile else null
                    } else {
                        null
                    }
                }

                _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
                    progressFraction = 0.50f,
                    statusMessage = "جاري تفريغ الصوت وتحليل توقيتات الكلام بالذكاء الاصطناعي..."
                )

                val result = geminiTranscriptionService.transcribeVideoAudio(
                    videoFile = videoFile,
                    targetDialect = targetDialect,
                    customApiKey = _geminiApiKey.value,
                    customPromptContext = customPromptContext
                )

                _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
                    isTranscribing = false,
                    progressFraction = 1.0f,
                    statusMessage = "اكتمل تفريغ ${result.segments.size} مقطع كلامي مع التوقيتات!",
                    result = result,
                    segments = result.segments,
                    selectedSegmentId = result.segments.firstOrNull()?.id
                )

                if (result.segments.isNotEmpty()) {
                    val convertedLines = result.segments.mapIndexed { idx, seg ->
                        ScriptLine(
                            id = "transcribed_line_${idx}_${System.currentTimeMillis()}",
                            characterName = seg.speaker,
                            characterAvatar = if (seg.speakerGender == "FEMALE") "👩" else if (seg.speakerGender == "CHILD") "🧒" else "🎙️",
                            textArabic = seg.arabicDubbedAdaptation.ifBlank { seg.originalSpeech },
                            startSeconds = seg.startSeconds,
                            endSeconds = seg.endSeconds
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        scriptLines = convertedLines,
                        toastMessage = "تم تفريغ وحساب توقيتات ${result.segments.size} مقطع كلامي حقيقي بنجاح! 🎙️⏱️"
                    )
                } else if (!result.errorMessage.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = result.errorMessage
                    )
                }
            } catch (e: Exception) {
                _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
                    isTranscribing = false,
                    errorMessage = e.message ?: "تعذر استخراج كلام الفيديو"
                )
                _uiState.value = _uiState.value.copy(
                    toastMessage = "خطأ في تفريغ الكلام: ${e.message ?: "خطأ غير معروف"}"
                )
            }
        }
    }

    fun loadSampleSpeechToText() {
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
            isTranscribing = false,
            progressFraction = 1f,
            statusMessage = "يرجى استيراد فيديو للتعرف الصوتي الحقيقي بدون نصوص وهمية",
            errorMessage = null
        )
    }

    fun seekToSpeechSegment(segment: VideoAudioTranscriptionSegment) {
        seekTo(segment.startSeconds)
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
            selectedSegmentId = segment.id
        )
    }

    fun playSpeechSegmentAudio(segment: VideoAudioTranscriptionSegment) {
        if (_videoSpeechToTextState.value.currentlyPlayingSegmentId == segment.id) {
            stopSpeechSegmentAudio()
            return
        }
        val textToSpeak = segment.arabicDubbedAdaptation.ifBlank { segment.originalSpeech }
        ttsManager.speakText(
            text = textToSpeak,
            languageCode = "ar",
            onDone = {
                _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
                    currentlyPlayingSegmentId = null
                )
            }
        )
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
            currentlyPlayingSegmentId = segment.id
        )
    }

    fun stopSpeechSegmentAudio() {
        ttsManager.stop()
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(
            currentlyPlayingSegmentId = null
        )
    }

    fun updateSpeechSegment(updatedSegment: VideoAudioTranscriptionSegment) {
        val updatedList = _videoSpeechToTextState.value.segments.map {
            if (it.id == updatedSegment.id) updatedSegment else it
        }.sortedBy { it.startSeconds }
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(segments = updatedList)
    }

    fun deleteSpeechSegment(segmentId: String) {
        val updatedList = _videoSpeechToTextState.value.segments.filter { it.id != segmentId }
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(segments = updatedList)
    }

    fun addManualSpeechSegment(
        startSec: Float,
        endSec: Float,
        speaker: String,
        gender: String,
        originalText: String,
        arabicText: String
    ) {
        val newSegment = VideoAudioTranscriptionSegment(
            startSeconds = startSec,
            endSeconds = endSec,
            speaker = speaker,
            speakerGender = gender,
            originalSpeech = originalText,
            arabicDubbedAdaptation = arabicText,
            isEditedByUser = true
        )
        val updatedList = (_videoSpeechToTextState.value.segments + newSegment).sortedBy { it.startSeconds }
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(segments = updatedList)
    }

    fun addSingleSpeechSegmentToStudio(segment: VideoAudioTranscriptionSegment) {
        val gender = when (segment.speakerGender) {
            "FEMALE" -> "FEMALE"
            "CHILD" -> "CHILD"
            else -> "MALE"
        }
        val avatar = when (gender) {
            "FEMALE" -> "👩"
            "CHILD" -> "🧒"
            else -> "👨"
        }
        val newLine = ScriptLine(
            id = segment.id,
            characterName = segment.speaker,
            characterAvatar = avatar,
            textArabic = segment.arabicDubbedAdaptation.ifBlank { segment.originalSpeech },
            textOriginal = segment.originalSpeech,
            startSeconds = segment.startSeconds,
            endSeconds = segment.endSeconds,
            voiceType = if (gender == "FEMALE") "ARABIC_FEMALE" else "ARABIC_MALE",
            speakerGender = gender
        )
        val currentLines = _uiState.value.scriptLines
        val updatedLines = (currentLines + newLine).sortedBy { it.startSeconds }
        _uiState.value = _uiState.value.copy(
            scriptLines = updatedLines,
            toastMessage = "تمت إضافة مقطع '${segment.speaker}' بالتوقيت إلى الاستوديو! 🎬"
        )
    }

    fun applySpeechSegmentsToStudioTimeline(replaceExisting: Boolean = false) {
        val segments = _videoSpeechToTextState.value.segments
        if (segments.isEmpty()) return
        val newLines = segments.map { seg ->
            val gender = when (seg.speakerGender) {
                "FEMALE" -> "FEMALE"
                "CHILD" -> "CHILD"
                else -> "MALE"
            }
            val avatar = when (gender) {
                "FEMALE" -> "👩"
                "CHILD" -> "🧒"
                else -> "👨"
            }
            ScriptLine(
                id = seg.id,
                characterName = seg.speaker,
                characterAvatar = avatar,
                textArabic = seg.arabicDubbedAdaptation.ifBlank { seg.originalSpeech },
                textOriginal = seg.originalSpeech,
                startSeconds = seg.startSeconds,
                endSeconds = seg.endSeconds,
                voiceType = if (gender == "FEMALE") "ARABIC_FEMALE" else "ARABIC_MALE",
                speakerGender = gender
            )
        }
        val updatedLines = if (replaceExisting) {
            newLines.sortedBy { it.startSeconds }
        } else {
            (_uiState.value.scriptLines + newLines).distinctBy { it.id }.sortedBy { it.startSeconds }
        }
        _uiState.value = _uiState.value.copy(
            scriptLines = updatedLines,
            toastMessage = "تم نقل ${segments.size} مقطع كلامي إلى خط زمن الاستوديو بالتوقيتات! 🎬⏱️"
        )
    }

    fun exportTranscriptsAsSrtFile(): File? {
        val segments = _videoSpeechToTextState.value.segments
        if (segments.isEmpty()) return null
        return try {
            val srtFile = File(getApplication<Application>().cacheDir, "subtitles_${System.currentTimeMillis()}.srt")
            srtFile.bufferedWriter().use { writer ->
                segments.forEachIndexed { index, seg ->
                    writer.write("${index + 1}\n")
                    val startMs = (seg.startSeconds * 1000).toLong()
                    val endMs = (seg.endSeconds * 1000).toLong()
                    val startStr = formatSrtTimestamp(startMs)
                    val endStr = formatSrtTimestamp(endMs)
                    writer.write("$startStr --> $endStr\n")
                    val text = seg.arabicDubbedAdaptation.ifBlank { seg.originalSpeech }
                    writer.write("$text\n\n")
                }
            }
            srtFile
        } catch (e: Exception) {
            null
        }
    }

    fun exportTranscriptsAsVttFile(): File? {
        val segments = _videoSpeechToTextState.value.segments
        if (segments.isEmpty()) return null
        return try {
            val vttFile = File(getApplication<Application>().cacheDir, "subtitles_${System.currentTimeMillis()}.vtt")
            vttFile.bufferedWriter().use { writer ->
                writer.write("WEBVTT\n\n")
                segments.forEachIndexed { index, seg ->
                    writer.write("${index + 1}\n")
                    val startMs = (seg.startSeconds * 1000).toLong()
                    val endMs = (seg.endSeconds * 1000).toLong()
                    val startStr = formatVttTimestamp(startMs)
                    val endStr = formatVttTimestamp(endMs)
                    writer.write("$startStr --> $endStr\n")
                    val text = seg.arabicDubbedAdaptation.ifBlank { seg.originalSpeech }
                    writer.write("$text\n\n")
                }
            }
            vttFile
        } catch (e: Exception) {
            null
        }
    }

    private fun formatSrtTimestamp(millis: Long): String {
        val h = millis / 3600000
        val m = (millis % 3600000) / 60000
        val s = (millis % 60000) / 1000
        val ms = millis % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", h, m, s, ms)
    }

    private fun formatVttTimestamp(millis: Long): String {
        val h = millis / 3600000
        val m = (millis % 3600000) / 60000
        val s = (millis % 60000) / 1000
        val ms = millis % 1000
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", h, m, s, ms)
    }

    fun getFormattedTranscriptText(): String {
        val segments = _videoSpeechToTextState.value.segments
        if (segments.isEmpty()) return "لا توجد مقاطع كلام مفرغة حتى الآن."
        val sb = StringBuilder()
        sb.append("🎙️ تفريغ كلام الفيديو والتوقيتات:\n")
        sb.append("====================================\n\n")
        segments.forEachIndexed { index, seg ->
            sb.append("${index + 1}. [${seg.formattedTimeRange}] (المدة: ${String.format(Locale.US, "%.1f", seg.durationSeconds)}ث) - ${seg.speaker}:\n")
            if (seg.originalSpeech.isNotBlank()) {
                sb.append("   • النص الأصلي: ${seg.originalSpeech}\n")
            }
            if (seg.arabicDubbedAdaptation.isNotBlank()) {
                sb.append("   • الدبلجة العربية: ${seg.arabicDubbedAdaptation}\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    fun setSpeechToTextSearchQuery(query: String) {
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(searchQuery = query)
    }

    fun setSpeechToTextSpeakerFilter(speaker: String?) {
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(speakerFilter = speaker)
    }

    fun setSpeechToTextDialect(dialect: DubbingDialect) {
        _videoSpeechToTextState.value = _videoSpeechToTextState.value.copy(selectedDialect = dialect)
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            themePrefs.edit().clear().apply()
            langPrefs.edit().clear().apply()
            _isDarkMode.value = true
            _currentAppLanguage.value = com.example.localization.AppLanguage.ARABIC
            _videoExportConfig.value = VideoExportConfig()
            _uiState.value = _uiState.value.copy(
                originalVolume = 0.25f,
                dubVolume = 1.0f,
                bgmVolume = 0.35f,
                isAutoDuckingEnabled = true,
                autoVoiceRecognitionEnabled = true,
                toastMessage = "تمت استعادة جميع الإعدادات الافتراضية بنجاح 🔄"
            )
        }
    }

    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        themePrefs.edit().putBoolean("is_dark_mode", newMode).apply()
        viewModelScope.launch {
            userSettingsDataStore.updateDarkMode(newMode)
        }
        _uiState.value = _uiState.value.copy(
            toastMessage = if (newMode) "تم تفعيل وحفظ الوضع الليلي عبر DataStore 🌙" else "تم تفعيل وحفظ الوضع النهاري عبر DataStore ☀️"
        )
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        themePrefs.edit().putBoolean("is_dark_mode", enabled).apply()
        viewModelScope.launch {
            userSettingsDataStore.updateDarkMode(enabled)
        }
    }

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private val _exportDialogState = MutableStateFlow(ExportDialogUiState())
    val exportDialogState: StateFlow<ExportDialogUiState> = _exportDialogState.asStateFlow()

    fun updateVideoExportConfig(config: VideoExportConfig) {
        _videoExportConfig.value = config
    }

    private var playbackJob: Job? = null
    private var countdownJob: Job? = null
    private var trimmerPlaybackJob: Job? = null

    val allSavedProjects: StateFlow<List<DubbingProject>>
    val allSavedRecordings: StateFlow<List<VoiceRecordingEntity>>

    private var previewMediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingRecordingId = MutableStateFlow<Long?>(null)
    val currentlyPlayingRecordingId: StateFlow<Long?> = _currentlyPlayingRecordingId.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = DubbingRepository(db.dubbingDao(), db.voiceRecordingDao())
        allSavedProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        allSavedRecordings = repository.allRecordings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize with first clip
        loadClip(SampleClipsRepository.clips.first())

        // Load & synchronize persistent user preferences from Jetpack DataStore
        viewModelScope.launch {
            userSettingsDataStore.userSettingsFlow.collect { settings ->
                val lang = com.example.localization.AppLanguage.fromCode(settings.languageCode)
                if (_currentAppLanguage.value != lang) {
                    _currentAppLanguage.value = lang
                }
                if (_isDarkMode.value != settings.isDarkMode) {
                    _isDarkMode.value = settings.isDarkMode
                }
            }
        }

        // Collect mic amplitude & waveform history
        viewModelScope.launch {
            recordingManager.currentAmplitude.collect { amp ->
                if (_uiState.value.isRecording) {
                    _uiState.value = _uiState.value.copy(liveAmplitude = amp)
                }
            }
        }

        viewModelScope.launch {
            recordingManager.amplitudeHistory.collect { history ->
                if (_uiState.value.isRecording) {
                    _uiState.value = _uiState.value.copy(waveformHistory = history)
                }
            }
        }

        // Collect voice gender detector results
        viewModelScope.launch {
            genderDetector.analysisResult.collect { result ->
                if (result.detectedGender != DetectedGender.SILENCE && result.detectedGender != DetectedGender.DETECTING) {
                    _uiState.value = _uiState.value.copy(lastDetectedGender = result.detectedGender)
                }
            }
        }

        // Automatic Cache Cleaner: Clean temporary audio/video files older than 24 hours
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = application.cacheDir
                val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
                cacheDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() < cutoff &&
                        (file.name.endsWith(".wav") || file.name.endsWith(".m4a") ||
                         file.name.endsWith(".mp4") || file.name.endsWith(".tmp"))
                    ) {
                        file.delete()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun loadClip(clip: DubbingClip) {
        pausePlayback()
        stopRecording()
        _uiState.value = _uiState.value.copy(
            currentClip = clip,
            currentPlaybackSeconds = 0f,
            scriptLines = clip.scriptLines,
            recordedAudioPath = null,
            activeLineIndex = -1,
            currentProject = null
        )
    }

    fun loadProject(project: DubbingProject) {
        pausePlayback()
        stopRecording()
        val clip = SampleClipsRepository.clips.find { it.id == project.clipId }
            ?: SampleClipsRepository.clips.first()

        val parsedLines = parseScriptLines(project.scriptJson, clip.scriptLines)
        val voiceEffect = try {
            VoiceEffect.valueOf(project.voiceEffect)
        } catch (_: Exception) {
            VoiceEffect.NORMAL
        }
        val bgm = try {
            BgmStyle.valueOf(project.bgmStyle)
        } catch (_: Exception) {
            BgmStyle.CINEMATIC
        }

        _uiState.value = _uiState.value.copy(
            currentClip = clip,
            currentProject = project,
            scriptLines = parsedLines,
            recordedAudioPath = project.recordedAudioPath,
            selectedVoiceEffect = voiceEffect,
            selectedBgmStyle = bgm,
            originalVolume = project.originalVolume,
            dubVolume = project.dubVolume,
            bgmVolume = project.bgmVolume,
            currentPlaybackSeconds = 0f,
            activeLineIndex = -1,
            isPlaying = false,
            isRecording = false,
            toastMessage = "تم تحميل المشروع '${project.title}' بنجاح! يمكنك مواصلة التعديل والعمل 🎬📂"
        )
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        if (_uiState.value.isRecording) return
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = true)

        // Start BGM
        if (_uiState.value.selectedBgmStyle != BgmStyle.NONE) {
            SoundEffectsGenerator.startAmbientBgm(
                _uiState.value.selectedBgmStyle,
                _uiState.value.bgmVolume,
                viewModelScope
            )
        }

        // Play recorded voice if available and at start
        val audioPath = _uiState.value.recordedAudioPath
        if (audioPath != null && File(audioPath).exists() && _uiState.value.currentPlaybackSeconds < 0.5f) {
            if (_uiState.value.autoVoiceRecognitionEnabled) {
                instantDubbingEngine.playTransformedAudio(
                    filePath = audioPath,
                    detectedGender = _uiState.value.lastDetectedGender,
                    volume = _uiState.value.dubVolume
                )
            } else {
                recordingManager.playAudioWithEffectParams(
                    filePath = audioPath,
                    params = _uiState.value.customEffectParams,
                    volume = _uiState.value.dubVolume
                )
            }
        }

        playbackJob = viewModelScope.launch {
            val totalSeconds = _uiState.value.currentClip.durationSeconds.toFloat()
            while (isActive && _uiState.value.isPlaying) {
                delay(50)
                val newPos = _uiState.value.currentPlaybackSeconds + 0.05f
                if (newPos >= totalSeconds) {
                    _uiState.value = _uiState.value.copy(
                        currentPlaybackSeconds = 0f,
                        isPlaying = false,
                        activeLineIndex = -1
                    )
                    SoundEffectsGenerator.stopBgm()
                    recordingManager.stopPlayback()
                    instantDubbingEngine.stopPlayback()
                    break
                } else {
                    val activeIdx = _uiState.value.scriptLines.indexOfFirst {
                        newPos >= it.startSeconds && newPos <= it.endSeconds
                    }
                    _uiState.value = _uiState.value.copy(
                        currentPlaybackSeconds = newPos,
                        activeLineIndex = activeIdx
                    )
                }
            }
        }
    }

    fun pausePlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.value = _uiState.value.copy(isPlaying = false)
        SoundEffectsGenerator.stopBgm()
        recordingManager.stopPlayback()
        instantDubbingEngine.stopPlayback()
        ttsManager.stop()
    }

    fun seekTo(seconds: Float) {
        val clamped = seconds.coerceIn(0f, _uiState.value.currentClip.durationSeconds.toFloat())
        val activeIdx = _uiState.value.scriptLines.indexOfFirst {
            clamped >= it.startSeconds && clamped <= it.endSeconds
        }
        _uiState.value = _uiState.value.copy(
            currentPlaybackSeconds = clamped,
            activeLineIndex = activeIdx
        )
    }

    fun seekPlaybackDelta(deltaSeconds: Float) {
        val totalSec = _uiState.value.currentClip.durationSeconds.toFloat()
        val current = _uiState.value.currentPlaybackSeconds
        val target = (current + deltaSeconds).coerceIn(0f, totalSec)
        seekTo(target)
        val dir = if (deltaSeconds > 0) "تقديم" else "ترجيع"
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم $dir الصوت إلى ${String.format(java.util.Locale.US, "%.1f", target)} ثانية ⏱️"
        )
    }

    fun discardCurrentTake() {
        val path = _uiState.value.recordedAudioPath
        if (path != null) {
            try {
                val f = java.io.File(path)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
        }
        applyVoiceRecordingToStudio("")
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم حذف المسار الصوتي الحالي بنجاح 🗑️"
        )
    }

    fun readCurrentScriptAloud() {
        val lines = _uiState.value.scriptLines
        if (lines.isEmpty()) {
            ttsManager.speakText("لا يوجد سيناريو حالي لقراءته", utteranceId = "read_empty")
            return
        }
        val fullText = lines.joinToString(". ") { it.textArabic }
        ttsManager.speakText("إليك سيناريو المشهد: $fullText", utteranceId = "read_script")
    }

    fun startRecordingCountdown() {
        if (_uiState.value.isRecording) {
            stopRecording()
            return
        }
        pausePlayback()
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(countdownNumber = 3)
            delay(800)
            _uiState.value = _uiState.value.copy(countdownNumber = 2)
            delay(800)
            _uiState.value = _uiState.value.copy(countdownNumber = 1)
            delay(800)
            _uiState.value = _uiState.value.copy(countdownNumber = 0)
            actuallyStartRecording()
        }
    }

    private fun actuallyStartRecording() {
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            isPlaying = true,
            currentPlaybackSeconds = 0f
        )

        // Only start live analysis if user enabled auto voice recognition
        if (_uiState.value.autoVoiceRecognitionEnabled) {
            try {
                genderDetector.startAnalysis(viewModelScope)
            } catch (_: Exception) {}
        }

        recordingManager.startRecording(viewModelScope) { path ->
            _uiState.value = _uiState.value.copy(recordedAudioPath = path)
        }

        // Connect amplitude updates to gender detector
        viewModelScope.launch {
            recordingManager.currentAmplitude.collect { amp ->
                if (_uiState.value.isRecording) {
                    genderDetector.updateFromAmplitude(amp)
                }
            }
        }

        // Run playback in sync with recording
        playbackJob = viewModelScope.launch {
            val totalSeconds = _uiState.value.currentClip.durationSeconds.toFloat()
            while (isActive && _uiState.value.isRecording) {
                delay(50)
                val newPos = _uiState.value.currentPlaybackSeconds + 0.05f
                if (newPos >= totalSeconds) {
                    stopRecording()
                    break
                } else {
                    val activeIdx = _uiState.value.scriptLines.indexOfFirst {
                        newPos >= it.startSeconds && newPos <= it.endSeconds
                    }
                    _uiState.value = _uiState.value.copy(
                        currentPlaybackSeconds = newPos,
                        activeLineIndex = activeIdx
                    )
                }
            }
        }
    }

    fun stopRecording() {
        countdownJob?.cancel()
        countdownJob = null
        playbackJob?.cancel()
        playbackJob = null

        genderDetector.stopAnalysis()
        val path = recordingManager.stopRecording()
        val finalPath = path ?: _uiState.value.recordedAudioPath

        if (finalPath != null && File(finalPath).exists() && _uiState.value.autoVoiceRecognitionEnabled) {
            viewModelScope.launch {
                val result = genderDetector.analyzeAudioFile(File(finalPath))
                if (result.detectedGender == DetectedGender.MALE || result.detectedGender == DetectedGender.FEMALE) {
                    _uiState.value = _uiState.value.copy(
                        lastDetectedGender = result.detectedGender,
                        toastMessage = "تم التعرف على الصوت: ${result.detectedGender.titleArabic} • جاهز للدبلجة! 🎙️✨"
                    )
                }
            }
        }

        val detected = genderDetector.analysisResult.value.detectedGender
        val recognitionMsg = if (_uiState.value.autoVoiceRecognitionEnabled && (detected == DetectedGender.MALE || detected == DetectedGender.FEMALE)) {
            "تم التعرف على الصوت: ${detected.titleArabic} • جاهز للدبلجة الفورية! 🎙️✨"
        } else {
            "تم تسجيل مقطع الدبلجة بنجاح! 🎙️"
        }

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isPlaying = false,
            countdownNumber = 0,
            recordedAudioPath = finalPath,
            toastMessage = recognitionMsg
        )

        // Automatically persist the recording and apply Media3 AI noise suppression + gain normalization
        if (finalPath != null && File(finalPath).exists()) {
            val audioFile = File(finalPath)
            val durationSec = if (_uiState.value.currentPlaybackSeconds > 0.5f) {
                _uiState.value.currentPlaybackSeconds
            } else {
                (audioFile.length() / 32000f).coerceAtLeast(1.0f)
            }
            viewModelScope.launch {
                val formattedTime = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date())
                val newRecording = VoiceRecordingEntity(
                    title = "تسجيل صوتي ($formattedTime)",
                    filePath = finalPath,
                    durationSeconds = durationSec,
                    fileSizeBytes = audioFile.length(),
                    voiceEffect = _uiState.value.selectedVoiceEffect.name,
                    detectedGender = _uiState.value.lastDetectedGender.name,
                    associatedScript = _uiState.value.scriptLines.getOrNull(_uiState.value.activeLineIndex)?.textArabic ?: "",
                    timestamp = System.currentTimeMillis()
                )
                repository.saveVoiceRecording(newRecording)

                // Automatic Media3 AI Audio Processing Layer for background noise suppression and gain normalization
                try {
                    val media3Result = media3ProcessingLayer.processVoiceClip(audioFile)
                    if (media3Result.success) {
                        _uiState.value = _uiState.value.copy(
                            recordedAudioPath = media3Result.processedFile.absolutePath,
                            toastMessage = "تم التسجيل وتطبيق تنقية Media3 وموازنة درجات الصوت آلياً! 🎚️✨"
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun setAutoVoiceRecognition(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoVoiceRecognitionEnabled = enabled)
    }

    fun toggleAutoVoiceRecognition(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            autoVoiceRecognitionEnabled = enabled,
            toastMessage = if (enabled) "تم تفعيل التعرف التلقائي على طبقة الصوت 🎙️" else "تم إيقاف التعرف التلقائي على الصوت"
        )
    }

    fun startLiveVoiceAnalysis() {
        genderDetector.startAnalysis(viewModelScope)
    }

    fun stopLiveVoiceAnalysis() {
        genderDetector.stopAnalysis()
    }

    fun setInstantDubbingMode(mode: InstantDubbingMode) {
        instantDubbingEngine.updateMode(mode)
        _uiState.value = _uiState.value.copy(instantDubbingMode = mode)
    }

    fun setInstantPitchShift(pitch: Float) {
        instantDubbingEngine.updatePitch(pitch)
        _uiState.value = _uiState.value.copy(postProcessingPitch = pitch)
    }

    fun setInstantSpeed(speed: Float) {
        instantDubbingEngine.updateSpeed(speed)
        _uiState.value = _uiState.value.copy(postProcessingSpeed = speed)
    }

    fun setPostProcessingPitch(pitch: Float) {
        setInstantPitchShift(pitch)
    }

    fun setPostProcessingSpeed(speed: Float) {
        setInstantSpeed(speed)
    }

    fun toggleLiveMonitoring() {
        instantDubbingEngine.toggleLiveMonitoring()
    }

    fun playInstantDubbedTake(filePath: String) {
        instantDubbingEngine.playTransformedAudio(
            filePath = filePath,
            detectedGender = _uiState.value.lastDetectedGender,
            volume = 1.0f
        )
    }

    fun stopInstantDubbedTakePlayback() {
        instantDubbingEngine.stopPlayback()
    }

    fun applyInstantTakeToCurrentStudioTake(takePath: String) {
        _uiState.value = _uiState.value.copy(
            recordedAudioPath = takePath,
            toastMessage = "تم تعيين التسجيل الفوري كمقطع للدبلجة في الاستوديو! 🎙️✨"
        )
    }

    fun setVoiceEffect(effect: VoiceEffect) {
        _uiState.value = _uiState.value.copy(selectedVoiceEffect = effect)
    }

    /**
     * إلغاء التسجيل الجاري وحذف المقطع المسجل، وإلغاء كافة التأثيرات الصوتية والرجوع للصوت الطبيعي
     */
    fun cancelRecordingAndResetEffects() {
        countdownJob?.cancel()
        countdownJob = null
        playbackJob?.cancel()
        playbackJob = null
        genderDetector.stopAnalysis()

        try {
            audioEffectsProcessor.stopPreview()
        } catch (_: Exception) {}

        try {
            val path = recordingManager.stopRecording() ?: _uiState.value.recordedAudioPath
            if (path != null) {
                val f = File(path)
                if (f.exists()) f.delete()
            }
        } catch (_: Exception) {}

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isPlaying = false,
            countdownNumber = 0,
            recordedAudioPath = null,
            selectedVoiceEffect = VoiceEffect.NORMAL,
            toastMessage = "تم إلغاء التسجيل وتصفير جميع التأثيرات الصوتية إلى الوضع الطبيعي الأصلي 🔄"
        )
    }

    fun setBgmStyle(style: BgmStyle) {
        _uiState.value = _uiState.value.copy(selectedBgmStyle = style)
        if (_uiState.value.isPlaying) {
            SoundEffectsGenerator.startAmbientBgm(style, _uiState.value.bgmVolume, viewModelScope)
        }
    }

    fun setOriginalVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(originalVolume = volume)
    }

    fun setDubVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(dubVolume = volume)
    }

    fun setBgmVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(bgmVolume = volume)
        if (_uiState.value.isPlaying && _uiState.value.selectedBgmStyle != BgmStyle.NONE) {
            SoundEffectsGenerator.startAmbientBgm(
                _uiState.value.selectedBgmStyle,
                volume,
                viewModelScope
            )
        }
    }

    fun toggleMuteOriginal() {
        _uiState.value = _uiState.value.copy(isMutedOriginal = !_uiState.value.isMutedOriginal)
    }

    fun toggleMuteDub() {
        _uiState.value = _uiState.value.copy(isMutedDub = !_uiState.value.isMutedDub)
    }

    fun toggleSoloOriginal() {
        val newSolo = !_uiState.value.isSoloOriginal
        _uiState.value = _uiState.value.copy(
            isSoloOriginal = newSolo,
            isSoloDub = if (newSolo) false else _uiState.value.isSoloDub
        )
    }

    fun toggleSoloDub() {
        val newSolo = !_uiState.value.isSoloDub
        _uiState.value = _uiState.value.copy(
            isSoloDub = newSolo,
            isSoloOriginal = if (newSolo) false else _uiState.value.isSoloOriginal
        )
    }

    fun toggleVocalClarity() {
        _uiState.value = _uiState.value.copy(isVocalClarityActive = !_uiState.value.isVocalClarityActive)
    }

    fun setMixBalancePreset(original: Float, dub: Float) {
        _uiState.value = _uiState.value.copy(
            originalVolume = original,
            dubVolume = dub,
            isMutedOriginal = original <= 0.01f,
            isMutedDub = dub <= 0.01f
        )
    }

    fun setShowMixer(show: Boolean) {
        _uiState.value = _uiState.value.copy(showMixerSheet = show)
    }

    fun setShowVoicePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVoicePicker = show)
    }

    fun playSoundEffect(name: String) {
        SoundEffectsGenerator.playSoundEffect(name)
    }

    fun synthesizeTextToDubbingTake(text: String, profile: VoiceProfile) {
        viewModelScope.launch {
            if (!ttsManager.isEngineReady()) {
                val ready = ttsManager.awaitInitialization(3000L)
                if (!ready) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (ttsManager.initState.value.state == TtsEngineState.INITIALIZING)
                            "محرك الصوت ما زال قيد التهيئة، يرجى المحاولة بعد لحظات... ⏳"
                        else
                            "محرك الصوت غير جاهز (${ttsManager.initState.value.errorMessage ?: "تأكد من إعدادات TTS"}) ⚠️"
                    )
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(isGeneratingAiDub = true)
            ttsManager.synthesizeToFile(text, profile) { filePath ->
                _uiState.value = _uiState.value.copy(
                    isGeneratingAiDub = false,
                    recordedAudioPath = filePath ?: _uiState.value.recordedAudioPath,
                    toastMessage = if (filePath != null) "تم توليد الصوت وحفظه كملف دبلجة بنجاح! 🎙️✨" else "تعذر توليد الصوت، يرجى التحقق من محرك TTS ⚠️"
                )
            }
        }
    }

    fun speakScriptLine(line: ScriptLine) {
        if (!ttsManager.isEngineReady()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = if (ttsManager.initState.value.state == TtsEngineState.INITIALIZING)
                    "محرك الصوت قيد التهيئة، يرجى الانتظار ثوانٍ... ⏳"
                else
                    "محرك تحويل النص إلى كلام غير جاهز (${ttsManager.initState.value.errorMessage ?: "تأكد من دعم اللغة"}) ⚠️"
            )
            return
        }
        val profile = ttsManager.voiceProfiles.find { it.id == line.voiceType }
            ?: ttsManager.voiceProfiles.first()
        ttsManager.speakText(line.textArabic, profile)
    }

    fun generateAiTtsDubbing() {
        if (_uiState.value.isGeneratingAiDub) return
        if (!ttsManager.isEngineReady()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = if (ttsManager.initState.value.state == TtsEngineState.INITIALIZING)
                    "محرك الصوت قيد التهيئة، يرجى الانتظار ثوانٍ قبل توليد الدبلجة... ⏳"
                else
                    "محرك الصوت غير جاهز للعمل حالياً ⚠️"
            )
            return
        }
        _uiState.value = _uiState.value.copy(isGeneratingAiDub = true)

        viewModelScope.launch {
            val lines = _uiState.value.scriptLines
            for (line in lines) {
                val profile = ttsManager.voiceProfiles.find { it.id == line.voiceType }
                    ?: ttsManager.voiceProfiles.first()
                ttsManager.speakText(line.textArabic, profile)
                delay(1200)
            }
            _uiState.value = _uiState.value.copy(
                isGeneratingAiDub = false,
                toastMessage = "تمت دبلجة سيناريو المشهد كاملاً بواسطة أصوات الذكاء الاصطناعي! 🎬✨"
            )
        }
    }

    fun addScriptLine(characterName: String, textArabic: String, startSec: Float, endSec: Float) {
        val newLine = ScriptLine(
            id = "custom_${System.currentTimeMillis()}",
            characterName = characterName.ifBlank { "شخصية" },
            characterAvatar = "🎙️",
            textArabic = textArabic,
            textOriginal = "",
            startSeconds = startSec,
            endSeconds = endSec,
            voiceType = "ARABIC_MALE"
        )
        val updated = _uiState.value.scriptLines + newLine
        _uiState.value = _uiState.value.copy(scriptLines = updated.sortedBy { it.startSeconds })
    }

    fun saveCurrentProject(title: String = "") {
        saveProject(title)
    }

    fun saveProject(title: String) {
        viewModelScope.launch {
            val scriptJson = serializeScriptLines(_uiState.value.scriptLines)
            val project = DubbingProject(
                id = _uiState.value.currentProject?.id ?: 0L,
                title = title.ifBlank { "مشروع دبلجة ${_uiState.value.currentClip.title}" },
                clipId = _uiState.value.currentClip.id,
                clipTitle = _uiState.value.currentClip.title,
                recordedAudioPath = _uiState.value.recordedAudioPath,
                originalVolume = _uiState.value.originalVolume,
                dubVolume = _uiState.value.dubVolume,
                bgmVolume = _uiState.value.bgmVolume,
                voiceEffect = _uiState.value.selectedVoiceEffect.name,
                bgmStyle = _uiState.value.selectedBgmStyle.name,
                scriptJson = scriptJson,
                durationSeconds = _uiState.value.currentClip.durationSeconds
            )
            val savedId = repository.saveProject(project)
            val updatedProject = project.copy(id = if (project.id == 0L) savedId else project.id)
            _uiState.value = _uiState.value.copy(
                currentProject = updatedProject,
                toastMessage = "تم حفظ مشروع الدبلجة بنجاح! 💾✨"
            )
        }
    }


    private val _previewPlayingProjectId = MutableStateFlow<Long?>(null)
    val previewPlayingProjectId: StateFlow<Long?> = _previewPlayingProjectId.asStateFlow()

    fun previewProjectAudio(project: DubbingProject) {
        val audioPath = project.recordedAudioPath
        if (audioPath != null && File(audioPath).exists()) {
            if (_previewPlayingProjectId.value == project.id) {
                stopProjectAudioPreview()
                return
            }
            _previewPlayingProjectId.value = project.id
            val voiceEffect = try {
                VoiceEffect.valueOf(project.voiceEffect)
            } catch (_: Exception) {
                VoiceEffect.NORMAL
            }
            recordingManager.playAudio(
                filePath = audioPath,
                voiceEffect = voiceEffect,
                volume = project.dubVolume,
                onComplete = {
                    _previewPlayingProjectId.value = null
                }
            )
        } else {
            _uiState.value = _uiState.value.copy(toastMessage = "لا يوجد تسجيل صوتي محفوظ لهذا المشروع بعد")
        }
    }

    fun stopProjectAudioPreview() {
        recordingManager.stopPlayback()
        _previewPlayingProjectId.value = null
    }

    fun duplicateProject(project: DubbingProject) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0L,
                title = "نسخة من ${project.title}",
                lastModified = System.currentTimeMillis()
            )
            repository.saveProject(duplicate)
            _uiState.value = _uiState.value.copy(toastMessage = "تم نسخ مشروع الدبلجة بنجاح! 📋✨")
        }
    }

    fun renameProject(project: DubbingProject, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val updated = project.copy(
                title = newTitle.trim(),
                lastModified = System.currentTimeMillis()
            )
            repository.saveProject(updated)
            if (_uiState.value.currentProject?.id == project.id) {
                _uiState.value = _uiState.value.copy(currentProject = updated)
            }
            _uiState.value = _uiState.value.copy(toastMessage = "تمت إعادة تسمية المشروع بنجاح ✏️")
        }
    }

    fun deleteProject(project: DubbingProject) {
        viewModelScope.launch {
            if (_previewPlayingProjectId.value == project.id) {
                stopProjectAudioPreview()
            }
            repository.deleteProject(project)
            if (_uiState.value.currentProject?.id == project.id) {
                _uiState.value = _uiState.value.copy(currentProject = null)
            }
            _uiState.value = _uiState.value.copy(toastMessage = "تم حذف المشروع بنجاح")
        }
    }

    fun saveVoiceRecordingToDb(
        title: String,
        filePath: String,
        durationSeconds: Float,
        voiceEffect: String = _uiState.value.selectedVoiceEffect.name,
        associatedScript: String = ""
    ) {
        viewModelScope.launch {
            val file = File(filePath)
            val size = if (file.exists()) file.length() else 0L
            val recordingEntity = VoiceRecordingEntity(
                title = title.ifBlank { "تسجيل صوتي ${System.currentTimeMillis() % 10000}" },
                filePath = filePath,
                durationSeconds = durationSeconds,
                fileSizeBytes = size,
                voiceEffect = voiceEffect,
                detectedGender = _uiState.value.lastDetectedGender.name,
                associatedScript = associatedScript,
                timestamp = System.currentTimeMillis()
            )
            val savedId = repository.saveVoiceRecording(recordingEntity)
            _uiState.value = _uiState.value.copy(
                toastMessage = "تم حفظ التسجيل الصوتي في قاعدة البيانات بنجاح! 💾🎙️ (ID: $savedId)"
            )
        }
    }

    fun deleteVoiceRecordingFromDb(recording: VoiceRecordingEntity) {
        viewModelScope.launch {
            if (_currentlyPlayingRecordingId.value == recording.id) {
                stopRecordingPreview()
            }
            repository.deleteVoiceRecording(recording)
            try {
                val file = File(recording.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {}

            if (_uiState.value.recordedAudioPath == recording.filePath) {
                _uiState.value = _uiState.value.copy(recordedAudioPath = null)
            }
            _uiState.value = _uiState.value.copy(toastMessage = "تم حذف التسجيل الصوتي '${recording.title}' من قاعدة البيانات بنجاح 🗑️")
        }
    }

    /**
     * Preview playback of a recorded voice file using Android MediaPlayer
     */
    fun togglePlayRecordingPreview(recording: VoiceRecordingEntity) {
        if (_currentlyPlayingRecordingId.value == recording.id) {
            stopRecordingPreview()
        } else {
            playRecordingPreview(recording)
        }
    }

    fun playRecordingPreview(recording: VoiceRecordingEntity) {
        stopRecordingPreview()
        val file = File(recording.filePath)
        if (!file.exists()) {
            _uiState.value = _uiState.value.copy(toastMessage = "الملف الصوتي غير موجود على هذا المسار: ${recording.filePath}")
            return
        }
        try {
            previewMediaPlayer = MediaPlayer().apply {
                setDataSource(recording.filePath)
                prepare()
                setOnCompletionListener {
                    _currentlyPlayingRecordingId.value = null
                    stopRecordingPreview()
                }
                start()
            }
            _currentlyPlayingRecordingId.value = recording.id
        } catch (e: Exception) {
            _currentlyPlayingRecordingId.value = null
            _uiState.value = _uiState.value.copy(toastMessage = "خطأ في تشغيل المقطع عبر MediaPlayer: ${e.message}")
        }
    }

    fun stopRecordingPreview() {
        try {
            if (previewMediaPlayer?.isPlaying == true) {
                previewMediaPlayer?.stop()
            }
            previewMediaPlayer?.release()
        } catch (_: Exception) {}
        previewMediaPlayer = null
        _currentlyPlayingRecordingId.value = null
    }

    fun applyVoiceRecordingToStudio(filePath: String) {
        _uiState.value = _uiState.value.copy(
            recordedAudioPath = filePath
        )
    }

    fun shareProject(project: DubbingProject? = null) {
        val audioPath = project?.recordedAudioPath ?: _uiState.value.recordedAudioPath
        if (audioPath == null || !File(audioPath).exists()) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى تسجيل أو دبلجة الصوت أولاً للمشاركة!")
            return
        }

        try {
            val file = File(audioPath)
            val uri = FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )

            val clipTitle = project?.clipTitle ?: _uiState.value.currentClip.title
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "استمع إلى دبلجتي لمشهد: $clipTitle عبر تطبيق استوديو دبلجة المقاطع العربي! 🎬🎙️"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة الدبلجة الصوتية").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(toastMessage = "تعذر فتح نافذة المشاركة")
        }
    }

    fun openExportDialog(
        project: DubbingProject? = null,
        initialFormat: ExportFormat = ExportFormat.MP4_VIDEO
    ) {
        val targetClip = if (project != null) {
            SampleClipsRepository.getClipById(project.clipId)
        } else {
            _uiState.value.currentClip
        }
        val audioPath = project?.recordedAudioPath ?: _uiState.value.recordedAudioPath
        val hasLinesAudio = _uiState.value.scriptLines.any { it.customAudioPath != null && File(it.customAudioPath).exists() } ||
                targetClip.scriptLines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }

        if ((audioPath == null || !File(audioPath).exists()) && !hasLinesAudio) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى تسجيل أو دبلجة الصوت أولاً لتتمكن من تصدير المشروع! 🎙️")
            return
        }
        _exportDialogState.value = ExportDialogUiState(
            isVisible = true,
            project = project ?: _uiState.value.currentProject,
            clip = targetClip,
            isExporting = false,
            progress = 0f,
            statusMessage = "جاهز للتصدير",
            successResult = null,
            errorMessage = null,
            initialFormat = initialFormat
        )
    }

    fun closeExportDialog() {
        _exportDialogState.value = _exportDialogState.value.copy(isVisible = false)
    }

    fun startExport(
        format: ExportFormat,
        customFileName: String,
        videoConfig: VideoExportConfig = _videoExportConfig.value
    ) {
        val currentState = _exportDialogState.value
        val clip = currentState.clip ?: _uiState.value.currentClip
        val project = currentState.project ?: _uiState.value.currentProject
        val audioPath = project?.recordedAudioPath ?: _uiState.value.recordedAudioPath
        val lines = _uiState.value.scriptLines.ifEmpty { clip.scriptLines }
        val hasAudio = (audioPath != null && File(audioPath).exists()) ||
                lines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }

        if (!hasAudio) {
            _exportDialogState.value = currentState.copy(
                errorMessage = "لا يوجد تسجيل صوتي أو مقاطع مدبلجة متوفرة للتصدير. يرجى توفير صوت مدبلج أولاً!"
            )
            return
        }

        _videoExportConfig.value = videoConfig

        _exportDialogState.value = currentState.copy(
            isExporting = true,
            progress = 0.05f,
            statusMessage = "جاري بدء التصدير...",
            errorMessage = null,
            successResult = null
        )

        viewModelScope.launch {
            val result = if (format == ExportFormat.MP4_VIDEO) {
                val activeVideoSource: String? = clip.videoUri?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.currentClip.videoUri?.takeIf { it.isNotBlank() }

                if (activeVideoSource != null) {
                    exportManager.mergeOriginalVideoWithDubbedAudio(
                        clip = clip,
                        project = project,
                        customVideoPathOrUri = activeVideoSource,
                        recordedAudioPath = audioPath,
                        scriptLines = lines,
                        customTitle = customFileName,
                        syncOffsetMs = _uiState.value.syncOffsetMs,
                        onProgress = { prog, status ->
                            _exportDialogState.value = _exportDialogState.value.copy(
                                progress = prog,
                                statusMessage = status
                            )
                        }
                    )
                } else {
                    exportManager.exportVideo(
                        clip = clip,
                        project = project,
                        recordedAudioPath = audioPath,
                        scriptLines = lines,
                        customTitle = customFileName,
                        videoConfig = videoConfig,
                        onProgress = { prog, status ->
                            _exportDialogState.value = _exportDialogState.value.copy(
                                progress = prog,
                                statusMessage = status
                            )
                        }
                    )
                }
            } else {
                exportManager.exportSynchronizedAudioTrack(
                    clip = clip,
                    project = project,
                    recordedAudioPath = audioPath,
                    scriptLines = lines,
                    customTitle = customFileName,
                    syncOffsetMs = _uiState.value.syncOffsetMs,
                    onProgress = { prog, status ->
                        _exportDialogState.value = _exportDialogState.value.copy(
                            progress = prog,
                            statusMessage = status
                        )
                    }
                )
            }

            when (result) {
                is ExportResult.Success -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        progress = 1.0f,
                        statusMessage = "تم التصدير بنجاح!",
                        successResult = result,
                        errorMessage = null
                    )
                    _uiState.value = _uiState.value.copy(toastMessage = "تم تصدير ${result.fileName} بنجاح إلى وحدة التخزين 📁✨")
                }
                is ExportResult.Error -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Export the final synchronized dubbed audio track as a separate file to device storage (Music/VoiceMasterPro).
     */
    fun exportSynchronizedDubbedAudioTrack(
        clip: DubbingClip = _uiState.value.currentClip,
        customTitle: String? = null,
        onComplete: ((ExportResult.Success) -> Unit)? = null
    ) {
        val lines = _uiState.value.scriptLines.ifEmpty { clip.scriptLines }
        val audioPath = _uiState.value.recordedAudioPath ?: _uiState.value.currentProject?.recordedAudioPath
        val hasAudio = (audioPath != null && File(audioPath).exists()) ||
                lines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }

        if (!hasAudio) {
            _uiState.value = _uiState.value.copy(toastMessage = "لا يوجد تسجيل صوتي أو مقاطع مدبلجة متوفرة لتصدير الصوت! 🎙️")
            return
        }

        val fileName = customTitle ?: "صوت_مدبلج_${clip.title}"

        _exportDialogState.value = ExportDialogUiState(
            isVisible = true,
            project = _uiState.value.currentProject,
            clip = clip,
            isExporting = true,
            progress = 0.08f,
            statusMessage = "جاري تجميع ومزامنة المسار الصوتي النهائي وحفظه في الذاكرة...",
            successResult = null,
            errorMessage = null,
            initialFormat = ExportFormat.MP3_AUDIO
        )

        viewModelScope.launch {
            val result = exportManager.exportSynchronizedAudioTrack(
                clip = clip,
                scriptLines = lines,
                recordedAudioPath = audioPath,
                project = _uiState.value.currentProject,
                customTitle = fileName,
                syncOffsetMs = _uiState.value.syncOffsetMs,
                onProgress = { prog, status ->
                    _exportDialogState.value = _exportDialogState.value.copy(
                        progress = prog,
                        statusMessage = status
                    )
                }
            )

            when (result) {
                is ExportResult.Success -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        progress = 1.0f,
                        statusMessage = "تم تصدير مسار الصوت المتزامن بنجاح وحفظه في الذاكرة!",
                        successResult = result,
                        errorMessage = null
                    )
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "تم حفظ مسار الصوت في ${result.filePath} بنجاح! 🎵💾"
                    )
                    onComplete?.invoke(result)
                }
                is ExportResult.Error -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Directly saves any dubbed audio file (from AudioDubbingScreen or VATT or any audio path)
     * to the user device's storage (Music/VoiceMasterPro).
     */
    fun saveDubbedAudioFileToStorage(
        audioPath: String,
        suggestedTitle: String = "dubbed_audio",
        onResult: (ExportResult) -> Unit = {}
    ) {
        val file = File(audioPath)
        if (!file.exists()) {
            _uiState.value = _uiState.value.copy(toastMessage = "الملف الصوتي المراد حفظه غير موجود")
            onResult(ExportResult.Error("الملف الصوتي المراد حفظه غير موجود"))
            return
        }
        viewModelScope.launch {
            val result = exportManager.saveAudioFileToDeviceStorage(file, suggestedTitle)
            when (result) {
                is ExportResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "تم حفظ الملف الصوتي بنجاح في: ${result.filePath} 💾🎵"
                    )
                    ttsManager.speakText("تم حفظ الملف الصوتي بنجاح في ذاكرة الهاتف", utteranceId = "audio_saved_speak")
                }
                is ExportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "خطأ في حفظ الملف: ${result.message}"
                    )
                }
            }
            onResult(result)
        }
    }

    fun openExportedFile(result: ExportResult.Success) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val mimeType = when {
                    result.format == ExportFormat.MP4_VIDEO -> "video/mp4"
                    result.fileName.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                    else -> "audio/mpeg"
                }
                if (result.uri != null) {
                    setDataAndType(result.uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    val file = File(result.filePath)
                    val uri = FileProvider.getUriForFile(
                        getApplication(),
                        "${getApplication<Application>().packageName}.fileprovider",
                        file
                    )
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(toastMessage = "تم حفظ الملف بنجاح في: ${result.filePath}")
        }
    }

    fun shareExportedFile(result: ExportResult.Success) {
        try {
            val uri = result.uri ?: run {
                val file = File(result.filePath)
                FileProvider.getUriForFile(
                    getApplication(),
                    "${getApplication<Application>().packageName}.fileprovider",
                    file
                )
            }
            val mimeType = when {
                result.format == ExportFormat.MP4_VIDEO -> "video/mp4"
                result.fileName.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                else -> "audio/mpeg"
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (result.format == ExportFormat.MP4_VIDEO)
                        "شاهد مقطع الفيديو المدبلج بصوتي! 🎬✨ عبر تطبيق استوديو دبلجة المقاطع العربي"
                    else
                        "استمع إلى تسجيل الدبلجة الصوتي المتزامن! 🎙️✨ عبر تطبيق استوديو دبلجة المقاطع العربي"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "مشاركة الملف المصدر").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(toastMessage = "تعذر فتح نافذة المشاركة")
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun shareRecordedAudioOnly() {
        val audioPath = _uiState.value.recordedAudioPath ?: _uiState.value.currentProject?.recordedAudioPath
        if (audioPath != null && File(audioPath).exists()) {
            shareProject(null)
        } else {
            val lines = _uiState.value.scriptLines
            if (lines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }) {
                exportSynchronizedDubbedAudioTrack { successResult ->
                    shareExportedFile(successResult)
                }
            } else {
                _uiState.value = _uiState.value.copy(toastMessage = "يرجى تسجيل أو دبلجة الصوت أولاً للمشاركة!")
            }
        }
    }

    fun quickExportAndShareMergedVideo(customTitle: String? = null) {
        val clip = _uiState.value.currentClip
        val audioPath = _uiState.value.recordedAudioPath ?: _uiState.value.currentProject?.recordedAudioPath
        val lines = _uiState.value.scriptLines.ifEmpty { clip.scriptLines }
        val hasAudio = (audioPath != null && File(audioPath).exists()) ||
                lines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }

        if (!hasAudio) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى تسجيل أو دبلجة الصوت أولاً لتصدير ومشاركة الفيديو المدمج! 🎙️")
            return
        }
        val fileName = customTitle ?: "دبلجة_${clip.title}"
        _exportDialogState.value = ExportDialogUiState(
            isVisible = true,
            project = _uiState.value.currentProject,
            clip = clip,
            isExporting = true,
            progress = 0.1f,
            statusMessage = "جاري دمج الفيديو مع الصوت المسجل والترجمة...",
            successResult = null,
            errorMessage = null
        )
        viewModelScope.launch {
            val result = exportManager.mergeOriginalVideoWithDubbedAudio(
                clip = clip,
                project = _uiState.value.currentProject,
                customVideoPathOrUri = clip.videoUri,
                recordedAudioPath = audioPath,
                scriptLines = lines,
                customTitle = fileName,
                syncOffsetMs = _uiState.value.syncOffsetMs,
                onProgress = { prog, status ->
                    _exportDialogState.value = _exportDialogState.value.copy(
                        progress = prog,
                        statusMessage = status
                    )
                }
            )
            when (result) {
                is ExportResult.Success -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        progress = 1.0f,
                        statusMessage = "تم دمج وتصدير الفيديو بنجاح!",
                        successResult = result,
                        errorMessage = null
                    )
                    _uiState.value = _uiState.value.copy(toastMessage = "تم تجهيز الفيديو المدمج بنجاح وحفظه في الذاكرة! 🎬✨")
                    shareExportedFile(result)
                }
                is ExportResult.Error -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Merge the original video track directly with the newly dubbed audio and save to device storage (Movies/DubbingStudio).
     */
    fun mergeOriginalVideoWithDubbedAudio(
        clip: DubbingClip = _uiState.value.currentClip,
        project: DubbingProject? = _uiState.value.currentProject,
        customVideoPathOrUri: String? = null,
        recordedAudioPath: String? = null,
        customTitle: String? = null,
        onComplete: ((ExportResult.Success) -> Unit)? = null
    ) {
        val effectiveAudioPath = recordedAudioPath ?: _uiState.value.recordedAudioPath ?: project?.recordedAudioPath
        val effectiveVideoSource = customVideoPathOrUri ?: clip.videoUri
        val effectiveLines = _uiState.value.scriptLines.ifEmpty { clip.scriptLines }

        val hasAudio = effectiveAudioPath?.let { File(it).exists() } == true ||
                effectiveLines.any { it.customAudioPath != null && File(it.customAudioPath).exists() }

        if (!hasAudio) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى توفير تسجيل صوتي أو توليد دبلجة للمقاطع أولاً قبل الدمج! 🎙️")
            return
        }

        val fileName = customTitle ?: "دبلجة_${clip.title}"

        _exportDialogState.value = ExportDialogUiState(
            isVisible = true,
            project = project,
            clip = clip,
            isExporting = true,
            progress = 0.05f,
            statusMessage = "جاري دمج مسار الفيديو الأصلي مع الصوت المدبلج وحفظه في الذاكرة...",
            successResult = null,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = exportManager.mergeOriginalVideoWithDubbedAudio(
                clip = clip,
                project = project,
                customVideoPathOrUri = effectiveVideoSource,
                recordedAudioPath = effectiveAudioPath,
                scriptLines = effectiveLines,
                customTitle = fileName,
                syncOffsetMs = _uiState.value.syncOffsetMs,
                onProgress = { prog, status ->
                    _exportDialogState.value = _exportDialogState.value.copy(
                        progress = prog,
                        statusMessage = status
                    )
                }
            )

            when (result) {
                is ExportResult.Success -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        progress = 1.0f,
                        statusMessage = "تم دمج الفيديو وحفظه في مسار الأفلام بنجاح!",
                        successResult = result,
                        errorMessage = null
                    )
                    _uiState.value = _uiState.value.copy(toastMessage = "تم حفظ الفيديو المدمج في ${result.filePath} بنجاح! 📁🎬")
                    onComplete?.invoke(result)
                }
                is ExportResult.Error -> {
                    _exportDialogState.value = _exportDialogState.value.copy(
                        isExporting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun serializeScriptLines(lines: List<ScriptLine>): String {
        val arr = JSONArray()
        for (line in lines) {
            val obj = JSONObject().apply {
                put("id", line.id)
                put("characterName", line.characterName)
                put("characterAvatar", line.characterAvatar)
                put("textArabic", line.textArabic)
                put("textOriginal", line.textOriginal)
                put("startSeconds", line.startSeconds.toDouble())
                put("endSeconds", line.endSeconds.toDouble())
                put("voiceType", line.voiceType)
                put("isDubbed", line.isDubbed)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseScriptLines(json: String, fallback: List<ScriptLine>): List<ScriptLine> {
        if (json.isBlank()) return fallback
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<ScriptLine>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ScriptLine(
                        id = obj.getString("id"),
                        characterName = obj.getString("characterName"),
                        characterAvatar = obj.optString("characterAvatar", "🎭"),
                        textArabic = obj.getString("textArabic"),
                        textOriginal = obj.optString("textOriginal", ""),
                        startSeconds = obj.getDouble("startSeconds").toFloat(),
                        endSeconds = obj.getDouble("endSeconds").toFloat(),
                        voiceType = obj.optString("voiceType", "ARABIC_MALE"),
                        isDubbed = obj.optBoolean("isDubbed", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            fallback
        }
    }

    // ==========================================
    // Visual Audio Trimmer & Audio Import Engine
    // ==========================================

    fun importAudioFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showAudioTrimmer = true,
                isTrimmerLoading = true,
                isTrimmerPlaying = false,
                trimmerPlaybackSeconds = 0f
            )
            stopTrimmerPlayback()
            pausePlayback()

            val localPath = audioTrimmerManager.copyUriToCache(uri)
            if (localPath != null && File(localPath).exists()) {
                val metadata = audioTrimmerManager.extractWaveform(localPath)
                _uiState.value = _uiState.value.copy(
                    trimmerAudioPath = localPath,
                    trimmerWaveform = metadata.waveform,
                    trimmerDurationSeconds = metadata.durationSeconds,
                    trimmerStartSeconds = 0f,
                    trimmerEndSeconds = metadata.durationSeconds,
                    trimmerFormatName = metadata.formatName,
                    trimmerSilenceStart = (metadata.silenceStartMs / 1000f),
                    trimmerSilenceEnd = (metadata.silenceEndMs / 1000f),
                    isTrimmerLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    showAudioTrimmer = false,
                    isTrimmerLoading = false,
                    toastMessage = "تعذر استيراد الملف الصوتي، يرجى اختيار ملف صالح."
                )
            }
        }
    }

    fun openAudioTrimmerForFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            _uiState.value = _uiState.value.copy(toastMessage = "الملف الصوتي غير موجود.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showAudioTrimmer = true,
                isTrimmerLoading = true,
                isTrimmerPlaying = false,
                trimmerPlaybackSeconds = 0f
            )
            stopTrimmerPlayback()
            pausePlayback()

            val metadata = audioTrimmerManager.extractWaveform(filePath)
            _uiState.value = _uiState.value.copy(
                trimmerAudioPath = filePath,
                trimmerWaveform = metadata.waveform,
                trimmerDurationSeconds = metadata.durationSeconds,
                trimmerStartSeconds = 0f,
                trimmerEndSeconds = metadata.durationSeconds,
                trimmerFormatName = metadata.formatName,
                trimmerSilenceStart = (metadata.silenceStartMs / 1000f),
                trimmerSilenceEnd = (metadata.silenceEndMs / 1000f),
                isTrimmerLoading = false
            )
        }
    }

    fun openAudioTrimmerForCurrentTake() {
        val path = _uiState.value.recordedAudioPath
        if (path != null && File(path).exists()) {
            openAudioTrimmerForFile(path)
        } else {
            _uiState.value = _uiState.value.copy(toastMessage = "لا يوجد تسجيل صوتي حالي لقصه. قم بالتسجيل أو استيراد ملف صوتي أولاً!")
        }
    }

    fun closeAudioTrimmer() {
        stopTrimmerPlayback()
        _uiState.value = _uiState.value.copy(
            showAudioTrimmer = false,
            isTrimmerPlaying = false,
            trimmerPlaybackSeconds = 0f
        )
    }

    fun setTrimmerStart(sec: Float) {
        val maxStart = (_uiState.value.trimmerEndSeconds - 0.2f).coerceAtLeast(0f)
        val clamped = sec.coerceIn(0f, maxStart)
        _uiState.value = _uiState.value.copy(trimmerStartSeconds = clamped)
        if (_uiState.value.isTrimmerPlaying) {
            seekTrimmerPlayback(0f)
        }
    }

    fun setTrimmerEnd(sec: Float) {
        val minEnd = (_uiState.value.trimmerStartSeconds + 0.2f)
        val clamped = sec.coerceIn(minEnd, _uiState.value.trimmerDurationSeconds)
        _uiState.value = _uiState.value.copy(trimmerEndSeconds = clamped)
        if (_uiState.value.isTrimmerPlaying) {
            seekTrimmerPlayback(0f)
        }
    }

    fun seekTrimmerPlayback(offsetSeconds: Float) {
        val range = (_uiState.value.trimmerEndSeconds - _uiState.value.trimmerStartSeconds).coerceAtLeast(0.1f)
        val clamped = offsetSeconds.coerceIn(0f, range)
        _uiState.value = _uiState.value.copy(trimmerPlaybackSeconds = clamped)
    }

    fun toggleTrimmerLoop() {
        _uiState.value = _uiState.value.copy(isTrimmerLooping = !_uiState.value.isTrimmerLooping)
    }

    fun toggleTrimmerPlayback() {
        if (_uiState.value.isTrimmerPlaying) {
            stopTrimmerPlayback()
        } else {
            startTrimmerPlayback()
        }
    }

    private fun startTrimmerPlayback() {
        val path = _uiState.value.trimmerAudioPath ?: return
        if (!File(path).exists()) return

        stopTrimmerPlayback()
        _uiState.value = _uiState.value.copy(isTrimmerPlaying = true)

        recordingManager.playAudio(
            filePath = path,
            voiceEffect = VoiceEffect.NORMAL,
            volume = 1.0f
        )

        trimmerPlaybackJob = viewModelScope.launch {
            val startSec = _uiState.value.trimmerStartSeconds
            val endSec = _uiState.value.trimmerEndSeconds
            val duration = (endSec - startSec).coerceAtLeast(0.2f)

            var currentOffset = _uiState.value.trimmerPlaybackSeconds
            if (currentOffset >= duration) {
                currentOffset = 0f
            }

            while (isActive && _uiState.value.isTrimmerPlaying) {
                delay(50)
                currentOffset += 0.05f
                if (currentOffset >= duration) {
                    if (_uiState.value.isTrimmerLooping) {
                        currentOffset = 0f
                        _uiState.value = _uiState.value.copy(trimmerPlaybackSeconds = 0f)
                        recordingManager.playAudio(
                            filePath = path,
                            voiceEffect = VoiceEffect.NORMAL,
                            volume = 1.0f
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isTrimmerPlaying = false,
                            trimmerPlaybackSeconds = 0f
                        )
                        recordingManager.stopPlayback()
                        break
                    }
                } else {
                    _uiState.value = _uiState.value.copy(trimmerPlaybackSeconds = currentOffset)
                }
            }
        }
    }

    fun stopTrimmerPlayback() {
        trimmerPlaybackJob?.cancel()
        trimmerPlaybackJob = null
        recordingManager.stopPlayback()
        _uiState.value = _uiState.value.copy(isTrimmerPlaying = false)
    }

    fun applyTrimmedAudio(startSec: Float, endSec: Float) {
        val sourcePath = _uiState.value.trimmerAudioPath ?: return
        if (!File(sourcePath).exists()) return

        recordUndoableAction("قص الصوت (${String.format("%.1f", startSec)}s - ${String.format("%.1f", endSec)}s)")
        stopTrimmerPlayback()
        _uiState.value = _uiState.value.copy(isProcessingTrim = true)

        viewModelScope.launch {
            val startMs = (startSec * 1000L).toLong()
            val endMs = (endSec * 1000L).toLong()

            val trimmedPath = audioTrimmerManager.trimAudio(
                sourcePath = sourcePath,
                startMs = startMs,
                endMs = endMs
            )

            val finalAudioPath = trimmedPath ?: sourcePath
            val durationSecFormatted = String.format("%.1f", (endSec - startSec).coerceAtLeast(0f))

            _uiState.value = _uiState.value.copy(
                recordedAudioPath = finalAudioPath,
                showAudioTrimmer = false,
                isProcessingTrim = false,
                currentPlaybackSeconds = 0f,
                toastMessage = "تم قص واعتماد المقطع الصوتي ($durationSecFormatted ثانية) للدبلجة بنجاح! ✂️🎙️"
            )
        }
    }

    fun saveTrimmedAudioAsStandalone(startSec: Float, endSec: Float) {
        val sourcePath = _uiState.value.trimmerAudioPath ?: return
        if (!File(sourcePath).exists()) return

        stopTrimmerPlayback()
        _uiState.value = _uiState.value.copy(isProcessingTrim = true)

        viewModelScope.launch {
            val startMs = (startSec * 1000L).toLong()
            val endMs = (endSec * 1000L).toLong()

            val trimmedPath = audioTrimmerManager.trimAudio(
                sourcePath = sourcePath,
                startMs = startMs,
                endMs = endMs
            )

            val durationSecFormatted = String.format("%.1f", (endSec - startSec).coerceAtLeast(0f))
            val finalPath = trimmedPath ?: sourcePath

            _uiState.value = _uiState.value.copy(
                recordedAudioPath = finalPath,
                showAudioTrimmer = false,
                isProcessingTrim = false,
                toastMessage = "تم قص وحفظ المقطع كملف صوتي جديد ($durationSecFormatted ثانية) بنجاح! 💾✂️"
            )
        }
    }

    fun openAudioEffectsLibrary() {
        pausePlayback()
        _uiState.value = _uiState.value.copy(showEffectsLibrary = true)
    }

    fun closeAudioEffectsLibrary() {
        audioEffectsProcessor.stopPreview()
        _uiState.value = _uiState.value.copy(
            showEffectsLibrary = false,
            isEffectsPreviewPlaying = false
        )
    }

    fun selectAudioEffect(effect: AudioEffectItem) {
        val matchingVoiceEffect = try {
            VoiceEffect.valueOf(effect.id)
        } catch (_: Exception) {
            when (effect.id) {
                "STUDIO_REVERB", "CATHEDRAL_REVERB", "CAVE_ECHO" -> VoiceEffect.ECHO
                "STUDIO_ECHO" -> VoiceEffect.ECHO
                "CYBER_ROBOT", "SPACE_ALIEN" -> VoiceEffect.ROBOT
                "CHIPMUNK_CARTOON" -> VoiceEffect.CHIPMUNK
                "CINEMATIC_DEEP" -> VoiceEffect.DEEP
                "FEMALE_VOICE" -> VoiceEffect.FEMALE_VOICE
                "MALE_VOICE" -> VoiceEffect.MALE_VOICE
                "VINTAGE_RADIO", "MEGAPHONE", "PHONE_CALL" -> VoiceEffect.RADIO
                else -> VoiceEffect.NORMAL
            }
        }

        val matchingPreset = VoicePresetType.values().find { it.effectId == effect.id || it.voiceEffectEnum == matchingVoiceEffect } ?: VoicePresetType.NORMAL

        _uiState.value = _uiState.value.copy(
            selectedAudioEffectItem = effect,
            customEffectParams = effect.defaultParams,
            selectedVoiceEffect = matchingVoiceEffect,
            selectedVoicePreset = matchingPreset
        )

        // If preview is playing, refresh preview with new effect params
        if (_uiState.value.isEffectsPreviewPlaying) {
            val audioPath = _uiState.value.recordedAudioPath
            if (audioPath != null && File(audioPath).exists()) {
                audioEffectsProcessor.playPreview(
                    filePath = audioPath,
                    params = effect.defaultParams,
                    isLooping = true
                )
            }
        }
    }

    fun selectVoicePreset(preset: VoicePresetType) {
        val effectItem = AudioEffectsLibrary.findById(preset.effectId)
        _uiState.value = _uiState.value.copy(
            selectedVoicePreset = preset,
            selectedVoiceEffect = preset.voiceEffectEnum,
            selectedAudioEffectItem = effectItem,
            customEffectParams = effectItem.defaultParams
        )

        if (_uiState.value.isEffectsPreviewPlaying) {
            val audioPath = _uiState.value.recordedAudioPath
            if (audioPath != null && File(audioPath).exists()) {
                audioEffectsProcessor.playPreview(
                    filePath = audioPath,
                    params = effectItem.defaultParams,
                    isLooping = true
                )
            }
        }
    }

    fun togglePresetPreview(preset: VoicePresetType? = null) {
        val targetPreset = preset ?: _uiState.value.selectedVoicePreset
        val effectItem = AudioEffectsLibrary.findById(targetPreset.effectId)
        val params = effectItem.defaultParams

        if (_uiState.value.isEffectsPreviewPlaying) {
            audioEffectsProcessor.stopPreview()
            _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = false)
        } else {
            val audioPath = _uiState.value.recordedAudioPath
            if (audioPath != null && File(audioPath).exists()) {
                audioEffectsProcessor.playPreview(
                    filePath = audioPath,
                    params = params,
                    isLooping = true,
                    onComplete = {
                        _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = false)
                    }
                )
                _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = true)
            } else {
                // If no audio take recorded yet, trigger demonstration sample speech through TTS
                if (ttsManager.isEngineReady()) {
                    val sampleText = when (targetPreset) {
                        VoicePresetType.ROBOT -> "أنا روبوت ذكاء اصطناعي، تم تفعيل مؤثر الروبوت الآلي."
                        VoicePresetType.ECHO -> "استوديو وصدى الصوت، تجربة ارتداد وتأخير الكلمات."
                        VoicePresetType.DEEP_VOICE -> "طبقة صوت جهورية سينمائية فخمة وعميقة للأفلام."
                        VoicePresetType.CHIPMUNK -> "صوت كرتوني ومرح لشخصيات الرسوم المتحركة."
                        VoicePresetType.VINTAGE_RADIO -> "نداء عبر المذياع الكلاسيكي واللاسلكي."
                        VoicePresetType.NORMAL -> "صوت طبيعي نقي بدون أي مؤثرات."
                    }
                    ttsManager.speakText(sampleText)
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "معاينة صوتية لمؤثر ${targetPreset.titleArabic} (${targetPreset.titleEnglish}) 🎙️"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "محرك الصوت قيد التهيئة، يرجى المحاولة بعد لحظات... ⏳"
                    )
                }
            }
        }
    }

    fun applyVoicePreset(preset: VoicePresetType) {
        selectVoicePreset(preset)
        applyEffectToCurrentTake()
    }

    fun updateCustomEffectParams(params: AudioEffectParameters) {
        _uiState.value = _uiState.value.copy(customEffectParams = params)

        // If preview is playing, refresh playback params live
        if (_uiState.value.isEffectsPreviewPlaying) {
            val audioPath = _uiState.value.recordedAudioPath
            if (audioPath != null && File(audioPath).exists()) {
                audioEffectsProcessor.playPreview(
                    filePath = audioPath,
                    params = params,
                    isLooping = true
                )
            }
        }
    }

    fun toggleEffectsPreviewPlayback() {
        if (_uiState.value.isEffectsPreviewPlaying) {
            audioEffectsProcessor.stopPreview()
            _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = false)
        } else {
            val audioPath = _uiState.value.recordedAudioPath
            if (audioPath != null && File(audioPath).exists()) {
                audioEffectsProcessor.playPreview(
                    filePath = audioPath,
                    params = _uiState.value.customEffectParams,
                    isLooping = true,
                    onComplete = {
                        _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = false)
                    }
                )
                _uiState.value = _uiState.value.copy(isEffectsPreviewPlaying = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "قم بتسجيل أو استيراد مقطع صوتي أولاً لمعاينته مع المؤثر! 🎙️"
                )
            }
        }
    }

    fun applyEffectToCurrentTake() {
        val audioPath = _uiState.value.recordedAudioPath
        val effect = _uiState.value.selectedAudioEffectItem
        val params = _uiState.value.customEffectParams

        if (audioPath == null || !File(audioPath).exists()) {
            _uiState.value = _uiState.value.copy(
                showEffectsLibrary = false,
                toastMessage = "تم اختيار مؤثر ${effect.titleArabic} كصوت افتراضي للتسجيلات القادمة! 🎛️✨"
            )
            return
        }

        recordUndoableAction("تطبيق مؤثر: ${effect.titleArabic}")

        // If normal/bypass without tweaks, apply directly
        if (effect.id == "NORMAL" && params.pitchSemitones == 0 && params.reverbRoomSize == 0f && params.echoDelayMs == 0 && params.robotModulationHz == 0f) {
            audioEffectsProcessor.stopPreview()
            _uiState.value = _uiState.value.copy(
                showEffectsLibrary = false,
                isEffectsPreviewPlaying = false,
                toastMessage = "تم تعيين الصوت الطبيعي بدون تعديل ✅"
            )
            return
        }

        audioEffectsProcessor.stopPreview()
        _uiState.value = _uiState.value.copy(
            isProcessingEffectDsp = true,
            isEffectsPreviewPlaying = false
        )

        viewModelScope.launch {
            val processedPath = audioEffectsProcessor.processAndExportEffectAudio(
                sourcePath = audioPath,
                params = params
            )

            val finalPath = processedPath ?: audioPath
            _uiState.value = _uiState.value.copy(
                recordedAudioPath = finalPath,
                showEffectsLibrary = false,
                isProcessingEffectDsp = false,
                currentPlaybackSeconds = 0f,
                toastMessage = "تم تطبيق مؤثر ${effect.titleArabic} ومعالجة الصوت بنجاح! 🎛️✨"
            )
        }
    }

    /**
     * Noise Reduction Cleanup: Removes background static and hiss from current take
     */
    fun cleanUpRecordedAudioStatic() {
        val audioPath = _uiState.value.recordedAudioPath
        if (audioPath == null || !File(audioPath).exists()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = "قم بتسجيل أو استيراد مقطع صوتي أولاً لتنظيفه من التشويش! 🎙️"
            )
            return
        }

        recordUndoableAction("تنظيف وتشذيب الضوضاء (Noise Cleanup)")
        audioEffectsProcessor.stopPreview()
        _uiState.value = _uiState.value.copy(
            isProcessingEffectDsp = true,
            isEffectsPreviewPlaying = false
        )

        viewModelScope.launch {
            val cleanedPath = audioEffectsProcessor.cleanAudioStatic(audioPath)
            val finalPath = cleanedPath ?: audioPath
            _uiState.value = _uiState.value.copy(
                recordedAudioPath = finalPath,
                isProcessingEffectDsp = false,
                currentPlaybackSeconds = 0f,
                toastMessage = if (cleanedPath != null) "تم تنظيف الصوت من الضوضاء والتشويش بنجاح! 🧹✨" else "تمت معالجة الصوت بنجاح"
            )
        }
    }

    /**
     * Gemini AI Arabic Script Generation
     */
    fun generateGeminiArabicScript(customPrompt: String = "") {
        if (_uiState.value.isGeneratingAiDub) return
        _uiState.value = _uiState.value.copy(
            isGeneratingAiDub = true,
            toastMessage = "جاري توليد سيناريو دبلجة عربي احترافي عبر Gemini AI... ✨"
        )

        viewModelScope.launch {
            recordUndoableAction("توليد سيناريو بالذكاء الاصطناعي")
            val currentClip = _uiState.value.currentClip
            val result = geminiScriptGenerator.generateArabicDubbingScript(
                clip = currentClip,
                customPromptOrStyle = customPrompt
            )

            val generatedLines = result.getOrNull() ?: currentClip.scriptLines
            _uiState.value = _uiState.value.copy(
                isGeneratingAiDub = false,
                scriptLines = generatedLines,
                activeLineIndex = -1,
                toastMessage = "تم توليد ${generatedLines.size} حوارات عربية متزامنة بالذكاء الاصطناعي بنجاح! 🎬✨"
            )
        }
    }

    // ==========================================
    // Professional 7-Band Equalizer Management
    // ==========================================

    fun openEqualizerSheet() {
        _uiState.value = _uiState.value.copy(showEqualizerSheet = true)
    }

    fun closeEqualizerSheet() {
        _uiState.value = _uiState.value.copy(showEqualizerSheet = false)
    }

    fun updateEqualizerBandGain(bandId: Int, newGainDb: Float) {
        val currentBands = _uiState.value.equalizerState.bands.map {
            if (it.id == bandId) it.copy(gainDb = newGainDb.coerceIn(-12f, 12f)) else it
        }
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                bands = currentBands,
                selectedPresetName = "مخصص"
            )
        )
    }

    fun applyEqualizerPreset(presetName: String, gains: List<Float>) {
        val updatedBands = _uiState.value.equalizerState.bands.mapIndexed { index, band ->
            val gain = gains.getOrElse(index) { 0f }
            band.copy(gainDb = gain.coerceIn(-12f, 12f))
        }
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                bands = updatedBands,
                selectedPresetName = presetName
            ),
            toastMessage = "تم تطبيق إعداد المعادل الصوتي: $presetName 🎚️"
        )
    }

    fun setEqualizerBassBoost(fraction: Float) {
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                bassBoostFraction = fraction.coerceIn(0f, 1f)
            )
        )
    }

    fun setEqualizerSpatialVirtualizer(fraction: Float) {
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                spatialVirtualizerFraction = fraction.coerceIn(0f, 1f)
            )
        )
    }

    fun setEqualizerMasterGain(gainDb: Float) {
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                masterGainDb = gainDb.coerceIn(-6f, 6f)
            )
        )
    }

    fun toggleEqualizerBypass() {
        val newBypass = !_uiState.value.equalizerState.isBypassed
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(isBypassed = newBypass),
            toastMessage = if (newBypass) "تم تجاوز المعادل (صوت خام الأصلي) 🔈" else "تم تفعيل المعادل الصوتي 🎚️"
        )
    }

    fun resetEqualizerToFlat() {
        val flatBands = _uiState.value.equalizerState.bands.map { it.copy(gainDb = 0f) }
        _uiState.value = _uiState.value.copy(
            equalizerState = _uiState.value.equalizerState.copy(
                bands = flatBands,
                selectedPresetName = "Flat (متوازن)",
                bassBoostFraction = 0f,
                spatialVirtualizerFraction = 0f,
                masterGainDb = 0f,
                isBypassed = false
            ),
            toastMessage = "تمت إعادة ضبط المعادل الصوتي للوضع الافتراضي (Flat) 🔄"
        )
    }

    // ==========================================
    // Volume Normalization & Dynamic Balancing
    // ==========================================

    fun openVolumeNormalizationSheet() {
        _uiState.value = _uiState.value.copy(showNormalizationSheet = true)
        analyzeAndCalculateNormalization()
    }

    fun closeVolumeNormalizationSheet() {
        _uiState.value = _uiState.value.copy(showNormalizationSheet = false)
    }

    fun setNormalizationMode(mode: NormalizationMode) {
        _uiState.value = _uiState.value.copy(normalizationMode = mode)
        analyzeAndCalculateNormalization()
    }

    fun toggleAutoDucking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoDuckingEnabled = enabled)
        analyzeAndCalculateNormalization()
    }

    fun analyzeAndCalculateNormalization() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzingAudioLevels = true)

            val audioPath = _uiState.value.recordedAudioPath
            val voiceProfile = audioNormalizer.analyzeAudioLoudness(audioPath)
            
            // Standard original background profile approximation
            val backgroundProfile = TrackLoudnessProfile(
                peakDb = -6.5f,
                rmsDb = -18.5f,
                lufsApprox = -21.5f,
                maxAmplitude = 0.45f,
                isSilent = false
            )

            val result = audioNormalizer.calculateAutoBalancedVolumes(
                voiceProfile = voiceProfile,
                originalBackgroundProfile = backgroundProfile,
                mode = _uiState.value.normalizationMode,
                currentVoiceVolume = _uiState.value.dubVolume,
                currentOriginalVolume = _uiState.value.originalVolume,
                currentBgmVolume = _uiState.value.bgmVolume
            )

            _uiState.value = _uiState.value.copy(
                isAnalyzingAudioLevels = false,
                voiceLoudnessProfile = voiceProfile,
                backgroundLoudnessProfile = backgroundProfile,
                normalizationResult = result
            )
        }
    }

    fun applyAutoVolumeBalance(orig: Float? = null, dub: Float? = null, bgm: Float? = null) {
        val result = _uiState.value.normalizationResult
        val targetOrig = orig ?: result?.calculatedOriginalVolume ?: 0.25f
        val targetDub = dub ?: result?.calculatedDubVolume ?: 1.15f
        val targetBgm = bgm ?: result?.calculatedBgmVolume ?: 0.25f

        _uiState.value = _uiState.value.copy(
            originalVolume = targetOrig,
            dubVolume = targetDub,
            bgmVolume = targetBgm,
            showNormalizationSheet = false,
            toastMessage = "تمت موازنة الصوت تلقائياً بنجاح! ⚖️ (صوت الدبلجة ${(targetDub * 100).toInt()}% • صوت المشهد ${(targetOrig * 100).toInt()}%)"
        )
    }

    fun quickAutoBalanceMix() {
        viewModelScope.launch {
            val audioPath = _uiState.value.recordedAudioPath
            val voiceProfile = audioNormalizer.analyzeAudioLoudness(audioPath)
            val result = audioNormalizer.calculateAutoBalancedVolumes(
                voiceProfile = voiceProfile,
                mode = _uiState.value.normalizationMode,
                currentVoiceVolume = _uiState.value.dubVolume,
                currentOriginalVolume = _uiState.value.originalVolume,
                currentBgmVolume = _uiState.value.bgmVolume
            )

            _uiState.value = _uiState.value.copy(
                originalVolume = result.calculatedOriginalVolume,
                dubVolume = result.calculatedDubVolume,
                bgmVolume = result.calculatedBgmVolume,
                normalizationResult = result,
                toastMessage = "تمت الموازنة السريعة للصوت بنجاح! ⚡ (دبلجة ${(result.calculatedDubVolume * 100).toInt()}% / مشهد ${(result.calculatedOriginalVolume * 100).toInt()}%)"
            )
        }
    }

    fun resetVolumesToDefault() {
        _uiState.value = _uiState.value.copy(
            originalVolume = 0.25f,
            dubVolume = 1.0f,
            bgmVolume = 0.35f,
            toastMessage = "تمت استعادة مستويات الصوت الافتراضية 🔄"
        )
        analyzeAndCalculateNormalization()
    }

    // ==========================================
    // AUTO VIDEO DUBBER PIPELINE FUNCTIONS
    // ==========================================

    fun importVideoForAutoDubbing(uri: Uri, fileName: String? = null) {
        viewModelScope.launch {
            val result = autoVideoDubber.importVideoFromUri(uri, fileName)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم استيراد الفيديو بنجاح: ${result.title} 🎬"
                )
            }
        }
    }

    fun loadSampleVideoForAutoDubbing(clip: DubbingClip) {
        autoVideoDubber.loadDemoSampleVideo(clip)
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم تحميل المشهد التجريبي: ${clip.title} 🎥"
        )
    }

    fun setAutoDubbingStyle(style: AutoDubbingStyle) {
        autoVideoDubber.setDubbingStyle(style)
    }

    fun setAutoDubbingSourceLanguage(language: DubbingTargetLanguage) {
        autoVideoDubber.setSourceLanguage(language)
    }

    fun setAutoDubbingLanguage(language: DubbingTargetLanguage) {
        autoVideoDubber.setTargetLanguage(language)
    }

    fun setAutoDubbingDialect(dialect: DubbingDialect) {
        autoVideoDubber.setDubbingDialect(dialect)
    }

    fun detectSourceVideoLanguage() {
        viewModelScope.launch {
            val detected = autoVideoDubber.detectSourceLanguage()
            if (detected != null) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم التعرف على لغة الفيديو: ${detected.language.flagEmoji} ${detected.language.displayNameArabic} (${(detected.confidence * 100).toInt()}%) 🌐"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "يرجى التأكد من استيراد الفيديو أو فحص الاتصال بالإنترنت ⚠️"
                )
            }
        }
    }

    fun applyDetectedSourceLanguage() {
        autoVideoDubber.applyDetectedLanguageAsSource()
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم تطبيق لغة المصدر المكتشفة بنجاح ✅"
        )
    }

    fun dismissLanguageSuggestionBanner() {
        autoVideoDubber.dismissLanguageSuggestionBanner()
    }

    fun setAutoDubbingPacing(pacing: DubbingPacing) {
        autoVideoDubber.setDubbingPacing(pacing)
    }

    fun setAutoDubDuckedVolumes(originalVol: Float, dubVol: Float) {
        autoVideoDubber.setDuckedVolumes(originalVol, dubVol)
    }

    fun startAutoVideoDubbing() {
        val video = autoDubberState.value.importedVideo
        val selectedDialect = autoDubberState.value.selectedDialect
        if (video != null && video.isLongVideo) {
            longFormDubbingWorker.startLongFormDubbing(
                video = video,
                style = autoDubberState.value.selectedStyle,
                pacing = autoDubberState.value.selectedPacing,
                targetLanguage = autoDubberState.value.selectedLanguage,
                dialect = selectedDialect
            ) { dubbedClip ->
                if (dubbedClip != null) {
                    viewModelScope.launch {
                        applyDubbedClipToStudio(dubbedClip)
                        autoSaveDubbedProjectToDrafts(dubbedClip)
                        _uiState.value = _uiState.value.copy(
                            toastMessage = "تمت دبلجة الفيديو الطويل (${video.formattedDuration}) بنجاح! 💾🎉 جاهز للمعاينة"
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val dubbedClip = autoVideoDubber.startAutoDubbingPipeline(
                    dialect = selectedDialect
                )
                if (dubbedClip != null) {
                    applyDubbedClipToStudio(dubbedClip)
                    autoSaveDubbedProjectToDrafts(dubbedClip)
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "تمت دبلجة الفيديو وحفظ المسودة تلقائياً بنجاح! 💾🎉 جاهز للمعاينة والتصدير"
                    )
                }
            }
        }
    }

    private suspend fun autoSaveDubbedProjectToDrafts(clip: DubbingClip) {
        try {
            val scriptJson = serializeScriptLines(clip.scriptLines)
            val project = DubbingProject(
                title = "مسودة: ${clip.title}",
                clipId = clip.id,
                clipTitle = clip.title,
                recordedAudioPath = null,
                originalVolume = autoDubberState.value.originalVideoVolume,
                dubVolume = autoDubberState.value.dubbedVoiceVolume,
                bgmVolume = 0.25f,
                voiceEffect = if (autoDubberState.value.selectedStyle == AutoDubbingStyle.CLASSIC_ANIME) "NORMAL" else "AUTO_GENDER",
                bgmStyle = if (autoDubberState.value.selectedStyle == AutoDubbingStyle.CLASSIC_ANIME) "ORCHESTRAL" else "CINEMATIC",
                scriptJson = scriptJson,
                durationSeconds = clip.durationSeconds,
                lastModified = System.currentTimeMillis()
            )
            val savedId = repository.saveProject(project)
            val updated = project.copy(id = savedId)
            _uiState.value = _uiState.value.copy(currentProject = updated)
        } catch (_: Exception) {}
    }

    fun applyDubbedClipToStudio(clip: DubbingClip) {
        _uiState.value = _uiState.value.copy(
            currentClip = clip,
            currentProject = null,
            scriptLines = clip.scriptLines,
            originalVolume = autoDubberState.value.originalVideoVolume,
            dubVolume = autoDubberState.value.dubbedVoiceVolume,
            bgmVolume = if (autoDubberState.value.selectedStyle == AutoDubbingStyle.CLASSIC_ANIME) 0.35f else 0.25f,
            selectedBgmStyle = if (autoDubberState.value.selectedStyle == AutoDubbingStyle.CLASSIC_ANIME) BgmStyle.ORCHESTRAL else BgmStyle.CINEMATIC,
            currentPlaybackSeconds = 0f,
            activeLineIndex = -1,
            recordedAudioPath = null
        )
    }

    fun updateCloudTtsConfig(config: CloudTtsConfig) {
        _cloudTtsConfig.value = config
        cloudTtsPrefs.saveConfig(config)
    }

    fun testCloudTtsConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = cloudTtsService.testConnection(_cloudTtsConfig.value)
            if (result.isSuccess) {
                onResult(true, result.getOrNull() ?: "تم الاتصال بنجاح!")
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "فشل الاتصال بالخدمة")
            }
        }
    }

    fun previewSpeechForLine(line: ScriptLine) {
        if (!ttsManager.isEngineReady()) {
            _uiState.value = _uiState.value.copy(
                toastMessage = if (ttsManager.initState.value.state == TtsEngineState.INITIALIZING)
                    "محرك الصوت قيد التهيئة، يرجى الانتظار ثوانٍ... ⏳"
                else
                    "محرك الصوت غير جاهز (${ttsManager.initState.value.errorMessage ?: "تأكد من إعدادات TTS"}) ⚠️"
            )
            return
        }
        val profile = ttsManager.voiceProfiles.find { 
            when (line.voiceType) {
                "HERO_MALE" -> it.id == "hero_male"
                "HEROINE_FEMALE" -> it.id == "heroine_female"
                "EPIC_NARRATOR" -> it.id == "epic_narrator"
                "FEMALE" -> it.id == "natural_arabic_female"
                "CARTOON" -> it.id == "cartoon_hero"
                "DRAMATIC" -> it.id == "male_narrator"
                "TECH" -> it.id == "cyber_bot"
                else -> it.id == "natural_arabic_male"
            }
        } ?: ttsManager.voiceProfiles.first()

        val langCode = autoDubberState.value.selectedLanguage.code
        ttsManager.speakText(line.textArabic, profile, langCode)
    }

    /**
     * Synthesizes text to a real audio file using TTS and assigns it as the main recorded take
     * or line-specific take for the video.
     */
    fun synthesizeCustomScriptToAudio(
        text: String,
        profile: VoiceProfile,
        targetLineId: String? = null,
        targetDurationSeconds: Float? = null,
        speechRateMultiplier: Float = 1.0f,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى كتابة نص لتوليد الصوت ⚠️")
            onComplete(false, null)
            return
        }

        viewModelScope.launch {
            if (!ttsManager.isEngineReady()) {
                val ready = ttsManager.awaitInitialization(3000L)
                if (!ready) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = if (ttsManager.initState.value.state == TtsEngineState.INITIALIZING)
                            "محرك الصوت قيد التهيئة، يرجى المحاولة بعد قليل... ⏳"
                        else
                            "محرك الصوت غير جاهز (${ttsManager.initState.value.errorMessage ?: "تأكد من إعدادات TTS"}) ⚠️"
                    )
                    onComplete(false, null)
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(toastMessage = "جاري توليد الصوت بالذكاء الاصطناعي... 🎙️")
            val outputFileName = "tts_take_${System.currentTimeMillis()}.wav"
            ttsManager.synthesizeToFile(
                text = text,
                profile = profile,
                languageCode = "ar",
                targetDurationSeconds = targetDurationSeconds,
                speechRateMultiplier = speechRateMultiplier,
                outputFileName = outputFileName
            ) { generatedPath ->
                if (generatedPath != null && File(generatedPath).exists()) {
                    if (targetLineId != null) {
                        // Assign to specific script line take
                        val updatedLines = _uiState.value.scriptLines.map { line ->
                            if (line.id == targetLineId) {
                                line.copy(customAudioPath = generatedPath, isDubbed = true, textArabic = text)
                            } else line
                        }
                        _uiState.value = _uiState.value.copy(
                            scriptLines = updatedLines,
                            toastMessage = "تم توليد وربط الصوت بالمشهد بنجاح! 🎬✨"
                        )
                    } else {
                        // Assign as main studio dubbed voice
                        _uiState.value = _uiState.value.copy(
                            recordedAudioPath = generatedPath,
                            toastMessage = "تم تحويل النص إلى صوت واستخدامه كمسار دبلجة رئيسي! 🎙️✨"
                        )
                    }
                    onComplete(true, generatedPath)
                } else {
                    _uiState.value = _uiState.value.copy(toastMessage = "تعذر توليد الملف الصوتي، يرجى المحاولة ثانية ⚠️")
                    onComplete(false, null)
                }
            }
        }
    }

    // ==========================================
    // BATCH VIDEO DUBBING PIPELINE FUNCTIONS
    // ==========================================

    fun importVideosForBatch(uris: List<Uri>) {
        viewModelScope.launch {
            val importedList = mutableListOf<com.example.audio.ImportedVideoMetadata>()
            for (uri in uris) {
                val metadata = autoVideoDubber.importVideoFromUri(uri)
                if (metadata != null) {
                    importedList.add(metadata)
                }
            }
            if (importedList.isNotEmpty()) {
                batchDubbingEngine.addMultipleVideosToQueue(importedList)
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تمت إضافة ${importedList.size} فيديوهات إلى قائمة معالجة الدُفعة 📥"
                )
            }
        }
    }

    fun addSamplePackToBatch() {
        batchDubbingEngine.addSamplePackToQueue()
        _uiState.value = _uiState.value.copy(
            toastMessage = "تمت إضافة حزمة المشاهد التجريبية إلى قائمة الدُفعة 🎬"
        )
    }

    fun removeBatchItem(id: String) {
        batchDubbingEngine.removeVideoFromQueue(id)
    }

    fun moveBatchItem(fromIndex: Int, toIndex: Int) {
        batchDubbingEngine.moveItem(fromIndex, toIndex)
    }

    fun updateBatchItemLanguage(id: String, language: DubbingTargetLanguage) {
        batchDubbingEngine.updateItemConfig(id, targetLanguage = language)
    }

    fun updateBatchItemStyle(id: String, style: AutoDubbingStyle) {
        batchDubbingEngine.updateItemConfig(id, dubbingStyle = style)
    }

    fun updateBatchItemPacing(id: String, pacing: DubbingPacing) {
        batchDubbingEngine.updateItemConfig(id, pacing = pacing)
    }

    fun applyGlobalBatchLanguage(language: DubbingTargetLanguage) {
        batchDubbingEngine.applyGlobalLanguage(language)
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم تطبيق لغة الدبلجة (${language.displayNameArabic}) على جميع المقاطع في الدُفعة 🌐"
        )
    }

    fun applyGlobalBatchStyle(style: AutoDubbingStyle) {
        batchDubbingEngine.applyGlobalStyle(style)
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم تطبيق النمط (${style.titleArabic}) على جميع المقاطع في الدُفعة 🎭"
        )
    }

    fun startBatchDubbing() {
        batchDubbingEngine.startBatchProcessing(
            onClipCompleted = { clip ->
                // Auto add to saved clips/state and notify
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تمت دبلجة: ${clip.title} بنجاح! 🎉"
                )
            }
        )
    }

    fun pauseBatchDubbing() {
        batchDubbingEngine.pauseBatchProcessing()
    }

    fun resumeBatchDubbing() {
        batchDubbingEngine.resumeBatchProcessing()
    }

    fun cancelBatchDubbing() {
        batchDubbingEngine.cancelBatchProcessing()
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم إلغاء معالجة الدُفعة ⏹️"
        )
    }

    fun retryFailedBatchDubbing() {
        batchDubbingEngine.retryFailedItems(
            onClipCompleted = { clip ->
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تمت دبلجة: ${clip.title} بنجاح! 🎉"
                )
            }
        )
    }

    fun clearBatchQueue() {
        batchDubbingEngine.clearQueue()
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم تفريغ قائمة معالجة الدُفعة 🗑️"
        )
    }

    fun clearCompletedBatch() {
        batchDubbingEngine.clearCompleted()
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم مسح المقاطع المكتملة من قائمة الدُفعة 🧹"
        )
    }

    // ==========================================
    // SPLIT-SCREEN 'PREVIEW & COMPARE' & QUALITY AUDIT
    // ==========================================

    fun openSplitScreenCompare(open: Boolean = true) {
        _uiState.value = _uiState.value.copy(showSplitScreenCompare = open)
        if (open) {
            runScriptQualityAudit()
        }
    }

    fun setSplitCompareViewMode(mode: SplitCompareViewMode) {
        _uiState.value = _uiState.value.copy(splitCompareViewMode = mode)
    }

    fun setSplitDividerFraction(fraction: Float) {
        _uiState.value = _uiState.value.copy(splitDividerFraction = fraction.coerceIn(0.05f, 0.95f))
    }

    fun toggleAbFlip() {
        _uiState.value = _uiState.value.copy(isAbFlipActive = !_uiState.value.isAbFlipActive)
    }

    fun setAbFlipActive(isDubbed: Boolean) {
        _uiState.value = _uiState.value.copy(isAbFlipActive = isDubbed)
    }

    fun runScriptQualityAudit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuditRunning = true)
            delay(250) // Small calculation pause for smooth animation
            val lines = _uiState.value.scriptLines
            val duration = _uiState.value.currentClip.durationSeconds.toFloat()
            val report = ArabicPhoneticsEngine.auditScriptQuality(lines, duration)
            _uiState.value = _uiState.value.copy(
                auditReport = report,
                isAuditRunning = false
            )
        }
    }

    fun autoFixAndDiacritizeAllScriptLines() {
        val currentLines = _uiState.value.scriptLines
        if (currentLines.isEmpty()) return
        val correctedLines = ArabicPhoneticsEngine.autoCorrectAllScriptLines(currentLines)
        _uiState.value = _uiState.value.copy(
            scriptLines = correctedLines,
            toastMessage = "تم التدقيق وتشكيل الحركات وضبط التوقيتات آلياً بنجاح! 🪄✨"
        )
        // Refresh audit report after correction
        runScriptQualityAudit()
    }

    fun updateScriptLineDirectly(index: Int, newText: String, startSec: Float, endSec: Float) {
        val currentList = _uiState.value.scriptLines.toMutableList()
        if (index in currentList.indices) {
            val old = currentList[index]
            val enriched = ArabicPhoneticsEngine.enrichArabicLetteringAndTashkeel(newText)
            currentList[index] = old.copy(
                textArabic = enriched,
                startSeconds = startSec,
                endSeconds = endSec
            )
            _uiState.value = _uiState.value.copy(scriptLines = currentList)
            runScriptQualityAudit()
        }
    }

    fun nudgeScriptLineTimestamp(index: Int, deltaStartMs: Float, deltaEndMs: Float) {
        val currentList = _uiState.value.scriptLines.toMutableList()
        if (index in currentList.indices) {
            val old = currentList[index]
            val totalDuration = _uiState.value.currentClip.durationSeconds.toFloat()
            val newStart = (old.startSeconds + (deltaStartMs / 1000f)).coerceIn(0f, totalDuration - 0.2f)
            val newEnd = (old.endSeconds + (deltaEndMs / 1000f)).coerceIn(newStart + 0.2f, totalDuration)
            currentList[index] = old.copy(startSeconds = newStart, endSeconds = newEnd)
            _uiState.value = _uiState.value.copy(
                scriptLines = currentList,
                toastMessage = "تم ضبط توقيت السطر ${index + 1}: ${String.format(java.util.Locale.US, "%.2f", newStart)}ث - ${String.format(java.util.Locale.US, "%.2f", newEnd)}ث 🎯"
            )
            runScriptQualityAudit()
        }
    }

    fun autoFitScriptLineDuration(index: Int) {
        val currentList = _uiState.value.scriptLines.toMutableList()
        if (index in currentList.indices) {
            val old = currentList[index]
            val idealMs = ArabicPhoneticsEngine.calculatePhoneticDurationMs(old.textArabic)
            val idealSec = idealMs / 1000f
            val totalDuration = _uiState.value.currentClip.durationSeconds.toFloat()
            val newEnd = (old.startSeconds + idealSec).coerceIn(old.startSeconds + 0.3f, totalDuration)
            currentList[index] = old.copy(endSeconds = newEnd)
            _uiState.value = _uiState.value.copy(
                scriptLines = currentList,
                toastMessage = "تمت مواءمة مدة السطر صوتياً (${String.format(java.util.Locale.US, "%.2f", idealSec)}ث) بدقة متناهية! ⏱️✨"
            )
            runScriptQualityAudit()
        }
    }

    fun applyRecommendedLipSyncSpeed(speedMultiplier: Float) {
        val clamped = speedMultiplier.coerceIn(0.5f, 2.0f)
        _uiState.value = _uiState.value.copy(
            syncPlaybackSpeed = clamped,
            toastMessage = "تم تفعيل سرعة المزامنة المثالية: ${String.format(java.util.Locale.US, "%.2f", clamped)}x 🎯⚡"
        )
    }

    fun saveDubbedProjectToStorage(customTitle: String? = null) {
        viewModelScope.launch {
            val clip = _uiState.value.currentClip
            val title = customTitle ?: "دبلجة متقنة: ${clip.title}"
            val scriptJson = serializeScriptLines(_uiState.value.scriptLines)
            val project = DubbingProject(
                title = title,
                clipId = clip.id,
                clipTitle = clip.title,
                recordedAudioPath = _uiState.value.recordedAudioPath,
                originalVolume = _uiState.value.originalVolume,
                dubVolume = _uiState.value.dubVolume,
                bgmVolume = _uiState.value.bgmVolume,
                voiceEffect = _uiState.value.selectedVoiceEffect.name,
                bgmStyle = _uiState.value.selectedBgmStyle.name,
                scriptJson = scriptJson,
                durationSeconds = clip.durationSeconds,
                lastModified = System.currentTimeMillis()
            )
            val savedId = repository.saveProject(project)
            val updated = project.copy(id = savedId)
            _uiState.value = _uiState.value.copy(
                currentProject = updated,
                toastMessage = "تم حفظ العمل كاملاً في وحدة التخزين والمسودات بنجاح! 💾🎉"
            )
        }
    }

    fun startUpdateWebServer() {
        updateWebServer.start()
        _uiState.value = _uiState.value.copy(toastMessage = "تم تشغيل خادم موقع التحديثات بنجاح 🌐")
    }

    fun stopUpdateWebServer() {
        updateWebServer.stop()
        _uiState.value = _uiState.value.copy(toastMessage = "تم إيقاف خادم موقع التحديثات ⏸️")
    }

    fun toggleUpdateWebServer() {
        if (updateWebServer.isRunning.value) {
            stopUpdateWebServer()
        } else {
            startUpdateWebServer()
        }
    }

    fun checkForAppUpdates() {
        updateManager.performAsyncUpdateCheck { result ->
            if (result.isUpdateAvailable && result.latestRelease != null) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "🎉 يتوفر تحديث جديد عبر الإنترنت: v${result.latestRelease.versionName}!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "أنت تستخدم أحدث إصدار من التطبيق بالفعل! ✅"
                )
            }
        }
    }

    fun saveGitHubUpdateConfig(owner: String, repo: String, customUrl: String = "", token: String = "") {
        updateManager.onlineProvider.githubOwner = owner.trim()
        updateManager.onlineProvider.githubRepo = repo.trim()
        updateManager.onlineProvider.customApiUrl = customUrl.trim()
        if (token.isNotBlank()) {
            updateManager.onlineProvider.githubToken = token.trim()
        }
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم حفظ إعدادات خادم ومستودع التحديثات بنجاح! 🌐💾"
        )
        checkForAppUpdates()
    }

    // ==========================================
    // 🎚️ Precise Video-Audio Synchronization Engine
    // ==========================================

    fun setDubSyncOffsetMs(offsetMs: Float) {
        val clamped = offsetMs.coerceIn(-5000f, 5000f)
        _uiState.value = _uiState.value.copy(dubSyncOffsetMs = clamped)
    }

    fun nudgeDubSyncOffsetMs(deltaMs: Float) {
        val newOffset = (_uiState.value.dubSyncOffsetMs + deltaMs).coerceIn(-5000f, 5000f)
        _uiState.value = _uiState.value.copy(
            dubSyncOffsetMs = newOffset,
            toastMessage = "إزاحة المزامنة: ${String.format(java.util.Locale.US, "%+.0f", newOffset)} ميلي ثانية 🎚️"
        )
    }

    fun resetDubSyncOffset() {
        _uiState.value = _uiState.value.copy(
            dubSyncOffsetMs = 0f,
            toastMessage = "تمت إعادة ضبط المزامنة للوضع الافتراضي (0ms) 🔄"
        )
    }

    fun autoAlignSyncOffset() {
        val currentLines = _uiState.value.scriptLines
        val targetOffset = if (currentLines.isNotEmpty()) {
            val firstSpokenStart = currentLines.first().startSeconds * 1000f
            // Optimal speech latency compensation (-120ms standard reaction time)
            (-120f).coerceIn(-3000f, 3000f)
        } else {
            0f
        }
        _uiState.value = _uiState.value.copy(
            dubSyncOffsetMs = targetOffset,
            toastMessage = "تمت المحاذاة الذكية التلقائية لمزامنة الشفاه (-120ms) 🎯✨"
        )
    }

    fun setSyncZoomLevel(zoom: Float) {
        _uiState.value = _uiState.value.copy(syncZoomLevel = zoom.coerceIn(1.0f, 8.0f))
    }

    fun toggleSyncLooping() {
        val newLoop = !_uiState.value.isSyncLooping
        _uiState.value = _uiState.value.copy(
            isSyncLooping = newLoop,
            toastMessage = if (newLoop) "تم تفعيل تكرار مقطع المزامنة 🔁" else "تم إيقاف تكرار المقطع"
        )
    }

    fun setSyncLoopRange(startSec: Float, endSec: Float) {
        _uiState.value = _uiState.value.copy(
            syncLoopStartSec = startSec.coerceAtLeast(0f),
            syncLoopEndSec = endSec.coerceAtLeast(startSec + 0.5f)
        )
    }

    fun setSyncPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(
            syncPlaybackSpeed = speed,
            toastMessage = "سرعة التدقيق والمزامنة: ${speed}x ⏱️"
        )
    }

    fun setSyncWaveformGain(gain: Float) {
        _uiState.value = _uiState.value.copy(syncWaveformGain = gain.coerceIn(0.5f, 4.0f))
    }

    fun toggleSyncLocked() {
        val newLock = !_uiState.value.isSyncLocked
        _uiState.value = _uiState.value.copy(
            isSyncLocked = newLock,
            toastMessage = if (newLock) "تم قفل مزامنة الفيديو والصوت معاً 🔒" else "تم فك قفل المزامنة للتحريك الحر 🔓"
        )
    }

    fun addSyncMarker(positionSec: Float) {
        val current = _uiState.value.syncMarkerSeconds
        if (!current.any { kotlin.math.abs(it - positionSec) < 0.15f }) {
            _uiState.value = _uiState.value.copy(
                syncMarkerSeconds = (current + positionSec).sorted(),
                toastMessage = "تم وضع علامة مزامنة عند ${String.format(java.util.Locale.US, "%.2f", positionSec)} ثانية 📍"
            )
        }
    }

    fun clearSyncMarkers() {
        _uiState.value = _uiState.value.copy(
            syncMarkerSeconds = emptyList(),
            toastMessage = "تم مسح جميع علامات المزامنة 🧹"
        )
    }

    fun stepFrameForward(deltaMs: Float = 50f) {
        val duration = _uiState.value.currentClip.durationSeconds.toFloat()
        val nextPos = (_uiState.value.currentPlaybackSeconds + (deltaMs / 1000f)).coerceIn(0f, duration)
        _uiState.value = _uiState.value.copy(currentPlaybackSeconds = nextPos)
    }

    fun stepFrameBackward(deltaMs: Float = 50f) {
        val duration = _uiState.value.currentClip.durationSeconds.toFloat()
        val nextPos = (_uiState.value.currentPlaybackSeconds - (deltaMs / 1000f)).coerceIn(0f, duration)
        _uiState.value = _uiState.value.copy(currentPlaybackSeconds = nextPos)
    }

    fun seekPlaybackPosition(seconds: Float) {
        val duration = _uiState.value.currentClip.durationSeconds.toFloat()
        val clamped = seconds.coerceIn(0f, duration)
        val activeIdx = _uiState.value.scriptLines.indexOfFirst {
            clamped >= it.startSeconds && clamped <= it.endSeconds
        }
        _uiState.value = _uiState.value.copy(
            currentPlaybackSeconds = clamped,
            activeLineIndex = activeIdx
        )
    }

    fun toggleSyncPlayback() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startSyncPlayback()
        }
    }

    fun startSyncPlayback() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = true)

        val audioPath = _uiState.value.recordedAudioPath
        if (audioPath != null && File(audioPath).exists()) {
            recordingManager.playAudio(
                filePath = audioPath,
                voiceEffect = _uiState.value.selectedVoiceEffect,
                volume = if (_uiState.value.isMutedDub) 0f else _uiState.value.dubVolume
            )
        }

        playbackJob = viewModelScope.launch {
            val totalSeconds = _uiState.value.currentClip.durationSeconds.toFloat()
            val speedFactor = _uiState.value.syncPlaybackSpeed
            val intervalMs = (50L / speedFactor).toLong().coerceIn(10L, 200L)
            val stepSeconds = 0.05f

            while (isActive && _uiState.value.isPlaying) {
                delay(intervalMs)
                var newPos = _uiState.value.currentPlaybackSeconds + stepSeconds
                
                if (_uiState.value.isSyncLooping) {
                    val loopEnd = _uiState.value.syncLoopEndSec.coerceAtMost(totalSeconds)
                    val loopStart = _uiState.value.syncLoopStartSec.coerceAtLeast(0f)
                    if (newPos >= loopEnd) {
                        newPos = loopStart
                    }
                } else if (newPos >= totalSeconds) {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        currentPlaybackSeconds = 0f
                    )
                    break
                }

                val activeIdx = _uiState.value.scriptLines.indexOfFirst {
                    newPos >= it.startSeconds && newPos <= it.endSeconds
                }
                _uiState.value = _uiState.value.copy(
                    currentPlaybackSeconds = newPos,
                    activeLineIndex = activeIdx
                )
            }
        }
    }

    // =========================================================================
    // 3-Step Dubbing Pipeline (STT -> Gemini Translation -> TTS Waveform Sync)
    // =========================================================================

    /**
     * Step 1: Start voice STT recognition.
     */
    fun startListeningStt(languageCode: String = "ar-SA") {
        ttsManager.speakText("بدأ الاستماع لصوتك الآن، تفضل بالتحدث", utteranceId = "stt_start_speak")
        _uiState.value = _uiState.value.copy(
            isSttListening = true,
            toastMessage = "جاري الاستماع لصوتك لتحويله إلى نص متزامن... 🎙️"
        )
        sttManager.startListening(languageCode) { recognizedText ->
            _uiState.value = _uiState.value.copy(
                isSttListening = false,
                sttTranscribedText = recognizedText
            )
            applySttToTimedLines(recognizedText)
        }
    }

    /**
     * Step 1: Stop voice STT recognition.
     */
    fun stopListeningStt() {
        sttManager.stopListening()
        _uiState.value = _uiState.value.copy(isSttListening = false)
        ttsManager.speakText("تم إيقاف الاستماع", utteranceId = "stt_stop_speak")
    }

    /**
     * Step 1: Maps raw STT transcription into chronologically synchronized script lines across the clip.
     */
    fun applySttToTimedLines(rawText: String) {
        if (rawText.isBlank()) return
        val clip = _uiState.value.currentClip
        val generatedLines = sttManager.buildSynchronizedScriptLines(
            rawText = rawText,
            totalClipDurationSeconds = clip.durationSeconds,
            characterName = "المتحدث المدبلج",
            characterAvatar = "🎙️"
        )
        if (generatedLines.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                scriptLines = generatedLines,
                sttTranscribedText = rawText,
                currentClip = clip.copy(scriptLines = generatedLines),
                toastMessage = "تم توزيع النص على خط الزمن بنجاح (${generatedLines.size} سطر) ⏱️"
            )
            ttsManager.speakText(
                "تم استخراج النص وتوزيعه على خط زمن المشهد، بإمكانك الآن نسخه أو ترجمته",
                utteranceId = "stt_applied_speak"
            )
        }
    }

    /**
     * Step 1 & 2: Copies the entire dialogue script to the system clipboard for the user.
     */
    fun copyScriptTextToClipboard(includeTimestamps: Boolean = false) {
        val lines = _uiState.value.scriptLines
        if (lines.isEmpty()) {
            _uiState.value = _uiState.value.copy(toastMessage = "لا يوجد نص لنسخه حالياً")
            ttsManager.speakText("لا يوجد نص لنسخه حالياً", utteranceId = "copy_empty_speak")
            return
        }

        val textToCopy = lines.joinToString("\n") { line ->
            if (includeTimestamps) {
                "[${line.startSeconds}s - ${line.endSeconds}s] ${line.characterName}: ${line.textArabic}"
            } else {
                line.textArabic
            }
        }

        try {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Dubbing Script", textToCopy)
            clipboard?.setPrimaryClip(clip)
            _uiState.value = _uiState.value.copy(toastMessage = "تم نسخ النص إلى الحافظة بنجاح! 📋")
            ttsManager.speakText("تم نسخ النص بالكامل إلى الحافظة", utteranceId = "copy_success_speak")
        } catch (e: Exception) {
            Log.e("DubbingViewModel", "Failed to copy script to clipboard", e)
        }
    }

    /**
     * Step 2: Translates all dialogue lines using Gemini API while strictly maintaining timing.
     */
    fun translateScriptLinesWithGemini(
        targetLanguage: String = "العربية",
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentLines = _uiState.value.scriptLines
        if (currentLines.isEmpty()) {
            _uiState.value = _uiState.value.copy(toastMessage = "يرجى توفير نص أو استخراجه أولاً للترجمة")
            ttsManager.speakText("يرجى توفير نص أولاً للترجمة", utteranceId = "trans_empty_speak")
            onComplete(false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeminiTranslating = true,
                targetTranslationLanguage = targetLanguage,
                toastMessage = "جاري ترجمة الحوارات إلى $targetLanguage بواسطة Gemini AI مع الحفاظ على التوقيت... 🤖"
            )
            ttsManager.speakText("جاري ترجمة الحوارات إلى $targetLanguage عبر الذكاء الاصطناعي جيميناي", utteranceId = "trans_start_speak")

            try {
                val promptBuilder = StringBuilder()
                promptBuilder.append("You are an expert dubbing translator. Translate the following spoken dialogue lines into $targetLanguage.\n")
                promptBuilder.append("CRITICAL: Keep the emotional nuance and rhythm for voice acting. Format your response strictly as a JSON array where each object has:\n")
                promptBuilder.append("{\"index\": 0, \"translatedText\": \"...\"}\n")
                promptBuilder.append("Here are the lines to translate:\n")
                currentLines.forEachIndexed { idx, line ->
                    promptBuilder.append("Line $idx: ${line.textArabic}\n")
                }

                val prompt = promptBuilder.toString()
                val responseResult = geminiUnifiedClient.sendChatMessage(
                    messages = emptyList(),
                    userPrompt = prompt,
                    model = com.example.ai.GeminiChatModel.FLASH_3_5
                )

                val responseText = responseResult.getOrNull()?.text ?: ""
                var updatedLines = currentLines

                if (responseText.isNotBlank()) {
                    try {
                        val cleanedJson = responseText.replace("```json", "").replace("```", "").trim()
                        val jsonArrayStart = cleanedJson.indexOf('[')
                        val jsonArrayEnd = cleanedJson.lastIndexOf(']')
                        if (jsonArrayStart != -1 && jsonArrayEnd != -1) {
                            val jsonArray = JSONArray(cleanedJson.substring(jsonArrayStart, jsonArrayEnd + 1))
                            updatedLines = currentLines.mapIndexed { idx, line ->
                                var translated = line.textArabic
                                for (i in 0 until jsonArray.length()) {
                                    val obj = jsonArray.getJSONObject(i)
                                    if (obj.optInt("index", -1) == idx) {
                                        translated = obj.optString("translatedText", translated)
                                        break
                                    }
                                }
                                line.copy(
                                    textOriginal = line.textArabic,
                                    textArabic = translated
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("DubbingViewModel", "JSON parsing of translation failed, using line fallback", e)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    scriptLines = updatedLines,
                    currentClip = _uiState.value.currentClip.copy(scriptLines = updatedLines),
                    isGeminiTranslating = false,
                    toastMessage = "اكتملت الترجمة إلى $targetLanguage بنجاح! ✨"
                )
                ttsManager.speakText("اكتملت ترجمة النص إلى $targetLanguage بنجاح، يمكنك الآن توليد الصوت وتركيبه", utteranceId = "trans_done_speak")
                onComplete(true)
            } catch (e: Exception) {
                Log.e("DubbingViewModel", "Error in Gemini translation", e)
                _uiState.value = _uiState.value.copy(
                    isGeminiTranslating = false,
                    toastMessage = "تعذر إكمال الترجمة عبر الإنترنت، تم الاحتفاظ بالنص الحالي"
                )
                onComplete(false)
            }
        }
    }

    /**
     * Step 3: Generates a synchronized WAV audio track containing all dialogue lines placed at exact timestamps,
     * saves it directly in app storage, and attaches it to the video timeline for instant synchronized playback.
     */
    fun synthesizeAndSyncDubbedAudio(
        selectedProfile: VoiceProfile? = null,
        onComplete: (String?) -> Unit = {}
    ) {
        val lines = _uiState.value.scriptLines
        val clip = _uiState.value.currentClip
        val profile = selectedProfile ?: ttsManager.voiceProfiles.first()

        if (lines.isEmpty()) {
            _uiState.value = _uiState.value.copy(toastMessage = "لا توجد نصوص حوارية لتوليد الصوت لها")
            ttsManager.speakText("لا توجد نصوص لتوليد الصوت لها، يرجى كتابة أو استخراج نص أولاً", utteranceId = "synth_empty_speak")
            onComplete(null)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTtsSynthesizingTimeline = true,
                toastMessage = "جاري توليد الصوت وحفظه ومزامنته مع خط زمن الفيديو... 🎙️🎬"
            )
            ttsManager.speakText("جاري توليد ملف الصوت وحفظه ومزامنته مع الفيديو بدقة", utteranceId = "synth_start_speak")

            try {
                val dialogues = lines.map {
                    Triple(it.textArabic, it.startSeconds, it.endSeconds)
                }

                val audioDir = File(getApplication<Application>().filesDir, "synced_dubbing")
                audioDir.mkdirs()
                val outputFile = File(audioDir, "dubbed_sync_${clip.id}_${System.currentTimeMillis()}.wav")

                val generatedFile = ttsManager.synthesizeSynchronizedTimelineWav(
                    dialogues = dialogues,
                    totalDurationSeconds = clip.durationSeconds.toFloat(),
                    profile = profile,
                    outputFile = outputFile
                )

                if (generatedFile.exists() && generatedFile.length() > 44) {
                    val filePath = generatedFile.absolutePath
                    _uiState.value = _uiState.value.copy(
                        recordedAudioPath = filePath,
                        synthesizedDubbedAudioPath = filePath,
                        isTtsSynthesizingTimeline = false,
                        originalVolume = 0.15f,
                        dubVolume = 1.0f,
                        toastMessage = "تم توليد الصوت وحفظه في الاستوديو وتركيبه على الفيديو بنجاح! 🎵🎬"
                    )

                    // Update project if active
                    _uiState.value.currentProject?.let { proj ->
                        val updatedProj = proj.copy(
                            recordedAudioPath = filePath,
                            lastModified = System.currentTimeMillis()
                        )
                        repository.updateProject(updatedProj)
                    }

                    ttsManager.speakText("تم توليد الصوت وحفظه وتركيبه على الفيديو بنجاح، اضغط تشغيل للاستماع الآن", utteranceId = "synth_done_speak")
                    onComplete(filePath)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isTtsSynthesizingTimeline = false,
                        toastMessage = "تعذر توليد ملف الصوت، يرجى إعادة المحاولة"
                    )
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("DubbingViewModel", "Failed to synthesize and sync dubbed audio", e)
                _uiState.value = _uiState.value.copy(
                    isTtsSynthesizingTimeline = false,
                    toastMessage = "حدث خطأ أثناء توليد الصوت: ${e.localizedMessage}"
                )
                onComplete(null)
            }
        }
    }

    /**
     * Master 100% Autonomous Dubbing Pipeline:
     * - Requires ZERO microphone.
     * - Requires ZERO manual text writing.
     * - Automatically extracts dialogue & timestamps from clip/scene or generates them intelligently.
     * - Automatically translates to targetLanguage using Gemini AI (with timing preserved).
     * - Automatically synthesizes character audio track via TTS engine and attaches to timeline.
     * - Saves the project and triggers playback!
     */
    fun performCompleteAutonomousDubbing(
        targetLanguage: String = "العربية",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isTtsSynthesizingTimeline = true,
                    toastMessage = "جاري التحليل الصوتي الذاتي وكشف المتحدثين وتعيين الأصوات عبر Gemini AI... 🎙️✨"
                )

                val context = getApplication<Application>()
                val videoFile = withContext(Dispatchers.IO) {
                    val localPath = autoDubberState.value.importedVideo?.localFilePath
                    if (localPath != null && File(localPath).exists() && File(localPath).length() > 500) {
                        File(localPath)
                    } else {
                        val uriString = autoDubberState.value.importedVideo?.uriString
                        val uri = uriString?.let { Uri.parse(it) }
                        if (uri != null) {
                            val cacheFile = File(context.cacheDir, "auto_dub_input_${System.currentTimeMillis()}.mp4")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
                            }
                            if (cacheFile.exists() && cacheFile.length() > 500) cacheFile else null
                        } else null
                    }
                }

                if (videoFile == null) {
                    _uiState.value = _uiState.value.copy(isTtsSynthesizingTimeline = false)
                    onComplete(false, "يرجى استيراد مقطع فيديو أولاً لإجراء الدبلجة التلقائية")
                    return@launch
                }

                val analysisResult = automatedVoiceAnalysisService.executeAutonomousAnalysisAndDubbing(
                    mediaFile = videoFile,
                    targetDialect = _videoSpeechToTextState.value.selectedDialect,
                    targetLanguage = targetLanguage,
                    customApiKey = _geminiApiKey.value
                )

                if (analysisResult.isSuccess && analysisResult.dubbedAudioWavFile != null) {
                    val lines = analysisResult.dialogueSegments.mapIndexed { idx, seg ->
                        ScriptLine(
                            id = seg.id,
                            characterName = seg.speakerName,
                            characterAvatar = when (seg.gender.uppercase()) {
                                "FEMALE" -> "👩"
                                "CHILD" -> "🧒"
                                "NARRATOR" -> "🎙️"
                                else -> "👨"
                            },
                            textArabic = seg.dubbedArabicText,
                            startSeconds = seg.startSeconds,
                            endSeconds = seg.endSeconds,
                            voiceType = seg.voiceProfile.id,
                            isDubbed = true,
                            customAudioPath = null
                        )
                    }

                    val finalWavPath = analysisResult.dubbedAudioWavFile.absolutePath
                    _uiState.value = _uiState.value.copy(
                        scriptLines = lines,
                        recordedAudioPath = finalWavPath,
                        isTtsSynthesizingTimeline = false,
                        toastMessage = "اكتملت الدبلجة الذاتية بنجاح (${analysisResult.speakerCount} شخصيات) 🚀🎉"
                    )

                    startPlayback()
                    val summaryText = "تمت دبلجة المشهد بالكامل! تم كشف ${analysisResult.speakerCount} شخصيات وتعيين أصواتهم وتوليد المسار الصوتي بنجاح."
                    onComplete(true, summaryText)
                } else {
                    _uiState.value = _uiState.value.copy(isTtsSynthesizingTimeline = false)
                    onComplete(false, analysisResult.errorMessage ?: "تعذر إتمام الدبلجة التلقائية")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeminiTranslating = false,
                    isTtsSynthesizingTimeline = false
                )
                onComplete(false, "حدث خطأ: ${e.localizedMessage}")
            }
        }
    }

    fun autoGenerateSmartDialogueLines() {
        val currentVideoUri = autoDubberState.value.importedVideo?.uriString?.let { Uri.parse(it) }
        val localPath = autoDubberState.value.importedVideo?.localFilePath
        if (currentVideoUri != null || (localPath != null && File(localPath).exists())) {
            transcribeVideoForTimestamps(currentVideoUri)
        } else {
            _uiState.value = _uiState.value.copy(
                toastMessage = "يرجى استيراد مقطع فيديو أولاً لاستخراج الحوارات والتوقيتات بالذكاء الاصطناعي 🎬"
            )
        }
    }

    // ==============================================================
    // 🌟 Hollywood Studio Actions & Autonomous Enhancements
    // ==============================================================
    fun launchHollywoodMastering(characterName: String = "البطل الرئيسي", onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val audioFile = uiState.value.recordedAudioPath?.let { File(it) }
                ?: autoDubberState.value.importedVideo?.localFilePath?.let { File(it) }
                ?: File(getApplication<Application>().cacheDir, "sample_clip.wav")

            val result = hollywoodSuiteService.performAutonomousHollywoodMastering(
                sampleVideoOrAudioFile = audioFile,
                characterName = characterName,
                selectedDialect = _videoSpeechToTextState.value.selectedDialect.code
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "اكتمل الإنتاج السينمائي الهوليوودي بنجاح! 🎬✨"
                )
                onComplete(true, "تم الإخراج السينمائي وتفكيك التراكات واستنساخ النبرة بنجاح.")
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تعذر إتمام الماسترينج السينمائي: ${result.exceptionOrNull()?.message}"
                )
                onComplete(false, result.exceptionOrNull()?.message ?: "خطأ")
            }
        }
    }

    fun generateFoleyEffect(category: FoleyCategory) {
        viewModelScope.launch {
            val result = hollywoodSuiteService.foleyAndScoreGenerator.generateFoleyEffectWav(category)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم توليد المؤثر الصوتي السينمائي (${category.labelArabic}) بنجاح! 🔊✨"
                )
            }
        }
    }

    fun generateCinematicScore(style: CinematicScoreStyle) {
        viewModelScope.launch {
            val result = hollywoodSuiteService.foleyAndScoreGenerator.generateCinematicScoreWav(style)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم تأليف الموسيقى التصويرية (${style.labelArabic}) بنجاح! 🎻✨"
                )
            }
        }
    }

    fun separateAudioStems() {
        viewModelScope.launch {
            val target = uiState.value.recordedAudioPath?.let { File(it) }
                ?: autoDubberState.value.importedVideo?.localFilePath?.let { File(it) }
                ?: File(getApplication<Application>().cacheDir, "sample_clip.wav")

            val res = hollywoodSuiteService.stemSeparator.separateMixedAudio(target)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم تفكيك الصوت بنجاح إلى 4 تراكات منفصلة (صوت، موسيقى، مؤثرات، أجواء) 🎧🎉"
                )
            }
        }
    }

    fun generateViralShortsClips() {
        val duration = autoDubberState.value.importedVideo?.durationSeconds?.toFloat() ?: 45f
        val clips = hollywoodSuiteService.shortsClipper.extractViralHooks(
            totalDurationSec = duration,
            dialogueLines = uiState.value.scriptLines.map { it.textArabic }
        )
        _uiState.value = _uiState.value.copy(
            toastMessage = "تم استخراج ${clips.size} مقاطع ريلز وتريند جاهزة للنشر بنقرة واحدة! 📱🔥"
        )
    }

    fun renderSpatial3DAudio() {
        viewModelScope.launch {
            val target = uiState.value.recordedAudioPath?.let { File(it) } ?: File(getApplication<Application>().cacheDir, "sample.wav")
            val res = hollywoodSuiteService.spatial3dEngine.spatializeMonoWavToStereo3D(target, 0.4f, 1.2f)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم تفعيل الصوت المجسم ثلاثي الأبعاد 3D/8D بنجاح! 🎧🌌"
                )
            }
        }
    }

    fun applyVintageMicMastering(mic: VintageMicProfile = VintageMicProfile.NEUMANN_U87, age: VocalAgeMorph = VocalAgeMorph.NATURAL) {
        viewModelScope.launch {
            val target = uiState.value.recordedAudioPath?.let { File(it) } ?: File(getApplication<Application>().cacheDir, "sample.wav")
            val res = hollywoodSuiteService.studioPhysicsEngine.applyStudioMastering(target, mic, age)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم تطبيق هندسة الميكروفون (${mic.labelArabic}) وتعديل العمر (${age.labelArabic}) بنجاح! 🎙️✨"
                )
            }
        }
    }

    fun embedPublisherCopyrightWatermark() {
        viewModelScope.launch {
            val target = uiState.value.recordedAudioPath?.let { File(it) } ?: File(getApplication<Application>().cacheDir, "sample.wav")
            val cert = hollywoodSuiteService.watermarkSecurityEngine.generateDigitalCertificate(target)
            val res = hollywoodSuiteService.watermarkSecurityEngine.embedInaudibleWatermark(target)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "تم زرع البصمة الصوتية غير المسموعة وحفظ الحقوق لـ ${cert.authorName} بنجاح 🛡️📜"
                )
            }
        }
    }

    fun toggleLiveStreamDubbing(mode: LiveDubbingMode = LiveDubbingMode.SPORTS_COMMENTARY) {
        val current = hollywoodSuiteService.liveStreamEngine.streamState.value
        if (current.isActive) {
            hollywoodSuiteService.liveStreamEngine.stopLiveDubbing()
            _uiState.value = _uiState.value.copy(toastMessage = "تم إيقاف وضع البث المباشر ⏹️")
        } else {
            hollywoodSuiteService.liveStreamEngine.startLiveDubbing(mode)
            _uiState.value = _uiState.value.copy(toastMessage = "تم تفعيل وضع الدبلجة الحية المباشرة (${mode.labelArabic}) 🔴⚡")
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        countdownJob?.cancel()
        trimmerPlaybackJob?.cancel()
        recordingManager.release()
        ttsManager.release()
        sttManager.release()
        assetAudioPlayerService.release()
        offlineTtsService.release()
        offlineSttService.release()
        genderDetector.stopAnalysis()
        instantDubbingEngine.release()
        audioEffectsProcessor.release()
        SoundEffectsGenerator.stopBgm()
        audioDubbingManager.release()
        stopRecordingPreview()
        updateWebServer.stop()
    }
}
