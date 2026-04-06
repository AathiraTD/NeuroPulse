package com.neuropulse.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuropulse.data.auth.FirebaseAuthRepositoryImpl
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.NetworkMonitor
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * LoginViewModel — orchestrates authentication and session persistence for the login screen.
 *
 * State is exposed as a [StateFlow<LoginUiState>] — the Composable observes this
 * and derives its entire UI from the current state value.
 *
 * Business rules:
 * - Typing in either field while [LoginUiState.Error] is active resets to [LoginUiState.Idle].
 *   This gives immediate positive feedback that the error is acknowledged (ADHD-friendly).
 * - [onSuccessConsumed] must be called by the NavGraph after consuming a [LoginUiState.Success]
 *   state, to prevent re-triggering navigation on back-stack pop.
 * - All auth operations run on [viewModelScope]; repositories handle Dispatcher.IO internally.
 *
 * Privacy: this ViewModel never logs or stores email, password, or idToken. Only UIDs
 * are passed to [UserPreferencesRepository] (ADR-002, privacy rules).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    companion object {
        private const val OFFLINE_MESSAGE =
            "No internet connection — connect to Wi-Fi or mobile data to sign in"
    }

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _showForgotPasswordDialog = MutableStateFlow(false)
    val showForgotPasswordDialog: StateFlow<Boolean> = _showForgotPasswordDialog.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    /**
     * Called on every keystroke in the email field.
     *
     * If the current state is [LoginUiState.Error], resets to [LoginUiState.Idle]
     * (preserving the password) to clear the error as soon as the user re-engages.
     */
    fun onEmailChange(email: String) {
        _uiState.update { current ->
            when (current) {
                is LoginUiState.Idle  -> current.copy(email = email)
                is LoginUiState.Error -> LoginUiState.Idle(email = email, password = current.password)
                else                  -> current
            }
        }
    }

    /**
     * Called on every keystroke in the password field.
     *
     * Mirrors [onEmailChange] — resets Error state on first password keystroke.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { current ->
            when (current) {
                is LoginUiState.Idle  -> current.copy(password = password)
                is LoginUiState.Error -> LoginUiState.Idle(email = current.email, password = password)
                else                  -> current
            }
        }
    }

    /**
     * Initiates sign-in with a Google ID token from the One-Tap UI.
     *
     * The UI layer is responsible for launching the One-Tap intent and extracting
     * the [idToken] from the returned [com.google.android.gms.auth.api.signin.GoogleSignInAccount].
     * This ViewModel does not import Google Sign-In classes directly.
     *
     * @param idToken The ID token string from GoogleSignInAccount.idToken.
     */
    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess { uid -> handleAuthSuccess(uid) }
                .onFailure { handleAuthFailure(it, currentEmail(), currentPassword()) }
        }
    }

    /**
     * Initiates email/password sign-in using the current [LoginUiState.Idle] field values.
     *
     * No-op if state is not [LoginUiState.Idle] (guards against double-tap on the CTA).
     */
    fun onEmailSignIn() {
        val current = _uiState.value as? LoginUiState.Idle ?: return
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithEmail(current.email, current.password)
                .onSuccess { uid -> handleAuthSuccess(uid) }
                .onFailure { handleAuthFailure(it, current.email, current.password) }
        }
    }

    /**
     * Initiates new account creation using the current [LoginUiState.Idle] field values.
     *
     * Called from the persona selection / sign-up flow (Phase 1b).
     * No-op if state is not [LoginUiState.Idle].
     */
    fun onCreateAccount() {
        val current = _uiState.value as? LoginUiState.Idle ?: return
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
            _uiState.value = LoginUiState.Loading
            authRepository.createAccountWithEmail(current.email, current.password)
                .onSuccess { uid -> handleAuthSuccess(uid) }
                .onFailure { handleAuthFailure(it, current.email, current.password) }
        }
    }

    /**
     * Signs in anonymously for the guest/try-without-account flow (E-005, DD-015).
     *
     * Intentionally does NOT call [handleAuthSuccess] — the anonymous UID is never written
     * to DataStore. When the app is killed and relaunched, [AuthGateViewModel] finds no
     * cached UID and routes back to LOGIN, which is the correct behaviour for a guest session.
     *
     * Network check still applies — Firebase anonymous auth requires a connection.
     */
    /**
     * Signs in via Yahoo, Microsoft, or Apple OAuth browser-redirect flow (DD-012).
     *
     * [activity] is the foreground [android.app.Activity], passed from the NavGraph
     * so this ViewModel does not hold any Android references directly. The Activity
     * reference must not be stored — it is used only for the duration of this call.
     *
     * @param activity   Current foreground Activity (typed as [Any] per ADR-001).
     * @param providerId Firebase OAuth provider ID: `"yahoo.com"`, `"microsoft.com"`, `"apple.com"`.
     */
    fun onOAuthSignIn(activity: Any, providerId: String) {
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithOAuthProvider(activity, providerId)
                .onSuccess { uid -> handleAuthSuccess(uid) }
                .onFailure { handleAuthFailure(it, currentEmail(), currentPassword()) }
        }
    }

    fun onContinueAsGuest() {
        viewModelScope.launch {
            if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
            _uiState.value = LoginUiState.Loading
            authRepository.signInAnonymously()
                .onSuccess {
                    // Deliberately skip DataStore write — session is ephemeral (E-005)
                    _uiState.value = LoginUiState.Success
                    Timber.tag("NeuroPulse").d("LoginViewModel: anonymous guest session started")
                }
                .onFailure { handleAuthFailure(it, currentEmail(), currentPassword()) }
        }
    }

    /**
     * Resets state to [LoginUiState.Idle] after the NavGraph has consumed a [LoginUiState.Success].
     *
     * MUST be called immediately after initiating navigation away from the login screen.
     * Without this, popping back to the login destination while state is still [Success]
     * re-triggers the navigation LaunchedEffect and creates an infinite navigation loop.
     */
    fun onSuccessConsumed() {
        _uiState.value = LoginUiState.Idle()
    }

    /**
     * Returns true if the user has already completed the onboarding persona selection.
     *
     * Called by [com.neuropulse.ui.navigation.NeuroPulseNavGraph.resolvePostLoginDestination]
     * after a successful sign-in to decide whether to route to HOME or PERSONA_SELECT.
     */
    suspend fun isOnboardingComplete(): Boolean =
        userPreferencesRepository.isOnboardingComplete()

    /**
     * Shows the forgot password dialog on user tap of "Forgot password?" link.
     */
    fun onForgotPasswordTap() {
        _showForgotPasswordDialog.value = true
    }

    /**
     * Hides the forgot password dialog (user cancels or confirms).
     */
    fun onForgotPasswordDialogDismiss() {
        _showForgotPasswordDialog.value = false
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }

    /**
     * Sends a password reset email to the given address.
     *
     * Shows a loading state while the email is being sent. On success, shows a confirmation
     * message and auto-closes the dialog after a short delay. On failure, shows an error message.
     */
    fun onForgotPasswordSubmit(email: String) {
        if (email.isEmpty()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Enter your email address")
            return
        }
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _forgotPasswordState.value = ForgotPasswordState.Success
                    // Auto-close after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    onForgotPasswordDialogDismiss()
                }
                .onFailure { exception ->
                    _forgotPasswordState.value = ForgotPasswordState.Error(
                        "Reset email failed — check your internet and try again"
                    )
                }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Emits an [LoginUiState.Error] without showing a loading spinner.
     *
     * Called before any Firebase call when [networkMonitor.isOnline] returns false.
     * Preserves current email/password so the user does not lose their input.
     * The message is specific and actionable — not "No connection" (DD-002).
     */
    private fun handleOffline() {
        _uiState.value = LoginUiState.Error(
            userMessage = OFFLINE_MESSAGE,
            email       = currentEmail(),
            password    = currentPassword(),
        )
    }

    private suspend fun handleAuthSuccess(uid: String) {
        userPreferencesRepository.saveFirebaseUid(uid)
        _uiState.value = LoginUiState.Success
        Timber.tag("NeuroPulse").d("LoginViewModel: auth success, UID cached")
    }

    private fun handleAuthFailure(exception: Throwable, email: String, password: String) {
        _uiState.value = LoginUiState.Error(
            userMessage = mapAuthError(exception),
            email       = email,
            password    = password,
        )
    }

    private fun currentEmail(): String =
        (_uiState.value as? LoginUiState.Idle)?.email ?: ""

    private fun currentPassword(): String =
        (_uiState.value as? LoginUiState.Idle)?.password ?: ""

    /**
     * Maps a Firebase auth exception to a user-readable, actionable string.
     *
     * Delegates to [FirebaseAuthRepositoryImpl.mapFirebaseError] so that Firebase
     * exception class references stay in the data layer. The ViewModel only handles
     * the presentation of the message, not the classification.
     */
    private fun mapAuthError(exception: Throwable): String =
        FirebaseAuthRepositoryImpl.mapFirebaseError(exception)
}

