package com.example.spend_trend.data.repository

import com.example.spend_trend.data.*
import com.example.spend_trend.data.network.SupabaseClient
import com.example.spend_trend.data.sms.SmsSyncManager
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(private val context: Context, private val db: AppDatabase) {
    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    suspend fun syncAllFromCloud() = withContext(Dispatchers.IO) {
        if (auth.currentUserOrNull() == null) return@withContext

        try {
            // Clear local database for fresh sync
            db.clearAllTables()

            // 1. Sync Transactions
            val txs = postgrest["transactions"].select().decodeList<TransactionEntity>()
            txs.forEach { db.transactionDao().insert(it) }

            // 2. Sync Budgets
            val budgets = postgrest["budgets"].select().decodeList<BudgetEntity>()
            budgets.forEach { db.budgetDao().insert(it) }

            // 3. Sync Bills
            val bills = postgrest["bills"].select().decodeList<BillEntity>()
            bills.forEach { db.billDao().insertBill(it) }

            // 4. Sync Goals
            val goals = postgrest["goals"].select().decodeList<GoalEntity>()
            goals.forEach { db.goalDao().insertGoal(it) }
            
            // 5. Trigger local SMS sync to catch any new/missing local data
            try {
                SmsSyncManager(context).syncLast30Days()
            } catch (smsE: Exception) {
                smsE.printStackTrace()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Log sync failure
        }
    }
}
