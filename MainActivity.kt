package com.morningbrief.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var picker: TimePicker
    private lateinit var status: TextView

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun add(view: android.view.View, topMargin: Int = 8) {
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(topMargin)
        root.addView(view, lp)
    }

    private fun addText(text: String, size: Float, bold: Boolean = false, top: Int = 8) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = size
        if (bold) tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
        add(tv, top)
    }

    private fun addButton(text: String, onClick: () -> Unit) {
        val b = Button(this)
        b.text = text
        b.setOnClickListener { onClick() }
        add(b)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(24), dp(16), dp(24))

        addText("Morning Brief", 28f, bold = true)
        addText(
            "A wake-up alarm that gives you a summary of your important Gmail " +
                "10 minutes after you dismiss it.",
            14f
        )

        addText("1. Gmail login", 18f, bold = true, top = 24)
        addText(
            "Use a Gmail APP PASSWORD (16 letters), not your normal password. " +
                "The README explains how to make one.",
            13f
        )
        emailEt = EditText(this)
        emailEt.hint = "you@gmail.com"
        emailEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailEt.setText(Prefs.email(this))
        add(emailEt)

        passEt = EditText(this)
        passEt.hint = "App password"
        passEt.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passEt.setText(Prefs.password(this))
        add(passEt)

        addButton("Save login") {
            saveCreds()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        addText("2. Alarm time", 18f, bold = true, top = 24)
        picker = TimePicker(this)
        picker.setIs24HourView(false)
        picker.hour = Prefs.hour(this)
        picker.minute = Prefs.minute(this)
        add(picker)

        addButton("Turn alarm ON (repeats every day)") {
            saveCreds()
            Prefs.saveTime(this, picker.hour, picker.minute)
            Prefs.setEnabled(this, true)
            AlarmScheduler.scheduleAlarm(this)
            refreshStatus()
        }
        addButton("Turn alarm OFF") {
            Prefs.setEnabled(this, false)
            AlarmScheduler.cancelAlarm(this)
            refreshStatus()
        }

        status = TextView(this)
        status.textSize = 14f
        add(status, 16)

        addText("3. Permissions (do these once)", 18f, bold = true, top = 24)
        addButton("Allow full-screen alarm screen") {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:$packageName")
                    )
                )
            } else {
                Toast.makeText(this, "Not needed on your Android version", Toast.LENGTH_SHORT).show()
            }
        }
        addButton("Stop battery saver from killing the alarm") {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }

        addText("4. Test it", 18f, bold = true, top = 24)
        addButton("Test alarm (brief comes 15 seconds after dismiss)") {
            saveCreds()
            startForegroundService(
                Intent(this, AlarmService::class.java)
                    .setAction(AlarmService.ACTION_RING)
                    .putExtra(AlarmService.EXTRA_TEST, true)
            )
            startActivity(Intent(this, RingActivity::class.java))
        }
        addButton("Test email brief right now") {
            saveCreds()
            startForegroundService(
                Intent(this, BriefService::class.java).setAction(BriefService.ACTION_BRIEF)
            )
            Toast.makeText(this, "Checking... notification coming", Toast.LENGTH_SHORT).show()
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun saveCreds() {
        Prefs.saveCreds(
            this,
            emailEt.text.toString().trim(),
            passEt.text.toString().replace(" ", "") // Google shows app passwords with spaces
        )
    }

    private fun refreshStatus() {
        val sb = StringBuilder()
        if (Prefs.enabled(this)) {
            val t = AlarmScheduler.nextTrigger(Prefs.hour(this), Prefs.minute(this))
            val fmt = SimpleDateFormat("EEE d MMM, h:mm a", Locale.getDefault())
            sb.append("Alarm is ON. Next ring: ").append(fmt.format(Date(t)))
        } else {
            sb.append("Alarm is OFF.")
        }
        val last = Prefs.lastBrief(this)
        if (last.isNotEmpty()) {
            sb.append("\n\nLast email brief:\n").append(last)
        }
        status.text = sb.toString()
    }
}
