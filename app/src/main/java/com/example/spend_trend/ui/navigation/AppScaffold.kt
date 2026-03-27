package com.example.spend_trend.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.ui.budgets.AddBudgetScreen
import com.example.spend_trend.ui.budgets.BudgetDetailScreen
import com.example.spend_trend.ui.budgets.BudgetViewModel
import com.example.spend_trend.ui.budgets.BudgetViewModelFactory
import com.example.spend_trend.ui.budgets.BudgetsScreen
import com.example.spend_trend.ui.contact.ContactUsScreen
import com.example.spend_trend.ui.copilot.CopilotScreen
import com.example.spend_trend.ui.dashboard.DashboardScreen
import com.example.spend_trend.ui.forecast.ForecastScreen
import com.example.spend_trend.ui.help.HelpScreen
import com.example.spend_trend.ui.profile.ProfileScreen
import com.example.spend_trend.ui.settings.SettingsScreen
import com.example.spend_trend.ui.transaction.AddTransactionScreen
import com.example.spend_trend.ui.transaction.TransactionHistoryScreen
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onLogout = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                SpendTrendTopBar(
                    currentRoute = currentDestination?.route,
                    onDrawerOpen = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                SpendTrendBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            floatingActionButton = {
                val route = currentDestination?.route
                if (route == Screen.Dashboard.route || route == Screen.Budgets.route) {
                    FloatingActionButton(
                        onClick = {
                            if (route == Screen.Budgets.route) {
                                navController.navigate(Screen.AddBudget.route)
                            } else {
                                navController.navigate(Screen.AddTransaction.route)
                            }
                        },
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (route == Screen.Budgets.route) "Add new budget" else "Add new transaction"
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onViewAllTransactions = {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Transactions.route) {
                    TransactionHistoryScreen(snackbarHostState = snackbarHostState)
                }

                composable(Screen.Budgets.route) {
                    BudgetsScreen(
                        onCardClick = { budgetId ->
                            navController.navigate(Screen.BudgetDetail.createRoute(budgetId))
                        },
                        onAddBudget = { navController.navigate(Screen.AddBudget.route) }
                    )
                }

                composable(Screen.Forecast.route) {
                    ForecastScreen()
                }

                composable(Screen.Copilot.route) {
                    CopilotScreen()
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Help.route) { HelpScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Contact.route) { ContactUsScreen(onBack = { navController.popBackStack() }) }

                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(
                        onSave = { newTx ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Transaction added!", "View")
                            }
                        },
                        onDismiss = { navController.popBackStack() },
                        snackbarHostState = snackbarHostState
                    )
                }

                // ── Budget Detail ──
                composable(
                    route = Screen.BudgetDetail.route,
                    arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
                    val budgetVm: BudgetViewModel = viewModel(
                        factory = BudgetViewModelFactory(
                            BudgetRepository(AppDatabase.getDatabase(navController.context).budgetDao())
                        )
                    )
                    BudgetDetailScreen(
                        viewModel = budgetVm,
                        budgetId = budgetId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── Add Budget ──
                composable(Screen.AddBudget.route) {
                    val budgetVm: BudgetViewModel = viewModel(
                        factory = BudgetViewModelFactory(
                            BudgetRepository(AppDatabase.getDatabase(navController.context).budgetDao())
                        )
                    )
                    AddBudgetScreen(
                        viewModel = budgetVm,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────
// Top App Bar
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpendTrendTopBar(
    currentRoute: String?,
    onDrawerOpen: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = screenIcon(currentRoute),
                    contentDescription = "Current screen icon",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(Dimens.IconLg)
                )
                Text(
                    text = screenTitle(currentRoute),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            ProfileAvatar(
                initials = "N",
                onClick = onDrawerOpen,
                modifier = Modifier.padding(end = Dimens.SpacingLg)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface.copy(alpha = 0.85f),
            titleContentColor = colorScheme.onSurface,
            actionIconContentColor = colorScheme.onSurface
        )
    )
}

// ────────────────────────────────────────────────
// Bottom Navigation Bar
// ────────────────────────────────────────────────
@Composable
private fun SpendTrendBottomBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = colorScheme.surface.copy(alpha = 0.90f),
        tonalElevation = Dimens.ElevationNone,
        contentColor = colorScheme.onSurfaceVariant
    ) {
        listOf(
            BottomNavItem.Dashboard,
            BottomNavItem.Transactions,
            BottomNavItem.Budgets,
            BottomNavItem.Forecast,
            BottomNavItem.Copilot
        ).forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.contentDescription
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimary,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    selectedTextColor = colorScheme.primary,
                    indicatorColor = colorScheme.primary
                )
            )
        }
    }
}

