package com.gis.smartfinance.data.repository

import com.gis.smartfinance.data.dao.TransactionDao
import com.gis.smartfinance.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    fun getAllTransactions(): Flow<List<FinancialTransaction>> {
        return transactionDao.getAllTransactions()
    }

    suspend fun getTransactionById(id: String): FinancialTransaction? {
        return transactionDao.getTransactionById(id)
    }

    fun getRecentTransactions(limit: Int = 10): Flow<List<FinancialTransaction>> {
        return transactionDao.getRecentTransactions(limit)
    }

    /**
     * Add a new transaction
     * Returns TransactionResult with specific error types
     */
    suspend fun addTransaction(transaction: FinancialTransaction): TransactionResult<Unit> {
        return try {
            // Validate transaction
            when (val validation = transaction.validate()) {
                is ValidationResult.Success -> {
                    // Save to database
                    transactionDao.insertTransaction(transaction)
                    TransactionResult.Success(Unit)
                }
                is ValidationResult.Error -> {
                    // Map validation error to appropriate field
                    val field = determineValidationField(validation.message)
                    TransactionResult.ValidationError(field, validation.message)
                }
            }
        } catch (e: Exception) {
            TransactionResult.Error(
                message = e.message ?: "Failed to save transaction",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    /**
     * Update existing transaction
     */
    suspend fun updateTransaction(transaction: FinancialTransaction): TransactionResult<Unit> {
        return try {
            when (val validation = transaction.validate()) {
                is ValidationResult.Success -> {
                    transactionDao.updateTransaction(transaction)
                    TransactionResult.Success(Unit)
                }
                is ValidationResult.Error -> {
                    val field = determineValidationField(validation.message)
                    TransactionResult.ValidationError(field, validation.message)
                }
            }
        } catch (e: Exception) {
            TransactionResult.Error(
                message = e.message ?: "Failed to update transaction",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(transaction: FinancialTransaction): TransactionResult<Unit> {
        return try {
            transactionDao.deleteTransaction(transaction)
            TransactionResult.Success(Unit)
        } catch (e: Exception) {
            TransactionResult.Error(
                message = e.message ?: "Failed to delete transaction",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    /**
     * Delete all transactions
     */
    suspend fun deleteAllTransactions(): TransactionResult<Unit> {
        return try {
            transactionDao.deleteAllTransactions()
            TransactionResult.Success(Unit)
        } catch (e: Exception) {
            TransactionResult.Error(
                message = e.message ?: "Failed to clear data",
                code = ErrorCode.DATABASE_ERROR,
                exception = e
            )
        }
    }

    fun getTransactionsBetween(startDate: Date, endDate: Date): Flow<List<FinancialTransaction>> {
        return transactionDao.getTransactionsBetween(startDate.time, endDate.time)
    }

    fun getTransactionsByCategory(category: String): Flow<List<FinancialTransaction>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    fun searchTransactions(query: String): Flow<List<FinancialTransaction>> {
        return transactionDao.searchTransactions(query)
    }

    fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalByType(TransactionType.INCOME)
    }

    fun getTotalExpense(): Flow<Double> {
        return transactionDao.getTotalByType(TransactionType.EXPENSE)
    }

    fun getBalance(): Flow<Double> {
        return combine(
            getTotalIncome(),
            getTotalExpense()
        ) { income, expense ->
            income - expense
        }
    }

    fun getExpensesByCategory(): Flow<Map<String, Double>> {
        return transactionDao.getCategoryTotalsList(TransactionType.EXPENSE)
            .map { list ->
                list.associate { it.category to it.total }
            }
    }

    fun getIncomeByCategory(): Flow<Map<String, Double>> {
        return transactionDao.getCategoryTotalsList(TransactionType.INCOME)
            .map { list ->
                list.associate { it.category to it.total }
            }
    }

    fun getTransactionCount(): Flow<Int> {
        return transactionDao.getTransactionCount()
    }

    suspend fun getDaysOfData(): Int {
        val oldest = transactionDao.getOldestTransactionDate()
        val newest = transactionDao.getNewestTransactionDate()

        if (oldest == null || newest == null) return 0

        val diffInMillis = newest - oldest
        val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        return days.coerceIn(0, 3650)
    }

    suspend fun getAverageDailySpending(days: Int = 30): Double {
        val endDate = Date()
        val startDate = Date(endDate.time - (days.toLong() * 24 * 60 * 60 * 1000))

        val total = transactionDao.getTotalByTypeAndDateRange(
            TransactionType.EXPENSE,
            startDate.time,
            endDate.time
        )

        return if (days > 0) total / days else 0.0
    }

    /**
     * Helper function to determine validation field from error message
     */
    private fun determineValidationField(message: String): ValidationField {
        return when {
            message.contains("amount", ignoreCase = true) -> ValidationField.AMOUNT
            message.contains("description", ignoreCase = true) -> ValidationField.DESCRIPTION
            message.contains("category", ignoreCase = true) -> ValidationField.CATEGORY
            message.contains("date", ignoreCase = true) -> ValidationField.DATE
            else -> ValidationField.AMOUNT // Default
        }
    }
}