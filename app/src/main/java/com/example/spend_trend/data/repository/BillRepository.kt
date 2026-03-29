package com.example.spend_trend.data.repository

import com.example.spend_trend.data.BillDao
import com.example.spend_trend.data.BillEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val pendingBills: Flow<List<BillEntity>> = billDao.getPendingBills()

    suspend fun insertBill(bill: BillEntity) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun markAsPaid(billId: Int) {
        billDao.markAsPaid(billId)
    }
}
