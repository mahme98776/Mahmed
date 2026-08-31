package com.example.audio.tts

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CloudTtsService(
    private val context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "CloudTtsService"
        private const val ELEVEN_LABS_BASE_URL = "https://api.elevenlabs.io/v1/text-to-speech"
        private const val GOOGLE_TTS_BASE_URL = "https://texttospeech.googleapis.com/v1/text:synthesize"
    }

    /**
     * Synthesizes the given Arabic text into natural human speech and saves it to [outputFile].
     */
    suspend fun synthesizeSpeechToFile(
        text: String,
        config: CloudTtsConfig,
        languageCode: String = "ar",
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            when (config.provider) {
                CloudTtsProvider.ELEVEN_LABS -> {
                    synthesizeWithElevenLabs(text, config, outputFile)
                }
                CloudTtsProvider.GOOGLE_CLOUD_TTS -> {
                    synthesizeWithGoogleCloud(text, config, languageCode, outputFile)
                }
                CloudTtsProvider.DEVICE_TTS -> {
                    Result.failure(IllegalStateException("Device TTS must be synthesized through TextToSpeechManager"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error synthesizing speech with ${config.provider}", e)
            Result.failure(e)
        }
    }

    /**
     * ElevenLabs Text-to-Speech API integration:
     * POST https://api.elevenlabs.io/v1/text-to-speech/{voice_id}
     */
    private fun synthesizeWithElevenLabs(
        text: String,
        config: CloudTtsConfig,
        outputFile: File
    ): Result<File> {
        val apiKey = config.elevenLabsApiKey.trim()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalArgumentException("ElevenLabs API Key is missing. Please set your API key in Dubbing Settings."))
        }

        val voiceId = config.elevenLabsVoiceId.ifBlank { "21m00Tcm4TlvDq8ikWAM" }
        val url = "$ELEVEN_LABS_BASE_URL/$voiceId"

        val jsonBody = JSONObject().apply {
            put("text", text)
            put("model_id", config.elevenLabsModelId.ifBlank { "eleven_multilingual_v2" })
            put("voice_settings", JSONObject().apply {
                put("stability", config.stability.toDouble())
                put("similarity_boost", config.similarityBoost.toDouble())
                put("style", 0.0)
                put("use_speaker_boost", true)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("xi-api-key", apiKey)
            .addHeader("Accept", "audio/mpeg")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            val errorMsg = try {
                val errJson = JSONObject(errorBody)
                errJson.optJSONObject("detail")?.optString("message")
                    ?: errJson.optString("message", "HTTP ${response.code}: $errorBody")
            } catch (_: Exception) {
                "HTTP ${response.code}: $errorBody"
            }
            return Result.failure(Exception("ElevenLabs API Error: $errorMsg"))
        }

        val responseBody = response.body
            ?: return Result.failure(Exception("ElevenLabs API returned empty response"))

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { fos ->
            responseBody.byteStream().copyTo(fos)
        }

        return Result.success(outputFile)
    }

    /**
     * Google Cloud Text-to-Speech API integration:
     * POST https://texttospeech.googleapis.com/v1/text:synthesize?key={API_KEY}
     */
    private fun synthesizeWithGoogleCloud(
        text: String,
        config: CloudTtsConfig,
        languageCode: String,
        outputFile: File
    ): Result<File> {
        val apiKey = config.googleCloudApiKey.trim()
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalArgumentException("Google Cloud TTS API Key is missing. Please set your API key in Dubbing Settings."))
        }

        val url = "$GOOGLE_TTS_BASE_URL?key=$apiKey"

        val targetLangCode = when (languageCode.lowercase()) {
            "ar" -> "ar-XA"
            "en" -> "en-US"
            "es" -> "es-ES"
            "fr" -> "fr-FR"
            "de" -> "de-DE"
            "tr" -> "tr-TR"
            "ru" -> "ru-RU"
            "hi" -> "hi-IN"
            "ja" -> "ja-JP"
            "zh" -> "cmn-CN"
            else -> if (config.googleCloudLanguageCode.isNotBlank()) config.googleCloudLanguageCode else "ar-XA"
        }

        val targetVoiceName = if (config.googleCloudVoiceName.startsWith(targetLangCode.substringBefore("-"))) {
            config.googleCloudVoiceName
        } else {
            if (targetLangCode == "ar-XA") config.googleCloudVoiceName else "${targetLangCode}-Wavenet-A"
        }

        val jsonBody = JSONObject().apply {
            put("input", JSONObject().apply {
                put("text", text)
            })
            put("voice", JSONObject().apply {
                put("languageCode", targetLangCode)
                put("name", targetVoiceName)
            })
            put("audioConfig", JSONObject().apply {
                put("audioEncoding", "MP3")
                put("speakingRate", config.speakingRate.toDouble())
                put("pitch", config.pitch.toDouble())
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            val errorMsg = try {
                val errJson = JSONObject(errorBody)
                errJson.optJSONObject("error")?.optString("message")
                    ?: "HTTP ${response.code}: $errorBody"
            } catch (_: Exception) {
                "HTTP ${response.code}: $errorBody"
            }
            return Result.failure(Exception("Google Cloud TTS Error: $errorMsg"))
        }

        val responseBody = response.body?.string()
            ?: return Result.failure(Exception("Google Cloud TTS returned empty response"))

        val responseJson = JSONObject(responseBody)
        val audioContentBase64 = responseJson.optString("audioContent")
        if (audioContentBase64.isBlank()) {
            return Result.failure(Exception("Google Cloud TTS did not return audioContent"))
        }

        val audioBytes = Base64.decode(audioContentBase64, Base64.DEFAULT)

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { fos ->
            fos.write(audioBytes)
        }

        return Result.success(outputFile)
    }

    /**
     * Test the API configuration with a quick preview sample.
     */
    suspend fun testConnection(config: CloudTtsConfig): Result<String> = withContext(Dispatchers.IO) {
        val testFile = File(context.cacheDir, "tts_test_${System.currentTimeMillis()}.mp3")
        val sampleText = "تم الاتصال بنجاح بمحرك الصوت البشري الفصيح."

        val result = synthesizeSpeechToFile(sampleText, config, "ar", testFile)
        result.map { "الاتصال سليم! تم توليد الصوت بنجاح (" + (it.length() / 1024) + " KB)" }
    }
}
