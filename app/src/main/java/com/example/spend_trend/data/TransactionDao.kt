package com.example.spend_trend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC LIMIT 10")
    fun getRecent(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM transactions WHERE referenceNo = :refNo LIMIT 1")
    suspend fun getByReferenceNo(refNo: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        WHERE amount = :amount 
          AND dateMillis >= :startMillis 
          AND dateMillis <= :endMillis 
        LIMIT 1
    """)
    suspend fun findSimilar(amount: Int, startMillis: Long, endMillis: Long): TransactionEntity?

    // Optional: monthly totals example (for Dashboard later)
    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis
    """)
    suspend fun getNetForPeriod(startMillis: Long, endMillis: Long): Int?

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE amount > 0 AND dateMillis >= :startMillis AND dateMillis <= :endMillis
    """)
    suspend fun getIncomeForPeriod(startMillis: Long, endMillis: Long): Int?

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE amount < 0 AND dateMillis >= :startMillis AND dateMillis <= :endMillis
    """)
    suspend fun getExpenseForPeriod(startMillis: Long, endMillis: Long): Int?

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE category = :category AND amount < 0 AND dateMillis >= :startMillis AND dateMillis <= :endMillis
    """)
    suspend fun getCategoryExpenseForPeriod(category: String, startMillis: Long, endMillis: Long): Int?
}