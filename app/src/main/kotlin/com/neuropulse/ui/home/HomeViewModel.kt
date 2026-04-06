package com.neuropulse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeViewModel — manages the biometric setup dialog shown on the first Home visit (DD-010).
 *
 * On first composition of HomeScreen, [checkBiometricPrompt] reads DataStore to decide
 * whether to show the [BiometricSetupDialog]. The dialog is shown exactly once — dismissed
 * via "Turn on" or "Maybe later" — and never reappears after [markBiometricPromptShown].
 *
 * State flow:
 *   App open → HomeScreen → checkBiometricPrompt()
 *     → hasBiometricPromptBeenShown = false → showDialog = true
 *     → user taps "Turn on"     → enableBiometric() → setBiometricEnabled(true) → dismiss
 *     → user taps "Maybe later" → dismissBiometricPrompt() → dismiss
 *   On all subsequent Home visits → hasBiometricPromptBeenShown = true → no dialog
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _showBiometricSetupDialog = MutableStateFlow(false)
    val showBiometricSetupDialog: StateFlow<Boolean> = _showBiometricSetupDialog.asStateFlow()

    /**
     * True when the current Firebase user is anonymous (guest session, E-005).
     * Drives the persistent banner on HomeScreen reminding the user that their
     * progress won't be saved until they create an account.
     */
    val isGuestSession: Boolean
        get() = authRepository.isCurrentUserAnonymous()

    /**
     * Reads DataStore on first Home composition to decide whether to show the biometric dialog.
     * No-op if the prompt has already been shown (idempotent, safe to call on re-composition).
     */
    fun checkBiometricPrompt() {
        viewModelScope.launch {
            if (!userPreferencesRepository.hasBiometricPromptBeenShown()) {
                _showBiometricSetupDialog.value = true
            }
        }
    }

    /**
     * User tapped "Turn on" — enables biometric and dismisses the dialog.
     * Marks the prompt as shown so it never reappears.
     */
    fun enableBiometric() {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(true)
            userPreferencesRepository.markBiometricPromptShown()
            _showBiometricSetupDialog.value = false
        }
    }

    /**
     * User tapped "Maybe later" — dismisses without enabling biometric.
     * Marks the prompt as shown so it never reappears (one-shot, DD-010).
     */
    fun dismissBiometricPrompt() {
        viewModelScope.launch {
            userPreferencesRepository.markBiometricPromptShown()
            _showBiometricSetupDialog.value = false
        }
    }
}
