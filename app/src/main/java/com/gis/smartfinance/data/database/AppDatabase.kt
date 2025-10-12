package com.gis.smartfinance.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gis.smartfinance.data.dao.TransactionDao
import com.gis.smartfinance.data.model.FinancialTransaction

/**
 * Room Database Configuration
 *
 * This is the main database class for the app
 * Room creates the actual SQLite database file
 *
 * @Database annotation parameters:
 * - entities: List of data classes to store
 * - version: Increment when schema changes (for migrations)
 * - exportSchema: Save schema for migration testing
 */
@Database(
    entities = [FinancialTransaction::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to TransactionDao
     * Room implements this automatically
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * Database is created by Hilt in DatabaseModule
     * No need for companion object getInstance() pattern!
     */
}

/**
 * DATABASE FILE LOCATION:
 * /data/data/com.gis.smartfinance/databases/smartfinance_database
 *
 * You can inspect it with Android Studio's Database Inspector:
 * View -> Tool Windows -> App Inspection -> Database Inspector
 *
 * MIGRATION STRATEGY (for future):
 * When you add new fields (like subcategories), increment version:
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         database.execSQL("ALTER TABLE transactions ADD COLUMN subcategory TEXT")
 *     }
 * }
 *
 * Then add to database builder:
 * .addMigrations(MIGRATION_1_2)
 */