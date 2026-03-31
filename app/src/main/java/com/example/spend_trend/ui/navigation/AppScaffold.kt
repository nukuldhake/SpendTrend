package com.example.spend_trend.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.ui.bills.BillsScreen
import com.example.spend_trend.ui.bills.AddBillScreen
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
import com.example.spend_trend.ui.analytics.AnalyticsScreen
import com.example.spend_trend.ui.goals.GoalScreen
import com.example.spend_trend.ui.theme.*
import com.example.spend_trend.ui.auth.*
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.data.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Auth State
    var isLoggedIn by remember { mutableStateOf(UserPreferences.isLoggedIn()) }
    var isRegistered by remember { mutableStateOf(UserPreferences.isRegistered()) }
    var hasPin by remember { mutableStateOf(UserPreferences.hasPin()) }
    
    val isAuthScreen = currentDestination?.route in listOf(Screen.Register.route, Screen.Login.route, Screen.PinSetup.route)
    
    val startDestination = when {
        !isRegistered -> Screen.Register.route
        !hasPin -> Screen.PinSetup.route
        !isLoggedIn -> Screen.Login.route
        else -> Screen.Dashboard.route
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthScreen,
        drawerContent = {
            DrawerContent(
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onLogout = { 
                    scope.launch { drawerState.close() }
                    UserPreferences.setLoggedIn(false)
                    isLoggedIn = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!isAuthScreen) {
                    SpendTrendTopBar(
                        currentRoute = currentDestination?.route,
                        onDrawerOpen = { scope.launch { drawerState.open() } }
                    )
                }
            },
            bottomBar = {
                if (!isAuthScreen) {
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
                }
            },
            floatingActionButton = {
                val route = currentDestination?.route
                if (route == Screen.Dashboard.route || route == Screen.Budgets.route || route == Screen.Bills.route) {
                    NeumorphicFab(
                        icon = Icons.Default.Add,
                        onClick = {
                            when (route) {
                                Screen.Budgets.route -> navController.navigate(Screen.AddBudget.route)
                                Screen.Bills.route -> navController.navigate(Screen.AddBill.route)
                                else -> navController.navigate(Screen.AddTransaction.route)
                            }
                        }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(if (isAuthScreen) PaddingValues(0.dp) else innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                // Auth Routes
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = { 
                            isRegistered = true
                            navController.navigate(Screen.PinSetup.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                    )
                }

                composable(Screen.PinSetup.route) {
                    PinSetupScreen(
                        onSetupComplete = {
                            hasPin = true
                            UserPreferences.setLoggedIn(true)
                            isLoggedIn = true
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.PinSetup.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            UserPreferences.setLoggedIn(true)
                            isLoggedIn = true
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                    )
                }

                // Main Routes
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

                composable(Screen.Forecast.route) { ForecastScreen() }
                composable(Screen.Copilot.route) { CopilotScreen() }
                composable(Screen.Goals.route) { GoalScreen() }
                composable(Screen.Analytics.route) { AnalyticsScreen() }
                composable(Screen.Bills.route) { BillsScreen() }
                composable(Screen.AddBill.route) { 
                    val billViewModel: com.example.spend_trend.ui.bills.BillViewModel = viewModel(
                        factory = com.example.spend_trend.ui.bills.BillViewModelFactory(
                            billRepo = com.example.spend_trend.data.repository.BillRepository(AppDatabase.getDatabase(context).billDao()),
                            txRepo = com.example.spend_trend.data.repository.TransactionRepository(AppDatabase.getDatabase(context).transactionDao())
                        )
                    )
                    AddBillScreen(viewModel = billViewModel, onBack = { navController.popBackStack() }) 
                }
                composable(Screen.Profile.route) { ProfileScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Settings.route) { SettingsScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Help.route) { HelpScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Contact.route) { ContactUsScreen(onBack = { navController.popBackStack() }) }

                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(
                        onSave = { _ ->
                            coroutineScope.launch { snackbarHostState.showSnackbar("Transaction added!", "View") }
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
                    val budgetVm: BudgetViewModel = viewModel(
                        factory = BudgetViewModelFactory(BudgetRepository(AppDatabase.getDatabase(navController.context).budgetDao()))
                    )
                    BudgetDetailScreen(viewModel = budgetVm, budgetId = budgetId, onBack = { navController.popBackStack() })
                }

                composable(Screen.AddBudget.route) {
                    val budgetVm: BudgetViewModel = viewModel(
                        factory = BudgetViewModelFactory(BudgetRepository(AppDatabase.getDatabase(navController.context).budgetDao()))
                    )
                    AddBudgetScreen(viewModel = budgetVm, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun SpendTrendTopBar(currentRoute: String?, onDrawerOpen: () -> Unit) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        cornerRadius = 0.dp,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = screenIcon(currentRoute),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(Dimens.IconLg)
                )
                Text(
                    text = screenTitle(currentRoute),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            ProfileAvatar(name = UserPreferences.getName() ?: "G", onClick = onDrawerOpen)
        }
    }
}

@Composable
private fun SpendTrendBottomBar(currentDestination: androidx.navigation.NavDestination?, onNavigate: (String) -> Unit) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth().height(84.dp),
        cornerRadius = 0.dp,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                BottomNavItem.Dashboard,
                BottomNavItem.Transactions,
                BottomNavItem.Budgets,
                BottomNavItem.Analytics,
                BottomNavItem.Copilot
            ).forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NeumorphicCard(
                        modifier = Modifier.size(48.dp),
                        cornerRadius = 14.dp,
                        elevation = if (isSelected) 0.dp else 2.dp,
                        isConcave = isSelected,
                        backgroundColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
    ) {
        DrawerHeader()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingLg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            DrawerSectionHeader("Financials")
            DrawerItem(Icons.Default.Stars, "Goals", "Savings targets") { onNavigate(Screen.Goals.route) }
            DrawerItem(Icons.Default.Receipt, "Bills", "Upcoming payments") { onNavigate(Screen.Bills.route) }
            DrawerItem(Icons.AutoMirrored.Filled.TrendingUp, "Forecast", "Future trends") { onNavigate(Screen.Forecast.route) }
            
            Spacer(Modifier.height(Dimens.SpacingMd))
            DrawerSectionHeader("System")
            DrawerItem(Icons.Default.Person, "Profile", "User profile") { onNavigate(Screen.Profile.route) }
            DrawerItem(Icons.Default.Settings, "Settings", "Preferences") { onNavigate(Screen.Settings.route) }
            DrawerItem(Icons.AutoMirrored.Filled.Help, "Help", "FAQ") { onNavigate(Screen.Help.route) }
            DrawerItem(Icons.Default.Email, "Contact Us", "Support") { onNavigate(Screen.Contact.route) }

            Spacer(Modifier.height(Dimens.SpacingMd))
            DrawerSectionHeader("Appearance")
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                ThemeOption("Light", ThemeMode.LIGHT, Modifier.weight(1f))
                ThemeOption("Dark", ThemeMode.DARK, Modifier.weight(1f))
            }
            ThemeOption("System", ThemeMode.SYSTEM, Modifier.fillMaxWidth())

            Spacer(Modifier.weight(1f))
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onLogout() },
                cornerRadius = 16.dp,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ExpenseRose)
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text("Log Out", color = ExpenseRose, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = Dimens.SpacingSm, bottom = Dimens.SpacingXs)
    )
}

@Composable
private fun ProfileAvatar(name: String, onClick: () -> Unit) {
    val initials = name.take(1).uppercase().ifEmpty { "?" }
    NeumorphicCard(
        modifier = Modifier.size(44.dp).clickable(onClick = onClick),
        cornerRadius = 22.dp,
        elevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    val name = UserPreferences.getName() ?: "Guest"
    val initials = name.take(1).uppercase().ifEmpty { "G" }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NeumorphicCard(
            modifier = Modifier.size(100.dp),
            cornerRadius = 50.dp,
            elevation = 12.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    initials,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            UserPreferences.getEmail() ?: "Welcome to SpendTrend",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, contentDesc: String, onClick: () -> Unit) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth().height(52.dp).clickable { onClick() },
        cornerRadius = 14.dp,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDesc, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ThemeOption(label: String, mode: ThemeMode, modifier: Modifier = Modifier) {
    val isSelected = ThemePreferences.themeMode == mode
    NeumorphicCard(
        modifier = modifier.height(44.dp).clickable { ThemePreferences.updateTheme(mode) },
        cornerRadius = 10.dp,
        elevation = if (isSelected) 0.dp else 2.dp,
        isConcave = isSelected,
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun NeumorphicFab(icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    NeumorphicCard(
        modifier = Modifier
            .size(64.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        cornerRadius = 32.dp,
        elevation = if (isPressed) 0.dp else 12.dp,
        isConcave = isPressed,
        backgroundColor = Primary // Vibrant accent for FAB
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun screenTitle(route: String?): String = when (route) {
    Screen.Dashboard.route -> "Dashboard"
    Screen.Transactions.route -> "Transactions"
    Screen.Budgets.route -> "Budgets"
    Screen.Forecast.route -> "Forecast"
    Screen.Copilot.route -> "Copilot"
    Screen.Goals.route -> "Goals"
    Screen.Analytics.route -> "Analytics"
    Screen.Profile.route -> "Profile"
    Screen.Settings.route -> "Settings"
    else -> "SpendTrend"
}

private fun screenIcon(route: String?): ImageVector = when (route) {
    Screen.Dashboard.route -> Icons.Default.Dashboard
    Screen.Transactions.route -> Icons.AutoMirrored.Filled.ReceiptLong
    Screen.Budgets.route -> Icons.Default.Savings
    Screen.Forecast.route -> Icons.Default.AutoGraph
    Screen.Analytics.route -> Icons.Default.PieChart
    Screen.Copilot.route -> Icons.Default.SmartToy
    Screen.Goals.route -> Icons.Default.Stars
    Screen.Settings.route -> Icons.Default.Settings
    Screen.Profile.route -> Icons.Default.Person
    else -> Icons.Default.Info
}