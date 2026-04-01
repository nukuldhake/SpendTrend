package com.example.spend_trend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*

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
    hasShadow: Boolean = false,
    shadowColor: Color = Primary,               // Accent shadow — configurable
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Box(modifier = baseModifier) {
        // Hard Shadow Layer (Offset block — now with accent color)
        if (hasShadow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = Dimens.ShadowOffset, y = Dimens.ShadowOffset)
                    .background(shadowColor)
            )
        }

        // Main Card Layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .border(BorderStroke(width = borderWidth, color = borderColor))
                .padding(Dimens.CardPadding),
            content = content
        )
    }
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
    val containerColor = if (isPrimary) MonoBlack else MonoWhite
    val contentColor   = if (isPrimary) MonoWhite else MonoBlack
    val borderColor    = MonoBlack

    Box(modifier = modifier) {
        // Hard shadow for primary buttons
        if (isPrimary && enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = Dimens.SpacingXxs, y = Dimens.SpacingXxs)
                    .background(accentColor)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(Dimens.MinTouchTarget)
                .background(if (enabled && !isLoading) containerColor else containerColor.copy(alpha = 0.5f))
                .border(BorderStroke(width = Dimens.BorderWidthStandard, color = borderColor))
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .padding(horizontal = Dimens.SpacingLg),
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
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val navIcon: @Composable () -> Unit = navigationIcon ?: {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            fontWeight = FontWeight.Black,
            color = if (isSelected) onSelectedColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
