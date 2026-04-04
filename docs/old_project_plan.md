# NeuroPulse — Implementation Plan

> Android + Wear OS neuroadaptive companion for adults with ADHD  
> BCU CMP6213 | Student: Aathira T Dev S21161041  
> Architecture: Clean Architecture + MVVM + Three-Agent Pipeline

---

## Hybrid Development Approach

| Phase | Primary Tool | Android Studio needed? |
|---|---|---|
| 0 — Scaffold & secrets | VS Code + SDK CLI | No — terminal only |
| 1 — Auth & onboarding | VS Code | No |
| 2 — Room schema & calendar | VS Code | No |
| 3 — Health Connect & physio | VS Code → install Android Studio here | Yes — Health Connect APK on emulator |
| 4 — AI / RAG / Gemini | VS Code | No — unit testable, Gemini via host internet |
| 5 — Triggers & Wear OS | Android Studio preferred | Recommended — dual emulator management |
| 6 — Polish & E2E | Android Studio | Required — network inspector, privacy audit |

---

## Tech Stack Overview

| Layer | Technology |
|---|---|
| Language | Kotlin 2.x |
| UI | Jetpack Compose (phone + Wear OS) |
| DI | Hilt (Dagger Hilt) |
| Local DB | Room (SQLite) + FTS5 |
| Async | Kotlin Flow + Coroutines |
| Auth | Firebase Auth (Google Sign-In + email) |
| Cloud prefs | Firestore |
| Health data | Health Connect SDK + PassiveMonitoringClient |
| AI / LLM | LangChain4j + Gemini Flash API |
| Background | WorkManager + AlarmManager |
| Wearable comms | NotificationManager (Wear OS channel) — MessageClient as production upgrade path |
| Secrets | `.env` / `local.properties` + GitHub Secrets |
| CI | GitHub Actions |
| Testing | JUnit 5, MockK, Espresso, Hilt testing |

---

## Phase 0 — Environment Setup & Project Scaffold

> Windows dev machine · VS Code + Android SDK CLI · emulators · secrets · CI skeleton  
> Android Studio deferred — install before Phase 3

### VS Code Extensions to Install

- Kotlin (JetBrains)
- Extension Pack for Java (Microsoft)
- Gradle for Java (Microsoft)
- Android iOS Emulator (DiemasMichiels)
- ADB Interface (yungtravla)
- REST Client — for testing Gemini API calls directly in `.http` files
- GitLens
- Error Lens

### Android SDK — Command-Line Tools Only

```bash
# Download command-line tools only (no Android Studio) from:
# https://developer.android.com/studio#command-line-tools-only

# Set Windows environment variables (System → Advanced → Environment Variables)
ANDROID_HOME = C:\Users\<you>\AppData\Local\Android\Sdk
Path += %ANDROID_HOME%\cmdline-tools\latest\bin
Path += %ANDROID_HOME%\platform-tools
Path += %ANDROID_HOME%\emulator

# Install required SDK packages
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "system-images;android-34;google_apis;x86_64"
sdkmanager "system-images;android-34;android-wear;x86_64"

# Create phone AVD
avdmanager create avd -n NeuroPulse_AVD -k "system-images;android-34;google_apis;x86_64"

# Create Wear OS AVD
avdmanager create avd -n NeuroPulse_Wear -k "system-images;android-34;android-wear;x86_64" --device "wearos_small_round"

# Launch emulators (run each in a separate terminal)
emulator -avd NeuroPulse_AVD
emulator -avd NeuroPulse_Wear
```

### ADB Wear OS Pairing (No Bluetooth, No Android Studio UI)

```bash
# Forward ADB port from phone emulator to Wear OS emulator
adb -s emulator-5554 forward tcp:5601 tcp:5601
adb -s emulator-5556 connect localhost:5601
```

### Common Build Commands from VS Code Terminal

```bash
./gradlew build                     # compile check
./gradlew assembleDebug             # build APK
./gradlew installDebug              # install on running emulator
./gradlew test                      # unit tests
./gradlew connectedAndroidTest      # instrumented tests on emulator
adb logcat -s NeuroPulse:V         # filtered logcat
```

