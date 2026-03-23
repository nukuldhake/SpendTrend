package com.example.spend_trend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY category ASC")
    fun getAllActive(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear AND isActive = 1")
    fun getForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    // Optional: total spent for a category in a month (we'll use transactions for real spent later)
    @Query("SELECT SUM(currentSpent) FROM budgets WHERE monthYear = :monthYear")
    suspend fun getTotalSpentForMonth(monthYear: String): Float?
}