package com.gis.smartfinance

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for SmartFinance
 *
 * @HiltAndroidApp annotation triggers Hilt's code generation
 * This creates a base application class that serves as the dependency container
 *
 * How it works:
 * 1. Hilt creates a Dagger component attached to Application lifecycle
 * 2. All dependencies defined in @Module classes become available
 * 3. ViewModels can be injected with @HiltViewModel
 * 4. Screens can access ViewModels with hiltViewModel()
 */
@HiltAndroidApp
class SmartFinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Hilt initializes automatically
        // Database is created lazily when first accessed
        // No manual initialization needed!
    }
}

/**
 * WHAT THIS FIXES:
 * - Before: Manual singleton management with getInstance()
 * - After: Hilt manages lifecycle automatically
 * - Before: Context passed everywhere
 * - After: Hilt injects dependencies where needed
 * - Before: Hard to test
 * - After: Easy to mock dependencies in tests
 */