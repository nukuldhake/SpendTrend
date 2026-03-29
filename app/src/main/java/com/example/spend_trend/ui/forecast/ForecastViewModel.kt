package com.example.spend_trend.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.BuildConfig
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.model.ForecastInsight
import com.example.spend_trend.data.model.InsightType
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.copilot.CopilotGroqService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

class ForecastViewModel(
    private val repository: TransactionRepository,
    private val groqService: CopilotGroqService
) : ViewModel() {

    // Helper for grouping transactions by month epoch
    private fun getMonthKey(millis: Long): Long {
        val date = LocalDate.ofEpochDay(millis / 86400000L).withDayOfMonth(1)
        return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    val forecastData: StateFlow<List<Float>> = repository.allTransactions
        .map { entities ->
            calculateForecasting(entities)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = List(6) { 0f }
        )

    val insights: StateFlow<List<ForecastInsight>> = repository.allTransactions
        .map { entities ->
            val projections = calculateForecasting(entities)
            val history = calculateHistory(entities)
            
            // Try to get AI insights from Groq
            val aiInsights = groqService.getForecastInsights(history, projections)
            if (aiInsights.isNotEmpty() && aiInsights.none { it.title.contains("Unavailable") }) {
                aiInsights
            } else {
                generateInsights(entities)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun calculateHistory(entities: List<TransactionEntity>): List<Float> {
        val now = LocalDate.now()
        val expenseEntities = entities.filter { it.amount < 0 }
        val monthlySpend = expenseEntities.groupBy { getMonthKey(it.dateMillis) }
            .mapValues { it.value.sumOf { tx -> -tx.amount.toDouble() }.toFloat() }
        
        return (0..5).map { i -> 5 - i }.map { monthsBack ->
            val key = now.minusMonths(monthsBack.toLong()).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            monthlySpend[key] ?: 0f
        }
    }

    /**
     * ENHANCED FORECASTING: Linear Regression with Seasonal Weighting
     * We calculate the base trend line and then apply a multiplier based on 
     * typical monthly spending patterns if historical data allows.
     */
    private fun calculateForecasting(entities: List<TransactionEntity>): List<Float> {
        val now = LocalDate.now()
        val expenseEntities = entities.filter { it.amount < 0 }
        
        // 1. Group past 6 months spending
        val monthlySpend = expenseEntities.groupBy { getMonthKey(it.dateMillis) }
            .mapValues { it.value.sumOf { tx -> -tx.amount.toDouble() }.toFloat() }
            .toSortedMap()

        val historyIndices = (0..5).map { i -> 5 - i }
        val historyY = historyIndices.map { monthsBack ->
            val key = now.minusMonths(monthsBack.toLong()).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            monthlySpend[key] ?: 0f
        }

        // 2. Perform Linear Regression
        val validPoints = historyY.withIndex().filter { it.value > 0 }
        if (validPoints.isEmpty()) return List(6) { 0f }

        val n = validPoints.size.toDouble()
        val sumX = validPoints.sumOf { it.index.toDouble() }
        val sumY = validPoints.sumOf { it.value.toDouble() }
        val sumXY = validPoints.sumOf { it.index.toDouble() * it.value.toDouble() }
        val sumX2 = validPoints.sumOf { it.index.toDouble() * it.index.toDouble() }

        val denominator = (n * sumX2 - sumX * sumX)
        val m = if (denominator != 0.0) (n * sumXY - sumX * sumY) / denominator else 0.0
        val c = (sumY - m * sumX) / n

        // 3. Project next 6 months with Seasonal Multipliers (Simplified)
        // In a real app, this would use multi-year history. 
        // Here we simulate it with common month-end/holiday logic.
        val projections = (6..11).map { x ->
            val monthIdx = now.plusMonths((x - 5).toLong()).monthValue
            val seasonalWeight = when (monthIdx) {
                12 -> 1.25f // Holiday spike
                1 -> 0.85f  // Post-holiday dip
                3, 4 -> 1.10f // Fiscal year end/Start
                else -> 1.0f
            }
            
            val y = (m * x + c).toFloat().coerceAtLeast(0f)
            (y * seasonalWeight).coerceAtLeast(0f)
        }
        
        return projections
    }

    private fun generateInsights(entities: List<TransactionEntity>): List<ForecastInsight> {
        val now = LocalDate.now()
        val expenseEntities = entities.filter { it.amount < 0 }
        if (expenseEntities.isEmpty()) return listOf(ForecastInsight("Ready for Data", "Log your first transactions to unlock AI-powered insights.", InsightType.NEUTRAL))

        val currentMonthStart = now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val currentSpent = expenseEntities.filter { it.dateMillis >= currentMonthStart }.sumOf { -it.amount.toDouble() }.toFloat()
        
        val lastMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val lastMonthEnd = now.withDayOfMonth(1).minusDays(1).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val lastSpent = expenseEntities.filter { it.dateMillis in lastMonthStart..lastMonthEnd }.sumOf { -it.amount.toDouble() }.toFloat()

        val insights = mutableListOf<ForecastInsight>()

        // Enhanced comparison with descriptive titles
        if (lastSpent > 0) {
            val diff = ((currentSpent - lastSpent) / lastSpent * 100).roundToInt()
            when {
                diff > 15 -> insights.add(ForecastInsight("Spending Upward Trend", "You are currently pacing $diff% higher than last month. Consider reviewing large purchases.", InsightType.WARNING))
                diff < -15 -> insights.add(ForecastInsight("Excellent Budget Control", "Spending is ${-diff}% lower than last month. You're on track to save more!", InsightType.POSITIVE))
                else -> insights.add(ForecastInsight("Stable Spending", "Your monthly pace is consistent with last month.", InsightType.NEUTRAL))
            }
        }

        // Category Concentration
        val topCategory = expenseEntities.filter { it.dateMillis >= currentMonthStart }.groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> -tx.amount.toDouble() } }
            .maxByOrNull { it.value }

        if (topCategory != null && currentSpent > 0) {
            val perc = (topCategory.value / currentSpent * 100).roundToInt()
            if (perc > 45) {
                insights.add(ForecastInsight("High ${topCategory.key} Focus", "${topCategory.key} takes up $perc% of your total budget this month.", InsightType.NEGATIVE))
            }
        }

        if (insights.isEmpty()) {
            insights.add(ForecastInsight("Financial Health: Good", "Your spending patterns are stable and within healthy limits.", InsightType.POSITIVE))
        }

        return insights
    }
}

class ForecastViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            val groqService = CopilotGroqService(BuildConfig.GROQ_API_KEY)
            @Suppress("UNCHECKED_CAST")
            return ForecastViewModel(repository, groqService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
