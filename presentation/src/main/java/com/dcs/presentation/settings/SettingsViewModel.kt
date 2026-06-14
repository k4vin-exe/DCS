package com.dcs.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcs.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isScanningEnabled: StateFlow<Boolean> = settingsRepository.isScanningEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isOverlayEnabled: StateFlow<Boolean> = settingsRepository.isOverlayEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleScanning(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setScanningEnabled(enabled) }
    }

    fun toggleOverlay(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOverlayEnabled(enabled) }
    }
}
