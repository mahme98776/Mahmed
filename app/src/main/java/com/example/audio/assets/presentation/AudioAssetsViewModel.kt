package com.example.audio.assets.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.assets.data.repository.AudioAssetsRepositoryImpl
import com.example.audio.assets.data.service.AssetExoAudioPlayerService
import com.example.audio.assets.data.service.OfflineSttServiceImpl
import com.example.audio.assets.data.service.OfflineTtsServiceImpl
import com.example.audio.assets.domain.model.AssetPlaybackState
import com.example.audio.assets.domain.model.AudioAssetCategory
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import com.example.audio.assets.domain.repository.AudioAssetsRepository
import com.example.audio.assets.domain.service.AssetAudioPlayerService
import com.example.audio.assets.domain.service.OfflineSttService
import com.example.audio.assets.domain.service.OfflineSttState
import com.example.audio.assets.domain.service.OfflineTtsService
import com.example.audio.assets.domain.usecase.GetAudioAssetsUseCase
import com.example.audio.assets.domain.usecase.PlayAudioAssetUseCase
import com.example.audio.assets.domain.usecase.SynthesizeOfflineTtsUseCase
import com.example.audio.assets.domain.usecase.TranscribeOfflineSpeechUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * حالة واجهة المستخدم لشاشة مدير أصول الصوت والمعالجة المحلية.
 */
