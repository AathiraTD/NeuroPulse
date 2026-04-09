# NeuroPulse — Execution Status Tracker

**Project:** NeuroPulse — A Neuroadaptive Companion for Adults Living with ADHD
**Student:** Aathira T Dev (S21161041) | BCU CMP6213
**Last Updated:** 9 Apr 2026

---

## Phase Status Overview

| Phase | Phase Name | Planned Start | Planned End | Actual Start | Actual End | Status | % Complete | Comments |
|-------|-----------|---------------|-------------|--------------|------------|--------|------------|----------|
| 1 | Requirements Gathering and Design | 6 Apr 2026 | 17 Apr 2026 | 3 Apr 2026 | | In Progress | 55% | Auth & onboarding (Phase 1b) complete; Create Account screen built from Figma 9 Apr |
| 2 | Development | 20 Apr 2026 | 26 Jun 2026 | | | Not Started | 0% | |
| 3 | Testing and Quality Assurance | 29 Jun 2026 | 17 Jul 2026 | | | Not Started | 0% | |
| 4 | Deployment and Launch | 20 Jul 2026 | 31 Jul 2026 | | | Not Started | 0% | |
| 5 | Post-Launch Monitoring and Updates | 3 Aug 2026 | Ongoing | | | Not Started | 0% | |

**Status values:** `Not Started` | `In Progress` | `Blocked` | `Complete` | `Deferred`

---

## Phase 1 — Requirements Gathering and Design

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 1.1 | Finalise acceptance criteria for UC1–UC5 | 6 Apr 2026 | 8 Apr 2026 | | | Not Started | |
| 1.2 | Finalise non-functional requirements (offline-first, GDPR, battery) | 6 Apr 2026 | 8 Apr 2026 | | | Not Started | |
| 1.3 | High-fidelity UI design — Android screens (Figma) | 13 Apr 2026 | 17 Apr 2026 | 9 Apr 2026 | | In Progress | Figma designs accessed via MCP; Create Account screen built from Figma node 2:373 |
| 1.4 | High-fidelity UI design — Wear OS tiles (Figma) | 13 Apr 2026 | 17 Apr 2026 | | | Not Started | |
| 1.5 | Room database schema freeze (6-table schema) | 6 Apr 2026 | 9 Apr 2026 | | | Not Started | |
| 1.6 | API contract definition (Repository + UseCase interfaces) | 6 Apr 2026 | 9 Apr 2026 | 3 Apr 2026 | 5 Apr 2026 | Complete | Auth, UserPreferences, NetworkMonitor interfaces committed (Phase 1b) |
| 1.7 | Permission flow design (Health Connect, SCHEDULE_EXACT_ALARM) | 13 Apr 2026 | 15 Apr 2026 | | | Not Started | |
| 1.8 | Risk register documentation | 14 Apr 2026 | 17 Apr 2026 | | | Not Started | |
| 1.9 | Development environment setup | 6 Apr 2026 | 7 Apr 2026 | 3 Apr 2026 | 3 Apr 2026 | Complete | Phase 0 scaffold commit; CI pipeline green |

### Phase 1 Milestones

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M1.1 | Signed-off acceptance criteria for UC1–UC5 | 10 Apr 2026 | | Not Started |
| M1.2 | Figma prototype complete (Android + Wear OS) | 17 Apr 2026 | | Not Started |
| M1.3 | Room schema frozen and migration v1 drafted | 10 Apr 2026 | | Not Started |
| M1.4 | Repository interface contracts committed to VCS | 10 Apr 2026 | 5 Apr 2026 | Complete |
| M1.5 | Risk register baselined | 17 Apr 2026 | | Not Started |

---

## Phase 2 — Development

