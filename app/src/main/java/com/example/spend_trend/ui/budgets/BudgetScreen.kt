package com.example.spend_trend.ui.budgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.theme.*

@Composable
fun BudgetsScreen(
    onCardClick: (Int) -> Unit = {},
    onAddBudget: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(
            repository = BudgetRepository(AppDatabase.getDatabase(context).budgetDao())
        )
    )

    val budgets by viewModel.allBudgets.collectAsState(initial = emptyList())

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Check if any budget is over limit
    val hasOverBudget = budgets.any { it.currentSpent > it.monthlyLimit }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg)
            .padding(vertical = Dimens.SpacingLg)
    ) {
        if (hasOverBudget) {
            val overBudgetCategory = budgets.first { it.currentSpent > it.monthlyLimit }.category
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ExpenseRose.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "Over budget warning",
                            tint = ExpenseRose,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                    }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Text(
                        "You've exceeded your $overBudgetCategory budget this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpenseRose
                    )
                }
            }
            Spacer(Modifier.height(Dimens.SpacingLg))
        }

        if (budgets.isEmpty()) {
            // Empty state
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = "No budgets",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(Dimens.IconHero)
                )
                Spacer(Modifier.height(Dimens.SpacingLg))
                Text(
                    "No budgets yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap + to create your first budget category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
                contentPadding = PaddingValues(bottom = Dimens.BottomNavClearance)
            ) {
                itemsIndexed(budgets) { index, budget ->
                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 80),
                        label = "budget_card_alpha_$index"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer { this.alpha = alpha }
                            .clickable { onCardClick(budget.id) }
                    ) {
                        BudgetGlassCard(budget)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetGlassCard(budget: BudgetEntity) {
    val progress = if (budget.monthlyLimit > 0) (budget.currentSpent / budget.monthlyLimit).coerceIn(0f, 1f) else 0f
    val remaining = (budget.monthlyLimit - budget.currentSpent).toInt()

    val statusColor = when {
        progress >= 1f -> ExpenseRose
        progress >= 0.8f -> WarningAmber
        else -> Primary
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(budget.category),
                    contentDescription = "${budget.category} budget",
                    tint = statusColor,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }

            Text(
                budget.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(80.dp),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.12f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )

            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "₹${budget.currentSpent.toInt().formatWithComma()} / ₹${budget.monthlyLimit.toInt().formatWithComma()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (remaining > 0) {
                Text(
                    "₹${remaining.formatWithComma()} left",
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            } else if (remaining < 0) {
                Text(
                    "₹${(-remaining).formatWithComma()} over",
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpenseRose
                )
            }
        }
    }
}