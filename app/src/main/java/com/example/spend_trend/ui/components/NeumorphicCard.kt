package com.example.spend_trend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.spend_trend.ui.theme.Dimens

/**
 * NeumorphicCard is DEPRECATED for Neo-Brutalism.
 * Now renders as a flat, thick-bordered Box with no soft shadows.
 * Parameters kept for API compat but isConcave/elevation are ignored.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") cornerRadius: Dp = Dimens.RadiusMd,
    @Suppress("UNUSED_PARAMETER") elevation: Dp = Dimens.ElevationXl,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    @Suppress("UNUSED_PARAMETER") isConcave: Boolean = false,
    @Suppress("UNUSED_PARAMETER") isPressed: Boolean = false,
    contentPadding: Dp = Dimens.CardPadding,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(BorderStroke(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline))
            .padding(contentPadding),
        content = content
    )
}
