package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.data.model.TransactionResult // ✅ ADDED
import com.gis.smartfinance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            amountError = null // ✅ ADDED: Clear error when user types
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = null // ✅ ADDED: Clear error when user types
        )
    }

    fun updateType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            type = type,
            category = "",
            categoryError = null // ✅ ADDED: Clear error when user changes type
        )
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            category = category,
            categoryError = null // ✅ ADDED: Clear error when user selects
        )
    }

    // ✅ ADDED: Date picker support (#9)
    fun updateDate(date: Date) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun saveTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value

        // ✅ CHANGED: Clear all previous errors
        _uiState.value = _uiState.value.copy(
            amountError = null,
            descriptionError = null,
            categoryError = null
        )

        var hasError = false

        // ✅ CHANGED: Set specific error messages
        if (state.amount.isBlank()) {
            _uiState.value = _uiState.value.copy(amountError = "Amount is required")
            hasError = true
        } else {
            val amountDouble = state.amount.toDoubleOrNull()
            if (amountDouble == null) {
                _uiState.value = _uiState.value.copy(amountError = "Invalid amount")
                hasError = true
            } else if (amountDouble <= 0) {
                _uiState.value = _uiState.value.copy(amountError = "Amount must be greater than 0")
                hasError = true
            } else if (amountDouble > 1_000_000) {
                _uiState.value = _uiState.value.copy(amountError = "Amount too large")
                hasError = true
            }
        }

        if (state.description.isBlank()) {
            _uiState.value = _uiState.value.copy(descriptionError = "Description is required")
            hasError = true
        }
        // ✅ REMOVED: Length validation - user can enter 1+ characters

        if (state.category.isBlank()) {
            _uiState.value = _uiState.value.copy(categoryError = "Please select a category")
            hasError = true
        }

        // ✅ ADDED: Date validation
        if (state.date.after(Date())) {
            onError("Transaction date cannot be in the future")
            hasError = true
        }

        if (hasError) {
            onError("Please fix the errors above")
            return
        }

        // Create transaction
        val transaction = FinancialTransaction(
            id = UUID.randomUUID().toString(),
            amount = state.amount.toDouble(),
            type = state.type,
            category = state.category,
            description = state.description,
            date = state.date, // ✅ ADDED: Use selected date
            createdAt = Date(),
            updatedAt = Date()
        )

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.addTransaction(transaction)

            _uiState.value = _uiState.value.copy(isLoading = false)

            // ✅ FIXED: Handle TransactionResult with proper type
            when (result) {
                is TransactionResult.Success<*> -> { // ✅ FIX: Add <*> for type
                    onSuccess()
                }
                is TransactionResult.Error -> {
                    onError("${result.message} (${result.code.name})")
                }
                is TransactionResult.ValidationError -> {
                    onError("${result.field.name}: ${result.message}")
                }
            }
        }
    }
}

/**
 * ✅ UPDATED: Added error fields for each input
 */
data class AddTransactionUiState(
    val amount: String = "",
    val amountError: String? = null, // ✅ ADDED
    val description: String = "",
    val descriptionError: String? = null, // ✅ ADDED
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val categoryError: String? = null, // ✅ ADDED
    val date: Date = Date(), // ✅ ADDED: For date picker
    val isLoading: Boolean = false
)