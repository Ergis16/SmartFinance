package com.gis.smartfinance.repository

import com.gis.smartfinance.data.dao.*
import com.gis.smartfinance.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    // Get all transactions
    fun getAllTransactions(): Flow<List<FinancialTransaction>> =  // ← CHANGED!
        transactionDao.getAllTransactions()

    // Get transactions between dates
    fun getTransactionsBetween(startDate: Date, endDate: Date): Flow<List<FinancialTransaction>> =  // ← CHANGED!
        transactionDao.getTransactionsBetween(startDate, endDate)

    // Add new transaction
    suspend fun addTransaction(transaction: FinancialTransaction) {  // ← CHANGED!
        transactionDao.insertTransaction(transaction)
    }

    // Update transaction
    suspend fun updateTransaction(transaction: FinancialTransaction) {  // ← CHANGED!
        transactionDao.updateTransaction(transaction)
    }

    // Delete transaction
    suspend fun deleteTransaction(transaction: FinancialTransaction) {  // ← CHANGED!
        transactionDao.deleteTransaction(transaction)
    }

    // Get total amount by type
    suspend fun getTotalAmount(type: TransactionType, startDate: Date, endDate: Date): Double =
        transactionDao.getTotalAmount(type, startDate, endDate) ?: 0.0

    // Get all categories
    fun getCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()

    // Get all savings goals
    fun getSavingsGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.getAllGoals()

    // Initialize default categories
    suspend fun initializeDefaultCategories() {
        val defaultCategories = listOf(
            Category(
                name = "Food & Dining",
                icon = "🍔",
                color = "#FF6B6B",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Transport",
                icon = "🚗",
                color = "#4ECDC4",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Shopping",
                icon = "🛍️",
                color = "#45B7D1",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Entertainment",
                icon = "🎬",
                color = "#96CEB4",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Bills",
                icon = "📄",
                color = "#FFEAA7",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Healthcare",
                icon = "🏥",
                color = "#DDA0DD",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Groceries",
                icon = "🛒",
                color = "#FFB6C1",
                type = TransactionType.EXPENSE
            ),
            Category(
                name = "Salary",
                icon = "💰",
                color = "#90EE90",
                type = TransactionType.INCOME
            ),
            Category(
                name = "Freelance",
                icon = "💻",
                color = "#87CEEB",
                type = TransactionType.INCOME
            ),
            Category(
                name = "Investment",
                icon = "📈",
                color = "#FFD700",
                type = TransactionType.INCOME
            )
        )

        defaultCategories.forEach { category ->
            categoryDao.insertCategory(category)
        }
    }

    // Add new savings goal
    suspend fun addSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.insertGoal(goal)
    }

    // Update savings goal
    suspend fun updateSavingsGoal(goal: SavingsGoal) {
        savingsGoalDao.updateGoal(goal)
    }
}