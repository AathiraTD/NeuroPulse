package com.neuropulse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neuropulse.domain.model.UserPreferences
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// File-level delegate — must be declared here, not inside the class body.
// DataStore is a singleton per name; this ensures a single instance for the app lifetime.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "neuropulse_user_prefs",
)

/**
 * UserPreferencesDataStoreImpl — DataStore-backed implementation of [UserPreferencesRepository].
 *
 * All preference keys are declared as typed [Preferences.Key] constants in the companion object.
 * No raw strings are used elsewhere — the companion object is the single source of key names.
 *
 * All write operations use [DataStore.edit] which is atomic and crash-safe.
 * Read operations first() the flow for one-shot values, or expose the Flow directly.
 */
@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private companion object {
        val KEY_FIREBASE_UID               = stringPreferencesKey("firebase_uid")
        val KEY_ONBOARDING_COMPLETE        = booleanPreferencesKey("onboarding_complete")
        val KEY_USER_PERSONA               = stringPreferencesKey("user_persona")
        val KEY_HAS_SEEN_SPLASH            = booleanPreferencesKey("has_seen_splash")
        val KEY_BIOMETRIC_ENABLED          = booleanPreferencesKey("biometric_enabled")
        val KEY_BIOMETRIC_PROMPT_SHOWN     = booleanPreferencesKey("biometric_prompt_shown")
    }

    override fun observePreferences(): Flow<UserPreferences> =
        context.dataStore.data.map { prefs ->
            UserPreferences(
                firebaseUid                  = prefs[KEY_FIREBASE_UID] ?: "",
                isOnboardingComplete         = prefs[KEY_ONBOARDING_COMPLETE] ?: false,
                userPersona                  = prefs[KEY_USER_PERSONA] ?: "",
                hasSeenSplash                = prefs[KEY_HAS_SEEN_SPLASH] ?: false,
                isBiometricEnabled           = prefs[KEY_BIOMETRIC_ENABLED] ?: false,
                hasBiometricPromptBeenShown  = prefs[KEY_BIOMETRIC_PROMPT_SHOWN] ?: false,
            )
        }

    override suspend fun getFirebaseUid(): String =
        context.dataStore.data.first()[KEY_FIREBASE_UID] ?: ""

    override suspend fun saveFirebaseUid(uid: String) {
        context.dataStore.edit { it[KEY_FIREBASE_UID] = uid }
        Timber.tag("NeuroPulse").d("UserPrefs: Firebase UID cached")
    }

    override suspend fun isOnboardingComplete(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING_COMPLETE] ?: false

    override suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = true }
        Timber.tag("NeuroPulse").d("UserPrefs: onboarding marked complete")
    }

    override suspend fun getUserPersona(): String =
        context.dataStore.data.first()[KEY_USER_PERSONA] ?: ""

    override suspend fun setUserPersona(persona: String) {
        context.dataStore.edit { it[KEY_USER_PERSONA] = persona }
        Timber.tag("NeuroPulse").d("UserPrefs: persona set to $persona")
    }

    override suspend fun hasSeenSplash(): Boolean =
        context.dataStore.data.first()[KEY_HAS_SEEN_SPLASH] ?: false

    override suspend fun markSplashSeen() {
        context.dataStore.edit { it[KEY_HAS_SEEN_SPLASH] = true }
    }

    override suspend fun isBiometricEnabled(): Boolean =
        context.dataStore.data.first()[KEY_BIOMETRIC_ENABLED] ?: false

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
        Timber.tag("NeuroPulse").d("UserPrefs: biometric_enabled = $enabled")
    }

    override suspend fun hasBiometricPromptBeenShown(): Boolean =
        context.dataStore.data.first()[KEY_BIOMETRIC_PROMPT_SHOWN] ?: false

    override suspend fun markBiometricPromptShown() {
        context.dataStore.edit { it[KEY_BIOMETRIC_PROMPT_SHOWN] = true }
        Timber.tag("NeuroPulse").d("UserPrefs: biometric prompt marked shown")
    }

    override suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
        Timber.tag("NeuroPulse").d("UserPrefs: all preferences cleared (sign-out)")
    }
}
