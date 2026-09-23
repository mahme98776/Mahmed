package com.example.alad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ALAD Mobile - Floating Control Overlay Service
 * Displays a draggable floating bubble / control widget above active applications (YouTube, Netflix, Spotify)
 * allowing one-touch live dubbing control, language switching, and ducking adjustment.
 *
 * All Rights Reserved to Mohamed Salima (محمد سليمة) © 2026
 */
class AladFloatingWidgetService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "alad_live_dubbing_channel"
        const val NOTIFICATION_ID = 20261

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, AladFloatingWidgetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AladFloatingWidgetService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        _isServiceRunning.value = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Overlay permission missing
            return
        }

        initFloatingWidget()
    }

    private fun initFloatingWidget() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 350
        }

        // Programmatic floating view layout
        val rootLayout = FrameLayout(this).apply {
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val bubbleCard = FrameLayout(this).apply {
            val bg = android.graphics.drawable.GradientDrawable().apply {
                setColor(0xEE1E1B24.toInt())
                cornerRadius = 60f
                setStroke(3, 0xFF6750A4.toInt())
            }
            background = bg
            setPadding(28, 20, 28, 20)
        }

        val contentLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconView = TextView(this).apply {
            text = "🔴"
            textSize = 18f
        }

        val titleView = TextView(this).apply {
            text = " ALAD دبلجة حية ⚡"
            setTextColor(0xFFE6E1E5.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        contentLayout.addView(iconView)
        contentLayout.addView(titleView)
        bubbleCard.addView(contentLayout)
        rootLayout.addView(bubbleCard)

        floatingView = rootLayout

        // Draggable touch listener
        bubbleCard.setOnTouchListener { _, event ->
            val p = params ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = initialX + (event.rawX - initialTouchX).toInt()
                    p.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, p)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        // Click action: open main app
                        openMainActivity()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openMainActivity() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(launchIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ALAD Mobile Live Audio Dubbing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "دبلجة حية لتطبيقات الفيديو والصوت بالذكاء الاصطناعي"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ALAD Mobile | دبلجة التطبيقات الحية نشطة 🔴")
            .setContentText("يتم الآن الاستماع وترجمة الصوت مباشرة عبر Gemini Live Translate")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
    }
}
