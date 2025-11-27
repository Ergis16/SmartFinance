package com.gis.smartfinance.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gis.smartfinance.data.Currency
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.viewmodel.CurrencyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredCurrencies by viewModel.filteredCurrencies.collectAsState()
    val autoDetectedCurrency = viewModel.autoDetectedCurrency

    // ✅ Track pending selection (not saved yet)
    var pendingCurrency by remember { mutableStateOf<Currency?>(null) }
    val hasUnsavedChanges = pendingCurrency != null && pendingCurrency != selectedCurrency

    var showAutoDetectDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Currency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Changes not saved. Click 'Save' to confirm.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
        // ✅ Confirmation button at bottom
        bottomBar = {
            AnimatedVisibility(
                visible = hasUnsavedChanges,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pendingCurrency = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                pendingCurrency?.let { currency ->
                                    scope.launch {
                                        viewModel.selectCurrency(currency)
                                        pendingCurrency = null
                                        snackbarHostState.showSnackbar(
                                            "Currency changed to ${currency.name}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Purple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val displayCurrency = pendingCurrency ?: selectedCurrency

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasUnsavedChanges)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        AppColors.Purple
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (hasUnsavedChanges) "Selected Currency (Not Saved)" else "Current Currency",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (hasUnsavedChanges)
                            MaterialTheme.colorScheme.onSurface
                        else
                            Color.White.copy(alpha = 0.9f)
                    )

                    if (hasUnsavedChanges) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Click 'Save Changes' to confirm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            displayCurrency.flag,
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                displayCurrency.code,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (hasUnsavedChanges)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    Color.White
                            )
                            Text(
                                displayCurrency.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasUnsavedChanges)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            displayCurrency.symbol,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (hasUnsavedChanges)
                                MaterialTheme.colorScheme.onSurface
                            else
                                Color.White
                        )
                    }
                }
            }

            if (autoDetectedCurrency.code != selectedCurrency.code) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showAutoDetectDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto-Detected Currency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${autoDetectedCurrency.flag} ${autoDetectedCurrency.name} (${autoDetectedCurrency.code})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap to use this currency",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search currencies...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Purple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = if (hasUnsavedChanges) 80.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCurrencies) { currency ->
                    CurrencyItem(
                        currency = currency,
                        isSelected = currency.code == selectedCurrency.code,
                        isPending = currency.code == pendingCurrency?.code,
                        onClick = {
                            pendingCurrency = currency
                        }
                    )
                }
            }
        }
    }

    if (showAutoDetectDialog) {
        AlertDialog(
            onDismissRequest = { showAutoDetectDialog = false },
            icon = {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    tint = AppColors.Purple,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Use Auto-Detected Currency?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Based on your device settings, we detected:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            autoDetectedCurrency.flag,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                autoDetectedCurrency.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${autoDetectedCurrency.code} (${autoDetectedCurrency.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.useAutoDetectedCurrency()
                            showAutoDetectDialog = false
                            snackbarHostState.showSnackbar(
                                "Currency changed to ${autoDetectedCurrency.name}"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Purple
                    )
                ) {
                    Text("Use This Currency")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoDetectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    isPending: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPending -> MaterialTheme.colorScheme.primaryContainer
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isPending -> 6.dp
                isSelected -> 4.dp
                else -> 2.dp
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                currency.flag,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    currency.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected || isPending) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isPending -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    currency.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                currency.symbol,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isPending -> MaterialTheme.colorScheme.primary
                    isSelected -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(12.dp))
            when {
                isPending -> {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Pending",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
                isSelected -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Current",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}