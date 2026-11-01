package com.spendsight.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merchant_patterns",
    indices = [
        Index(value = ["merchantPattern"], unique = true),
        Index(value = ["categoryId"])
    ]
)
data class MerchantPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantPattern: String, // Normalized uppercase merchant token, e.g. "SUPERMART", "COFFEE"
    val categoryId: Long,
    val hitCount: Int = 1,
    val lastMatchedTimestamp: Long = System.currentTimeMillis()
)

