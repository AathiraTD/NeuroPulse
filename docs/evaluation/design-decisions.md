# NeuroPulse — Design Decisions Log

Running log of every UI/UX decision made during development with rationale
and clinical reference. Feeds directly into the demo PPT and proves design
thinking process for CMP6213 assessment.

CLAUDE: Append a new entry here for every UI/UX decision made during a session.
Format: `## DD-XXX — Title` with Date, Phase, Screen, Decision, Rationale, Clinical ref.

---

## DD-001 — Colour token system with 2-active-colour budget

**Date:** 2026-03 | **Phase:** 0 — Scaffold  
**Screen:** Global (all screens)

**Decision:** Maximum 2 active colours visible on any screen at any time:
`primary` (Dusk Periwinkle) for the single CTA, and `signal` (Attention Amber)
for any warning/error state. `dopamineSuccess` (teal) is transient — 2 seconds
maximum, never persistent. `secondaryAction` (#FF8225) was designed and then
dropped before implementation.

**Rationale:** Competing colour signals increase attentional load — the exact
problem NeuroPulse exists to reduce. Orange (#FF8225) + periwinkle creates two
focal points of equal visual weight, fragmenting attention during a screen scan.

**Clinical reference:** Healey & Enns (2012) — salience competition in
multi-target visual search. Reduced colour set lowers salience competition,
keeping attention anchored to the single CTA.

---

## DD-002 — Amber replaces red for all error and warning states

**Date:** 2026-03 | **Phase:** 0 — Scaffold  
**Screen:** Global (all error states, `NeuroPulseColors.signal`)

**Decision:** `Color.Red` is never used anywhere in NeuroPulse. All error,
warning, and time-pressure states use `signal` (Attention Amber `#FFB224`).
This is enforced at the Material3 level — `NeuroPulseColors.toMaterialColorScheme()`
maps Material's `error` slot to amber, so all default error components inherit it.

**Rationale:** Red is associated with threat and danger. Adults with ADHD often
have Rejection Sensitive Dysphoria (RSD) — a heightened emotional response to
negative stimuli. A red error state in a scheduling app can trigger an
avoidance response, causing the user to abandon the task entirely.

**Clinical reference:** Dodson (2016) — Rejection Sensitive Dysphoria in ADHD.
Amber conveys urgency and attention without the threat connotation of red.

---

## DD-003 — Atkinson Hyperlegible selected over system sans-serif

**Date:** 2026-03 | **Phase:** 0 — Scaffold  
**Screen:** Global (all text, `NeuroPulseTypography`)

**Decision:** Atkinson Hyperlegible (Braille Institute, SIL OFL) is used for
all text in NeuroPulse. System default (Roboto) was the alternative.
`FontWeight.Light` is deliberately excluded from the font family declaration.

**Rationale:** Atkinson Hyperlegible was purpose-designed to maximise character
disambiguation for low-vision and reading-difficulty users. The characters most
commonly confused under reading difficulty (b/d, p/q, 1/l/I, 0/O) have
distinct letterforms — reducing visual processing effort during rapid scanning.
Light weight at small sizes drops below 4.5:1 contrast ratio on Ghost Cloud
background — excluded to prevent any text falling below WCAG AA minimum.

**Clinical reference:** Braille Institute (2019) — Atkinson Hyperlegible design
specification. Reading difficulties co-occur with ADHD in ~30-50% of cases
(Willcutt & Pennington, 2000).

---
<!-- Append new entries below this line in format DD-XXX -->

---

## DD-004 — Splash implemented as a Compose NavGraph destination

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** SplashScreen

**Decision:** The 4-phase animated splash is a Compose composable at `NavDestinations.SPLASH`,
not a dedicated `SplashActivity` or the Android 12+ Splash Screen API window background.

**Rationale:** The animation requires independent control of Canvas arc rotation, sweep angle,
Dp-based radius growth, and simultaneous overlay alpha — none of which are achievable on a
window background (single-frame icon only). A separate `SplashActivity` adds a second Activity
lifecycle, contradicting the single-Activity architecture. The Compose destination gives full
`DrawScope` and `animateFloatAsState` control within the established NavGraph.

**Trade-off:** The app shows a blank `NeuroPulseTheme` surface for the auth gate resolution
period (~50ms DataStore read). Mitigated by: (1) always routing to SPLASH first regardless of
auth state, so the auth check runs concurrently behind the 2400ms animation window; (2) the
resolution is a local DataStore read — no network required on first check.

---

## DD-005 — Google Sign-In as the sole primary CTA on LoginScreen

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** LoginScreen

**Decision:** The Google Sign-In button is the full-width primary CTA in `colors.primary`.
Email/password sign-in is a secondary option (outlined button). Account creation is a `TextButton`
link only — not a CTA. One primary action visible at any time (ADR-005).

**Rationale:** Password recall is a documented friction point for ADHD users. Working memory
deficits (Barkley, 2015) impair credential recall under any cognitive load — and attempting
to use an app is itself a cognitive event. One-tap Google authentication eliminates the recall
step entirely. Presenting Google first (primary colour, full width) signals the preferred path
without removing the email fallback for users without Google accounts.

**Clinical reference:** Barkley (2015) — executive function deficits in ADHD impair working
memory, including password and credential recall. Reducing authentication friction increases
consistent app engagement and reduces abandonment at first touch.

---

## DD-006 — Progressive disclosure of password field on LoginScreen

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** LoginScreen

**Decision:** The password field is hidden behind `AnimatedVisibility(visible = email.isNotBlank())`.
It only appears after the user has typed at least one character in the email field.
The reveal uses a `slideInVertically` + `fadeIn` enter transition (collapses to instant fade
when `LocalReduceMotion` is true).

**Rationale:** Presenting both email and password fields simultaneously adds visual complexity at
the highest-friction moment (first app interaction). For ADHD users, two empty fields to fill
creates initiation paralysis — the cognitive barrier of "where do I start?" prevents engagement.
Revealing the password field after email entry makes the authentication feel like a two-step
conversation rather than a form, reducing perceived complexity at each step.

---

## DD-007 — Session persistence with no auto-logout

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** Global (auth behaviour)

**Decision:** Firebase Auth persistence is left at its Android default (LOCAL — persists across
app restarts). The Firebase UID is also cached in DataStore for the auth gate check on cold
start. No session timeout or auto-logout timer is implemented.

**Rationale:** Auto-logout forces ADHD users to re-authenticate, re-recall passwords, and
re-orient themselves to the app on every session — all high-friction events that lead to
abandonment. NeuroPulse is a personal companion app, not a financial or medical record system,
so the security trade-off (reduced protection on shared devices) is acceptable. Biometric
authentication provides a lightweight second factor for device-sharing scenarios without
imposing the full credential recall burden.

**Trade-off:** Reduced security on physically shared devices. Acceptable for a personal ADHD
companion app. Explicitly documented here as a conscious decision, not an oversight.

---

## DD-008 — Single LaunchedEffect state machine for SplashScreen animation

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** SplashScreen

**Decision:** All four splash phase transitions are driven by a single `LaunchedEffect(Unit)`
coroutine that advances one `MutableState<SplashPhase>` through sequential `delay()` calls.
Rejected approach: multiple `LaunchedEffect(phase)` blocks that fire reactively on phase changes.

**Rationale:** A single coroutine with sequential delays is easier to reason about, easier to
cancel (single job), and avoids the race conditions that multiple reactive effects introduce
when rapid state changes trigger several effects simultaneously. The `reduceMotion` early-exit
(`if (reduceMotion) { onSplashComplete(); return@LaunchedEffect }`) is a single guard at the
top of the coroutine — clean, obvious, and impossible to miss. All animated values derive from
the single `phase` state, maintaining one source of truth.

---

## DD-009 — Logo repositioning via BoxWithConstraints absolute offset (not SharedTransitionLayout)

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** SplashScreen

**Decision:** The logo repositioning from screen centre → top-left during Phase 3 is implemented
using `animateDpAsState` with absolute `Modifier.offset` values derived from `BoxWithConstraints`
measurements. `SharedTransitionLayout` (Compose 1.7+) was considered and rejected.

**Rationale:** `SharedTransitionLayout` is designed for cross-navigation shared element
transitions — it requires an `AnimatedContentScope` that exists only within a `NavHost`
`composable {}` block or inside `AnimatedContent`. The logo repositioning happens entirely
within a single composable (`SplashScreen`) with no navigation event — there is no scope
for `SharedTransitionLayout` to operate in. Using it here would require wrapping the entire
screen in `AnimatedContent`, adding unnecessary complexity and a recomposition surface.
`BoxWithConstraints` + `animateDpAsState` achieves the same visual result with idiomatic
Compose APIs and no additional dependency.

---

## DD-010 — Biometric prompt placement: one-time popup over Home (Option B)

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** HomeScreen (popup on first visit)

**Decision:** Biometric setup is offered as a dismissable popup on the first Home visit.
Rejected alternatives: (A) during registration flow, (C) settings-only discovery.
DataStore keys added: `KEY_BIOMETRIC_PROMPT_SHOWN: Boolean`, `KEY_BIOMETRIC_ENABLED: Boolean`.

**Rationale:** Registration is the wrong moment — the user is focused on completing sign-in
and has not yet seen the app's value, so a biometric setup prompt is contextually meaningless.
Settings-only placement requires self-initiation, which is consistently low in ADHD adults
(Barkley, 2015 — executive function deficits reduce voluntary task initiation for non-urgent
items). The post-Home popup fires at the first moment the user has seen the app and has a
concrete reason to care about "quick access next time". Single CTA, dismissable with "Maybe
later" — no guilt, no pressure, accessible again from Account Settings.

**Clinical reference:** Barkley (2015) — low self-initiation in ADHD means opt-in features
must be proactively surfaced in context, not left for self-discovery in settings menus.

---

## DD-011 — Privacy consent screen: between persona selection and Home, first-launch only

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** PrivacyConsentScreen (new, first-launch only)

**Decision:** Dedicated privacy consent screen sits between PersonaSelectionScreen and
HomeScreen, shown only on first launch. Two specific opt-in checkboxes (not a single wall
of legal text). "Read full privacy policy" link opens the browser — present for compliance,
not expected to be read by most users. DataStore key: `KEY_PRIVACY_ACCEPTED: Boolean`.

**Rationale:** Consent shown before sign-in is meaningless — the user has no idea what they
are agreeing to access. Consent shown after persona selection is contextually meaningful —
the user has committed to the product and understands the app's purpose. ADHD-appropriate
format uses two concrete, specific statements ("Health data stays on device", "Briefing
notifications required") rather than a legal summary, reducing cognitive load at a
trust-building moment. The gate on `Continue` (both checkboxes required) ensures explicit
informed consent for both data use and notifications.

---

## DD-012 — SSO strategy: Google/Yahoo/Microsoft/Apple as equal-weight options

**Date:** 2026-04 | **Phase:** 1b — Auth extension
**Screen:** LoginScreen (redesigned)

**Decision:** Four SSO providers (Google, Yahoo, Microsoft, Apple) presented as equal-weight
outlined buttons after a tab toggle ("Sign in" / "Create account"). Email/password placed
below an "or" divider as the fallback path. No single provider is visually primary — all
four perform the same Firebase auth action.

**Rationale:** The reference layout (Immersia.io pattern, reviewed 2026-04) validates
equal-weight SSO buttons as a familiar, low-friction convention. Multiple providers removes
the assumption that every user has a Google account. The tab toggle ("Sign in" / "Create
account") on a single screen avoids maintaining two separate composables while keeping the
layout identical for both modes — the ViewModel knows which Firebase call to make based on
the active mode.

**Implementation:** Google via `CredentialManager` API; Yahoo, Microsoft, Apple via
`OAuthProvider.newBuilder("provider.com")` — all Firebase-managed, no extra SDKs required.

---

## DD-013 — Persona stored values retained as "MARCUS" / "ZOE"

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** PersonaSelectionScreen

**Decision:** `UserPreferences.PERSONA_MARCUS = "MARCUS"` and `PERSONA_ZOE = "ZOE"` retained
as DataStore values. Alternative considered: `"TIME_SENSITIVE"` / `"HYPERFOCUS"` as
self-describing clinical labels.

**Rationale:** Marcus (time-blindness dominant) and Zoe (hyperfocus dominant) are the
established narrative archetypes across all dissertation documentation, the project plan,
and the ADRs. Changing the stored value to clinical labels would create a semantic mismatch
with assessment materials that reference these characters by name. The stored value is an
opaque DataStore key — it only needs to be consistent between write and read, not
self-describing. The clinical meaning is carried by the domain layer documentation, not the
string constant.

---

## DD-014 — Session restore: Mode A — auto-restore default, biometric as optional lock

**Date:** 2026-04 | **Phase:** 1 — Auth & Onboarding
**Screen:** Global (auth behaviour)

**Decision:** Default behaviour on app relaunch with a valid Firebase session: silent
auto-restore directly to Home. No biometric gate by default. Biometric is an optional
"lock on reopen" setting the user enables via the first-Home popup (DD-010). The auth gate
(`AuthGateViewModel`) routes to `BIOMETRIC_LOCK` only when `biometric_enabled = true` in
DataStore.

**Rationale:** Every additional authentication step on relaunch is a friction event. For
ADHD users, repeated friction on app open creates an avoidance pattern — the app gets
opened less frequently, which defeats the purpose of a daily companion. Auto-restore
prioritises consistent engagement over security. Biometric opt-in serves privacy-conscious
users on shared devices without penalising the majority who use the app on a personal device.

**Trade-off:** Unprotected session on shared devices for users who have not opted into
biometric. Acceptable for a personal ADHD companion app. Explicitly documented here as a
conscious decision.

---

## DD-015 — Anonymous/guest mode via Firebase Anonymous Auth

**Date:** 2026-04 | **Phase:** 1b — Auth extension
**Screen:** LoginScreen

**Decision:** A "Try without signing in" `TextButton` on LoginScreen initiates
`FirebaseAuth.signInAnonymously()`. Anonymous users access the full Phase 1 UI. Account
linking (anonymous → Google/email) is available from Account Settings at any time.
Anonymous session UID is NOT persisted to DataStore — the user sees the login screen again
on the next cold start until they link an account.

**Rationale:** Sign-up friction is the single largest ADHD abandonment point at first
launch. Asking a user to register before they have experienced any value is a known
conversion anti-pattern. Anonymous mode lets the user see the morning plan and receive a
demo briefing before committing. Once they have seen the app's value proposition in action,
the decision to register is informed and the conversion rate is meaningfully higher.

**Trade-off:** Anonymous users who uninstall without linking an account permanently lose
any data entered. Disclosed in an in-session banner: "Your progress won't be saved until
you create an account."

---

## DD-016 — Create Account screen: form-based registration with progressive validation

**Date:** 2026-04 | **Phase:** 1b — Auth extension
**Screen:** CreateAccountScreen

**Decision:** Full registration form with three sections (Personal Details, Sign-in Details,
Consent) presented on a single scrollable screen. The CREATE ACCOUNT button is disabled
until all fields are valid and consent is checked. Helper text below the button explains why
it may be disabled. Social auth alternatives (Google, Github, Microsoft, Apple) in a 2×2
grid below the form. Password fields include visibility toggles.

**Rationale:** The Figma design (node 2:373) establishes the visual layout. ADHD adaptations
applied: (1) disabled-state helper text reduces frustration by explaining what's missing —
ADHD users may not scan back to find the empty field; (2) password visibility toggles reduce
re-typing anxiety common with working memory deficits; (3) amber error messages (never red)
for all validation failures; (4) social auth prominently available as a lower-friction
alternative for users who find form completion overwhelming.

**Clinical reference:** Barkley (2015) — working memory deficits in ADHD impair multi-step
form completion. Progressive validation feedback (disabled → enabled as fields complete)
provides micro-dopamine rewards for each field filled, maintaining engagement through the
full form.

---

## DD-017 — Login screen: Create Account promoted to outlined button

**Date:** 2026-04 | **Phase:** 1b — Auth extension
**Screen:** LoginScreen

**Decision:** "Create Account" was promoted from a `TextButton` link to an `OutlinedButton`
at the bottom of the login screen, matching the Figma design (node 2:350). "Can't log in?"
helper link added above it. Guest path ("Try without signing in") remains as a subtle text
link below.

**Rationale:** The Figma design shows Create Account as a visually distinct action — an
outlined button with a border. This is more discoverable than a text link while still
maintaining the one-primary-CTA rule (Google Sign-In remains the only filled button).
The visual hierarchy is preserved: filled primary → outlined secondary → text link tertiary.
