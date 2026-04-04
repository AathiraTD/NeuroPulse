package com.neuropulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.neuropulse.ui.theme.NeuroPulseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Placeholder main activity — replaced with full Compose NavGraph in Phase 1.
 *
 * Hilt @AndroidEntryPoint is established here so DI is wired from first launch.
 * NeuroPulseTheme wrapper ensures the correct Material3 theme is applied
 * even before the NavGraph is implemented.
 *
 * Phase 1: Replace setContent body with NeuroPulseNavGraph().
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeuroPulseTheme {
                // Phase 1: NeuroPulseNavGraph() replaces this comment
            }
        }
    }
}
