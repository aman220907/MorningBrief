package com.morningbrief.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView

/** Full-screen "wake up" screen shown over the lock screen. */
class RingActivity : Activity() {

    private val closer = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.BLACK)
        val pad = (24 * resources.displayMetrics.density).toInt()
        root.setPadding(pad, pad, pad, pad)

        val clock = TextClock(this)
        clock.format12Hour = "h:mm a"
        clock.format24Hour = "h:mm a"
        clock.textSize = 56f
        clock.setTextColor(Color.WHITE)
        clock.gravity = Gravity.CENTER
        root.addView(clock)

        val msg = TextView(this)
        msg.text = "Good morning! Tap the button when you're awake."
        msg.textSize = 18f
        msg.setTextColor(Color.LTGRAY)
        msg.gravity = Gravity.CENTER
        msg.setPadding(0, pad, 0, pad * 2)
        root.addView(msg)

        val btn = Button(this)
        btn.text = "I'm awake - Dismiss"
        btn.textSize = 20f
        btn.setOnClickListener {
            startService(
                Intent(this, AlarmService::class.java).setAction(AlarmService.ACTION_DISMISS)
            )
            finish()
        }
        root.addView(
            btn,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (80 * resources.displayMetrics.density).toInt()
            )
        )

        setContentView(root)

        val filter = IntentFilter(AlarmService.ACTION_CLOSE_UI)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(closer, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(closer, filter)
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(closer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
