package com.morningbrief.app

import android.content.Context

/** Simple settings storage (private to this app). */
object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("brief_prefs", Context.MODE_PRIVATE)

    fun email(c: Context): String = sp(c).getString("email", "") ?: ""
    fun password(c: Context): String = sp(c).getString("pw", "") ?: ""
    fun saveCreds(c: Context, email: String, password: String) {
        sp(c).edit().putString("email", email).putString("pw", password).apply()
    }

    fun hour(c: Context): Int = sp(c).getInt("hour", 7)
    fun minute(c: Context): Int = sp(c).getInt("minute", 0)
    fun saveTime(c: Context, hour: Int, minute: Int) {
        sp(c).edit().putInt("hour", hour).putInt("minute", minute).apply()
    }

    fun enabled(c: Context): Boolean = sp(c).getBoolean("enabled", false)
    fun setEnabled(c: Context, value: Boolean) {
        sp(c).edit().putBoolean("enabled", value).apply()
    }

    fun lastBrief(c: Context): String = sp(c).getString("last", "") ?: ""
    fun saveLastBrief(c: Context, text: String) {
        sp(c).edit().putString("last", text).apply()
    }
}
