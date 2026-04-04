# NeuroPulse — Decision Log

**Project:** NeuroPulse — A Neuroadaptive Companion for Adults Living with ADHD
**Student:** Aathira T Dev (S21161041) | BCU CMP6213
**Last Updated:** _(update this field whenever a new decision is added)_

> This log captures significant design and implementation decisions made during the NeuroPulse project. Each entry records the options considered, their trade-offs, the final decision, and the rationale. Routine implementation choices not involving meaningful trade-offs are excluded.

---

## Decision Index

| ID | Title | Phase | Date | Status |
|----|-------|-------|------|--------|
| D-001 | Local-first data persistence strategy | Design | Mar 2026 | Decided |
| D-002 | AI inference approach — training vs. RAG | Design | Mar 2026 | Decided |
| D-003 | LLM provider — Gemini Flash vs. Gemini Nano vs. direct API | Design | Mar 2026 | Decided |
| D-004 | Embedding and retrieval strategy — FTS5 vs. cosine similarity | Design | Mar 2026 | Decided |
| D-005 | Wearable delivery channel — MessageClient vs. ChannelClient vs. direct Wear OS notification | Design | Mar 2026 | Decided |
| D-006 | Background scheduling — WorkManager vs. AlarmManager | Design | Mar 2026 | Decided |
| D-007 | Architecture pattern — Clean Architecture + MVVM vs. alternatives | Design | Mar 2026 | Decided |
| D-008 | UI framework — Jetpack Compose vs. XML Views | Design | Mar 2026 | Decided |
| D-009 | Dependency injection — Hilt vs. Koin | Design | Mar 2026 | Decided |
| D-010 | Physiological sensing approach — passive vs. active polling | Design | Mar 2026 | Decided |
| D-011 | ADHD feature scope — Time Blindness + Hyperfocus (PoC only) | Design | Mar 2026 | Decided |
| D-012 | Briefing delivery surface — wrist-first vs. phone-first | Design | Mar 2026 | Decided |
| D-013 | GDPR compliance strategy — on-device vs. cloud data | Design | Mar 2026 | Decided |
| D-014 | Agent pipeline pattern — Pipe & Filter vs. monolithic service | Design | Mar 2026 | Decided |
| D-015 | Hyperfocus detection signal — stillness + HR vs. app usage time | Design | Mar 2026 | Decided |

---

## Decision Records

---

### D-001 — Local-First Data Persistence Strategy

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse must store physiological snapshots, calendar events, day plans, briefing logs, and user captures. Health and behavioural data falls under GDPR Art. 9 special category — highest protection tier. The system must also operate fully offline as a core constraint.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Room (SQLite)** | Android-native relational ORM | Kotlin Flow native, FTS5 full-text search, compile-time query validation, migration support, fully offline, on-device | Multi-table join complexity; schema migrations needed as app evolves |
| **SQLDelight** | KMM SQL-first library | Explicit SQL control, Kotlin Multiplatform ready (future iOS expansion) | Less Android-native than Room; smaller community; migration tooling less mature |
| **Firebase Firestore** | Cloud-hosted NoSQL | Real-time sync, multi-device, flexible document model | Cloud-hosted — violates privacy-first requirement; GDPR Art. 9 complexity; ongoing cost; requires internet |
| **Realm (MongoDB)** | On-device object DB | Object-oriented, fast, on-device capable, optional cloud sync | Larger footprint; weaker Kotlin Flow integration; optional cloud sync re-introduces privacy risk |
| **DataStore (Prefs)** | Jetpack key-value store | Lightweight, coroutine-native, type-safe proto variant | Not a relational DB; cannot store captures, events, or physio data at required complexity |

**Trade-offs**
- Room requires schema migrations for evolution; acceptable given the benefit of FTS5, Kotlin Flow integration, and zero-network dependency.
- Firebase was attractive for real-time sync and multi-device but fundamentally incompatible with GDPR Art. 9 compliance for health data.
- SQLDelight offers future iOS portability but adds tooling complexity not justified at PoC stage.

