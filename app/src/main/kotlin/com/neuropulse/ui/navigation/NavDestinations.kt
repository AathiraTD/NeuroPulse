package com.neuropulse.ui.navigation

/**
 * NavDestinations — all route string constants for the NeuroPulse NavGraph.
 *
 * Single source of truth for every route in the app.
 * CLAUDE: Never use a raw string for navigation. Always reference this object.
 * Adding a new screen requires: new constant here + new composable() in NeuroPulseNavGraph.
 */
object NavDestinations {

    /** Animated 4-phase entry splash. Always the first destination on first launch. */
    const val SPLASH = "splash"

    /** Firebase Auth login screen. Shown to unauthenticated users post-splash. */
    const val LOGIN = "login"

    /**
     * Persona selection questionnaire (Marcus vs. Zoe).
     * Shown after sign-up when onboarding is not yet complete.
     */
    const val PERSONA_SELECT = "persona_select"

    /**
     * Biometric lock screen — shown on relaunch when biometric opt-in is enabled (DD-014).
     * Triggers BiometricPrompt immediately. On success → HOME. On cancel → LOGIN.
     */
    const val BIOMETRIC_LOCK = "biometric_lock"

    /**
     * Create account / registration screen.
     * Full registration form with personal details, sign-in details, consent,
     * and social auth alternatives. Navigated to from LOGIN via "Create Account" link.
     */
    const val CREATE_ACCOUNT = "create_account"

    /**
     * Home / morning plan screen — Phase 2.
     * Authenticated + onboarded users land here.
     * Currently a placeholder stub until Phase 2 implementation.
     */
    const val HOME = "home"
}
