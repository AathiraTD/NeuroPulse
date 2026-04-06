package com.neuropulse.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.neuropulse.ui.theme.NeuroPulseTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.neuropulse.BuildConfig
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.UserPreferencesRepository
import com.neuropulse.ui.home.BiometricLockScreen
import com.neuropulse.ui.home.BiometricSetupDialog
import com.neuropulse.ui.home.HomeViewModel
import com.neuropulse.ui.onboarding.ForgotPasswordDialog
import com.neuropulse.ui.onboarding.ForgotPasswordState
import com.neuropulse.ui.onboarding.LoginScreen
import com.neuropulse.ui.onboarding.LoginUiState
import com.neuropulse.ui.onboarding.LoginViewModel
import com.neuropulse.ui.onboarding.PersonaSelectionScreen
import com.neuropulse.ui.onboarding.PersonaSelectionViewModel
import com.neuropulse.ui.onboarding.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Auth gate ViewModel ───────────────────────────────────────────────────────

/**
 * AuthGateViewModel — resolves the correct start destination on app cold start.
 *
 * Runs [resolveStartDestination] in a [kotlinx.coroutines.flow.flow] so the check
 * happens asynchronously. [MainActivity] waits for the first non-null emission
 * before rendering the [NeuroPulseNavGraph], preventing any wrong-screen flash.
 *
 * Auth gate logic:
 * - Debug builds with SKIP_AUTH=true → HOME immediately (never shown to users)
 * - No cached UID → SPLASH (first launch; full splash + login flow)
 * - Cached UID, invalid token → LOGIN (token expired; re-auth without splash)
 * - Cached UID, valid token, onboarding incomplete → PERSONA_SELECT
 * - Cached UID, valid token, onboarding complete, biometric enabled → BIOMETRIC_LOCK (DD-014)
 * - Cached UID, valid token, onboarding complete, biometric disabled → HOME
 *
 * The auth check runs concurrently with the splash animation on first launch —
 * latency is hidden behind the ~2400ms animation window (DD-004).
 */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * Emits the resolved start destination route once the auth check completes.
     * Emits null until the check finishes — the UI shows nothing until first non-null.
     */
    val startDestination: StateFlow<String?> = flow {
        emit(resolveStartDestination())
    }.stateIn(
        scope            = viewModelScope,
        started          = SharingStarted.WhileSubscribed(5_000),
        initialValue     = null,
    )

    private suspend fun resolveStartDestination(): String {
        if (BuildConfig.DEBUG && BuildConfig.SKIP_AUTH) return NavDestinations.HOME
        val uid = userPreferencesRepository.getFirebaseUid()
        if (uid.isEmpty()) return NavDestinations.SPLASH
        val tokenValid = authRepository.isTokenValid()
        return when {
            !tokenValid                                         -> NavDestinations.LOGIN
            !userPreferencesRepository.isOnboardingComplete()  -> NavDestinations.PERSONA_SELECT
            userPreferencesRepository.isBiometricEnabled()     -> NavDestinations.BIOMETRIC_LOCK
            else                                               -> NavDestinations.HOME
        }
    }
}

// ── NavGraph ──────────────────────────────────────────────────────────────────

/**
 * NeuroPulseNavGraph — the root navigation host for the NeuroPulse app.
 *
 * CLAUDE: Do not add business logic here. Route logic belongs in [AuthGateViewModel]
 * and screen-specific ViewModels. This composable only wires destinations to composables.
 *
 * Navigation rules:
 * - SPLASH always pops itself inclusive after completion (no back-stack entry).
 * - LOGIN pops itself inclusive on [LoginUiState.Success] (no back-to-login on back press).
 * - PERSONA_SELECT pops itself inclusive after persona is saved.
 *
 * @param navController     NavController — passed in for testability (can be overridden).
 * @param startDestination  Pre-resolved start route from [AuthGateViewModel].
 */
