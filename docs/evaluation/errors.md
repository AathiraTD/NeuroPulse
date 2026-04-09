# NeuroPulse — Errors Log

Running log of every non-trivial bug encountered during development.
Format: symptom → root cause → fix → learning.
Feeds into demo talking points and demonstrates debugging skills for CMP6213 assessment.

CLAUDE: Append a new entry here for every non-trivial bug identified or fixed.
Format: `## E-XXX — Title` with Date, Phase, Symptom, Root Cause, Fix, Learning.

---

<!-- Append entries below this line in format E-XXX -->

---

## E-001 — Google Sign-In button does nothing on tap

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/navigation/NeuroPulseNavGraph.kt:141`](../app/src/main/kotlin/com/neuropulse/ui/navigation/NeuroPulseNavGraph.kt#L141)

**Symptom:** User taps "Continue with Google" — no loading state, no system prompt, no error. Silent no-op.

**Root cause:** The CredentialManager intent that retrieves a Google `idToken` from the device was never wired. The Firebase backend (`FirebaseAuthRepositoryImpl.signInWithGoogle`) and the ViewModel handler (`LoginViewModel.onGoogleSignIn`) are both complete and correct. Only the UI-side token retrieval step is missing.

**Before (current — broken):**
```kotlin
// NeuroPulseNavGraph.kt:141
onGoogleSignIn = { /* TODO Phase 1b: launch One-Tap, pass idToken to viewModel */ },
```

**After (Phase 1b fix):**
```kotlin
// NeuroPulseNavGraph.kt — inside LOGIN composable block
val context = LocalContext.current
val scope = rememberCoroutineScope()
val credentialManager = remember { CredentialManager.create(context) }

