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
 * CUSTOM COLORS - Used consistently across the app
 */
object AppColors {
    // Primary Purple
    val Purple = Color(0xFF6C63FF)
    val PurpleLight = Color(0xFF8B83FF)
    val PurpleDark = Color(0xFF4834DF)

    // Backgrounds
    val LightBackground = Color(0xFFF5F7FA)
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkSurfaceVariant = Color(0xFF2A2A2A)

    // Text
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF757575)
    val TextDark = Color(0xFFE0E0E0)
    val TextDarkSecondary = Color(0xFF9E9E9E)

    // Status Colors (same for both themes)
    val Success = Color(0xFF43A047)
    val SuccessLight = Color(0xFFE8F5E9)
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFFFEBEE)
    val Warning = Color(0xFFFFA000)
    val WarningLight = Color(0xFFFFF3E0)
    val Info = Color(0xFF1976D2)
    val InfoLight = Color(0xFFE3F2FD)
}

/**
 * LIGHT COLOR SCHEME
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
 * DARK COLOR SCHEME
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PurpleLight,
    onPrimary = Color.White,
    primaryContainer = AppColors.Purple.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.PurpleLight,

    secondary = AppColors.PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = AppColors.Purple.copy(alpha = 0.2f),
    onSecondaryContainer = AppColors.PurpleLight,

    background = AppColors.DarkBackground,
    onBackground = AppColors.TextDark,

    surface = AppColors.DarkSurface,
    onSurface = AppColors.TextDark,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.TextDarkSecondary,

    error = AppColors.Error,
    onError = Color.White,
    errorContainer = AppColors.Error.copy(alpha = 0.2f),
    onErrorContainer = AppColors.Error,

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