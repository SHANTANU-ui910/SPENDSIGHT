package com.spendsight.app.domain.model

enum class PaymentMethod(val displayName: String) {
    UPI("UPI"),
    CARD("Debit / Credit Card"),
    CASH("Cash"),
    NETBANKING("Net Banking"),
    WALLET("Digital Wallet");

    companion object {
        fun fromString(value: String?): PaymentMethod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UPI
        }
    }
}

