package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.repository.TransactionRepository
import com.gis.smartfinance.domain.insights.AnalyzeTransactionsUseCase
import com.gis.smartfinance.domain.insights.InsightsAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * InsightsViewModel
 *
 * Handles all insights calculation in background thread
 * UI just observes the result
 *
 * Performance: All heavy calculations done on Dispatchers.Default
 * No UI blocking!
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val analyzeTransactionsUseCase: AnalyzeTransactionsUseCase
) : ViewModel() {

    /**
     * Insights State
     *
     * Automatically recalculates when transactions change
     * Heavy work done in background thread
     */
    val insightsState: StateFlow<InsightsUiState> = combine(
        repository.getAllTransactions(),
        repository.getTotalIncome(),
        repository.getTotalExpense(),
        repository.getBalance()
    ) { transactions, income, expense, balance ->
        // Switch to background thread for heavy calculation
        if (transactions.isEmpty()) {
            InsightsUiState.Empty
        } else {
            InsightsUiState.Loading
        }
    }
        .flatMapLatest { state ->
            if (state is InsightsUiState.Empty) {
                flowOf(state)
            } else {
                // Perform analysis in background
                flow {
                    val transactions = repository.getAllTransactions().first()
                    val income = repository.getTotalIncome().first()
                    val expense = repository.getTotalExpense().first()
                    val balance = repository.getBalance().first()

                    val analysis = analyzeTransactionsUseCase(
                        transactions = transactions,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = balance
                    )

                    emit(InsightsUiState.Success(analysis))
                }.flowOn(Dispatchers.Default) // Run on background thread!
            }
        }
        .catch { error ->
            emit(InsightsUiState.Error(error.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState.Loading
        )
}

/**
 * UI State for Insights Screen
 */
sealed class InsightsUiState {
    object Loading : InsightsUiState()
    object Empty : InsightsUiState()
    data class Success(val analysis: InsightsAnalysis) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}

/**
 * WHAT THIS FIXES:
 *
 * Performance:
 * - Before: 500 lines of calculations in UI thread (LAG!)
 * - After: All calculations in background (Dispatchers.Default)
 *
 * Architecture:
 * - Before: Business logic mixed with UI
 * - After: Clean separation (ViewModel -> UseCase -> Repository)
 *
 * Testability:
 * - Before: Impossible to test analysis logic
 * - After: Can test ViewModel with mock repository
 */

