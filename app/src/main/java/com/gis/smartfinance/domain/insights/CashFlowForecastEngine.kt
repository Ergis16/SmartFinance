package com.gis.smartfinance.domain.insights

import com.gis.smartfinance.domain.model.Money
import com.gis.smartfinance.domain.model.Transaction
import com.gis.smartfinance.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Cash Flow Forecast Engine
 *
 * Phase 2: Predicts future income, expenses, and balance based on historical transaction patterns.
 *
 * **Forecasting Methodology:**
 * 1. Detect recurring transactions (same merchant, similar amounts, regular intervals)
 * 2. Calculate category spending trends (30-day moving average)
 * 3. Project irregular expenses based on historical averages
 * 4. Forecast income based on patterns
 * 5. Generate warnings for potential issues
 *
 * **FINANCIAL PRECISION:**
 * - All monetary calculations use BigDecimal with HALF_EVEN rounding
 * - Money value object for all amounts
 * - Confidence scores use BigDecimal for precision
 *
 * **Data Requirements:**
 * - Minimum 14 days of data for basic forecast
 * - 30+ days recommended for higher confidence
 *
 * @see CashFlowForecast
 * @see RecurringPattern
 */
class CashFlowForecastEngine {

    companion object {
        // Forecast configuration
        private const val MIN_DAYS_FOR_FORECAST = 14
        private const val RECURRING_MIN_OCCURRENCES = 3  // FIXED: Need at least 3 occurrences for reliable variance calculation
        private const val RECURRING_MIN_INTERVAL_DAYS = 7  // Minimum 7 days between occurrences (weekly+)
        private const val RECURRING_AMOUNT_TOLERANCE_PERCENT = 10  // ±10% amount variance allowed
        private const val RECURRING_INTERVAL_TOLERANCE_DAYS = 3  // ±3 days interval variance allowed

        // Confidence thresholds
        private val HIGH_CONFIDENCE_THRESHOLD = BigDecimal("0.80")  // 80%+
        private val MEDIUM_CONFIDENCE_THRESHOLD = BigDecimal("0.60")  // 60-79%

        // Warning thresholds
        private const val LOW_BALANCE_THRESHOLD_CENTS = 10000L  // $100
        private val BUDGET_OVERRUN_THRESHOLD = BigDecimal("1.20")  // 120% of budget
    }

    /**
     * Generate cash flow forecast for multiple periods
     *
     * @param transactions Historical transactions (sorted newest first)
     * @param currentBalance Current account balance
     * @param monthlyBudget User's monthly budget (optional, for warnings)
     * @param daysOfHistory How many days of transaction history available
     * @return CashFlowForecast with 30/60/90 day predictions, or null if insufficient data
     */
    fun generateForecast(
        transactions: List<Transaction>,
        currentBalance: Money,
        monthlyBudget: Money?,
        daysOfHistory: Int
    ): CashFlowForecast? {
        // Check if we have enough data
        if (daysOfHistory < MIN_DAYS_FOR_FORECAST) {
            return null  // Not enough data for reliable forecast
        }

        val currencyCode = currentBalance.currencyCode

        // Detect recurring patterns
        val recurringPatterns = detectRecurringTransactions(transactions)

        // Calculate category averages for irregular expenses
        val categoryAverages = calculateCategoryAverages(transactions, currencyCode, recurringPatterns)

        // Generate forecast periods (30, 60, 90 days)
        val periods = listOf(30, 60, 90).map { days ->
            generateForecastPeriod(
                periodDays = days,
                periodLabel = "Next $days Days",
                transactions = transactions,
                currentBalance = currentBalance,
                recurringPatterns = recurringPatterns,
                categoryAverages = categoryAverages,
                currencyCode = currencyCode
            )
        }

        // Calculate forecast confidence
        val confidence = calculateForecastConfidence(
            daysOfHistory = daysOfHistory,
            recurringPatternsCount = recurringPatterns.size,
            transactionCount = transactions.size
        )

        // Generate warnings
        val warnings = generateWarnings(
            periods = periods,
            monthlyBudget = monthlyBudget,
            recurringPatterns = recurringPatterns
        )

        // Determine methodology description
        val methodology = buildMethodologyString(
            recurringCount = recurringPatterns.size,
            daysOfHistory = daysOfHistory,
            confidence = confidence
        )

        return CashFlowForecast(
            forecastPeriods = periods,
            confidence = confidence,
            methodology = methodology,
            warnings = warnings
        )
    }

