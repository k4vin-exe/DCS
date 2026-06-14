package com.dcs.ml

import com.dcs.core.constants.AppConstants
import com.dcs.core.model.RiskLevel
import com.dcs.core.model.ThreatResult
import com.dcs.core.model.ThreatType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combines Rule Engine and ML scores using a weighted fusion formula:
 *
 *   FinalRisk = (0.6 × RuleScore) + (0.4 × MLScore)
 *
 * Where MLScore = (1 - safe_probability) × 100, i.e. the complement of the "safe" probability.
 * The result is normalized to 0–100 and classified into risk levels.
 */
@Singleton
class RiskFusionEngine @Inject constructor() {

    /**
     * Fuse rule-based and ML-based scores into a final [ThreatResult].
     */
    fun fuse(
        ruleResult: RuleResult,
        mlScores: Map<String, Float>,
        sourceApp: String,
        timestamp: Long
    ): ThreatResult {
        // ML threat score: complement of safe probability, scaled to 0–100
        val mlSafeScore = mlScores["safe"] ?: 0.5f
        val mlThreatScore = (1f - mlSafeScore) * 100f

        // Weighted fusion
        val fusedScore = (AppConstants.RULE_WEIGHT * ruleResult.score +
                AppConstants.ML_WEIGHT * mlThreatScore)
            .toInt()
            .coerceIn(0, 100)

        val threatType = determineThreatType(ruleResult, mlScores)
        val reasons = ruleResult.matchedRules.distinct()

        return ThreatResult(
            riskScore = fusedScore,
            riskLevel = RiskLevel.fromScore(fusedScore),
            threatType = threatType,
            reasons = reasons,
            mlScores = mlScores,
            ruleScore = ruleResult.score,
            sourceApp = sourceApp,
            timestamp = timestamp
        )
    }

    /**
     * Determine the primary threat type based on rule matches and ML output.
     * Priority: rule-detected threats > ML-detected threats.
     */
    private fun determineThreatType(
        ruleResult: RuleResult,
        mlScores: Map<String, Float>
    ): ThreatType {
        if (ruleResult.detectedThreats.isNotEmpty()) {
            // Return the most severe threat (priority order)
            val priority = listOf(
                ThreatType.THREAT,
                ThreatType.SCAM,
                ThreatType.PHISHING,
                ThreatType.ABUSE,
                ThreatType.HARASSMENT,
                ThreatType.EMOTIONAL_MANIPULATION
            )
            for (type in priority) {
                if (type in ruleResult.detectedThreats) return type
            }
        }

        // Fallback to ML scores
        val scamScore = mlScores["scam"] ?: 0f
        val abuseScore = mlScores["abuse"] ?: 0f
        return when {
            scamScore > 0.5f -> ThreatType.SCAM
            abuseScore > 0.5f -> ThreatType.ABUSE
            else -> ThreatType.NONE
        }
    }
}
