package com.spendsight.app.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentParserEngineTest {

    @Test
    fun parse_gpayPayment_extractsCorrectValues() {
        val result = PaymentParserEngine.parse(
            title = "Google Pay",
            body = "Paid ₹420.00 to Merchant Cafe. UPI Ref: 123456789012",
            packageName = PaymentParserEngine.PKG_GPAY
        )

        assertNotNull(result)
        assertEquals(420.00, result!!.amount, 0.001)
        assertTrue(result.merchant.contains("Merchant Cafe", ignoreCase = true))
        assertEquals("123456789012", result.upiRef)
        assertTrue(result.isDebit)
    }

    @Test
    fun parse_phonepePayment_extractsCorrectValues() {
        val result = PaymentParserEngine.parse(
            title = "PhonePe",
            body = "Payment of ₹1,250 to Food Service was successful.",
            packageName = PaymentParserEngine.PKG_PHONEPE
        )

        assertNotNull(result)
        assertEquals(1250.00, result!!.amount, 0.001)
        assertTrue(result.merchant.contains("Food Service", ignoreCase = true))
        assertTrue(result.isDebit)
    }

    @Test
    fun parse_bankDebitSms_extractsAccountAndMerchant() {
        val result = PaymentParserEngine.parse(
            title = "VK-BANK",
            body = "Dear Customer, INR 2,499.00 debited from A/c ending 9999 to Merchant via UPI Ref 123456789012.",
            packageName = null
        )

        assertNotNull(result)
        assertEquals(2499.00, result!!.amount, 0.001)
        assertEquals("9999", result.accountLast4)
        assertEquals("123456789012", result.upiRef)
        assertTrue(result.merchant.contains("Merchant", ignoreCase = true))
    }

    @Test
    fun parse_otpMessage_isIgnored() {
        val result = PaymentParserEngine.parse(
            title = "Bank Alert",
            body = "Your OTP for transaction verification is 123456. Do not share with anyone.",
            packageName = null
        )
        assertNull("OTP message must be discarded", result)
    }

    @Test
    fun parse_promoMessage_isIgnored() {
        val result = PaymentParserEngine.parse(
            title = "Paytm Deals",
            body = "Congratulations! Get ₹100 cashback on your next order. Use code CASH100.",
            packageName = PaymentParserEngine.PKG_PAYTM
        )
        assertNull("Promo message must be discarded", result)
    }
}

