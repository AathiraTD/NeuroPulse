# CLAUDE.md — NeuroPulse AI Session Instructions

## Read these files before writing any code

1. `/app/src/main/kotlin/com/neuropulse/ui/theme/NeuroPulseTheme.kt`
   Never hardcode colours, dimensions, or font sizes.
   Always import from NeuroPulseColors, NeuroPulseTypography, NeuroPulseSpacing.

2. `/docs/extension-points.md`
   Check before adding any class implementing an existing interface.
   Update this file after adding any new strategy, repository impl, or delivery surface.

3. `/docs/adr/`
   Check before making any architectural decision.
   Create a new ADR before writing code for a new architectural pattern.

4. `/docs/evaluation/design-decisions.md`
   Append an entry for every UI/UX decision made during this session.

5. `/docs/evaluation/errors.md`
   Append an entry for every non-trivial bug identified or fixed.

## Module structure

This project uses 2 Gradle modules:
- `:app` — phone app (presentation + domain + data layers as packages)
- `:wear` — Wear OS companion

Clean Architecture layers inside `:app`:
```
app/src/main/kotlin/com/neuropulse/
├── domain/         ← Pure Kotlin — zero Android imports allowed here
│   ├── model/
│   ├── usecase/
│   └── repository/ ← interfaces only
├── data/           ← Android implementations (Room, Health Connect, Hilt modules)
│   ├── local/
│   ├── health/
│   ├── ai/
│   ├── delivery/
│   └── di/
└── ui/             ← Jetpack Compose screens + ViewModels
    ├── theme/
    ├── onboarding/
    ├── morning/
    ├── briefing/
    ├── capture/
    └── profile/
```

## Fixed class names — never rename

ContextEngine, MorningPlanUseCase, MorningPlanViewModel,
DayPlanRepository, BriefingRepository, CaptureRepository,
PhysioRepository, TriggerType, PhysioContext, BriefingPayload,
DeliveryAgent, BriefingStrategy, DayPlanEntity, CaptureEntity,
BriefingLogEntity, PhysioSnapshotEntity, BriefingCaptureEntity,
HealthConnectFacade, DatabaseSeeder

## Architecture rules

- `domain/` package = zero Android imports. If you write `import android.*` inside `domain/`, stop.
- Dependency direction: ViewModel → UseCase → Repository interface → DAO/Client. Never reverse.
- No magic strings. Use TriggerType enum, string resources, or named constants.
- No hardcoded colours, dimensions, font sizes. Use NeuroPulseTheme tokens.
- Secrets via BuildConfig from `local.properties` only. Never in source code.

## ADHD UX rules — enforced on every Composable

- One primary CTA per screen maximum.
- All text in `sp` units, never `dp`.
- All touch targets minimum 48dp (use `NeuroPulseSpacing.touchTarget`).
- Every interactive Composable needs `contentDescription`.
- Never use `Color.Red` for errors — use `NeuroPulseTheme.colors.warning`.
- All animations check `LocalReduceMotion` before playing.
- Task lists: cap at 5 visible items, show-more for the rest.
- Notifications: graduated sequence (gentle → firm → full) — never a single jarring alert.
- High contrast: minimum 4.5:1 ratio — enforced in NeuroPulseColors.

## Code quality rules

- KDoc on every public class and function.
- Inline comments explain WHY not WHAT. Reference ADR number where relevant.
- Timber for all logging. Never use `Log.d`, `Log.e`, or `println`.
- Every public function needs a corresponding unit test.
- No function body longer than 30 lines. Extract if needed.
- Add `@Preview` (light + dark) to every new Composable screen.

## Commit format — Conventional Commits

```
feat|fix|test|docs|refactor|chore(scope): description
```

## Privacy rules

- Raw HR, HRV, sleep data never leaves the device.
- `BriefingStrategy.buildPrompt()` output must never contain raw biometric values.
- Gemini API payload: anonymised summaries only.

## After generating code, confirm

1. New class implementing an interface → `extension-points.md` updated?
2. New architectural decision → ADR created?
3. New UI/UX decision → `design-decisions.md` entry appended?
4. All public APIs have KDoc?
5. `domain/` package still has zero Android imports?
6. New Composable has `@Preview` annotations (light + dark)?
