package com.dcs.core.model

/** Risk classification levels based on composite threat score. */
enum class RiskLevel(val label: String, val minScore: Int, val maxScore: Int) {
    SAFE("Safe", 0, 30),
    SUSPICIOUS("Suspicious", 31, 70),
    DANGEROUS("Dangerous", 71, 100);

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score <= 30 -> SAFE
            score <= 70 -> SUSPICIOUS
            else -> DANGEROUS
        }
    }
}
