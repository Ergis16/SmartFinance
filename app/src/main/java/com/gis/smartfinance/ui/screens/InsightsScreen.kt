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
import kotlin.math.min
import kotlin.math.roundToInt
import java.util.concurrent.TimeUnit

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
                // Data Overview Card - Shows what data we have
                item {
                    DataOverviewCard(
                        transactionCount = transactions.size,
                        daysOfData = insightsAnalysis.daysOfData,
                        dataQuality = insightsAnalysis.dataQuality
                    )
                }

                // Financial Health Score Card
                if (insightsAnalysis.daysOfData >= 3) {
                    item {
                        FinancialHealthCard(
                            healthScore = insightsAnalysis.healthScore,
                            scoreBreakdown = insightsAnalysis.scoreBreakdown,
                            explanation = insightsAnalysis.scoreExplanation
                        )
                    }
                }

                // Insights Summary Card
                if (insightsAnalysis.insights.isNotEmpty()) {
                    item {
                        InsightsSummaryCard(
                            totalSavingsPotential = insightsAnalysis.savingsPotential,
                            insightsCount = insightsAnalysis.insights.size,
                            urgentInsights = insightsAnalysis.insights.count { it.priority == InsightPriority.URGENT }
                        )
                    }
                }

                // Spending Patterns Card - Only show if we have enough data
                if (insightsAnalysis.spendingPatterns.isNotEmpty() && insightsAnalysis.daysOfData >= 7) {
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
fun DataOverviewCard(
    transactionCount: Int,
    daysOfData: Int,
    dataQuality: DataQuality
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (dataQuality) {
                DataQuality.EXCELLENT -> Color(0xFF43A047)
                DataQuality.GOOD -> Color(0xFF66BB6A)
                DataQuality.LIMITED -> Color(0xFFFFA726)
                DataQuality.INSUFFICIENT -> Color(0xFFFF9800)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Data Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DataMetric(
                    label = "Transactions",
                    value = transactionCount.toString(),
                    icon = Icons.Default.Receipt
                )
                DataMetric(
                    label = "Days Tracked",
                    value = daysOfData.toString(),
                    icon = Icons.Default.CalendarToday
                )
                DataMetric(
                    label = "Data Quality",
                    value = when (dataQuality) {
                        DataQuality.EXCELLENT -> "Excellent"
                        DataQuality.GOOD -> "Good"
                        DataQuality.LIMITED -> "Limited"
                        DataQuality.INSUFFICIENT -> "Building"
                    },
                    icon = Icons.Default.Assessment
                )
            }

            if (dataQuality == DataQuality.INSUFFICIENT || dataQuality == DataQuality.LIMITED) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    when (dataQuality) {
                        DataQuality.INSUFFICIENT -> "📊 Add more transactions to get detailed insights"
                        DataQuality.LIMITED -> "📈 Keep tracking for ${7 - daysOfData} more days for full analysis"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun DataMetric(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun FinancialHealthCard(
    healthScore: Int,
    scoreBreakdown: ScoreBreakdown,
    explanation: String
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

                // Score breakdown with explanations
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    ScoreItem("Savings", scoreBreakdown.savingsScore, "% of income saved")
                    ScoreItem("Spending", scoreBreakdown.spendingScore, "% budget control")
                    ScoreItem("Income", scoreBreakdown.incomeScore, "% stability")
                    ScoreItem("Balance", scoreBreakdown.balanceScore, "% growth")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation of the score
            Text(
                explanation,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

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
fun ScoreItem(label: String, score: Int, hint: String) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
        Text(
            hint,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
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
                        "Potential Monthly Savings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "€${String.format("%.2f", totalSavingsPotential)}",
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
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val savingAmount: Double? = null,
    val actionItems: List<String> = emptyList()
)

data class Recommendation(
    val title: String,
    val description: String,
    val icon: ImageVector,
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

enum class DataQuality {
    INSUFFICIENT,  // < 3 days of data
    LIMITED,       // 3-7 days of data
    GOOD,          // 7-30 days of data
    EXCELLENT      // 30+ days of data
}

// ============== IMPROVED ANALYSIS ENGINE ==============

fun analyzeTransactions(
    transactions: List<FinancialTransaction>,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
): InsightsAnalysis {
    val insights = mutableListOf<Insight>()
    val recommendations = mutableListOf<Recommendation>()
    val patterns = mutableListOf<SpendingPattern>()

    // Calculate data availability metrics
    val daysOfData = calculateDaysOfData(transactions)
    val dataQuality = when {
        daysOfData < 3 -> DataQuality.INSUFFICIENT
        daysOfData < 7 -> DataQuality.LIMITED
        daysOfData < 30 -> DataQuality.GOOD
        else -> DataQuality.EXCELLENT
    }

    // Don't generate misleading insights with insufficient data
    if (dataQuality == DataQuality.INSUFFICIENT) {
        insights.add(Insight(
            title = "Building Your Financial Profile",
            description = "Keep adding transactions for the next ${3 - daysOfData} days to unlock personalized insights. I need at least 3 days of data to provide meaningful analysis.",
            priority = InsightPriority.LOW,
            priorityColor = Color(0xFF1976D2),
            icon = Icons.Default.Info,
            iconColor = Color(0xFF1976D2),
            iconBackground = Color(0xFFE3F2FD)
        ))

        // Basic recommendation for new users
        recommendations.add(Recommendation(
            title = "Track Everything",
            description = "Record all your expenses and income for at least a week to get accurate insights.",
            icon = Icons.Default.Assignment,
            iconColor = Color(0xFF6C63FF),
            backgroundColor = Color(0xFFEDE7F6)
        ))

        return InsightsAnalysis(
            healthScore = 50, // Neutral score for new users
            scoreBreakdown = ScoreBreakdown(50, 50, 50, 50),
            scoreExplanation = "Need more data to calculate accurate health score",
            savingsPotential = 0.0,
            insights = insights,
            recommendations = recommendations,
            spendingPatterns = emptyList(),
            daysOfData = daysOfData,
            dataQuality = dataQuality
        )
    }

    // Calculate time-based metrics with proper data checks
    val now = Date()
    val expensesByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }
        .toList()
        .sortedByDescending { it.second }

    val incomeByCategory = transactions
        .filter { it.type == TransactionType.INCOME }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }

    // Calculate daily average based on actual days of data
    val dailyAverageExpense = if (daysOfData > 0) totalExpense / daysOfData else 0.0
    val dailyAverageIncome = if (daysOfData > 0) totalIncome / daysOfData else 0.0

    // Project monthly values based on daily averages
    val projectedMonthlyExpense = dailyAverageExpense * 30
    val projectedMonthlyIncome = dailyAverageIncome * 30
    val projectedMonthlyBalance = projectedMonthlyIncome - projectedMonthlyExpense

    // Calculate Financial Health Score with proper context
    val scoreBreakdown = calculateHealthScore(
        projectedMonthlyIncome,
        projectedMonthlyExpense,
        balance,
        expensesByCategory,
        daysOfData
    )
    val healthScore = (scoreBreakdown.savingsScore + scoreBreakdown.spendingScore +
            scoreBreakdown.incomeScore + scoreBreakdown.balanceScore) / 4

    val scoreExplanation = when {
        healthScore >= 80 -> "You're doing great! Your spending is under control and you're saving well."
        healthScore >= 60 -> "Good financial habits overall. Focus on the highlighted areas for improvement."
        healthScore >= 40 -> "There's room for improvement. Follow the recommendations to boost your score."
        else -> "Your finances need attention. Start with the urgent items below."
    }

    // === GENERATE CONTEXTUAL INSIGHTS ===

    // 1. Savings Rate Analysis (with proper projections)
    if (daysOfData >= 3) {
        val savingsRate = if (projectedMonthlyIncome > 0) {
            projectedMonthlyBalance / projectedMonthlyIncome
        } else 0.0

        when {
            savingsRate < 0 -> {
                insights.add(Insight(
                    title = "Spending Exceeds Income",
                    description = "Based on ${daysOfData} days of data, you're on track to spend €${String.format("%.2f", abs(projectedMonthlyBalance))} more than you earn monthly.",
                    priority = InsightPriority.URGENT,
                    priorityColor = Color(0xFFD32F2F),
                    icon = Icons.Default.Error,
                    iconColor = Color(0xFFD32F2F),
                    iconBackground = Color(0xFFFFEBEE),
                    savingAmount = abs(projectedMonthlyBalance),
                    actionItems = listOf(
                        "Review your largest expense categories",
                        "Look for subscriptions to cancel",
                        "Set a daily spending limit of €${String.format("%.2f", dailyAverageIncome * 0.8)}"
                    )
                ))
            }
            savingsRate < 0.1 && projectedMonthlyIncome > 0 -> {
                insights.add(Insight(
                    title = "Low Savings Rate",
                    description = "You're saving only ${(savingsRate * 100).roundToInt()}% of your income. Experts recommend 20%.",
                    priority = InsightPriority.HIGH,
                    priorityColor = Color(0xFFE53935),
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFE53935),
                    iconBackground = Color(0xFFFFEBEE),
                    savingAmount = projectedMonthlyIncome * 0.2 - projectedMonthlyBalance,
                    actionItems = listOf(
                        "Set up automatic savings of €${String.format("%.2f", projectedMonthlyIncome * 0.1)}",
                        "Try the 50/30/20 budget rule"
                    )
                ))
            }
            savingsRate >= 0.2 -> {
                insights.add(Insight(
                    title = "Excellent Savings!",
                    description = "You're saving ${(savingsRate * 100).roundToInt()}% of your income. Keep it up!",
                    priority = InsightPriority.LOW,
                    priorityColor = Color(0xFF43A047),
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF43A047),
                    iconBackground = Color(0xFFE8F5E9)
                ))
            }
        }
    }

    // 2. Category Analysis (only with sufficient data)
    if (daysOfData >= 7) {
        expensesByCategory.forEach { (category, amount) ->
            val dailyAverage = amount / daysOfData
            val projectedMonthly = dailyAverage * 30
            val percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0

            when (category) {
                "Food & Dining" -> {
                    if (projectedMonthly > 400) {
                        insights.add(Insight(
                            title = "High Food & Dining Spending",
                            description = "You're on track to spend €${String.format("%.2f", projectedMonthly)}/month on dining (€${String.format("%.2f", dailyAverage)}/day).",
                            priority = InsightPriority.MEDIUM,
                            priorityColor = Color(0xFFFFA726),
                            icon = Icons.Default.Restaurant,
                            iconColor = Color(0xFFFFA726),
                            iconBackground = Color(0xFFFFF3E0),
                            savingAmount = projectedMonthly * 0.3,
                            actionItems = listOf(
                                "Try cooking at home 3 more times per week",
                                "Meal prep on Sundays",
                                "Set a dining out budget of €${String.format("%.2f", projectedMonthly * 0.6)}"
                            )
                        ))
                    }
                }

                "Shopping" -> {
                    if (percentage > 30) {
                        insights.add(Insight(
                            title = "Shopping is Your Biggest Expense",
                            description = "${percentage.roundToInt()}% of your spending goes to shopping. Consider if all purchases are necessary.",
                            priority = InsightPriority.MEDIUM,
                            priorityColor = Color(0xFFFFA726),
                            icon = Icons.Default.ShoppingBag,
                            iconColor = Color(0xFFFFA726),
                            iconBackground = Color(0xFFFFF3E0),
                            savingAmount = projectedMonthly * 0.25,
                            actionItems = listOf(
                                "Wait 24 hours before non-essential purchases",
                                "Unsubscribe from promotional emails",
                                "Try a no-spend week challenge"
                            )
                        ))
                    }
                }

                "Transport" -> {
                    if (projectedMonthly > 300) {
                        recommendations.add(Recommendation(
                            title = "Optimize Transportation",
                            description = "Consider carpooling or public transport for some trips to reduce costs.",
                            icon = Icons.Default.DirectionsBus,
                            iconColor = Color(0xFF1976D2),
                            backgroundColor = Color(0xFFE3F2FD),
                            expectedImpact = "Save up to €${String.format("%.2f", projectedMonthly * 0.2)}/month"
                        ))
                    }
                }
            }
        }
    }

    // 3. Spending Trend Analysis (requires at least 7 days)
    if (daysOfData >= 7) {
        val recentDaysToCheck = minOf(7, daysOfData / 2)
        val recentDaysAgo = Date(now.time - recentDaysToCheck * 24L * 60 * 60 * 1000)

        val recentTransactions = transactions.filter { it.date.after(recentDaysAgo) }
        val olderTransactions = transactions.filter { it.date.before(recentDaysAgo) }

        if (olderTransactions.isNotEmpty()) {
            val recentDailyAvg = recentTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount } / recentDaysToCheck

            val olderDailyAvg = olderTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount } / (daysOfData - recentDaysToCheck).coerceAtLeast(1)

            when {
                recentDailyAvg > olderDailyAvg * 1.2 -> {
                    patterns.add(SpendingPattern(
                        type = PatternType.INCREASING,
                        description = "Your spending has increased ${((recentDailyAvg / olderDailyAvg - 1) * 100).roundToInt()}% recently",
                        impact = "This trend would add €${String.format("%.2f", (recentDailyAvg - olderDailyAvg) * 30)} to monthly expenses"
                    ))
                }
                recentDailyAvg < olderDailyAvg * 0.8 -> {
                    patterns.add(SpendingPattern(
                        type = PatternType.DECREASING,
                        description = "Great! Your spending decreased ${((1 - recentDailyAvg / olderDailyAvg) * 100).roundToInt()}% recently",
                        impact = "Keep this up to save €${String.format("%.2f", (olderDailyAvg - recentDailyAvg) * 30)}/month"
                    ))
                }
                else -> {
                    patterns.add(SpendingPattern(
                        type = PatternType.STABLE,
                        description = "Your spending is consistent",
                        impact = null
                    ))
                }
            }
        }
    }

    // 4. Day of Week Analysis (requires at least 14 days for meaningful patterns)
    if (daysOfData >= 14) {
        val dayOfWeekSpending = analyzeDayOfWeekSpending(transactions)
        val highestSpendingDay = dayOfWeekSpending.maxByOrNull { it.value }

        if (highestSpendingDay != null && highestSpendingDay.value > dailyAverageExpense * 1.5) {
            insights.add(Insight(
                title = "${getDayName(highestSpendingDay.key)} Spending Pattern",
                description = "You tend to spend €${String.format("%.2f", highestSpendingDay.value)} on ${getDayName(highestSpendingDay.key)}s, ${((highestSpendingDay.value / dailyAverageExpense - 1) * 100).roundToInt()}% above your daily average.",
                priority = InsightPriority.LOW,
                priorityColor = Color(0xFF1976D2),
                icon = Icons.Default.CalendarToday,
                iconColor = Color(0xFF1976D2),
                iconBackground = Color(0xFFE3F2FD),
                savingAmount = (highestSpendingDay.value - dailyAverageExpense) * 4,
                actionItems = listOf(
                    "Plan ${getDayName(highestSpendingDay.key)} activities in advance",
                    "Set a specific ${getDayName(highestSpendingDay.key)} budget"
                )
            ))
        }
    }

    // 5. General Recommendations based on data quality
    when (dataQuality) {
        DataQuality.LIMITED -> {
            recommendations.add(Recommendation(
                title = "Keep Tracking",
                description = "You're building good habits! Continue tracking for ${7 - daysOfData} more days to unlock detailed spending patterns.",
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFF43A047),
                backgroundColor = Color(0xFFE8F5E9)
            ))
        }
        DataQuality.GOOD -> {
            if (expensesByCategory.isNotEmpty()) {
                val topCategory = expensesByCategory.first()
                recommendations.add(Recommendation(
                    title = "Focus on ${topCategory.first}",
                    description = "This is your biggest expense category. Small changes here can have big impact.",
                    icon = Icons.Default.Flag,
                    iconColor = Color(0xFF6C63FF),
                    backgroundColor = Color(0xFFEDE7F6),
                    expectedImpact = "Potential savings: €${String.format("%.2f", (topCategory.second / daysOfData * 30 * 0.2))}/month"
                ))
            }
        }
        DataQuality.EXCELLENT -> {
            // Advanced recommendations for users with lots of data
            val avgTransaction = totalExpense / transactions.filter { it.type == TransactionType.EXPENSE }.size
            if (avgTransaction < 10) {
                insights.add(Insight(
                    title = "Many Small Transactions",
                    description = "Your average transaction is only €${String.format("%.2f", avgTransaction)}. These small expenses add up!",
                    priority = InsightPriority.MEDIUM,
                    priorityColor = Color(0xFFFFA726),
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFFFFA726),
                    iconBackground = Color(0xFFFFF3E0),
                    actionItems = listOf(
                        "Review small daily purchases",
                        "Consider if each small expense is necessary"
                    )
                ))
            }
        }
        else -> {}
    }

    // 6. Emergency Fund Check (only after 7+ days)
    if (daysOfData >= 7 && balance >= 0) {
        val monthsOfExpenses = if (projectedMonthlyExpense > 0) {
            balance / projectedMonthlyExpense
        } else 0.0

        when {
            monthsOfExpenses < 1 -> {
                recommendations.add(Recommendation(
                    title = "Build an Emergency Fund",
                    description = "You have less than 1 month of expenses saved. Aim for at least 3 months.",
                    icon = Icons.Default.Shield,
                    iconColor = Color(0xFFD32F2F),
                    backgroundColor = Color(0xFFFFEBEE),
                    expectedImpact = "Financial security in emergencies"
                ))
            }
            monthsOfExpenses in 1.0..3.0 -> {
                recommendations.add(Recommendation(
                    title = "Grow Your Safety Net",
                    description = "You have ${String.format("%.1f", monthsOfExpenses)} months of expenses saved. Work towards 3-6 months.",
                    icon = Icons.Default.Security,
                    iconColor = Color(0xFFFFA726),
                    backgroundColor = Color(0xFFFFF3E0)
                ))
            }
            monthsOfExpenses >= 3 -> {
                insights.add(Insight(
                    title = "Strong Emergency Fund!",
                    description = "You have ${String.format("%.1f", monthsOfExpenses)} months of expenses saved. Great job!",
                    priority = InsightPriority.LOW,
                    priorityColor = Color(0xFF43A047),
                    icon = Icons.Default.Verified,
                    iconColor = Color(0xFF43A047),
                    iconBackground = Color(0xFFE8F5E9)
                ))
            }
        }
    }

    // Calculate realistic savings potential
    val savingsPotential = insights.mapNotNull { it.savingAmount }.sum()

    return InsightsAnalysis(
        healthScore = healthScore,
        scoreBreakdown = scoreBreakdown,
        scoreExplanation = scoreExplanation,
        savingsPotential = savingsPotential,
        insights = insights.sortedByDescending { it.priority.ordinal },
        recommendations = recommendations,
        spendingPatterns = patterns,
        daysOfData = daysOfData,
        dataQuality = dataQuality
    )
}

