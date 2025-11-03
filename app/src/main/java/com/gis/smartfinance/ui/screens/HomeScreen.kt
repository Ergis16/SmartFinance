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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.viewmodel.HomeViewModel
import com.gis.smartfinance.ui.viewmodel.HomeUiState
import com.gis.smartfinance.ui.viewmodel.CurrencyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    currencyViewModel: CurrencyViewModel = hiltViewModel() // ✅ ADDED
) {
    val uiState by viewModel.uiState.collectAsState()
    val currency by currencyViewModel.selectedCurrency.collectAsState() // ✅ ADDED
    var selectedTransaction by remember { mutableStateOf<FinancialTransaction?>(null) }
    var showDetailsSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartFinance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = AppColors.Purple,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.Purple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ModernBalanceCardFixed(
                            totalIncome = state.totalIncome,
                            totalExpense = state.totalExpense,
                            balance = state.balance,
                            currency = currency // ✅ PASS CURRENCY
                        )
                    }

                    item {
                        ModernQuickActions(
                            onNavigateToInsights = onNavigateToInsights,
                            onNavigateToAnalytics = onNavigateToAnalytics
                        )
                    }

                    item {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (state.recentTransactions.isEmpty()) {
                        item { EmptyStateCard() }
                    } else {
                        items(
                            items = state.recentTransactions,
                            key = { it.id }
                        ) { transaction ->
                            AnimatedTransactionItem(
                                transaction = transaction,
                                currency = currency, // ✅ PASS CURRENCY
                                onClick = {
                                    selectedTransaction = transaction
                                    showDetailsSheet = true
                                }
                            )
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Something went wrong",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showDetailsSheet && selectedTransaction != null) {
        TransactionDetailsSheet(
            transaction = selectedTransaction!!,
            currency = currency, // ✅ PASS CURRENCY
            onDismiss = {
                showDetailsSheet = false
                selectedTransaction = null
            },
            onSave = { updatedTransaction ->
                viewModel.updateTransaction(updatedTransaction)
                showDetailsSheet = false
                selectedTransaction = null
            },
            onDelete = {
                viewModel.deleteTransaction(selectedTransaction!!)
                showDetailsSheet = false
                selectedTransaction = null
            }
        )
    }
}

@Composable
fun ModernBalanceCardFixed(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    currency: com.gis.smartfinance.data.Currency // ✅ ADDED PARAMETER
) {
    val isDark = MaterialTheme.colorScheme.background == AppColors.DarkBackground

    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF4F47B8),
            Color(0xFF3D2F9F)
        )
    } else {
        listOf(
            AppColors.Purple,
            AppColors.PurpleDark
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    AutoSizeText(
                        text = "${currency.symbol} ${String.format("%,.2f", balance)}", // ✅ USES CURRENCY
                        maxLines = 1,
                        minFontSize = 20.sp,
                        maxFontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Income",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        AutoSizeText(
                            text = "${currency.symbol} ${String.format("%,.2f", totalIncome)}", // ✅ USES CURRENCY
                            maxLines = 1,
                            minFontSize = 12.sp,
                            maxFontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        AutoSizeText(
                            text = "${currency.symbol} ${String.format("%,.2f", totalExpense)}", // ✅ USES CURRENCY
                            maxLines = 1,
                            minFontSize = 12.sp,
                            maxFontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    minFontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    maxFontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = 1,
    textAlign: TextAlign? = null
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Visible,
        textAlign = textAlign,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth || textLayoutResult.didOverflowHeight) {
                val nextFontSize = fontSize * 0.9f
                if (nextFontSize >= minFontSize) {
                    fontSize = nextFontSize
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
        style = if (readyToDraw) {
            LocalTextStyle.current
        } else {
            LocalTextStyle.current.copy(color = Color.Transparent)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernQuickActions(
    onNavigateToInsights: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == AppColors.DarkBackground

    val insightsBg = if (isDark) Color(0xFF4A3800) else AppColors.WarningLight
    val insightsIcon = if (isDark) Color(0xFFFFB74D) else AppColors.Warning
    val analyticsBg = if (isDark) Color(0xFF1B5E20) else AppColors.SuccessLight
    val analyticsIcon = if (isDark) Color(0xFF66BB6A) else AppColors.Success

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            onClick = onNavigateToInsights,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(insightsBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Insights",
                        tint = insightsIcon,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Insights",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            onClick = onNavigateToAnalytics,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(analyticsBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "Analytics",
                        tint = analyticsIcon,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Analytics",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTransactionItem(
    transaction: FinancialTransaction,
    currency: com.gis.smartfinance.data.Currency, // ✅ ADDED PARAMETER
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (transaction.type) {
                                    TransactionType.EXPENSE ->
                                        if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                            AppColors.ErrorDarkBg
                                        } else {
                                            AppColors.ErrorLight
                                        }
                                    TransactionType.INCOME ->
                                        if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                            AppColors.SuccessDarkBg
                                        } else {
                                            AppColors.SuccessLight
                                        }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (transaction.category) {
                                "Food & Dining" -> Icons.Default.Restaurant
                                "Transport" -> Icons.Default.DirectionsCar
                                "Shopping" -> Icons.Default.ShoppingBag
                                "Salary" -> Icons.Default.AccountBalance
                                "Freelance" -> Icons.Default.Computer
                                "Investment" -> Icons.Default.TrendingUp
                                else -> Icons.Default.AttachMoney
                            },
                            contentDescription = null,
                            tint = when (transaction.type) {
                                TransactionType.EXPENSE ->
                                    if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                        AppColors.ErrorDark
                                    } else {
                                        AppColors.Error
                                    }
                                TransactionType.INCOME ->
                                    if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                        AppColors.SuccessDark
                                    } else {
                                        AppColors.Success
                                    }
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${transaction.category} • ${formatDate(transaction.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${
                        String.format("%.2f", transaction.amount)
                    } ${currency.symbol}", // ✅ USES CURRENCY
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (transaction.type) {
                        TransactionType.EXPENSE ->
                            if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                AppColors.ErrorDark
                            } else {
                                AppColors.Error
                            }
                        TransactionType.INCOME ->
                            if (MaterialTheme.colorScheme.background == AppColors.DarkBackground) {
                                AppColors.SuccessDark
                            } else {
                                AppColors.Success
                            }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No transactions yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap the + button to add your first transaction",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}