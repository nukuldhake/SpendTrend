package com.example.spend_trend.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.theme.Primary

/**
 * A shared Neumorphic keypad button used for PIN entry and numeric inputs.
 */
@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    NeumorphicCard(
        modifier = Modifier
            .size(72.dp)
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        cornerRadius = 36.dp, // Circular for layout buttons
        elevation = if (isPressed) 0.dp else 6.dp,
        isConcave = isPressed,
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (text) {
                "⌫" -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = if (isPressed) Primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                "OK" -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = if (isPressed) Primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                else -> Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) Primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
