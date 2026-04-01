package com.example.spend_trend.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * neumorphicShadow is DEPRECATED for Neo-Brutalism.
 * This extension now returns the Modifier unchanged.
 * Kept as a no-op stub so existing call-sites compile without changes.
 *
 * All visual shadow logic has been replaced with flat borders
 * in Neo-Brutal components (BlockCard, BlockTopBar, etc.)
 */
fun Modifier.neumorphicShadow(
    @Suppress("UNUSED_PARAMETER") elevation: Dp = 8.dp,
    @Suppress("UNUSED_PARAMETER") cornerRadius: Dp = 24.dp,
    @Suppress("UNUSED_PARAMETER") lightShadowColor: Color? = null,
    @Suppress("UNUSED_PARAMETER") darkShadowColor: Color? = null,
    @Suppress("UNUSED_PARAMETER") isConcave: Boolean = false
): Modifier = this  // No-op — Neumorphic shadows are purged
