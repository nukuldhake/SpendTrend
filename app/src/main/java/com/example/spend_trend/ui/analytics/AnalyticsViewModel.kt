package com.example.spend_trend.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CategorySpend(val category: String, val amount: Double, val color: androidx.compose.ui.graphics.Color)
data class MonthlyComparison(val monthName: String, val currentYear: Int, val previousYear: Int)

class AnalyticsViewModel(private val repository: TransactionRepository) : ViewModel() {

    val categoryDistribution: StateFlow<List<CategorySpend>> = repository.allTransactions
        .map { txs ->
            val currentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            txs.filter { it.dateMillis >= currentMonth && it.amount < 0 }
                .groupBy { it.category }
                .map { (cat, list) -> 
                    CategorySpend(cat, list.sumOf { -it.amount.toDouble() }, getCategoryColor(cat))
                }
                .sortedByDescending { it.amount }
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
