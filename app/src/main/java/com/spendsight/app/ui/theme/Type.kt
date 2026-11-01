package com.spendsight.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.util.Locale

// Advanced tabular figures font feature for flawless vertical alignment of ledger columns
private const val TABULAR_NUMBERS_FEATURE = "tnum, zero, ss01"

val SpendSightTypography = Typography(
    // Hero Currency & Key Performance Metrics
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 46.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = TABULAR_NUMBERS_FEATURE
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.75).sp,
        fontFeatureSettings = TABULAR_NUMBERS_FEATURE
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TABULAR_NUMBERS_FEATURE
    ),

    // Technical Section Headers & HUD Titles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.8.sp
    ),

    // Card Titles & Group Labels
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),

    // Ledger Items & Readable Body
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    ),

    // Technical HUD Tags, UPI IDs & Micro Telemetry Badges
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.2.sp
    )
)

/**
 * Unique Financial Typography Formatter:
 * Produces an editorial style formatted currency with superscript decimal cents:
 * Example: "₹ 14,250.75" -> "₹ 14,250" with ".75" as smaller elevated superscript.
 */
@Composable
fun formatChromaCurrency(
    amount: Double,
    currencySymbol: String = "₹",
    showDecimals: Boolean = true
): androidx.compose.ui.text.AnnotatedString {
    val formatter = DecimalFormat("#,##,##0.00")
    val formatted = formatter.format(amount)
    val parts = formatted.split(".")
    val wholePart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else "00"

    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 0.85em)) {
            append("$currencySymbol ")
        }
        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
            append(wholePart)
        }
        if (showDecimals) {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 0.6em,
                    baselineShift = BaselineShift(0.35f)
                )
            ) {
                append(".$decimalPart")
            }
        }
    }
}
