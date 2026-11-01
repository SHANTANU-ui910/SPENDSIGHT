package com.spendsight.app.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.domain.model.AnalyticsSummary
import com.spendsight.app.ui.components.AnimatedCountUpText
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.theme.CrimsonLaser
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.HyperTealGlow
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SolarAmber
import com.spendsight.app.ui.theme.SpendSightTypography
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextSlateBody
import com.spendsight.app.ui.theme.TextTechnicalCyan
import com.spendsight.app.ui.theme.formatChromaCurrency

/**
 * HeroSpendingTelemetryCard:
 * Primary executive cockpit card displaying real-time financial telemetry,
 * 800ms count-up totals, hourly burn rate, and comparative liquidity metrics.
 */
@Composable
fun HeroSpendingTelemetryCard(
    summary: AnalyticsSummary,
    modifier: Modifier = Modifier
) {
    ChromaMorphicCard(
        modifier = modifier.fillMaxWidth(),
        glowAccent = HyperTealGlow,
        glowRadius = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Telemetry Badge & Hourly Burn Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(HyperTeal)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "TOTAL OUTFLOW",
                        color = TextTechnicalCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                // Hourly Burn Rate Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BURN: ₹${summary.burnRatePerHour.toInt()}/HR",
                        color = TextSlateBody,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Outflow Amount with 800ms Count-Up
            AnimatedCountUpText(
                targetAmount = summary.totalExpense,
                style = SpendSightTypography.displayMedium,
                color = TextPureWhite,
                showDecimals = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Velocity vs Previous Period Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isUp = summary.percentageVsPreviousPeriod > 0
                val varianceColor = if (isUp) CrimsonLaser else PhosphorMint
                val arrow = if (isUp) "▲" else "▼"

                Text(
                    text = "$arrow ${String.format(java.util.Locale.US, "%.1f", Math.abs(summary.percentageVsPreviousPeriod))}%",
                    color = varianceColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "vs previous period",
                    color = TextMutedSubtext,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Fine Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics Row: Inflow | Balance | Daily Average
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Inflow
                Column {
                    Text(
                        text = "INCOME",
                        color = TextMutedSubtext,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatChromaCurrency(summary.totalIncome, showDecimals = false),
                        color = PhosphorMint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Balance
                Column {
                    Text(
                        text = "NET LIQUIDITY",
                        color = TextMutedSubtext,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatChromaCurrency(summary.netBalance, showDecimals = false),
                        color = if (summary.netBalance >= 0) TextPureWhite else CrimsonLaser,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Daily Average
                Column {
                    Text(
                        text = "DAILY AVG",
                        color = TextMutedSubtext,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatChromaCurrency(summary.dailyAverage, showDecimals = false),
                        color = TextSlateBody,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

