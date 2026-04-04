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
