package com.example.spend_trend.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
import com.example.spend_trend.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ArrowForward

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val isRegistered = viewModel.isRegistered
    val hasPin = viewModel.hasPin
    var showPasswordLogin by remember { mutableStateOf(!hasPin) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle background ornament replaced with soft shadow logic if needed, 
        // but keeping it simple as SoftZinc is the hero.

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SpendTrend",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (!isRegistered) "Master your finances" else "Welcome Back, ${viewModel.registeredName ?: ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(48.dp))

            if (!isRegistered) {
                // Not registered locally - Allow Email Login or Register
                Column {
                    NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
                        TextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    PasswordLoginContent(
                        viewModel = viewModel,
                        onLoginSuccess = onLoginSuccess,
                        onSwitchToPin = null,
                        passwordVisible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible }
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
                        Text("New here? Create an account")
                    }
                }
            } else {
                // Registered - Fixed Email Display
                Text(
                    text = viewModel.registeredName ?: "Welcome",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = viewModel.registeredEmail ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                
                Spacer(Modifier.height(24.dp))

                AnimatedContent(targetState = showPasswordLogin, label = "loginType") { showPassword ->
                    if (!showPassword && hasPin) {
                        PinLoginContent(
                            viewModel = viewModel,
                            onLoginSuccess = onLoginSuccess,
                            onSwitchToPassword = { showPasswordLogin = true }
                        )
                    } else {
                        PasswordLoginContent(
                            viewModel = viewModel,
                            onLoginSuccess = onLoginSuccess,
                            onSwitchToPin = if (hasPin) ({ showPasswordLogin = false }) else null,
                            passwordVisible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible }
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordLoginContent(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSwitchToPin: (() -> Unit)?,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column {
        NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
            TextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(56.dp).clickable(enabled = !viewModel.isLoading) { viewModel.loginWithPassword(onLoginSuccess) },
            cornerRadius = 16.dp,
            elevation = if (viewModel.isLoading) 0.dp else 6.dp,
            isConcave = viewModel.isLoading
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontWeight = FontWeight.Bold, color = Primary)
                }
            }
        }

        if (onSwitchToPin != null) {
            TextButton(
                onClick = onSwitchToPin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login with PIN instead")
            }
        }
    }
}

@Composable
fun PinLoginContent(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSwitchToPassword: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Pin Indicator with subtle animation
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                val isActive = viewModel.pin.length > index
                
                NeumorphicCard(
                    modifier = Modifier.size(20.dp),
                    cornerRadius = 10.dp,
                    elevation = if (isActive) 0.dp else 4.dp,
                    isConcave = isActive,
                    backgroundColor = if (isActive) Primary else MaterialTheme.colorScheme.background
                ) {
                    // Empty content, card does the work
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Neumorphic Keypad Container
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
                
                for (row in 0..3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            KeypadButton(key) {
                                when (key) {
                                    "⌫" -> { if (viewModel.pin.isNotEmpty()) viewModel.pin = viewModel.pin.dropLast(1) }
                                    "OK" -> { if (viewModel.loginWithPin()) onLoginSuccess() }
                                    else -> { if (viewModel.pin.length < 4) viewModel.pin += key }
                                }
                                // Auto-login removed: user must press OK explicitly
                            }
                        }
                    }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Prominent Password Option
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).clickable { onSwitchToPassword() },
            cornerRadius = 24.dp,
            elevation = 4.dp
        ) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Login with Password", style = MaterialTheme.typography.labelLarge, color = Primary)
            }
        }
    }
}