**Decision:** **Room (SQLite)** as the single on-device relational store. **DataStore (Prefs)** used supplementarily for user settings only.

**Rationale:** Room is the only option that simultaneously satisfies all three non-negotiables: privacy-first (on-device), offline operation, and query flexibility (FTS5, time, person, event, physiological state). Firebase's eventual consistency model is architecturally incompatible with offline triggers. The principle adopted: *"The intelligence is cloud-assisted but the data is device-sovereign."*

---

### D-002 — AI Inference Approach — Training vs. RAG

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse needs AI-generated contextual briefings personalised to the user. The question is how to inject user-specific context into the model without requiring data to leave the device for training.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Fine-tuning** | Train or fine-tune a model on user data | Highly personalised | Requires GPU; personal data sent to cloud; no training data at install; impractical for PoC |
| **Prompt engineering only** | Static prompt with no user data | Simple, fast, private | Generic outputs; no personalisation; fails to address the tool-friction problem |
| **RAG — Retrieval-Augmented Generation** | Retrieve user captures at inference time; inject as context into frozen model prompt | No model training; no GPU; no population-level data; personal context injected at inference only; model weights are Google's, context is the user's | Retrieval quality depends on capture history volume; cold-start problem on first use |

**Trade-offs**
- Fine-tuning would produce optimal personalisation but is incompatible with on-device privacy requirements and is infeasible without GPU.
- Prompt engineering alone cannot address the personalisation requirement (knowing Sarah focuses on cost metrics for this meeting).
- RAG provides personalisation without training by retrieving the user's own stored captures at inference time.

**Decision:** **RAG (Retrieval-Augmented Generation)** using Room's FTS5 BM25 search for retrieval at the PoC stage.

**Rationale:** RAG injects personal data at inference time — no training, no fine-tuning, no GPU required. The model weights are Google's; the context is the user's. This satisfies both the personalisation requirement and the GDPR Art. 9 constraint, as only anonymised summaries (not raw HR/HRV data) are sent in the prompt. Cold-start is mitigated by `DatabaseSeeder` pre-populated captures at onboarding.

---

### D-003 — LLM Provider — Gemini Flash vs. Gemini Nano vs. Direct Gemini API

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse requires an LLM for briefing generation. Options differ on connectivity, privacy, cost, and build complexity.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Gemini Flash API (cloud)** | Cloud LLM via Gemini API, wrapped by LangChain4j | Free at PoC volumes; no on-device model weight (80 MB); buildable in 2 days via LangChain4j | Requires internet connectivity; conflicts with offline-first claim without fallback |
| **Gemini Nano (on-device)** | Android AICore on-device inference | Fully offline; raw data never leaves device; no API cost | Android AICore not universally available; adds build complexity; out of scope for PoC |
| **Direct Gemini API (no LangChain4j)** | Raw REST calls to Gemini Flash | Removes LangChain4j dependency | Loses model-agnostic interface; architectural change required to swap later |

**Trade-offs**
- Gemini Flash is the pragmatic choice for PoC given free quota and speed of integration, but requires a deterministic fallback for offline scenarios.
- Gemini Nano is the ideal end-state but depends on Android AICore availability — not yet universally available on target devices.
- LangChain4j's `ChatLanguageModel` abstraction means the binding can be swapped from Gemini Flash to Gemini Nano in v2 with a single DI change — zero architectural change.

**Decision:** **Gemini Flash API** via **LangChain4j** (`ChatLanguageModel` interface) for PoC. Gemini Nano (Android AICore) targeted for v2 as a drop-in swap behind the same interface.

