package com.gis.smartfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.domain.insights.*
import com.gis.smartfinance.ui.screens.insights.*
import com.gis.smartfinance.ui.viewmodel.InsightsUiState
import com.gis.smartfinance.ui.viewmodel.InsightsViewModel

/**
 * Insights Screen - FULLY FIXED VERSION
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.insightsState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("AI Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A2E)
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InsightsUiState.Loading -> {
                LoadingInsightsState(Modifier.padding(paddingValues))
            }

            is InsightsUiState.Empty -> {
                EmptyInsightsState(Modifier.padding(paddingValues))
            }

            is InsightsUiState.Success -> {
                InsightsContent(
                    analysis = state.analysis,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is InsightsUiState.Error -> {
                ErrorInsightsState(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun InsightsContent(
    analysis: InsightsAnalysis,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Data overview
        item {
            DataOverviewCard(
                transactionCount = analysis.insights.size,
                daysOfData = analysis.daysOfData,
                dataQuality = analysis.dataQuality
            )
        }

        // Health score
        if (analysis.daysOfData >= InsightsConstants.MIN_DAYS_FOR_BASIC_INSIGHTS) {
            item {
                FinancialHealthCard(
                    healthScore = analysis.healthScore,
                    scoreBreakdown = analysis.scoreBreakdown,
                    explanation = analysis.scoreExplanation
                )
            }
        }

        // Savings potential
        if (analysis.insights.isNotEmpty()) {
            item {
                InsightsSummaryCard(
                    totalSavingsPotential = analysis.savingsPotential,
                    insightsCount = analysis.insights.size,
                    urgentInsights = analysis.insights.count {
                        it.priority == InsightPriority.URGENT
                    }
                )
            }
        }

        // Spending patterns
        if (analysis.spendingPatterns.isNotEmpty()) {
            item {
                SpendingPatternsCard(patterns = analysis.spendingPatterns)
            }
        }

        // Individual insights
        items(analysis.insights) { insight ->
            InsightCard(insight = insight)
        }

        // Recommendations
        if (analysis.recommendations.isNotEmpty()) {
            item {
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(analysis.recommendations) { recommendation ->
                RecommendationCard(recommendation = recommendation)
            }
        }
    }
}

@Composable
private fun LoadingInsightsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFF6C63FF))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Analyzing your finances...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun EmptyInsightsState(modifier: Modifier = Modifier) {
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
                "No Data Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add transactions to get personalized insights",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorInsightsState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFE53935)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }
    }
}