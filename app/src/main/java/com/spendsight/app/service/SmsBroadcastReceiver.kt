package com.spendsight.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.spendsight.app.data.parser.PaymentParserEngine
import com.spendsight.app.data.preferences.SpendSightPreferences
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var preferences: SpendSightPreferences

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SpendSightSMS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        receiverScope.launch {
            try {
                // Check if user has opted in to SMS detection in settings
                val isSmsEnabled = preferences.isSmsDetectionEnabled.first()
                if (!isSmsEnabled) return@launch

                // Concatenate multi-part SMS if any
                val sender = messages[0].originatingAddress.orEmpty()
                val bodyBuilder = StringBuilder()
                var timestamp = messages[0].timestampMillis

                for (msg in messages) {
                    bodyBuilder.append(msg.messageBody)
                    if (msg.timestampMillis > 0) timestamp = msg.timestampMillis
                }

                val fullBody = bodyBuilder.toString()
                Log.d(TAG, "Incoming SMS from $sender: $fullBody")

                // Parse SMS
                val parsed = PaymentParserEngine.parse(
                    title = sender,
                    body = fullBody,
                    packageName = null,
                    timestamp = timestamp
                ) ?: return@launch

                Log.d(TAG, "Parsed debit/credit SMS: ₹${parsed.amount} at ${parsed.merchant}")

                // Check Deduplication (in case GPay/PhonePe notification already arrived)
                val isDup = expenseRepository.isDuplicate(
                    upiRef = parsed.upiRef,
                    amount = parsed.amount,
                    merchant = parsed.merchant,
                    timestamp = parsed.timestamp
                )

                if (isDup) {
                    Log.d(TAG, "SMS Deduplication: payment already captured via notification. Merged.")
                    return@launch
                }

                // Query Self-Learning Category
                val suggestedCategoryId = expenseRepository.suggestCategoryForMerchant(parsed.merchant) ?: 10L
                val category = categoryRepository.getCategoryById(suggestedCategoryId)
                val categoryName = category?.name ?: "Other"

                // Save to local Room ledger
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
                    detectionSource = "SMS",
                    timestamp = parsed.timestamp
                )

                val insertedId = expenseRepository.insertExpense(expense)

                // Trigger Overlay Service
                val overlayIntent = Intent(context, PaymentOverlayService::class.java).apply {
                    putExtra(SpendNotificationListenerService.EXTRA_EXPENSE_ID, insertedId)
                    putExtra(SpendNotificationListenerService.EXTRA_AMOUNT, parsed.amount)
                    putExtra(SpendNotificationListenerService.EXTRA_MERCHANT, parsed.merchant)
                    putExtra(SpendNotificationListenerService.EXTRA_SUGGESTED_CATEGORY_ID, suggestedCategoryId)
                    putExtra(SpendNotificationListenerService.EXTRA_SUGGESTED_CATEGORY_NAME, categoryName)
                    putExtra(SpendNotificationListenerService.EXTRA_UPI_REF, parsed.upiRef)
                    putExtra(SpendNotificationListenerService.EXTRA_ACCOUNT_LAST4, parsed.accountLast4)
                    putExtra(SpendNotificationListenerService.EXTRA_TIMESTAMP, parsed.timestamp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(overlayIntent)
                } else {
                    context.startService(overlayIntent)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in SMS Broadcast Receiver", e)
            }
        }
    }
}

