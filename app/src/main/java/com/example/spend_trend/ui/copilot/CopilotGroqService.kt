package com.example.spend_trend.ui.copilot

import com.example.spend_trend.data.TransactionEntity
import com.example.spend_trend.data.BudgetEntity
import com.example.spend_trend.data.model.ForecastInsight
import com.example.spend_trend.data.model.InsightType
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- Data Models for Groq API (OpenAI Compatible) ---

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

// --- Retrofit Interface ---

interface GroqApi {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): GroqResponse
}

// --- Service Implementation ---

class CopilotGroqService(private val apiKey: String) {

    private val groqApi: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GroqApi::class.java)
    }

    private val modelName = "llama-3.3-70b-versatile" // Using Llama 3.3 70B

    suspend fun getCopilotResponse(
        userInput: String,
        allTransactions: List<TransactionEntity>,
        allBudgets: List<BudgetEntity>,
        allGoals: List<com.example.spend_trend.data.GoalEntity> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemPrompt(allTransactions, allBudgets, allGoals)
            
            val request = GroqRequest(
                model = modelName,
                messages = listOf(
                    GroqMessage("system", systemPrompt),
                    GroqMessage("user", userInput)
                )
            )

            val response = groqApi.getChatCompletion("Bearer $apiKey", request)
            response.choices.firstOrNull()?.message?.content ?: "I am sorry, I couldn't understand that."
        } catch (e: Exception) {
            "I'm having trouble connecting to Groq. Error: ${e.localizedMessage}"
        }
    }

    private fun buildSystemPrompt(
        txs: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        goals: List<com.example.spend_trend.data.GoalEntity> = emptyList()
    ): String {
        val now = LocalDate.now()
        val currentMonthStart = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lastMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lastMonthEnd = now.withDayOfMonth(1).minusDays(1).atTime(23, 59, 59).toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now())).toEpochMilli()

        val thisMonthTxs = txs.filter { it.dateMillis >= currentMonthStart }
        val totalSpent = thisMonthTxs.filter { it.amount < 0 }.sumOf { -it.amount.toDouble() }
        val totalIncome = thisMonthTxs.filter { it.amount > 0 }.sumOf { it.amount.toDouble() }
        
        val categoryBreakdown = thisMonthTxs.filter { it.amount < 0 }
            .groupBy { it.category }
            .mapValues { (_, txList) -> txList.sumOf { -it.amount.toDouble() } }

        val lastMonthTxs = txs.filter { it.dateMillis in lastMonthStart..lastMonthEnd }
        val lastMonthTotalSpent = lastMonthTxs.filter { it.amount < 0 }.sumOf { -it.amount.toDouble() }
        val lastMonthCategoryBreakdown = lastMonthTxs.filter { it.amount < 0 }
            .groupBy { it.category }
            .mapValues { (_, txList) -> txList.sumOf { -it.amount.toDouble() } }

        val budgetInfo = budgets.map { 
            val spent = thisMonthTxs.filter { tx -> tx.category == it.category }.sumOf { tx -> -tx.amount.toDouble() }
            "Category: ${it.category}, Limit: ₹${it.monthlyLimit}, Currently Spent: ₹$spent (${if(it.monthlyLimit > 0) (spent/it.monthlyLimit * 100).toInt() else 0}% of budget)"
        }.joinToString("\n")

        val goalInfo = goals.filter { it.isActive }.joinToString("\n") {
            "Goal: ${it.title}, Target: ₹${it.targetAmount}, Current: ₹${it.currentAmount} (${if(it.targetAmount > 0) (it.currentAmount/it.targetAmount * 100).toInt() else 0}% progress)"
        }

        val recentTxs = txs.takeLast(15).reversed().joinToString("\n") { 
            val date = Instant.ofEpochMilli(it.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            "${date}: ${it.title} - ₹${it.amount} (${it.category})"
        }

        val growthSummary = if (lastMonthTotalSpent > 0) {
            val growth = (((totalSpent - lastMonthTotalSpent) / lastMonthTotalSpent) * 100).toInt()
            "Total spending is $growth% ${if (growth >= 0) "UP" else "DOWN"} compared to last month (₹$lastMonthTotalSpent)."
        } else "No spending data for last month."

        return """
            You are 'Sakura', a high-end financial advisor and personal lifestyle companion.
            Tone: Professional, encouraging, and deeply analytical. You speak with grace and precision.
            TODAY'S DATE: ${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}

            DASHBOARD:
            - Spending: ₹$totalSpent, Income: ₹$totalIncome, Growth: $growthSummary
            - Budgets: $budgetInfo
            - Savings Goals: $goalInfo
            - Categories: [${categoryBreakdown.entries.joinToString { "${it.key}: ₹${it.value}" }}]
            - Recent Activity: $recentTxs

            INSTRUCTIONS:
            1. Keep responses under 150 words.
            2. Bold key figures. Use '₹' for currency.
            3. Provide specific advice for over-budget categories.
            4. Never mention internal system labels or specific database IDs.
            5. Since you are Sakura, maintain a theme of growth and renewal in your advice.
        """.trimIndent()
    }

    suspend fun getForecastInsights(
        history: List<Float>,
        projections: List<Float>
    ): List<ForecastInsight> = withContext(Dispatchers.IO) {
        try {
            val historyStr = history.joinToString(", ") { "₹${it.toInt()}" }
            val projectionsStr = projections.joinToString(", ") { "₹${it.toInt()}" }
            
            val prompt = """
                Analyze these trends and provide 3 deep, actionable insights.
                HISTORICAL: $historyStr
                PROJECTED: $projectionsStr
                
                FORMAT EXACTLY:
                TITLE: [Title] | DESCRIPTION: [Advice] | TYPE: [POSITIVE/NEGATIVE/NEUTRAL/WARNING]
            """.trimIndent()

            val request = GroqRequest(
                model = modelName,
                messages = listOf(GroqMessage("user", prompt))
            )

            val response = groqApi.getChatCompletion("Bearer $apiKey", request)
            val text = response.choices.firstOrNull()?.message?.content ?: ""
            
            text.lines()
                .filter { it.contains("|") }
                .take(3)
                .mapNotNull { line ->
                    try {
                        val parts = line.split("|").associate { 
                            val pair = it.split(":", limit = 2) 
                            pair[0].trim().uppercase() to pair[1].trim()
                        }
                        ForecastInsight(
                            title = parts["TITLE"] ?: "Insight",
                            description = parts["DESCRIPTION"] ?: "",
                            type = try { InsightType.valueOf(parts["TYPE"] ?: "NEUTRAL") } catch (e: Exception) { InsightType.NEUTRAL }
                        )
                    } catch (e: Exception) { null }
                }
        } catch (e: Exception) {
            listOf(ForecastInsight("Analysis Unavailable", "Error connecting to Groq.", InsightType.WARNING))
        }
    }
}