    /**
     * Detect recurring transaction patterns
     *
     * Groups transactions by merchant/category and identifies regular intervals.
     *
     * @param transactions Historical transactions
     * @return List of detected recurring patterns
     */
    private fun detectRecurringTransactions(
        transactions: List<Transaction>
    ): List<RecurringPattern> {
        val patterns = mutableListOf<RecurringPattern>()

        // FIXED: Only detect recurring EXPENSE patterns (not income)
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        // Group transactions by merchant (if available) or category
        val groupedByMerchant = expenseTransactions
            .filter { it.merchantName != null }
            .groupBy { it.merchantName!! }

        val groupedByCategory = expenseTransactions
            .filter { it.merchantName == null }
            .groupBy { it.category }

        // Check merchant groups for recurring patterns
        groupedByMerchant.forEach { (merchant, txns) ->
            detectRecurringPattern(txns, merchant, null)?.let { patterns.add(it) }
        }

        // Check category groups for recurring patterns
        groupedByCategory.forEach { (category, txns) ->
            detectRecurringPattern(txns, null, category)?.let { patterns.add(it) }
        }

        return patterns
    }

    /**
     * Detect recurring pattern within a group of transactions
     *
     * Checks for:
     * - Similar amounts (within 10% variance)
     * - Regular intervals (within 3 days variance)
     * - Minimum 2 occurrences
     *
     * @param transactions Group of similar transactions
     * @param merchantName Merchant name (if grouped by merchant)
     * @param category Category (if grouped by category)
     * @return RecurringPattern if detected, null otherwise
     */
    private fun detectRecurringPattern(
        transactions: List<Transaction>,
        merchantName: String?,
        category: String?
    ): RecurringPattern? {
        if (transactions.size < RECURRING_MIN_OCCURRENCES) {
            return null  // Need at least 3 occurrences for reliable variance calculation
        }

        // Sort by date (oldest first) for interval calculation
        val sortedTxns = transactions.sortedBy { it.date }

        // Calculate average amount using BigDecimal for precision
        val averageAmount = if (sortedTxns.isEmpty()) {
            Money.zero(sortedTxns.first().amount.currencyCode)
        } else {
            val totalCents = sortedTxns.sumOf { it.amount.amountCents }
            val count = BigDecimal(sortedTxns.size.toString())
            val avgCents = BigDecimal(totalCents.toString())
                .divide(count, 0, RoundingMode.HALF_EVEN)
                .toLong()
            Money(avgCents, sortedTxns.first().amount.currencyCode)
        }

        // Check if amounts are similar (within tolerance) using BigDecimal
        val amountVariance = sortedTxns.map { txn ->
            val diff = BigDecimal((txn.amount.amountCents - averageAmount.amountCents).toString())
            val avgCents = BigDecimal(averageAmount.amountCents.toString())
            if (avgCents.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal.ZERO
            } else {
                diff.divide(avgCents, 4, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal("100"))
            }
        }

        val tolerancePercent = BigDecimal(RECURRING_AMOUNT_TOLERANCE_PERCENT.toString())
        if (amountVariance.any { it.abs() > tolerancePercent }) {
            return null  // Amounts vary too much
        }

        // Calculate intervals between transactions
        val intervals = mutableListOf<Long>()
        for (i in 1 until sortedTxns.size) {
            val daysBetween = TimeUnit.MILLISECONDS.toDays(
                sortedTxns[i].date.time - sortedTxns[i - 1].date.time
            )
            intervals.add(daysBetween)
        }

        // Calculate average interval using BigDecimal for precision
        val averageInterval = if (intervals.isEmpty()) {
            BigDecimal.ZERO
        } else {
            val totalDays = intervals.sum()
            val count = BigDecimal(intervals.size.toString())
            BigDecimal(totalDays.toString())
                .divide(count, 2, RoundingMode.HALF_EVEN)
        }
        // FIXED: Reject patterns with intervals < 7 days (daily/frequent expenses use category averages)
        if (averageInterval.compareTo(BigDecimal(RECURRING_MIN_INTERVAL_DAYS.toString())) < 0) {
            return null  // Too frequent - should be handled by category averages
        }

        // Check if intervals are regular (within tolerance) using BigDecimal
        val intervalVariance = intervals.map { interval ->
            BigDecimal(interval.toString()).subtract(averageInterval).abs()
        }

        val toleranceDays = BigDecimal(RECURRING_INTERVAL_TOLERANCE_DAYS.toString())
        if (intervalVariance.any { it > toleranceDays }) {
            return null  // Intervals too irregular
        }

        // Calculate average variances using BigDecimal
        val avgAmountVariance = if (amountVariance.isEmpty()) {
            BigDecimal.ZERO
        } else {
            amountVariance.map { it.abs() }
                .reduce { acc, bd -> acc.add(bd) }
                .divide(BigDecimal(amountVariance.size.toString()), 4, RoundingMode.HALF_EVEN)
        }

        val avgIntervalVariance = if (intervalVariance.isEmpty()) {
            BigDecimal.ZERO
        } else {
            intervalVariance.reduce { acc, bd -> acc.add(bd) }
                .divide(BigDecimal(intervalVariance.size.toString()), 2, RoundingMode.HALF_EVEN)
        }

        // Calculate confidence (higher if more occurrences and lower variance)
        val confidence = calculatePatternConfidence(
            occurrences = sortedTxns.size,
            amountVariance = avgAmountVariance,
            intervalVariance = avgIntervalVariance
        )

        // Predict next occurrence
        val lastDate = sortedTxns.last().date
        val nextExpectedDate = Date(lastDate.time + TimeUnit.DAYS.toMillis(averageInterval.toLong()))

        return RecurringPattern(
            transactionIds = sortedTxns.map { it.id },
            merchantName = merchantName,
            category = category ?: sortedTxns.first().category,
            averageAmount = averageAmount,
            intervalDays = averageInterval,
            confidence = confidence,
            nextExpectedDate = nextExpectedDate
        )
    }

