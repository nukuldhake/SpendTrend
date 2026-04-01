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
    onViewAllTransactions: () -> Unit = {}
) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            txRepository = TransactionRepository(db.transactionDao()),
            budgetRepository = BudgetRepository(db.budgetDao()),
            billRepository = com.example.spend_trend.data.repository.BillRepository(db.billDao())
        )
    )

    val recentTx by viewModel.recentTransactions.collectAsState(initial = emptyList())
    val summary by viewModel.currentMonthSummary.collectAsState(initial = MonthSummary(0, 0, 0))
    val todaySpend by viewModel.todayTransactions.collectAsState(initial = 0)
    val monthlyTrend by viewModel.monthlyTrend.collectAsState(initial = emptyList())
    val budgetProgress by viewModel.budgetProgress.collectAsState(initial = 0f)
    val pendingBills by viewModel.pendingBills.collectAsState(initial = emptyList())
    val motivationalTip by viewModel.motivationalTip.collectAsState()
    val balance = summary.net

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = Dimens.BottomNavClearance)
    ) {
        // ── Noir Hero Balance Card ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -60 }
            ) {
                HeroBalanceCard(
                    balance = balance,
                    income = summary.income,
                    expense = summary.expense,
                    onIncomeClick = onViewAllTransactions,
                    onExpenseClick = onViewAllTransactions
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

        // ── Quick Stats Row (Today + Budget) ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { -30 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    BlockCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Today,
                                contentDescription = "Today's spending",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(Dimens.SpacingSm))
                            Column {
                                Text(
                                    "TODAY".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (todaySpend >= 0) "+₹${todaySpend.formatWithComma()}"
                                    else "−₹${(-todaySpend).formatWithComma()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (todaySpend >= 0) IncomeGreen else ExpenseRose,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    BlockCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${(budgetProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(Modifier.width(Dimens.SpacingSm))
                            Column {
                                Text(
                                    "BUDGET".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "USED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Spending Trend Chart ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 200))
            ) {
                BlockCard(modifier = Modifier.fillMaxWidth(), hasShadow = true) {
                    Text(
                        "TREND".uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "LAST 6 MONTHS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.SpacingLg))
                    NoirSparkLine(
                        data = monthlyTrend,
                        modifier = Modifier
                            .height(Dimens.ChartHeight)
                            .fillMaxWidth()
                    )
                }
            }
        }

        // ── Income / Expense Cards ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 300))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    StatCard(
                        label = "Income",
                        amount = summary.income,
                        icon = Icons.Default.ArrowDownward,
                        accentColor = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Expense",
                        amount = summary.expense,
                        icon = Icons.Default.ArrowUpward,
                        accentColor = ExpenseRose,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Motivational Tip (data-driven) ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                MotivationalTip(tipText = motivationalTip)
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
//  Hero Balance Card — Stark Noir
// ════════════════════════════════════════════════
@Composable
private fun HeroBalanceCard(
    balance: Int,
    income: Int,
    expense: Int,
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {}
) {
    BlockCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MonoBlack,
        borderColor = MonoBlack,
        hasShadow = true,
        shadowColor = Primary
    ) {
        Text(
            "AVAILABLE BALANCE",
            style = MaterialTheme.typography.labelSmall,
            color = MonoWhite.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(Dimens.SpacingXs))
        Text(
            text = "₹${balance.formatWithComma()}",
            style = MaterialTheme.typography.displayMedium,
            color = MonoWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(Dimens.SpacingXxl))

        // Income / Expense pills — 0dp Sharp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            // Income Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MonoWhite.copy(alpha = 0.3f))
                    .clickable(onClick = onIncomeClick)
                    .padding(Dimens.SpacingMd)
            ) {
                Column {
                    Text("INCOME", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                    Text(
                        "₹${income.formatWithComma()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MonoWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expense Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MonoWhite.copy(alpha = 0.3f))
                    .clickable(onClick = onExpenseClick)
                    .padding(Dimens.SpacingMd)
            ) {
                Column {
                    Text("EXPENSE", style = MaterialTheme.typography.labelSmall, color = ExpenseRose)
                    Text(
                        "₹${expense.formatWithComma()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MonoWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(tx.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.title.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    tx.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (tx.amount < 0) "−₹${(-tx.amount).formatWithComma()}" else "+₹${tx.amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (tx.amount < 0) ExpenseRose else IncomeGreen
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