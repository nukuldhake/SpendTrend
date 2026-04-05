package com.example.spend_trend.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val allBudgets: StateFlow<List<BudgetEntity>> = combine(
        budgetRepository.getAllActive(),
        transactionRepository.allTransactions
    ) { budgets, transactions ->
        budgets.map { budget ->
            val spent = transactions.filter { tx ->
                tx.category.equals(budget.category, ignoreCase = true) &&
                getYearMonth(tx.dateMillis) == budget.monthYear &&
                tx.amount < 0
            }.sumOf { -it.amount.toDouble() }.toFloat()
            budget.copy(currentSpent = spent)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedBudgetId = kotlinx.coroutines.flow.MutableStateFlow<Int?>(null)
    val selectedBudget: StateFlow<BudgetEntity?> = combine(allBudgets, _selectedBudgetId) { budgets, id ->
        budgets.find { it.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private fun getYearMonth(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun addBudget(category: String, limit: Float) {
        viewModelScope.launch {
            budgetRepository.insert(
                BudgetEntity(
                    category = category,
                    monthlyLimit = limit,
                    currentSpent = 0f,
                    monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    isActive = true
                )
            )
        }
    }

    fun updateBudgetLimit(budget: BudgetEntity, newLimit: Float) {
        viewModelScope.launch {
            budgetRepository.update(budget.copy(monthlyLimit = newLimit))
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
        }
    }

    fun selectBudget(budget: BudgetEntity) {
        _selectedBudgetId.value = budget.id
    }

    fun loadBudgetById(budgetId: Int) {
        _selectedBudgetId.value = budgetId
    }
}

class BudgetViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(budgetRepository, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
