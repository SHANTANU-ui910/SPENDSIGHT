package com.spendsight.app.domain.model

data class AnalyticsSummary(
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netBalance: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val burnRatePerHour: Double = 0.0,
    val percentageVsPreviousPeriod: Double = 0.0, // e.g. +14.2%
    val smartInsightHeadline: String = "Tracking live telemetry",
    val smartInsightDetail: String = "No unusual spikes detected."
)

data class BudgetStatus(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val spentAmount: Double,
    val limitAmount: Double,
    val percentageSpent: Int, // e.g. 82%
    val isOverLimit: Boolean = false,
    val isNearWarning: Boolean = false // > 85%
)

