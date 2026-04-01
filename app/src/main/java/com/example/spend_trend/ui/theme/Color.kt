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
val MonoBlack            = Color(0xFF000000)   // Pure Black
val MonoWhite            = Color(0xFFFFFFFF)   // Pure White
val MonoGrayDark         = Color(0xFF333333)   
val MonoGrayLight        = Color(0xFFE0E0E0)   
val MonoGrayMedium       = Color(0xFF888888)   

// ── Brand Primary (Neon Cyan) ──
val Primary              = Color(0xFF22D3EE)   // Cyan-400
val OnPrimary            = Color(0xFF000000)
val PrimaryDark          = Color(0xFF0891B2)   // Cyan-600
val PrimaryLight         = Color(0xFFA5F3FC)   // Cyan-200

// ── Secondary (Slate) ──
val Secondary            = Color(0xFF14C38E)   // Vibrant Green
val OnSecondary          = Color(0xFF000000)

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
val DarkBackground       = Color(0xFF083344)   // Cyan-950 (Deep Slate Cyan)
val DarkSurface          = Color(0xFF164E63)   // Cyan-900 (Dark Teal)
val DarkSurfaceVariant   = Color(0xFF064E3B)
val DarkOnSurface        = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFAAAAAA)
val DarkOutline          = Color(0xFF000000)   // Pure Black for Brutalist contrast

// ════════════════════════════════════════════════
//  Light Mode — Paper White
// ════════════════════════════════════════════════
val LightBackground      = Color(0xFFD4F6ED)   // Pastel Mint
val LightSurface         = Color(0xFFFFFFFF)   // Pure white for cards
val LightSurfaceVariant  = Color(0xFFF2F2F2)
val LightOnSurface       = Color(0xFF000000)   // Pure Black
val LightOnSurfaceVariant = Color(0xFF444444)
val LightOutline         = Color(0xFF000000)   // Pure Black

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
//  Category Colors (For Transaction Cards)
// ════════════════════════════════════════════════
object CategoryColors {
    val Orange = Color(0xFFFF8E4F)
    val Blue   = Color(0xFF7AD1F9)
    val Purple = Color(0xFFA07CF6)
    val Yellow = Color(0xFFFFDF59)
    val Green  = Color(0xFF14C38E)
    val Pink   = Color(0xFFFFA1C1)

    fun getColorForCategory(category: String, default: Color = Color(0xFFFFFFFF)): Color {
        return when (category.lowercase()) {
            "food", "dining" -> Orange
            "transport", "travel" -> Blue
            "shopping", "retail" -> Purple
            "housing", "bills", "utilities" -> Yellow
            "salary", "income" -> Green
            "health", "fitness" -> Pink
            else -> default // Will fall back to white card if unknown
        }
    }
}

// ════════════════════════════════════════════════
//  Chart Palette (for pie chart / analytics)
// ════════════════════════════════════════════════
object ChartPalette {
    val colors = listOf(
        CategoryColors.Orange,
        CategoryColors.Purple,
        CategoryColors.Yellow,
        CategoryColors.Green,
        CategoryColors.Blue,
        CategoryColors.Pink,
        Color(0xFFEC4899),
        Color(0xFF84CC16)
    )
}
