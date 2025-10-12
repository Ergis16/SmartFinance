package com.gis.smartfinance.data.dao

import androidx.room.*
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * FIXED DAO - All Room annotation errors resolved
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): FinancialTransaction?

    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsBetween(
        startDate: Long,
        endDate: Long
    ): Flow<List<FinancialTransaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(category: String): Flow<List<FinancialTransaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE type = :type 
        ORDER BY date DESC
    """)
    fun getTransactionsByType(type: TransactionType): Flow<List<FinancialTransaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = :type
    """)
    fun getTotalByType(type: TransactionType): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = :type 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalByTypeAndDateRange(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double

    /**
     * FIXED: Returns List<CategoryTotal> instead of Map
     * We'll convert to Map in Repository
     */
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE type = :type 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun getCategoryTotalsList(type: TransactionType): Flow<List<CategoryTotal>>

    @Query("""
        SELECT * FROM transactions 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int = 10): Flow<List<FinancialTransaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE description LIKE '%' || :query || '%' 
        OR merchantName LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchTransactions(query: String): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinancialTransaction>)

    @Update
    suspend fun updateTransaction(transaction: FinancialTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>

    @Query("SELECT MIN(date) FROM transactions")
    suspend fun getOldestTransactionDate(): Long?

    @Query("SELECT MAX(date) FROM transactions")
    suspend fun getNewestTransactionDate(): Long?
}

/**
 * Data class for category totals
 * Room can map query results to this
 */
data class CategoryTotal(
    val category: String,
    val total: Double
)