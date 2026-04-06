# NeuroPulse — Implementation Plan (Final)

> Android + Wear OS neuroadaptive companion for adults with ADHD
> BCU CMP6213 | Student: Aathira T Dev S21161041
> Architecture: Clean Architecture + MVVM + Three-Agent Pipeline

---

## Development Environment

**Recommended:** Android Studio Panda 3 (2025.3.3) with Claude Code JetBrains extension.

Android Studio provides Compose Previews, integrated emulator management, Layout Inspector,
and the debugger — all needed for ADHD UX validation. See `README.md` for full setup instructions.

VS Code can also be used with the CLI emulator (see extensions below for reference),
but lacks Compose Preview support and visual debugging.

| Phase | Key tool requirement |
|---|---|
| 0 — Scaffold & standards | Any editor (VS Code or Android Studio) |
| 1 — Auth & onboarding | Android Studio recommended (Compose previews) |
| 2 — Room schema & calendar | Android Studio recommended (Database Inspector) |
| 3 — Health Connect & physio | Android Studio required (Health Connect emulator APK) |
| 4 — AI / RAG / Gemini | Any editor (Gemini API via host internet) |
| 5 — Triggers & Wear OS | Android Studio required (dual emulator) |
| 6 — Polish, compliance & release | Android Studio required (signed APK, profiler) |
| 7 — Demo preparation | Either (PowerPoint + demo script) |

---

## Tech Stack

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
| Wearable delivery | NotificationManager (Wear OS channel) → MessageClient upgrade path |
| Logging | Timber |
| Secrets | `local.properties` + GitHub Secrets |
| CI | GitHub Actions |
| Testing | JUnit 5, MockK, Espresso, Hilt testing |

---

## VS Code Setup

### Extensions — Development

| Extension | Publisher | Purpose |
|---|---|---|
| Kotlin | JetBrains | Syntax, completion, refactoring |
| Extension Pack for Java | Microsoft | Required Kotlin runtime |
| Gradle for Java | Microsoft | Gradle task runner |
| Android iOS Emulator | DiemasMichiels | Launch AVD from sidebar |
| ADB Interface | yungtravla | ADB commands from VS Code |
| REST Client | Huachao Mao | Test Gemini API via `.http` files |
| GitLens | GitKraken | Git history, blame, branch management |
| Error Lens | Alexander | Inline error display |
| Todo Tree | Gruntfuggly | Track TODOs across phases |

### Extensions — UI Preview (mobile view while building)

| Extension | Publisher | Purpose |
|---|---|---|
| Android for VS Code (beta) | Google | `@Preview` Compose rendering in sidebar |
| Jetpack Compose for VS Code | Google | Compose syntax support + preview |
| XML | Red Hat | Layout XML preview for older views |

> **How to use Compose Preview in VS Code:**
> Add `@Preview` annotations to every Composable. With Android for VS Code
> installed, a "Preview" lens appears above each annotated function.
> Click it to render a static mobile-sized preview in a side panel —
> no emulator launch needed.

```kotlin
@Preview(showBackground = true, showSystemUi = true,
    widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light")
@Preview(showBackground = true, showSystemUi = true,
    widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun MorningPlanScreenPreview() {
    NeuroPulseTheme { MorningPlanScreen(previewData()) }
}
```

### Android SDK Setup (Windows, no Android Studio)

```bash
# 1. Download command-line tools only:
#    https://developer.android.com/studio#command-line-tools-only

# 2. Windows environment variables
ANDROID_HOME = C:\Users\<you>\AppData\Local\Android\Sdk
Path += %ANDROID_HOME%\tools
Path += %ANDROID_HOME%\platform-tools
Path += %ANDROID_HOME%\emulator

# 3. Install SDK packages
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "system-images;android-34;google_apis;x86_64"
sdkmanager "system-images;android-34;android-wear;x86_64"

# 4. Create AVDs
avdmanager create avd -n NeuroPulse_AVD \
  -k "system-images;android-34;google_apis;x86_64"
avdmanager create avd -n NeuroPulse_Wear \
  -k "system-images;android-34;android-wear;x86_64" \
  --device "wearos_small_round"

# 5. Launch
emulator -avd NeuroPulse_AVD
emulator -avd NeuroPulse_Wear

# 6. Pair Wear OS to phone emulator via ADB
adb -s emulator-5554 forward tcp:5601 tcp:5601
adb -s emulator-5556 connect localhost:5601
```

### Common Terminal Commands

```bash
./gradlew assembleDebug          # Build
./gradlew installDebug           # Install on emulator
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
adb logcat -s NeuroPulse:V       # Filtered Logcat
```

---

## Project File Structure

Every file is listed with a one-line description of its purpose.
Files marked `[Phase N]` are created in that phase — not before.

