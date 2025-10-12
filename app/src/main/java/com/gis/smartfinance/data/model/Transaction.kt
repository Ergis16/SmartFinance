package com.gis.smartfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gis.smartfinance.data.database.Converters
import java.util.Date
import java.util.UUID

/**
 * Main transaction entity stored in Room database
 *
 * Room benefits over DataStore:
 * - Can query by date range: "SELECT * WHERE date BETWEEN X AND Y"
 * - Can group by category: "SELECT category, SUM(amount)"
 * - Indexed searches (fast even with 10,000+ transactions)
 * - Proper data types (no JSON parsing errors)
 * - Database migrations (add columns without losing data)
 */
@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class FinancialTransaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val amount: Double = 0.0,

    val type: TransactionType = TransactionType.EXPENSE,

    val category: String = "",

    val description: String = "",

    val date: Date = Date(),

    // Future-ready fields (for subcategories feature you want)
    val subcategory: String? = null,

    val tags: String = "",  // Comma-separated for now, can parse later

    val location: String? = null,

    val merchantName: String? = null,

    val isRecurring: Boolean = false,

    val recurringPeriod: RecurringPeriod? = null,

    val createdAt: Date = Date(),

    val updatedAt: Date = Date()
) {
    /**
     * Validation: Ensures data integrity
     * Call this before saving to database
     */
    fun isValid(): Boolean {
        return amount > 0.0 &&
                description.isNotBlank() &&
                category.isNotBlank()
    }

    /**
     * Validation with specific error messages
     */
    fun validate(): ValidationResult {
        return when {
            amount <= 0.0 -> ValidationResult.Error("Amount must be greater than 0")
            amount > 1_000_000.0 -> ValidationResult.Error("Amount too large")
            description.isBlank() -> ValidationResult.Error("Description required")
            category.isBlank() -> ValidationResult.Error("Category required")
            date.after(Date()) -> ValidationResult.Error("Date cannot be in future")
            else -> ValidationResult.Success
        }
    }
}

/**
 * Transaction type enum
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Recurring period enum
 */
enum class RecurringPeriod {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * Extension function to format amount with currency
 * (Will use CurrencyManager in future when you implement it)
 */
fun Double.toFormattedPrice(): String {
    return "€${String.format("%.2f", this)}"
}

/**
 * WHAT THIS FIXES:
 * - Added validation to prevent bad data
 * - Added subcategory field for future feature
 * - Type-safe with Room (no JSON parsing errors)
 * - Can add more fields without breaking existing data (migrations)
 */