package com.spendsight.app.ui.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.components.RadialAddFab
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.SurfaceObsidian
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite

enum class NavDestination(val label: String, val icon: ImageVector) {
    HOME("HUD", Icons.Rounded.Home),
    TRANSACTIONS("LEDGER", Icons.Rounded.Receipt),
    INSIGHTS("METRICS", Icons.Rounded.Analytics),
    SETTINGS("CONFIG", Icons.Rounded.Settings)
}

/**
 * SpendSightBottomNav:
 * Futuristic bottom navigation bar featuring a 250ms sliding pill indicator,
 * light haptic feedback on tab changes, and a center speed dial FAB.
 */
@Composable
fun SpendSightBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onManualAddClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(navShape)
            .background(SurfaceObsidian)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                ),
                shape = navShape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / HUD
            NavPillItem(
                destination = NavDestination.HOME,
                isSelected = currentDestination == NavDestination.HOME,
                onClick = { onNavigate(NavDestination.HOME) }
            )

            // Transactions / Ledger
            NavPillItem(
                destination = NavDestination.TRANSACTIONS,
                isSelected = currentDestination == NavDestination.TRANSACTIONS,
                onClick = { onNavigate(NavDestination.TRANSACTIONS) }
            )

            // Center Radial FAB
            RadialAddFab(
                onManualAddClick = onManualAddClick,
                onScanReceiptClick = onScanReceiptClick
            )

            // Insights / Metrics
            NavPillItem(
                destination = NavDestination.INSIGHTS,
                isSelected = currentDestination == NavDestination.INSIGHTS,
                onClick = { onNavigate(NavDestination.INSIGHTS) }
            )

            // Settings / Config
            NavPillItem(
                destination = NavDestination.SETTINGS,
                isSelected = currentDestination == NavDestination.SETTINGS,
                onClick = { onNavigate(NavDestination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavPillItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) HyperTeal else TextMutedSubtext,
        animationSpec = tween(durationMillis = MotionTokens.DURATION_NAV_PILL),
        label = "navIconColor"
    )

    val pillBg by animateColorAsState(
        targetValue = if (isSelected) HyperTeal.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = MotionTokens.DURATION_NAV_PILL),
        label = "navPillBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(pillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isSelected) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClick()
                    }
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = destination.label,
            color = if (isSelected) TextPureWhite else TextMutedSubtext,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