```
NeuroPulse/
│
├── CLAUDE.md
│   └── AI session instruction file — tells Claude which files to read before
│       generating code, enforces class names, architecture rules, ADHD UX
│       rules, and code quality standards automatically every session.
│
├── .gitignore
│   └── Prevents secrets, keystore files, and local config from being
│       committed to GitHub accidentally.
│
├── .env                                           [gitignored]
│   └── Local secret values (GEMINI_API_KEY, FIREBASE keys) — never
│       committed; injected into BuildConfig via local.properties at build time.
│
├── local.properties                               [gitignored]
│   └── Android SDK path + secret key references — standard Android mechanism
│       for keeping credentials out of source control.
│
├── README.md
│   └── Developer setup guide — emulator launch commands, ADB pairing steps,
│       Gradle terminal commands, and demo seeder instructions for any
│       developer (or examiner) running the project fresh.
│
├── .github/
│   └── workflows/
│       └── ci.yml
│           └── GitHub Actions pipeline — runs build, lint, and unit tests
│               on every push to ensure the main branch is always green
│               and no broken code is merged.
│
├── docs/
│   │
│   ├── adr/                         Architectural Decision Records
│   │   ├── 001-clean-architecture.md
│   │   │   └── Documents why Clean Architecture + MVVM was chosen over
│   │   │       simpler alternatives — justifies the one-way dependency rule
│   │   │       and testability benefits for the examiner.
│   │   │
│   │   ├── 002-room-over-firebase.md
│   │   │   └── Documents why Room (local SQLite) was chosen over Firebase
│   │   │       Firestore — covers offline-first requirement, GDPR Art.9
│   │   │       compliance, and why eventual consistency is architecturally
│   │   │       incompatible with real-time trigger firing.
│   │   │
│   │   ├── 003-rag-over-finetuning.md
│   │   │   └── Documents why RAG (Retrieval Augmented Generation) was
│   │   │       chosen over fine-tuning — no GPU, no training loop, personal
│   │   │       context injected at inference time, GDPR-safe.
│   │   │
│   │   ├── 004-notificationmanager-delivery.md
│   │   │   └── Documents why NotificationManager + Wear OS channel was
│   │   │       chosen over MessageClient for the PoC — reliable on emulator
│   │   │       without Bluetooth pairing, with MessageClient as the
│   │   │       documented production upgrade path.
│   │   │
│   │   └── 005-adhd-ux-constraints.md
│   │       └── Documents every ADHD-specific UX decision encoded in
│   │           NeuroPulseTheme — one CTA per screen, amber-not-red errors,
│   │           48dp touch targets, graduated alerts — with clinical evidence
│   │           references for the demo discussion.
│   │
│   ├── extension-points.md
│   │   └── Documents exactly how to add new features in future versions
│   │       (new trigger types, new delivery surfaces, RAG upgrade, iOS port)
│   │       with the precise files to change and files guaranteed untouched —
│   │       proves progressive architecture to the examiner.
│   │
│   └── evaluation/
│       ├── design-decisions.md
│       │   └── Running log of every UI/UX decision made during development
│       │       with rationale and clinical reference — feeds directly into
│       │       the demo PPT slides and proves design thinking process.
│       │
│       └── errors.md
│           └── Running log of every non-trivial bug: symptom, root cause,
│               fix, and learning — demonstrates debugging skills required
│               by the marking criteria and provides demo talking points.
│
├── gradle/
│   └── libs.versions.toml
│       └── Centralised version catalog for all dependencies — single place
│           to update library versions, prevents version conflicts across
│           modules, standard practice in production Android projects.
│
├── :domain/                         Pure Kotlin — zero Android imports
│   └── src/main/kotlin/com/neuropulse/domain/
│       │
│       ├── model/
│       │   ├── TriggerType.kt                                    [Phase 0]
│       │   │   └── Enum defining the 5 trigger values (MORNING_PLAN,
│       │   │       PRE_EVENT, HYPERFOCUS, STRESS_GROUNDING, TASK_COMPLETE)
│       │   │       — the typed contract used by every agent, strategy,
│       │   │       and DAO throughout the entire system.
│       │   │
│       │   ├── PhysioContext.kt                                   [Phase 0]
│       │   │   └── Data class holding the current physiological snapshot
│       │   │       (sleepScore, hrv, currentHr, stressIndicator) — passed
│       │   │       through the Three-Agent Pipeline without any Android
│       │   │       dependencies.
│       │   │
│       │   ├── BriefingPayload.kt                                 [Phase 0]
│       │   │   └── Typed output of the AI reasoning layer — carries the
│       │   │       assembled briefing content and target delivery surface
│       │   │       from ContextEngine to DeliveryAgent.
│       │   │
│       │   └── UserProfile.kt                                     [Phase 1]
│       │       └── Holds the user's assigned TriggerType (persona) and
│       │           preferences — the single source of truth that gates
│       │           which features are visible throughout the app.
│       │
│       ├── usecase/
│       │   └── MorningPlanUseCase.kt                              [Phase 2]
│       │       └── Orchestrates the morning plan assembly — fetches today's
│       │           tasks, calls reorderByCapacity() with current PhysioContext,
│       │           and returns the ordered DayPlan; zero Android dependencies
│       │           so it is fully JUnit testable without an emulator.
│       │
│       └── repository/              Interfaces only — implementations in :data
│           ├── DayPlanRepository.kt                               [Phase 2]
│           │   └── Contract for all task and calendar data operations —
│           │       keeping the domain layer independent of Room so the
│           │       implementation can be swapped or faked in tests.
│           │
│           ├── PhysioRepository.kt                                [Phase 3]
│           │   └── Contract for reading physiological data — abstracts
│           │       Health Connect so the domain never knows whether data
│           │       comes from a real wearable or the synthetic seeder.
│           │
│           ├── CaptureRepository.kt                               [Phase 4]
│           │   └── Contract for capture storage and FTS5 retrieval —
│           │       findRelevant() is the RAG retrieval method that feeds
│           │       personal context into Gemini briefings.
│           │
│           └── BriefingRepository.kt                              [Phase 4]
│               └── Contract for LLM inference — abstracts Gemini Flash
│                   so the domain never knows which model is used; enables
│                   the Gemini Nano on-device upgrade with zero domain changes.
│
├── :data/                           Android data layer implementations
│   └── src/main/kotlin/com/neuropulse/data/
│       │
│       ├── local/
│       │   ├── NeuroPulseDatabase.kt                              [Phase 2]
│       │   │   └── Room database definition listing all entity classes and
│       │   │       DAOs — the single entry point for all local persistence,
│       │   │       with migration support for future schema changes.
│       │   │
│       │   ├── entity/
│       │   │   ├── DayPlanEntity.kt                               [Phase 2]
│       │   │   │   └── Room entity for daily task plans — stores original
│       │   │   │       and AI-reordered task JSON alongside the sleep and
│       │   │   │       HRV scores that drove the reordering decision.
│       │   │   │
│       │   │   ├── CaptureEntity.kt                               [Phase 2]
│       │   │   │   └── Room entity for user captures (voice notes, typed
│       │   │   │       notes, photos) — the personal context store that
│       │   │   │       feeds the RAG pipeline via FTS5 full-text search.
│       │   │   │
│       │   │   ├── PhysioSnapshotEntity.kt                        [Phase 3]
│       │   │   │   └── Room entity for timestamped physiological readings
│       │   │   │       — persists Health Connect data locally so triggers
│       │   │   │       fire from Room even when Health Connect is offline.
│       │   │   │
│       │   │   ├── BriefingLogEntity.kt                           [Phase 4]
│       │   │   │   └── Room entity recording every AI briefing delivered —
│       │   │   │       stores trigger type, payload, surface, and whether
│       │   │   │       the user acknowledged it; feeds the penalty scoring
│       │   │   │       system and the clinical audit log.
│       │   │   │
│       │   │   └── BriefingCaptureEntity.kt                       [Phase 4]
│       │   │       └── Junction table linking briefings to the captures
│       │   │           that contributed to them — stores relevance score
│       │   │           and rank so dismissed briefings penalise their
│       │   │           contributing captures over time (RAG feedback loop).
│       │   │
│       │   └── dao/
│       │       ├── DayPlanDao.kt                                  [Phase 2]
│       │       │   └── Room DAO for task and day plan operations — all
│       │       │       queries return Flow<T> so the UI reacts automatically
│       │       │       to data changes without manual refresh calls.
│       │       │
│       │       ├── CaptureDao.kt                                  [Phase 2]
│       │       │   └── Room DAO with FTS5 virtual table support — the
│       │       │       findRelevant() BM25 keyword search is defined here,
│       │       │       enabling RAG retrieval without an embedding model.
│       │       │
│       │       ├── PhysioSnapshotDao.kt                           [Phase 3]
│       │       │   └── Room DAO for physiological snapshot reads and writes
│       │       │       — latestSnapshot() provides the current PhysioContext
│       │       │       to the morning plan and trigger workers.
│       │       │
│       │       └── BriefingLogDao.kt                              [Phase 4]
│       │           └── Room DAO for briefing history — getPenaltyScores()
│       │               query returns a map of capture IDs to penalty weights
│       │               based on how often their briefings were dismissed.
│       │
│       ├── health/
│       │   ├── HealthConnectFacade.kt                             [Phase 3]
│       │   │   └── Wraps all Health Connect API calls behind a clean
│       │   │       interface — handles permission checking, null safety,
│       │   │       and error recovery so PhysioRepository never deals
│       │   │       with raw Health Connect exceptions directly.
│       │   │
│       │   └── HealthConnectSeeder.kt                             [Phase 3]
│       │       └── Writes realistic synthetic sleep, HRV, and HR records
│       │           into Health Connect on the emulator — provides a good-day
│       │           and bad-day toggle so all physiological features can be
│       │           demonstrated without a real wearable device.
│       │
│       ├── ai/
│       │   ├── strategy/
│       │   │   ├── BriefingStrategy.kt                            [Phase 4]
│       │   │   │   └── Interface defining the Strategy pattern for briefing
│       │   │   │       assembly — each trigger type gets its own implementation;
│       │   │   │       documented in extension-points.md as the hook for
│       │   │   │       adding new trigger types in future versions.
│       │   │   │
│       │   │   ├── MorningPlanStrategy.kt                         [Phase 4]
│       │   │   │   └── Builds the anonymised Gemini prompt for the morning
│       │   │   │       adaptive day plan — injects sleep score, HRV, and
│       │   │   │       relevant captures; never includes raw biometric values.
│       │   │   │
│       │   │   ├── PreEventStrategy.kt                            [Phase 4]
│       │   │   │   └── Builds the briefing prompt for UC2 pre-event wrist
│       │   │   │       alerts — includes event context, attendees, and
│       │   │   │       relevant captures linked to that event or contact.
│       │   │   │
│       │   │   ├── HyperfocusStrategy.kt                          [Phase 4]
│       │   │   │   └── Builds the briefing prompt for UC3 hyperfocus
│       │   │   │       interruption — gentle framing that surfaces the
│       │   │   │       commitment without fully breaking the flow state.
│       │   │   │
│       │   │   └── StressGroundingStrategy.kt                     [Phase 4]
│       │   │       └── Builds the briefing prompt for UC4 stress response
│       │   │           — short breathing exercise instruction calibrated
│       │   │           to the user's current HR reading.
│       │   │
│       │   └── ContextEngine.kt                                   [Phase 4]
│       │       └── The AI reasoning filter in the Three-Agent Pipeline —
│       │           selects the correct BriefingStrategy, retrieves relevant
│       │           captures via FTS5, assembles the anonymised prompt, and
│       │           calls BriefingRepository.runInference() to produce
│       │           a typed BriefingPayload.
│       │
│       ├── delivery/
│       │   ├── DeliveryStrategy.kt                                [Phase 5]
│       │   │   └── Interface for the delivery layer — the extension point
│       │   │       that makes swapping NotificationManager for MessageClient
│       │   │       (physical watch haptics) a single Hilt binding change
│       │   │       with zero impact on DeliveryAgent or any upstream code.
│       │   │
│       │   ├── DeliveryAgent.kt                                   [Phase 5]
│       │   │   └── The routing filter in the Three-Agent Pipeline — receives
│       │   │       BriefingPayload from ContextEngine and delegates to the
│       │   │       injected DeliveryStrategy; agnostic to whether delivery
│       │   │       goes to phone, watch, or any future surface.
│       │   │
│       │   └── PhoneDeliveryStrategy.kt                           [Phase 5]
│       │       └── PoC implementation of DeliveryStrategy using
│       │           NotificationManager with a Wear OS notification channel —
│       │           delivers all 5 UC cards to the Wear OS emulator over ADB
│       │           without requiring Bluetooth pairing.
│       │
│       ├── seeder/
│       │   └── DatabaseSeeder.kt                                  [Phase 2]
│       │       └── Pre-populates Room with 7 days of sample tasks, calendar
│       │           events, and captures for emulator demos — provides
│       │           reproducible good-day, bad-day, and offline states so
│       │           every demo run looks identical.
│       │
│       └── di/
│           ├── DatabaseModule.kt                                  [Phase 2]
│           │   └── Hilt module providing Room database and all DAOs as
│           │       singletons — ensures one database instance across the
│           │       app and makes DAOs injectable everywhere without
│           │       passing context manually.
│           │
│           ├── HealthModule.kt                                    [Phase 3]
│           │   └── Hilt module providing HealthConnectClient and
│           │       HealthConnectFacade — isolates the Health Connect
│           │       dependency so it can be faked in unit tests without
│           │       touching real sensor APIs.
│           │
│           ├── BriefingModule.kt                                  [Phase 4]
│           │   └── Hilt module binding ChatLanguageModel to Gemini Flash
│           │       and registering all BriefingStrategy implementations —
│           │       swapping to Gemini Nano in v2 requires changing only
│           │       this file.
│           │
│           └── DeliveryModule.kt                                  [Phase 5]
│               └── Hilt module binding DeliveryStrategy to
│                   PhoneDeliveryStrategy for the PoC — the single file
│                   to change when upgrading to WearDeliveryStrategy
│                   for physical watch haptic delivery.
│
├── :app/                            Phone UI layer
│   └── src/main/kotlin/com/neuropulse/
│       │
│       ├── NeuroPulseApplication.kt                               [Phase 0]
│       │   └── Application entry point — initialises Hilt for dependency
│       │       injection and plants Timber debug tree for structured logging;
│       │       the first file that runs when the app launches.
│       │
│       ├── ui/
│       │   ├── theme/
│       │   │   ├── NeuroPulseTheme.kt                             [Phase 0]
│       │   │   │   └── Master theme file and ADHD UX enforcement point —
│       │   │   │       wraps MaterialTheme with NeuroPulse tokens and wires
│       │   │   │       LocalReduceMotion; every screen must be wrapped in
│       │   │   │       this, never raw MaterialTheme.
│       │   │   │
│       │   │   ├── NeuroPulseColors.kt                            [Phase 0]
│       │   │   │   └── All colour tokens for light and dark mode — encodes
│       │   │   │       ADHD constraints (warning amber instead of red,
│       │   │   │       minimum 4.5:1 contrast ratios) so no screen can
│       │   │   │       accidentally use an anxiety-triggering colour.
│       │   │   │
│       │   │   ├── NeuroPulseTypography.kt                        [Phase 0]
│       │   │   │   └── Typography scale using sp units exclusively — ensures
│       │   │   │       all text respects the user's system font size setting,
│       │   │   │       which is critical for ADHD users who often increase
│       │   │   │       font size for readability.
│       │   │   │
│       │   │   └── NeuroPulseSpacing.kt                           [Phase 0]
│       │   │       └── Spacing and sizing constants — defines touchTarget
│       │   │           (48dp minimum), standard padding values, and pill
│       │   │           heights so every screen is consistent without
│       │   │           hardcoded dimension values.
│       │   │
│       │   ├── onboarding/
│       │   │   ├── WelcomeScreen.kt                               [Phase 1]
│       │   │   │   └── First screen after sign-in — introduces NeuroPulse
│       │   │   │       with calm language and a single CTA to begin the
│       │   │   │       questionnaire; sets the tone for the whole app.
│       │   │   │
│       │   │   ├── QuestionnaireScreen.kt                         [Phase 1]
│       │   │   │   └── Three-question ADHD profile assessment — maps
│       │   │   │       answers to TriggerType weights and assigns
│       │   │   │       TIME_BLINDNESS, HYPERFOCUS, or COMBINED persona.
│       │   │   │
│       │   │   └── ProfileResultScreen.kt                         [Phase 1]
│       │   │       └── Shows the assigned persona with a plain-language
│       │   │           explanation of what it means and what the app will
│       │   │           do — builds user trust before any permissions are
│       │   │           requested.
│       │   │
│       │   ├── morning/
│       │   │   ├── MorningPlanScreen.kt                           [Phase 2]
│       │   │   │   └── Primary daily screen — shows physiological context
│       │   │   │       pills (Sleep · HRV · Stress), AI-reordered task list
│       │   │   │       capped at 5 items, and a single "Start with Task 01"
│       │   │   │       CTA; the most-used screen in the app.
│       │   │   │
│       │   │   └── MorningPlanViewModel.kt                        [Phase 2]
│       │   │       └── Presentation layer for the morning plan — exposes
│       │   │           StateFlow to MorningPlanScreen, calls
│       │   │           MorningPlanUseCase, and handles task completion
│       │   │           events without any Room or Health Connect imports.
│       │   │
│       │   ├── briefing/
│       │   │   └── BriefingScreen.kt                              [Phase 4]
│       │   │       └── Displays the AI-generated contextual briefing for
│       │   │           pre-event, hyperfocus, and stress triggers — one
│       │   │           primary action ("Got it") to acknowledge and dismiss.
│       │   │
│       │   ├── capture/
│       │   │   └── QuickCaptureScreen.kt                          [Phase 4]
│       │   │       └── Minimal friction capture interface — voice note,
│       │   │           typed note, or photo saved as CaptureEntity and
│       │   │           fed into the RAG pipeline to enrich future briefings.
│       │   │
│       │   └── profile/
│       │       └── ProfileScreen.kt                               [Phase 6]
│       │           └── Shows current TriggerType assignment, allows
│       │               re-taking the questionnaire, and surfaces the
│       │               briefing log for user review and clinical audit.
│       │
│       └── navigation/
│           └── NeuroPulseNavGraph.kt                              [Phase 1]
│               └── Single navigation graph for the entire app — gates
│                   all routes behind auth check and routes to onboarding
│                   if UserProfile is absent; persona-aware routing shows
│                   only the features matching the user's TriggerType.
│
└── :wear/                           Wear OS companion module
    └── src/main/kotlin/com/neuropulse/wear/
        │
        ├── WearApplication.kt                                     [Phase 5]
        │   └── Wear OS application entry point — initialises Hilt and
        │       Timber for the watch module; required for the Wear OS
        │       emulator to recognise the companion app.
        │
        ├── receiver/
        │   └── WearNotificationReceiver.kt                        [Phase 5]
        │       └── Receives notifications dispatched from the phone via
        │           the Wear OS notification channel and routes them to
        │           the correct tile or watch face card for display.
        │
        └── tile/
            ├── BreathingTile.kt                                   [Phase 5]
            │   └── Wear OS tile for the UC4 stress grounding prompt —
            │       displays a guided breathing exercise with a visual
            │       inhale/exhale indicator sized for a round watch face.
            │
            └── TaskCompleteTile.kt                                [Phase 5]
                └── Wear OS tile for UC5 task completion reinforcement —
                    delivers a short positive affirmation message to the
                    wrist when the user marks a task complete on the phone.
```

