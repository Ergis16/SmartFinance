package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel - Manages Home Screen State
 *
 * @HiltViewModel: Tells Hilt to inject dependencies
 * @Inject constructor: Hilt provides TransactionRepository
 *
 * ViewModel benefits:
 * - Survives configuration changes (screen rotation)
 * - Separates business logic from UI
 * - Testable (can mock repository)
 * - Lifecycle-aware (cleans up automatically)
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    /**
     * UI STATE
     *
     * StateFlow = Flow that always has a value
     * UI collects this and recomposes when it changes
     *
     * SharingStarted.WhileSubscribed(5000):
     * - Stops collecting when no subscribers for 5 seconds
     * - Saves battery/resources
     * - Restarts when UI comes back
     */
    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllTransactions(),
        repository.getTotalIncome(),
        repository.getTotalExpense(),
        repository.getBalance(),
        repository.getRecentTransactions(10)
    ) { transactions, income, expense, balance, recent ->
        HomeUiState.Success(
            allTransactions = transactions,
            recentTransactions = recent,
            totalIncome = income,
            totalExpense = expense,
            balance = balance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    /**
     * DELETE TRANSACTION
     * Called when user confirms delete in bottom sheet
     */
    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    /**
     * UPDATE TRANSACTION
     * ✅ NEW: For inline editing from bottom sheet
     * Called when user saves edited transaction
     */
    fun updateTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    /**
     * CLEAR ALL TRANSACTIONS
     * Called from Settings screen
     */
    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }
}

/**
 * UI STATE SEALED CLASS
 *
 * Represents all possible states of Home screen
 * UI can pattern match on this
 */
sealed class HomeUiState {
    object Loading : HomeUiState()

    data class Success(
        val allTransactions: List<FinancialTransaction>,
        val recentTransactions: List<FinancialTransaction>,
        val totalIncome: Double,
        val totalExpense: Double,
        val balance: Double
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}