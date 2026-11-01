package com.spendsight.app.domain.repository

import com.spendsight.app.domain.model.BudgetStatus
import com.spendsight.app.domain.model.Category
import com.spendsight.app.domain.model.TimeFilter
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun getCategoryByName(name: String): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateBudgetLimit(categoryId: Long, budgetLimit: Double?)
    fun getBudgetStatuses(timeFilter: TimeFilter): Flow<List<BudgetStatus>>
}

