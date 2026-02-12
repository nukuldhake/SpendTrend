package com.example.spend_trend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.spend_trend.ui.navigation.AppScaffold
import com.example.spend_trend.ui.theme.SpendTrendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendTrendTheme {  // ← no parameters anymore!
                AppScaffold()
            }
        }
    }
}