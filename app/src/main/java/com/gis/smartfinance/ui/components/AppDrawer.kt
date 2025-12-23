package com.gis.smartfinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gis.smartfinance.ui.theme.AppColors

/**
 * Hamburger menu drawer component
 * Provides navigation to recurring transactions and future features
 */
@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigateToRecurringTransactions: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp * 0.80f).dp

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(drawerWidth)
    ) {
        // Compact purple header with icon and name in one line
        // Compact purple header with gradient background
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
                Text(
                    "SmartFin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recurring Transactions menu item
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Repeat, contentDescription = null) },
            label = { Text("Recurring Transactions") },
            selected = currentRoute == "recurring_transactions",
            onClick = {
                onNavigateToRecurringTransactions()
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Space for future menu items
        // NavigationDrawerItem for Reports, Budgets, etc.
    }
}
