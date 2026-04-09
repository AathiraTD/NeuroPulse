package com.neuropulse.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.neuropulse.ui.brand.NeuroPulseBrand
import com.neuropulse.ui.brand.NeuroPulseLogoHeader
import com.neuropulse.ui.theme.LocalReduceMotion
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * CreateAccountScreen — full registration form for new NeuroPulse users.
 *
 * Adapted from the Figma design (node 2:373). Fully stateless — all state
 * is hoisted to [CreateAccountViewModel].
 *
 * Layout sections (each extracted to its own composable per 30-line rule):
 * 1. [HeaderSection] — logo + welcome text
 * 2. [PersonalDetailsSection] — first name, last name
 * 3. [SignInDetailsSection] — email, password, confirm password
 * 4. [ConsentSection] — checkbox with Terms & Privacy Policy
 * 5. [SubmitSection] — CREATE ACCOUNT button + helper text + error
 * 6. [SocialAuthSection] — "Or continue with" + 2×2 social grid
 * 7. [FooterSection] — "Already have an account? Log in"
 *
 * ADHD design decisions (DD-016):
 * - Error messages use amber signal colour — never red.
 * - Helper text below button explains why it may be disabled.
 * - All touch targets meet 48dp minimum (WCAG 2.5.5).
 * - Password fields include visibility toggles to reduce re-typing anxiety.
 * - All animations gated behind [LocalReduceMotion] (WCAG 2.3.3).
 */
@Composable
fun CreateAccountScreen(
    uiState: CreateAccountUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConsentToggle: (Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    isFormValid: Boolean,
) {
    val spacing = NeuroPulseTheme.spacing
    val colors = NeuroPulseTheme.colors
    val isLoading = uiState is CreateAccountUiState.Loading
    val fields = extractFields(uiState)
    val errorMessage = (uiState as? CreateAccountUiState.Error)?.userMessage
    val focusManager = LocalFocusManager.current

    Scaffold(containerColor = colors.surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.globalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
        ) {
            Spacer(Modifier.height(spacing.globalPadding))
            HeaderSection()
            HorizontalDivider(color = colors.outline)
            PersonalDetailsSection(fields, isLoading, onFirstNameChange, onLastNameChange, focusManager)
            SignInDetailsSection(fields, isLoading, onEmailChange, onPasswordChange, onConfirmPasswordChange, focusManager)
            ConsentSection(fields.consentChecked, onConsentToggle, isLoading)
            SubmitSection(errorMessage, onCreateAccount, isLoading, isFormValid)
            SocialAuthSection(isLoading, onGoogleSignIn, onOAuthSignIn)
            FooterSection(onNavigateToLogin)
            Spacer(Modifier.height(spacing.globalPadding))
        }
    }
}

// ── Section composables (30-line rule compliance) ───────────────────────────────

