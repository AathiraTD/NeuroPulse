package com.neuropulse

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * NeuroPulse application entry point.
 *
 * Responsibilities:
 * - Initialises Hilt for dependency injection across all modules.
 * - Plants Timber debug logging tree in DEBUG builds only.
 *   Release builds produce no log output — health data must never appear in logs (ADR-002).
 *
 * CLAUDE: This is the only place Timber.plant() is called.
 * Never call Timber.plant() anywhere else in the app.
 */
@HiltAndroidApp
class NeuroPulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // Debug tree only — raw health data must never appear in release logs (ADR-002).
            Timber.plant(Timber.DebugTree())
        }
    }
}
