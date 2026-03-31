package com.example.spend_trend.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        NeumorphicTopBar(title = "Settings", onBack = onBack)

        Text("Automation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)

        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Auto-track transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Read bank SMS & emails", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                    )
                }
                
                Spacer(Modifier.height(Dimens.SpacingLg))
                
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Budget limits & unusual spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                }

                if (notificationsEnabled) {
                    Spacer(Modifier.height(Dimens.SpacingLg))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    Spacer(Modifier.height(Dimens.SpacingLg))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Warning Threshold", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${ThemePreferences.lowThreshold}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                        }
                        Slider(
                            value = ThemePreferences.lowThreshold.toFloat(),
                            onValueChange = { ThemePreferences.updateThresholds(it.toInt(), ThemePreferences.highThreshold) },
                            valueRange = 50f..100f,
                            steps = 9,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                        )

                        Spacer(Modifier.height(Dimens.SpacingXs))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Critical Threshold", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${ThemePreferences.highThreshold}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = ExpenseRose)
                        }
                        Slider(
                            value = ThemePreferences.highThreshold.toFloat(),
                            onValueChange = { ThemePreferences.updateThresholds(ThemePreferences.lowThreshold, it.toInt()) },
                            valueRange = 80f..150f,
                            steps = 13,
                            colors = SliderDefaults.colors(thumbColor = ExpenseRose, activeTrackColor = ExpenseRose)
                        )
                    }
                }
            }
        }

        Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)

        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Currency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedCurrency, onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.width(120.dp).menuAnchor(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("INR", "USD", "EUR", "GBP").forEach { currency ->
                                DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; expanded = false })
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(Dimens.SpacingXxl))
                
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clickable {
                        coroutineScope.launch {
                            viewModel.exportToCsv(context)
                        }
                    },
                    cornerRadius = 28.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, "Export CSV", tint = Primary)
                        Spacer(Modifier.width(Dimens.SpacingSm))
                        Text("Export Data (CSV)", fontWeight = FontWeight.Black, color = Primary)
                    }
                }
            }
        }

        Text("Cloud Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)

        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                if (user != null) {
                    Text("Linked to: ${user.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clickable {
                            coroutineScope.launch {
                                // For now, sync is automatic on each change. 
                                // A full sync to Supabase can be implemented here if needed.
                                Toast.makeText(context, "Cloud sync is active!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        cornerRadius = 28.dp,
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudSync, "Sync Active", tint = Primary)
                            Spacer(Modifier.width(Dimens.SpacingSm))
                            Text("Cloud Sync Active", fontWeight = FontWeight.Black, color = Primary)
                        }
                    }
                } else {
                    Text("Please sign in to enable cloud sync.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        
        if (user != null) {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth().height(56.dp).clickable {
                    coroutineScope.launch {
                        auth.signOut()
                        UserPreferences.setLoggedIn(false)
                        onBack()
                    }
                },
                backgroundColor = ExpenseRose.copy(alpha = 0.1f),
                cornerRadius = 28.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, "Logout", tint = ExpenseRose)
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text("Logout", fontWeight = FontWeight.Bold, color = ExpenseRose)
                }
            }
        }
    }
}
