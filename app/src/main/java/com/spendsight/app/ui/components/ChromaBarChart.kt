package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.CobaltFlux
import com.spendsight.app.ui.theme.CrimsonLaser
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SolarAmber
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

data class DaySpend(
    val dayLabel: String,
    val amount: Double
)

/**
 * ChromaBarChart:
 * Futuristic cyber-finance vertical bar chart with gradient energy pill bars,
 * peak spend detection, and touch selection.
 */
@Composable
fun ChromaBarChart(
    data: List<DaySpend>,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val reduceMotion = LocalReduceMotion.current
    val maxSpend = remember(data) { (data.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(100.0) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val animationProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(data, reduceMotion) {
        if (reduceMotion) {
            animationProgress.snapTo(1f)
        } else {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_CHART_ANIMATION,
                    easing = MotionTokens.EaseOutQuart
                )
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Active selected day inspector readout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAILY SPEND VELOCITY",
                color = TextMutedSubtext,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            val active = selectedIndex?.let { data.getOrNull(it) }
            if (active != null) {
                Text(
                    text = "${active.dayLabel}: ₹${active.amount.toInt()}",
                    color = HyperTeal,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "TAP BAR TO INSPECT",
                    color = TextMutedSubtext.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // The Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, item ->
                val barFraction = ((item.amount / maxSpend) * animationProgress.value).toFloat().coerceIn(0.04f, 1f)
                val isSelected = selectedIndex == index
                val isPeak = item.amount == maxSpend && item.amount > 0

                val barColor = when {
                    isPeak -> CrimsonLaser
                    isSelected -> HyperTeal
                    else -> CobaltFlux
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                selectedIndex = if (selectedIndex == index) null else index
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        barColor,
                                        barColor.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.dayLabel,
                        color = if (isSelected || isPeak) TextPureWhite else TextMutedSubtext,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected || isPeak) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