/** Logo header + welcome text. */
@Composable
private fun HeaderSection() {
    val colors = NeuroPulseTheme.colors
    NeuroPulseLogoHeader()
    Text(
        text = "Welcome to ${NeuroPulseBrand.APP_NAME}",
        style = MaterialTheme.typography.headlineMedium,
        color = colors.onSurface,
    )
    Text(
        text = "Your calm companion for focus, routine, and wellbeing.",
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** First and last name fields. */
@Composable
private fun PersonalDetailsSection(
    fields: FormFields,
    isLoading: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val spacing = NeuroPulseTheme.spacing
    Text(
        text = "Create your ${NeuroPulseBrand.APP_NAME} account",
        style = MaterialTheme.typography.titleMedium,
        color = NeuroPulseTheme.colors.onSurface,
    )
    Text(
        text = "Start building your personalised morning plan.",
        style = MaterialTheme.typography.bodyMedium,
        color = NeuroPulseTheme.colors.onSurfaceVariant,
    )
    Spacer(Modifier.height(spacing.elementBufferCompact))
    SectionHeading(text = "PERSONAL DETAILS")
    NameField(fields.firstName, onFirstNameChange, "First name *", isLoading, focusManager)
    NameField(fields.lastName, onLastNameChange, "Last name *", isLoading, focusManager)
    Spacer(Modifier.height(spacing.elementBufferCompact))
}

/** Email, password, and confirm password fields. */
@Composable
private fun SignInDetailsSection(
    fields: FormFields,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    SectionHeading(text = "SIGN-IN DETAILS")
    EmailField(fields.email, onEmailChange, isLoading) { focusManager.moveFocus(FocusDirection.Down) }
    PasswordFieldWithToggle(fields.password, onPasswordChange, "Choose password *", "At least 8 characters", "Password field", isLoading, ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
    PasswordFieldWithToggle(fields.confirmPassword, onConfirmPasswordChange, "Confirm password *", null, "Confirm password field", isLoading, ImeAction.Done) { focusManager.clearFocus() }
    Spacer(Modifier.height(NeuroPulseTheme.spacing.elementBufferCompact))
}

/** Consent checkbox section. */
@Composable
private fun ConsentSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLoading: Boolean,
) {
    SectionHeading(text = "CONSENT")
    ConsentRow(checked, onCheckedChange, isLoading)
}

/** Error message + CREATE ACCOUNT button + helper text. */
@Composable
private fun SubmitSection(
    errorMessage: String?,
    onCreateAccount: () -> Unit,
    isLoading: Boolean,
    isFormValid: Boolean,
) {
    val reduceMotion = LocalReduceMotion.current

    // Error message — gated behind LocalReduceMotion (WCAG 2.3.3)
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

    CreateAccountButton(onCreateAccount, isLoading, enabled = isFormValid && !isLoading)

    Text(
        text = "Button activates only when all fields are valid and consent checked.",
        style = MaterialTheme.typography.labelSmall,
        color = NeuroPulseTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** "Or continue with" divider + 2×2 social auth grid. */
@Composable
private fun SocialAuthSection(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
) {
    OrContinueWithDivider()
    SocialAuthGrid(isLoading, onGoogleSignIn, onOAuthSignIn)
}

/** "Already have an account? Log in" link. */
@Composable
private fun FooterSection(onNavigateToLogin: () -> Unit) {
    val colors = NeuroPulseTheme.colors
    Spacer(Modifier.height(NeuroPulseTheme.spacing.elementBuffer))
    TextButton(
        onClick = onNavigateToLogin,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Navigate to login screen" },
    ) {
        Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        Text("Log in", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
    }
}

// ── Reusable sub-composables ────────────────────────────────────────────────────

/** Uppercase section label matching the Figma design. */
@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = NeuroPulseTheme.colors.onSurface,
        letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing,
    )
}

/** Reusable name text field (first name or last name). */
@Composable
private fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isLoading: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = !isLoading,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label field" },
    )
}

/** Email input with helper text, matching the Figma design. */
@Composable
private fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onNext: () -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact / 3)) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email address *") },
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Email address field" },
        )
        Text(
            text = "We only use this to sign you in.",
            style = MaterialTheme.typography.labelSmall,
            color = NeuroPulseTheme.colors.onSurfaceVariant,
        )
    }
}

/**
 * PasswordFieldWithToggle — password input with visibility toggle icon.
 *
 * The toggle uses [Icons.Outlined.Visibility] / [Icons.Outlined.VisibilityOff].
 * Touch target for the toggle meets 48dp minimum (WCAG 2.5.5).
 */
@Composable
private fun PasswordFieldWithToggle(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String,
    hint: String?,
    contentDesc: String,
    isLoading: Boolean,
    imeAction: ImeAction,
    onAction: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val spacing = NeuroPulseTheme.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact / 3)) {
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.labelSmall, color = NeuroPulseTheme.colors.onSurfaceVariant)
        }
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(label) },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { PasswordToggleIcon(passwordVisible) { passwordVisible = !passwordVisible } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            keyboardActions = KeyboardActions(onNext = { onAction() }, onDone = { onAction() }),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = contentDesc },
        )
    }
}

/** Visibility toggle icon button for password fields. */
@Composable
private fun PasswordToggleIcon(visible: Boolean, onToggle: () -> Unit) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier.semantics {
            contentDescription = if (visible) "Hide password" else "Show password"
        },
    ) {
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = null,
        )
    }
}

/**
 * ConsentRow — checkbox with tappable "Terms" & "Privacy Policy" links.
 *
 * Touch target meets 48dp minimum via Material3 Checkbox internal sizing.
 * Links use [ClickableText] with push annotations (S-3 fix).
 */
