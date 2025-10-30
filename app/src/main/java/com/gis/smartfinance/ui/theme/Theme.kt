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
 * ✅ FIX #8: COMPLETELY REDESIGNED DARK THEME
 * - Proper contrast ratios
 * - Muted colors for dark mode
 * - No harsh bright colors
 * - Consistent throughout app
 */
object AppColors {
    // ==================== LIGHT MODE COLORS ====================

    // Primary Purple (Light Mode)
    val Purple = Color(0xFF6C63FF)
    val PurpleLight = Color(0xFF8B83FF)
    val PurpleDark = Color(0xFF4834DF)

    // Backgrounds (Light Mode)
    val LightBackground = Color(0xFFF5F7FA)

    // ==================== DARK MODE COLORS ====================

    // Primary Purple (Dark Mode - Muted)
    val PurpleDarkMode = Color(0xFF7B73E8) // Softer, less harsh
    val PurpleLightDarkMode = Color(0xFF9B93F5)
    val PurpleDarkDarkMode = Color(0xFF5B4FCC)

    // Backgrounds (Dark Mode)
    val DarkBackground = Color(0xFF0F0F0F) // True dark
    val DarkSurface = Color(0xFF1A1A1A) // Cards
    val DarkSurfaceVariant = Color(0xFF252525) // Elevated cards

    // Text (Light Mode)
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF757575)

    // Text (Dark Mode)
    val TextDark = Color(0xFFE8E8E8) // Softer white
    val TextDarkSecondary = Color(0xFFA0A0A0) // Muted gray

    // ==================== STATUS COLORS ====================

    // LIGHT MODE Status Colors
    val Success = Color(0xFF43A047)
    val SuccessLight = Color(0xFFE8F5E9)
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFFFEBEE)
    val Warning = Color(0xFFFFA000)
    val WarningLight = Color(0xFFFFF3E0)
    val Info = Color(0xFF1976D2)
    val InfoLight = Color(0xFFE3F2FD)

    // DARK MODE Status Colors (Muted & Darker)
    val SuccessDark = Color(0xFF5CB85C) // Softer green
    val SuccessDarkBg = Color(0xFF1B3A1B) // Dark green background
    val ErrorDark = Color(0xFFE57373) // Softer red
    val ErrorDarkBg = Color(0xFF3A1B1B) // Dark red background
    val WarningDark = Color(0xFFFFB74D) // Softer orange
    val WarningDarkBg = Color(0xFF3A2F1B) // Dark orange background
    val InfoDark = Color(0xFF64B5F6) // Softer blue
    val InfoDarkBg = Color(0xFF1B2A3A) // Dark blue background
}

/**
 * ✅ LIGHT COLOR SCHEME (Unchanged - already good)
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

    surface = Color.White,
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
 * ✅ DARK COLOR SCHEME (COMPLETELY REDESIGNED)
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Muted purple for dark mode
    primary = AppColors.PurpleDarkMode,
    onPrimary = Color.White,
    primaryContainer = AppColors.PurpleDarkDarkMode.copy(alpha = 0.3f),
    onPrimaryContainer = AppColors.PurpleLightDarkMode,

    // Secondary colors
    secondary = AppColors.PurpleDarkMode,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PurpleDarkDarkMode.copy(alpha = 0.3f),
    onSecondaryContainer = AppColors.PurpleLightDarkMode,

    // Background - True dark
    background = AppColors.DarkBackground,
    onBackground = AppColors.TextDark,

    // Surface - Slightly lighter than background
    surface = AppColors.DarkSurface,
    onSurface = AppColors.TextDark,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.TextDarkSecondary,

    // Error colors - Muted
    error = AppColors.ErrorDark,
    onError = Color.White,
    errorContainer = AppColors.ErrorDarkBg,
    onErrorContainer = AppColors.ErrorDark,

    // Borders
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A)
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

/**
 * ✅ HELPER: Get appropriate colors based on theme
 */
@Composable
fun getThemeAwareColors(): ThemeColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        ThemeColors(
            purple = AppColors.PurpleDarkMode,
            purpleLight = AppColors.PurpleLightDarkMode,
            purpleDark = AppColors.PurpleDarkDarkMode,
            success = AppColors.SuccessDark,
            successBg = AppColors.SuccessDarkBg,
            error = AppColors.ErrorDark,
            errorBg = AppColors.ErrorDarkBg,
            warning = AppColors.WarningDark,
            warningBg = AppColors.WarningDarkBg,
            info = AppColors.InfoDark,
            infoBg = AppColors.InfoDarkBg
        )
    } else {
        ThemeColors(
            purple = AppColors.Purple,
            purpleLight = AppColors.PurpleLight,
            purpleDark = AppColors.PurpleDark,
            success = AppColors.Success,
            successBg = AppColors.SuccessLight,
            error = AppColors.Error,
            errorBg = AppColors.ErrorLight,
            warning = AppColors.Warning,
            warningBg = AppColors.WarningLight,
            info = AppColors.Info,
            infoBg = AppColors.InfoLight
        )
    }
}

data class ThemeColors(
    val purple: Color,
    val purpleLight: Color,
    val purpleDark: Color,
    val success: Color,
    val successBg: Color,
    val error: Color,
    val errorBg: Color,
    val warning: Color,
    val warningBg: Color,
    val info: Color,
    val infoBg: Color
)