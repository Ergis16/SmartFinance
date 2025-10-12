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
     * CLEAR ALL TRANSACTIONS
     * Called from Settings screen
     */
    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }

    /**
     * DELETE TRANSACTION
     * For future swipe-to-delete feature
     */
    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
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

/**
 * HOW TO USE IN COMPOSABLE:
 *
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsState()
 *
 *     when (uiState) {
 *         is HomeUiState.Loading -> LoadingIndicator()
 *         is HomeUiState.Success -> ShowContent(uiState.balance, ...)
 *         is HomeUiState.Error -> ShowError(uiState.message)
 *     }
 * }
 */