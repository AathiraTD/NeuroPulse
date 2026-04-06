package com.neuropulse.ui.onboarding

/**
 * LoginUiState — all possible states of the login screen.
 *
 * Sealed so every call site can exhaustively handle each case in a `when` expression.
 *
 * [Idle] carries the current form field values so that [com.neuropulse.ui.onboarding.LoginViewModel]
 * is the single source of truth for form state — the Composable derives its field values
 * from this state rather than maintaining local `remember` variables.
 *
 * ADHD UX note: typing after an [Error] resets to [Idle], clearing the amber error
 * message immediately. This provides positive feedback that the user has re-engaged
 * without waiting for re-validation — reducing the anxiety of seeing a persistent error.
 */
sealed class LoginUiState {

    /**
     * The form is visible and accepting input. No async operation is in progress.
     *
     * @param email    Current text in the email field.
     * @param password Current text in the password field.
     */
    data class Idle(
        val email: String    = "",
        val password: String = "",
    ) : LoginUiState()

    /**
     * A sign-in or account-creation operation is in progress.
     *
     * All form inputs and buttons must be disabled while in this state.
     * The primary CTA shows a [androidx.compose.material3.CircularProgressIndicator]
     * in place of its label.
     */
    data object Loading : LoginUiState()

    /**
     * Sign-in succeeded. The NavGraph observes this state and navigates away.
     *
     * IMPORTANT: [com.neuropulse.ui.onboarding.LoginViewModel.onSuccessConsumed] must
     * be called immediately after navigation to reset this back to [Idle]. Without this
     * reset, popping the back stack to the login destination re-triggers navigation.
     */
    data object Success : LoginUiState()

    /**
     * Sign-in failed with a user-visible message.
     *
     * The [userMessage] is specific and actionable — never "Invalid credentials".
     * Displayed in [com.neuropulse.ui.theme.NeuroPulseColors.signal] (amber) — never red (DD-002).
     * The email and password fields are preserved so the user does not lose input.
     *
     * @param userMessage A human-readable, specific description of what went wrong.
     * @param email       Preserved from the failed attempt so the email field is not cleared.
     * @param password    Preserved from the failed attempt so the password field is not cleared.
     */
    data class Error(
        val userMessage: String,
        val email: String,
        val password: String,
    ) : LoginUiState()
}
