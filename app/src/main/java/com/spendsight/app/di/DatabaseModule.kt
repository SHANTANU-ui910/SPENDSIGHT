package com.spendsight.app.di

import android.content.Context
import com.spendsight.app.data.local.SpendSightDatabase
import com.spendsight.app.data.local.dao.BudgetDao
import com.spendsight.app.data.local.dao.CategoryDao
import com.spendsight.app.data.local.dao.ExpenseDao
import com.spendsight.app.data.local.dao.MerchantPatternDao
import com.spendsight.app.data.preferences.SpendSightPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideSpendSightDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): SpendSightDatabase {
        return SpendSightDatabase.buildDatabase(context, scope)
    }

    @Provides
    fun provideExpenseDao(database: SpendSightDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideCategoryDao(database: SpendSightDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideMerchantPatternDao(database: SpendSightDatabase): MerchantPatternDao {
        return database.merchantPatternDao()
    }

    @Provides
    fun provideBudgetDao(database: SpendSightDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideSpendSightPreferences(@ApplicationContext context: Context): SpendSightPreferences {
        return SpendSightPreferences(context)
    }
}

