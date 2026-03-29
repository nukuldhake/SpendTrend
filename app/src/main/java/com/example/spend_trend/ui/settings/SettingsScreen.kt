package com.example.spend_trend.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.transaction.TransactionViewModel
import com.example.spend_trend.ui.transaction.TransactionViewModelFactory
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassTopBar
import com.example.spend_trend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            repository = TransactionRepository(AppDatabase.getDatabase(context).transactionDao())
        )
    )

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedCurrency by remember { mutableStateOf("INR") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        GlassTopBar(title = "Settings", onBack = onBack)

        Text("Automation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Auto-track transactions", style = MaterialTheme.typography.titleMedium)
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
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    Text("Budget limits & unusual spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
            }
        }

        Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Currency", style = MaterialTheme.typography.titleMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCurrency, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().width(120.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("INR", "USD", "EUR", "GBP").forEach { currency ->
                            DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; expanded = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(Dimens.SpacingLg))
            Button(
                onClick = { viewModel.exportToCsv(context) }, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Download, "Export CSV")
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text("Export Data (CSV)")
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Your data stays on your device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}