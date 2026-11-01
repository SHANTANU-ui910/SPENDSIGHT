package com.spendsight.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.add.AddExpenseScreen
import com.spendsight.app.ui.dashboard.DashboardScreen
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.VoidBackground
import kotlinx.coroutines.delay

/**
 * SpendSightAppContainer:
 * Manages the top-level app lifecycle:
 * 1. App Launch: Logo draws in 600ms, fades into dashboard in 300ms.
 * 2. Bottom navigation bar with central radial FAB.
 * 3. Full-screen Fast Entry Add modal sheet.
 */
@Composable
fun SpendSightAppContainer() {
    val reduceMotion = LocalReduceMotion.current
    var isSplashFinished by remember { mutableStateOf(reduceMotion) }
    var currentNav by remember { mutableStateOf(NavDestination.HOME) }
    var isAddSheetVisible by remember { mutableStateOf(false) }

    // Motion 1: Logo draws in 600ms, then fades into dashboard in 300ms
    val logoDrawProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val splashAlpha = remember { Animatable(if (reduceMotion) 0f else 1f) }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            logoDrawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_LOGO_DRAW,
                    easing = MotionTokens.EaseOutExpo
                )
            )
            delay(100L)
            splashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_SPLASH_FADE,
                    easing = FastOutSlowInEasing
                )
            )
            isSplashFinished = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VoidBackground)) {
        if (isSplashFinished) {
            Scaffold(
                containerColor = VoidBackground,
                bottomBar = {
                    SpendSightBottomNav(
                        currentDestination = currentNav,
                        onNavigate = { currentNav = it },
                        onManualAddClick = { isAddSheetVisible = true },
                        onScanReceiptClick = { isAddSheetVisible = true }
                    )
                }
            ) { _ ->
                when (currentNav) {
                    NavDestination.HOME -> DashboardScreen(
                        onNavigateToAdd = { isAddSheetVisible = true },
                        onNavigateToDetail = { /* Handled in detail sheet */ }
                    )
                    NavDestination.TRANSACTIONS -> DashboardScreen(
                        onNavigateToAdd = { isAddSheetVisible = true },
                        onNavigateToDetail = { }
                    )
                    NavDestination.INSIGHTS -> DashboardScreen(
                        onNavigateToAdd = { isAddSheetVisible = true },
                        onNavigateToDetail = { }
                    )
                    NavDestination.SETTINGS -> DashboardScreen(
                        onNavigateToAdd = { isAddSheetVisible = true },
                        onNavigateToDetail = { }
                    )
                }
            }
        }

        // Animated Splash Overlay (draws in 600ms, fades in 300ms)
        if (!isSplashFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VoidBackground)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        // Futuristic geometric 'S' lightning insignia
                        val path = Path().apply {
                            moveTo(size.width * 0.75f, size.height * 0.15f)
                            lineTo(size.width * 0.25f, size.height * 0.15f)
                            lineTo(size.width * 0.25f, size.height * 0.45f)
                            lineTo(size.width * 0.75f, size.height * 0.45f)
                            lineTo(size.width * 0.75f, size.height * 0.85f)
                            lineTo(size.width * 0.25f, size.height * 0.85f)
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(path, false)
                        val totalLength = pathMeasure.length

                        val segment = Path()
                        pathMeasure.getSegment(0f, totalLength * logoDrawProgress.value, segment, true)

                        drawPath(
                            path = segment,
                            color = HyperTeal,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "SPENDSIGHT",
                        color = TextPureWhite,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Modal Add Expense Screen (Slides up 300ms)
        AnimatedVisibility(
            visible = isAddSheetVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = MotionTokens.EaseOutQuart)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
        ) {
            AddExpenseScreen(
                onClose = { isAddSheetVisible = false }
            )
        }
    }
}

