package com.example.spend_trend.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillScreen(
    viewModel: BillViewModel,
    onBack: () -> Unit
) {
    val predefinedCategories = listOf(
        "Utilities", "Rent", "Insurance", "Internet", "Subscriptions",
        "Credit Card", "Loan", "Water", "Electricity", "Mobile"
    )

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var dueDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = title.isNotBlank() && amount > 0.0 && selectedCategory != null

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = androidx.compose.ui.graphics.RectangleShape,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Black, color = MonoBlack) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCEL", fontWeight = FontWeight.Black, color = MonoGrayMedium) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoWhite)
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        BlockTopBar(
            title = "REGISTER BILL",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MonoBlack)
                }
            }
        )

        // ── Title Input ──
        BlockCard {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("BILL TITLE", fontWeight = FontWeight.Black) },
                leadingIcon = { Icon(Icons.Default.Title, null, tint = MonoBlack) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MonoBlack,
                    unfocusedTextColor = MonoBlack
                )
            )
        }

        // ── Amount Input ──
        BlockCard {
            TextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = { Text("AMOUNT (₹)", fontWeight = FontWeight.Black) },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MonoBlack) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MonoBlack,
                    unfocusedTextColor = MonoBlack
                )
            )
        }

        // ── Date Picker Field ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MonoWhite)
                .border(2.dp, MonoBlack)
                .clickable { showDatePicker = true },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = MonoBlack)
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("DUE DATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MonoGrayMedium)
                    Text(
                        dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")).uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = MonoBlack
                    )
                }
            }
        }

        Text(
            "CATEGORY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MonoBlack
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            items(predefinedCategories) { cat ->
                val isSelected = selectedCategory == cat
                BlockCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = cat },
                    backgroundColor = if (isSelected) MonoBlack else MonoWhite
                ) {
                    Box(Modifier.fillMaxSize().padding(Dimens.SpacingMd), contentAlignment = Alignment.Center) {
                        Text(
                            cat.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MonoWhite else MonoBlack
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Register Button ──
        BlockButton(
            text = "REGISTER BILL",
            onClick = {
                if (selectedCategory != null) {
                    val millis = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.addBill(title, amount, selectedCategory!!, millis)
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
            enabled = isValid
        )
        
        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}
