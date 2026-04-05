package com.example.spend_trend.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AnalyticsTimeRange(val label: String) {
    TODAY("TODAY"),
    WEEK("7D"),
    MONTH("1M"),
    QUARTER("3M"),
    YEAR("1Y"),
    ALL("ALL")
}

data class CategorySpend(val category: String, val amount: Double, val color: androidx.compose.ui.graphics.Color)
data class MonthlyComparison(val monthName: String, val currentYear: Int, val previousYear: Int)
data class DailyTrend(val dayLabel: String, val amount: Double)
data class CashFlow(val income: Double, val expense: Double)
data class MerchantSpend(val merchant: String, val amount: Double)

class AnalyticsViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(AnalyticsTimeRange.MONTH)
    val selectedTimeRange: StateFlow<AnalyticsTimeRange> = _selectedTimeRange.asStateFlow()

    fun setTimeRange(range: AnalyticsTimeRange) {
        _selectedTimeRange.value = range
    }

    private val filteredTransactions = combine(
        repository.allTransactions,
        _selectedTimeRange
    ) { txs, range ->
        val startMillis = getStartMillis(range)
        txs.filter { it.dateMillis >= startMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryDistribution: StateFlow<List<CategorySpend>> = filteredTransactions
        .map { txs ->
            txs.filter { it.amount < 0 }
                .groupBy { it.category }
                .map { (cat, list) -> 
                    CategorySpend(cat, list.sumOf { -it.amount.toDouble() }, getCategoryColor(cat))
                }
                .sortedByDescending { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashFlow: StateFlow<CashFlow> = filteredTransactions
        .map { txs ->
            val income = txs.filter { it.amount > 0 }.sumOf { it.amount.toDouble() }
            val expense = txs.filter { it.amount < 0 }.sumOf { -it.amount.toDouble() }
            CashFlow(income, expense)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashFlow(0.0, 0.0))

    val dailyTrend: StateFlow<List<DailyTrend>> = filteredTransactions
        .map { txs ->
            val formatter = DateTimeFormatter.ofPattern("dd MMM")
            txs.filter { it.amount < 0 }
                .groupBy { 
                    Instant.ofEpochMilli(it.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                .map { (date, list) ->
                    date to list.sumOf { -it.amount.toDouble() }
                }
                .sortedBy { it.first } // Sort by LocalDate
                .map { (date, total) ->
                    DailyTrend(date.format(formatter), total)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topMerchants: StateFlow<List<MerchantSpend>> = filteredTransactions
        .map { txs ->
            txs.filter { it.amount < 0 }
                .groupBy { it.title }
                .map { (merchant, list) ->
                    MerchantSpend(merchant, list.sumOf { -it.amount.toDouble() })
                }
                .sortedByDescending { it.amount }
                .take(5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yoyComparison: StateFlow<List<MonthlyComparison>> = repository.allTransactions
        .map { txs ->
            val currentYear = LocalDate.now().year
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            
            months.mapIndexed { index, name ->
                val month = index + 1
                val currentYearAmt = txs.filter { 
                    val date = Instant.ofEpochMilli(it.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    date.year == currentYear && date.monthValue == month && it.amount < 0
                }.sumOf { -it.amount }
                
                val previousYearAmt = txs.filter { 
                    val date = Instant.ofEpochMilli(it.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    date.year == currentYear - 1 && date.monthValue == month && it.amount < 0
                }.sumOf { -it.amount }
                
                MonthlyComparison(name, currentYearAmt, previousYearAmt)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getStartMillis(range: AnalyticsTimeRange): Long {
        val now = LocalDate.now()
        val startDay = when (range) {
            AnalyticsTimeRange.TODAY -> now
            AnalyticsTimeRange.WEEK -> now.minusDays(7)
            AnalyticsTimeRange.MONTH -> now.minusMonths(1)
            AnalyticsTimeRange.QUARTER -> now.minusMonths(3)
            AnalyticsTimeRange.YEAR -> now.minusYears(1)
            AnalyticsTimeRange.ALL -> LocalDate.of(2000, 1, 1)
        }
        return startDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
        return when (category.lowercase()) {
            "food" -> androidx.compose.ui.graphics.Color(0xFFFB7185)
            "transport" -> androidx.compose.ui.graphics.Color(0xFF38BDF8)
            "shopping" -> androidx.compose.ui.graphics.Color(0xFFFBBF24)
            "entertainment" -> androidx.compose.ui.graphics.Color(0xFF818CF8)
            "bills" -> androidx.compose.ui.graphics.Color(0xFF34D399)
            "health" -> androidx.compose.ui.graphics.Color(0xFFF472B6)
            else -> androidx.compose.ui.graphics.Color(0xFF94A3B8)
        }
    }
}
