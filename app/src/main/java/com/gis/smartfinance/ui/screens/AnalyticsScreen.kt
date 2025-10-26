package com.gis.smartfinance.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.ui.components.EmptyChartIllustration
import com.gis.smartfinance.ui.components.EmptyStateCard
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    // ✅ ADDED: Loading state
    val isLoading by remember {
        derivedStateOf {
            expensesByCategory.isEmpty() && totalExpense == 0.0
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        // ✅ ADDED: Show loading or content
        if (isLoading) {
            LoadingAnalytics(Modifier.padding(paddingValues))
        } else if (expensesByCategory.isEmpty()) {
            // ✅ ADDED: Beautiful empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "No data to analyze",
                    description = "Add some transactions to see beautiful charts and insights",
                    icon = Icons.Default.BarChart,
                    illustration = { EmptyChartIllustration() }
                )
            }
        } else {
            AnalyticsContent(
                expensesByCategory = expensesByCategory,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * ✅ ADDED: Separate composable to limit recomposition scope
 */
@Composable
private fun LoadingAnalytics(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = AppColors.Purple)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Calculating analytics...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ✅ EXTRACTED: Content composable
 */
@Composable
private fun AnalyticsContent(
    expensesByCategory: Map<String, Double>,
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        item(key = "summary") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Income",
                    amount = totalIncome,
                    icon = Icons.Default.TrendingUp,
                    color = AppColors.Success,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Expenses",
                    amount = totalExpense,
                    icon = Icons.Default.TrendingDown,
                    color = AppColors.Error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Net Balance Card
        item(key = "net_balance") {
            val netBalance = totalIncome - totalExpense
            val isPositive = netBalance >= 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPositive)
                        AppColors.SuccessLight
                    else
                        AppColors.ErrorLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Net Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${if (isPositive) "+" else ""}${String.format("%.2f", netBalance)} Lek",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) AppColors.Success else AppColors.Error
                        )
                    }
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isPositive) AppColors.Success else AppColors.Error
                    )
                }
            }
        }

        // Pie Chart
        item(key = "pie_chart") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PieChart(
                        data = expensesByCategory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryLegend(data = expensesByCategory)
                }
            }
        }

        // Category Details
        item(key = "category_details") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Category Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    expensesByCategory.entries.forEachIndexed { index, (category, amount) ->
                        CategoryRow(
                            category = category,
                            amount = amount,
                            percentage = if (totalExpense > 0) {
                                (amount / totalExpense * 100).toFloat()
                            } else 0f
                        )
                        if (index != expensesByCategory.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // ✅ ADDED: Insights based on data
        if (totalExpense > 0) {
            item(key = "quick_insights") {
                QuickInsightsCard(
                    expensesByCategory = expensesByCategory,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome
                )
            }
        }
    }
}

/**
 * ✅ ADDED: Quick insights card
 */
@Composable
private fun QuickInsightsCard(
    expensesByCategory: Map<String, Double>,
    totalExpense: Double,
    totalIncome: Double
) {
    val topCategory = expensesByCategory.maxByOrNull { it.value }
    val savingsRate = if (totalIncome > 0) {
        ((totalIncome - totalExpense) / totalIncome * 100).toInt()
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.InfoLight
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = AppColors.Info,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Quick Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (topCategory != null) {
                InsightRow(
                    icon = Icons.Default.TrendingUp,
                    text = "Your biggest expense category is ${topCategory.key} (${String.format("%.0f", topCategory.value / totalExpense * 100)}%)"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InsightRow(
                icon = if (savingsRate >= 20) Icons.Default.CheckCircle else Icons.Default.Warning,
                text = when {
                    savingsRate >= 20 -> "Great job! You're saving $savingsRate% of your income"
                    savingsRate >= 10 -> "You're saving $savingsRate%. Try to reach 20%"
                    savingsRate > 0 -> "Low savings rate ($savingsRate%). Consider reducing expenses"
                    else -> "Spending exceeds income. Review your budget"
                },
                color = when {
                    savingsRate >= 20 -> AppColors.Success
                    savingsRate >= 10 -> AppColors.Warning
                    else -> AppColors.Error
                }
            )
        }
    }
}

@Composable
private fun InsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${String.format("%.2f", amount)} Lek",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        AppColors.Purple,
        AppColors.Error,
        Color(0xFF4ECDC4),
        AppColors.Warning,
        AppColors.Success,
        Color(0xFFAB47BC),
        Color(0xFF29B6F6),
        Color(0xFFFF7043)
    )

    val total = data.values.sum()
    if (total == 0.0) return

    var startAngle = -90f

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total * 360).toFloat()
            val color = colors[index % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryLegend(data: Map<String, Double>) {
    val colors = listOf(
        AppColors.Purple,
        AppColors.Error,
        Color(0xFF4ECDC4),
        AppColors.Warning,
        AppColors.Success,
        Color(0xFFAB47BC),
        Color(0xFF29B6F6),
        Color(0xFFFF7043)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.entries.forEachIndexed { index, entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            colors[index % colors.size],
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    entry.key,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${String.format("%.2f", entry.value)} Lek",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    category: String,
    amount: Double,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${String.format("%.2f", amount)} (${String.format("%.1f", percentage)}%) Lek",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Purple
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AppColors.Purple,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}