package com.example.spend_trend.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════
//  SpendTrend — Dark-First Premium Palette
// ════════════════════════════════════════════════

// ── Primary Accent — Vivid Emerald ──
val Primary             = Color(0xFF00D09C)
val OnPrimary           = Color(0xFF00382A)
val PrimaryContainer    = Color(0xFF004D3B)
val OnPrimaryContainer  = Color(0xFF7DFBD2)

// ── Secondary — Electric Blue ──
val Secondary           = Color(0xFF3B82F6)
val OnSecondary         = Color(0xFFFFFFFF)
val SecondaryContainer  = Color(0xFF1E3A5F)
val OnSecondaryContainer = Color(0xFFBFDBFE)

// ── Tertiary — Soft Purple ──
val Tertiary            = Color(0xFFA78BFA)
val OnTertiary          = Color(0xFFFFFFFF)
val TertiaryContainer   = Color(0xFF3B1F7E)
val OnTertiaryContainer = Color(0xFFDDD6FE)

// ── Semantic ──
val Error               = Color(0xFFFF6B6B)
val OnError             = Color(0xFFFFFFFF)
val ErrorContainer      = Color(0xFF5C1A1A)
val OnErrorContainer    = Color(0xFFFCA5A5)

val IncomeGreen         = Color(0xFF4ADE80)
val ExpenseRose         = Color(0xFFFB7185)
val WarningAmber        = Color(0xFFFBBF24)

// ════════════════════════════════════════════════
//  Dark Mode (Hero) — Deep Navy / Charcoal
// ════════════════════════════════════════════════
val DarkBackground      = Color(0xFF0A0E1A)
val DarkSurface         = Color(0xFF111827)
val DarkSurfaceVariant  = Color(0xFF1F2937)
val DarkOnSurface       = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline         = Color(0xFF334155)
val DarkOutlineVariant  = Color(0xFF1E293B)

// ════════════════════════════════════════════════
//  Light Mode — Soft Slate
// ════════════════════════════════════════════════
val LightBackground     = Color(0xFFF7FDFB)
val LightSurface        = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFECFDF5)
val LightOnSurface      = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline        = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)

val InverseSurface      = Color(0xFF1E293B)
val InverseOnSurface    = Color(0xFFF1F5F9)
val InversePrimary      = Color(0xFF6EE7B7)

// ════════════════════════════════════════════════
//  Glassmorphism Helpers
// ════════════════════════════════════════════════
object GlassColors {
    // Dark mode glass
    val DarkGlassSurface   = Color(0xFF1A1F2E).copy(alpha = 0.65f)
    val DarkGlassBorder    = Color.White.copy(alpha = 0.08f)
    val DarkGlassHighlight = Color.White.copy(alpha = 0.04f)

    // Light mode glass — fresh crystal white
    val LightGlassSurface  = Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val LightGlassBorder   = Color(0xFF00D09C).copy(alpha = 0.15f)
    val LightGlassHighlight = Color.White.copy(alpha = 0.80f)
}

// ════════════════════════════════════════════════
//  Gradient Presets (for hero cards, buttons)
// ════════════════════════════════════════════════
object GradientPalette {
    val EmeraldTeal = Brush.linearGradient(
        colors = listOf(Color(0xFF00D09C), Color(0xFF0891B2))
    )
    val BluePurple = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFFA78BFA))
    )
    val SunsetCoral = Brush.linearGradient(
        colors = listOf(Color(0xFFF97316), Color(0xFFFB7185))
    )
    val DeepOcean = Brush.linearGradient(
        colors = listOf(Color(0xFF0F766E), Color(0xFF1E40AF))
    )
    val Shimmer = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.0f)
        )
    )
}