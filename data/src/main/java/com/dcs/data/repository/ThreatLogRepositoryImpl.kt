package com.dcs.data.repository

import com.dcs.data.local.dao.ThreatLogDao
import com.dcs.data.local.entity.ThreatLogEntity
import com.dcs.domain.repository.ThreatLogEntry
import com.dcs.domain.repository.ThreatLogRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThreatLogRepositoryImpl @Inject constructor(
    private val dao: ThreatLogDao
) : ThreatLogRepository {

    private val gson = Gson()

    override fun getAllLogs(): Flow<List<ThreatLogEntry>> =
        dao.getAllLogs().map { it.map(::toDomain) }

    override fun getTodayLogs(): Flow<List<ThreatLogEntry>> =
        dao.getTodayLogs(getStartOfDay()).map { it.map(::toDomain) }

    override fun getHighRiskLogs(): Flow<List<ThreatLogEntry>> =
        dao.getHighRiskLogs().map { it.map(::toDomain) }

    override fun getThreatCount(): Flow<Int> =
        dao.getThreatCount()

    override fun getTodayScanCount(): Flow<Int> =
        dao.getTodayScanCount(getStartOfDay())

    override suspend fun insertLog(entry: ThreatLogEntry) {
        dao.insert(toEntity(entry))
    }

    override suspend fun clearAllLogs() {
        dao.clearAll()
    }

    override suspend fun getLogById(id: Long): ThreatLogEntry? =
        dao.getLogById(id)?.let(::toDomain)

    private fun getStartOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun toDomain(entity: ThreatLogEntity) = ThreatLogEntry(
        id = entity.id,
        timestamp = entity.timestamp,
        sourceApp = entity.sourceApp,
        threatType = entity.threatType,
        riskScore = entity.riskScore,
        reasons = parseReasons(entity.reasons)
    )

    private fun toEntity(entry: ThreatLogEntry) = ThreatLogEntity(
        id = entry.id,
        timestamp = entry.timestamp,
        sourceApp = entry.sourceApp,
        threatType = entry.threatType,
        riskScore = entry.riskScore,
        reasons = gson.toJson(entry.reasons)
    )

    private fun parseReasons(json: String): List<String> = try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
