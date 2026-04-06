package com.neuropulse.ui.home

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.neuropulse.ui.brand.NeuroPulseLogoHeader
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * BiometricLockScreen — biometric authentication gate on app relaunch (DD-014).
 *
 * Shown only when the user has opted into biometric lock-on-reopen AND a valid Firebase
 * session exists. [AuthGateViewModel] routes here instead of HOME when `biometric_enabled = true`.
 *
 * Behaviour:
 * - [BiometricPrompt] is launched automatically via [LaunchedEffect] on first composition.
 * - On success → [onAuthenticated] navigates to HOME.
 * - On error (cancel, lockout, hardware failure) → fallback UI with two options:
 *   "Try again" (re-triggers the prompt) and "Sign in differently" → LOGIN.
 *
 * Uses [BIOMETRIC_STRONG] | [DEVICE_CREDENTIAL] as authenticator types — falls back to
 * PIN/pattern if biometric is not enrolled, which is safer than a blank screen.
 *
 * @param onAuthenticated   Navigates to HOME after successful biometric auth.
 * @param onUsePasswordInstead Clears the session and navigates to LOGIN.
 */
@Composable
fun BiometricLockScreen(
    onAuthenticated: () -> Unit,
    onUsePasswordInstead: () -> Unit,
) {
    val context  = LocalContext.current
    val activity = context as FragmentActivity
    val colors   = NeuroPulseTheme.colors
    val spacing  = NeuroPulseTheme.spacing
    val executor = remember(activity) { ContextCompat.getMainExecutor(activity) }

    // Use rememberUpdatedState so the callback always invokes the latest lambda,
    // even if the composable recomposes with a new onAuthenticated reference.
    val currentOnAuthenticated = rememberUpdatedState(onAuthenticated)

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Welcome back")
            .setSubtitle("Use biometric to open NeuroPulse")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    // Keyed on activity so the prompt is recreated after configuration changes,
    // avoiding a stale Activity reference that would crash BiometricPrompt.
    val biometricPrompt = remember(activity) {
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    currentOnAuthenticated.value()
                }
                // Error and failure both leave the user on the fallback UI below.
                // They can retry or sign in differently — no auto-navigation on failure.
            },
        )
    }

    // Launch the prompt immediately when this screen first appears.
    LaunchedEffect(Unit) {
        val manager = BiometricManager.from(context)
        val canAuthenticate = manager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Hardware not available or no credentials enrolled — fall through to password.
            onUsePasswordInstead()
        }
    }

    // Fallback UI — visible behind the system prompt and shown if the prompt is dismissed.
    Scaffold(containerColor = colors.surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.globalPadding, vertical = spacing.globalPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeuroPulseLogoHeader()

            Spacer(Modifier.weight(1f))

            Text(
                text      = "Verify it's you",
                style     = MaterialTheme.typography.headlineMedium,
                color     = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = "Use your fingerprint, face, or device PIN to continue.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { biometricPrompt.authenticate(promptInfo) },
                colors  = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget)
                    .semantics { contentDescription = "Try biometric authentication again" },
            ) {
                Text("Try again")
            }

            TextButton(
                onClick  = onUsePasswordInstead,
                modifier = Modifier.semantics { contentDescription = "Sign in with email or Google instead" },
            ) {
                Text(
                    text  = "Sign in differently",
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Static preview of the biometric fallback UI (no BiometricPrompt system call).
 *
 * Shows the "Verify it's you" screen that appears behind or after the system prompt.
 * Cannot preview the actual BiometricPrompt — it requires a real FragmentActivity.
 */
@Composable
private fun BiometricLockScreenPreviewContent() {
    val colors  = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing
    Scaffold(containerColor = colors.surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.globalPadding, vertical = spacing.globalPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeuroPulseLogoHeader()
            Spacer(Modifier.weight(1f))
            Text(
                text  = "Verify it's you",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text  = "Use your fingerprint, face, or device PIN to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {},
                colors  = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget),
            ) { Text("Try again") }
            TextButton(onClick = {}) {
                Text(text = "Sign in differently", color = colors.onSurfaceVariant)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Biometric Lock — Light")
@Composable
private fun BiometricLockScreenLightPreview() {
    NeuroPulseTheme { BiometricLockScreenPreviewContent() }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Biometric Lock — Dark")
@Composable
private fun BiometricLockScreenDarkPreview() {
    NeuroPulseTheme(darkTheme = true) { BiometricLockScreenPreviewContent() }
}
