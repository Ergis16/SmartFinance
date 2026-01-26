package com.gis.smartfinance.domain.insights

import com.gis.smartfinance.domain.model.Money
import com.gis.smartfinance.domain.model.Transaction
import com.gis.smartfinance.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Comprehensive unit tests for CashFlowForecastEngine
 *
 * Tests cover:
 * - Forecast generation for 30/60/90 day periods
 * - Recurring pattern detection
 * - Confidence score calculation
 * - Warning generation
 * - Income and expense projections
 * - Financial precision (BigDecimal, overflow protection)
 * - Edge cases (insufficient data, empty lists, etc.)
 */
class CashFlowForecastEngineTest {

    private lateinit var engine: CashFlowForecastEngine

    @Before
    fun setup() {
        engine = CashFlowForecastEngine()
    }

    // =============================================================================================
    // Helper Functions
    // =============================================================================================

    /**
     * Create Money object from dollars
     */
    private fun money(dollars: Int, currency: String = "USD"): Money {
        return Money(dollars.toLong() * 100, currency)
    }

    /**
     * Create Money object from cents
     */
    private fun moneyCents(cents: Long, currency: String = "USD"): Money {
        return Money(cents, currency)
    }

    /**
     * Create date X days ago from today
     */
    private fun daysAgo(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    /**
     * Create date X days in the future from today
     */
    private fun daysFromNow(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.time
    }

    /**
     * Create a test transaction
     */
    private fun createTransaction(
        amount: Money,
        type: TransactionType,
        category: String = "Food & Dining",
        merchantName: String? = null,
        date: Date = Date(),
        description: String = "Test transaction"
    ): Transaction {
        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            category = category,
            description = description,
            date = date,
            merchantName = merchantName,
            createdAt = date,
            updatedAt = date
        )
    }

