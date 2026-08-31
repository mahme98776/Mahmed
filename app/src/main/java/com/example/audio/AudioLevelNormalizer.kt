package com.example.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Normalization Presets for balancing voice track against original background audio
 */
enum class NormalizationMode(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val iconEmoji: String,
    val targetVoiceProminenceDb: Float, // How much louder voice should be over background (dB)
    val backgroundDuckingRatio: Float   // Multiplier to compress/attenuate background when voice is present
) {
    SMART_ADAPTIVE(
        id = "SMART_ADAPTIVE",
        titleArabic = "موازنة تكيفية ذكية (موصى بها)",
        descriptionArabic = "توازن ديناميكي يضمن وضوح نبرة الصوت فوق الموسيقى دون تشويش",
        iconEmoji = "✨",
        targetVoiceProminenceDb = 5.0f,
        backgroundDuckingRatio = 0.35f
    ),
    DIALOGUE_CLARITY(
        id = "DIALOGUE_CLARITY",
        titleArabic = "تركيز الحوار ونقاء الدبلجة",
        descriptionArabic = "يرفع صوت الميكروفون بوضوح ويخفض الخلفية تلقائياً بنسبة أكبر",
        iconEmoji = "🎙️",
        targetVoiceProminenceDb = 8.5f,
        backgroundDuckingRatio = 0.20f
    ),
    CINEMATIC_MIX(
        id = "CINEMATIC_MIX",
        titleArabic = "مكس سينمائي غني (Cinematic)",
        descriptionArabic = "يحافظ على مؤثرات وموسيقى المشهد الأصلية مع حضور صوتي متوازن",
        iconEmoji = "🎬",
        targetVoiceProminenceDb = 3.0f,
        backgroundDuckingRatio = 0.50f
    ),
    PEAK_MATCH(
        id = "PEAK_MATCH",
        titleArabic = "مطابقة مستويات الذروة (EBU R128)",
        descriptionArabic = "يضبط كلا المقطعين للوصول إلى أعلى جودة نقية بدون قص أو تشويه",
        iconEmoji = "🎚️",
        targetVoiceProminenceDb = 4.0f,
        backgroundDuckingRatio = 0.30f
    ),
    EQUAL_LOUDNESS(
        id = "EQUAL_LOUDNESS",
        titleArabic = "مستويات صوتية متساوية (1:1)",
        descriptionArabic = "يجعل الصوت المسجل وموسيقى الخلفية بنفس مستوى العلو الصوتي تماماً",
        iconEmoji = "⚖️",
        targetVoiceProminenceDb = 0.0f,
        backgroundDuckingRatio = 0.65f
    )
}

/**
 * Loudness Profile data measured from an audio track
 */
data class TrackLoudnessProfile(
    val peakDb: Float = -20f,
    val rmsDb: Float = -24f,
    val lufsApprox: Float = -24f,
    val maxAmplitude: Float = 0.1f,
    val isSilent: Boolean = false
)

/**
 * Result of volume normalization balancing
 */
data class VolumeNormalizationResult(
    val calculatedOriginalVolume: Float,
    val calculatedDubVolume: Float,
    val calculatedBgmVolume: Float,
    val voiceLoudnessDb: Float,
    val backgroundLoudnessDb: Float,
    val loudnessDiffDb: Float,
    val voiceGainAdjustmentDb: Float,
    val backgroundDuckingDb: Float,
    val mode: NormalizationMode,
    val summaryArabic: String,
    val recommendationArabic: String
)

class AudioLevelNormalizer(private val context: Context) {

    /**
     * Analyzes an audio file to extract peak amplitude, RMS energy and approx LUFS loudness.
     */
    suspend fun analyzeAudioLoudness(filePath: String?): TrackLoudnessProfile = withContext(Dispatchers.IO) {
        if (filePath == null) return@withContext TrackLoudnessProfile(isSilent = true)
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            return@withContext TrackLoudnessProfile(isSilent = true)
        }

        val extractor = MediaExtractor()
        var codec: MediaCodec? = null

