package com.example.audio.assets.domain.repository

import com.example.audio.assets.domain.model.AudioAssetCategory
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import java.io.File

/**
 * واجهة مستودع إدارة أصول الصوت من مجلد assets وتخزينها المؤقت بكفاءة عالية في الذاكرة.
 */
interface AudioAssetsRepository {
    suspend fun getAudioAssets(): ResourceResult<List<AudioAssetItem>>
    suspend fun getAudioAssetsByCategory(category: AudioAssetCategory): ResourceResult<List<AudioAssetItem>>
    suspend fun copyAssetToCache(assetPath: String): ResourceResult<File>
    suspend fun clearAssetCache(): ResourceResult<Unit>
    fun getCachedFileForAsset(assetPath: String): File?
}
