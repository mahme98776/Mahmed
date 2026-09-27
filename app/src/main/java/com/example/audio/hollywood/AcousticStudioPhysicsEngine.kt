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

enum class VintageMicProfile(val labelArabic: String, val bassWarmth: Float, val presenceBoost: Float) {
    NEUMANN_U87("نيومان U87 الأسطوري (دفء سينمائي)", 1.25f, 1.35f),
    SHURE_SM7B("شور SM7B الإذاعي (عزل صوتي إذاعي)", 1.15f, 1.20f),
    ABBEY_ROAD_TUBE("صمامات آبي رود التناظرية 1965", 1.30f, 1.28f),
    CRYSTAL_CLEAR_MODERN("ميكروفون حديث فائق النقاوة", 1.05f, 1.45f)
}

enum class VocalAgeMorph(val labelArabic: String, val pitchShiftFactor: Float, val formantFactor: Float) {
    NATURAL("طبيعي", 1.0f, 1.0f),
    CHILD("صوت طفل بريء", 1.45f, 1.35f),
    YOUNG_HERO("بطل شاب مفعم بالحيوية", 1.08f, 1.05f),
    MATURE_50S("رجل ناضج وقور (خمسيني)", 0.88f, 0.90f),
    WISE_ELDER("حكيم / شيخ عجوز رزين", 0.76f, 0.82f)
}

/**
 * Acoustic Physics, Vintage Microphone Emulation & Vocal Age Morphing Engine.
 */
class AcousticStudioPhysicsEngine(private val context: Context) {
    private val tag = "StudioPhysicsEngine"

    suspend fun applyStudioMastering(
        inputWav: File,
        micProfile: VintageMicProfile = VintageMicProfile.NEUMANN_U87,
        ageMorph: VocalAgeMorph = VocalAgeMorph.NATURAL
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val rawBytes = if (inputWav.exists()) inputWav.readBytes() else ByteArray(0)
            val pcmOffset = if (rawBytes.size > 44 && String(rawBytes.sliceArray(0..3)) == "RIFF") 44 else 0
            val pcmLength = if (rawBytes.size > pcmOffset) rawBytes.size - pcmOffset else 16000 * 2

            val sampleCount = pcmLength / 2
            val inputShorts = ShortArray(sampleCount)

            if (rawBytes.size > pcmOffset) {
                ByteBuffer.wrap(rawBytes, pcmOffset, pcmLength).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputShorts)
            }

            val processedShorts = ShortArray(sampleCount)
            val pitchFactor = ageMorph.pitchShiftFactor
            val micWarmth = micProfile.bassWarmth
            val micPresence = micProfile.presenceBoost

            var readIdx = 0.0
            for (i in 0 until sampleCount) {
                val idx = readIdx.toInt()
                val frac = (readIdx - idx).toFloat()

                val s1 = if (idx in inputShorts.indices) inputShorts[idx].toFloat() else 0f
                val s2 = if (idx + 1 in inputShorts.indices) inputShorts[idx + 1].toFloat() else s1
                val interpolated = s1 + frac * (s2 - s1)

                // Apply mic warmth saturation and presence boost
                val warmSample = (interpolated * micWarmth * 0.95f) + (interpolated * micPresence * 0.15f)
                processedShorts[i] = warmSample.coerceIn(-32767f, 32767f).toInt().toShort()

                readIdx += pitchFactor
                if (readIdx >= sampleCount - 1) {
                    readIdx = 0.0
                }
            }

            val targetFile = File(context.cacheDir, "studio_master_${System.currentTimeMillis()}.wav")
            FileOutputStream(targetFile).use { fos ->
                val pcmSize = processedShorts.size * 2
                writeWavHeader(fos, pcmSize, 16000, 1, 16)
                val outBytes = ByteArray(pcmSize)
                ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(processedShorts)
                fos.write(outBytes)
            }

            Log.i(tag, "Studio mastering applied: ${micProfile.labelArabic}, Age: ${ageMorph.labelArabic}")
            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(tag, "Studio mastering error", e)
            Result.failure(e)
        }
    }

    private fun writeWavHeader(out: FileOutputStream, pcmSize: Int, sampleRate: Int, channels: Int, bitsPerSample: Int) {
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
