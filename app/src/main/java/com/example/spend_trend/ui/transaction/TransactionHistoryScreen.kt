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
import androidx.compose.material.icons.outlined.*
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
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val allTx by viewModel.allTransactions.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var selectedRange by remember { mutableStateOf(DateRangeFilter.ALL_TIME) }
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedEditTx by remember { mutableStateOf<TransactionUi?>(null) }

    val today = LocalDate.now()

    val filtered = allTx
        .filter {
            when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> it.amount > 0
                TransactionFilter.EXPENSE -> it.amount < 0
            }
        }
        .filter {
            when (selectedRange) {
                DateRangeFilter.LAST_7_DAYS -> it.date.isAfter(today.minusDays(7))
                DateRangeFilter.LAST_30_DAYS -> it.date.isAfter(today.minusDays(30))
                DateRangeFilter.LAST_3_MONTHS -> it.date.isAfter(today.minusMonths(3))
                DateRangeFilter.LAST_6_MONTHS -> it.date.isAfter(today.minusMonths(6))
                DateRangeFilter.LAST_1_YEAR -> it.date.isAfter(today.minusYears(1))
                DateRangeFilter.ALL_TIME -> true
            }
        }

    val coroutineScope = rememberCoroutineScope()
    val income = filtered.filter { it.amount > 0 }.sumOf { it.amount }
    val expense = filtered.filter { it.amount < 0 }.sumOf { it.amount }
    val net = income + expense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg)
            .padding(vertical = Dimens.SpacingLg)
    ) {
        // ── Glass Summary Row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
        ) {
            GlassSummaryChip("Income", income, IncomeGreen, Modifier.weight(1f))
            GlassSummaryChip("Expense", expense, ExpenseRose, Modifier.weight(1f))
            GlassSummaryChip("Net", net, if (net >= 0) IncomeGreen else ExpenseRose, Modifier.weight(1f))
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        // ── Filters ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                TransactionFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.2f),
                            selectedLabelColor = Primary
                        )
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
            GlassEmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                filtered.groupBy { it.dateLabel() }.forEach { (group, txList) ->
                    stickyHeader {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = Dimens.SpacingSm)
                        )
                    }

                    items(txList, key = { it.hashCode() }) { tx ->
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
            Column(Modifier.padding(Dimens.SpacingLg)) {
                Text("Select time range", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Dimens.SpacingLg))
                DateRangeFilter.values().forEach { range ->
                    ListItem(
                        headlineContent = { Text(range.label) },
                        leadingContent = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select ${range.label}", tint = Primary)
                        },
                        modifier = Modifier.clickable { selectedRange = range; showRangePicker = false }
                    )
                }
            }
        }
    }

    if (selectedEditTx != null) {
        EditTransactionBottomSheet(
            transaction = selectedEditTx!!,
            onUpdate = { updated -> viewModel.updateTransaction(updated); selectedEditTx = null },
            onDismiss = { selectedEditTx = null }
        )
    }
}

@Composable
fun GlassSummaryChip(label: String, amount: Int, color: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, cornerRadius = Dimens.RadiusSm) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₹${amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
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
                SwipeToDismissBoxValue.StartToEnd -> { onDelete(); true }
                SwipeToDismissBoxValue.EndToStart -> { onEdit(); false }
                else -> false
            }
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) dismissState.reset()
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(ExpenseRose.copy(alpha = 0.15f)).padding(horizontal = Dimens.SpacingXxl),
                        contentAlignment = Alignment.CenterStart
                    ) { Icon(Icons.Default.DeleteForever, "Delete", tint = ExpenseRose) }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.15f)).padding(horizontal = Dimens.SpacingXxl),
                        contentAlignment = Alignment.CenterEnd
                    ) { Icon(Icons.Default.Edit, "Edit", tint = Primary) }
                }
                else -> {}
            }
        }
    ) {
        GlassTransactionItem(transaction)
    }
}

@Composable
fun GlassTransactionItem(tx: TransactionUi) {
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = Dimens.RadiusSm) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(tx.category), "${tx.category} category", tint = Primary, modifier = Modifier.size(Dimens.IconMd))
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(tx.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                if (tx.amount < 0) "−₹${(-tx.amount).formatWithComma()}" else "+₹${tx.amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.amount < 0) ExpenseRose else IncomeGreen
            )
        }
    }
}

@Composable
fun GlassEmptyState() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingHuge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.ReceiptLong, "No transactions", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(Dimens.IconHero))
            Spacer(Modifier.height(Dimens.SpacingLg))
            Text("No transactions yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Your financial activity will appear here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}

data class TransactionUi(val title: String, val category: String, val amount: Int, val date: LocalDate)

fun TransactionUi.dateLabel(): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("dd MMM"))
        else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}

enum class TransactionFilter { ALL, INCOME, EXPENSE }

enum class DateRangeFilter(val label: String) {
    LAST_7_DAYS("Last 7 days"), LAST_30_DAYS("Last 30 days"), LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"), LAST_1_YEAR("Last 1 year"), ALL_TIME("All time")
}
