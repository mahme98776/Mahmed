package com.example.alad

import android.content.Context
import android.util.Log
import com.example.ai.GeminiUnifiedClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ALAD Mobile - Master AI Live Audio Dubbing Controller
 * Coordinates live internal/mic audio capture, Gemini 3.5 Live Translate streaming,
 * dynamic audio ducking, and low-latency voice synthesis.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladLiveDubbingEngine(
    private val context: Context,
    private val geminiClient: GeminiUnifiedClient
) {
    private val tag = "AladLiveDubbingEngine"
    private val scope = CoroutineScope(Dispatchers.Main)

    val duckingManager = AladAudioDuckingManager(context)
    val captureEngine = AladAudioCaptureEngine(context)
    val liveClient = AladGeminiLiveClient(context, geminiClient, duckingManager)

    private val _isLiveDubbing = MutableStateFlow(false)
    val isLiveDubbing: StateFlow<Boolean> = _isLiveDubbing.asStateFlow()

    private val _targetLanguage = MutableStateFlow(AladLanguageCatalog.findByCode("ar"))
    val targetLanguage: StateFlow<AladLanguage> = _targetLanguage.asStateFlow()

    private val _selectedTargetApp = MutableStateFlow("YouTube")
    val selectedTargetApp: StateFlow<String> = _selectedTargetApp.asStateFlow()

    private val _audioSourceMode = MutableStateFlow(AladAudioSource.INTERNAL_AND_MIC)
    val audioSourceMode: StateFlow<AladAudioSource> = _audioSourceMode.asStateFlow()

    private val _isDemoSimulationRunning = MutableStateFlow(false)
    val isDemoSimulationRunning: StateFlow<Boolean> = _isDemoSimulationRunning.asStateFlow()

    private var simulationJob: Job? = null

    enum class AladAudioSource(val titleArabic: String, val descArabic: String) {
        INTERNAL_AND_MIC("التقاط صوت النظام الداخلي + الميكروفون", "يدبلج التطبيقات الخارجية مباشرة مثل يوتيوب ونتفليكس مع عزل الضوضاء"),
        MIC_ONLY("الميكروفون عالي النقاء فقط", "لالتقاط صوت السماعة الخارجية أو المكالمات والغرف الصوتية")
    }

    fun setTargetLanguage(language: AladLanguage) {
        _targetLanguage.value = language
    }

    fun setTargetApp(appName: String) {
        _selectedTargetApp.value = appName
    }

    fun setAudioSource(source: AladAudioSource) {
        _audioSourceMode.value = source
    }

    /**
     * Start live AI audio dubbing session
     */
    fun startLiveDubbing(): Boolean {
        if (_isLiveDubbing.value) return true

        val success = captureEngine.startCapture()
        if (success) {
            _isLiveDubbing.value = true
            Log.d(tag, "ALAD Live Dubbing Started for ${_selectedTargetApp.value} -> ${_targetLanguage.value.nameArabic}")
        }
        return success
    }

    /**
     * Stop live dubbing session
     */
    fun stopLiveDubbing() {
        _isLiveDubbing.value = false
        captureEngine.stopCapture()
        liveClient.stopSpeaking()
        duckingManager.release()
        stopDemoSimulation()
        Log.d(tag, "ALAD Live Dubbing Stopped")
    }

    /**
     * Runs a live interactive simulation of foreign audio stream (e.g., documentary / podcast in English)
     * being translated and dubbed into the chosen target language with real-time ducking!
     */
    fun startDemoSimulation(customApiKey: String = "") {
        if (_isDemoSimulationRunning.value) return
        _isDemoSimulationRunning.value = true
        _isLiveDubbing.value = true

        val demoSentences = listOf(
            "Welcome everyone to this live international documentary broadcast.",
            "Today we are exploring advanced artificial intelligence and cyber security frontiers.",
            "Our team is demonstrating real-time voice translation across seventy-eight languages.",
            "Notice how the background audio automatically ducks whenever the dubbed voice speaks.",
            "All intellectual property and publishing rights are protected for Mohamed Salima."
        )

        simulationJob = scope.launch(Dispatchers.IO) {
            var index = 0
            while (isActive && _isDemoSimulationRunning.value) {
                val sentence = demoSentences[index % demoSentences.size]
                liveClient.processLiveSpeechChunk(sentence, _targetLanguage.value, customApiKey)
                index++
                delay(4800L)
            }
        }
    }

    fun stopDemoSimulation() {
        _isDemoSimulationRunning.value = false
        simulationJob?.cancel()
        simulationJob = null
        liveClient.stopSpeaking()
    }

    fun release() {
        stopLiveDubbing()
        liveClient.release()
    }
}
