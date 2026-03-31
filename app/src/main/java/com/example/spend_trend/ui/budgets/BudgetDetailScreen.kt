package com.example.spend_trend.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
import com.example.spend_trend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    viewModel: BudgetViewModel,
    budgetId: Int,
    onBack: () -> Unit
) {
    LaunchedEffect(budgetId) { viewModel.loadBudgetById(budgetId) }

    val budget by viewModel.selectedBudget.collectAsState()
    var showEditSheet by remember { mutableStateOf(false) }
    var newLimitText by remember { mutableStateOf("") }

    if (budget == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val b = budget!!
    val progress = if (b.monthlyLimit > 0) (b.currentSpent / b.monthlyLimit).coerceIn(0f, 1f) else 0f
    val remaining = (b.monthlyLimit - b.currentSpent).toInt()
    val statusColor = when {
        progress >= 1f -> ExpenseRose
        progress >= 0.8f -> WarningAmber
        else -> Primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        NeumorphicTopBar(title = b.category, onBack = onBack)

        // ── Neumorphic Hero Card ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 12.dp,
            cornerRadius = Dimens.RadiusLg
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon(b.category),
                        contentDescription = "${b.category} icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconLg)
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                Text(
                    "₹${b.monthlyLimit.toInt().formatWithComma()}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Monthly budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Progress Card ──
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.10f),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )

                Text(
                    "${(progress * 100).toInt()}% used",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Spent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${b.currentSpent.toInt().formatWithComma()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ExpenseRose)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Remaining", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (remaining >= 0) "₹${remaining.formatWithComma()}" else "−₹${(-remaining).formatWithComma()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining >= 0) IncomeGreen else ExpenseRose
                        )
                    }
                }
            }
        }

        // ── Edit Budget Button ──
        Button(
            onClick = {
                newLimitText = b.monthlyLimit.toInt().toString()
                showEditSheet = true
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary),
            shape = RoundedCornerShape(Dimens.RadiusMd),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
        ) {
            Icon(Icons.Default.Edit, "Edit budget")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Edit Budget Limit", fontWeight = FontWeight.SemiBold)
        }

        // ── Delete Button ──
        OutlinedButton(
            onClick = {
                viewModel.deleteBudget(b)
                onBack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRose),
            border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(Dimens.RadiusMd)
        ) {
            Icon(Icons.Default.DeleteForever, "Delete budget")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Delete Budget", fontWeight = FontWeight.SemiBold)
        }
    }

    // ── Edit Bottom Sheet ──
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
            ) {
                Text(
                    "Edit Budget for ${b.category}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
                    TextField(
                        value = newLimitText,
                        onValueChange = { newLimitText = it.filter { c -> c.isDigit() } },
                        label = { Text("New monthly limit (₹)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, "Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }

                Button(
                    onClick = {
                        val newLimit = newLimitText.toFloatOrNull()
                        if (newLimit != null && newLimit > 0) {
                            viewModel.updateBudgetLimit(b, newLimit)
                            showEditSheet = false
                        }
                    },
                    enabled = (newLimitText.toFloatOrNull() ?: 0f) > 0f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(Dimens.RadiusMd)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(Dimens.SpacingLg))
            }
        }
    }
}
