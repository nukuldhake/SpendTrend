package com.example.spend_trend.ui.profile

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var name by remember { mutableStateOf("Nukul") }
    var email by remember { mutableStateOf("nukul@example.com") }
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(name) }
    var showAvatarOptions by remember { mutableStateOf(false) }

    Scaffold(
            ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar – tappable
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { showAvatarOptions = true }
                    .background(MaterialTheme.colorScheme.primaryContainer),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "N",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Name – tap to edit (with smooth animation)
            AnimatedContent(
                targetState = isEditingName,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))) togetherWith
                            (fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)))
                },
                label = "name_edit"
            ) { editing ->
                if (editing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (tempName.isNotBlank()) {
                                name = tempName
                            }
                            isEditingName = false
                        }) {
                            Icon(Icons.Default.Check, "Save name", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { isEditingName = false }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = {
                            tempName = name
                            isEditingName = true
                        }) {
                            Icon(Icons.Default.Edit, "Edit name", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Member since", style = MaterialTheme.typography.labelMedium)
                            Text("March 2026", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Preferred currency", style = MaterialTheme.typography.labelMedium)
                            Text("INR (Indian Rupee)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Logout
            OutlinedButton(
                onClick = { /* TODO: logout */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Avatar options bottom sheet
    if (showAvatarOptions) {
        ModalBottomSheet(onDismissRequest = { showAvatarOptions = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Change Avatar", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable { /* TODO: camera */ showAvatarOptions = false }
                )
                ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable { /* TODO: gallery */ showAvatarOptions = false }
                )
                ListItem(
                    headlineContent = { Text("Remove Avatar") },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { /* TODO: remove */ showAvatarOptions = false }
                )
            }
        }
    }
}