---

## CLAUDE.md — Contents (create this as the very first file in Phase 0)

```markdown
# CLAUDE.md — NeuroPulse AI Session Instructions

## Read these files before writing any code

1. /app/src/main/java/com/neuropulse/ui/theme/NeuroPulseTheme.kt
   Never hardcode colours, dimensions, or font sizes.
   Always import from NeuroPulseColors, NeuroPulseTypography, NeuroPulseSpacing.

2. /docs/extension-points.md
   Check before adding any class implementing an existing interface.
   Update this file after adding any new strategy, repository impl, or delivery surface.

3. /docs/adr/
   Check before making any architectural decision.
   Create a new ADR before writing code for a new architectural pattern.

4. /docs/evaluation/design-decisions.md
   Append an entry for every UI/UX decision made during this session.

5. /docs/evaluation/errors.md
   Append an entry for every non-trivial bug identified or fixed.

## Fixed class names — never rename

ContextEngine, MorningPlanUseCase, MorningPlanViewModel,
DayPlanRepository, BriefingRepository, CaptureRepository,
PhysioRepository, TriggerType, PhysioContext, BriefingPayload,
DeliveryAgent, BriefingStrategy, DayPlanEntity, CaptureEntity,
BriefingLogEntity, PhysioSnapshotEntity, BriefingCaptureEntity,
HealthConnectFacade, DatabaseSeeder

## Architecture rules

- :domain = zero Android imports. If you write `import android.*` in :domain, stop.
- Dependency direction: ViewModel → UseCase → Repository → DAO/Client. Never reverse.
- No magic strings. Use TriggerType enum, string resources, or named constants.
- No hardcoded colours, dimensions, font sizes. Use NeuroPulseTheme tokens.
- Secrets via BuildConfig from local.properties only. Never in source code.

## ADHD UX rules — enforced on every Composable

- One primary CTA per screen maximum.
- All text in sp units, never dp.
- All touch targets minimum 48dp (use NeuroPulseSpacing.touchTarget).
- Every interactive Composable needs contentDescription.
- Never use Color.Red for errors — use NeuroPulseTheme.colors.warning.
- All animations check LocalReduceMotion before playing.
- Task lists: cap at 5 visible items, show-more for the rest.
- Notifications: graduated sequence (gentle → firm → full) — never single jarring alert.
- High contrast: minimum 4.5:1 ratio — enforced in NeuroPulseColors.

## Code quality rules

- KDoc on every public class and function.
- Inline comments explain WHY not WHAT. Reference ADR number where relevant.
- Timber for all logging. Never use Log.d, Log.e, or println.
- Every public function needs a corresponding unit test.
- No function body longer than 30 lines. Extract if needed.
- Add @Preview (light + dark) to every new Composable screen.

## Commit format — Conventional Commits

feat|fix|test|docs|refactor|chore(scope): description

## Privacy rules

- Raw HR, HRV, sleep data never leaves the device.
- BriefingStrategy.buildPrompt() output must never contain raw biometric values.
- Gemini API payload: anonymised summaries only.

## After generating code, confirm

1. New class implementing an interface → extension-points.md updated?
2. New architectural decision → ADR created?
3. New UI/UX decision → design-decisions.md entry appended?
4. All public APIs have KDoc?
5. :domain still has zero Android imports?
6. New Composable has @Preview annotations (light + dark)?
```