**Rationale:** LangChain4j provides a model-agnostic interface that future-proofs the on-device migration. Gemini Flash is free at PoC volumes and buildable quickly. A deterministic fallback (PhysioContext + FTS5 retrieval, no LLM) is implemented in `BriefingRepository.runInference()` to handle connectivity failures without breaking the offline-first architecture claim.

---

### D-004 — Embedding and Retrieval Strategy — FTS5 BM25 vs. Cosine Similarity

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** RAG retrieval requires ranking captures by relevance to a briefing trigger. Two main approaches are available: keyword-based (FTS5 BM25) and semantic (cosine similarity over dense embeddings).

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **FTS5 BM25 (keyword)** | Room's built-in full-text search ranking | Built into Room; zero extra dependency; offline; buildable in 2 days; compile-time query validation | Keyword-only — misses semantic synonyms; less nuanced than embedding similarity |
| **AllMiniLM-L6-v2 embeddings + cosine similarity** | 80 MB on-device embedding model; in-memory vector store | Semantic retrieval; captures synonyms and related concepts | 80 MB model adds startup latency; in-memory store lost on process death; out of scope for PoC build time |
| **LangChain4j EmbeddingStore** | LangChain4j managed embedding pipeline | Model-agnostic; typed outputs | Adds dependency; latency on embedding generation; not needed at PoC volumes |

**Trade-offs**
- FTS5 is good enough for PoC given the capture vocabulary is relatively narrow (personal notes, not diverse corpora).
- AllMiniLM embeddings would improve retrieval quality but add 80 MB and process-death volatility that conflicts with the offline-first constraint.
- The same `CaptureRepository.findRelevant()` interface is used regardless of retrieval backend — FTS5 for PoC, cosine similarity in v2 via implementation swap.

**Decision:** **FTS5 BM25** for PoC retrieval. AllMiniLM-L6-v2 + cosine similarity scoped for v2 as a behind-interface swap.

**Rationale:** FTS5 is available natively in Room, requires no extra dependency, is fully offline, and is buildable in 2 days. The repository interface contract ensures zero upstream changes when the embedding upgrade is introduced. Progressive enhancement without architectural debt.

---

### D-005 — Wearable Delivery Channel — MessageClient vs. ChannelClient vs. Direct Wear OS Notification

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse must deliver briefing payloads and haptic alerts from the phone logic layer to the Wear OS companion. Three Wearable Data Layer API channels are available with different characteristics.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **MessageClient** | One-shot message up to 100 KB | Low latency; ideal for real-time alerts; bidirectional | 100 KB payload limit; fire-and-forget (no delivery guarantee without ack) |
| **ChannelClient** | Streaming channel for larger payloads | Handles briefing payloads > 100 KB if needed; reliable stream | Higher overhead; latency on BLE reconnect; overkill for short briefing cards |
| **Direct Wear OS notification** | Phone sends notification forwarded to watch | Simple; no Data Layer needed | Less control over haptic pattern; not ADHD-specific; generic interruption |

**Trade-offs**
- `MessageClient` is ideal for time-sensitive micro-interventions (UC2, UC3) due to low latency.
- `ChannelClient` is appropriate for larger payloads (full briefing text) but adds BLE reconnect latency.
- Direct Wear OS notifications lose the ADHD-specific haptic sequencing that distinguishes NeuroPulse from existing tools.

**Decision:** **MessageClient** as the primary delivery mechanism for real-time alerts and haptic triggers. **ChannelClient** as secondary channel for larger briefing payloads. WorkManager window delivery as fallback for Bluetooth disconnection scenarios.

**Rationale:** The primary differentiation of NeuroPulse is wrist-native ADHD-specific haptic delivery — this cannot be replicated by generic Wear OS notification forwarding. `MessageClient` provides the latency needed for UC2 (pre-event, time-sensitive). `ChannelClient` fallback ensures larger payloads (full pre-event briefing) are not truncated. WorkManager window ensures delivery even across Bluetooth reconnects, though with degraded timing precision.

---