### Tasks

- Install VS Code with all extensions listed above
- Install Android SDK command-line tools; configure `ANDROID_HOME` and `PATH`
- Install SDK packages: `platforms;android-34`, `build-tools;34.0.0`, phone + Wear OS system images
- Create phone AVD (`NeuroPulse_AVD`) and Wear OS AVD (`NeuroPulse_Wear`) via `avdmanager`
- Create Android project with `minSdk 28`, Kotlin DSL Gradle, version catalog — open in VS Code
- Set up multi-module Clean Architecture structure: `:app`, `:wear`, `:domain`, `:data`
- Establish base Hilt setup across all modules
- Configure `.env` file + `local.properties` injection for `GEMINI_API_KEY`, `FIREBASE_*`; add `.gitignore` entries
- Add GitHub Actions workflow: build + lint on push; GitHub Secrets for all keys
- Document all emulator + ADB commands in `README.md`
- Configure `local.properties` so secrets never appear in source

### Test Coverage

- Verify `./gradlew build` passes from VS Code terminal
- Confirm phone emulator launches via `emulator -avd NeuroPulse_AVD`
- Confirm Wear OS emulator launches standalone via `emulator -avd NeuroPulse_Wear`
- Confirm `GEMINI_API_KEY` resolves from `BuildConfig` — log masked value only
- GitHub Actions green on empty project

### Tech

`VS Code` `Android SDK CLI` `sdkmanager` `avdmanager` `ADB` `Kotlin 2.x` `Gradle version catalog` `Hilt` `GitHub Actions`

---

## Phase 1 — Auth, Onboarding & Persona Assignment

> Firebase Auth · onboarding questionnaire · UserProfile → Room

### Tasks

- Add Firebase Auth (Google Sign-In + email) — `FirebaseAuth` wrapper in `:data`
- Create `UserProfile` entity with `dominantTrigger: TriggerType` (enum: `TIME_BLINDNESS`, `HYPERFOCUS`, `COMBINED`)
- Build 3-screen onboarding Compose flow: welcome → questionnaire (3 questions) → result screen
- Scoring logic: answers map weights → assign `TriggerType`; persist to Room + Firestore preferences
- Gate main nav behind auth — redirect to onboarding if `UserProfile` absent
- Feature flag helper: `UserProfile.dominantTrigger` controls which UC features are visible
- Implement `DataStore` for lightweight prefs (theme, notification preference)
- Skip-auth mode via BuildConfig flag for emulator testing

### Questionnaire Design

| # | Question | Maps to |
|---|---|---|
| 1 | Do you often lose track of time during tasks? | TIME_BLINDNESS weight |
| 2 | Do you get so absorbed in work you miss meetings? | HYPERFOCUS weight |
| 3 | How often does this affect your daily life? | Severity baseline |

Score determines: `TIME_BLINDNESS` / `HYPERFOCUS` / `COMBINED`

### Demo Checkpoint

- Sign in with Google → questionnaire → assigned Marcus or Zoe profile shown on screen
- Re-open app → lands on main screen (auth persisted)
- COMBINED profile shows both feature sets in nav

### Test Coverage

- Unit: questionnaire scoring logic for all 3 outcome branches
- Unit: `UserProfile` Room DAO insert + read
- Integration: onboarding flow completes, `TriggerType` correctly written
- E2E (emulator): full sign-in → questionnaire → main screen journey

### Tech

`Firebase Auth` `Firestore` `Room` `Jetpack Compose` `DataStore` `Hilt`

---

## Phase 2 — Room Schema, Task Planning & Calendar Sync

> Full ER schema · task CRUD · CalendarProvider · adaptive ordering stub

### Tasks

