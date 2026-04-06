package com.neuropulse.ui.onboarding

/**
 * ForgotPasswordState — all possible states of the password reset flow.
 *
 * Kept separate from [LoginUiState] because the reset flow is an async sub-flow
 * within the login screen — it runs independently of the main sign-in state machine.
 *
 * State machine:
 *   Idle → Loading (user submits email)
 *   Loading → Success (Firebase confirms email sent)
 *   Loading → Error (network error; account exists check stays opaque to prevent enumeration)
 *   Success / Error → Idle (dialog dismissed, or auto-dismiss after 2 s on Success)
 *
 * ADHD UX: Firebase succeeds even for unknown emails (prevents "I have no account" dead-end).
 * We always show "Check your email" on Success regardless — user is unstuck either way (DD-011).
 */
sealed class ForgotPasswordState {

    /** Dialog is not visible. Initial state, and the state after dismiss. */
    data object Idle : ForgotPasswordState()

    /** Reset email request is in flight. Submit button and email field are disabled. */
    data object Loading : ForgotPasswordState()

    /** Firebase confirmed the reset email was sent (or silently succeeded for unknown email). */
    data object Success : ForgotPasswordState()

    /**
     * Reset email request failed (network error, malformed email, etc.).
     *
     * @param message User-readable description — specific and actionable (DD-002 principle).
     */
    data class Error(val message: String) : ForgotPasswordState()
}
