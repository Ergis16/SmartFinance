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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gis.smartfinance.data.TransactionManager
import com.gis.smartfinance.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit
) {
    val transactions by TransactionManager.transactions.collectAsState()
    val totalExpense by TransactionManager.totalExpense.collectAsState()
    val totalIncome by TransactionManager.totalIncome.collectAsState()

    // Generate insights based on actual data
    val insights = remember(transactions) {
        generateInsights(transactions, totalIncome, totalExpense)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Insights Summary Card
            item {
                InsightsSummaryCard(
                    totalSavingsPotential = calculateSavingsPotential(totalExpense),
                    insightsCount = insights.size
                )
            }

            // Individual Insight Cards
            items(insights) { insight ->
                InsightCard(insight = insight)
            }

            // If no insights available
            if (insights.isEmpty()) {
                item {
                    EmptyInsightsCard()
                }
            }
        }
    }
}

@Composable
fun InsightsSummaryCard(
    totalSavingsPotential: Double,
    insightsCount: Int
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
            Text(
                "Your Savings Potential",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "€${String.format("%.2f", totalSavingsPotential)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "per month",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = 0.65f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$insightsCount actionable insights found",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
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
                        color = Color(0xFF1A1A2E)
                    )

                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = insight.priorityColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            insight.priority,
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

                if (insight.savingAmount != null) {
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
            }
        }
    }
}

@Composable
fun EmptyInsightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDBDBD)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No insights yet",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add more transactions to get personalized insights",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

// Data class for insights
data class Insight(
    val title: String,
    val description: String,
    val priority: String,
    val priorityColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val savingAmount: Double? = null
)

// Generate insights based on transaction data
fun generateInsights(
    transactions: List<com.gis.smartfinance.data.model.FinancialTransaction>,
    totalIncome: Double,
    totalExpense: Double
): List<Insight> {
    val insights = mutableListOf<Insight>()

    // Spending ratio insight
    if (totalIncome > 0) {
        val spendingRatio = totalExpense / totalIncome
        if (spendingRatio > 0.8) {
            insights.add(
                Insight(
                    title = "High Spending Alert",
                    description = "You're spending ${(spendingRatio * 100).toInt()}% of your income. Consider reducing expenses to save more.",
                    priority = "HIGH",
                    priorityColor = Color(0xFFE53935),
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFE53935),
                    iconBackground = Color(0xFFFFEBEE),
                    savingAmount = totalExpense * 0.2
                )
            )
        }
    }

    // Category analysis
    val expensesByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }

    // Find highest spending category
    val highestCategory = expensesByCategory.maxByOrNull { it.value }
    if (highestCategory != null && highestCategory.value > totalExpense * 0.4) {
        insights.add(
            Insight(
                title = "Category Spending Pattern",
                description = "${highestCategory.key} represents ${(highestCategory.value / totalExpense * 100).toInt()}% of your expenses. Look for ways to optimize.",
                priority = "MEDIUM",
                priorityColor = Color(0xFFFFA726),
                icon = Icons.Default.PieChart,
                iconColor = Color(0xFFFFA726),
                iconBackground = Color(0xFFFFF3E0),
                savingAmount = highestCategory.value * 0.15
            )
        )
    }

    // Weekend spending pattern (simplified)
    if (transactions.size > 5) {
        insights.add(
            Insight(
                title = "Smart Saving Tip",
                description = "Based on your spending patterns, setting a weekly budget could help you save more effectively.",
                priority = "LOW",
                priorityColor = Color(0xFF43A047),
                icon = Icons.Default.TipsAndUpdates,
                iconColor = Color(0xFF43A047),
                iconBackground = Color(0xFFE8F5E9),
                savingAmount = totalExpense * 0.1
            )
        )
    }

    return insights
}

fun calculateSavingsPotential(totalExpense: Double): Double {
    return totalExpense * 0.25 // Assume 25% saving potential
}