package com.spendsight.app.data.parser

import com.spendsight.app.domain.model.PaymentMethod
import java.util.regex.Pattern

object PaymentParserEngine {

    // Target Payment Apps
    const val PKG_GPAY = "com.google.android.apps.nbu.paisa.user"
    const val PKG_PHONEPE = "com.phonepe.app"
    const val PKG_PAYTM = "net.one97.paytm"
    const val PKG_BHIM = "in.org.npci.upiapp"
    const val PKG_CRED = "com.dreamplug.androidapp"
    const val PKG_AMAZON_PAY = "in.amazon.mShop.android.shopping"

    val TRACKED_PAYMENT_PACKAGES = setOf(
        PKG_GPAY,
        PKG_PHONEPE,
        PKG_PAYTM,
        PKG_BHIM,
        PKG_CRED,
        PKG_AMAZON_PAY,
        "com.snapwork.hdfc",        // HDFC Bank MobileBanking
        "com.sbi.lotusintouch",      // SBI YONO
        "com.upi.sbi",              // BHIM SBI Pay
        "com.csam.icici.bank.imobile", // ICICI iMobile
        "com.axis.mobile",          // Axis Mobile
        "com.msf.kbank.mobile",     // Kotak 811
        "com.indusind.mobilebanking"
    )

