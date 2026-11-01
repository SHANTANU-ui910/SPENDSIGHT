package com.spendsight.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextTechnicalCyan
import kotlin.math.atan2
import kotlin.math.sqrt

data class DonutSegment(
    val categoryName: String,
    val amount: Double,
    val color: Color
)

/**
 * ChromaDonutChart:
 * Native Compose Canvas financial donut chart featuring animated staggered sweeps,
 * neon segment glows, and interactive slice tap-selection with central telemetry.
 */
@Composable
fun ChromaDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 36f
) {
    val view = LocalView.current
    val reduceMotion = LocalReduceMotion.current
    val totalAmount = remember(segments) { segments.sumOf { it.amount } }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(segments, reduceMotion) {
        if (reduceMotion) {
            animationProgress.snapTo(1f)
        } else {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_CHART_ANIMATION,
                    easing = MotionTokens.EaseOutQuart
                )
            )
        }
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .pointerInput(segments, totalAmount) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val distanceFromCenter = sqrt(dx * dx + dy * dy)
                        val radius = size.width / 2f

                        // Detect tap within donut ring band
                        if (distanceFromCenter in (radius - strokeWidth * 1.5f)..(radius + strokeWidth * 0.5f)) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            var currentAngle = 0f
                            var tappedIdx: Int? = null
                            for (i in segments.indices) {
                                val sweep = if (totalAmount > 0) ((segments[i].amount / totalAmount) * 360f).toFloat() else 0f
                                if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                    tappedIdx = i
                                    break
                                }
                                currentAngle += sweep
                            }

                            if (tappedIdx != null) {
                                selectedIndex = if (selectedIndex == tappedIdx) null else tappedIdx
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        }
                    }
                }
        ) {
            val chartRadius = (size.minDimension - strokeWidth * 2) / 2f
            val chartCenter = Offset(size.width / 2f, size.height / 2f)
            val arcSize = Size(chartRadius * 2, chartRadius * 2)
            val arcTopLeft = Offset(chartCenter.x - chartRadius, chartCenter.y - chartRadius)

            var startAngle = -90f // Start from 12 o'clock

            segments.forEachIndexed { index, segment ->
                val fullSweep = if (totalAmount > 0) ((segment.amount / totalAmount) * 360f).toFloat() else 0f
                val animatedSweep = fullSweep * animationProgress.value
                val isSelected = selectedIndex == index
                val currentStroke = if (isSelected) strokeWidth * 1.35f else strokeWidth

                // Draw arc segment
                drawArc(
                    color = if (selectedIndex == null || isSelected) segment.color else segment.color.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = animatedSweep.coerceAtLeast(1f),
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(
                        width = currentStroke,
                        cap = StrokeCap.Round
                    )
                )

                startAngle += fullSweep
            }
        }

        // Central Telemetry Readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayedSegment = selectedIndex?.let { segments.getOrNull(it) }

            if (displayedSegment != null) {
                Text(
                    text = displayedSegment.categoryName.uppercase(),
                    color = displayedSegment.color,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "₹${displayedSegment.amount.toInt()}",
                    color = TextPureWhite,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold
                )
                val percentage = if (totalAmount > 0) ((displayedSegment.amount / totalAmount) * 100).toInt() else 0
                Text(
                    text = "$percentage% OF TOTAL",
                    color = TextTechnicalCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "TOTAL SPENT",
                    color = TextMutedSubtext,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "₹${totalAmount.toInt()}",
                    color = TextPureWhite,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${segments.size} CATEGORIES",
                    color = TextTechnicalCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