### Sprint 1 — Core Infrastructure and Data Layer

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 2.1.1 | Gradle multi-module structure (`:app`, `:wear`, `:domain`, `:data`) | 20 Apr 2026 | 20 Apr 2026 | | | Not Started | |
| 2.1.2 | Hilt DI setup and module bindings | 20 Apr 2026 | 21 Apr 2026 | | | Not Started | |
| 2.1.3 | Room database v1 — six entities, DAOs, migration scaffold | 21 Apr 2026 | 24 Apr 2026 | | | Not Started | |
| 2.1.4 | Repository implementations (DayPlan, Physio, Capture, Briefing) | 24 Apr 2026 | 28 Apr 2026 | | | Not Started | |
| 2.1.5 | DataStore settings implementation | 28 Apr 2026 | 29 Apr 2026 | | | Not Started | |
| 2.1.6 | Unit tests — DAO and repository contract tests | 29 Apr 2026 | 1 May 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M2.1 | All six Room entities persisting; DAO tests green | 1 May 2026 | | Not Started |
| M2.2 | Hilt DI graph wires repositories without compile errors | 1 May 2026 | | Not Started |

---

### Sprint 2 — Physiological Sensing and Calendar Integration

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 2.2.1 | Health Connect — HealthConnectFacade (Facade pattern) | 4 May 2026 | 7 May 2026 | | | Not Started | |
| 2.2.2 | PhysioRepository — streamHeartRate(), isHrvThresholdBreached() | 7 May 2026 | 9 May 2026 | | | Not Started | |
| 2.2.3 | Chain of Responsibility — physio state evaluation pipeline | 9 May 2026 | 12 May 2026 | | | Not Started | |
| 2.2.4 | Calendar sync — CalendarContentResolver → EventEntity | 12 May 2026 | 14 May 2026 | | | Not Started | |
| 2.2.5 | Health Connect permission onboarding flow | 14 May 2026 | 15 May 2026 | | | Not Started | |
| 2.2.6 | Unit tests — physio layer (fake Health Connect client) | 13 May 2026 | 15 May 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M2.3 | HR stream flowing from Health Connect into Room | 15 May 2026 | | Not Started |
| M2.4 | Calendar events syncing into local Room database | 15 May 2026 | | Not Started |

---

### Sprint 3 — Capture Agent and Morning Plan (UC1)

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 2.3.1 | Agent 1 — Capture Agent data ingestion filter | 18 May 2026 | 20 May 2026 | | | Not Started | |
| 2.3.2 | Quick Capture UI (voice, text, photo/OCR) | 19 May 2026 | 22 May 2026 | | | Not Started | |
| 2.3.3 | FTS5 BM25 search — CaptureRepository.findRelevant() | 21 May 2026 | 23 May 2026 | | | Not Started | |
| 2.3.4 | MorningPlanUseCase — UC1 adaptive task reordering | 22 May 2026 | 26 May 2026 | | | Not Started | |
| 2.3.5 | Home screen UI (Compose + StateFlow) | 24 May 2026 | 27 May 2026 | | | Not Started | |
| 2.3.6 | WorkManager — MorningPlanWorker + PhysioSnapshotWorker | 26 May 2026 | 28 May 2026 | | | Not Started | |
| 2.3.7 | Unit tests — MorningPlanUseCase with fake repositories | 28 May 2026 | 29 May 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M2.5 | Home screen renders adaptive task list from Health Connect data | 29 May 2026 | | Not Started |
| M2.6 | Quick Capture saves and FTS5 retrieval returns ranked results | 29 May 2026 | | Not Started |

---

