package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.GradientPalette
import com.example.spend_trend.ui.theme.MonoBlack

/**
 * GradientCard — Neo-Brutal edition.
 * No rounded corners, no drop shadow. Just a flat gradient fill
 * with a thick border and sharp edges.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    brush: Brush = GradientPalette.EmeraldTeal,
    borderColor: Color = MonoBlack,
    borderWidth: Dp = Dimens.BorderWidthStandard,
    @Suppress("UNUSED_PARAMETER") cornerRadius: Dp = Dimens.RadiusLg,  // Ignored, 0dp always
    @Suppress("UNUSED_PARAMETER") shadowElevation: Dp = Dimens.ElevationLg,  // Ignored
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush)
            .border(borderWidth, borderColor)
            .padding(Dimens.CardPaddingLg),
        content = content
    )
}
