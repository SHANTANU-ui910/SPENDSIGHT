package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.KeypadButtonShape18
import com.spendsight.app.ui.theme.KeypadDishBrush
import com.spendsight.app.ui.theme.KeypadPressedBrush
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SpecularHighlightSubtle
import com.spendsight.app.ui.theme.SpecularHighlightTop
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * Tactile Neumorphic Keypad
 * Engineered with physically dished keys, depth compression on press,
 * and high-fidelity haptic feedback.
 */
@Composable
fun TactileNeumorphicKeypad(
    modifier: Modifier = Modifier,
    onDigitPress: (String) -> Unit,
    onDecimalPress: () -> Unit,
    onBackspacePress: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKeys.forEach { key ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (key) {
                            "⌫" -> TactileKey(
                                onClick = onBackspacePress,
                                content = {
                                    Icon(
                                        imageVector = Icons.Rounded.Backspace,
                                        contentDescription = "Backspace",
                                        tint = PhosphorMint,
                                        modifier = Modifier.scale(0.9f)
                                    )
                                }
                            )
                            "." -> TactileKey(
                                onClick = onDecimalPress,
                                content = {
                                    Text(
                                        text = "•",
                                        color = TextPureWhite,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                            else -> TactileKey(
                                onClick = { onDigitPress(key) },
                                content = {
                                    Text(
                                        text = key,
                                        color = TextPureWhite,
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TactileKey(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 80)
        } else {
            MotionTokens.CardPressSpring
        },
        label = "keypadDepress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(KeypadButtonShape18)
            .background(if (isPressed) KeypadPressedBrush else KeypadDishBrush)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(Color.Black.copy(alpha = 0.4f), SpecularHighlightSubtle)
                    } else {
                        listOf(SpecularHighlightTop, Color.Transparent)
                    }
                ),
                shape = KeypadButtonShape18
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

