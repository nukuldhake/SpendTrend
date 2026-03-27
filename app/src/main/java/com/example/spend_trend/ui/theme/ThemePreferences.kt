package com.example.spend_trend.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { LIGHT, DARK, SYSTEM }

object ThemePreferences {
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "spend_trend_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_AUTO_TRACKING = "auto_tracking"

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    var userName by mutableStateOf("Nukul")
        private set

    var autoTrackingEnabled by mutableStateOf(true)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        themeMode = ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        userName = prefs.getString(KEY_USER_NAME, "Nukul") ?: "Nukul"
        autoTrackingEnabled = prefs.getBoolean(KEY_AUTO_TRACKING, true)
    }

    fun updateTheme(newMode: ThemeMode) {
        themeMode = newMode
        prefs.edit().putString(KEY_THEME, newMode.name).apply()
    }

    fun updateUserName(newName: String) {
        userName = newName
        prefs.edit().putString(KEY_USER_NAME, newName).apply()
    }

    fun updateAutoTracking(enabled: Boolean) {
        autoTrackingEnabled = enabled
        prefs.edit().putBoolean(KEY_AUTO_TRACKING, enabled).apply()
    }

    @Composable
    fun isDarkTheme(): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}