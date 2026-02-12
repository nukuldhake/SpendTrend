package com.example.spend_trend.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*
import java.time.LocalTime

@Composable
fun DashboardScreen(
    onViewAllTransactions: () -> Unit = {}
) {
    // Fake data – later from ViewModel / Repository
    val balance = 32850
    val income = 45000
    val expense = 12150

    val recentTransactions = listOf(
        DashboardTx("Starbucks", "Food", -320),
        DashboardTx("Uber", "Transport", -450),
        DashboardTx("Amazon", "Shopping", -1299),
        DashboardTx("Salary", "Income", 45000)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Balance Card – prominent, primary color
                BalanceCard(balance, income, expense)
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
                        amount = income,
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    InsightCard(
                        title = "Expense",
                        amount = expense,
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
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
                Spacer(Modifier.height(8.dp))
            }

            if (recentTransactions.isEmpty()) {
                item {
                    EmptyTransactionsPlaceholder()
                }
            } else {
                items(recentTransactions) { tx ->
                    DashboardTransactionRow(tx)
                    if (tx != recentTransactions.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) } // bottom padding for FAB/nav
        }
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