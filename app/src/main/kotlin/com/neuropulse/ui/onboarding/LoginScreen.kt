package com.neuropulse.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

// ── Figma gradient colours (node 2:350 background) ──────────────────────────
private val FigmaGradientStart = Color(0xFFA480F0)
private val FigmaGradientEnd = Color(0xFF2563EB)
private val FigmaContinueBlue = Color(0xFF1E85F7)

/**
 * LoginScreen — primary authentication entry point, matching Figma node 2:350.
 *
 * Visual layout from Figma:
 * 1. Purple-to-blue gradient header with logo + app name
 * 2. White card with Login ID / Password underline fields
 * 3. Remember me checkbox + Forgot password link
 * 4. Blue "Continue" button
 * 5. "Or continue with:" + 4 stacked social pill buttons
 * 6. "Can't log in?" link
 * 7. "Create Account" outlined button
 *
 * Fully stateless — all state is hoisted to [LoginViewModel].
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
    val colors = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing
    val isLoading = uiState is LoginUiState.Loading
    val reduceMotion = LocalReduceMotion.current
    val fields = extractLoginFields(uiState)
    val errorMessage = (uiState as? LoginUiState.Error)?.userMessage

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient background — matches Figma gradient (#a480f0 → #2563eb)
        GradientHeader()

        // Scrollable content layered on top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Logo area on gradient
            LogoOnGradient()

            // White card with form — matches Figma white overlay
            WhiteFormCard(
                fields = fields,
                isLoading = isLoading,
                errorMessage = errorMessage,
                reduceMotion = reduceMotion,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onEmailSignIn = onEmailSignIn,
                onForgotPasswordTap = onForgotPasswordTap,
                onGoogleSignIn = onGoogleSignIn,
                onOAuthSignIn = onOAuthSignIn,
                onNavigateToSignUp = onNavigateToSignUp,
                onContinueAsGuest = onContinueAsGuest,
            )
        }
    }
}

// ── Section composables ─────────────────────────────────────────────────────

/** Purple-to-blue gradient background covering the top portion of the screen. */
@Composable
private fun GradientHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(listOf(FigmaGradientStart, FigmaGradientEnd)),
            ),
    )
}

/** Logo + app name positioned on the gradient area — matches Figma node 2:353. */
@Composable
private fun LogoOnGradient() {
    val spacing = NeuroPulseTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .padding(horizontal = spacing.globalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeuroPulseLogoHeader(logoSize = 72.dp)
    }
}

/** White card containing all form elements — matches Figma LoginPage component. */
@Composable
private fun WhiteFormCard(
    fields: LoginFields,
    isLoading: Boolean,
    errorMessage: String?,
    reduceMotion: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignIn: () -> Unit,
    onForgotPasswordTap: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onContinueAsGuest: () -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing
    val colors = NeuroPulseTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = spacing.cornerRadius, topEnd = spacing.cornerRadius))
            .background(colors.surface)
            .padding(horizontal = spacing.globalPadding)
            .padding(top = spacing.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
    ) {
        LoginFields(fields, isLoading, onEmailChange, onPasswordChange, onEmailSignIn)
        RememberMeAndForgotRow(onForgotPasswordTap, isLoading)
        ErrorMessage(errorMessage, reduceMotion)
        ContinueButton(onEmailSignIn, isLoading, fields)
        OrContinueWithText()
        SocialButtonsColumn(isLoading, onGoogleSignIn, onOAuthSignIn)
        CantLogInLink(onContinueAsGuest)
        CreateAccountButton(onNavigateToSignUp)
        Spacer(Modifier.height(spacing.globalPadding))
    }
}

// ── Form fields ─────────────────────────────────────────────────────────────

