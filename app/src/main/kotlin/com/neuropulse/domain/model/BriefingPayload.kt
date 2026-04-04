package com.neuropulse.domain.model

/**
 * BriefingPayload — the typed output of the AI reasoning layer (ContextEngine).
 *
 * This is the handoff object between ContextEngine (AI filter) and DeliveryAgent
 * (delivery filter) in the Three-Agent Pipeline. It carries everything DeliveryAgent
 * needs to route and display the briefing — no additional processing required.
 *
 * CLAUDE: DeliveryAgent must only read from BriefingPayload — never modify it.
 * If a field needs to change between context and delivery, that change belongs
 * in ContextEngine or a BriefingStrategy, not in DeliveryAgent.
 *
 * @param triggerType       Which of the 5 use cases produced this briefing.
 *                          DeliveryAgent uses this to select the correct notification channel.
 * @param title             Short, plain-language title for the notification card (≤ 40 chars).
 *                          ADHD rule: no jargon, no urgency language in the title.
 * @param body              AI-generated briefing text from ContextEngine (≤ 200 chars).
 *                          Anonymised — no raw biometric values. Plain, calm language.
 * @param deliverySurface   Where this briefing should be displayed.
 *                          WATCH for all wrist-targeted UCs; PHONE as fallback.
 * @param timestampMs       Unix epoch milliseconds when ContextEngine produced this payload.
 *                          Stored in BriefingLogEntity for audit and penalty scoring.
 */
data class BriefingPayload(
    val triggerType: TriggerType,
    val title: String,
    val body: String,
    val deliverySurface: DeliverySurface,
    val timestampMs: Long,
)

/**
 * DeliverySurface — the display target for a [BriefingPayload].
 *
 * WATCH is the primary surface for all 5 UCs — NeuroPulse is wearable-first.
 * PHONE is the fallback when Wear OS emulator/device is not paired.
 *
 * DeliveryModule (Phase 5) binds [DeliveryStrategy] to PhoneDeliveryStrategy for the PoC,
 * which targets the Wear OS notification channel regardless of this value.
 * Swapping to WearDeliveryStrategy (haptic + tile) requires only a Hilt binding change.
 */
enum class DeliverySurface {
    /** Wear OS notification channel → wrist card (primary surface). */
    WATCH,

    /** Phone notification → standard Android notification shade (fallback). */
    PHONE,
}
