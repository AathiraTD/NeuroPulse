# ADR-005: ADHD UX Constraints

**Status:** Accepted  
**Date:** 2026-03  
**Full trade-off analysis:** `docs/decision_log.md` D-008, D-011, D-012  
**Implementation:** `app/src/main/kotlin/com/neuropulse/ui/theme/`

---

## Decision

NeuroPulse enforces a specific set of UX constraints in the theme system and
as code rules to reduce cognitive load for adults with ADHD. These constraints
are encoded in `NeuroPulseTheme`, `NeuroPulseColors`, `NeuroPulseTypography`,
`NeuroPulseSpacing`, and `NeuroPulseMotion` — not left to screen-level judgement.

---

## Clinical basis

Adults with ADHD exhibit:
- **Executive dysfunction** — difficulty initiating and switching tasks
- **Working memory deficits** — easily overloaded by dense information
- **Rejection-sensitive dysphoria** — heightened stress response to alarming signals
- **Vestibular sensitivity** — motion/animation can cause distraction or nausea
- **Reading difficulties** — character disambiguation issues (b/d, p/q, 1/l/I)

Each constraint below maps to one or more of these factors.

---

## Constraints and rationale

### Colour budget — max 2 active colours per screen

| Token | Colour | Use |
|---|---|---|
| `primary` | Dusk Periwinkle `#5B64E0` | One CTA per screen, active nav |
| `signal` | Attention Amber `#FFB224` | Errors, warnings, time pressure — replaces red |
| `dopamineSuccess` | Teal `#10B981` | Transient only (2s max), task completion |

`secondaryAction` (#FF8225) was evaluated and **dropped** — orange + periwinkle
creates two competing focal points, fragmenting attention.

**Never use `Color.Red`** — red triggers stress/threat responses in ADHD users.
Amber conveys urgency without the anxiety spike. `NeuroPulseColors.signal` maps
to Material3 `error` — ensuring all default Material error states use amber.

### Typography — Atkinson Hyperlegible, sp units only

- Purpose-designed for character disambiguation (b/d, p/q, 1/l/I)
- All sizes in `sp` — respects system font scale (WCAG 1.4.4)
- Minimum body size 18sp (Material default is 16sp)
- No `FontWeight.Light` anywhere — low-weight text at small sizes fails contrast
- Line height 1.5em on body — WCAG AAA readability standard
- `NumericalTextStyle` with `tnum` for timer/biometric readouts — prevents jitter

### Spacing — breathing room over density

- `touchTarget` = 48dp minimum (WCAG 2.5.5)
- `globalPadding` = 24dp screen edges
- `sectionSpacing` = 32dp between major sections
- `elementBuffer` = 16dp (increased from spec 12dp — 12dp was too visually tight)
- `cornerRadius` = 16dp — rounded corners reduce visual aggression

### Motion — reduce-motion gated, no pulsing

- All animations check `LocalReduceMotion.current` — collapse to 0ms if enabled
- `microInteractionSpec` = 200ms (faster than Material 250ms to reduce distraction)
- `transitionSpec` = 300ms — longer feels more considered and calm
- `gentleExpandSpec` (1.0 → 1.03 → 1.0) replaces pulse — pulsing is anxiety-inducing
- `dopamineSuccessSpec` — spring, plays once, Composable removes colour after 2000ms

### One CTA per screen

Every screen has a single primary action button. Secondary actions are text links
or icon buttons — never competing filled buttons. ADHD decision paralysis is a
real barrier; reducing choice at action points removes it.

### Notification graduation — never a single jarring alert

```
Step 1 — gentle (IMPORTANCE_DEFAULT): raise-to-wake card, no sound
Step 2 — firm (IMPORTANCE_HIGH): gentle vibration + card
Step 3 — full (IMPORTANCE_MAX): NEVER used — clinically contraindicated
```

### Task list cap — 5 visible items

`MorningPlanScreen` shows a maximum of 5 tasks. Additional tasks are hidden behind
a "show more" affordance. A list of 12 tasks induces overwhelm; 5 is the
working-memory-safe ceiling for adults with ADHD.

---

## Rules for developers (enforced by CLAUDE.md)

- Never use `Color.Red` — use `NeuroPulseTheme.colors.signal`
- Never hardcode colours, dimensions, or font sizes — import from theme
- All text must use `sp` units — never `dp` for font sizes
- All touch targets `≥ 48dp` — use `NeuroPulseSpacing.touchTarget`
- All interactive elements need `contentDescription`
- Every Composable with animation must check `NeuroPulseTheme.reduceMotion`
- Never add a new colour without updating `NeuroPulseColors.kt` and this ADR
- Task lists capped at 5 visible items with show-more affordance