### D-006 — Background Scheduling — WorkManager vs. AlarmManager

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse requires background scheduling for two distinct scenarios: (a) periodic physiological snapshot collection and morning plan generation, and (b) precise calendar-proximity triggers for UC2 (pre-event briefing, 15 minutes before event).

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **WorkManager only** | Constraint-aware battery-safe scheduling | Survives process death; Doze mode safe; battery-safe; recommended by Google for deferrable work | 15-minute minimum interval — insufficient for "meeting in 12 min" UC2 scenarios |
| **AlarmManager only** | Exact alarm scheduling (SCHEDULE_EXACT_ALARM) | Exact timing; fires at precise moment | Requires `SCHEDULE_EXACT_ALARM` permission (Android 12+ — high friction for ADHD users); no constraint awareness; battery drain risk |
| **WorkManager + AlarmManager** | WorkManager for periodic work; AlarmManager for exact calendar proximity | Combines battery-safety with exact timing for critical use case | Two scheduling systems to maintain; SCHEDULE_EXACT_ALARM permission required for UC2 only |

**Trade-offs**
- WorkManager alone cannot deliver UC2 (pre-event briefing) with sufficient timing precision — a 15-minute polling window means a meeting starting in 12 minutes may not be caught.
- AlarmManager alone is battery-unsafe for periodic physiological snapshots and violates Doze mode constraints.
- The hybrid approach assigns each scheduler to its correct use case.

**Decision:** **WorkManager** for all periodic background work (physiological snapshots, morning plan, RAG seeding). **AlarmManager** exclusively for exact calendar proximity triggers (UC2). Graceful degradation: if `SCHEDULE_EXACT_ALARM` permission denied, fall back to WorkManager 15-min window with an in-app prompt explaining the degradation.

**Rationale:** WorkManager is battery-safe and constraint-aware — correct for all non-time-critical background work. AlarmManager is necessary only for the UC2 exact-timing scenario where a 15-minute WorkManager gap would break the core product promise. The `SCHEDULE_EXACT_ALARM` permission friction is mitigated by a clear value-proposition prompt at onboarding and graceful degradation that preserves function even when denied.

---

### D-007 — Architecture Pattern — Clean Architecture + MVVM

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse is a complex multi-layer system spanning Android, Wear OS, AI inference, and Health Connect. The architecture must support testability (domain layer testable without Android), extensibility (new trigger types without modifying core logic), and the offline-first constraint.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Clean Architecture + MVVM** | Strict one-way dependency: Presentation → Domain → Data. Domain layer zero Android imports | Domain fully JUnit-testable without emulator; Repository pattern abstracts data sources; MVVM with StateFlow provides reactive UI | Learning curve; more boilerplate for a PoC; layer boundaries require discipline |
| **MVVM without Clean Architecture** | ViewModels + Repositories, no strict domain layer | Less boilerplate; simpler for small apps | Android dependencies leak into business logic; harder to unit test; tightly coupled |
| **MVI (Model-View-Intent)** | Unidirectional data flow with Intent → State → Side Effect | Predictable state; good for complex UIs | Higher complexity; less idiomatic for Wear OS; overkill for PoC scope |

**Trade-offs**
- Clean Architecture adds boilerplate but pays dividends in testability and extensibility — particularly critical here because new `TriggerType` values must be addable without modifying `ContextEngine` core logic (Open-Closed Principle).
- Domain layer with zero Android imports means all use cases are pure JUnit-testable — no emulator required for business logic tests. This is a significant velocity advantage.

**Decision:** **Clean Architecture + MVVM** with strict one-way dependency rule. Domain layer holds zero Android imports.

**Rationale:** The ADHD domain logic (task reordering, physiological state evaluation, briefing strategy selection) must be unit-testable in isolation. Clean Architecture enables this without emulator overhead. The Strategy and Repository patterns — both enabled by Clean Architecture — are required for the NeuroPulse briefing assembly and data source abstraction respectively. Every architectural decision must reduce cognitive burden on the user; patterns that improve testability and predictability are preferred.

