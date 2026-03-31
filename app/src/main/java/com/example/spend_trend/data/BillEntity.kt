package com.example.spend_trend.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val amount: Int = 0,
    val category: String = "Other",
    val dueDateMillis: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val referenceNo: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDueDate(): LocalDate = LocalDate.ofEpochDay(dueDateMillis / 86400000L)
}
