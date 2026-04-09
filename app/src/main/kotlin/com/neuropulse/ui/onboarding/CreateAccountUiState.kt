package com.neuropulse.ui.onboarding

/**
 * CreateAccountUiState — all possible states of the create account screen.
 *
 * Sealed so every call site can exhaustively handle each case in a `when` expression.
 * Follows the same pattern as [LoginUiState] — form field values are carried in [Idle]
 * and [Error] so the ViewModel is the single source of truth.
 *
 * ADHD UX note: typing after an [Error] resets to [Idle], clearing the amber error
 * message immediately — same instant-feedback pattern as [LoginUiState].
 */
sealed class CreateAccountUiState {

    /**
     * The form is visible and accepting input. No async operation is in progress.
     *
     * @param firstName       Current text in the first name field.
     * @param lastName        Current text in the last name field.
     * @param email           Current text in the email field.
     * @param password        Current text in the password field.
     * @param confirmPassword Current text in the confirm password field.
     * @param consentChecked  Whether the terms & privacy consent checkbox is checked.
     */
    data class Idle(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val consentChecked: Boolean = false,
    ) : CreateAccountUiState()

    /**
     * An account creation operation is in progress.
     *
     * All form inputs and buttons must be disabled while in this state.
     * The primary CTA shows a [androidx.compose.material3.CircularProgressIndicator].
     */
    data object Loading : CreateAccountUiState()

    /**
     * Account creation succeeded. The NavGraph observes this state and navigates away.
     *
     * IMPORTANT: [CreateAccountViewModel.onSuccessConsumed] must be called immediately
     * after navigation to reset this back to [Idle] — same pattern as [LoginUiState.Success].
     */
    data object Success : CreateAccountUiState()

    /**
     * Account creation failed with a user-visible message.
     *
     * Displayed in amber ([com.neuropulse.ui.theme.NeuroPulseColors.signal]) — never red.
     * All form fields are preserved so the user does not lose input.
     *
     * @param userMessage A human-readable, specific description of what went wrong.
     */
    data class Error(
        val userMessage: String,
        val firstName: String,
        val lastName: String,
        val email: String,
        val password: String,
        val confirmPassword: String,
        val consentChecked: Boolean,
    ) : CreateAccountUiState()
}