---

## Phase 0 — Environment Setup & Project Scaffold

### Quality gate (applies to every phase from here)
- All new public APIs KDoc'd before phase is closed
- Timber logging on every new class
- @Preview (light + dark) on every new Composable
- ADR written for every new architectural decision
- design-decisions.md entry for every UI/UX decision
- errors.md entry for every non-trivial bug

### Tasks

**Environment**
- Install VS Code with all extensions listed above
- Install Android SDK CLI tools; set `ANDROID_HOME` and `PATH`
- Install SDK packages via `sdkmanager` (phone + Wear OS system images)
- Create phone AVD (`NeuroPulse_AVD`) and Wear OS AVD (`NeuroPulse_Wear`)

**Project scaffold**
- Create multi-module Android project: `:app`, `:wear`, `:domain`, `:data`
- Configure `libs.versions.toml` version catalog
- Set up base Hilt across all modules
- Configure `local.properties` + `.env` for secrets; add to `.gitignore`
- Add GitHub Actions `ci.yml`: build + lint + test on push
- Configure GitHub Secrets for all API keys

**Standards files — create before any feature Kotlin code**
- `CLAUDE.md` — full content above
- `NeuroPulseTheme.kt`, `NeuroPulseColors.kt`, `NeuroPulseTypography.kt`, `NeuroPulseSpacing.kt`
- `NeuroPulseApplication.kt` — Hilt + Timber init
- All 5 ADR files in `/docs/adr/`
- `/docs/extension-points.md`
- `/docs/evaluation/design-decisions.md`
- `/docs/evaluation/errors.md`
- `README.md`

