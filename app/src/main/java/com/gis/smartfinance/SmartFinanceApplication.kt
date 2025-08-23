package com.gis.smartfinance

import android.app.Application
import com.gis.smartfinance.data.PersistentTransactionManager

class SmartFinanceApplication : Application() {
    lateinit var transactionManager: PersistentTransactionManager

    override fun onCreate() {
        super.onCreate()
        // Initialize the transaction manager with context
        transactionManager = PersistentTransactionManager.getInstance(this)
    }

    companion object {
        @Volatile
        private var INSTANCE: SmartFinanceApplication? = null

        fun getInstance(): SmartFinanceApplication {
            return INSTANCE ?: throw IllegalStateException("Application not initialized")
        }
    }
}