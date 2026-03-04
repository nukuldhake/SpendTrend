package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val amount: Int,           // positive = income, negative = expense
    val dateMillis: Long,      // we store date as millis for Room
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to convert back to LocalDate when needed
    fun getDate(): LocalDate = LocalDate.ofEpochDay(dateMillis / 86400000L)
}