package com.example.spend_trend.ui.goals

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.GoalEntity
import com.example.spend_trend.data.repository.GoalRepository
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.neoShadow
import com.example.spend_trend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(onBack: () -> Unit = {}, onMenuClick: (() -> Unit)? = null) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(GoalRepository(db.goalDao()))
    )
    
    val allGoals by viewModel.allGoals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            com.example.spend_trend.ui.components.BlockTopBar(
                title = "Goals",
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = MonoBlack,
                shape = RoundedCornerShape(Dimens.RadiusLg),
                modifier = Modifier.border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusLg))
            ) {
                Icon(Icons.Default.Add, "Add Goal")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(Modifier.height(Dimens.SpacingMd)) }

            if (allGoals.isEmpty()) {
                item {
                    EmptyGoalsState()
                }
            } else {
                items(allGoals) { goal ->
                    var showContributeDialog by remember { mutableStateOf(false) }
                    GoalBlockCard(
                        goal = goal, 
                        onIncrement = { showContributeDialog = true }
                    )
                    if (showContributeDialog) {
                        ContributeDialog(
                            goalTitle = goal.title,
                            onDismiss = { showContributeDialog = false },
                            onConfirm = { amount ->
                                viewModel.incrementProgress(goal, amount)
                                showContributeDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, target, category ->
                viewModel.addGoal(title, target.toDouble(), category, System.currentTimeMillis() + (86400000L * 30))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GoalBlockCard(goal: GoalEntity, onIncrement: () -> Unit) {
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(goal.deadlineMillis)).uppercase()

    BlockCard(modifier = Modifier.fillMaxWidth(), hasShadow = true) {
        Column(modifier = Modifier.padding(Dimens.SpacingSm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, MonoBlack)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Flag, 
                            contentDescription = null, 
                            tint = MonoBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Column {
                        Text(
                            goal.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MonoBlack
                        )
                        Text(
                            goal.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MonoGrayMedium
                        )
                    }
                }
                
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.AddBox, contentDescription = "Contribute", tint = MonoBlack)
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "₹${goal.currentAmount.toInt().formatWithComma()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MonoBlack
                    )
                    Text(
                        "CURRENT SAVINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MonoGrayMedium
                    )
                }
                Text(
                    "TARGET: ₹${goal.targetAmount.toInt().formatWithComma()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MonoBlack,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(Dimens.SpacingSm))

            // Curvy Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusFull))
                    .background(MonoWhite, RoundedCornerShape(Dimens.RadiusFull))
                    .clip(RoundedCornerShape(Dimens.RadiusFull))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(if (progress >= 1f) IncomeGreen else MonoBlack)
                )
            }

            Spacer(Modifier.height(Dimens.SpacingMd))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = MonoGrayMedium,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "DEADLINE: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonoGrayMedium
                )
            }
        }
    }
}

@Composable
private fun EmptyGoalsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlockCard(modifier = Modifier.size(100.dp), hasShadow = true) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = null,
                    tint = MonoBlack,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text(
            "NO TRACKED GOALS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MonoBlack
        )
        Text(
            "INITIALIZE YOUR FINANCIAL DREAMS",
            style = MaterialTheme.typography.labelSmall,
            color = MonoGrayMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Investment") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            BlockCard(
                modifier = Modifier.fillMaxWidth(0.9f).padding(Dimens.SpacingLg),
                borderColor = MonoBlack,
                hasShadow = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                    Text("NEW GOAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("WHAT ARE YOU SAVING FOR?") },
                        modifier = Modifier.fillMaxWidth().neoShadow(),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight,
                            focusedLabelColor = MonoBlack
                        )
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it.filter { c -> c.isDigit() } },
                        label = { Text("TARGET AMOUNT (₹)") },
                        modifier = Modifier.fillMaxWidth().neoShadow(),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight,
                            focusedLabelColor = MonoBlack
                        )
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("CATEGORY") },
                        modifier = Modifier.fillMaxWidth().neoShadow(),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight,
                            focusedLabelColor = MonoBlack
                        )
                    )
                    
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("CANCEL", color = MonoGrayMedium) }
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        BlockButton(
                            text = "START",
                            onClick = { if (title.isNotEmpty() && target.isNotEmpty()) onConfirm(title, target, category) },
                            modifier = Modifier.height(48.dp).width(120.dp)
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContributeDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            BlockCard(
                modifier = Modifier.fillMaxWidth(0.9f).padding(Dimens.SpacingLg),
                borderColor = MonoBlack,
                hasShadow = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                    Text("CONTRIBUTE TO ${goalTitle.uppercase()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("CONTRIBUTION AMOUNT (₹)") },
                        modifier = Modifier.fillMaxWidth().neoShadow(),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight,
                            focusedLabelColor = MonoBlack
                        )
                    )
                    
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("CANCEL", color = MonoGrayMedium) }
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        BlockButton(
                            text = "CONFIRM",
                            onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                            modifier = Modifier.height(48.dp).width(120.dp),
                            enabled = amount.isNotEmpty()
                        )
                    }
                }
            }
        }
    )
}

