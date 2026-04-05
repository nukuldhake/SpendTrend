package com.example.spend_trend.ui.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import androidx.compose.ui.text.style.TextOverflow
import com.example.spend_trend.ui.components.BlockTopBar
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen(onBack: () -> Unit = {}, onMenuClick: (() -> Unit)? = null) {
    val db = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: AnalyticsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(TransactionRepository(db.transactionDao())) as T
            }
        }
    )
    
    val categorySpent by viewModel.categoryDistribution.collectAsState()
    val yoyData by viewModel.yoyComparison.collectAsState()
    val dailyTrend by viewModel.dailyTrend.collectAsState()
    val cashFlow by viewModel.cashFlow.collectAsState()
    val topMerchants by viewModel.topMerchants.collectAsState()
    val selectedRange by viewModel.selectedTimeRange.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            BlockTopBar(
                title = "Analytics",
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
            contentPadding = PaddingValues(bottom = Dimens.BottomNavClearance)
        ) {
            item { Spacer(Modifier.height(Dimens.SpacingSm)) }

            // ── Time Slicer ──
            item {
                TimeRangeSlicer(
                    selectedRange = selectedRange,
                    onRangeSelected = viewModel::setTimeRange
                )
            }

            // ── Key Stats Row ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)
                ) {
                    val totalSpent = categorySpent.sumOf { it.amount }
                    InsightStat(
                        label = "BURN RATE",
                        value = "₹${(totalSpent / 30).toInt()}/d",
                        color = CategoryColors.Orange,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStat(
                        label = "TOP CAT",
                        value = categorySpent.firstOrNull()?.category?.uppercase() ?: "N/A",
                        color = CategoryColors.Purple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Cash Flow Bar Chart ──
            item {
                AnalyticsCard(title = "CASH FLOW", subtitle = "INCOME VS EXPENSE") {
                    NoirCashFlowChart(
                        income = cashFlow.income,
                        expense = cashFlow.expense,
                        modifier = Modifier.fillMaxWidth().height(140.dp).padding(vertical = Dimens.SpacingMd)
                    )
                }
            }

            // ── Daily Spend Trend ──
            item {
                AnalyticsCard(title = "SPEND TREND", subtitle = "DAILY VELOCITY") {
                    if (dailyTrend.isEmpty()) {
                        EmptyDataPlaceHolder()
                    } else {
                        NoirTrendChart(
                            data = dailyTrend,
                            modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = Dimens.SpacingMd)
                        )
                    }
                }
            }

            // ── Category Distribution ──
            item {
                AnalyticsCard(title = "DISTRIBUTION", subtitle = "BY CATEGORY") {
                    if (categorySpent.isEmpty()) {
                        EmptyDataPlaceHolder()
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingMd)) {
                            PieChart(
                                data = categorySpent,
                                modifier = Modifier.size(180.dp).align(Alignment.CenterHorizontally)
                            )
                            Spacer(Modifier.height(Dimens.SpacingLg))
                            val total = categorySpent.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
                            categorySpent.take(3).forEach { spend ->
                                val pct = (spend.amount.toFloat() / total * 100).toInt()
                                LegendItem(spend.color, spend.category.uppercase(), "₹${spend.amount.toInt()}", "$pct%")
                            }
                        }
                    }
                }
            }

            // ── Top Merchants ──
            item {
                AnalyticsCard(title = "TOP SPEND TO", subtitle = "MOST FREQUENT MERCHANTS") {
                    if (topMerchants.isEmpty()) {
                        EmptyDataPlaceHolder()
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                            topMerchants.forEach { m ->
                                MerchantItem(m.merchant.uppercase(), "₹${m.amount.toInt()}")
                            }
                        }
                    }
                }
            }

            // ── Year over Year (Legacy View) ──
            item {
                AnalyticsCard(title = "HISTORY", subtitle = "YEAR OVER YEAR TREND") {
                    NoirYoYChart(
                        data = yoyData,
                        modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = Dimens.SpacingMd)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRangeSlicer(
    selectedRange: AnalyticsTimeRange,
    onRangeSelected: (AnalyticsTimeRange) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        contentPadding = PaddingValues(bottom = Dimens.SpacingSm)
    ) {
        items(AnalyticsTimeRange.values()) { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .border(
                        width = Dimens.BorderWidthStandard,
                        color = MonoBlack,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .background(if (isSelected) MonoBlack else MonoWhite)
                    .clickable { onRangeSelected(range) }
                    .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm)
            ) {
                Text(
                    text = range.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) MonoWhite else MonoBlack
                )
            }
        }
    }
}

