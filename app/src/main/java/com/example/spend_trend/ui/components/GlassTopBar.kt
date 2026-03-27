package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*

/**
 * Premium glass-styled back row with circular arrow button + screen title.
 * Consistent across all secondary screens.
 */
@Composable
fun GlassTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val btnBg = if (isDark) GlassColors.DarkGlassSurface else GlassColors.LightGlassSurface
    val btnBorder = if (isDark) GlassColors.DarkGlassBorder else GlassColors.LightGlassBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glass circle back button
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            shape = CircleShape,
            color = btnBg,
            border = androidx.compose.foundation.BorderStroke(Dimens.GlassBorderWidth, btnBorder),
            tonalElevation = Dimens.ElevationNone,
            shadowElevation = Dimens.ElevationNone
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = Primary,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
        }

        Spacer(Modifier.width(Dimens.SpacingMd))

        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
