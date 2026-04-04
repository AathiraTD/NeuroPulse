# NeuroPulse — Extension Points

This document maps exactly which files to change (and which to leave untouched)
when extending NeuroPulse with new capabilities. It is updated whenever a new
strategy, repository implementation, or delivery surface is added.

CLAUDE: Check this file before implementing any new interface. Update it after.

---

## 1 — Adding a new TriggerType (new use case)

**Touch these files:**

| File | Change |
|---|---|
| `domain/model/TriggerType.kt` | Add new enum value |
| `data/ai/strategy/` | Create new `BriefingStrategy` implementation |
| `data/di/BriefingModule.kt` | Register new strategy in the Hilt multi-binding map |
| `data/delivery/DeliveryAgent.kt` | Add routing case for new TriggerType if different surface |
| `app/src/main/AndroidManifest.xml` | Add any new permissions required |
| `docs/adr/003-rag-over-finetuning.md` | Document new strategy's prompt anonymisation approach |
| This file | Add entry here |

**Do NOT touch:**
- `ContextEngine.kt` — strategy selection is map-driven, no switch statement to update
- `DeliveryAgent.kt` — only if the new trigger uses the same delivery surface
- `BriefingRepository.kt` — interface is trigger-agnostic
- Any existing `BriefingStrategy` implementations

---

## 2 — Adding a new DeliveryStrategy (new delivery surface)

Example: upgrading from `PhoneDeliveryStrategy` (NotificationManager) to
`WearDeliveryStrategy` (MessageClient + haptic + tile).

**Touch these files:**

| File | Change |
|---|---|
| `data/delivery/` | Create new class implementing `DeliveryStrategy` |
| `data/di/DeliveryModule.kt` | Rebind `DeliveryStrategy` to new implementation |
| `docs/adr/004-notificationmanager-delivery.md` | Update status |
| This file | Update entry here |

**Do NOT touch:**
- `DeliveryAgent.kt` — receives `DeliveryStrategy` via injection, agnostic to impl
- `ContextEngine.kt` — upstream of delivery, unaffected
- Any `BriefingStrategy` — upstream of delivery, unaffected

---

## 3 — Upgrading RAG retrieval (FTS5 → AllMiniLM cosine similarity)

**Touch these files:**

| File | Change |
|---|---|
| `data/local/dao/CaptureDao.kt` | Replace FTS5 `findRelevant()` with vector cosine query |
| `data/local/entity/CaptureEntity.kt` | Add `embedding: FloatArray` BLOB column |
| `data/local/NeuroPulseDatabase.kt` | Bump schema version + add migration |
| `data/local/` | Add embedding generation step on capture save |
| `gradle/libs.versions.toml` | Add AllMiniLM dependency if using a library |
| `docs/adr/003-rag-over-finetuning.md` | Update retrieval section |
| This file | Update entry here |

**Do NOT touch:**
- `CaptureRepository.kt` interface — `findRelevant()` signature is identical
- `ContextEngine.kt` — calls `CaptureRepository.findRelevant()`, agnostic to impl
- Any `BriefingStrategy` — prompt assembly is unaffected

---

## 4 — Swapping Gemini Flash → Gemini Nano (on-device inference)

**Touch these files:**

| File | Change |
|---|---|
| `data/di/BriefingModule.kt` | Rebind `ChatLanguageModel` to Gemini Nano client |
| `gradle/libs.versions.toml` | Add `langchain4j-google-ai-gemini-nano` dependency |
| `docs/adr/003-rag-over-finetuning.md` | Update LLM provider section |
| This file | Update entry here |

**Do NOT touch:**
- `BriefingRepository.kt` interface — `runInference()` signature unchanged
- `GeminiBriefingRepository.kt` implementation — depends on `ChatLanguageModel`
  interface, not concrete Gemini class
- `ContextEngine.kt` — calls `BriefingRepository`, agnostic to model

---

## 5 — Adding a new Room entity

**Touch these files:**

| File | Change |
|---|---|
| `data/local/entity/` | Create new `@Entity` data class |
| `data/local/dao/` | Create new `@Dao` interface |
| `data/local/NeuroPulseDatabase.kt` | Add entity to `entities = [...]`, add DAO abstract fun, bump version |
| `data/di/DatabaseModule.kt` | Expose new DAO as Hilt singleton |
| `domain/repository/` | Add or extend repository interface if new domain concept |

---

## Current extension-point implementations

| Interface | PoC Implementation | Location |
|---|---|---|
| `BriefingStrategy` | `MorningPlanStrategy`, `PreEventStrategy`, `HyperfocusStrategy`, `StressGroundingStrategy` | `data/ai/strategy/` — Phase 4 |
| `DeliveryStrategy` | `PhoneDeliveryStrategy` | `data/delivery/` — Phase 5 |
| `ChatLanguageModel` | Gemini Flash via LangChain4j | `data/di/BriefingModule.kt` — Phase 4 |
| `DayPlanRepository` | `RoomDayPlanRepository` | `data/local/` — Phase 2 |
| `PhysioRepository` | `HealthConnectPhysioRepository` | `data/health/` — Phase 3 |
| `CaptureRepository` | `RoomCaptureRepository` | `data/local/` — Phase 4 |
| `BriefingRepository` | `GeminiBriefingRepository` | `data/ai/` — Phase 4 |
