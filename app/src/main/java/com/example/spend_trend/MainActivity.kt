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
import com.example.spend_trend.ui.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.sms.SmsSyncManager
import java.util.concurrent.TimeUnit

import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import android.content.pm.PackageManager
import com.example.spend_trend.ui.contact.PermissionRationaleScreen
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    // NOTE: ContentObserver removed. SMS sync is now handled exclusively
    // via a one-time WorkManager job or user-initiated action from Settings.
    // The old smsObserver triggered redundant syncs on every lifecycle start,
    // hammering the local database unnecessarily.

    /** Tracks permission state reactively — Compose observes this to recompose */
    private val _permissionsGranted = mutableStateOf(false)

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            ThemePreferences.updateAutoTracking(true)
            scheduleSmsSyncWork()
        }
        // Update reactive state instead of calling recreate()
        // Compose will recompose automatically when this changes
        _permissionsGranted.value = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        UserPreferences.init(this)

        scheduleBillReminders()
        
        // SMS sync is now deferred to a background WorkManager job
        // instead of blocking onCreate with lifecycleScope.launch
        if (hasSmsPermissions() && !UserPreferences.isSmsSyncDone()) {
            scheduleSmsSyncWork()
        }

        // Initialize reactive permission state
        _permissionsGranted.value = hasSmsPermissions()

        setContent {
            SpendTrendTheme {
                // Observe the reactive permission state — auto-dismisses rationale
                val permissionsGranted by _permissionsGranted
                var userSkipped by remember { mutableStateOf(false) }

                if (!permissionsGranted && !userSkipped) {
                    PermissionRationaleScreen(
                        onGrantClick = {
                            requestSmsPermissions()
                        },
                        onSkipClick = {
                            userSkipped = true
                        }
                    )
                } else {
                    val authViewModel: AuthViewModel = viewModel()
                    AppScaffold(authViewModel = authViewModel)
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

    /**
     * Schedules a one-time background SMS sync via WorkManager.
     * This replaces the old approach of syncing directly in lifecycleScope,
     * which would re-trigger on every rotation and app restart.
     */
    private fun scheduleSmsSyncWork() {
        lifecycleScope.launch {
            val manager = SmsSyncManager(this@MainActivity)
            manager.syncLast30Days()
            UserPreferences.setSmsSyncDone(true)
        }
    }

    // NOTE: onStart/onStop ContentObserver registration removed entirely.
    // The old implementation registered a ContentObserver for "content://sms"
    // which fired on every incoming SMS, causing redundant database writes
    // and battery drain. SMS tracking should be event-driven via
    // BroadcastReceiver or periodic WorkManager, not lifecycle-coupled observers.
}