package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.CyberAmethyst
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.HyperTealGlow
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.TextDarkInverse
import com.spendsight.app.ui.theme.TextPureWhite

/**
 * RadialAddFab:
 * Center bottom navigation FAB that expands into a 280ms radial spring speed dial
 * offering "Manual Entry" and "Scan Receipt".
 */
@Composable
fun RadialAddFab(
    onManualAddClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val reduceMotion = LocalReduceMotion.current
    var isExpanded by remember { mutableStateOf(false) }

    val fabRotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = if (reduceMotion) tween(0) else MotionTokens.FabRadialSpring,
        label = "fabRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Expanded Radial Menu Items
        AnimatedVisibility(
            visible = isExpanded,
            enter = if (reduceMotion) fadeIn(tween(0)) else scaleIn(MotionTokens.FabRadialSpring) + fadeIn(tween(180)),
            exit = if (reduceMotion) fadeOut(tween(0)) else scaleOut(tween(150)) + fadeOut(tween(150))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Option 1: Scan Receipt
                RadialSpeedDialItem(
                    label = "SCAN RECEIPT (OCR)",
                    icon = Icons.Rounded.CameraAlt,
                    accentColor = CyberAmethyst,
                    onClick = {
                        isExpanded = false
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onScanReceiptClick()
                    }
                )

                // Option 2: Manual Keypad Entry
                RadialSpeedDialItem(
                    label = "MANUAL KEYPAD",
                    icon = Icons.Rounded.Keyboard,
                    accentColor = HyperTeal,
                    onClick = {
                        isExpanded = false
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onManualAddClick()
                    }
                )
            }
        }

        // Main Center FAB Button
        Box(
            modifier = Modifier
                .size(62.dp)
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(
                                    20.dp.toPx(),
                                    0f,
                                    6.dp.toPx(),
                                    HyperTealGlow.toArgb()
                                )
                            }
                        }
                        canvas.drawCircle(
                            Offset(size.width / 2f, size.height / 2f),
                            size.width / 2f,
                            paint
                        )
                    }
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(HyperTeal, PhosphorMint)
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        isExpanded = !isExpanded
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Expand Add Options",
                tint = TextDarkInverse,
                modifier = Modifier
                    .size(30.dp)
                    .rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun RadialSpeedDialItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceElevated)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            color = TextPureWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

