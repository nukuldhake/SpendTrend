package com.example.spend_trend.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassTopBar
import com.example.spend_trend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit = {}) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(ThemePreferences.userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassTopBar(title = "Profile", onBack = onBack)
        Spacer(Modifier.height(Dimens.SpacingLg))
        // Gradient Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Primary, Secondary))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                ThemePreferences.userName.firstOrNull()?.uppercase() ?: "N",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        if (isEditingName) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { 
                    if (tempName.isNotBlank()) ThemePreferences.updateUserName(tempName)
                    isEditingName = false 
                }) {
                    Icon(Icons.Default.Check, "Save", tint = Primary)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ThemePreferences.userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { tempName = ThemePreferences.userName; isEditingName = true }) {
                    Icon(Icons.Default.Edit, "Edit name", tint = Primary)
                }
            }
        }

        Text("nukul@example.com", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(Dimens.SpacingHuge))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.CalendarToday, "Member since", tint = Primary, modifier = Modifier.size(Dimens.IconMd)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Member since", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("March 2026", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.AttachMoney, "Currency", tint = Secondary, modifier = Modifier.size(Dimens.IconMd)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Preferred currency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("INR (Indian Rupee)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRose),
            border = BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Logout, "Log out")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Log Out")
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}