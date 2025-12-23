package com.gis.smartfinance.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.data.model.RecurringFrequency
import com.gis.smartfinance.data.model.RecurringTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.data.Currency
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.theme.*
import com.gis.smartfinance.ui.components.ThemedIconBackground
import com.gis.smartfinance.ui.viewmodel.RecurringTransactionViewModel
import com.gis.smartfinance.ui.viewmodel.CurrencyViewModel
import com.gis.smartfinance.utils.formatWithCurrency
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel(),
    currencyViewModel: CurrencyViewModel = hiltViewModel()
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val currency by currencyViewModel.selectedCurrency.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recurring Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Transaction")
            }
        }
    ) { paddingValues ->
        if (recurringTransactions.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Illustration with themed icon background
                    ThemedIconBackground(
                        icon = Icons.Default.Repeat,
                        contentDescription = null,
                        baseColor = MaterialTheme.colorScheme.primary,
                        size = 140.dp,
                        iconSize = 70.dp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "No Recurring Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Set up automatic transactions like salary, subscriptions, or bills that repeat regularly",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hint about FAB button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Purple.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Tap the + button to create your first recurring transaction",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // List of recurring transactions
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recurringTransactions, key = { it.id }) { recurring ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                showDeleteDialog = recurring.id
                            }
                            false  // Don't actually dismiss, show dialog instead
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.Error)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        RecurringTransactionCard(
                            recurring = recurring,
                            currency = currency,
                            onClick = { onNavigateToEdit(recurring.id) },
                            onDelete = { showDeleteDialog = recurring.id }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Stop Recurring Transaction?") },
            text = {
                Text(
                    "This will stop future automatic transactions. " +
                    "Past transactions will remain in your history."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecurringTransaction(
                            id = id,
                            onSuccess = {
                                showDeleteDialog = null
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Recurring transaction stopped",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onError = { }
                        )
                    }
                ) {
                    Text("Stop", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionCard(
    recurring: RecurringTransaction,
    currency: Currency,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recurring.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${if (recurring.type == TransactionType.EXPENSE) "-" else "+"}${recurring.amount.formatWithCurrency(currency)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (recurring.type == TransactionType.EXPENSE) AppColors.Error else AppColors.Success
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AppColors.Error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frequency badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        getFrequencyText(recurring),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Next occurrence
                Text(
                    "Next: ${formatRecurringListDate(recurring.nextOccurrenceDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Generation count
            if (recurring.generatedCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Generated ${recurring.generatedCount} time${if (recurring.generatedCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getFrequencyText(recurring: RecurringTransaction): String {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return when (recurring.frequencyType) {
        RecurringFrequency.DAILY -> if (recurring.frequencyValue == 1) "Daily" else "Every ${recurring.frequencyValue} days"
        RecurringFrequency.WEEKLY -> if (recurring.frequencyValue == 1) {
            recurring.dayOfWeek?.let { "Weekly on ${daysOfWeek[it - 1]}" } ?: "Weekly"
        } else {
            "Every ${recurring.frequencyValue} weeks"
        }
        RecurringFrequency.MONTHLY -> if (recurring.frequencyValue == 1) {
            recurring.dayOfMonth?.let { "Monthly on day $it" } ?: "Monthly"
        } else {
            "Every ${recurring.frequencyValue} months"
        }
        RecurringFrequency.CUSTOM -> "Every ${recurring.frequencyValue} days"
    }
}

private fun formatRecurringListDate(date: Date): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(date)
}
