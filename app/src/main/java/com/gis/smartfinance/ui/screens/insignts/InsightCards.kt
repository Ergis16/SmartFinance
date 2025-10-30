package com.gis.smartfinance.ui.screens.insights

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gis.smartfinance.domain.insights.*
import com.gis.smartfinance.ui.theme.AppColors

/**
 * ✅ REAL FIX: Uses MaterialTheme.colorScheme instead of isSystemInDarkTheme()
 * This respects the theme set in Theme.kt
 */

@Composable
fun DataOverviewCard(
    transactionCount: Int,
    daysOfData: Int,
    dataQuality: DataQuality
) {
    // ✅ FIXED: Use predefined colors, MaterialTheme handles light/dark
    val backgroundColor = when (dataQuality) {
        DataQuality.EXCELLENT -> Color(0xFF43A047)
        DataQuality.GOOD -> Color(0xFF66BB6A)
        DataQuality.LIMITED -> Color(0xFFFFA726)
        DataQuality.INSUFFICIENT -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        transactionCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        daysOfData.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        dataQuality.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Quality",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (dataQuality == DataQuality.INSUFFICIENT || dataQuality == DataQuality.LIMITED) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    when (dataQuality) {
                        DataQuality.INSUFFICIENT -> "💡 Add more transactions"
                        DataQuality.LIMITED -> "📊 Track ${7 - daysOfData} more days"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FinancialHealthCard(
    healthScore: Int,
    scoreBreakdown: ScoreBreakdown,
    explanation: String
) {
    val animatedScore by animateIntAsState(
        targetValue = healthScore,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "score_animation"
    )

    var previousScore by remember { mutableStateOf(healthScore) }
    val scoreDelta = healthScore - previousScore

    LaunchedEffect(healthScore) {
        kotlinx.coroutines.delay(1000)
        previousScore = healthScore
    }

    // ✅ Simple color based on score
    val backgroundColor = when {
        healthScore >= 80 -> Color(0xFF43A047)
        healthScore >= 60 -> Color(0xFFFFA726)
        else -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
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
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
                            "$animatedScore",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 56.sp
                        )
                        Text(
                            "/100",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            healthScore.getRating(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )

                        if (scoreDelta != 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            ScoreDeltaIndicator(delta = scoreDelta)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedScore / 100f },
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
private fun ScoreDeltaIndicator(delta: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (delta > 0) Color(0xFF4CAF50) else Color(0xFFE53935)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                if (delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (delta > 0) "+$delta" else "$delta",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
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
        colors = CardDefaults.cardColors(containerColor = AppColors.Purple)
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

@Composable
fun SpendingPatternsCard(patterns: List<SpendingPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Spending Patterns Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (pattern.impact != null) {
                            Text(
                                pattern.impact,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurface
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
        colors = CardDefaults.cardColors(containerColor = recommendation.backgroundColor)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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