---

### D-008 — UI Framework — Jetpack Compose vs. XML Views

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse requires a UI framework for both the Android companion app and the Wear OS companion. The design principle is one-primary-CTA-per-screen to eliminate decision paralysis for ADHD users.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Jetpack Compose** | Declarative, state-driven UI | Single paradigm for phone and watch; reactive state → UI; StateFlow integration; Wear OS Compose native; less boilerplate | Steeper learning curve vs. XML Views; some Wear OS Compose APIs still maturing |
| **XML Views + ViewBinding** | Traditional Android UI | Mature; large community; familiar | Separate paradigm for Wear OS; imperative state management; more boilerplate for reactive data; no Compose Wear OS tile support |

**Trade-offs**
- Compose's reactive state model directly enables the NeuroPulse design principle: `ViewModel` exposes `StateFlow` → Compose reacts automatically. No manual `notifyDataSetChanged()` calls.
- Wear OS tiles are natively Compose — using XML Views would require a mixed paradigm that increases complexity without benefit.
- The steeper learning curve is a one-time cost; the ongoing maintenance benefit of a single declarative paradigm across both surfaces justifies it.

**Decision:** **Jetpack Compose** for both Android and Wear OS surfaces.

**Rationale:** Declarative, single paradigm across phone and watch. `MorningPlanViewModel` and `BriefingViewModel` expose `StateFlow` — Compose reacts automatically, eliminating polling. Wear OS tiles are Compose-native. The cognitive load reduction for ADHD users (no competing equal-weight buttons, one CTA per screen) is more naturally expressed in Compose's composable tree than in XML.

---

### D-009 — Dependency Injection — Hilt vs. Koin

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse requires DI to inject `DayPlanRepository`, `PhysioRepository`, `BriefingRepository`, and `ContextEngine` into use cases and ViewModels, including the ability to swap `ChatLanguageModel` bindings (Gemini Flash → Gemini Nano) without upstream changes.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Hilt (Dagger Hilt)** | Compile-time DI, Jetpack-integrated | Compile-time validation; test fakes via `@TestInstallIn`; Jetpack ViewModel integration; annotation-driven | Annotation processing adds build time |
| **Koin** | Runtime DI, Kotlin-first | Lightweight; no annotation processing; faster builds | Runtime errors instead of compile-time; less Jetpack integration; smaller community |

**Trade-offs**
- Hilt's compile-time validation catches DI graph errors before runtime — critical in a multi-layer system where incorrect bindings could silently degrade the AI pipeline.
- Koin's runtime approach is simpler but trades build-time safety for runtime fragility.
- The single most important DI use case here is the `ChatLanguageModel` swap (Gemini Flash → Gemini Nano) — Hilt's `@Binds` makes this a one-line change.

**Decision:** **Hilt (Dagger Hilt)**

**Rationale:** Compile-time safety is non-negotiable in a system where the data pipeline (physio snapshot → context agent → delivery agent) must always resolve correctly. `@TestInstallIn` enables fake repository injection in unit tests without modifying production code. The annotation processing overhead is an acceptable trade-off for the safety guarantees.

---

### D-010 — Physiological Sensing Approach — Passive vs. Active Polling

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse must monitor heart rate, HRV, sleep, and motion continuously in the background without degrading Wear OS battery life or triggering Android Doze mode restrictions.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Passive monitoring (PassiveMonitoringClient)** | Register for passive updates; OS delivers data when available | Battery-safe; Doze-compatible; no polling loop; OS-managed delivery | Data delivery timing not guaranteed; latency between real-world event and app receipt |
| **Active polling** | Periodic WorkManager tasks querying Health Connect | Predictable data freshness | Battery drain; conflicts with Doze mode; not recommended for continuous biometric monitoring |
| **Wear OS sensor APIs direct** | Direct accelerometer/HR from watch sensors | Immediate sensor access | No cross-device support; no Health Connect integration; companion app required for all logic |

