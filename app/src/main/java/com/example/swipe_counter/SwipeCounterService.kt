package com.example.swipe_counter

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.edit

private const val DEBUG_TAG = "Gesture"
class SwipeCounterService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var gestureDetector: GestureDetector
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("SwipeData", MODE_PRIVATE)
        startForegroundService()
        addOverlayView()
    }

    private fun startForegroundService() {
        val channelId = "swipe_counter_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Swipe Counter", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Swipe Counter Running")
            .setContentText("Tracking swipes in the background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun addOverlayView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(0x00000000) // Transparent
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.OnGestureListener {
            override fun onDown(event: MotionEvent): Boolean {
                Log.d(DEBUG_TAG, "onDown: $event")
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                event1: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d(DEBUG_TAG, "onFling: $event1")
                val count = sharedPreferences.getInt("swipeCount", 0) + 1
                sharedPreferences.edit() { putInt("swipeCount", count) }
                incrementSwipeCount()
                return true
            }

            override fun onLongPress(event: MotionEvent) {
                Log.d(DEBUG_TAG, "onLongPress: $event")
            }


            override fun onShowPress(event: MotionEvent) {
                Log.d(DEBUG_TAG, "onShowPress: $event")
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                Log.d(DEBUG_TAG, "onSingleTapUp: $event")
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Log.d(DEBUG_TAG, "onScroll: $e1 $e2")
                return true
            }
        })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
    }

    private fun incrementSwipeCount() {
        val count = sharedPreferences.getInt("swipeCount", 0) + 1
        sharedPreferences.edit { putInt("swipeCount", count) }

        val notification = NotificationCompat.Builder(this, "swipe_counter_channel")
            .setContentTitle("Swipe Counter Running")
            .setContentText("Total Swipes: $count")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
