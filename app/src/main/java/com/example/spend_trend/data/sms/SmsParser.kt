package com.example.spend_trend.data.sms

import android.util.Log
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
        // "due on 15 Oct 2026", "due on 15-10-2026", "due on 15/10/2026"
        Pattern.compile("(?i)due\\s+(?:on|by|date)?[:\\s]*(\\d{1,2}[\\s\\-/\\.](?:[a-z]{3,9}|\\d{1,2})[\\s\\-/\\.]\\d{2,4})"),
        // "due on Oct 15, 2026" (month first)
        Pattern.compile("(?i)due\\s+(?:on|by|date)?[:\\s]*([a-z]{3,9})\\s+(\\d{1,2})[,\\s]+(\\d{2,4})"),
        // "last date 15/10"
        Pattern.compile("(?i)last\\s+date[:\\s]*(\\d{1,2}[\\s\\-/\\.]\\d{1,2})"),
        // "payment due date is Oct 15, 2026"
        Pattern.compile("(?i)due\\s+date\\s+is\\s+([a-z]{3,9}\\s+\\d{1,2}[,\\s]+\\d{2,4})")
    )

    private val LOSS_KEYWORDS = listOf("spent", "paid", "debited", "withdrawal", "sent to", "towards", "txn", "purchase", "dr")
    private val GAIN_KEYWORDS = listOf("credited", "received", "deposited", "refund", "added to", "cashback", "salary", "cr")
    private val BALANCE_KEYWORDS = listOf("available balance", "bal", "available limit", "outstanding", "bal in a/c")
    private val EXCLUSION_KEYWORDS = listOf("otp", "verification", "code", "cvv", "secret", "is due", "bill amount", "due on")

    // ──────────────── CORE PARSING LOGIC ────────────────

    fun parse(message: String): ParsedTransaction? {
        val lowMsg = message.lowercase()
        
        // 1. Initial filter - must look like a transaction and NOT be an exclusion
        if (!isTransactionMessage(lowMsg)) return null
        if (EXCLUSION_KEYWORDS.any { lowMsg.contains(it) }) return null

        // 2. Extract Amount and its position
        val amountResult = findAmountWithPosition(message) ?: return null
        val amountValue = amountResult.first
        val amountPos = amountResult.second
        
        if (amountValue <= 0) return null

        // 3. Determine if it's an Expense or Income based on proximity
        val isExpense = determineIfExpense(lowMsg, amountPos)

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
            amount = amountValue.toInt(),
            merchant = merchant,
            category = category,
            isExpense = isExpense,
            bankName = bankName,
            referenceNo = refNo
        )
    }

    fun parseBill(message: String): ParsedBill? {
        val lowMsg = message.lowercase()
        
        // 1. Check if it's a bill reminder (more flexible indicators)
        val indicators = listOf("bill", "recharge", "postpaid", "electricity", "rent", "installment", "emi", "due on", "bill amount")
        if (!indicators.any { lowMsg.contains(it) } || !lowMsg.contains("due")) return null

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
                val groupCount = matcher.groupCount()
                try {
                    val date: java.time.LocalDate = if (groupCount >= 3) {
                        // Handle "Month Day, Year"
                        val month = matcher.group(1) ?: ""
                        val day = matcher.group(2) ?: ""
                        val year = matcher.group(3) ?: ""
                        
                        val monthVal = parseMonth(month)
                        val dayVal = day.toInt()
                        val yearVal = if (year.length == 2) 2000 + year.toInt() else year.toInt()
                        
                        java.time.LocalDate.of(yearVal, monthVal, dayVal)
                    } else {
                        // Handle "Day Month Year" or other single-string formats
                        val dateStr = matcher.group(1) ?: continue
                        val cleanDate = dateStr.replace(Regex("[\\s/\\.]"), "-")
                        val formats = listOf(
                            "dd-MM-yyyy", "d-MM-yyyy", "dd-MMM-yyyy", "d-MMM-yyyy",
                            "dd-MM-yy", "d-MM-yy", "MMM-dd-yyyy", "dd-MM"
                        )
                        
                        var result: java.time.LocalDate? = null
                        for (fmt in formats) {
                            try {
                                val formatter = java.time.format.DateTimeFormatter.ofPattern(fmt, java.util.Locale.ENGLISH)
                                result = if (fmt.contains("yyyy") || fmt.contains("yy")) {
                                    java.time.LocalDate.parse(cleanDate, formatter)
                                } else {
                                    val partial = java.time.format.DateTimeFormatter.ofPattern(fmt).parse(cleanDate)
                                    java.time.LocalDate.now().withMonth(partial.get(java.time.temporal.ChronoField.MONTH_OF_YEAR))
                                        .withDayOfMonth(partial.get(java.time.temporal.ChronoField.DAY_OF_MONTH))
                                }
                                break
                            } catch (e: Exception) { continue }
                        }
                        result ?: continue
                    }
                    return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    Log.e("SmsParser", "Date parse error in ${matcher.group(0)}", e)
                }
            }
        }
        return null // If no date found, return null and let caller decide
    }

    private fun parseMonth(m: String): Int {
        val low = m.lowercase()
        return when {
            low.startsWith("jan") -> 1
            low.startsWith("feb") -> 2
            low.startsWith("mar") -> 3
            low.startsWith("apr") -> 4
            low.startsWith("may") -> 5
            low.startsWith("jun") -> 6
            low.startsWith("jul") -> 7
            low.startsWith("aug") -> 8
            low.startsWith("sep") -> 9
            low.startsWith("oct") -> 10
            low.startsWith("nov") -> 11
            low.startsWith("dec") -> 12
            else -> 1
        }
    }

    private fun isTransactionMessage(msg: String): Boolean {
        // High confidence indicators that signify a banking/payment SMS
        val indicators = listOf("rs", "inr", "₹", "upi", "vpa", "txn", "spent", "paid", "debited", "credited")
        
        // 1. Must have at least one currency indicator
        if (!indicators.any { msg.contains(it) }) return false
        
        // 2. Must have an action verb - this excludes balance-only or random info messages
        val hasAction = (GAIN_KEYWORDS + LOSS_KEYWORDS).any { msg.contains(it) }
        if (!hasAction) return false

        // 3. Stricter balance alert exclusion
        // If "balance" or "bal" appears, and "debited"/"credited" doesn't appear earlier, it might be an alert
        val hasBalance = BALANCE_KEYWORDS.any { msg.contains(it) }
        if (hasBalance) {
            // Check if "debited" or "credited" is the primary subject (usually at the start)
            val firstAction = (GAIN_KEYWORDS + LOSS_KEYWORDS).map { msg.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
            val firstBalance = BALANCE_KEYWORDS.map { msg.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
            
            // If balance keyword is the very first thing (or before any action), skip it
            if (firstBalance < firstAction && firstAction > 20) return false
        }
        
        return true
    }

    private fun determineIfExpense(msg: String, amountPos: Int): Boolean {
        // Prioritize the keyword closest to the amount
        val expenseDist = LOSS_KEYWORDS
            .map { msg.indexOf(it) }
            .filter { it != -1 }
            .map { Math.abs(it - amountPos) }
            .minOrNull() ?: Int.MAX_VALUE

        val incomeDist = GAIN_KEYWORDS
            .map { msg.indexOf(it) }
            .filter { it != -1 }
            .map { Math.abs(it - amountPos) }
            .minOrNull() ?: Int.MAX_VALUE

        if (expenseDist < incomeDist) return true
        if (incomeDist < expenseDist) return false

        // Fallback to weight-based or first keyword
        val firstExpense = LOSS_KEYWORDS.map { msg.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
        val firstIncome = GAIN_KEYWORDS.map { msg.indexOf(it) }.filter { it != -1 }.minOrNull() ?: Int.MAX_VALUE
        return firstExpense < firstIncome
    }

    private fun findAmountWithPosition(message: String): Pair<Double, Int>? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val cleaned = matcher.group(1)?.replace(",", "")
                val value = cleaned?.toDoubleOrNull()
                if (value != null && value >= 1.0 && value < 1000000.0) {
                    return Pair(value, matcher.start())
                }
            }
        }
        return null
    }

    private fun findAmount(message: String): Double? {
        return findAmountWithPosition(message)?.first
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
        // Remove VPA IDs (@upi, @okaxis, etc)
        var clean = if (m.contains("@")) m.split("@")[0] else m
        
        // Remove common prefixes
        clean = clean.replace(Regex("(?i)^(vpa|to|from|at|paid to|sent to|transfer to|spent on)\\s+"), "")
        
        // Remove punctuation and special chars often found in bank SMS
        clean = clean.replace(Regex("[.;*:\\-_]"), " ").trim()
        
        // If it's a long number-heavy string (like a transaction ID), truncate it
        if (clean.count { it.isDigit() } > clean.count { it.isLetter() }) {
            clean = clean.take(12)
        }
        
        // Limit length but keep it readable
        clean = clean.take(25)
        
        // Capitalize words
        return clean.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
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

    private fun containsAny(merchant: String, msg: String, vararg keywords: String): Boolean {
        return keywords.any { merchant.contains(it) || msg.contains(it) }
    }
}
