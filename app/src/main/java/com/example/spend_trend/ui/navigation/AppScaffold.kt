package com.example.spend_trend.ui.navigation

import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
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
import kotlinx.coroutines.launch
import com.example.spend_trend.ui.theme.ThemePreferences
import com.example.spend_trend.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                DrawerHeader()
                HorizontalDivider(thickness = 1.dp)

                DrawerItem(Icons.Default.Person, "Profile") {
                    scope.launch { drawerState.close() }
                    navController.navigate("profile")
                }

                DrawerItem(Icons.Default.Settings, "Settings") {
                    scope.launch { drawerState.close() }
                    navController.navigate("settings")
                }

                DrawerItem(Icons.Default.Help, "Help") {
                    scope.launch { drawerState.close() }
                    navController.navigate("help")
                }

                DrawerItem(Icons.Default.Email, "Contact Us") {
                    scope.launch { drawerState.close() }
                    navController.navigate("contact")
                }

                HorizontalDivider(thickness = 1.dp)
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )

                ThemeOption("Light", ThemeMode.LIGHT)
                ThemeOption("Dark", ThemeMode.DARK)
                ThemeOption("System", ThemeMode.SYSTEM)

                Spacer(Modifier.weight(1f))
                DrawerItem(Icons.Default.Logout, "Log Out", color = MaterialTheme.colorScheme.error) {
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Screen-specific icon with primary color
                            Icon(
                                imageVector = when (currentDestination?.route) {
                                    BottomNavItem.Dashboard.route -> Icons.Default.Dashboard
                                    BottomNavItem.Transactions.route -> Icons.Default.ReceiptLong
                                    BottomNavItem.Budgets.route -> Icons.Default.Savings
                                    BottomNavItem.Forecast.route -> Icons.Default.AutoGraph
                                    BottomNavItem.Copilot.route -> Icons.Default.SmartToy
                                    "settings" -> Icons.Default.Settings
                                    "profile" -> Icons.Default.Person
                                    "help" -> Icons.Default.Help
                                    "contact" -> Icons.Default.Email

                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )

                            Text(
                                text = getScreenTitle(currentDestination?.route),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions ={
                        ProfileAvatar(
                            initials = "N",
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,  // ← same shade as bottom bar (your request)
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.contentDescription
                                )
                            },
                            label = { Text(item.label) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentDestination?.route == BottomNavItem.Dashboard.route) {
                    FloatingActionButton(
                        onClick = { navController.navigate("add_transaction") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new transaction"
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Dashboard.route) {
                    DashboardScreen(
                        onViewAllTransactions = {
                            navController.navigate(BottomNavItem.Transactions.route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(BottomNavItem.Transactions.route) {
                    TransactionHistoryScreen(snackbarHostState = snackbarHostState)
                }

                composable(BottomNavItem.Budgets.route) {
                    BudgetsScreen()
                }

                composable(BottomNavItem.Forecast.route) {
                    ForecastScreen()
                }

                composable(BottomNavItem.Copilot.route) {
                    CopilotScreen()
                }

                composable("profile") {
                    ProfileScreen()
                }

                composable("settings") {
                    SettingsScreen()
                }

                composable("help") { HelpScreen() }
                composable("contact") { ContactUsScreen() }

                composable("add_transaction") {
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
            }
        }
    }
}

// Circular profile avatar (placeholder)
@Composable
private fun ProfileAvatar(
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primaryContainer),
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Drawer header with larger avatar
@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            initials = "N",
            onClick = {},
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Nukul", style = MaterialTheme.typography.titleLarge)
        Text(
            "nukul@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Reusable drawer menu item
@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label, tint = color) },
        label = { Text(label, color = color) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

// Theme switcher item
@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = ThemePreferences.themeMode == mode,
        onClick = {
            ThemePreferences.themeMode = mode
        },
        icon = {
            Icon(
                imageVector = when (mode) {
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                },
                contentDescription = null
            )
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

// Dynamic screen title
private fun getScreenTitle(route: String?): String = when (route) {
    BottomNavItem.Dashboard.route -> "Dashboard"
    BottomNavItem.Transactions.route -> "Transactions"
    BottomNavItem.Budgets.route -> "Budgets"
    BottomNavItem.Forecast.route -> "Forecast"
    BottomNavItem.Copilot.route -> "Copilot"
    "add_transaction" -> "Add Transaction"
    "profile" -> "Profile"
    "settings" -> "Settings"
    "help"-> "Help"
    "contact" -> "Contact Us"
    else -> "SpendTrend"
}