package com.example.spend_trend.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.GlassColors
import com.example.spend_trend.ui.theme.ThemeMode
import com.example.spend_trend.ui.theme.ThemePreferences

/**
 * Glassmorphism card composable.
 * Semi-transparent background + thin glass border + optional blur.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimens.RadiusMd,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = when (ThemePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    
    val glassSurface = if (isDark) GlassColors.DarkGlassSurface else GlassColors.LightGlassSurface
    val glassBorder = if (isDark) GlassColors.DarkGlassBorder else GlassColors.LightGlassBorder

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = glassSurface,
        border = BorderStroke(Dimens.GlassBorderWidth, glassBorder),
        tonalElevation = Dimens.ElevationNone,
        shadowElevation = if (isDark) Dimens.ElevationNone else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            content = content
        )
    }
}

/**
 * Compact glass card variant with smaller padding.
 */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = when (ThemePreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    
    val glassSurface = if (isDark) GlassColors.DarkGlassSurface else GlassColors.LightGlassSurface
    val glassBorder = if (isDark) GlassColors.DarkGlassBorder else GlassColors.LightGlassBorder

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.RadiusFull),
        color = glassSurface,
        border = BorderStroke(Dimens.GlassBorderWidth, glassBorder),
        tonalElevation = Dimens.ElevationNone,
        shadowElevation = if (isDark) Dimens.ElevationNone else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm),
            content = content
        )
    }
}
