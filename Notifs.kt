package com.morningbrief.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context

object Notifs {
    const val CH_ALARM = "alarm"
    const val CH_BRIEF = "brief"
    const val CH_WORK = "work"

    val PI_FLAGS: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)

        val alarm = NotificationChannel(CH_ALARM, "Alarm", NotificationManager.IMPORTANCE_HIGH)
        alarm.setSound(null, null) // the sound is played by AlarmService itself
        alarm.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val brief = NotificationChannel(
            CH_BRIEF, "Morning email brief", NotificationManager.IMPORTANCE_HIGH
        )
        val work = NotificationChannel(
            CH_WORK, "Background work", NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannels(listOf(alarm, brief, work))
    }
}
