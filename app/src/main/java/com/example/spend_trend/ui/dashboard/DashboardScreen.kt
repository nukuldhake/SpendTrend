package com.example.spend_trend.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.GoalEntity
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*

@Composable
fun DashboardScreen(
    onViewAllTransactions: () -> Unit = {},
    onNavigateToAddTx: () -> Unit = {},
    onNavigateToBills: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToForecast: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            txRepository = TransactionRepository(db.transactionDao()),
            budgetRepository = BudgetRepository(db.budgetDao()),
            billRepository = com.example.spend_trend.data.repository.BillRepository(db.billDao()),
            goalRepository = com.example.spend_trend.data.repository.GoalRepository(db.goalDao())
        )
    )

    val recentTx by viewModel.recentTransactions.collectAsState(initial = emptyList())
    val summary by viewModel.currentMonthSummary.collectAsState(initial = MonthSummary(0, 0, 0))
    val monthlyTrend by viewModel.monthlyTrend.collectAsState(initial = emptyList())
    val pendingBills by viewModel.pendingBills.collectAsState(initial = emptyList())
    val totalBalance by viewModel.totalBalance.collectAsState()
    
    val balance = totalBalance
    val expense = summary.expense

    val trendData = remember(monthlyTrend) {
        if (monthlyTrend.size >= 2) {
            val current = monthlyTrend.last()
            val previous = monthlyTrend[monthlyTrend.size - 2]
            if (previous > 0) {
                val pct = ((current - previous).toFloat() / previous * 100).toInt()
                val diff = current - previous
                Pair(if (pct >= 0) "+$pct%" else "$pct%", if (diff >= 0) "+₹${diff.formatWithComma()}" else "-₹${(-diff).formatWithComma()}")
            } else Pair("0%", "₹0")
        } else Pair("0%", "₹0")
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    var selectedWeekIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            com.example.spend_trend.ui.components.BlockTopBar(
                title = "SPENDTREND",
                onMenuClick = onOpenDrawer,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(Dimens.AvatarSm)
                            .background(Primary, androidx.compose.foundation.shape.CircleShape)
                            .border(Dimens.BorderWidthStandard, MonoBlack, androidx.compose.foundation.shape.CircleShape)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MonoBlack, modifier = Modifier.size(16.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
            contentPadding = PaddingValues(top = Dimens.SpacingMd, bottom = Dimens.BottomNavClearance)
        ) {
            // ── Greeting Item ──
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(500))) {
                    Column {
                        val userName = com.example.spend_trend.data.UserPreferences.getName() ?: "User"
                        Text("HI $userName,", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("WELCOME BACK", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                    }
                }
            }

            // ── Curvy Hero Balance & Quick Add Row ──
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -60 }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                    ) {
                        Box(modifier = Modifier.weight(0.72f).fillMaxHeight()) {
                            HeroBalanceCard(
                                balance = balance,
                                trendPct = trendData.first,
                                trendVal = trendData.second
                            )
                        }
                        
                        BlockCard(
                            modifier = Modifier.weight(0.28f).fillMaxHeight().clickable(onClick = onNavigateToAddTx),
                            backgroundColor = Primary,
                            borderColor = MonoBlack,
                            hasShadow = true
                        ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(Icons.Default.Add, "Add Transaction", tint = MonoBlack, modifier = Modifier.size(Dimens.IconLg))
                            Spacer(Modifier.height(Dimens.SpacingXs))
                            Text("ADD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // ── Upcoming Bills Alert ──
        if (pendingBills.isNotEmpty()) {
            val billsToNotify = pendingBills.filter {
                it.dueDateMillis <= System.currentTimeMillis() + (86400000 * 3) // Due within 3 days
            }
            if (billsToNotify.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.clickable(onClick = onNavigateToBills)) {
                        UpcomingBillAlert(billsToNotify.size, billsToNotify.sumOf { it.amount })
                    }
                }
            }
        }

        // ── Action Grid (4 distinct curvy buttons) ──
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { -30 }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    ActionSquircle(label = "Forecast", icon = Icons.AutoMirrored.Filled.TrendingUp, backgroundColor = CategoryColors.Green, onClick = onNavigateToForecast, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Bills", icon = Icons.AutoMirrored.Filled.ReceiptLong, backgroundColor = CategoryColors.Yellow, onClick = onNavigateToBills, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Goals", icon = Icons.Default.Stars, backgroundColor = CategoryColors.Purple, onClick = onNavigateToGoals, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Analytics", icon = Icons.Default.PieChart, backgroundColor = CategoryColors.Orange, onClick = onNavigateToAnalytics, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Side-by-Side Insights (Graph & Goals) ──
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 200))) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(200.dp), // Unified professional height
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    BlockCard(
                        modifier = Modifier.weight(1f).fillMaxHeight() // Removed redirecting clickable
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Activity", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(Dimens.SpacingSm))
                        NoirBarChart(
                            data = monthlyTrend,
                            selectedIndex = selectedWeekIndex,
                            onPointClick = { selectedWeekIndex = it },
                            modifier = Modifier.height(100.dp).fillMaxWidth().padding(vertical = Dimens.SpacingXs)
                        )
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        // --- Week Labels ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            monthlyTrend.forEachIndexed { i, _ ->
                                Text(
                                    "W${i + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedWeekIndex == i) Primary else MonoGrayMedium,
                                    fontWeight = if (selectedWeekIndex == i) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(Dimens.SpacingMd))
                        if (selectedWeekIndex != null && selectedWeekIndex!! < monthlyTrend.size) {
                            Text("Week ${selectedWeekIndex!! + 1}: ₹${monthlyTrend[selectedWeekIndex!!].formatWithComma()}", 
                                style = MaterialTheme.typography.labelMedium, 
                                fontWeight = FontWeight.Black, 
                                color = Primary
                            )
                        } else {
                            Text("₹${expense.formatWithComma()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("↗ +1.7% (THIS MONTH)", style = MaterialTheme.typography.labelSmall, color = MonoBlack.copy(alpha = 0.6f))
                        }
                    }
 
                    val goals by viewModel.activeGoals.collectAsState()
                    BlockCard(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Text("Goals", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Dimens.SpacingSm))
                        
                        if (goals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No active goals", style = MaterialTheme.typography.bodySmall, color = MonoGrayMedium)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(goals) { goal ->
                                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(goal.title, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(MonoGrayLight, androidx.compose.foundation.shape.CircleShape)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress)
                                                    .fillMaxHeight()
                                                    .background(MonoBlack, androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Recent Transactions Header ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onViewAllTransactions) {
                    Text("VIEW ALL", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Transaction List ──
        if (recentTx.isEmpty()) {
            item { EmptyState() }
        } else {
            itemsIndexed(recentTx, key = { index, item -> "dashboard_${item.id}_${index}" }) { _, tx ->
                BlockTransactionRow(tx)
            }
        }
    }
    }
}

// ════════════════════════════════════════════════
//  Hero Balance Card — Playful Curvy
// ════════════════════════════════════════════════
@Composable
private fun HeroBalanceCard(
    balance: Int,
    trendPct: String,
    trendVal: String
) {
    BlockCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MonoWhite,
        borderColor = MonoBlack,
        hasShadow = true,
        shadowColor = MonoBlack,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusXl) // Huge Curve!
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(
                    "Your Balance is",
                    style = MaterialTheme.typography.labelMedium,
                    color = MonoBlack.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    text = "₹${balance.formatWithComma()}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MonoBlack,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPositive = !trendPct.startsWith("-")
                    val trendColor = if (isPositive) CategoryColors.Green else ExpenseRose
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, 
                        contentDescription = null, 
                        tint = trendColor, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$trendPct ($trendVal)",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Action Squircle (Grid items)
// ════════════════════════════════════════════════
@Composable
private fun ActionSquircle(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlockCard(
            modifier = Modifier.aspectRatio(1f),
            backgroundColor = backgroundColor,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Circle inside squircle
                Box(
                    modifier = Modifier.size(32.dp).border(1.dp, MonoBlack, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = MonoBlack, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(Dimens.SpacingSm))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ════════════════════════════════════════════════
//  Noir Stat Card
// ════════════════════════════════════════════════
@Composable
private fun StatCard(
    label: String,
    amount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    BlockCard(modifier = modifier) {
        Column {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(Dimens.SpacingSm))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₹${amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}

// ════════════════════════════════════════════════
//  Noir Bar Chart (Sharp & Interactive)
// ════════════════════════════════════════════════
@Composable
private fun NoirBarChart(
    data: List<Int>, 
    selectedIndex: Int? = null,
    onPointClick: (Int) -> Unit = {}, 
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val max = data.maxOrNull() ?: 0
    val range = max.coerceAtLeast(1)

    Canvas(modifier = modifier
        .pointerInput(data) {
            detectTapGestures { offset ->
                val w = size.width.toFloat()
                val barWidthWithGap = w / data.size
                val index = (offset.x / barWidthWithGap).toInt().coerceIn(0, data.size - 1)
                onPointClick(index)
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val barWidthWithGap = w / data.size.toFloat()
        val barWidth = barWidthWithGap * 0.75f
        val gap = barWidthWithGap * 0.25f

        // Draw horizontal grid lines (3 levels)
        repeat(3) { i ->
            val y = (h / 3) * (i + 1)
            drawLine(
                color = MonoGrayLight.copy(alpha = 0.4f),
                start = androidx.compose.ui.geometry.Offset(0f, h - y),
                end = androidx.compose.ui.geometry.Offset(w, h - y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }

        data.forEachIndexed { i, v ->
            val barHeight = (v.toFloat() / range) * h
            val x = i * barWidthWithGap + (gap / 2)
            val y = h - barHeight

            // Draw Bar
            drawRect(
                color = if (selectedIndex == i) Primary else Primary.copy(alpha = 0.3f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )

            // Draw Border for Noir look
            drawRect(
                color = MonoBlack,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

// ════════════════════════════════════════════════
//  Block Transaction Row
// ════════════════════════════════════════════════
@Composable
private fun BlockTransactionRow(tx: DashboardTx) {
    BlockCard(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        backgroundColor = CategoryColors.getColorForCategory(tx.category),
        shape = androidx.compose.foundation.shape.CircleShape // Pill shape for transactions!
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MonoWhite, androidx.compose.foundation.shape.CircleShape)
                    .border(Dimens.BorderWidthStandard, MonoBlack, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(tx.category),
                    contentDescription = null,
                    tint = MonoBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MonoBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(tx.category, style = MaterialTheme.typography.labelSmall, color = MonoBlack.copy(alpha = 0.7f))
            }
            Text(
                text = if (tx.amount < 0) "−₹${(-tx.amount).formatWithComma()}" else "+₹${tx.amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MonoBlack
            )
        }
    }
}

@Composable
private fun MotivationalTip(tipText: String) {
    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Text("!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Primary)
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(
                text = tipText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingHuge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(Dimens.SpacingLg))
            Text("NO DATA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun UpcomingBillAlert(count: Int, total: Int) {
    BlockCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ExpenseRose,
        borderColor = MonoBlack
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = MonoWhite)
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column {
                Text("$count BILLS DUE", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MonoWhite)
                Text("TOTAL: ₹${total.formatWithComma()}", style = MaterialTheme.typography.labelSmall, color = MonoWhite.copy(alpha = 0.8f))
            }
        }
    }
}



// ════════════════════════════════════════════════
//  Factory & Helpers
// ════════════════════════════════════════════════
// Factory removed — moved to DashboardViewModel.kt