package com.example.spend_trend.ui.copilot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class CopilotViewModel(
    private val txRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val messages = mutableStateListOf(
        ChatMessage("Hi! I'm your SpendTrend Copilot. Ask me about your spending, budgets, or income!", false, nowTime())
    )

    var isTyping by mutableStateOf(false)
        private set

    fun sendMessage(text: String) {
        messages.add(ChatMessage(text, true, nowTime()))
        generateResponse(text)
    }

    private fun generateResponse(input: String) {
        viewModelScope.launch {
            isTyping = true
            delay(1500) // Simulating thought

            val query = input.lowercase()
            val now = LocalDate.now()
            val monthStart = now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            
            val allTxs = txRepository.allTransactions.first()
            val monthTxs = allTxs.filter { it.dateMillis >= monthStart }
            
            val response = when {
                query.contains("spent") || query.contains("spending") -> {
                    val totalSpent = monthTxs.filter { it.amount < 0 }.sumOf { (-it.amount).toLong() }
                    "You've spent ₹$totalSpent so far this month. Would you like to see a category breakdown?"
                }
                query.contains("income") || query.contains("earned") -> {
                    val totalIncome = monthTxs.filter { it.amount > 0 }.sumOf { it.amount.toLong() }
                    "Your total income this month is ₹$totalIncome."
                }
                query.contains("budget") -> {
                    val budgets = budgetRepository.getAllActive().first()
                    val totalBudget = budgets.sumOf { it.monthlyLimit.toLong() }
                    if (totalBudget > 0) {
                        val spent = monthTxs.filter { it.amount < 0 }.sumOf { (-it.amount).toLong() }
                        val perc = (spent.toFloat() / totalBudget * 100).toInt()
                        "You've used $perc% of your total monthly budget (₹$spent of ₹$totalBudget)."
                    } else {
                        "You haven't set any budgets yet. Go to the Budgets tab to start!"
                    }
                }
                query.contains("food") || query.contains("dining") -> {
                    val foodSpent = monthTxs.filter { it.category.equals("Food", true) || it.category.equals("Dining", true) }
                        .sumOf { (-it.amount).toLong() }
                    "You've spent ₹$foodSpent on Food/Dining this month."
                }
                else -> {
                    "I'm still learning, but I can help with questions about your spending, budgets, or specific categories like Food or Rent!"
                }
            }

            messages.add(ChatMessage(response, false, nowTime()))
            isTyping = false
        }
    }

    private fun nowTime(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}

class CopilotViewModelFactory(
    private val txRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CopilotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CopilotViewModel(txRepository, budgetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
