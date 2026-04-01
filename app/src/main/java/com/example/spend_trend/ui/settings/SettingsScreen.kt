package com.example.spend_trend.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.UserPreferences
import com.example.spend_trend.data.network.SupabaseClient
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.theme.*
import com.example.spend_trend.ui.components.*
import com.example.spend_trend.ui.transaction.TransactionViewModel
import com.example.spend_trend.ui.transaction.TransactionViewModelFactory
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            repository = TransactionRepository(AppDatabase.getDatabase(context).transactionDao())
        )
    )
    val coroutineScope = rememberCoroutineScope()
    
    val auth = SupabaseClient.client.auth
    val user = auth.currentUserOrNull()

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedCurrency by remember { mutableStateOf("INR") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        BlockTopBar(
            title = "SETTINGS",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingSm))

            Text("AUTOMATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimens.SpacingXs)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("AUTO-TRACK", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            Text("READ BANK SMS & EMAILS", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        }
                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            val granted = permissions.getOrDefault(android.Manifest.permission.RECEIVE_SMS, false)
                            ThemePreferences.updateAutoTracking(granted)
                        }

                        Switch(
                            checked = ThemePreferences.autoTrackingEnabled, 
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    launcher.launch(arrayOf(
                                        android.Manifest.permission.RECEIVE_SMS,
                                        android.Manifest.permission.READ_SMS
                                    ))
                                } else {
                                    ThemePreferences.updateAutoTracking(false)
                                }
                            }, 
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MonoWhite,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = MonoGrayLight,
                                uncheckedTrackColor = MonoGrayLight
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(Dimens.SpacingLg))
                    
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("NOTIFICATIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            Text("BUDGET LIMITS & SPENDING", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        }
                        Switch(
                            checked = notificationsEnabled, 
                            onCheckedChange = { notificationsEnabled = it }, 
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MonoWhite,
                                checkedTrackColor = Primary
                            )
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(Modifier.height(Dimens.SpacingLg))
                        HorizontalDivider(thickness = Dimens.DividerThickness, color = MonoGrayLight)
                        Spacer(Modifier.height(Dimens.SpacingLg))

                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("WARNING THRESHOLD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                Text("${ThemePreferences.lowThreshold}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Slider(
                                value = ThemePreferences.lowThreshold.toFloat(),
                                onValueChange = { ThemePreferences.updateThresholds(it.toInt(), ThemePreferences.highThreshold) },
                                valueRange = 50f..100f,
                                steps = 9,
                                colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary, inactiveTrackColor = MonoGrayLight)
                            )

                            Spacer(Modifier.height(Dimens.SpacingXs))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CRITICAL THRESHOLD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                Text("${ThemePreferences.highThreshold}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = ExpenseRose)
                            }
                            Slider(
                                value = ThemePreferences.highThreshold.toFloat(),
                                onValueChange = { ThemePreferences.updateThresholds(ThemePreferences.lowThreshold, it.toInt()) },
                                valueRange = 80f..150f,
                                steps = 13,
                                colors = SliderDefaults.colors(thumbColor = ExpenseRose, activeTrackColor = ExpenseRose, inactiveTrackColor = MonoGrayLight)
                            )
                        }
                    }
                }
            }

            Text("PREFERENCES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimens.SpacingXs)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("THEME", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        var themeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = !themeExpanded }) {
                            OutlinedTextField(
                                value = ThemePreferences.themeMode.name, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                                modifier = Modifier.width(135.dp).menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                                shape = RectangleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = MonoGrayLight,
                                    focusedLabelColor = MonoBlack
                                )
                            )
                            ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                                ThemeMode.entries.forEach { mode ->
                                    DropdownMenuItem(text = { Text(mode.name) }, onClick = { 
                                        ThemePreferences.updateTheme(mode)
                                        themeExpanded = false 
                                    })
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(Dimens.SpacingLg))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CURRENCY", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedCurrency, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.width(120.dp).menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                                shape = RectangleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = MonoGrayLight,
                                    focusedLabelColor = MonoBlack
                                )
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("INR", "USD", "EUR", "GBP").forEach { currency ->
                                    DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; expanded = false })
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(Dimens.SpacingLg))
                    
                    BlockButton(
                        text = "EXPORT DATA (CSV)",
                        onClick = {
                            coroutineScope.launch {
                                viewModel.exportToCsv(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false
                    )
                }
            }

            Text("CLOUD SYNC", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimens.SpacingXs)) {
                    if (user != null) {
                        Text("LINKED: ${user.email?.uppercase()}", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        Spacer(Modifier.height(Dimens.SpacingMd))
                        
                        BlockButton(
                            text = "SYNC NOW",
                            onClick = {
                                coroutineScope.launch {
                                    Toast.makeText(context, "CLOUD SYNC ACTIVE", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    } else {
                        Text("SIGN IN TO ENABLE CLOUD SYNC", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            
            if (user != null) {
                BlockButton(
                    text = "LOGOUT",
                    onClick = {
                        coroutineScope.launch {
                            auth.signOut()
                            UserPreferences.setLoggedIn(false)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )
            }
            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }
}

