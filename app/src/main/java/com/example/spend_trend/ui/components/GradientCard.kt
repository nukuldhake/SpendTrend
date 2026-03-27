package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.GradientPalette

/**
 * Gradient hero card for premium accent sections.
 * Used for Balance card, Forecast projection, etc.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    brush: Brush = GradientPalette.EmeraldTeal,
    cornerRadius: Dp = Dimens.RadiusLg,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.CardPaddingLg),
            content = content
        )
    }
}
