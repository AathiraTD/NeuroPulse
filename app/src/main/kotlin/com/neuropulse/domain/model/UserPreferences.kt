package com.neuropulse.domain.model

/**
 * UserPreferences — the persisted preferences for a NeuroPulse user.
 *
 * Pure Kotlin data class — zero Android imports (ADR-001).
 * Constructed from DataStore values in [com.neuropulse.domain.repository.UserPreferencesRepository].
 *
 * @param firebaseUid           Cached Firebase UID. Empty string means no authenticated user.
 * @param isOnboardingComplete  Whether the persona selection questionnaire has been completed.
 * @param userPersona           "MARCUS", "ZOE", or empty string if persona not yet selected.
 *                              Marcus = time-blindness dominant. Zoe = hyperfocus dominant.
 * @param hasSeenSplash              Whether the animated splash has been shown at least once.
 *                                   Allows the splash to run only on first launch if desired.
 * @param isBiometricEnabled         Whether the user has opted into biometric lock-on-reopen.
 *                                   When true, AuthGateViewModel routes to BIOMETRIC_LOCK (DD-014).
 * @param hasBiometricPromptBeenShown Whether the first-Home biometric setup popup has been shown.
 *                                   Prevents the popup from reappearing on subsequent Home visits (DD-010).
 */
data class UserPreferences(
    val firebaseUid: String               = "",
    val isOnboardingComplete: Boolean      = false,
    val userPersona: String               = "",
    val hasSeenSplash: Boolean            = false,
    val isBiometricEnabled: Boolean       = false,
    val hasBiometricPromptBeenShown: Boolean = false,
) {
    companion object {
        /** Returned when DataStore has not yet been written (true first-launch state). */
        val EMPTY = UserPreferences()

        /** Persona constant for the time-blindness dominant user profile. */
        const val PERSONA_MARCUS = "MARCUS"

        /** Persona constant for the hyperfocus dominant user profile. */
        const val PERSONA_ZOE = "ZOE"
    }
}
