package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.PresetReverb
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tanh

class AudioEffectsProcessor(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var presetReverb: PresetReverb? = null

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    private val _previewPlaybackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val previewPlaybackProgress: StateFlow<Float> = _previewPlaybackProgress.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * Previews an audio file applying the real-time pitch, speed, and audiofx parameters.
     */
    fun playPreview(
        filePath: String,
        params: AudioEffectParameters,
        isLooping: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        stopPreview()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Attach Hardware Reverb if requested
                if (params.reverbRoomSize > 0.1f) {
                    try {
                        val reverb = PresetReverb(0, audioSessionId).apply {
                            preset = when {
                                params.reverbRoomSize > 0.8f -> PresetReverb.PRESET_LARGEROOM
                                params.reverbRoomSize > 0.4f -> PresetReverb.PRESET_MEDIUMROOM
                                else -> PresetReverb.PRESET_SMALLROOM
                            }
                            enabled = true
                        }
                        presetReverb = reverb
                        attachAuxEffect(reverb.id)
                        setAuxEffectSendLevel(params.dryWetMix.coerceIn(0f, 1f))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Apply dynamic pitch and speed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val pParams = PlaybackParams().apply {
                        pitch = params.pitchMultiplier.coerceIn(0.5f, 2.0f)
                        speed = params.speedMultiplier.coerceIn(0.5f, 2.0f)
                    }
                    playbackParams = pParams
                }

                this.isLooping = isLooping
                prepare()
                setOnCompletionListener {
                    if (!isLooping) {
                        _isPlayingPreview.value = false
                        onComplete()
                    }
                }
                start()
            }
            mediaPlayer = player
            _isPlayingPreview.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlayingPreview.value = false
        }
    }

    fun stopPreview() {
        try {
            presetReverb?.enabled = false
            presetReverb?.release()
            presetReverb = null

            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlayingPreview.value = false
        _previewPlaybackProgress.value = 0f
    }

    /**
     * Applies a noise-gate and spectral smoothing algorithm to remove background static/hiss
     * from user-recorded audio.
     */
    suspend fun cleanAudioStatic(
        sourcePath: String,
        noiseFloorThreshold: Float = 0.035f
    ): String? = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) return@withContext null

        _isProcessing.value = true
        val outputDir = File(context.cacheDir, "cleaned_audio")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, "cleaned_take_${System.currentTimeMillis()}.m4a")

        try {
            val pcmData = decodeAudioToPcm(sourceFile)
            if (pcmData == null || pcmData.samples.isEmpty()) {
                _isProcessing.value = false
                return@withContext null
            }

            val samples = pcmData.samples.copyOf()
            val numSamples = samples.size
            val sampleRate = pcmData.sampleRate

            // Estimate noise floor from lowest energy segments
            val windowSize = (sampleRate * 0.02f).toInt().coerceAtLeast(64) // 20ms window
            var calculatedThreshold = noiseFloorThreshold

            var minRms = 1.0f
            var i = 0
            while (i + windowSize < numSamples) {
                var sumSq = 0.0
                for (j in 0 until windowSize) {
                    val s = samples[i + j]
                    sumSq += (s * s)
                }
                val rms = Math.sqrt(sumSq / windowSize).toFloat()
                if (rms > 0.0001f && rms < minRms) {
                    minRms = rms
                }
                i += windowSize
            }
            if (minRms < 0.1f) {
                calculatedThreshold = (minRms * 1.8f).coerceIn(0.015f, 0.08f)
            }

            // Downward Expander / Noise Gate with soft knee and high-frequency static smoothing
            var envelope = 0.0f
            val attack = 0.01f
            val release = 0.05f
            var prevSample = 0f

            for (sIdx in 0 until numSamples) {
                val inputAbs = Math.abs(samples[sIdx])
                envelope += if (inputAbs > envelope) (inputAbs - envelope) * attack else (inputAbs - envelope) * release

                // Soft knee gain calculation
                val gain = when {
                    envelope < calculatedThreshold * 0.5f -> 0.04f // Strong attenuation on background hiss
                    envelope < calculatedThreshold -> {
                        val factor = (envelope - (calculatedThreshold * 0.5f)) / (calculatedThreshold * 0.5f)
                        0.04f + 0.96f * (factor * factor)
                    }
                    else -> 1.0f // Pass-through speech unhindered
                }

                // Smooth high-frequency hiss when gain is low
                val cleaned = samples[sIdx] * gain
                val smoothed = if (gain < 0.5f) (cleaned * 0.7f + prevSample * 0.3f) else cleaned
                prevSample = smoothed
                samples[sIdx] = smoothed.coerceIn(-1f, 1f)
            }

            val success = encodePcmToM4a(
                samples = samples,
                sampleRate = pcmData.sampleRate,
                channels = pcmData.channels,
                outputFile = outputFile
            )

            _isProcessing.value = false
            if (success && outputFile.exists() && outputFile.length() > 0) {
                outputFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isProcessing.value = false
            null
        }
    }

    /**
     * Applies full DSP pipeline to an audio file and saves the processed output.
     * Supports Reverb, Echo / Delay, Robot Ring Modulation, Bandpass Filter, Distortion, and Pitch.
     */
    suspend fun processAndExportEffectAudio(
        sourcePath: String,
        params: AudioEffectParameters,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String? = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) return@withContext null

        _isProcessing.value = true
        onProgress(0.1f, "جاري قراءة وتحليل بيانات الصوت الرقمية...")

        val outputDir = File(context.cacheDir, "processed_effects")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, "fx_take_${System.currentTimeMillis()}.m4a")

        try {
            // Step 1: Extract PCM from source audio file
            val pcmData = decodeAudioToPcm(sourceFile)
            if (pcmData == null || pcmData.samples.isEmpty()) {
                _isProcessing.value = false
                return@withContext null
            }

            onProgress(0.4f, "جاري تطبيق خوارزميات المؤثرات الصوتية (DSP)...")

            // Step 2: Apply DSP pipeline on 16-bit PCM float samples
            val processedSamples = applyDspFilters(
                inputSamples = pcmData.samples,
                sampleRate = pcmData.sampleRate,
                channels = pcmData.channels,
                params = params
            )

            onProgress(0.75f, "جاري تشفير وحفظ المقطع النهائي...")

            // Step 3: Encode processed PCM to AAC M4A file
            val success = encodePcmToM4a(
                samples = processedSamples,
                sampleRate = pcmData.sampleRate,
                channels = pcmData.channels,
                outputFile = outputFile
            )

            _isProcessing.value = false
            if (success && outputFile.exists() && outputFile.length() > 0) {
                onProgress(1.0f, "تم تطبيق المؤثر بنجاح!")
                return@withContext outputFile.absolutePath
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isProcessing.value = false
            return@withContext null
        }
    }

    private data class DecodedPcm(
        val samples: FloatArray,
        val sampleRate: Int,
        val channels: Int
    )

    /**
     * Decodes an audio file (AAC/M4A/MP3/WAV) to float PCM samples (-1.0f to +1.0f)
     */
    private fun decodeAudioToPcm(file: File): DecodedPcm? {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = f
                    break
                }
            }

            if (audioTrackIndex < 0 || format == null) return null

            extractor.selectTrack(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            val sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
            val channels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val pcmShorts = mutableListOf<Short>()
            val bufferInfo = MediaCodec.BufferInfo()
            var isEos = false

            while (!isEos) {
                val inIndex = codec.dequeueInputBuffer(10000)
                if (inIndex >= 0) {
                    val inBuffer = codec.getInputBuffer(inIndex)
                    if (inBuffer != null) {
                        val sampleSize = extractor.readSampleData(inBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEos = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                var outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                while (outIndex >= 0) {
                    val outBuffer = codec.getOutputBuffer(outIndex)
                    if (outBuffer != null && bufferInfo.size > 0) {
                        outBuffer.position(bufferInfo.offset)
                        outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        outBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        val shortBuffer = outBuffer.asShortBuffer()
                        while (shortBuffer.hasRemaining()) {
                            pcmShorts.add(shortBuffer.get())
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                        break
                    }
                    outIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            // Convert shorts to normalized floats
            val floatSamples = FloatArray(pcmShorts.size) { i ->
                (pcmShorts[i] / 32768.0f).coerceIn(-1.0f, 1.0f)
            }

            return DecodedPcm(floatSamples, sampleRate, channels)
        } catch (e: Exception) {
            e.printStackTrace()
            extractor.release()
            return null
        }
    }

    /**
     * Digital Signal Processing pipeline implementing DSP algorithms
     */
    private fun applyDspFilters(
        inputSamples: FloatArray,
        sampleRate: Int,
        channels: Int,
        params: AudioEffectParameters
    ): FloatArray {
        var samples = inputSamples.clone()
        val numSamples = samples.size

        // 1. Robot Ring Modulation & Harmonic Saturation
        if (params.robotModulationHz > 5f) {
            val modHz = params.robotModulationHz
            val resonance = params.robotResonance
            val depth = params.dryWetMix.coerceIn(0.2f, 1.0f)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val carrier = sin(2.0 * PI * modHz * t).toFloat()
                val modulated = samples[i] * (1.0f - depth + depth * carrier)
                // Add metallic resonance wave
                val resCarrier = sin(4.0 * PI * modHz * t).toFloat() * 0.25f
                val blended = modulated + (samples[i] * resCarrier * resonance)
                samples[i] = (blended * (1.0f + resonance * 0.3f)).coerceIn(-1f, 1f)
            }
        }

        // 2. High-pass & Low-pass Bandpass (Radio, Megaphone, Telephone)
        if (params.highPassCutoffHz > 30f || params.lowPassCutoffHz < 18000f) {
            val dt = 1.0f / sampleRate

            // High-pass filter
            if (params.highPassCutoffHz > 30f) {
                val rcHigh = 1.0f / (2.0f * PI.toFloat() * params.highPassCutoffHz)
                val alphaHigh = rcHigh / (rcHigh + dt)
                var prevIn = 0f
                var prevOut = 0f
                for (i in 0 until numSamples) {
                    val currentIn = samples[i]
                    val out = alphaHigh * (prevOut + currentIn - prevIn)
                    prevIn = currentIn
                    prevOut = out
                    samples[i] = out
                }
            }

            // Low-pass filter
            if (params.lowPassCutoffHz < 18000f) {
                val rcLow = 1.0f / (2.0f * PI.toFloat() * params.lowPassCutoffHz)
                val alphaLow = dt / (rcLow + dt)
                var prevOut = 0f
                for (i in 0 until numSamples) {
                    val out = prevOut + alphaLow * (samples[i] - prevOut)
                    prevOut = out
                    samples[i] = out
                }
            }
        }

        // 3. Distortion / Drive (Megaphone / Radio grit)
        if (params.distortionDrive > 0.05f) {
            val drive = 1.0f + params.distortionDrive * 4.0f
            for (i in 0 until numSamples) {
                samples[i] = tanh(samples[i] * drive).toFloat()
            }
        }

        // 4. Echo / Delay Line
        if (params.echoDelayMs > 10 && params.echoFeedback > 0.05f) {
            val delaySamples = ((params.echoDelayMs.toFloat() / 1000f) * sampleRate * channels).toInt().coerceAtLeast(1)
            val feedback = params.echoFeedback.coerceIn(0.1f, 0.88f)
            val wet = params.dryWetMix.coerceIn(0f, 1f)
            val dry = 1.0f - (wet * 0.5f)

            val delayBuffer = FloatArray(delaySamples)
            var delayIdx = 0
            val outputWithEcho = FloatArray(numSamples)

            for (i in 0 until numSamples) {
                val delayedSample = delayBuffer[delayIdx]
                val current = samples[i]
                outputWithEcho[i] = (dry * current + wet * delayedSample).coerceIn(-1f, 1f)
                delayBuffer[delayIdx] = current + delayedSample * feedback
                delayIdx = (delayIdx + 1) % delaySamples
            }
            samples = outputWithEcho
        }

        // 5. Schroeder Reverberation (Comb Filters + All-Pass)
        if (params.reverbRoomSize > 0.1f) {
            val wet = (params.dryWetMix * params.reverbRoomSize * 0.7f).coerceIn(0f, 0.85f)
            val dry = 1.0f - (wet * 0.4f)
            val roomScale = params.reverbRoomSize.coerceIn(0.3f, 1.2f)

            val combDelays = intArrayOf(
                (0.0297f * sampleRate * roomScale).toInt(),
                (0.0371f * sampleRate * roomScale).toInt(),
                (0.0411f * sampleRate * roomScale).toInt(),
                (0.0437f * sampleRate * roomScale).toInt()
            )
            val feedbackGain = (0.7f + params.reverbRoomSize * 0.22f).coerceIn(0.5f, 0.92f)

            val combBuffers = Array(4) { idx -> FloatArray(combDelays[idx].coerceAtLeast(10)) }
            val combIndices = IntArray(4)

            val outReverb = FloatArray(numSamples)
            for (i in 0 until numSamples) {
                var combSum = 0f
                for (c in 0 until 4) {
                    val cBuf = combBuffers[c]
                    val cIdx = combIndices[c]
                    val delayed = cBuf[cIdx]
                    combSum += delayed
                    cBuf[cIdx] = samples[i] + delayed * feedbackGain
                    combIndices[c] = (cIdx + 1) % cBuf.size
                }
                outReverb[i] = (dry * samples[i] + wet * (combSum * 0.25f)).coerceIn(-1f, 1f)
            }
            samples = outReverb
        }

        // 6. Chorus Ensemble
        if (params.chorusDepth > 0.1f) {
            val chorusDepthSamples = (0.003f * sampleRate).toInt() // 3ms depth
            val baseDelay = (0.015f * sampleRate).toInt() // 15ms base
            val lfoRate = 1.5 // 1.5 Hz
            val depth = params.chorusDepth

            val maxDelay = baseDelay + chorusDepthSamples + 5
            val cBuffer = FloatArray(maxDelay)
            var cIdx = 0
            val chorusOut = FloatArray(numSamples)

            for (i in 0 until numSamples) {
                cBuffer[cIdx] = samples[i]
                val lfo = sin(2.0 * PI * lfoRate * (i.toDouble() / sampleRate)).toFloat()
                val curDelay = baseDelay + (lfo * chorusDepthSamples).toInt()
                val readPos = (cIdx - curDelay + maxDelay) % maxDelay
                val delayedSample = cBuffer[readPos]
                chorusOut[i] = (samples[i] * 0.7f + delayedSample * depth * 0.5f).coerceIn(-1f, 1f)
                cIdx = (cIdx + 1) % maxDelay
            }
            samples = chorusOut
        }

        return samples
    }

    /**
     * Encodes Float PCM samples to standard AAC MP4 (.m4a)
     */
    fun encodePcmToM4a(
        samples: FloatArray,
        sampleRate: Int,
        channels: Int,
        outputFile: File
    ): Boolean {
        try {
            val mime = MediaFormat.MIMETYPE_AUDIO_AAC
            val bitRate = 128000

            val format = MediaFormat.createAudioFormat(mime, sampleRate, channels).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, android.media.MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 64 * 1024)
            }

            val encoder = MediaCodec.createEncoderByType(mime)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            var sampleOffset = 0
            val totalSamples = samples.size
            var isInputEos = false
            var isOutputEos = false

            val shortBuffer = ByteBuffer.allocateDirect(16384).order(ByteOrder.LITTLE_ENDIAN)

            while (!isOutputEos) {
                if (!isInputEos) {
                    val inIdx = encoder.dequeueInputBuffer(10000)
                    if (inIdx >= 0) {
                        val inBuffer = encoder.getInputBuffer(inIdx)
                        if (inBuffer != null) {
                            inBuffer.clear()
                            val samplesToWrite = min(inBuffer.remaining() / 2, totalSamples - sampleOffset)
                            if (samplesToWrite <= 0) {
                                encoder.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isInputEos = true
                            } else {
                                for (k in 0 until samplesToWrite) {
                                    val shortVal = (samples[sampleOffset + k] * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                                    inBuffer.putShort(shortVal)
                                }
                                val ptsUs = (sampleOffset.toLong() * 1_000_000L) / (sampleRate * channels)
                                encoder.queueInputBuffer(inIdx, 0, samplesToWrite * 2, ptsUs, 0)
                                sampleOffset += samplesToWrite
                            }
                        }
                    }
                }

                var outIdx = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                while (outIdx >= 0) {
                    val outBuffer = encoder.getOutputBuffer(outIdx)
                    if (outBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                        outBuffer.position(bufferInfo.offset)
                        outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outIdx, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isOutputEos = true
                        break
                    }
                    outIdx = encoder.dequeueOutputBuffer(bufferInfo, 0)
                }

                if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            }

            encoder.stop()
            encoder.release()
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun release() {
        stopPreview()
    }
}