onGoogleSignIn = {
    scope.launch {
        val request = GetCredentialRequest(
            listOf(GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                .build())
        )
        runCatching { credentialManager.getCredential(context, request) }
            .onSuccess { result ->
                val idToken = (result.credential as GoogleIdTokenCredential).idToken
                viewModel.onGoogleSignIn(idToken)
            }
            .onFailure { viewModel.onGoogleSignIn("") } // surfaces error state
    }
},
```

**Status:** Fixed (Phase 1b, 2026-04)

---

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/onboarding/LoginScreen.kt:161-202`](../app/src/main/kotlin/com/neuropulse/ui/onboarding/LoginScreen.kt#L161-L202), [`domain/repository/AuthRepository.kt`](../app/src/main/kotlin/com/neuropulse/domain/repository/AuthRepository.kt)

**Symptom:** Email user enters wrong password, sees the amber error message, and has no recovery path. No "Forgot your password?" link exists anywhere in the UI or codebase. The user is completely stuck.

**Root cause:** `sendPasswordResetEmail()` was never added to `AuthRepository` or `FirebaseAuthRepositoryImpl`. No reset link was added to `LoginScreen` alongside the password field.

**Before (current — method missing):**
```kotlin
// AuthRepository.kt — interface ends here, no reset method
interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun signInWithEmail(email: String, password: String): Result<String>
    suspend fun createAccountWithEmail(email: String, password: String): Result<String>
    suspend fun isTokenValid(): Boolean
    suspend fun signOut()
    fun currentUser(): Any?
    // sendPasswordResetEmail() — MISSING
}
```

**After (Phase 1b fix):**
```kotlin
// AuthRepository.kt — add method
suspend fun sendPasswordResetEmail(email: String): Result<Unit>

// FirebaseAuthRepositoryImpl.kt — implement
override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching { auth.sendPasswordResetEmail(email).await() }
    }

// LoginViewModel.kt — add state and callbacks
private val _showForgotPasswordDialog = MutableStateFlow(false)
val showForgotPasswordDialog: StateFlow<Boolean> = _showForgotPasswordDialog.asStateFlow()

fun onForgotPasswordTap() {
    _showForgotPasswordDialog.value = true
}

fun onForgotPasswordSubmit(email: String) {
    viewModelScope.launch {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        authRepository.sendPasswordResetEmail(email)
            .onSuccess {
                _forgotPasswordState.value = ForgotPasswordState.Success
                kotlinx.coroutines.delay(2000)
                onForgotPasswordDialogDismiss()
            }
            .onFailure {
                _forgotPasswordState.value = ForgotPasswordState.Error(
                    "Reset email failed — check your internet and try again"
                )
            }
    }
}

// LoginScreen.kt — "Forgot password?" link inside password AnimatedVisibility
TextButton(
    onClick = onForgotPasswordTap,
    modifier = Modifier.align(Alignment.End),
) {
    Text(
        text  = "Forgot your password?",
        style = MaterialTheme.typography.bodySmall,
        color = NeuroPulseTheme.colors.primary,
    )
}

// LoginScreen.kt — ForgotPasswordDialog composable (new)
@Composable
fun ForgotPasswordDialog(
    uiState: ForgotPasswordState,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (uiState) {
                    ForgotPasswordState.Success ->
                        Text("✓ Reset link sent! Check your email.")
                    is ForgotPasswordState.Error ->
                        Text(uiState.message, color = colors.signal)
                    else -> {}
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = uiState !is ForgotPasswordState.Loading,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(email) }, enabled = email.isNotBlank()) {
                Text("Send reset link")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// NeuroPulseNavGraph.kt — show dialog in LOGIN composable
if (showForgotPasswordDialog) {
    ForgotPasswordDialog(
        uiState  = forgotPasswordState,
        onSubmit = viewModel::onForgotPasswordSubmit,
        onDismiss = viewModel::onForgotPasswordDialogDismiss,
    )
}
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-003 — New users bypass persona selection after first sign-in

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/navigation/NeuroPulseNavGraph.kt:188-192`](../app/src/main/kotlin/com/neuropulse/ui/navigation/NeuroPulseNavGraph.kt#L188-L192)

**Symptom:** First-time user completes Google or email sign-in and is routed directly to HomeScreen (Phase 2 stub). PersonaSelectionScreen is never shown. `isOnboardingComplete` remains `false` in DataStore indefinitely.

**Root cause:** `resolvePostLoginDestination()` is hardcoded to return `NavDestinations.HOME`. The `// Phase 1b` comment in the function body acknowledges this is a known gap.

**Before (current — always HOME):**
```kotlin
// NeuroPulseNavGraph.kt:188
private suspend fun resolvePostLoginDestination(viewModel: LoginViewModel): String {
    // Phase 1b: inject UserPreferencesRepository here to check onboarding state.
    // For now, default to HOME — PersonaSelectionScreen is reached via sign-up link.
    return NavDestinations.HOME
}
```

**After (Phase 1b fix — implemented):**
```kotlin
// LoginViewModel.kt — exposes check via existing userPreferencesRepository
suspend fun isOnboardingComplete(): Boolean =
    userPreferencesRepository.isOnboardingComplete()

// NeuroPulseNavGraph.kt — resolvePostLoginDestination delegates to ViewModel
private suspend fun resolvePostLoginDestination(viewModel: LoginViewModel): String =
    if (viewModel.isOnboardingComplete()) NavDestinations.HOME else NavDestinations.PERSONA_SELECT

// NeuroPulseNavGraph.kt — PersonaSelectionViewModel saves selection + marks complete
personaViewModel.selectPersona(persona) {
    navController.navigate(NavDestinations.HOME) {
        popUpTo(NavDestinations.PERSONA_SELECT) { inclusive = true }
    }
}
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-004 — No network awareness — silent 10-second timeout before auth error

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/onboarding/LoginViewModel.kt:83-88`](../app/src/main/kotlin/com/neuropulse/ui/onboarding/LoginViewModel.kt#L83-L88)

**Symptom:** User with no internet connection taps "Continue with Google" or "Sign in". A spinner appears immediately and runs for approximately 10 seconds before Firebase times out and surfaces the generic error: "Sign-in failed — check your connection and try again". No upfront indication that connectivity is required. For an ADHD user, 10 seconds of unexplained spinner followed by a vague error is a high-anxiety, high-abandonment pattern.

**Root cause:** No `ConnectivityManager` or network state check exists anywhere in the auth flow. The Firebase `IOException` from a timeout is caught by `runCatching` and falls through to the generic fallback string in `mapFirebaseError`.

**Before (current — no network check):**
```kotlin
// LoginViewModel.kt:83
fun onGoogleSignIn(idToken: String) {
    viewModelScope.launch {
        _uiState.value = LoginUiState.Loading   // spinner starts immediately, even offline
        authRepository.signInWithGoogle(idToken) // hangs ~10s with no connection
            .onSuccess { uid -> handleAuthSuccess(uid) }
            .onFailure { handleAuthFailure(it, currentEmail(), currentPassword()) }
    }
}
```

**After (Phase 1b fix — implemented):**
```kotlin
// domain/repository/NetworkMonitor.kt (new — pure Kotlin interface, no Android imports)
interface NetworkMonitor {
    suspend fun isOnline(): Boolean
}

// data/network/ConnectivityNetworkMonitor.kt (new — Android implementation)
override suspend fun isOnline(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
}

// LoginViewModel.kt — guard all three auth methods
fun onGoogleSignIn(idToken: String) {
    viewModelScope.launch {
        if (!networkMonitor.isOnline()) { handleOffline(); return@launch }
        _uiState.value = LoginUiState.Loading
        authRepository.signInWithGoogle(idToken)
            .onSuccess { uid -> handleAuthSuccess(uid) }
            .onFailure { handleAuthFailure(it, currentEmail(), currentPassword()) }
    }
}

private fun handleOffline() {
    _uiState.value = LoginUiState.Error(
        userMessage = "No internet connection — connect to Wi-Fi or mobile data to sign in",
        email       = currentEmail(),
        password    = currentPassword(),
    )
}
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-005 — No anonymous/guest mode — account required before seeing any content

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/onboarding/LoginScreen.kt:222-233`](../app/src/main/kotlin/com/neuropulse/ui/onboarding/LoginScreen.kt#L222-L233), [`domain/repository/AuthRepository.kt`](../app/src/main/kotlin/com/neuropulse/domain/repository/AuthRepository.kt)

**Symptom:** First-time user sees the login screen with no path to explore the app without creating an account. This is a hard abandonment point — ADHD users who encounter mandatory registration before experiencing value frequently close the app permanently.

**Root cause:** `signInAnonymously()` not added to `AuthRepository`. No anonymous path in the NavGraph. No guest option on `LoginScreen`.

**Before (current — account required):**
```kotlin
// LoginScreen.kt:222 — only a sign-up link, no guest path
TextButton(onClick = onNavigateToSignUp) {
    Text("First time here? Set up your account")
}
// Nothing below this. User must create an account to proceed.
```

**After (Phase 1b fix):**
```kotlin
// LoginScreen.kt — add below the sign-up link
TextButton(onClick = onContinueAsGuest) {
    Text(
        text  = "Try without signing in",
        style = MaterialTheme.typography.labelMedium,
        color = NeuroPulseTheme.colors.onSurfaceVariant,
    )
}

// AuthRepository.kt — new interface method
suspend fun signInAnonymously(): Result<String>

// FirebaseAuthRepositoryImpl.kt
override suspend fun signInAnonymously(): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            val result = auth.signInAnonymously().await()
            result.user?.uid ?: error("Anonymous sign-in returned null user")
        }
    }
// Anonymous UID is NOT saved to DataStore — session ends on app kill.
// HomeScreen shows a persistent banner: "Your progress won't be saved until you create an account."
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-006 — Biometric permissions declared in manifest but nothing wired

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`AndroidManifest.xml:8-11`](../app/src/main/AndroidManifest.xml#L8-L11), [`domain/repository/UserPreferencesRepository.kt`](../app/src/main/kotlin/com/neuropulse/domain/repository/UserPreferencesRepository.kt)

**Symptom:** `USE_BIOMETRIC` and `USE_FINGERPRINT` are declared in the manifest as forward-declarations. No `BiometricPrompt` is shown at any point. No `biometric_enabled` or `biometric_prompt_shown` DataStore keys exist. No `BIOMETRIC_LOCK` NavGraph destination. No popup on Home first visit. The permissions are dead declarations.

**Root cause:** Biometric was intentionally deferred from Phase 1 — permissions added as forward-declarations only to avoid a second manifest edit later.

**Before (current — permissions only):**
```xml
<!-- AndroidManifest.xml — permissions declared, nothing wired behind them -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```

**After (Phase 1b fix — multiple additions required):**
```kotlin
// 1. UserPreferencesDataStoreImpl.kt — new DataStore keys
val KEY_BIOMETRIC_ENABLED      = booleanPreferencesKey("biometric_enabled")
val KEY_BIOMETRIC_PROMPT_SHOWN = booleanPreferencesKey("biometric_prompt_shown")

// 2. NavDestinations.kt — new route
const val BIOMETRIC_LOCK   = "biometric_lock"
const val PRIVACY_CONSENT  = "privacy_consent"  // also missing

// 3. AuthGateViewModel.resolveStartDestination() — new routing branch
val biometricEnabled = userPreferencesRepository.isBiometricEnabled()
return when {
    !tokenValid      -> NavDestinations.LOGIN
    !onboarded       -> NavDestinations.PERSONA_SELECT
    biometricEnabled -> NavDestinations.BIOMETRIC_LOCK   // new
    else             -> NavDestinations.HOME
}

// 4. BiometricLockScreen — new composable
// Shows BiometricPrompt immediately on composition.
// On success → navigate HOME. On cancel → show "Use a different sign-in method" fallback.

// 5. HomeScreen — one-time BiometricSetupDialog (DD-010)
if (!hasShownBiometricPrompt) {
    BiometricSetupDialog(
        onEnable  = { viewModel.enableBiometric() },
        onDismiss = { viewModel.dismissBiometricPrompt() },
    )
}
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-007 — Logo asset saved with double file extension (fixed)

**Date:** 2026-04 | **Phase:** 1 — Asset setup
**File:** `app/src/main/res/drawable/`

**Symptom:** Logo saved as `ic_neuropulse_logo.png.png`. AAPT2 rejects resource file names containing dots — the build would fail at the resource compilation step.

**Root cause:** The file was already named with a `.png` extension before being copied into the drawable folder. The operating system appended a second `.png` extension on copy.

**Before (broken):**
```
app/src/main/res/drawable/
  ic_neuropulse_logo.png.png   ← dot in resource name, AAPT2 rejects this
```

**After (fixed):**
```
app/src/main/res/drawable/
  ic_neuropulse_logo.png       ← R.drawable.ic_neuropulse_logo resolves correctly
```

Fix applied via: `mv ic_neuropulse_logo.png.png ic_neuropulse_logo.png`

**Status:** Fixed (2026-04)

**Learning:** Always verify the exact filename after dropping assets into `res/drawable/`. AAPT2 error messages for invalid resource names are not always clearly linked to the dot-in-filename cause — the resource compiler may report a generic merge failure instead.

---

## E-008 — PersonaSelectionScreen is a stub — no questionnaire UI implemented

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**File:** [`ui/onboarding/PersonaSelectionScreen.kt:58-65`](../app/src/main/kotlin/com/neuropulse/ui/onboarding/PersonaSelectionScreen.kt#L58-L65)

**Symptom:** User navigated to PersonaSelectionScreen (via "First time here? Set up your account") sees only the text "Persona selection coming in Phase 1b" on a blank surface. No questions, no selection UI, no way to proceed — the user is stranded on a placeholder screen.

**Root cause:** Intentional Phase 1 stub. The questionnaire UI, `PersonaSelectionViewModel`, and DataStore write (`setUserPersona`, `setOnboardingComplete`) were all deferred to Phase 1b. The nav route exists so the skeleton compiles, not so the feature works.

**Before (current — stub only):**
```kotlin
// PersonaSelectionScreen.kt:58
Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
    Text(
        text      = "Persona selection\ncoming in Phase 1b",
        style     = MaterialTheme.typography.bodyLarge,
        color     = NeuroPulseTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
```

**After (Phase 1b fix — two-card scenario questionnaire):**
```kotlin
// PersonaSelectionScreen.kt — replace Box stub with:
// Two mutually exclusive selection cards (satisfies one-CTA-per-screen via card selection, ADR-005)
PersonaCard(
    title       = "I often lose track of time",
    description = "Events sneak up on me. I need earlier reminders and time anchors.",
    selected    = selectedPersona == UserPreferences.PERSONA_MARCUS,
    onClick     = { viewModel.selectPersona(UserPreferences.PERSONA_MARCUS) },
)
PersonaCard(
    title       = "I get pulled into tasks and can't stop",
    description = "Once I start something, hours disappear. I need a pattern interrupt.",
    selected    = selectedPersona == UserPreferences.PERSONA_ZOE,
    onClick     = { viewModel.selectPersona(UserPreferences.PERSONA_ZOE) },
)
// Single confirm CTA — enabled only after a card is selected
Button(
    onClick  = viewModel::confirmPersona, // saves persona + setOnboardingComplete() → PrivacyConsent
    enabled  = selectedPersona != null,
    modifier = Modifier.fillMaxWidth().height(spacing.touchTarget),
) {
    Text("This sounds like me")
}
// PersonaSelectionViewModel required (new file)
```

**Status:** Fixed (Phase 1b, 2026-04)

---

## E-008 — Missing google-id dependency causes build failure

**Date:** 2026-04 | **Phase:** 1 | **File:** gradle/libs.versions.toml, app/build.gradle.kts

**Symptom:** Compilation failure — `GetGoogleIdOption` and `GoogleIdTokenCredential` unresolved in `NeuroPulseNavGraph.kt`.

**Root cause:** `com.google.android.libraries.identity.googleid:googleid` was not declared in the version catalog. The `androidx.credentials:credentials` library was present but does not include the Google ID token classes.

**Fix:** Added `googleId = "1.1.1"` version, `google-id` library entry in `libs.versions.toml`, and `implementation(libs.google.id)` in `app/build.gradle.kts`.

**Learning:** Credential Manager and Google Identity are separate artifacts. Always verify transitive dependencies when adding new import paths.

**Status:** Fixed (code review, 2026-04-06)

---

## E-009 — Duplicate ForgotPasswordDialog with hardcoded colour

**Date:** 2026-04 | **Phase:** 1 | **File:** ui/onboarding/ForgotPasswordDialog.kt, ui/onboarding/LoginScreen.kt

**Symptom:** Two public `ForgotPasswordDialog` composables with identical signatures in the same package — causes compile ambiguity. Standalone version uses hardcoded `Color(0xFFB8860B)` (Goldenrod) instead of `NeuroPulseTheme.colors.signal` (Attention Amber `0xFFFFB224`).

**Root cause:** The dialog was initially implemented in its own file, then re-implemented inline in `LoginScreen.kt` with theme compliance. The original file was not removed.

**Fix:** Deleted standalone `ForgotPasswordDialog.kt`. The `LoginScreen.kt` version correctly uses `colors.signal`.

**Learning:** When moving composables inline, always delete the original file. Theme colour compliance must be verified against `NeuroPulseColors` constants, not approximated.

**Status:** Fixed (code review, 2026-04-06)

---

## E-010 — LoginViewModel imports data-layer concrete class

**Date:** 2026-04 | **Phase:** 1 | **File:** ui/onboarding/LoginViewModel.kt

**Symptom:** `import com.neuropulse.data.auth.FirebaseAuthRepositoryImpl` in the ViewModel layer — breaks ADR-001 dependency direction (ViewModel → UseCase → Repository interface → DAO/Client, never reverse).

**Root cause:** `mapFirebaseError()` was placed as a companion function on `FirebaseAuthRepositoryImpl` to "keep Firebase exception classes out of the ViewModel," but the ViewModel imported the concrete class anyway, defeating the purpose.

**Fix:** Replaced with `exception::class.simpleName` matching in the ViewModel — no Firebase imports needed. Error mapping is presentation logic (exception → user-facing string).

**Learning:** Static utility methods on data-layer classes create hidden coupling. Presentation-layer error mapping belongs in the ViewModel.

**Status:** Fixed (code review, 2026-04-06)

---

## E-011 — BiometricPrompt captures stale Activity in remember

**Date:** 2026-04 | **Phase:** 1 | **File:** ui/home/BiometricLockScreen.kt

**Symptom:** `BiometricPrompt` created inside `remember { }` captures the Activity reference. On configuration change (rotation), the remembered prompt holds the old (destroyed) Activity — crash on next `authenticate()` call.

**Root cause:** `remember` without a key does not recompute when the Activity is recreated.

**Fix:** Keyed `remember(activity)` so `BiometricPrompt` and executor are recreated after config changes. Callbacks use `rememberUpdatedState` to always invoke the latest lambda.

**Learning:** Never capture `Activity` in keyless `remember`. Always use `remember(activity)` for objects that hold Activity references.

**Status:** Fixed (code review, 2026-04-06)

---

## E-012 — Google Sign-In swallows user cancellation as error

**Date:** 2026-04 | **Phase:** 1 | **File:** ui/navigation/NeuroPulseNavGraph.kt

**Symptom:** When user cancels Google Sign-In, `onGoogleSignIn("")` is called with an empty token, causing Firebase to throw `IllegalArgumentException` and show a generic "Sign-in failed" error.

**Root cause:** Both `GetCredentialCancellationException` (user cancelled) and other `GetCredentialException` subtypes were caught in the same handler and treated identically.

**Fix:** Separated `GetCredentialCancellationException` (no-op — user stays on login screen) from real errors (logged via Timber, empty token triggers error state).

**Learning:** Always distinguish user cancellation from genuine errors in credential flows.

**Status:** Fixed (code review, 2026-04-06)

## E-013 — isFormValid() not reactive in Error state — Create Account button permanently disabled

**Date:** 2026-04 | **Phase:** 1b — Auth extension
**File:** [`ui/onboarding/CreateAccountViewModel.kt`](../app/src/main/kotlin/com/neuropulse/ui/onboarding/CreateAccountViewModel.kt), [`ui/navigation/NeuroPulseNavGraph.kt`](../app/src/main/kotlin/com/neuropulse/ui/navigation/NeuroPulseNavGraph.kt)

**Symptom:** After a failed account creation attempt (e.g. network error), the CREATE ACCOUNT button remains disabled even when all form fields are valid and consent is checked. The user must type a character in any field to escape the Error state.

**Root cause:** `isFormValid()` was a plain synchronous function that read `_uiState.value` and checked only the `Idle` subclass. When state was `Error` (which also carries all field values), `currentIdle()` returned null and `isFormValid()` returned `false`. The NavGraph passed this as `isFormValid = viewModel.isFormValid()` — a non-reactive snapshot.

**Fix:** Replaced with a reactive `StateFlow<Boolean>` derived from `_uiState` via `.map {}`. The map handles both `Idle` and `Error` states by extracting field values from either. NavGraph collects it via `collectAsStateWithLifecycle()`.

**Before (broken):**
```kotlin
// CreateAccountViewModel.kt
fun isFormValid(): Boolean {
    val idle = currentIdle() ?: return false  // null in Error state!
    return validateForm(idle) == null
}

// NeuroPulseNavGraph.kt
isFormValid = viewModel.isFormValid(),  // snapshot, not reactive
```

**After (fixed):**
```kotlin
// CreateAccountViewModel.kt
val isFormValid: StateFlow<Boolean> = _uiState
    .map { state ->
        val idle = when (state) {
            is CreateAccountUiState.Idle -> state
            is CreateAccountUiState.Error -> CreateAccountUiState.Idle(
                firstName = state.firstName, lastName = state.lastName,
                email = state.email, password = state.password,
                confirmPassword = state.confirmPassword, consentChecked = state.consentChecked,
            )
            else -> return@map false
        }
        validateForm(idle) == null
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

// NeuroPulseNavGraph.kt
val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()
```

**Learning:** StateFlow-derived properties must handle ALL sealed subclasses that carry data, not just the "happy path" Idle state. When two sealed subclasses share the same field shape (Idle and Error both carry form values), validation logic must account for both. Prefer reactive `StateFlow` over synchronous getters for any value consumed by Compose.

**Status:** Fixed (caught during code review, 2026-04-09)

---

<!-- Example structure:

## E-001 — [Short bug title]

**Date:** YYYY-MM | **Phase:** N | **File:** path/to/file.kt

**Symptom:** What was observed / what failed.

**Root cause:** Why it happened.

**Fix:** What was changed to resolve it.

**Learning:** What this tells us to watch out for in future.

-->
