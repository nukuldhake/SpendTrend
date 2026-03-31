package com.example.spend_trend.data.repository

import com.example.spend_trend.data.TransactionDao
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth
    private fun getUid() = auth.currentUserOrNull()?.id

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAll()

    val recentTransactions: Flow<List<TransactionEntity>> = dao.getRecent()

    suspend fun insert(entity: TransactionEntity) {
        dao.insert(entity)
        getUid()?.let { uid ->
            try {
                postgrest["transactions"].insert(entity)
            } catch (e: Exception) {
                // Log or handle error if needed
            }
        }
    }

    suspend fun update(entity: TransactionEntity) {
        dao.update(entity)
        getUid()?.let { uid ->
            try {
                postgrest["transactions"].update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    suspend fun delete(entity: TransactionEntity) {
        dao.delete(entity)
        getUid()?.let { uid ->
            try {
                postgrest["transactions"].delete {
                    filter {
                        eq("id", entity.id)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
        getUid()?.let { uid ->
            try {
                postgrest["transactions"].delete {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
            }
        }
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
