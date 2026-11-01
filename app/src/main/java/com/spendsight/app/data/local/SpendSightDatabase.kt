package com.spendsight.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendsight.app.data.local.dao.BudgetDao
import com.spendsight.app.data.local.dao.CategoryDao
import com.spendsight.app.data.local.dao.ExpenseDao
import com.spendsight.app.data.local.dao.MerchantPatternDao
import com.spendsight.app.data.local.entity.BudgetEntity
import com.spendsight.app.data.local.entity.CategoryEntity
import com.spendsight.app.data.local.entity.ExpenseEntity
import com.spendsight.app.data.local.entity.MerchantPatternEntity
import com.spendsight.app.data.local.entity.RecurringRuleEntity
import com.spendsight.app.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        MerchantPatternEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpendSightDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantPatternDao(): MerchantPatternDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "spendsight_ledger.db"

        fun buildDatabase(context: Context, scope: CoroutineScope): SpendSightDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SpendSightDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.beginTransaction()
                    try {
                        Category.DEFAULT_CATEGORIES.forEach { cat ->
                            val budget = cat.budgetLimit ?: "NULL"
                            db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, colorHex, budgetLimit, isDefault) VALUES (${cat.id}, '${cat.name}', '${cat.iconName}', '${cat.colorHex}', $budget, 1);")
                        }
                        val patterns = listOf(
                            Pair("FOOD", 1L),
                            Pair("RESTAURANT", 1L),
                            Pair("CAFE", 1L),
                            Pair("COFFEE", 1L),
                            Pair("TRAVEL", 2L),
                            Pair("CAB", 2L),
                            Pair("TAXI", 2L),
                            Pair("METRO", 2L),
                            Pair("TRANSIT", 2L),
                            Pair("BILL", 3L),
                            Pair("UTILITY", 3L),
                            Pair("ELECTRICITY", 3L),
                            Pair("SHOPPING", 4L),
                            Pair("STORE", 4L),
                            Pair("RETAIL", 4L),
                            Pair("HEALTH", 5L),
                            Pair("PHARMACY", 5L),
                            Pair("MEDICAL", 5L),
                            Pair("GROCERY", 7L),
                            Pair("SUPERMART", 7L),
                            Pair("ENTERTAINMENT", 8L),
                            Pair("CINEMA", 8L),
                            Pair("INVESTMENT", 9L)
                        )
                        val now = System.currentTimeMillis()
                        patterns.forEach { (pattern, catId) ->
                            db.execSQL("INSERT OR IGNORE INTO merchant_patterns (merchantPattern, categoryId, hitCount, lastMatchedTimestamp) VALUES ('$pattern', $catId, 5, $now);")
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}

