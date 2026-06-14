package com.dcs.domain.usecase

import com.dcs.core.model.ThreatResult

/**
 * Interface for the message analysis pipeline.
 * Implemented by the ML module to decouple domain from ML implementation details.
 */
interface MessageAnalyzer {
    suspend fun analyze(text: String, sourceApp: String): ThreatResult
}
