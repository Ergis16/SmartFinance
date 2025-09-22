package com.gis.smartfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gis.smartfinance.data.PersistentTransactionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // GET CONTEXT AND MANAGER
    val context = LocalContext.current
    val transactionManager = remember { PersistentTransactionManager.getInstance(context) }

    // CREATE COROUTINE SCOPE
    val scope = rememberCoroutineScope()

    // STATE VARIABLES
    var showClearDialog by remember { mutableStateOf(false) }
    var isClearing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Data Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear All Data
                    SettingItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Data",
                        subtitle = "Remove all transactions and start fresh",
                        onClick = { showClearDialog = true },
                        iconColor = Color(0xFFE53935),
                        enabled = !isClearing
                    )
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0",
                        onClick = { },
                        iconColor = Color(0xFF6C63FF),
                        enabled = true
                    )
                }
            }
        }
    }

    // CLEAR DATA CONFIRMATION DIALOG
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isClearing) showClearDialog = false
            },
            title = { Text("Clear All Data?") },
            text = { Text("This will remove all your transactions. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isClearing = true

                        // LAUNCH COROUTINE TO CLEAR DATA
                        scope.launch {
                            try {
                                // THIS IS THE SUSPEND FUNCTION CALL
                                transactionManager.clearAll()
                                showClearDialog = false
                            } catch (e: Exception) {
                                // Handle error if needed
                            } finally {
                                isClearing = false
                            }
                        }
                    },
                    enabled = !isClearing
                ) {
                    if (isClearing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Clear", color = Color(0xFFE53935))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    enabled = !isClearing
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) iconColor else iconColor.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(alpha = 0.5f)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) Color(0xFF757575) else Color(0xFF757575).copy(alpha = 0.5f)
                )
            }
        }
    }
}