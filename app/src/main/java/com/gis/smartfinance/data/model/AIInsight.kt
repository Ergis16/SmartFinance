package com.gis.smartfinance.data.model

import java.util.Date
import java.util.UUID

data class AIInsight(
    val id: String = UUID.randomUUID().toString(),
    val type: InsightType,
    val title: String,
    val description: String,
    val savingsPotential: Double? = null,
    val category: String? = null,
    val priority: InsightPriority,
    val actionable: Boolean = true,
    val createdAt: Date = Date()
)

enum class InsightType {
    SPENDING_PATTERN, SAVING_OPPORTUNITY, SUBSCRIPTION_ALERT,
    BUDGET_WARNING, GOAL_PROGRESS, COMPARISON, RECOMMENDATION
}

enum class InsightPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}