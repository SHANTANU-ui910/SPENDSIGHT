package com.spendsight.app.ui.overlay

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.domain.model.Category
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.components.HolographicCategoryChip
import com.spendsight.app.ui.theme.CategoryBillsChroma
import com.spendsight.app.ui.theme.CategoryFoodChroma
import com.spendsight.app.ui.theme.CategoryGroceriesChroma
import com.spendsight.app.ui.theme.CategoryHealthChroma
import com.spendsight.app.ui.theme.CategoryOtherChroma
import com.spendsight.app.ui.theme.CategoryRentChroma
import com.spendsight.app.ui.theme.CategoryShoppingChroma
import com.spendsight.app.ui.theme.CategoryTravelChroma
import com.spendsight.app.ui.theme.GlassOverlayBrush
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SheetShapeTop28
import com.spendsight.app.ui.theme.SolarAmber
import com.spendsight.app.ui.theme.TextDarkInverse
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextSlateBody
import com.spendsight.app.ui.theme.TextTechnicalCyan
import com.spendsight.app.ui.theme.VoidBackground
import com.spendsight.app.ui.theme.formatChromaCurrency
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentOverlayContent(
    amount: Double,
    merchant: String,
    suggestedCategoryId: Long,
    suggestedCategoryName: String,
    upiRef: String?,
    accountLast4: String?,
    timestamp: Long,
    isIncome: Boolean = false,
    autoDismissDurationSeconds: Int = 10,
    onSave: (selectedCategoryId: Long, note: String) -> Unit,
    onSkip: () -> Unit
) {
    val view = LocalView.current
    val reduceMotion = LocalReduceMotion.current

    var selectedCategoryId by remember { mutableLongStateOf(suggestedCategoryId) }
    var noteText by remember { mutableStateOf("") }
    var secondsRemaining by remember { mutableIntStateOf(autoDismissDurationSeconds) }
    var isSavedState by remember { mutableStateOf(false) }

    // Motion 1: Amount Pulse (scale 1.0 -> 1.08 -> 1.0 in 400ms)
    val amountScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            amountScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 400
                    1.0f at 0 with FastOutSlowInEasing
                    1.08f at 200 with FastOutSlowInEasing
                    1.0f at 400
                }
            )
        }
    }

    // Countdown Timer (10 seconds to auto-dismiss as uncategorized)
    LaunchedEffect(isSavedState) {
        if (isSavedState) return@LaunchedEffect
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        // Auto-dismiss on timeout
        onSkip()
    }

    // Motion 2: Animated Success Checkmark Drawing (400ms)
    val checkmarkProgress = remember { Animatable(0f) }
    LaunchedEffect(isSavedState) {
        if (isSavedState) {
            checkmarkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = MotionTokens.DURATION_CHECKMARK_DRAW, easing = FastOutSlowInEasing)
            )
            delay(300L)
            onSave(selectedCategoryId, noteText)
        }
    }

    val formattedTime = remember(timestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    }

    // Bottom sheet layout with Cyber-Morphic styling
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SheetShapeTop28)
            .background(GlassOverlayBrush)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                ),
                shape = SheetShapeTop28
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Pill
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Telemetry Status Header & Countdown Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isIncome) PhosphorMint else HyperTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isIncome) "PAYMENT RECEIVED // INFLOW" else "NEW PAYMENT INTERCEPTED",
                        color = if (isIncome) PhosphorMint else TextTechnicalCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // 10s Countdown Circular Ring
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progressFraction = secondsRemaining.toFloat() / autoDismissDurationSeconds.toFloat()
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                        CircularProgressIndicator(
                            progress = { progressFraction },
                            color = if (secondsRemaining <= 3) SolarAmber else (if (isIncome) PhosphorMint else HyperTeal),
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$secondsRemaining",
                            color = TextPureWhite,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AUTO",
                        color = TextMutedSubtext,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isSavedState) {
                // Primary Amount Display with Pulsing Motion
                Text(
                    text = (if (isIncome) "+ " else "- ") + formatChromaCurrency(amount, showDecimals = true),
                    color = if (isIncome) PhosphorMint else TextPureWhite,
                    fontSize = 36.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.scale(amountScale.value)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Merchant & Account Metadata Pill
                Text(
                    text = if (isIncome) "From: $merchant" else merchant,
                    color = TextPureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (!accountLast4.isNullOrBlank()) {
                        Text(
                            text = "A/c **$accountLast4",
                            color = TextMutedSubtext,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " • ",
                            color = TextMutedSubtext,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = TextMutedSubtext,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!upiRef.isNullOrBlank()) {
                        Text(
                            text = " • UPI: $upiRef",
                            color = TextMutedSubtext,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-suggested Category Badge
                if (suggestedCategoryId > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Suggested",
                            tint = SolarAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Suggested from history: $suggestedCategoryName",
                            color = TextSlateBody,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // One-Tap Horizontal Category Chips (Food, Travel, Bills, Shopping, Health, Rent, Other)
                Text(
                    text = "SELECT CATEGORY",
                    color = TextMutedSubtext,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Triple(1L, "Food", Pair(Icons.Rounded.Restaurant, CategoryFoodChroma)),
                        Triple(2L, "Travel", Pair(Icons.Rounded.DirectionsCar, CategoryTravelChroma)),
                        Triple(3L, "Bills", Pair(Icons.Rounded.ReceiptLong, CategoryBillsChroma)),
                        Triple(4L, "Shopping", Pair(Icons.Rounded.ShoppingBag, CategoryShoppingChroma)),
                        Triple(5L, "Health", Pair(Icons.Rounded.MedicalServices, CategoryHealthChroma)),
                        Triple(6L, "Rent", Pair(Icons.Rounded.Home, CategoryRentChroma)),
                        Triple(7L, "Groceries", Pair(Icons.Rounded.ShoppingCart, CategoryGroceriesChroma)),
                        Triple(10L, "Other", Pair(Icons.Rounded.MoreHoriz, CategoryOtherChroma))
                    )

                    categories.forEach { (id, name, meta) ->
                        HolographicCategoryChip(
                            name = name,
                            icon = meta.first,
                            chromaColor = meta.second,
                            isSelected = selectedCategoryId == id,
                            onClick = {
                                selectedCategoryId = id
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Memo / Note Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = {
                        Text(
                            text = "Add optional note (e.g. Lunch with team)...",
                            color = TextMutedSubtext,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPureWhite,
                        unfocusedTextColor = TextSlateBody,
                        focusedBorderColor = HyperTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = VoidBackground.copy(alpha = 0.5f),
                        unfocusedContainerColor = VoidBackground.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: [ Skip ] and [ Save Categorized ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Skip button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable(onClick = onSkip),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Skip",
                            color = TextMutedSubtext,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save Categorized button
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(HyperTeal, PhosphorMint)
                                )
                            )
                            .clickable(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    isSavedState = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save Categorized",
                            color = TextDarkInverse,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Success Confirmation State with animated drawing checkmark
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 400ms Animated Checkmark
                    Canvas(modifier = Modifier.size(56.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.25f, size.height * 0.52f)
                            lineTo(size.width * 0.44f, size.height * 0.72f)
                            lineTo(size.width * 0.76f, size.height * 0.32f)
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(path, false)
                        val totalLength = pathMeasure.length

                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, totalLength * checkmarkProgress.value, animatedPath, true)

                        drawPath(
                            path = animatedPath,
                            color = PhosphorMint,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SAVED TO LEDGER",
                        color = PhosphorMint,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Learned preference for $merchant",
                        color = TextSlateBody,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

