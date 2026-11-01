package com.spendsight.app.ui.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsight.app.data.local.dao.CategorySpendTuple
import com.spendsight.app.data.local.dao.MerchantSpendTuple
import com.spendsight.app.data.preferences.SpendSightPreferences
import com.spendsight.app.domain.model.AnalyticsSummary
import com.spendsight.app.domain.model.BudgetStatus
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.model.TimeFilter
import com.spendsight.app.domain.model.TimePeriod
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import com.spendsight.app.ui.components.DaySpend
import com.spendsight.app.ui.components.DonutSegment
import com.spendsight.app.ui.theme.CategoryBillsChroma
import com.spendsight.app.ui.theme.CategoryFoodChroma
import com.spendsight.app.ui.theme.CategoryGroceriesChroma
import com.spendsight.app.ui.theme.CategoryHealthChroma
import com.spendsight.app.ui.theme.CategoryOtherChroma
import com.spendsight.app.ui.theme.CategoryRentChroma
import com.spendsight.app.ui.theme.CategoryShoppingChroma
import com.spendsight.app.ui.theme.CategoryTravelChroma
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val timeFilter: TimeFilter = TimeFilter(period = TimePeriod.WEEK),
    val analyticsSummary: AnalyticsSummary = AnalyticsSummary(),
    val donutSegments: List<DonutSegment> = emptyList(),
    val dailySpends: List<DaySpend> = emptyList(),
    val topMerchants: List<MerchantSpendTuple> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val budgetStatuses: List<BudgetStatus> = emptyList(),
    val isBiometricRequired: Boolean = false,
    val isBiometricAuthenticated: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val preferences: SpendSightPreferences
) : ViewModel() {

    private val timeFilterState = MutableStateFlow(TimeFilter(period = TimePeriod.WEEK))
    private val searchQueryState = MutableStateFlow("")
    private val biometricAuthenticatedState = MutableStateFlow(false)

    // Reactive pipeline driven by timeFilter and search
    val uiState: StateFlow<DashboardUiState> = combine(
        timeFilterState,
        searchQueryState,
        biometricAuthenticatedState,
        preferences.isBiometricEnabled
    ) { filter, query, isAuth, isBioEnabled ->
        FilterState(filter, query, isAuth, isBioEnabled)
    }.flatMapLatest { state ->
        val summaryFlow = expenseRepository.getAnalyticsSummary(state.filter)
        val categoryBreakdownFlow = expenseRepository.getCategorySpendBreakdown(state.filter)
        val dailySpendFlow = expenseRepository.getDailySpendBreakdown(state.filter)
        val topMerchantsFlow = expenseRepository.getTopMerchants(state.filter, limit = 5)
        val budgetStatusesFlow = categoryRepository.getBudgetStatuses(state.filter)
        val expensesFlow = if (state.query.isBlank()) {
            expenseRepository.getExpenses(state.filter)
        } else {
            expenseRepository.searchExpenses(
                query = state.query,
                categoryId = null,
                paymentMethod = null,
                minAmount = null,
                maxAmount = null,
                timeFilter = state.filter
            )
        }

        combine(
            summaryFlow,
            categoryBreakdownFlow,
            dailySpendFlow,
            topMerchantsFlow,
            budgetStatusesFlow,
            expensesFlow
        ) { summary, catBreakdowns, dailyList, merchants, budgets, expenses ->

            val donutSegments = catBreakdowns.map { tuple ->
                DonutSegment(
                    categoryName = tuple.categoryName,
                    amount = tuple.totalAmount,
                    color = resolveCategoryColor(tuple.categoryId)
                )
            }

            val daySpends = formatDailySpends(dailyList, state.filter)

            DashboardUiState(
                timeFilter = state.filter,
                analyticsSummary = summary,
                donutSegments = donutSegments,
                dailySpends = daySpends,
                topMerchants = merchants,
                recentExpenses = expenses,
                budgetStatuses = budgets,
                isBiometricRequired = state.isBioEnabled,
                isBiometricAuthenticated = state.isAuth,
                searchQuery = state.query,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun setTimePeriod(period: TimePeriod) {
        timeFilterState.value = TimeFilter(period = period)
    }

    fun setCustomRange(startEpochMs: Long, endEpochMs: Long) {
        timeFilterState.value = TimeFilter(
            period = TimePeriod.CUSTOM,
            customStartEpochMs = startEpochMs,
            customEndEpochMs = endEpochMs
        )
    }

    fun setSearchQuery(query: String) {
        searchQueryState.value = query
    }

    fun onBiometricAuthSuccess() {
        biometricAuthenticatedState.value = true
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun exportToCsv(context: Context): Uri? {
        val expenses = uiState.value.recentExpenses
        if (expenses.isEmpty()) return null

        val csvHeader = "ID,Date,Time,Amount,Merchant,Category,PaymentMethod,Bank,AccountLast4,UPIRef,Notes,Type\n"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val csvBody = expenses.joinToString("\n") { e ->
            val date = dateFormat.format(Date(e.timestamp))
            val time = timeFormat.format(Date(e.timestamp))
            val type = if (e.isIncome) "INCOME" else "EXPENSE"
            "${e.id},\"$date\",\"$time\",${e.amount},\"${e.merchant}\",\"${e.categoryName}\",\"${e.paymentMethod.name}\",\"${e.bankName.orEmpty()}\",\"${e.accountLast4.orEmpty()}\",\"${e.upiRef.orEmpty()}\",\"${e.notes.orEmpty()}\",\"$type\""
        }

        val file = File(context.cacheDir, "spendsight_export_${System.currentTimeMillis()}.csv")
        file.writeText(csvHeader + csvBody)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun resolveCategoryColor(categoryId: Long): Color {
        return when (categoryId) {
            1L -> CategoryFoodChroma
            2L -> CategoryTravelChroma
            3L -> CategoryBillsChroma
            4L -> CategoryShoppingChroma
            5L -> CategoryHealthChroma
            6L -> CategoryRentChroma
            7L -> CategoryGroceriesChroma
            else -> CategoryOtherChroma
        }
    }

    private fun formatDailySpends(
        tuples: List<com.spendsight.app.data.local.dao.DailySpendTuple>,
        filter: TimeFilter
    ): List<DaySpend> {
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        if (tuples.isEmpty()) {
            return daysOfWeek.map { DaySpend(it, 0.0) }
        }

        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val tupleMap = tuples.associate {
            sdf.format(Date(it.dayTimestamp)) to it.totalAmount
        }

        return daysOfWeek.map { day ->
            DaySpend(day, tupleMap[day] ?: 0.0)
        }
    }

    private data class FilterState(
        val filter: TimeFilter,
        val query: String,
        val isAuth: Boolean,
        val isBioEnabled: Boolean
    )
}

