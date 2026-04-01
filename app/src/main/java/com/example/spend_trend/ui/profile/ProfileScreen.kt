package com.example.spend_trend.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.data.UserPreferences
import com.example.spend_trend.ui.components.*
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
    var userName by remember { mutableStateOf(UserPreferences.getName() ?: "USER") }
    val userEmail = UserPreferences.getEmail() ?: "USER@SPENDTREND.AI"
    
    val memberSinceMillis = UserPreferences.getMemberSinceMillis()
    val memberSinceDate = try {
        Instant.ofEpochMilli(memberSinceMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            .uppercase()
    } catch (e: Exception) {
        "MARCH 2026"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlockTopBar(
            title = "PROFILE",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
        
        Spacer(Modifier.height(Dimens.SpacingHuge))
        
        // ── Noir Block Avatar ──
        BlockCard(
            modifier = Modifier.size(120.dp),
            hasShadow = true,
            shadowColor = Primary
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.SpacingLg)
            ) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MonoGrayLight
                    )
                )
                Spacer(Modifier.width(Dimens.SpacingMd))
                IconButton(onClick = { 
                    if (userName.isNotBlank()) {
                        UserPreferences.updateName(userName)
                        ThemePreferences.updateUserName(userName) 
                    }
                    isEditingName = false 
                }) {
                    Icon(Icons.Default.CheckCircle, "Save", tint = Primary)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    userName.uppercase(), 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { isEditingName = true }) {
                    Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }
        }

        Text(
            userEmail.uppercase(), 
            style = MaterialTheme.typography.labelSmall, 
            color = MonoGrayMedium
        )

        Spacer(Modifier.height(Dimens.SpacingHuge))

        // ── Info Blocks ──
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(32.dp).border(Dimens.BorderWidthThin, MaterialTheme.colorScheme.outline).padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurface) }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Column {
                        Text("MEMBER SINCE", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        Text(memberSinceDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                    }
                }
            }

            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(32.dp).border(Dimens.BorderWidthThin, MaterialTheme.colorScheme.outline).padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Shield, null, tint = Primary) }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Column {
                        Text("SECURITY STATUS", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                        Text("ACTIVE (ENCRYPTED)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Logout Button ──
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingLg)) {
            BlockButton(
                text = "SYSTEM LOGOUT",
                onClick = {
                    UserPreferences.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true
            )
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}