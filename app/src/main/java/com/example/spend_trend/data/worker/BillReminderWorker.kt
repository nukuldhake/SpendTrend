package com.example.spend_trend.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.ui.util.NotificationHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BillReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val billDao = database.billDao()
        val notificationHelper = NotificationHelper(applicationContext)

        val pendingBills = billDao.getPendingBillsNow()
        val today = LocalDate.now()
        
        // Notify for bills due today or in the next 2 days
        val upcomingBills = pendingBills.filter { 
            val dueDate = it.getDueDate()
            dueDate == today || (dueDate.isAfter(today) && dueDate.isBefore(today.plusDays(3)))
        }

        if (upcomingBills.isNotEmpty()) {
            val title = "Upcoming Bill Payments"
            val message = if (upcomingBills.size == 1) {
                val bill = upcomingBills[0]
                "Your bill '${bill.title}' for ₹${bill.amount} is due on ${bill.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM"))}."
            } else {
                val totalAmount = upcomingBills.sumOf { it.amount }
                "You have ${upcomingBills.size} bills due soon. Total: ₹$totalAmount."
            }
            notificationHelper.showBillReminderNotification(title, message)
        }

        return Result.success()
    }
}
