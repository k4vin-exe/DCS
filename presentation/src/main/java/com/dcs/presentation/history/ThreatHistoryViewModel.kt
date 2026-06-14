package com.dcs.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcs.domain.repository.ThreatLogEntry
import com.dcs.domain.usecase.ClearLogsUseCase
import com.dcs.domain.usecase.GetThreatHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreatHistoryViewModel @Inject constructor(
    getThreatHistoryUseCase: GetThreatHistoryUseCase,
    private val clearLogsUseCase: ClearLogsUseCase
) : ViewModel() {

    val logs: StateFlow<List<ThreatLogEntry>> = getThreatHistoryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            clearLogsUseCase()
        }
    }
}
