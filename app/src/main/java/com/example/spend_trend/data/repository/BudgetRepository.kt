package com.example.spend_trend.data.repository

import com.example.spend_trend.data.BudgetDao
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth
    private fun getUid() = auth.currentUserOrNull()?.id

    fun getAllActive(): Flow<List<BudgetEntity>> = dao.getAllActive()

    suspend fun insert(budget: BudgetEntity) {
        dao.insert(budget)
        getUid()?.let { uid ->
            try {
                postgrest["budgets"].insert(budget)
            } catch (e: Exception) {}
        }
    }

    suspend fun update(budget: BudgetEntity) {
        dao.update(budget)
        getUid()?.let { uid ->
            try {
                postgrest["budgets"].update(budget) {
                    filter {
                        eq("id", budget.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun delete(budget: BudgetEntity) {
        dao.delete(budget)
        getUid()?.let { uid ->
            try {
                postgrest["budgets"].delete {
                    filter {
                        eq("id", budget.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }
}
