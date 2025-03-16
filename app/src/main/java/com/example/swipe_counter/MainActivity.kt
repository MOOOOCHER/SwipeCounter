package com.example.swipe_counter

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.widget.ProgressBar
import java.util.Calendar
import kotlin.math.min

private const val DEBUG_TAG = "Gesture"
private val levels = intArrayOf(100, 300, 500, 1000, 5000, 10000)
class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var swipeCounter: TextView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),0)
        }
        setContentView(R.layout.activity_main)
        swipeCounter = findViewById(R.id.swipeCount)
        sharedPreferences = getSharedPreferences("SwipeData", MODE_PRIVATE)
        progressBar = findViewById(R.id.progressBar)
        updateSwipeCount()

        startService(Intent(this, SwipeCounterService::class.java))
        gestureDetector = GestureDetector(this, this)
        scheduleDailyReset()
    }

    private fun updateSwipeCount(){
        val count = sharedPreferences.getInt("swipeCount",0)
        swipeCounter.text = count.toString()
        updateUI()
    }
    private fun updateUI(){
        val count = sharedPreferences.getInt("swipeCount", 0)
        swipeCounter.text = count.toString()
        val percentage = when {
            count < levels[0] -> (count * 100/ levels[0])
            count < levels[1] -> ((count-levels[0]) * 100/ (levels[1]-levels[0]))
            count < levels[2] -> ((count-levels[1]) * 100/ (levels[2]-levels[1]))
            count < levels[3] -> ((count-levels[2]) * 100/ (levels[3]-levels[2]))
            count < levels[4] -> ((count-levels[3]) * 100/ (levels[4]-levels[3]))
            else -> min(100,((count-levels[4]) * 100/ (levels[5]-levels[4])))
        }
        progressBar.progress = percentage
    }

    private fun scheduleDailyReset() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

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
        updateSwipeCount()
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
}