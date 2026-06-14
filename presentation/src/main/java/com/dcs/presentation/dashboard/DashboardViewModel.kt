package com.dcs.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcs.domain.usecase.DashboardStats
import com.dcs.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    val stats: StateFlow<DashboardStats> = getDashboardStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )
}