@Composable
fun NeuroPulseNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(NavDestinations.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        // ── Login ─────────────────────────────────────────────────────────────
        composable(NavDestinations.LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val showForgotPasswordDialog by viewModel.showForgotPasswordDialog.collectAsStateWithLifecycle()
            val forgotPasswordState by viewModel.forgotPasswordState.collectAsStateWithLifecycle()

            // Captured outside LaunchedEffect — composition locals are not accessible inside
            val view = LocalView.current

            // Consume Success state and navigate — prevents re-trigger on back-pop (plan §pitfall 4)
            LaunchedEffect(uiState) {
                if (uiState is LoginUiState.Success) {
                    // Dopamine reward haptic on auth success — fired before navigation so the
                    // user feels it while still on the login screen (not mid-transition).
                    // CONFIRM (API 30+) is a crisp double-tap pattern designed for confirmations.
                    // VIRTUAL_KEY is the closest pre-30 equivalent — short, distinct click.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    viewModel.onSuccessConsumed()
                    val destination = resolvePostLoginDestination(viewModel)
                    navController.navigate(destination) {
                        popUpTo(NavDestinations.LOGIN) { inclusive = true }
                    }
                }
            }

            // CredentialManager setup for Google One-Tap (E-001 fix)
            // Activity reference captured here for OAuth browser-redirect flow (DD-012)
            val context = LocalContext.current
            val activity = context as androidx.fragment.app.FragmentActivity
            val scope = rememberCoroutineScope()
            val credentialManager = remember { CredentialManager.create(context) }

            // Show forgot password dialog (E-002 fix)
            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    uiState  = forgotPasswordState,
                    onSubmit = viewModel::onForgotPasswordSubmit,
                    onDismiss = viewModel::onForgotPasswordDialogDismiss,
                )
            }

            LoginScreen(
                uiState            = uiState,
                onContinueAsGuest  = viewModel::onContinueAsGuest,
                onGoogleSignIn     = {
                    scope.launch {
                        try {
                            val request = GetCredentialRequest(
                                listOf(
                                    GetGoogleIdOption.Builder()
                                        .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                                        .build()
                                )
                            )
                            val result = credentialManager.getCredential(context, request)
                            val idToken = (result.credential as GoogleIdTokenCredential).idToken
                            viewModel.onGoogleSignIn(idToken)
                        } catch (e: GetCredentialException) {
                            // User cancelled or no credential available — fail silently
                            viewModel.onGoogleSignIn("")
                        } catch (e: Exception) {
                            // Unexpected error — pass empty idToken to trigger error state
                            viewModel.onGoogleSignIn("")
                        }
                    }
                },
                onEmailChange      = viewModel::onEmailChange,
                onPasswordChange   = viewModel::onPasswordChange,
                onEmailSignIn      = viewModel::onEmailSignIn,
                onForgotPasswordTap = viewModel::onForgotPasswordTap,
                onNavigateToSignUp = {
                    navController.navigate(NavDestinations.PERSONA_SELECT)
                },
                onOAuthSignIn = { providerId ->
                    viewModel.onOAuthSignIn(activity, providerId)
                },
            )
        }

        // ── Persona selection ─────────────────────────────────────────────────
        composable(NavDestinations.PERSONA_SELECT) {
            val personaViewModel: PersonaSelectionViewModel = hiltViewModel()

            PersonaSelectionScreen(
                onPersonaSelected = { persona ->
                    // Save persona + mark onboarding complete, then navigate to HOME (E-003 fix)
                    personaViewModel.selectPersona(persona) {
                        navController.navigate(NavDestinations.HOME) {
                            popUpTo(NavDestinations.PERSONA_SELECT) { inclusive = true }
                        }
                    }
                },
            )
        }

        // ── Biometric lock — relaunch gate when opt-in is enabled (DD-014) ──────
        composable(NavDestinations.BIOMETRIC_LOCK) {
            BiometricLockScreen(
                onAuthenticated = {
                    navController.navigate(NavDestinations.HOME) {
                        popUpTo(NavDestinations.BIOMETRIC_LOCK) { inclusive = true }
                    }
                },
                onUsePasswordInstead = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.BIOMETRIC_LOCK) { inclusive = true }
                    }
                },
            )
        }

        // ── Home — Phase 2 stub with biometric dialog + guest banner ─────────
        composable(NavDestinations.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val showBiometricSetupDialog by homeViewModel.showBiometricSetupDialog.collectAsStateWithLifecycle()
            val isGuest = homeViewModel.isGuestSession

            // Check once on first composition — no-op on subsequent calls
            LaunchedEffect(Unit) { homeViewModel.checkBiometricPrompt() }

            if (showBiometricSetupDialog) {
                BiometricSetupDialog(
                    onEnable  = homeViewModel::enableBiometric,
                    onDismiss = homeViewModel::dismissBiometricPrompt,
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Guest session banner — visible for all anonymous users (E-005)
                if (isGuest) {
                    Surface(
                        color    = NeuroPulseTheme.colors.signal.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text     = "Guest mode — your progress won't be saved. Create an account to keep it.",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = NeuroPulseTheme.colors.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }

                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "NeuroPulse — Phase 2 coming soon",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Determines where to navigate after a successful login (E-003 fix).
 *
 * - New users (onboarding incomplete) → PERSONA_SELECT
 * - Returning users (onboarding complete) → HOME
 *
 * [LoginViewModel.isOnboardingComplete] delegates to [UserPreferencesRepository] so
 * no DataStore imports are needed here. Extracted to keep the [LaunchedEffect] concise.
 */
private suspend fun resolvePostLoginDestination(viewModel: LoginViewModel): String =
    if (viewModel.isOnboardingComplete()) NavDestinations.HOME else NavDestinations.PERSONA_SELECT
