package com.neuropulse.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neuropulse.ui.brand.NeuroPulseBrand
import com.neuropulse.ui.brand.NeuroPulseLogoHeader
import com.neuropulse.ui.theme.LocalReduceMotion
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * LoginScreen — the primary authentication entry point for NeuroPulse.
 *
 * Fully stateless — all state is hoisted to [LoginViewModel]. The composable
 * derives its entire UI from the [uiState] parameter.
 *
 * ADHD design decisions (DD-005, DD-006):
 * - Google Sign-In is the sole primary CTA — eliminates password recall burden.
 * - Password field is hidden behind [AnimatedVisibility] until email has content.
 *   Sequential disclosure reduces initiation paralysis at the first touch.
 * - Error messages use [com.neuropulse.ui.theme.NeuroPulseColors.signal] (amber) — never red.
 * - One primary CTA only — satisfies the one-CTA-per-screen rule (ADR-005).
 *
 * @param uiState            Current [LoginUiState] from [LoginViewModel].
 * @param onGoogleSignIn     Triggers the Google One-Tap flow from the caller.
 * @param onEmailChange      Reports email text changes to the ViewModel.
 * @param onPasswordChange   Reports password text changes to the ViewModel.
 * @param onEmailSignIn      Triggers email/password sign-in.
 * @param onForgotPasswordTap Triggers the forgot password dialog (E-002).
 * @param onNavigateToSignUp Navigates to persona selection / account creation.
 * @param onContinueAsGuest  Starts an anonymous Firebase session (E-005, DD-015).
 * @param onOAuthSignIn      Triggers Yahoo/Microsoft/Apple OAuth flow with the provider ID string (DD-012).
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onGoogleSignIn: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignIn: () -> Unit,
    onForgotPasswordTap: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
) {
    val spacing      = NeuroPulseTheme.spacing
    val reduceMotion = LocalReduceMotion.current
    val isLoading    = uiState is LoginUiState.Loading

    val email    = when (uiState) {
        is LoginUiState.Idle  -> uiState.email
        is LoginUiState.Error -> uiState.email
        else                  -> ""
    }
    val password = when (uiState) {
        is LoginUiState.Idle  -> uiState.password
        is LoginUiState.Error -> uiState.password
        else                  -> ""
    }
    val errorMessage = (uiState as? LoginUiState.Error)?.userMessage

    val focusManager   = LocalFocusManager.current
    val passwordFocus  = remember { FocusRequester() }

    Scaffold(
        containerColor = NeuroPulseTheme.colors.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.globalPadding, vertical = spacing.globalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
        ) {
            // Brand header — logo and wordmark in final top-left position
            NeuroPulseLogoHeader()

            Text(
                text  = NeuroPulseBrand.TAGLINE,
                style = MaterialTheme.typography.bodyMedium,
                color = NeuroPulseTheme.colors.onSurfaceVariant,
            )

            Spacer(Modifier.height(spacing.sectionSpacing))

            Text(
                text  = "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                color = NeuroPulseTheme.colors.onSurface,
            )

            // Primary CTA — Google Sign-In (DD-005)
            GoogleSignInButton(
                onClick    = onGoogleSignIn,
                isLoading  = isLoading,
                modifier   = Modifier.fillMaxWidth(),
            )

            OrDivider()

            // Secondary SSO options — Yahoo, Microsoft, Apple (DD-012)
            // OutlinedButton (not filled) so they don't compete with the primary Google CTA
            SsoButton(label = "Continue with Yahoo",     providerId = "yahoo.com",     isLoading = isLoading, onSignIn = onOAuthSignIn)
            SsoButton(label = "Continue with Microsoft", providerId = "microsoft.com",  isLoading = isLoading, onSignIn = onOAuthSignIn)
            SsoButton(label = "Continue with Apple",     providerId = "apple.com",      isLoading = isLoading, onSignIn = onOAuthSignIn)

            OrDivider()

            // Email field — always visible
            OutlinedTextField(
                value         = email,
                onValueChange = onEmailChange,
                label         = { Text("Email address") },
                singleLine    = true,
                enabled       = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocus.requestFocus() },
                ),
                modifier      = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Email address field" },
            )

            // Password field + sign-in button — revealed only after email has content (DD-006)
            AnimatedVisibility(
                visible = email.isNotBlank(),
                enter   = passwordRevealEnter(reduceMotion),
                exit    = fadeOut(tween(150)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer)) {
                    OutlinedTextField(
                        value                  = password,
                        onValueChange          = onPasswordChange,
                        label                  = { Text("Password") },
                        singleLine             = true,
                        enabled                = !isLoading,
                        visualTransformation   = PasswordVisualTransformation(),
                        keyboardOptions        = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done,
                        ),
                        keyboardActions        = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onEmailSignIn()
                            },
                        ),
                        modifier               = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocus)
                            .semantics { contentDescription = "Password field" },
                    )

                    // "Forgot password?" link — aligned right (E-002)
                    TextButton(
                        onClick = onForgotPasswordTap,
                        modifier = Modifier
                            .align(Alignment.End)
                            .semantics { contentDescription = "Reset password" },
                    ) {
                        Text(
                            text  = "Forgot your password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeuroPulseTheme.colors.primary,
                        )
                    }

                    OutlinedButton(
                        onClick  = onEmailSignIn,
                        enabled  = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.touchTarget)
                            .semantics { contentDescription = "Sign in with email" },
                    ) {
                        Text("Sign in")
                    }
                }
            }

            // Error message — amber, specific, never red (DD-002)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(150)),
            ) {
                Text(
                    text      = errorMessage.orEmpty(),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = NeuroPulseTheme.colors.signal,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.weight(1f))

            // "Can't log in?" link — matches Figma design
            TextButton(
                onClick  = onForgotPasswordTap,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .semantics { contentDescription = "Get help signing in" },
            ) {
                Text(
                    text  = "Can't log in?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeuroPulseTheme.colors.onSurfaceVariant,
                )
            }

            // Create Account button — outlined, prominent (matches Figma node 2:350)
            OutlinedButton(
                onClick  = onNavigateToSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget)
                    .semantics { contentDescription = "Create a new account" },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeuroPulseTheme.colors.onSurface,
                ),
            ) {
                Text("Create Account")
            }

            // Guest path — visually de-emphasised so it doesn't compete (E-005)
            TextButton(
                onClick  = onContinueAsGuest,
                enabled  = !isLoading,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .semantics { contentDescription = "Try NeuroPulse without signing in" },
            ) {
                Text(
                    text  = "Try without signing in",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeuroPulseTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

/**
 * GoogleSignInButton — the primary CTA on the login screen.
 *
 * Shows a [CircularProgressIndicator] in place of the label while [isLoading].
 * Background is [com.neuropulse.ui.theme.NeuroPulseColors.primary] (Dusk Periwinkle).
 * Minimum height matches [com.neuropulse.ui.theme.NeuroPulseSpacing.touchTarget] (WCAG 2.5.5).
 */
@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors  = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing

    Button(
        onClick  = onClick,
        enabled  = !isLoading,
        colors   = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor   = colors.onPrimary,
        ),
        modifier = modifier
            .height(spacing.touchTarget)
            .semantics { contentDescription = "Sign in with Google" },
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color    = colors.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text("Continue with Google")
        }
    }
}

