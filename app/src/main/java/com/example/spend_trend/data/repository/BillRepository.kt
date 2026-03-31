package com.example.spend_trend.data.repository

import com.example.spend_trend.data.BillDao
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth
    private fun getUid() = auth.currentUserOrNull()?.id

    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val pendingBills: Flow<List<BillEntity>> = billDao.getPendingBills()

    suspend fun insertBill(bill: BillEntity) {
        billDao.insertBill(bill)
        getUid()?.let { uid ->
            try {
                postgrest["bills"].insert(bill)
            } catch (e: Exception) {}
        }
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
        getUid()?.let { uid ->
            try {
                postgrest["bills"].update(bill) {
                    filter {
                        eq("id", bill.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
        getUid()?.let { uid ->
            try {
                postgrest["bills"].delete {
                    filter {
                        eq("id", bill.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun markAsPaid(billId: Int) {
        billDao.markAsPaid(billId)
        getUid()?.let { uid ->
            try {
                postgrest["bills"].update({
                    set("is_paid", true)
                }) {
                    filter {
                        eq("id", billId)
                    }
                }
            } catch (e: Exception) {}
        }
    }
}
