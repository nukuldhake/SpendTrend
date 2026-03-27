package com.example.spend_trend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.spend_trend.ui.navigation.AppScaffold
import com.example.spend_trend.ui.theme.ThemePreferences
import com.example.spend_trend.ui.theme.SpendTrendTheme
import com.example.spend_trend.data.UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        UserPreferences.init(this)
        UserPreferences.setLoggedIn(false) // Force logout on app startup
        setContent {
            SpendTrendTheme {  // ← no parameters anymore!
                AppScaffold()
            }
        }
    }
}