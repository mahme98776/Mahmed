package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

data class SoundEffectItem(
    val id: String,
    val titleArabic: String,
    val emoji: String,
    val description: String,
    val color: Long
)

object SoundEffectsGenerator {

    val soundEffectsList: List<SoundEffectItem> = listOf(
        SoundEffectItem("spacetoon_hero_entry", "دخول بطل سبيستون", "🦸", "لحن حماسي ملحمي لدخول بطل سبيستون", 0xFF10B981),
        SoundEffectItem("spacetoon_power", "طاقة سبيستون الخارقة", "⚡", "صوت وميض وهالة طاقة خارقة للأنمي", 0xFFF59E0B),
        SoundEffectItem("spacetoon_fight", "ضربة قتال وسيف أنمي", "⚔️", "صوت التحام السيوف وضربات الأكشن", 0xFFEF4444),
        SoundEffectItem("spacetoon_laser", "شعاع ليزر وسحر", "🔮", "صوت إطلاق شعاع سحري وقوى خارقة", 0xFF8B5CF6),
        SoundEffectItem("spacetoon_sting", "فاصل سبيستون النغمي", "✨", "فاصل كلاسيكي جميل بين مشاهد الكرتون", 0xFF06B6D4),
        SoundEffectItem("applause", "تصفيق حار", "👏", "مؤثر تصفيق للجمهور والاحتفال", 0xFF14B8A6),
        SoundEffectItem("laugh", "ضحكات كوميدية", "😂", "مؤثر ضحك للمشاهد المضحكة", 0xFFF59E0B),
        SoundEffectItem("boom", "انفجار سينمائي", "💥", "ضربة درامية قوية للمشاهد المثيرة", 0xFFEF4444),
        SoundEffectItem("fanfare", "لحن الانتصار", "🎺", "نغمات فوز وتتويج حماسية", 0xFF8B5CF6),
        SoundEffectItem("ding", "جرس تنبيه", "🔔", "صوت تنبيه لامع وذكي", 0xFF06B6D4),
        SoundEffectItem("boing", "قفزة كرتونية", "🦆", "قفزة مرحة للرسوم المتحركة", 0xFFEC4899),
        SoundEffectItem("sad_violin", "كمان حزين", "🎻", "لحن درامي حزين للمشاهد المؤثرة", 0xFF6366F1),
        SoundEffectItem("radio_beep", "إشارة لاسلكي", "📻", "تصفير ورنين أجهزة اللاسلكي", 0xFF14B8A6)
    )

    private val sampleRate = 44100
    private var activeBgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null