/**
 * OrDivider — two [HorizontalDivider] lines with a centred "or" label.
 *
 * Extracted to keep [LoginScreen] under 30 lines per function (CLAUDE.md).
 * Divider color uses [com.neuropulse.ui.theme.NeuroPulseColors.outline] for subtlety.
 */
@Composable
private fun OrDivider() {
    val colors  = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing

    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact),
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = colors.outline,
        )
        Text(
            text  = "or",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = colors.outline,
        )
    }
}

/**
 * SsoButton — a single secondary sign-in option for Yahoo, Microsoft, or Apple (DD-012).
 *
 * [OutlinedButton] rather than filled [Button] — the secondary visual weight signals
 * "alternative option" without competing with the primary Google CTA (ADR-005).
 * Disabled during any loading state to prevent concurrent auth requests.
 *
 * @param label      Human-readable button label (e.g. "Continue with Yahoo").
 * @param providerId Firebase OAuth provider ID passed to [onSignIn].
 * @param isLoading  Disables the button while any auth operation is in progress.
 * @param onSignIn   Callback that receives [providerId] to start the OAuth flow.
 */
@Composable
private fun SsoButton(
    label: String,
    providerId: String,
    isLoading: Boolean,
    onSignIn: (String) -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing
    OutlinedButton(
        onClick  = { onSignIn(providerId) },
        enabled  = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.touchTarget)
            .semantics { contentDescription = label },
    ) {
        Text(label)
    }
}

