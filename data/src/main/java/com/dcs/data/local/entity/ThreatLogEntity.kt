package com.dcs.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisted threat logs.
 * Stores ONLY threat metadata — never raw message content.
 * The [reasons] field is serialized as a JSON string via [com.dcs.data.local.converter.Converters].
 */
@Entity(tableName = "threat_logs")
data class ThreatLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sourceApp: String,
    val threatType: String,
    val riskScore: Int,
    val reasons: String  // JSON-serialized List<String>
)
