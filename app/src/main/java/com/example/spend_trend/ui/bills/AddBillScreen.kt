package com.example.spend_trend.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
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
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        NeumorphicTopBar(title = "Add Bill", onBack = onBack)

        // ── Title Input ──
        NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Bill Title") },
                placeholder = { Text("e.g. Electricity Bill") },
                leadingIcon = { Icon(Icons.Default.Title, "Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        // ── Amount Input ──
        NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
            TextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (₹)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, "Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        // ── Date Picker Field ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(64.dp).clickable { showDatePicker = true },
            isConcave = true,
            backgroundColor = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, "Date", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Text(
            "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Dimens.SpacingSm)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            items(predefinedCategories) { cat ->
                val isSelected = selectedCategory == cat
                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = cat },
                    isConcave = isSelected,
                    elevation = if (isSelected) 0.dp else 4.dp,
                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
                    cornerRadius = Dimens.RadiusMd
                ) {
                    Box(Modifier.fillMaxSize().padding(Dimens.SpacingMd), contentAlignment = Alignment.Center) {
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Register Button ──
        Button(
            onClick = {
                if (isValid && selectedCategory != null) {
                    val millis = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.addBill(title, amount, selectedCategory!!, millis)
                    onBack()
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary),
            shape = RoundedCornerShape(Dimens.RadiusMd),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 0.dp)
        ) {
            Text("Register Bill", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}
