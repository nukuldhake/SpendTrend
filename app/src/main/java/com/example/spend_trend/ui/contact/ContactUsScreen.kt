package com.example.spend_trend.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassTopBar
import com.example.spend_trend.ui.theme.*

@Composable
fun ContactUsScreen(onBack: () -> Unit = {}) {
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
    ) {
        GlassTopBar(title = "Contact Us", onBack = onBack)
        Text("We usually reply within 24–48 hours.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Your email") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message, onValueChange = { message = it },
            label = { Text("Your message") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {}, enabled = message.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) { Text("Send Message") }

        Spacer(Modifier.weight(1f))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Other ways to reach us", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Dimens.SpacingMd))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, "Email", tint = Primary, modifier = Modifier.size(Dimens.IconSm))
                }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Text("support@spendtrend.app", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(Dimens.SpacingSm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Phone, "Phone", tint = Secondary, modifier = Modifier.size(Dimens.IconSm))
                }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}