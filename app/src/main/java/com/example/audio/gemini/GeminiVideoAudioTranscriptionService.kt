package com.example.audio.gemini

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.audio.DubbingDialect
import com.example.audio.DubbingTargetLanguage
import com.example.model.ScriptLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Data model for an individual transcribed speech segment aligned to the video audio timeline.
 */
data class VideoAudioTranscriptionSegment(
    val id: String = UUID.randomUUID().toString(),
    val startSeconds: Float,
    val endSeconds: Float,
    val speaker: String,
    val speakerGender: String = "MALE", // MALE, FEMALE, CHILD, NARRATOR
    val originalSpeech: String,
    val arabicDubbedAdaptation: String,
    val confidence: Float = 0.95f,
    val isEditedByUser: Boolean = false
) {
    val durationSeconds: Float
        get() = (endSeconds - startSeconds).coerceAtLeast(0.1f)

    fun formatTime(seconds: Float): String {
        val totalSec = seconds.toInt()
        val mins = totalSec / 60
        val secs = totalSec % 60
        val millis = ((seconds - totalSec) * 10).toInt()
        return String.format(Locale.US, "%02d:%02d.%01d", mins, secs, millis)
    }

    val formattedTimeRange: String
        get() = "${formatTime(startSeconds)} ➔ ${formatTime(endSeconds)}"

    /**
     * Converts this transcribed segment to a ScriptLine for the studio timeline.
     */
    fun toScriptLine(): ScriptLine {
        val avatar = when (speakerGender.uppercase()) {
            "FEMALE" -> "👩"
            "CHILD" -> "🧒"
            "NARRATOR" -> "🎙️"
            else -> "👨"
        }
        val voiceType = when (speakerGender.uppercase()) {
            "FEMALE" -> "ARABIC_FEMALE"
            "CHILD" -> "CARTOON"
            "NARRATOR" -> "DRAMATIC"
            else -> "ARABIC_MALE"
        }
        return ScriptLine(
            id = id,
            characterName = speaker,
            characterAvatar = avatar,
            textArabic = arabicDubbedAdaptation.ifBlank { originalSpeech },
            textOriginal = originalSpeech,
            startSeconds = startSeconds,
            endSeconds = endSeconds,
            voiceType = voiceType,
            isDubbed = false,
            customAudioPath = null,
            speakerGender = speakerGender.uppercase(),
            genderConfidence = (confidence * 100).toInt().coerceIn(70, 99)
        )
    }
}

/**
 * Result wrapper for the complete video transcription.
 */
data class VideoAudioTranscriptionResult(
    val detectedLanguage: String,
    val totalSpokenSegments: Int,
    val segments: List<VideoAudioTranscriptionSegment>,
    val videoAudioDurationSeconds: Float,
    val isFromLiveGeminiApi: Boolean,
    val extractionLatencyMs: Long = 0L,
    val summaryOrMood: String = "",
    val originalAudioPath: String? = null,
    val errorMessage: String? = null
)

/**
 * Gemini Video Audio Transcription & Alignment Service.
 *
 * Extracts the audio track from imported video files and uses the Gemini API (gemini-3.5-flash)
 * to transcribe the speech verbatim with accurate start/end timestamps, speaker detection,
 * and duration-matched Arabic dubbing adaptations to help users align dubbing with original speech.
 */
class GeminiVideoAudioTranscriptionService(private val context: Context) {

    private val tag = "GeminiTranscription"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    // Using Gemini 2.5 Flash for multimodal audio tasks
    private val geminiModel = "gemini-2.5-flash"

    /**
     * Resolves the effective Gemini API key across all configuration sources.
     */
    fun resolveApiKey(customApiKey: String = ""): String {
        if (customApiKey.isNotBlank() && !isSamplePlaceholder(customApiKey)) {
            return customApiKey.trim()
        }

        val appPrefs = context.getSharedPreferences("app_ai_prefs", Context.MODE_PRIVATE)
        val savedAppKey = appPrefs.getString("gemini_api_key", "")?.trim() ?: ""
        if (savedAppKey.isNotBlank() && !isSamplePlaceholder(savedAppKey)) {
            return savedAppKey
        }

        val studioPrefs = context.getSharedPreferences("dubbing_studio_prefs", Context.MODE_PRIVATE)
        val savedStudioKey = studioPrefs.getString("gemini_api_key", "")?.trim() ?: ""
        if (savedStudioKey.isNotBlank() && !isSamplePlaceholder(savedStudioKey)) {
            return savedStudioKey
        }

        // Developer access check: if the registered developer is logged in, or if configured in build/env
        try {
            val buildConfigKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildConfigKey.isNotBlank() && !isSamplePlaceholder(buildConfigKey)) {
                return buildConfigKey
            }
        } catch (_: Throwable) {}

