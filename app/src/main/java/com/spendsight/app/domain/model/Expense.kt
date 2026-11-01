package com.spendsight.app.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String = "#8E9AAF",
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val accountLast4: String? = null,
    val bankName: String? = null,
    val upiRef: String? = null,
    val notes: String? = null,
    val receiptUri: String? = null,
    val isIncome: Boolean = false,
    val isRecurring: Boolean = false,
    val isAutoDetected: Boolean = false,
    val detectionSource: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

