package com.dcs.domain.usecase

import com.dcs.domain.repository.ThreatLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Aggregated dashboard statistics. */
data class DashboardStats(
    val todayScans: Int = 0,
    val threatsDetected: Int = 0,
    val highRiskCount: Int = 0
)

/**
 * Provides real-time dashboard statistics by combining multiple repository flows.
 */
class GetDashboardStatsUseCase @Inject constructor(
    private val repository: ThreatLogRepository
) {
    operator fun invoke(): Flow<DashboardStats> {
        return combine(
            repository.getTodayScanCount(),
            repository.getThreatCount(),
            repository.getHighRiskLogs()
        ) { todayScans, threatCount, highRiskLogs ->
            DashboardStats(
                todayScans = todayScans,
                threatsDetected = threatCount,
                highRiskCount = highRiskLogs.size
            )
        }
    }
}
