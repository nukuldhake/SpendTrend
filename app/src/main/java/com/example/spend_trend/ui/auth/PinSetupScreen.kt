package com.example.spend_trend.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*

@Composable
fun PinSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isConfirming by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingXxl)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Step indicator ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepSquare(isActive = true, label = "1")
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(Dimens.BorderWidthStandard)
                        .background(MaterialTheme.colorScheme.outline)
                )
                StepSquare(isActive = isConfirming, label = "2")
            }

            Spacer(Modifier.height(Dimens.SpacingHuge))

            Text(
                text = (if (!isConfirming) "SET QUICK PIN" else "CONFIRM PIN"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = (if (!isConfirming) "CHOOSE A 4-DIGIT PIN TO SECURE YOUR ACCOUNT"
                       else "RE-ENTER YOUR PIN TO CONFIRM"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MonoGrayMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Dimens.Spacing3xl))

            // ── PIN dots (as squares) ──
            Row(
                modifier = Modifier.padding(Dimens.SpacingLg),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
            ) {
                val currentPin = if (!isConfirming) viewModel.pin else viewModel.confirmPin
                repeat(4) { index ->
                    val isActive = currentPin.length > index
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline)
                            .background(if (isActive) Primary else MaterialTheme.colorScheme.surface)
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingHuge))

            // ── Keypad ──
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0..3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                        modifier = Modifier.padding(vertical = Dimens.SpacingSm)
                    ) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline)
                                    .clickable {
                                        when (key) {
                                            "⌫" -> {
                                                if (isConfirming) {
                                                    if (viewModel.confirmPin.isNotEmpty())
                                                        viewModel.confirmPin = viewModel.confirmPin.dropLast(1)
                                                } else {
                                                    if (viewModel.pin.isNotEmpty())
                                                        viewModel.pin = viewModel.pin.dropLast(1)
                                                }
                                            }
                                            "OK" -> {
                                                if (!isConfirming && viewModel.pin.length == 4) {
                                                    viewModel.error = null
                                                    isConfirming = true
                                                } else if (isConfirming && viewModel.confirmPin.length == 4) {
                                                    if (viewModel.setPin()) {
                                                        onSetupComplete()
                                                    }
                                                }
                                            }
                                            else -> {
                                                if (!isConfirming) {
                                                    if (viewModel.pin.length < 4) viewModel.pin += key
                                                } else {
                                                    if (viewModel.confirmPin.length < 4) viewModel.confirmPin += key
                                                }
                                            }
                                        }
                                    }
                                    .background(if (key == "OK") Primary else MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (key == "OK") MonoWhite else MonoBlack
                                )
                            }
                        }
                    }
                }
            }

            // ── Back / Reset ──
            if (isConfirming) {
                Spacer(Modifier.height(Dimens.SpacingLg))
                BlockButton(
                    text = "RE-ENTER PIN",
                    onClick = {
                        viewModel.pin = ""
                        viewModel.confirmPin = ""
                        viewModel.error = null
                        isConfirming = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )
            }

            // ── Error ──
            AnimatedVisibility(visible = viewModel.error != null) {
                Text(
                    text = viewModel.error?.uppercase() ?: "",
                    color = ExpenseRose,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = Dimens.SpacingLg),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepSquare(isActive: Boolean, label: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(Dimens.BorderWidthStandard, if (isActive) Primary else MaterialTheme.colorScheme.outline)
            .background(if (isActive) Primary else MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = if (isActive) MonoWhite else MonoBlack
        )
    }
}

