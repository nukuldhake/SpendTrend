package com.example.spend_trend.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.Primary

/**
 * GlassCard is DEPRECATED for Neo-Brutalism.
 * This now delegates to BlockCard with an accent shadow
 * for backward compatibility with call-sites.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BlockCard(
        modifier = modifier,
        hasShadow = true,
        shadowColor = Primary,
        content = content
    )
}
