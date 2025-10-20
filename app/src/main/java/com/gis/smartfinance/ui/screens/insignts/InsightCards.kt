package com.gis.smartfinance.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gis.smartfinance.domain.insights.DataQuality
import com.gis.smartfinance.domain.insights.Insight
import com.gis.smartfinance.domain.insights.PatternType
import com.gis.smartfinance.domain.insights.Recommendation
import com.gis.smartfinance.domain.insights.ScoreBreakdown
import com.gis.smartfinance.domain.insights.SpendingPattern
import com.gis.smartfinance.ui.theme.AppColors

/**
 * Reusable UI components for Insights Screen
 * Extracted from massive InsightsScreen.kt
 */

/**
 * Data Overview Card
 * Shows transaction count, days tracked, and data quality
 */
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
            Text(
                "Data Overview",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

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
                    value = dataQuality.displayName,
                    icon = Icons.Default.Assessment
                )
            }

            if (dataQuality == DataQuality.INSUFFICIENT || dataQuality == DataQuality.LIMITED) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    when (dataQuality) {
                        DataQuality.INSUFFICIENT -> " Add more transactions for detailed insights"
                        DataQuality.LIMITED -> "Track ${7 - daysOfData} more days for full analysis"
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
private fun DataMetric(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

/**
 * Financial Health Card
 * Shows overall health score with breakdown
 */
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
                        healthScore.getRating(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    ScoreItem("Savings", scoreBreakdown.savingsScore)
                    ScoreItem("Spending", scoreBreakdown.spendingScore)
                    ScoreItem("Income", scoreBreakdown.incomeScore)
                    ScoreItem("Balance", scoreBreakdown.balanceScore)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
private fun ScoreItem(label: String, score: Int) {
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

/**
 * Insights Summary Card
 * Shows total savings potential
 */
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
                        "${String.format("%.2f", totalSavingsPotential)} Lek",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
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

/**
 * Spending Patterns Card
 */
@Composable
fun SpendingPatternsCard(patterns: List<SpendingPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        pattern.type.icon,
                        contentDescription = null,
                        tint = pattern.type.color,
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

/**
 * Individual Insight Card
 */
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

                if (insight.savingAmount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Save ${String.format("%.2f", insight.savingAmount)} Lek/month",
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

/**
 * Recommendation Card
 */
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

/**
 * Extension functions for cleaner code
 */
private val DataQuality.displayName: String
    get() = when (this) {
        DataQuality.EXCELLENT -> "Excellent"
        DataQuality.GOOD -> "Good"
        DataQuality.LIMITED -> "Limited"
        DataQuality.INSUFFICIENT -> "Building"
    }

private fun Int.getRating(): String = when {
    this >= 80 -> "Excellent"
    this >= 60 -> "Good"
    this >= 40 -> "Fair"
    else -> "Needs Work"
}

private val PatternType.icon: ImageVector
    get() = when (this) {
        PatternType.INCREASING -> Icons.Default.TrendingUp
        PatternType.DECREASING -> Icons.Default.TrendingDown
        PatternType.STABLE -> Icons.Default.TrendingFlat
        PatternType.IRREGULAR -> Icons.Default.ShowChart
    }

private val PatternType.color: Color
    get() = when (this) {
        PatternType.INCREASING -> Color(0xFFE53935)
        PatternType.DECREASING -> Color(0xFF43A047)
        PatternType.STABLE -> Color(0xFF1976D2)
        PatternType.IRREGULAR -> Color(0xFFFFA726)
    }