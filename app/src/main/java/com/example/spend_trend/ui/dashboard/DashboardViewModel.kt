package com.example.spend_trend.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.transaction.TransactionUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneOffset

class DashboardViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val recentTransactions: StateFlow<List<DashboardTx>> = repository.recentTransactions
        .map { entities ->
            entities.map { entity ->
                DashboardTx(
                    title = entity.title,
                    category = entity.category,
                    amount = entity.amount
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val todayTransactions: StateFlow<Int> = repository.allTransactions
        .map { entities ->
            val todayStart = LocalDate.now().atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
            val todayEnd = LocalDate.now().atTime(23, 59, 59)
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()

            entities
                .filter { it.dateMillis in todayStart..todayEnd }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Monthly trend (last 6 months total spend, oldest → newest)
    val monthlyTrend: StateFlow<List<Int>> = repository.allTransactions
        .map { entities ->
            val now = LocalDate.now()
            (0..5).map { monthsBack ->
                val month = now.minusMonths(monthsBack.toLong())
                val start = month.withDayOfMonth(1).atStartOfDay()
                    .toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli()
                val end = month.withDayOfMonth(month.lengthOfMonth()).atTime(23, 59, 59)
                    .toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli()

                entities
                    .filter { it.dateMillis in start..end }
                    .sumOf { it.amount }
            }.reversed() // make oldest first for chart left-to-right
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val currentMonthSummary: StateFlow<MonthSummary> = repository.allTransactions
        .map { entities ->
            val now = LocalDate.now()
            val monthStart = now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val monthEnd = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

            val monthEntities = entities.filter { it.dateMillis in monthStart..monthEnd }

            val income = monthEntities.filter { it.amount > 0 }.sumOf { it.amount }
            val expense = monthEntities.filter { it.amount < 0 }.sumOf { -it.amount }
            val net = income - expense

            MonthSummary(income, expense, net)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = MonthSummary(0, 0, 0)
        )
}

data class MonthSummary(val income: Int, val expense: Int, val net: Int)

// Extension function – add this at the bottom
fun TransactionEntity.toUi(): TransactionUi {
    return TransactionUi(
        title = title,
        category = category,
        amount = amount,
        date = LocalDate.ofEpochDay(dateMillis / 86400000L)
    )
}