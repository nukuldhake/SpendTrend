package com.example.spend_trend.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.repository.BillRepository
import com.example.spend_trend.data.repository.TransactionRepository
import com.example.spend_trend.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillViewModel(
    private val billRepository: BillRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val allBills: StateFlow<List<BillEntity>> = billRepository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBills: StateFlow<List<BillEntity>> = billRepository.pendingBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAsPaid(bill: BillEntity) {
        viewModelScope.launch {
            // 1. Mark bill as paid in database
            billRepository.markAsPaid(bill.id)
            
            // 2. Automatically log a transaction for this bill
            val transaction = TransactionEntity(
                title = bill.title,
                category = bill.category,
                amount = -bill.amount, // Bills are expenses
                dateMillis = System.currentTimeMillis(),
                description = "Automatic payment for ${bill.title}",
                referenceNo = bill.referenceNo
            )
            transactionRepository.insert(transaction)
        }
    }

    fun addBill(title: String, amount: Double, category: String, dueDateMillis: Long) {
        viewModelScope.launch {
            val bill = BillEntity(
                title = title,
                amount = amount.toInt(),
                category = category,
                dueDateMillis = dueDateMillis,
                isPaid = false
            )
            billRepository.insertBill(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }
}

class BillViewModelFactory(
    private val billRepo: BillRepository,
    private val txRepo: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillViewModel(billRepo, txRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
