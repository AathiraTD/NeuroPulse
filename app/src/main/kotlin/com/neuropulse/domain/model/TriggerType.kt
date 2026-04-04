package com.neuropulse.domain.model

/**
 * TriggerType — the typed contract for every agent, strategy, DAO, and worker in NeuroPulse.
 *
 * Each value maps directly to one of the 5 MVP use cases:
 *
 * | Value            | Use Case | Persona        | Trigger source                        |
 * |------------------|----------|----------------|---------------------------------------|
 * | MORNING_PLAN     | UC1      | Both           | Scheduled (WorkManager, 07:00)        |
 * | PRE_EVENT        | UC2      | Marcus         | AlarmManager 15 min before calendar   |
 * | HYPERFOCUS       | UC3      | Zoe            | Stillness + motion via Sensor Bridge  |
 * | STRESS_GROUNDING | UC4      | Both           | HR spike detected by Health Connect   |
 * | TASK_COMPLETE    | UC5      | Both           | User marks task done in MorningPlan   |
 *
 * CLAUDE: Never use raw strings for trigger types anywhere in the codebase.
 * Always use this enum. BriefingStrategy, DeliveryAgent, and all workers
 * dispatch on this value — adding a new trigger type requires a new enum value here
 * plus entries in extension-points.md.
 *
 * Persona assignment is stored in UserProfile.dominantTrigger (Phase 1).
 * MORNING_PLAN and STRESS_GROUNDING and TASK_COMPLETE fire for all personas.
 */
enum class TriggerType {

    /** UC1 — Morning Adaptive Day Plan. Sleep + HRV → task reorder. Both personas. */
    MORNING_PLAN,

    /** UC2 — Pre-Event Wrist Briefing. AlarmManager, 15-min pre-event. Marcus persona. */
    PRE_EVENT,

    /** UC3 — Hyperfocus Alert. Stillness + motion detection via Sensor Bridge. Zoe persona. */
    HYPERFOCUS,

    /** UC4 — Stress Grounding Prompt. HR spike → breathing exercise on watch. Both personas. */
    STRESS_GROUNDING,

    /** UC5 — Task Completion Reinforcement. Positive affirmation to wrist. Both personas. */
    TASK_COMPLETE,
}
