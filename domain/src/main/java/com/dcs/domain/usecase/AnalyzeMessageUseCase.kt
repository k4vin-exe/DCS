package com.dcs.domain.usecase

import com.dcs.core.model.NotificationData
import com.dcs.core.model.ThreatResult
import com.dcs.domain.repository.ThreatLogEntry
import com.dcs.domain.repository.ThreatLogRepository
import javax.inject.Inject

/**
 * Orchestrates the full message analysis pipeline and persists the result.
 *
 * Flow: notification data → combine title+text → analyze → store threat log
 * Raw message text is NEVER persisted — only metadata and reasons.
 */
class AnalyzeMessageUseCase @Inject constructor(
    private val messageAnalyzer: MessageAnalyzer,
    private val threatLogRepository: ThreatLogRepository
) {
    suspend operator fun invoke(notificationData: NotificationData): ThreatResult {
        // Combine title and message text for analysis
        val text = buildString {
            notificationData.title?.let { append(it).append(" ") }
            notificationData.messageText?.let { append(it) }
        }

        // Run the full detection pipeline
        val result = messageAnalyzer.analyze(text, notificationData.packageName)

        // Persist only the threat metadata — never raw message text
        threatLogRepository.insertLog(
            ThreatLogEntry(
                timestamp = notificationData.timestamp,
                sourceApp = notificationData.packageName,
                threatType = result.threatType.name,
                riskScore = result.riskScore,
                reasons = result.reasons
            )
        )

        return result
    }
}
