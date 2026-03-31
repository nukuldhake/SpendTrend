package com.example.spend_trend.ui.copilot

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CopilotScreen() {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: CopilotViewModel = viewModel(
        factory = CopilotViewModelFactory(
            txRepository = TransactionRepository(db.transactionDao()),
            budgetRepository = BudgetRepository(db.budgetDao())
        )
    )

    var inputText by remember { mutableStateOf("") }
    val messages = viewModel.messages
    val isTyping = viewModel.isTyping

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Neumorphic Header ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            cornerRadius = 0.dp,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Primary, Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AI",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text(
                        "SpendTrend Copilot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "AI financial assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Messages ──
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                NeumorphicChatBubble(msg)
            }

            if (isTyping) {
                item { PulsingTypingIndicator() }
            }
        }

        // ── Neumorphic Input Bar ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(88.dp).padding(Dimens.SpacingSm),
            cornerRadius = 44.dp,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicCard(
                    modifier = Modifier.weight(1f).height(48.dp),
                    isConcave = true,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    cornerRadius = 24.dp
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask about your money...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(Modifier.width(Dimens.SpacingSm))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(Dimens.BottomNavClearance))
    }
}

@Composable
private fun NeumorphicChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingLg),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        NeumorphicCard(
            modifier = Modifier.widthIn(max = 280.dp),
            isConcave = false, // Flat surface for both — AI bubbles need readable text, not shadow-obscured text
            elevation = if (isUser) 4.dp else 2.dp,
            cornerRadius = 16.dp,
            backgroundColor = if (isUser) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) Primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
        modifier = Modifier.padding(start = Dimens.SpacingXxl, top = Dimens.SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            // Lightweight Box instead of NeumorphicCard — no GPU shadow recalculation per frame
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = alpha))
            )
        }
        Spacer(Modifier.width(Dimens.SpacingSm))
        Text(
            "Copilot is thinking…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}