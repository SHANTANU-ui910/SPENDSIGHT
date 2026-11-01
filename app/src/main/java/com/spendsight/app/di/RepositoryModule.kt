package com.spendsight.app.di

import com.spendsight.app.data.local.dao.CategoryDao
import com.spendsight.app.data.local.dao.ExpenseDao
import com.spendsight.app.data.local.dao.MerchantPatternDao
import com.spendsight.app.data.repository.CategoryRepositoryImpl
import com.spendsight.app.data.repository.ExpenseRepositoryImpl
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao,
        categoryDao: CategoryDao,
        merchantPatternDao: MerchantPatternDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao, categoryDao, merchantPatternDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        expenseDao: ExpenseDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao, expenseDao)
    }
}

