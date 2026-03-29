package com.example.spend_trend.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.spend_trend.data.UserPreferences
import com.example.spend_trend.ui.components.GlassCard
import com.example.spend_trend.ui.components.GlassTopBar
import com.example.spend_trend.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var isEditingName by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf(UserPreferences.getName() ?: "User") }
    val userEmail = UserPreferences.getEmail() ?: "user@example.com"
    
    val memberSinceMillis = UserPreferences.getMemberSinceMillis()
    val memberSinceDate = try {
        Instant.ofEpochMilli(memberSinceMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } catch (e: Exception) {
        "March 2026"
    }

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
                userName.firstOrNull()?.uppercase() ?: "U",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.width(200.dp)
                )
                Spacer(Modifier.width(Dimens.SpacingSm))
                IconButton(onClick = { 
                    if (userName.isNotBlank()) {
                        UserPreferences.updateName(userName)
                        ThemePreferences.updateUserName(userName) 
                    }
                    isEditingName = false 
                }) {
                    Icon(Icons.Default.Check, "Save", tint = Primary)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { isEditingName = true }) {
                    Icon(Icons.Default.Edit, "Edit name", tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Text(userEmail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(Dimens.SpacingHuge))

        // Session Information Cards
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.CalendarToday, "Member since", tint = Primary, modifier = Modifier.size(Dimens.IconMd)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Member since", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(memberSinceDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Security, "Security", tint = Secondary, modifier = Modifier.size(Dimens.IconMd)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Session Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Active (Secure)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                UserPreferences.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRose),
            border = BorderStroke(1.dp, ExpenseRose.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Logout, "Log out")
            Spacer(Modifier.width(Dimens.SpacingSm))
            Text("Log Out", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}