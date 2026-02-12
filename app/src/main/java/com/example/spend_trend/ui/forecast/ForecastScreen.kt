package com.example.spend_trend.ui.forecast

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spend_trend.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun ForecastScreen() {
    // Fake forecast data – later from ML model
    val forecastData = listOf(8200f, 9100f, 9800f, 10500f, 11200f, 11800f)
    val months = (0..5).map {
        LocalDate.now().plusMonths(it.toLong())
            .format(DateTimeFormatter.ofPattern("MMM"))
    }
    val monthFullNames = (0..5).map {
        LocalDate.now().plusMonths(it.toLong())
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    val avgSpend = forecastData.average().roundToInt()
    val highest = forecastData.maxOrNull()?.roundToInt() ?: 0
    val projectedYearEnd = (forecastData.last() * 12).roundToInt()
    val trendPercentage = ((forecastData.last() - forecastData.first()) / forecastData.first() * 100).roundToInt()

    var selectedMonthIndex by remember { mutableStateOf(-1) }
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showAnimation = true
    }

    Scaffold(
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ────────────────────────────────────────────────
            // Hero / Title Card – replaces plain text header
            // Looks premium, no floating text, strong visual entry
            // ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "Spending Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            }

            // Big projected total card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Projected Year-End Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${projectedYearEnd.formatWithComma()}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (trendPercentage > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (trendPercentage > 0) colorScheme.error else colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = if (trendPercentage > 0) "+$trendPercentage% vs last year" else "$trendPercentage% vs last year",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (trendPercentage > 0) colorScheme.error else colorScheme.primary
                    )
                }
            }

            // Main Chart with tooltip
            ForecastChart(
                data = forecastData,
                labels = months,
                fullLabels = monthFullNames,
                animated = showAnimation,
                onPointSelected = { index -> selectedMonthIndex = index }
            )

            // Selected month info (tooltip-like)
            if (selectedMonthIndex >= 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${monthFullNames[selectedMonthIndex]} Projection",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "₹${forecastData[selectedMonthIndex].roundToInt().formatWithComma()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "Monthly Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                months.forEachIndexed { index, month ->
                    val amount = forecastData[index].roundToInt()
                    val change = if (index > 0) {
                        ((amount - forecastData[index - 1]) / forecastData[index - 1] * 100).roundToInt()
                    } else 0

                    ElevatedCard(
                        modifier = Modifier
                            .width(140.dp)
                            .height(160.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "₹${amount.formatWithComma()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            if (index > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (change >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (change >= 0) colorScheme.error else colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (change >= 0) "+${change}%" else "$change%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (change >= 0) colorScheme.error else colorScheme.primary
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }

            // Risk / Insight banner
            val riskLevel = when {
                trendPercentage > 20 -> "High risk of overspending"
                trendPercentage > 5 -> "Moderate increase expected"
                else -> "Stable spending trend"
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        trendPercentage > 20 -> colorScheme.errorContainer
                        trendPercentage > 5 -> colorScheme.tertiaryContainer
                        else -> colorScheme.primaryContainer
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (trendPercentage > 5) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(riskLevel, style = MaterialTheme.typography.titleMedium)
                        Text("Consider reviewing subscriptions and discretionary spending", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// Your existing ForecastChart function remains unchanged (it's already very good)

@Composable
fun ForecastChart(
    data: List<Float>,
    labels: List<String>,
    fullLabels: List<String>,
    animated: Boolean = true,
    onPointSelected: (Int) -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        label = "chart animation"
    )

    var selectedIndex by remember { mutableStateOf(-1) }

    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val xStep = size.width / (data.size - 1)
                                val tappedIndex = (offset.x / xStep).roundToInt().coerceIn(0, data.size - 1)
                                selectedIndex = tappedIndex
                                onPointSelected(tappedIndex)
                            }
                        }
                ) {
                    val maxValue = (data.maxOrNull() ?: 1f) * 1.15f
                    val minValue = (data.minOrNull() ?: 0f) * 0.85f
                    val xStep = size.width / (data.size - 1).coerceAtLeast(1)
                    val yRange = maxValue - minValue
                    val yScale = size.height / yRange

                    // Grid lines
                    val gridCount = 5
                    for (i in 0..gridCount) {
                        val y = size.height * (i.toFloat() / gridCount)
                        drawLine(
                            color = outlineVariant.copy(alpha = 0.2f),
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
                            colors = listOf(
                                primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )

                    // Line
                    val linePath = Path().apply {
                        data.forEachIndexed { index, value ->
                            val x = xStep * index
                            val y = size.height - ((value - minValue) * yScale * animatedProgress)
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = primary,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Points
                    data.forEachIndexed { index, value ->
                        val x = xStep * index
                        val y = size.height - ((value - minValue) * yScale * animatedProgress)
                        drawCircle(
                            color = if (index == selectedIndex) Color.White else primary,
                            radius = if (index == selectedIndex) 10.dp.toPx() else 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = primary,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Keep your existing formatWithComma, etc.
fun Int.formatWithComma(): String = toString().reversed().chunked(3).joinToString(",").reversed()