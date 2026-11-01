package com.spendsight.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsight.app.domain.model.Category
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.domain.model.PaymentMethod
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val amountString: String = "0",
    val merchant: String = "",
    val selectedCategoryId: Long = 1L,
    val selectedCategoryName: String = "Food",
    val isIncome: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringFrequency: String = "MONTHLY",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false
) {
    val amountDouble: Double
        get() = amountString.toDoubleOrNull() ?: 0.0
}

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val amountState = MutableStateFlow("0")
    private val merchantState = MutableStateFlow("")
    private val selectedCategoryIdState = MutableStateFlow(1L)
    private val selectedCategoryNameState = MutableStateFlow("Food")
    private val isIncomeState = MutableStateFlow(false)
    private val paymentMethodState = MutableStateFlow(PaymentMethod.UPI)
    private val timestampState = MutableStateFlow(System.currentTimeMillis())
    private val notesState = MutableStateFlow("")
    private val receiptUriState = MutableStateFlow<String?>(null)
    private val isRecurringState = MutableStateFlow(false)
    private val recurringFrequencyState = MutableStateFlow("MONTHLY")
    private val isSavingState = MutableStateFlow(false)

    val uiState: StateFlow<AddExpenseUiState> = combine(
        amountState,
        merchantState,
        selectedCategoryIdState,
        selectedCategoryNameState,
        isIncomeState,
        paymentMethodState,
        timestampState,
        notesState,
        receiptUriState,
        isRecurringState,
        recurringFrequencyState,
        isSavingState,
        categoryRepository.getAllCategories()
    ) { params: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        AddExpenseUiState(
            amountString = params[0] as String,
            merchant = params[1] as String,
            selectedCategoryId = params[2] as Long,
            selectedCategoryName = params[3] as String,
            isIncome = params[4] as Boolean,
            paymentMethod = params[5] as PaymentMethod,
            timestamp = params[6] as Long,
            notes = params[7] as String,
            receiptUri = params[8] as String?,
            isRecurring = params[9] as Boolean,
            recurringFrequency = params[10] as String,
            isSaving = params[11] as Boolean,
            categories = params[12] as List<Category>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

    fun onDigitPress(digit: String) {
        val current = amountState.value
        if (current == "0") {
            amountState.value = digit
        } else {
            // Check decimal places constraint (at most 2 decimal places)
            if (current.contains(".")) {
                val parts = current.split(".")
                if (parts.size > 1 && parts[1].length >= 2) return
            }
            if (current.length < 9) {
                amountState.value = current + digit
            }
        }
    }

    fun onDecimalPress() {
        val current = amountState.value
        if (!current.contains(".")) {
            amountState.value = "$current."
        }
    }

    fun onBackspacePress() {
        val current = amountState.value
        if (current.length > 1) {
            amountState.value = current.dropLast(1)
        } else {
            amountState.value = "0"
        }
    }

    fun onMerchantChange(name: String) {
        merchantState.value = name
        // Auto suggest category from merchant history if user types known merchant
        if (name.length >= 3) {
            viewModelScope.launch {
                val suggestedId = expenseRepository.suggestCategoryForMerchant(name)
                if (suggestedId != null) {
                    val cat = categoryRepository.getCategoryById(suggestedId)
                    if (cat != null) {
                        selectedCategoryIdState.value = cat.id
                        selectedCategoryNameState.value = cat.name
                    }
                }
            }
        }
    }

    fun onCategorySelect(category: Category) {
        selectedCategoryIdState.value = category.id
        selectedCategoryNameState.value = category.name
    }

    fun onPaymentMethodSelect(method: PaymentMethod) {
        paymentMethodState.value = method
    }

    fun onToggleIncome(isIncome: Boolean) {
        isIncomeState.value = isIncome
    }

    fun onNotesChange(notes: String) {
        notesState.value = notes
    }

    fun onReceiptUriSelected(uri: String?) {
        receiptUriState.value = uri
    }

    fun onToggleRecurring(recurring: Boolean) {
        isRecurringState.value = recurring
    }

    fun onFrequencyChange(freq: String) {
        recurringFrequencyState.value = freq
    }

    fun onDateSelected(epochMs: Long) {
        timestampState.value = epochMs
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val amount = amountState.value.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) return

        isSavingState.value = true
        viewModelScope.launch {
            val merchantName = if (merchantState.value.isNotBlank()) merchantState.value.trim() else "Manual Entry"

            val expense = Expense(
                amount = amount,
                merchant = merchantName,
                categoryId = selectedCategoryIdState.value,
                categoryName = selectedCategoryNameState.value,
                paymentMethod = paymentMethodState.value,
                notes = notesState.value.ifBlank { null },
                receiptUri = receiptUriState.value,
                isIncome = isIncomeState.value,
                isRecurring = isRecurringState.value,
                isAutoDetected = false,
                detectionSource = "MANUAL",
                timestamp = timestampState.value
            )

            expenseRepository.insertExpense(expense)
            isSavingState.value = false
            onSuccess()
        }
    }
}