        val envKey = System.getenv("GEMINI_API_KEY")?.trim() ?: ""
        if (envKey.isNotBlank() && !isSamplePlaceholder(envKey)) {
            return envKey
        }

        return ""
    }

    private fun isSamplePlaceholder(key: String): Boolean {
        val lower = key.lowercase()
        return lower.contains("your_gemini") ||
                lower.contains("my_gemini_api_key") ||
                lower.contains("your_api_key") ||
                lower == "null" ||
                key.isBlank()
    }

    /**
     * Extracts the raw audio track from an imported video file into a standalone M4A/AAC file
     * using Android's native MediaExtractor and MediaMuxer (high performance, no transcode needed).
     */
    suspend fun extractAudioTrackFromVideo(videoFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!videoFile.exists() || videoFile.length() < 500) {
                return@withContext Result.failure(IllegalArgumentException("ملف الفيديو غير صالح أو غير موجود"))
            }

            val audiosDir = File(context.cacheDir, "extracted_video_audio")
            if (!audiosDir.exists()) audiosDir.mkdirs()

            val outputFile = File(audiosDir, "extracted_audio_${System.currentTimeMillis()}.m4a")

            val extractor = MediaExtractor()
            var muxer: MediaMuxer? = null
            try {
                extractor.setDataSource(videoFile.absolutePath)
                var audioTrackIndex = -1
                var audioFormat: MediaFormat? = null

                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("audio/")) {
                        audioTrackIndex = i
                        audioFormat = format
                        break
                    }
                }

                if (audioTrackIndex < 0 || audioFormat == null) {
                    // Video does not have an audio track
                    return@withContext Result.failure(IllegalStateException("لم يتم العثور على مسار صوتي داخل ملف الفيديو المستورد"))
                }

                extractor.selectTrack(audioTrackIndex)

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val muxerAudioTrack = muxer.addTrack(audioFormat)
                muxer.start()

                val maxBufferSize = try {
                    audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                } catch (_: Exception) {
                    128 * 1024
                }.coerceAtLeast(64 * 1024)

                val buffer = ByteBuffer.allocate(maxBufferSize)
                val bufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags

                    muxer.writeSampleData(muxerAudioTrack, buffer, bufferInfo)
                    extractor.advance()
                }

                Result.success(outputFile)
            } finally {
                try { extractor.release() } catch (_: Exception) {}
                try {
                    muxer?.stop()
                    muxer?.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(tag, "MediaMuxer audio extraction failed, attempting fallback copy", e)
            // Fallback: If demuxing failed (e.g. rare container issue), return original file if readable
            if (videoFile.length() < 12 * 1024 * 1024) {
                Result.success(videoFile)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Transcribes imported video audio using Gemini 3.5 Flash via REST API with inline audio data.
     * Maps spoken speech to exact timestamps (start_sec, end_sec) and generates duration-aligned Arabic dubbing lines.
     */
    suspend fun transcribeVideoAudio(
        videoFile: File?,
        targetDialect: DubbingDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
        targetLanguage: DubbingTargetLanguage = DubbingTargetLanguage.ARABIC,
        customApiKey: String = "",
        customPromptContext: String = ""
    ): VideoAudioTranscriptionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (videoFile == null || !videoFile.exists() || videoFile.length() < 100) {
            return@withContext VideoAudioTranscriptionResult(
                detectedLanguage = "غير محدد",
                totalSpokenSegments = 0,
                segments = emptyList(),
                videoAudioDurationSeconds = 0f,
                isFromLiveGeminiApi = false,
                errorMessage = "يرجى استيراد مقطع فيديو أولاً لاستخراج الحوارات والتوقيتات"
            )
        }

        val apiKey = resolveApiKey(customApiKey)

        // Step 1: Extract Audio
        val extractionResult = extractAudioTrackFromVideo(videoFile)
        val audioFile = extractionResult.getOrNull() ?: videoFile
        val durationSeconds = extractAudioOrVideoDuration(videoFile)

        if (apiKey.isBlank()) {
            return@withContext VideoAudioTranscriptionResult(
                detectedLanguage = "غير محدد",
                totalSpokenSegments = 0,
                segments = emptyList(),
                videoAudioDurationSeconds = durationSeconds,
                isFromLiveGeminiApi = false,
                errorMessage = "يرجى إضافة مفتاح Gemini API في الإعدادات لاستخراج الصوت الحقيقي"
            )
        }

        try {
            // Step 2: Read Audio File & Base64 Encode
            // Read up to 12MB of audio to fit in Gemini REST payload comfortably
            val maxBytes = 12 * 1024 * 1024
            val audioBytes = if (audioFile.length() > maxBytes) {
                val buffer = ByteArray(maxBytes)
                FileInputStream(audioFile).use { it.read(buffer) }
                buffer
            } else {
                audioFile.readBytes()
            }

            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val mimeType = when {
                audioFile.name.endsWith(".wav", true) -> "audio/wav"
                audioFile.name.endsWith(".mp3", true) -> "audio/mp3"
                audioFile.name.endsWith(".m4a", true) -> "audio/mp4"
                audioFile.name.endsWith(".aac", true) -> "audio/aac"
                else -> "audio/mp4"
            }

            // Step 3: Construct Gemini Multimodal Prompt
            val targetLangDesc = if (targetLanguage == DubbingTargetLanguage.ARABIC) {
                "Arabic (${targetDialect.displayNameArabic})"
            } else {
                "${targetLanguage.displayNameArabic} (${targetLanguage.nativeName} - code: ${targetLanguage.code})"
            }

            val systemPrompt = """
                You are a world-class Speech-to-Text, Audio Segmentation, and Professional Lip-Sync Dubbing AI (specialized in pyvideotrans video translation and audio-video alignment).
                Listen to this audio track extracted from an imported video (total duration: approximately $durationSeconds seconds).
                
                YOUR TASKS:
                1. Accurately transcribe ALL spoken dialogue verbatim in the exact original language spoken in the video audio.
                2. Segment the speech naturally into short, granular phrasing segments (ideal: 1.5 to 3.5 seconds each, maximum 4 seconds) to ensure perfect video-audio synchronization and lip-sync alignment.
                3. For each spoken line/utterance, identify the precise start_seconds and end_seconds relative to the video audio timeline (0.0s = start of audio).
                4. Identify the speaker or character role (e.g. "المتحدث 1", "الراوي", "سارة", "أحمد", or character names).
                5. Identify the speaker gender (MALE, FEMALE, CHILD, NARRATOR).
                6. Translate and adapt each segment into the requested target language: $targetLangDesc.
                   Ensure the translated text matches the speaking duration and cadence of the segment so the synthesized voice aligns seamlessly with the video frames.
                7. Identify the primary detected spoken language (e.g. English, Arabic, Spanish, French, Japanese, etc.).
                
                ${if (customPromptContext.isNotBlank()) "Additional context: $customPromptContext" else ""}
                
                Return STRICTLY valid JSON with no markdown formatting around it:
                {
                  "detected_language": "Detected language name",
                  "summary": "Short 1-sentence summary of the spoken audio",
                  "segments": [
                    {
                      "start_seconds": 1.2,
                      "end_seconds": 3.8,
                      "speaker": "المتحدث 1",
                      "gender": "male",
                      "original_speech": "Verbatim transcript of spoken speech in original language...",
                      "arabic_dubbed_adaptation": "Translated text tailored to exact timing in the target language ($targetLangDesc)",
                      "confidence": 0.96
                    }
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", mimeType)
                                    put("data", base64Audio)
                                })
                            })
                            put(JSONObject().apply {
                                put("text", systemPrompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.w(tag, "Gemini API HTTP ${response.code}: $errorBody")
                return@withContext VideoAudioTranscriptionResult(
                    detectedLanguage = "غير محدد",
                    totalSpokenSegments = 0,
                    segments = emptyList(),
                    videoAudioDurationSeconds = durationSeconds,
                    isFromLiveGeminiApi = false,
                    errorMessage = "فشل طلب استخراج الصوت من Gemini (رمز الاستجابة: ${response.code}). يرجى التأكد من صلاحية المفتاح والاتصال."
                )
            }

            val responseBody = response.body?.string().orEmpty()
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val textPart = firstCandidate?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?.trim().orEmpty()

            val cleanJson = textPart.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJson)

            val detectedLanguage = parsed.optString("detected_language", "تم الكشف عن اللغة")
            val summary = parsed.optString("summary", "تفريغ صوتي لمحادثات المقطع")
            val segmentsArray = parsed.optJSONArray("segments") ?: JSONArray()

            val segments = mutableListOf<VideoAudioTranscriptionSegment>()
            for (i in 0 until segmentsArray.length()) {
                val segObj = segmentsArray.optJSONObject(i) ?: continue
                val startSec = segObj.optDouble("start_seconds", (i * 4.0)).toFloat().coerceAtLeast(0.0f)
                val endSec = segObj.optDouble("end_seconds", (startSec + 3.5)).toFloat().coerceAtLeast(startSec + 0.5f)
                val speaker = segObj.optString("speaker", "المتحدث ${i + 1}")
                val genderRaw = segObj.optString("gender", "male").uppercase()
                val origSpeech = segObj.optString("original_speech", "")
                val arabicDubbed = segObj.optString("arabic_dubbed_adaptation", origSpeech)
                val conf = segObj.optDouble("confidence", 0.95).toFloat()

                segments.add(
                    VideoAudioTranscriptionSegment(
                        startSeconds = startSec,
                        endSeconds = minOf(endSec, durationSeconds),
                        speaker = speaker,
                        speakerGender = if (genderRaw.contains("FEMALE")) "FEMALE" else if (genderRaw.contains("CHILD")) "CHILD" else "MALE",
                        originalSpeech = origSpeech,
                        arabicDubbedAdaptation = arabicDubbed,
                        confidence = conf
                    )
                )
            }

            if (segments.isEmpty()) {
                return@withContext VideoAudioTranscriptionResult(
                    detectedLanguage = detectedLanguage,
                    totalSpokenSegments = 0,
                    segments = emptyList(),
                    videoAudioDurationSeconds = durationSeconds,
                    isFromLiveGeminiApi = true,
                    errorMessage = "لم يتم اكتشاف مقاطع حوارية مسموعة في هذا المقطع."
                )
            }

            VideoAudioTranscriptionResult(
                detectedLanguage = detectedLanguage,
                totalSpokenSegments = segments.size,
                segments = segments,
                videoAudioDurationSeconds = durationSeconds,
                isFromLiveGeminiApi = true,
                extractionLatencyMs = latency,
                summaryOrMood = summary,
                originalAudioPath = audioFile.absolutePath
            )
        } catch (e: Exception) {
            Log.e(tag, "Transcription failed: ${e.message}", e)
            VideoAudioTranscriptionResult(
                detectedLanguage = "غير محدد",
                totalSpokenSegments = 0,
                segments = emptyList(),
                videoAudioDurationSeconds = durationSeconds,
                isFromLiveGeminiApi = false,
                errorMessage = "حدث خطأ أثناء الاتصال بالذكاء الاصطناعي: ${e.localizedMessage ?: "تحقق من الاتصال بالشبكة"}"
            )
        }
    }

    private fun extractAudioOrVideoDuration(file: File): Float {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 30000L
            (dur / 1000f).coerceAtLeast(5.0f)
        } catch (_: Exception) {
            30.0f
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Clean fallback transcription result without inserting fake dummy dialogue.
     */
    fun createSampleTranscriptionResult(
        audioFile: File? = null,
        durationSeconds: Float = 30f,
        targetDialect: DubbingDialect = DubbingDialect.MODERN_STANDARD_CLASSIC,
        startTime: Long = System.currentTimeMillis(),
        notice: String = "لا توجد بيانات مسجلة"
    ): VideoAudioTranscriptionResult {
        return VideoAudioTranscriptionResult(
            detectedLanguage = "غير محدد",
            totalSpokenSegments = 0,
            segments = emptyList(),
            videoAudioDurationSeconds = durationSeconds,
            isFromLiveGeminiApi = false,
            extractionLatencyMs = System.currentTimeMillis() - startTime,
            summaryOrMood = notice,
            originalAudioPath = audioFile?.absolutePath,
            errorMessage = notice
        )
    }

    private fun createFallbackTranscription(
        audioFile: File,
        durationSeconds: Float,
        targetDialect: DubbingDialect,
        startTime: Long,
        notice: String
    ): VideoAudioTranscriptionResult = createSampleTranscriptionResult(
        audioFile = audioFile,
        durationSeconds = durationSeconds,
        targetDialect = targetDialect,
        startTime = startTime,
        notice = notice
    )
}
