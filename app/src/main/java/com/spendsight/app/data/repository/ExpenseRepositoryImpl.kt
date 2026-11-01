package com.spendsight.app.data.repository

import com.spendsight.app.data.local.dao.CategoryDao
import com.spendsight.app.data.local.dao.CategorySpendTuple
import com.spendsight.app.data.local.dao.DailySpendTuple
import com.spendsight.app.data.local.dao.ExpenseDao
import com.spendsight.app.data.local.dao.MerchantPatternDao
import com.spendsight.app.data.local.dao.MerchantSpendTuple
import com.spendsight.app.data.local.entity.ExpenseEntity
import com.spendsight.app.domain.model.AnalyticsSummary
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.model.PaymentMethod
import com.spendsight.app.domain.model.TimeFilter
import com.spendsight.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val merchantPatternDao: MerchantPatternDao
) : ExpenseRepository {

    override fun getExpenses(timeFilter: TimeFilter): Flow<List<Expense>> {
        val start = timeFilter.toStartEpochMs()
        val end = timeFilter.toEndEpochMs()
        return expenseDao.getExpensesBetween(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(id).map { it?.toDomain() }
    }

    override fun getAnalyticsSummary(timeFilter: TimeFilter): Flow<AnalyticsSummary> {
        val start = timeFilter.toStartEpochMs()
        val end = timeFilter.toEndEpochMs()
        val periodDurationMs = (end - start).coerceAtLeast(1000L)
        val periodDays = (periodDurationMs / 86400000.0).coerceAtLeast(1.0)
        val periodHours = (periodDurationMs / 3600000.0).coerceAtLeast(1.0)

        // Previous period for comparison
        val prevStart = start - periodDurationMs
        val prevEnd = start

        val currentSpendFlow = expenseDao.getTotalSpendBetween(start, end)
        val currentIncomeFlow = expenseDao.getTotalIncomeBetween(start, end)
        val prevSpendFlow = expenseDao.getTotalSpendBetween(prevStart, prevEnd)
        val topCategoryFlow = expenseDao.getCategorySpendBreakdown(start, end)

        return combine(currentSpendFlow, currentIncomeFlow, prevSpendFlow, topCategoryFlow) { spend, income, prevSpend, topCategories ->
            val net = income - spend
            val dailyAvg = spend / periodDays
            val burnRate = spend / periodHours

            val pctChange = if (prevSpend > 0) {
                ((spend - prevSpend) / prevSpend) * 100.0
            } else 0.0

            val topCategory = topCategories.firstOrNull()
            val (headline, detail) = if (topCategory != null) {
                val catShare = if (spend > 0) ((topCategory.totalAmount / spend) * 100).toInt() else 0
                Pair(
                    "Top Outflow: ${topCategory.categoryName} ($catShare%)",
                    if (pctChange > 15) "Spending velocity is up ${pctChange.toInt()}% vs last period."
                    else "Spending velocity is stable across tracked accounts."
                )
            } else {
                Pair("All systems nominal", "Telemetry active and awaiting new payment intercepts.")
            }

            AnalyticsSummary(
                totalExpense = spend,
                totalIncome = income,
                netBalance = net,
                dailyAverage = dailyAvg,
                burnRatePerHour = burnRate,
                percentageVsPreviousPeriod = pctChange,
                smartInsightHeadline = headline,
                smartInsightDetail = detail
            )
        }
    }

    override fun getCategorySpendBreakdown(timeFilter: TimeFilter): Flow<List<CategorySpendTuple>> {
        return expenseDao.getCategorySpendBreakdown(timeFilter.toStartEpochMs(), timeFilter.toEndEpochMs())
    }

    override fun getDailySpendBreakdown(timeFilter: TimeFilter): Flow<List<DailySpendTuple>> {
        return expenseDao.getDailySpendBreakdown(timeFilter.toStartEpochMs(), timeFilter.toEndEpochMs())
    }

    override fun getTopMerchants(timeFilter: TimeFilter, limit: Int): Flow<List<MerchantSpendTuple>> {
        return expenseDao.getTopMerchants(timeFilter.toStartEpochMs(), timeFilter.toEndEpochMs(), limit)
    }

    override fun searchExpenses(
        query: String?,
        categoryId: Long?,
        paymentMethod: PaymentMethod?,
        minAmount: Double?,
        maxAmount: Double?,
        timeFilter: TimeFilter
    ): Flow<List<Expense>> {
        return expenseDao.searchExpenses(
            query = if (query.isNullOrBlank()) null else query.trim(),
            categoryId = categoryId,
            paymentMethod = paymentMethod?.name,
            minAmount = minAmount,
            maxAmount = maxAmount,
            startTime = timeFilter.toStartEpochMs(),
            endTime = timeFilter.toEndEpochMs()
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        val hash = generateDedupeHash(expense.upiRef, expense.merchant, expense.amount, expense.timestamp)
        val entity = expense.toEntity(dedupeHash = hash)
        val id = expenseDao.insertExpense(entity)

        // Learn merchant to category relationship
        if (expense.merchant.isNotBlank() && expense.categoryId > 0) {
            merchantPatternDao.recordMerchantSelection(expense.merchant, expense.categoryId)
        }

        return id
    }

    override suspend fun updateExpense(expense: Expense) {
        val hash = generateDedupeHash(expense.upiRef, expense.merchant, expense.amount, expense.timestamp)
        expenseDao.updateExpense(expense.toEntity(dedupeHash = hash))
        if (expense.merchant.isNotBlank() && expense.categoryId > 0) {
            merchantPatternDao.recordMerchantSelection(expense.merchant, expense.categoryId)
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    override suspend fun isDuplicate(
        upiRef: String?,
        amount: Double,
        merchant: String,
        timestamp: Long
    ): Boolean {
        val hash = generateDedupeHash(upiRef, merchant, amount, timestamp)
        val minTime = timestamp - (10 * 60 * 1000L) // 10 minutes prior
        val maxTime = timestamp + (10 * 60 * 1000L) // 10 minutes after

        val candidate = expenseDao.findDedupeCandidate(
            upiRef = upiRef,
            amount = amount,
            dedupeHash = hash,
            merchant = merchant,
            minTime = minTime,
            maxTime = maxTime
        )
        return candidate != null
    }

    override suspend fun suggestCategoryForMerchant(merchant: String): Long? {
        val pattern = merchantPatternDao.findBestFuzzyMatch(merchant.trim().uppercase())
        return pattern?.categoryId
    }

    override suspend fun recordMerchantCategoryChoice(merchant: String, categoryId: Long) {
        merchantPatternDao.recordMerchantSelection(merchant, categoryId)
    }

    private fun generateDedupeHash(upiRef: String?, merchant: String, amount: Double, timestamp: Long): String {
        // 10-minute time bucket to avoid duplicate SMS + Notification processing of the exact same payment
        val timeBucket = timestamp / (10 * 60 * 1000L)
        val raw = if (!upiRef.isNullOrBlank()) {
            "UPI_${upiRef}_$amount"
        } else {
            "${merchant.trim().uppercase()}_${amount}_$timeBucket"
        }
        val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            merchant = merchant,
            categoryId = categoryId,
            categoryName = categoryName,
            paymentMethod = PaymentMethod.fromString(paymentMethod),
            accountLast4 = accountLast4,
            bankName = bankName,
            upiRef = upiRef,
            notes = notes,
            receiptUri = receiptUri,
            isIncome = isIncome,
            isRecurring = isRecurring,
            isAutoDetected = isAutoDetected,
            detectionSource = detectionSource,
            timestamp = timestamp
        )
    }

    private fun Expense.toEntity(dedupeHash: String = ""): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            amount = amount,
            merchant = merchant,
            categoryId = categoryId,
            categoryName = categoryName,
            paymentMethod = paymentMethod.name,
            accountLast4 = accountLast4,
            bankName = bankName,
            upiRef = upiRef,
            notes = notes,
            receiptUri = receiptUri,
            isIncome = isIncome,
            isRecurring = isRecurring,
            isAutoDetected = isAutoDetected,
            detectionSource = detectionSource,
            timestamp = timestamp,
            dedupeHash = dedupeHash
        )
    }
}

