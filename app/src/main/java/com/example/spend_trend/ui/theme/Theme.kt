package com.example.spend_trend.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ════════════════════════════════════════════════
//  Shapes — Stark 0dp corners for Neo-Brutal
// ════════════════════════════════════════════════
val SpendTrendShapes = Shapes(
    extraSmall = AbsoluteCutCornerShape(0.dp),
    small      = AbsoluteCutCornerShape(0.dp),
    medium     = AbsoluteCutCornerShape(0.dp),
    large      = AbsoluteCutCornerShape(0.dp),
    extraLarge = AbsoluteCutCornerShape(0.dp)
)

// ════════════════════════════════════════════════
//  Dark Color Scheme — Deep Slate
// ════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary              = Primary,              // Blue-500
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryDark,           // Blue-700
    onPrimaryContainer   = PrimaryLight,          // Blue-100
    secondary            = AccentEmerald,         // Emerald-600
    onSecondary          = OnSecondary,
    secondaryContainer   = AccentEmeraldDark,     // Emerald-700
    onSecondaryContainer = AccentEmeraldLight,    // Emerald-50
    tertiary             = WarningAmber,
    onTertiary           = Color(0xFF0F172A),
    background           = DarkBackground,        // Slate-950
    onBackground         = DarkOnSurface,         // Slate-100
    surface              = DarkSurface,           // Slate-900
    onSurface            = DarkOnSurface,         // Slate-100
    surfaceVariant       = DarkSurfaceVariant,    // Slate-800
    onSurfaceVariant     = DarkOnSurfaceVariant,  // Slate-400
    outline              = DarkOutline,           // Slate-600
    outlineVariant       = Color(0xFF334155),     // Slate-700
    inverseSurface       = Color(0xFFF1F5F9),
    inverseOnSurface     = Color(0xFF0F172A),
    error                = ExpenseRose,
    onError              = Color(0xFFFFFFFF),
    errorContainer       = ExpenseRoseLight,
    onErrorContainer     = Color(0xFF881337),
)

// ════════════════════════════════════════════════
//  Light Color Scheme — Paper White + Slate Borders
// ════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary              = Primary,              // Blue-500
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryLight,         // Blue-100
    onPrimaryContainer   = PrimaryDark,          // Blue-700
    secondary            = AccentEmerald,        // Emerald-600
    onSecondary          = OnSecondary,
    secondaryContainer   = AccentEmeraldLight,   // Emerald-50
    onSecondaryContainer = AccentEmeraldDark,    // Emerald-700
    tertiary             = WarningAmber,
    onTertiary           = Color(0xFF0F172A),
    background           = LightBackground,      // Slate-50
    onBackground         = LightOnSurface,       // Slate-900
    surface              = LightSurface,         // Pure white
    onSurface            = LightOnSurface,       // Slate-900
    surfaceVariant       = LightSurfaceVariant,  // Slate-100
    onSurfaceVariant     = LightOnSurfaceVariant,// Slate-600
    outline              = LightOutline,         // Slate-900
    outlineVariant       = Color(0xFFCBD5E1),    // Slate-300
    inverseSurface       = Color(0xFF0F172A),
    inverseOnSurface     = Color(0xFFF1F5F9),
    error                = ExpenseRose,
    onError              = Color(0xFFFFFFFF),
    errorContainer       = ExpenseRoseLight,
    onErrorContainer     = Color(0xFF881337),
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
        shapes = SpendTrendShapes,
        content = content
    )
}