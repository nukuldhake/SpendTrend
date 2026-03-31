package com.example.spend_trend.ui.contact

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.components.NeumorphicTopBar
import com.example.spend_trend.ui.theme.*

@Composable
fun ContactUsScreen(onBack: () -> Unit = {}) {
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
    ) {
        NeumorphicTopBar(title = "Contact Us", onBack = onBack)
        
        Text(
            "We usually reply within 24–48 hours.", 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Dimens.SpacingSm)
        )

        // ── Email Input ──
        NeumorphicCard(isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
            TextField(
                value = email, 
                onValueChange = { email = it },
                label = { Text("Your email") },
                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        // ── Message Input ──
        NeumorphicCard(
            modifier = Modifier.height(200.dp),
            isConcave = true, 
            backgroundColor = MaterialTheme.colorScheme.background
        ) {
            TextField(
                value = message, 
                onValueChange = { message = it },
                label = { Text("Your message") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(Modifier.height(Dimens.SpacingSm))

        // ── Send Button ──
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = message.isNotBlank()) { /* Send logic */ },
            cornerRadius = 28.dp,
            elevation = if (message.isNotBlank()) 6.dp else 2.dp,
            backgroundColor = if (message.isNotBlank()) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Send Message", 
                    fontWeight = FontWeight.Black, 
                    color = if (message.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Footer Info ──
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Dimens.SpacingMd)) {
                Text("Other ways to reach us", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(Dimens.SpacingMd))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Email, "Email", tint = Primary, modifier = Modifier.size(Dimens.IconSm))
                    }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Text("support@spendtrend.app", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(Dimens.SpacingSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Phone, "Phone", tint = Secondary, modifier = Modifier.size(Dimens.IconSm))
                    }
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingHuge))
    }
}