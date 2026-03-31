package com.example.spend_trend.data.repository

import com.example.spend_trend.data.GoalDao
import com.example.spend_trend.data.GoalEntity
import com.example.spend_trend.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth
    private fun getUid() = auth.currentUserOrNull()?.id

    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllActive()

    fun getGoalById(id: Int): Flow<GoalEntity?> = goalDao.getById(id)

    suspend fun insertGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
        getUid()?.let { uid ->
            try {
                postgrest["goals"].insert(goal)
            } catch (e: Exception) {}
        }
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
        getUid()?.let { uid ->
            try {
                postgrest["goals"].update(goal) {
                    filter {
                        eq("id", goal.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
        getUid()?.let { uid ->
            try {
                postgrest["goals"].delete {
                    filter {
                        eq("id", goal.id)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun getTotalTarget(): Double = goalDao.getTotalTargetAmount() ?: 0.0
}
