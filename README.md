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
| Android Studio | **Panda 3 (2025.3.3)** | Download from [developer.android.com/studio](https://developer.android.com/studio) |
| Claude Code extension | Latest | Install from Android Studio → Settings → Plugins → search "Claude Code" |
| Git | Any recent version | [git-scm.com](https://git-scm.com) |

**You do NOT need to install separately:** JDK (bundled with Android Studio), Android SDK
(auto-downloaded on first project open), Gradle (bundled in the project as `gradlew`).

### 2 — Clone and open

```bash
git clone https://github.com/AathiraTD/NeuroPulse.git
cd NeuroPulse
```

Open in Android Studio: **File → Open** → select the `NeuroPulse` folder.

On first open, Android Studio will:
- Download the Android SDK (API 35) if missing
- Create `local.properties` with your SDK path automatically
- Run Gradle sync (may take 2–3 minutes the first time)

### 3 — Quick start (no Firebase needed)

The app runs in debug mode with `SKIP_AUTH=true` — this bypasses Firebase Auth
entirely and goes straight to the HOME screen. No API keys or Firebase config
required for this path.

Just click the **green Play button** in the toolbar (after creating an emulator — see step 5).

### 4 — Firebase setup (needed for auth flow)

Skip this step if you just want to see the HOME screen.
Do this when you want to test the full login → persona selection → home flow.

**A) Create a Firebase project**

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **"Add project"** → name it `NeuroPulse` → create
3. Go to **Authentication → Sign-in method** → enable:
   - Email/Password
   - Google
   - Anonymous
4. Go to **Project Settings → Your apps** → click **Android icon**
   - Package name: `com.neuropulse`
   - Register app → download `google-services.json`

**B) Place `google-services.json`**

Copy the downloaded file to:
```
NeuroPulse/app/google-services.json
```

**C) Add keys to `local.properties`**

Open `local.properties` (Android Studio created this in step 2) and add:
```properties
FIREBASE_WEB_CLIENT_ID=<your-web-client-id>.apps.googleusercontent.com
GEMINI_API_KEY=
```

To find your Web Client ID:
Firebase Console → Authentication → Sign-in method → Google → **Web SDK configuration** → Web client ID.

The `GEMINI_API_KEY` can stay empty until Phase 4 (AI pipeline).

A template file `local.properties.template` is included in the repo for reference.

**D) Enable the auth flow**

In `app/build.gradle.kts`, change the debug build type:
```kotlin
buildConfigField("Boolean", "SKIP_AUTH", "false")
```

Rebuild and run — you'll now see the full splash → login → persona selection flow.

### 5 — Emulator setup

1. In Android Studio, click **Device Manager** (phone icon in the right toolbar)
2. Click **"Create Virtual Device"**
3. Select **Pixel 6** → click Next
4. Select **API 35** (click Download if not installed) → click Next
5. Click **Finish**
6. Click the **Play** icon next to your new device to launch it

For **Wear OS** (Phase 5 only):
- Same steps, but select "Wear OS" category and a round watch device

### 6 — Build and run

- **Run on emulator:** Click the green **Play** button in the toolbar
- **Build only:** `./gradlew assembleDebug`
- **Run tests:** `./gradlew test`

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
