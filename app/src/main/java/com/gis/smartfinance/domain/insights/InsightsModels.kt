package com.gis.smartfinance.domain.insights


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain models for Insights feature
 * Separated from UI for clean architecture
 */

/**
 * Complete analysis result
 */
data class InsightsAnalysis(
    val healthScore: Int,
    val scoreBreakdown: ScoreBreakdown,
    val scoreExplanation: String,
    val savingsPotential: Double,
    val insights: List<Insight>,
    val recommendations: List<Recommendation>,
    val spendingPatterns: List<SpendingPattern>,
    val daysOfData: Int,
    val dataQuality: DataQuality
) {
    companion object {
        fun empty() = InsightsAnalysis(
            healthScore = 0,
            scoreBreakdown = ScoreBreakdown(0, 0, 0, 0),
            scoreExplanation = "",
            savingsPotential = 0.0,
            insights = emptyList(),
            recommendations = emptyList(),
            spendingPatterns = emptyList(),
            daysOfData = 0,
            dataQuality = DataQuality.INSUFFICIENT
        )
    }
}

/**
 * Health score breakdown by category
 */
data class ScoreBreakdown(
    val savingsScore: Int,      // 0-100: How much you're saving
    val spendingScore: Int,      // 0-100: How controlled spending is
    val incomeScore: Int,        // 0-100: Income stability/adequacy
    val balanceScore: Int        // 0-100: Emergency fund status
) {
    fun average(): Int = (savingsScore + spendingScore + incomeScore + balanceScore) / 4
}

/**
 * Individual insight with actionable advice
 */
data class Insight(
    val title: String,
    val description: String,
    val priority: InsightPriority,
    val priorityColor: Color,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val savingAmount: Double = 0.0,
    val actionItems: List<String> = emptyList()
)

/**
 * Personalized recommendation
 */
data class Recommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val expectedImpact: String? = null
)

/**
 * Detected spending pattern
 */
data class SpendingPattern(
    val type: PatternType,
    val description: String,
    val impact: String? = null
)

/**
 * Insight priority levels
 */
enum class InsightPriority {
    LOW,        // Nice to know
    MEDIUM,     // Should address
    HIGH,       // Important
    URGENT      // Take action now!
}

/**
 * Spending pattern types
 */
enum class PatternType {
    INCREASING,     // Spending going up
    DECREASING,     // Spending going down (good!)
    STABLE,         // Consistent spending
    IRREGULAR       // Unpredictable pattern
}

/**
 * Data quality assessment
 * Determines what insights we can provide
 */
enum class DataQuality {
    INSUFFICIENT,   // < 3 days: Not enough data
    LIMITED,        // 3-7 days: Basic insights only
    GOOD,           // 7-30 days: Most insights available
    EXCELLENT       // 30+ days: Full analysis
}

/**
 * Constants for analysis thresholds
 * Extracted from magic numbers!
 */
object InsightsConstants {
    // Data quality thresholds
    const val MIN_DAYS_FOR_BASIC_INSIGHTS = 3
    const val MIN_DAYS_FOR_FULL_INSIGHTS = 7
    const val MIN_DAYS_FOR_PATTERNS = 14
    const val MIN_DAYS_FOR_EXCELLENT = 30

    // Spending thresholds
    const val HIGH_FOOD_SPENDING_MONTHLY = 400.0
    const val HIGH_TRANSPORT_SPENDING_MONTHLY = 300.0
    const val HIGH_CATEGORY_PERCENTAGE = 30.0

    // Savings thresholds
    const val RECOMMENDED_SAVINGS_RATE = 0.20  // 20%
    const val LOW_SAVINGS_THRESHOLD = 0.10     // 10%

    // Emergency fund thresholds
    const val MINIMUM_EMERGENCY_MONTHS = 1.0
    const val GOOD_EMERGENCY_MONTHS = 3.0
    const val EXCELLENT_EMERGENCY_MONTHS = 6.0

    // Potential savings percentages
    const val FOOD_SAVINGS_POTENTIAL = 0.30      // Can save 30% on food
    const val SHOPPING_SAVINGS_POTENTIAL = 0.25  // Can save 25% on shopping
    const val TRANSPORT_SAVINGS_POTENTIAL = 0.20 // Can save 20% on transport

    // Score calculation parameters
    const val DATA_MULTIPLIER_1_WEEK = 0.7      // 70% confidence
    const val DATA_MULTIPLIER_1_MONTH = 0.85    // 85% confidence
    const val DATA_MULTIPLIER_FULL = 1.0        // 100% confidence
}

/**
 * BENEFITS OF SEPARATING MODELS:
 *
 * 1. Testability: Can test models independently
 * 2. Reusability: Models can be used in multiple screens
 * 3. Type Safety: Compiler catches errors
 * 4. Documentation: Clear structure shows what data exists
 * 5. Maintenance: Easy to add new fields
 * 6. No UI dependency: Models don't know about Compose
 */