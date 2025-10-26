package com.gis.smartfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gis.smartfinance.data.database.Converters
import java.util.Date
import java.util.UUID

/**
 * Main transaction entity stored in Room database
 * ✅ FIXED #16: Added proper validation with init block
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

    val subcategory: String? = null,

    val tags: String = "",  // TODO: Convert to List<String> with type converter

    val location: String? = null,

    val merchantName: String? = null,

    val isRecurring: Boolean = false,

    val recurringPeriod: RecurringPeriod? = null,

    val createdAt: Date = Date(),

    val updatedAt: Date = Date()
) {
    // ✅ FIXED: Minimal validation - allows 1+ character descriptions
    init {
        require(amount > 0) {
            "Amount must be greater than 0 (got: $amount)"
        }
        require(amount <= 10_000_000) {
            "Amount is unrealistically large (got: $amount)"
        }
        // ✅ ONLY CHECK: Description is not blank (1+ character is fine)
        require(description.isNotBlank()) {
            "Description cannot be empty"
        }
        require(category.isNotBlank()) {
            "Category cannot be empty"
        }
        require(!date.after(Date())) {
            "Transaction date cannot be in the future"
        }

        // Validate that date isn't unrealistically old (e.g., before year 2000)
        val year2000 = Date(946684800000L) // January 1, 2000
        require(date.after(year2000)) {
            "Transaction date is too old (before year 2000)"
        }

        // If recurring, period must be set
        if (isRecurring) {
            requireNotNull(recurringPeriod) {
                "Recurring period must be set for recurring transactions"
            }
        }
    }

    /**
     * ✅ UPDATED: More lenient validation - minimum 1 character
     */
    fun validate(): ValidationResult {
        return when {
            amount <= 0.0 -> ValidationResult.Error("Amount must be greater than 0")
            amount > 10_000_000.0 -> ValidationResult.Error("Amount too large")
            description.isBlank() -> ValidationResult.Error("Description required")
            description.length > 500 -> ValidationResult.Error("Description too long (max 500 characters)")
            category.isBlank() -> ValidationResult.Error("Category required")
            date.after(Date()) -> ValidationResult.Error("Date cannot be in future")
            isRecurring && recurringPeriod == null -> ValidationResult.Error("Recurring period required")
            else -> ValidationResult.Success
        }
    }

    /**
     * Simple validity check (for backward compatibility)
     */
    fun isValid(): Boolean {
        return validate() is ValidationResult.Success
    }

    /**
     * ✅ ADDED #16: Sanitize user input
     * Use this before creating transactions from user input
     */
    companion object {
        /**
         * Create a safe transaction with sanitized inputs
         */
        fun createSafe(
            amount: Double,
            type: TransactionType,
            category: String,
            description: String,
            date: Date = Date()
        ): Result<FinancialTransaction> {
            return try {
                val sanitizedDescription = description
                    .trim()
                    .take(500) // Max 500 chars
                    .replace(Regex("\\s+"), " ") // Normalize whitespace

                val sanitizedCategory = category.trim()

                val transaction = FinancialTransaction(
                    id = UUID.randomUUID().toString(),
                    amount = amount.coerceIn(0.01, 10_000_000.0),
                    type = type,
                    category = sanitizedCategory,
                    description = sanitizedDescription,
                    date = date,
                    createdAt = Date(),
                    updatedAt = Date()
                )

                Result.success(transaction)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            }
        }

        /**
         * Validate amount before creating transaction
         */
        fun isValidAmount(amount: Double): Boolean {
            return amount > 0 && amount <= 10_000_000
        }

        /**
         * Validate description before creating transaction
         */
        fun isValidDescription(description: String): Boolean {
            val trimmed = description.trim()
            return trimmed.length in 3..500
        }

        /**
         * Validate category before creating transaction
         */
        fun isValidCategory(category: String): Boolean {
            return category.trim().isNotBlank()
        }

        /**
         * Validate date before creating transaction
         */
        fun isValidDate(date: Date): Boolean {
            val now = Date()
            val year2000 = Date(946684800000L)
            return date.before(now) && date.after(year2000)
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
 */
fun Double.toFormattedPrice(currencySymbol: String = "Lek"): String {
    return "$currencySymbol ${String.format("%,.2f", this)}"
}