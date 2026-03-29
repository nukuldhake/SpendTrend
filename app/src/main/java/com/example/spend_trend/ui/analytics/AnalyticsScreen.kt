package com.example.spend_trend.ui.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.components.GlassCard
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
            .padding(horizontal = Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        contentPadding = PaddingValues(top = Dimens.SpacingLg, bottom = Dimens.BottomNavClearance)
    ) {
        item {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Insights into your financial habits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Category Distribution (Pie Chart)
        item {
            AnalyticsCard(title = "Category Distribution", subtitle = "Current Month") {
                if (categorySpent.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No spending data yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PieChart(
                            data = categorySpent,
                            modifier = Modifier.size(160.dp).weight(1f)
                        )
                        Spacer(Modifier.width(Dimens.SpacingLg))
                        Column(modifier = Modifier.weight(1f)) {
                            categorySpent.take(5).forEach { spend ->
                                LegendItem(spend.color, spend.category, "₹${spend.amount.toInt()}")
                            }
                        }
                    }
                }
            }
        }

        // Year over Year Comparison
        item {
            AnalyticsCard(title = "Year over Year", subtitle = "${java.time.LocalDate.now().year} vs ${java.time.LocalDate.now().year - 1}") {
                YoYChart(
                    data = yoyData,
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = Dimens.SpacingMd)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.SpacingMd))
            content()
        }
    }
}

@Composable
private fun PieChart(data: List<CategorySpend>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
    
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
            startAngle += sweepAngle
        }
        
        // Inner hole for donut effect (optional but looks modern)
        drawCircle(
            color = Color(0xFF1E293B), // Match background color or card color
            radius = size.minDimension / 4f
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label, 
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun YoYChart(data: List<MonthlyComparison>, modifier: Modifier = Modifier) {
    val max = data.flatMap { listOf(it.currentYear, it.previousYear) }.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        fun getPoints(selector: (MonthlyComparison) -> Int): List<Pair<Float, Float>> {
            return data.mapIndexed { i, d ->
                val x = i * stepX
                val y = h - (selector(d) / max * h)
                x to y
            }
        }

        val currentPoints = getPoints { it.currentYear }
        val previousPoints = getPoints { it.previousYear }

        fun drawPathEffect(points: List<Pair<Float, Float>>, color: Color) {
            if (points.isEmpty()) return
            val path = Path().apply {
                moveTo(points[0].first, points[0].second)
                for (i in 1 until points.size) {
                    val prev = points[i-1]
                    val curr = points[i]
                    val cpx = (prev.first + curr.first) / 2f
                    cubicTo(cpx, prev.second, cpx, curr.second, curr.first, curr.second)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw Previous Year (Dashed/Lighter)
        drawPathEffect(previousPoints, Color.White.copy(alpha = 0.2f))
        
        // Draw Current Year (Solid/Primary)
        drawPathEffect(currentPoints, Primary)
        
        // X-Axis labels (every 3rd month for clarity)
        data.forEachIndexed { i, d ->
            if (i % 2 == 0) {
                val x = i * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    d.monthName,
                    x,
                    h + 30f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
