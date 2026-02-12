package com.example.spend_trend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

enum class ThemeMode { LIGHT, DARK, SYSTEM }

object ThemePreferences {
    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)

    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) { _themeMode.value = value }

    @Composable
    fun isDarkTheme(): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}