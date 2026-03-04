package com.example.spend_trend.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    Scaffold(
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                HelpItem(
                    question = "How do I add a transaction?",
                    answer = "Tap the + button on Dashboard or go to Transactions and tap the FAB. You can also enable auto-tracking from SMS later."
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                HelpItem(
                    question = "Why is my balance not updating?",
                    answer = "Make sure your transactions are added correctly. If auto-tracking is on, check SMS permissions. Manual entries update instantly."
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                HelpItem(
                    question = "Is my data safe?",
                    answer = "Yes… all data stays on your device unless you enable cloud sync later. We never share personal info."
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                HelpItem(
                    question = "How do I change the theme?",
                    answer = "Open the side drawer (tap avatar) → tap Theme → choose Light, Dark, or System."
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Still need help?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Button(
                    onClick = { /* TODO: open contact us */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Contact Support")
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HelpItem(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}