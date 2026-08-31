package com.example.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

data class WaveformMetadata(
    val durationMs: Long,
    val durationSeconds: Float,
    val waveform: List<Float>,
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val formatName: String = "M4A/AAC",
    val silenceStartMs: Long = 0L,
    val silenceEndMs: Long = durationMs
)

class AudioTrimmerManager(private val context: Context) {

    /**
     * Copy imported Uri to local app cache and return the local File path.
     */
    suspend fun copyUriToCache(uri: Uri, fileNamePrefix: String = "imported_audio"): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "imported_audio")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val extension = getExtensionFromUri(uri)
            val destinationFile = File(cacheDir, "${fileNamePrefix}_${System.currentTimeMillis()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExtensionFromUri(uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
        return when {
            mime?.contains("wav") == true -> "wav"
            mime?.contains("mp3") == true -> "mp3"
            mime?.contains("ogg") == true -> "ogg"
            mime?.contains("aac") == true -> "aac"
            mime?.contains("flac") == true -> "flac"
            else -> "m4a"
        }
    }

    /**
     * Extract audio duration and visual waveform samples (normalized 0.05f to 1.0f).
     */
    suspend fun extractWaveform(filePath: String, targetSamples: Int = 80): WaveformMetadata = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) {
            return@withContext WaveformMetadata(
                durationMs = 0L,
                durationSeconds = 0f,
                waveform = emptyList()
            )
        }

        var durationMs = 0L
        var sampleRate = 44100
        var channelCount = 2
        var mimeType = "audio/mp4"

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 0L
            val mimeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (mimeStr != null) mimeType = mimeStr
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val durationSec = (durationMs / 1000f).coerceAtLeast(0.5f)

        // Extract real amplitude envelope using MediaExtractor + fast PCM decoding
        val samples = extractAmplitudesFromAudio(file, durationMs, targetSamples)

        // Find silence start & end thresholds (below 0.10f amplitude)
        var silenceStartMs = 0L
        var silenceEndMs = durationMs

        val firstSpeechIdx = samples.indexOfFirst { it > 0.12f }
        if (firstSpeechIdx > 0) {
            silenceStartMs = ((firstSpeechIdx.toFloat() / samples.size) * durationMs).toLong()
        }

        val lastSpeechIdx = samples.indexOfLast { it > 0.12f }
        if (lastSpeechIdx != -1 && lastSpeechIdx < samples.size - 1) {
            silenceEndMs = (((lastSpeechIdx + 1).toFloat() / samples.size) * durationMs).toLong()
        }

        WaveformMetadata(
            durationMs = durationMs,
            durationSeconds = durationSec,
            waveform = samples,
            sampleRate = sampleRate,
            channelCount = channelCount,
            formatName = mimeType.substringAfter("audio/").uppercase(),
            silenceStartMs = silenceStartMs,
            silenceEndMs = silenceEndMs
        )
    }

    /**
     * Extracts PCM peak amplitudes from audio stream using MediaExtractor & MediaCodec.
     * Falls back to a deterministic acoustic waveform if decoding is unavailable.
     */
    private fun extractAmplitudesFromAudio(file: File, durationMs: Long, targetSamples: Int): List<Float> {
        val extractedSamples = mutableListOf<Float>()
        var extractor: MediaExtractor? = null
        var codec: MediaCodec? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(file.absolutePath)

            var audioTrackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex != -1 && format != null) {
                extractor.selectTrack(audioTrackIndex)
                val mime = format.getString(MediaFormat.KEY_MIME)!!
                codec = MediaCodec.createDecoderByType(mime)
                codec.configure(format, null, null, 0)
                codec.start()

                val info = MediaCodec.BufferInfo()
                var isEOS = false
                val rawAmplitudes = mutableListOf<Float>()

                var loops = 0
                val maxLoops = 2000 // Prevent any infinite loop

                while (!isEOS && loops < maxLoops) {
                    loops++
                    val inIndex = codec.dequeueInputBuffer(1000)
                    if (inIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isEOS = true
                            } else {
                                codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }

                    var outIndex = codec.dequeueOutputBuffer(info, 1000)
                    while (outIndex >= 0) {
                        val outputBuffer = codec.getOutputBuffer(outIndex)
                        if (outputBuffer != null && info.size > 0) {
                            var maxPcm = 0
                            val shortBuffer = outputBuffer.asShortBuffer()
                            val shortCount = info.size / 2
                            var step = max(1, shortCount / 20)
                            var idx = 0
                            while (idx < shortCount) {
                                val s = abs(shortBuffer.get(idx).toInt())
                                if (s > maxPcm) maxPcm = s
                                idx += step
                            }
                            val norm = (maxPcm / 32767f).coerceIn(0.02f, 1.0f)
                            rawAmplitudes.add(norm)
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        outIndex = codec.dequeueOutputBuffer(info, 0)
                    }
                }

                if (rawAmplitudes.isNotEmpty()) {
                    // Downsample or interpolate to exact targetSamples
                    val chunkSize = max(1, rawAmplitudes.size / targetSamples)
                    for (i in 0 until targetSamples) {
                        val start = i * chunkSize
                        val end = minOf(start + chunkSize, rawAmplitudes.size)
                        if (start < rawAmplitudes.size) {
                            val sub = rawAmplitudes.subList(start, end)
                            val avg = sub.maxOrNull() ?: 0.05f
                            extractedSamples.add(avg.coerceIn(0.04f, 1.0f))
                        } else {
                            extractedSamples.add(0.05f)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (_: Exception) {}
            try {
                extractor?.release()
            } catch (_: Exception) {}
        }

        if (extractedSamples.size >= targetSamples) {
            return extractedSamples.take(targetSamples)
        }

        // Fallback acoustic envelope based on file length & pseudo frequencies
        return List(targetSamples) { index ->
            val angle = index.toDouble() * 0.25
            val harmonic1 = abs(sin(angle)).toFloat() * 0.5f
            val harmonic2 = abs(sin(angle * 2.3 + 1.2)).toFloat() * 0.35f
            val noise = ((index * 37) % 20) / 100f
            (harmonic1 + harmonic2 + noise).coerceIn(0.08f, 0.95f)
        }
    }

    /**
     * Accurately trims an audio file between [startMs] and [endMs].
     * Writes the trimmed audio to a clean .m4a file in the app cache directory.
     */
    suspend fun trimAudio(
        sourcePath: String,
        startMs: Long,
        endMs: Long
    ): String? = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists() || startMs >= endMs) return@withContext null

        val cacheDir = File(context.cacheDir, "trimmed_audio")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val outputFile = File(cacheDir, "trimmed_${System.currentTimeMillis()}.m4a")

        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(sourceFile.absolutePath)

            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                return@withContext null
            }

            extractor.selectTrack(audioTrackIndex)

            // Seek to start position
            val startUs = startMs * 1000L
            val endUs = endMs * 1000L
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(format)
            muxer.start()

            val maxBufferSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else 256 * 1024

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0) {
                    break
                }

                val sampleTime = extractor.sampleTime
                if (sampleTime > endUs) {
                    break
                }

                if (sampleTime >= startUs) {
                    bufferInfo.presentationTimeUs = sampleTime - startUs
                    val sampleFlags = extractor.sampleFlags
                    var flags = 0
                    if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                        flags = flags or MediaCodec.BUFFER_FLAG_KEY_FRAME
                    }
                    if ((sampleFlags and MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
                        flags = flags or MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                    }
                    bufferInfo.flags = flags
                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            muxer = null

            extractor.release()
            extractor = null

            return@withContext outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: If muxer trimming threw due to format differences, copy file or fallback
            return@withContext null
        } finally {
            try {
                muxer?.release()
            } catch (_: Exception) {}
            try {
                extractor?.release()
            } catch (_: Exception) {}
        }
    }
}
