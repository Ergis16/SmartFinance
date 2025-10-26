package com.gis.smartfinance.utils

/**
 * ✅ BONUS: Centralized constants file
 * Create: app/src/main/java/com/gis/smartfinance/utils/Constants.kt
 *
 * Benefits:
 * - No magic numbers in code
 * - Easy to update in one place
 * - Self-documenting
 */

object AppConstants {

    // ==================== APP INFO ====================
    const val APP_NAME = "SmartFinance"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    // ==================== TRANSACTION LIMITS ====================
    const val MIN_TRANSACTION_AMOUNT = 0.01
    const val MAX_TRANSACTION_AMOUNT = 10_000_000.0
    const val MIN_DESCRIPTION_LENGTH = 3
    const val MAX_DESCRIPTION_LENGTH = 500

    // ==================== PAGINATION ====================
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5
    const val INITIAL_LOAD_SIZE = 40

    // ==================== UI CONSTANTS ====================
    const val SPLASH_SCREEN_DURATION_MS = 2500L
    const val ANIMATION_DURATION_SHORT = 300
    const val ANIMATION_DURATION_MEDIUM = 500
    const val ANIMATION_DURATION_LONG = 1000

    // ==================== DATE RANGES ====================
    const val DAYS_IN_WEEK = 7
    const val DAYS_IN_MONTH = 30
    const val DAYS_IN_YEAR = 365
    const val MONTHS_IN_YEAR = 12

    // ==================== INSIGHTS THRESHOLDS ====================
    const val MIN_TRANSACTIONS_FOR_INSIGHTS = 5
    const val MIN_DAYS_FOR_INSIGHTS = 3
    const val MIN_DAYS_FOR_PATTERNS = 7
    const val MIN_DAYS_FOR_DETAILED_ANALYSIS = 30

    // ==================== FINANCIAL THRESHOLDS ====================
    const val HIGH_FOOD_SPENDING_MONTHLY = 400.0
    const val HIGH_TRANSPORT_SPENDING_MONTHLY = 300.0
    const val HIGH_SHOPPING_SPENDING_MONTHLY = 500.0
    const val HIGH_CATEGORY_PERCENTAGE = 30.0

    const val RECOMMENDED_SAVINGS_RATE = 0.20 // 20%
    const val GOOD_SAVINGS_RATE = 0.15 // 15%
    const val LOW_SAVINGS_RATE = 0.10 // 10%

    const val MINIMUM_EMERGENCY_FUND_MONTHS = 1.0
    const val GOOD_EMERGENCY_FUND_MONTHS = 3.0
    const val EXCELLENT_EMERGENCY_FUND_MONTHS = 6.0

    // ==================== CACHE DURATIONS ====================
    const val CACHE_TIMEOUT_SHORT = 1000L // 1 second
    const val CACHE_TIMEOUT_MEDIUM = 5000L // 5 seconds
    const val CACHE_TIMEOUT_LONG = 30000L // 30 seconds

    // ==================== DATABASE ====================
    const val DATABASE_NAME = "smartfinance_database"
    const val DATABASE_VERSION = 1

    // ==================== DATASTORE ====================
    const val CURRENCY_PREFS = "currency_prefs"
    const val THEME_PREFS = "theme_preferences"
    const val APP_PREFS = "app_preferences"

    // ==================== DEFAULT VALUES ====================
    const val DEFAULT_CURRENCY = "Lek"
    const val DEFAULT_CURRENCY_CODE = "ALL"
    const val DEFAULT_RECENT_TRANSACTIONS_LIMIT = 10

    // ==================== ERROR MESSAGES ====================
    const val ERROR_AMOUNT_REQUIRED = "Amount is required"
    const val ERROR_AMOUNT_INVALID = "Invalid amount"
    const val ERROR_AMOUNT_TOO_SMALL = "Amount must be greater than 0"
    const val ERROR_AMOUNT_TOO_LARGE = "Amount too large"
    const val ERROR_DESCRIPTION_REQUIRED = "Description is required"
    const val ERROR_DESCRIPTION_TOO_SHORT = "Description too short (min 3 characters)"
    const val ERROR_DESCRIPTION_TOO_LONG = "Description too long (max 500 characters)"
    const val ERROR_CATEGORY_REQUIRED = "Please select a category"
    const val ERROR_DATE_FUTURE = "Transaction date cannot be in the future"
    const val ERROR_DATE_TOO_OLD = "Transaction date is too old"
    const val ERROR_NETWORK = "Network error occurred"
    const val ERROR_DATABASE = "Database error occurred"
    const val ERROR_UNKNOWN = "An unknown error occurred"

