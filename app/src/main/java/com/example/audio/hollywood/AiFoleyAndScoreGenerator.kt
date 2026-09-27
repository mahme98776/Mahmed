package com.example.audio.hollywood

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class FoleyCategory(val labelArabic: String, val baseFreqHz: Float, val decayRate: Float) {
    CINEMATIC_BOOM("ضربة سينمائية عميقة", 55f, 0.999f),
    SWOOSH_TRANSITION("حركة وانتقال سريع (Swoosh)", 320f, 0.995f),
    RAIN_AND_THUNDER("مطر ورعد درامي", 80f, 0.998f),
    FOOTSTEPS("وقع أقدام متزامنة", 120f, 0.97f),
    HEARTBEAT_SUSPENSE("نبضات قلب وتشويق", 65f, 0.985f),
    DOOR_CREAK("صرير باب غامض", 240f, 0.99f);
}

enum class CinematicScoreStyle(val labelArabic: String, val chordRoots: List<Float>) {
    HEROIC_ACTION("حماسي وبطولي", listOf(220f, 277f, 330f, 440f)),
    MYSTERY_SUSPENSE("غموض وتشويق", listOf(146f, 174f, 220f, 293f)),
    EMOTIONAL_DRAMA("دراما وعاطفة", listOf(196f, 246f, 293f, 392f)),
    EPIC_TRAILER("تريلر سينمائي ملحمي", listOf(110f, 138f, 164f, 220f));
}

/**
 * AI Foley & Cinematic Film Score Generator.
 * Generates custom synthesized SFX layers and adaptive orchestral scores.
 */
class AiFoleyAndScoreGenerator(private val context: Context) {
    private val tag = "FoleyAndScoreGenerator"

    /**
     * Synthesizes a foley effect sound file.
     */
    suspend fun generateFoleyEffectWav(
        category: FoleyCategory,
        durationSec: Float = 2.0f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sampleRate = 16000
            val totalSamples = (sampleRate * durationSec).toInt()
            val targetFile = File(context.cacheDir, "foley_${category.name.lowercase()}_${System.currentTimeMillis()}.wav")

            val shorts = ShortArray(totalSamples)
            var currentFreq = category.baseFreqHz
            var envelope = 1.0f

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                val phase = 2.0 * PI * currentFreq * t
                val tone = sin(phase).toFloat()
                val noise = (Random.nextFloat() * 2f - 1f) * 0.15f
                val sampleValue = ((tone + noise) * envelope * 24000f).coerceIn(-32767f, 32767f)
                shorts[i] = sampleValue.toInt().toShort()

                envelope *= category.decayRate
                if (category == FoleyCategory.CINEMATIC_BOOM) {
                    currentFreq *= 0.9999f
                }
            }

            FileOutputStream(targetFile).use { fos ->
                val pcmSize = shorts.size * 2
                writeWavHeader(fos, pcmSize, sampleRate, 1, 16)
                val outBytes = ByteArray(pcmSize)
                ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts)
                fos.write(outBytes)
            }

            Log.i(tag, "Synthesized foley effect: ${category.name} at ${targetFile.absolutePath}")
            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(tag, "Failed to generate foley effect", e)
            Result.failure(e)
        }
    }

    /**
     * Synthesizes an ambient cinematic background music track.
     */
    suspend fun generateCinematicScoreWav(
        style: CinematicScoreStyle,
        durationSec: Float = 10.0f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sampleRate = 16000
            val totalSamples = (sampleRate * durationSec).toInt()
            val targetFile = File(context.cacheDir, "score_${style.name.lowercase()}_${System.currentTimeMillis()}.wav")

            val shorts = ShortArray(totalSamples)
            val roots = style.chordRoots

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                var compositeWave = 0f

                for ((idx, rootFreq) in roots.withIndex()) {
                    val detune = 1.0f + (idx * 0.003f)
                    val wave = sin(2.0 * PI * rootFreq * detune * t).toFloat()
                    compositeWave += wave / roots.size.toFloat()
                }

                // Add subtle slow tremolo pulse
                val tremolo = 0.8f + 0.2f * sin(2.0 * PI * 1.5 * t).toFloat()
                val finalSample = (compositeWave * tremolo * 16000f).coerceIn(-32767f, 32767f)
                shorts[i] = finalSample.toInt().toShort()
            }

            FileOutputStream(targetFile).use { fos ->
                val pcmSize = shorts.size * 2
                writeWavHeader(fos, pcmSize, sampleRate, 1, 16)
                val outBytes = ByteArray(pcmSize)
                ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts)
                fos.write(outBytes)
            }

            Log.i(tag, "Synthesized cinematic score: ${style.name} at ${targetFile.absolutePath}")
            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(tag, "Failed to generate cinematic score", e)
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
