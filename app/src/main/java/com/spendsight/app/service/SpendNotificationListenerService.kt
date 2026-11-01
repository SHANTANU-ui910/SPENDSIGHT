package com.spendsight.app.service

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.spendsight.app.data.parser.PaymentParserEngine
import com.spendsight.app.data.preferences.SpendSightPreferences
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpendNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var preferences: SpendSightPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "SpendSightListener"
        const val ACTION_PAYMENT_INTERCEPTED = "com.spendsight.app.ACTION_PAYMENT_INTERCEPTED"
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_MERCHANT = "extra_merchant"
        const val EXTRA_SUGGESTED_CATEGORY_ID = "extra_suggested_category_id"
        const val EXTRA_SUGGESTED_CATEGORY_NAME = "extra_suggested_category_name"
        const val EXTRA_UPI_REF = "extra_upi_ref"
        const val EXTRA_ACCOUNT_LAST4 = "extra_account_last4"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        val fullBody = if (!bigText.isNullOrBlank()) bigText else text.orEmpty()

        serviceScope.launch {
            try {
                // Check if detection is enabled in preferences
                val isDetectionEnabled = preferences.isNotificationDetectionEnabled.first()
                if (!isDetectionEnabled) return@launch

                // Check eligibility
                if (!PaymentParserEngine.TRACKED_PAYMENT_PACKAGES.contains(pkgName) &&
                    !PaymentParserEngine.isEligiblePaymentText("$title $fullBody")
                ) {
                    return@launch
                }

                // Run Parser
                val parsed = PaymentParserEngine.parse(
                    title = title,
                    body = fullBody,
                    packageName = pkgName,
                    timestamp = sbn.postTime
                ) ?: return@launch

                Log.d(TAG, "Parsed incoming payment: ₹${parsed.amount} at ${parsed.merchant} from $pkgName")

                // Check Deduplication
                val isDup = expenseRepository.isDuplicate(
                    upiRef = parsed.upiRef,
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    timestamp = parsed.timestamp
                )

                if (isDup) {
                    Log.d(TAG, "Deduplication: payment already captured. Suppressing duplicate alert.")
                    return@launch
                }

                // Query Self-Learning Category Suggestion
                val suggestedCategoryId = expenseRepository.suggestCategoryForMerchant(parsed.merchant) ?: 10L // Default "Other"
                val category = categoryRepository.getCategoryById(suggestedCategoryId)
                val categoryName = category?.name ?: "Other"

                // Create and insert into local ledger
                val expense = Expense(
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    categoryId = suggestedCategoryId,
                    categoryName = categoryName,
                    paymentMethod = parsed.paymentMethod,
                    accountLast4 = parsed.accountLast4,
                    bankName = parsed.bankName,
                    upiRef = parsed.upiRef,
                    isIncome = !parsed.isDebit,
                    isAutoDetected = true,
                    detectionSource = parsed.source,
                    timestamp = parsed.timestamp
                )

                val insertedId = expenseRepository.insertExpense(expense)
                Log.d(TAG, "Expense successfully saved to database with ID: $insertedId")

                // Trigger Floating Overlay Pop-up Service or Interactive Heads-Up Notification
                triggerOverlay(
                    expenseId = insertedId,
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    suggestedCategoryId = suggestedCategoryId,
                    suggestedCategoryName = categoryName,
                    upiRef = parsed.upiRef,
                    accountLast4 = parsed.accountLast4,
                    timestamp = parsed.timestamp
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming notification", e)
            }
        }
    }

    private fun triggerOverlay(
        expenseId: Long,
        amount: Double,
        merchant: String,
        suggestedCategoryId: Long,
        suggestedCategoryName: String,
        upiRef: String?,
        accountLast4: String?,
        timestamp: Long
    ) {
        val overlayIntent = Intent(this, PaymentOverlayService::class.java).apply {
            putExtra(EXTRA_EXPENSE_ID, expenseId)
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_MERCHANT, merchant)
            putExtra(EXTRA_SUGGESTED_CATEGORY_ID, suggestedCategoryId)
            putExtra(EXTRA_SUGGESTED_CATEGORY_NAME, suggestedCategoryName)
            putExtra(EXTRA_UPI_REF, upiRef)
            putExtra(EXTRA_ACCOUNT_LAST4, accountLast4)
            putExtra(EXTRA_TIMESTAMP, timestamp)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(overlayIntent)
        } else {
            startService(overlayIntent)
        }
    }
}

