package com.example.spend_trend.ui.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockTopBar
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
    ) {
        BlockTopBar(
            title = "LEDGER",
            actions = {
                IconButton(onClick = { showRangePicker = true }) {
                    Icon(Icons.Default.FilterList, null, tint = MonoBlack)
                }
                IconButton(onClick = { viewModel.exportToCsv(context) }) {
                    Icon(Icons.Default.IosShare, null, tint = MonoBlack)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingSm))

            // ── Summary Cards ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
            ) {
                BlockSummaryCard("INCOME", income, IncomeGreen, Modifier.weight(1f))
                BlockSummaryCard("EXPENSE", expense, ExpenseRose, Modifier.weight(1f))
                BlockSummaryCard("NET", net, if (net >= 0) MonoBlack else ExpenseRose, Modifier.weight(1f))
            }

            // ── Filter Switcher ──
            Row(
                modifier = Modifier.fillMaxWidth().border(2.dp, MonoBlack)
            ) {
                TransactionFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) MonoBlack else MonoWhite)
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            filter.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                BlockEmptyState()
            } else {
                val groupedTransactions = remember(filtered) {
                    filtered.groupBy { it.dateLabel() }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    groupedTransactions.forEach { (group, txList) ->
                        stickyHeader(key = group) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Text(
                                    text = group.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MonoGrayMedium,
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
                                            message = "ENTRY REMOVED",
                                            actionLabel = "UNDO",
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
    }

    if (showRangePicker) {
        ModalBottomSheet(
            onDismissRequest = { showRangePicker = false },
            shape = androidx.compose.ui.graphics.RectangleShape,
            containerColor = MonoWhite
        ) {
            Column(Modifier.padding(Dimens.SpacingLg).padding(bottom = 32.dp)) {
                Text("TIME RANGE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(Dimens.SpacingLg))
                DateRangeFilter.entries.forEach { range ->
                    BlockCard(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            selectedRange = range
                            showRangePicker = false 
                        },
                        backgroundColor = if (selectedRange == range) MonoGrayLight else MonoWhite
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = MonoBlack)
                            Spacer(Modifier.width(Dimens.SpacingMd))
                            Text(range.label.uppercase(), fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.height(Dimens.SpacingSm))
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
fun BlockSummaryCard(label: String, amount: Int, color: Color, modifier: Modifier = Modifier) {
    BlockCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MonoGrayMedium)
            Text(
                "₹${amount.formatWithComma()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
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
                SwipeToDismissBoxValue.StartToEnd -> ExpenseRose
                SwipeToDismissBoxValue.EndToStart -> MonoBlack
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MonoBlack)
                    .background(color)
                    .padding(horizontal = Dimens.SpacingXxl),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(Icons.Default.Delete, "Delete", tint = MonoWhite)
                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Edit, "Edit", tint = MonoWhite)
                }
            }
        }
    ) {
        BlockTransactionItem(transaction)
    }
}

@Composable
fun BlockTransactionItem(tx: TransactionUi) {
    BlockCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CategoryColors.getColorForCategory(tx.category)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MonoBlack)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(tx.category), null, tint = MonoBlack, modifier = Modifier.size(Dimens.IconMd))
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Column(Modifier.weight(1f)) {
                Text(tx.title.uppercase(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MonoBlack)
                Text(tx.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MonoBlack.copy(alpha = 0.7f))
            }
            Text(
                valStr(tx.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = MonoBlack
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
fun BlockEmptyState() {
    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingHuge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ReceiptLong,
                null,
                tint = MonoBlack,
                modifier = Modifier.size(Dimens.IconHero)
            )
            Spacer(Modifier.height(Dimens.SpacingLg))
            Text("NO RECORDS FOUND", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("YOUR FINANCIAL LEDGER IS EMPTY", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium, textAlign = TextAlign.Center)
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

