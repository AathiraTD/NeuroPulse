package com.neuropulse.ui.brand

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neuropulse.ui.theme.NeuroPulseTheme

/**
 * NeuroPulseLogoHeader — logo and wordmark in the standard top-left header position.
 *
 * Used on [com.neuropulse.ui.onboarding.LoginScreen] and all subsequent screens
 * that need a consistent brand presence. The logo is decorative within this Row —
 * the Row itself carries a single merged content description for accessibility.
 *
 * The [logoSize] parameter allows the splash transition to animate logo scale
 * externally while reusing this composable for the static post-splash state.
 *
 * @param modifier  Applied to the root [Row].
 * @param logoSize  Size of the logo image. Defaults to 52dp (post-splash position).
 */
@Composable
fun NeuroPulseLogoHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 52.dp,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            // Merge logo + wordmark into a single accessibility node (ADR-005)
            contentDescription = NeuroPulseBrand.APP_NAME
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter            = painterResource(NeuroPulseBrand.logoRes),
            contentDescription = null, // parent Row carries the description
            modifier           = Modifier.size(logoSize),
        )
        Spacer(Modifier.width(NeuroPulseTheme.spacing.elementBuffer))
        Text(
            text  = NeuroPulseBrand.APP_NAME,
            style = MaterialTheme.typography.headlineMedium,
            color = NeuroPulseTheme.colors.onSurface,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "LogoHeader — Light")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "LogoHeader — Dark")
@Composable
private fun NeuroPulseLogoHeaderPreview() {
    NeuroPulseTheme {
        NeuroPulseLogoHeader()
    }
}
