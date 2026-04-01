package com.example.spend_trend.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Single source of truth for category icons.
 * Covers ALL categories used across Transaction, Budget, and Bill screens.
 */
fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    // Transaction categories
    "food", "dining"          -> Icons.Default.Restaurant
    "transport"               -> Icons.Default.DirectionsCar
    "shopping"                -> Icons.Default.ShoppingBag
    "salary", "income"        -> Icons.Default.AccountBalance
    "entertainment", "fun"    -> Icons.Default.Movie
    "bills", "utilities"      -> Icons.Default.Receipt
    "health"                  -> Icons.Default.LocalHospital

    // Budget / Bill categories
    "rent"                    -> Icons.Default.Home
    "education"               -> Icons.Default.School
    "travel"                  -> Icons.Default.Flight
    "subscriptions"           -> Icons.Default.Subscriptions
    "insurance"               -> Icons.Default.Shield
    "loan"                    -> Icons.Default.CreditCard
    "credit card"             -> Icons.Default.CreditCard
    "water"                   -> Icons.Default.WaterDrop
    "electricity"             -> Icons.Default.ElectricBolt
    "mobile"                  -> Icons.Default.PhoneAndroid
    "internet"                -> Icons.Default.Wifi
    "investment"              -> Icons.AutoMirrored.Filled.TrendingUp
    "savings"                 -> Icons.Default.Savings

    else                      -> Icons.Default.Category
}