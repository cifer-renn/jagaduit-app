package com.example.jagaduit.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val KEY_LOGIN_TIME = "last_login_time"
    private val KEY_IS_LOGGED_IN = "is_logged_in"
    private val KEY_USER_NAME = "user_full_name"

    // 5 Menit dalam Milidetik (5 * 60 * 1000)
    private val SESSION_DURATION = 5 * 60 * 1000L

    fun createLoginSession(name: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun isSessionValid(): Boolean {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val lastLoginTime = prefs.getLong(KEY_LOGIN_TIME, 0)
        val currentTime = System.currentTimeMillis()

        if (isLoggedIn) {
            val timeElapsed = currentTime - lastLoginTime
            if (timeElapsed < SESSION_DURATION) {
                // Masih dalam durasi 10 menit -> Update waktu agar diperpanjang (Opsional)
                prefs.edit().putLong(KEY_LOGIN_TIME, currentTime).apply()
                return true
            } else {
                logout()
                return false
            }
        }
        return false
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}