**Trade-offs**
- `PassiveMonitoringClient` is the Google-recommended approach for background biometric monitoring — it respects Doze mode and is battery-safe by design.
- Active polling would provide more predictable data but at unacceptable battery cost on a wearable.
- For the hyperfocus detection scenario (UC3), the Sensor Bridge pattern (watch → phone via MessageClient) provides the low-latency signal needed without breaking the passive model.

**Decision:** **PassiveMonitoringClient** for background biometric data. **Sensor Bridge** (watch accelerometer → phone via MessageClient) for real-time motion-based hyperfocus detection.

**Rationale:** Continuous HR polling + accelerometer conflict with Doze mode and ambient power management — fatal for a wearable. `PassiveMonitoringClient` receives data passively from the OS; `setExpedited()` on `BriefingDispatchWorker` bypasses battery constraints only for urgent UC2/UC3 triggers. The two-layer approach (passive baseline + expedited dispatch) balances battery life against intervention latency.

---

### D-011 — ADHD Feature Scope — Time Blindness and Hyperfocus Only (PoC)

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** ADHD presents many executive function challenges. NeuroPulse must decide which to target in the PoC. Scope must be narrow enough to be demonstrable but meaningful enough to validate the core proposition.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Time Blindness + Hyperfocus only (2 personas)** | UC2 (Marcus) + UC3 (Zoe) as primary differentiators; UC1, UC4, UC5 as supporting use cases | Focused; both represent highest-impact, hardest-to-address ADHD challenges; maps to distinct user personas | Excludes task initiation, emotional dysregulation, working memory challenges |
| **Full ADHD spectrum** | Task initiation, working memory, emotional regulation, time blindness, hyperfocus, impulsivity | Comprehensive | Too broad for PoC; dilutes differentiation; no existing tool covers all these — gap analysis would be lost |
| **Time Blindness only** | Focus on Marcus persona only | Maximum focus | Excludes significant user segment (Zoe); weakens market differentiation story |

**Trade-offs**
- Gap analysis showed no existing tool (Focusmate, Tiimo, Goblin Tools, Apple Watch Mindfulness) addresses either Time Blindness or Hyperfocus with passive sensing — both are genuine market gaps.
- Two personas allow a richer demonstration than one, without the complexity of a full spectrum approach.
- UC1 (Morning Plan), UC4 (Stress Grounding), and UC5 (Task Completion) naturally support both personas without requiring separate implementation paths.

**Decision:** **Time Blindness (Marcus) + Hyperfocus (Zoe)** as the two PoC focus areas, supported by UC1, UC4, and UC5 as shared use cases.

**Rationale:** These two challenges represent the highest-impact ADHD executive function failures that no existing product addresses with passive wearable sensing. The two-persona approach validates the generalisation of the architecture (same three-agent pipeline, different `BriefingStrategy` per `TriggerType`) while remaining demonstrable within the PoC scope.

---

### D-012 — Briefing Delivery Surface — Wrist-First vs. Phone-First

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse can deliver briefings to the watch (wrist) or the phone (Compose notification card). The design must match the core proposition: reaching the user before they need to open their phone.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Wrist-first (watch tile primary)** | Briefing delivered to watch as primary surface | Reaches user without phone interaction; matches research finding that wearables are worn daily; ADHD-specific intervention without app-opening | Requires Wear OS companion; smaller display constrains briefing length |
| **Phone-first (notification primary)** | Rich notification on phone; watch as secondary | Richer content possible; no companion app required | Requires phone interaction — reintroduces the tool-friction problem; standard alarm behaviour NeuroPulse is designed to replace |
| **Adaptive (user preference + surface availability)** | `surface_preference` in `DeliveryAgent` routes to wrist or phone based on user setting and Bluetooth state | Flexible; respects user preference | Additional routing logic; must not degrade to phone-only for UC2/UC3 where wrist is critical |

