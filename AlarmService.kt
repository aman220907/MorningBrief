package com.morningbrief.app

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Rings like a normal alarm until you tap Dismiss.
 * Dismissing schedules the email brief for 10 minutes later.
 */
class AlarmService : Service() {

    companion object {
        const val ACTION_RING = "com.morningbrief.app.RING"
        const val ACTION_DISMISS = "com.morningbrief.app.DISMISS"
        const val ACTION_CLOSE_UI = "com.morningbrief.app.CLOSE_UI"
        const val EXTRA_TEST = "test"
        const val NOTIF_ID = 1
        const val MAX_RING_MS = 10 * 60 * 1000L // stop ringing by itself after 10 minutes
        const val TEST_BRIEF_DELAY_MS = 15 * 1000L

        @Volatile
        var testMode = false
    }

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())

    private val timeout = Runnable {
        stopRinging()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISMISS) {
            dismiss()
        } else {
            testMode = intent?.getBooleanExtra(EXTRA_TEST, false) ?: false
            if (!testMode) {
                AlarmScheduler.scheduleAlarm(this) // line up tomorrow's alarm right away
            }
            startRinging()
        }
        return START_NOT_STICKY
    }

    private fun startRinging() {
        Notifs.ensureChannels(this)
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, n)
        }
        if (player != null) return // already ringing

        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setDataSource(this, uri)
            mp.isLooping = true
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 800), 0))
            vibrator = v
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handler.postDelayed(timeout, MAX_RING_MS)
    }

    private fun dismiss() {
        stopRinging()
        sendBroadcast(Intent(ACTION_CLOSE_UI).setPackage(packageName))
        val delay = if (testMode) TEST_BRIEF_DELAY_MS else AlarmScheduler.BRIEF_DELAY_MS
        AlarmScheduler.scheduleBrief(this, delay)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopRinging() {
        handler.removeCallbacks(timeout)
        try {
            player?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun buildNotification(): Notification {
        val ringIntent = Intent(this, RingActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val ringPi = PendingIntent.getActivity(this, 10, ringIntent, Notifs.PI_FLAGS)
        val dismissPi = PendingIntent.getService(
            this, 11,
            Intent(this, AlarmService::class.java).setAction(ACTION_DISMISS),
            Notifs.PI_FLAGS
        )
        val dismissAction = Notification.Action.Builder(
            Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
            "Dismiss",
            dismissPi
        ).build()

        return Notification.Builder(this, Notifs.CH_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Wake up!")
            .setContentText("Dismiss to get your email brief in 10 minutes")
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(ringPi)
            .setFullScreenIntent(ringPi, true)
            .addAction(dismissAction)
            .build()
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }
}
