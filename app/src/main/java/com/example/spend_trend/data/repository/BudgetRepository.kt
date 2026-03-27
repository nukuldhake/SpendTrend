package com.example.spend_trend.data.repository

import com.example.spend_trend.data.BudgetDao
import com.example.spend_trend.data.BudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {

    fun getAllActive(): Flow<List<BudgetEntity>> = dao.getAllActive()

    suspend fun insert(budget: BudgetEntity) = dao.insert(budget)

    suspend fun update(budget: BudgetEntity) = dao.update(budget)

    suspend fun delete(budget: BudgetEntity) = dao.delete(budget)
}
