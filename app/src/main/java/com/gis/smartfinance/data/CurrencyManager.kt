package com.gis.smartfinance.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * DataStore extension - MUST be at top level
 */
private val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_prefs")

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

class CurrencyManager(private val context: Context) {

    private val CURRENCY_KEY = stringPreferencesKey("selected_currency")

    val availableCurrencies = listOf(
        Currency("EUR", "€", "Euro", "🇪🇺"),
        Currency("USD", "$", "US Dollar", "🇺🇸"),
        Currency("GBP", "£", "British Pound", "🇬🇧"),
        Currency("JPY", "¥", "Japanese Yen", "🇯🇵"),
        Currency("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        Currency("INR", "₹", "Indian Rupee", "🇮🇳"),
        Currency("CAD", "$", "Canadian Dollar", "🇨🇦"),
        Currency("AUD", "$", "Australian Dollar", "🇦🇺"),
        Currency("CHF", "Fr", "Swiss Franc", "🇨🇭"),
        Currency("SEK", "kr", "Swedish Krona", "🇸🇪"),
        Currency("ALL", "L", "Albanian Lek", "🇦🇱")
    )

    val selectedCurrency: Flow<Currency> = context.currencyDataStore.data
        .map { preferences ->
            val code = preferences[CURRENCY_KEY] ?: getDefaultCurrencyCode()
            availableCurrencies.find { it.code == code } ?: availableCurrencies[0]
        }

    suspend fun setSelectedCurrency(currency: Currency) {
        context.currencyDataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency.code
        }
    }

    fun getDefaultCurrencyCode(): String {
        return try {
            val locale = Locale.getDefault()
            val currency = java.util.Currency.getInstance(locale)
            val code = currency.currencyCode

            if (availableCurrencies.any { it.code == code }) {
                code
            } else {
                getCountryBasedCurrency(locale.country)
            }
        } catch (e: Exception) {
            "EUR"
        }
    }

    private fun getCountryBasedCurrency(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "US" -> "USD"
            "GB", "UK" -> "GBP"
            "JP" -> "JPY"
            "CN" -> "CNY"
            "IN" -> "INR"
            "CA" -> "CAD"
            "AU" -> "AUD"
            "CH" -> "CHF"
            "SE" -> "SEK"
            "AL" -> "ALL"
            "DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "FI", "IE",
            "GR", "LU", "SI", "SK", "EE", "LV", "LT", "MT", "CY" -> "EUR"
            else -> "EUR"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CurrencyManager? = null

        fun getInstance(context: Context): CurrencyManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CurrencyManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * WHAT WAS FIXED:
 * - Added currencyDataStore extension at top level
 * - Changed from context.dataStore to context.currencyDataStore
 * - This prevents conflict with transaction dataStore
 */