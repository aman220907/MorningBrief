package com.morningbrief.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    const val BRIEF_DELAY_MS = 10 * 60 * 1000L // 10 minutes after you wake up

    /** Next time (in millis) that hour:minute happens, today or tomorrow. */
    fun nextTrigger(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis() + 1000) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun showPendingIntent(ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java), Notifs.PI_FLAGS
        )

    /** Schedules the daily wake-up alarm (does nothing if the alarm is switched off). */
    fun scheduleAlarm(ctx: Context) {
        if (!Prefs.enabled(ctx)) return
        val am = ctx.getSystemService(AlarmManager::class.java)
        val trigger = nextTrigger(Prefs.hour(ctx), Prefs.minute(ctx))
        val intent = Intent(ctx, AlarmService::class.java).setAction(AlarmService.ACTION_RING)
        val op = PendingIntent.getForegroundService(ctx, 1, intent, Notifs.PI_FLAGS)
        // setAlarmClock = the same kind of alarm the stock Clock app uses.
        am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, showPendingIntent(ctx)), op)
    }

    fun cancelAlarm(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val intent = Intent(ctx, AlarmService::class.java).setAction(AlarmService.ACTION_RING)
        val op = PendingIntent.getForegroundService(ctx, 1, intent, Notifs.PI_FLAGS)
        am.cancel(op)
    }

    /** Schedules the email brief to run [delayMs] from now. */
    fun scheduleBrief(ctx: Context, delayMs: Long) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val intent = Intent(ctx, BriefService::class.java).setAction(BriefService.ACTION_BRIEF)
        val op = PendingIntent.getForegroundService(ctx, 2, intent, Notifs.PI_FLAGS)
        val trigger = System.currentTimeMillis() + delayMs
        am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, showPendingIntent(ctx)), op)
    }
}