    /**
     * Calculate category spending averages
     *
     * Uses 30-day moving average for each category.
     *
     * @param transactions Historical transactions
     * @param currencyCode Currency for Money objects
     * @return Map of category to average monthly spending
     */
    private fun calculateCategoryAverages(
        transactions: List<Transaction>,
        currencyCode: String,
        recurringPatterns: List<RecurringPattern>
    ): Map<String, Money> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        if (expenses.isEmpty()) {
            return emptyMap()
        }

        // FIXED: Exclude transactions that are part of recurring patterns
        val recurringTxnIds = recurringPatterns.flatMap { it.transactionIds }.toSet()
        val irregularExpenses = expenses.filterNot { it.id in recurringTxnIds }

        if (irregularExpenses.isEmpty()) {
            return emptyMap()
        }
        // Calculate actual days of historical data
        val oldestDate = irregularExpenses.minByOrNull { it.date }?.date ?: Date()
        val newestDate = irregularExpenses.maxByOrNull { it.date }?.date ?: Date()
        val actualHistoryDays = TimeUnit.MILLISECONDS.toDays(newestDate.time - oldestDate.time).toInt()
            .coerceAtLeast(1)  // Ensure at least 1 day to avoid division by zero

        val groupedByCategory = irregularExpenses.groupBy { it.category }

