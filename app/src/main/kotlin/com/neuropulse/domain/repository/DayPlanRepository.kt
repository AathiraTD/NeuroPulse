package com.neuropulse.domain.repository

/**
 * DayPlanRepository — contract for all task and calendar data operations.
 *
 * Implementation lives in data/local/ (RoomDayPlanRepository) — the domain layer
 * never imports Room, DAO, or any Android class. See ADR-001 (Clean Architecture).
 *
 * CLAUDE: This interface is the only thing MorningPlanUseCase and CalendarWorker
 * may call for task/event data. Never let a ViewModel or UseCase import a DAO directly.
 *
 * Methods are added in Phase 2 when DayPlan, Task, and CalendarEvent domain
 * models are defined. Stub declared here in Phase 0 to establish the architectural
 * boundary — Hilt module and Room implementation scaffold in Phase 2.
 *
 * Phase 2 methods (to be added):
 *   fun getTodayTasks(): Flow<List<Task>>
 *   suspend fun saveDayPlan(plan: DayPlan)
 *   suspend fun getUpcomingEvents(windowHours: Int): List<CalendarEvent>
 *   suspend fun markTaskComplete(taskId: Long)
 *   suspend fun reorderByCapacity(tasks: List<Task>, physio: PhysioContext): List<Task>
 */
interface DayPlanRepository
