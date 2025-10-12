package com.gis.smartfinance.data.database

import androidx.room.TypeConverter
import com.gis.smartfinance.data.model.RecurringPeriod
import com.gis.smartfinance.data.model.TransactionType
import java.util.Date

/**
 * Room Type Converters
 *
 * Room can only store primitive types (String, Int, Long, etc.)
 * These converters tell Room how to convert custom types to/from primitives
 *
 * Example: Date -> Long (timestamp) when saving to DB
 *          Long -> Date when reading from DB
 */
class Converters {

    /**
     * DATE CONVERTERS
     * Stores dates as Unix timestamps (milliseconds since 1970)
     * This is safe, efficient, and allows date range queries
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * TRANSACTION TYPE CONVERTERS
     * Stores enum as string name
     */
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }

    /**
     * RECURRING PERIOD CONVERTERS
     * Stores enum as string name
     */
    @TypeConverter
    fun fromRecurringPeriod(value: RecurringPeriod?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRecurringPeriod(value: String?): RecurringPeriod? {
        return value?.let {
            try {
                RecurringPeriod.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null // Handle invalid enum values gracefully
            }
        }
    }
}

/**
 * WHAT THIS FIXES:
 * - Before: Manual JSON conversion (error-prone)
 * - After: Room handles conversion automatically
 * - Type-safe: Compiler catches errors
 * - No risk of JSON parsing exceptions
 */