**Domain stubs**
- `TriggerType.kt`, `PhysioContext.kt`, `BriefingPayload.kt`
- All repository interfaces in `:domain`

### Test coverage
- `./gradlew build` passes on all modules
- Phone emulator launches; Wear OS emulator launches standalone
- `GEMINI_API_KEY` resolves masked from `BuildConfig`
- GitHub Actions green

### Tech
`VS Code` `Android SDK CLI` `Kotlin 2.x` `Gradle version catalog` `Hilt` `Timber` `GitHub Actions`

---

## Phase 1 — Auth, Onboarding & Persona Assignment

### Tasks
- Firebase Auth (Google Sign-In + email) wrapper in `:data`
- `UserProfile` entity with `dominantTrigger: TriggerType`
- `DataStore` for lightweight prefs
- 3-screen onboarding flow: `WelcomeScreen` → `QuestionnaireScreen` → `ProfileResultScreen`
- Questionnaire scoring → `TriggerType` assignment; persist to Room + Firestore
- `NeuroPulseNavGraph.kt` — auth gate + persona-aware routing
- Feature flag helper reading `UserProfile.dominantTrigger`
- `BuildConfig` skip-auth flag for emulator testing
- Every screen wrapped in `NeuroPulseTheme` — never raw MaterialTheme
- `LocalReduceMotion` wired in theme
- All text in `sp`, all touch targets `≥ 48dp`, all interactive elements `contentDescription`
- `@Preview` (light + dark) on all 3 screens
- First `design-decisions.md` entries: one-CTA rule, colour token rationale

