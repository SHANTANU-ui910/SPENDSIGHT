package com.spendsight.app.domain.repository

import com.spendsight.app.data.local.dao.CategorySpendTuple
import com.spendsight.app.data.local.dao.DailySpendTuple
import com.spendsight.app.data.local.dao.MerchantSpendTuple
import com.spendsight.app.domain.model.AnalyticsSummary
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.model.PaymentMethod
import com.spendsight.app.domain.model.TimeFilter
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(timeFilter: TimeFilter): Flow<List<Expense>>
    fun getExpenseById(id: Long): Flow<Expense?>
    fun getAnalyticsSummary(timeFilter: TimeFilter): Flow<AnalyticsSummary>
    fun getCategorySpendBreakdown(timeFilter: TimeFilter): Flow<List<CategorySpendTuple>>
    fun getDailySpendBreakdown(timeFilter: TimeFilter): Flow<List<DailySpendTuple>>
    fun getTopMerchants(timeFilter: TimeFilter, limit: Int = 10): Flow<List<MerchantSpendTuple>>
    fun searchExpenses(
        query: String?,
        categoryId: Long?,
        paymentMethod: PaymentMethod?,
        minAmount: Double?,
        maxAmount: Double?,
        timeFilter: TimeFilter
    ): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
    suspend fun isDuplicate(upiRef: String?, amount: Double, merchant: String, timestamp: Long): Boolean
    suspend fun suggestCategoryForMerchant(merchant: String): Long?
    suspend fun recordMerchantCategoryChoice(merchant: String, categoryId: Long)
}

