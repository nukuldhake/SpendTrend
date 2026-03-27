package com.example.spend_trend.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.ui.FakeData
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
            val entity = ui.toEntity()
            repository.delete(entity)
        }
    }

    // Export to CSV
    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            val transactions = repository.allTransactions.map { it.reversed() }.stateIn(viewModelScope).value
            if (transactions.isEmpty()) return@launch

            val header = "Date,Title,Category,Amount\n"
            val rows = transactions.joinToString("\n") { tx ->
                val date = LocalDate.ofEpochDay(tx.dateMillis / 86400000L).format(DateTimeFormatter.ISO_DATE)
                "\"$date\",\"${tx.title}\",\"${tx.category}\",${tx.amount}"
            }
            val csvData = header + rows

            try {
                val file = File(context.cacheDir, "spendtrend_export.csv")
                file.writeText(csvData)
                
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "SpendTrend Export")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export CSV"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper converters (Entity ↔ Ui)
    private fun TransactionEntity.toUi() = TransactionUi(
        title = title,
        category = category,
        amount = amount,
        date = getDate()
    )

    private fun TransactionUi.toEntity() = TransactionEntity(
        title = title,
        category = category,
        amount = amount,
        dateMillis = date.toEpochDay() * 86400000L,
        description = title // or add description field later
    )
}