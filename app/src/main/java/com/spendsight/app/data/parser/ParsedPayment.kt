package com.spendsight.app.data.parser

import com.spendsight.app.domain.model.PaymentMethod

data class ParsedPayment(
    val amount: Double,
    val merchant: String,
    val isDebit: Boolean = true,
    val accountLast4: String? = null,
    val bankName: String? = null,
    val upiRef: String? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val source: String, // "NOTIFICATION_GPAY", "NOTIFICATION_PHONEPE", "NOTIFICATION_PAYTM", "NOTIFICATION_BANK", "SMS"
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis()
)

