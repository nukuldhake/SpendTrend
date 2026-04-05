package com.example.spend_trend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import com.example.spend_trend.ui.theme.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path

/**
 * SpendTrend Neo-Brutal — Standard Block Card
 * A stark, sharp-cornered container with a solid border and optional "hard" shadow.
 * The shadow color can be customized (defaulting to the accent blue for visual pop).
 */
@Composable
fun BlockCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = Dimens.BorderWidthStandard,
    hasShadow: Boolean = true,
    shadowColor: Color = MonoBlack,
    shape: Shape = RoundedCornerShape(Dimens.RadiusLg),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .then(if (hasShadow) Modifier.neoShadow(shape, shadowColor) else Modifier)
            .background(backgroundColor, shape)
            .border(BorderStroke(width = borderWidth, color = borderColor), shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Dimens.CardPadding),
        content = content
    )
}

/**
 * SpendTrend Neo-Brutal — Sharp Button with accent shadow
 */
@Composable
fun BlockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    accentColor: Color = Primary    // Accent color for primary buttons
) {
    val containerColor = if (isPrimary) accentColor else MonoWhite
    val contentColor   = if (isPrimary) MonoBlack else MonoBlack
    val borderColor    = MonoBlack

    val shape = RoundedCornerShape(Dimens.RadiusLg)

    Box(modifier = modifier) {
        // Hard shadow for buttons
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = Dimens.ShadowOffset, y = Dimens.ShadowOffset)
                    .background(MonoBlack, shape)
                    .border(BorderStroke(Dimens.BorderWidthStandard, MonoBlack), shape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.MinTouchTarget)
                .background(if (enabled && !isLoading) containerColor else containerColor.copy(alpha = 0.5f), shape)
                .border(BorderStroke(width = Dimens.BorderWidthStandard, color = borderColor), shape)
                .clip(shape)
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text.uppercase(),
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * SpendTrend Neo-Brutal — Top Bar with thick bottom divider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navIcon: @Composable () -> Unit = navigationIcon ?: {
        if (onMenuClick != null) {
            IconButton(onClick = onMenuClick) {
                Icon(androidx.compose.material.icons.Icons.Default.Menu, contentDescription = "Menu")
            }
        } else if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }

    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            },
            navigationIcon = navIcon,
            actions = actions,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        // Heavy bottom divider for brutalist separation
        HorizontalDivider(thickness = Dimens.BorderWidthStandard, color = MaterialTheme.colorScheme.outline)
    }
}

/**
 * SpendTrend Neo-Brutal — Chip (flat, bordered, no neumorphism)
 * Used for filter selections, category tags, etc.
 */
@Composable
fun BlockChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = Primary,
    onSelectedColor: Color = OnPrimary,
) {
    Box(modifier = modifier) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = Dimens.ShadowOffset, y = Dimens.ShadowOffset)
                    .background(MonoBlack, CircleShape)
                    .border(Dimens.BorderWidthStandard, MonoBlack, CircleShape)
            )
        }
        
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(if (isSelected) selectedColor else MaterialTheme.colorScheme.surface, CircleShape)
                .border(Dimens.BorderWidthStandard, if (isSelected) MonoBlack else MonoGrayMedium, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = if (isSelected) onSelectedColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Modifier Extension for Neo-Brutalist Global Shadows
 */
fun Modifier.neoShadow(
    shape: Shape = RoundedCornerShape(Dimens.RadiusLg),
    shadowColor: Color = MonoBlack,
    offset: Dp = Dimens.ShadowOffset
): Modifier = this.then(
    Modifier.drawBehind {
        val shadowOutline = shape.createOutline(size, layoutDirection, this)
        val shadowPath = Path().apply {
            when (shadowOutline) {
                is Outline.Rectangle -> addRect(shadowOutline.rect)
                is Outline.Rounded -> addRoundRect(shadowOutline.roundRect)
                is Outline.Generic -> addPath(shadowOutline.path)
            }
        }

        // --- Hole-Punch Shadow Logic ---
        // We clip out the component's own shape from the shadow's drawing area.
        // This ensures that even if the component is transparent, we never see 
        // the black shadow directly behind it—only where it is offset.
        clipPath(path = shadowPath, clipOp = ClipOp.Difference) {
            translate(left = offset.toPx(), top = offset.toPx()) {
                drawPath(path = shadowPath, color = shadowColor)
            }
        }
    }
)
