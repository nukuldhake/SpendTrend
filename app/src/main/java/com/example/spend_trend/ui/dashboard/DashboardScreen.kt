package com.example.spend_trend.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassChip
import com.example.spend_trend.ui.components.GradientCard
import com.example.spend_trend.ui.theme.*
import androidx.compose.runtime.collectAsState

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
        // ── Gradient Hero Balance Card ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -60 }
            ) {
                HeroBalanceCard(balance, summary.income, summary.expense)
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
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Today,
                                    contentDescription = "Today's spending",
                                    tint = Primary,
                                    modifier = Modifier.size(Dimens.IconMd)
                                )
                            }
                            Spacer(Modifier.width(Dimens.SpacingMd))
                            Column {
                                Text(
                                    "Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (todaySpend >= 0) "+₹${todaySpend.formatWithComma()}"
                                    else "−₹${(-todaySpend).formatWithComma()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (todaySpend >= 0) IncomeGreen else ExpenseRose
                                )
                            }
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { budgetProgress },
                                modifier = Modifier.size(40.dp),
                                color = Primary,
                                trackColor = Primary.copy(alpha = 0.15f),
                                strokeWidth = 5.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.width(Dimens.SpacingMd))
                            Column {
                                Text(
                                    "Budget",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(budgetProgress * 100).toInt()}% used",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Last 6 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.SpacingLg))
                    PremiumSparkLine(
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

        // ── Motivational Tip ──
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                MotivationalTip()
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
                    "Recent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onViewAllTransactions) {
                    Text("View all", color = Primary)
                }
            }
        }

        // ── Transaction List ──
        if (recentTx.isEmpty()) {
            item { EmptyState() }
        } else {
            itemsIndexed(recentTx, key = { index, item -> "dashboard_${item.id}_${index}" }) { _, tx ->
                GlassTransactionRow(tx)
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Hero Balance Card — Gradient
// ════════════════════════════════════════════════
@Composable
private fun HeroBalanceCard(balance: Int, income: Int, expense: Int) {
    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        brush = GradientPalette.EmeraldTeal
    ) {
        Text(
            "Available Balance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.80f)
        )
        Spacer(Modifier.height(Dimens.SpacingXs))
        Text(
            text = "₹${balance.formatWithComma()}",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Spacer(Modifier.height(Dimens.SpacingXxl))

        // Income / Expense pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            // Income pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Income",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(Dimens.IconSm)
                    )
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Column {
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            "₹${income.formatWithComma()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Expense pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingMd)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Expense",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(Dimens.IconSm)
                    )
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Column {
                        Text(
                            "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            "₹${expense.formatWithComma()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Stat Card (Income / Expense)
// ════════════════════════════════════════════════
@Composable
private fun StatCard(
    label: String,
    amount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = "$label indicator",
                    tint = accentColor,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${amount.formatWithComma()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

// ════════════════════════════════════════════════
//  Premium Bézier Spark Line
// ════════════════════════════════════════════════
@Composable
private fun PremiumSparkLine(data: List<Int>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val max = data.maxOrNull() ?: 0
    val min = data.minOrNull() ?: 0
    val range = (max - min).coerceAtLeast(1)

    val lineColor = Primary
    val glowColor = Primary.copy(alpha = 0.35f)
    val fillBrush = Brush.verticalGradient(
        colors = listOf(Primary.copy(alpha = 0.20f), Color.Transparent)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, v ->
            val x = i * stepX
            val y = h - ((v - min) / range.toFloat() * (h * 0.85f)) - h * 0.05f
            x to y
        }

        // Build smooth Bézier path
        val linePath = Path().apply {
            moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cpx = (prev.first + curr.first) / 2f
                cubicTo(cpx, prev.second, cpx, curr.second, curr.first, curr.second)
            }
        }

        // Gradient fill
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().first, h)
            lineTo(points.first().first, h)
            close()
        }
        drawPath(path = fillPath, brush = fillBrush)

        // Glow line (wider, transparent)
        drawPath(
            path = linePath,
            color = glowColor,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Main line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = Dimens.ChartStrokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Glow dots at each point
        points.forEach { (x, y) ->
            drawCircle(color = glowColor, radius = 8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
            drawCircle(color = lineColor, radius = Dimens.ChartDotRadius.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
        }
    }
}

// ════════════════════════════════════════════════
//  Glass Transaction Row
// ════════════════════════════════════════════════
@Composable
private fun GlassTransactionRow(tx: DashboardTx) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = Dimens.RadiusSm
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(tx.category),
                    contentDescription = "${tx.category} category",
                    tint = Primary,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    tx.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (tx.amount < 0) "−₹${(-tx.amount).formatWithComma()}" else "+₹${tx.amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.amount < 0) ExpenseRose else IncomeGreen
            )
        }
    }
}

// ════════════════════════════════════════════════
//  Motivational Tip
// ════════════════════════════════════════════════
@Composable
private fun MotivationalTip() {
    val tips = listOf(
        "You saved ₹1,200 more this month… keep going ♡",
        "Small steps today lead to big freedom tomorrow…",
        "Every rupee tracked is a rupee controlled…",
        "One mindful choice at a time… you've got this…"
    )
    val randomTip = remember { tips.random() }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Tertiary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("💡", fontSize = 18.sp)
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(
                text = randomTip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ════════════════════════════════════════════════
//  Empty State
// ════════════════════════════════════════════════
@Composable
private fun EmptyState() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingHuge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = "No transactions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(Dimens.IconHero)
            )
            Spacer(Modifier.height(Dimens.SpacingLg))
            Text(
                "No transactions yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Add your first transaction to see insights",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UpcomingBillAlert(count: Int, total: Int) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ExpenseRose.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = ExpenseRose,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$count Upcoming Bills",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRose
                )
                Text(
                    "Total amount: ₹${total.formatWithComma()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun Int.formatWithComma(): String = "%,d".format(this)

// ════════════════════════════════════════════════
//  Factory & Helpers
// ════════════════════════════════════════════════
// Factory removed — moved to DashboardViewModel.kt