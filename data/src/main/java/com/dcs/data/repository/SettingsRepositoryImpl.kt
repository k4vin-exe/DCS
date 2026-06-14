package com.dcs.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dcs.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dcs_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    companion object {
        val SCANNING_ENABLED = booleanPreferencesKey("scanning_enabled")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val SENSITIVITY_LEVEL = intPreferencesKey("sensitivity_level")
    }

    override fun isScanningEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[SCANNING_ENABLED] ?: true }

    override fun isOverlayEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[OVERLAY_ENABLED] ?: true }

    override fun getSensitivityLevel(): Flow<Int> =
        context.dataStore.data.map { it[SENSITIVITY_LEVEL] ?: 2 }

    override suspend fun setScanningEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SCANNING_ENABLED] = enabled }
    }

    override suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[OVERLAY_ENABLED] = enabled }
    }

    override suspend fun setSensitivityLevel(level: Int) {
        context.dataStore.edit { it[SENSITIVITY_LEVEL] = level.coerceIn(1, 3) }
    }
}
