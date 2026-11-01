package com.spendsight.app.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.domain.model.BudgetStatus
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.theme.CrimsonLaser
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SolarAmber
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * BudgetStatusRingsCard:
 * Real-time category budget telemetry rings with warning thresholds (>85%) and over-limit alarms.
 */
@Composable
fun BudgetStatusRingsCard(
    budgets: List<BudgetStatus>,
    modifier: Modifier = Modifier
) {
    if (budgets.isEmpty()) return

    val reduceMotion = LocalReduceMotion.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CATEGORY BUDGET TARGETS",
                color = TextMutedSubtext,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "${budgets.size} ACTIVE",
                color = TextMutedSubtext,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            budgets.forEach { budget ->
                BudgetRingItem(budget = budget, reduceMotion = reduceMotion)
            }
        }
    }
}

@Composable
private fun BudgetRingItem(
    budget: BudgetStatus,
    reduceMotion: Boolean
) {
    val targetFraction = (budget.spentAmount / budget.limitAmount).toFloat().coerceIn(0f, 1.2f)
    val animatedProgress = remember { Animatable(if (reduceMotion) targetFraction else 0f) }

    LaunchedEffect(budget.spentAmount, reduceMotion) {
        if (reduceMotion) {
            animatedProgress.snapTo(targetFraction)
        } else {
            animatedProgress.animateTo(
                targetValue = targetFraction,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_CHART_ANIMATION,
                    easing = MotionTokens.EaseOutQuart
                )
            )
        }
    }

    val ringColor = when {
        budget.isOverLimit -> CrimsonLaser
        budget.isNearWarning -> SolarAmber
        else -> PhosphorMint
    }

    val itemShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(itemShape)
            .background(SurfaceElevated)
            .border(
                width = 1.dp,
                color = if (budget.isOverLimit) CrimsonLaser.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                shape = itemShape
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Circular Ring with Percentage
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(52.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress.value.coerceAtMost(1f) },
                    color = ringColor,
                    trackColor = Color.White.copy(alpha = 0.08f),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = "${budget.percentageSpent}%",
                    color = if (budget.isOverLimit) CrimsonLaser else TextPureWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = budget.categoryName,
                color = TextPureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "₹${budget.spentAmount.toInt()} / ₹${budget.limitAmount.toInt()}",
                color = TextMutedSubtext,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

