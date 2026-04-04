# NeuroPulse

**A neuroadaptive companion for adults living with ADHD.**  
BCU CMP6213 | Student: Aathira T Dev (S21161041)  
Platform: Android (Jetpack Compose) + Wear OS companion

---

## What it does

NeuroPulse is a passive cognitive scaffolding system — no app-opening required.
It monitors physiological signals (sleep, HRV, heart rate) via Health Connect
and delivers contextual briefings to the wrist at the right moment:

| Use Case | Trigger | Persona |
|---|---|---|
| UC1 — Morning Adaptive Day Plan | Scheduled 07:00, sleep + HRV → task reorder | Both |
| UC2 — Pre-Event Wrist Briefing | 15 min before calendar event | Marcus (time blindness) |
| UC3 — Hyperfocus Alert | Stillness + motion detection | Zoe (hyperfocus) |
| UC4 — Stress Grounding Prompt | HR spike detected | Both |
| UC5 — Task Completion Reinforcement | User marks task done | Both |

---

## Project structure

```
NeuroPulse - Mobile App/
├── app/                        Phone app module
│   └── src/main/kotlin/com/neuropulse/
│       ├── domain/             Pure Kotlin — zero Android imports
│       │   ├── model/          TriggerType, PhysioContext, BriefingPayload
│       │   └── repository/     Repository interfaces (implementations in data/)
│       ├── data/               Android implementations
│       │   ├── local/          Room database, DAOs, entities
│       │   ├── health/         Health Connect facade + seeder
│       │   ├── ai/             ContextEngine, BriefingStrategies, Gemini
│       │   ├── delivery/       DeliveryAgent, DeliveryStrategy
│       │   └── di/             Hilt modules
│       └── ui/                 Jetpack Compose screens + ViewModels
│           ├── theme/          NeuroPulseTheme, Colors, Typography, Spacing
│           ├── onboarding/     Phase 1
│           ├── morning/        Phase 2
│           ├── briefing/       Phase 4
│           └── capture/        Phase 4
├── wear/                       Wear OS companion module (Phase 5)
├── docs/
│   ├── adr/                    5 Architectural Decision Records
│   ├── evaluation/             design-decisions.md + errors.md
│   ├── extension-points.md     How to add new trigger types, delivery surfaces, etc.
│   └── projectplan.md          Full 7-phase implementation plan
├── gradle/libs.versions.toml   Centralised dependency version catalog
├── CLAUDE.md                   AI session instructions (read before coding)
└── local.properties            [gitignored] SDK path + API keys
```

---

## Local setup (fresh clone)

### 1 — Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 (Temurin) | Portable install at `C:\Users\<you>\.jdks\jdk-21.x.x+x\` |
| Android SDK | API 34–35 | Install via `sdkmanager` (Phase 3) |
| VS Code | Latest | See extensions below |

### 2 — Clone and open

```bash
git clone <repo-url> "NeuroPulse - Mobile App"
cd "NeuroPulse - Mobile App"
```

Open in VS Code — it will prompt to install recommended extensions
(from `.vscode/extensions.json`). Accept all.

### 3 — Generate Gradle wrapper (first time only)

```bash
gradle wrapper --gradle-version 8.11.1
```

### 4 — Create `local.properties`

Copy the template and fill in your keys:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=<your key from aistudio.google.com/apikey>
FIREBASE_WEB_API_KEY=<from Firebase console>
FIREBASE_PROJECT_ID=<from Firebase console>
FIREBASE_APP_ID=<from Firebase console>
```

### 5 — Download Atkinson Hyperlegible fonts

1. Download from [brailleinstitute.org/freefont](https://brailleinstitute.org/freefont)
2. Rename and place in `app/src/main/res/font/`:
   - `atkinson_hyperlegible_regular.ttf`
   - `atkinson_hyperlegible_bold.ttf`
   - `atkinson_hyperlegible_italic.ttf`
   - `atkinson_hyperlegible_bold_italic.ttf`

See `app/src/main/res/font/FONTS_REQUIRED.md` for full instructions.

### 6 — Build

```bash
./gradlew assembleDebug
```

---

## Common commands

```bash
./gradlew assembleDebug          # Build debug APKs (app + wear)
./gradlew installDebug           # Install on connected emulator/device
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (needs running emulator)
./gradlew lint                   # Lint — reports in build/reports/
adb logcat -s NeuroPulse:V       # Filtered Logcat
```

---

## Emulator setup (Phase 3 — Android SDK required)

```bash
# Install SDK packages
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "system-images;android-34;google_apis;x86_64"
sdkmanager "system-images;android-34;android-wear;x86_64"

# Create AVDs
avdmanager create avd -n NeuroPulse_AVD \
  -k "system-images;android-34;google_apis;x86_64"
avdmanager create avd -n NeuroPulse_Wear \
  -k "system-images;android-34;android-wear;x86_64" \
  --device "wearos_small_round"

# Launch
emulator -avd NeuroPulse_AVD &
emulator -avd NeuroPulse_Wear &

# Pair Wear OS to phone emulator over ADB
adb -s emulator-5554 forward tcp:5601 tcp:5601
adb -s emulator-5556 connect localhost:5601
```

---

## GitHub Secrets (for CI)

Add these in Settings → Secrets → Actions:

| Secret | Where to get it |
|---|---|
| `GEMINI_API_KEY` | [aistudio.google.com/apikey](https://aistudio.google.com/apikey) |
| `FIREBASE_WEB_API_KEY` | Firebase console → Project Settings → Your apps |
| `FIREBASE_PROJECT_ID` | Firebase console → Project Settings |
| `FIREBASE_APP_ID` | Firebase console → Project Settings → Your apps |

All can be empty strings until Phase 1 (Firebase) and Phase 4 (Gemini) are wired up.

---

## Key architecture references

| Document | Purpose |
|---|---|
| `CLAUDE.md` | AI session rules — read before every coding session |
| `docs/adr/001-clean-architecture.md` | Layer boundaries + Three-Agent Pipeline |
| `docs/adr/002-room-over-firebase.md` | Why Room, GDPR Art.9 compliance |
| `docs/adr/003-rag-over-finetuning.md` | RAG pipeline + Gemini Flash |
| `docs/adr/004-notificationmanager-delivery.md` | Notification delivery + Wear OS channel |
| `docs/adr/005-adhd-ux-constraints.md` | ADHD colour, typography, motion rules |
| `docs/extension-points.md` | How to add triggers, strategies, delivery surfaces |
| `docs/decision_log.md` | Full trade-off analysis for all 15 architectural decisions |
