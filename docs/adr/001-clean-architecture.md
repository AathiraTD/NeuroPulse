# ADR-001: Clean Architecture + MVVM

**Status:** Accepted  
**Date:** 2026-03  
**Full trade-off analysis:** `docs/decision_log.md` D-007, D-014

---

## Decision

NeuroPulse uses Clean Architecture with MVVM presentation layer and a
Three-Agent Pipeline (Pipe & Filter) for the AI briefing flow.

---

## Context

The PoC must demonstrate academic rigour for CMP6213 assessment.
A monolithic service would tightly couple physiological sensing, AI reasoning,
and delivery — making each independently untestable and impossible to swap
(e.g. Gemini Flash → Gemini Nano, or NotificationManager → haptic delivery).

---

## Layer boundaries

```
UI (Composables + ViewModels)
    ↓  calls
Domain (UseCases + Repository interfaces)   ← zero Android imports
    ↓  calls
Data (Room, Health Connect, Gemini, Hilt modules)
```

**One-way dependency rule:** arrows only point downward. A ViewModel may call a
UseCase; a UseCase may call a Repository interface; a Repository implementation
may call a DAO or API client. The reverse is never permitted.

## Three-Agent Pipeline (AI briefing flow)

```
TriggerAgent  →  ContextEngine (AI filter)  →  DeliveryAgent
```

| Agent | Responsibility | Key class |
|---|---|---|
| TriggerAgent | Detects physiological or calendar event | WorkManager / AlarmManager |
| ContextEngine | Selects strategy, retrieves RAG context, calls Gemini | `ContextEngine.kt` |
| DeliveryAgent | Routes BriefingPayload to correct surface | `DeliveryAgent.kt` |

---

## Rules for developers (enforced by CLAUDE.md)

- `:domain` package = zero `import android.*` — enforced by CLAUDE.md
- `ViewModel` → `UseCase` → `Repository interface` → `DAO/Client` — never skip layers
- `ContextEngine` only receives anonymised prompts — never raw `PhysioContext`
- `DeliveryAgent` only reads `BriefingPayload` — never modifies it
- Adding a new trigger type → new `TriggerType` enum value + new `BriefingStrategy` + update `extension-points.md`