// ============== HELPER FUNCTIONS ==============

fun calculateDaysOfData(transactions: List<FinancialTransaction>): Int {
    if (transactions.isEmpty()) return 0

    val oldestTransaction = transactions.minByOrNull { it.date }?.date ?: return 0
    val newestTransaction = transactions.maxByOrNull { it.date }?.date ?: return 0

    val diffInMillis = newestTransaction.time - oldestTransaction.time
    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() + 1 // +1 to include both days

    return days
}

fun calculateHealthScore(
    monthlyIncome: Double,
    monthlyExpenses: Double,
    currentBalance: Double,
    expensesByCategory: List<Pair<String, Double>>,
    daysOfData: Int
): ScoreBreakdown {
    // Adjust scoring based on data availability
    val dataMultiplier = when {
        daysOfData < 7 -> 0.7
        daysOfData < 30 -> 0.85
        else -> 1.0
    }

    // Savings Score (0-100)
    val savingsRate = if (monthlyIncome > 0) {
        (monthlyIncome - monthlyExpenses) / monthlyIncome
    } else 0.0

    val savingsScore = (when {
        savingsRate >= 0.20 -> 100.0
        savingsRate >= 0.10 -> 70.0 + (savingsRate - 0.10) * 300
        savingsRate >= 0 -> 40.0 + savingsRate * 300
        else -> 0.0
    } * dataMultiplier).toInt().coerceIn(0, 100)

    // Spending Score (0-100)
    val spendingRatio = if (monthlyIncome > 0) monthlyExpenses / monthlyIncome else 1.0
    val spendingScore = (when {
        spendingRatio <= 0.70 -> 100.0
        spendingRatio <= 0.85 -> 70.0 + ((0.85 - spendingRatio) / 0.15 * 30)
        spendingRatio <= 1.0 -> 40.0 + ((1.0 - spendingRatio) / 0.15 * 30)
        else -> 20.0
    } * dataMultiplier).toInt().coerceIn(0, 100)

    // Income Score (simplified for better understanding)
    val incomeScore = when {
        monthlyIncome <= 0 -> 0
        monthlyIncome < 1000 -> (monthlyIncome / 1000 * 50).toInt()
        monthlyIncome < 3000 -> 50 + ((monthlyIncome - 1000) / 2000 * 30).toInt()
        else -> 80 + kotlin.math.min(20.0, (monthlyIncome - 3000) / 1000 * 5).toInt()
    }.coerceIn(0, 100)

    // Balance Score (emergency fund perspective)
    val monthsOfExpenses = if (monthlyExpenses > 0) currentBalance / monthlyExpenses else 0.0
    val balanceScore = (when {
        monthsOfExpenses >= 6 -> 100.0
        monthsOfExpenses >= 3 -> 70.0 + ((monthsOfExpenses - 3) / 3 * 30)
        monthsOfExpenses >= 1 -> 40.0 + ((monthsOfExpenses - 1) / 2 * 30)
        monthsOfExpenses >= 0 -> monthsOfExpenses * 40
        else -> 0.0
    } * dataMultiplier).toInt().coerceIn(0, 100)

    return ScoreBreakdown(savingsScore, spendingScore, incomeScore, balanceScore)
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

    // Only return averages if we have enough data points
    return daySpending
        .filter { it.value.size >= 2 } // Need at least 2 occurrences
        .mapValues { it.value.average() }
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