### Demo checkpoint
- Google Sign-In → questionnaire → profile assigned and shown
- Re-open → persisted, lands on main screen
- COMBINED profile shows both feature sets in nav

### Test coverage
- Unit: scoring logic — all 3 outcome branches
- Unit: `UserProfile` DAO insert + read
- Integration: onboarding flow → `TriggerType` written correctly
- E2E: sign-in → questionnaire → main screen

### Tech
`Firebase Auth` `Firestore` `Room` `Jetpack Compose` `DataStore` `Hilt`

---

## Phase 2 — Room Schema, Task Planning & Calendar Sync

### Tasks
- Full Room schema: all 6 entities + DAOs with `Flow<T>`; FTS5 on `captures`
- `MorningPlanScreen` + `MorningPlanViewModel`
- Wire: `MorningPlanViewModel` → `MorningPlanUseCase` → `DayPlanRepository`
- `CalendarContract` ContentResolver → persist to `events` table
- `DayPlanRepository.getUpcomingEvents(windowHours)` implemented
- Stub `reorderByCapacity()` — priority sort only, no physio yet
- `DatabaseSeeder` — 7 days sample events, tasks, and captures
- Task list: cap at 5 visible, "show more" affordance
- All strings in `strings.xml`, plain ADHD-friendly language
- `@Preview` on `MorningPlanScreen`

### Demo checkpoint
- Morning plan shows tasks ordered by priority
- Create / complete tasks persist across restarts
- Emulator calendar events appear in upcoming list

