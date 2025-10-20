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
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.viewmodel.InsightsUiState
import com.gis.smartfinance.ui.viewmodel.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.insightsState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("AI Insights", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
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
        item {
            DataOverviewCard(
                transactionCount = analysis.insights.size,
                daysOfData = analysis.daysOfData,
                dataQuality = analysis.dataQuality
            )
        }

        if (analysis.daysOfData >= InsightsConstants.MIN_DAYS_FOR_BASIC_INSIGHTS) {
            item {
                FinancialHealthCard(
                    healthScore = analysis.healthScore,
                    scoreBreakdown = analysis.scoreBreakdown,
                    explanation = analysis.scoreExplanation
                )
            }
        }

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

        if (analysis.spendingPatterns.isNotEmpty()) {
            item {
                SpendingPatternsCard(patterns = analysis.spendingPatterns)
            }
        }

        items(analysis.insights) { insight ->
            InsightCard(insight = insight)
        }

        if (analysis.recommendations.isNotEmpty()) {
            item {
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
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
            CircularProgressIndicator(color = AppColors.Purple)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Analyzing your finances...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Data Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add transactions to get personalized insights",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                tint = AppColors.Error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}