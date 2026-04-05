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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
fun ForecastScreen(onBack: () -> Unit = {}, onMenuClick: (() -> Unit)? = null) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: ForecastViewModel = viewModel(
        factory = ForecastViewModelFactory(TransactionRepository(db.transactionDao()))
    )

    val forecastData by viewModel.forecastData.collectAsState()
    val insights by viewModel.insights.collectAsState()
    
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            com.example.spend_trend.ui.components.BlockTopBar(
                title = "Forecast",
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Spacer(Modifier.height(Dimens.SpacingMd))
            }

            // ── Linear Run-Rate Card ──
            item {
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
                                    .background(if (trendPercentage > 0) ExpenseRose else IncomeGreen, RoundedCornerShape(Dimens.RadiusXs))
                                    .border(Dimens.BorderWidthStandard, MonoBlack, RoundedCornerShape(Dimens.RadiusXs)),
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
            }

            // ── Forecast Chart Section ──
            item {
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
            }

            // ── Tooltip Card ──
            item {
                if (selectedMonthIndex >= 0) {
                    BlockCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Primary
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    monthFullNames[selectedMonthIndex],
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MonoBlack.copy(alpha = 0.7f)
                                )
                                Text(
                                    "₹${forecastData[selectedMonthIndex].roundToInt().formatWithComma()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MonoBlack
                                )
                            }
                        }
                    }
                }
            }

            // ── Monthly Breakdown Title ──
            item {
                Text(
                    "MONTHLY BREAKDOWN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Dimens.SpacingMd)
                )
            }

            // ── Horizontal Cards ──
            item {
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

                        BlockCard(modifier = Modifier.width(160.dp)) {
                            Column {
                                Text(month, fontWeight = FontWeight.Black, color = MonoGrayMedium)
                                Spacer(Modifier.height(Dimens.SpacingSm))
                                Text("₹${amount.formatWithComma()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                if (index > 0) {
                                    Spacer(Modifier.height(Dimens.SpacingXs))
                                    Text(
                                        "${if (change >= 0) "+" else ""}$change%",
                                        color = if (change > 0) ExpenseRose else if (change < 0) IncomeGreen else MonoGrayMedium,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── AI Insights Section ──
            item {
                Text(
                    "AI INSIGHTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Dimens.SpacingLg)
                )
            }

            items(insights) { insight ->
                BlockCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingSm),
                    backgroundColor = when (insight.type) {
                        InsightType.POSITIVE -> Color(0xFFD1FAE5)
                        InsightType.NEGATIVE -> Color(0xFFFFE4E6)
                        InsightType.WARNING -> Color(0xFFFEF3C7)
                        InsightType.NEUTRAL -> MonoWhite
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (insight.type) {
                            InsightType.POSITIVE -> Icons.Default.CheckCircle
                            InsightType.NEGATIVE -> Icons.Default.Cancel
                            InsightType.WARNING -> Icons.Default.Warning
                            InsightType.NEUTRAL -> Icons.Default.Info
                        }
                        val tint = when (insight.type) {
                            InsightType.POSITIVE -> Color(0xFF059669)
                            InsightType.NEGATIVE -> Color(0xFFE11D48)
                            InsightType.WARNING -> Color(0xFFD97706)
                            InsightType.NEUTRAL -> MonoBlack
                        }
                        Icon(icon, null, tint = tint)
                        Spacer(Modifier.width(Dimens.SpacingMd))
                        Column {
                            Text(insight.title.uppercase(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall)
                            Text(insight.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastChart(
    data: List<Float>,
    labels: List<String>,
    animated: Boolean,
    onPointSelected: (Int) -> Unit
) {
    if (data.isEmpty()) return
    
    val maxVal = (data.maxOrNull() ?: 100f).coerceAtLeast(10f)
    val animationProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
    )

    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val step = size.width / (data.size - 1).coerceAtLeast(1)
                        val index = (offset.x / step).roundToInt().coerceIn(0, data.size - 1)
                        onPointSelected(index)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1).coerceAtLeast(1)
            
            val points = data.mapIndexed { index, value ->
                Offset(
                    x = index * stepX,
                    y = height - (value / maxVal * height * animationProgress)
                )
            }

            // Fill Path
            val fillPath = Path().apply {
                moveTo(0f, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(width, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Primary.copy(alpha = 0.3f), Color.Transparent)
                )
            )

            // Line Path
            val strokePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = strokePath,
                color = Primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dots
            points.forEach { pt ->
                drawCircle(color = MonoBlack, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = Primary, radius = 3.dp.toPx(), center = pt)
            }
        }
    }
    
    // Labels
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingSm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}