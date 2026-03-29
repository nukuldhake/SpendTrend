package com.example.spend_trend.data.model

data class ForecastInsight(
    val title: String,
    val description: String,
    val type: InsightType = InsightType.NEUTRAL
)

enum class InsightType { POSITIVE, NEGATIVE, NEUTRAL, WARNING }
