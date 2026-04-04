# ADR-004: NotificationManager + Wear OS Channel over MessageClient

**Status:** Accepted  
**Date:** 2026-03  
**Full trade-off analysis:** `docs/decision_log.md` D-005, D-012

---

## Decision

For the PoC, `BriefingPayload` is delivered to the wrist via Android
`NotificationManager` with a dedicated Wear OS notification channel.
`MessageClient` / `ChannelClient` (Wearable Data Layer API) is the
documented upgrade path for physical device deployment.

---

## Context

The PoC must run entirely on emulators (phone AVD + Wear OS AVD) connected
over ADB — no physical Pixel Watch or Bluetooth pairing available during
development. `MessageClient` requires an active Wearable Data Layer connection,
which is unreliable over the ADB TCP bridge on emulators.

`NotificationManager` with `IMPORTANCE_HIGH` and a `NotificationChannel`
targeting the Wear OS surface delivers notification cards to the watch face
reliably over ADB. This is sufficient to demonstrate all 5 UC delivery flows
in the examiner demo without hardware.

---

## Delivery architecture

```
DeliveryAgent
    ↓  injects
DeliveryStrategy  (interface — extension point, ADR-001)
    ↓  bound by Hilt
PhoneDeliveryStrategy  (PoC — NotificationManager + Wear OS channel)
    ↓  upgrade path (one Hilt binding change)
WearDeliveryStrategy   (v2 — MessageClient + haptic + tile)
```

`DeliveryAgent` is agnostic to which strategy is bound.
`DeliveryModule` (Hilt) is the only file that changes between PoC and v2.

---

## Notification channels

| Channel ID | UC | Importance | Wear OS |
|---|---|---|---|
| `neuropulse_morning` | UC1 | DEFAULT | Card on raise-to-wake |
| `neuropulse_pre_event` | UC2 | HIGH | Card + gentle vibration |
| `neuropulse_hyperfocus` | UC3 | HIGH | Card + gentle vibration |
| `neuropulse_stress` | UC4 | HIGH | Card + gentle vibration |
| `neuropulse_task` | UC5 | DEFAULT | Card on raise-to-wake |

**Never** use `IMPORTANCE_MAX` (heads-up) — jarring alerts are clinically
contraindicated for ADHD users. Graduated from DEFAULT to HIGH only.
See ADR-005 for the full graduated notification rule.

---

## Rules for developers

- All notification builders must go through `PhoneDeliveryStrategy` — never
  call `NotificationManager` directly from a ViewModel or UseCase
- `DeliveryAgent` receives `BriefingPayload` — never a raw string
- Channel registration happens once in `NeuroPulseApplication.onCreate()`
  (Phase 5) — do not re-register on each delivery
- To upgrade to physical watch: implement `WearDeliveryStrategy`,
  rebind in `DeliveryModule` — zero changes to `DeliveryAgent` or any upstream class
