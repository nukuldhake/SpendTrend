package com.example.spend_trend.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockTopBar
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
            CircularProgressIndicator(color = MonoBlack)
        }
        return
    }

    val b = budget!!
    val progress = if (b.monthlyLimit > 0) (b.currentSpent / b.monthlyLimit).coerceIn(0f, 1f) else 0f
    val remaining = (b.monthlyLimit - b.currentSpent).toInt()
    val statusColor = when {
        progress >= 1f -> ExpenseRose
        progress >= 0.8f -> WarningAmber
        else -> MonoBlack
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoWhite),
        topBar = {
            BlockTopBar(
                title = b.category.uppercase(),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingMd))

            // ── Block Hero Card ──
        BlockCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MonoBlack
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MonoWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon(b.category),
                        contentDescription = "${b.category} icon",
                        tint = MonoBlack,
                        modifier = Modifier.size(Dimens.IconLg)
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                Text(
                    "₹${b.monthlyLimit.toInt().formatWithComma()}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MonoWhite
                )
                Text(
                    "MONTHLY BUDGET",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MonoGrayMedium
                )
            }
        }

        // ── Progress Card ──
        BlockCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = statusColor,
                        trackColor = MonoGrayLight,
                        strokeWidth = 12.dp,
                        strokeCap = StrokeCap.Butt
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MonoBlack
                    )
                }

                Text(
                    "USED PROGRESS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MonoGrayMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SPENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MonoGrayMedium)
                        Text("₹${b.currentSpent.toInt().formatWithComma()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = ExpenseRose)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REMAINING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MonoGrayMedium)
                        Text(
                            if (remaining >= 0) "₹${remaining.formatWithComma()}" else "−₹${(-remaining).formatWithComma()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (remaining >= 0) IncomeGreen else ExpenseRose
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Edit Budget Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MonoWhite)
                .border(2.dp, MonoBlack)
                .clickable {
                    newLimitText = b.monthlyLimit.toInt().toString()
                    showEditSheet = true
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, "Edit budget", tint = MonoBlack)
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text("EDIT BUDGET LIMIT", fontWeight = FontWeight.Black, color = MonoBlack)
            }
        }

        // ── Delete Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(ExpenseRose)
                .border(2.dp, MonoBlack)
                .clickable {
                    viewModel.deleteBudget(b)
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteForever, "Delete budget", tint = MonoWhite)
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text("DELETE BUDGET", fontWeight = FontWeight.Black, color = MonoWhite)
            }
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
        }
    }

    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            containerColor = MonoWhite,
            shape = RectangleShape,
            dragHandle = { Box(Modifier.fillMaxWidth().height(4.dp).background(MonoBlack)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
            ) {
                Text(
                    "EDIT BUDGET LIMIT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )

                BlockCard {
                    TextField(
                        value = newLimitText,
                        onValueChange = { newLimitText = it.filter { c -> c.isDigit() } },
                        label = { Text("NEW MONTHLY LIMIT (₹)", fontWeight = FontWeight.Black) },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = MonoBlack) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedTextColor = MonoBlack,
                            unfocusedTextColor = MonoBlack
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MonoBlack)
                        .clickable(enabled = (newLimitText.toFloatOrNull() ?: 0f) > 0f) {
                            val newLimit = newLimitText.toFloatOrNull()
                            if (newLimit != null && newLimit > 0) {
                                viewModel.updateBudgetLimit(b, newLimit)
                                showEditSheet = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Black, color = MonoWhite)
                }

                Spacer(Modifier.height(Dimens.SpacingLg))
            }
        }
    }
}
