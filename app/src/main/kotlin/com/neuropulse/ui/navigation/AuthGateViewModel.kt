package com.neuropulse.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuropulse.BuildConfig
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * AuthGateViewModel — resolves the initial navigation destination on app launch.
 *
 * Checks cached Firebase UID, token validity, onboarding completion, and biometric
 * preference to determine which screen the user should see first.
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
