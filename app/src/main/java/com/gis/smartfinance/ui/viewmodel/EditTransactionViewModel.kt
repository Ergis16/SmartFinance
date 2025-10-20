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
import javax.inject.Inject

/**
 * ViewModel for editing existing transaction
 */
@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    private var currentTransaction: FinancialTransaction? = null

    /**
     * Load transaction data by ID
     */
    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val transaction = repository.getTransactionById(transactionId)

            if (transaction != null) {
                currentTransaction = transaction
                _uiState.value = EditTransactionUiState(
                    amount = transaction.amount.toString(),
                    description = transaction.description,
                    type = transaction.type,
                    category = transaction.category,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            type = type,
            category = "" // Reset category when type changes
        )
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    /**
     * Save edited transaction
     */
    fun saveTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        val original = currentTransaction

        if (original == null) {
            onError("Transaction not found")
            return
        }

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

        // Create updated transaction (keep original date and ID)
        val updatedTransaction = original.copy(
            amount = amountDouble,
            type = state.type,
            category = state.category,
            description = state.description,
            updatedAt = java.util.Date() // Update timestamp
        )

        // Save to database
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.updateTransaction(updatedTransaction)

            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update transaction")
            }
        }
    }
}

/**
 * UI State for Edit Transaction screen
 */
data class EditTransactionUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val isLoading: Boolean = false
)