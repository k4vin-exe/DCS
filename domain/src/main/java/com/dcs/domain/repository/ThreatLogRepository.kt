package com.dcs.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Domain model for a stored threat log entry.
 * Contains only metadata — never raw message content.
 */
data class ThreatLogEntry(
    val id: Long = 0,
    val timestamp: Long,
    val sourceApp: String,
    val threatType: String,
    val riskScore: Int,
    val reasons: List<String>
)

/**
 * Repository interface for threat log persistence operations.
 * Implementations are in the data module.
 */
interface ThreatLogRepository {
    fun getAllLogs(): Flow<List<ThreatLogEntry>>
    fun getTodayLogs(): Flow<List<ThreatLogEntry>>
    fun getHighRiskLogs(): Flow<List<ThreatLogEntry>>
    fun getThreatCount(): Flow<Int>
    fun getTodayScanCount(): Flow<Int>
    suspend fun insertLog(entry: ThreatLogEntry)
    suspend fun clearAllLogs()
    suspend fun getLogById(id: Long): ThreatLogEntry?
}
