package com.example.spend_trend.data.repository

import com.example.spend_trend.data.TransactionDao
import com.example.spend_trend.data.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAll()

    val recentTransactions: Flow<List<TransactionEntity>> = dao.getRecent()

    suspend fun insert(entity: TransactionEntity) {
        dao.insert(entity)
    }

    suspend fun update(entity: TransactionEntity) {
        dao.update(entity)
    }

    suspend fun delete(entity: TransactionEntity) {
        dao.delete(entity)
    }

    // Optional helpers for Dashboard
    suspend fun getNetForPeriod(startMillis: Long, endMillis: Long): Int? {
        return dao.getNetForPeriod(startMillis, endMillis)
    }

    suspend fun getIncomeForPeriod(startMillis: Long, endMillis: Long): Int? {
        return dao.getIncomeForPeriod(startMillis, endMillis)
    }

    suspend fun getExpenseForPeriod(startMillis: Long, endMillis: Long): Int? {
        return dao.getExpenseForPeriod(startMillis, endMillis)
    }
}