package com.neuropulse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.neuropulse.R

/**
 * Atkinson Hyperlegible — chosen for ADHD reading comfort.
 *
 * Purpose-designed for low vision and reading difficulties.
 * Character disambiguation (b/d, p/q, 1/l/I) is significantly
 * improved over standard sans-serif fonts — directly reduces
 * visual processing effort for ADHD users.
 *
 * Setup required: add the following font files to res/font/
 *   atkinson_hyperlegible_regular.ttf
 *   atkinson_hyperlegible_bold.ttf
 *   atkinson_hyperlegible_italic.ttf
 *   atkinson_hyperlegible_bold_italic.ttf
 * Download from: https://brailleinstitute.org/freefont
 * See res/font/FONTS_REQUIRED.md for full instructions.
 *
 * FontWeight.Light is deliberately excluded — low-weight text
 * at small sizes drops below contrast thresholds for ADHD users.
 */
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold_italic, FontWeight.Bold),
    // Medium mapped to Normal — Atkinson ships Regular and Bold only.
    // Medium weight uses Regular to avoid artificially synthesised strokes.
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Medium),
)

/**
 * NumericalTextStyle — applies tabular figures (tnum) to timer and biometric text.
 *
 * Atkinson Hyperlegible does not natively support the OpenType tnum feature,
 * but FontFeatureSettings("tnum") instructs the text engine to use fixed-width
 * numeral slots where available — preventing the jitter effect on countdown
 * timers and HRV/HR readouts as digits change width between updates.
 *
 * CLAUDE: Apply this style only to:
 *   - Countdown timers (UC2 pre-event time remaining)
 *   - HRV and HR readout displays
 *   - Sleep score pill numbers
 * Do NOT apply globally — tnum has no effect on non-numerical glyphs.
 */
val NumericalTextStyle = TextStyle(
    fontFamily      = AtkinsonHyperlegible,
    fontFeatureSettings = "tnum",
    fontWeight      = FontWeight.Medium,
    fontSize        = 18.sp,
    lineHeight      = 1.5.em,
    letterSpacing   = 0.5.sp,
)

/**
 * NeuroPulseTypography — the complete type scale for NeuroPulse.
 *
 * Scale philosophy:
 * - All sizes in sp — respects user system font scale setting (WCAG 1.4.4)
 * - Minimum body size 18sp — larger than Material default (16sp) for ADHD comfort
 * - Bold used only for "anchors" — primary CTAs and screen titles
 * - Medium weight for body — sufficient contrast without bold weight noise
 * - No Light weight anywhere — see ADR-005
 * - Line height 1.5em on body — WCAG AAA readability recommendation
 * - Letter spacing 0.5sp on body — aids character discrimination for ADHD scanning
 *
 * LineHeightStyle.Alignment.Center prevents clipping on rounded backgrounds.
 */
internal val NeuroPulseTypography = Typography(

    // Screen titles — bold anchor, largest text on screen
    displayLarge = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 1.3.em,
        letterSpacing = (-0.5).sp,
    ),

    // Section headers — bold anchor
    headlineMedium = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 1.3.em,
        letterSpacing = 0.sp,
    ),

    // Sub-section labels — medium, not bold to avoid visual noise
    titleMedium = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Medium,
        fontSize      = 20.sp,
        lineHeight    = 1.4.em,
        letterSpacing = 0.1.sp,
    ),

    // Primary body text — 18sp, 1.5em, 0.5sp per specification
    bodyLarge = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Medium,
        fontSize      = 18.sp,
        lineHeight    = 1.5.em,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim      = LineHeightStyle.Trim.None,
        ),
    ),

    // Secondary body text — slightly smaller, same comfortable spacing
    bodyMedium = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 1.5.em,
        letterSpacing = 0.3.sp,
    ),

    // Pill labels, chips, tab labels — medium weight for legibility at small size
    labelMedium = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 1.3.em,
        letterSpacing = 0.4.sp,
    ),

    // Smallest text — captions, helper text
    // Never go below 12sp — below this ADHD scanning fails completely
    labelSmall = TextStyle(
        fontFamily    = AtkinsonHyperlegible,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 1.4.em,
        letterSpacing = 0.4.sp,
    ),
)
