package com.example.spend_trend.ui.transaction

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TransactionUi(
    val id: Int = 0,
    val title: String,
    val category: String,
    val amount: Int,
    val date: LocalDate
)

fun TransactionUi.dateLabel(): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("dd MMM"))
        else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}
