package com.neuropulse.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PersonaSelectionViewModel — persists the user's selected persona after onboarding.
 *
 * Writes two values to DataStore atomically (sequentially in the same coroutine):
 *  - [UserPreferencesRepository.setUserPersona] — stores "MARCUS" or "ZOE"
 *  - [UserPreferencesRepository.setOnboardingComplete] — gates future routing to HOME
 *
 * [selectPersona] is called from [com.neuropulse.ui.navigation.NeuroPulseNavGraph]
 * immediately before navigating to HOME. The NavGraph only navigates after the
 * ViewModel call completes (suspend in a launch block).
 *
 * Phase 1b: Full UI logic (questionnaire flow) will move here in Task 4.
 * For now this ViewModel is purely a persistence coordinator.
 */
@HiltViewModel
class PersonaSelectionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /**
     * Saves the selected persona and marks onboarding as complete.
     *
     * Both writes must succeed before navigating to HOME — if either fails, the user
     * would be re-routed to PERSONA_SELECT on the next app launch, which is safe
     * (onboarding is idempotent).
     *
     * @param persona One of [com.neuropulse.domain.model.UserPreferences.PERSONA_MARCUS]
     *                or [com.neuropulse.domain.model.UserPreferences.PERSONA_ZOE].
     * @param onSaved Called on the Main dispatcher after both writes complete —
     *                triggers navigation in the NavGraph.
     */
    fun selectPersona(persona: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.setUserPersona(persona)
            userPreferencesRepository.setOnboardingComplete()
            onSaved()
        }
    }
}
