package com.neuropulse.ui.brand

import com.neuropulse.R

/**
 * NeuroPulseBrand — single source of truth for NeuroPulse brand identity constants.
 *
 * CLAUDE: Never hardcode "NeuroPulse", "Your calm companion", or the logo drawable
 * reference directly in a Composable. Always import from this object.
 *
 * To replace the placeholder logo vector with the final PNG:
 * 1. Delete `res/drawable/ic_neuropulse_logo.xml`
 * 2. Copy the final PNG to `res/drawable/ic_neuropulse_logo.png`
 * 3. No other file changes are needed — [logoRes] references the same resource name.
 */
object NeuroPulseBrand {

    /**
     * The NeuroPulse logo drawable resource.
     *
     * Currently a vector placeholder — replace with the final PNG asset.
     * Used by [NeuroPulseLogoHeader] and [com.neuropulse.ui.onboarding.SplashScreen].
     */
    val logoRes: Int = R.drawable.ic_neuropulse_logo

    /** The app's display name. Used wherever the brand name appears as text. */
    const val APP_NAME: String = "NeuroPulse"

    /**
     * The app's tagline. Shown on the login screen below the logo header.
     *
     * "calm" is deliberate — anchors the brand promise for ADHD users who
     * associate productivity tools with stress and overwhelm.
     */
    const val TAGLINE: String = "Your calm companion"
}
