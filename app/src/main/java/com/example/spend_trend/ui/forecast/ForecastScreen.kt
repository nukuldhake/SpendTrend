package com.example.spend_trend.ui.forecast

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*
import com.example.spend_trend.data.model.ForecastInsight
import com.example.spend_trend.data.model.InsightType
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(onBack: () -> Unit = {}) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: ForecastViewModel = viewModel(
        factory = ForecastViewModelFactory(TransactionRepository(db.transactionDao()))
    )

    val forecastData by viewModel.forecastData.collectAsState()
    
    val months = (0..5).map {
        LocalDate.now().plusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("MMM")).uppercase()
    }
    val monthFullNames = (0..5).map {
        LocalDate.now().plusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase()
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
    ) {
        com.example.spend_trend.ui.components.BlockTopBar(
            title = "Forecast",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingLg)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
        ) {
            Spacer(Modifier.height(Dimens.SpacingSm))

            // ── Noir Hero ──
        BlockCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MonoBlack
        ) {
            Column {
                Text(
                    "LINEAR ANNUAL RUN-RATE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MonoWhite.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(Dimens.SpacingXs))
                Text(
                    "₹${projectedYearEnd.formatWithComma()}",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 38.sp),
                    fontWeight = FontWeight.Black,
                    color = MonoWhite
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (trendPercentage > 0) ExpenseRose else IncomeGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (trendPercentage > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (trendPercentage > 0) MonoBlack else MonoWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text(
                        if (trendPercentage > 0) "+$trendPercentage% VS LAST YEAR" else "$trendPercentage% VS LAST YEAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (trendPercentage > 0) ExpenseRose else IncomeGreen
                    )
                }
            }
        }

        // ── Forecast Chart ──
        BlockCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "SPENDING FORECAST",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
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
            BlockCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PROJECTION FOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MonoBlack.copy(alpha = 0.6f)
                        )
                        Text(
                            monthFullNames[selectedMonthIndex],
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MonoBlack
                        )
                    }
                    Text(
                        "₹${forecastData[selectedMonthIndex].roundToInt().formatWithComma()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MonoBlack
                    )
                }
            }
        }

        // ── Monthly Breakdown ──
        Text(
            "MONTHLY BREAKDOWN",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
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

                BlockCard(modifier = Modifier.width(140.dp)) {
                    Column {
                        Text(
                            month,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MonoGrayMedium
                        )
                        Spacer(Modifier.height(Dimens.SpacingSm))
                        Text(
                            "₹${amount.formatWithComma()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MonoBlack
                        )
                        if (index > 0) {
                            Spacer(Modifier.height(Dimens.SpacingSm))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val indicatorColor = if (change > 0) ExpenseRose else if (change < 0) IncomeGreen else MonoGrayLight
                                val indicatorIcon = if (change > 0) Icons.Default.ArrowUpward else if (change < 0) Icons.Default.ArrowDownward else Icons.Default.Remove
                                
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(indicatorColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        indicatorIcon,
                                        contentDescription = null,
                                        tint = if (change == 0) MonoBlack else MonoWhite,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(Modifier.width(Dimens.SpacingXs))
                                Text(
                                    "${if (change > 0) "+" else ""}$change%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
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
                "AI SPENDING INSIGHTS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Box(
                modifier = Modifier
                    .border(2.dp, MonoBlack)
                    .background(MonoWhite)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "GEMINI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MonoBlack
                    )
                }
            }
        }

        insights.forEach { insight ->
            BlockCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (insight.type) {
                        InsightType.POSITIVE -> Icons.Default.CheckCircle
                        InsightType.NEGATIVE -> Icons.AutoMirrored.Filled.TrendingUp
                        InsightType.WARNING -> Icons.Default.WarningAmber
                        else -> Icons.Default.Lightbulb
                    }
                    val color = when (insight.type) {
                        InsightType.POSITIVE -> IncomeGreen
                        InsightType.NEGATIVE -> ExpenseRose
                        InsightType.WARNING -> WarningAmber
                        else -> MonoBlack
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon, 
                            contentDescription = null, 
                            tint = if (color == MonoWhite) MonoBlack else MonoWhite, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(Dimens.SpacingMd))
                    
                    Column {
                        Text(
                            insight.title.uppercase(), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Black, 
                            color = color
                        )
                        Text(
                            insight.description.uppercase(), 
                            style = MaterialTheme.typography.bodySmall, 
                            fontWeight = FontWeight.Black,
                            color = MonoGrayMedium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.BottomNavClearance))
    }
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

    val lineColor = Primary
    val gridColor = MonoGrayLight.copy(alpha = 0.4f)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ChartHeight)
                .background(MaterialTheme.colorScheme.surface)
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
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Trend line (Noir style - sharp steps and solid line)
            val linePath = Path().apply {
                data.forEachIndexed { index, value ->
                    val x = xStep * index
                    val y = size.height - ((value - minValue) * yScale * animatedProgress)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = linePath, 
                color = lineColor, 
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Butt)
            )

            // Dynamic points
            data.forEachIndexed { index, value ->
                val x = xStep * index
                val y = size.height - ((value - minValue) * yScale * animatedProgress)
                val isSelected = index == selectedIndex
                
                if (isSelected) {
                    drawRect(
                        color = Primary,
                        topLeft = Offset(x - 8.dp.toPx(), y - 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 16.dp.toPx())
                    )
                    drawRect(
                        color = MonoBlack,
                        topLeft = Offset(x - 6.dp.toPx(), y - 6.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = Dimens.BorderWidthStandard.value)
                    )
                } else {
                    drawRect(
                        color = MonoBlack,
                        topLeft = Offset(x - 4.dp.toPx(), y - 4.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 8.dp.toPx())
                    )
                }
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
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                }
        }
    }
}