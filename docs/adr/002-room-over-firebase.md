# ADR-002: Room (SQLite) over Firebase Firestore

**Status:** Accepted  
**Date:** 2026-03  
**Full trade-off analysis:** `docs/decision_log.md` D-001, D-013

---

## Decision

All persistent data is stored in a local Room (SQLite) database on the device.
Firebase is used only for Auth (Phase 1) and lightweight user preferences
(Firestore profile doc) — never for health or behavioural data.

---

## Context

Health data (HR, HRV, sleep) falls under **GDPR Article 9** — special category
("data concerning health"). The legal basis for processing requires the data
to be device-sovereign. Storing raw physiological snapshots in Firebase
Firestore would require explicit consent under Art. 9(2)(a) and a lawful
transfer mechanism that is impractical for a student PoC.

Beyond privacy, Firebase's eventual consistency model is architecturally
incompatible with real-time trigger firing: a trigger that depends on
the latest HR reading must read from a source of truth that is always
current, not eventually consistent.

---

## What lives where

| Data | Store | Rationale |
|---|---|---|
| PhysioSnapshot | Room | Health data — device-sovereign (GDPR Art. 9) |
| DayPlan / Tasks | Room | Offline-first — must work without internet |
| BriefingLog | Room | Audit log — health-adjacent, keep on device |
| Captures | Room + FTS5 | RAG retrieval requires full-text search |
| UserProfile (prefs) | Room + DataStore | Non-health, but offline-first requirement |
| UserProfile (sync) | Firestore | Persona + preferences only — no raw biometrics |
| Auth tokens | Firebase Auth | Standard identity, no health data |

---

## Rules for developers

- **Never** store `PhysioContext`, `PhysioSnapshotEntity`, or any HR/HRV value in Firestore
- `BriefingStrategy.buildPrompt()` must produce anonymised summaries only — raw values
  must not appear in any outbound API call (Gemini or Firebase)
- All Room entities require a `@Entity` annotation and must be registered in `NeuroPulseDatabase`
- Schema migrations must be added to `NeuroPulseDatabase.migrations` before bumping `version`
- FTS5 virtual table for captures enables BM25 keyword retrieval — `CaptureDao.findRelevant()`
  is the only approved retrieval path for RAG context injection
