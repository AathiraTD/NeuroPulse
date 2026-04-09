package com.neuropulse.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.NetworkMonitor
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * CreateAccountViewModel — orchestrates account creation and form validation.
 *
 * State is exposed as a [StateFlow<CreateAccountUiState>] — the Composable observes
 * this and derives its entire UI from the current state value.
 *
 * Business rules:
 * - Typing in any field while [CreateAccountUiState.Error] resets to [CreateAccountUiState.Idle].
 * - The CREATE ACCOUNT button is only enabled when all fields are valid and consent is checked.
 * - Password must be at least 8 characters and match the confirm password field.
 * - [onSuccessConsumed] must be called by the NavGraph after consuming a Success state.
 *
 * Privacy: this ViewModel never logs email, password, or name fields. Only UIDs
 * are passed to [UserPreferencesRepository] (ADR-002).
 */
@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val OFFLINE_MESSAGE =
            "No internet connection — connect to Wi-Fi or mobile data"
    }

    private val _uiState = MutableStateFlow<CreateAccountUiState>(CreateAccountUiState.Idle())

    /** Current form state — observed by the Composable. */
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    /** Called on every keystroke in the first name field. */
    fun onFirstNameChange(value: String) {
        updateIdleField { it.copy(firstName = value) }
    }

    /** Called on every keystroke in the last name field. */
    fun onLastNameChange(value: String) {
        updateIdleField { it.copy(lastName = value) }
    }

    /** Called on every keystroke in the email field. */
    fun onEmailChange(value: String) {
        updateIdleField { it.copy(email = value) }
    }

    /** Called on every keystroke in the password field. */
    fun onPasswordChange(value: String) {
        updateIdleField { it.copy(password = value) }
    }

    /** Called on every keystroke in the confirm password field. */
    fun onConfirmPasswordChange(value: String) {
        updateIdleField { it.copy(confirmPassword = value) }
    }

    /** Called when the consent checkbox is toggled. */
    fun onConsentToggle(checked: Boolean) {
        updateIdleField { it.copy(consentChecked = checked) }
    }

    /**
     * Submits the account creation form.
     *
     * Validates all fields locally before making the network call.
     * Uses [AuthRepository.createAccountWithEmail] with the email and password.
     */
    fun onCreateAccount() {
        val current = currentIdle() ?: return
        val validationError = validateForm(current)
        if (validationError != null) {
            _uiState.value = CreateAccountUiState.Error(
                userMessage = validationError,
                firstName = current.firstName,
                lastName = current.lastName,
                email = current.email,
                password = current.password,
                confirmPassword = current.confirmPassword,
                consentChecked = current.consentChecked,
            )
            return
        }
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(current); return@launch }
            _uiState.value = CreateAccountUiState.Loading
            authRepository.createAccountWithEmail(current.email.trim(), current.password)
                .onSuccess { uid ->
                    handleSuccess(uid)
                    // Persist display name — first + last joined with a space
                    val displayName = "${current.firstName.trim()} ${current.lastName.trim()}"
                    userPreferencesRepository.setUserDisplayName(displayName)
                }
                .onFailure { handleFailure(it, current) }
        }
    }

    /**
     * Initiates sign-in via Yahoo, Microsoft, or Apple OAuth (DD-012).
     *
     * Reuses the same OAuth pattern as [LoginViewModel.onOAuthSignIn].
     * On success, the UID is cached and the user is navigated to persona selection.
     */
    fun onOAuthSignIn(activity: Any, providerId: String) {
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) {
                handleOffline(currentIdle() ?: CreateAccountUiState.Idle())
                return@launch
            }
            _uiState.value = CreateAccountUiState.Loading
            authRepository.signInWithOAuthProvider(activity, providerId)
                .onSuccess { uid -> handleSuccess(uid) }
                .onFailure { handleFailure(it, currentIdle() ?: CreateAccountUiState.Idle()) }
        }
    }

    /**
     * Initiates sign-in with a Google ID token from the One-Tap UI.
     *
     * Reuses the same pattern as [LoginViewModel.onGoogleSignIn].
     */
    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) {
                handleOffline(currentIdle() ?: CreateAccountUiState.Idle())
                return@launch
            }
            _uiState.value = CreateAccountUiState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess { uid -> handleSuccess(uid) }
                .onFailure { handleFailure(it, currentIdle() ?: CreateAccountUiState.Idle()) }
        }
    }

    /**
     * Returns true if the user has already completed the onboarding persona selection.
     *
     * Used by the NavGraph to route social auth returning users directly to HOME (I-5 fix).
     */
    suspend fun isOnboardingComplete(): Boolean =
        userPreferencesRepository.isOnboardingComplete()

    /**
     * Resets state to [CreateAccountUiState.Idle] after navigation consumes Success.
     */
    fun onSuccessConsumed() {
        _uiState.value = CreateAccountUiState.Idle()
    }

    /**
     * Reactive form validity — emits true when all fields pass validation.
     *
     * Derived from [uiState] so Compose observes changes automatically via
     * [collectAsStateWithLifecycle]. Works correctly in both Idle and Error states
     * (Error preserves field values, so the button re-enables when fields are valid).
     */
    val isFormValid: StateFlow<Boolean> = _uiState
        .map { state ->
            val idle = when (state) {
                is CreateAccountUiState.Idle -> state
                is CreateAccountUiState.Error -> CreateAccountUiState.Idle(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    consentChecked = state.consentChecked,
                )
                else -> return@map false
            }
            validateForm(idle) == null
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ── Private helpers ─────────────────────────────────────────────────────────

    private fun currentIdle(): CreateAccountUiState.Idle? =
        _uiState.value as? CreateAccountUiState.Idle

    /**
     * Updates a field in the Idle state. If current state is Error, resets to Idle
     * with preserved field values — clearing the error on first keystroke (ADHD-friendly).
     */
    private fun updateIdleField(transform: (CreateAccountUiState.Idle) -> CreateAccountUiState.Idle) {
        _uiState.update { current ->
            when (current) {
                is CreateAccountUiState.Idle -> transform(current)
                is CreateAccountUiState.Error -> transform(
                    CreateAccountUiState.Idle(
                        firstName = current.firstName,
                        lastName = current.lastName,
                        email = current.email,
                        password = current.password,
                        confirmPassword = current.confirmPassword,
                        consentChecked = current.consentChecked,
                    )
                )
                else -> current
            }
        }
    }

    /**
     * Validates all form fields. Returns null if valid, or an error message string.
     */
    private fun validateForm(state: CreateAccountUiState.Idle): String? = when {
        state.firstName.isBlank() -> "Enter your first name"
        state.lastName.isBlank() -> "Enter your last name"
        state.email.isBlank() -> "Enter your email address"
        !state.email.trim().contains("@") -> "Enter a valid email address"
        state.password.length < MIN_PASSWORD_LENGTH ->
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        state.password != state.confirmPassword -> "Passwords don't match"
        !state.consentChecked -> "Accept the Terms & Privacy Policy to continue"
        else -> null
    }

    private fun handleOffline(current: CreateAccountUiState.Idle) {
        _uiState.value = CreateAccountUiState.Error(
            userMessage = OFFLINE_MESSAGE,
            firstName = current.firstName,
            lastName = current.lastName,
            email = current.email,
            password = current.password,
            confirmPassword = current.confirmPassword,
            consentChecked = current.consentChecked,
        )
    }

    private suspend fun handleSuccess(uid: String) {
        userPreferencesRepository.saveFirebaseUid(uid)
        _uiState.value = CreateAccountUiState.Success
        Timber.tag("NeuroPulse").d("CreateAccountViewModel: account created, UID cached")
    }

    private fun handleFailure(exception: Throwable, current: CreateAccountUiState.Idle) {
        _uiState.value = CreateAccountUiState.Error(
            userMessage = mapAuthError(exception),
            firstName = current.firstName,
            lastName = current.lastName,
            email = current.email,
            password = current.password,
            confirmPassword = current.confirmPassword,
            consentChecked = current.consentChecked,
        )
    }

    /**
     * Maps an auth exception to a user-readable, actionable string.
     *
     * Uses simple-name matching so the presentation layer never imports Firebase (ADR-001).
     */
    private fun mapAuthError(exception: Throwable): String =
        when (exception::class.simpleName) {
            "FirebaseAuthUserCollisionException" ->
                "An account already exists for this email — try signing in instead"
            "FirebaseAuthWeakPasswordException" ->
                "Choose a stronger password — at least 8 characters with a mix of letters and numbers"
            "FirebaseAuthInvalidCredentialsException" ->
                "Check your email address and try again"
            else ->
                "Account creation failed — check your connection and try again"
        }
}
