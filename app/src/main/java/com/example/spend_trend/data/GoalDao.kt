package com.example.spend_trend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY deadlineMillis ASC")
    fun getAllActive(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getById(id: Int): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // Optional: total target for all goals
    @Query("SELECT SUM(targetAmount) FROM goals WHERE isActive = 1")
    suspend fun getTotalTargetAmount(): Double?
}
