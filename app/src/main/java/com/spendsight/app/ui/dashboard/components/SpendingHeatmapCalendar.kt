package com.spendsight.app.ui.dashboard.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * SpendingHeatmapCalendar:
 * 30-day intensity grid visualizing daily financial burn patterns.
 */
@Composable
fun SpendingHeatmapCalendar(
    dailySpendMap: Map<Int, Double>, // Day 1..30 -> Spend amount
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    val maxSpend = remember(dailySpendMap) { (dailySpendMap.values.maxOrNull() ?: 1.0).coerceAtLeast(100.0) }

    ChromaMorphicCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "30-DAY SPENDING HEATMAP",
                    color = TextMutedSubtext,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (selectedDay != null) {
                    val amt = dailySpendMap[selectedDay] ?: 0.0
                    Text(
                        text = "Day $selectedDay: ₹${amt.toInt()}",
                        color = HyperTeal,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "INTENSITY",
                        color = TextMutedSubtext.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 rows x 6 columns = 30 days grid
            val daysList = (1..30).toList()
            val chunked = daysList.chunked(6)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                chunked.forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowDays.forEach { day ->
                            val amount = dailySpendMap[day] ?: 0.0
                            val ratio = (amount / maxSpend).toFloat()

                            val cellColor = when {
                                amount <= 0.0 -> Color.White.copy(alpha = 0.04f)
                                ratio < 0.25f -> PhosphorMint.copy(alpha = 0.25f)
                                ratio < 0.65f -> PhosphorMint.copy(alpha = 0.60f)
                                else -> PhosphorMint
                            }

                            val isSelected = selectedDay == day

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HyperTeal else cellColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            selectedDay = if (selectedDay == day) null else day
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    color = if (isSelected) Color.Black else TextPureWhite.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

