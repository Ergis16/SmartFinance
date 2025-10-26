package com.gis.smartfinance.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * ✅ BONUS: Utility extensions to make code cleaner
 * Create: app/src/main/java/com/gis/smartfinance/utils/Extensions.kt
 */

// ==================== DATE EXTENSIONS ====================

/**
 * Format date as "Jan 15, 2025"
 */
fun Date.toShortDateString(): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(this)
}

/**
 * Format date as "Monday, January 15, 2025"
 */
fun Date.toLongDateString(): String {
    return SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(this)
}

/**
 * Format time as "2:30 PM"
 */
fun Date.toTimeString(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(this)
}

/**
 * Format as "Jan 15 at 2:30 PM"
 */
fun Date.toDateTimeString(): String {
    return SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault()).format(this)
}

/**
 * Get relative time: "Today", "Yesterday", "2 days ago"
 */
fun Date.toRelativeString(): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { time = this@toRelativeString }

    val daysDiff = ((now.timeInMillis - date.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    return when (daysDiff) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> "$daysDiff days ago"
        in 7..13 -> "Last week"
        in 14..29 -> "${daysDiff / 7} weeks ago"
        in 30..60 -> "Last month"
        else -> toShortDateString()
    }
}

/**
 * Check if date is today
 */
fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { time = this@isToday }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

/**
 * Check if date is this month
 */
fun Date.isThisMonth(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { time = this@isThisMonth }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            today.get(Calendar.MONTH) == date.get(Calendar.MONTH)
}

/**
 * Get start of month for this date
 */
fun Date.startOfMonth(): Date {
    return Calendar.getInstance().apply {
        time = this@startOfMonth
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

/**
 * Get end of month for this date
 */
fun Date.endOfMonth(): Date {
    return Calendar.getInstance().apply {
        time = this@endOfMonth
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time
}

// ==================== DOUBLE/MONEY EXTENSIONS ====================

/**
 * Format as currency: 1234.56 -> "1,234.56 Lek"
 */
fun Double.toCurrency(symbol: String = "Lek"): String {
    val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(this)
    return "$formatted $symbol"
}

/**
 * Format as compact currency: 1234567 -> "1.2M Lek"
 */
fun Double.toCompactCurrency(symbol: String = "Lek"): String {
    return when {
        abs(this) >= 1_000_000 -> "${String.format("%.1f", this / 1_000_000)}M $symbol"
        abs(this) >= 1_000 -> "${String.format("%.1f", this / 1_000)}K $symbol"
        else -> toCurrency(symbol)
    }
}

/**
 * Format with sign: -100.0 -> "-100.00 Lek", 100.0 -> "+100.00 Lek"
 */
fun Double.toSignedCurrency(symbol: String = "Lek"): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${toCurrency(symbol)}"
}

/**
 * Format as percentage: 0.15 -> "15%"
 */
fun Double.toPercentage(): String {
    return "${(this * 100).toInt()}%"
}

/**
 * Format as percentage with decimals: 0.1543 -> "15.4%"
 */
fun Double.toPercentageDecimal(decimals: Int = 1): String {
    return String.format("%.${decimals}f%%", this * 100)
}

// ==================== STRING EXTENSIONS ====================

/**
 * Capitalize first letter: "hello" -> "Hello"
 */
fun String.capitalizeFirst(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

/**
 * Truncate with ellipsis: "Long text here" -> "Long tex..."
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length > maxLength) {
        "${take(maxLength - ellipsis.length)}$ellipsis"
    } else {
        this
    }
}

/**
 * Check if string is a valid amount
 */
fun String.isValidAmount(): Boolean {
    val amount = toDoubleOrNull() ?: return false
    return amount > 0 && amount <= 10_000_000
}

// ==================== LIST EXTENSIONS ====================

/**
 * Safe get or null
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in indices) get(index) else null
}

/**
 * Sum of Double list
 */
fun List<Double>.sum(): Double = fold(0.0) { acc, d -> acc + d }

// ==================== COMPOSE EXTENSIONS ====================

/**
 * Remember a date formatter (prevents recreation on recomposition)
 */
@Composable
fun rememberDateFormatter(pattern: String = "MMM dd, yyyy"): SimpleDateFormat {
    val locale = Locale.getDefault()
    return remember(pattern, locale) {
        SimpleDateFormat(pattern, locale)
    }
}

/**
 * Remember a number formatter
 */
@Composable
fun rememberCurrencyFormatter(symbol: String = "Lek"): (Double) -> String {
    return remember(symbol) {
        { amount: Double -> amount.toCurrency(symbol) }
    }
}

// ==================== VALIDATION EXTENSIONS ====================

/**
 * Validate transaction amount
 */
fun Double.isValidTransactionAmount(): Boolean {
    return this > 0 && this <= 10_000_000
}

/**
 * Validate description
 */
fun String.isValidDescription(): Boolean {
    val trimmed = trim()
    return trimmed.length in 3..500
}

// ==================== COLOR EXTENSIONS ====================

/**
 * Get contrasting text color for background
 */
fun androidx.compose.ui.graphics.Color.contrastingColor(): androidx.compose.ui.graphics.Color {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return if (luminance > 0.5) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        androidx.compose.ui.graphics.Color.White
    }
}

// ==================== USAGE EXAMPLES ====================

/*
// In your Composables:

// Format dates
Text(transaction.date.toRelativeString()) // "2 days ago"
Text(transaction.date.toShortDateString()) // "Jan 15, 2025"

// Format money
Text(amount.toCurrency()) // "1,234.56 Lek"
Text(amount.toCompactCurrency()) // "1.2K Lek"
Text(savingsRate.toPercentageDecimal()) // "15.4%"

// Validate input
if (amountString.isValidAmount()) {
    // Save
}

// In ViewModels:
val thisMonthTransactions = transactions.filter { it.date.isThisMonth() }
val todayTransactions = transactions.filter { it.date.isToday() }

// Safe list access
val firstTransaction = transactions.getOrNull(0)

// Date ranges
val monthStart = Date().startOfMonth()
val monthEnd = Date().endOfMonth()
*/