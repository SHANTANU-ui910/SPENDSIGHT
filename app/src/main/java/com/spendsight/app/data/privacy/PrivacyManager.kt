package com.spendsight.app.data.privacy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object PrivacyManager {

    /**
     * Checks if SpendSight is authorized as an active Notification Listener.
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(packageName)
    }

    /**
     * Opens the Android system Notification Access Settings screen directly.
     */
    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Checks if the app is granted permission to draw floating overlays over other apps.
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Opens the System Alert Window (Draw Over Other Apps) settings screen for SpendSight.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Checks if the optional SMS debit alert read permission is granted.
     */
    fun isSmsPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Privacy Pledge & Architectural Guarantees for SpendSight Users
     */
    const val PRIVACY_PLEDGE_TITLE = "Zero-Cloud Privacy Architecture"
    const val PRIVACY_PLEDGE_BODY = """
SpendSight operates entirely on-device:

• 100% Local Intelligence: Transaction notifications and debit alerts are parsed inside your phone's memory. No financial data, account numbers, or balances are ever uploaded to any external server.
• Automatic OTP & Spam Filter: One-Time Passwords, verification codes, and personal messages are automatically discarded before processing.
• Offline Encrypted Ledger: Your spending database is stored exclusively on your device inside an encrypted SQLite database.
• Granular Control: You can revoke notification access or delete all recorded transaction history at any moment.
"""
}

