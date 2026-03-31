package com.example.spend_trend.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NeumorphicTopBar(title = "Profile", onBack = onBack)
        
        Spacer(Modifier.height(Dimens.SpacingXxl))
        
        // ── Neumorphic Avatar ──
        NeumorphicCard(
            modifier = Modifier.size(120.dp),
            cornerRadius = 60.dp,
            elevation = 12.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.SpacingMd)
            ) {
                NeumorphicCard(
                    modifier = Modifier.weight(1f).height(56.dp),
                    isConcave = true,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    cornerRadius = 28.dp
                ) {
                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxSize(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                Spacer(Modifier.width(Dimens.SpacingMd))
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
                Text(
                    userName, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isEditingName = true }) {
                    Icon(Icons.Default.Edit, "Edit name", tint = Primary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Text(
            userEmail, 
            style = MaterialTheme.typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Dimens.SpacingHuge))

        // ── Info Cards ──
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.CalendarToday, "Member since", tint = Primary, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Member since", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(memberSinceDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingMd))

        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Dimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Security, "Security", tint = Secondary, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(Dimens.SpacingMd))
                Column {
                    Text("Session Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Active (Secure)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Logout Button ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth().height(56.dp).clickable {
                UserPreferences.logout()
                onLogout()
            },
            cornerRadius = 28.dp,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, "Log out", tint = ExpenseRose)
                Spacer(Modifier.width(Dimens.SpacingSm))
                Text("Log Out", color = ExpenseRose, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}