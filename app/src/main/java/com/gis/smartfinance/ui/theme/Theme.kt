package com.gis.smartfinance.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * ✅ FIXED: Complete dark mode overhaul
 * - True dark backgrounds (no light pixels)
 * - Muted colors in dark mode
 * - Pure white in light mode
 * - Consistent across all screens
 */
object AppColors {
    // ==================== LIGHT MODE COLORS ====================

    // Primary Purple (Light Mode) - UNCHANGED
    val Purple = Color(0xFF6C63FF)
    val PurpleLight = Color(0xFF8B83FF)
    val PurpleDark = Color(0xFF4834DF)

    // Backgrounds (Light Mode) - UNCHANGED
    val LightBackground = Color(0xFFF5F7FA)

    // ==================== DARK MODE COLORS ====================

    // ✅ NEW: Much darker, muted purple for dark mode
    val PurpleDarkMode = Color(0xFF6B63E8) // Softer purple
    val PurpleLightDarkMode = Color(0xFF8A82F5)
    val PurpleDarkDarkMode = Color(0xFF4F47B8)

    // ✅ NEW: TRUE DARK backgrounds (almost black)
    val DarkBackground = Color(0xFF000000) // Pure black background
    val DarkSurface = Color(0xFF121212) // Very dark gray for cards
    val DarkSurfaceVariant = Color(0xFF1E1E1E) // Slightly lighter for elevated cards

    // Text (Light Mode) - UNCHANGED
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF757575)

    // ✅ NEW: Softer text for dark mode (easier on eyes)
    val TextDark = Color(0xFFE0E0E0) // Softer white
    val TextDarkSecondary = Color(0xFF9E9E9E) // Muted gray

    // ==================== STATUS COLORS ====================

    // LIGHT MODE Status Colors - UNCHANGED
    val Success = Color(0xFF43A047)
    val SuccessLight = Color(0xFFE8F5E9)
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFFFEBEE)
    val Warning = Color(0xFFFFA000)
    val WarningLight = Color(0xFFFFF3E0)
    val Info = Color(0xFF1976D2)
    val InfoLight = Color(0xFFE3F2FD)

    // ✅ NEW: DARK MODE Status Colors (muted & dark backgrounds)
    val SuccessDark = Color(0xFF66BB6A) // Muted green
    val SuccessDarkBg = Color(0xFF1B5E20) // Dark green background
    val ErrorDark = Color(0xFFEF5350) // Muted red
    val ErrorDarkBg = Color(0xFFB71C1C) // Dark red background
    val WarningDark = Color(0xFFFFB74D) // Muted orange
    val WarningDarkBg = Color(0xFFE65100) // Dark orange background
    val InfoDark = Color(0xFF42A5F5) // Muted blue
    val InfoDarkBg = Color(0xFF0D47A1) // Dark blue background
}

/**
 * ✅ LIGHT COLOR SCHEME (Perfect - no changes needed)
 */
private val LightColorScheme = lightColorScheme(
    primary = AppColors.Purple,
    onPrimary = Color.White,
    primaryContainer = AppColors.PurpleLight.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.PurpleDark,

    secondary = AppColors.Purple,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PurpleLight.copy(alpha = 0.1f),
    onSecondaryContainer = AppColors.PurpleDark,

    background = AppColors.LightBackground,
    onBackground = AppColors.TextPrimary,

    surface = Color.White, // ✅ Pure white cards
    onSurface = AppColors.TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = AppColors.TextSecondary,

    error = AppColors.Error,
    onError = Color.White,
    errorContainer = AppColors.ErrorLight,
    onErrorContainer = AppColors.Error,

    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5)
)

/**
 * ✅ DARK COLOR SCHEME (Completely redesigned for true dark)
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Muted purple
    primary = AppColors.PurpleDarkMode,
    onPrimary = Color.White,
    primaryContainer = AppColors.PurpleDarkDarkMode,
    onPrimaryContainer = AppColors.PurpleLightDarkMode,

    // Secondary colors
    secondary = AppColors.PurpleDarkMode,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PurpleDarkDarkMode,
    onSecondaryContainer = AppColors.PurpleLightDarkMode,

    // ✅ Background - PURE BLACK
    background = AppColors.DarkBackground,
    onBackground = AppColors.TextDark,

    // ✅ Surface - VERY DARK (cards)
    surface = AppColors.DarkSurface,
    onSurface = AppColors.TextDark,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.TextDarkSecondary,

    // Error colors - Muted
    error = AppColors.ErrorDark,
    onError = Color.White,
    errorContainer = AppColors.ErrorDarkBg,
    onErrorContainer = AppColors.ErrorDark,

    // ✅ Borders - Very dark
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF1A1A1A)
)

@Composable
fun SmartFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}