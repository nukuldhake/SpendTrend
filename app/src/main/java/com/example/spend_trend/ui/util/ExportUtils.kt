package com.example.spend_trend.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.spend_trend.data.TransactionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    fun exportTransactionsToCsv(context: Context, transactions: List<TransactionEntity>) {
        val csvHeader = "ID,Date,Title,Category,Amount,Description,Bank\n"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        val csvContent = transactions.joinToString("\n") { tx ->
            val date = sdf.format(Date(tx.dateMillis))
            val desc = tx.description?.replace("\"", "\"\"") ?: ""
            "${tx.id},$date,\"${tx.title.replace("\"", "\"\"")}\",\"${tx.category}\",${tx.amount},\"$desc\",\"${tx.bankName ?: ""}\""
        }

        val fullCsv = csvHeader + csvContent
        val fileName = "SpendTrend_Export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        
        try {
            val file = File(context.cacheDir, fileName)
            file.writeText(fullCsv)
            
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "SpendTrend Transaction Export")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export CSV"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
