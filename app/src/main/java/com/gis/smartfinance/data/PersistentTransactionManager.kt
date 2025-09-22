package com.gis.smartfinance.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

// Extension to create DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "transactions_prefs")

class PersistentTransactionManager(private val context: Context) {
    private val gson = Gson()
    private val TRANSACTIONS_KEY = stringPreferencesKey("transactions_list")
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _transactions = MutableStateFlow<List<FinancialTransaction>>(emptyList())
    val transactions: StateFlow<List<FinancialTransaction>> = _transactions

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Load saved transactions asynchronously
        scope.launch {
            loadTransactions()
        }
    }

    private suspend fun loadTransactions() {
        try {
            context.dataStore.data
                .catch { exception ->
                    // If there's an error reading data, emit empty preferences
                    emit(emptyPreferences())
                }
                .first() // Get only the first value, don't collect indefinitely
                .let { preferences ->
                    val jsonString = preferences[TRANSACTIONS_KEY] ?: "[]"
                    val type = object : TypeToken<List<FinancialTransaction>>() {}.type
                    val loadedTransactions = try {
                        gson.fromJson<List<FinancialTransaction>>(jsonString, type) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    _transactions.value = loadedTransactions
                    updateTotals()
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            // If anything goes wrong, just start with empty data
            _transactions.value = emptyList()
            updateTotals()
            _isLoading.value = false
        }
    }

    suspend fun addTransaction(transaction: FinancialTransaction) {
        val updatedList = _transactions.value + transaction
        _transactions.value = updatedList
        updateTotals()
        saveTransactions(updatedList)
    }

    suspend fun removeTransaction(transactionId: String) {
        val updatedList = _transactions.value.filter { it.id != transactionId }
        _transactions.value = updatedList
        updateTotals()
        saveTransactions(updatedList)
    }

    suspend fun updateTransaction(transaction: FinancialTransaction) {
        val updatedList = _transactions.value.map {
            if (it.id == transaction.id) transaction else it
        }
        _transactions.value = updatedList
        updateTotals()
        saveTransactions(updatedList)
    }

    suspend fun clearAll() {
        _transactions.value = emptyList()
        updateTotals()
        saveTransactions(emptyList())
    }

    private suspend fun saveTransactions(transactions: List<FinancialTransaction>) {
        try {
            val jsonString = gson.toJson(transactions)
            context.dataStore.edit { preferences ->
                preferences[TRANSACTIONS_KEY] = jsonString
            }
        } catch (e: Exception) {
            // If saving fails, continue anyway
        }
    }

    private fun updateTotals() {
        val allTransactions = _transactions.value
        _totalIncome.value = allTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        _totalExpense.value = allTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        _balance.value = _totalIncome.value - _totalExpense.value
    }

    fun getRecentTransactions(limit: Int = 10): List<FinancialTransaction> {
        return _transactions.value
            .sortedByDescending { it.date }
            .take(limit)
    }

    // ========== ADDITIONAL METHODS FOR AI INSIGHTS ==========

    /**
     * Get transactions for a specific time period
     */
    fun getTransactionsBetween(startDate: Date, endDate: Date): List<FinancialTransaction> {
        return _transactions.value.filter {
            it.date.after(startDate) && it.date.before(endDate)
        }
    }

    /**
     * Get transactions by category
     */
    fun getTransactionsByCategory(category: String): List<FinancialTransaction> {
        return _transactions.value.filter { it.category == category }
    }

    /**
     * Get expense breakdown by category
     */
    fun getExpensesByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
    }

    /**
     * Get income breakdown by category
     */
    fun getIncomeByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
    }

    /**
     * Calculate average daily spending
     */
    fun getAverageDailySpending(days: Int = 30): Double {
        val startDate = Date(System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000))
        val recentExpenses = _transactions.value
            .filter { it.type == TransactionType.EXPENSE && it.date.after(startDate) }
            .sumOf { it.amount }

        return if (days > 0) recentExpenses / days else 0.0
    }

    /**
     * Get spending trend (comparing last 7 days to previous 7 days)
     */
    fun getSpendingTrend(): SpendingTrend {
        val now = System.currentTimeMillis()
        val oneWeekAgo = Date(now - (7L * 24 * 60 * 60 * 1000))
        val twoWeeksAgo = Date(now - (14L * 24 * 60 * 60 * 1000))

        val lastWeekExpenses = _transactions.value
            .filter { it.type == TransactionType.EXPENSE && it.date.after(oneWeekAgo) }
            .sumOf { it.amount }

        val previousWeekExpenses = _transactions.value
            .filter { it.type == TransactionType.EXPENSE && it.date.after(twoWeeksAgo) && it.date.before(oneWeekAgo) }
            .sumOf { it.amount }

        return when {
            previousWeekExpenses == 0.0 -> SpendingTrend.STABLE
            lastWeekExpenses > previousWeekExpenses * 1.1 -> SpendingTrend.INCREASING
            lastWeekExpenses < previousWeekExpenses * 0.9 -> SpendingTrend.DECREASING
            else -> SpendingTrend.STABLE
        }
    }

    /**
     * Get transactions from the last N days
     */
    fun getTransactionsFromLastDays(days: Int): List<FinancialTransaction> {
        val startDate = Date(System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000))
        return _transactions.value.filter { it.date.after(startDate) }
    }

    /**
     * Get highest spending category
     */
    fun getHighestSpendingCategory(): Pair<String, Double>? {
        return getExpensesByCategory().maxByOrNull { it.value }?.toPair()
    }

    /**
     * Calculate savings rate
     */
    fun getSavingsRate(): Double {
        return if (_totalIncome.value > 0) {
            _balance.value / _totalIncome.value
        } else {
            0.0
        }
    }

    /**
     * Get monthly average for income and expenses
     */
    fun getMonthlyAverages(): Pair<Double, Double> {
        val thirtyDaysAgo = Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))

        val monthlyIncome = _transactions.value
            .filter { it.type == TransactionType.INCOME && it.date.after(thirtyDaysAgo) }
            .sumOf { it.amount }

        val monthlyExpenses = _transactions.value
            .filter { it.type == TransactionType.EXPENSE && it.date.after(thirtyDaysAgo) }
            .sumOf { it.amount }

        return Pair(monthlyIncome, monthlyExpenses)
    }

    enum class SpendingTrend {
        INCREASING, DECREASING, STABLE
    }

    companion object {
        @Volatile
        private var INSTANCE: PersistentTransactionManager? = null

        fun getInstance(context: Context): PersistentTransactionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PersistentTransactionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}