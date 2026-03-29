package com.example.spend_trend.ui.goals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.GoalEntity
import com.example.spend_trend.data.repository.GoalRepository
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GradientCard
import com.example.spend_trend.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalScreen() {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(GoalRepository(db.goalDao()))
    )
    
    val allGoals by viewModel.allGoals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
            contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = 100.dp)
        ) {
            item {
                Text(
                    "Financial Goals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Turn your dreams into reality",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (allGoals.isEmpty()) {
                item {
                    EmptyGoalsState()
                }
            } else {
                items(allGoals) { goal ->
                    GoalCard(goal, onIncrement = { viewModel.incrementProgress(goal, 500.0) })
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.SpacingLg)
                .padding(bottom = Dimens.BottomNavClearance),
            containerColor = Primary,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("New Goal")
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
private fun GoalCard(goal: GoalEntity, onIncrement: () -> Unit) {
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(goal.deadlineMillis))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            Icons.Default.Flag, 
                            contentDescription = null, 
                            tint = Primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Column {
                        Text(
                            goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            goal.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Save more", tint = Primary)
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "₹${goal.currentAmount.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Target: ₹${goal.targetAmount.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(Dimens.SpacingSm))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = Primary.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(Dimens.SpacingSm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Deadline: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(
                Icons.Default.Stars,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(24.dp)
            )
        }
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text(
            "No goals yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Save for what matters most",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are you saving for?") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty() && target.isNotEmpty()) onConfirm(title, target, category) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Start Saving", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
