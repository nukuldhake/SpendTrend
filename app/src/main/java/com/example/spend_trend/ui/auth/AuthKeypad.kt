package com.example.spend_trend.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import com.example.spend_trend.ui.theme.Dimens
import com.example.spend_trend.ui.theme.MonoBlack
import com.example.spend_trend.ui.theme.MonoWhite
import com.example.spend_trend.ui.theme.Primary

/**
 * Neo-Brutal keypad button — sharp rectangle, no circles.
 * Action keys (⌫, OK) get accent-tinted backgrounds.
 * Includes haptic feedback on press.
 */
@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isActionKey = text == "⌫" || text == "OK"

    Box(
        modifier = Modifier
            .size(Dimens.MinTouchTarget + Dimens.SpacingXl)  // 68.dp
            .border(Dimens.BorderWidthStandard, MonoBlack)
            .background(
                when {
                    text == "OK" -> Primary
                    isActionKey -> MonoBlack
                    else -> MonoWhite
                }
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        when (text) {
            "⌫" -> Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = MonoWhite,
                modifier = Modifier.size(Dimens.IconMd)
            )
            "OK" -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirm",
                tint = MonoWhite,
                modifier = Modifier.size(Dimens.IconMd)
            )
            else -> Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MonoBlack
            )
        }
    }
}