### Test coverage
- Unit: all DAO operations (insert, update, query, Flow emission)
- Unit: `MorningPlanUseCase.getTodayPlan()` with fake repository
- Unit: CalendarProvider parser with mock `ContentResolver`
- Integration: Room migration from v1 baseline
- E2E: task creation → completion → relaunch → state preserved

### Tech
`Room + FTS5` `CalendarContract` `Kotlin Flow` `Compose` `WorkManager`

---

## Phase 3 — Health Connect & Physiological Engine

> Install Android Studio before starting this phase.

### Tasks
- Install Android Studio — required for Health Connect APK install on emulator
- Health Connect SDK + calm permission request flow
- `HealthConnectFacade` — permission checks, null safety, error recovery
- `PhysioRepository` impl: sleep score, HRV, HR, step count reads
- `PassiveMonitoringClient` for background listening
- `HealthConnectSeeder` — 7-day synthetic records, good/bad day toggle
- `PhysioSnapshotEntity` persisted to Room on each read
- Chain of Responsibility: `SleepQualityHandler → HRStateHandler → ActivityStateHandler → StressIndicatorHandler` → `PhysioContext`
- Wire `reorderByCapacity()` using live `PhysioContext`
- Physiological context pills on `MorningPlanScreen`

### Demo checkpoint
- Pills: Sleep 62 · HRV 28ms · Stress Elevated
- Tasks reorder visibly between good-day and bad-day seed states
- Seeder togglable for demo

### Test coverage
- Unit: each Chain of Responsibility handler in isolation
- Unit: `reorderByCapacity()` with mocked `PhysioContext`
- Unit: `HealthConnectFacade` null-HRV graceful fallback
- Integration: seeder writes → repository reads → Room snapshot stored
- E2E: low-HRV seed → verify task order on screen

### Tech
`Health Connect SDK` `PassiveMonitoringClient` `WorkManager` `Kotlin Flow` `Hilt` `Android Studio`

---

## Phase 4 — AI Briefing Engine (RAG + Gemini)

### Tasks
- LangChain4j + `ChatLanguageModel` → Gemini Flash via Hilt
- `ContextEngine.assembleBriefing(trigger, physioContext)`
- Four `BriefingStrategy` implementations: Morning, PreEvent, Hyperfocus, StressGrounding
- `CaptureRepository.findRelevant(query, topK)` — FTS5 BM25
- `BriefingRepository.runInference(prompt)` — Gemini Flash; anonymised prompt only
- Deterministic fallback when offline
- `BriefingLogEntity` written after each inference; `getPenaltyScores()` implemented
- `QuickCaptureScreen` — voice / typed / photo → `CaptureEntity`
- `MIN_THRESHOLD` constant; `DatabaseSeeder` pre-populates captures
- `@Preview` on `BriefingScreen` and `QuickCaptureScreen`
- `extension-points.md` updated: BriefingStrategy documented as hook for new trigger types

### Demo checkpoint
- Morning plan triggers real Gemini briefing enriched by seeded captures
- Pre-event briefing 15 min before seeded event
- Offline → deterministic fallback shown
- New capture → appears in next briefing

### Test coverage
- Unit: each `buildPrompt()` — assert no raw PII in output
- Unit: `findRelevant()` FTS5 ranking with seed data
- Unit: `getPenaltyScores()` — dismissed briefing reduces capture score
- Integration: full pipeline → `BriefingPayload`
- Integration: offline fallback path
- E2E: morning launch → AI briefing on screen within 5s

### Tech
`LangChain4j` `Gemini Flash API` `Room FTS5` `WorkManager` `Hilt`

---

## Phase 5 — Trigger Engine & Wear OS Notification Dispatch

### Wear OS demo — what each UC shows on watch emulator

| UC | Watch emulator shows |
|---|---|
| UC1 Morning plan | Extended notification card |
| UC2 Pre-event briefing | Heads-up: event title + time remaining |
| UC3 Hyperfocus alert | Nudge notification with commitment text |
| UC4 Stress grounding | Notification → opens BreathingTile |
| UC5 Task completion | Positive affirmation card |

### Tasks
- `AlarmManager` exact alarm for UC2 (15 min before event)
- `SCHEDULE_EXACT_ALARM` detection + WorkManager 15-min fallback
- Wear OS notification channel on startup (`IMPORTANCE_HIGH`)
- `DeliveryStrategy` interface — documented in `extension-points.md`
- `PhoneDeliveryStrategy` — `NotificationManager` implementation
- `DeliveryAgent` routes `BriefingPayload` → injected `DeliveryStrategy`
- Hyperfocus detection worker: stillness + low HR variance → UC3
- HR spike: `isHrSpikeDetected()` → UC4
- Inactivity worker: drink water reminder (both personas)
- Graduated notification sequence: gentle → 30s → firm → 60s → full
- Calendar suppression: hyperfocus alert off during active event
- `:wear` module: `WearNotificationReceiver`, `BreathingTile`, `TaskCompleteTile`
- `DeliveryModule.kt` — binding `PhoneDeliveryStrategy`; upgrade note to `WearDeliveryStrategy`

### Demo checkpoint
- Seeded event → alarm fires → card on phone + watch simultaneously
- All 5 UC notifications on Wear OS emulator screen
- Graduated sequence in logs
- Breathing tile renders on round watch face

### Test coverage
- Unit: hyperfocus detection — stillness threshold conditions
- Unit: `DeliveryAgent` with mocked `DeliveryStrategy` — payload per UC
- Unit: Wear OS channel correct importance
- Unit: calendar suppression logic
- Unit: `AlarmManager` scheduling with mocked clock
- Integration: trigger → `ContextEngine` → `DeliveryAgent` → dispatched
- E2E: UC2 full flow — seeded event → alarm → watch card