- Implement full Room schema: `events`, `physio_snapshots`, `captures`, `day_plans`, `briefing_log`, `briefing_captures`
- All DAOs with `Flow<T>` returns; FTS5 on `captures` table
- Task planning Compose UI: create / edit / complete tasks with priority weighting
- Wire `MorningPlanViewModel` → `MorningPlanUseCase` → `DayPlanRepository` (exact class names from class diagram)
- Read device calendar via `CalendarContract` ContentResolver → persist to `events` table
- `DayPlanRepository.getUpcomingEvents(windowHours)` implemented
- Stub `reorderByCapacity()` — returns tasks sorted by priority only (no physio yet)
- `DatabaseSeeder` — inserts 7 days of sample events + tasks for emulator demo

> **Note on CalendarProvider:** No API key, no cost, no external endpoints. It is a built-in Android content provider. Add test events in the emulator's Calendar app; `CalendarContract` reads them identically to a real device.

### Demo Checkpoint

- Morning plan screen shows today's tasks ordered by priority
- Create / complete tasks persisted across app restarts
- Calendar events from emulator visible in upcoming events list

### Test Coverage

- Unit: all DAO operations (insert, update, query, flow emission)
- Unit: `MorningPlanUseCase.getTodayPlan()` with fake repository
- Unit: CalendarProvider query parser with mock `ContentResolver`
- Integration: Room migration from v1 schema baseline
- E2E: task creation → completion → relaunch → state preserved

### Tech

`Room + FTS5` `CalendarContract` `Kotlin Flow` `Compose` `WorkManager`

---

## Phase 3 — Health Connect Integration & Physiological Engine

> PassiveMonitoringClient · synthetic seeder · PhysioRepository · adaptive reordering  
> **Install Android Studio here** — Health Connect APK install on emulator requires it

### Tasks

- Add Health Connect SDK; implement permission request flow with calm explanatory UI
- `PhysioRepository`: reads sleep score, HRV (ms), HR, step count via `HealthConnectClient`
- `PassiveMonitoringClient` setup for background HR + motion listening
- `HealthConnectSeeder`: writes 7 days realistic synthetic sleep/HRV/HR records into Health Connect on emulator
- `PhysioSnapshotEntity` persisted to Room on each read cycle
- Chain of Responsibility: `SleepQualityHandler → HRStateHandler → ActivityStateHandler → StressIndicatorHandler` → produces `PhysioContext`
- Wire `reorderByCapacity()` — uses `PhysioContext.sleepScore` + `hrv` to reorder tasks
- `HealthConnectFacade` wraps all permission checks + error handling

> **Health Connect on emulator:** Install Health Connect APK on the emulator and use `HealthConnectSeeder` to write synthetic records. `PhysioRepository` reads them through the exact same API as a real wearable. The integration code is identical.

### Demo Checkpoint

- Morning plan shows sleep score + HRV pills (e.g. Sleep 62 · HRV 28ms · Stress Elevated)
- Tasks visibly reordered based on seeded low-sleep day vs high-sleep day
- Seeder can be toggled: good day vs bad day → plan changes accordingly

### Test Coverage

- Unit: each handler in the Chain of Responsibility in isolation
- Unit: `reorderByCapacity()` with mocked `PhysioContext` — high/low sleep scenarios
- Unit: `HealthConnectFacade` graceful null-HRV fallback
- Integration: seeder writes → repository reads → snapshot stored in Room
- E2E: launch app with seeded low-HRV day → verify task order on screen

### Tech

`Health Connect SDK` `PassiveMonitoringClient` `WorkManager` `Kotlin Flow` `Hilt`

---

## Phase 4 — AI Briefing Engine (RAG + Gemini)

> ContextEngine · LangChain4j · BriefingStrategy · FTS5 retrieval · real Gemini API

### Tasks