        // Normalize each category total to a 30-day (monthly) average
        return groupedByCategory.mapValues { (_, txns) ->
            val totalCents = txns.sumOf { it.amount.amountCents }

            // Calculate daily average and scale to 30 days (monthly)
            val dailyAverage = BigDecimal(totalCents.toString())
                .divide(BigDecimal(actualHistoryDays.toString()), 10, RoundingMode.HALF_EVEN)

            val monthlyCents = dailyAverage
                .multiply(BigDecimal("30"))
                .setScale(0, RoundingMode.HALF_EVEN)
                .toLong()

            Money(monthlyCents, currencyCode)
        }
    }

    /**
     * Generate forecast for a specific period
     *
     * Combines recurring patterns, category averages, and income projections.
     *
     * @param periodDays Number of days to forecast
     * @param periodLabel Display label for this period
     * @param transactions Historical transactions
     * @param currentBalance Current account balance
     * @param recurringPatterns Detected recurring patterns
     * @param categoryAverages Average spending by category
     * @param currencyCode Currency code
     * @return ForecastPeriod with predictions
     */
    private fun generateForecastPeriod(
        periodDays: Int,
        periodLabel: String,
        transactions: List<Transaction>,
        currentBalance: Money,
        recurringPatterns: List<RecurringPattern>,
        categoryAverages: Map<String, Money>,
        currencyCode: String
    ): ForecastPeriod {
        val startDate = Date()
        val endDate = Date(startDate.time + TimeUnit.DAYS.toMillis(periodDays.toLong()))

        // Project recurring expenses in this period
        val recurringExpenses = projectRecurringExpenses(
            recurringPatterns = recurringPatterns,
            startDate = startDate,
            endDate = endDate,
            currencyCode = currencyCode
        )

        // Project irregular expenses based on category averages
        val irregularExpenses = projectIrregularExpenses(
            categoryAverages = categoryAverages,
            periodDays = periodDays,
            currencyCode = currencyCode
        )

        val totalExpenses = recurringExpenses + irregularExpenses

        // Project income (simple average of past 30 days)
        val projectedIncome = projectIncome(
            transactions = transactions,
            periodDays = periodDays,
            currencyCode = currencyCode
        )

        // Calculate projected balance
        val projectedBalance = currentBalance + projectedIncome - totalExpenses

        // Breakdown by category
        val categoryBreakdown = buildCategoryBreakdown(
            categoryAverages = categoryAverages,
            periodDays = periodDays
        )

        return ForecastPeriod(
            periodLabel = periodLabel,
            startDate = startDate,
            endDate = endDate,
            projectedIncome = projectedIncome,
            projectedExpenses = totalExpenses,
            projectedBalance = projectedBalance,
            categoryBreakdown = categoryBreakdown
        )
    }

    /**
     * Project recurring expenses within a date range
     */
    private fun projectRecurringExpenses(
        recurringPatterns: List<RecurringPattern>,
        startDate: Date,
        endDate: Date,
        currencyCode: String
    ): Money {
        var totalCents = 0L

        recurringPatterns.forEach { pattern ->
            var currentDate = pattern.nextExpectedDate
            while (currentDate.time <= endDate.time) {
                if (currentDate.time >= startDate.time) {
                    // FIXED: Check for overflow before addition
                    val amountToAdd = pattern.averageAmount.amountCents
                    if (totalCents > Long.MAX_VALUE - amountToAdd) {
                        // Would overflow - cap at Long.MAX_VALUE
                        // (This represents an extremely large amount, likely unrealistic)
                        totalCents = Long.MAX_VALUE
                        break  // Stop processing this pattern
                    }
                    totalCents += amountToAdd
                }
                // Add interval to get next occurrence
                currentDate = Date(currentDate.time + TimeUnit.DAYS.toMillis(pattern.intervalDays.toLong()))
            }
        }

        return Money(totalCents, currencyCode)
    }

    /**
     * Project irregular expenses based on historical averages
     */
    private fun projectIrregularExpenses(
        categoryAverages: Map<String, Money>,
        periodDays: Int,
        currencyCode: String
    ): Money {
        // Scale monthly averages to the forecast period
        val scaleFactor = BigDecimal(periodDays.toString())
            .divide(BigDecimal("30"), 10, RoundingMode.HALF_EVEN)

        val totalCents = categoryAverages.values.sumOf { amount ->
            BigDecimal(amount.amountCents.toString())
                .multiply(scaleFactor)
                .setScale(0, RoundingMode.HALF_EVEN)
                .toLong()
        }

        return Money(totalCents, currencyCode)
    }

    /**
     * Project income for the forecast period
     */
    private fun projectIncome(
        transactions: List<Transaction>,
        periodDays: Int,
        currencyCode: String
    ): Money {
        val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }

        if (incomeTransactions.isEmpty()) {
            return Money.zero(currencyCode)
        }

        // Calculate actual days of historical data (using income transactions only)
        val oldestDate = incomeTransactions.minByOrNull { it.date }?.date ?: Date()
        val newestDate = incomeTransactions.maxByOrNull { it.date }?.date ?: Date()
        val actualHistoryDays = TimeUnit.MILLISECONDS.toDays(newestDate.time - oldestDate.time).toInt()
            .coerceAtLeast(1)  // Ensure at least 1 day to avoid division by zero

        // Calculate daily average income from actual history
        val totalIncomeCents = incomeTransactions.sumOf { it.amount.amountCents }
        val dailyAverageIncome = BigDecimal(totalIncomeCents.toString())
            .divide(BigDecimal(actualHistoryDays.toString()), 10, RoundingMode.HALF_EVEN)

        // Scale to forecast period
        val scaleFactor = BigDecimal(periodDays.toString())
        val projectedCents = dailyAverageIncome
            .multiply(scaleFactor)
            .setScale(0, RoundingMode.HALF_EVEN)
            .toLong()

        return Money(projectedCents, currencyCode)
    }

    /**
     * Build category breakdown for forecast period
     */
    private fun buildCategoryBreakdown(
        categoryAverages: Map<String, Money>,
        periodDays: Int
    ): Map<String, Money> {
        val scaleFactor = BigDecimal(periodDays.toString())
            .divide(BigDecimal("30"), 10, RoundingMode.HALF_EVEN)

        return categoryAverages.mapValues { (_, amount) ->
            val scaledCents = BigDecimal(amount.amountCents.toString())
                .multiply(scaleFactor)
                .setScale(0, RoundingMode.HALF_EVEN)
                .toLong()
            Money(scaledCents, amount.currencyCode)
        }
    }

    /**
     * Calculate confidence score for forecast
     *
     * Higher confidence when:
     * - More days of historical data
     * - More recurring patterns detected
     * - More transactions overall
     *
     * @return Confidence score (0.0 to 1.0)
     */
    private fun calculateForecastConfidence(
        daysOfHistory: Int,
        recurringPatternsCount: Int,
        transactionCount: Int
    ): BigDecimal {
        // Base confidence from data history
        val historyScore = when {
            daysOfHistory >= 90 -> BigDecimal("0.40")  // 40% for 90+ days
            daysOfHistory >= 60 -> BigDecimal("0.30")  // 30% for 60+ days
            daysOfHistory >= 30 -> BigDecimal("0.20")  // 20% for 30+ days
            else -> BigDecimal("0.10")  // 10% for 14-29 days
        }

        // Bonus for recurring patterns
        val recurringBonus = BigDecimal(recurringPatternsCount.coerceAtMost(5).toString())
            .multiply(BigDecimal("0.10"))  // +10% per pattern (max 50%)

        // Bonus for transaction volume
        val volumeBonus = when {
            transactionCount >= 100 -> BigDecimal("0.10")
            transactionCount >= 50 -> BigDecimal("0.05")
            else -> BigDecimal.ZERO
        }

        val totalConfidence = historyScore
            .add(recurringBonus)
            .add(volumeBonus)
            .setScale(2, RoundingMode.HALF_EVEN)

        // Cap at 1.0 (100%) - maintain scale consistency
        return if (totalConfidence > BigDecimal.ONE) {
            BigDecimal.ONE.setScale(2, RoundingMode.HALF_EVEN)
        } else {
            totalConfidence
        }
    }

    /**
     * Calculate confidence for a single recurring pattern
     */
    private fun calculatePatternConfidence(
        occurrences: Int,
        amountVariance: BigDecimal,
        intervalVariance: BigDecimal
    ): BigDecimal {
        // More occurrences = higher confidence
        val occurrenceScore = BigDecimal((occurrences.coerceAtMost(10) * 5).toString())
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)

        // Lower variance = higher confidence (amount variance is percentage)
        val cappedAmountVariance = amountVariance.min(BigDecimal("10"))
        val amountScore = BigDecimal("100")
            .subtract(cappedAmountVariance)
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)

        // Lower interval variance = higher confidence (interval variance is days)
        val cappedIntervalVariance = intervalVariance.min(BigDecimal("3"))
        val intervalScore = BigDecimal("100")
            .subtract(cappedIntervalVariance.multiply(BigDecimal("33.33")))
            .divide(BigDecimal("100"), 2, RoundingMode.HALF_EVEN)

        // Weighted average
        val confidence = occurrenceScore.multiply(BigDecimal("0.40"))
            .add(amountScore.multiply(BigDecimal("0.30")))
            .add(intervalScore.multiply(BigDecimal("0.30")))
            .setScale(2, RoundingMode.HALF_EVEN)

        return confidence.min(BigDecimal.ONE)
    }

    /**
     * Generate warnings based on forecast
     */
    private fun generateWarnings(
        periods: List<ForecastPeriod>,
        monthlyBudget: Money?,
        recurringPatterns: List<RecurringPattern>
    ): List<ForecastWarning> {
        val warnings = mutableListOf<ForecastWarning>()

        // Check for low balance
        val firstPeriod = periods.firstOrNull()
        if (firstPeriod != null && firstPeriod.projectedBalance.amountCents < LOW_BALANCE_THRESHOLD_CENTS) {
            warnings.add(ForecastWarning.LOW_BALANCE_EXPECTED)
        }

        // Check for budget overrun
        if (monthlyBudget != null && firstPeriod != null) {
            val monthlyExpenses = firstPeriod.projectedExpenses
            val overrunRatio = BigDecimal(monthlyExpenses.amountCents.toString())
                .divide(BigDecimal(monthlyBudget.amountCents.toString()), 10, RoundingMode.HALF_EVEN)

            if (overrunRatio > BUDGET_OVERRUN_THRESHOLD) {
                warnings.add(ForecastWarning.BUDGET_OVERRUN_PROJECTED)
            }
        }

        // Check for upcoming recurring payments (next 7 days)
        val sevenDaysFromNow = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))
        recurringPatterns.forEach { pattern ->
            if (pattern.nextExpectedDate.time <= sevenDaysFromNow.time) {
                warnings.add(ForecastWarning.RECURRING_PAYMENT_UPCOMING)
            }
        }

        return warnings.distinct()
    }

    /**
     * Build methodology description string
     */
    private fun buildMethodologyString(
        recurringCount: Int,
        daysOfHistory: Int,
        confidence: BigDecimal
    ): String {
        val confidenceLevel = when {
            confidence >= HIGH_CONFIDENCE_THRESHOLD -> "high"
            confidence >= MEDIUM_CONFIDENCE_THRESHOLD -> "medium"
            else -> "low"
        }

        val recurringPart = if (recurringCount > 0) {
            "$recurringCount recurring pattern${if (recurringCount > 1) "s" else ""}"
        } else {
            "historical averages"
        }

        return "Based on $recurringPart from $daysOfHistory days of data ($confidenceLevel confidence)"
    }
}