// ────────────────────────────────────────────────
// Drawer Content
// ────────────────────────────────────────────────
@Composable
private fun DrawerContent(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = colorScheme.background,
        drawerContentColor = colorScheme.onSurface
    ) {
        DrawerHeader()
        HorizontalDivider(thickness = 1.dp)

        DrawerItem(Icons.Default.Person, "Profile", "Open profile screen") {
            onNavigate(Screen.Profile.route)
        }

        DrawerItem(Icons.Default.Settings, "Settings", "Open settings") {
            onNavigate(Screen.Settings.route)
        }

        DrawerItem(Icons.Default.Help, "Help", "Open help and FAQ") {
            onNavigate(Screen.Help.route)
        }

        DrawerItem(Icons.Default.Email, "Contact Us", "Open contact form") {
            onNavigate(Screen.Contact.route)
        }

        HorizontalDivider(thickness = 1.dp)
        Spacer(Modifier.height(Dimens.SpacingSm))

        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = Dimens.SpacingSm)
        )

        ThemeOption("Light", ThemeMode.LIGHT)
        ThemeOption("Dark", ThemeMode.DARK)
        ThemeOption("System", ThemeMode.SYSTEM)

        Spacer(Modifier.weight(1f))
        DrawerItem(
            icon = Icons.Default.Logout,
            label = "Log Out",
            contentDesc = "Log out of the app",
            color = colorScheme.error,
            onClick = onLogout
        )
    }
}

// ────────────────────────────────────────────────
// Reusable Sub-components
// ────────────────────────────────────────────────

@Composable
private fun ProfileAvatar(
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Primary, Secondary)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Primary, Secondary))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "N",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text("Nukul", style = MaterialTheme.typography.titleLarge)
        Text(
            "nukul@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    contentDesc: String,
    color: Color = colorScheme.onSurface,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = contentDesc, tint = color) },
        label = { Text(label, color = color) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
private fun ThemeOption(label: String, mode: ThemeMode) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = ThemePreferences.themeMode == mode,
        onClick = { ThemePreferences.updateTheme(mode) },
        icon = {
            Icon(
                imageVector = when (mode) {
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                },
                contentDescription = "Switch to $label theme"
            )
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

// ────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────

private fun screenTitle(route: String?): String = when (route) {
    Screen.Dashboard.route      -> "Dashboard"
    Screen.Transactions.route   -> "Transactions"
    Screen.Budgets.route        -> "Budgets"
    Screen.Forecast.route       -> "Forecast"
    Screen.Copilot.route        -> "Copilot"
    Screen.AddTransaction.route -> "Add Transaction"
    Screen.BudgetDetail.route   -> "Budget Detail"
    Screen.AddBudget.route      -> "Add Budget"
    Screen.Profile.route        -> "Profile"
    Screen.Settings.route       -> "Settings"
    Screen.Help.route           -> "Help"
    Screen.Contact.route        -> "Contact Us"
    else                        -> "SpendTrend"
}

private fun screenIcon(route: String?): ImageVector = when (route) {
    Screen.Dashboard.route      -> Icons.Default.Dashboard
    Screen.Transactions.route   -> Icons.Default.ReceiptLong
    Screen.Budgets.route        -> Icons.Default.Savings
    Screen.Forecast.route       -> Icons.Default.AutoGraph
    Screen.Copilot.route        -> Icons.Default.SmartToy
    Screen.AddTransaction.route -> Icons.Default.Add
    Screen.BudgetDetail.route   -> Icons.Default.Savings
    Screen.AddBudget.route      -> Icons.Default.Add
    Screen.Settings.route       -> Icons.Default.Settings
    Screen.Profile.route        -> Icons.Default.Person
    Screen.Help.route           -> Icons.Default.Help
    Screen.Contact.route        -> Icons.Default.Email
    else                        -> Icons.Default.Info
}