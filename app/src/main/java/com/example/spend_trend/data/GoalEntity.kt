package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: String,
    val deadlineMillis: Long,
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
