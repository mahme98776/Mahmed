package com.example.audio.assets.data.repository

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaMetadataRetriever
import android.util.Log
import com.example.audio.assets.domain.model.AudioAssetCategory
import com.example.audio.assets.domain.model.AudioAssetItem
import com.example.audio.assets.domain.model.ResourceResult
import com.example.audio.assets.domain.repository.AudioAssetsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * تنفيذ مستودع إدارة أصول الصوت مع إدارة ذكية للذاكرة والتخزين المؤقت النظيف.
 */
class AudioAssetsRepositoryImpl(
    private val context: Context
) : AudioAssetsRepository {

    private val tag = "AudioAssetsRepo"
    private val assetManager: AssetManager = context.assets
    private val cacheMap = ConcurrentHashMap<String, File>()

    private val cacheDirectory: File by lazy {
        File(context.cacheDir, "audio_assets_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun getAudioAssets(): ResourceResult<List<AudioAssetItem>> = withContext(Dispatchers.IO) {
        try {
            val list = mutableListOf<AudioAssetItem>()
            val categories = listOf(
                "audio/soundtracks" to AudioAssetCategory.SOUNDTRACKS,
                "audio/voiceovers" to AudioAssetCategory.VOICEOVERS,
                "audio/effects" to AudioAssetCategory.EFFECTS,
                "audio/samples" to AudioAssetCategory.SAMPLES
            )

            for ((folder, category) in categories) {
                val fileNames = try {
                    assetManager.list(folder) ?: emptyArray()
                } catch (e: Exception) {
                    Log.w(tag, "Could not list assets in $folder", e)
                    emptyArray<String>()
                }

                for (name in fileNames) {
                    if (name.endsWith(".wav", ignoreCase = true) ||
                        name.endsWith(".mp3", ignoreCase = true) ||
                        name.endsWith(".m4a", ignoreCase = true)
                    ) {
                        val assetPath = "$folder/$name"
                        val size = getAssetSize(assetPath)
                        val title = generateArabicTitle(name, category)
                        val cachedFile = cacheMap[assetPath]?.takeIf { it.exists() }

                        // Calculate duration if already cached or quickly extract
                        val duration = cachedFile?.let { extractDuration(it) } ?: 3000L

                        list.add(
                            AudioAssetItem(
                                assetPath = assetPath,
                                fileName = name,
                                titleArabic = title,
                                category = category,
                                sizeBytes = size,
                                durationMs = duration,
                                format = name.substringAfterLast('.').uppercase(),
                                isCached = cachedFile != null,
                                cachedFile = cachedFile
                            )
                        )
                    }
                }
            }

            ResourceResult.Success(list)
        } catch (e: Exception) {
            Log.e(tag, "Failed to load audio assets", e)
            ResourceResult.Error(
                messageArabic = "تعذر قراءة ملفات الصوت من مجلد الأصول: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    override suspend fun getAudioAssetsByCategory(category: AudioAssetCategory): ResourceResult<List<AudioAssetItem>> = withContext(Dispatchers.IO) {
        when (val all = getAudioAssets()) {
            is ResourceResult.Success -> {
                ResourceResult.Success(all.data.filter { it.category == category })
            }
            is ResourceResult.Error -> all
            is ResourceResult.Loading -> ResourceResult.Loading
        }
    }

    override suspend fun copyAssetToCache(assetPath: String): ResourceResult<File> = withContext(Dispatchers.IO) {
        try {
            // Check cache map first for memory & IO efficiency
            val existing = cacheMap[assetPath]
            if (existing != null && existing.exists() && existing.length() > 0) {
                return@withContext ResourceResult.Success(existing)
            }

            val fileName = assetPath.substringAfterLast('/')
            val subFolder = assetPath.substringBeforeLast('/', "").replace('/', '_')
            val destFile = File(cacheDirectory, "${subFolder}_$fileName")

            if (destFile.exists() && destFile.length() > 0) {
                cacheMap[assetPath] = destFile
                return@withContext ResourceResult.Success(destFile)
            }

            // Buffer-based streaming to prevent OutOfMemoryError on large audio assets
            assetManager.open(assetPath).use { input: InputStream ->
                FileOutputStream(destFile).use { output: FileOutputStream ->
                    val buffer = ByteArray(8 * 1024) // 8KB buffer for optimal performance
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            cacheMap[assetPath] = destFile
            Log.i(tag, "Successfully copied asset '$assetPath' to cache: ${destFile.absolutePath}")
            ResourceResult.Success(destFile)
        } catch (e: Exception) {
            Log.e(tag, "Error extracting asset to cache: $assetPath", e)
            ResourceResult.Error(
                messageArabic = "فشل استخراج ملف الصوت من مجلد الأصول إلى الذاكرة المؤقتة: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    override suspend fun clearAssetCache(): ResourceResult<Unit> = withContext(Dispatchers.IO) {
        try {
            cacheMap.clear()
            if (cacheDirectory.exists()) {
                cacheDirectory.listFiles()?.forEach { it.delete() }
            }
            Log.i(tag, "Asset cache successfully cleared")
            ResourceResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error clearing asset cache", e)
            ResourceResult.Error(
                messageArabic = "فشل تنظيف ذاكرة التخزين المؤقت لملفات الصوت: ${e.localizedMessage}",
                throwable = e
            )
        }
    }

    override fun getCachedFileForAsset(assetPath: String): File? {
        return cacheMap[assetPath]?.takeIf { it.exists() }
    }

    private fun getAssetSize(assetPath: String): Long {
        return try {
            val fd = assetManager.openFd(assetPath)
            val len = fd.length
            fd.close()
            len
        } catch (_: Exception) {
            try {
                assetManager.open(assetPath).use { it.available().toLong() }
            } catch (_: Exception) {
                0L
            }
        }
    }

    private fun extractDuration(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            time?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateArabicTitle(fileName: String, category: AudioAssetCategory): String {
        val nameWithoutExt = fileName.substringBeforeLast('.')
        return when (nameWithoutExt) {
            "ambient_cinematic" -> "موسيقى سينمائية هادئة وعميقة"
            "epic_orchestral_intro" -> "مقدمة أوركسترالية حماسية وملحمية"
            "arabic_sample_intro" -> "تسجيل ترحيبي صوتي باللغة العربية"
            "english_sample_narrator" -> "تسجيل تعليق صوتي باللغة الإنجليزية"
            "whoosh_transition" -> "تأثير انتقال هوائي سريع (Whoosh)"
            "bell_notification" -> "جرس تنبيه سينمائي نقي"
            "dialogue_arabic_scene1" -> "حوار تمثيلي تجريبي للمزامنة"
            else -> nameWithoutExt.replace('_', ' ').replace('-', ' ')
        }
    }
}
