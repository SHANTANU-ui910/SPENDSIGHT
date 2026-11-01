package com.spendsight.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.spendsight.app.R
import com.spendsight.app.data.preferences.SpendSightPreferences
import com.spendsight.app.domain.repository.CategoryRepository
import com.spendsight.app.domain.repository.ExpenseRepository
import com.spendsight.app.ui.overlay.PaymentOverlayContent
import com.spendsight.app.ui.theme.SpendSightTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var preferences: SpendSightPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    companion object {
        private const val TAG = "PaymentOverlayService"
        const val CHANNEL_ID = "spendsight_payment_overlay"
        const val NOTIFICATION_ID = 9001
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val expenseId = intent?.getLongExtra(SpendNotificationListenerService.EXTRA_EXPENSE_ID, -1L) ?: -1L
        val amount = intent?.getDoubleExtra(SpendNotificationListenerService.EXTRA_AMOUNT, 0.0) ?: 0.0
        val merchant = intent?.getStringExtra(SpendNotificationListenerService.EXTRA_MERCHANT) ?: "Unknown Merchant"
        val suggestedCategoryId = intent?.getLongExtra(SpendNotificationListenerService.EXTRA_SUGGESTED_CATEGORY_ID, 10L) ?: 10L
        val suggestedCategoryName = intent?.getStringExtra(SpendNotificationListenerService.EXTRA_SUGGESTED_CATEGORY_NAME) ?: "Other"
        val upiRef = intent?.getStringExtra(SpendNotificationListenerService.EXTRA_UPI_REF)
        val accountLast4 = intent?.getStringExtra(SpendNotificationListenerService.EXTRA_ACCOUNT_LAST4)
        val timestamp = intent?.getLongExtra(SpendNotificationListenerService.EXTRA_TIMESTAMP, System.currentTimeMillis()) ?: System.currentTimeMillis()

        // 1. Post Foreground Notification to satisfy Android platform service lifecycle
        val foregroundNotif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Payment Detected: ₹${amount.toInt()}")
            .setContentText("Categorizing payment to $merchant")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        startForeground(NOTIFICATION_ID, foregroundNotif)

        // 2. Check if Draw Over Other Apps is permitted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            showFloatingOverlay(
                expenseId = expenseId,
                amount = amount,
                merchant = merchant,
                suggestedCategoryId = suggestedCategoryId,
                suggestedCategoryName = suggestedCategoryName,
                upiRef = upiRef,
                accountLast4 = accountLast4,
                timestamp = timestamp
            )
        } else {
            // Fallback: Heads-up Interactive Notification with 1-tap action buttons
            showHeadsUpFallback(
                expenseId = expenseId,
                amount = amount,
                merchant = merchant,
                suggestedCategoryName = suggestedCategoryName
            )
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun showFloatingOverlay(
        expenseId: Long,
        amount: Double,
        merchant: String,
        suggestedCategoryId: Long,
        suggestedCategoryName: String,
        upiRef: String?,
        accountLast4: String?,
        timestamp: Long
    ) {
        removeOverlayView()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 0
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PaymentOverlayService)
            setViewTreeViewModelStoreOwner(this@PaymentOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PaymentOverlayService)

            setContent {
                SpendSightTheme(darkTheme = true) {
                    PaymentOverlayContent(
                        amount = amount,
                        merchant = merchant,
                        suggestedCategoryId = suggestedCategoryId,
                        suggestedCategoryName = suggestedCategoryName,
                        upiRef = upiRef,
                        accountLast4 = accountLast4,
                        timestamp = timestamp,
                        autoDismissDurationSeconds = 10,
                        onSave = { chosenCategoryId, note ->
                            serviceScope.launch(Dispatchers.IO) {
                                val expense = expenseRepository.getExpenseById(expenseId).first()
                                if (expense != null) {
                                    val cat = categoryRepository.getCategoryById(chosenCategoryId)
                                    val updated = expense.copy(
                                        categoryId = chosenCategoryId,
                                        categoryName = cat?.name ?: "Other",
                                        notes = if (note.isNotBlank()) note else expense.notes
                                    )
                                    expenseRepository.updateExpense(updated)
                                    Log.d(TAG, "Expense $expenseId updated to category ${cat?.name}")
                                }
                            }
                            removeOverlayView()
                            stopSelf()
                        },
                        onSkip = {
                            removeOverlayView()
                            stopSelf()
                        }
                    )
                }
            }
        }

        overlayView = composeView
        windowManager?.addView(composeView, layoutParams)
    }

    private fun showHeadsUpFallback(
        expenseId: Long,
        amount: Double,
        merchant: String,
        suggestedCategoryName: String
    ) {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun createCategoryAction(name: String, catId: Long): NotificationCompat.Action {
            val intent = Intent(this, QuickCategoryReceiver::class.java).apply {
                action = QuickCategoryReceiver.ACTION_CATEGORIZE
                putExtra(QuickCategoryReceiver.EXTRA_EXPENSE_ID, expenseId)
                putExtra(QuickCategoryReceiver.EXTRA_CATEGORY_ID, catId)
                putExtra(QuickCategoryReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID + 1)
            }
            val pending = PendingIntent.getBroadcast(
                this,
                catId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(0, name, pending).build()
        }

        val headsUpNotif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚡ ₹${amount.toInt()} paid to $merchant")
            .setContentText("Suggested: $suggestedCategoryName • Tap to categorize")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(createCategoryAction("🍔 Food", 1L))
            .addAction(createCategoryAction("🚕 Travel", 2L))
            .addAction(createCategoryAction("💡 Bills", 3L))
            .addAction(createCategoryAction("🛍 Shopping", 4L))
            .setAutoCancel(true)
            .setTimeoutAfter(10000L) // Auto dismiss after 10 seconds
            .build()

        notifManager.notify(NOTIFICATION_ID + 1, headsUpNotif)
    }

    private fun removeOverlayView() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Failed removing overlay view", e)
            }
            overlayView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SpendSight Instant Pop-ups",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Displays instant floating payment categorization pop-ups."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        removeOverlayView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

