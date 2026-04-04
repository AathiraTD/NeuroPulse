# ADR-003: RAG over Fine-Tuning

**Status:** Accepted  
**Date:** 2026-03  
**Full trade-off analysis:** `docs/decision_log.md` D-002, D-003, D-004

---

## Decision

NeuroPulse uses Retrieval-Augmented Generation (RAG) with a frozen Gemini Flash
model for personalised briefings. No model fine-tuning or training is performed.
FTS5 BM25 keyword search is the retrieval mechanism for the PoC.

---

## Context

Fine-tuning would require:
1. A GPU for training — unavailable for PoC
2. Sending user personal data to a training pipeline — GDPR Art. 9 violation
3. Retraining when user context changes — impractical for a single user

RAG avoids all three: the model weights remain on Google's infrastructure
(Gemini Flash API), and the user's personal context (captures, events, physio
summaries) is injected at inference time in the prompt. The model never "sees"
raw biometric values — only anonymised summaries.

---

## How the RAG pipeline works

```
Trigger fires
    ↓
ContextEngine selects BriefingStrategy (by TriggerType)
    ↓
BriefingStrategy.buildPrompt():
    1. CaptureRepository.findRelevant(query, limit=5)  ← FTS5 BM25 retrieval
    2. Anonymise PhysioContext → summary string
    3. Assemble prompt (context + task + system instructions)
    ↓
BriefingRepository.runInference(prompt, triggerType)  ← Gemini Flash API
    ↓
BriefingPayload returned to DeliveryAgent
```

---

## Retrieval: FTS5 BM25 (PoC) → AllMiniLM cosine (v2)

| Approach | PoC | v2 |
|---|---|---|
| Method | FTS5 keyword (BM25 ranking) | AllMiniLM-L6 cosine similarity |
| Storage | Room FTS5 virtual table | Embedding vectors in Room BLOB column |
| Why PoC | No embedding model needed, no extra dependencies | Better semantic match for captures |
| Swap cost | Replace `CaptureDao.findRelevant()` only | One DAO change + embedding generation step |

The `CaptureRepository` interface abstracts this swap — `findRelevant()` signature
is identical regardless of retrieval method underneath.

---

## Rules for developers

- `BriefingStrategy.buildPrompt()` must **never** include raw HR, HRV, or sleep values
  in its return string — use anonymised summaries only (e.g. `"sleep quality: good"`)
- `BriefingRepository.runInference()` receives a `String` prompt — not a `PhysioContext`
- Adding a new trigger type requires a new `BriefingStrategy` implementation —
  register it in `BriefingModule` and document in `extension-points.md`
- `CaptureDao.findRelevant()` limit defaults to 5 — do not increase for the PoC,
  Gemini Flash context window is not the constraint but prompt cost is
- `BriefingCaptureEntity` junction table records which captures contributed to each
  briefing — this feeds the penalty scoring system in `BriefingLogDao.getPenaltyScores()`
