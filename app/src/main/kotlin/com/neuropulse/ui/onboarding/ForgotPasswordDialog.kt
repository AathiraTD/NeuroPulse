package com.neuropulse.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * ForgotPasswordDialog — lets the user request a password reset email.
 *
 * States:
 * - Idle/Error: Shows text field + "Send Reset Email" button
 * - Loading: Shows spinner while email is being sent
 * - Success: Shows confirmation message "Check your email", then auto-closes
 *
 * ADHD-friendly:
 * - No jargon ("reset link" vs "reset email")
 * - Amber error color (never red)
 * - Short, direct labels ("Send Reset Email" not "Send Authentication E-Mail Reset Link")
 */
@Composable
fun ForgotPasswordDialog(
    uiState: ForgotPasswordState,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reset your password")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (uiState) {
                    is ForgotPasswordState.Idle, is ForgotPasswordState.Error -> {
                        OutlinedTextField(
                            value              = email,
                            onValueChange      = { email = it },
                            label              = { Text("Email address") },
                            modifier           = Modifier.fillMaxWidth(),
                            enabled            = uiState !is ForgotPasswordState.Loading,
                            singleLine         = true,
                        )
                        if (uiState is ForgotPasswordState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text  = uiState.message,
                                color = MaterialTheme.colorScheme.warning,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    is ForgotPasswordState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sending reset email…")
                        }
                    }

                    is ForgotPasswordState.Success -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text  = "✓ Check your email",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = "We've sent a reset link to your inbox. Click it to choose a new password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (uiState) {
                        is ForgotPasswordState.Idle, is ForgotPasswordState.Error -> {
                            onSubmit(email)
                        }

                        is ForgotPasswordState.Loading -> {} // no-op
                        is ForgotPasswordState.Success -> onDismiss()
                    }
                },
                enabled = uiState !is ForgotPasswordState.Loading,
            ) {
                Text(
                    when (uiState) {
                        is ForgotPasswordState.Idle, is ForgotPasswordState.Error -> "Send Reset Email"
                        is ForgotPasswordState.Loading -> "Sending…"
                        is ForgotPasswordState.Success -> "Done"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = uiState !is ForgotPasswordState.Loading,
            ) {
                Text("Cancel")
            }
        },
    )
}

// Helper color extension for amber warning
private val MaterialTheme.colorScheme.warning
    get() = androidx.compose.material3.Color(0xFFB8860B) // Goldenrod (amber)