- Add LangChain4j (latest Android-stable); bind `ChatLanguageModel` → Gemini Flash via Hilt
- Implement `ContextEngine.assembleBriefing(trigger, physioContext)` (exact signature from class diagram)
- Four `BriefingStrategy` implementations: `MorningPlanStrategy`, `PreEventStrategy`, `HyperfocusStrategy`, `StressGroundingStrategy`
- `CaptureRepository.findRelevant(query, topK)` using Room FTS5 BM25 ranking
- `BriefingRepository.runInference(prompt)` → Gemini Flash API; anonymised prompt only — raw HR/HRV never sent
- Deterministic fallback briefing when offline (no LLM call)
- `BriefingLogEntity` written after each inference; `getPenaltyScores()` implemented
- Quick Capture Compose screen: voice note / typed note / photo → `CaptureEntity` → feeds RAG
- `MIN_THRESHOLD` constant; `DatabaseSeeder` pre-populates captures for demo

### RAG Architecture

```
Capture history (Room FTS5)
        ↓
CaptureRepository.findRelevant()   ← BM25 keyword ranking
        ↓
PhysioContext + FTS5 results
        ↓
BriefingStrategy.buildPrompt()     ← anonymised, no raw PII
        ↓
BriefingRepository.runInference()  ← Gemini Flash API call
        ↓
BriefingPayload (typed Kotlin data class)
```

### Demo Checkpoint

- Morning plan triggers real Gemini briefing enriched with seeded captures
- Pre-event briefing generated 15 min before a seeded calendar event
- Offline mode: deterministic fallback briefing shown without LLM
- Capture added → appears in next briefing context

### Test Coverage

- Unit: each `BriefingStrategy.buildPrompt()` — assert no raw PII in output
- Unit: `CaptureRepository.findRelevant()` FTS5 ranking with seed data
- Unit: `getPenaltyScores()` — dismissed briefing reduces capture score
- Integration: full pipeline trigger → FTS5 retrieval → Gemini call → `BriefingPayload`
- Integration: offline fallback returns deterministic briefing
- E2E: morning launch → AI briefing renders on screen within 5s

### Tech

`LangChain4j` `Gemini Flash API` `Room FTS5` `WorkManager` `Hilt`

---

## Phase 5 — Trigger Engine & Wear OS Notification Dispatch

> AlarmManager · hyperfocus detection · NotificationManager (Wear OS channel) · Wear OS tiles  
> All 5 UCs visible on Wear OS emulator — no Bluetooth pairing or physical watch required

### Delivery Architecture

```
BriefingPayload
      ↓
DeliveryAgent
      ↓
NotificationManager (Wear OS notification channel)
      ↓  via ADB — no Bluetooth pairing needed
Wear OS emulator — notification card visible on watch face

Production upgrade path (physical watch):
Swap NotificationManager → MessageClient + haptic patterns
One DeliveryStrategy binding change, zero architectural impact
```

### Tasks

- `AlarmManager` exact alarm for UC2 pre-event briefing (15 min before calendar event)
- `SCHEDULE_EXACT_ALARM` permission detection + WorkManager 15-min fallback if denied
- Hyperfocus detection worker: extended stillness + low HR variance → fires UC3
- HR spike detection: `PhysioRepository.isHrSpikeDetected()` → triggers UC4 breathing prompt
- Drink water reminder: inactivity threshold worker (persona-aware — both UC types)
- Create Wear OS notification channel on app startup (`IMPORTANCE_HIGH`; haptic flag noted for production)
- `DeliveryAgent`: routes `BriefingPayload` → `NotificationManager` with Wear OS channel
- UC1 — extended notification card on watch (morning adaptive plan summary)
- UC2 — heads-up notification on watch: event title + time remaining
- UC3 — notification with hyperfocus commitment nudge text on watch
- UC4 — notification that opens breathing exercise tile on watch
- UC5 — positive affirmation notification card on watch
- `:wear` module: `WearNotificationReceiver`, breathing exercise tile, task completion tile — styled for round watch face
- Calendar suppression: hyperfocus alert suppressed during active calendar event
- Document `MessageClient` as production upgrade path in `README.md`

### Wear OS Demo Strategy (No Physical Watch)

