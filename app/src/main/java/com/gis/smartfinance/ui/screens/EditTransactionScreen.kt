package com.gis.smartfinance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.ui.viewmodel.EditTransactionViewModel
import kotlinx.coroutines.launch
import com.gis.smartfinance.ui.theme.AppColors

/**
 * Screen for editing existing transaction
 * Similar to AddTransactionScreen but pre-filled with data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load transaction when screen opens
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    val expenseCategories = listOf(
        "Food & Dining" to Icons.Default.Restaurant,
        "Transport" to Icons.Default.DirectionsCar,
        "Shopping" to Icons.Default.ShoppingBag,
        "Entertainment" to Icons.Default.MovieFilter,
        "Bills" to Icons.Default.Receipt,
        "Healthcare" to Icons.Default.LocalHospital,
        "Education" to Icons.Default.School,
        "Other" to Icons.Default.MoreHoriz
    )

    val incomeCategories = listOf(
        "Salary" to Icons.Default.AccountBalance,
        "Freelance" to Icons.Default.Computer,
        "Investment" to Icons.Default.TrendingUp,
        "Gift" to Icons.Default.CardGiftcard,
        "Other" to Icons.Default.AttachMoney
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction", fontWeight = FontWeight.Bold) },
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
        // Show loading while fetching transaction
        if (uiState.isLoading && uiState.amount.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6C63FF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Transaction Type Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Transaction Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = uiState.type == TransactionType.EXPENSE,
                                onClick = { viewModel.updateType(TransactionType.EXPENSE) },
                                label = {
                                    Text(
                                        "Expense",
                                        fontWeight = if (uiState.type == TransactionType.EXPENSE)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEBEE),
                                    selectedLabelColor = Color(0xFFE53935)
                                )
                            )

                            FilterChip(
                                selected = uiState.type == TransactionType.INCOME,
                                onClick = { viewModel.updateType(TransactionType.INCOME) },
                                label = {
                                    Text(
                                        "Income",
                                        fontWeight = if (uiState.type == TransactionType.INCOME)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8F5E9),
                                    selectedLabelColor = Color(0xFF43A047)
                                )
                            )
                        }
                    }
                }

                // Amount Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Amount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.amount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    viewModel.updateAmount(newValue)
                                }
                            },
                            label = { Text("Enter amount") },
                            prefix = { Text("Lek ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6C63FF),
                                focusedLabelColor = Color(0xFF6C63FF)
                            )
                        )
                    }
                }

                // Description Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("What was this for?") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6C63FF),
                                focusedLabelColor = Color(0xFF6C63FF)
                            )
                        )
                    }
                }

                // Category Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val categories = if (uiState.type == TransactionType.EXPENSE)
                            expenseCategories else incomeCategories

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { (category, icon) ->
                                        FilterChip(
                                            selected = uiState.category == category,
                                            onClick = { viewModel.updateCategory(category) },
                                            label = {
                                                Text(
                                                    category,
                                                    fontWeight = if (uiState.category == category)
                                                        FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = if (uiState.type == TransactionType.EXPENSE)
                                                    Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                                selectedLabelColor = if (uiState.type == TransactionType.EXPENSE)
                                                    Color(0xFFE53935) else Color(0xFF43A047)
                                            )
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        viewModel.saveTransaction(
                            onSuccess = {
                                onNavigateBack()
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = error,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}