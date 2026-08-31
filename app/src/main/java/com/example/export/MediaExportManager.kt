package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.DubbingProject
import com.example.model.DubbingClip
import com.example.model.ScriptLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.media.MediaMetadataRetriever
import java.io.RandomAccessFile
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

enum class ExportFormat {
    MP4_VIDEO,
    MP3_AUDIO
}

sealed class ExportResult {
    data class Success(
        val uri: Uri?,
        val filePath: String,
        val fileName: String,
        val format: ExportFormat
    ) : ExportResult()

    data class Error(val message: String) : ExportResult()
}

class MediaExportManager(private val context: Context) {

    /**
     * Export dubbing project as MP3/M4A Audio to External Storage (Music / Downloads)
     */
    suspend fun exportAudio(
        clip: DubbingClip,
        project: DubbingProject?,
        recordedAudioPath: String?,
        customTitle: String? = null,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f, "جاري تحضير الملف الصوتي للدبلجة...")

            val sourcePath = recordedAudioPath ?: project?.recordedAudioPath
            if (sourcePath == null || !File(sourcePath).exists()) {
                return@withContext ExportResult.Error("لا يوجد تسجيل صوتي محفوظ للتصدير. يرجى تسجيل الصوت أولاً!")
            }

            val sourceFile = File(sourcePath)
            val title = customTitle?.takeIf { it.isNotBlank() } ?: project?.title ?: "دبلجة_${clip.title}"
            val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis() % 10000}.mp3"

            onProgress(0.4f, "جاري كتابة الملف إلى وحدة التخزين الخارجية...")

            var savedUri: Uri? = null
            var savedPath: String = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/DubbingStudio")
                    put(MediaStore.Audio.Media.TITLE, title)
                    put(MediaStore.Audio.Media.ARTIST, "استوديو دبلجة المقاطع العربي")
                    put(MediaStore.Audio.Media.ALBUM, clip.title)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    savedUri = uri
                    savedPath = "Music/DubbingStudio/$fileName"
                }
            }

            // Fallback or older API
            if (savedUri == null) {
                val musicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "DubbingStudio"
                )
                if (!musicDir.exists()) musicDir.mkdirs()
                val destFile = File(musicDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                savedPath = destFile.absolutePath
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("audio/mpeg"),
                    null
                )
            }

            onProgress(1.0f, "تم تصدير الملف الصوتي بنجاح! 🎵✨")
            ExportResult.Success(
                uri = savedUri,
                filePath = savedPath,
                fileName = fileName,
                format = ExportFormat.MP3_AUDIO
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("فشل تصدير الصوت: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Export dubbing project as MP4 Video with rendered animated scene, synchronized subtitles,
     * waveforms, and multiplexed audio track to External Storage (Movies / Downloads).
     * Configured with selected resolution, bitrate, and framerate presets.
     */
    suspend fun exportVideo(
        clip: DubbingClip,
        project: DubbingProject?,
        recordedAudioPath: String?,
        scriptLines: List<ScriptLine>,
        customTitle: String? = null,
        videoConfig: VideoExportConfig = VideoExportConfig(),
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ExportResult = withContext(Dispatchers.IO) {
        val tempOutputFile = File(context.cacheDir, "export_temp_${System.currentTimeMillis()}.mp4")
        try {
            onProgress(0.05f, "جاري إعداد محرك معالجة الفيديو بدقة ${videoConfig.resolution.badge}...")

            val durationSeconds = clip.durationSeconds.coerceAtLeast(5)
            val width = videoConfig.resolution.width
            val height = videoConfig.resolution.height
            val frameRate = videoConfig.frameRatePreset.fps
            val totalFrames = durationSeconds * frameRate
            val bitRate = videoConfig.getCalculatedBitrateBps()

            // Scale factor relative to baseline 720p (1280x720)
            val scaleFactor = (width / 1280f).coerceAtLeast(0.5f)

            val audioPath = recordedAudioPath ?: project?.recordedAudioPath

            // 1. Setup MediaCodec Video Encoder
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            val muxer = MediaMuxer(tempOutputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var muxerStarted = false

            // Check if audio file has valid tracks to mux
            var audioExtractor: MediaExtractor? = null
            var audioFormat: MediaFormat? = null
            if (audioPath != null && File(audioPath).exists()) {
                try {
                    val extractor = MediaExtractor()
                    extractor.setDataSource(audioPath)
                    for (i in 0 until extractor.trackCount) {
                        val trackFormat = extractor.getTrackFormat(i)
                        val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("audio/")) {
                            extractor.selectTrack(i)
                            audioExtractor = extractor
                            audioFormat = trackFormat
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = 1_000_000L / frameRate

            onProgress(0.15f, "جاري معالجة وتوليد إطارات الفيديو (${videoConfig.resolution.badge} @ ${frameRate}fps)...")

            // Paint and drawing objects with scalable sizing
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 34f * scaleFactor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(8f * scaleFactor, 0f, 4f * scaleFactor, AndroidColor.BLACK)
            }

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.parseColor("#D0BCFF")
                textAlign = Paint.Align.CENTER
                textSize = 28f * scaleFactor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.parseColor("#FFD993")
                textAlign = Paint.Align.CENTER
                textSize = 22f * scaleFactor
            }

            val qualityBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.parseColor("#806366F1")
            }

            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 14f * scaleFactor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = 6f * scaleFactor
                strokeCap = Paint.Cap.ROUND
            }

            // Render loop
            for (frame in 0 until totalFrames) {
                val currentSeconds = frame.toFloat() / frameRate
                val presentationTimeUs = frame * frameDurationUs

                // Lock Canvas on Surface
                val canvas: Canvas? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    try {
                        // Draw Background Gradient
                        val gradient = LinearGradient(
                            0f, 0f, width.toFloat(), height.toFloat(),
                            AndroidColor.parseColor("#141218"),
                            clip.primaryColor.toInt(),
                            Shader.TileMode.CLAMP
                        )
                        val bgPaint = Paint().apply { shader = gradient }
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                        // Draw Header / Title
                        canvas.drawText("🎬 استوديو دبلجة المقاطع العربي", width / 2f, 60f * scaleFactor, titlePaint)
                        canvas.drawText(clip.title, width / 2f, 100f * scaleFactor, subPaint)

                        // Quality stamp badge at top right
                        val badgeWidth = 140f * scaleFactor
                        val badgeHeight = 32f * scaleFactor
                        val badgeRect = RectF(
                            width - badgeWidth - 30f * scaleFactor,
                            30f * scaleFactor,
                            width - 30f * scaleFactor,
                            30f * scaleFactor + badgeHeight
                        )
                        canvas.drawRoundRect(badgeRect, 8f * scaleFactor, 8f * scaleFactor, qualityBadgePaint)
                        canvas.drawText(
                            "${videoConfig.resolution.badge} • ${frameRate}fps",
                            badgeRect.centerX(),
                            badgeRect.centerY() + 5f * scaleFactor,
                            badgeTextPaint
                        )

                        // Draw Character Avatar & Central Stage
                        val activeLine = scriptLines.find {
                            currentSeconds >= it.startSeconds && currentSeconds <= it.endSeconds
                        }

                        val avatarEmoji = activeLine?.characterAvatar ?: clip.coverEmoji
                        val charName = activeLine?.characterName ?: "شخصية المشهد"
                        val arabicText = activeLine?.textArabic ?: "🎙️ [مقطع مدبلج بصوت رائع]"

                        // Avatar container
                        val avatarCenterY = height / 2f - 60f * scaleFactor
                        val avatarRadius = 80f * scaleFactor
                        val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = AndroidColor.parseColor("#2B2930")
                        }
                        canvas.drawCircle(width / 2f, avatarCenterY, avatarRadius, avatarPaint)

                        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            textSize = 72f * scaleFactor
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText(avatarEmoji, width / 2f, avatarCenterY + 25f * scaleFactor, emojiPaint)

                        // Character Name Badge
                        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = AndroidColor.parseColor("#D0BCFF")
                            textSize = 24f * scaleFactor
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }
                        canvas.drawText(charName, width / 2f, avatarCenterY + 120f * scaleFactor, namePaint)

                        // Soundwave animation
                        if (videoConfig.showWaveform) {
                            val waveY = height / 2f + 110f * scaleFactor
                            val waveBars = 32
                            val barSpacing = (width - 400f * scaleFactor) / waveBars
                            for (b in 0 until waveBars) {
                                val waveHeight = (sin(frame * 0.2 + b * 0.4) * 25 * scaleFactor + 30 * scaleFactor).toFloat()
                                val bx = 200f * scaleFactor + b * barSpacing
                                wavePaint.color = if (b % 2 == 0) AndroidColor.parseColor("#D0BCFF") else AndroidColor.parseColor("#FFD993")
                                canvas.drawLine(bx, waveY - waveHeight / 2, bx, waveY + waveHeight / 2, wavePaint)
                            }
                        }

                        // Subtitle Box at Bottom
                        if (videoConfig.burnSubtitles) {
                            val subBoxMarginHorizontal = 100f * scaleFactor
                            val subBoxHeight = 130f * scaleFactor
                            val subBoxBottom = height - 50f * scaleFactor
                            val subBoxRect = RectF(
                                subBoxMarginHorizontal,
                                subBoxBottom - subBoxHeight,
                                width - subBoxMarginHorizontal,
                                subBoxBottom
                            )
                            val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = AndroidColor.parseColor("#E61E1B24")
                            }
                            canvas.drawRoundRect(subBoxRect, 18f * scaleFactor, 18f * scaleFactor, boxPaint)

                            // Subtitle text (wrapped if long)
                            if (arabicText.length > 45) {
                                val part1 = arabicText.substring(0, 45)
                                val part2 = arabicText.substring(45)
                                canvas.drawText(part1, width / 2f, subBoxRect.centerY() - 15f * scaleFactor, textPaint)
                                canvas.drawText(part2, width / 2f, subBoxRect.centerY() + 30f * scaleFactor, textPaint)
                            } else {
                                canvas.drawText(arabicText, width / 2f, subBoxRect.centerY() + 10f * scaleFactor, textPaint)
                            }
                        }

                        // Progress Timeline bar
                        val progress = currentSeconds / durationSeconds.toFloat()
                        val timelinePaintBg = Paint().apply { color = AndroidColor.parseColor("#49454F") }
                        val timelinePaintFg = Paint().apply { color = AndroidColor.parseColor("#FFD993") }
                        val barHeight = 12f * scaleFactor
                        canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), timelinePaintBg)
                        canvas.drawRect(0f, height - barHeight, width * progress, height.toFloat(), timelinePaintFg)

                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }
                }

                // Drain Encoder output
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 0)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val newFormat = encoder.outputFormat
                        videoTrackIndex = muxer.addTrack(newFormat)
                        if (audioFormat != null && audioExtractor != null) {
                            audioTrackIndex = muxer.addTrack(audioFormat)
                        }
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val encodedData = encoder.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = presentationTimeUs
                            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(status, false)
                    }
                }

                // Update UI progress
                if (frame % 15 == 0) {
                    val p = 0.15f + (frame.toFloat() / totalFrames) * 0.65f
                    val secFormatted = String.format("%.1f", currentSeconds)
                    onProgress(p, "جاري تحويل الفيديو: ثانية $secFormatted من $durationSeconds...")
                }
            }

            // Signal End of Stream to Video Encoder
            encoder.signalEndOfInputStream()

            // Drain remaining frames
            var eos = false
            while (!eos) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    eos = true
                } else if (status >= 0) {
                    val encodedData = encoder.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(status, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eos = true
                    }
                }
            }

            // Mux Audio Track if available
            if (audioExtractor != null && audioTrackIndex >= 0 && muxerStarted) {
                onProgress(0.85f, "جاري دمج ومزامنة الصوت المدبلج مع الفيديو...")
                val maxBufferSize = 256 * 1024
                val audioBuffer = ByteBuffer.allocateDirect(maxBufferSize)
                val audioBufferInfo = MediaCodec.BufferInfo()

                while (true) {
                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuffer, 0)
                    if (audioBufferInfo.size < 0) {
                        break
                    }
                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    val sampleFlags = audioExtractor.sampleFlags
                    var flags = 0
                    if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                        flags = flags or MediaCodec.BUFFER_FLAG_KEY_FRAME
                    }
                    if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
                        flags = flags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                    }
                    audioBufferInfo.flags = flags
                    muxer.writeSampleData(audioTrackIndex, audioBuffer, audioBufferInfo)
                    audioExtractor.advance()
                }
                audioExtractor.release()
            }

            // Stop Encoder and Muxer
            encoder.stop()
            encoder.release()
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()

            onProgress(0.92f, "جاري حفظ الفيديو في وحدة التخزين الخارجية...")

            // Save to MediaStore (Movies / Downloads)
            val title = customTitle?.takeIf { it.isNotBlank() } ?: project?.title ?: "دبلجة_${clip.title}"
            val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis() % 10000}.mp4"

            var savedUri: Uri? = null
            var savedPath = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/DubbingStudio")
                    put(MediaStore.Video.Media.TITLE, title)
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(tempOutputFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    savedUri = uri
                    savedPath = "Movies/DubbingStudio/$fileName"
                }
            }

            if (savedUri == null) {
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "DubbingStudio"
                )
                if (!moviesDir.exists()) moviesDir.mkdirs()
                val destFile = File(moviesDir, fileName)
                tempOutputFile.copyTo(destFile, overwrite = true)
                savedPath = destFile.absolutePath
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
            }

            tempOutputFile.delete()

            onProgress(1.0f, "تم تصدير فيديو MP4 بنجاح! 🎬✨")
            ExportResult.Success(
                uri = savedUri,
                filePath = savedPath,
                fileName = fileName,
                format = ExportFormat.MP4_VIDEO
            )
        } catch (e: Exception) {
            e.printStackTrace()
            tempOutputFile.delete()
            ExportResult.Error("فشل تصدير الفيديو: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Merge the original video track directly with the newly dubbed audio track (lossless video passthrough)
     * and save the final result to the device storage (MediaStore Movies/DubbingStudio).
     */
    suspend fun mergeOriginalVideoWithDubbedAudio(
        clip: DubbingClip,
        project: DubbingProject? = null,
        customVideoPathOrUri: String? = null,
        recordedAudioPath: String? = null,
        scriptLines: List<ScriptLine> = clip.scriptLines,
        customTitle: String? = null,
        syncOffsetMs: Long = 0L,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ExportResult = withContext(Dispatchers.IO) {
        val tempMergedFile = File(context.cacheDir, "merged_video_${System.currentTimeMillis()}.mp4")
        var tempSourceVideoFile: File? = null
        var tempSynthesizedAudioFile: File? = null

        try {
            onProgress(0.05f, "جاري فحص مسار الفيديو الأصلي ومسار الصوت المدبلج...")

            // 1. Resolve source video file or URI
            val rawVideoSource = customVideoPathOrUri?.takeIf { it.isNotBlank() }
                ?: clip.videoUri?.takeIf { it.isNotBlank() }

            var videoFilePath: String? = null

            if (rawVideoSource != null) {
                if (rawVideoSource.startsWith("content://")) {
                    try {
                        val uri = Uri.parse(rawVideoSource)
                        val cacheVideo = File(context.cacheDir, "source_vid_${System.currentTimeMillis()}.mp4")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(cacheVideo).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (cacheVideo.exists() && cacheVideo.length() > 0) {
                            tempSourceVideoFile = cacheVideo
                            videoFilePath = cacheVideo.absolutePath
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (rawVideoSource.startsWith("file://")) {
                    val path = Uri.parse(rawVideoSource).path
                    if (path != null && File(path).exists()) {
                        videoFilePath = path
                    }
                } else if (File(rawVideoSource).exists()) {
                    videoFilePath = rawVideoSource
                }
            }

            // 2. Resolve or synthesize dubbed audio track
            var audioFilePath = recordedAudioPath?.takeIf { File(it).exists() }
                ?: project?.recordedAudioPath?.takeIf { File(it).exists() }

            if (audioFilePath == null) {
                val linesWithAudio = scriptLines.filter { it.customAudioPath != null && File(it.customAudioPath).exists() }
                if (linesWithAudio.isNotEmpty()) {
                    onProgress(0.12f, "جاري تجميع وتوليد مسار الصوت المدبلج بدقة التوقيت...")
                    val synthesized = buildTimelineDubbedAudioTrack(
                        lines = scriptLines,
                        totalDurationSec = clip.durationSeconds
                    )
                    if (synthesized != null && synthesized.exists()) {
                        tempSynthesizedAudioFile = synthesized
                        audioFilePath = synthesized.absolutePath
                    }
                }
            }

            // If no physical original video file is available, fallback gracefully to full canvas renderer
            if (videoFilePath == null || !File(videoFilePath).exists()) {
                onProgress(0.2f, "جاري دمج الصوت المدبلج مع مشاهد الفيديو المخصصة...")
                return@withContext exportVideo(
                    clip = clip,
                    project = project,
                    recordedAudioPath = audioFilePath,
                    scriptLines = scriptLines,
                    customTitle = customTitle ?: "دبلجة_${clip.title}",
                    onProgress = onProgress
                )
            }

            onProgress(0.25f, "جاري استخراج مسار الفيديو الأصلي بدون فقدان للجودة...")

            // 3. Setup Extractors
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFilePath)

            var videoTrackIndex = -1
            var videoFormat: MediaFormat? = null

            for (i in 0 until videoExtractor.trackCount) {
                val format = videoExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    videoExtractor.selectTrack(i)
                    videoTrackIndex = i
                    videoFormat = format
                    break
                }
            }

            if (videoTrackIndex == -1 || videoFormat == null) {
                videoExtractor.release()
                // Fallback to exportVideo if no video track found
                return@withContext exportVideo(
                    clip = clip,
                    project = project,
                    recordedAudioPath = audioFilePath,
                    scriptLines = scriptLines,
                    customTitle = customTitle ?: "دبلجة_${clip.title}",
                    onProgress = onProgress
                )
            }

            // Check rotation
            var rotationDegrees = 0
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoFilePath)
                val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                if (rotStr != null) {
                    rotationDegrees = rotStr.toIntOrNull() ?: 0
                }
                retriever.release()
            } catch (_: Exception) {}

            var audioExtractor: MediaExtractor? = null
            var audioFormat: MediaFormat? = null
            var audioTrackIndex = -1

            if (audioFilePath != null && File(audioFilePath).exists()) {
                try {
                    val aExtractor = MediaExtractor()
                    aExtractor.setDataSource(audioFilePath)
                    for (i in 0 until aExtractor.trackCount) {
                        val format = aExtractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("audio/")) {
                            aExtractor.selectTrack(i)
                            audioExtractor = aExtractor
                            audioFormat = format
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 4. Mux together
            val muxer = MediaMuxer(tempMergedFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerVideoTrack = muxer.addTrack(videoFormat)
            var muxerAudioTrack = -1
            if (audioFormat != null && audioExtractor != null) {
                muxerAudioTrack = muxer.addTrack(audioFormat)
            }

            if (rotationDegrees != 0) {
                muxer.setOrientationHint(rotationDegrees)
            }

            muxer.start()

            onProgress(0.40f, "جاري نسخ ونقل إطارات الفيديو الأصلي بدقة كاملة...")

            // Copy Video Track
            val bufferSize = 1024 * 1024 // 1MB
            val videoBuffer = ByteBuffer.allocateDirect(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = videoExtractor.readSampleData(videoBuffer, 0)
                if (bufferInfo.size < 0) {
                    break
                }
                bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                val flags = videoExtractor.sampleFlags
                var sampleFlags = 0
                if ((flags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                    sampleFlags = sampleFlags or MediaCodec.BUFFER_FLAG_KEY_FRAME
                }
                if ((flags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
                    sampleFlags = sampleFlags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                }
                bufferInfo.flags = sampleFlags
                muxer.writeSampleData(muxerVideoTrack, videoBuffer, bufferInfo)
                videoExtractor.advance()
            }
            videoExtractor.release()

            // Copy Dubbed Audio Track
            if (audioExtractor != null && muxerAudioTrack >= 0) {
                onProgress(0.75f, "جاري دمج ومزامنة الصوت المدبلج مع الفيديو...")
                val audioBuffer = ByteBuffer.allocateDirect(256 * 1024)
                val audioBufferInfo = MediaCodec.BufferInfo()
                val syncOffsetUs = syncOffsetMs * 1000L

                while (true) {
                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuffer, 0)
                    if (audioBufferInfo.size < 0) {
                        break
                    }
                    val sampleTimeUs = (audioExtractor.sampleTime + syncOffsetUs).coerceAtLeast(0L)
                    audioBufferInfo.presentationTimeUs = sampleTimeUs
                    val flags = audioExtractor.sampleFlags
                    var sampleFlags = 0
                    if ((flags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                        sampleFlags = sampleFlags or MediaCodec.BUFFER_FLAG_KEY_FRAME
                    }
                    if ((flags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
                        sampleFlags = sampleFlags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                    }
                    audioBufferInfo.flags = sampleFlags
                    muxer.writeSampleData(muxerAudioTrack, audioBuffer, audioBufferInfo)
                    audioExtractor.advance()
                }
                audioExtractor.release()
            }

            muxer.stop()
            muxer.release()

            onProgress(0.92f, "جاري حفظ الفيديو المدمج في ذاكرة الجهاز (Movies/DubbingStudio)...")

            // 5. Save final result to Device Storage (MediaStore)
            val title = customTitle?.takeIf { it.isNotBlank() } ?: project?.title ?: "دبلجة_${clip.title}"
            val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val fileName = "${sanitizedTitle}_مدمج_${System.currentTimeMillis() % 10000}.mp4"

            var savedUri: Uri? = null
            var savedPath = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/DubbingStudio")
                    put(MediaStore.Video.Media.TITLE, title)
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(tempMergedFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    savedUri = uri
                    savedPath = "Movies/DubbingStudio/$fileName"
                }
            }

            if (savedUri == null) {
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "DubbingStudio"
                )
                if (!moviesDir.exists()) moviesDir.mkdirs()
                val destFile = File(moviesDir, fileName)
                tempMergedFile.copyTo(destFile, overwrite = true)
                savedPath = destFile.absolutePath
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
            }

            tempMergedFile.delete()
            tempSourceVideoFile?.delete()
            tempSynthesizedAudioFile?.delete()

            onProgress(1.0f, "تم دمج الفيديو وحفظه في الجهاز بنجاح! 🎬✨")

            ExportResult.Success(
                uri = savedUri,
                filePath = savedPath,
                fileName = fileName,
                format = ExportFormat.MP4_VIDEO
            )
        } catch (e: Exception) {
            e.printStackTrace()
            tempMergedFile.delete()
            tempSourceVideoFile?.delete()
            tempSynthesizedAudioFile?.delete()
            ExportResult.Error("فشل دمج الفيديو مع الصوت: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Synthesize individual dubbed script lines into a single continuous audio WAV file matching video timeline
     */
    private fun buildTimelineDubbedAudioTrack(
        lines: List<ScriptLine>,
        totalDurationSec: Int
    ): File? {
        return try {
            val sampleRate = 44100
            val channels = 1
            val bytesPerSample = 2 // 16-bit PCM
            val bytesPerSec = sampleRate * channels * bytesPerSample
            val totalBytes = totalDurationSec * bytesPerSec

            val masterPcm = ByteArray(totalBytes)

            for (line in lines) {
                val audioPath = line.customAudioPath ?: continue
                val audioFile = File(audioPath)
                if (!audioFile.exists()) continue

                val startByte = (line.startSeconds * bytesPerSec).toInt().coerceIn(0, totalBytes)
                val lineBytes = audioFile.readBytes()

                // Skip standard 44-byte WAV header if present
                val pcmOffset = if (lineBytes.size > 44 && lineBytes[0] == 'R'.code.toByte() && lineBytes[1] == 'I'.code.toByte()) 44 else 0
                val pcmLength = lineBytes.size - pcmOffset

                val copyLength = minOf(pcmLength, totalBytes - startByte)
                if (copyLength > 0 && startByte < totalBytes) {
                    System.arraycopy(lineBytes, pcmOffset, masterPcm, startByte, copyLength)
                }
            }

            val outFile = File(context.cacheDir, "dubbed_master_${System.currentTimeMillis()}.wav")
            FileOutputStream(outFile).use { fos ->
                writeWavHeader(fos, channels, masterPcm.size, sampleRate, 16)
                fos.write(masterPcm)
            }
            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        channels: Int,
        pcmDataLength: Int,
        sampleRate: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = pcmDataLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmDataLength and 0xff).toByte()
        header[41] = ((pcmDataLength shr 8) and 0xff).toByte()
        header[42] = ((pcmDataLength shr 16) and 0xff).toByte()
        header[43] = ((pcmDataLength shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}

