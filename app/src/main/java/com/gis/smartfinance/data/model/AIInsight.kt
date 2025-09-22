package com.gis.smartfinance.data.model

import java.util.Date
import java.util.UUID

/**
 * Enhanced AI Insight data model with comprehensive analysis capabilities
 */
data class AIInsight(
    val id: String = UUID.randomUUID().toString(),
    val type: InsightType,
    val title: String,
    val description: String,
    val savingsPotential: Double? = null,
    val category: String? = null,
    val priority: InsightPriority,
    val actionable: Boolean = true,
    val createdAt: Date = Date(),
    val confidence: Float = 0.8f,  // How confident the AI is about this insight
    val actionItems: List<String> = emptyList(),  // Specific actions user can take
    val timeframe: InsightTimeframe = InsightTimeframe.MONTHLY,  // Timeframe for the insight
    val metrics: InsightMetrics? = null  // Associated metrics
)

/**
 * Types of insights the AI can generate
 */
enum class InsightType {
    SPENDING_PATTERN,      // Detected patterns in spending
    SAVING_OPPORTUNITY,    // Opportunities to save money
    SUBSCRIPTION_ALERT,    // Recurring/subscription related
    BUDGET_WARNING,        // Budget exceeded or nearly exceeded
    GOAL_PROGRESS,         // Progress towards savings goals
    COMPARISON,            // Comparison with previous periods
    RECOMMENDATION,        // General recommendations
    ANOMALY,              // Unusual spending detected
    CATEGORY_ANALYSIS,    // Deep dive into specific category
    INCOME_ANALYSIS,      // Income patterns and stability
    EMERGENCY_FUND,       // Emergency fund status
    OPTIMIZATION          // Optimization opportunities
}

/**
 * Priority levels for insights
 */
enum class InsightPriority {
    LOW,       // Nice to know
    MEDIUM,    // Should consider
    HIGH,      // Important to address
    URGENT     // Requires immediate attention
}

/**
 * Timeframe for the insight
 */
enum class InsightTimeframe {
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

/**
 * Metrics associated with an insight
 */
data class InsightMetrics(
    val currentValue: Double,
    val targetValue: Double,
    val percentageChange: Float,
    val trend: TrendDirection,
    val comparisonPeriod: String? = null
)

/**
 * Direction of trend
 */
enum class TrendDirection {
    INCREASING,
    DECREASING,
    STABLE,
    VOLATILE
}

/**
 * Personalized recommendation based on user behavior
 */
data class PersonalizedRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: RecommendationCategory,
    val expectedImpact: String,
    val difficulty: DifficultyLevel,
    val timeToImplement: String,  // e.g., "5 minutes", "1 hour", "ongoing"
    val savingsPotential: Double? = null,
    val priority: Int = 0,  // Higher number = higher priority
    val relatedInsights: List<String> = emptyList(),  // IDs of related insights
    val createdAt: Date = Date()
)

/**
 * Categories for recommendations
 */
enum class RecommendationCategory {
    BUDGETING,
    SAVING,
    SPENDING_REDUCTION,
    INCOME_INCREASE,
    INVESTMENT,
    DEBT_REDUCTION,
    LIFESTYLE,
    AUTOMATION,
    EDUCATION
}

/**
 * Difficulty level for implementing recommendation
 */
enum class DifficultyLevel {
    EASY,      // Can be done immediately
    MEDIUM,    // Requires some effort
    HARD       // Requires significant change
}

/**
 * Spending behavior analysis
 */
data class SpendingBehavior(
    val id: String = UUID.randomUUID().toString(),
    val pattern: BehaviorPattern,
    val description: String,
    val frequency: String,  // e.g., "Daily", "Weekly", "Occasionally"
    val averageAmount: Double,
    val impact: SpendingImpact,
    val suggestion: String,
    val detectedAt: Date = Date()
)

/**
 * Types of spending behavior patterns
 */
enum class BehaviorPattern {
    IMPULSE_BUYING,        // Frequent small unplanned purchases
    WEEKEND_SPLURGE,       // Higher spending on weekends
    PAYDAY_SPIKE,          // Spending spike after income
    STRESS_SPENDING,       // Emotional spending patterns
    SUBSCRIPTION_CREEP,    // Gradual increase in subscriptions
    CATEGORY_CONCENTRATION, // Too much spending in one category
    IRREGULAR_INCOME,      // Variable income patterns
    CONSISTENT_SAVER,      // Good saving habits
    BUDGET_CONSCIOUS       // Stays within budget
}

/**
 * Impact level of spending behavior
 */
enum class SpendingImpact {
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    CRITICAL
}

/**
 * Financial health assessment
 */
data class FinancialHealthAssessment(
    val overallScore: Int,  // 0-100
    val savingsScore: Int,
    val spendingScore: Int,
    val incomeStabilityScore: Int,
    val debtScore: Int,
    val emergencyFundScore: Int,
    val investmentScore: Int,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val immediateActions: List<String>,
    val longTermGoals: List<String>,
    val assessmentDate: Date = Date()
)

/**
 * Category-specific advice
 */
data class CategoryAdvice(
    val category: String,
    val currentSpending: Double,
    val recommendedBudget: Double,
    val savingsPotential: Double,
    val tips: List<String>,
    val alternatives: List<String>,  // Alternative options for this category
    val benchmarkComparison: String  // How user compares to average
)

/**
 * Time-based spending analysis
 */
data class TimeBasedAnalysis(
    val period: String,  // e.g., "Morning", "Weekend", "End of month"
    val averageSpending: Double,
    val transactionCount: Int,
    val topCategories: List<String>,
    val insight: String,
    val recommendation: String
)

/**
 * Subscription management insight
 */
data class SubscriptionInsight(
    val subscriptionName: String,
    val monthlyCost: Double,
    val yearlyImpact: Double,
    val usageEstimate: String,  // e.g., "Low", "Medium", "High"
    val valueAssessment: String,  // Is it worth it?
    val alternatives: List<String>,
    val cancellationImpact: String
)

/**
 * Progress tracking for financial goals
 */
data class GoalProgress(
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progressPercentage: Float,
    val estimatedCompletionDate: Date?,
    val monthlyContributionNeeded: Double,
    val onTrack: Boolean,
    val adjustmentSuggestion: String?
)