### Sprint 4 — Wear OS Companion App (UC2–UC5)

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 2.4.1 | Wear OS module setup + Wearable Data Layer API channel | 1 Jun 2026 | 3 Jun 2026 | | | Not Started | |
| 2.4.2 | Sensor Bridge — motion + HR forwarding from watch to phone | 2 Jun 2026 | 5 Jun 2026 | | | Not Started | |
| 2.4.3 | Pre-Event Wrist Briefing tile — AlarmManager + MessageClient (UC2) | 4 Jun 2026 | 8 Jun 2026 | | | Not Started | |
| 2.4.4 | Hyperfocus Alert tile — stillness detection + haptic nudge (UC3) | 6 Jun 2026 | 10 Jun 2026 | | | Not Started | |
| 2.4.5 | Haptic micro-intervention patterns (distinct per TriggerType) | 8 Jun 2026 | 10 Jun 2026 | | | Not Started | |
| 2.4.6 | Breathing Exercise tile — HR spike → breathing guide (UC4) | 9 Jun 2026 | 11 Jun 2026 | | | Not Started | |
| 2.4.7 | Task Completion tile — positive affirmation delivery (UC5) | 10 Jun 2026 | 12 Jun 2026 | | | Not Started | |
| 2.4.8 | Wearable sync reliability — disconnect fallback (WorkManager window) | 11 Jun 2026 | 12 Jun 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M2.7 | Pre-Event tile fires on Wear OS device 15 min before calendar event | 12 Jun 2026 | | Not Started |
| M2.8 | Hyperfocus alert fires after extended stillness threshold | 12 Jun 2026 | | Not Started |
| M2.9 | Breathing and Task Completion tiles functional end-to-end | 12 Jun 2026 | | Not Started |

---

### Sprint 5 — AI Context Engine and Briefing Assembly

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 2.5.1 | Agent 2 — ContextEngine (Strategy selector) | 15 Jun 2026 | 17 Jun 2026 | | | Not Started | |
| 2.5.2 | BriefingStrategy implementations (5 trigger types) | 16 Jun 2026 | 19 Jun 2026 | | | Not Started | |
| 2.5.3 | LangChain4j + Gemini Flash integration | 18 Jun 2026 | 22 Jun 2026 | | | Not Started | |
| 2.5.4 | RAG cold-start seeding (DatabaseSeeder) | 19 Jun 2026 | 21 Jun 2026 | | | Not Started | |
| 2.5.5 | Agent 3 — DeliveryAgent routing filter | 22 Jun 2026 | 23 Jun 2026 | | | Not Started | |
| 2.5.6 | Briefing log persistence + penalty scoring | 22 Jun 2026 | 24 Jun 2026 | | | Not Started | |
| 2.5.7 | Briefings screen UI | 23 Jun 2026 | 25 Jun 2026 | | | Not Started | |
| 2.5.8 | Profile screen UI | 24 Jun 2026 | 25 Jun 2026 | | | Not Started | |
| 2.5.9 | End-to-end integration tests (physio → Gemini → Wear OS) | 25 Jun 2026 | 26 Jun 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M2.10 | AI briefing pipeline produces output for all 5 TriggerTypes | 26 Jun 2026 | | Not Started |
| M2.11 | Briefing log persists deliveries with RAG attribution | 26 Jun 2026 | | Not Started |
| M2.12 | Full Android app navigable (Home, Capture, Briefings, Profile) | 26 Jun 2026 | | Not Started |

---

