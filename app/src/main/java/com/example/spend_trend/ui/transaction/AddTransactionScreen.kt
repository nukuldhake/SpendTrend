package com.example.spend_trend.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onSave: (TransactionUi) -> Unit = {},
    onDismiss: () -> Unit = {},
    onViewAdded: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            repository = TransactionRepository(AppDatabase.getDatabase(context).transactionDao())
        )
    )

    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var transactionDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var hasAttemptedSave by remember { mutableStateOf(false) }

    val parsedAmount = amountText.toFloatOrNull()
    val isAmountValid = parsedAmount != null && parsedAmount > 0f
    val amountError = when {
        amountText.isBlank() && hasAttemptedSave -> "AMOUNT REQUIRED"
        amountText.isNotBlank() && parsedAmount == null -> "INVALID FORMAT"
        parsedAmount != null && parsedAmount <= 0f -> "MUST BE > 0"
        else -> null
    }
    val isValid = description.isNotBlank() && isAmountValid && selectedCategory != null

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(modifier = Modifier.padding(Dimens.SpacingLg).padding(bottom = Dimens.BottomNavClearance)) {
                BlockButton(
                    text = "CONFIRM TRANSACTION",
                    onClick = {
                        if (isValid && parsedAmount != null) {
                            val newUi = TransactionUi(
                                title = description.trim().ifEmpty { "TRANSACTION" },
                                category = selectedCategory!!,
                                amount = if (isIncome) parsedAmount.toInt() else -parsedAmount.toInt(),
                                date = transactionDate
                            )
                            viewModel.addTransaction(newUi)
                            onSave(newUi)
                            snackbarHostState?.let { host ->
                                coroutineScope.launch {
                                    host.showSnackbar("TRANSACTION INITIALIZED")
                                }
                            }
                            onViewAdded()
                            onDismiss()
                        } else {
                            hasAttemptedSave = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.SpacingLg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            BlockTopBar(
                title = "NEW RECORD",
                onBack = onDismiss
            )

            // Income / Expense Switcher (Noir Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(Dimens.BorderWidthStandard, MonoBlack)
            ) {
                listOf(false to "EXPENSE", true to "INCOME").forEach { (income, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isIncome == income) MonoBlack else MonoWhite)
                            .clickable { isIncome = income }
                            .padding(vertical = Dimens.SpacingMd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isIncome == income) MonoWhite else MonoBlack,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Description Field
            Column {
                Text(
                    "DESCRIPTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("WHAT FOR?", color = MonoGrayMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = if (description.isBlank() && hasAttemptedSave) ExpenseRose else MonoBlack
                    )
                )
            }

            // Amount Field
            Column {
                Text(
                    "AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountText = it },
                    placeholder = { Text("0.00", color = MonoGrayMedium) },
                    prefix = { Text(if (isIncome) "+" else "−", fontWeight = FontWeight.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    singleLine = true,
                    isError = amountError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = if (amountError != null) ExpenseRose else MonoBlack
                    )
                )
                if (amountError != null) {
                    Text(
                        text = amountError,
                        color = ExpenseRose,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Category Picker
            BlockCard(
                onClick = { showCategoryPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Text(
                            selectedCategory ?: "SELECT CATEGORY",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategory == null && hasAttemptedSave) ExpenseRose else MonoBlack
                        )
                    }
                    Icon(Icons.Default.Category, null)
                }
            }

            // Date Picker
            BlockCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Text(
                            transactionDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")).uppercase(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.DateRange, null)
                }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            onCategorySelected = {
                selectedCategory = it
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        transactionDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = MonoBlack, fontWeight = FontWeight.Bold) }
            },
            colors = DatePickerDefaults.colors(containerColor = MonoWhite)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CategoryPickerDialog(
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("FOOD", "TRANSPORT", "RENT", "SHOPPING", "ENTERTAINMENT", "HEALTH", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SELECT CATEGORY", fontWeight = FontWeight.Black) },
        text = {
            LazyColumn {
                items(categories) { category ->
                    Text(
                        text = category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = Dimens.SpacingMd),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(thickness = 1.dp, color = MonoGrayLight)
                }
            }
        },
        confirmButton = {},
        containerColor = MonoWhite,
        shape = androidx.compose.ui.graphics.RectangleShape
    )
}
