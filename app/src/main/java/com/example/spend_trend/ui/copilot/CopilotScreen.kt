package com.example.spend_trend.ui.copilot

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.GoalRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.components.neoShadow
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CopilotScreen(onBack: () -> Unit = {}, onMenuClick: (() -> Unit)? = null) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: CopilotViewModel = viewModel(
        factory = CopilotViewModelFactory(
            txRepository = TransactionRepository(db.transactionDao()),
            budgetRepository = BudgetRepository(db.budgetDao()),
            billRepository = BillRepository(db.billDao()),
            goalRepository = GoalRepository(db.goalDao())
        )
    )

    var inputText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val isTyping = viewModel.isTyping
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            com.example.spend_trend.ui.components.BlockTopBar(
                title = "Copilot",
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Messages ──
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.SpacingLg),
                state = listState,
                contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = Dimens.SpacingLg),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
            ) {
                // Welcome message if empty
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingHuge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BlockCard(
                                modifier = Modifier.size(64.dp),
                                backgroundColor = Primary
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "AI",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MonoWhite
                                    )
                                }
                            }
                            Spacer(Modifier.height(Dimens.SpacingLg))
                            Text(
                                "SpendTrend Copilot",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Your Financial Sidekick",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MonoGrayMedium
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubble(msg)
                }

                if (isTyping) {
                    item { PulsingTypingIndicator() }
                }
            }

            // ── Input Area ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpacingLg)
                    .padding(bottom = Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { 
                        Text("Ask anything...", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) 
                    },
                    modifier = Modifier.weight(1f).neoShadow(),
                    shape = RoundedCornerShape(Dimens.RadiusLg),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoWhite,
                        unfocusedContainerColor = MonoWhite,
                        focusedBorderColor = MonoBlack,
                        unfocusedBorderColor = MonoGrayLight,
                        cursorColor = MonoBlack
                    )
                )

                Spacer(Modifier.width(Dimens.SpacingSm))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(if (inputText.isNotBlank()) Primary else MonoGrayLight, RoundedCornerShape(Dimens.RadiusLg))
                        .border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.RadiusLg))
                        .clip(RoundedCornerShape(Dimens.RadiusLg))
                        .clickable(enabled = inputText.isNotBlank()) {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) MonoWhite else MonoBlack.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        BlockCard(
            modifier = Modifier.widthIn(max = 300.dp),
            backgroundColor = if (isUser) MaterialTheme.colorScheme.surface else MonoBlack,
            borderColor = MaterialTheme.colorScheme.outline
        ) {
            Column {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = if (isUser) MaterialTheme.colorScheme.onSurface else MonoWhite,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Text(
                    message.time,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = (if (isUser) MonoGrayLight else MonoGrayMedium),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun PulsingTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier.padding(top = Dimens.SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
    ) {
        repeat(3) { index ->
            val sizeScale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MonoBlack.copy(alpha = sizeScale))
            )
        }
        Spacer(Modifier.width(Dimens.SpacingSm))
        Text(
            "Copilot is thinking…",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MonoGrayMedium
        )
    }
}