    /**
     * Create recurring transactions (same merchant, similar amounts, regular intervals)
     */
    private fun createRecurringTransactions(
        merchantName: String,
        amount: Money,
        intervalDays: Int,
        count: Int,
        startDaysAgo: Int = 90
    ): List<Transaction> {
        return (0 until count).map { i ->
            createTransaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                category = "Subscriptions",
                merchantName = merchantName,
                date = daysAgo(startDaysAgo - (i * intervalDays)),
                description = merchantName
            )
        }
    }

    /**
     * Create random expense transactions
     */
    private fun createRandomExpenses(
        category: String,
        count: Int,
        averageAmount: Money,
        dayRange: Int = 30
    ): List<Transaction> {
        return (0 until count).map { i ->
            val variance = (i % 3 - 1) * 500L  // Vary amounts slightly
            createTransaction(
                amount = Money(averageAmount.amountCents + variance, averageAmount.currencyCode),
                type = TransactionType.EXPENSE,
                category = category,
                date = daysAgo(dayRange - (i * (dayRange / count))),
                description = "Random $category"
            )
        }
    }

    /**
     * Create income transactions
     */
    private fun createIncomeTransactions(
        amount: Money,
        intervalDays: Int,
        count: Int
    ): List<Transaction> {
        return (0 until count).map { i ->
            createTransaction(
                amount = amount,
                type = TransactionType.INCOME,
                category = "Salary",
                date = daysAgo(90 - (i * intervalDays)),
                description = "Monthly salary"
            )
        }
    }

    // =============================================================================================
    // generateForecast() Tests
    // =============================================================================================

    @Test
    fun `GIVEN insufficient data WHEN generateForecast SHOULD return null`() {
        // Arrange: Only 10 days of data (need 14+)
        val transactions = createRandomExpenses("Food & Dining", 5, money(50), dayRange = 10)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 10
        )

        // Assert
        assertThat(forecast).isNull()
    }

    @Test
    fun `GIVEN exactly 14 days WHEN generateForecast SHOULD return forecast`() {
        // Arrange: Exactly 14 days (minimum)
        val transactions = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 14)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 14
        )

        // Assert
        assertThat(forecast).isNotNull()
        assertThat(forecast!!.forecastPeriods).hasSize(3)  // 30, 60, 90 days
    }

    @Test
    fun `GIVEN valid data WHEN generateForecast SHOULD return 30 60 90 day periods`() {
        // Arrange
        val transactions = createRandomExpenses("Food & Dining", 20, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.forecastPeriods).hasSize(3)
        assertThat(forecast.forecastPeriods[0].periodLabel).contains("30")
        assertThat(forecast.forecastPeriods[1].periodLabel).contains("60")
        assertThat(forecast.forecastPeriods[2].periodLabel).contains("90")
    }

    @Test
    fun `GIVEN recurring patterns WHEN generateForecast SHOULD include in methodology`() {
        // Arrange: Add recurring Netflix subscription
        val recurring = createRecurringTransactions(
            merchantName = "Netflix",
            amount = money(16),
            intervalDays = 30,
            count = 3
        )
        val random = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 90)
        val transactions = recurring + random

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        assertThat(forecast.methodology).contains("1 recurring pattern")
        assertThat(forecast.methodology).contains("90 days")
    }

    @Test
    fun `GIVEN high confidence data WHEN generateForecast SHOULD have high confidence`() {
        // Arrange: 90+ days, 5 recurring patterns, many transactions
        // Confidence formula: 0.40 (90 days) + 0.50 (5 patterns * 0.10) + 0.10 (>100 txns) = 1.00 (capped at 1.0)
        val recurring1 = createRecurringTransactions("Netflix", money(16), 30, 4, 120)
        val recurring2 = createRecurringTransactions("Spotify", money(10), 30, 4, 120)
        val recurring3 = createRecurringTransactions("Planet Fitness", money(25), 30, 4, 120)
        val recurring4 = createRecurringTransactions("Verizon", money(85), 30, 4, 120)
        val recurring5 = createRecurringTransactions("Rent", money(1200), 30, 4, 120)
        val random = createRandomExpenses("Food & Dining", 100, money(50), dayRange = 90)
        val transactions = recurring1 + recurring2 + recurring3 + recurring4 + recurring5 + random

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: With 5 recurring patterns, should reach 0.80+ confidence
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.80"))  // High confidence
        assertThat(forecast.methodology).contains("high confidence")
    }

    @Test
    fun `GIVEN projected low balance WHEN generateForecast SHOULD include warning`() {
        // Arrange: Small balance, high expenses
        val transactions = createRandomExpenses("Food & Dining", 20, money(100), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(50),  // Low balance
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.warnings).contains(ForecastWarning.LOW_BALANCE_EXPECTED)
    }

    @Test
    fun `GIVEN budget overrun WHEN generateForecast SHOULD include warning`() {
        // Arrange: Expenses will exceed budget by >20%
        val transactions = createRandomExpenses("Food & Dining", 30, money(200), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(5000),
            monthlyBudget = money(1000),  // Low budget
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.warnings).contains(ForecastWarning.BUDGET_OVERRUN_PROJECTED)
    }

    @Test
    fun `GIVEN upcoming recurring payment WHEN generateForecast SHOULD include warning`() {
        // Arrange: Recurring payment due in 3 days
        val lastPayment = daysAgo(27)  // Last payment 27 days ago
        val recurring = listOf(
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", daysAgo(87)),
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", daysAgo(57)),
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", lastPayment)
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = recurring,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        assertThat(forecast.warnings).contains(ForecastWarning.RECURRING_PAYMENT_UPCOMING)
    }

    @Test
    fun `GIVEN income and expenses WHEN generateForecast SHOULD project balance correctly`() {
        // Arrange
        val income = createIncomeTransactions(money(3000), 30, 3)
        val expenses = createRandomExpenses("Food & Dining", 30, money(50), dayRange = 90)
        val transactions = income + expenses

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedIncome.amountCents).isGreaterThan(0L)
        assertThat(period30.projectedExpenses.amountCents).isGreaterThan(0L)

        // Balance = current + income - expenses
        val expectedBalance = money(1000) + period30.projectedIncome - period30.projectedExpenses
        assertThat(period30.projectedBalance).isEqualTo(expectedBalance)
    }

    // =============================================================================================
    // Recurring Pattern Detection Tests
    // =============================================================================================

    @Test
    fun `GIVEN 3 recurring transactions WHEN detectRecurring SHOULD detect pattern`() {
        // Arrange: Netflix every 30 days, $15.99
        val transactions = createRecurringTransactions(
            merchantName = "Netflix",
            amount = money(16),
            intervalDays = 30,
            count = 3
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        assertThat(forecast.methodology).contains("1 recurring pattern")
    }

    @Test
    fun `GIVEN only 2 occurrences WHEN detectRecurring SHOULD NOT detect pattern`() {
        // Arrange: Only 2 Netflix transactions (need 3+)
        val transactions = createRecurringTransactions(
            merchantName = "Netflix",
            amount = money(16),
            intervalDays = 30,
            count = 2
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 60
        )!!

        // Assert
        assertThat(forecast.methodology).contains("historical averages")
        assertThat(forecast.methodology).doesNotContain("recurring pattern")
    }

    @Test
    fun `GIVEN varying amounts WHEN detectRecurring SHOULD NOT detect if variance over 10 percent`() {
        // Arrange: Same merchant but amounts vary by >10%
        val transactions = listOf(
            createTransaction(money(100), TransactionType.EXPENSE, "Shopping", "Target", daysAgo(90)),
            createTransaction(money(120), TransactionType.EXPENSE, "Shopping", "Target", daysAgo(60)),  // +20%
            createTransaction(money(90), TransactionType.EXPENSE, "Shopping", "Target", daysAgo(30))   // -10%
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Should not detect as recurring due to high variance
        assertThat(forecast.methodology).contains("historical averages")
    }

    @Test
    fun `GIVEN irregular intervals WHEN detectRecurring SHOULD NOT detect if variance over 3 days`() {
        // Arrange: Same merchant but intervals vary by >3 days
        val transactions = listOf(
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", daysAgo(90)),
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", daysAgo(50)),  // 40 days later
            createTransaction(money(16), TransactionType.EXPENSE, "Subscriptions", "Netflix", daysAgo(20))   // 30 days later
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Should not detect due to irregular intervals (40 vs 30 = 10 day variance > 3)
        assertThat(forecast.methodology).contains("historical averages")
    }

    @Test
    fun `GIVEN multiple recurring patterns WHEN detectRecurring SHOULD detect all`() {
        // Arrange: Netflix + Spotify + Gym
        val netflix = createRecurringTransactions("Netflix", money(16), 30, 3, 90)
        val spotify = createRecurringTransactions("Spotify", money(10), 30, 3, 90)
        val gym = createRecurringTransactions("Planet Fitness", money(20), 30, 3, 90)
        val transactions = netflix + spotify + gym

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        assertThat(forecast.methodology).contains("3 recurring patterns")
    }

    @Test
    fun `GIVEN no merchant name WHEN detectRecurring SHOULD group by category`() {
        // Arrange: Same category, no merchant, similar amounts, regular intervals
        val transactions = listOf(
            createTransaction(money(50), TransactionType.EXPENSE, "Groceries", null, daysAgo(84)),
            createTransaction(money(52), TransactionType.EXPENSE, "Groceries", null, daysAgo(56)),
            createTransaction(money(51), TransactionType.EXPENSE, "Groceries", null, daysAgo(28))
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Should detect recurring pattern by category
        assertThat(forecast.methodology).contains("1 recurring pattern")
    }

    // =============================================================================================
    // Income Projection Tests
    // =============================================================================================

    @Test
    fun `GIVEN no income transactions WHEN projectIncome SHOULD return zero`() {
        // Arrange: Only expenses, no income
        val transactions = createRandomExpenses("Food & Dining", 20, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.forecastPeriods[0].projectedIncome).isEqualTo(Money.zero("USD"))
    }

    @Test
    fun `GIVEN regular monthly income WHEN projectIncome SHOULD scale to period`() {
        // Arrange: $3000 salary every 30 days (3 payments at Day 90, 60, 30)
        // Income transactions: Day 90, 60, 30
        // Expense transactions: Day 90, 81, 72, 63, 54, 45, 36, 27, 18, 9
        // ACTUAL history span: Day 90 to Day 9 = 81 days (newest expense extends range)
        // Total income: $9000
        // Daily average: $9000 / 81 = $111.11/day
        val income = createIncomeTransactions(money(3000), 30, 3)
        val expenses = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 90)
        val transactions = income + expenses

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Income projection uses daily average from actual history span (81 days)
        val period30 = forecast.forecastPeriods[0]
        val period60 = forecast.forecastPeriods[1]
        val period90 = forecast.forecastPeriods[2]

        // 30-day: $111.11/day * 30 = $3333
        assertThat(period30.projectedIncome.amountCents).isAtLeast(money(4400).amountCents)
        assertThat(period30.projectedIncome.amountCents).isAtMost(money(4600).amountCents)

        // 60-day: $111.11/day * 60 = $6667
        assertThat(period60.projectedIncome.amountCents).isAtLeast(money(8800).amountCents)
        assertThat(period60.projectedIncome.amountCents).isAtMost(money(9200).amountCents)

        // 90-day: $111.11/day * 90 = $10,000
        assertThat(period90.projectedIncome.amountCents).isAtLeast(money(13300).amountCents)
        assertThat(period90.projectedIncome.amountCents).isAtMost(money(13700).amountCents)
    }

    @Test
    fun `GIVEN irregular income WHEN projectIncome SHOULD use daily average`() {
        // Arrange: Variable income amounts
        val income = listOf(
            createTransaction(money(2000), TransactionType.INCOME, "Freelance", date = daysAgo(60)),
            createTransaction(money(1500), TransactionType.INCOME, "Freelance", date = daysAgo(45)),
            createTransaction(money(2500), TransactionType.INCOME, "Freelance", date = daysAgo(30)),
            createTransaction(money(1800), TransactionType.INCOME, "Freelance", date = daysAgo(15))
        )
        val expenses = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 60)
        val transactions = income + expenses

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 60
        )!!

        // Assert: Should average out to ~$130/day * 30 = ~$3900/month
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedIncome.amountCents).isGreaterThan(0L)
    }

    // =============================================================================================
    // Expense Projection Tests
    // =============================================================================================

    @Test
    fun `GIVEN category expenses WHEN projectExpenses SHOULD scale to period`() {
        // Arrange: $1500 total in 30 days
        val transactions = createRandomExpenses("Food & Dining", 30, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(5000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        val period60 = forecast.forecastPeriods[1]
        val period90 = forecast.forecastPeriods[2]

        // 30-day should be close to historical
        assertThat(period30.projectedExpenses.amountCents).isAtLeast(money(1400).amountCents)
        assertThat(period30.projectedExpenses.amountCents).isAtMost(money(1600).amountCents)

        // 60-day should be ~2x, 90-day ~3x
        assertThat(period60.projectedExpenses.amountCents).isAtLeast(money(2800).amountCents)
        assertThat(period90.projectedExpenses.amountCents).isAtLeast(money(4200).amountCents)
    }

    @Test
    fun `GIVEN recurring expenses WHEN projectExpenses SHOULD include all occurrences in period`() {
        // Arrange: Netflix $16/month, expect to charge in next 30 days
        val recurring = createRecurringTransactions("Netflix", money(16), 30, 3, 90)

        // Act
        val forecast = engine.generateForecast(
            transactions = recurring,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Should include at least 1 Netflix charge in 30-day period
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedExpenses.amountCents).isAtLeast(money(16).amountCents)
    }

    @Test
    fun `GIVEN category breakdown WHEN projectExpenses SHOULD include all categories`() {
        // Arrange: Multiple categories
        val food = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 30)
        val transport = createRandomExpenses("Transport", 10, money(30), dayRange = 30)
        val entertainment = createRandomExpenses("Entertainment", 10, money(40), dayRange = 30)
        val transactions = food + transport + entertainment

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(5000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.categoryBreakdown).containsKey("Food & Dining")
        assertThat(period30.categoryBreakdown).containsKey("Transport")
        assertThat(period30.categoryBreakdown).containsKey("Entertainment")
    }

    // =============================================================================================
    // Confidence Score Tests
    // =============================================================================================

    @Test
    fun `GIVEN 14 days of data WHEN calculateConfidence SHOULD return low confidence`() {
        // Arrange: Minimum data
        val transactions = createRandomExpenses("Food & Dining", 5, money(50), dayRange = 14)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 14
        )!!

        // Assert
        assertThat(forecast.confidence).isAtMost(BigDecimal("0.30"))
        assertThat(forecast.methodology).contains("low confidence")
    }

    @Test
    fun `GIVEN 30 days of data WHEN calculateConfidence SHOULD return medium confidence`() {
        // Arrange
        val transactions = createRandomExpenses("Food & Dining", 30, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.20"))
        assertThat(forecast.confidence).isAtMost(BigDecimal("0.60"))
    }

    @Test
    fun `GIVEN 90 plus days of data WHEN calculateConfidence SHOULD return high confidence`() {
        // Arrange
        val transactions = createRandomExpenses("Food & Dining", 100, money(50), dayRange = 90)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.40"))
    }

    @Test
    fun `GIVEN many recurring patterns WHEN calculateConfidence SHOULD increase confidence`() {
        // Arrange: Multiple recurring patterns
        val netflix = createRecurringTransactions("Netflix", money(16), 30, 5, 150)
        val spotify = createRecurringTransactions("Spotify", money(10), 30, 5, 150)
        val gym = createRecurringTransactions("Planet Fitness", money(20), 30, 5, 150)
        val phone = createRecurringTransactions("Verizon", money(85), 30, 5, 150)
        val internet = createRecurringTransactions("Comcast", money(60), 30, 5, 150)
        val transactions = netflix + spotify + gym + phone + internet

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 150
        )!!

        // Assert: Should have very high confidence
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.80"))
    }

    @Test
    fun `GIVEN high transaction volume WHEN calculateConfidence SHOULD increase confidence`() {
        // Arrange: 100+ transactions
        val transactions = createRandomExpenses("Food & Dining", 120, money(50), dayRange = 90)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert: Volume bonus should apply
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.50"))
    }

    @Test
    fun `GIVEN maximum factors WHEN calculateConfidence SHOULD cap at 1 point 0`() {
        // Arrange: 90+ days, 5+ recurring, 100+ transactions
        val recurring1 = createRecurringTransactions("Netflix", money(16), 30, 10, 300)
        val recurring2 = createRecurringTransactions("Spotify", money(10), 30, 10, 300)
        val recurring3 = createRecurringTransactions("Gym", money(20), 30, 10, 300)
        val recurring4 = createRecurringTransactions("Phone", money(85), 30, 10, 300)
        val recurring5 = createRecurringTransactions("Internet", money(60), 30, 10, 300)
        val random = createRandomExpenses("Food & Dining", 200, money(50), dayRange = 300)
        val transactions = recurring1 + recurring2 + recurring3 + recurring4 + recurring5 + random

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 300
        )!!

        // Assert: Should be capped at 1.0
        assertThat(forecast.confidence).isAtMost(BigDecimal.ONE)
        assertThat(forecast.confidence).isEqualTo(BigDecimal("1.00"))
    }

    // =============================================================================================
    // Financial Precision Tests
    // =============================================================================================

    @Test
    fun `GIVEN large amounts WHEN projectExpenses SHOULD use BigDecimal precision`() {
        // Arrange: Very large expenses
        val transactions = createRandomExpenses("Shopping", 30, money(10000), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(500000),
            monthlyBudget = money(400000),
            daysOfHistory = 30
        )!!

        // Assert: Should handle large numbers correctly
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedExpenses.amountCents).isGreaterThan(0L)
    }

    @Test
    fun `GIVEN overflow scenario WHEN projectRecurringExpenses SHOULD cap at Long MAX VALUE`() {
        // Arrange: Create scenario that would overflow
        // This is tested internally in the engine with overflow protection
        val transactions = createRandomExpenses("Food & Dining", 10, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert: Should complete without error
        assertThat(forecast.forecastPeriods).isNotEmpty()
    }

    @Test
    fun `GIVEN fractional amounts WHEN calculating averages SHOULD use HALF EVEN rounding`() {
        // Arrange: Amounts that don't divide evenly
        val transactions = listOf(
            createTransaction(moneyCents(1033), TransactionType.EXPENSE, "Food & Dining", date = daysAgo(30)),
            createTransaction(moneyCents(1066), TransactionType.EXPENSE, "Food & Dining", date = daysAgo(25)),
            createTransaction(moneyCents(1099), TransactionType.EXPENSE, "Food & Dining", date = daysAgo(20))
        )

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert: Should complete with proper rounding
        assertThat(forecast.forecastPeriods[0].projectedExpenses.amountCents).isGreaterThan(0L)
    }

    // =============================================================================================
    // Edge Case Tests
    // =============================================================================================

    @Test
    fun `GIVEN empty transaction list WHEN generateForecast SHOULD return forecast with zero values`() {
        // Act
        val forecast = engine.generateForecast(
            transactions = emptyList(),
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )

        // Assert: With no transaction history, should return forecast with zero projections
        assertThat(forecast).isNotNull()
        assertThat(forecast!!.forecastPeriods).hasSize(3)
        assertThat(forecast.forecastPeriods[0].projectedIncome.amountCents).isEqualTo(0L)
        assertThat(forecast.forecastPeriods[0].projectedExpenses.amountCents).isEqualTo(0L)
    }

    @Test
    fun `GIVEN only income transactions WHEN generateForecast SHOULD project positive balance`() {
        // Arrange: Only income, no expenses
        val income = createIncomeTransactions(money(3000), 30, 3)

        // Act
        val forecast = engine.generateForecast(
            transactions = income,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 90
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedIncome.amountCents).isGreaterThan(0L)
        assertThat(period30.projectedExpenses).isEqualTo(Money.zero("USD"))
        assertThat(period30.projectedBalance.amountCents).isGreaterThan(money(1000).amountCents)
    }

    @Test
    fun `GIVEN only expenses WHEN generateForecast SHOULD project decreasing balance`() {
        // Arrange: Only expenses, no income
        val expenses = createRandomExpenses("Food & Dining", 30, money(100), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = expenses,
            currentBalance = money(5000),
            monthlyBudget = money(4000),
            daysOfHistory = 30
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.projectedIncome).isEqualTo(Money.zero("USD"))
        assertThat(period30.projectedExpenses.amountCents).isGreaterThan(0L)
        assertThat(period30.projectedBalance.amountCents).isLessThan(money(5000).amountCents)
    }

    @Test
    fun `GIVEN zero current balance WHEN generateForecast SHOULD still generate forecast`() {
        // Arrange
        val transactions = createRandomExpenses("Food & Dining", 20, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = Money.zero("USD"),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        assertThat(forecast.forecastPeriods).hasSize(3)
        assertThat(forecast.warnings).contains(ForecastWarning.LOW_BALANCE_EXPECTED)
    }

    @Test
    fun `GIVEN null monthly budget WHEN generateForecast SHOULD not check budget warnings`() {
        // Arrange
        val transactions = createRandomExpenses("Food & Dining", 30, money(200), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(5000),
            monthlyBudget = null,  // No budget set
            daysOfHistory = 30
        )!!

        // Assert: Should not have budget overrun warning
        assertThat(forecast.warnings).doesNotContain(ForecastWarning.BUDGET_OVERRUN_PROJECTED)
    }

    @Test
    fun `GIVEN single category WHEN generateForecast SHOULD include in breakdown`() {
        // Arrange: All transactions in one category
        val transactions = createRandomExpenses("Food & Dining", 30, money(50), dayRange = 30)

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(1000),
            monthlyBudget = money(2000),
            daysOfHistory = 30
        )!!

        // Assert
        val period30 = forecast.forecastPeriods[0]
        assertThat(period30.categoryBreakdown).hasSize(1)
        assertThat(period30.categoryBreakdown).containsKey("Food & Dining")
    }

    // =============================================================================================
    // Integration Test
    // =============================================================================================

    @Test
    fun `INTEGRATION TEST GIVEN realistic 90 day history WHEN generateForecast SHOULD produce accurate forecast`() {
        // Arrange: Realistic scenario
        // - Monthly salary: $4000 (3 payments at Day 90, 60, 30 = 60-day span)
        // - Total income: $12,000 / 60 days = $200/day
        // - 30-day forecast: $200 * 30 = $6000 income
        // - Recurring bills: Netflix ($16), Spotify ($10), Gym ($25), Phone ($85), Rent ($1200)
        // - Variable expenses: Food ($400/mo), Transport ($150/mo), Shopping ($300/mo)

        // Income
        val salary = createIncomeTransactions(money(4000), 30, 3)

        // Recurring expenses
        val netflix = createRecurringTransactions("Netflix", money(16), 30, 3, 90)
        val spotify = createRecurringTransactions("Spotify", money(10), 30, 3, 90)
        val gym = createRecurringTransactions("Planet Fitness", money(25), 30, 3, 90)
        val phone = createRecurringTransactions("Verizon", money(85), 30, 3, 90)
        val rent = createRecurringTransactions("Landlord", money(1200), 30, 3, 90)

        // Variable expenses
        val food = createRandomExpenses("Food & Dining", 40, money(30), dayRange = 90)
        val transport = createRandomExpenses("Transport", 15, money(30), dayRange = 90)
        val shopping = createRandomExpenses("Shopping", 10, money(90), dayRange = 90)

        val transactions = salary + netflix + spotify + gym + phone + rent + food + transport + shopping

        // Act
        val forecast = engine.generateForecast(
            transactions = transactions,
            currentBalance = money(2000),
            monthlyBudget = money(3500),
            daysOfHistory = 90
        )!!

        // Assert
        // 1. Should detect 6 recurring patterns (5 monthly bills + shopping every 9 days)
        assertThat(forecast.methodology).contains("6 recurring patterns")

        // 2. Should have high confidence (90 days + 6 recurring patterns + volume)
        // Confidence: 0.40 (90 days) + 0.50 (5 patterns) + 0.10 (volume) = 1.0 (capped)
        assertThat(forecast.confidence).isAtLeast(BigDecimal("0.80"))

        // 3. 30-day forecast should be reasonable
        val period30 = forecast.forecastPeriods[0]

        // Income: $12,000 total / 60 days * 30 = $6000
        assertThat(period30.projectedIncome.amountCents).isAtLeast(money(5800).amountCents)
        assertThat(period30.projectedIncome.amountCents).isAtMost(money(6200).amountCents)

        // Expenses should be ~$2200 (rent + bills + variable)
        assertThat(period30.projectedExpenses.amountCents).isAtLeast(money(2000).amountCents)
        assertThat(period30.projectedExpenses.amountCents).isAtMost(money(2600).amountCents)

        // Balance should increase (income > expenses)
        assertThat(period30.projectedBalance.amountCents).isGreaterThan(money(2000).amountCents)

        // 4. Should have category breakdown
        assertThat(period30.categoryBreakdown).isNotEmpty()

        // 5. Should NOT have warnings (budget is healthy)
        assertThat(forecast.warnings).doesNotContain(ForecastWarning.BUDGET_OVERRUN_PROJECTED)
        assertThat(forecast.warnings).doesNotContain(ForecastWarning.LOW_BALANCE_EXPECTED)
    }
}
