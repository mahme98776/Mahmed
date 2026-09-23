package com.example.audio.assets.domain.usecase

import com.example.audio.assets.domain.model.AudioAssetCategory
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import com.example.audio.assets.domain.repository.AudioAssetsRepository
import com.example.audio.assets.domain.service.AssetAudioPlayerService
import com.example.audio.assets.domain.service.OfflineSttService
import com.example.audio.assets.domain.service.OfflineTtsService
import java.io.File

/**
 * حالات استخدام Clean Architecture لإدارة أصول الصوت وتشغيلها
 */
class GetAudioAssetsUseCase(private val repository: AudioAssetsRepository) {
    suspend operator fun invoke(category: AudioAssetCategory? = null): ResourceResult<List<AudioAssetItem>> {
        return if (category == null) {
            repository.getAudioAssets()
        } else {
            repository.getAudioAssetsByCategory(category)
        }
    }
}

class PlayAudioAssetUseCase(private val playerService: AssetAudioPlayerService) {
    suspend operator fun invoke(item: AudioAssetItem): ResourceResult<Unit> {
        return playerService.playAsset(item)
    }
}

class SynthesizeOfflineTtsUseCase(private val ttsService: OfflineTtsService) {
    suspend operator fun invoke(
        text: String,
        outputFile: File,
        languageCode: String = "ar",
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): ResourceResult<File> {
        return ttsService.synthesizeToWavFile(text, outputFile, languageCode, pitch, speechRate)
    }
}

class TranscribeOfflineSpeechUseCase(private val sttService: OfflineSttService) {
    suspend operator fun invoke(
        audioFile: File,
        languageCode: String = "ar-SA"
    ): ResourceResult<String> {
        return sttService.transcribeAudioFile(audioFile, languageCode)
    }
}
