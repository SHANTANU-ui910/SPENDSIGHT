package com.spendsight.app.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.HyperTealGlow
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.VoidBackground

/**
 * BiometricLockShield:
 * Fullscreen cybernetic security shield requiring fingerprint/PIN authentication
 * to unlock the admin telemetry dashboard.
 */
@Composable
fun BiometricLockShield(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(HyperTealGlow)
                    .clickable(onClick = onUnlockClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Unlock SpendSight",
                        tint = HyperTeal,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SPENDSIGHT VAULT LOCKED",
                color = TextPureWhite,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap fingerprint sensor or button to authenticate",
                color = TextMutedSubtext,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            ChromaMorphicCard(
                onClick = onUnlockClick,
                shape = CircleShape
            ) {
                Text(
                    text = "UNLOCK ADMIN HUD",
                    color = HyperTeal,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

