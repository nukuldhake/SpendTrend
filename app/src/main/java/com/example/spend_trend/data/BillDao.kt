package com.example.spend_trend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDateMillis ASC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueDateMillis ASC")
    fun getPendingBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueDateMillis ASC")
    suspend fun getPendingBillsNow(): List<BillEntity>

    @Query("SELECT * FROM bills WHERE referenceNo = :refNo LIMIT 1")
    suspend fun getByReferenceNo(refNo: String): BillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("UPDATE bills SET isPaid = 1 WHERE id = :billId")
    suspend fun markAsPaid(billId: Int)
}
