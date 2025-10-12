package com.gis.smartfinance.data.repository

import com.gis.smartfinance.data.dao.TransactionDao
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.data.model.ValidationResult
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

    suspend fun addTransaction(transaction: FinancialTransaction): Result<Unit> {
        return try {
            when (val validation = transaction.validate()) {
                is ValidationResult.Success -> {
                    transactionDao.insertTransaction(transaction)
                    Result.success(Unit)
                }
                is ValidationResult.Error -> {
                    Result.failure(IllegalArgumentException(validation.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: FinancialTransaction): Result<Unit> {
        return try {
            when (val validation = transaction.validate()) {
                is ValidationResult.Success -> {
                    transactionDao.updateTransaction(transaction)
                    Result.success(Unit)
                }
                is ValidationResult.Error -> {
                    Result.failure(IllegalArgumentException(validation.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transaction: FinancialTransaction): Result<Unit> {
        return try {
            transactionDao.deleteTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllTransactions(): Result<Unit> {
        return try {
            transactionDao.deleteAllTransactions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    /**
     * FIXED: Convert List<CategoryTotal> to Map<String, Double>
     */
    fun getExpensesByCategory(): Flow<Map<String, Double>> {
        return transactionDao.getCategoryTotalsList(TransactionType.EXPENSE)
            .map { list ->
                list.associate { it.category to it.total }
            }
    }

    /**
     * FIXED: Convert List<CategoryTotal> to Map<String, Double>
     */
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
}

/**
 * WHAT WAS FIXED:
 *
 * 1. DAO now returns List<CategoryTotal> instead of Map
 * 2. Repository converts List to Map using .associate()
 * 3. Room can now properly generate the implementation
 * 4. All type mapping errors resolved
 */