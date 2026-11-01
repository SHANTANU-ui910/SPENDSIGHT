package com.spendsight.app.ui.add

import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsight.app.domain.model.Category
import com.spendsight.app.domain.model.PaymentMethod
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.components.HolographicCategoryChip
import com.spendsight.app.ui.components.TactileNeumorphicKeypad
import com.spendsight.app.ui.theme.CategoryBillsChroma
import com.spendsight.app.ui.theme.CategoryFoodChroma
import com.spendsight.app.ui.theme.CategoryGroceriesChroma
import com.spendsight.app.ui.theme.CategoryHealthChroma
import com.spendsight.app.ui.theme.CategoryOtherChroma
import com.spendsight.app.ui.theme.CategoryRentChroma
import com.spendsight.app.ui.theme.CategoryShoppingChroma
import com.spendsight.app.ui.theme.CategoryTravelChroma
import com.spendsight.app.ui.theme.CrimsonLaser
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.SurfaceElevated
import com.spendsight.app.ui.theme.SurfaceObsidian
import com.spendsight.app.ui.theme.TextDarkInverse
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextSlateBody
import com.spendsight.app.ui.theme.TextTechnicalCyan
import com.spendsight.app.ui.theme.VoidBackground
import com.spendsight.app.ui.theme.formatChromaCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    // ActivityResultLauncher for receipt photo attachment
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onReceiptUriSelected(uri?.toString())
    }

    Scaffold(
        containerColor = VoidBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MANUAL ENTRY",
                        color = TextPureWhite,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextMutedSubtext
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.saveExpense(onSuccess = onClose)
                        },
                        enabled = uiState.amountDouble > 0.0
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (uiState.amountDouble > 0) HyperTeal else Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Save",
                                tint = if (uiState.amountDouble > 0) TextDarkInverse else TextMutedSubtext,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VoidBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable upper configuration section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Income vs Expense Segmented Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceObsidian)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    val expenseBg by animateColorAsState(
                        targetValue = if (!uiState.isIncome) CrimsonLaser else Color.Transparent,
                        animationSpec = tween(200),
                        label = "expBg"
                    )
                    val incomeBg by animateColorAsState(
                        targetValue = if (uiState.isIncome) PhosphorMint else Color.Transparent,
                        animationSpec = tween(200),
                        label = "incBg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(expenseBg)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.onToggleIncome(false)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EXPENSE (DEBIT)",
                            color = if (!uiState.isIncome) Color.White else TextMutedSubtext,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(incomeBg)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.onToggleIncome(true)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INCOME (CREDIT)",
                            color = if (uiState.isIncome) Color.Black else TextMutedSubtext,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Large Numeric Display
                Text(
                    text = formatChromaCurrency(uiState.amountDouble, showDecimals = uiState.amountString.contains(".")),
                    color = if (uiState.isIncome) PhosphorMint else TextPureWhite,
                    fontSize = 44.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Payment Method Chips (UPI, Card, Cash, Netbanking)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        val isSelected = uiState.paymentMethod == method
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) HyperTeal else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) HyperTeal else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.onPaymentMethodSelect(method)
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = method.name,
                                color = if (isSelected) TextDarkInverse else TextSlateBody,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Category Selector Horizontal Grid
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
                    val defaultDisplayCategories = listOf(
                        Triple(1L, "Food", Pair(Icons.Rounded.Restaurant, CategoryFoodChroma)),
                        Triple(2L, "Travel", Pair(Icons.Rounded.DirectionsCar, CategoryTravelChroma)),
                        Triple(3L, "Bills", Pair(Icons.Rounded.ReceiptLong, CategoryBillsChroma)),
                        Triple(4L, "Shopping", Pair(Icons.Rounded.ShoppingBag, CategoryShoppingChroma)),
                        Triple(5L, "Health", Pair(Icons.Rounded.MedicalServices, CategoryHealthChroma)),
                        Triple(6L, "Rent", Pair(Icons.Rounded.Home, CategoryRentChroma)),
                        Triple(7L, "Groceries", Pair(Icons.Rounded.ShoppingCart, CategoryGroceriesChroma)),
                        Triple(10L, "Other", Pair(Icons.Rounded.MoreHoriz, CategoryOtherChroma))
                    )

                    defaultDisplayCategories.forEach { (id, name, meta) ->
                        HolographicCategoryChip(
                            name = name,
                            icon = meta.first,
                            chromaColor = meta.second,
                            isSelected = uiState.selectedCategoryId == id,
                            onClick = {
                                viewModel.onCategorySelect(
                                    Category(id = id, name = name, iconName = "", colorHex = "")
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Merchant Name Text Field
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = { viewModel.onMerchantChange(it) },
                    label = { Text("Merchant / Payee Name", color = TextMutedSubtext, fontSize = 12.sp) },
                    placeholder = { Text("Payee or merchant name", color = TextMutedSubtext) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPureWhite,
                        unfocusedTextColor = TextSlateBody,
                        focusedBorderColor = HyperTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = SurfaceObsidian,
                        unfocusedContainerColor = SurfaceObsidian
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Notes Field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notes / Memo", color = TextMutedSubtext, fontSize = 12.sp) },
                    placeholder = { Text("Add contextual memo...", color = TextMutedSubtext) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPureWhite,
                        unfocusedTextColor = TextSlateBody,
                        focusedBorderColor = HyperTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = SurfaceObsidian,
                        unfocusedContainerColor = SurfaceObsidian
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Receipt Photo Attachment Card
                ChromaMorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AttachFile,
                                contentDescription = "Receipt",
                                tint = HyperTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.receiptUri != null) "RECEIPT ATTACHED ✓" else "ATTACH RECEIPT PHOTO",
                                color = if (uiState.receiptUri != null) PhosphorMint else TextSlateBody,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.receiptUri != null) {
                            Text(
                                text = "Remove",
                                color = CrimsonLaser,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { viewModel.onReceiptUriSelected(null) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 8. Recurring Toggle
                ChromaMorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = "Recurring",
                                tint = PhosphorMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "RECURRING PAYMENT",
                                    color = TextPureWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rent, subscriptions, EMI",
                                    color = TextMutedSubtext,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isRecurring,
                            onCheckedChange = { viewModel.onToggleRecurring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PhosphorMint,
                                checkedTrackColor = PhosphorMint.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMutedSubtext,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }

            // Fixed Tactile Neumorphic Keypad at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceObsidian)
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TactileNeumorphicKeypad(
                    onDigitPress = { viewModel.onDigitPress(it) },
                    onDecimalPress = { viewModel.onDecimalPress() },
                    onBackspacePress = { viewModel.onBackspacePress() }
                )
            }
        }
    }
}