    fun playSoundEffect(effectId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pcmData = generateSfxPcm(effectId)
                playPcm(pcmData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playPcm(buffer: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackSize = maxOf(minBufferSize, buffer.size * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        
        // Auto release after sound plays
        val durationMs = (buffer.size * 1000L) / sampleRate
        CoroutineScope(Dispatchers.Default).launch {
            delay(durationMs + 300)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    private fun generateSfxPcm(effectId: String): ShortArray {
        return when (effectId) {
            "spacetoon_hero_entry" -> generateSpacetoonHeroEntry()
            "spacetoon_power" -> generateSpacetoonPower()
            "spacetoon_fight" -> generateSpacetoonFight()
            "spacetoon_laser" -> generateSpacetoonLaser()
            "spacetoon_sting" -> generateSpacetoonSting()
            "applause" -> generateApplause()
            "laugh" -> generateLaugh()
            "boom" -> generateBoom()
            "fanfare" -> generateFanfare()
            "ding" -> generateDing()
            "boing" -> generateBoing()
            "sad_violin" -> generateSadViolin()
            "radio_beep" -> generateRadioBeep()
            else -> generateDing()
        }
    }

    private fun generateSpacetoonHeroEntry(): ShortArray {
        val durationMs = 1800
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        // Spacetoon iconic brass/synth hero fanfare chords: D4 -> G4 -> A4 -> D5
        val notes = listOf(293.66, 392.00, 440.00, 587.33)
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val noteIndex = (i / step).coerceIn(0, notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val noteTime = (i % step).toDouble() / sampleRate
            val env = exp(-2.2 * noteTime)
            // Rich harmonic synth brass
            val sample = (sin(2 * PI * freq * t) * 0.5 + sin(4 * PI * freq * t) * 0.3 + sin(6 * PI * freq * t) * 0.15) * env
            buffer[i] = (sample * 27000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSpacetoonPower(): ShortArray {
        val durationMs = 1200
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Rising energetic pitch sweep + energy modulation
            val pitch = 220.0 + 800.0 * (t / 1.2) + sin(2 * PI * 24.0 * t) * 80.0
            val env = sin(PI * (t / 1.2))
            val sparkle = sin(2 * PI * 1800.0 * t) * 0.2
            val sample = (sin(2 * PI * pitch * t) * 0.7 + sparkle) * env
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSpacetoonFight(): ShortArray {
        val durationMs = 900
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-4.5 * t)
            // Metallic sword clank + impact
            val metallic1 = sin(2 * PI * 2400.0 * t) * 0.4
            val metallic2 = sin(2 * PI * 3600.0 * t) * 0.3
            val impact = sin(2 * PI * 140.0 * t) * 0.5
            val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-9.0 * t) * 0.4
            val sample = (metallic1 + metallic2 + impact + noise) * decay
            buffer[i] = (sample * 29000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSpacetoonLaser(): ShortArray {
        val durationMs = 750
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-3.2 * t)
            // Fast downwards sweeping laser beam
            val freq = 2600.0 * exp(-4.0 * t) + 300.0
            val sample = sin(2 * PI * freq * t) * decay
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSpacetoonSting(): ShortArray {
        val durationMs = 1100
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        // Spacetoon glittering chime arpeggio: C5 -> G5 -> E6
        val notes = listOf(523.25, 783.99, 1318.51)
        val step = numSamples / 3
        for (i in 0 until numSamples) {
            val noteIndex = (i / step).coerceIn(0, notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val noteTime = (i % step).toDouble() / sampleRate
            val env = exp(-3.0 * noteTime)
            val sample = (sin(2 * PI * freq * t) * 0.7 + sin(4 * PI * freq * t) * 0.3) * env
            buffer[i] = (sample * 25000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateDing(): ShortArray {
        val durationMs = 800
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        val freq1 = 880.0
        val freq2 = 1760.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-4.5 * t)
            val sample = (sin(2 * PI * freq1 * t) * 0.6 + sin(2 * PI * freq2 * t) * 0.4) * envelope
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBoom(): ShortArray {
        val durationMs = 1200
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-3.0 * t)
            val freq = 120.0 * exp(-3.5 * t) + 40.0
            val subBass = sin(2 * PI * freq * t) * 0.7
            val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-6.0 * t) * 0.5
            val sample = (subBass + noise) * decay
            buffer[i] = (sample * 30000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateBoing(): ShortArray {
        val durationMs = 700
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val pitch = 250.0 + 400.0 * (t / 0.7) + sin(2 * PI * 18.0 * t) * 70.0
            val envelope = exp(-2.5 * t)
            val sample = sin(2 * PI * pitch * t) * envelope
            buffer[i] = (sample * 28000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateFanfare(): ShortArray {
        val durationMs = 1500
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val noteIndex = (i / step).coerceIn(0, notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val noteTime = (i % step).toDouble() / sampleRate
            val env = exp(-2.0 * noteTime)
            val sample = (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) * env
            buffer[i] = (sample * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateSadViolin(): ShortArray {
        val durationMs = 1800
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        val freqBase = 440.0 // A4
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val vibrato = sin(2 * PI * 5.0 * t) * 6.0
            val freq = (freqBase - 40.0 * (t / 1.8)) + vibrato
            val env = sin(PI * (t / 1.8))
            val sample = (sin(2 * PI * freq * t) + 0.4 * sin(2 * PI * (freq * 2) * t) + 0.2 * sin(2 * PI * (freq * 3) * t)) * env
            buffer[i] = (sample * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateLaugh(): ShortArray {
        val durationMs = 1200
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val pulse = (sin(2 * PI * 6.0 * t) + 1.0) / 2.0 // laughing chuckle pulse
            val freq = 360.0 + sin(2 * PI * 12.0 * t) * 40.0
            val sample = sin(2 * PI * freq * t) * pulse * exp(-1.2 * t)
            buffer[i] = (sample * 25000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateApplause(): ShortArray {
        val durationMs = 1400
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val clapBurst = (sin(2 * PI * 14.0 * t) + 1.0) * 0.4 + 0.2
            val noise = (Random.nextDouble() * 2.0 - 1.0) * clapBurst * exp(-0.8 * t)
            buffer[i] = (noise * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun generateRadioBeep(): ShortArray {
        val durationMs = 600
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = if (t < 0.2) 1200.0 else if (t < 0.4) 1800.0 else 1000.0
            val sample = sin(2 * PI * freq * t) * 0.8
            buffer[i] = (sample * 26000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    fun startAmbientBgm(style: BgmStyle, volume: Float, scope: CoroutineScope) {
        stopBgm()
        if (style == BgmStyle.NONE || volume <= 0.01f) return

        activeBgmJob = scope.launch(Dispatchers.Default) {
            try {
                val chordFreqs = when (style) {
                    BgmStyle.CINEMATIC -> listOf(220.0, 277.18, 329.63, 440.0) // A minor / Ambient
                    BgmStyle.FUNNY -> listOf(261.63, 329.63, 392.0, 523.25) // C major playful
                    BgmStyle.DRAMATIC -> listOf(196.0, 233.08, 293.66, 392.0) // G minor
                    BgmStyle.LOFI -> listOf(293.66, 349.23, 440.0, 523.25) // D minor 7
                    BgmStyle.SPACETOON -> listOf(261.63, 329.63, 392.0, 587.33) // C Major 9 Heroic Spacetoon Chord
                    else -> emptyList()
                }
                if (chordFreqs.isEmpty()) return@launch

                val durationSec = 4
                val samples = sampleRate * durationSec
                val pcmLoop = ShortArray(samples)
                for (i in 0 until samples) {
                    val t = i.toDouble() / sampleRate
                    var sum = 0.0
                    for (f in chordFreqs) {
                        sum += sin(2 * PI * f * t) * (1.0 / chordFreqs.size)
                    }
                    val swell = (sin(2 * PI * (1.0 / durationSec) * t) + 1.0) / 2.0
                    pcmLoop[i] = (sum * swell * 18000 * volume).toInt().coerceIn(-32767, 32767).toShort()
                }

                val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBuf, pcmLoop.size * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()
                while (isActive) {
                    bgmTrack?.write(pcmLoop, 0, pcmLoop.size)
                }
            } catch (_: Exception) {}
        }
    }

    fun stopBgm() {
        activeBgmJob?.cancel()
        activeBgmJob = null
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
        } catch (_: Exception) {}
        bgmTrack = null
    }
}
