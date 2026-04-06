package com.neuropulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.neuropulse.ui.navigation.AuthGateViewModel
import com.neuropulse.ui.navigation.NeuroPulseNavGraph
import com.neuropulse.ui.theme.NeuroPulseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — the single Activity for the entire NeuroPulse app.
 *
 * Hosts the Compose NavGraph. The [AuthGateViewModel] resolves the correct start
 * destination asynchronously (DataStore read + optional Firebase token check) before
 * the NavGraph is composed — preventing any wrong-screen flash on app start.
 *
 * While [AuthGateViewModel.startDestination] is null (check in-flight), a blank
 * [NeuroPulseTheme] surface is shown. The check is fast enough (<50ms for DataStore
 * reads) that this blank frame is imperceptible. On first launch the 2400ms splash
 * animation conceals the auth check entirely.
 *
 * Phase 1: NavGraph with Splash + Login + PersonaSelect + Home stub.
 * Phase 2+: NavGraph expanded with MorningPlanScreen, BriefingScreen, CaptureScreen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeuroPulseTheme {
                val authGateViewModel: AuthGateViewModel = hiltViewModel()
                val startDestination by authGateViewModel.startDestination
                    .collectAsStateWithLifecycle()

                // Compose NavGraph once start destination is resolved.
                // The null guard prevents flickering to the wrong screen while the check runs.
                startDestination?.let { destination ->
                    NeuroPulseNavGraph(startDestination = destination)
                }
            }
        }
    }
}
