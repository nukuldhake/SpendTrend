package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val category: String = "Other",
    val deadlineMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
