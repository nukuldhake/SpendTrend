package com.example.spend_trend.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { LIGHT, DARK, SYSTEM }

object ThemePreferences {
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "spend_trend_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_AUTO_TRACKING = "auto_tracking"
    private const val KEY_LOW_THRESHOLD = "low_threshold"
    private const val KEY_HIGH_THRESHOLD = "high_threshold"

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    var userName by mutableStateOf("Friend")
        private set

    var autoTrackingEnabled by mutableStateOf(false)
        private set

    var lowThreshold by mutableIntStateOf(80)
        private set

    var highThreshold by mutableIntStateOf(100)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        themeMode = ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        userName = prefs.getString(KEY_USER_NAME, "Friend") ?: "Friend"
        autoTrackingEnabled = prefs.getBoolean(KEY_AUTO_TRACKING, false)
        lowThreshold = prefs.getInt(KEY_LOW_THRESHOLD, 80)
        highThreshold = prefs.getInt(KEY_HIGH_THRESHOLD, 100)
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

    fun updateThresholds(low: Int, high: Int) {
        lowThreshold = low
        highThreshold = high
        prefs.edit()
            .putInt(KEY_LOW_THRESHOLD, low)
            .putInt(KEY_HIGH_THRESHOLD, high)
            .apply()
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