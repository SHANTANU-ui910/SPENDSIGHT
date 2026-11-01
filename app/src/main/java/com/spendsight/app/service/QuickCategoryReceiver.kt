package com.spendsight.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuickCategoryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ACTION_CATEGORIZE = "com.spendsight.app.ACTION_CATEGORIZE"
        const val ACTION_DISMISS = "com.spendsight.app.ACTION_DISMISS"
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L)
        val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancel(notifId)

        if (intent.action == ACTION_CATEGORIZE && expenseId > 0 && categoryId > 0) {
            scope.launch {
                try {
                    val expense = expenseRepository.getExpenseById(expenseId).first() ?: return@launch
                    val category = categoryRepository.getCategoryById(categoryId) ?: return@launch

                    val updatedExpense = expense.copy(
                        categoryId = category.id,
                        categoryName = category.name
                    )
                    expenseRepository.updateExpense(updatedExpense)
                    Log.d("QuickCategoryReceiver", "Categorized expense $expenseId as ${category.name}")
                } catch (e: Exception) {
                    Log.e("QuickCategoryReceiver", "Failed to update category", e)
                }
            }
        }
    }
}

