package com.spendsight.app.ui.dashboard.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.theme.CobaltFlux
import com.spendsight.app.ui.theme.CrimsonLaser
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.LocalReduceMotion
import com.spendsight.app.ui.theme.MotionTokens
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextSlateBody
import com.spendsight.app.ui.theme.formatChromaCurrency
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * StaggeredExpenseItem:
 * Ledger row item with staggered entrance animation (40ms stagger, 250ms slide+fade),
 * press scale physics (0.97 in 120ms), and swipe-to-delete / swipe-to-edit gestures.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaggeredExpenseItem(
    expense: Expense,
    index: Int,
    onDelete: (Expense) -> Unit,
    onEdit: (Expense) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val reduceMotion = LocalReduceMotion.current

    var isVisible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(index, reduceMotion) {
        if (!reduceMotion) {
            delay((index * MotionTokens.STAGGER_LIST_ITEM).toLong())
            isVisible = true
        }
    }

    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onDelete(expense)
                    true
                }
                DismissValue.DismissedToEnd -> {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onEdit(expense)
                    false // Don't dismiss, trigger edit
                }
                else -> false
            }
        }
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = if (reduceMotion) fadeIn(tween(0))
        else slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_LIST_ITEM_ENTER,
                easing = MotionTokens.EaseOutQuart
            )
        ) + fadeIn(tween(MotionTokens.DURATION_LIST_ITEM_ENTER)),
        modifier = modifier
    ) {
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
            background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                val isDelete = direction == DismissDirection.EndToStart
                val bg = if (isDelete) CrimsonLaser else CobaltFlux
                val alignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
                val icon = if (isDelete) Icons.Rounded.Delete else Icons.Rounded.Edit

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .padding(horizontal = 24.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isDelete) "Delete" else "Edit",
                        tint = Color.White
                    )
                }
            },
            dismissContent = {
                ChromaMorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = onClick
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Icon / Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = expense.merchant.take(1).uppercase(),
                                color = HyperTeal,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Merchant & Metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.merchant,
                                color = TextPureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = expense.categoryName.uppercase(),
                                    color = HyperTeal,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (!expense.accountLast4.isNullOrBlank()) {
                                    Text(
                                        text = " • **${expense.accountLast4}",
                                        color = TextMutedSubtext,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(expense.timestamp))
                                Text(
                                    text = " • $timeStr",
                                    color = TextMutedSubtext,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Amount with superscript cents
                        val amountColor = if (expense.isIncome) PhosphorMint else TextPureWhite
                        val prefix = if (expense.isIncome) "+" else "-"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = prefix,
                                color = amountColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatChromaCurrency(expense.amount, showDecimals = true),
                                color = amountColor,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        )
    }
}

