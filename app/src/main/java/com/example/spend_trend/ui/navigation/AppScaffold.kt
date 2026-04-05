package com.example.spend_trend.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.data.UserPreferences
import com.example.spend_trend.ui.auth.AuthViewModel
import com.example.spend_trend.ui.auth.LoginScreen
import com.example.spend_trend.ui.auth.RegisterScreen
import com.example.spend_trend.ui.auth.PinSetupScreen
import com.example.spend_trend.ui.bills.AddBillScreen
import com.example.spend_trend.ui.bills.BillViewModel
import com.example.spend_trend.ui.bills.BillViewModelFactory
import com.example.spend_trend.ui.bills.BillsScreen
import com.example.spend_trend.ui.budgets.AddBudgetScreen
import com.example.spend_trend.ui.budgets.BudgetDetailScreen
import com.example.spend_trend.ui.budgets.BudgetViewModel
import com.example.spend_trend.ui.budgets.BudgetViewModelFactory
import com.example.spend_trend.ui.budgets.BudgetsScreen
import com.example.spend_trend.ui.contact.ContactUsScreen
import com.example.spend_trend.ui.copilot.CopilotScreen
import com.example.spend_trend.ui.dashboard.DashboardScreen
import com.example.spend_trend.ui.forecast.ForecastScreen
import com.example.spend_trend.ui.goals.GoalScreen
import com.example.spend_trend.ui.help.HelpScreen
import com.example.spend_trend.ui.profile.ProfileScreen
import com.example.spend_trend.ui.settings.SettingsScreen
import com.example.spend_trend.ui.transaction.AddTransactionScreen
import com.example.spend_trend.ui.transaction.TransactionHistoryScreen
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import io.github.jan.supabase.gotrue.auth

