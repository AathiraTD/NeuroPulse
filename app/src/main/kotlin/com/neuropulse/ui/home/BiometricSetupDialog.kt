package com.neuropulse.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * BiometricSetupDialog — one-time popup on the first Home visit offering biometric opt-in (DD-010).
 *
 * Design rules:
 * - Shown exactly once — [HomeViewModel.dismissBiometricPrompt] and [HomeViewModel.enableBiometric]
 *   both call [UserPreferencesRepository.markBiometricPromptShown], ensuring no repeat.
 * - Single primary CTA ("Turn on") — satisfies one-CTA-per-screen rule (ADR-005).
 * - "Maybe later" is a [TextButton], not a secondary [Button] — reduces visual weight so the
 *   user does not feel pressured (ADHD low-pressure design, ADR-005).
 * - Dialog is dismissible via back gesture → treated as "Maybe later" by [onDismiss].
 * - No anxiety-inducing language ("required", "secure", "protect"). Framing is about
 *   convenience ("quick access"), not security theatre.
 *
 * @param onEnable   Called when the user taps "Turn on". [HomeViewModel.enableBiometric].
 * @param onDismiss  Called when the user taps "Maybe later" or back-dismisses.
 *                   [HomeViewModel.dismissBiometricPrompt].
 */
@Composable
fun BiometricSetupDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors  = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = "Quick access next time?",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact)) {
                Text(
                    text  = "Use your fingerprint or face to open NeuroPulse instantly when you come back.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text  = "You can change this anytime in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEnable,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor   = colors.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget)
                    .semantics { contentDescription = "Turn on biometric unlock" },
            ) {
                Text("Turn on")
            }
        },
        dismissButton = {
            TextButton(
                onClick  = onDismiss,
                modifier = Modifier.semantics { contentDescription = "Dismiss biometric setup, maybe later" },
            ) {
                Text(
                    text  = "Maybe later",
                    color = colors.onSurfaceVariant,
                )
            }
        },
        containerColor = colors.surface,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "BiometricSetupDialog — Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "BiometricSetupDialog — Dark")
@Composable
private fun BiometricSetupDialogPreview() {
    NeuroPulseTheme {
        BiometricSetupDialog(onEnable = {}, onDismiss = {})
    }
}
