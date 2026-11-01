package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendsight.app.ui.theme.CardShape24
import com.spendsight.app.ui.theme.ChromaBorderTeal
import com.spendsight.app.ui.theme.GlassSurfaceBrush
import com.spendsight.app.ui.theme.OcclusionShadowBottom
import com.spendsight.app.ui.theme.SpecularHighlightSubtle
import com.spendsight.app.ui.theme.SpecularHighlightTop
import com.spendsight.app.ui.theme.pressScale

/**
 * ChromaMorphicCard:
 * Bespoke glass-morphism container with physical dual-lit specular edge lighting,
 * sub-surface refractive luminescence, and tactile spring feedback.
 */
@Composable
fun ChromaMorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape24,
    surfaceBrush: Brush = GlassSurfaceBrush,
    glowAccent: Color? = null,
    glowRadius: Dp = 20.dp,
    hasSubtleRefraction: Boolean = true,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Dual-lighting border gradient: light strikes top-left, shadow settles bottom-right
    val borderBrush = if (borderColor != null) {
        Brush.linearGradient(
            colors = listOf(borderColor, borderColor.copy(alpha = 0.2f)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                SpecularHighlightTop,
                SpecularHighlightSubtle,
                OcclusionShadowBottom
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Outer glow for key telemetric anchors
    val glowModifier = if (glowAccent != null) {
        Modifier.drawBehind {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(
                            glowRadius.toPx(),
                            0f,
                            4.dp.toPx(),
                            glowAccent.toArgb()
                        )
                    }
                }
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    24.dp.toPx(),
                    24.dp.toPx(),
                    paint
                )
            }
        }
    } else Modifier

    // Sub-surface refraction gradient inside the card
    val refractionModifier = if (hasSubtleRefraction && glowAccent != null) {
        Modifier.drawBehind {
            val radialGlow = Brush.radialGradient(
                colors = listOf(glowAccent.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.15f),
                radius = size.width * 0.8f
            )
            drawRect(radialGlow)
        }
    } else Modifier

    Box(
        modifier = modifier
            .then(glowModifier)
            .pressScale(
                enabled = onClick != null,
                onClick = if (onClick != null) {
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onClick()
                    }
                } else null
            )
            .clip(shape)
            .background(surfaceBrush)
            .then(refractionModifier)
            .border(
                width = 1.2.dp,
                brush = borderBrush,
                shape = shape
            )
            .padding(18.dp),
        content = content
    )
}

