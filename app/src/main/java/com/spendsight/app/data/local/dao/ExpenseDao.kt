package com.spendsight.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendsight.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

data class CategorySpendTuple(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double
)

data class DailySpendTuple(
    val dayTimestamp: Long,
    val totalAmount: Double
)

data class MerchantSpendTuple(
    val merchant: String,
    val totalSpend: Double,
    val transactionCount: Int
)

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: Long): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE (upiRef IS NOT NULL AND upiRef = :upiRef AND amount = :amount)
           OR (dedupeHash = :dedupeHash)
           OR (merchant = :merchant AND amount = :amount AND timestamp BETWEEN :minTime AND :maxTime)
        LIMIT 1
    """)
    suspend fun findDedupeCandidate(
        upiRef: String?,
        amount: Double,
        dedupeHash: String,
        merchant: String,
        minTime: Long,
        maxTime: Long
    ): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE isIncome = 0 AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalSpendBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE isIncome = 1 AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double>

    @Query("""
        SELECT categoryId, categoryName, COALESCE(SUM(amount), 0.0) AS totalAmount 
        FROM expenses 
        WHERE isIncome = 0 AND timestamp BETWEEN :startTime AND :endTime
        GROUP BY categoryId, categoryName
        ORDER BY totalAmount DESC
    """)
    fun getCategorySpendBreakdown(startTime: Long, endTime: Long): Flow<List<CategorySpendTuple>>

    @Query("""
        SELECT (timestamp / 86400000) * 86400000 AS dayTimestamp, COALESCE(SUM(amount), 0.0) AS totalAmount
        FROM expenses
        WHERE isIncome = 0 AND timestamp BETWEEN :startTime AND :endTime
        GROUP BY dayTimestamp
        ORDER BY dayTimestamp ASC
    """)
    fun getDailySpendBreakdown(startTime: Long, endTime: Long): Flow<List<DailySpendTuple>>

    @Query("""
        SELECT merchant, COALESCE(SUM(amount), 0.0) AS totalSpend, COUNT(id) AS transactionCount
        FROM expenses
        WHERE isIncome = 0 AND timestamp BETWEEN :startTime AND :endTime
        GROUP BY merchant
        ORDER BY totalSpend DESC
        LIMIT :limit
    """)
    fun getTopMerchants(startTime: Long, endTime: Long, limit: Int = 10): Flow<List<MerchantSpendTuple>>

    @Query("""
        SELECT * FROM expenses
        WHERE (:query IS NULL OR merchant LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
          AND (:minAmount IS NULL OR amount >= :minAmount)
          AND (:maxAmount IS NULL OR amount <= :maxAmount)
          AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
    """)
    fun searchExpenses(
        query: String?,
        categoryId: Long?,
        paymentMethod: String?,
        minAmount: Double?,
        maxAmount: Double?,
        startTime: Long,
        endTime: Long
    ): Flow<List<ExpenseEntity>>
}