@Composable
fun AppScaffold(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(
            BudgetRepository(db.budgetDao()),
            TransactionRepository(db.transactionDao())
        )
    )
    val billViewModel: BillViewModel = viewModel(
        factory = BillViewModelFactory(
            BillRepository(db.billDao()),
            TransactionRepository(db.transactionDao())
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Budgets,
        BottomNavItem.Copilot
    )

    val isBottomNavRoute = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    val startDestination = when {
        !UserPreferences.isRegistered() -> Screen.Register.route
        !UserPreferences.hasPin() -> Screen.PinSetup.route
        !UserPreferences.isLoggedIn() -> Screen.Login.route
        else -> Screen.Dashboard.route
    }

    val performLogout: (Boolean) -> Unit = { isSystemLogout ->
        scope.launch {
            drawerState.close()
            try {
                com.example.spend_trend.data.network.SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {}
            
            if (isSystemLogout) {
                // wiping all data
                withContext(Dispatchers.IO) {
                    db.clearAllTables()
                }
                UserPreferences.clearAll()
                navController.navigate(Screen.Register.route) {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                UserPreferences.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isBottomNavRoute,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = Dimens.RadiusHero, bottomEnd = Dimens.RadiusHero),
                modifier = Modifier.border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline, RoundedCornerShape(topEnd = Dimens.RadiusHero, bottomEnd = Dimens.RadiusHero))
            ) {
                DrawerHeader()
                Spacer(Modifier.height(Dimens.SpacingLg))
                Column(
                    modifier = Modifier.padding(horizontal = Dimens.SpacingLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
                ) {
                    DrawerItem(Icons.Default.PieChart, "ANALYTICS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Analytics.route)
                    }
                    DrawerItem(Icons.Default.Receipt, "BILLS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Bills.route)
                    }
                    DrawerItem(Icons.Default.Stars, "GOALS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Goals.route)
                    }
                    DrawerItem(Icons.Default.Timeline, "FORECAST") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Forecast.route)
                    }
                    
                    Box(Modifier.fillMaxWidth().height(Dimens.DividerThickness).background(MaterialTheme.colorScheme.outline).padding(vertical = Dimens.SpacingSm))
                    
                    DrawerItem(Icons.Default.Person, "PROFILE") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Profile.route)
                    }
                    DrawerItem(Icons.Default.Settings, "SETTINGS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route)
                    }
                    DrawerItem(Icons.AutoMirrored.Filled.Logout, "LOGOUT") {
                        performLogout(false)
                    }
                }
            }
        },
        content = {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {},
                bottomBar = {
                    if (isBottomNavRoute) {
                        Box(modifier = Modifier.padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd).fillMaxWidth()) {
                            NavigationBar(
                                containerColor = MonoBlack,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(CircleShape)
                                    .border(Dimens.BorderWidthStandard, MonoBlack, CircleShape)
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    val indicatorCol = when(item.route) {
                                        Screen.Dashboard.route -> CategoryColors.Green
                                        Screen.Transactions.route -> CategoryColors.Blue
                                        Screen.Analytics.route -> CategoryColors.Yellow
                                        Screen.Copilot.route -> CategoryColors.Purple
                                        else -> Primary
                                    }
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon, 
                                                contentDescription = item.contentDescription,
                                                tint = if (isSelected) MonoBlack else MonoWhite.copy(alpha = 0.7f)
                                            ) 
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = indicatorCol,
                                            selectedIconColor = MonoBlack,
                                            unselectedIconColor = MonoWhite.copy(alpha = 0.7f),
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // Auth Flow
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(Screen.PinSetup.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                        )
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                        )
                    }
                    composable(Screen.PinSetup.route) {
                        PinSetupScreen(
                            onSetupComplete = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.PinSetup.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Main Flow
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            onViewAllTransactions = { 
                                navController.navigate(Screen.Transactions.route) 
                            },
                            onNavigateToAddTx = { 
                                navController.navigate(Screen.AddTransaction.route) 
                            },
                            onNavigateToBills = {
                                navController.navigate(Screen.Bills.route)
                            },
                            onNavigateToAnalytics = {
                                navController.navigate(Screen.Analytics.route)
                            },
                            onNavigateToGoals = {
                                navController.navigate(Screen.Goals.route)
                            },
                            onNavigateToForecast = {
                                navController.navigate(Screen.Forecast.route)
                            },
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            },
                            onOpenDrawer = { 
                                scope.launch { drawerState.open() } 
                            }
                        )
                    }
                    composable(Screen.Transactions.route) {
                        TransactionHistoryScreen(
                            snackbarHostState = snackbarHostState,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable(Screen.Copilot.route) {
                        CopilotScreen(
                            onBack = { navController.popBackStack() },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable(Screen.Analytics.route) {
                        com.example.spend_trend.ui.analytics.AnalyticsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = { performLogout(true) }
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = { performLogout(false) }
                        )
                    }
                    composable(Screen.Help.route) {
                        HelpScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Contact.route) {
                        ContactUsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.AddTransaction.route) {
                        AddTransactionScreen(
                            onSave = { _ ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Transaction added!")
                                }
                            },
                            onDismiss = { navController.popBackStack() },
                            snackbarHostState = snackbarHostState
                        )
                    }
                    composable(
                        route = Screen.BudgetDetail.route,
                        arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
                        BudgetDetailScreen(
                            viewModel = budgetViewModel,
                            budgetId = budgetId, 
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.AddBudget.route) {
                        AddBudgetScreen(viewModel = budgetViewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Budgets.route) {
                        BudgetsScreen(
                            onCardClick = { id -> navController.navigate(Screen.BudgetDetail.createRoute(id)) },
                            onBack = { navController.popBackStack() },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onAddClick = { navController.navigate(Screen.AddBudget.route) }
                        )
                    }
                    composable(Screen.Goals.route) {
                        GoalScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Bills.route) {
                        BillsScreen(
                            onBack = { navController.popBackStack() },
                            onAddClick = { navController.navigate(Screen.AddBill.route) }
                        )
                    }
                    composable(Screen.AddBill.route) {
                        AddBillScreen(viewModel = billViewModel, onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Forecast.route) {
                        com.example.spend_trend.ui.forecast.ForecastScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun DrawerHeader() {
    val name = remember { mutableStateOf(UserPreferences.getName() ?: "GUEST") }
    LaunchedEffect(Unit) {
        name.value = UserPreferences.getName() ?: "GUEST"
    }
    Box(
        modifier = Modifier.fillMaxWidth().background(MonoBlack).padding(Dimens.SpacingXxl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Primary, CircleShape)
                    .border(Dimens.BorderWidthStandard, MonoBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(name.value.take(1).uppercase(), style = MaterialTheme.typography.displaySmall, color = MonoBlack, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(Dimens.SpacingMd))
            Text(name.value.uppercase(), style = MaterialTheme.typography.titleLarge, color = MonoWhite, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusLg))
            .border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusLg))
            .background(MonoWhite)
            .clickable(onClick = onClick)
            .padding(Dimens.SpacingMd)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MonoBlack)
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(label, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = MonoBlack)
        }
    }
}

private fun screenTitle(route: String?): String = when (route) {
    Screen.Dashboard.route -> "DASHBOARD"
    Screen.Transactions.route -> "HISTORY"
    Screen.Analytics.route -> "ANALYTICS"
    Screen.Copilot.route -> "COPILOT"
    Screen.Bills.route -> "BILLS"
    Screen.Budgets.route -> "BUDGETS"
    Screen.Goals.route -> "GOALS"
    Screen.Forecast.route -> "FORECAST"
    else -> "SPENDTREND"
}
