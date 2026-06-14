package com.dcs.core.model

/**
 * Result of analyzing a message through the full detection pipeline.
 * Contains the fused risk score, classification, and explainability reasons.
 * Raw message text is NEVER stored in this object.
 */
data class ThreatResult(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val threatType: ThreatType,
    val reasons: List<String>,
    val mlScores: Map<String, Float>,
    val ruleScore: Int,
    val sourceApp: String,
    val timestamp: Long
)
