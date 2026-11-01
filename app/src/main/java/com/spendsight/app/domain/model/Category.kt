package com.spendsight.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val budgetLimit: Double? = null,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category(id = 1, name = "Food", iconName = "restaurant", colorHex = "#FF8A00", budgetLimit = 12000.0, isDefault = true),
            Category(id = 2, name = "Travel", iconName = "directions_car", colorHex = "#00C2FF", budgetLimit = 6000.0, isDefault = true),
            Category(id = 3, name = "Bills", iconName = "receipt_long", colorHex = "#FF2A6D", budgetLimit = 8000.0, isDefault = true),
            Category(id = 4, name = "Shopping", iconName = "shopping_bag", colorHex = "#A259FF", budgetLimit = 10000.0, isDefault = true),
            Category(id = 5, name = "Health", iconName = "medical_services", colorHex = "#00FF9D", budgetLimit = 4000.0, isDefault = true),
            Category(id = 6, name = "Rent", iconName = "home", colorHex = "#FF5252", budgetLimit = 25000.0, isDefault = true),
            Category(id = 7, name = "Groceries", iconName = "shopping_cart", colorHex = "#FFD166", budgetLimit = 9000.0, isDefault = true),
            Category(id = 8, name = "Entertainment", iconName = "movie", colorHex = "#7C4DFF", budgetLimit = 5000.0, isDefault = true),
            Category(id = 9, name = "Investments", iconName = "savings", colorHex = "#06D6A0", budgetLimit = 20000.0, isDefault = true),
            Category(id = 10, name = "Other", iconName = "more_horiz", colorHex = "#8E9AAF", budgetLimit = 3000.0, isDefault = true)
        )
    }
}

