package com.dcs.ml

import com.dcs.core.model.ThreatResult
import com.dcs.core.util.TextCleaner
import com.dcs.domain.usecase.MessageAnalyzer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full message analysis pipeline implementation.
 *
 * Pipeline: Text Cleaning → Tanglish Normalization → Rule Engine → TFLite ML → Risk Fusion
 */
@Singleton
class MessageAnalyzerImpl @Inject constructor(
    private val tanglishNormalizer: TanglishNormalizer,
    private val ruleEngine: RuleEngine,
    private val tfLiteInferenceManager: TFLiteInferenceManager,
    private val riskFusionEngine: RiskFusionEngine
) : MessageAnalyzer {

    override suspend fun analyze(text: String, sourceApp: String): ThreatResult {
        // Step 1: Check for URLs before cleaning
        val hasUrl = TextCleaner.containsUrl(text)

        // Step 2: Clean text (lowercase, remove URLs/emoji/special chars)
        val cleanedText = TextCleaner.clean(text)

        // Step 3: Normalize Tanglish → English
        val normalizedText = tanglishNormalizer.normalize(cleanedText)

        // Step 4: Rule Engine evaluation
        val ruleResult = ruleEngine.evaluate(normalizedText, hasUrl)

        // Step 5: TFLite ML classification
        val mlScores = tfLiteInferenceManager.classify(normalizedText)

        // Step 6: Fuse rule + ML scores
        return riskFusionEngine.fuse(
            ruleResult = ruleResult,
            mlScores = mlScores,
            sourceApp = sourceApp,
            timestamp = System.currentTimeMillis()
        )
    }
}
