package com.spendsight.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spendsight_preferences")

class SpendSightPreferences(private val context: Context) {

    companion object {
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("key_biometric_enabled")
        val KEY_NOTIFICATION_DETECTION_ENABLED = booleanPreferencesKey("key_notification_detection_enabled")
        val KEY_SMS_DETECTION_ENABLED = booleanPreferencesKey("key_sms_detection_enabled")
        val KEY_OVERLAY_POPUP_ENABLED = booleanPreferencesKey("key_overlay_popup_enabled")
        val KEY_AUTO_DISMISS_SECONDS = intPreferencesKey("key_auto_dismiss_seconds")
        val KEY_LAST_BACKUP_TIMESTAMP = longPreferencesKey("key_last_backup_timestamp")
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val isNotificationDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_DETECTION_ENABLED] ?: true
    }

    val isSmsDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SMS_DETECTION_ENABLED] ?: false
    }

    val isOverlayPopupEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_OVERLAY_POPUP_ENABLED] ?: true
    }

    val autoDismissSeconds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_DISMISS_SECONDS] ?: 10 // Default 10 seconds as specified
    }

    val lastBackupTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_BACKUP_TIMESTAMP] ?: 0L
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setNotificationDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATION_DETECTION_ENABLED] = enabled }
    }

    suspend fun setSmsDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SMS_DETECTION_ENABLED] = enabled }
    }

    suspend fun setOverlayPopupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_OVERLAY_POPUP_ENABLED] = enabled }
    }

    suspend fun setAutoDismissSeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_AUTO_DISMISS_SECONDS] = seconds }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { it[KEY_LAST_BACKUP_TIMESTAMP] = timestamp }
    }
}

