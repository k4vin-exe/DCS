package com.dcs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dcs.data.local.entity.ThreatLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ThreatLogEntity)

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDay: Long): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE riskScore >= 71 ORDER BY timestamp DESC")
    fun getHighRiskLogs(): Flow<List<ThreatLogEntity>>

    @Query("SELECT COUNT(*) FROM threat_logs WHERE riskScore >= 31")
    fun getThreatCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threat_logs WHERE timestamp >= :startOfDay")
    fun getTodayScanCount(startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM threat_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ThreatLogEntity?

    @Query("DELETE FROM threat_logs")
    suspend fun clearAll()
}
