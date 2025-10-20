package com.gis.smartfinance.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for theme preferences
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences"
)

/**
 * Theme options
 */
enum class ThemeMode {
    LIGHT,      // Always light
    DARK,       // Always dark
    SYSTEM      // Follow system setting
}

/**
 * Manages app theme preference
 */
@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    /**
     * Get current theme preference
     * Default: SYSTEM (follow device)
     */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    /**
     * Save theme preference
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }
}