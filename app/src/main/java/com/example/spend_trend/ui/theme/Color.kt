package com.example.spend_trend.ui.theme

import androidx.compose.ui.graphics.Color

// ────────────────────────────────────────────────
// Core Brand Colors (Emerald/Green focused, modern fintech feel)
// ────────────────────────────────────────────────
val Primary = Color(0xFF10B981)           // Main action color (emerald green)
val OnPrimary = Color(0xFFFFFFFF)         // Text/icon on primary buttons

val PrimaryContainer = Color(0xFFD1FAE5)  // Subtle background for primary elements
val OnPrimaryContainer = Color(0xFF064E3B)

// ────────────────────────────────────────────────
// Secondary / Complementary Accent
// ────────────────────────────────────────────────
val Secondary = Color(0xFF0EA5E9)         // Soft blue — trust & info
val OnSecondary = Color(0xFFFFFFFF)

// ────────────────────────────────────────────────
// Status / Semantic Colors
// ────────────────────────────────────────────────
val Success = Color(0xFF34D399)
val OnSuccess = Color(0xFF064E3B)

val Warning = Color(0xFFF59E0B)
val OnWarning = Color(0xFFFFFFFF)

val Error = Color(0xFFEF4444)             // ← this fixes your 'Error' confusion
val OnError = Color(0xFFFFFFFF)

// ────────────────────────────────────────────────
// Neutrals & Surfaces (very important for elevation & readability)
// ────────────────────────────────────────────────
val Background = Color(0xFFF8FAFC)        // Very light cool gray-white

val Surface = Color(0xFFFAFAFA)           // Cards / main content areas
val SurfaceVariant = Color(0xFFE2E8F0)    // Slightly different surfaces (e.g. chips)
val OnSurface = Color(0xFF0F172A)         // Main text color (dark slate)
val OnSurfaceVariant = Color(0xFF475569)  // Secondary text, icons

val Outline = Color(0xFFCBD5E1)           // Borders, dividers

// Optional: Inverse for dark mode overlays, etc.
val InverseSurface = Color(0xFF0F172A)
val InverseOnSurface = Color(0xFFF8FAFC)