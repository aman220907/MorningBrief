package com.morningbrief.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import javax.mail.AuthenticationFailedException

/** Fetches your important emails and posts the "morning message" notification. */
class BriefService : Service() {

    companion object {
        const val ACTION_BRIEF = "com.morningbrief.app.BRIEF"
        const val FG_ID = 2
        const val NOTIF_ID = 3
        const val LOOKBACK_DAYS = 2
        const val MAX_LISTED = 10
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Notifs.ensureChannels(this)
        val working = Notification.Builder(this, Notifs.CH_WORK)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Checking your important emails...")
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(FG_ID, working, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(FG_ID, working)
        }

        val ctx = applicationContext
        Thread {
            val (title, body) = buildBrief(ctx)
            Prefs.saveLastBrief(ctx, title + "\n" + body)
            showBrief(ctx, title, body)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }.start()

        return START_NOT_STICKY
    }

    private fun buildBrief(ctx: Context): Pair<String, String> {
        val email = Prefs.email(ctx)
        val pw = Prefs.password(ctx)
        if (email.isBlank() || pw.isBlank()) {
            return "Setup needed" to
                "Open Morning Brief and enter your Gmail address and app password."
        }

        var lastError: Exception? = null
        for (attempt in 1..3) {
            try {
                val r = EmailFetcher.fetchImportantUnread(email, pw, LOOKBACK_DAYS, MAX_LISTED)
                if (r.total == 0) {
                    return "Good morning! No unread important emails" to
                        "You're all caught up."
                }
                val title = "Good morning! ${r.total} important email" +
                    (if (r.total == 1) "" else "s")
                val lines = r.items.joinToString("\n") { "- ${it.from}: ${it.subject}" }
                val more = if (r.total > r.items.size) "\n...and ${r.total - r.items.size} more" else ""
                return title to (lines + more)
            } catch (e: Exception) {
                lastError = e
                if (e is EmailFetcher.NoImportantFolderException ||
                    e is AuthenticationFailedException
                ) break // retrying won't help
                try {
                    Thread.sleep(5000)
                } catch (ie: InterruptedException) {
                    break
                }
            }
        }

        val e = lastError
        val msg: String = if (e == null) {
            "Unknown problem."
        } else if (e is AuthenticationFailedException) {
            "Gmail rejected the login. Check your address and the 16-letter app password."
        } else {
            e.message ?: e.toString()
        }
        return "Couldn't check your emails" to msg
    }

    private fun showBrief(ctx: Context, title: String, body: String) {
        val launch = ctx.packageManager.getLaunchIntentForPackage("com.google.android.gm")
            ?: Intent(ctx, MainActivity::class.java)
        val pi = PendingIntent.getActivity(ctx, 20, launch, Notifs.PI_FLAGS)
        val n = Notification.Builder(ctx, Notifs.CH_BRIEF)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body.lineSequence().firstOrNull() ?: "")
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, n)
    }
}
