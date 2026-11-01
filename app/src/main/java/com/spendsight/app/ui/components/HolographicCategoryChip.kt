package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * Holographic Category Chip Capsule
 * Provides an individual chromatic aura for each financial category,
 * tactile haptics, and glowing border feedback upon selection.
 */
@Composable
fun HolographicCategoryChip(
    name: String,
    icon: ImageVector,
    chromaColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) chromaColor.copy(alpha = 0.22f) else SurfaceElevated.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = MotionTokens.DURATION_CHIP_RIPPLE),
        label = "chipBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) chromaColor else Color.White.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = MotionTokens.DURATION_CHIP_RIPPLE),
        label = "chipBorderColor"
    )

    val glowRadius by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = MotionTokens.DURATION_CHIP_RIPPLE),
        label = "chipGlowRadius"
    )

    val chipShape = RoundedCornerShape(16.dp)

    val glowModifier = if (isSelected) {
        Modifier.drawBehind {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(
                            glowRadius.toPx(),
                            0f,
                            0f,
                            chromaColor.toArgb()
                        )
                    }
                }
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    16.dp.toPx(),
                    16.dp.toPx(),
                    paint
                )
            }
        }
    } else Modifier

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(chipShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = Brush.horizontalGradient(
                    colors = if (isSelected) {
                        listOf(borderColor, borderColor.copy(alpha = 0.5f))
                    } else {
                        listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                    }
                ),
                shape = chipShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) chromaColor else chromaColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = if (isSelected) Color.Black else chromaColor,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name,
                color = if (isSelected) TextPureWhite else TextMutedSubtext,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

