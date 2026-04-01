package com.example.spend_trend.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized design tokens for SpendTrend Playful Neo-Brutalism.
 * Large curvy geometry, thick borders, hard offset shadows.
 * All layout values must reference these tokens — no magic numbers.
 */
object Dimens {
    // ── Spacing Scale (4dp grid) ──
    val SpacingXxs  = 2.dp
    val SpacingXs   = 4.dp
    val SpacingSm   = 8.dp
    val SpacingMd   = 12.dp
    val SpacingLg   = 16.dp
    val SpacingXl   = 20.dp
    val SpacingXxl  = 24.dp
    val SpacingHuge = 32.dp
    val Spacing3xl  = 40.dp

    // ── Playful Curvy Radii ──
    val RadiusXs   = 8.dp
    val RadiusSm   = 12.dp
    val RadiusMd   = 16.dp
    val RadiusLg   = 24.dp
    val RadiusXl   = 32.dp
    val RadiusHero = 40.dp
    val RadiusFull = 999.dp

    // ── Border & Outline (Neo-Brutal Core) ──
    val BorderWidthThin     = 1.dp
    val BorderWidthStandard = 2.dp     // Bumped from 1.5 → 2 for brutalist heft
    val BorderWidthThick    = 3.dp
    val ShadowOffset        = 4.dp     // Standard hard shadow offset
    val ShadowOffsetLg      = 6.dp     // Hero card shadow

    // ── Glassmorphism (deprecated stubs, kept for ABI) ──
    val GlassBlurRadius   = 16.dp
    val GlassBorderWidth  = 1.dp

    // ── Elevation (flat design — minimal use) ──
    val ElevationNone = 0.dp
    val ElevationSm   = 1.dp
    val ElevationMd   = 2.dp
    val ElevationLg   = 4.dp
    val ElevationXl   = 8.dp

    // ── Icon Sizes ──
    val IconXs   = 16.dp
    val IconSm   = 20.dp
    val IconMd   = 24.dp
    val IconLg   = 28.dp
    val IconXl   = 32.dp
    val IconHero = 64.dp

    // ── Avatar Sizes ──
    val AvatarSm   = 32.dp
    val AvatarMd   = 44.dp
    val AvatarLg   = 64.dp
    val AvatarXl   = 120.dp

    // ── Card ──
    val CardPadding   = 16.dp
    val CardPaddingLg = 24.dp
    val CardPaddingSm = 12.dp

    // ── Divider ──
    val DividerThickness = 2.dp       // Brutalist divider (was 1.5)

    // ── Badge ──
    val BadgeSize = 18.dp

    // ── Chart ──
    val ChartHeight      = 200.dp
    val ChartStrokeWidth = 4.dp       // Thicker chart lines
    val ChartDotRadius   = 5.dp       // Larger data point markers

    // ── Touch targets ──
    val MinTouchTarget = 48.dp

    // ── Bottom nav / FAB clearance ──
    val BottomNavHeight    = 64.dp
    val BottomNavClearance = 80.dp

    // ── Input Fields ──
    val InputHeight = 56.dp
    val InputBorderWidth = 2.dp
}
