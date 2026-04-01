package com.example.spend_trend.ui.budgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*

@Composable
fun BudgetsScreen(
    onCardClick: (Int) -> Unit = {}
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
            .background(MonoWhite)
            .padding(horizontal = Dimens.SpacingLg)
            .padding(vertical = Dimens.SpacingLg)
    ) {
        if (hasOverBudget) {
            val overBudgetCategories = budgets.filter { it.currentSpent > it.monthlyLimit }
            overBudgetCategories.forEach { overBudget ->
                BlockCard(modifier = Modifier.fillMaxWidth(), backgroundColor = ExpenseRose) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = MonoWhite,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        Text(
                            "EXCEEDED: ${overBudget.category.uppercase()} BUDGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MonoWhite
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.SpacingSm))
            }
            Spacer(Modifier.height(Dimens.SpacingSm))
        }

        if (budgets.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BlockCard(modifier = Modifier.size(120.dp), hasShadow = true) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MonoBlack,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.SpacingLg))
                Text(
                    "NO BUDGETS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )
                Text(
                    "TAP + TO INITIALIZE TRACKING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonoGrayMedium
                )
            }
            Spacer(Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
                contentPadding = PaddingValues(bottom = Dimens.BottomNavClearance)
            ) {
                itemsIndexed(budgets) { index, budget ->
                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(durationMillis = 400, delayMillis = index * 50),
                        label = "budget_card_alpha_$index"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer { this.alpha = alpha }
                            .clickable { onCardClick(budget.id) }
                    ) {
                        BudgetBlockCard(budget)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetBlockCard(budget: BudgetEntity) {
    val progress = if (budget.monthlyLimit > 0) (budget.currentSpent.toFloat() / budget.monthlyLimit).coerceIn(0f, 1f) else 0f
    val remaining = (budget.monthlyLimit - budget.currentSpent).toInt()

    val statusColor = when {
        progress >= 1f -> ExpenseRose
        progress >= 0.8f -> WarningAmber
        else -> MonoBlack
    }

    BlockCard(modifier = Modifier.fillMaxWidth(), hasShadow = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(2.dp, MonoBlack)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(budget.category),
                    contentDescription = null,
                    tint = MonoBlack,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                budget.category.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MonoBlack
            )

            // Stark Linear Progress instead of Circular for "Edgy" look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .border(1.5.dp, MonoBlack)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(statusColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
                Text(
                    "₹${budget.currentSpent.toInt().formatWithComma()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MonoBlack
                )
            }

            HorizontalDivider(thickness = 1.dp, color = MonoGrayLight)

            if (remaining > 0) {
                Text(
                    "₹${remaining.formatWithComma()} LEFT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MonoGrayMedium
                )
            } else {
                Text(
                    "LIMIT EXCEEDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = ExpenseRose
                )
            }
        }
    }
}