| Feature | Verification method |
|---|---|
| UC1–UC5 notification cards | Visible on Wear OS emulator via ADB — no pairing needed |
| Tile UI rendering | Wear OS emulator standalone |
| Haptic delivery | Physical device only — documented as production upgrade |
| Sensor bridge | Mocked via `HealthConnectSeeder` on phone side |

### Demo Checkpoint

- Both phone emulator and Wear OS emulator running simultaneously
- Seeded event 15 min away → `AlarmManager` fires → briefing appears on phone + watch notification
- Simulated HR spike → UC4 breathing tile notification visible on Wear OS emulator
- All 5 UC notifications confirmed visible on watch face in emulator
- Hyperfocus alert suppressed when calendar event active (verified in logs)

### Test Coverage

- Unit: hyperfocus detection logic — stillness threshold conditions
- Unit: `DeliveryAgent` with mocked `NotificationManager` — assert channel ID + payload for each UC
- Unit: Wear OS notification channel created with correct importance + name
- Unit: calendar suppression logic
- Unit: `AlarmManager` scheduling with mocked system clock
- Integration: trigger fires → `ContextEngine` → `DeliveryAgent` → notification dispatched
- E2E: full UC2 flow — seeded event → alarm → briefing on phone + watch emulator

### Tech

`AlarmManager` `WorkManager` `NotificationManager` `Wear OS notification channel` `Wear OS Tiles` `Compose for Wear`

---

## Phase 6 — Polish, GDPR Compliance & Final E2E

> Privacy audit · task completion reinforcement · full regression · demo prep

### Tasks

- UC5 task completion reinforcement: positive micro-affirmation dispatched to watch on task complete
- GDPR Art.9 audit: confirm raw HR/HRV/sleep never leaves device; Gemini prompt contains anonymised summaries only
- Data access audit log: Admin UC15 — `briefing_log` query screen for clinician/demo review
- Onboarding permission explanation screens for Health Connect (per Denyer et al. 2023 guidance)
- Error states: no Health Connect data, no internet, no calendar permission — all handled gracefully with user-facing messages
- UI polish: physiological context pills, one-CTA-per-screen rule enforced, decision paralysis review
- Profile screen: show current `TriggerType`, allow re-taking questionnaire
- Final `README.md`: emulator setup, seeder instructions, demo script

### Demo Checkpoint

- Full Marcus journey: low-sleep morning → reordered plan → pre-event briefing → task complete → wrist affirmation on Wear OS emulator
- Full Zoe journey: hyperfocus detected → nudge dispatched → breathing tile on Wear OS emulator
- Offline mode: all core features work without internet (AI briefing shows fallback)
- No raw biometric data visible in any Gemini API call (confirmed via network inspector)

### Test Coverage

- Full regression: all prior unit + integration tests pass
- E2E: complete Marcus persona journey (UC1 → UC2 → UC5)
- E2E: complete Zoe persona journey (UC3 → UC4 → UC5)
- E2E: COMBINED persona — both feature sets active, no UI conflicts
- Privacy test: intercept Gemini API call — assert absence of raw HR/HRV values in prompt
- Offline E2E: disable emulator network → core features verified functional

### Tech

`Network Inspector` `Espresso` `JUnit 5` `MockK` `Hilt testing`

---

## Key Architecture Notes

- **Class names are fixed** — `ContextEngine`, `MorningPlanUseCase`, `DayPlanRepository`, `BriefingRepository`, `CaptureRepository`, `TriggerType`, `PhysioContext`, `BriefingPayload` all match the class diagram exactly
- **One-way dependency rule** — Domain layer holds zero Android imports; fully JUnit-testable without emulator
- **Data sovereignty** — all physiological data stays on-device (Room); only anonymised summaries sent to Gemini
- **Offline-first** — every core trigger fires from local Room data; LLM enriches but never blocks
- **Persona gating** — `UserProfile.dominantTrigger` is the single source of truth for feature visibility throughout the app
- **Wear OS delivery** — `NotificationManager` with Wear OS channel for PoC; `MessageClient` + haptics is the one-binding production upgrade

---

*NeuroPulse | S21161041 | Birmingham City University 2026*