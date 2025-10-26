package com.gis.smartfinance.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gis.smartfinance.ui.theme.AppColors

/**
 * ✅ ADDED #14: Beautiful empty state with animation
 * Reusable across different screens
 */
@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    illustration: @Composable (() -> Unit)? = null
) {
    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom illustration or default icon
            if (illustration != null) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    illustration()
                }
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Optional action button
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Purple
                    )
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

/**
 * ✅ ADDED: Empty wallet illustration
 */
@Composable
fun EmptyWalletIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "wallet")
    val coinFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_float"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val walletWidth = size.width * 0.6f
        val walletHeight = size.height * 0.5f
        val walletLeft = (size.width - walletWidth) / 2
        val walletTop = size.height * 0.3f

        // Wallet body
        val walletPath = Path().apply {
            moveTo(walletLeft, walletTop)
            lineTo(walletLeft + walletWidth, walletTop)
            lineTo(walletLeft + walletWidth, walletTop + walletHeight)
            lineTo(walletLeft, walletTop + walletHeight)
            close()
        }

        drawPath(
            path = walletPath,
            color = AppColors.Purple.copy(alpha = 0.3f)
        )

        drawPath(
            path = walletPath,
            color = AppColors.Purple,
            style = Stroke(width = 4f)
        )

        // Wallet flap
        val flapPath = Path().apply {
            moveTo(walletLeft, walletTop)
            lineTo(walletLeft + walletWidth, walletTop)
            lineTo(walletLeft + walletWidth, walletTop + walletHeight * 0.3f)
            lineTo(walletLeft, walletTop + walletHeight * 0.3f)
            close()
        }

        drawPath(
            path = flapPath,
            color = AppColors.PurpleDark.copy(alpha = 0.5f)
        )

        // Floating coin (to show it's empty)
        val coinX = size.width * 0.7f
        val coinY = walletTop - 30f + coinFloat

        drawCircle(
            color = AppColors.Warning,
            radius = 15f,
            center = Offset(coinX, coinY)
        )

        drawCircle(
            color = AppColors.Warning,
            radius = 15f,
            center = Offset(coinX, coinY),
            style = Stroke(width = 2f)
        )
    }
}

/**
 * ✅ ADDED: Empty chart illustration
 */
@Composable
fun EmptyChartIllustration() {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // ✅ FIX: Get color in Composable context

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val barWidth = size.width / 5
        val maxHeight = size.height * 0.6f
        val baseY = size.height * 0.8f

        // Draw empty bars
        for (i in 0 until 4) {
            val x = (i + 0.5f) * barWidth
            val height = maxHeight * (0.2f + i * 0.1f)

            drawRect(
                color = AppColors.Purple.copy(alpha = 0.2f),
                topLeft = Offset(x - barWidth * 0.3f, baseY - height),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, height)
            )
        }

        // Draw axes
        drawLine(
            color = lineColor, // ✅ FIX: Use pre-fetched color
            start = Offset(barWidth * 0.2f, baseY),
            end = Offset(size.width - barWidth * 0.2f, baseY),
            strokeWidth = 2f
        )

        drawLine(
            color = lineColor, // ✅ FIX: Use pre-fetched color
            start = Offset(barWidth * 0.2f, size.height * 0.2f),
            end = Offset(barWidth * 0.2f, baseY),
            strokeWidth = 2f
        )
    }
}

/**
 * ✅ ADDED: Light bulb illustration for insights
 */
@Composable
fun EmptyInsightsIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "bulb")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val bulbRadius = size.minDimension * 0.3f

        // Glow effect
        drawCircle(
            color = AppColors.Warning.copy(alpha = glow * 0.3f),
            radius = bulbRadius * 1.5f,
            center = Offset(centerX, centerY)
        )

        // Bulb
        drawCircle(
            color = AppColors.Warning.copy(alpha = 0.3f),
            radius = bulbRadius,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            color = AppColors.Warning,
            radius = bulbRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )

        // Filament
        val filamentPath = Path().apply {
            moveTo(centerX - bulbRadius * 0.3f, centerY - bulbRadius * 0.2f)
            lineTo(centerX, centerY + bulbRadius * 0.2f)
            lineTo(centerX + bulbRadius * 0.3f, centerY - bulbRadius * 0.2f)
        }

        drawPath(
            path = filamentPath,
            color = AppColors.Warning,
            style = Stroke(width = 3f)
        )
    }
}

/**
 * ✅ USAGE EXAMPLES:
 */

// In HomeScreen.kt:
@Composable
fun EmptyTransactionsState() {
    EmptyStateCard(
        title = "No transactions yet",
        description = "Start tracking your finances by adding your first transaction",
        icon = Icons.Default.AccountBalanceWallet,
        illustration = { EmptyWalletIllustration() }
    )
}

// In AnalyticsScreen.kt:
@Composable
fun EmptyAnalyticsState() {
    EmptyStateCard(
        title = "No data to analyze",
        description = "Add some transactions to see beautiful charts and insights",
        icon = Icons.Default.BarChart,
        illustration = { EmptyChartIllustration() }
    )
}

// In InsightsScreen.kt:
@Composable
fun EmptyInsightsState() {
    EmptyStateCard(
        title = "Building Your Profile",
        description = "Add at least 3 days of transactions to unlock AI-powered insights",
        icon = Icons.Default.Psychology,
        illustration = { EmptyInsightsIllustration() }
    )
}