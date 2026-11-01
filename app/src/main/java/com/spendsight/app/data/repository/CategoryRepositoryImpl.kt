package com.spendsight.app.data.repository

import com.spendsight.app.data.local.dao.CategoryDao
import com.spendsight.app.data.local.dao.ExpenseDao
import com.spendsight.app.data.local.entity.CategoryEntity
import com.spendsight.app.domain.model.BudgetStatus
import com.spendsight.app.domain.model.Category
import com.spendsight.app.domain.model.TimeFilter
import com.spendsight.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getBudgetStatuses(timeFilter: TimeFilter): Flow<List<BudgetStatus>> {
        val categoriesFlow = categoryDao.getAllCategories()
        val breakdownFlow = expenseDao.getCategorySpendBreakdown(timeFilter.toStartEpochMs(), timeFilter.toEndEpochMs())

        return combine(categoriesFlow, breakdownFlow) { categories, breakdowns ->
            val spendMap = breakdowns.associate { it.categoryId to it.totalAmount }

            categories
                .filter { (it.budgetLimit ?: 0.0) > 0.0 }
                .map { category ->
                    val spent = spendMap[category.id] ?: 0.0
                    val limit = category.budgetLimit ?: 0.0
                    val percentage = if (limit > 0) ((spent / limit) * 100).toInt() else 0

                    BudgetStatus(
                        categoryId = category.id,
                        categoryName = category.name,
                        colorHex = category.colorHex,
                        spentAmount = spent,
                        limitAmount = limit,
                        percentageSpent = percentage,
                        isOverLimit = spent > limit,
                        isNearWarning = percentage in 85..100
                    )
                }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateBudgetLimit(categoryId: Long, budgetLimit: Double?) {
        categoryDao.updateBudgetLimit(categoryId, budgetLimit)
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            budgetLimit = budgetLimit,
            isDefault = isDefault
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            budgetLimit = budgetLimit,
            isDefault = isDefault
        )
    }
}

