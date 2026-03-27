package com.example.spend_trend.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassTopBar
import com.example.spend_trend.ui.theme.*

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
    ) {
        GlassTopBar(title = "Help & FAQ", onBack = onBack)

        GlassHelpItem("How do I add a transaction?", "Tap the + button or go to Transactions and tap the FAB. Auto-tracking from SMS can be enabled in Settings.")
        GlassHelpItem("Why is my balance not updating?", "Ensure transactions are correctly added. Check SMS permissions if auto-tracking is enabled.")
        GlassHelpItem("Is my data safe?", "All data stays on your device unless you enable cloud sync. We never share personal info.")
        GlassHelpItem("How do I change the theme?", "Open the side drawer → tap Theme → choose Light, Dark, or System.")

        Spacer(Modifier.weight(1f))

        Text("Still need help?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Icon(Icons.Default.Email, "Contact support")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Contact Support")
        }
    }
}

@Composable
private fun GlassHelpItem(question: String, answer: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Dimens.SpacingSm))
        Text(answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}