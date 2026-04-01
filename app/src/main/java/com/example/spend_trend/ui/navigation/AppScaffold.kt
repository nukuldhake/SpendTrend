package com.example.spend_trend.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.spend_trend.ui.analytics.AnalyticsScreen
import com.example.spend_trend.ui.auth.AuthViewModel
import com.example.spend_trend.ui.auth.LoginScreen
import com.example.spend_trend.ui.auth.RegisterScreen
import com.example.spend_trend.ui.auth.PinSetupScreen
import com.example.spend_trend.data.UserPreferences
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
import kotlinx.coroutines.launch

import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.MonoBlack
import com.example.spend_trend.ui.theme.MonoWhite
import com.example.spend_trend.ui.theme.Primary
import com.example.spend_trend.ui.theme.Dimens
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment

@Composable
fun AppScaffold(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // ViewModels for shared sections
    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(BudgetRepository(db.budgetDao()))
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
    val coroutineScope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Analytics,
        BottomNavItem.Copilot
    )

    // Check if current route is a bottom nav destination
    val isBottomNavRoute = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    val startDestination = when {
        !UserPreferences.isRegistered() -> Screen.Register.route
        !UserPreferences.hasPin() -> Screen.PinSetup.route
        !UserPreferences.isLoggedIn() -> Screen.Login.route
        else -> Screen.Dashboard.route
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isBottomNavRoute,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RectangleShape,
                modifier = Modifier.border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline, RectangleShape)
            ) {
                DrawerHeader()
                Spacer(Modifier.height(Dimens.SpacingLg))
                Column(
                    modifier = Modifier.padding(horizontal = Dimens.SpacingLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
                ) {
                    DrawerItem(Icons.Default.Savings, "BUDGETS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Budgets.route)
                    }
                    DrawerItem(Icons.Default.Receipt, "BILLS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Bills.route)
                    }
                    DrawerItem(Icons.Default.Stars, "GOALS") {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Goals.route)
                    }
                    DrawerItem(Icons.Default.AutoGraph, "FORECAST") {
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
                        scope.launch { 
                            drawerState.close()
                            UserPreferences.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isBottomNavRoute) {
                    BlockTopBar(
                        title = screenTitle(currentDestination?.route),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isBottomNavRoute) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline, RectangleShape)
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon, 
                                        contentDescription = item.contentDescription,
                                        tint = if (isSelected) MonoWhite else MaterialTheme.colorScheme.onSurface
                                    ) 
                                },
                                label = { 
                                    Text(
                                        item.label.uppercase(), 
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelSmall
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
                                    indicatorColor = Primary,
                                    selectedIconColor = MonoWhite,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (isBottomNavRoute) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Primary)
                            .border(Dimens.BorderWidthStandard, MonoBlack)
                            .clickable {
                                navController.navigate(Screen.AddTransaction.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MonoWhite, modifier = Modifier.size(32.dp))
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
                        onNavigateToLogin = { 
                            navController.navigate(Screen.Login.route)
                        }
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
                        onNavigateToRegister = { 
                            navController.navigate(Screen.Register.route)
                        }
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
                        onViewAllTransactions = { navController.navigate(Screen.Transactions.route) }
                    )
                }
                composable(Screen.Transactions.route) {
                    TransactionHistoryScreen(snackbarHostState = snackbarHostState)
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(Screen.Copilot.route) {
                    CopilotScreen()
                }

                // Secondary Screens
                composable(Screen.Profile.route) {
                    ProfileScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
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
                            coroutineScope.launch {
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
                    AddBudgetScreen(
                        viewModel = budgetViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Budgets.route) {
                    BudgetsScreen(
                        onCardClick = { id -> navController.navigate(Screen.BudgetDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Goals.route) {
                    GoalScreen()
                }

                composable(Screen.Bills.route) {
                    BillsScreen()
                }

                composable(Screen.AddBill.route) {
                    AddBillScreen(
                        viewModel = billViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Forecast.route) {
                    ForecastScreen()
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    val name = UserPreferences.getName() ?: "GUEST"
    Box(
        modifier = Modifier.fillMaxWidth().background(MonoBlack).padding(Dimens.SpacingXxl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp).border(Dimens.BorderWidthStandard, Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), style = MaterialTheme.typography.displaySmall, color = MonoWhite, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(Dimens.SpacingMd))
            Text(name.uppercase(), style = MaterialTheme.typography.titleLarge, color = MonoWhite, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline).clickable(onClick = onClick).padding(Dimens.SpacingMd)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(label, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun screenTitle(route: String?): String = when (route) {
    Screen.Dashboard.route -> "DASHBOARD"
    Screen.Transactions.route -> "HISTORY"
    Screen.Analytics.route -> "ANALYTICS"
    Screen.Copilot.route -> "COPILOT"
    else -> "SPENDTREND"
}
