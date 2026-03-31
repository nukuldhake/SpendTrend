package com.example.spend_trend.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.theme.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check

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
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (!isConfirming) "Set Your Quick PIN" else "Confirm Your PIN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = if (!isConfirming) "Choose a 4-digit PIN to secure your account"
                       else "Re-enter your PIN to confirm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(48.dp))

            // Neumorphic Pin Dots Indicator
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentPin = if (!isConfirming) viewModel.pin else viewModel.confirmPin
                repeat(4) { index ->
                    val isActive = currentPin.length > index
                    NeumorphicCard(
                        modifier = Modifier.size(20.dp),
                        cornerRadius = 10.dp,
                        elevation = if (isActive) 0.dp else 4.dp,
                        isConcave = isActive,
                        backgroundColor = if (isActive) Primary else MaterialTheme.colorScheme.background
                    ) {
                        // Card handles visual state
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Custom Layout for Numeric Keypad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0..3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            KeypadButton(key) {
                                when (key) {
                                    "⌫" -> {
                                        if (isConfirming) {
                                            if (viewModel.confirmPin.isNotEmpty()) viewModel.confirmPin = viewModel.confirmPin.dropLast(1)
                                        } else {
                                            if (viewModel.pin.isNotEmpty()) viewModel.pin = viewModel.pin.dropLast(1)
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
                                            // If setPin() returns false, error is shown below
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
                                // NOTE: Auto-advance removed. User MUST press OK to proceed.
                            }
                        }
                    }
                }
            }

            // ── Back / Reset Button (Confirm Step) ──
            if (isConfirming) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        // Reset both PINs and go back to first entry
                        viewModel.pin = ""
                        viewModel.confirmPin = ""
                        viewModel.error = null
                        isConfirming = false
                    }
                ) {
                    Text(
                        "↩ Re-enter PIN",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (viewModel.error != null) {
                Text(
                    text = viewModel.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