## Phase 3 — Testing and Quality Assurance

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 3.1 | Unit test completion — domain use cases and handlers | 29 Jun 2026 | 1 Jul 2026 | | | Not Started | |
| 3.2 | Integration tests — Room DAO and migration | 29 Jun 2026 | 1 Jul 2026 | | | Not Started | |
| 3.3 | Integration tests — Health Connect facade | 30 Jun 2026 | 2 Jul 2026 | | | Not Started | |
| 3.4 | UI tests — Compose screens (ComposeTestRule) | 30 Jun 2026 | 3 Jul 2026 | | | Not Started | |
| 3.5 | Wear OS tile testing (physical device + emulator) | 6 Jul 2026 | 8 Jul 2026 | | | Not Started | |
| 3.6 | Permission flow and SCHEDULE_EXACT_ALARM degradation testing | 6 Jul 2026 | 8 Jul 2026 | | | Not Started | |
| 3.7 | AI pipeline validation (all 5 trigger prompts + LLM fallback) | 7 Jul 2026 | 9 Jul 2026 | | | Not Started | |
| 3.8 | RAG retrieval accuracy — BM25 + penalty score logic | 8 Jul 2026 | 9 Jul 2026 | | | Not Started | |
| 3.9 | False positive mitigation testing (calendar suppression, HR threshold) | 9 Jul 2026 | 10 Jul 2026 | | | Not Started | |
| 3.10 | GDPR compliance audit (no raw physio in prompt, device-local data) | 13 Jul 2026 | 14 Jul 2026 | | | Not Started | |
| 3.11 | Battery and performance profiling (Wear OS passive monitoring) | 13 Jul 2026 | 15 Jul 2026 | | | Not Started | |
| 3.12 | Accessibility review (WCAG 2.1 AA, TalkBack) | 14 Jul 2026 | 15 Jul 2026 | | | Not Started | |
| 3.13 | Bug triage and critical/high bug resolution | 15 Jul 2026 | 17 Jul 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M3.1 | Domain layer unit tests green; no Android imports in domain | 3 Jul 2026 | | Not Started |
| M3.2 | Room migration and DAO tests verified | 3 Jul 2026 | | Not Started |
| M3.3 | All 5 MVP use cases demonstrable through automated tests | 10 Jul 2026 | | Not Started |
| M3.4 | QA sign-off report — critical/high bugs resolved | 17 Jul 2026 | | Not Started |
| M3.5 | GDPR compliance checklist completed | 14 Jul 2026 | | Not Started |

---

## Phase 4 — Deployment and Launch

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 4.1 | Release build — ProGuard/R8, signed APK/AAB | 20 Jul 2026 | 21 Jul 2026 | | | Not Started | |
| 4.2 | Google Play internal testing track upload | 21 Jul 2026 | 22 Jul 2026 | | | Not Started | |
| 4.3 | Health Connect data type declaration submission | 20 Jul 2026 | 22 Jul 2026 | | | Not Started | |
| 4.4 | Demo environment preparation (DatabaseSeeder, demo script) | 22 Jul 2026 | 24 Jul 2026 | | | Not Started | |
| 4.5 | Submission package compilation (APK, source, docs, video) | 27 Jul 2026 | 30 Jul 2026 | | | Not Started | |
| 4.6 | Pilot user onboarding + feedback questionnaire | 27 Jul 2026 | 31 Jul 2026 | | | Not Started | |
| 4.7 | Stakeholder demonstration (supervisors / examiners) | 30 Jul 2026 | 31 Jul 2026 | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M4.1 | Signed release APKs (Android + Wear OS) built | 22 Jul 2026 | | Not Started |
| M4.2 | Internal testing track live with ≥3 pilot users | 24 Jul 2026 | | Not Started |
| M4.3 | Health Connect declaration submitted | 22 Jul 2026 | | Not Started |
| M4.4 | Demo video covering all 5 MVP use cases recorded | 27 Jul 2026 | | Not Started |
| M4.5 | Final project submission package complete | 30 Jul 2026 | | Not Started |

---

## Phase 5 — Post-Launch Monitoring and Updates

