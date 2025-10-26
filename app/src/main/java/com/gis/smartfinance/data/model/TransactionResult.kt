package com.gis.smartfinance.data.model

/**
 * ✅ ADDED #11: Sealed class for better error handling
 *
 * More specific than Kotlin's Result<T> type
 * Allows for different error types and codes
 */
sealed class TransactionResult<out T> {
    data class Success<T>(val data: T) : TransactionResult<T>()

    data class Error(
        val message: String,
        val code: ErrorCode,
        val exception: Throwable? = null
    ) : TransactionResult<Nothing>()

    data class ValidationError(
        val field: ValidationField,
        val message: String
    ) : TransactionResult<Nothing>()
}

/**
 * Error codes for different failure scenarios
 */
enum class ErrorCode {
    DATABASE_ERROR,        // Room database errors
    VALIDATION_ERROR,      // Invalid data
    NETWORK_ERROR,         // Future: when syncing to cloud
    PERMISSION_ERROR,      // Future: file access, etc.
    UNKNOWN_ERROR
}

/**
 * Fields that can have validation errors
 */
enum class ValidationField {
    AMOUNT,
    DESCRIPTION,
    CATEGORY,
    DATE,
    TYPE
}

/**
 * Extension functions for easier usage
 */
fun <T> TransactionResult<T>.isSuccess(): Boolean = this is TransactionResult.Success

fun <T> TransactionResult<T>.isError(): Boolean = this is TransactionResult.Error

fun <T> TransactionResult<T>.getOrNull(): T? = when (this) {
    is TransactionResult.Success -> data
    else -> null
}

fun <T> TransactionResult<T>.getErrorMessageOrNull(): String? = when (this) {
    is TransactionResult.Error -> message
    is TransactionResult.ValidationError -> message
    else -> null
}

/**
 * Execute block only if result is success
 */
inline fun <T> TransactionResult<T>.onSuccess(block: (T) -> Unit): TransactionResult<T> {
    if (this is TransactionResult.Success) {
        block(data)
    }
    return this
}

/**
 * Execute block only if result is error
 */
inline fun <T> TransactionResult<T>.onError(block: (message: String, code: ErrorCode) -> Unit): TransactionResult<T> {
    if (this is TransactionResult.Error) {
        block(message, code)
    }
    return this
}

/**
 * Execute block only if result is validation error
 */
inline fun <T> TransactionResult<T>.onValidationError(
    block: (field: ValidationField, message: String) -> Unit
): TransactionResult<T> {
    if (this is TransactionResult.ValidationError) {
        block(field, message)
    }
    return this
}