    // Regex for OTP & Spam disqualification
    private val OTP_PATTERN = Pattern.compile(
        "\\b(otp|one time password|verification code|secret code|do not share|valid for|is your login)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val PROMO_PATTERN = Pattern.compile(
        "\\b(cashback on your next|use code|win up to|congratulations|apply now|pre-approved|discount code)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val REQUEST_PATTERN = Pattern.compile(
        "\\b(requested money|request to pay|has requested)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for extracting Monetary Amount: Handles ₹, Rs, Rs., INR with commas and optional decimals
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:(?:Rs\\.?|INR|₹)\\s*|Rs\\s+)([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for Debit / Inflow distinction
    private val DEBIT_KEYWORDS = Pattern.compile(
        "\\b(paid|debited|spent|sent to|deducted|payment to|withdrawn|transferred to|charged)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val CREDIT_KEYWORDS = Pattern.compile(
        "\\b(credited|received|refunded|deposited|cashback|payment received|salary|deposit|inward|added)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for Merchant / Sender Name extraction
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?:received from|sent by|payment from|transfer from|credited by|from)\\s+([A-Za-z0-9&.,' -]+?)(?:\\s+(?:using|on|via|ref|txn|for|successful|bal|at|avl|upi|in|a/c)\\b|[.]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([A-Za-z0-9&.,' -]+?)\\s+(?:sent you|has sent you|paid you)\\s+(?:Rs\\.?|INR|₹)?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:paid to|sent to|payment to|transfer to|to)\\s+([A-Za-z0-9&.,' -]+?)(?:\\s+(?:using|on|via|ref|txn|for|successful|bal|at|avl|upi)\\b|[.]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:at|at merchant)\\s+([A-Za-z0-9&.,' -]+?)(?:\\s+(?:on|via|ref|txn|bal|avl|for)\\b|[.]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:debited.*(?:to|at))\\s+([A-Za-z0-9&.,' -]+?)(?:\\s+(?:on|via|ref|bal)\\b|[.]|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("VPA\\s+([a-zA-Z0-9.\\-_]+@[a-zA-Z]+)", Pattern.CASE_INSENSITIVE)
    )

    // Regex for Account Last 4 Digits
    private val ACCOUNT_PATTERN = Pattern.compile(
        "(?:a/c|acct|account|card)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with)?)?\\s*[*xX]*([0-9]{4})\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for UPI Reference / UTR
    private val UPI_REF_PATTERN = Pattern.compile(
        "(?:UPI Ref(?:erence)?|Ref no|UTR|UPI:|Txn ID)\\s*[:#]?\\s*([0-9]{10,12})\\b",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Determines whether an incoming notification or SMS is eligible for parsing.
     */
    fun isEligiblePaymentText(text: String): Boolean {
        if (text.isBlank()) return false
        if (OTP_PATTERN.matcher(text).find()) return false
        if (PROMO_PATTERN.matcher(text).find()) return false
        if (REQUEST_PATTERN.matcher(text).find()) return false

        val hasAmount = AMOUNT_PATTERN.matcher(text).find()
        val hasDebit = DEBIT_KEYWORDS.matcher(text).find()
        val hasCredit = CREDIT_KEYWORDS.matcher(text).find()

        return hasAmount && (hasDebit || hasCredit)
    }

    /**
     * Core Parsing Engine: Extracts amount, merchant, last 4 digits, UPI ref, and source.
     */
    fun parse(
        title: String?,
        body: String?,
        packageName: String?,
        timestamp: Long = System.currentTimeMillis()
    ): ParsedPayment? {
        val fullText = "${title.orEmpty()} ${body.orEmpty()}".trim()
        if (!isEligiblePaymentText(fullText)) {
            return null
        }

        // 1. Extract Amount
        val amountMatcher = AMOUNT_PATTERN.matcher(fullText)
        if (!amountMatcher.find()) return null
        val rawAmountStr = amountMatcher.group(1)?.replace(",", "") ?: return null
        val amount = rawAmountStr.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        // 2. Determine Debit vs Credit
        val hasCredit = CREDIT_KEYWORDS.matcher(fullText).find()
        val hasDebit = DEBIT_KEYWORDS.matcher(fullText).find()
        val isDebit = if (hasCredit && !hasDebit) {
            false
        } else if (hasCredit && hasDebit) {
            !fullText.contains("paid", ignoreCase = true) && !fullText.contains("debited", ignoreCase = true)
        } else {
            true
        }

        // 3. Extract Merchant / Sender
        var extractedMerchant: String? = null
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(fullText)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 1 && !candidate.equals("UPI", ignoreCase = true)) {
                    extractedMerchant = cleanMerchantName(candidate)
                    break
                }
            }
        }

        val finalMerchant = extractedMerchant ?: fallbackMerchantFromTitle(title, packageName, isDebit)

        // 4. Extract Account Last 4 digits
        var accountLast4: String? = null
        val accountMatcher = ACCOUNT_PATTERN.matcher(fullText)
        if (accountMatcher.find()) {
            accountLast4 = accountMatcher.group(1)
        }

        // 5. Extract UPI Reference
        var upiRef: String? = null
        val upiMatcher = UPI_REF_PATTERN.matcher(fullText)
        if (upiMatcher.find()) {
            upiRef = upiMatcher.group(1)
        }

        // 6. Identify Source and Bank Name
        val source = resolveSource(packageName)
        val bankName = resolveBankName(fullText, packageName)
        val paymentMethod = resolvePaymentMethod(fullText, packageName)

        return ParsedPayment(
            amount = amount,
            merchant = finalMerchant,
            isDebit = isDebit,
            accountLast4 = accountLast4,
            bankName = bankName,
            upiRef = upiRef,
            paymentMethod = paymentMethod,
            source = source,
            rawText = fullText,
            timestamp = timestamp
        )
    }

    private fun cleanMerchantName(raw: String): String {
        return raw
            .replace(Regex("(?i)\\b(via upi|using upi|ref no.*|txn id.*|on \\d{2}.*|bal.*|avl lmt.*|successful|in your bank.*|in your account.*)\\b"), "")
            .replace(Regex("[^a-zA-Z0-9 .&'-]"), "")
            .trim()
            .take(40)
    }

    private fun fallbackMerchantFromTitle(title: String?, packageName: String?, isDebit: Boolean): String {
        if (!title.isNullOrBlank() && 
            !title.contains("Paid", ignoreCase = true) && 
            !title.contains("Alert", ignoreCase = true) &&
            !title.contains("Received", ignoreCase = true)
        ) {
            return title.take(30)
        }
        return when (packageName) {
            PKG_GPAY -> if (isDebit) "Google Pay Merchant" else "Google Pay Contact"
            PKG_PHONEPE -> if (isDebit) "PhonePe Merchant" else "PhonePe Contact"
            PKG_PAYTM -> if (isDebit) "Paytm Merchant" else "Paytm Contact"
            PKG_BHIM -> "BHIM UPI Transfer"
            else -> if (isDebit) "Merchant Transaction" else "Payment Received"
        }
    }

    private fun resolveSource(packageName: String?): String {
        return when (packageName) {
            PKG_GPAY -> "NOTIFICATION_GPAY"
            PKG_PHONEPE -> "NOTIFICATION_PHONEPE"
            PKG_PAYTM -> "NOTIFICATION_PAYTM"
            PKG_BHIM -> "NOTIFICATION_BHIM"
            PKG_CRED -> "NOTIFICATION_CRED"
            PKG_AMAZON_PAY -> "NOTIFICATION_AMAZON_PAY"
            null -> "SMS"
            else -> "NOTIFICATION_BANK"
        }
    }

    private fun resolveBankName(text: String, packageName: String?): String? {
        val upper = text.uppercase()
        return when {
            upper.contains("HDFC") -> "HDFC Bank"
            upper.contains("SBI") || upper.contains("STATE BANK") -> "State Bank of India"
            upper.contains("ICICI") -> "ICICI Bank"
            upper.contains("AXIS") -> "Axis Bank"
            upper.contains("KOTAK") -> "Kotak Mahindra Bank"
            upper.contains("INDUSIND") -> "IndusInd Bank"
            upper.contains("PUNJAB NATIONAL") || upper.contains("PNB") -> "PNB"
            upper.contains("YES BANK") -> "Yes Bank"
            packageName?.contains("hdfc") == true -> "HDFC Bank"
            packageName?.contains("sbi") == true -> "State Bank of India"
            packageName?.contains("icici") == true -> "ICICI Bank"
            packageName?.contains("axis") == true -> "Axis Bank"
            packageName?.contains("kbank") == true -> "Kotak Mahindra Bank"
            else -> null
        }
    }

    private fun resolvePaymentMethod(text: String, packageName: String?): PaymentMethod {
        val upper = text.uppercase()
        return when {
            upper.contains("UPI") || packageName in setOf(PKG_GPAY, PKG_PHONEPE, PKG_PAYTM, PKG_BHIM) -> PaymentMethod.UPI
            upper.contains("CARD") || upper.contains("DEBIT CARD") || upper.contains("CREDIT CARD") -> PaymentMethod.CARD
            upper.contains("NETBANKING") || upper.contains("NET BANKING") || upper.contains("IMPS") || upper.contains("NEFT") -> PaymentMethod.NETBANKING
            else -> PaymentMethod.UPI
        }
    }
}

