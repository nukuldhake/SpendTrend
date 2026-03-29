package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Int,
    val category: String,
    val dueDateMillis: Long,
    val isPaid: Boolean = false,
    val referenceNo: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDueDate(): LocalDate = LocalDate.ofEpochDay(dueDateMillis / 86400000L)
}
