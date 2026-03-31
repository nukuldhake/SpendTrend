package com.example.spend_trend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.Dimens

/**
 * A Neumorphic (Soft UI) themed Chip.
 * Can be 'Popped Out' (convex) or 'Pressed In' (concave).
 */
@Composable
fun NeumorphicChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onSelectedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .neumorphicShadow(
                elevation = if (isSelected) 4.dp else 8.dp, // flattened when selected
                cornerRadius = 16.dp,
                isConcave = isSelected // 'Pressed In' when selected
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedColor else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) onSelectedColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
