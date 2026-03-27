package com.example.spend_trend.ui.navigation

/**
 * Type-safe route declarations for all screens.
 * Use Screen.*.route instead of hardcoded strings.
 */
sealed class Screen(val route: String) {
    object Dashboard      : Screen("dashboard")
    object Transactions   : Screen("transactions")
    object Budgets        : Screen("budgets")
    object Forecast       : Screen("forecast")
    object Copilot        : Screen("copilot")
    object AddTransaction : Screen("add_transaction")
    object Profile        : Screen("profile")
    object Settings       : Screen("settings")
    object Help           : Screen("help")
    object Contact        : Screen("contact")
    object BudgetDetail   : Screen("budget_detail/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget_detail/$budgetId"
    }
    object AddBudget      : Screen("add_budget")
}
