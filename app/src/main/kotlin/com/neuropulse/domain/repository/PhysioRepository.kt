package com.neuropulse.domain.repository

import com.neuropulse.domain.model.PhysioContext
import kotlinx.coroutines.flow.Flow

/**
 * PhysioRepository — contract for reading physiological data.
 *
 * Abstracts Health Connect so the domain layer never knows whether data
 * comes from a real wearable, the Health Connect emulator APK, or the
 * synthetic HealthConnectSeeder used in demos. See ADR-001.
 *
 * Implementation lives in data/health/ (HealthConnectPhysioRepository) — Phase 3.
 * Stub declared here in Phase 0 to establish the boundary.
 *
 * CLAUDE: Raw HR/HRV values flow through here in memory only.
 * PhysioContext must never be serialised or logged — GDPR Art.9 (ADR-002).
 * Return PhysioContext.NEUTRAL when Health Connect is unavailable
 * rather than throwing — passive sensing must degrade gracefully.
 *
 * @see PhysioContext
 * @see com.neuropulse.data.health.HealthConnectFacade
 */
interface PhysioRepository {

    /**
     * Returns the most recent physiological snapshot as a one-shot read.
     *
     * Returns [PhysioContext.NEUTRAL] if Health Connect is unavailable,
     * permissions are not granted, or no recent data exists.
     * Never throws — callers should not need to handle health data exceptions.
     */
    suspend fun getLatestSnapshot(): PhysioContext

    /**
     * Emits the latest [PhysioContext] whenever new Health Connect data arrives.
     *
     * Used by the background WorkManager worker to reactively detect
     * HR spikes (UC4) without polling. Completes when the worker is cancelled.
     */
    fun observePhysioContext(): Flow<PhysioContext>

    /**
     * Returns true if the Health Connect app is installed and permissions are granted.
     *
     * Used by the morning plan worker to decide whether to use real data
     * or fall back to [PhysioContext.NEUTRAL].
     */
    suspend fun isAvailable(): Boolean
}
