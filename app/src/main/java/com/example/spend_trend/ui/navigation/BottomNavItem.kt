package com.example.spend_trend.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
) {
    object Dashboard : BottomNavItem(
        route = "dashboard",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Home screen"
    )

    object Transactions : BottomNavItem(
        route = "transactions",
        label = "Transactions",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        contentDescription = "Transaction history"
    )

    object Budgets : BottomNavItem(
        route = "budgets",
        label = "Budgets",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        contentDescription = "Budgets overview"
    )

    object Forecast : BottomNavItem(
        route = "forecast",
        label = "Forecast",
        selectedIcon = Icons.Filled.TrendingUp,
        unselectedIcon = Icons.Outlined.TrendingUp,
        contentDescription = "Spending forecast"
    )

    object Copilot : BottomNavItem(
        route = "copilot",
        label = "Copilot",
        selectedIcon = Icons.Filled.SmartToy,
        unselectedIcon = Icons.Outlined.SmartToy,
        contentDescription = "AI financial assistant"
    )
}