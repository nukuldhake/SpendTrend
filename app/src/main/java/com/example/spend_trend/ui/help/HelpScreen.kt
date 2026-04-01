package com.example.spend_trend.ui.help

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.theme.*

private data class FaqItem(val question: String, val answer: String)

private val faqItems = listOf(
    FaqItem(
        "HOW DO I ADD A TRANSACTION?",
        "TAP THE + BUTTON ON THE DASHBOARD OR GO TO TRANSACTIONS AND TAP THE FAB. AUTO-TRACKING FROM SMS CAN BE ENABLED IN SETTINGS."
    ),
    FaqItem(
        "WHY IS MY BALANCE NOT UPDATING?",
        "ENSURE TRANSACTIONS ARE CORRECTLY ADDED. CHECK SMS PERMISSIONS IF AUTO-TRACKING IS ENABLED. PULL DOWN ON THE DASHBOARD TO REFRESH."
    ),
    FaqItem(
        "IS MY DATA SAFE?",
        "ALL DATA STAYS ON YOUR DEVICE UNLESS YOU ENABLE CLOUD SYNC. WE NEVER SHARE PERSONAL INFO. YOUR FINANCIAL DATA IS ENCRYPTED AT REST."
    ),
    FaqItem(
        "HOW DO I CHANGE THE THEME?",
        "OPEN THE SIDE DRAWER → TAP THEME → CHOOSE LIGHT, DARK, OR SYSTEM. THE THEME CHANGES INSTANTLY ACROSS ALL SCREENS."
    ),
    FaqItem(
        "HOW DOES BUDGET TRACKING WORK?",
        "GO TO BUDGETS → TAP + TO CREATE A CATEGORY BUDGET WITH A MONTHLY LIMIT. SPENDTREND WILL AUTOMATICALLY TRACK SPENDING AGAINST YOUR LIMITS AND WARN YOU WHEN APPROACHING THE THRESHOLD."
    ),
    FaqItem(
        "CAN I EXPORT MY DATA?",
        "YES! GO TO SETTINGS → PREFERENCES → EXPORT DATA (CSV). YOUR TRANSACTIONS WILL BE SAVED AS A CSV FILE TO YOUR DEVICE'S DOWNLOADS FOLDER."
    ),
    FaqItem(
        "WHAT DOES THE COPILOT DO?",
        "SPENDTREND COPILOT IS AN AI ASSISTANT THAT ANALYZES YOUR SPENDING PATTERNS AND PROVIDES PERSONALIZED FINANCIAL INSIGHTS, TIPS, AND ANSWERS TO YOUR MONEY-RELATED QUESTIONS."
    ),
    FaqItem(
        "HOW DO I SET FINANCIAL GOALS?",
        "GO TO GOALS → NEW GOAL. ENTER A TITLE, TARGET AMOUNT, AND CATEGORY. YOU CAN TRACK YOUR PROGRESS AND ADD CONTRIBUTIONS OVER TIME."
    )
)

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            BlockTopBar(
                title = "HELP & FAQ",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MonoBlack)
                    }
                }
            )
        },
        containerColor = MonoWhite
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MonoWhite)
                .padding(horizontal = Dimens.SpacingLg),
            contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = Dimens.SpacingSm)) {
                    Text(
                        "FIND ANSWERS TO COMMON QUESTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MonoGrayMedium
                    )
                }
            }

            items(faqItems) { faq ->
                ExpandableFaqItem(faq.question, faq.answer)
            }

            item {
                Spacer(Modifier.height(Dimens.SpacingLg))
                Text(
                    "STILL NEED HELP?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )
                Spacer(Modifier.height(Dimens.SpacingMd))
                BlockButton(
                    text = "CONTACT SUPPORT",
                    onClick = { /* Navigate or open email */ },
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
                )
            }
        }
    }
}

@Composable
private fun ExpandableFaqItem(question: String, answer: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "faq_rotation"
    )

    BlockCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        borderColor = if (isExpanded) MonoBlack else MonoGrayLight
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    question,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MonoBlack
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MonoBlack,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(300))
            ) {
                Column {
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    Box(Modifier.fillMaxWidth().height(2.dp).background(MonoBlack))
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    Text(
                        answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MonoGrayMedium,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
    }
}