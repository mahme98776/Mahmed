package com.example.audio.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Alexa & Gemini Background Wake-Word Service
 * 
 * Runs continuously in the background with a completely silent, low-priority (IMPORTANCE_MIN)
 * notification that never disturbs the user with sound or popup banners.
 * Listens for the wake phrases "أليكسا", "Alexa", or "جيمناي", and upon detection:
 * 1. Provides a subtle haptic vibration.
 * 2. Launches the application and summons the full-screen glowing Alexa & Gemini Assistant interface
 *    instantly, mirroring the native Gemini / Google Assistant experience on Android.
 * 
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AlexaBackgroundWakeWordService : Service() {

    private val tag = "AlexaWakeWordService"
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var speechRecognizer: SpeechRecognizer? = null
    private var restartListeningJob: Job? = null
    private var isDestroyed = false
    private var serviceErrorCount = 0

    companion object {
        const val CHANNEL_ID = "alexa_silent_background_channel"
        const val NOTIFICATION_ID = 20263
        const val EXTRA_TRIGGER_VOICE_IMMEDIATELY = "EXTRA_TRIGGER_VOICE_IMMEDIATELY"

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, AlexaBackgroundWakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlexaBackgroundWakeWordService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceActive.value = true
        createSilentNotificationChannel()
        startForeground(NOTIFICATION_ID, buildSilentNotification())
        initSpeechRecognizer()
        startListening()
        Log.i(tag, "Alexa Background Wake-Word Service successfully started in silent mode.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _isServiceActive.value = true
        if (speechRecognizer == null) {
            initSpeechRecognizer()
            startListening()
        }
        return START_STICKY
    }

    private fun createSilentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "المساعد الصوتي في الخلفية (صامت)",
                NotificationManager.IMPORTANCE_MIN // Minimum priority: No sound, no vibration, no heads-up popup
            ).apply {
                description = "خدمة التعرف التلقائي على نداء 'أليكسا' و 'جيمناي' في الخلفية"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildSilentNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_AUTO_OPEN_ALEXA, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("مساعد أليكسا وجيمناي الذكي")
            .setContentText("في وضع الاستعداد الصامت • قل: 'أليكسا' في أي وقت")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun initSpeechRecognizer() {
        if (isDestroyed) return
        try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            serviceErrorCount++
                            val delayMs = when {
                                serviceErrorCount > 10 -> 8000L
                                serviceErrorCount > 5 -> 4000L
                                serviceErrorCount > 2 -> 2000L
                                else -> 1000L
                            }
                            scheduleRestartListening(delayMs)
                        }

                        override fun onResults(results: Bundle?) {
                            serviceErrorCount = 0
                            handleSpeechResults(results)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            serviceErrorCount = 0
                            handleSpeechResults(partialResults)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "SpeechRecognizer initialization error: ${e.message}")
        }
    }

    private fun startListening() {
        if (isDestroyed) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start listening: ${e.message}")
            scheduleRestartListening(1000)
        }
    }

    private fun handleSpeechResults(bundle: Bundle?) {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        val recognizedText = matches.joinToString(" ").lowercase().trim()

        val wakeWords = listOf(
            "اليكسا", "أليكسا", "alexa", "يا اليكسا", "يا أليكسا",
            "جيمناي", "جيميناي", "gemini", "يا جيمناي", "يا جيميناي", "مساعدي"
        )

        val triggeredWakeWord = wakeWords.firstOrNull { recognizedText.contains(it) }

        if (triggeredWakeWord != null) {
            Log.i(tag, "Wake-Word detected: '$triggeredWakeWord' in text: '$recognizedText'")
            triggerAssistantWakeUp()
        } else {
            scheduleRestartListening(250)
        }
    }

    /**
     * Wakes up the system and launches the assistant modal directly on screen
     */
    private fun triggerAssistantWakeUp() {
        // Subtle Haptic feedback
        performHapticVibration()

        // Set global trigger flag
        MainActivity.globalAlexaTriggerFlow.value = true

        // Launch MainActivity and bring to front with the Alexa assistant visible
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_AUTO_OPEN_ALEXA, true)
            putExtra(EXTRA_TRIGGER_VOICE_IMMEDIATELY, true)
        }
        startActivity(launchIntent)

        // Pause background listener briefly so it does not conflict with active modal listening
        scheduleRestartListening(3000)
    }

    private fun performHapticVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    private fun scheduleRestartListening(delayMs: Long) {
        if (isDestroyed) return
        restartListeningJob?.cancel()
        restartListeningJob = serviceScope.launch {
            delay(delayMs)
            if (!isDestroyed) {
                try {
                    speechRecognizer?.cancel()
                    startListening()
                } catch (e: Exception) {
                    initSpeechRecognizer()
                    startListening()
                }
            }
        }
    }

    override fun onDestroy() {
        isDestroyed = true
        _isServiceActive.value = false
        restartListeningJob?.cancel()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        super.onDestroy()
        Log.i(tag, "Alexa Background Wake-Word Service stopped.")
    }
}
