package com.example.audio.hollywood

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spatial 3D Binaural Audio Panning Engine.
 * Simulates head-related acoustic positioning (Interaural Level and Time Differences)
 * to render immersive 3D/8D spatialized stereo tracks.
 */
class Spatial3DBinauralEngine(private val context: Context) {
    private val tag = "Spatial3DBinauralEngine"

    /**
     * Converts a mono speech WAV file into an immersive 3D binaural stereo WAV file.
     * panPosition: -1.0f (full Left) to +1.0f (full Right), 0.0f (Center)
     * depthDistance: 1.0f (close) to 4.0f (far in room)
     */
    suspend fun spatializeMonoWavToStereo3D(
        monoWavFile: File,
        panPosition: Float = 0.0f,
        depthDistance: Float = 1.2f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val rawBytes = if (monoWavFile.exists()) monoWavFile.readBytes() else ByteArray(0)
            val pcmOffset = if (rawBytes.size > 44 && String(rawBytes.sliceArray(0..3)) == "RIFF") 44 else 0
            val pcmLength = if (rawBytes.size > pcmOffset) rawBytes.size - pcmOffset else 16000 * 2

            val monoCount = pcmLength / 2
            val monoShorts = ShortArray(monoCount)

            if (rawBytes.size > pcmOffset) {
                ByteBuffer.wrap(rawBytes, pcmOffset, pcmLength).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(monoShorts)
            }

            // Stereo output: 2 channels (Left, Right)
            val stereoShorts = ShortArray(monoCount * 2)

            // Calculate interaural gains based on pan position
            val panNormalized = panPosition.coerceIn(-1.0f, 1.0f)
            val angleRad = (panNormalized + 1f) * (Math.PI / 4.0).toFloat() // 0 to PI/2
            val leftGain = (cos(angleRad) / depthDistance).coerceIn(0.1f, 1.0f)
            val rightGain = (sin(angleRad) / depthDistance).coerceIn(0.1f, 1.0f)

            // Interaural time delay in samples (approx up to 10 samples for realistic delay)
            val sampleDelay = ((panNormalized * 8f)).toInt()

            for (i in 0 until monoCount) {
                val sampleMono = monoShorts[i].toFloat()

                val delayedIndex = (i - sampleDelay).coerceIn(0, monoCount - 1)
                val delayedSample = monoShorts[delayedIndex].toFloat()

                val leftSample = (if (panNormalized > 0) delayedSample * leftGain else sampleMono * leftGain)
                val rightSample = (if (panNormalized < 0) delayedSample * rightGain else sampleMono * rightGain)

                val outIdx = i * 2
                stereoShorts[outIdx] = leftSample.coerceIn(-32767f, 32767f).toInt().toShort()
                stereoShorts[outIdx + 1] = rightSample.coerceIn(-32767f, 32767f).toInt().toShort()
            }

            val targetFile = File(context.cacheDir, "spatial3d_${System.currentTimeMillis()}.wav")
            FileOutputStream(targetFile).use { fos ->
                val pcmSize = stereoShorts.size * 2
                writeWavHeaderStereo(fos, pcmSize, 16000, 2, 16)
                val outBytes = ByteArray(pcmSize)
                ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(stereoShorts)
                fos.write(outBytes)
            }

            Log.i(tag, "Spatial 3D binaural render complete at ${targetFile.absolutePath}")
            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(tag, "Spatial audio rendering error", e)
            Result.failure(e)
        }
    }

    private fun writeWavHeaderStereo(out: FileOutputStream, pcmSize: Int, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val totalDataLen = pcmSize + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = (pcmSize shr 8 and 0xff).toByte()
        header[42] = (pcmSize shr 16 and 0xff).toByte()
        header[43] = (pcmSize shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}
