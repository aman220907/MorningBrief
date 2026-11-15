package com.morningbrief.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Alarms are forgotten when the phone restarts (or the app updates), so set them again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.scheduleAlarm(context)
    }
}
