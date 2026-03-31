package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String = "Other",           // e.g. "Food", "Transport"
    val monthlyLimit: Float = 0f,        // target limit for the month
    val currentSpent: Float = 0f,   // we'll update this from transactions
    val monthYear: String = "",          // "2025-03" format to identify the month
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)