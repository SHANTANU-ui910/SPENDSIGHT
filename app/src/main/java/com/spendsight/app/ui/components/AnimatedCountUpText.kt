package com.spendsight.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.formatChromaCurrency

/**
 * AnimatedCountUpText:
 * Executes an 800ms ease-out count up on monetary values with tabular figure formatting
 * and superscript decimal cents. Respects the accessibility reduce-motion setting.
 */
@Composable
fun AnimatedCountUpText(
    targetAmount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color = TextPureWhite,
    currencySymbol: String = "₹",
    showDecimals: Boolean = true
) {
    val reduceMotion = LocalReduceMotion.current
    val animatableValue = remember { Animatable(if (reduceMotion) targetAmount.toFloat() else 0f) }

    LaunchedEffect(targetAmount, reduceMotion) {
        if (reduceMotion) {
            animatableValue.snapTo(targetAmount.toFloat())
        } else {
            animatableValue.animateTo(
                targetValue = targetAmount.toFloat(),
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_COUNT_UP,
                    easing = MotionTokens.EaseOutQuart
                )
            )
        }
    }

    val currentAmount = animatableValue.value.toDouble()
    val annotatedString = formatChromaCurrency(
        amount = currentAmount,
        currencySymbol = currencySymbol,
        showDecimals = showDecimals
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color
    )
}

