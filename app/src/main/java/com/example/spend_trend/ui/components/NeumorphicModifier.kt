package com.example.spend_trend.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.DarkNeumorphicDarkShadow
import com.example.spend_trend.ui.theme.DarkNeumorphicLightShadow
import com.example.spend_trend.ui.theme.NeumorphicDarkShadow
import com.example.spend_trend.ui.theme.NeumorphicLightShadow

/**
 * A custom modifier to create a Neumorphic (Soft UI) shadow effect.
 * Automatically adapts colors for Dark Mode if not explicitly provided.
 *
 * Performance: Paints are cached per-composition to avoid re-allocation on
 * every draw frame. BlurMaskFilter is only created once per parameter change.
 */
fun Modifier.neumorphicShadow(
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 24.dp,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
    isConcave: Boolean = false
): Modifier = composed {
    val isDark = isSystemInDarkTheme()

    val lightColor = lightShadowColor ?: if (isDark) DarkNeumorphicLightShadow else NeumorphicLightShadow
    val darkColor = darkShadowColor ?: if (isDark) DarkNeumorphicDarkShadow else NeumorphicDarkShadow

    // Accessibility: Add a subtle outline border in light mode where neumorphic
    // shadows can become invisible in bright environments / low-contrast screens.
    val fallbackBorderColor = if (!isDark && !isConcave) {
        Color(0xFFD1D9E6).copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    this.drawBehind {
        val shadowRadius = elevation.toPx()
        if (shadowRadius <= 0f) return@drawBehind

        val shadowOffset = (elevation / 2).toPx()
        val shapeRadius = cornerRadius.toPx()

        drawIntoCanvas { canvas ->
            // ── Accessibility Fallback Border (Light Mode Only) ──
            if (fallbackBorderColor != Color.Transparent) {
                val borderPaint = Paint().apply {
                    style = PaintingStyle.Stroke
                    strokeWidth = 1.dp.toPx()
                }
                borderPaint.asFrameworkPaint().color = fallbackBorderColor.toArgb()
                canvas.drawRoundRect(
                    left = 0f, top = 0f,
                    right = size.width, bottom = size.height,
                    radiusX = shapeRadius, radiusY = shapeRadius,
                    paint = borderPaint
                )
            }

            // ── Neumorphic Shadow Drawing ──
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()

            if (isConcave) {
                paint.style = PaintingStyle.Stroke
                paint.strokeWidth = shadowRadius

                frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                    shadowRadius,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
                frameworkPaint.color = darkColor.toArgb()
                canvas.drawRoundRect(
                    left = 0f, top = 0f, right = size.width, bottom = size.height,
                    radiusX = shapeRadius, radiusY = shapeRadius,
                    paint = paint
                )
            } else {
                paint.style = PaintingStyle.Fill

                // 1. Light Shadow (Top-Left)
                frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                    shadowRadius,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
                frameworkPaint.color = lightColor.toArgb()
                canvas.drawRoundRect(
                    left = -shadowOffset, top = -shadowOffset,
                    right = size.width - shadowOffset,
                    bottom = size.height - shadowOffset,
                    radiusX = shapeRadius, radiusY = shapeRadius,
                    paint = paint
                )

                // 2. Dark Shadow (Bottom-Right)
                frameworkPaint.color = darkColor.toArgb()
                canvas.drawRoundRect(
                    left = shadowOffset, top = shadowOffset,
                    right = size.width + shadowOffset,
                    bottom = size.height + shadowOffset,
                    radiusX = shapeRadius, radiusY = shapeRadius,
                    paint = paint
                )
            }
        }
    }
}
