package com.example.spend_trend.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.absoluteValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreen(
    onViewAllTransactions: () -> Unit = {}
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            repository = TransactionRepository(
                AppDatabase.getDatabase(LocalContext.current).transactionDao()
            )
        )
    )

    val recentTx by viewModel.recentTransactions.collectAsState(initial = emptyList())
    val summary by viewModel.currentMonthSummary.collectAsState(initial = MonthSummary(0, 0, 0))
    val todaySpend by viewModel.todayTransactions.collectAsState(initial = 0)
    val monthlyTrend by viewModel.monthlyTrend.collectAsState(initial = emptyList())

    val balance = summary.net

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BalanceCard(balance, summary.income, summary.expense)
            }

            item {
                TodaySpendCard(todaySpend)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }


            item {
                Text(
                    text = "This month",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard(
                        title = "Income",
                        amount = summary.income,
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    InsightCard(
                        title = "Expense",
                        amount = summary.expense,
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.errorContainer
                    )

                }
                Spacer(Modifier.height(22.dp))
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }

            item {
                MiniTrendCard(monthlyTrend)
            }

            item {
                BudgetProgressRing(0.68f)

            }

            item {
                MotivationalTip()
                Spacer(Modifier.height(22.dp))
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent transactions",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onViewAllTransactions) {
                        Text("View all")
                    }
                }

            }

            if (recentTx.isEmpty()) {
                item {
                    EmptyTransactionsPlaceholder()
                }
            } else {
                items(recentTx) { tx ->
                    DashboardTransactionRow(tx)
                    if (tx != recentTx.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TodaySpendCard(todaySpend: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Today,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Today", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = if (todaySpend >= 0) "+₹${todaySpend.formatWithComma()}"
                    else "−₹${(-todaySpend).formatWithComma()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (todaySpend >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Mini Trend Sparkline
@Composable
fun MiniTrendCard(trendData: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spending Trend (last 6 mo)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SparkLineChart(data = trendData, modifier = Modifier.height(80.dp).fillMaxWidth())
        }
    }
}

@Composable
fun SparkLineChart(data: List<Int>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val max = data.maxOrNull() ?: 0
    val min = data.minOrNull() ?: 0
    val range = (max - min).coerceAtLeast(1)

    // Get color here — this is a composable context
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value - min) / range.toFloat() * height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,               // ← safe to use here
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

// Budget Progress Ring (fake % for now)
@Composable
fun BudgetProgressRing(progress: Float = 0.68f) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(60.dp),
                color = when {
                    progress < 0.7f -> MaterialTheme.colorScheme.primary
                    progress < 0.9f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                },
                strokeWidth = 8.dp
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Monthly Budget Used", style = MaterialTheme.typography.titleMedium)
                Text("${(progress * 100).toInt()}% remaining", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Motivational Tip
@Composable
fun MotivationalTip() {
    val tips = listOf(
        "You saved ₹1,200 more this month… keep going ♡",
        "Small steps today lead to big freedom tomorrow…",
        "You're doing better than you think… believe in yourself…",
        "Every rupee tracked is a rupee controlled… you're strong…",
        "One mindful choice at a time… you’ve got this…"
    )
    val randomTip = tips.random()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Text(
            text = randomTip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}
@Composable
fun BalanceCard(
    balance: Int,
    income: Int,
    expense: Int
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Available Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )

            Text(
                text = "₹${balance.toString().reversed().chunked(3).joinToString(",").reversed()}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceStat("Income", income, Icons.Default.ArrowDownward)
                BalanceStat("Expense", expense, Icons.Default.ArrowUpward)
            }
        }
    }
}

@Composable
fun BalanceStat(
    label: String,
    amount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Text(
                text = "₹${amount.toString().reversed().chunked(3).joinToString(",").reversed()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    amount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${amount.toString().reversed().chunked(3).joinToString(",").reversed()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DashboardTransactionRow(tx: DashboardTx) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = categoryIcon(tx.category),
            contentDescription = tx.category,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tx.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = if (tx.amount < 0) "−₹${-tx.amount}" else "+₹${tx.amount}",
            style = MaterialTheme.typography.titleMedium,
            color = if (tx.amount < 0)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyTransactionsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add your first transaction to see insights here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/* Helpers */

fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good Morning ☀️"
        hour < 18 -> "Good Afternoon 🌤️"
        else -> "Good Evening 🌙"
    }
}

fun categoryIcon(category: String) = when (category.lowercase()) {
    "food" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "shopping" -> Icons.Default.ShoppingBag
    "income" -> Icons.Default.AttachMoney
    "entertainment" -> Icons.Default.Movie
    else -> Icons.Default.Receipt
}

data class DashboardTx(
    val title: String,
    val category: String,
    val amount: Int
)
// Add these helpers if not already present
fun Int.formatWithComma(): String = toString().reversed().chunked(3).joinToString(",").reversed()

// ViewModel Factory (if not already in a separate file)
class DashboardViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}