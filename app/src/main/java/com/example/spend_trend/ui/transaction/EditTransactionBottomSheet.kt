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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.neoShadow
import com.example.spend_trend.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    transaction: TransactionUi,
    onUpdate: (TransactionUi) -> Unit,
    onDelete: (TransactionUi) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(transaction.title) }
    var amountText by remember { mutableStateOf(transaction.amount.absoluteValue.toString()) }
    var isIncome by remember { mutableStateOf(transaction.amount > 0) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var transactionDate by remember { mutableStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val amount = amountText.toFloatOrNull() ?: 0f
    val isValid = description.isNotBlank() && amount > 0f && selectedCategory != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = androidx.compose.ui.graphics.RectangleShape,
        containerColor = MonoWhite,
        dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(MonoBlack)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingLg)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EDIT RECORD", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = ExpenseRose)
                }
            }

            // Income / Expense switcher
            Row(
                modifier = Modifier.fillMaxWidth().border(Dimens.BorderWidthStandard, MonoBlack)
            ) {
                listOf(false to "EXPENSE", true to "INCOME").forEach { (income, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isIncome == income) MonoBlack else MonoWhite)
                            .clickable { isIncome = income }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isIncome == income) MonoWhite else MonoBlack
                        )
                    }
                }
            }

            // Amount field
            Column {
                Text("AMOUNT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { new ->
                        if (new.all { it.isDigit() || it == '.' }) amountText = new
                    },
                    prefix = { Text(if (isIncome) "+" else "−", fontWeight = FontWeight.Black) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.fillMaxWidth().neoShadow(),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MonoGrayLight
                    )
                )
            }

            // Description
            Column {
                Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().neoShadow(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.RadiusLg),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MonoGrayLight
                    )
                )
            }

            // Date & Category Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BlockCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Text(transactionDate.format(DateTimeFormatter.ofPattern("dd MMM")).uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
                BlockCard(
                    onClick = { showCategoryPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Text(selectedCategory.uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            BlockButton(
                text = "SAVE CHANGES",
                onClick = {
                    if (isValid) {
                        onUpdate(
                            transaction.copy(
                                title = description,
                                amount = if (isIncome) amount.toInt() else -amount.toInt(),
                                category = selectedCategory,
                                date = transactionDate
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("DELETE RECORD?", fontWeight = FontWeight.Black) },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    showDeleteConfirm = false
                    onDismiss()
                }) { Text("DELETE", color = ExpenseRose, fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = MonoBlack)
                }
            },
            containerColor = MonoWhite,
            shape = androidx.compose.ui.graphics.RectangleShape
        )
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
