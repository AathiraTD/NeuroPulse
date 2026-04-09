package com.neuropulse.domain.repository

import com.neuropulse.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * UserPreferencesRepository — read/write access to persisted user preferences.
 *
 * Implemented in the data layer ([com.neuropulse.data.local.UserPreferencesDataStoreImpl])
 * using Jetpack DataStore. Zero Android imports here (ADR-001).
 *
 * Individual suspend functions are provided alongside [observePreferences] so that
 * ViewModels can read a single value without collecting the full preferences stream.
 * [clearAll] is called on sign-out to purge all user-specific data from DataStore.
 */
interface UserPreferencesRepository {

    /** Returns the cached Firebase UID, or empty string if not set. */
    suspend fun getFirebaseUid(): String

    /** Persists the Firebase UID after a successful sign-in. */
    suspend fun saveFirebaseUid(uid: String)

    /** Returns true if the user has completed the onboarding persona selection. */
    suspend fun isOnboardingComplete(): Boolean

    /** Marks onboarding as complete. Called after persona selection is saved. */
    suspend fun setOnboardingComplete()

    /** Returns the selected persona ("MARCUS", "ZOE", or empty string). */
    suspend fun getUserPersona(): String

    /** Persists the selected persona. Use [UserPreferences.PERSONA_MARCUS] / [UserPreferences.PERSONA_ZOE]. */
    suspend fun setUserPersona(persona: String)

    /** Returns the user's display name, or empty string if not set. */
    suspend fun getUserDisplayName(): String

    /** Persists the user's display name (first + last) after account creation. */
    suspend fun setUserDisplayName(displayName: String)

    /** Returns true if the animated splash has been shown at least once. */
    suspend fun hasSeenSplash(): Boolean

    /** Marks the splash as seen. Called from [com.neuropulse.ui.navigation.NeuroPulseNavGraph] after first run. */
    suspend fun markSplashSeen()

    /** Returns true if the user has opted into biometric lock-on-reopen (DD-010, DD-014). */
    suspend fun isBiometricEnabled(): Boolean

    /** Persists the user's biometric preference. Pass true to enable lock-on-reopen. */
    suspend fun setBiometricEnabled(enabled: Boolean)

    /** Returns true if the first-Home biometric setup popup has already been shown (DD-010). */
    suspend fun hasBiometricPromptBeenShown(): Boolean

    /** Marks the biometric setup popup as shown so it never reappears (DD-010). */
    suspend fun markBiometricPromptShown()

    /**
     * Clears all persisted user preferences.
     *
     * Called on sign-out. Ensures no stale user data remains after session ends.
     */
    suspend fun clearAll()

    /**
     * Emits the full [UserPreferences] snapshot whenever any preference changes.
     *
     * Use in ViewModels that need to react to any preference change.
     * For one-shot reads, prefer the individual suspend functions above.
     */
    fun observePreferences(): Flow<UserPreferences>
}
