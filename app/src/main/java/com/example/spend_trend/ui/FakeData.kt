package com.example.spend_trend.ui

import com.example.spend_trend.ui.transaction.TransactionUi
import java.time.LocalDate

object FakeData {
    val transactions = mutableListOf<TransactionUi>()

    init {
        // Initial fake data (same as your current fake list)
        transactions.addAll(
            listOf(
                TransactionUi("Starbucks", "Food", -320, LocalDate.now()),
                TransactionUi("Uber", "Transport", -450, LocalDate.now()),
                TransactionUi("Salary", "Income", 45000, LocalDate.now().minusDays(1)),
                TransactionUi("Amazon", "Shopping", -1299, LocalDate.now().minusDays(10)),
                TransactionUi("Movie Tickets", "Entertainment", -500, LocalDate.now().minusMonths(1))
            )
        )
    }

    fun addTransaction(tx: TransactionUi) {
        transactions.add(0, tx) // add to top
    }

    // Later: edit, delete, etc.
}