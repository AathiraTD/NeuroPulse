package com.neuropulse.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neuropulse.domain.model.UserPreferences
import com.neuropulse.ui.brand.NeuroPulseLogoHeader
import com.neuropulse.ui.theme.LocalReduceMotion
import com.neuropulse.ui.theme.NeuroPulseMotion
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * PersonaSelectionScreen — onboarding questionnaire for ADHD archetype selection.
 *
 * Presents two persona cards — Marcus (time-blindness dominant) and Zoe (hyperfocus
 * dominant) — as a conversation, not a clinical survey. The user taps the card that
 * resonates most, then confirms with a single CTA (DD-013).
 *
 * Design rules applied:
 * - One question per screen — no sub-questions or follow-ups (ADHD UX, KDN §3.2)
 * - No radio buttons or checkboxes — card tap = selection, single CTA = confirm
 * - Selection state is local [remember] — ephemeral, does not need ViewModel storage
 * - Card selection animates via primary-color border grow (reduce-motion gated)
 * - CTA disabled until a card is selected — prevents empty submission
 *
 * @param onPersonaSelected Called with [UserPreferences.PERSONA_MARCUS] or
 *                          [UserPreferences.PERSONA_ZOE] when the user confirms.
 */
@Composable
fun PersonaSelectionScreen(
    onPersonaSelected: (String) -> Unit,
) {
    val spacing      = NeuroPulseTheme.spacing
    val colors       = NeuroPulseTheme.colors
    val reduceMotion = LocalReduceMotion.current

    var selectedPersona by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = colors.surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.globalPadding, vertical = spacing.globalPadding),
        ) {
            // Scrollable content area - weight(1f) ensures it takes up available space
            // and allows the Button below to be pinned at the bottom.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.elementBuffer),
            ) {
                NeuroPulseLogoHeader()

                Spacer(Modifier.height(spacing.sectionSpacing))

                Text(
                    text  = "Which feels more like you?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurface,
                )
                Text(
                    text  = "There's no wrong answer — this shapes how NeuroPulse supports your day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )

                Spacer(Modifier.height(spacing.elementBuffer))

                PersonaCard(
                    name         = "Marcus",
                    tagline      = "Time-blindness dominant",
                    description  = "I lose track of time without noticing. Tasks I meant to take " +
                                   "10 minutes somehow take 2 hours — and I'm never sure where the " +
                                   "time went.",
                    personaKey   = UserPreferences.PERSONA_MARCUS,
                    isSelected   = selectedPersona == UserPreferences.PERSONA_MARCUS,
                    reduceMotion = reduceMotion,
                    onSelect     = { selectedPersona = UserPreferences.PERSONA_MARCUS },
                )

                PersonaCard(
                    name         = "Zoe",
                    tagline      = "Hyperfocus dominant",
                    description  = "I get completely absorbed in one thing and struggle to switch — " +
                                   "especially when it's interesting. Other tasks pile up while I'm " +
                                   "deep in the zone.",
                    personaKey   = UserPreferences.PERSONA_ZOE,
                    isSelected   = selectedPersona == UserPreferences.PERSONA_ZOE,
                    reduceMotion = reduceMotion,
                    onSelect     = { selectedPersona = UserPreferences.PERSONA_ZOE },
                )

                // Add bottom buffer for the scrollable area
                Spacer(Modifier.height(spacing.elementBuffer))
            }

            // Confirm CTA — pinned to bottom, outside of scrollable area.
            // Using Modifier.weight(1f) inside a scrollable Column is illegal in Compose
            // and was likely causing the render crash.
            Button(
                onClick  = { selectedPersona?.let(onPersonaSelected) },
                enabled  = selectedPersona != null,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = colors.primary,
                    contentColor           = colors.onPrimary,
                    disabledContainerColor = colors.outline,
                    disabledContentColor   = colors.onSurfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget)
                    .semantics {
                        contentDescription = when (selectedPersona) {
                            UserPreferences.PERSONA_MARCUS -> "Confirm — I'm a Marcus"
                            UserPreferences.PERSONA_ZOE    -> "Confirm — I'm a Zoe"
                            else                           -> "Select a profile to continue"
                        }
                    },
            ) {
                Text(
                    text = if (selectedPersona != null) "That's me →" else "Select a profile to continue",
                )
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

/**
 * PersonaCard — a tappable card representing one ADHD archetype.
 *
 * When [isSelected] is true:
 * - Border animates to primary at 2dp width
 * - Card background deepens to surfaceVariant
 * - Semantics `selected = true` is set for accessibility
 *
 * When [isSelected] is false:
 * - Border is outline at 1dp
 * - Card background is surface
 *
 * The name row shows the archetype label in a small primary-tinted badge
 * to signal this is a recognisable category, not a clinical diagnosis.
 */
@Composable
private fun PersonaCard(
    name: String,
    tagline: String,
    description: String,
    personaKey: String,
    isSelected: Boolean,
    reduceMotion: Boolean,
    onSelect: () -> Unit,
) {
    val colors  = NeuroPulseTheme.colors
    val spacing = NeuroPulseTheme.spacing


    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.outline,
        animationSpec = NeuroPulseMotion.microInteractionSpec(reduceMotion),
        label = "persona-card-border-$personaKey",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = NeuroPulseMotion.microInteractionSpec(reduceMotion),
        label = "persona-card-border-width-$personaKey",
    )
    val cardBackground by animateColorAsState(
        targetValue = if (isSelected) colors.surfaceVariant else colors.surface,
        animationSpec = NeuroPulseMotion.microInteractionSpec(reduceMotion),
        label = "persona-card-bg-$personaKey",
    )

    Card(
        onClick  = onSelect,
        shape    = RoundedCornerShape(spacing.cornerRadius),
        border   = BorderStroke(borderWidth, borderColor),
        colors   = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$name — $tagline"
                role               = Role.RadioButton
                selected           = isSelected
            },
    ) {
        Column(
            modifier            = Modifier.padding(spacing.globalPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.elementBufferCompact),
            ) {
                Text(
                    text  = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) colors.primary else colors.onSurface,
                )
            }

            Text(
                text  = tagline,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) colors.primary else colors.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text  = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "PersonaSelect — Light — None selected")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "PersonaSelect — Dark — None selected")
@Composable
internal fun PersonaSelectionNoneSelectedPreview() {
    NeuroPulseTheme {
        PersonaSelectionScreen(onPersonaSelected = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "PersonaSelect — Light — Marcus selected")
@Composable
internal fun PersonaSelectionMarcusPreview() {
    NeuroPulseTheme {
        // Previewing selected state via a wrapper — selection state lives in the composable
        PersonaCard(
            name         = "Marcus",
            tagline      = "Time-blindness dominant",
            description  = "I lose track of time without noticing. Tasks I meant to take " +
                           "10 minutes somehow take 2 hours — and I'm never sure where the time went.",
            personaKey   = UserPreferences.PERSONA_MARCUS,
            isSelected   = true,
            reduceMotion = false,
            onSelect     = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "PersonaSelect — Light — Zoe selected")
@Composable
internal fun PersonaSelectionZoePreview() {
    NeuroPulseTheme {
        PersonaCard(
            name         = "Zoe",
            tagline      = "Hyperfocus dominant",
            description  = "I get completely absorbed in one thing and struggle to switch — " +
                           "especially when it's interesting. Other tasks pile up while I'm deep in the zone.",
            personaKey   = UserPreferences.PERSONA_ZOE,
            isSelected   = true,
            reduceMotion = false,
            onSelect     = {},
        )
    }
}
