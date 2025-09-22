package com.gis.smartfinance.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gis.smartfinance.data.PersistentTransactionManager
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit
) {
    // Get the PersistentTransactionManager instance
    val context = LocalContext.current
    val transactionManager = remember { PersistentTransactionManager.getInstance(context) }

    // Collect states from the manager
    val transactions by transactionManager.transactions.collectAsState()
    val totalExpense by transactionManager.totalExpense.collectAsState()
    val totalIncome by transactionManager.totalIncome.collectAsState()
    val balance by transactionManager.balance.collectAsState()

    // Generate comprehensive insights
    val insightsAnalysis = remember(transactions) {
        if (transactions.isEmpty()) {
            InsightsAnalysis.empty()
        } else {
            analyzeTransactions(transactions, totalIncome, totalExpense, balance)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("AI Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A2E)
                )
            )
        }
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            EmptyInsightsState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Financial Health Score Card
                item {
                    FinancialHealthCard(
                        healthScore = insightsAnalysis.healthScore,
                        scoreBreakdown = insightsAnalysis.scoreBreakdown
                    )
                }

                // Insights Summary Card
                item {
                    InsightsSummaryCard(
                        totalSavingsPotential = insightsAnalysis.savingsPotential,
                        insightsCount = insightsAnalysis.insights.size,
                        urgentInsights = insightsAnalysis.insights.count { it.priority == InsightPriority.URGENT }
                    )
                }

                // Spending Patterns Card
                if (insightsAnalysis.spendingPatterns.isNotEmpty()) {
                    item {
                        SpendingPatternsCard(patterns = insightsAnalysis.spendingPatterns)
                    }
                }

                // Individual Insight Cards
                items(insightsAnalysis.insights) { insight ->
                    InsightCard(insight = insight)
                }

                // Personalized Recommendations
                if (insightsAnalysis.recommendations.isNotEmpty()) {
                    item {
                        Text(
                            "Personalized Recommendations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(insightsAnalysis.recommendations) { recommendation ->
                        RecommendationCard(recommendation = recommendation)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialHealthCard(
    healthScore: Int,
    scoreBreakdown: ScoreBreakdown
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                healthScore >= 80 -> Color(0xFF43A047)
                healthScore >= 60 -> Color(0xFFFFA726)
                else -> Color(0xFFE53935)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Financial Health Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$healthScore/100",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        when {
                            healthScore >= 80 -> "Excellent"
                            healthScore >= 60 -> "Good"
                            healthScore >= 40 -> "Fair"
                            else -> "Needs Improvement"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Score breakdown
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    ScoreItem("Savings", scoreBreakdown.savingsScore)
                    ScoreItem("Spending", scoreBreakdown.spendingScore)
                    ScoreItem("Income", scoreBreakdown.incomeScore)
                    ScoreItem("Balance", scoreBreakdown.balanceScore)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = healthScore / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ScoreItem(label: String, score: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$score%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SpendingPatternsCard(patterns: List<SpendingPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Spending Patterns Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(12.dp))

            patterns.forEach { pattern ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        when (pattern.type) {
                            PatternType.INCREASING -> Icons.Default.TrendingUp
                            PatternType.DECREASING -> Icons.Default.TrendingDown
                            PatternType.STABLE -> Icons.Default.TrendingFlat
                            PatternType.IRREGULAR -> Icons.Default.ShowChart
                        },
                        contentDescription = null,
                        tint = when (pattern.type) {
                            PatternType.INCREASING -> Color(0xFFE53935)
                            PatternType.DECREASING -> Color(0xFF43A047)
                            PatternType.STABLE -> Color(0xFF1976D2)
                            PatternType.IRREGULAR -> Color(0xFFFFA726)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            pattern.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1A2E)
                        )
                        if (pattern.impact != null) {
                            Text(
                                pattern.impact,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsSummaryCard(
    totalSavingsPotential: Double,
    insightsCount: Int,
    urgentInsights: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6C63FF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Savings Potential",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "€${String.format("%.2f", totalSavingsPotential)}/month",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (urgentInsights > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "$urgentInsights Urgent",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$insightsCount insights found",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(insight.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    insight.icon,
                    contentDescription = null,
                    tint = insight.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.weight(1f)
                    )

                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = insight.priorityColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            insight.priority.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = insight.priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )

                if (insight.savingAmount != null && insight.savingAmount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Save €${String.format("%.2f", insight.savingAmount)}/month",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF43A047),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (insight.actionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        insight.actionItems.forEach { action ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", modifier = Modifier.padding(end = 4.dp))
                                Text(
                                    action,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1A1A2E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = recommendation.backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                recommendation.icon,
                contentDescription = null,
                tint = recommendation.iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
                if (recommendation.expectedImpact != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        recommendation.expectedImpact,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = recommendation.iconColor
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyInsightsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFBDBDBD)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Data to Analyze",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add some transactions to get personalized financial insights and recommendations",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============== DATA CLASSES ==============

data class Insight(
    val title: String,
    val description: String,
    val priority: InsightPriority,
    val priorityColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val savingAmount: Double? = null,
    val actionItems: List<String> = emptyList()
)

data class Recommendation(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val expectedImpact: String? = null
)

data class SpendingPattern(
    val type: PatternType,
    val description: String,
    val impact: String? = null
)

data class InsightsAnalysis(
    val healthScore: Int,
    val scoreBreakdown: ScoreBreakdown,
    val savingsPotential: Double,
    val insights: List<Insight>,
    val recommendations: List<Recommendation>,
    val spendingPatterns: List<SpendingPattern>
) {
    companion object {
        fun empty() = InsightsAnalysis(
            healthScore = 0,
            scoreBreakdown = ScoreBreakdown(0, 0, 0, 0),
            savingsPotential = 0.0,
            insights = emptyList(),
            recommendations = emptyList(),
            spendingPatterns = emptyList()
        )
    }
}

data class ScoreBreakdown(
    val savingsScore: Int,
    val spendingScore: Int,
    val incomeScore: Int,
    val balanceScore: Int
)

enum class InsightPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class PatternType {
    INCREASING, DECREASING, STABLE, IRREGULAR
}

// ============== ANALYSIS ENGINE ==============

fun analyzeTransactions(
    transactions: List<FinancialTransaction>,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
): InsightsAnalysis {
    val insights = mutableListOf<Insight>()
    val recommendations = mutableListOf<Recommendation>()
    val patterns = mutableListOf<SpendingPattern>()

    // Calculate time-based metrics
    val now = Date()
    val thirtyDaysAgo = Date(now.time - 30L * 24 * 60 * 60 * 1000)
    val sevenDaysAgo = Date(now.time - 7L * 24 * 60 * 60 * 1000)

    val recentTransactions = transactions.filter { it.date.after(thirtyDaysAgo) }
    val lastWeekTransactions = transactions.filter { it.date.after(sevenDaysAgo) }

    // Category analysis
    val expensesByCategory = recentTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }
        .toList()
        .sortedByDescending { it.second }

    val incomeByCategory = recentTransactions
        .filter { it.type == TransactionType.INCOME }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }

    // Time-based spending analysis
    val weeklyExpenses = calculateWeeklyExpenses(recentTransactions)
    val dailyAverageExpense = if (recentTransactions.isNotEmpty()) {
        totalExpense / 30.0
    } else 0.0

    // === CALCULATE FINANCIAL HEALTH SCORE ===
    val scoreBreakdown = calculateHealthScore(
        totalIncome, totalExpense, balance,
        expensesByCategory, weeklyExpenses
    )
    val healthScore = (scoreBreakdown.savingsScore + scoreBreakdown.spendingScore +
            scoreBreakdown.incomeScore + scoreBreakdown.balanceScore) / 4

    // === GENERATE INSIGHTS ===

    // 1. Savings Rate Analysis
    if (totalIncome > 0) {
        val savingsRate = if (balance > 0) balance / totalIncome else 0.0
        val idealSavingsRate = 0.20 // 20% is ideal

        when {
            savingsRate < 0 -> {
                insights.add(Insight(
                    title = "Negative Savings Alert",
                    description = "You're spending €${String.format("%.2f", abs(balance))} more than you earn. This is unsustainable and requires immediate action.",
                    priority = InsightPriority.URGENT,
                    priorityColor = Color(0xFFD32F2F),
                    icon = Icons.Default.Error,
                    iconColor = Color(0xFFD32F2F),
                    iconBackground = Color(0xFFFFEBEE),
                    savingAmount = abs(balance) + (totalIncome * 0.1),
                    actionItems = listOf(
                        "Review all subscriptions and cancel unused ones",
                        "Set a strict daily spending limit",
                        "Consider additional income sources"
                    )
                ))
            }
            savingsRate < 0.05 -> {
                insights.add(Insight(
                    title = "Low Savings Rate",
                    description = "You're only saving ${(savingsRate * 100).roundToInt()}% of your income. Financial experts recommend saving at least 20%.",
                    priority = InsightPriority.HIGH,
                    priorityColor = Color(0xFFE53935),
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFE53935),
                    iconBackground = Color(0xFFFFEBEE),
                    savingAmount = (idealSavingsRate - savingsRate) * totalIncome,
                    actionItems = listOf(
                        "Set up automatic savings transfer",
                        "Use the 50/30/20 budget rule"
                    )
                ))
            }
            savingsRate >= 0.20 -> {
                insights.add(Insight(
                    title = "Excellent Savings Rate!",
                    description = "You're saving ${(savingsRate * 100).roundToInt()}% of your income. Keep up the great work!",
                    priority = InsightPriority.LOW,
                    priorityColor = Color(0xFF43A047),
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF43A047),
                    iconBackground = Color(0xFFE8F5E9)
                ))
            }
        }
    }

    // 2. Category-Specific Insights
    expensesByCategory.forEach { (category, amount) ->
        val percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0
        val monthlyAmount = amount

        when (category) {
            "Food & Dining" -> {
                when {
                    monthlyAmount > 500 -> {
                        val avgMealCost = monthlyAmount / 30
                        insights.add(Insight(
                            title = "High Dining Expenses",
                            description = "You're spending €${String.format("%.2f", monthlyAmount)} on dining (€${String.format("%.2f", avgMealCost)}/day). The average person spends €200-300/month.",
                            priority = InsightPriority.HIGH,
                            priorityColor = Color(0xFFE53935),
                            icon = Icons.Default.Restaurant,
                            iconColor = Color(0xFFE53935),
                            iconBackground = Color(0xFFFFEBEE),
                            savingAmount = monthlyAmount * 0.4,
                            actionItems = listOf(
                                "Try meal prepping on Sundays",
                                "Limit dining out to weekends only",
                                "Use grocery delivery to avoid impulse buys"
                            )
                        ))

                        recommendations.add(Recommendation(
                            title = "Start Cooking at Home",
                            description = "Based on your dining expenses, cooking at home just 3 more times per week could save you €${String.format("%.2f", monthlyAmount * 0.3)}/month. Try simple recipes like pasta, stir-fries, or one-pot meals.",
                            icon = Icons.Default.Kitchen,
                            iconColor = Color(0xFF43A047),
                            backgroundColor = Color(0xFFE8F5E9),
                            expectedImpact = "Potential savings: €${String.format("%.2f", monthlyAmount * 0.3)}/month"
                        ))
                    }
                    monthlyAmount > 300 -> {
                        recommendations.add(Recommendation(
                            title = "Try Meal Planning",
                            description = "Plan your meals for the week and grocery shop with a list. This can reduce food expenses by 20-30%.",
                            icon = Icons.Default.ListAlt,
                            iconColor = Color(0xFFFFA726),
                            backgroundColor = Color(0xFFFFF3E0),
                            expectedImpact = "Save up to €${String.format("%.2f", monthlyAmount * 0.25)}/month"
                        ))
                    }
                }
            }

            "Transport" -> {
                when {
                    monthlyAmount > 400 -> {
                        insights.add(Insight(
                            title = "High Transportation Costs",
                            description = "You're spending €${String.format("%.2f", monthlyAmount)} on transport. Consider carpooling, public transport, or cycling for short trips.",
                            priority = InsightPriority.MEDIUM,
                            priorityColor = Color(0xFFFFA726),
                            icon = Icons.Default.DirectionsCar,
                            iconColor = Color(0xFFFFA726),
                            iconBackground = Color(0xFFFFF3E0),
                            savingAmount = monthlyAmount * 0.25,
                            actionItems = listOf(
                                "Check if public transport pass is cheaper",
                                "Consider carpooling apps",
                                "Walk or bike for trips under 2km"
                            )
                        ))
                    }
                }
            }

            "Shopping" -> {
                when {
                    monthlyAmount > 300 -> {
                        val itemCount = recentTransactions.count {
                            it.category == "Shopping" && it.type == TransactionType.EXPENSE
                        }
                        val avgPerPurchase = monthlyAmount / itemCount.coerceAtLeast(1)

                        insights.add(Insight(
                            title = "Shopping Habits Analysis",
                            description = "You made $itemCount shopping purchases averaging €${String.format("%.2f", avgPerPurchase)} each. Consider the 24-hour rule before buying non-essentials.",
                            priority = InsightPriority.MEDIUM,
                            priorityColor = Color(0xFFFFA726),
                            icon = Icons.Default.ShoppingBag,
                            iconColor = Color(0xFFFFA726),
                            iconBackground = Color(0xFFFFF3E0),
                            savingAmount = monthlyAmount * 0.3,
                            actionItems = listOf(
                                "Wait 24 hours before non-essential purchases",
                                "Unsubscribe from marketing emails",
                                "Make a wishlist and buy only during sales"
                            )
                        ))

                        recommendations.add(Recommendation(
                            title = "Implement No-Spend Days",
                            description = "Try having 2-3 'no-spend' days per week where you don't make any purchases except essentials.",
                            icon = Icons.Default.DoNotDisturbOn,
                            iconColor = Color(0xFF1976D2),
                            backgroundColor = Color(0xFFE3F2FD),
                            expectedImpact = "Could reduce shopping expenses by 30%"
                        ))
                    }
                }
            }

            "Entertainment" -> {
                when {
                    percentage > 15 -> {
                        insights.add(Insight(
                            title = "Entertainment Budget",
                            description = "Entertainment is ${percentage.roundToInt()}% of your expenses. The recommended range is 5-10% of income.",
                            priority = InsightPriority.MEDIUM,
                            priorityColor = Color(0xFFFFA726),
                            icon = Icons.Default.MovieFilter,
                            iconColor = Color(0xFFFFA726),
                            iconBackground = Color(0xFFFFF3E0),
                            savingAmount = monthlyAmount * 0.3,
                            actionItems = listOf(
                                "Look for free entertainment options",
                                "Use streaming services instead of cinema",
                                "Take advantage of happy hours and discounts"
                            )
                        ))
                    }
                }
            }
        }
    }

    // 3. Spending Velocity Analysis
    val lastWeekExpense = lastWeekTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val weeklyAverage = weeklyExpenses.average()

    when {
        lastWeekExpense > weeklyAverage * 1.5 -> {
            patterns.add(SpendingPattern(
                type = PatternType.INCREASING,
                description = "Your spending increased by ${((lastWeekExpense / weeklyAverage - 1) * 100).roundToInt()}% this week",
                impact = "At this rate, you'll overspend by €${String.format("%.2f", (lastWeekExpense - weeklyAverage) * 4)} this month"
            ))

            insights.add(Insight(
                title = "Spending Spike Detected",
                description = "You spent €${String.format("%.2f", lastWeekExpense)} this week, ${((lastWeekExpense / weeklyAverage - 1) * 100).roundToInt()}% above your average.",
                priority = InsightPriority.HIGH,
                priorityColor = Color(0xFFE53935),
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFFE53935),
                iconBackground = Color(0xFFFFEBEE),
                actionItems = listOf(
                    "Review this week's transactions",
                    "Set a weekly spending limit",
                    "Use cash for discretionary spending"
                )
            ))
        }
        lastWeekExpense < weeklyAverage * 0.7 -> {
            patterns.add(SpendingPattern(
                type = PatternType.DECREASING,
                description = "Great job! Your spending decreased by ${((1 - lastWeekExpense / weeklyAverage) * 100).roundToInt()}% this week",
                impact = "Keep this up to save an extra €${String.format("%.2f", (weeklyAverage - lastWeekExpense) * 4)} this month"
            ))
        }
        else -> {
            patterns.add(SpendingPattern(
                type = PatternType.STABLE,
                description = "Your spending is consistent with your average",
                impact = null
            ))
        }
    }

    // 4. Day of Week Analysis
    val dayOfWeekSpending = analyzeDayOfWeekSpending(recentTransactions)
    val highestSpendingDay = dayOfWeekSpending.maxByOrNull { it.value }
    if (highestSpendingDay != null && highestSpendingDay.value > dailyAverageExpense * 2) {
        insights.add(Insight(
            title = "${getDayName(highestSpendingDay.key)} Spending Pattern",
            description = "You spend an average of €${String.format("%.2f", highestSpendingDay.value)} on ${getDayName(highestSpendingDay.key)}s, which is ${((highestSpendingDay.value / dailyAverageExpense - 1) * 100).roundToInt()}% above your daily average.",
            priority = InsightPriority.MEDIUM,
            priorityColor = Color(0xFFFFA726),
            icon = Icons.Default.CalendarToday,
            iconColor = Color(0xFFFFA726),
            iconBackground = Color(0xFFFFF3E0),
            savingAmount = (highestSpendingDay.value - dailyAverageExpense) * 4,
            actionItems = listOf(
                "Plan activities for ${getDayName(highestSpendingDay.key)}s in advance",
                "Set a specific budget for ${getDayName(highestSpendingDay.key)}s"
            )
        ))
    }

    // 5. Subscription Detection
    val recurringExpenses = detectRecurringExpenses(transactions)
    if (recurringExpenses.isNotEmpty()) {
        val totalRecurring = recurringExpenses.sumOf { it.amount }
        insights.add(Insight(
            title = "Recurring Expenses Detected",
            description = "You have approximately ${recurringExpenses.size} recurring expenses totaling €${String.format("%.2f", totalRecurring)}/month. Review if all subscriptions are being used.",
            priority = InsightPriority.MEDIUM,
            priorityColor = Color(0xFF1976D2),
            icon = Icons.Default.Repeat,
            iconColor = Color(0xFF1976D2),
            iconBackground = Color(0xFFE3F2FD),
            savingAmount = totalRecurring * 0.2,
            actionItems = listOf(
                "Audit all subscriptions",
                "Cancel unused services",
                "Share family plans where possible"
            )
        ))

        recommendations.add(Recommendation(
            title = "Subscription Audit Time",
            description = "Review your subscriptions - studies show people save 20-30% by canceling forgotten subscriptions.",
            icon = Icons.Default.Checklist,
            iconColor = Color(0xFF6C63FF),
            backgroundColor = Color(0xFFEDE7F6),
            expectedImpact = "Average savings: €${String.format("%.2f", totalRecurring * 0.25)}/month"
        ))
    }

    // 6. Income Stability Check
    val incomeVariability = calculateIncomeVariability(recentTransactions)
    if (incomeVariability > 0.3 && totalIncome > 0) {
        insights.add(Insight(
            title = "Variable Income Detected",
            description = "Your income varies by ${(incomeVariability * 100).roundToInt()}%. Consider building a larger emergency fund (4-6 months expenses).",
            priority = InsightPriority.MEDIUM,
            priorityColor = Color(0xFF1976D2),
            icon = Icons.Default.ShowChart,
            iconColor = Color(0xFF1976D2),
            iconBackground = Color(0xFFE3F2FD),
            actionItems = listOf(
                "Build emergency fund to 6 months expenses",
                "Budget based on lowest income month",
                "Look for additional stable income sources"
            )
        ))
    }

    // 7. Zero-Day Analysis
    val zeroDays = countZeroSpendDays(recentTransactions)
    if (zeroDays < 5) {
        recommendations.add(Recommendation(
            title = "Try No-Spend Days",
            description = "You had only $zeroDays days without spending last month. Try to have at least 8-10 no-spend days per month.",
            icon = Icons.Default.Savings,
            iconColor = Color(0xFF43A047),
            backgroundColor = Color(0xFFE8F5E9),
            expectedImpact = "Could save €${String.format("%.2f", dailyAverageExpense * 5)}/month"
        ))
    }

    // 8. Smart Recommendations based on patterns
    if (expensesByCategory.isNotEmpty()) {
        val topCategory = expensesByCategory.first()

        // Specific recommendations based on top spending category
        when (topCategory.first) {
            "Food & Dining" -> {
                recommendations.add(Recommendation(
                    title = "Meal Prep Sunday",
                    description = "Dedicate 2 hours on Sunday to meal prep. This can reduce weekday dining expenses by 60%.",
                    icon = Icons.Default.FoodBank,
                    iconColor = Color(0xFF43A047),
                    backgroundColor = Color(0xFFE8F5E9),
                    expectedImpact = "Save 3-4 hours and €50-80 per week"
                ))
            }
            "Transport" -> {
                recommendations.add(Recommendation(
                    title = "Optimize Your Commute",
                    description = "Consider carpooling 2-3 days a week or using public transport for longer trips.",
                    icon = Icons.Default.DirectionsBus,
                    iconColor = Color(0xFF1976D2),
                    backgroundColor = Color(0xFFE3F2FD),
                    expectedImpact = "Reduce transport costs by 30%"
                ))
            }
            "Shopping" -> {
                recommendations.add(Recommendation(
                    title = "The 30-Day List",
                    description = "Write down items you want to buy and wait 30 days. You'll find you don't actually want 80% of them.",
                    icon = Icons.Default.Timer,
                    iconColor = Color(0xFF6C63FF),
                    backgroundColor = Color(0xFFEDE7F6),
                    expectedImpact = "Reduce impulse purchases by 70%"
                ))
            }
        }
    }

    // 9. Savings Goals Recommendation
    if (balance > 0 && totalIncome > 0) {
        val monthsOfExpenses = balance / (totalExpense / 30 * 30)
        when {
            monthsOfExpenses < 1 -> {
                recommendations.add(Recommendation(
                    title = "Build Emergency Fund",
                    description = "You have less than 1 month of expenses saved. Aim for at least 3 months as a safety net.",
                    icon = Icons.Default.Shield,
                    iconColor = Color(0xFFD32F2F),
                    backgroundColor = Color(0xFFFFEBEE),
                    expectedImpact = "Financial security and peace of mind"
                ))
            }
            monthsOfExpenses < 3 -> {
                recommendations.add(Recommendation(
                    title = "Grow Your Safety Net",
                    description = "You have ${String.format("%.1f", monthsOfExpenses)} months of expenses saved. Work towards 3-6 months.",
                    icon = Icons.Default.TrendingUp,
                    iconColor = Color(0xFFFFA726),
                    backgroundColor = Color(0xFFFFF3E0)
                ))
            }
        }
    }

    // Calculate total savings potential
    val savingsPotential = insights.mapNotNull { it.savingAmount }.sum()

    return InsightsAnalysis(
        healthScore = healthScore,
        scoreBreakdown = scoreBreakdown,
        savingsPotential = savingsPotential,
        insights = insights.sortedByDescending { it.priority.ordinal },
        recommendations = recommendations,
        spendingPatterns = patterns
    )
}

// ============== HELPER FUNCTIONS ==============

fun calculateHealthScore(
    income: Double,
    expenses: Double,
    balance: Double,
    expensesByCategory: List<Pair<String, Double>>,
    weeklyExpenses: List<Double>
): ScoreBreakdown {
    // Savings Score (0-100)
    val savingsRate = if (income > 0) balance / income else 0.0
    val savingsScore = when {
        savingsRate >= 0.20 -> 100
        savingsRate >= 0.10 -> 70 + (savingsRate - 0.10) * 300
        savingsRate >= 0 -> 40 + savingsRate * 300
        else -> 0
    }.toInt().coerceIn(0, 100)

    // Spending Score (0-100)
    val spendingRatio = if (income > 0) expenses / income else 1.0
    val spendingScore = when {
        spendingRatio <= 0.70 -> 100
        spendingRatio <= 0.85 -> 70 + ((0.85 - spendingRatio) / 0.15 * 30)
        spendingRatio <= 1.0 -> 40 + ((1.0 - spendingRatio) / 0.15 * 30)
        else -> (20 / spendingRatio)
    }.toInt().coerceIn(0, 100)

    // Income Score (0-100)
    val incomeScore = when {
        income >= 3000 -> 100
        income >= 2000 -> 70 + ((income - 2000) / 1000 * 30)
        income >= 1000 -> 40 + ((income - 1000) / 1000 * 30)
        income > 0 -> (income / 1000 * 40)
        else -> 0
    }.toInt().coerceIn(0, 100)

    // Balance Score (0-100)
    val monthsOfExpenses = if (expenses > 0) balance / expenses else 0.0
    val balanceScore = when {
        monthsOfExpenses >= 6 -> 100
        monthsOfExpenses >= 3 -> 70 + ((monthsOfExpenses - 3) / 3 * 30)
        monthsOfExpenses >= 1 -> 40 + ((monthsOfExpenses - 1) / 2 * 30)
        monthsOfExpenses >= 0 -> (monthsOfExpenses * 40)
        else -> 0
    }.toInt().coerceIn(0, 100)

    return ScoreBreakdown(savingsScore, spendingScore, incomeScore, balanceScore)
}

fun calculateWeeklyExpenses(transactions: List<FinancialTransaction>): List<Double> {
    val calendar = Calendar.getInstance()
    val weeklyExpenses = mutableListOf<Double>()

    for (week in 0..3) {
        calendar.time = Date()
        calendar.add(Calendar.WEEK_OF_YEAR, -week)
        val weekStart = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekEnd = calendar.time

        val weekExpense = transactions
            .filter {
                it.type == TransactionType.EXPENSE &&
                        it.date.before(weekStart) &&
                        it.date.after(weekEnd)
            }
            .sumOf { it.amount }
        weeklyExpenses.add(weekExpense)
    }

    return weeklyExpenses
}

fun analyzeDayOfWeekSpending(transactions: List<FinancialTransaction>): Map<Int, Double> {
    val calendar = Calendar.getInstance()
    val daySpending = mutableMapOf<Int, MutableList<Double>>()

    transactions
        .filter { it.type == TransactionType.EXPENSE }
        .forEach { transaction ->
            calendar.time = transaction.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            daySpending.getOrPut(dayOfWeek) { mutableListOf() }.add(transaction.amount)
        }

    return daySpending.mapValues { it.value.average() }
}

fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
    }
}

fun detectRecurringExpenses(transactions: List<FinancialTransaction>): List<FinancialTransaction> {
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val grouped = expenses.groupBy { it.description.lowercase() }

    return grouped
        .filter { it.value.size >= 2 }
        .mapNotNull { (_, trans) ->
            if (trans.size >= 2) {
                val amounts = trans.map { it.amount }
                val avgAmount = amounts.average()
                val variance = amounts.map { abs(it - avgAmount) }.average()
                if (variance < avgAmount * 0.1) {
                    trans.first()
                } else null
            } else null
        }
}

fun calculateIncomeVariability(transactions: List<FinancialTransaction>): Double {
    val incomes = transactions
        .filter { it.type == TransactionType.INCOME }
        .map { it.amount }

    if (incomes.size < 2) return 0.0

    val average = incomes.average()
    val variance = incomes.map { abs(it - average) }.average()

    return if (average > 0) variance / average else 0.0
}

fun countZeroSpendDays(transactions: List<FinancialTransaction>): Int {
    val calendar = Calendar.getInstance()
    val spendDays = mutableSetOf<String>()

    transactions
        .filter { it.type == TransactionType.EXPENSE }
        .forEach { transaction ->
            calendar.time = transaction.date
            val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            spendDays.add(dayKey)
        }

    return 30 - spendDays.size
}