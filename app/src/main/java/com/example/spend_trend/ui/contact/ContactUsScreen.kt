package com.example.spend_trend.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.*

@Composable
fun ContactUsScreen(onBack: () -> Unit = {}) {
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BlockTopBar(
                title = "CONTACT US",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MonoBlack)
                    }
                }
            )
        },
        containerColor = MonoWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MonoWhite)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingSm))
            
            Text(
                "WE USUALLY REPLY WITHIN 24–48 HOURS.", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black,
                color = MonoGrayMedium
            )

            // ── Email Input ──
            Column {
                Text("YOUR EMAIL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(Dimens.SpacingSm))
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MonoGrayLight
                    )
                )
            }

            // ── Message Input ──
            Column {
                Text("YOUR MESSAGE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message, 
                    onValueChange = { message = it },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MonoGrayLight
                    )
                )
            }

            // ── Send Button ──
            BlockButton(
                text = "SEND MESSAGE",
                onClick = { /* Send logic */ },
                modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
                enabled = message.isNotBlank() && email.isNotBlank()
            )

            Spacer(Modifier.height(Dimens.SpacingSm))

            // ── Other Contact Info ──
            Text("OTHER WAYS TO REACH US", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                BlockCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Email, "Email", tint = Primary)
                        }
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        Text("SUPPORT@SPENDTREND.APP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                }

                BlockCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Phone, "Phone", tint = Primary)
                        }
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        Text("+91 98765 43210", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(Dimens.MinTouchTarget))
        }
    }
}