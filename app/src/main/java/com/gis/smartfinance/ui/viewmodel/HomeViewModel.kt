package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // ✅ FIXED #19: Separate flows to prevent unnecessary recompositions
    // Each screen component can subscribe to only what it needs

    private val _allTransactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<FinancialTransaction>> = _allTransactions
        .map { transactions ->
            transactions.take(10) // Only take first 10
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = repository.getBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ✅ FIXED #19: Computed state using derivedStateOf
    // Only recomputes when dependencies change
    val hasTransactions: StateFlow<Boolean> = _allTransactions
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ✅ FIXED: Combined UI state with proper error handling
    val uiState: StateFlow<HomeUiState> = combine(
        _allTransactions,
        recentTransactions,
        totalIncome,
        totalExpense,
        balance
    ) { all, recent, income, expense, bal ->
        // Return Success state
        HomeUiState.Success(
            allTransactions = all,
            recentTransactions = recent,
            totalIncome = income,
            totalExpense = expense,
            balance = bal
        ) as HomeUiState // ✅ FIX: Cast to base type
    }
        .catch { error ->
            // Emit error state
            emit(HomeUiState.Error(error.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()

    data class Success(
        val allTransactions: List<FinancialTransaction>,
        val recentTransactions: List<FinancialTransaction>,
        val totalIncome: Double,
        val totalExpense: Double,
        val balance: Double
    ) : HomeUiState() {
        // ✅ ADDED #19: Computed properties that don't cause recomposition
        val hasTransactions: Boolean get() = allTransactions.isNotEmpty()
        val transactionCount: Int get() = allTransactions.size
        val savingsRate: Double get() =
            if (totalIncome > 0) (totalIncome - totalExpense) / totalIncome else 0.0
    }

    data class Error(val message: String) : HomeUiState()
}