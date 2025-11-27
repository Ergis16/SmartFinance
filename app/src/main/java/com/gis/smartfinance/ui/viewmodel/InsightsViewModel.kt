package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.CurrencyManager // ✅ ADD THIS
import com.gis.smartfinance.data.repository.TransactionRepository
import com.gis.smartfinance.domain.insights.AnalyzeTransactionsUseCase
import com.gis.smartfinance.domain.insights.InsightsAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val analyzeTransactionsUseCase: AnalyzeTransactionsUseCase,
    private val currencyManager: CurrencyManager // ✅ ADD THIS
) : ViewModel() {

    val insightsState: StateFlow<InsightsUiState> = combine(
        repository.getAllTransactions(),
        repository.getTotalIncome(),
        repository.getTotalExpense(),
        repository.getBalance(),
        currencyManager.selectedCurrency // ✅ ADD THIS
    ) { transactions, income, expense, balance, currency -> // ✅ ADD currency HERE
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
                flow {
                    val transactions = repository.getAllTransactions().first()
                    val income = repository.getTotalIncome().first()
                    val expense = repository.getTotalExpense().first()
                    val balance = repository.getBalance().first()
                    val currency = currencyManager.selectedCurrency.first() // ✅ ADD THIS

                    val analysis = analyzeTransactionsUseCase(
                        transactions = transactions,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = balance,
                        currencySymbol = currency.symbol // ✅ ADD THIS
                    )

                    emit(InsightsUiState.Success(analysis))
                }.flowOn(Dispatchers.Default)
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

sealed class InsightsUiState {
    object Loading : InsightsUiState()
    object Empty : InsightsUiState()
    data class Success(val analysis: InsightsAnalysis) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
}