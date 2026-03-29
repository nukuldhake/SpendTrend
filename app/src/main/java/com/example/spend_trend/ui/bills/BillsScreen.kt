package com.example.spend_trend.ui.bills

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable as composeClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BillsScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: BillViewModel = viewModel(
        factory = BillViewModelFactory(
            BillRepository(db.billDao()),
            TransactionRepository(db.transactionDao())
        )
    )

    val allBills by viewModel.allBills.collectAsState()
    val pendingBills = allBills.filter { !it.isPaid }
    val paidBills = allBills.filter { it.isPaid }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        Spacer(Modifier.height(Dimens.SpacingSm))

        // ── Tabs ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
            listOf("Pending", "History").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Primary else Color.Transparent)
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            contentPadding = PaddingValues(bottom = Dimens.BottomNavClearance)
        ) {
            val displayList = if (selectedTab == 0) pendingBills else paidBills

            if (displayList.isEmpty()) {
                item {
                    EmptyBillsState(isHistory = selectedTab == 1)
                }
            } else {
                items(displayList, key = { it.id }) { bill ->
                    BillCard(
                        bill = bill,
                        onPay = { viewModel.markAsPaid(bill) },
                        onDelete = { viewModel.deleteBill(bill) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BillCard(
    bill: BillEntity,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = try {
        Instant.ofEpochMilli(bill.dueDateMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (e: Exception) {
        "Unknown Date"
    }

    val isOverdue = !bill.isPaid && bill.dueDateMillis < System.currentTimeMillis()

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (bill.isPaid) IncomeGreen.copy(alpha = 0.15f)
                        else if (isOverdue) ExpenseRose.copy(alpha = 0.15f)
                        else Primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.Payment,
                    contentDescription = null,
                    tint = if (bill.isPaid) IncomeGreen else if (isOverdue) ExpenseRose else Primary
                )
            }

            Spacer(Modifier.width(Dimens.SpacingMd))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bill.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (bill.isPaid) "Paid on $dateStr" else "Due on $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) ExpenseRose else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${bill.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!bill.isPaid) {
                    TextButton(
                        onClick = onPay,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("PAY NOW", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBillsState(isHistory: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp)
            .graphicsLayer(alpha = 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text(
            if (isHistory) "No payment history yet" else "All bills are paid! 🎉",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (isHistory) "Paid bills will appear here" else "You're all caught up with your utilities",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// Extension to allow clickable on any modifier
private fun Modifier.clickable(onClick: () -> Unit) = composed {
    this.composeClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