**Trade-offs**
- Phone-first briefings require the user to notice and open their phone — this is exactly the executive function demand NeuroPulse is designed to eliminate.
- Wrist-first delivery is the primary differentiator: *"the smartwatch is the primary output channel — micro-interventions arrive on the wrist"* (core proposition).
- Adaptive routing via `DeliveryAgent` preserves flexibility without compromising the wrist-first principle.

**Decision:** **Wrist-first delivery** as the primary output channel via Wear OS tiles. **Compose notification card** as fallback when watch is unavailable. `DeliveryAgent` routes based on `surface_preference` and Bluetooth state.

**Rationale:** User research confirmed wearables are worn daily. Adults with ADHD are 3× more likely to use wearable devices. The phone requires initiation — the exact executive function the user lacks. The wrist meets the user at the moment of need without requiring any action. "Proactive not reactive" — the wrist tile fires; the phone catches edge cases.

---

### D-013 — GDPR Compliance Strategy — On-Device vs. Cloud Data

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse handles health and behavioural data classified as GDPR Art. 9 special category data (physiological snapshots, HR, HRV, sleep). Non-compliance would preclude NHS Digital integration pathway and could expose users to data breach risk.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Device-local storage + anonymised cloud inference** | All raw data stays on device; only anonymised prompt summaries sent to Gemini API | GDPR Art. 9 compliant by architecture; no cloud copy of sensitive data; user trust | Inference requires connectivity; Gemini API receives prompt text (anonymised) |
| **Full cloud storage** | Health data stored in Firebase/cloud | Multi-device sync; no storage limit | Violates GDPR Art. 9; cloud breach risk; NHS integration impossible; user consent burden |
| **On-device inference only** | Gemini Nano + on-device embeddings; zero cloud | Maximum privacy; fully offline | Android AICore not universally available; 80 MB+ model weight; out of scope for PoC |

**Trade-offs**
- Full cloud storage is non-viable given Art. 9 special category classification — the data processing burden and breach risk far outweigh the sync benefits.
- Full on-device inference is the ideal end-state but requires Android AICore availability that is not yet universal.
- The anonymised prompt approach: physiological data stays in Room; the prompt contains only anonymised summaries ("HRV 28ms — keep response concise today") — no raw biometric values sent to Gemini.

**Decision:** **Device-sovereign architecture**: all raw physiological data stays in Room on-device. Gemini Flash receives only anonymised prompt summaries. `BriefingStrategy` is responsible for anonymisation before `BriefingRepository.runInference()` is called.

**Rationale:** GDPR Art. 9 compliance achieved by architecture, not policy. Raw HR/HRV/sleep data never leaves the device. The Gemini prompt contains anonymised summaries assembled by `BriefingStrategy` — no cloud copy of sensitive data exists in the PoC architecture. This also enables the NHS Digital integration pathway, where clinical-grade data handling is required. Principle: *"The intelligence is cloud-assisted but the data is device-sovereign."*

---

### D-014 — Agent Pipeline Pattern — Pipe & Filter vs. Monolithic Service

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** NeuroPulse requires a structured pipeline from raw sensor data through context reasoning to delivery surface. The pattern must support adding new trigger types without modifying existing components.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Pipe & Filter (3-Agent Pipeline)** | Agent 1 (Capture) → Agent 2 (Context/AI Reasoning) → Agent 3 (Delivery). Each agent is a filter with defined input and output | Open-Closed: new agents addable without touching existing ones; independently testable; defined data contracts between agents | More components to maintain; inter-agent data contracts require discipline |
| **Monolithic service** | Single `BriefingService` handles ingestion, reasoning, and delivery | Simple; fewer files | Violates Single Responsibility; untestable without full system; new trigger types require modifying the service; coupling grows with feature additions |
| **Event bus** | Components communicate via event bus (e.g. Otto, EventBus) | Loose coupling | Hidden dependencies; hard to trace data flow; testing fragility |

