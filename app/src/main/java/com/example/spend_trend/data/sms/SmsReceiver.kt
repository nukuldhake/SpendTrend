package com.example.spend_trend.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("SmsReceiver", "onReceive triggered with action: ${intent?.action}")
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        
        // Check if Auto-Tracking is enabled in Preferences
        ThemePreferences.init(context)
        if (!ThemePreferences.autoTrackingEnabled) {
            Log.d("SmsReceiver", "Auto-tracking disabled")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val body = sms.displayMessageBody
            val sender = sms.displayOriginatingAddress ?: "Unknown"

            Log.d("SmsReceiver", "Received SMS from $sender: $body")
            
            // 1. Check for transactions (Payments made)
            val txParsed = SmsParser.parse(body)
            if (txParsed != null) {
                saveTransaction(context, txParsed)
            }

            // 2. Check for bill reminders (Upcoming payments)
            val billParsed = SmsParser.parseBill(body)
            if (billParsed != null) {
                saveBill(context, billParsed)
            }
        }
    }

    private fun saveTransaction(context: Context, parsed: ParsedTransaction) {
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.transactionDao()
                
                // Deduplication check
                if (parsed.referenceNo != null) {
                    val existing = dao.getByReferenceNo(parsed.referenceNo)
                    if (existing != null) {
                        Log.d("SmsReceiver", "Duplicate transaction detected: ${parsed.referenceNo}")
                        return@launch
                    }
                }

                val entity = TransactionEntity(
                    title = parsed.merchant,
                    category = parsed.category,
                    amount = if (parsed.isExpense) -parsed.amount else parsed.amount,
                    dateMillis = System.currentTimeMillis(),
                    description = "Auto-tracked SMS${if (parsed.bankName != null) " (${parsed.bankName})" else ""}",
                    bankName = parsed.bankName,
                    referenceNo = parsed.referenceNo
                )
                dao.insert(entity)
                Log.d("SmsReceiver", "Saved transaction: ${parsed.merchant} - ${parsed.amount}")

                // --- Real-time Budget Check ---
                if (parsed.isExpense) {
                    performBudgetCheck(context, db, parsed.category)
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Failed to save transaction", e)
            }
        }
    }

    private suspend fun performBudgetCheck(context: Context, db: AppDatabase, category: String) {
        val now = java.time.LocalDate.now()
        val monthYear = "${String.format("%02d", now.monthValue)}-${now.year}"
        val budget = db.budgetDao().getByCategory(category, monthYear)
        
        if (budget != null && budget.isActive) {
            val startMillis = now.withDayOfMonth(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = now.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val totalSpent = db.transactionDao().getCategoryExpenseForPeriod(category, startMillis, endMillis) ?: 0
            val absSpent = -totalSpent.toDouble()
            val limit = budget.monthlyLimit
            
            val percentage = (absSpent / limit * 100).toInt()
            val notificationHelper = com.example.spend_trend.ui.util.NotificationHelper(context)
            
            // Check if we just crossed thresholds
            // This is a simple version; in a real app, you'd store notified thresholds to avoid duplicate alerts.
            if (percentage >= 100) {
                notificationHelper.showBudgetAlertNotification(category, 100)
            } else if (percentage >= 80) {
                notificationHelper.showBudgetAlertNotification(category, 80)
            }
        }
    }

    private fun saveBill(context: Context, parsed: ParsedBill) {
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.billDao()

                // Deduplication check
                if (parsed.referenceNo != null) {
                    val existing = dao.getByReferenceNo(parsed.referenceNo)
                    if (existing != null) {
                        Log.d("SmsReceiver", "Duplicate bill detected: ${parsed.referenceNo}")
                        return@launch
                    }
                }

                val entity = BillEntity(
                    title = parsed.title,
                    amount = parsed.amount,
                    category = parsed.category,
                    dueDateMillis = parsed.dueDateMillis,
                    isPaid = false,
                    referenceNo = parsed.referenceNo
                )
                dao.insertBill(entity)
                Log.d("SmsReceiver", "Registered bill: ${parsed.title} - ${parsed.amount}")
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Failed to register bill", e)
            }
        }
    }
}
