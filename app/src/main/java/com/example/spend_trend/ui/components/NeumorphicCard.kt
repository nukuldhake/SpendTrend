package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.Dimens

/**
 * A Neumorphic (Soft UI) themed Card.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimens.RadiusMd,
    elevation: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    isConcave: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .neumorphicShadow(
                elevation = elevation,
                cornerRadius = cornerRadius,
                isConcave = isConcave
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(Dimens.SpacingMd)
    ) {
        content()
    }
}
