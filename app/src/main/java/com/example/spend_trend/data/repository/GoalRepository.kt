package com.example.spend_trend.data.repository

import com.example.spend_trend.data.GoalDao
import com.example.spend_trend.data.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {

    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllActive()

    fun getGoalById(id: Int): Flow<GoalEntity?> = goalDao.getById(id)

    suspend fun insertGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    suspend fun getTotalTarget(): Double = goalDao.getTotalTargetAmount() ?: 0.0
}
