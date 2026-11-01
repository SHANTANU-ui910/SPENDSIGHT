package com.spendsight.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 85
)

@Entity(
    tableName = "recurring_rules",
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val nextDueDate: Long,
    val isActive: Boolean = true
)

