package com.example.swipe_counter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResetReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPreferences = context?.getSharedPreferences("SwipeData",Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putInt("swipeCount",0)?.apply()
    }

}