package com.example.spend_trend.ui.theme  // or .ui.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "food" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "shopping" -> Icons.Default.ShoppingBag
    "salary", "income" -> Icons.Default.AttachMoney
    "entertainment" -> Icons.Default.Movie
    "bills" -> Icons.Default.Receipt
    "health" -> Icons.Default.LocalHospital
    else -> Icons.Default.Category
}