package com.example.spend_trend.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ════════════════════════════════════════════════
//  Dark Color Scheme — The Hero
// ════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary             = Primary,
    onPrimary           = OnPrimary,
    primaryContainer    = PrimaryContainer,
    onPrimaryContainer  = OnPrimaryContainer,
    secondary           = Secondary,
    onSecondary         = OnSecondary,
    secondaryContainer  = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary            = Tertiary,
    onTertiary          = OnTertiary,
    tertiaryContainer   = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error               = Error,
    onError             = OnError,
    errorContainer      = ErrorContainer,
    onErrorContainer    = OnErrorContainer,
    background          = DarkBackground,
    onBackground        = DarkOnSurface,
    surface             = DarkSurface,
    onSurface           = DarkOnSurface,
    surfaceVariant      = DarkSurfaceVariant,
    onSurfaceVariant    = DarkOnSurfaceVariant,
    outline             = DarkOutline,
    outlineVariant      = DarkOutlineVariant,
    inverseSurface      = LightSurface,
    inverseOnSurface    = LightOnSurface,
    inversePrimary      = Primary,
)

// ════════════════════════════════════════════════
//  Light Color Scheme
// ════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary             = Color(0xFF059669), // slightly deeper for contrast on white
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFFD1FAE5),
    onPrimaryContainer  = Color(0xFF064E3B),
    secondary           = Secondary,
    onSecondary         = OnSecondary,
    secondaryContainer  = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A5F),
    tertiary            = Tertiary,
    onTertiary          = OnTertiary,
    tertiaryContainer   = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF3B1F7E),
    error               = Color(0xFFDC2626),
    onError             = Color(0xFFFFFFFF),
    errorContainer      = Color(0xFFFEE2E2),
    onErrorContainer    = Color(0xFF991B1B),
    background          = LightBackground,
    onBackground        = LightOnSurface,
    surface             = LightSurface,
    onSurface           = LightOnSurface,
    surfaceVariant      = LightSurfaceVariant,
    onSurfaceVariant    = LightOnSurfaceVariant,
    outline             = LightOutline,
    outlineVariant      = LightOutlineVariant,
    inverseSurface      = InverseSurface,
    inverseOnSurface    = InverseOnSurface,
    inversePrimary      = InversePrimary,
)

@Composable
fun SpendTrendTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = when (ThemePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColors else LightColors

    // Edge-to-edge: tint status bar & nav bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}