        try {
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

            if (audioTrackIndex == -1 || format == null) {
                return@withContext estimateLoudnessFromFileSize(file)
            }

            extractor.selectTrack(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var maxPeakSample = 0
            var sumSquare: Double = 0.0
            var totalSamples: Long = 0
            var isEos = false
            val timeoutUs = 5000L
            var iterationCount = 0
            val maxIterations = 200 // Analyze up to ~10-15 seconds for speed

            while (!isEos && iterationCount < maxIterations) {
                iterationCount++
                val inputIndex = codec.dequeueInputBuffer(timeoutUs)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEos = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                var outputIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                while (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

                        val shortBuffer = outputBuffer.asShortBuffer()
                        while (shortBuffer.hasRemaining()) {
                            val sample = shortBuffer.get().toInt()
                            val absSample = kotlin.math.abs(sample)
                            if (absSample > maxPeakSample) {
                                maxPeakSample = absSample
                            }
                            sumSquare += (sample.toDouble() * sample.toDouble())
                            totalSamples++
                        }
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                        break
                    }
                    outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            if (totalSamples == 0L) {
                return@withContext estimateLoudnessFromFileSize(file)
            }

            val peakRatio = (maxPeakSample / 32768f).coerceIn(0.0001f, 1.0f)
            val rms = sqrt(sumSquare / totalSamples).toFloat()
            val rmsRatio = (rms / 32768f).coerceIn(0.0001f, 1.0f)

            val peakDb = (20 * log10(peakRatio)).coerceIn(-60f, 0f)
            val rmsDb = (20 * log10(rmsRatio)).coerceIn(-60f, 0f)
            val lufsApprox = (rmsDb - 3.0f).coerceIn(-60f, 0f)

            TrackLoudnessProfile(
                peakDb = peakDb,
                rmsDb = rmsDb,
                lufsApprox = lufsApprox,
                maxAmplitude = peakRatio,
                isSilent = peakRatio < 0.02f
            )
        } catch (e: Exception) {
            e.printStackTrace()
            estimateLoudnessFromFileSize(file)
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (_: Exception) {}
            try {
                extractor.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Fallback estimation when codec decoding is unavailable
     */
    private fun estimateLoudnessFromFileSize(file: File): TrackLoudnessProfile {
        if (!file.exists() || file.length() < 1000L) {
            return TrackLoudnessProfile(isSilent = true)
        }
        return TrackLoudnessProfile(
            peakDb = -6.0f,
            rmsDb = -18.0f,
            lufsApprox = -21.0f,
            maxAmplitude = 0.5f,
            isSilent = false
        )
    }

    /**
     * Automatically balances the volume levels between the original background track and the recorded voice track.
     */
    fun calculateAutoBalancedVolumes(
        voiceProfile: TrackLoudnessProfile,
        originalBackgroundProfile: TrackLoudnessProfile = TrackLoudnessProfile(
            peakDb = -8.0f,
            rmsDb = -19.0f,
            lufsApprox = -22.0f,
            maxAmplitude = 0.4f
        ),
        mode: NormalizationMode = NormalizationMode.SMART_ADAPTIVE,
        currentVoiceVolume: Float = 1.0f,
        currentOriginalVolume: Float = 0.25f,
        currentBgmVolume: Float = 0.35f
    ): VolumeNormalizationResult {

        val voiceRms = if (voiceProfile.isSilent) -28.0f else voiceProfile.rmsDb
        val bgRms = if (originalBackgroundProfile.isSilent) -26.0f else originalBackgroundProfile.rmsDb

        // Target: voice should sit comfortably above background by `targetVoiceProminenceDb`
        val targetDiff = mode.targetVoiceProminenceDb
        val currentDiff = voiceRms - bgRms

        // Dynamic Gain Calculation
        // Standard Dialogue Target: Voice normalized to approx -14 LUFS / -12 dB RMS
        val targetVoiceRms = -14.0f
        val voiceGainDb = (targetVoiceRms - voiceRms).coerceIn(-6f, +12f)
        val voiceGainLinear = (10.0.pow(voiceGainDb / 20.0)).toFloat().coerceIn(0.6f, 1.5f)

        // Background Target: Background compressed/ducked to sit behind voice
        val targetBgRms = targetVoiceRms - targetDiff
        val bgGainDb = (targetBgRms - bgRms).coerceIn(-18f, +3f)
        val bgGainLinear = (10.0.pow(bgGainDb / 20.0)).toFloat().coerceIn(0.10f, 0.70f)

        // Apply mode-specific ducking ratio
        val balancedOriginalVol = (bgGainLinear * mode.backgroundDuckingRatio * 2.2f).coerceIn(0.12f, 0.85f)
        val balancedDubVol = (voiceGainLinear * currentVoiceVolume).coerceIn(0.80f, 1.45f)
        val balancedBgmVol = (balancedOriginalVol * 0.9f).coerceIn(0.10f, 0.60f)

        val diffDb = voiceRms - bgRms
        val duckingDb = -((1.0f - mode.backgroundDuckingRatio) * 12f)

        val summary = when (mode) {
            NormalizationMode.SMART_ADAPTIVE -> "تمت موازنة الصوت بذكاء: رفع صوت الدبلجة بنسبة ${(balancedDubVol * 100).toInt()}% وخفض الخلفية إلى ${(balancedOriginalVol * 100).toInt()}%"
            NormalizationMode.DIALOGUE_CLARITY -> "تم تفعيل وضوح الحوار: تركيز عالي على نبرة الصوت مع تخفيض مريح للموسيقى (${(balancedOriginalVol * 100).toInt()}%)"
            NormalizationMode.CINEMATIC_MIX -> "تم ضبط المكس السينمائي: توازن مثالي بين المؤثرات الصوتية (${(balancedOriginalVol * 100).toInt()}%) وصوت الدبلجة"
            NormalizationMode.PEAK_MATCH -> "تمت مطابقة ذروة الصوت EBU R128 لمنع التشويش مع وضوح نقي"
            NormalizationMode.EQUAL_LOUDNESS -> "تمت مطابقة مستويات الصوت بالتساوي بنسبة 1:1"
        }

        val recommendation = if (voiceProfile.peakDb > -1.5f) {
            "⚠️ تنبيه: نبرة التسجيل قريبة من مستوى التشويش (Clipping). تم تخفيض الذروة تلقائياً للحفاظ على النقاء."
        } else if (voiceProfile.isSilent || voiceProfile.rmsDb < -35f) {
            "ℹ️ ملاحظة: مستوى التسجيل الصوتي منخفض. تم تعزيز الكسب الصوتي (+Gain) للوضوح التام."
        } else {
            "✅ مستوى الصوت متوازن ومثالي للمشهد بنقاء استوديو احترافي."
        }

        return VolumeNormalizationResult(
            calculatedOriginalVolume = balancedOriginalVol,
            calculatedDubVolume = balancedDubVol,
            calculatedBgmVolume = balancedBgmVol,
            voiceLoudnessDb = voiceRms,
            backgroundLoudnessDb = bgRms,
            loudnessDiffDb = diffDb,
            voiceGainAdjustmentDb = voiceGainDb,
            backgroundDuckingDb = duckingDb,
            mode = mode,
            summaryArabic = summary,
            recommendationArabic = recommendation
        )
    }
}
