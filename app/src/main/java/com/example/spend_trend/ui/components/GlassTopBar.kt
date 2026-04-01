package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.spend_trend.ui.theme.*

/**
 * GlassTopBar is DEPRECATED for Neo-Brutalism.
 * Now renders as a sharp, bordered back row with no blur/glass.
 */
@Composable
fun GlassTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sharp square back button (replaces glass circle)
        Box(
            modifier = Modifier
                .size(Dimens.MinTouchTarget)
                .border(Dimens.BorderWidthStandard, MonoBlack)
                .background(MonoWhite)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = MonoBlack,
                modifier = Modifier.size(Dimens.IconMd)
            )
        }

        Spacer(Modifier.width(Dimens.SpacingMd))

        Text(
            title.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
