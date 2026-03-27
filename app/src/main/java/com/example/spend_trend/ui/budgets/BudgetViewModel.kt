package com.example.spend_trend.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    val allBudgets: Flow<List<BudgetEntity>> = repository.getAllActive()

    private val _selectedBudget = MutableStateFlow<BudgetEntity?>(null)
    val selectedBudget: StateFlow<BudgetEntity?> = _selectedBudget

    fun addBudget(category: String, limit: Float) {
        viewModelScope.launch {
            repository.insert(
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
            repository.update(budget.copy(monthlyLimit = newLimit))
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.delete(budget)
        }
    }

    fun selectBudget(budget: BudgetEntity) {
        _selectedBudget.value = budget
    }

    fun loadBudgetById(budgetId: Int) {
        viewModelScope.launch {
            val budgets = allBudgets.firstOrNull() ?: emptyList()
            _selectedBudget.value = budgets.find { it.id == budgetId }
        }
    }
}

class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
