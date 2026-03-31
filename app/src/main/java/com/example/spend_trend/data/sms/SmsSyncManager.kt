package com.example.spend_trend.data.sms

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.example.spend_trend.data.AppDatabase
import com.example.spend_trend.data.BillEntity
import com.example.spend_trend.data.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsSyncManager(private val context: Context) {

    suspend fun syncLast30Days() = withContext(Dispatchers.IO) {
        sync(daysBack = 30)
    }

    suspend fun sync(daysBack: Int) = withContext(Dispatchers.IO) {
        Log.i("SmsSyncManager", "Starting SMS sync for last $daysBack days")
        
        val database = AppDatabase.getDatabase(context)
        val txDao = database.transactionDao()
        val billDao = database.billDao()

        val timeLimit = System.currentTimeMillis() - (86400000L * daysBack)
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "date > ?"
        val selectionArgs = arrayOf(timeLimit.toString())
        val sortOrder = "date DESC"

        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, selection, selectionArgs, sortOrder
        )

        var txCount = 0
        var billCount = 0

        cursor?.use {
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")
            val addrIdx = it.getColumnIndex("address")

            while (it.moveToNext()) {
                val body = it.getString(bodyIdx)
                val dateMillis = it.getLong(dateIdx)
                val sender = it.getString(addrIdx)

                // 1. Check for transactions
                val parsedTx = SmsParser.parse(body)
                if (parsedTx != null) {
                    val signedAmount = if (parsedTx.isExpense) -parsedTx.amount else parsedTx.amount
                    
                    // Check deduplication (by ref if exists, else by fuzzy matching)
                    val isDuplicate = if (parsedTx.referenceNo != null) {
                        txDao.getByReferenceNo(parsedTx.referenceNo) != null
                    } else {
                        // Fuzzy check: same amount within 5 minutes
                        val window = 5 * 60 * 1000L // 5 minutes
                        txDao.findSimilar(signedAmount, dateMillis - window, dateMillis + window) != null
                    }

                    if (!isDuplicate) {
                        val entity = TransactionEntity(
                            title = parsedTx.merchant,
                            category = parsedTx.category,
                            amount = signedAmount,
                            dateMillis = dateMillis,
                            description = "Auto-tracked from $sender",
                            bankName = parsedTx.bankName,
                            referenceNo = parsedTx.referenceNo
                        )
                        txDao.insert(entity)
                        txCount++
                    }
                }


                // 2. Check for bills
                val parsedBill = SmsParser.parseBill(body)
                if (parsedBill != null) {
                    val isDuplicate = if (parsedBill.referenceNo != null) {
                        billDao.getByReferenceNo(parsedBill.referenceNo) != null
                    } else {
                        // Fuzzy check for bills: same amount and category within 15 days
                        // Bill reminders are usually monthly, so a 15-day window prevents double tracking of the same month's bill
                        val window = 15 * 86400000L
                        billDao.findSimilarBill(parsedBill.amount, parsedBill.category, 
                            parsedBill.dueDateMillis - window, parsedBill.dueDateMillis + window) != null
                    }

                    if (!isDuplicate) {
                        val entity = BillEntity(
                            title = parsedBill.title,
                            amount = parsedBill.amount,
                            category = parsedBill.category,
                            dueDateMillis = parsedBill.dueDateMillis,
                            isPaid = false,
                            referenceNo = parsedBill.referenceNo
                        )
                        billDao.insertBill(entity)
                        billCount++
                    }
                }
            }
        }

        Log.i("SmsSyncManager", "Sync complete. Added $txCount transactions and $billCount bills.")
        return@withContext Pair(txCount, billCount)
    }
}
