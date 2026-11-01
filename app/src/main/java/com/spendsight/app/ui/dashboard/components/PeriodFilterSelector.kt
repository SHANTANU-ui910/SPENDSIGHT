package com.spendsight.app.ui.dashboard.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.spendsight.app.domain.model.TimePeriod
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.SurfaceObsidian
import com.spendsight.app.ui.theme.TextDarkInverse
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * PeriodFilterSelector:
 * Frosted capsule segmented toggle with 250ms indicator glide and haptic feedback.
 */
@Composable
fun PeriodFilterSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val periods = TimePeriod.entries
    val containerShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(SurfaceObsidian)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                ),
                shape = containerShape
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            periods.forEach { period ->
                val isSelected = period == selectedPeriod
                val pillShape = RoundedCornerShape(12.dp)

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) HyperTeal else Color.Transparent,
                    animationSpec = tween(durationMillis = MotionTokens.DURATION_NAV_PILL, easing = MotionTokens.EaseOutQuart),
                    label = "periodPillBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(pillShape)
                        .background(pillBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!isSelected) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onPeriodSelected(period)
                                }
                            }
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label.uppercase(),
                        color = if (isSelected) TextDarkInverse else TextMutedSubtext,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

