package com.example.spend_trend.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════
//  SpendTrend Neo-Brutal — Slate / Emerald Palette
// ════════════════════════════════════════════════
//  Philosophy: Replace harsh pure monochrome with
//  sophisticated Slate darks, warm Paper-White lights,
//  and vivid but refined accent colors.
// ════════════════════════════════════════════════

// ── Core Identity (replaces Mono*) ──
val MonoBlack            = Color(0xFF0F172A)   // Slate-900 — deep ink, not harsh black
val MonoWhite            = Color(0xFFF8FAFC)   // Slate-50 / Paper White
val MonoGrayDark         = Color(0xFF1E293B)   // Slate-800
val MonoGrayLight        = Color(0xFFE2E8F0)   // Slate-200
val MonoGrayMedium       = Color(0xFF64748B)   // Slate-500

// ── Brand Primary (Electric Blue) ──
val Primary              = Color(0xFF3B82F6)   // Blue-500
val OnPrimary            = Color(0xFFFFFFFF)
val PrimaryDark          = Color(0xFF1D4ED8)   // Blue-700
val PrimaryLight         = Color(0xFFDBEAFE)   // Blue-100

// ── Secondary (Slate) ──
val Secondary            = Color(0xFF475569)   // Slate-600
val OnSecondary          = Color(0xFFFFFFFF)

// ── Semantic Status ──
val IncomeGreen          = Color(0xFF10B981)   // Emerald-500
val IncomeGreenLight     = Color(0xFFD1FAE5)   // Emerald-100
val ExpenseRose          = Color(0xFFF43F5E)   // Rose-500
val ExpenseRoseLight     = Color(0xFFFFE4E6)   // Rose-100
val WarningAmber         = Color(0xFFF59E0B)   // Amber-500
val WarningAmberLight    = Color(0xFFFEF3C7)   // Amber-100

// ── Accent: Emerald for highlights ──
val AccentEmerald        = Color(0xFF059669)   // Emerald-600
val AccentEmeraldDark    = Color(0xFF047857)   // Emerald-700
val AccentEmeraldLight   = Color(0xFFECFDF5)   // Emerald-50

// ════════════════════════════════════════════════
//  Dark Mode — Deep Slate
// ════════════════════════════════════════════════
val DarkBackground       = Color(0xFF020617)   // Slate-950
val DarkSurface          = Color(0xFF0F172A)   // Slate-900
val DarkSurfaceVariant   = Color(0xFF1E293B)   // Slate-800
val DarkOnSurface        = Color(0xFFF1F5F9)   // Slate-100
val DarkOnSurfaceVariant = Color(0xFF94A3B8)   // Slate-400
val DarkOutline          = Color(0xFF475569)   // Slate-600

// ════════════════════════════════════════════════
//  Light Mode — Paper White
// ════════════════════════════════════════════════
val LightBackground      = Color(0xFFF8FAFC)   // Slate-50
val LightSurface         = Color(0xFFFFFFFF)   // Pure white for cards
val LightSurfaceVariant  = Color(0xFFF1F5F9)   // Slate-100
val LightOnSurface       = Color(0xFF0F172A)   // Slate-900
val LightOnSurfaceVariant = Color(0xFF475569)  // Slate-600
val LightOutline         = Color(0xFF0F172A)   // Slate-900

val InverseSurface       = Color(0xFFF1F5F9)   // Slate-100
val InverseOnSurface     = Color(0xFF0F172A)   // Slate-900

// ════════════════════════════════════════════════
//  Gradient Presets (Brutalist Accent)
// ════════════════════════════════════════════════
object GradientPalette {
    val EmeraldTeal = Brush.linearGradient(
        colors = listOf(Color(0xFF059669), Color(0xFF0D9488))
    )
    val BluePurple  = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
    )
    val SunsetCoral = Brush.linearGradient(
        colors = listOf(Color(0xFFF43F5E), Color(0xFFF97316))
    )
    val DeepOcean   = Brush.linearGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
    )
    val Shimmer     = Brush.horizontalGradient(
        colors = listOf(Color.Transparent, Color(0xFFFFFFFF).copy(alpha = 0.08f), Color.Transparent)
    )
}

// ════════════════════════════════════════════════
//  Glassmorphism Stubs (retained for ABI compat)
// ════════════════════════════════════════════════
object GlassColors {
    val DarkGlassSurface   = Color(0xFF1E293B).copy(alpha = 0.92f)
    val DarkGlassBorder    = Color(0xFF475569)
    val DarkGlassHighlight = Color(0xFFFFFFFF).copy(alpha = 0.06f)

    val LightGlassSurface  = Color(0xFFFFFFFF).copy(alpha = 0.92f)
    val LightGlassBorder   = Color(0xFF0F172A)
    val LightGlassHighlight = Color(0xFF0F172A).copy(alpha = 0.06f)
}

// ════════════════════════════════════════════════
//  Neumorphic Shadows (retained for ABI compat)
// ════════════════════════════════════════════════
val NeumorphicLightShadow     = Color(0xFFFFFFFF)
val NeumorphicDarkShadow      = Color(0xFFCBD5E1) // Slate-300
val DarkNeumorphicLightShadow = Color(0xFF1E293B) // Slate-800
val DarkNeumorphicDarkShadow  = Color(0xFF020617) // Slate-950

// ════════════════════════════════════════════════
//  Chart Palette (for pie chart / analytics)
// ════════════════════════════════════════════════
object ChartPalette {
    val colors = listOf(
        Color(0xFF3B82F6),  // Blue-500
        Color(0xFF10B981),  // Emerald-500
        Color(0xFFF59E0B),  // Amber-500
        Color(0xFFF43F5E),  // Rose-500
        Color(0xFF8B5CF6),  // Violet-500
        Color(0xFF06B6D4),  // Cyan-500
        Color(0xFFEC4899),  // Pink-500
        Color(0xFF84CC16),  // Lime-500
    )
}
