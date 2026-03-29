package com.example.spend_trend.data.sms

import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Int,
    val merchant: String,
    val category: String = "Auto-tracked",
    val isExpense: Boolean = true,
    val bankName: String? = null,
    val referenceNo: String? = null
)

data class ParsedBill(
    val amount: Int,
    val title: String,
    val category: String,
    val dueDateMillis: Long,
    val referenceNo: String? = null
)

object SmsParser {
    // ────────────── REFINED REGEX PATTERNS ──────────────

    private val AMOUNT_PATTERNS = listOf(
        // "Rs. 1,000.00", "INR 500", "Amt 200"
        Pattern.compile("(?i)(?:rs|inr|amt|amount|spent|paid|vpa|debit(?:ed)?|credit(?:ed)?|withdrawal|transfer)\\.?\\s*(?:of)?\\s*([\\d,]+(?:\\.\\d{1,2})?)"),
        // "for ₹500.00", "sent ₹500"
        Pattern.compile("(?i)(?:for|sent|received|total|₹)\\s*(?:₹|rs)?\\s*([\\d,]+(?:\\.\\d{1,2})?)"),
        // "debited by 500"
        Pattern.compile("(?i)(?:debited|credited)\\s+by\\s*([\\d,]+(?:\\.\\d{1,2})?)")
    )
    
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?i)at\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)to\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)vpa\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)from\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)paid\\s+to\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)info[:*]\\s*([^\\s.;]+)"),
        Pattern.compile("(?i)towards\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)transfer\\s+to\\s+([^\\s.;]+)"),
        Pattern.compile("(?i)spent\\s+on\\s+([^\\s.;]+)")
    )

    private val BANK_PATTERNS = listOf(
        Pattern.compile("(?i)(hdfc|icici|sbi|axis|kotak|rbl|idfc|paytm|bob|canara|pnb|yes bank|hsbc|standard chartered|federal|union|indusind)"),
        Pattern.compile("(?i)on\\s+(?:a/c|acct|acc)\\s+(\\d*X+\\d+)")
    )

    private val REF_PATTERNS = listOf(
        Pattern.compile("(?i)(?:ref|rrn|txn|id|upi)[:*]\\s*([a-z0-9]+)"),
        Pattern.compile("(?i)txn\\s*([0-9]+)"),
        Pattern.compile("(?i)rrn\\s*([0-9]+)")
    )

    private val DUE_DATE_PATTERNS = listOf(
        // "due on 15 Oct 2026", "due on 15-10-2026"
        Pattern.compile("(?i)due\\s+(?:on|by|date)?[:\\s]*(\\d{1,2}[\\s\\-/\\.](?:[a-z]{3,9}|\\d{1,2})[\\s\\-/\\.]\\d{2,4})"),
        // "last date 15/10" (assumes current year)
        Pattern.compile("(?i)last\\s+date[:\\s]*(\\d{1,2}[\\s\\-/\\.]\\d{1,2})")
    )

    private val BILL_KEYWORDS = listOf("bill", "recharge", "postpaid", "electricity", "rent", "installment", "emi")

    private val INCOME_KEYWORDS = listOf("credited", "received", "deposited", "refund", "added to", "cashback", "salary")
    private val EXPENSE_KEYWORDS = listOf("spent", "paid", "debited", "withdrawal", "sent to", "towards", "txn", "purchase")

    // ──────────────── CORE PARSING LOGIC ────────────────

    fun parse(message: String): ParsedTransaction? {
        val lowMsg = message.lowercase()
        
        // 1. Initial filter - must look like a transaction
        if (!isTransactionMessage(lowMsg)) return null

        // 2. Extract Amount
        val amount = findAmount(message) ?: return null
        if (amount <= 0) return null

        // 3. Determine if it's an Expense or Income
        val isExpense = determineIfExpense(lowMsg)

        // 4. Extract Merchant Name
        var merchant = findMerchant(message) ?: "Digital Transaction"
        // Cleanup merchant string
        merchant = cleanupMerchant(merchant)

        // 5. Intelligent Categorization
        val category = categorize(merchant, lowMsg)

        // 6. Bank & Reference Info
        val bankName = findBank(message)
        val refNo = findReference(message)

        return ParsedTransaction(
            amount = amount.toInt(),
            merchant = merchant,
            category = category,
            isExpense = isExpense,
            bankName = bankName,
            referenceNo = refNo
        )
    }

    fun parseBill(message: String): ParsedBill? {
        val lowMsg = message.lowercase()
        
        // 1. Check if it's a bill reminder
        if (!BILL_KEYWORDS.any { lowMsg.contains(it) } || !lowMsg.contains("due")) return null

        // 2. Extract Amount
        val amount = findAmount(message) ?: return null

        // 3. Extract Due Date
        val dueDateMillis = findDueDate(message) ?: return null

        // 4. Extract Title (Merchant)
        val title = findMerchant(message) ?: "Service Provider"

        // 5. Categorize
        val category = categorize(title, lowMsg)

        return ParsedBill(
            amount = amount.toInt(),
            title = cleanupMerchant(title),
            category = category,
            dueDateMillis = dueDateMillis,
            referenceNo = findReference(message)
        )
    }

    private fun findDueDate(message: String): Long? {
        for (pattern in DUE_DATE_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                // For simplicity, we'll try to parse common formats or return current + 1 week
                // A real implementation would use a robust date parser
                return System.currentTimeMillis() + (86400000L * 7) // Placeholder: 1 week from now
            }
        }
        return null
    }

    private fun isTransactionMessage(msg: String): Boolean {
        // High confidence keywords that signify a banking/payment SMS
        val indicators = listOf("rs", "inr", "₹", "spent", "paid", "debited", "credited", "bank", "a/c", "acct", "transaction", "upi", "vpa")
        return indicators.any { msg.contains(it) }
    }

    private fun determineIfExpense(msg: String): Boolean {
        // Special case: "Credited for refund" is income, but "Debited for Bill" is expense
        if (msg.contains("credited") || msg.contains("received") || msg.contains("deposited") || msg.contains("refund")) {
            // Check if there's a strong reversal like "debited for refund" (rare)
            return false
        }
        if (msg.contains("debited") || msg.contains("spent") || msg.contains("paid") || msg.contains("withdrawal")) return true
        
        // Count keyword weights
        val incomeWeight = INCOME_KEYWORDS.count { msg.contains(it) }
        val expenseWeight = EXPENSE_KEYWORDS.count { msg.contains(it) }
        return expenseWeight >= incomeWeight
    }

    private fun categorize(merchant: String, msg: String): String {
        val m = merchant.lowercase()
        return when {
            // Food & Dining
            containsAny(m, msg, "zomato", "swiggy", "starbucks", "dominos", "kfc", "dine", "restaurant", "food", "eat", "cafe", "bakery", "pizza", "burger", "mcdonalds") -> "Food"
            
            // Transport & Fuel
            containsAny(m, msg, "uber", "ola", "rapido", "petrol", "fuel", "hpcl", "bpcl", "iocl", "shell", "travel", "metro", "bus", "train", "railway", "irctc", "redbus", "indigo", "air", "bridge") -> "Transport"
            
            // Shopping & Grocery
            containsAny(m, msg, "amazon", "flipkart", "myntra", "ajio", "reliance", "mart", "blinkit", "zepto", "bigbasket", "instamart", "shopping", "store", "supermarket", "grocery", "mall", "dmart", "fossil", "tata") -> "Shopping"
            
            // Entertainment
            containsAny(m, msg, "netflix", "hotstar", "cinema", "pvr", "bookmyshow", "spotify", "google play", "apple", "itunes", "gaming", "steam", "sony", "prime") -> "Entertainment"
            
            // Bills & Utilities
            containsAny(m, msg, "airtel", "jio", "vi", "mobile", "recharge", "electricity", "rent", "maintenance", "gas", "bsnl", "broadband", "wifi", "water", "act", "tata sky", "dth") -> "Bills"
            
            // Health & Wellness
            containsAny(m, msg, "hospital", "pharmacy", "medicine", "apollo", "doc", "health", "gym", "cult", "yoga", "clinic", "pathology", "1mg") -> "Health"
            
            // Education
            containsAny(m, msg, "school", "college", "university", "udemy", "coursera", "fees", "course", "education") -> "Education"
            
            // Income / Salary
            containsAny(m, msg, "salary", "bonus", "dividend", "interest", "stipend") -> "Salary"

            // Default
            else -> "Other"
        }
    }

    private fun findAmount(message: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val cleaned = matcher.group(1)?.replace(",", "")
                val value = cleaned?.toDoubleOrNull()
                // Sanity check: transactions usually aren't less than 1 Re or more than 10 Lakhs in SMS
                if (value != null && value >= 1.0 && value < 1000000.0) return value
            }
        }
        return null
    }

    private fun findMerchant(message: String): String? {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val m = matcher.group(1)?.trim()
                if (m != null && m.length > 2) {
                    // Filter out bank account strings like "XX123"
                    if (Regex("^[\\dX*]+$").matches(m)) continue
                    return m
                }
            }
        }
        return null
    }

    private fun findBank(message: String): String? {
        for (pattern in BANK_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)?.uppercase()
            }
        }
        return null
    }

    private fun findReference(message: String): String? {
        for (pattern in REF_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return null
    }

    private fun cleanupMerchant(m: String): String {
        var clean = m.replace(Regex("[.;*:\\-]"), " ").trim()
        clean = clean.split(" ")[0].take(20) // Take first word if it's too long, or limit to 20 chars
        if (m.contains("@")) { // Probably a VPA/UPI ID
             clean = m.split("@")[0]
        }
        // Capitalize words
        return clean.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    private fun containsAny(merchant: String, msg: String, vararg keywords: String): Boolean {
        return keywords.any { merchant.contains(it) || msg.contains(it) }
    }
}
