package com.example.spend_trend.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.neoShadow
import com.example.spend_trend.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
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
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Dimens.Spacing3xl))

            // ── Brand header ──
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
                text = "NEW ACCOUNT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "JOIN SPENDTREND TO MASTER YOUR FINANCES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MonoGrayMedium
            )

            Spacer(Modifier.height(Dimens.SpacingHuge))

            // ── Form fields ──
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)) {
                Column {
                    Text("FULL NAME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it },
                        modifier = Modifier.fillMaxWidth().neoShadow(),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight
                        )
                    )
                }

                Column {
                    Text("EMAIL ADDRESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(Dimens.RadiusLg),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight
                        )
                    )
                }

                Column {
                    Text("PASSWORD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight
                        )
                    )
                }

                Column {
                    Text("CONFIRM PASSWORD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { viewModel.confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = MonoGrayLight
                        )
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingHuge))

            // ── Register button ──
            BlockButton(
                text = "CREATE ACCOUNT",
                onClick = { viewModel.register(onRegisterSuccess) },
                modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
                enabled = !viewModel.isLoading,
                isLoading = viewModel.isLoading
            )

            // ── Error ──
            AnimatedVisibility(visible = viewModel.error != null) {
                Text(
                    text = viewModel.error?.uppercase() ?: "",
                    color = ExpenseRose,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = Dimens.SpacingSm)
                )
            }

            Spacer(Modifier.height(Dimens.SpacingLg))

            // ── Login link ──
            BlockButton(
                text = "ALREADY REGISTERED? LOG IN",
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )

            Spacer(Modifier.height(Dimens.SpacingHuge))
        }
    }
}

