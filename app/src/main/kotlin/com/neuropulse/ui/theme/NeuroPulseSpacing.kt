package com.neuropulse.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * NeuroPulseSpacing — all dimension constants for NeuroPulse.
 *
 * CLAUDE: Import from here. Never hardcode dp values in Composables.
 *
 * @param touchTarget           48dp — WCAG 2.5.5 minimum interactive target size.
 *                              All Buttons, IconButtons, and tappable rows must meet this.
 * @param globalPadding         24dp — screen edge padding. Applied via Scaffold contentPadding.
 * @param sectionSpacing        32dp — vertical space between major screen sections
 *                              (e.g. physio pills → task list gap). Creates breathing room.
 * @param elementBuffer         16dp — space between related elements within a section.
 *                              Increased from spec's 12dp — 12dp was too tight for ADHD
 *                              visual separation needs.
 * @param elementBufferCompact  12dp — use only for dense list items where 16dp
 *                              would waste too much screen space.
 * @param cornerRadius          16dp — rounded corners reduce visual aggression.
 *                              Applied uniformly to all cards, buttons, and chips.
 * @param cornerRadiusSmall     8dp — for smaller components like pills and badges.
 */
data class NeuroPulseSpacing(
    val touchTarget: Dp           = 48.dp,
    val globalPadding: Dp         = 24.dp,
    val sectionSpacing: Dp        = 32.dp,
    val elementBuffer: Dp         = 16.dp,
    val elementBufferCompact: Dp  = 12.dp,
    val cornerRadius: Dp          = 16.dp,
    val cornerRadiusSmall: Dp     = 8.dp,
)

/**
 * Provides [NeuroPulseSpacing] down the composition tree.
 * Access via [NeuroPulseTheme.spacing] from any Composable.
 */
val LocalNeuroPulseSpacing = staticCompositionLocalOf { NeuroPulseSpacing() }

// ─────────────────────────────────────────────────────────────────────────────
// MOTION TOKENS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * NeuroPulseMotion — animation specifications for NeuroPulse.
 *
 * Motion philosophy (ADR-005):
 * All animations are gated behind LocalReduceMotion. When reduce motion
 * is enabled by the user, all specs collapse to an instant (0ms) tween.
 * This satisfies WCAG 2.3.3 (Animation from Interactions) and is critical
 * for users with vestibular disorders, which co-occur with ADHD.
 *
 * Two durations defined:
 * - DurationShort (200ms): micro-interactions — button press, chip select,
 *   checkbox toggle. Faster than spec's 250ms to reduce distraction on
 *   frequently-triggered interactions.
 * - DurationStandard (300ms): screen transitions, card expansion,
 *   section reveals. Longer transitions feel more considered and calm.
 *
 * PulseAnimation replaced with GentleExpandSpec:
 * The original spec proposed a pulse for hyperfocus interrupts.
 * Pulsing/flashing animations are anxiety-inducing for ADHD users and
 * risk triggering photosensitivity responses. GentleExpandSpec uses a
 * subtle scale (1.0 → 1.03 → 1.0) — noticeable but not alarming.
 *
 * CLAUDE: Always call motionSpec() — never use the raw constants directly.
 * motionSpec() applies the LocalReduceMotion gate automatically.
 */
object NeuroPulseMotion {

    // Raw durations — do not use directly, use the spec functions below
    private const val DurationShortMs    = 200
    private const val DurationStandardMs = 300

    /**
     * Returns the appropriate [AnimationSpec] for micro-interactions,
     * gated by the current [reduceMotion] preference.
     *
     * @param reduceMotion Pass LocalReduceMotion.current from the call site.
     */
    fun microInteractionSpec(reduceMotion: Boolean): AnimationSpec<Float> =
        if (reduceMotion) tween(durationMillis = 0)
        else tween(
            durationMillis = DurationShortMs,
            easing         = FastOutSlowInEasing,
        )

    /**
     * Returns the appropriate [AnimationSpec] for screen-level transitions,
     * gated by the current [reduceMotion] preference.
     */
    fun transitionSpec(reduceMotion: Boolean): AnimationSpec<Float> =
        if (reduceMotion) tween(durationMillis = 0)
        else tween(
            durationMillis = DurationStandardMs,
            easing         = FastOutSlowInEasing,
        )

    /**
     * GentleExpandSpec — used for hyperfocus interrupt notifications.
     *
     * Replaces the originally specified PulseAnimation.
     * Scale 1.0 → 1.03 → 1.0 over 600ms — perceptible but not alarming.
     * The spring finish (after scale) settles naturally without overshoot.
     *
     * CLAUDE: Use this for the hyperfocus alert card entrance animation only.
     * Never use for repeated looping animation — one cycle on entry, then rest.
     */
    fun gentleExpandSpec(reduceMotion: Boolean): AnimationSpec<Float> =
        if (reduceMotion) tween(durationMillis = 0)
        else keyframes {
            durationMillis = 600
            1.0f at 0   using FastOutSlowInEasing
            1.03f at 300 using FastOutSlowInEasing
            1.0f at 600
        }

    /**
     * DopamineSuccessSpec — used for the 2-second task completion reward animation.
     *
     * Spring-based so it feels alive rather than mechanical.
     * Plays once on task completion — the Composable using this must
     * remove the success colour after 2000ms (use LaunchedEffect + delay).
     */
    fun dopamineSuccessSpec(reduceMotion: Boolean): AnimationSpec<Float> =
        if (reduceMotion) tween(durationMillis = 0)
        else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        )
}
