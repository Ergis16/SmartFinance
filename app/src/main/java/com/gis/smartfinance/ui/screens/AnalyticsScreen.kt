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
import com.gis.smartfinance.ui.viewmodel.AnalyticsViewModel

/**
 * Analytics Screen - Shows spending breakdown and visualizations
 *
 * Now uses ViewModel for proper MVVM architecture
 * All data comes from Room database through Repository
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    // Collect states from ViewModel
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Total Income",
                        amount = totalIncome,
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF43A047),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Total Expenses",
                        amount = totalExpense,
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFE53935),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pie Chart for Expenses
            if (expensesByCategory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Expense Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E)
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

                // Category Details List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Category Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            expensesByCategory.entries.forEach { (category, amount) ->
                                CategoryRow(
                                    category = category,
                                    amount = amount,
                                    percentage = if (totalExpense > 0) {
                                        (amount / totalExpense * 100).toFloat()
                                    } else 0f
                                )
                                if (category != expensesByCategory.keys.last()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty state
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
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
                                Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFBDBDBD)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No data to display",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF757575)
                            )
                            Text(
                                "Add some transactions to see analytics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Summary Card Component
 * Shows total income or expense with icon
 */
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                color = Color(0xFF757575)
            )
            Text(
                "€${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Pie Chart Component
 * Draws circular chart with category segments
 */
@Composable
fun PieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    // Color palette for categories
    val colors = listOf(
        Color(0xFF6C63FF),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFA726),
        Color(0xFF66BB6A),
        Color(0xFFAB47BC),
        Color(0xFF29B6F6),
        Color(0xFFFF7043)
    )

    val total = data.values.sum()
    if (total == 0.0) return

    var startAngle = -90f // Start from top

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

/**
 * Category Legend Component
 * Shows color-coded list of categories
 */
@Composable
fun CategoryLegend(data: Map<String, Double>) {
    val colors = listOf(
        Color(0xFF6C63FF),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFA726),
        Color(0xFF66BB6A),
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
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "€${String.format("%.2f", entry.value)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Category Row Component
 * Shows category with progress bar
 */
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
                color = Color(0xFF1A1A2E)
            )
            Text(
                "€${String.format("%.2f", amount)} (${String.format("%.1f", percentage)}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (percentage / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF6C63FF),
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

/**
 * WHAT WAS FIXED:
 *
 * CRITICAL BUG:
 * - Before: Text("Text(\"€ \", fontWeight = FontWeight.Bold) }")
 * - After: Text("€${String.format("%.2f", amount)}")
 *
 * ARCHITECTURE:
 * - Before: Manual data fetching from TransactionManager
 * - After: Clean MVVM with ViewModel
 *
 * PERFORMANCE:
 * - Before: Loading all transactions in UI
 * - After: Efficient database aggregations
 *
 * TYPE SAFETY:
 * - Before: Map operations in UI layer
 * - After: Repository handles data transformation
 */