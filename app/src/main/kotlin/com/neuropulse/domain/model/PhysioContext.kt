package com.neuropulse.domain.model

/**
 * PhysioContext — a snapshot of the user's current physiological state.
 *
 * This is the data carrier that flows through the Three-Agent Pipeline
 * from HealthConnectFacade → PhysioRepository → MorningPlanUseCase → ContextEngine.
 *
 * Design constraints (ADR-002, ADR-003):
 * - Raw biometric values are held here in memory only during pipeline execution.
 * - These values must NEVER be included in any Gemini API prompt.
 *   BriefingStrategy.buildPrompt() uses anonymised summaries derived from these values.
 * - PhysioContext is passed to reorderByCapacity() and BriefingStrategy — neither
 *   may serialise or log raw values.
 *
 * CLAUDE: If you see a function passing PhysioContext to any HTTP call or log statement,
 * that is a GDPR Art.9 violation — flag it immediately and remove the raw values.
 *
 * @param sleepScore        Composite sleep quality score, 0–100.
 *                          Derived from Health Connect SleepSessionRecord duration and stages.
 *                          Used to determine cognitive capacity tier for task reordering.
 * @param hrv               Heart rate variability in milliseconds (RMSSD).
 *                          Higher = better autonomic recovery. Used in stress assessment.
 * @param currentHr         Current heart rate in beats per minute.
 *                          Used to detect HR spikes for UC4 (Stress Grounding trigger).
 * @param stressIndicator   Derived stress level, 0.0 (low) to 1.0 (high).
 *                          Computed from hrv + currentHr by HealthConnectFacade.
 *                          Used by ContextEngine to select briefing intensity level.
 */
data class PhysioContext(
    val sleepScore: Int,
    val hrv: Int,
    val currentHr: Int,
    val stressIndicator: Float,
) {
    companion object {
        /**
         * A neutral PhysioContext used when Health Connect data is unavailable
         * (e.g. first launch, permissions not yet granted, emulator without seeded data).
         *
         * Mid-range values ensure the morning plan and triggers behave sensibly
         * without real data — no extreme reordering, no false stress alerts.
         */
        val NEUTRAL = PhysioContext(
            sleepScore       = 70,
            hrv              = 55,
            currentHr        = 72,
            stressIndicator  = 0.3f,
        )
    }
}
