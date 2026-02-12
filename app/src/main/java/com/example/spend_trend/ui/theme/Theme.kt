package com.example.spend_trend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ────────────────────────────────────────────────
// SpendTrend Theme – Light + Dark + System support
// Dynamic colors optional (default: false for brand consistency)
// ────────────────────────────────────────────────
@Composable
fun SpendTrendTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = when (ThemePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val dynamicColor = false // ← set to true only if you want wallpaper-based theme (not recommended for finance)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicScheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            // Hybrid: keep SpendTrend brand colors for primary/actions, let dynamic influence neutrals
            dynamicScheme.copy(
                primary = Primary,
                onPrimary = OnPrimary,
                primaryContainer = PrimaryContainer,
                onPrimaryContainer = OnPrimaryContainer,
                secondary = Secondary,
                onSecondary = OnSecondary,
                tertiary = Color(0xFF8B5CF6), // optional accent (purple for charts/graphs)
                error = Error,
                onError = OnError,
                background = if (darkTheme) Color(0xFF0F172A) else Background,
                surface = if (darkTheme) Color(0xFF1E293B) else Surface,
                surfaceVariant = if (darkTheme) Color(0xFF334155) else SurfaceVariant,
                onSurface = if (darkTheme) Color(0xFFF1F5F9) else OnSurface,
                onSurfaceVariant = if (darkTheme) Color(0xFFCBD5E1) else OnSurfaceVariant,
                outline = if (darkTheme) Color(0xFF475569) else Outline
            )
        }

        darkTheme -> darkColorScheme(
            primary = Primary,
            onPrimary = OnPrimary,
            primaryContainer = PrimaryContainer,
            onPrimaryContainer = OnPrimaryContainer,
            secondary = Secondary,
            onSecondary = OnSecondary,
            tertiary = Color(0xFF8B5CF6),
            background = Color(0xFF0F172A),        // dark slate background
            surface = Color(0xFF1E293B),
            surfaceVariant = Color(0xFF334155),
            onSurface = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFFCBD5E1),
            error = Error,
            onError = OnError,
            outline = Color(0xFF475569)
        )

        else -> lightColorScheme(
            primary = Primary,
            onPrimary = OnPrimary,
            primaryContainer = PrimaryContainer,
            onPrimaryContainer = OnPrimaryContainer,
            secondary = Secondary,
            onSecondary = OnSecondary,
            tertiary = Color(0xFF8B5CF6),
            background = Background,
            surface = Surface,
            surfaceVariant = SurfaceVariant,
            onSurface = OnSurface,
            onSurfaceVariant = OnSurfaceVariant,
            error = Error,
            onError = OnError,
            outline = Outline
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}