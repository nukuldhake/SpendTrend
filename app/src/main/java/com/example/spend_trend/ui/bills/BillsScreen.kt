package com.example.spend_trend.ui.bills

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.neoShadow
import com.example.spend_trend.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BillsScreen(
    onBack: () -> Unit = {}, 
    onMenuClick: (() -> Unit)? = null,
    onAddClick: () -> Unit = {}
) {
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
    val tabs = listOf("PENDING", "HISTORY")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            com.example.spend_trend.ui.components.BlockTopBar(
                title = "Bills",
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MonoBlack,
                contentColor = MonoWhite,
                shape = RoundedCornerShape(Dimens.RadiusLg),
                modifier = Modifier
                    .border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusLg))
                    .neoShadow(RoundedCornerShape(Dimens.RadiusLg))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingMd))

            // ── Noir Custom TabRow ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusFull))
                .background(MonoWhite, RoundedCornerShape(Dimens.RadiusFull))
                .clip(RoundedCornerShape(Dimens.RadiusFull))
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (selectedTab == index) MonoBlack else MonoWhite)
                        .clickable { selectedTab = index }
                        .padding(vertical = Dimens.SpacingMd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTab == index) MonoWhite else MonoBlack
                    )
                }
                if (index < tabs.size - 1) {
                    Box(modifier = Modifier.width(Dimens.BorderWidthStandard).fillMaxHeight().background(MonoBlack))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            val displayList = if (selectedTab == 0) pendingBills else paidBills

            if (displayList.isEmpty()) {
                item {
                    EmptyBillsState(isHistory = selectedTab == 1)
                }
            } else {
                items(displayList, key = { it.id }) { bill ->
                    BillBlockRow(
                        bill = bill,
                        onPay = { viewModel.markAsPaid(bill) }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun BillBlockRow(
    bill: BillEntity,
    onPay: () -> Unit
) {
    val dateStr = try {
        Instant.ofEpochMilli(bill.dueDateMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            .uppercase()
    } catch (e: Exception) {
        "UNKNOWN DATE"
    }

    val isOverdue = !bill.isPaid && bill.dueDateMillis < System.currentTimeMillis()

    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Block
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, if (isOverdue) ExpenseRose else MonoBlack)
                    .background(if (isOverdue) ExpenseRose.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (bill.isPaid) Icons.Default.Verified else Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = if (bill.isPaid) IncomeGreen else if (isOverdue) ExpenseRose else MonoBlack,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(Dimens.SpacingMd))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bill.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )
                Text(
                    if (bill.isPaid) "PAID ON $dateStr" else "DUE ON $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) ExpenseRose else MonoGrayMedium
                )
            }

            // Amount + Pay button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${bill.amount.toInt().formatWithComma()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )
                if (!bill.isPaid) {
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    BlockButton(
                        text = "PAY",
                        onClick = onPay,
                        modifier = Modifier.height(32.dp).width(80.dp),
                        isPrimary = true
                    )
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
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlockCard(modifier = Modifier.size(100.dp), hasShadow = true) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MonoBlack
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpacingLg))
        Text(
            if (isHistory) "NO HISTORY" else "NO PENDING BILLS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MonoBlack
        )
        Text(
            if (isHistory) "PAID BILLS WILL ACCUMULATE HERE" else "YOU ARE ALL CAUGHT UP",
            style = MaterialTheme.typography.labelSmall,
            color = MonoGrayMedium
        )
    }
}

