package com.gis.smartfinance.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    // ✅ ADDED #18: Paginated transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsPaged(): PagingSource<Int, FinancialTransaction>

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

    // ✅ ADDED #18: Paginated transactions by date range
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsBetweenPaged(
        startDate: Long,
        endDate: Long
    ): PagingSource<Int, FinancialTransaction>

    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(category: String): Flow<List<FinancialTransaction>>

    // ✅ ADDED #18: Paginated transactions by category
    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategoryPaged(category: String): PagingSource<Int, FinancialTransaction>

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

    // ✅ ADDED #18: Paginated search
    @Query("""
        SELECT * FROM transactions 
        WHERE description LIKE '%' || :query || '%' 
        OR merchantName LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchTransactionsPaged(query: String): PagingSource<Int, FinancialTransaction>

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

data class CategoryTotal(
    val category: String,
    val total: Double
)