package com.gis.smartfinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gis.smartfinance.ui.screens.*
import com.gis.smartfinance.ui.theme.SmartFinanceTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFinanceTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500) // Show splash for 2.5 seconds
                    showSplash = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main App Content
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        SmartFinanceApp()
                    }

                    // Splash Screen
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        SplashScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo scale
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Animate alpha
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF6C63FF)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF6C63FF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                "SmartFinance",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
            )

            Text(
                "Track • Analyze • Save",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { this.alpha = alpha.value }
            )
        }
    }
}

@Composable
fun SmartFinanceApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate("add_transaction")
                    },
                    onNavigateToInsights = {
                        navController.navigate("insights")
                    },
                    onNavigateToAnalytics = {
                        navController.navigate("analytics")
                    }
                )
            }

            composable("add_transaction") {
                AddTransactionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("insights") {
                InsightsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("analytics") {
                AnalyticsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}