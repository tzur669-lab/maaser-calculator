package com.maaser.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.maaser.app.data.model.AppLanguage
import com.maaser.app.data.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_settings")

@Singleton
class UserSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_MAASER_PERCENTAGE = doublePreferencesKey("maaser_percentage")
        val KEY_IS_GOOGLE_SIGNED_IN = booleanPreferencesKey("is_google_signed_in")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val KEY_DRIVE_LAST_BACKUP_AT = longPreferencesKey("drive_last_backup_at")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val settings: Flow<UserSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserSettings(
                maaserPercentage = prefs[KEY_MAASER_PERCENTAGE] ?: 10.0,
                isGoogleSignedIn = prefs[KEY_IS_GOOGLE_SIGNED_IN] ?: false,
                userId = prefs[KEY_USER_ID],
                userEmail = prefs[KEY_USER_EMAIL],
                lastSyncAt = prefs[KEY_LAST_SYNC_AT] ?: 0L,
                driveLastBackupAt = prefs[KEY_DRIVE_LAST_BACKUP_AT] ?: 0L,
                appLanguage = AppLanguage.entries.firstOrNull {
                    it.code == prefs[KEY_APP_LANGUAGE]
                } ?: AppLanguage.HEBREW
            )
        }

    suspend fun setMaaserPercentage(percentage: Double) {
        context.dataStore.edit { it[KEY_MAASER_PERCENTAGE] = percentage }
    }

    suspend fun setGoogleSignedIn(signedIn: Boolean, userId: String?, email: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_GOOGLE_SIGNED_IN] = signedIn
            if (userId != null) prefs[KEY_USER_ID] = userId else prefs.remove(KEY_USER_ID)
            if (email != null) prefs[KEY_USER_EMAIL] = email else prefs.remove(KEY_USER_EMAIL)
        }
    }

    suspend fun setLastSyncAt(timestamp: Long) {
        context.dataStore.edit { it[KEY_LAST_SYNC_AT] = timestamp }
    }

    suspend fun setDriveLastBackupAt(timestamp: Long) {
        context.dataStore.edit { it[KEY_DRIVE_LAST_BACKUP_AT] = timestamp }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_APP_LANGUAGE] = language.code }
    }
}
