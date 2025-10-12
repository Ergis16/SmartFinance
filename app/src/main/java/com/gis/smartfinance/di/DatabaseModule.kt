package com.gis.smartfinance.di

import android.content.Context
import androidx.room.Room
import com.gis.smartfinance.data.database.AppDatabase
import com.gis.smartfinance.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Database Dependencies
 *
 * @Module: Tells Hilt this class provides dependencies
 * @InstallIn(SingletonComponent::class): These dependencies live as long as the app
 *
 * How Hilt works:
 * 1. You define @Provides functions here
 * 2. Hilt generates code to create these dependencies
 * 3. When you @Inject in ViewModel, Hilt provides the instance
 * 4. Same instance is reused (Singleton)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * PROVIDES APP DATABASE
     *
     * @Provides: Tells Hilt how to create AppDatabase
     * @Singleton: Only one instance for entire app
     * @ApplicationContext: Hilt injects application context
     *
     * Room.databaseBuilder creates the database:
     * - context: Android context
     * - AppDatabase::class.java: Database class
     * - "smartfinance_database": Database file name
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartfinance_database"
        )
            // IMPORTANT: Remove this in production!
            // This deletes database on schema changes (OK during development)
            // In production, use proper migrations
            .fallbackToDestructiveMigration()

            // Optional: Enable query logging for debugging
            // .setQueryCallback({ sqlQuery, bindArgs ->
            //     Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
            // }, Executors.newSingleThreadExecutor())

            .build()
    }

    /**
     * PROVIDES TRANSACTION DAO
     *
     * DAO is obtained from database instance
     * Hilt automatically calls provideAppDatabase first
     */
    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
}

/**
 * HOW TO USE THIS IN YOUR CODE:
 *
 * Before (Manual):
 * val manager = PersistentTransactionManager.getInstance(context)
 *
 * After (Hilt):
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val repository: TransactionRepository  // Hilt injects this!
 * ) : ViewModel()
 *
 * Benefits:
 * - No context needed in ViewModel
 * - Easy to test (can inject fake repository)
 * - No manual singleton management
 * - Thread-safe by default
 */