package com.spendsight.app.ui.dashboard

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsight.app.domain.model.Expense
import com.spendsight.app.ui.components.ChromaBarChart
import com.spendsight.app.ui.components.ChromaDonutChart
import com.spendsight.app.ui.components.ChromaMorphicCard
import com.spendsight.app.ui.dashboard.components.AnimatedCoinLoader
import com.spendsight.app.ui.dashboard.components.BiometricLockShield
import com.spendsight.app.ui.dashboard.components.BudgetStatusRingsCard
import com.spendsight.app.ui.dashboard.components.HeroSpendingTelemetryCard
import com.spendsight.app.ui.dashboard.components.PeriodFilterSelector
import com.spendsight.app.ui.dashboard.components.SmartInsightBanner
import com.spendsight.app.ui.dashboard.components.SpendingHeatmapCalendar
import com.spendsight.app.ui.dashboard.components.StaggeredExpenseItem
import com.spendsight.app.ui.theme.HyperTeal
import com.spendsight.app.ui.theme.PhosphorMint
import com.spendsight.app.ui.theme.TextMutedSubtext
import com.spendsight.app.ui.theme.TextPureWhite
import com.spendsight.app.ui.theme.TextSlateBody
import com.spendsight.app.ui.theme.TextTechnicalCyan
import com.spendsight.app.ui.theme.VoidBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }

    // Biometric Security Guard
    if (uiState.isBiometricRequired && !uiState.isBiometricAuthenticated) {
        BiometricLockShield(
            onUnlockClick = {
                // In actual deployment, BiometricPrompt is presented here
                viewModel.onBiometricAuthSuccess()
            }
        )
        return
    }

    Scaffold(
        containerColor = VoidBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PhosphorMint)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPENDSIGHT // HUD",
                            color = TextPureWhite,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    // Search Action Button
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = HyperTeal
                        )
                    }

                    // Export CSV Button
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        val uri = viewModel.exportToCsv(context)
                        if (uri != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export SpendSight CSV"))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Export CSV",
                            tint = TextPureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VoidBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Optional Search Bar
            item {
                AnimatedVisibility(visible = isSearchExpanded) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text("Filter by merchant, notes, amount...", color = TextMutedSubtext, fontSize = 13.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPureWhite,
                            unfocusedTextColor = TextSlateBody,
                            focusedBorderColor = HyperTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color(0xFF0F1422),
                            unfocusedContainerColor = Color(0xFF0D111C)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true
                    )
                }
            }

            // Period Filter Segment
            item {
                PeriodFilterSelector(
                    selectedPeriod = uiState.timeFilter.period,
                    onPeriodSelected = { viewModel.setTimePeriod(it) }
                )
            }

            // Hero Spending Telemetry Card
            item {
                HeroSpendingTelemetryCard(summary = uiState.analyticsSummary)
            }

            // Smart Insight Observation Banner
            item {
                SmartInsightBanner(
                    headline = uiState.analyticsSummary.smartInsightHeadline,
                    detail = uiState.analyticsSummary.smartInsightDetail
                )
            }

            // Dual Telemetry Charts: Donut + Bar Chart
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.donutSegments.isNotEmpty()) {
                        ChromaMorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "OUTFLOW ALLOCATION",
                                    color = TextMutedSubtext,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ChromaDonutChart(
                                    segments = uiState.donutSegments,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (uiState.dailySpends.isNotEmpty()) {
                        ChromaMorphicCard(modifier = Modifier.fillMaxWidth()) {
                            ChromaBarChart(
                                data = uiState.dailySpends,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Category Budget Target Rings
            item {
                BudgetStatusRingsCard(budgets = uiState.budgetStatuses)
            }

            // 30-Day Heatmap Intensity Calendar
            item {
                val dailyHeatmapMap = remember(uiState.recentExpenses) {
                    val map = mutableMapOf<Int, Double>()
                    (1..30).forEach { map[it] = 0.0 }
                    val now = System.currentTimeMillis()
                    val dayMs = 86400000L
                    uiState.recentExpenses.filter { !it.isIncome }.forEach { exp ->
                        val daysAgo = ((now - exp.timestamp) / dayMs).toInt()
                        if (daysAgo in 0..29) {
                            val dayIndex = 30 - daysAgo // Day 1 to 30
                            map[dayIndex] = (map[dayIndex] ?: 0.0) + exp.amount
                        }
                    }
                    map
                }
                SpendingHeatmapCalendar(dailySpendMap = dailyHeatmapMap)
            }

            // Recent Transactions Stream Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE LEDGER STREAM",
                        color = TextMutedSubtext,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${uiState.recentExpenses.size} TXNS",
                        color = HyperTeal,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Staggered Transaction Ledger Items
            itemsIndexed(
                items = uiState.recentExpenses,
                key = { _, item -> item.id }
            ) { index, expense ->
                StaggeredExpenseItem(
                    expense = expense,
                    index = index,
                    onDelete = { viewModel.deleteExpense(it) },
                    onEdit = { onNavigateToDetail(it.id) },
                    onClick = { onNavigateToDetail(expense.id) }
                )
            }

            // Empty state if no transactions
            if (uiState.recentExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedCoinLoader(isSpinning = false)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NO TRANSACTIONS IN THIS WINDOW",
                                color = TextMutedSubtext,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Bottom Spacing for floating navigation
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