@Composable
private fun NoirCashFlowChart(income: Double, expense: Double, modifier: Modifier = Modifier) {
    val max = (income.coerceAtLeast(expense)).toFloat().coerceAtLeast(1f)
    val incomePercent = (income / max).toFloat()
    val expensePercent = (expense / max).toFloat()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
        // Income Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("IN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp))
            Box(Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth().height(24.dp).border(Dimens.BorderWidthThin, MonoBlack))
                Box(Modifier.fillMaxWidth(incomePercent).height(24.dp).background(CategoryColors.Green).border(Dimens.BorderWidthThin, MonoBlack))
            }
            Text("₹${income.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        // Expense Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("OUT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp))
            Box(Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth().height(24.dp).border(Dimens.BorderWidthThin, MonoBlack))
                Box(Modifier.fillMaxWidth(expensePercent).height(24.dp).background(CategoryColors.Orange).border(Dimens.BorderWidthThin, MonoBlack))
            }
            Text("₹${expense.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

@Composable
private fun NoirTrendChart(data: List<DailyTrend>, modifier: Modifier = Modifier) {
    val max = data.maxOfOrNull { it.amount }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, d ->
            androidx.compose.ui.geometry.Offset(
                x = i * stepX,
                y = h - (d.amount.toFloat() / max * h)
            )
        }

        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
        }

        drawPath(
            path = path,
            color = MonoBlack,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Square)
        )

        // Draw points as squares
        points.forEach { pt ->
            drawRect(
                color = MonoBlack,
                topLeft = androidx.compose.ui.geometry.Offset(pt.x - 3.dp.toPx(), pt.y - 3.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }
}

@Composable
private fun MerchantItem(name: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(Dimens.BorderWidthThin, MonoBlack)
            .padding(Dimens.SpacingSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun EmptyDataPlaceHolder() {
    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text("NO DATA COLLECTED", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun InsightStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    BlockCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.1f),
        borderColor = MonoBlack
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MonoBlack.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MonoBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    BlockCard(modifier = Modifier.fillMaxWidth(), hasShadow = true, backgroundColor = MonoWhite) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MonoBlack)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Dimens.SpacingMd))
            content()
        }
    }
}

@Composable
private fun PieChart(data: List<CategorySpend>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = MonoWhite
    )

    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.forEach { spend ->
            val sweepAngle = (spend.amount.toFloat() / total) * 360f
            
            drawArc(
                color = spend.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            
            drawArc(
                color = Color(0xFF0F172A),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = Stroke(width = 2.dp.toPx())
            )

            val pct = (spend.amount.toFloat() / total * 100).toInt()
            if (pct >= 8) {
                val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val labelRadius = size.minDimension / 3f
                val labelX = center.x + (labelRadius * cos(midAngle)).toFloat()
                val labelY = center.y + (labelRadius * sin(midAngle)).toFloat()

                val layoutResult = textMeasurer.measure("$pct%", labelStyle)
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        labelX - layoutResult.size.width / 2f,
                        labelY - layoutResult.size.height / 2f
                    )
                )
            }

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color(0xFF0F172A),
            radius = size.minDimension / 5f
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String, percentage: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp).border(1.dp, MonoGrayLight).padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(12.dp).background(color).border(Dimens.BorderWidthThin, MonoBlack)
        )
        Spacer(Modifier.width(Dimens.SpacingSm))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            percentage,
            style = MaterialTheme.typography.labelSmall,
            color = MonoGrayMedium,
            modifier = Modifier.padding(end = Dimens.SpacingSm)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NoirYoYChart(data: List<MonthlyComparison>, modifier: Modifier = Modifier) {
    val max = data.flatMap { listOf(it.currentYear, it.previousYear) }.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = MonoGrayMedium
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height - 30.dp.toPx()
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        fun getPoints(selector: (MonthlyComparison) -> Int): List<androidx.compose.ui.geometry.Offset> {
            return data.mapIndexed { i, d ->
                androidx.compose.ui.geometry.Offset(
                    x = i * stepX,
                    y = h - (selector(d) / max * h)
                )
            }
        }

        val currentPoints = getPoints { it.currentYear }
        val previousPoints = getPoints { it.previousYear }

        if (previousPoints.isNotEmpty()) {
            val prevPath = Path().apply {
                moveTo(previousPoints[0].x, previousPoints[0].y)
                for (i in 1 until previousPoints.size) {
                    lineTo(previousPoints[i].x, previousPoints[i].y)
                }
            }
            drawPath(
                path = prevPath,
                color = MonoGrayMedium,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Square)
            )
        }

        if (currentPoints.isNotEmpty()) {
            val currPath = Path().apply {
                moveTo(currentPoints[0].x, currentPoints[0].y)
                for (i in 1 until currentPoints.size) {
                    lineTo(currentPoints[i].x, currentPoints[i].y)
                }
            }
            drawPath(
                path = currPath,
                color = Color(0xFF3B82F6),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Square)
            )
            
            currentPoints.forEach { pt ->
                drawRect(
                    color = Color(0xFF3B82F6),
                    topLeft = androidx.compose.ui.geometry.Offset(pt.x - 5.dp.toPx(), pt.y - 5.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        data.forEachIndexed { i, d ->
            if (i % 2 == 0) {
                val x = i * stepX
                val layoutResult = textMeasurer.measure(d.monthName.uppercase(), labelStyle)
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x - layoutResult.size.width / 2f,
                        h + 10.dp.toPx()
                    )
                )
            }
        }
    }
}