/** Login ID and Password underline-style fields — matches Figma underline inputs. */
@Composable
private fun LoginFields(
    fields: LoginFields,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignIn: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }

    // Login ID field — underline style matching Figma
    OutlinedTextField(
        value = fields.email,
        onValueChange = onEmailChange,
        label = { Text("Login ID *") },
        singleLine = true,
        enabled = !isLoading,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { passwordFocus.requestFocus() },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Login ID field" },
    )

    // Password field — underline style matching Figma
    OutlinedTextField(
        value = fields.password,
        onValueChange = onPasswordChange,
        label = { Text("Password *") },
        singleLine = true,
        enabled = !isLoading,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onEmailSignIn()
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(passwordFocus)
            .semantics { contentDescription = "Password field" },
    )
}

/** Row with "Remember me" checkbox and "Forgot password?" link — matches Figma. */
@Composable
private fun RememberMeAndForgotRow(
    onForgotPasswordTap: () -> Unit,
    isLoading: Boolean,
) {
    val colors = NeuroPulseTheme.colors
    var rememberMe by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                enabled = !isLoading,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.onSurfaceVariant,
                ),
            )
            Text(
                text = "Remember me",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface,
            )
        }
        TextButton(
            onClick = onForgotPasswordTap,
            modifier = Modifier.semantics { contentDescription = "Reset password" },
        ) {
            Text(
                text = "Forgot password ?",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
            )
        }
    }
}

/** Error message — amber, gated behind reduce-motion. */
@Composable
private fun ErrorMessage(errorMessage: String?, reduceMotion: Boolean) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = if (reduceMotion) fadeIn(tween(0)) else fadeIn(tween(200)),
        exit = if (reduceMotion) fadeOut(tween(0)) else fadeOut(tween(150)),
    ) {
        Text(
            text = errorMessage.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = NeuroPulseTheme.colors.signal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Blue "Continue" button — matches Figma (#1e85f7, shadow, full width). */
@Composable
private fun ContinueButton(
    onEmailSignIn: () -> Unit,
    isLoading: Boolean,
    fields: LoginFields,
) {
    val spacing = NeuroPulseTheme.spacing

    Button(
        onClick = onEmailSignIn,
        enabled = !isLoading && fields.email.isNotBlank() && fields.password.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
            containerColor = FigmaContinueBlue,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(spacing.cornerRadiusSmall / 2),
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.touchTarget)
            .shadow(4.dp, RoundedCornerShape(spacing.cornerRadiusSmall / 2))
            .semantics { contentDescription = "Continue to sign in" },
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(spacing.globalPadding / 1.2f),
            )
        } else {
            Text("Continue")
        }
    }
}

/** "Or continue with:" text — matches Figma centered divider text. */
@Composable
private fun OrContinueWithText() {
    Text(
        text = "Or continue with:",
        style = MaterialTheme.typography.labelSmall,
        color = NeuroPulseTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Stacked social auth buttons — Google, Github, Microsoft, Apple pill-shaped. */
@Composable
private fun SocialButtonsColumn(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.sectionSpacing),
        verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SocialPillButton("Google", onGoogleSignIn, isLoading)
        SocialPillButton("Github", { onOAuthSignIn("github.com") }, isLoading)
        SocialPillButton("Microsoft", { onOAuthSignIn("microsoft.com") }, isLoading)
        SocialPillButton("Apple", { onOAuthSignIn("apple.com") }, isLoading)
    }
}

/** Single pill-shaped social button — matches Figma rounded-25px border. */
@Composable
private fun SocialPillButton(
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(NeuroPulseTheme.spacing.touchTarget)
            .semantics { contentDescription = "Sign in with $label" },
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

/** "Can't log in?" link — matches Figma centered text. */
@Composable
private fun CantLogInLink(onContinueAsGuest: () -> Unit) {
    TextButton(
        onClick = onContinueAsGuest,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Get help signing in or try as guest" },
    ) {
        Text(
            text = "Can't log in ?",
            style = MaterialTheme.typography.bodySmall,
            color = NeuroPulseTheme.colors.onSurfaceVariant,
        )
    }
}

/** "Create Account" outlined button — matches Figma blue border button at bottom. */
@Composable
private fun CreateAccountButton(onNavigateToSignUp: () -> Unit) {
    val colors = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing

    OutlinedButton(
        onClick = onNavigateToSignUp,
        shape = RoundedCornerShape(spacing.cornerRadiusSmall / 2),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.touchTarget)
            .shadow(4.dp, RoundedCornerShape(spacing.cornerRadiusSmall / 2))
            .semantics { contentDescription = "Create a new account" },
    ) {
        Text("Create Account", style = MaterialTheme.typography.bodyMedium)
    }
}

