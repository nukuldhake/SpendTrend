package com.example.spend_trend.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single source of truth for category icons.
 * All screens should import this instead of defining local copies.
 */
fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "food", "dining" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "shopping" -> Icons.Default.ShoppingBag
    "salary", "income" -> Icons.Default.AttachMoney
    "entertainment", "fun" -> Icons.Default.Movie
    "bills", "utilities" -> Icons.Default.Receipt
    "health" -> Icons.Default.LocalHospital
    else -> Icons.Default.Category
}

/**
 * Single source of truth for comma-formatted integers.
 * e.g. 12345 → "12,345"
 */
fun Int.formatWithComma(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()