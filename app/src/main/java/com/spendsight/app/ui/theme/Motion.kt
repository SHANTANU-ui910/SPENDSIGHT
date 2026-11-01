package com.spendsight.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext

/**
 * SpendSight Motion & Micro-interaction Tokens
 * Exact timing specifications and accessibility adaptations
 */
object MotionTokens {
    // Exact durations in milliseconds
    const val DURATION_LOGO_DRAW = 600
    const val DURATION_SPLASH_FADE = 300
    const val DURATION_COUNT_UP = 800
    const val DURATION_CHART_ANIMATION = 700
    const val STAGGER_CHART_SEGMENT = 60
    const val DURATION_CARD_PRESS = 120
    const val DURATION_LIST_ITEM_ENTER = 250
    const val STAGGER_LIST_ITEM = 40
    const val DURATION_NAV_PILL = 250
    const val DURATION_POPUP_SPRING = 300
    const val DURATION_CHIP_RIPPLE = 200
    const val DURATION_CHECKMARK_DRAW = 400
    const val DURATION_FAB_EXPAND = 280
    const val DURATION_SHARED_ELEMENT = 350

    // Easings
    val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
    val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseOut = LinearOutSlowInEasing
    val FastOutSlow = FastOutSlowInEasing

    // Springs
    val CardPressSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.6f, // Damping 0.6 as specified
        stiffness = Spring.StiffnessMediumLow
    )

    val PopupSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val FabRadialSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * CompositionLocal to respect "Reduce Motion" accessibility settings across all screens
 */
val LocalReduceMotion = compositionLocalOf { false }

@Composable
fun ProvideReduceMotion(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val animatorDurationScale = Settings.Global.getFloat(
        resolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    )
    val transitionAnimationScale = Settings.Global.getFloat(
        resolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1.0f
    )
    val reduceMotion = animatorDurationScale == 0f || transitionAnimationScale == 0f

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        content()
    }
}

/**
 * Card Press Micro-interaction:
 * Press scale down to 0.97 in 120ms, springs back with damping 0.6
 */
fun Modifier.pressScale(
    enabled: Boolean = true,
    targetScale: Float = 0.97f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reduceMotion) targetScale else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = MotionTokens.DURATION_CARD_PRESS, easing = MotionTokens.EaseOut)
        } else {
            MotionTokens.CardPressSpring
        },
        label = "pressScaleAnimation"
    )

    this
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null, // Custom scale provides the primary tactile feedback
                    enabled = enabled,
                    onClick = onClick
                )
            } else Modifier
        )
}

