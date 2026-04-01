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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
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
    val goalsProgress by viewModel.goalsProgress.collectAsState(initial = 0f)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = Dimens.BottomNavClearance)
    ) {
        // ── Top Bar Header (Profile & Greeting) ──
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500))) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.width(Dimens.SpacingSm))
                        Column {
                            val userName = com.example.spend_trend.data.UserPreferences.getName() ?: "User"
                            Text("Hi $userName,", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("Welcome back", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(Dimens.AvatarMd)
                            .background(Primary, androidx.compose.foundation.shape.CircleShape)
                            .border(Dimens.BorderWidthStandard, MonoBlack, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MonoBlack)
                    }
                }
            }
        }

        // ── Curvy Hero Balance Card ──
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -60 }) {
                HeroBalanceCard(
                    balance = balance,
                    trendPct = trendData.first,
                    trendVal = trendData.second
                )
            }
        }

        // ── Upcoming Bills Alert ──
        if (pendingBills.isNotEmpty()) {
            val billsToNotify = pendingBills.filter {
                it.dueDateMillis <= System.currentTimeMillis() + (86400000 * 3) // Due within 3 days
            }
            if (billsToNotify.isNotEmpty()) {
                item {
                    UpcomingBillAlert(billsToNotify.size, billsToNotify.sumOf { it.amount })
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
                    ActionSquircle(label = "Add Tx", icon = Icons.Default.Add, backgroundColor = CategoryColors.Green, onClick = onNavigateToAddTx, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Bills", icon = Icons.Default.Receipt, backgroundColor = CategoryColors.Yellow, onClick = onNavigateToBills, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Goals", icon = Icons.Default.Star, backgroundColor = CategoryColors.Purple, onClick = onNavigateToGoals, modifier = Modifier.weight(1f))
                    ActionSquircle(label = "Analytics", icon = Icons.Default.PieChart, backgroundColor = CategoryColors.Orange, onClick = onNavigateToAnalytics, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Side-by-Side Insights (Graph & Goals) ──
        item {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 200))) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), // Important to match heights
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    BlockCard(modifier = Modifier.weight(1f).fillMaxHeight(), hasShadow = false) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Activity", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.weight(1f))
                        NoirSparkLine(data = monthlyTrend, modifier = Modifier.height(60.dp).fillMaxWidth().padding(vertical = Dimens.SpacingSm))
                        Spacer(Modifier.weight(1f))
                        Text("₹${expense.formatWithComma()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("↗ +1.7%", style = MaterialTheme.typography.labelSmall, color = MonoBlack.copy(alpha = 0.6f))
                    }

                    BlockCard(modifier = Modifier.weight(1f).fillMaxHeight(), hasShadow = false) {
                        Text("Goals\nAchieved", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("${(goalsProgress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(MonoGrayLight, androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(goalsProgress)
                                    .fillMaxHeight()
                                    .background(MonoBlack, androidx.compose.foundation.shape.CircleShape)
                            )
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
            hasShadow = false
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
//  Noir Spark Line (Sharp Corners)
// ════════════════════════════════════════════════
@Composable
private fun NoirSparkLine(data: List<Int>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val max = data.maxOrNull() ?: 0
    val min = data.minOrNull() ?: 0
    val range = (max - min).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, v ->
            val norm = (v - min) / range.toFloat()
            androidx.compose.ui.geometry.Offset(
                x = i * stepX,
                y = h - (norm * h)
            )
        }

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = Primary,
            style = Stroke(width = Dimens.ChartStrokeWidth.value.dp.toPx(), cap = StrokeCap.Square)
        )

        // Draw data points as squares (accent)
        points.forEach { pt ->
            drawRect(
                color = Primary,
                topLeft = androidx.compose.ui.geometry.Offset(pt.x - 5.dp.toPx(), pt.y - 5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx())
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