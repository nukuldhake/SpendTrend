package com.example.spend_trend.ui.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicChip
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            repository = TransactionRepository(AppDatabase.getDatabase(context).transactionDao())
        )
    )

    val allTx by viewModel.allTransactions.collectAsState()

    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var selectedRange by remember { mutableStateOf(DateRangeFilter.ALL_TIME) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedEditTx by remember { mutableStateOf<TransactionUi?>(null) }

    val today = LocalDate.now()

    val filtered = remember(allTx, selectedFilter, selectedRange) {
        allTx.filter {
            when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> it.amount > 0
                TransactionFilter.EXPENSE -> it.amount < 0
            }
        }.filter {
            when (selectedRange) {
                DateRangeFilter.LAST_7_DAYS -> it.date.isAfter(today.minusDays(7))
                DateRangeFilter.LAST_30_DAYS -> it.date.isAfter(today.minusDays(30))
                DateRangeFilter.LAST_3_MONTHS -> it.date.isAfter(today.minusMonths(3))
                DateRangeFilter.LAST_6_MONTHS -> it.date.isAfter(today.minusMonths(6))
                DateRangeFilter.LAST_1_YEAR -> it.date.isAfter(today.minusYears(1))
                DateRangeFilter.ALL_TIME -> true
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val income = filtered.filter { it.amount > 0 }.sumOf { it.amount }
    val expense = filtered.filter { it.amount < 0 }.sumOf { it.amount }
    val net = income + expense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg)
            .padding(vertical = Dimens.SpacingLg)
    ) {
        // ── Summary Row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
        ) {
            NeumorphicSummaryChip("Income", income, IncomeGreen, Modifier.weight(1f))
            NeumorphicSummaryChip("Expense", expense, ExpenseRose, Modifier.weight(1f))
            NeumorphicSummaryChip("Net", net, if (net >= 0) IncomeGreen else ExpenseRose, Modifier.weight(1f))
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        // ── Filters ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                TransactionFilter.values().forEach { filter ->
                    NeumorphicChip(
                        text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Row {
                IconButton(onClick = { showRangePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Date range", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.exportToCsv(context) }) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        if (filtered.isEmpty()) {
            NeumorphicEmptyState()
        } else {
            val groupedTransactions = remember(filtered) {
                filtered.groupBy { it.dateLabel() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedTransactions.forEach { (group, txList) ->
                    stickyHeader(key = group) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Text(
                                text = group,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = Dimens.SpacingSm)
                            )
                        }
                    }

                    items(
                        items = txList,
                        key = { it.id }
                    ) { tx ->
                        SwipeableTransactionRow(
                            transaction = tx,
                            onDelete = {
                                viewModel.deleteTransaction(tx)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addTransaction(tx)
                                    }
                                }
                            },
                            onEdit = { selectedEditTx = tx }
                        )
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        ModalBottomSheet(onDismissRequest = { showRangePicker = false }) {
            Column(Modifier.padding(Dimens.SpacingLg).padding(bottom = 32.dp)) {
                Text("Select time range", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Dimens.SpacingLg))
                DateRangeFilter.values().forEach { range ->
                    ListItem(
                        headlineContent = { Text(range.label) },
                        leadingContent = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Primary)
                        },
                        modifier = Modifier.clickable { 
                            selectedRange = range
                            showRangePicker = false 
                        }
                    )
                }
            }
        }
    }

    if (selectedEditTx != null) {
        EditTransactionBottomSheet(
            transaction = selectedEditTx!!,
            onUpdate = { updated -> 
                viewModel.updateTransaction(updated)
                selectedEditTx = null 
            },
            onDelete = { toDelete -> 
                viewModel.deleteTransaction(toDelete)
                selectedEditTx = null 
            },
            onDismiss = { selectedEditTx = null }
        )
    }
}

@Composable
fun NeumorphicSummaryChip(label: String, amount: Int, color: Color, modifier: Modifier = Modifier) {
    NeumorphicCard(modifier = modifier, cornerRadius = Dimens.RadiusSm, elevation = 4.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₹${amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTransactionRow(
    transaction: TransactionUi,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> ExpenseRose.copy(alpha = 0.2f)
                SwipeToDismissBoxValue.EndToStart -> Primary.copy(alpha = 0.2f)
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimens.RadiusSm))
                    .background(color)
                    .padding(horizontal = Dimens.SpacingXxl),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(Icons.Default.DeleteForever, "Delete", tint = ExpenseRose)
                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Edit, "Edit", tint = Primary)
                }
            }
        }
    ) {
        NeumorphicTransactionItem(transaction)
    }
}

@Composable
fun NeumorphicTransactionItem(tx: TransactionUi) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = Dimens.RadiusSm,
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(tx.category), null, tint = Primary, modifier = Modifier.size(Dimens.IconMd))
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(tx.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                valStr(tx.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.amount < 0) ExpenseRose else IncomeGreen
            )
        }
    }
}

private fun valStr(amount: Int): String {
    val absVal = if (amount < 0) -amount else amount
    val prefix = if (amount < 0) "−₹" else "+₹"
    return "$prefix${absVal.formatWithComma()}"
}

@Composable
fun NeumorphicEmptyState() {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingHuge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ReceiptLong,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(Dimens.IconHero)
            )
            Spacer(Modifier.height(Dimens.SpacingLg))
            Text("No transactions yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Your financial activity will appear here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        }
    }
}

enum class TransactionFilter { ALL, INCOME, EXPENSE }

enum class DateRangeFilter(val label: String) {
    LAST_7_DAYS("Last 7 days"), 
    LAST_30_DAYS("Last 30 days"), 
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"), 
    LAST_1_YEAR("Last 1 year"), 
    ALL_TIME("All time")
}
