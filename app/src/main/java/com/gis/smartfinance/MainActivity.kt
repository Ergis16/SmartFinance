package com.gis.smartfinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gis.smartfinance.data.ThemeManager
import com.gis.smartfinance.data.ThemeMode
import com.gis.smartfinance.ui.navigation.Screen
import com.gis.smartfinance.ui.screens.*
import com.gis.smartfinance.ui.theme.AppColors
import com.gis.smartfinance.ui.theme.SmartFinanceTheme
import com.gis.smartfinance.ui.viewmodel.CurrencyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val systemInDarkTheme = isSystemInDarkTheme()

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }

            SmartFinanceTheme(darkTheme = useDarkTheme) {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500)
                    showSplash = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        SmartFinanceApp()
                    }

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
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6C63FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

    // ✅ Currency ViewModel for first launch detection
    val currencyViewModel: CurrencyViewModel = hiltViewModel()
    val isFirstLaunch by currencyViewModel.isFirstLaunch.collectAsState()
    val autoDetectedCurrency = currencyViewModel.autoDetectedCurrency

    var showFirstLaunchDialog by remember { mutableStateOf(false) }

    // ✅ Show currency dialog on first launch (after splash)
    LaunchedEffect(isFirstLaunch) {
        if (isFirstLaunch) {
            delay(1000) // Wait 1 second after splash finishes
            showFirstLaunchDialog = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
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
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    onNavigateToInsights = {
                        navController.navigate(Screen.Insights.route)
                    },
                    onNavigateToAnalytics = {
                        navController.navigate(Screen.Analytics.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCurrency = {
                        navController.navigate("currency_settings")
                    }
                )
            }

            // ✅ Currency Settings Route
            composable("currency_settings") {
                com.gis.smartfinance.ui.screens.settings.CurrencySettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    // ✅ First Launch Currency Selection Dialog
    if (showFirstLaunchDialog) {
        AlertDialog(
            onDismissRequest = { /* Can't dismiss without choosing */ },
            icon = {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    tint = AppColors.Purple,
                    modifier = Modifier.size(64.dp)
                )
            },
            title = {
                Text(
                    "Welcome to SmartFinance!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "We detected your currency based on your location:",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ✅ Detected Currency Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                autoDetectedCurrency.flag,
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    autoDetectedCurrency.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${autoDetectedCurrency.code} (${autoDetectedCurrency.symbol})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "You can change this later in Settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ✅ Accept Button
                    Button(
                        onClick = {
                            currencyViewModel.useAutoDetectedCurrency()
                            showFirstLaunchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Purple
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use This Currency")
                    }

                    // ✅ Choose Different Button
                    OutlinedButton(
                        onClick = {
                            navController.navigate("currency_settings")
                            showFirstLaunchDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Purple
                        )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Different")
                    }
                }
            }
        )
    }
}