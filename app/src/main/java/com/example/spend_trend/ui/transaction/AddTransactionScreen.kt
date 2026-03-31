package com.example.spend_trend.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository

import com.example.spend_trend.ui.components.NeumorphicTopBar
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicChip
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onSave: (NewTransaction) -> Unit = {},
    onDismiss: () -> Unit = {},
    onViewAdded: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    // ViewModel setup
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

    // Strict validation: reject malformed floats like "12..34" or "12.3.4"
    val parsedAmount = amountText.toFloatOrNull()
    val isAmountValid = parsedAmount != null && parsedAmount > 0f
    val amountError = when {
        amountText.isBlank() && hasAttemptedSave -> "Amount is required"
        amountText.isNotBlank() && parsedAmount == null -> "Invalid amount (e.g. \"12..3\")"
        parsedAmount != null && parsedAmount <= 0f -> "Amount must be greater than zero"
        else -> null
    }
    val isValid = description.isNotBlank() && isAmountValid && selectedCategory != null

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isValid && parsedAmount != null) {
                        val newUi = TransactionUi(
                            title = description.trim().ifEmpty { "Transaction" },
                            category = selectedCategory!!,
                            amount = if (isIncome) parsedAmount.toInt() else -parsedAmount.toInt(),
                            date = transactionDate
                        )

                        // Save using ViewModel (real DB)
                        viewModel.addTransaction(newUi)

                        // Trigger callback for UI feedback
                        onSave(NewTransaction(newUi.title, newUi.amount.toFloat(), newUi.category, newUi.date))

                        // Show success snackbar
                        snackbarHostState?.let { host ->
                            coroutineScope.launch {
                                host.showSnackbar(
                                    message = "Transaction added!",
                                    actionLabel = "View",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }

                        onViewAdded()
                        onDismiss()
                    } else {
                        hasAttemptedSave = true
                    }
                },
                containerColor = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                contentColor = if (isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            NeumorphicTopBar(title = "New Transaction", onBack = onDismiss)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

            // Income / Expense toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
            ) {
                NeumorphicChip(
                    text = "Expense",
                    isSelected = !isIncome,
                    onClick = { isIncome = false },
                    selectedColor = MaterialTheme.colorScheme.errorContainer,
                    onSelectedColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                NeumorphicChip(
                    text = "Income",
                    isSelected = isIncome,
                    onClick = { isIncome = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // Amount – recessed card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isConcave = true,
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                TextField(
                    value = amountText,
                    onValueChange = { new ->
                        if (new.all { it.isDigit() || it == '.' }) {
                            amountText = new
                        }
                    },
                    label = { Text("Amount") },
                    prefix = { Text(if (isIncome) "+" else "−") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = amountError != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                )
            }

            // Inline validation error
            if (amountError != null) {
                Text(
                    text = amountError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Validation helper when save attempted but category missing
            if (hasAttemptedSave && selectedCategory == null) {
                Text(
                    text = "Please select a category",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Category Picker – recessed card
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryPicker = true },
                isConcave = true,
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedCategory != null) {
                        Icon(
                            imageVector = categoryIcon(selectedCategory!!),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedCategory ?: "Select category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedCategory != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Description – recessed card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isConcave = true,
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Merchant") },
                    placeholder = { Text("e.g. Lunch at Zomato") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // Date Picker Field – recessed card
            NeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                isConcave = true,
                backgroundColor = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = transactionDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = transactionDate.toEpochDay() * 86400000L
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = LocalDate.ofEpochDay(millis / 86400000L)
                            transactionDate = newDate
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Select Category", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(listOf("Food", "Transport", "Shopping", "Salary", "Bills", "Entertainment", "Health", "Other")) { cat ->
                        ListItem(
                            headlineContent = { Text(cat) },
                            leadingContent = {
                                Icon(
                                    categoryIcon(cat),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedCategory = cat
                                showCategoryPicker = false
                            }
                        )
                    }
                }
            }
        }
    }
}

data class NewTransaction(
    val description: String,
    val amount: Float,
    val category: String,
    val date: LocalDate
)