    // ==================== SUCCESS MESSAGES ====================
    const val SUCCESS_TRANSACTION_ADDED = "Transaction added successfully"
    const val SUCCESS_TRANSACTION_UPDATED = "Transaction updated successfully"
    const val SUCCESS_TRANSACTION_DELETED = "Transaction deleted successfully"
    const val SUCCESS_ALL_DATA_CLEARED = "All data cleared successfully"

    // ==================== CATEGORIES ====================
    object Categories {
        // Expense categories
        val EXPENSE_CATEGORIES = listOf(
            "Food & Dining",
            "Transport",
            "Shopping",
            "Entertainment",
            "Bills",
            "Healthcare",
            "Education",
            "Other"
        )

        // Income categories
        val INCOME_CATEGORIES = listOf(
            "Salary",
            "Freelance",
            "Investment",
            "Gift",
            "Other"
        )

        // Category icons (emoji or Material Icons identifier)
        val CATEGORY_ICONS = mapOf(
            "Food & Dining" to "🍽️",
            "Transport" to "🚗",
            "Shopping" to "🛍️",
            "Entertainment" to "🎬",
            "Bills" to "📄",
            "Healthcare" to "🏥",
            "Education" to "📚",
            "Other" to "📦",
            "Salary" to "💼",
            "Freelance" to "💻",
            "Investment" to "📈",
            "Gift" to "🎁"
        )
    }

    // ==================== CHART COLORS ====================
    object ChartColors {
        const val COLOR_PRIMARY = 0xFF6C63FF
        const val COLOR_SECONDARY = 0xFF4834DF
        const val COLOR_SUCCESS = 0xFF43A047
        const val COLOR_ERROR = 0xFFE53935
        const val COLOR_WARNING = 0xFFFFA000
        const val COLOR_INFO = 0xFF1976D2

        val CATEGORY_COLORS = listOf(
            0xFF6C63FF,
            0xFFE53935,
            0xFF4ECDC4,
            0xFFFFA000,
            0xFF43A047,
            0xFFAB47BC,
            0xFF29B6F6,
            0xFFFF7043
        )
    }

    // ==================== SHARED PREFERENCES KEYS ====================
    object PrefKeys {
        const val SELECTED_CURRENCY = "selected_currency"
        const val THEME_MODE = "app_theme"
        const val FIRST_LAUNCH = "first_launch"
        const val LAST_SYNC = "last_sync"
        const val SHOW_TUTORIAL = "show_tutorial"
    }

    // ==================== FORMATTING ====================
    object Format {
        const val DATE_SHORT = "MMM dd, yyyy"
        const val DATE_LONG = "EEEE, MMMM dd, yyyy"
        const val DATE_TIME = "MMM dd 'at' h:mm a"
        const val TIME = "h:mm a"
        const val CURRENCY_DECIMAL_PLACES = 2
    }

    // ==================== ANALYTICS ====================
    object Analytics {
        const val EVENT_TRANSACTION_ADDED = "transaction_added"
        const val EVENT_TRANSACTION_DELETED = "transaction_deleted"
        const val EVENT_INSIGHTS_VIEWED = "insights_viewed"
        const val EVENT_ANALYTICS_VIEWED = "analytics_viewed"
        const val EVENT_THEME_CHANGED = "theme_changed"
    }

    // ==================== LIMITS ====================
    object Limits {
        const val MAX_TRANSACTIONS_IN_MEMORY = 1000
        const val MAX_SEARCH_RESULTS = 100
        const val MAX_RECENT_SEARCHES = 10
        const val MAX_CATEGORY_NAME_LENGTH = 50
    }
}

/**
 * Extension object for easier access
 */
typealias AC = AppConstants

/**
 * Usage examples:
 *
 * // Validation:
 * if (amount > AC.MAX_TRANSACTION_AMOUNT) { ... }
 *
 * // Error messages:
 * Text(AC.ERROR_AMOUNT_REQUIRED)
 *
 * // Categories:
 * val categories = AC.Categories.EXPENSE_CATEGORIES
 *
 * // Colors:
 * Color(AC.ChartColors.COLOR_PRIMARY)
 *
 * // Formatting:
 * SimpleDateFormat(AC.Format.DATE_SHORT, Locale.getDefault())
 */