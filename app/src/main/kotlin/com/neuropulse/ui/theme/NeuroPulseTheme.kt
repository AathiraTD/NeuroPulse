package com.neuropulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * LocalReduceMotion — reflects the system accessibility reduce-motion setting.
 *
 * All animation specs in [NeuroPulseMotion] read this value.
 * CLAUDE: Always pass LocalReduceMotion.current to NeuroPulseMotion functions.
 * Never animate directly without checking this value.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * NeuroPulseTheme — the root theme composable for the entire NeuroPulse app.
 *
 * CLAUDE: Every screen and every preview must be wrapped in this composable.
 * Never use MaterialTheme directly — all ADHD constraints are applied here.
 *
 * What this does:
 * 1. Selects light or dark [NeuroPulseColors] based on system setting.
 * 2. Maps NeuroPulseColors to Material3 ColorScheme (error = amber, not red).
 * 3. Applies [NeuroPulseTypography] (Atkinson Hyperlegible) globally.
 * 4. Provides [NeuroPulseSpacing] via CompositionLocal.
 * 5. Reads the system reduce-motion setting and provides it via [LocalReduceMotion].
 *
 * Usage:
 * ```kotlin
 * NeuroPulseTheme {
 *     MorningPlanScreen()
 * }
 * ```
 *
 * Preview usage:
 * ```kotlin
 * @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
 * @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
 * @Composable
 * fun MorningPlanPreview() {
 *     NeuroPulseTheme { MorningPlanScreen(previewData()) }
 * }
 * ```
 *
 * @param darkTheme     Override system dark mode — defaults to system setting.
 *                      Set to false in Compose Previews for consistent screenshots.
 * @param reduceMotion  Override system reduce-motion — defaults to system setting.
 *                      Set to true in tests to skip animation delays.
 * @param content       The screen or component to theme.
 */
@Composable
fun NeuroPulseTheme(
    darkTheme: Boolean     = isSystemInDarkTheme(),
    reduceMotion: Boolean  = isReduceMotionEnabled(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) NeuroPulseColorsDark else NeuroPulseColorsLight

    CompositionLocalProvider(
        LocalNeuroPulseColors  provides colors,
        LocalNeuroPulseSpacing provides NeuroPulseSpacing(),
        LocalReduceMotion      provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography  = NeuroPulseTypography,
            content     = content,
        )
    }
}

/**
 * NeuroPulseTheme — static accessors for theme tokens within Composables.
 *
 * Usage:
 * ```kotlin
 * val colors      = NeuroPulseTheme.colors
 * val spacing     = NeuroPulseTheme.spacing
 * val reduceMotion = NeuroPulseTheme.reduceMotion
 * ```
 */
object NeuroPulseTheme {
    /** Current [NeuroPulseColors] — light or dark based on system setting. */
    val colors: NeuroPulseColors
        @Composable @ReadOnlyComposable get() = LocalNeuroPulseColors.current

    /** Current [NeuroPulseSpacing] — all dimension tokens. */
    val spacing: NeuroPulseSpacing
        @Composable @ReadOnlyComposable get() = LocalNeuroPulseSpacing.current

    /**
     * Whether the user has enabled reduce-motion in system accessibility settings.
     * Always pass this to [NeuroPulseMotion] functions — never animate without checking.
     */
    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReduceMotion.current
}

/**
 * Reads the system-level reduce-motion accessibility preference.
 *
 * Detects the ANIMATOR_DURATION_SCALE system setting.
 * When the user sets "Remove animations" in Android Accessibility settings,
 * this returns true and all NeuroPulseMotion specs collapse to 0ms.
 *
 * @return true if reduce-motion is enabled, false otherwise.
 */
@Composable
private fun isReduceMotionEnabled(): Boolean {
    val animatorDurationScale = android.provider.Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return animatorDurationScale == 0f
}
