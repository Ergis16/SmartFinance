package com.gis.smartfinance.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

class CurrencyManager(private val context: Context) {

    private val CURRENCY_KEY = stringPreferencesKey("selected_currency")

    // Popular currencies with their symbols and flags
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
        Currency("NZD", "$", "New Zealand Dollar", "🇳🇿"),
        Currency("KRW", "₩", "South Korean Won", "🇰🇷"),
        Currency("SGD", "$", "Singapore Dollar", "🇸🇬"),
        Currency("NOK", "kr", "Norwegian Krone", "🇳🇴"),
        Currency("MXN", "$", "Mexican Peso", "🇲🇽"),
        Currency("ZAR", "R", "South African Rand", "🇿🇦"),
        Currency("TRY", "₺", "Turkish Lira", "🇹🇷"),
        Currency("RUB", "₽", "Russian Ruble", "🇷🇺"),
        Currency("BRL", "R$", "Brazilian Real", "🇧🇷"),
        Currency("PLN", "zł", "Polish Złoty", "🇵🇱"),
        Currency("THB", "฿", "Thai Baht", "🇹🇭"),
        Currency("IDR", "Rp", "Indonesian Rupiah", "🇮🇩"),
        Currency("HUF", "Ft", "Hungarian Forint", "🇭🇺"),
        Currency("CZK", "Kč", "Czech Koruna", "🇨🇿"),
        Currency("ILS", "₪", "Israeli Shekel", "🇮🇱"),
        Currency("CLP", "$", "Chilean Peso", "🇨🇱"),
        Currency("PHP", "₱", "Philippine Peso", "🇵🇭"),
        Currency("AED", "د.إ", "UAE Dirham", "🇦🇪"),
        Currency("COP", "$", "Colombian Peso", "🇨🇴"),
        Currency("SAR", "﷼", "Saudi Riyal", "🇸🇦"),
        Currency("MYR", "RM", "Malaysian Ringgit", "🇲🇾"),
        Currency("RON", "lei", "Romanian Leu", "🇷🇴"),
        Currency("ALL", "L", "Albanian Lek", "🇦🇱"),
        Currency("BGN", "лв", "Bulgarian Lev", "🇧🇬"),
        Currency("HRK", "kn", "Croatian Kuna", "🇭🇷"),
        Currency("DKK", "kr", "Danish Krone", "🇩🇰"),
        Currency("EGP", "£", "Egyptian Pound", "🇪🇬"),
        Currency("PKR", "₨", "Pakistani Rupee", "🇵🇰"),
        Currency("NGN", "₦", "Nigerian Naira", "🇳🇬"),
        Currency("BDT", "৳", "Bangladeshi Taka", "🇧🇩"),
        Currency("VND", "₫", "Vietnamese Dong", "🇻🇳")
    )

    // Get selected currency from storage
    val selectedCurrency: Flow<Currency> = context.dataStore.data
        .map { preferences ->
            val code = preferences[CURRENCY_KEY] ?: getDefaultCurrencyCode()
            availableCurrencies.find { it.code == code } ?: availableCurrencies[0]
        }

    // Save selected currency
    suspend fun setSelectedCurrency(currency: Currency) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency.code
        }
    }

    // Auto-detect currency based on device locale
    fun getDefaultCurrencyCode(): String {
        return try {
            val locale = Locale.getDefault()
            val currency = java.util.Currency.getInstance(locale)
            val code = currency.currencyCode

            // Check if we support this currency
            if (availableCurrencies.any { it.code == code }) {
                code
            } else {
                // Fallback to country-based detection
                getCountryBasedCurrency(locale.country)
            }
        } catch (e: Exception) {
            "EUR" // Default fallback
        }
    }

    // Map countries to currencies for better detection
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
            "NO" -> "NOK"
            "DK" -> "DKK"
            "NZ" -> "NZD"
            "KR" -> "KRW"
            "SG" -> "SGD"
            "MX" -> "MXN"
            "ZA" -> "ZAR"
            "TR" -> "TRY"
            "RU" -> "RUB"
            "BR" -> "BRL"
            "PL" -> "PLN"
            "TH" -> "THB"
            "ID" -> "IDR"
            "HU" -> "HUF"
            "CZ" -> "CZK"
            "IL" -> "ILS"
            "CL" -> "CLP"
            "PH" -> "PHP"
            "AE" -> "AED"
            "CO" -> "COP"
            "SA" -> "SAR"
            "MY" -> "MYR"
            "RO" -> "RON"
            "AL" -> "ALL"
            "BG" -> "BGN"
            "HR" -> "HRK"
            "EG" -> "EGP"
            "PK" -> "PKR"
            "NG" -> "NGN"
            "BD" -> "BDT"
            "VN" -> "VND"
            // EU countries
            "DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "FI", "IE",
            "GR", "LU", "SI", "SK", "EE", "LV", "LT", "MT", "CY" -> "EUR"
            else -> "EUR" // Default to EUR
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

