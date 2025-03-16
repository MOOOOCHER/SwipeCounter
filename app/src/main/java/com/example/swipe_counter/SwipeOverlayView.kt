package com.example.swipe_counter

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class SwipeOverlayView(context: Context, private val swipeListener: () -> Unit) : View(context) {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null && e2 != null) {
                val deltaX = e2.x - e1.x
                val deltaY = e2.y - e1.y

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > 100 && Math.abs(velocityX) > 100) {
                        // Left or Right swipe detected
                        swipeListener()
                        Toast.makeText(context, "Swipe Detected!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (Math.abs(deltaY) > 100 && Math.abs(velocityY) > 100) {
                        // Up or Down swipe detected
                        swipeListener()
                        Toast.makeText(context, "Swipe Detected!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}
