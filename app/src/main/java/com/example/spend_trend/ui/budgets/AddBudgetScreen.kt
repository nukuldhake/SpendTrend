package com.example.spend_trend.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        NeumorphicTopBar(title = "Add Budget", onBack = onBack)

        // ── Category Picker ──
        Text(
            "Choose a category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            items(predefinedCategories) { cat ->
                val isSelected = !useCustom && selectedCategory == cat
                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            useCustom = false
                            selectedCategory = cat
                        },
                    isConcave = isSelected,
                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
                    elevation = if (isSelected) 0.dp else 4.dp,
                    cornerRadius = Dimens.RadiusMd
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = Dimens.SpacingMd),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            categoryIcon(cat),
                            contentDescription = "$cat category",
                            tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.IconMd)
                        )
                        Spacer(Modifier.height(Dimens.SpacingXs))
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Custom Category ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useCustom,
                onCheckedChange = { useCustom = it },
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            Text(
                "Custom category",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (useCustom) {
            NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
                TextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Category name") },
                    leadingIcon = { Icon(Icons.Default.Category, "Category") },
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
        }

        // ── Budget Limit ──
        NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
            TextField(
                value = limitText,
                onValueChange = { limitText = it.filter { c -> c.isDigit() } },
                label = { Text("Monthly limit (₹)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, "Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        Spacer(Modifier.weight(1f))

        // ── Save Button ──
        Button(
            onClick = {
                if (isValid && category != null) {
                    viewModel.addBudget(category, limitValue)
                    onBack()
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary),
            shape = RoundedCornerShape(Dimens.RadiusMd),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 0.dp)
        ) {
            Icon(Icons.Default.Check, "Save")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Create Budget", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}
