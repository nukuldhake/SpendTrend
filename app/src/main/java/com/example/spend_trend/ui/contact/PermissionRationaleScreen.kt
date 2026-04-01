package com.example.spend_trend.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spend_trend.ui.components.BlockButton
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*

@Composable
fun PermissionRationaleScreen(
    onGrantClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoWhite)
            .padding(Dimens.SpacingXxl)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Header
            BlockCard(
                modifier = Modifier.size(96.dp),
                backgroundColor = Primary
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        tint = MonoWhite,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingHuge))
            
            Text(
                text = "AUTOMATE YOUR TRACKING",
                style = MaterialTheme.typography.headlineMedium,
                color = MonoBlack,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLg))
            
            Text(
                text = "SPENDTREND WORKS BEST WHEN IT CAN AUTOMATICALLY LOG YOUR EXPENSES FROM BANK SMS NOTIFICATIONS.",
                style = MaterialTheme.typography.labelSmall,
                color = MonoGrayMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(Dimens.Spacing3xl))
            
            // Feature List
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxl)) {
                RationaleItem(
                    icon = Icons.Default.Timeline,
                    title = "REAL-TIME INSIGHTS",
                    description = "SEE YOUR SPENDING TRENDS INSTANTLY AS YOU SPEND."
                )
                
                RationaleItem(
                    icon = Icons.Default.Security,
                    title = "PRIVACY FIRST",
                    description = "WE ONLY PARSE TRANSACTION SMS. DATA STAYS ON YOUR DEVICE."
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.Spacing3xl + Dimens.SpacingXxl))
            
            // Buttons
            BlockButton(
                text = "ENABLE AUTO-TRACKING",
                onClick = onGrantClick,
                modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget)
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLg))
            
            BlockButton(
                text = "LOG MANUALLY FOR NOW",
                onClick = onSkipClick,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )
        }
    }
}

@Composable
private fun RationaleItem(icon: ImageVector, title: String, description: String) {
    BlockCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(Dimens.MinTouchTarget).border(Dimens.BorderWidthStandard, MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(Dimens.IconMd)
                )
            }
            
            Spacer(modifier = Modifier.width(Dimens.SpacingLg))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MonoBlack,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MonoGrayMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

