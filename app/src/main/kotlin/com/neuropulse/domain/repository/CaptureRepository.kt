package com.neuropulse.domain.repository

/**
 * CaptureRepository — contract for capture storage and FTS5 retrieval.
 *
 * Captures are the user's personal context store — voice notes, typed notes,
 * and photos that feed the RAG pipeline to enrich AI briefings.
 *
 * [findRelevant] is the RAG retrieval method: it uses Room FTS5 BM25 keyword
 * search to surface the captures most relevant to a given trigger context,
 * then injects them into the Gemini prompt as personal context. See ADR-003.
 *
 * Implementation lives in data/local/ (RoomCaptureRepository) — Phase 4.
 * Stub declared here in Phase 0 to establish the boundary.
 *
 * CLAUDE: Capture content is personal data — never log or transmit raw capture text.
 * Only anonymised summaries derived from captures may appear in Gemini prompts (ADR-003).
 *
 * Phase 4 methods (to be added when CaptureEntity and Capture domain model are defined):
 *   suspend fun save(capture: Capture): Long
 *   suspend fun findRelevant(query: String, limit: Int): List<Capture>
 *   suspend fun applyPenalty(captureId: Long, weight: Float)
 *   fun observeAll(): Flow<List<Capture>>
 */
interface CaptureRepository
