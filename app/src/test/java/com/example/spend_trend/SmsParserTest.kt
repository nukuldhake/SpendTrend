package test

import com.example.spend_trend.data.sms.SmsParser

fun main() {
    val testCases = listOf(
        "Update from CRED: Payment for your Axis Bank credit card XXXX-2425 is due on Mar 07, 2026. Bill amount: INR 10,888 Tap on the link to pay: https://link.cred.club/CREDIN/link/9yOx3ZZH and avoid late payment fees.",
        "Bal in YES BANK Ac XX0266 on EOD 29MAR26 is INR 11,638.53. Tot. bal (incl. linked deposits & Limit) is INR 11,638.53",
        "OTP is 4638 for Cash WDL of Rs 10000 with Req Id:660525 on IDBI Bank ATM ID057201,Card No.8476.Dont share OTP to anyone.If not done by you, call-18002094324",
        "IDBI Bank Acct XX774 debited for Rs 2730.00 on 22-Mar-26; Bal Rs 13553.08 ARYAN SINGH credited. UPI:644729646261. To Block UPI send SMS UPIBLOCK <Mob. No> to 07799000298 or call 18002094324-IDBI Bank"
    )

    println("--- SMS PARSER VERIFICATION ---\n")

    testCases.forEachIndexed { index, msg ->
        println("Case ${index + 1}: ${msg.take(60)}...")
        val tx = SmsParser.parse(msg)
        val bill = SmsParser.parseBill(msg)

        if (tx != null) {
            println("  [TRANSACTION] Amount: ${tx.amount}, IsExpense: ${tx.isExpense}, Merchant: ${tx.merchant}")
        } else {
            println("  [TRANSACTION] Skipped (Correct)")
        }

        if (bill != null) {
            println("  [BILL] Amount: ${bill.amount}, Title: ${bill.title}")
        } else {
            println("  [BILL] No Bill Found")
        }
        println()
    }
}
