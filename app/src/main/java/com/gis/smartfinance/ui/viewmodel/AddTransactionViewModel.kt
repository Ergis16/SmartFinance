package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * AddTransactionViewModel
 * Manages state for adding new transactions
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // Mutable state for form inputs
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    /**
     * Update amount
     */
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    /**
     * Update description
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * Update transaction type
     */
    fun updateType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            type = type,
            category = "" // Reset category when type changes
        )
    }

    /**
     * Update category
     */
    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    /**
     * Save transaction
     * Returns true if successful
     */
    fun saveTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value

        // Validation
        if (state.amount.isBlank()) {
            onError("Please enter an amount")
            return
        }

        val amountDouble = state.amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            onError("Please enter a valid amount")
            return
        }

        if (state.description.isBlank()) {
            onError("Please enter a description")
            return
        }

        if (state.category.isBlank()) {
            onError("Please select a category")
            return
        }

        // Create transaction
        val transaction = FinancialTransaction(
            id = UUID.randomUUID().toString(),
            amount = amountDouble,
            type = state.type,
            category = state.category,
            description = state.description,
            date = Date()
        )

        // Save to database
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.addTransaction(transaction)

            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to save transaction")
            }
        }
    }
}

/**
 * UI State for Add Transaction screen
 */
data class AddTransactionUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val isLoading: Boolean = false
)
