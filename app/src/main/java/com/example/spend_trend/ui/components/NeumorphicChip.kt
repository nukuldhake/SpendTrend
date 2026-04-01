package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.MonoGrayLight
import com.example.spend_trend.ui.theme.Primary
import com.example.spend_trend.ui.theme.OnPrimary

/**
 * NeumorphicChip is DEPRECATED for Neo-Brutalism.
 * Now renders as a flat, bordered chip with instant color swap on selection.
 * Delegates to the same visual logic as BlockChip.
 */
@Composable
fun NeumorphicChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = Primary,
    onSelectedColor: Color = OnPrimary
) {
    Box(
        modifier = modifier
            .border(Dimens.BorderWidthStandard, if (isSelected) selectedColor else MonoGrayLight)
            .background(if (isSelected) selectedColor else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) onSelectedColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
