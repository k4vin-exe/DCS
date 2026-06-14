package com.dcs.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dcs.data.local.converter.Converters
import com.dcs.data.local.dao.ThreatLogDao
import com.dcs.data.local.entity.ThreatLogEntity

@Database(
    entities = [ThreatLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DcsDatabase : RoomDatabase() {
    abstract fun threatLogDao(): ThreatLogDao
}
