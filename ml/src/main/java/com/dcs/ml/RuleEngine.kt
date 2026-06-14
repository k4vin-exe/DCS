package com.dcs.ml

import com.dcs.core.model.ThreatType
import javax.inject.Inject
import javax.inject.Singleton

/** Result from the rule-based evaluation containing score, matched rules, and threat types. */
data class RuleResult(
    val score: Int,
    val matchedRules: List<String>,
    val detectedThreats: Set<ThreatType>
)

/**
 * Weighted keyword rule engine for threat detection.
 *
 * Each rule defines keywords, a weight (score contribution), a human-readable category,
 * and a threat type. When a rule matches, its weight is added to the total score.
 * Score is capped at 100.
 */
@Singleton
class RuleEngine @Inject constructor() {

    private data class Rule(
        val keywords: List<String>,
        val weight: Int,
        val category: String,
        val threatType: ThreatType
    )

    private val rules = listOf(
        // ── Financial Scam ──────────────────────────────────────────
        Rule(listOf("give otp", "tell otp", "share otp"), 30, "OTP sharing request detected", ThreatType.SCAM),
        Rule(listOf("give bank details", "give card number"), 30, "Financial detail request detected", ThreatType.SCAM),
        Rule(listOf("tell upi pin"), 30, "UPI PIN request detected", ThreatType.SCAM),
        Rule(listOf("otp"), 25, "OTP request detected", ThreatType.SCAM),
        Rule(listOf("bank", "account number"), 25, "Bank/account reference detected", ThreatType.SCAM),
        Rule(listOf("upi", "upi pin"), 25, "UPI reference detected", ThreatType.SCAM),
        Rule(listOf("send money", "give money", "transfer money"), 25, "Money request detected", ThreatType.SCAM),
        Rule(listOf("account blocked", "account suspended"), 25, "Account scare tactic detected", ThreatType.PHISHING),
        Rule(listOf("refund"), 20, "Refund scam pattern detected", ThreatType.SCAM),
        Rule(listOf("reward", "prize", "winner", "lottery"), 20, "Prize/lottery scam detected", ThreatType.SCAM),
        Rule(listOf("investment", "invest", "profit", "guaranteed returns"), 20, "Investment scam detected", ThreatType.SCAM),
        Rule(listOf("crypto", "bitcoin", "trading"), 20, "Crypto scam detected", ThreatType.SCAM),
        Rule(listOf("password", "credential", "login details"), 20, "Credential phishing detected", ThreatType.PHISHING),
        Rule(listOf("verify", "verification"), 15, "Verification request detected", ThreatType.PHISHING),
        Rule(listOf("click", "link", "url"), 15, "Suspicious link reference", ThreatType.PHISHING),
        Rule(listOf("fraud", "cheater"), 15, "Fraud reference detected", ThreatType.SCAM),

        // ── Abuse / Threats ─────────────────────────────────────────
        Rule(listOf("kill yourself", "go die"), 30, "Death threat/wish detected", ThreatType.THREAT),
        Rule(listOf("kill you"), 30, "Death threat detected", ThreatType.THREAT),
        Rule(listOf("die"), 25, "Death reference detected", ThreatType.THREAT),
        Rule(listOf("slur"), 25, "Offensive slur detected", ThreatType.ABUSE),
        Rule(listOf("idiot", "stupid", "fool", "dumb", "moron"), 20, "Abusive language detected", ThreatType.ABUSE),
        Rule(listOf("worthless", "useless", "waste"), 20, "Degrading language detected", ThreatType.ABUSE),
        Rule(listOf("loser", "pathetic"), 15, "Bullying language detected", ThreatType.HARASSMENT),

        // ── Urgency / Manipulation ──────────────────────────────────
        Rule(listOf("urgent", "urgently"), 10, "Urgency language detected", ThreatType.EMOTIONAL_MANIPULATION),
        Rule(listOf("immediately", "right now", "asap"), 10, "Immediate action pressure detected", ThreatType.EMOTIONAL_MANIPULATION),
        Rule(listOf("fast", "quickly", "hurry"), 10, "Rush pressure detected", ThreatType.EMOTIONAL_MANIPULATION),
        Rule(listOf("last chance", "expires", "limited time", "only today"), 15, "FOMO manipulation detected", ThreatType.EMOTIONAL_MANIPULATION),
        Rule(listOf("nobody cares", "nobody loves"), 15, "Emotional manipulation detected", ThreatType.EMOTIONAL_MANIPULATION),
        Rule(listOf("your fault", "blame you"), 15, "Guilt manipulation detected", ThreatType.EMOTIONAL_MANIPULATION)
    )

    /** URL score bonus */
    private val URL_BONUS = 20

    /**
     * Evaluate text against all rules and return a composite score.
     * @param text Normalized/cleaned text to evaluate
     * @param hasUrl Whether the original text contained a URL
     * @return [RuleResult] with score (0–100), matched rule descriptions, and detected threat types
     */
    fun evaluate(text: String, hasUrl: Boolean = false): RuleResult {
        val matchedRules = mutableListOf<String>()
        val detectedThreats = mutableSetOf<ThreatType>()
        var totalScore = 0

        val lowerText = text.lowercase()

        for (rule in rules) {
            val matched = rule.keywords.any { keyword -> lowerText.contains(keyword) }
            if (matched) {
                totalScore += rule.weight
                matchedRules.add(rule.category)
                detectedThreats.add(rule.threatType)
            }
        }

        if (hasUrl) {
            totalScore += URL_BONUS
            matchedRules.add("Suspicious URL detected")
            detectedThreats.add(ThreatType.PHISHING)
        }

        return RuleResult(
            score = totalScore.coerceIn(0, 100),
            matchedRules = matchedRules.distinct(),
            detectedThreats = detectedThreats
        )
    }
}