**Trade-offs**
- Pipe & Filter directly enables the extensibility requirement: a new `TriggerType` requires a new `BriefingStrategy` implementation and a new `BriefingDispatchWorker` — the three agents are untouched.
- Monolithic service would be faster to write initially but would require modification for every new trigger type, violating Open-Closed Principle.
- The event bus approach would obscure the data flow that must be auditable for GDPR attribution (which captures contributed to a briefing, at what relevance score).

**Decision:** **Pipe & Filter — Three-Agent Pipeline**: Agent 1 (Capture Agent / Data Ingestion Filter) → Agent 2 (Context Agent / AI Reasoning Filter) → Agent 3 (Delivery Agent / Routing Filter).

**Rationale:** Each agent has a defined input type and a defined output type — no shared mutable state. The data flow is fully traceable (Wearable Device → UC12 → UC19 [extend] → UC4 → UC11 [extend] → UC1 → UC19). Every AI briefing is auditable: which captures contributed, in what order, at what relevance score. `WorkManager` schedules all agents with battery-safe constraints. This pattern directly satisfies GDPR auditability and the Open-Closed extensibility requirement.

---

### D-015 — Hyperfocus Detection Signal — Stillness + HR vs. App Usage Time

**Date:** Mar 2026 | **Phase:** Design | **Status:** Decided

**Context:** Detecting hyperfocus (Zoe's JTBD) requires a passive signal that indicates deep attentional absorption without requiring the user to report it manually.

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Extended stillness (accelerometer) + HR via Sensor Bridge** | Watch detects low motion over threshold period; phone correlates with HR data | Fully passive; no phone interaction required; wearable-native | False positives during meetings (user is also still); requires calendar suppression logic |
| **App usage time (UsageStatsManager)** | Monitor which app is foreground and for how long | Captures computer-based hyperfocus | Misses non-digital hyperfocus (reading, sketching); requires PACKAGE_USAGE_STATS permission (sensitive); not wearable-native |
| **Manual self-report** | User presses "I'm in focus mode" | 100% accurate | Completely contradicts the zero-friction design principle; assumes executive function the user lacks |

**Trade-offs**
- App usage time cannot detect non-digital hyperfocus and adds a sensitive permission with no wearable equivalent.
- Manual self-report violates the core product proposition.
- Stillness + HR is a validated proxy for hyperfocus (cited: Olinic, Stretea and Cereches, 2025) but produces false positives during meetings — mitigated by calendar suppression.

**Decision:** **Extended stillness (accelerometer via Sensor Bridge) + HR correlation** as the hyperfocus detection signal. **Calendar suppression** via `DayPlanRepository.getUpcomingEvents()` prevents alert during scheduled meetings. **Penalty scoring** via `briefing_log.user_acknowledged` and `getPenaltyScores()` penalises captures contributing to dismissed briefings over time.

**Rationale:** The wearable accelerometer + HR combination is the only passive, wearable-native signal available without additional permissions. Calendar suppression handles the primary false positive scenario (meetings). The penalty scoring mechanism enables the system to learn which contexts produce valid vs. invalid hyperfocus detections over time, reducing false positives without requiring a training loop.

---

## Template for New Decisions

Copy this template when adding a new decision record:

```markdown
### D-XXX — [Decision Title]

**Date:** [Date] | **Phase:** [Phase] | **Status:** Decided / Under Review / Deferred

**Context:** [Why this decision needed to be made]

**Options Considered**

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **Option A** | | | |
| **Option B** | | | |

**Trade-offs**
[Key trade-offs between options]

**Decision:** [The chosen option, bolded]

**Rationale:** [Why this option was chosen over the alternatives]
```
