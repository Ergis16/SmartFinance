package com.gis.smartfinance.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gis.smartfinance.data.CurrencyManager  // ✅ ADD THIS
import com.gis.smartfinance.data.database.AppDatabase
import com.gis.smartfinance.data.dao.TransactionDao
import com.gis.smartfinance.data.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }

    // ✅ ADD THIS
    @Provides
    @Singleton
    fun provideCurrencyManager(
        @ApplicationContext context: Context
    ): CurrencyManager {
        return CurrencyManager(context)
    }
}