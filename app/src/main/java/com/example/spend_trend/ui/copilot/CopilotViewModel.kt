package com.example.spend_trend.ui.copilot

import com.example.spend_trend.BuildConfig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.BudgetRepository
import com.example.spend_trend.data.repository.GoalRepository
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
    private val budgetRepository: BudgetRepository,
    private val billRepository: BillRepository,
    private val goalRepository: GoalRepository,
    private val groqService: CopilotGroqService
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
            
            try {
                // Get fresh data
                val allTxs = txRepository.allTransactions.first()
                val allBudgets = budgetRepository.getAllActive().first()
                val allBills = billRepository.allBills.first()
                val allGoals = goalRepository.allGoals.first()
                
                // Get response from Groq
                val response = groqService.getCopilotResponse(input, allTxs, allBudgets, allBills, allGoals)
                
                messages.add(ChatMessage(response, false, nowTime()))
            } catch (e: Exception) {
                messages.add(ChatMessage("Sorry, I encountered an error: ${e.message}", false, nowTime()))
            } finally {
                isTyping = false
            }
        }
    }

    private fun nowTime(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}

class CopilotViewModelFactory(
    private val txRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val billRepository: BillRepository,
    private val goalRepository: GoalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CopilotViewModel::class.java)) {
            // Using the Groq API key from BuildConfig
            val groqService = CopilotGroqService(BuildConfig.GROQ_API_KEY)
            @Suppress("UNCHECKED_CAST")
            return CopilotViewModel(txRepository, budgetRepository, billRepository, goalRepository, groqService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
