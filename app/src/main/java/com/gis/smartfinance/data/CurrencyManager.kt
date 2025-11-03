package com.gis.smartfinance.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ DataStore extension - MUST be at top level
 */
private val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "currency_preferences"
)

/**
 * Currency data class with all info
 */
data class Currency(
    val code: String,        // "USD", "EUR", "ALL"
    val symbol: String,      // "$", "€", "Lek"
    val name: String,        // "US Dollar"
    val flag: String,        // "🇺🇸"
    val decimalPlaces: Int = 2  // How many decimals to show
)

/**
 * ✅ COMPLETELY REDESIGNED CurrencyManager
 *
 * Features:
 * - Auto-detects currency on first launch
 * - Stores user preference
 * - Provides Flow for reactive updates
 * - Singleton via Hilt injection
 */
@Singleton
class CurrencyManager @Inject constructor(
    private val context: Context
) {
    // Keys for DataStore
    private companion object {
        val CURRENCY_CODE_KEY = stringPreferencesKey("selected_currency_code")
        val FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")
    }

    /**
     * ✅ All supported currencies (50+ countries)
     */
    val availableCurrencies = listOf(
        // Europe
        Currency("EUR", "€", "Euro", "🇪🇺"),
        Currency("GBP", "£", "British Pound", "🇬🇧"),
        Currency("CHF", "Fr", "Swiss Franc", "🇨🇭"),
        Currency("SEK", "kr", "Swedish Krona", "🇸🇪"),
        Currency("NOK", "kr", "Norwegian Krone", "🇳🇴"),
        Currency("DKK", "kr", "Danish Krone", "🇩🇰"),
        Currency("PLN", "zł", "Polish Zloty", "🇵🇱"),
        Currency("CZK", "Kč", "Czech Koruna", "🇨🇿"),
        Currency("HUF", "Ft", "Hungarian Forint", "🇭🇺"),
        Currency("RON", "lei", "Romanian Leu", "🇷🇴"),
        Currency("BGN", "лв", "Bulgarian Lev", "🇧🇬"),
        Currency("HRK", "kn", "Croatian Kuna", "🇭🇷"),
        Currency("RSD", "дин", "Serbian Dinar", "🇷🇸"),
        Currency("ALL", "Lek", "Albanian Lek", "🇦🇱"),
        Currency("MKD", "ден", "Macedonian Denar", "🇲🇰"),
        Currency("BAM", "KM", "Bosnia Mark", "🇧🇦"),
        Currency("ISK", "kr", "Icelandic Króna", "🇮🇸"),
        Currency("RUB", "₽", "Russian Ruble", "🇷🇺"),
        Currency("UAH", "₴", "Ukrainian Hryvnia", "🇺🇦"),
        Currency("TRY", "₺", "Turkish Lira", "🇹🇷"),

        // Americas
        Currency("USD", "$", "US Dollar", "🇺🇸"),
        Currency("CAD", "$", "Canadian Dollar", "🇨🇦"),
        Currency("MXN", "$", "Mexican Peso", "🇲🇽"),
        Currency("BRL", "R$", "Brazilian Real", "🇧🇷"),
        Currency("ARS", "$", "Argentine Peso", "🇦🇷"),
        Currency("CLP", "$", "Chilean Peso", "🇨🇱"),
        Currency("COP", "$", "Colombian Peso", "🇨🇴"),
        Currency("PEN", "S/", "Peruvian Sol", "🇵🇪"),

        // Asia
        Currency("JPY", "¥", "Japanese Yen", "🇯🇵"),
        Currency("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        Currency("KRW", "₩", "South Korean Won", "🇰🇷"),
        Currency("INR", "₹", "Indian Rupee", "🇮🇳"),
        Currency("IDR", "Rp", "Indonesian Rupiah", "🇮🇩"),
        Currency("THB", "฿", "Thai Baht", "🇹🇭"),
        Currency("VND", "₫", "Vietnamese Dong", "🇻🇳"),
        Currency("PHP", "₱", "Philippine Peso", "🇵🇭"),
        Currency("SGD", "$", "Singapore Dollar", "🇸🇬"),
        Currency("MYR", "RM", "Malaysian Ringgit", "🇲🇾"),
        Currency("HKD", "$", "Hong Kong Dollar", "🇭🇰"),
        Currency("TWD", "$", "Taiwan Dollar", "🇹🇼"),
        Currency("PKR", "₨", "Pakistani Rupee", "🇵🇰"),
        Currency("BDT", "৳", "Bangladeshi Taka", "🇧🇩"),
        Currency("LKR", "Rs", "Sri Lankan Rupee", "🇱🇰"),

        // Oceania
        Currency("AUD", "$", "Australian Dollar", "🇦🇺"),
        Currency("NZD", "$", "New Zealand Dollar", "🇳🇿"),

        // Middle East
        Currency("AED", "د.إ", "UAE Dirham", "🇦🇪"),
        Currency("SAR", "﷼", "Saudi Riyal", "🇸🇦"),
        Currency("QAR", "﷼", "Qatari Riyal", "🇶🇦"),
        Currency("KWD", "د.ك", "Kuwaiti Dinar", "🇰🇼"),
        Currency("ILS", "₪", "Israeli Shekel", "🇮🇱"),

        // Africa
        Currency("ZAR", "R", "South African Rand", "🇿🇦"),
        Currency("EGP", "£", "Egyptian Pound", "🇪🇬"),
        Currency("NGN", "₦", "Nigerian Naira", "🇳🇬"),
        Currency("KES", "KSh", "Kenyan Shilling", "🇰🇪"),
        Currency("MAD", "د.م.", "Moroccan Dirham", "🇲🇦")
    )

    /**
     * ✅ Get currently selected currency as Flow (reactive)
     */
    val selectedCurrency: Flow<Currency> = context.currencyDataStore.data
        .map { preferences ->
            val code = preferences[CURRENCY_CODE_KEY]
            if (code != null) {
                availableCurrencies.find { it.code == code } ?: getDefaultCurrency()
            } else {
                getDefaultCurrency()
            }
        }

    /**
     * ✅ Check if this is first launch
     */
    val isFirstLaunch: Flow<Boolean> = context.currencyDataStore.data
        .map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true
        }

    /**
     * ✅ Set selected currency and mark as not first launch
     */
    suspend fun setSelectedCurrency(currency: Currency) {
        context.currencyDataStore.edit { preferences ->
            preferences[CURRENCY_CODE_KEY] = currency.code
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    /**
     * ✅ Auto-detect currency based on device locale
     */
    fun getDefaultCurrency(): Currency {
        return try {
            val locale = Locale.getDefault()
            val javaCurrency = java.util.Currency.getInstance(locale)
            val code = javaCurrency.currencyCode

            // Try to find in our list
            availableCurrencies.find { it.code == code }
                ?: getCountryBasedCurrency(locale.country)
                ?: Currency("EUR", "€", "Euro", "🇪🇺") // Fallback
        } catch (e: Exception) {
            Currency("EUR", "€", "Euro", "🇪🇺") // Fallback
        }
    }

    /**
     * ✅ Get currency by country code
     */
    private fun getCountryBasedCurrency(countryCode: String): Currency? {
        val code = when (countryCode.uppercase()) {
            // Europe
            "US" -> "USD"
            "GB", "UK" -> "GBP"
            "CH" -> "CHF"
            "SE" -> "SEK"
            "NO" -> "NOK"
            "DK" -> "DKK"
            "PL" -> "PLN"
            "CZ" -> "CZK"
            "HU" -> "HUF"
            "RO" -> "RON"
            "BG" -> "BGN"
            "HR" -> "HRK"
            "RS" -> "RSD"
            "AL" -> "ALL"
            "MK" -> "MKD"
            "BA" -> "BAM"
            "IS" -> "ISK"
            "RU" -> "RUB"
            "UA" -> "UAH"
            "TR" -> "TRY"

            // Euro countries
            "DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "FI", "IE",
            "GR", "LU", "SI", "SK", "EE", "LV", "LT", "MT", "CY" -> "EUR"

            // Americas
            "CA" -> "CAD"
            "MX" -> "MXN"
            "BR" -> "BRL"
            "AR" -> "ARS"
            "CL" -> "CLP"
            "CO" -> "COP"
            "PE" -> "PEN"

            // Asia
            "JP" -> "JPY"
            "CN" -> "CNY"
            "KR" -> "KRW"
            "IN" -> "INR"
            "ID" -> "IDR"
            "TH" -> "THB"
            "VN" -> "VND"
            "PH" -> "PHP"
            "SG" -> "SGD"
            "MY" -> "MYR"
            "HK" -> "HKD"
            "TW" -> "TWD"
            "PK" -> "PKR"
            "BD" -> "BDT"
            "LK" -> "LKR"

            // Oceania
            "AU" -> "AUD"
            "NZ" -> "NZD"

            // Middle East
            "AE" -> "AED"
            "SA" -> "SAR"
            "QA" -> "QAR"
            "KW" -> "KWD"
            "IL" -> "ILS"

            // Africa
            "ZA" -> "ZAR"
            "EG" -> "EGP"
            "NG" -> "NGN"
            "KE" -> "KES"
            "MA" -> "MAD"

            else -> "EUR" // Default
        }

        return availableCurrencies.find { it.code == code }
    }

    /**
     * ✅ Format amount with selected currency
     */
    fun formatAmount(amount: Double, currency: Currency): String {
        return "${currency.symbol} ${String.format("%,.${currency.decimalPlaces}f", amount)}"
    }
}