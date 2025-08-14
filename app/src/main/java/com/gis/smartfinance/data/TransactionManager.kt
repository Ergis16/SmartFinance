package com.gis.smartfinance.data

import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import java.util.UUID

object TransactionManager {
    private val _transactions = MutableStateFlow<List<FinancialTransaction>>(emptyList())
    val transactions: StateFlow<List<FinancialTransaction>> = _transactions

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    init {



    }

    fun addTransaction(transaction: FinancialTransaction) {
        _transactions.value = _transactions.value + transaction
        updateTotals()
    }

    fun removeTransaction(transactionId: String) {
        _transactions.value = _transactions.value.filter { it.id != transactionId }
        updateTotals()
    }

    fun clearAll() {
        _transactions.value = emptyList()
        updateTotals()
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
}