package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class InstantDubbingMode(
    val id: String,
    val titleArabic: String,
    val subtitleArabic: String,
    val iconEmoji: String,
    val basePitchMultiplier: Float,
    val baseSpeedMultiplier: Float
) {
    AUTO_DETECT(
        id = "AUTO_DETECT",
        titleArabic = "تحويل تلقائي ذكي (ذكر ➔ أنثى / أنثى ➔ ذكر)",
        subtitleArabic = "يكتشف نبرتك ويحولها للجنس الآخر تلقائياً فورياً",
        iconEmoji = "✨",
        basePitchMultiplier = 1.0f,
        baseSpeedMultiplier = 1.0f
    ),
    MALE_TO_FEMALE(
        id = "MALE_TO_FEMALE",
        titleArabic = "تحويل فوري: ذكر إلى أنثى ♀️",
        subtitleArabic = "يرفع طبقة الصوت بنعومة ليصبح صوتاً نسائياً ناعماً",
        iconEmoji = "👩",
        basePitchMultiplier = 1.45f,
        baseSpeedMultiplier = 1.05f
    ),
    FEMALE_TO_MALE(
        id = "FEMALE_TO_MALE",
        titleArabic = "تحويل فوري: أنثى إلى ذكر ♂️",
        subtitleArabic = "يخفض طبقة الصوت ليصبح صوتاً رجالياً عميقاً وجهورياً",
        iconEmoji = "👨",
        basePitchMultiplier = 0.72f,
        baseSpeedMultiplier = 0.94f
    ),
    TO_CARTOON(
        id = "TO_CARTOON",
        titleArabic = "تحويل لصوت كرتوني مرح 🧒",
        subtitleArabic = "طبقة صوت عالية وسريعة للشخصيات الكرتونية والأنمي",
        iconEmoji = "🧒",
        basePitchMultiplier = 1.62f,
        baseSpeedMultiplier = 1.15f
    ),
    TO_CYBER_ROBOT(
        id = "TO_CYBER_ROBOT",
        titleArabic = "تحويل لصوت روبوت آلي 🤖",
        subtitleArabic = "نبرة سايبر إلكترونية مستقبلية لأفلام الخيال العلمي",
        iconEmoji = "🤖",
        basePitchMultiplier = 0.85f,
        baseSpeedMultiplier = 0.95f
    )
}

data class InstantDubbingConfig(
    val mode: InstantDubbingMode = InstantDubbingMode.AUTO_DETECT,
    val customPitchShift: Float = 1.0f,
    val customSpeed: Float = 1.0f,
    val isLiveMonitoring: Boolean = false
)

class InstantDubbingEngine(private val context: Context) {

    private val _config = MutableStateFlow(InstantDubbingConfig())
    val config: StateFlow<InstantDubbingConfig> = _config.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun updateMode(mode: InstantDubbingMode) {
        _config.value = _config.value.copy(
            mode = mode,
            customPitchShift = if (mode == InstantDubbingMode.AUTO_DETECT) 1.0f else mode.basePitchMultiplier,
            customSpeed = if (mode == InstantDubbingMode.AUTO_DETECT) 1.0f else mode.baseSpeedMultiplier
        )
    }

    fun updatePitch(pitch: Float) {
        _config.value = _config.value.copy(customPitchShift = pitch.coerceIn(0.5f, 2.0f))
    }

    fun updateSpeed(speed: Float) {
        _config.value = _config.value.copy(customSpeed = speed.coerceIn(0.5f, 2.0f))
    }

    fun toggleLiveMonitoring() {
        _config.value = _config.value.copy(isLiveMonitoring = !_config.value.isLiveMonitoring)
    }

    /**
     * Calculates the effective pitch multiplier based on the chosen mode and detected gender.
     */
    fun calculateEffectivePitch(detectedGender: DetectedGender): Float {
        val currentConfig = _config.value
        return when (currentConfig.mode) {
            InstantDubbingMode.AUTO_DETECT -> {
                when (detectedGender) {
                    DetectedGender.MALE -> 1.45f * currentConfig.customPitchShift // Male -> Female
                    DetectedGender.FEMALE -> 0.72f * currentConfig.customPitchShift // Female -> Male
                    DetectedGender.CHILD -> 0.70f * currentConfig.customPitchShift // Child -> Deep Hero
                    else -> currentConfig.customPitchShift
                }
            }
            InstantDubbingMode.MALE_TO_FEMALE -> 1.45f * currentConfig.customPitchShift
            InstantDubbingMode.FEMALE_TO_MALE -> 0.72f * currentConfig.customPitchShift
            InstantDubbingMode.TO_CARTOON -> 1.62f * currentConfig.customPitchShift
            InstantDubbingMode.TO_CYBER_ROBOT -> 0.85f * currentConfig.customPitchShift
        }.coerceIn(0.5f, 2.0f)
    }

    /**
     * Calculates the effective speed multiplier based on the chosen mode and detected gender.
     */
    fun calculateEffectiveSpeed(detectedGender: DetectedGender): Float {
        val currentConfig = _config.value
        return when (currentConfig.mode) {
            InstantDubbingMode.AUTO_DETECT -> {
                when (detectedGender) {
                    DetectedGender.MALE -> 1.04f * currentConfig.customSpeed
                    DetectedGender.FEMALE -> 0.95f * currentConfig.customSpeed
                    DetectedGender.CHILD -> 1.10f * currentConfig.customSpeed
                    else -> currentConfig.customSpeed
                }
            }
            InstantDubbingMode.MALE_TO_FEMALE -> 1.05f * currentConfig.customSpeed
            InstantDubbingMode.FEMALE_TO_MALE -> 0.94f * currentConfig.customSpeed
            InstantDubbingMode.TO_CARTOON -> 1.15f * currentConfig.customSpeed
            InstantDubbingMode.TO_CYBER_ROBOT -> 0.95f * currentConfig.customSpeed
        }.coerceIn(0.5f, 2.0f)
    }

    /**
     * Plays the audio file transformed instantly with calculated pitch & speed.
     */
    fun playTransformedAudio(
        filePath: String,
        detectedGender: DetectedGender = DetectedGender.SILENCE,
        volume: Float = 1.0f,
        onComplete: () -> Unit = {}
    ) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) return

        val pitch = calculateEffectivePitch(detectedGender)
        val speed = calculateEffectiveSpeed(detectedGender)

        try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setVolume(volume, volume)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = playbackParams
                    params.pitch = pitch
                    params.speed = speed
                    playbackParams = params
                }
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    onComplete()
                }
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun release() {
        stopPlayback()
    }
}
