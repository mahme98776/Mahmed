package com.example.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object AudioMultipartHelper {

    /**
     * Resolves an audio [Uri] into a local cache [File], extracting its true file name.
     */
    suspend fun uriToCacheFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            var fileName = "audio_upload_${System.currentTimeMillis()}.mp3"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val displayName = cursor.getString(nameIndex)
                    if (!displayName.isNullOrBlank()) {
                        fileName = displayName
                    }
                }
            }

            val tempFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("AudioMultipartHelper", "Failed to cache uri: $uri", e)
            null
        }
    }

    /**
     * Converts a local audio [File] into a [MultipartBody.Part] ready for HTTP transmission.
     */
    fun createAudioMultipartPart(file: File, partName: String = "audio_file"): MultipartBody.Part {
        val mimeType = when (file.extension.lowercase()) {
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            else -> "audio/mpeg"
        }

        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    /**
     * Converts a string parameter to a plain-text RequestBody.
     */
    fun createTextRequestBody(value: String) =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}
