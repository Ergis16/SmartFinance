package com.gis.smartfinance.data.database

import androidx.room.*
import com.gis.smartfinance.data.model.FinancialTransaction  // ← NOT Transaction!
import com.gis.smartfinance.data.model.Category
import com.gis.smartfinance.data.model.SavingsGoal
import com.gis.smartfinance.data.dao.TransactionDao
import com.gis.smartfinance.data.dao.CategoryDao
import com.gis.smartfinance.data.dao.SavingsGoalDao

@Database(
    entities = [FinancialTransaction::class, Category::class, SavingsGoal::class],  // ← CHANGED!
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}