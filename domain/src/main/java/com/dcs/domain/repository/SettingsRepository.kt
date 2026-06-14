package com.dcs.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user settings/preferences.
 * All settings are stored locally via DataStore.
 */
interface SettingsRepository {
    fun isScanningEnabled(): Flow<Boolean>
    fun isOverlayEnabled(): Flow<Boolean>
    fun getSensitivityLevel(): Flow<Int> // 1 = Low, 2 = Medium, 3 = High
    suspend fun setScanningEnabled(enabled: Boolean)
    suspend fun setOverlayEnabled(enabled: Boolean)
    suspend fun setSensitivityLevel(level: Int)
}
