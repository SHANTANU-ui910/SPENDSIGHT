package com.spendsight.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["dedupeHash"]),
        Index(value = ["categoryId"]),
        Index(value = ["merchant"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val categoryId: Long,
    val categoryName: String,
    val paymentMethod: String, // "UPI", "CARD", "CASH", "NETBANKING"
    val accountLast4: String? = null,
    val bankName: String? = null,
    val upiRef: String? = null,
    val notes: String? = null,
    val receiptUri: String? = null,
    val isIncome: Boolean = false,
    val isRecurring: Boolean = false,
    val isAutoDetected: Boolean = false,
    val detectionSource: String? = null, // "NOTIFICATION_GPAY", "NOTIFICATION_PHONEPE", "SMS", "MANUAL"
    val timestamp: Long = System.currentTimeMillis(),
    val dedupeHash: String = "" // generated from upiRef or (amount + merchant + time bucket)
)

