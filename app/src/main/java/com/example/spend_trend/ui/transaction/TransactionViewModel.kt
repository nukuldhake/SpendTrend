package com.example.spend_trend.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.repository.TransactionRepository

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.io.File
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.time.LocalDate

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // All transactions as StateFlow (UI can collect this)
    val allTransactions: StateFlow<List<TransactionUi>> = repository.allTransactions
        .map { entities ->
            entities.map { it.toUi() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent 10 for Dashboard
    val recentTransactions: StateFlow<List<TransactionUi>> = repository.recentTransactions
        .map { entities ->
            entities.map { it.toUi() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Add new transaction
    fun addTransaction(ui: TransactionUi) {
        viewModelScope.launch {
            val entity = ui.toEntity()
            repository.insert(entity)
        }
    }

    // Update existing
    fun updateTransaction(ui: TransactionUi) {
        viewModelScope.launch {
            val entity = ui.toEntity()
            repository.update(entity)
        }
    }

    // Delete
    fun deleteTransaction(ui: TransactionUi) {
        viewModelScope.launch {
            if (ui.id > 0) {
                repository.deleteById(ui.id)
            } else {
                // fallback for entities not yet in DB (unlikely for delete but safe)
                val entity = ui.toEntity()
                repository.delete(entity)
            }
        }
    }

    // Export to CSV
    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            repository.allTransactions.collect { transactions ->
                if (transactions.isNotEmpty()) {
                    com.example.spend_trend.ui.util.ExportUtils.exportTransactionsToCsv(context, transactions)
                }
            }
        }
    }

    // Helper converters (Entity ↔ Ui)
    private fun TransactionEntity.toUi() = TransactionUi(
        id = id,
        title = title,
        category = category,
        amount = amount,
        date = getDate()
    )

    private fun TransactionUi.toEntity() = TransactionEntity(
        id = id,
        title = title,
        category = category,
        amount = amount,
        dateMillis = date.toEpochDay() * 86400000L,
        description = title // or add description field later
    )
}