package com.example.spend_trend.ui.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.spend_trend.ui.components.BlockCard
import com.example.spend_trend.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen() {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = Dimens.BottomNavClearance)
    ) {
        // Category Distribution
        item {
            AnalyticsCard(title = "DISTRIBUTION", subtitle = "CURRENT MONTH") {
                if (categorySpent.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("NO DATA AVAILABLE", style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingMd)) {
                        PieChart(
                            data = categorySpent,
                            modifier = Modifier.size(180.dp).align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(Dimens.SpacingLg))
                        val total = categorySpent.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
                        categorySpent.take(6).forEach { spend ->
                            val pct = (spend.amount.toFloat() / total * 100).toInt()
                            LegendItem(spend.color, spend.category.uppercase(), "₹${spend.amount.toInt()}", "$pct%")
                        }
                    }
                }
            }
        }

        // Year over Year Comparison
        item {
            AnalyticsCard(title = "YEARLY COMPARISON", subtitle = "BLOCK TREND ANALYSIS") {
                NoirYoYChart(
                    data = yoyData,
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = Dimens.SpacingMd)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    BlockCard(modifier = Modifier.fillMaxWidth(), hasShadow = true) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MonoGrayMedium)
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
            
            // Segment
            drawArc(
                color = spend.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            
            // Sharp Border between segments
            drawArc(
                color = Color(0xFF0F172A),  // Slate-900 borders
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

        // Center hole (Slate-900)
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

        // Draw Previous Year (Dashed / Muted Noir)
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

        // Draw Current Year (Accent Blue)
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
            
            // Sharp Data Points (Blue accent)
            currentPoints.forEach { pt ->
                drawRect(
                    color = Color(0xFF3B82F6),
                    topLeft = androidx.compose.ui.geometry.Offset(pt.x - 5.dp.toPx(), pt.y - 5.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }

        // X-Axis labels
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