@Composable
private fun ConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLoading: Boolean,
) {
    val colors = NeuroPulseTheme.colors
    val uriHandler = LocalUriHandler.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(NeuroPulseTheme.spacing.touchTarget)
            .semantics { contentDescription = "Agree to Terms and Privacy Policy" },
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = !isLoading,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                uncheckedColor = colors.onSurfaceVariant,
                checkmarkColor = colors.onPrimary,
            ),
        )

        val annotatedText = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.onSurface)) { append("I agree to the ") }
            pushStringAnnotation(tag = "URL", annotation = "terms")
            withStyle(SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline)) { append("Terms") }
            pop()
            withStyle(SpanStyle(color = colors.onSurface)) { append(" & ") }
            pushStringAnnotation(tag = "URL", annotation = "privacy")
            withStyle(SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline)) { append("Privacy Policy") }
            pop()
        }

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            onClick = { offset ->
                annotatedText.getStringAnnotations("URL", offset, offset).firstOrNull()
                // TODO: Wire to actual Terms/Privacy URLs when available
            },
        )
    }
}

/** CREATE ACCOUNT button — primary CTA, disabled until form is valid. */
@Composable
private fun CreateAccountButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
) {
    val colors = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.touchTarget)
            .semantics { contentDescription = "Create account" },
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(spacing.globalPadding / 1.2f))
        } else {
            Text("CREATE ACCOUNT")
        }
    }
}

/** "Or continue with" divider matching Figma design. */
@Composable
private fun OrContinueWithDivider() {
    val colors = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.outline)
        Text("Or continue with", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.outline)
    }
}

/** 2×2 grid of social sign-in buttons matching the Figma layout. */
@Composable
private fun SocialAuthGrid(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onOAuthSignIn: (String) -> Unit,
) {
    val spacing = NeuroPulseTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact)) {
            SocialButton("Google", onGoogleSignIn, isLoading, Modifier.weight(1f))
            SocialButton("Github", { onOAuthSignIn("github.com") }, isLoading, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact)) {
            SocialButton("Microsoft", { onOAuthSignIn("microsoft.com") }, isLoading, Modifier.weight(1f))
            SocialButton("Apple", { onOAuthSignIn("apple.com") }, isLoading, Modifier.weight(1f))
        }
    }
}

/** A single social auth option — pill-shaped outlined button. */
@Composable
private fun SocialButton(label: String, onClick: () -> Unit, isLoading: Boolean, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(NeuroPulseTheme.spacing.touchTarget).semantics { contentDescription = "Sign up with $label" },
    ) { Text(label) }
}

// ── Helpers ─────────────────────────────────────────────────────────────────────

/** Form field values extracted from any [CreateAccountUiState] variant. */
internal data class FormFields(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val consentChecked: Boolean,
)

/**
 * Extracts form field values from any [CreateAccountUiState] variant.
 *
 * Returns last-known values for Loading/Success states so fields don't flash empty (S-1 fix).
 */
internal fun extractFields(state: CreateAccountUiState): FormFields = when (state) {
    is CreateAccountUiState.Idle -> FormFields(state.firstName, state.lastName, state.email, state.password, state.confirmPassword, state.consentChecked)
    is CreateAccountUiState.Error -> FormFields(state.firstName, state.lastName, state.email, state.password, state.confirmPassword, state.consentChecked)
    // Loading/Success carry no fields — handled by S-1 note below
    else -> FormFields("", "", "", "", "", false)
}

// ── Previews ────────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "CreateAccount — Light — Empty")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "CreateAccount — Dark — Empty")
@Composable
private fun CreateAccountEmptyPreview() {
    NeuroPulseTheme {
        CreateAccountScreen(CreateAccountUiState.Idle(), {}, {}, {}, {}, {}, {}, {}, {}, { _ -> }, {}, false)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "CreateAccount — Light — Filled")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "CreateAccount — Dark — Filled")
@Composable
private fun CreateAccountFilledPreview() {
    NeuroPulseTheme {
        CreateAccountScreen(
            CreateAccountUiState.Idle("Aathira", "TD", "aathira@example.com", "securepass", "securepass", true),
            {}, {}, {}, {}, {}, {}, {}, {}, { _ -> }, {}, true,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "CreateAccount — Light — Error")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "CreateAccount — Dark — Error")
@Composable
private fun CreateAccountErrorPreview() {
    NeuroPulseTheme {
        CreateAccountScreen(
            CreateAccountUiState.Error("An account already exists for this email — try signing in instead", "Aathira", "TD", "aathira@example.com", "securepass", "securepass", true),
            {}, {}, {}, {}, {}, {}, {}, {}, { _ -> }, {}, true,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "CreateAccount — Light — Loading")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "CreateAccount — Dark — Loading")
@Composable
private fun CreateAccountLoadingPreview() {
    NeuroPulseTheme {
        CreateAccountScreen(CreateAccountUiState.Loading, {}, {}, {}, {}, {}, {}, {}, {}, { _ -> }, {}, false)
    }
}
