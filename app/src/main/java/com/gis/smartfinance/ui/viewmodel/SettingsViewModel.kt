package com.gis.smartfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gis.smartfinance.data.ThemeManager
import com.gis.smartfinance.data.ThemeMode
import com.gis.smartfinance.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing

    // Observe current theme
    val currentTheme: StateFlow<ThemeMode> = themeManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    // Set theme
    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }

    // Clear all data
    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isClearing.value = true
            repository.deleteAllTransactions()
            _isClearing.value = false
            onComplete()
        }
    }
}