package com.neuropulse.domain.repository

import com.neuropulse.domain.model.BriefingPayload
import com.neuropulse.domain.model.TriggerType

/**
 * BriefingRepository — contract for LLM inference.
 *
 * Abstracts the Gemini Flash API so the domain layer never knows which model
 * is in use. Swapping Gemini Flash (PoC) for Gemini Nano (on-device, v2)
 * requires changing only the Hilt binding in BriefingModule — zero domain changes.
 * This is the key architectural payoff of the LangChain4j ChatLanguageModel
 * interface. See ADR-003.
 *
 * Implementation lives in data/ai/ (GeminiBriefingRepository) — Phase 4.
 * Stub declared here in Phase 0 to establish the boundary.
 *
 * CLAUDE: [runInference] receives an already-anonymised prompt from ContextEngine.
 * Never pass raw PhysioContext or capture text directly to this method.
 * BriefingStrategy.buildPrompt() is responsible for anonymisation before this is called.
 *
 * @see com.neuropulse.data.ai.ContextEngine
 * @see BriefingPayload
 */
interface BriefingRepository {

    /**
     * Runs LLM inference on an anonymised prompt and returns a typed [BriefingPayload].
     *
     * The [prompt] must be pre-anonymised by [BriefingStrategy.buildPrompt] —
     * no raw biometric values, no personally identifiable information (ADR-003).
     *
     * @param prompt        Anonymised briefing prompt from ContextEngine.
     * @param triggerType   Which use case produced this prompt — carried through
     *                      to the returned [BriefingPayload] for routing and logging.
     * @return              Typed [BriefingPayload] ready for DeliveryAgent.
     * @throws              Any network or model error is propagated — callers
     *                      (ContextEngine) are responsible for fallback handling.
     */
    suspend fun runInference(prompt: String, triggerType: TriggerType): BriefingPayload
}
