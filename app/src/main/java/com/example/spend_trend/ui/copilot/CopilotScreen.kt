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
import com.example.spend_trend.ui.components.GlassCard
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

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Glass Header ──
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = Dimens.RadiusSm
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            contentPadding = PaddingValues(Dimens.SpacingLg, Dimens.SpacingMd, Dimens.SpacingLg, Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                GlassChatBubble(msg)
            }

            if (isTyping) {
                item { PulsingTypingIndicator() }
            }
        }

        // ── Glass Input Bar ──
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = Dimens.RadiusSm
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Ask anything about your money...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = Dimens.SpacingSm),
                    shape = RoundedCornerShape(Dimens.RadiusLg),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedBorderColor = Primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = if (inputText.isNotBlank()) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    // Bot reply logic is moved to CopilotViewModel
}

@Composable
private fun GlassChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        val bgModifier = if (isUser) {
            Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.RadiusMd,
                        topEnd = Dimens.RadiusMd,
                        bottomEnd = Dimens.SpacingXs,
                        bottomStart = Dimens.RadiusMd
                    )
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary.copy(alpha = 0.8f), Secondary.copy(alpha = 0.6f))
                    )
                )
        } else {
            Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.RadiusMd,
                        topEnd = Dimens.RadiusMd,
                        bottomEnd = Dimens.RadiusMd,
                        bottomStart = Dimens.SpacingXs
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(bgModifier)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                message.time,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun PulsingTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier.padding(start = Dimens.SpacingLg, top = Dimens.SpacingSm),
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

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String
)