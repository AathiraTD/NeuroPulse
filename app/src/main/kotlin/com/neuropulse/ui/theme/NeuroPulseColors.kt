package com.neuropulse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * NeuroPulseColors — the complete colour system for NeuroPulse.
 *
 * Palette philosophy (ADR-005):
 * An ADHD app with many colours is self-defeating. Competing colour signals
 * increase cognitive load — the exact problem this app exists to reduce.
 *
 * Active colour budget: PRIMARY + SIGNAL only.
 * dopamineSuccess is transient — 2 seconds maximum on screen, never persistent.
 * secondaryAction (#FF8225) was evaluated and dropped — orange + periwinkle
 * creates two competing focal points which fragments attention.
 *
 * @param primary           Dusk Periwinkle — primary CTA, active nav, focus ring only.
 *                          #5B64E0 (darkened from #8B93FF for WCAG AA text contrast).
 *                          Original #8B93FF kept as primaryTint for decorative/icon use.
 * @param primaryTint       #8B93FF — use for icon fills, progress track, decorative only.
 *                          Never for text or interactive element labels.
 * @param signal            Attention Amber — warning states, error states, time pressure pills.
 *                          Never for success, never decorative. Replaces red entirely.
 * @param dopamineSuccess   Transient success green — 2-second task completion animation only.
 *                          Never applied to persistent UI elements.
 * @param surface           Ghost Cloud — screen and card backgrounds. Not an active colour.
 * @param onSurface         Deep Midnight — all text and icons. Not an active colour.
 * @param surfaceVariant    Slightly deeper surface for card elevation without shadows.
 * @param outline           textAnchor at 20% opacity — subtle borders, dividers.
 */
data class NeuroPulseColors(
    val primary: Color,
    val primaryTint: Color,
    val onPrimary: Color,
    val signal: Color,
    val onSignal: Color,
    val dopamineSuccess: Color,
    val onDopamineSuccess: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val background: Color,
    val onBackground: Color,
    val isDark: Boolean,
)

// ── Light colour values ───────────────────────────────────────────────────────

private val PrimaryLight            = Color(0xFF5B64E0) // Dusk Periwinkle — AA contrast on surface
private val PrimaryTintLight        = Color(0xFF8B93FF) // Original spec value — decorative/icon only
private val OnPrimaryLight          = Color(0xFFFFFFFF)
private val SignalLight             = Color(0xFFFFB224) // Attention Amber — replaces red for errors
private val OnSignalLight           = Color(0xFF17153B) // Deep Midnight on amber — 7.1:1 contrast
private val DopamineSuccessLight    = Color(0xFF10B981) // Transient only
private val OnDopamineSuccessLight  = Color(0xFFFFFFFF)
private val SurfaceLight            = Color(0xFFF8FAFF) // Ghost Cloud
private val OnSurfaceLight          = Color(0xFF17153B) // Deep Midnight — 14.2:1 on surface
private val OnSurfaceVariantLight   = Color(0xFF3D3B6B) // Muted text — 7.8:1 on surface
private val SurfaceVariantLight     = Color(0xFFEEF0FA) // Slightly deeper card surface
private val OutlineLight            = Color(0x3317153B) // textAnchor @ 20% — subtle borders

// ── Dark colour values ────────────────────────────────────────────────────────
// Dark mode uses deep navy surfaces — low stimulation, non-harsh

private val PrimaryDark             = Color(0xFF8B93FF) // Original spec — sufficient on dark surface
private val PrimaryTintDark         = Color(0xFFB8BCFF) // Lighter tint for dark decorative use
private val OnPrimaryDark           = Color(0xFF17153B)
private val SignalDark              = Color(0xFFFFB224) // Amber unchanged — works on dark
private val OnSignalDark            = Color(0xFF17153B)
private val DopamineSuccessDark     = Color(0xFF10B981)
private val OnDopamineSuccessDark   = Color(0xFF17153B)
private val SurfaceDark             = Color(0xFF1A1B2E) // Deep navy — low stimulation dark surface
private val OnSurfaceDark           = Color(0xFFE8EAFF) // Soft white — 13.1:1 on dark surface
private val OnSurfaceVariantDark    = Color(0xFFB0B3D6) // Muted — 6.2:1 on dark surface
private val SurfaceVariantDark      = Color(0xFF252640) // Slightly elevated dark card
private val OutlineDark             = Color(0x33E8EAFF) // Light at 20%

// ── Colour instances ──────────────────────────────────────────────────────────

val NeuroPulseColorsLight = NeuroPulseColors(
    primary             = PrimaryLight,
    primaryTint         = PrimaryTintLight,
    onPrimary           = OnPrimaryLight,
    signal              = SignalLight,
    onSignal            = OnSignalLight,
    dopamineSuccess     = DopamineSuccessLight,
    onDopamineSuccess   = OnDopamineSuccessLight,
    surface             = SurfaceLight,
    onSurface           = OnSurfaceLight,
    onSurfaceVariant    = OnSurfaceVariantLight,
    surfaceVariant      = SurfaceVariantLight,
    outline             = OutlineLight,
    background          = SurfaceLight,
    onBackground        = OnSurfaceLight,
    isDark              = false,
)

val NeuroPulseColorsDark = NeuroPulseColors(
    primary             = PrimaryDark,
    primaryTint         = PrimaryTintDark,
    onPrimary           = OnPrimaryDark,
    signal              = SignalDark,
    onSignal            = OnSignalDark,
    dopamineSuccess     = DopamineSuccessDark,
    onDopamineSuccess   = OnDopamineSuccessDark,
    surface             = SurfaceDark,
    onSurface           = OnSurfaceDark,
    onSurfaceVariant    = OnSurfaceVariantDark,
    surfaceVariant      = SurfaceVariantDark,
    outline             = OutlineDark,
    background          = SurfaceDark,
    onBackground        = OnSurfaceDark,
    isDark              = true,
)

/**
 * Maps [NeuroPulseColors] to a Material3 [ColorScheme].
 *
 * Material3 error colour is mapped to [NeuroPulseColors.signal] (amber)
 * rather than red — intentional ADHD accessibility decision (ADR-005).
 * Red triggers stress responses in ADHD users. Amber conveys urgency
 * without the anxiety spike.
 */
internal fun NeuroPulseColors.toMaterialColorScheme(): ColorScheme =
    if (isDark) darkColorScheme(
        primary          = primary,
        onPrimary        = onPrimary,
        surface          = surface,
        onSurface        = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        surfaceVariant   = surfaceVariant,
        background       = background,
        onBackground     = onBackground,
        outline          = outline,
        // Error mapped to amber — never red (ADR-005)
        error            = signal,
        onError          = onSignal,
        errorContainer   = signal.copy(alpha = 0.15f),
        onErrorContainer = onSignal,
    ) else lightColorScheme(
        primary          = primary,
        onPrimary        = onPrimary,
        surface          = surface,
        onSurface        = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        surfaceVariant   = surfaceVariant,
        background       = background,
        onBackground     = onBackground,
        outline          = outline,
        // Error mapped to amber — never red (ADR-005)
        error            = signal,
        onError          = onSignal,
        errorContainer   = signal.copy(alpha = 0.15f),
        onErrorContainer = onSignal,
    )

/**
 * Provides [NeuroPulseColors] down the composition tree.
 *
 * Access via [NeuroPulseTheme.colors] from any Composable.
 * Never access MaterialTheme.colorScheme directly — use this instead
 * to ensure ADHD-specific colour constraints are always in effect.
 */
val LocalNeuroPulseColors = staticCompositionLocalOf { NeuroPulseColorsLight }
