package com.spendsight.app.ui.dashboard.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SolarAmber

/**
 * AnimatedCoinLoader:
 * Custom 3D spinning cybernetic Rupee coin loader for pull-to-refresh and network loading states.
 */
@Composable
fun AnimatedCoinLoader(
    modifier: Modifier = Modifier,
    isSpinning: Boolean = true
) {
    val reduceMotion = LocalReduceMotion.current

    val infiniteTransition = rememberInfiniteTransition(label = "coinSpin")
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coinRotationY"
    )

    Box(
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                if (!reduceMotion && isSpinning) {
                    this.rotationY = rotationY
                    cameraDistance = 12f * density
                }
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(SolarAmber, Color(0xFFB8860B))
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.8f), HyperTeal)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "₹",
            color = Color.Black,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

