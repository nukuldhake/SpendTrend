package com.example.spend_trend.ui.budgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetsScreen() {
    // Fake data – replace with real ViewModel later
    val globalOverBudget = true // simulate alert condition
    val budgets = listOf(
        BudgetItem("Food", 4200f, 5000f),
        BudgetItem("Transport", 1800f, 2000f),
        BudgetItem("Fun", 3200f, 3000f),
        BudgetItem("Shopping", 2500f, 4000f),
        BudgetItem("Bills", 8000f, 10000f),
        BudgetItem("Health", 1200f, 1500f)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
                .padding(horizontal = 16.dp)

        ) {
            // Global alert if any category overspent
            if (globalOverBudget) {
                OverBudgetAlert("You've exceeded your Entertainment budget this month")
                Spacer(Modifier.height(20.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)  // ← adjust this value to fit your needs (e.g. 400.dp, 500.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(budgets) { budget ->
                        BudgetCard(budget)
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // nav bar / FAB padding
        }
    }
}

@Composable
fun BudgetCard(budget: BudgetItem) {
    val progress = (budget.spent / budget.limit).coerceIn(0f, 1f)
    val remaining = (budget.limit - budget.spent).toInt()

    val statusColor = when {
        progress >= 1f -> MaterialTheme.colorScheme.error
        progress >= 0.8f -> MaterialTheme.colorScheme.tertiary // or custom warning
        else -> MaterialTheme.colorScheme.primary
    }

    val containerColor = when {
        progress >= 1f -> MaterialTheme.colorScheme.errorContainer
        progress >= 0.8f -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    ElevatedCard(
        onClick = { /* TODO: Navigate to budget details / edit */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = categoryIcon(budget.category),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(100.dp),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.2f),
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "₹${budget.spent.toInt().formatWithComma()} / ₹${budget.limit.toInt().formatWithComma()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (remaining > 0) {
                Text(
                    text = "₹$remaining left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progress < 0.8f) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (remaining < 0) {
                Text(
                    text = "₹${-remaining} over",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun OverBudgetAlert(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = "Alert",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun EmptyBudgetsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Savings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No budgets set yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Create budgets for categories to track spending and get alerts",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = { /* TODO: Navigate to create budget */ }) {
            Text("Set up your first budget")
        }
    }
}

fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "food", "dining" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "entertainment" -> Icons.Default.Movie
    "shopping" -> Icons.Default.ShoppingCart
    "bills", "utilities" -> Icons.Default.Receipt
    "health" -> Icons.Default.LocalHospital
    else -> Icons.Default.Category
}

fun Int.formatWithComma(): String = toString().reversed().chunked(3).joinToString(",").reversed()

data class BudgetItem(
    val category: String,
    val spent: Float,
    val limit: Float
)