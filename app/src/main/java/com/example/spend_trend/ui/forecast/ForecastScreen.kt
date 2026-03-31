package com.example.spend_trend.ui.forecast

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.NeumorphicCard
import com.example.spend_trend.ui.theme.*
import com.example.spend_trend.data.model.ForecastInsight
import com.example.spend_trend.data.model.InsightType
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun ForecastScreen() {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: ForecastViewModel = viewModel(
        factory = ForecastViewModelFactory(TransactionRepository(db.transactionDao()))
    )

    val forecastData by viewModel.forecastData.collectAsState()
    
    val months = (0..5).map {
        LocalDate.now().plusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("MMM"))
    }
    val monthFullNames = (0..5).map {
        LocalDate.now().plusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    val projectedYearEnd = if (forecastData.all { it == 0f }) 0 else (forecastData.last() * 12).roundToInt()
    val trendPercentage = if (forecastData.first() == 0f) 0 else ((forecastData.last() - forecastData.first()) / forecastData.first() * 100).roundToInt()

    var selectedMonthIndex by remember { mutableStateOf(-1) }
    var showAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); showAnimation = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg)
            .padding(vertical = Dimens.SpacingLg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        // ── Neumorphic Hero ──
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 12.dp, // High elevation for hero
            cornerRadius = Dimens.RadiusLg
        ) {
            Column(modifier = Modifier.padding(Dimens.SpacingSm)) {
                Text(
                    "Linear Annual Run-Rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    "₹${projectedYearEnd.formatWithComma()}",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (trendPercentage > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = "Trend direction",
                        tint = if (trendPercentage > 0) ExpenseRose else IncomeGreen,
                        modifier = Modifier.size(Dimens.IconSm)
                    )
                    Spacer(Modifier.width(Dimens.SpacingXs))
                    Text(
                        if (trendPercentage > 0) "+$trendPercentage% vs last year" else "$trendPercentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (trendPercentage > 0) ExpenseRose else IncomeGreen
                    )
                }
            }
        }

        // ── Neumorphic Chart ──
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Spending Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Dimens.SpacingLg))
            ForecastChart(
                data = forecastData,
                labels = months,
                animated = showAnimation,
                onPointSelected = { selectedMonthIndex = it }
            )
        }

        // ── Tooltip ──
        if (selectedMonthIndex >= 0) {
            NeumorphicCard(modifier = Modifier.fillMaxWidth(), isConcave = true, backgroundColor = MaterialTheme.colorScheme.background) {
                Text(
                    "${monthFullNames[selectedMonthIndex]} Projection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "₹${forecastData[selectedMonthIndex].roundToInt().formatWithComma()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }

        // ── Monthly Breakdown ──
        Text(
            "Monthly Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
        ) {
            months.forEachIndexed { index, month ->
                val amount = forecastData[index].roundToInt()
                val prevAmount = if (index > 0) forecastData[index - 1] else 0f
                val change = if (index > 0 && prevAmount != 0f) {
                    ((amount - prevAmount) / prevAmount * 100).roundToInt()
                } else 0

                NeumorphicCard(modifier = Modifier.width(130.dp), elevation = 4.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            month,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(Dimens.SpacingSm))
                        Text(
                            "₹${amount.formatWithComma()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        if (index > 0) {
                            Spacer(Modifier.height(Dimens.SpacingXs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val indicatorColor = if (change > 0) ExpenseRose else if (change < 0) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                val indicatorIcon = if (change > 0) Icons.Default.ArrowUpward else if (change < 0) Icons.Default.ArrowDownward else Icons.Default.Remove
                                
                                Icon(
                                    indicatorIcon,
                                    contentDescription = "Change indicator",
                                    tint = indicatorColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "${if (change > 0) "+" else ""}$change%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = indicatorColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── AI Insights ──
        val insights by viewModel.insights.collectAsState()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AI Spending Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            NeumorphicCard(
                elevation = 2.dp,
                cornerRadius = 12.dp,
                isConcave = true,
                backgroundColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.offset(y = (-2).dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(Dimens.SpacingXs))
                    Text(
                        "Gemini",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }

        insights.forEach { insight ->
            NeumorphicCard(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (insight.type) {
                        InsightType.POSITIVE -> Icons.Default.CheckCircle
                        InsightType.NEGATIVE -> Icons.Default.TrendingUp
                        InsightType.WARNING -> Icons.Default.WarningAmber
                        else -> Icons.Default.Lightbulb
                    }
                    val color = when (insight.type) {
                        InsightType.POSITIVE -> IncomeGreen
                        InsightType.NEGATIVE -> ExpenseRose
                        InsightType.WARNING -> WarningAmber
                        else -> Primary
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(Dimens.IconMd))
                    }
                    
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    
                    Column {
                        Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
                        Text(insight.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.BottomNavClearance))
    }
}

@Composable
fun ForecastChart(
    data: List<Float>,
    labels: List<String>,
    animated: Boolean = true,
    onPointSelected: (Int) -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        label = "chart_anim"
    )
    var selectedIndex by remember { mutableStateOf(-1) }

    val lineColor = Secondary
    val glowColor = Secondary.copy(alpha = 0.3f)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ChartHeight)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val xStep = size.width / (data.size - 1)
                        val tappedIndex = (offset.x / xStep)
                            .roundToInt()
                            .coerceIn(0, data.size - 1)
                        selectedIndex = tappedIndex
                        onPointSelected(tappedIndex)
                    }
                }
        ) {
            val maxValue = (data.maxOrNull() ?: 1f) * 1.15f
            val minValue = (data.minOrNull() ?: 0f) * 0.85f
            val xStep = size.width / (data.size - 1).coerceAtLeast(1)
            val yRange = (maxValue - minValue).coerceAtLeast(1f)
            val yScale = size.height / yRange

            // Grid lines
            for (i in 0..4) {
                val y = size.height * (i.toFloat() / 4)
                drawLine(
                    color = outlineVariant.copy(alpha = 0.15f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Gradient fill
            val areaPath = Path().apply {
                moveTo(0f, size.height)
                data.forEachIndexed { index, value ->
                    val x = xStep * index
                    val y = size.height - ((value - minValue) * yScale * animatedProgress)
                    lineTo(x, y)
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent)
                )
            )

            // Glow line
            val linePath = Path().apply {
                data.forEachIndexed { index, value ->
                    val x = xStep * index
                    val y = size.height - ((value - minValue) * yScale * animatedProgress)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(path = linePath, color = glowColor, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
            drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

            // Points
            data.forEachIndexed { index, value ->
                val x = xStep * index
                val y = size.height - ((value - minValue) * yScale * animatedProgress)
                val isSelected = index == selectedIndex
                drawCircle(
                    color = glowColor,
                    radius = if (isSelected) 12.dp.toPx() else 8.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
            }
        }

        // X labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.SpacingSm),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}