package com.example.spend_trend.ui.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
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

    // State for edit bottom sheet
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            SummaryRow(income, expense, net)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterRow(selectedFilter) { selectedFilter = it }

                Row {
                    IconButton(onClick = { showRangePicker = true }) {
                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { exportCsv(filtered) }) {
                        Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                EmptyTransactionsPlaceholder()
            } else {
                LazyColumn {
                    filtered
                        .groupBy { it.dateLabel() }
                        .forEach { (group, txList) ->

                            stickyHeader {
                                Text(
                                    text = group,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(vertical = 12.dp)
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
                                                viewModel.addTransaction(tx)  // undo
                                            }
                                        }
                                    },
                                    onEdit = {
                                        selectedEditTx = tx
                                    }
                                )
                            }
                        }
                }
            }
        }
    }

    // Range picker
    if (showRangePicker) {
        ModalBottomSheet(onDismissRequest = { showRangePicker = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Select time range", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                DateRangeFilter.values().forEach { range ->
                    ListItem(
                        headlineContent = { Text(range.label) },
                        leadingContent = {
                            Icon(
                                when (range) {
                                    DateRangeFilter.LAST_7_DAYS -> Icons.Default.CalendarToday
                                    DateRangeFilter.LAST_30_DAYS -> Icons.Default.CalendarMonth
                                    else -> Icons.Default.CalendarMonth
                                },
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
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

    // Edit bottom sheet
    if (selectedEditTx != null) {
        EditTransactionBottomSheet(
            transaction = selectedEditTx!!,
            onUpdate = { updated ->
                viewModel.updateTransaction(updated)
                selectedEditTx = null
            },
            onDismiss = { selectedEditTx = null }
        )
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

    // Reset state after dismiss to avoid stuck background
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                else -> {}
            }
        }
    ) {
        TransactionRow(transaction)
    }
}

// ... (keep all your other functions: SummaryRow, SummaryItem, TransactionRow, EmptyTransactionsPlaceholder, FilterRow, formatWithComma, data classes, enums, exportCsv)
@Composable
fun SummaryRow(income: Int, expense: Int, net: Int) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Income", income, MaterialTheme.colorScheme.primary)
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SummaryItem("Expense", expense, MaterialTheme.colorScheme.error)
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            SummaryItem("Net", net, if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Int, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${amount.formatWithComma()}",
            style = MaterialTheme.typography.titleLarge,
            color = tint
        )
    }
}


@Composable
fun TransactionRow(tx: TransactionUi) {
    ListItem(
        headlineContent = {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = tx.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = categoryIcon(tx.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        trailingContent = {
            Text(
                text = if (tx.amount < 0) "−₹${(-tx.amount).formatWithComma()}"
                else "+₹${tx.amount.formatWithComma()}",
                style = MaterialTheme.typography.titleMedium,
                color = if (tx.amount < 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Optional: open details */ }
    )
}

@Composable
fun EmptyTransactionsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No transactions yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Your financial activity will appear here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FilterRow(
    selected: TransactionFilter,
    onSelected: (TransactionFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TransactionFilter.values().forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

fun Int.formatWithComma(): String = toString().reversed().chunked(3).joinToString(",").reversed()

data class TransactionUi(
    val title: String,
    val category: String,
    val amount: Int,
    val date: LocalDate
)

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
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"),
    LAST_1_YEAR("Last 1 year"),
    ALL_TIME("All time")
}

fun exportCsv(transactions: List<TransactionUi>) {
    val csv = buildString {
        append("Title,Category,Amount,Date\n")
        transactions.forEach {
            append("${it.title.replace(",", "")},${it.category},${it.amount},${it.date}\n")
        }
    }
    println(csv) // Later: write to file / share intent
}