data class AudioAssetsManagerUiState(
    val isLoading: Boolean = false,
    val selectedCategory: AudioAssetCategory? = null,
    val assets: List<AudioAssetItem> = emptyList(),
    val filteredAssets: List<AudioAssetItem> = emptyList(),
    val searchQuery: String = "",

    // Offline TTS State
    val ttsInputText: String = "أهلاً بكم في فويس ماستر برو، نظام معالجة ودبلجة الصوت المحلي بدون إنترنت.",
    val isTtsSynthesizing: Boolean = false,
    val lastGeneratedWavFile: File? = null,
    val ttsPitch: Float = 1.0f,
    val ttsSpeed: Float = 1.0f,
    val selectedLanguageCode: String = "ar",

    // Offline STT State
    val sttTranscribedResult: String = "",
    val isSttTranscribingFile: Boolean = false,

    // Feedback
    val userNoticeMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel مسؤول عن تنسيق إدارة وتشغيل الصوتيات من assets مع تقنيات TTS/STT المحلية
 * بالاعتماد الصارم على مبادئ Clean Architecture و Coroutines وإدارة الذاكرة.
 */
class AudioAssetsViewModel(application: Application) : AndroidViewModel(application) {

    // Dependency Injection via clean composition
    val repository: AudioAssetsRepository = AudioAssetsRepositoryImpl(application)
    val playerService: AssetAudioPlayerService = AssetExoAudioPlayerService(application, repository)
    val offlineTtsService: OfflineTtsService = OfflineTtsServiceImpl(application)
    val offlineSttService: OfflineSttService = OfflineSttServiceImpl(application)

    // Use cases
    private val getAudioAssetsUseCase = GetAudioAssetsUseCase(repository)
    private val playAudioAssetUseCase = PlayAudioAssetUseCase(playerService)
    private val synthesizeOfflineTtsUseCase = SynthesizeOfflineTtsUseCase(offlineTtsService)
    private val transcribeOfflineSpeechUseCase = TranscribeOfflineSpeechUseCase(offlineSttService)

    // Playback state exposed directly from service
    val playbackState: StateFlow<AssetPlaybackState> = playerService.playbackState

    // STT State
    val sttState: StateFlow<OfflineSttState> = offlineSttService.state

    private val _uiState = MutableStateFlow(AudioAssetsManagerUiState())
    val uiState: StateFlow<AudioAssetsManagerUiState> = _uiState.asStateFlow()

    init {
        loadAssets()
    }

    fun loadAssets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = getAudioAssetsUseCase(_uiState.value.selectedCategory)) {
                is ResourceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        assets = res.data,
                        filteredAssets = filterAssets(res.data, _uiState.value.searchQuery)
                    )
                }
                is ResourceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.messageArabic
                    )
                }
                is ResourceResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun selectCategory(category: AudioAssetCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadAssets()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredAssets = filterAssets(_uiState.value.assets, query)
        )
    }

    private fun filterAssets(list: List<AudioAssetItem>, query: String): List<AudioAssetItem> {
        if (query.isBlank()) return list
        return list.filter {
            it.titleArabic.contains(query, ignoreCase = true) ||
                    it.fileName.contains(query, ignoreCase = true) ||
                    it.category.titleArabic.contains(query, ignoreCase = true)
        }
    }

    fun playAsset(item: AudioAssetItem) {
        viewModelScope.launch {
            val res = playAudioAssetUseCase(item)
            if (res is ResourceResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = res.messageArabic)
            }
        }
    }

    fun playFile(file: File, titleArabic: String = file.name) {
        viewModelScope.launch {
            val res = playerService.playFile(file, titleArabic)
            if (res is ResourceResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = res.messageArabic)
            }
        }
    }

    fun togglePlayPause() {
        val s = playbackState.value
        if (s.isPlaying) {
            playerService.pause()
        } else {
            if (s.currentItem != null) {
                playerService.resume()
            }
        }
    }

    fun stopPlayback() {
        playerService.stop()
    }

    fun seekTo(positionMs: Long) {
        playerService.seekTo(positionMs)
    }

    fun setVolume(vol: Float) {
        playerService.setVolume(vol)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerService.setPlaybackSpeed(speed)
    }

    // --- Offline TTS Functions ---

    fun setTtsInputText(text: String) {
        _uiState.value = _uiState.value.copy(ttsInputText = text)
    }

    fun setTtsPitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(ttsPitch = pitch)
    }

    fun setTtsSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(ttsSpeed = speed)
    }

    fun setLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(selectedLanguageCode = lang)
    }

    fun speakTtsDirectly() {
        val text = _uiState.value.ttsInputText
        viewModelScope.launch {
            offlineTtsService.speakText(
                text = text,
                languageCode = _uiState.value.selectedLanguageCode,
                pitch = _uiState.value.ttsPitch,
                speechRate = _uiState.value.ttsSpeed
            )
        }
    }

    fun generateOfflineTtsWav() {
        val text = _uiState.value.ttsInputText
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTtsSynthesizing = true, errorMessage = null)
            val destFile = File(getApplication<Application>().cacheDir, "offline_tts_${System.currentTimeMillis()}.wav")

            when (val res = synthesizeOfflineTtsUseCase(
                text = text,
                outputFile = destFile,
                languageCode = _uiState.value.selectedLanguageCode,
                pitch = _uiState.value.ttsPitch,
                speechRate = _uiState.value.ttsSpeed
            )) {
                is ResourceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTtsSynthesizing = false,
                        lastGeneratedWavFile = res.data,
                        userNoticeMessage = "تم توليد ملف الصوت المحلي (WAV) بنجاح وبدون إنترنت! 🎙️✨"
                    )
                    // Play immediately via ExoPlayer
                    playerService.playFile(res.data, "الصوت المولّد محلياً")
                }
                is ResourceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isTtsSynthesizing = false,
                        errorMessage = res.messageArabic
                    )
                }
                is ResourceResult.Loading -> {}
            }
        }
    }

    // --- Offline STT Functions ---

    fun startOfflineVoiceRecognition() {
        val lang = if (_uiState.value.selectedLanguageCode.startsWith("ar")) "ar-SA" else "en-US"
        offlineSttService.startListening(lang) { recognizedText ->
            _uiState.value = _uiState.value.copy(
                sttTranscribedResult = recognizedText,
                ttsInputText = recognizedText,
                userNoticeMessage = "تم التعرف على الصوت محلياً: \"$recognizedText\""
            )
        }
    }

    fun stopOfflineVoiceRecognition() {
        offlineSttService.stopListening()
    }

    fun transcribeAssetAudio(item: AudioAssetItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSttTranscribingFile = true, errorMessage = null)
            val fileRes = repository.copyAssetToCache(item.assetPath)
            when (fileRes) {
                is ResourceResult.Success -> {
                    when (val sttRes = transcribeOfflineSpeechUseCase(fileRes.data)) {
                        is ResourceResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isSttTranscribingFile = false,
                                sttTranscribedResult = sttRes.data,
                                userNoticeMessage = "تم تحويل ملف الأصول الصوتي إلى نص بنجاح! 📝"
                            )
                        }
                        is ResourceResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isSttTranscribingFile = false,
                                errorMessage = sttRes.messageArabic
                            )
                        }
                        is ResourceResult.Loading -> {}
                    }
                }
                is ResourceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSttTranscribingFile = false,
                        errorMessage = fileRes.messageArabic
                    )
                }
                is ResourceResult.Loading -> {}
            }
        }
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(userNoticeMessage = null, errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        playerService.release()
        offlineTtsService.release()
        offlineSttService.release()
    }
}
