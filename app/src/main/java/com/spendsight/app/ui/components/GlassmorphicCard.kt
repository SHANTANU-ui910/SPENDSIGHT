package com.spendsight.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendsight.app.ui.theme.CardShape24
import com.spendsight.app.ui.theme.DarkBorder
import com.spendsight.app.ui.theme.DarkBorderHighlight
import com.spendsight.app.ui.theme.DarkSurface
import com.spendsight.app.ui.theme.GlassCardBackground
import com.spendsight.app.ui.theme.GlassCardStroke
import com.spendsight.app.ui.theme.pressScale

/**
 * SpendSight Glassmorphic Card
 * Combines translucent surface with a fine hairline border and optional neon glow.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape24,
    backgroundColor: Color = GlassCardBackground,
    borderColor: Color = GlassCardStroke,
    glowColor: Color? = null,
    glowRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glowModifier = if (glowColor != null) {
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
                            glowColor.toArgb()
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

    Box(
        modifier = modifier
            .then(glowModifier)
            .pressScale(enabled = onClick != null, onClick = onClick)
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderColor,
                            borderColor.copy(alpha = 0.05f)
                        )
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}

