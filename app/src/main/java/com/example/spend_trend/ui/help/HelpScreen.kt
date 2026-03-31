package com.example.spend_trend.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
import com.example.spend_trend.ui.theme.*

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
    ) {
        NeumorphicTopBar(title = "Help & FAQ", onBack = onBack)

        Spacer(Modifier.height(Dimens.SpacingSm))

        NeumorphicHelpItem("How do I add a transaction?", "Tap the + button or go to Transactions and tap the FAB. Auto-tracking from SMS can be enabled in Settings.")
        NeumorphicHelpItem("Why is my balance not updating?", "Ensure transactions are correctly added. Check SMS permissions if auto-tracking is enabled.")
        NeumorphicHelpItem("Is my data safe?", "All data stays on your device unless you enable cloud sync. We never share personal info.")
        NeumorphicHelpItem("How do I change the theme?", "Open the side drawer → tap Theme → choose Light, Dark, or System.")

        Spacer(Modifier.weight(1f))

        Text(
            "Still need help?", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Dimens.SpacingSm)
        )

        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(56.dp).clickable { /* Contact logic */ },
            cornerRadius = 28.dp,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, "Contact support", tint = Primary)
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text("Contact Support", fontWeight = FontWeight.Black, color = Primary)
            }
        }
        
        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}

@Composable
private fun NeumorphicHelpItem(question: String, answer: String) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
            Text(question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Dimens.SpacingSm))
            Text(answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}