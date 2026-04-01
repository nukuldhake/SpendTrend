package com.example.spend_trend.ui.theme

/**
 * String/number formatting utilities.
 * Replaces the mis-located Int.formatWithComma() that was in Icons.kt.
 */

/**
 * Format an integer with comma separators.
 * e.g. 12345 → "12,345", -5000 → "-5,000"
 */
fun Int.formatWithComma(): String {
    val abs = if (this < 0) -this else this
    val formatted = abs.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (this < 0) "-$formatted" else formatted
}

/**
 * Format a Long with comma separators.
 */
fun Long.formatWithComma(): String {
    val abs = if (this < 0) -this else this
    val formatted = abs.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (this < 0) "-$formatted" else formatted
}