| Task ID | Task | Planned Start | Planned End | Actual Start | Actual End | Status | Comments |
|---------|------|---------------|-------------|--------------|------------|--------|----------|
| 5.1 | Pilot feedback analysis | 3 Aug 2026 | 14 Aug 2026 | | | Not Started | |
| 5.2 | Briefing quality review (acknowledgement rates by TriggerType) | 3 Aug 2026 | 14 Aug 2026 | | | Not Started | |
| 5.3 | RAG retrieval refinement (FTS5 weighting + penalty scores) | 10 Aug 2026 | 21 Aug 2026 | | | Not Started | |
| 5.4 | Permission drop-off analysis (onboarding funnel review) | 10 Aug 2026 | 21 Aug 2026 | | | Not Started | |
| 5.5 | Wear OS battery audit (real-world pilot data) | 17 Aug 2026 | 28 Aug 2026 | | | Not Started | |
| 5.6 | v2 scoping — Gemini Nano on-device (Android AICore) | 1 Sep 2026 | 30 Sep 2026 | | | Not Started | |
| 5.7 | v2 scoping — AllMiniLM embedding upgrade | 1 Sep 2026 | 30 Sep 2026 | | | Not Started | |
| 5.8 | v2 scoping — ASD/anxiety persona expansion | 1 Sep 2026 | 30 Sep 2026 | | | Not Started | |
| 5.9 | NHS Digital pathway exploration | 1 Oct 2026 | Ongoing | | | Not Started | |
| 5.10 | Dissertation evaluation chapter (briefing_log data analysis) | 1 Aug 2026 | Ongoing | | | Not Started | |

| Milestone | Description | Target Date | Achieved Date | Status |
|-----------|-------------|-------------|---------------|--------|
| M5.1 | Pilot feedback report + prioritised v2 backlog | 14 Aug 2026 | | Not Started |
| M5.2 | Threshold tuning report (hyperfocus, HR spike, RAG MIN_THRESHOLD) | 21 Aug 2026 | | Not Started |
| M5.3 | v2 feature specification document | 30 Sep 2026 | | Not Started |
| M5.4 | Dissertation evaluation chapter supported by usage data | Ongoing | | Not Started |

---

## Issue and Blocker Log

| Date | Phase | Issue / Blocker | Severity | Owner | Resolution | Resolved Date |
|------|-------|----------------|----------|-------|------------|---------------|
| 6 Apr 2026 | 1 | Missing google-id dependency — build fails on Google Sign-In | Critical | Claude | Added googleid:1.1.1 to version catalog + build.gradle | 6 Apr 2026 |
| 6 Apr 2026 | 1 | Duplicate ForgotPasswordDialog with hardcoded colour | Critical | Claude | Deleted standalone file; LoginScreen.kt version uses theme tokens | 6 Apr 2026 |
| 6 Apr 2026 | 1 | LoginViewModel imports data-layer concrete class | Critical | Claude | Replaced with simpleName-based error mapping (ADR-001 compliance) | 6 Apr 2026 |
| 6 Apr 2026 | 1 | Raw email logged in FirebaseAuthRepositoryImpl | High | Claude | Stripped PII from Timber log message | 6 Apr 2026 |
| 6 Apr 2026 | 1 | BiometricPrompt captures stale Activity reference | High | Claude | Keyed on activity + rememberUpdatedState for callbacks | 6 Apr 2026 |

**Severity values:** `Critical` | `High` | `Medium` | `Low`

---

## Change Log

| Date | Changed By | Change Description | Impact |
|------|-----------|-------------------|--------|
| 3 Apr 2026 | Aathira T Dev | Initial tracker created | Baseline established |
| 5 Apr 2026 | Aathira T Dev | Phase 1b auth & onboarding committed | Auth, onboarding, nav graph, biometric lock, persona selection |
| 6 Apr 2026 | Claude (code review) | Code review fixes applied (12 issues) | Dependency fix, architecture violation, privacy, UX compliance, decomposition |
| 7 Apr 2026 | Claude | Setup guide rewritten for Android Studio Panda 3 | README.md, local.properties.template, google-services plugin, projectplan.md updated |
| 9 Apr 2026 | Claude | Create Account screen built from Figma design | CreateAccountScreen.kt, CreateAccountViewModel.kt, CreateAccountUiState.kt, NavDestinations.kt, NeuroPulseNavGraph.kt, LoginScreen.kt updated |
| 9 Apr 2026 | Claude | Documentation updated for Create Account feature | extension-points.md, design-decisions.md (DD-016, DD-017), errors.md (E-013), execution_status_tracker.md, decision_log.md (D-016) |
