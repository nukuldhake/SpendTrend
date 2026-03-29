package com.example.spend_trend.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneOffset

class DashboardViewModel(
    private val txRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val billRepository: BillRepository
) : ViewModel() {

    val pendingBills: StateFlow<List<BillEntity>> = billRepository.pendingBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<DashboardTx>> = txRepository.recentTransactions
        .map { entities ->
            entities.map { entity ->
                DashboardTx(
                    id = entity.id,
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

    val todayTransactions: StateFlow<Int> = txRepository.allTransactions
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
    val monthlyTrend: StateFlow<List<Int>> = txRepository.allTransactions
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

    val currentMonthSummary: StateFlow<MonthSummary> = txRepository.allTransactions
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

    // Combined Budget usage percentage
    val budgetProgress: StateFlow<Float> = combine(txRepository.allTransactions, budgetRepository.getAllActive()) { txs, budgets ->
        val now = LocalDate.now()
        val monthStart = now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val monthEnd = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        val totalBudget = budgets.sumOf { it.monthlyLimit.toDouble() }.toFloat()
        val totalSpent = txs.filter { it.dateMillis in monthStart..monthEnd && it.amount < 0 }
            .sumOf { -it.amount.toDouble() }.toFloat()

        if (totalBudget > 0) (totalSpent / totalBudget).coerceIn(0f, 1f) else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
}

data class DashboardTx(val id: Int, val title: String, val category: String, val amount: Int)
data class MonthSummary(val income: Int, val expense: Int, val net: Int)

class DashboardViewModelFactory(
    private val txRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val billRepository: BillRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(txRepository, budgetRepository, billRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}