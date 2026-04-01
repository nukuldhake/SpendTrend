package com.example.spend_trend.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingXxl)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Brand Logo Area ──
            BlockCard(
                modifier = Modifier.size(80.dp),
                backgroundColor = MonoBlack,
                hasShadow = true,
                shadowColor = Primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "₹",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MonoWhite
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            Text(
                text = "SPENDTREND",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (!isRegistered) "MASTER YOUR FINANCES"
                       else "WELCOME BACK, ${viewModel.registeredName?.uppercase() ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MonoGrayMedium
            )

            Spacer(Modifier.height(Dimens.Spacing3xl))

            if (!isRegistered) {
                // Not registered — email + password login
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)) {
                    Column {
                        Text("EMAIL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(Dimens.SpacingSm))
                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MonoGrayLight
                            )
                        )
                    }

                    PasswordLoginContent(
                        viewModel = viewModel,
                        onLoginSuccess = onLoginSuccess,
                        onSwitchToPin = null,
                        passwordVisible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible }
                    )

                    Spacer(Modifier.height(Dimens.SpacingXxl))

                    BlockButton(
                        text = "CREATE ACCOUNT",
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false
                    )
                }
            } else {
                // Registered — show name/email + PIN or password
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.registeredName?.uppercase() ?: "WELCOME",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = viewModel.registeredEmail?.uppercase() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MonoGrayMedium
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingXxl))

                AnimatedContent(
                    targetState = showPasswordLogin,
                    label = "loginType",
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it / 4 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { -it / 4 })
                    }
                ) { showPassword ->
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

            // Error display
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
fun PasswordLoginContent(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSwitchToPin: (() -> Unit)?,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)) {
        Column {
            Text("PASSWORD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(Dimens.SpacingSm))
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.ui.graphics.RectangleShape,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MonoGrayLight
                )
            )
        }

        BlockButton(
            text = "SIGN IN",
            onClick = { viewModel.loginWithPassword(onLoginSuccess) },
            modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
            enabled = !viewModel.isLoading,
            isLoading = viewModel.isLoading
        )

        if (onSwitchToPin != null) {
            BlockButton(
                text = "USE PIN",
                onClick = onSwitchToPin,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )
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
        modifier = Modifier.padding(vertical = Dimens.SpacingLg)
    ) {
        // ── PIN dots ──
        Row(
            modifier = Modifier.padding(Dimens.SpacingLg),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            repeat(4) { index ->
                val isActive = viewModel.pin.length > index
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
                                            if (viewModel.pin.isNotEmpty()) viewModel.pin = viewModel.pin.dropLast(1)
                                        }
                                        "OK" -> {
                                            if (viewModel.loginWithPin()) onLoginSuccess()
                                        }
                                        else -> {
                                            if (viewModel.pin.length < 4) viewModel.pin += key
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

        Spacer(Modifier.height(Dimens.SpacingXxl))

        BlockButton(
            text = "USE PASSWORD",
            onClick = onSwitchToPassword,
            modifier = Modifier.fillMaxWidth(),
            isPrimary = false
        )
    }
}
