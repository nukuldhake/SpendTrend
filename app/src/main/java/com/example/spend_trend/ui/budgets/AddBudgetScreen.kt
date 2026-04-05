package com.example.spend_trend.ui.budgets

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

@Composable
fun AddBudgetScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val predefinedCategories = listOf(
        "Food", "Transport", "Fun", "Shopping", "Bills",
        "Health", "Education", "Rent", "Subscriptions", "Travel"
    )

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var customCategory by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }
    var limitText by remember { mutableStateOf("") }

    val category = if (useCustom) customCategory.trim() else selectedCategory
    val limitValue = limitText.toFloatOrNull() ?: 0f
    val isValid = !category.isNullOrBlank() && limitValue > 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoWhite)
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        BlockTopBar(
            title = "ADD BUDGET",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MonoBlack)
                }
            }
        )

        // ── Category Picker ──
        Text(
            "CHOOSE CATEGORY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MonoBlack
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            items(predefinedCategories) { cat ->
                val isSelected = !useCustom && selectedCategory == cat
                val itemColor = if (isSelected) MonoWhite else MonoBlack
                BlockCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            useCustom = false
                            selectedCategory = cat
                        },
                    backgroundColor = if (isSelected) MonoBlack else MonoWhite
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = Dimens.SpacingMd),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            categoryIcon(cat),
                            contentDescription = "$cat category",
                            tint = itemColor,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        Text(
                            cat.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = itemColor
                        )
                    }
                }
            }
        }

        // ── Custom Category ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (useCustom) MonoBlack else MonoWhite)
                .border(2.dp, MonoBlack)
                .clickable { useCustom = !useCustom }
                .padding(Dimens.SpacingMd)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(2.dp, if (useCustom) MonoWhite else MonoBlack)
                    .background(if (useCustom) MonoWhite else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (useCustom) {
                    Icon(Icons.Default.Check, null, tint = MonoBlack, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.width(Dimens.SpacingMd))
            Text(
                "CUSTOM CATEGORY",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = if (useCustom) MonoWhite else MonoBlack
            )
        }

        if (useCustom) {
            BlockCard {
                TextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    placeholder = { Text("CATEGORY NAME", fontWeight = FontWeight.Black) },
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = MonoBlack) },
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
        }

        // ── Budget Limit ──
        BlockCard {
            TextField(
                value = limitText,
                onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                placeholder = { Text("MONTHLY LIMIT (₹)", fontWeight = FontWeight.Black) },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MonoBlack) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        Spacer(Modifier.weight(1f))

        // ── Save Button ──
        BlockButton(
            text = "CREATE BUDGET",
            onClick = {
                if (category != null) {
                    viewModel.addBudget(category, limitValue)
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
            enabled = isValid
        )

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}
