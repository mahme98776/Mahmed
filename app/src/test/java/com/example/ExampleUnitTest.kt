package com.example

import com.example.audio.AudioEffectCategory
import com.example.audio.AudioEffectsLibrary
import com.example.audio.VoiceEffect
import com.example.ui.components.VoicePresetType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun audioEffectsLibrary_hasCompletePresets() {
    val effects = AudioEffectsLibrary.effects
    assertTrue("Effects library should contain at least 15 effects", effects.size >= 15)

    // Check Categories
    val categories = effects.map { it.category }.toSet()
    assertTrue(categories.contains(AudioEffectCategory.REVERB_SPACE))
    assertTrue(categories.contains(AudioEffectCategory.PITCH_VOICE))
    assertTrue(categories.contains(AudioEffectCategory.SCI_FI_ROBOT))
    assertTrue(categories.contains(AudioEffectCategory.VINTAGE_FILTERS))

    // Check Pitch Boundaries
    effects.forEach { effect ->
      val params = effect.defaultParams
      assertTrue("Pitch semitones should be within -12..12", params.pitchSemitones in -12..12)
      assertTrue("Pitch multiplier should be > 0", params.pitchMultiplier in 0.4f..2.5f)
      assertTrue("Speed multiplier should be > 0", params.speedMultiplier in 0.4f..2.5f)
      assertTrue("Reverb room size should be in 0..1", params.reverbRoomSize in 0.0f..1.0f)
      assertTrue("Echo delay should be >= 0", params.echoDelayMs >= 0)
      assertTrue("Echo feedback should be in 0..1", params.echoFeedback in 0.0f..1.0f)
      assertTrue("Robot modulation should be >= 0", params.robotModulationHz >= 0f)
      assertTrue("Dry/Wet mix should be in 0..1", params.dryWetMix in 0.0f..1.0f)
    }

    // Check VoiceEffect backward compatibility mapping
    VoiceEffect.values().forEach { voiceEffect ->
      val mapped = AudioEffectsLibrary.fromVoiceEffect(voiceEffect)
      assertNotNull("Mapped effect should not be null", mapped)
    }
  }

  @Test
  fun audioTrimmer_boundaryCalculations() {
    val totalDuration = 12.5f
    val start = 2.0f
    val end = 8.5f
    val trimmedDuration = (end - start).coerceAtLeast(0f)

    assertEquals(6.5f, trimmedDuration, 0.01f)
    assertTrue("Start must be >= 0", start >= 0f)
    assertTrue("End must be <= totalDuration", end <= totalDuration)
    assertTrue("Start must be < end", start < end)

    val startMs = (start * 1000L).toLong()
    val endMs = (end * 1000L).toLong()
    assertEquals(2000L, startMs)
    assertEquals(8500L, endMs)
  }

  @Test
  fun voicePresetType_validations() {
    val presets = VoicePresetType.values()
    assertTrue("Must have Robot preset", presets.any { it == VoicePresetType.ROBOT })
    assertTrue("Must have Echo preset", presets.any { it == VoicePresetType.ECHO })
    assertTrue("Must have Deep Voice preset", presets.any { it == VoicePresetType.DEEP_VOICE })

    presets.forEach { preset ->
      val effect = AudioEffectsLibrary.findById(preset.effectId)
      assertNotNull("Effect item must exist for preset ${preset.name}", effect)
      assertNotNull("Preset must map to valid VoiceEffect enum", preset.voiceEffectEnum)
    }
  }

  @Test
  fun voiceRecordingEntity_instantiationAndProperties() {
    val entity = com.example.data.VoiceRecordingEntity(
      id = 1L,
      title = "تسجيل اختباري",
      filePath = "/storage/emulated/0/recording_1.m4a",
      durationSeconds = 4.2f,
      fileSizeBytes = 65536L,
      voiceEffect = "ROBOT",
      detectedGender = "MALE",
      associatedScript = "مرحباً بكم في الدبلجة",
      timestamp = 1700000000000L
    )
    assertEquals(1L, entity.id)
    assertEquals("تسجيل اختباري", entity.title)
    assertEquals("/storage/emulated/0/recording_1.m4a", entity.filePath)
    assertEquals(4.2f, entity.durationSeconds, 0.01f)
    assertEquals(65536L, entity.fileSizeBytes)
    assertEquals("ROBOT", entity.voiceEffect)
    assertEquals("MALE", entity.detectedGender)
    assertEquals("مرحباً بكم في الدبلجة", entity.associatedScript)
  }
}
