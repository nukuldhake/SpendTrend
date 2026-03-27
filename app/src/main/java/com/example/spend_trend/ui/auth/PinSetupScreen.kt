package com.example.spend_trend.ui.auth

import androidx.compose.foundation.background
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
                text = "Secure your account with a 4-digit PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(48.dp))

            // Pin Dots Indicator
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentPin = if (!isConfirming) viewModel.pin else viewModel.confirmPin
                repeat(4) { index ->
                    val isActive = currentPin.length > index
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Custom Layout for Numeric Keypad
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0..3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            KeypadButton(key) {
                                when (key) {
                                    "C" -> {
                                        if (isConfirming) {
                                            if (viewModel.confirmPin.isNotEmpty()) viewModel.confirmPin = viewModel.confirmPin.dropLast(1)
                                        } else {
                                            if (viewModel.pin.isNotEmpty()) viewModel.pin = viewModel.pin.dropLast(1)
                                        }
                                    }
                                    "OK" -> {
                                        if (!isConfirming && viewModel.pin.length == 4) {
                                            isConfirming = true
                                        } else if (isConfirming && viewModel.confirmPin.length == 4) {
                                            if (viewModel.setPin()) onSetupComplete()
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
                                // Auto-advance logic
                                if (!isConfirming && viewModel.pin.length == 4 && key != "C" && key != "OK") {
                                    isConfirming = true
                                } else if (isConfirming && viewModel.confirmPin.length == 4 && key != "C" && key != "OK") {
                                    if (viewModel.setPin()) onSetupComplete()
                                }
                            }
                        }
                    }
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