/**
 * Returns the enter transition for the progressive password field disclosure.
 *
 * When [reduceMotion] is true, collapses to instant fade (no slide) to respect
 * vestibular sensitivity while still revealing the field (WCAG 2.3.3).
 */
private fun passwordRevealEnter(reduceMotion: Boolean): EnterTransition =
    if (reduceMotion) {
        fadeIn(tween(0))
    } else {
        fadeIn(tween(200)) + slideInVertically(tween(300)) { it / 2 }
    }

/**
 * ForgotPasswordDialog — modal for password reset flow (E-002).
 *
 * Shows a single email input field and sends a reset link when submitted.
 * Auto-closes after 2 seconds on success (as controlled by LoginViewModel).
 * On error, shows a failure message inline and keeps dialog open.
 *
 * @param uiState  Current [ForgotPasswordState] — controls loading/success/error states.
 * @param onSubmit Callback with email address to send the reset link.
 * @param onDismiss Closes the dialog (triggered by cancel or auto-close on success).
 */
@Composable
fun ForgotPasswordDialog(
    uiState: ForgotPasswordState,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing  = NeuroPulseTheme.spacing
    val colors   = NeuroPulseTheme.colors
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer)) {
                Text(
                    text  = "Enter your email address and we'll send you a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )

                when (uiState) {
                    ForgotPasswordState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    ForgotPasswordState.Success -> {
                        Text(
                            text  = "✓ Reset link sent! Check your email.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.primary,
                        )
                    }
                    is ForgotPasswordState.Error -> {
                        Text(
                            text  = uiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.signal,
                        )
                    }
                    ForgotPasswordState.Idle -> {}
                }

                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email address") },
                    singleLine    = true,
                    enabled       = uiState is ForgotPasswordState.Idle || uiState is ForgotPasswordState.Error,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send,
                    ),
                    modifier      = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onSubmit(email) },
                enabled  = email.isNotBlank() && uiState !is ForgotPasswordState.Loading,
            ) {
                Text("Send reset link")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = uiState !is ForgotPasswordState.Loading,
            ) {
                Text("Cancel")
            }
        },
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Idle")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Login — Dark — Idle")
@Composable
private fun LoginScreenIdlePreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState             = LoginUiState.Idle(),
            onGoogleSignIn      = {},
            onEmailChange       = {},
            onPasswordChange    = {},
            onEmailSignIn       = {},
            onForgotPasswordTap = {},
            onNavigateToSignUp  = {},
            onContinueAsGuest   = {},
            onOAuthSignIn       = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Email entered")
@Composable
private fun LoginScreenPasswordRevealedPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState             = LoginUiState.Idle(email = "aathira@example.com"),
            onGoogleSignIn      = {},
            onEmailChange       = {},
            onPasswordChange    = {},
            onEmailSignIn       = {},
            onForgotPasswordTap = {},
            onNavigateToSignUp  = {},
            onContinueAsGuest   = {},
            onOAuthSignIn       = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Error")
@Composable
private fun LoginScreenErrorPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState             = LoginUiState.Error(
                userMessage = "Check your email address or password and try again",
                email       = "aathira@example.com",
                password    = "wrongpass",
            ),
            onGoogleSignIn      = {},
            onEmailChange       = {},
            onPasswordChange    = {},
            onEmailSignIn       = {},
            onForgotPasswordTap = {},
            onNavigateToSignUp  = {},
            onContinueAsGuest   = {},
            onOAuthSignIn       = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState             = LoginUiState.Loading,
            onGoogleSignIn      = {},
            onEmailChange       = {},
            onPasswordChange    = {},
            onEmailSignIn       = {},
            onForgotPasswordTap = {},
            onNavigateToSignUp  = {},
            onContinueAsGuest   = {},
            onOAuthSignIn       = {},
        )
    }
}
