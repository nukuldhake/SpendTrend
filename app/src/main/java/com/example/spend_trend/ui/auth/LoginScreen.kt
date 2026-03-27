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
        // Gradient background ornament
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

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
                // Not registered yet - Invite to Register
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold)
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
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password"
                    )
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (viewModel.loginWithPassword()) onLoginSuccess() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Login", fontWeight = FontWeight.Bold)
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
                val size by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isActive) 20.dp else 16.dp,
                    label = "dotSize"
                )
                val color by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary 
                                 else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    label = "dotColor"
                )
                
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Glassmorphic Keypad Container
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
                
                for (row in 0..3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (col in 0..2) {
                            val key = keys[row * 3 + col]
                            KeypadButton(key) {
                                when (key) {
                                    "C" -> { if (viewModel.pin.isNotEmpty()) viewModel.pin = viewModel.pin.dropLast(1) }
                                    "OK" -> { if (viewModel.loginWithPin()) onLoginSuccess() }
                                    else -> { if (viewModel.pin.length < 4) viewModel.pin += key }
                                }
                                if (viewModel.pin.length == 4 && key != "C" && key != "OK") {
                                    if (viewModel.loginWithPin()) onLoginSuccess()
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Prominent Password Option
        OutlinedButton(
            onClick = onSwitchToPassword,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Login with Password", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .size(72.dp)
            .padding(4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text == "C") {
            Icon(Icons.Default.Backspace, contentDescription = "Clear", modifier = Modifier.size(24.dp))
        } else if (text == "OK") {
            Icon(Icons.Default.Check, contentDescription = "OK", modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
