package com.example.spend_trend

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.*
import com.example.spend_trend.data.worker.BillReminderWorker
import com.example.spend_trend.ui.navigation.AppScaffold
import com.example.spend_trend.ui.theme.ThemePreferences
import com.example.spend_trend.ui.theme.SpendTrendTheme
import com.example.spend_trend.data.UserPreferences
import java.util.concurrent.TimeUnit

import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.spend_trend.ui.contact.PermissionRationaleScreen

class MainActivity : ComponentActivity() {

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            ThemePreferences.updateAutoTracking(true)
        }
        recreate() 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        UserPreferences.init(this)
        
        // Removed forced logout if it was for testing; if intended keep it.
        // UserPreferences.setLoggedIn(false) 

        scheduleBillReminders()

        setContent {
            SpendTrendTheme {
                var showRationale by remember { 
                    mutableStateOf(!hasSmsPermissions()) 
                }

                if (showRationale) {
                    PermissionRationaleScreen(
                        onGrantClick = {
                            requestSmsPermissions()
                        },
                        onSkipClick = {
                            showRationale = false
                        }
                    )
                } else {
                    AppScaffold()
                }
            }
        }
    }

    private fun hasSmsPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        smsPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun scheduleBillReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val billWorkRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BillReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            billWorkRequest
        )
    }
}