// ── ForgotPasswordDialog ────────────────────────────────────────────────────

/**
 * ForgotPasswordDialog — modal for password reset flow (E-002).
 *
 * Shows a single email input and sends a reset link when submitted.
 * Auto-closes after 2 seconds on success. On error, shows failure inline.
 */
@Composable
fun ForgotPasswordDialog(
    uiState: ForgotPasswordState,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing
    val colors = NeuroPulseTheme.colors
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer)) {
                Text(
                    text = "Enter your email address and we'll send you a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                when (uiState) {
                    ForgotPasswordState.Loading -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    ForgotPasswordState.Success -> {
                        Text("Reset link sent! Check your email.", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                    }
                    is ForgotPasswordState.Error -> {
                        Text(uiState.message, style = MaterialTheme.typography.bodySmall, color = colors.signal)
                    }
                    ForgotPasswordState.Idle -> {}
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    singleLine = true,
                    enabled = uiState is ForgotPasswordState.Idle || uiState is ForgotPasswordState.Error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(email) }, enabled = email.isNotBlank() && uiState !is ForgotPasswordState.Loading) {
                Text("Send reset link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = uiState !is ForgotPasswordState.Loading) {
                Text("Cancel")
            }
        },
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private data class LoginFields(val email: String, val password: String)

private fun extractLoginFields(state: LoginUiState): LoginFields = when (state) {
    is LoginUiState.Idle -> LoginFields(state.email, state.password)
    is LoginUiState.Error -> LoginFields(state.email, state.password)
    else -> LoginFields("", "")
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Idle")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Login — Dark — Idle")
@Composable
private fun LoginScreenIdlePreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState = LoginUiState.Idle(),
            onGoogleSignIn = {}, onEmailChange = {}, onPasswordChange = {},
            onEmailSignIn = {}, onForgotPasswordTap = {}, onNavigateToSignUp = {},
            onContinueAsGuest = {}, onOAuthSignIn = { _ -> },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Filled")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Login — Dark — Filled")
@Composable
private fun LoginScreenFilledPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState = LoginUiState.Idle(email = "aathira@example.com", password = "mypassword"),
            onGoogleSignIn = {}, onEmailChange = {}, onPasswordChange = {},
            onEmailSignIn = {}, onForgotPasswordTap = {}, onNavigateToSignUp = {},
            onContinueAsGuest = {}, onOAuthSignIn = { _ -> },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Error")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Login — Dark — Error")
@Composable
private fun LoginScreenErrorPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState = LoginUiState.Error(
                userMessage = "Check your email address or password and try again",
                email = "aathira@example.com", password = "wrongpass",
            ),
            onGoogleSignIn = {}, onEmailChange = {}, onPasswordChange = {},
            onEmailSignIn = {}, onForgotPasswordTap = {}, onNavigateToSignUp = {},
            onContinueAsGuest = {}, onOAuthSignIn = { _ -> },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Login — Light — Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    NeuroPulseTheme {
        LoginScreen(
            uiState = LoginUiState.Loading,
            onGoogleSignIn = {}, onEmailChange = {}, onPasswordChange = {},
            onEmailSignIn = {}, onForgotPasswordTap = {}, onNavigateToSignUp = {},
            onContinueAsGuest = {}, onOAuthSignIn = { _ -> },
        )
    }
}
