package com.dcs.domain.usecase

import com.dcs.domain.repository.ThreatLogEntry
import com.dcs.domain.repository.ThreatLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Returns a Flow of all threat history entries, ordered by most recent first. */
class GetThreatHistoryUseCase @Inject constructor(
    private val repository: ThreatLogRepository
) {
    operator fun invoke(): Flow<List<ThreatLogEntry>> = repository.getAllLogs()
}

/** Clears all stored threat log entries. */
class ClearLogsUseCase @Inject constructor(
    private val repository: ThreatLogRepository
) {
    suspend operator fun invoke() = repository.clearAllLogs()
}

/** Retrieves a single threat log entry by its ID. */
class GetThreatDetailUseCase @Inject constructor(
    private val repository: ThreatLogRepository
) {
    suspend operator fun invoke(id: Long): ThreatLogEntry? = repository.getLogById(id)
}
