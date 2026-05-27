package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clarte_settings")

class SettingsStore(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_FORCE_MOCK_AI = booleanPreferencesKey("force_mock_ai")
        val KEY_HIDE_PREVIEWS_HISTORY = booleanPreferencesKey("hide_previews_history")
        val KEY_HIDE_LATEST_PREVIEW_HOME = booleanPreferencesKey("hide_latest_preview_home")
        val KEY_ENCRYPTED_BACKUP_ENABLED = booleanPreferencesKey("encrypted_backup_enabled")
        val KEY_WRAPPED_ENCRYPTION_KEY = stringPreferencesKey("wrapped_encryption_key")
        val KEY_ENCRYPTION_KEY_IV = stringPreferencesKey("encryption_key_iv")
        val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        
        val KEY_IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val KEY_IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val KEY_LOCK_ON_BACKGROUND = booleanPreferencesKey("lock_on_background")
        val KEY_AUTO_LOCK_DELAY_MINUTES = androidx.datastore.preferences.core.intPreferencesKey("auto_lock_delay_minutes")
        val KEY_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val KEY_PIN_SALT = stringPreferencesKey("app_lock_pin_salt")
        val KEY_HIDE_EXPORT_WARNING = booleanPreferencesKey("hide_export_warning")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "système"
    }

    val forceMockAiFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_FORCE_MOCK_AI] ?: false
    }

    val hidePreviewsHistoryFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HIDE_PREVIEWS_HISTORY] ?: false
    }

    val hideLatestPreviewHomeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HIDE_LATEST_PREVIEW_HOME] ?: false
    }

    val encryptedBackupEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ENCRYPTED_BACKUP_ENABLED] ?: false
    }

    val wrappedEncryptionKeyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_WRAPPED_ENCRYPTION_KEY]
    }

    val encryptionKeyIvFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ENCRYPTION_KEY_IV]
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAS_COMPLETED_ONBOARDING] ?: false
    }

    val isAppLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_APP_LOCK_ENABLED] ?: false
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_BIOMETRIC_ENABLED] ?: false
    }

    val lockOnBackgroundFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOCK_ON_BACKGROUND] ?: true
    }

    val autoLockDelayMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_LOCK_DELAY_MINUTES] ?: 0
    }

    val pinHashFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_HASH]
    }

    val pinSaltFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_SALT]
    }

    val hideExportWarningFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HIDE_EXPORT_WARNING] ?: false
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.lowercase()
        }
    }

    suspend fun setForceMockAi(force: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FORCE_MOCK_AI] = force
        }
    }

    suspend fun setHidePreviewsHistory(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIDE_PREVIEWS_HISTORY] = hide
        }
    }

    suspend fun setHideLatestPreviewHome(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIDE_LATEST_PREVIEW_HOME] = hide
        }
    }

    suspend fun setEncryptedBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ENCRYPTED_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setWrappedEncryptionKey(key: String, iv: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WRAPPED_ENCRYPTION_KEY] = key
            preferences[KEY_ENCRYPTION_KEY_IV] = iv
        }
    }

    suspend fun clearEncryptionData() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ENCRYPTED_BACKUP_ENABLED] = false
            preferences.remove(KEY_WRAPPED_ENCRYPTION_KEY)
            preferences.remove(KEY_ENCRYPTION_KEY_IV)
        }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setLockOnBackground(lock: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_ON_BACKGROUND] = lock
        }
    }

    suspend fun setAutoLockDelayMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_LOCK_DELAY_MINUTES] = minutes
        }
    }

    suspend fun setPin(hashBase64: String?, saltBase64: String?) {
        context.dataStore.edit { preferences ->
            if (hashBase64 == null || saltBase64 == null) {
                preferences.remove(KEY_PIN_HASH)
                preferences.remove(KEY_PIN_SALT)
            } else {
                preferences[KEY_PIN_HASH] = hashBase64
                preferences[KEY_PIN_SALT] = saltBase64
            }
        }
    }

    suspend fun setHideExportWarning(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIDE_EXPORT_WARNING] = hide
        }
    }
}
