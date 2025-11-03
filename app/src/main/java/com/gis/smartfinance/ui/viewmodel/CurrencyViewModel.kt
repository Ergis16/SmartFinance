package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.Currency
import com.gis.smartfinance.data.CurrencyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ✅ ViewModel for Currency Management
 *
 * Handles:
 * - First launch detection
 * - Auto-detection
 * - Manual selection
 * - Current currency state
 */
@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyManager: CurrencyManager
) : ViewModel() {

    // Current selected currency (reactive)
    val selectedCurrency: StateFlow<Currency> = currencyManager.selectedCurrency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = currencyManager.getDefaultCurrency()
        )

    // Is this first launch? (for showing auto-detect dialog)
    val isFirstLaunch: StateFlow<Boolean> = currencyManager.isFirstLaunch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // All available currencies
    val availableCurrencies: List<Currency> = currencyManager.availableCurrencies

    // Auto-detected currency
    val autoDetectedCurrency: Currency = currencyManager.getDefaultCurrency()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Filtered currencies based on search
    val filteredCurrencies: StateFlow<List<Currency>> = MutableStateFlow(availableCurrencies)

    /**
     * Set currency (user selection or auto-detect)
     */
    fun selectCurrency(currency: Currency) {
        viewModelScope.launch {
            currencyManager.setSelectedCurrency(currency)
        }
    }

    /**
     * Use auto-detected currency
     */
    fun useAutoDetectedCurrency() {
        selectCurrency(autoDetectedCurrency)
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        (filteredCurrencies as MutableStateFlow).value = if (query.isEmpty()) {
            availableCurrencies
        } else {
            availableCurrencies.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.code.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Format amount with current currency
     */
    fun formatAmount(amount: Double): String {
        return currencyManager.formatAmount(amount, selectedCurrency.value)
    }
}