### Tech
`AlarmManager` `WorkManager` `NotificationManager` `Wear OS channel` `Wear OS Tiles` `Compose for Wear`

---

## Phase 6 — Polish, Compliance & Release Readiness

### Tasks

**ADHD accessibility full audit**
- TalkBack walkthrough: every screen navigable by voice
- Content descriptions on all interactive elements verified
- 4.5:1 contrast ratio confirmed for all colour pairs
- Font scale 200% test — no clipping on any screen
- All error copy: plain language, no jargon, no red
- Touch targets ≥ 48dp confirmed on all elements

**UC5 + profile**
- Task completion → affirmation dispatched to watch
- `ProfileScreen` — shows `TriggerType`, re-questionnaire option
- Admin UC15 — `briefing_log` audit screen

**GDPR compliance**
- Art.9 audit: raw biometrics confirmed never leaving device
- `/docs/privacy-policy.md` written
- User data deletion flow implemented
- Onboarding permission screens finalised

**Release build**
- `release` build type with ProGuard/R8
- ProGuard rules for Room, Hilt, LangChain4j, Firebase
- App signing config + GitHub Secrets for CI signing
- `bundleRelease` verified buildable
- `versionCode` + `versionName` strategy in README

**Novel improvements roadmap** (appended to `design-decisions.md`)
- Gemini Nano on-device (Android AICore) — closes offline LLM gap
- AllMiniLM-L6-v2 embeddings + cosine similarity — semantic RAG upgrade
- `WearDeliveryStrategy` — MessageClient + haptic patterns for physical watch
- EEG biofeedback sensor integration
- iOS port via Kotlin Multiplatform (`:domain` already KMP-ready)
- NHS Digital API integration pathway

### Demo checkpoint
- Full Marcus journey: low-sleep → reordered plan → pre-event → task complete → wrist affirmation
- Full Zoe journey: hyperfocus → watch nudge → breathing tile
- Offline: core features work, AI shows fallback
- No raw biometric in Gemini call (network inspector confirmed)
- TalkBack navigates every screen without getting stuck
- Release bundle builds without ProGuard errors

### Test coverage
- Full regression: all prior tests pass
- E2E: Marcus journey (UC1 → UC2 → UC5)
- E2E: Zoe journey (UC3 → UC4 → UC5)
- E2E: COMBINED persona — both feature sets, no conflicts
- Privacy: Gemini call intercepted — no raw HR/HRV in prompt
- Offline E2E: network disabled → core features verified
- Accessibility: TalkBack E2E on onboarding + morning plan

### Tech
`Android Studio Network Inspector` `ProGuard/R8` `Espresso` `JUnit 5` `MockK` `Hilt testing`

---

## Phase 7 — Demo Preparation

### Tasks

**PowerPoint walkthrough (required by brief)**

| Slide | Content | Criteria addressed |
|---|---|---|
| 1 | Problem: 4 ADHD challenges | Module LO1 |
| 2 | Architecture: Clean Architecture + Three-Agent Pipeline | Module LO2 |
| 3 | Technology selection rationale (ADR references) | Module LO3 |
| 4 | Data persistence: Room schema + why Room over Firebase | Module LO4 + A* |
| 5 | ADHD UX decisions (one CTA, amber errors, graduated alerts) | A* novel |
| 6 | Design patterns: Strategy, Chain of Responsibility, Pipe & Filter | A* deep understanding |
| 7 | Live demo — Marcus journey | A* functional |
| 8 | Live demo — Zoe journey | A* functional |
| 9 | GDPR + privacy architecture | A* deployment |
| 10 | Deployment pipeline: Play Store, signing, ProGuard | A* deployment |
| 11 | Novel improvements roadmap (v2 + KMP iOS path) | A* novel |
| 12 | Extension points: how v2 features slot in | A* deep understanding |

**Demo script — pre-seeded states (reproducible every run)**
- State A: bad sleep (Sleep 42 · HRV 18ms · Stress Elevated) — shows Marcus reordering
- State B: good sleep (Sleep 78 · HRV 52ms · Stress Normal) — shows contrast
- State C: offline — shows deterministic fallback briefing

**Evaluation log review for PPT content**
- `design-decisions.md` — pick 3 most interesting decisions for slides
- `errors.md` — pick 2 most instructive bugs for debugging skills discussion
- `adr/` — reference each ADR in the relevant slide

---

## Key Architecture Notes

- **Class names fixed** — match class diagram exactly, never renamed
- **One-way dependency** — `:domain` zero Android imports, fully JUnit-testable
- **Data sovereignty** — physiological data stays in Room; only anonymised summaries to Gemini
- **Offline-first** — every trigger fires from local Room; LLM enriches but never blocks
- **Persona gating** — `UserProfile.dominantTrigger` single source of truth for feature visibility
- **ADHD UX** — encoded as compile-time constraints in `NeuroPulseTheme.kt` from Phase 0
- **Extension points** — every interface is a documented future swap; see `extension-points.md`
- **Wear OS delivery** — `NotificationManager` for PoC; `WearDeliveryStrategy` = one Hilt binding swap
- **Progressive architecture** — new trigger types, delivery surfaces, RAG upgrades each touch ≤ 3 files
- **CLAUDE.md** — ensures every AI code generation session automatically follows all project standards

---

*NeuroPulse | S21161041 | Birmingham City University 2026*