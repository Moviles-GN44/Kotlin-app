package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.model.HourlyWaitProfile
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.PeakHoursRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class PeakHoursUiState(
    val isLoading: Boolean = true,
    val profile: HourlyWaitProfile? = null,
    val fromCache: Boolean = false,
    val isOffline: Boolean = false,
    val hasNeverLoadedOffline: Boolean = false
)

class PeakHoursViewModel(
    private val repository: PeakHoursRepository = PeakHoursRepository(),
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeakHoursUiState())
    val uiState: StateFlow<PeakHoursUiState> = _uiState.asStateFlow()

    private var activeRestaurantId: String? = null

    init {
        // Eventual Connectivity: automatically reload live wait times when network returns
        viewModelScope.launch {
            ConnectivityHelper.isOnlineFlow.collect { online ->
                val rid = activeRestaurantId
                val state = _uiState.value
                if (online && rid != null && (state.isOffline || state.hasNeverLoadedOffline)) {
                    loadHourlyProfile(rid, forceRefresh = true)
                }
            }
        }
    }

    fun loadHourlyProfile(restaurantId: String, forceRefresh: Boolean = false) {
        activeRestaurantId = restaurantId
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getHourlyProfile(
                restaurantId = restaurantId,
                forceRefresh = forceRefresh
            )
            _uiState.value = PeakHoursUiState(
                isLoading = false,
                profile = result.profile,
                fromCache = result.fromCache,
                isOffline = result.isOffline,
                hasNeverLoadedOffline = result.hasNeverLoadedOffline
            )
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            analyticsRepository.logEvent(
                name = "best_time_viewed",
                params = mapOf(
                    "restaurantId" to restaurantId,
                    "hour" to currentHour,
                    "fromCache" to result.fromCache,
                    "load_time_ms" to result.loadTimeMs
                )
            )
        }
    }

    fun refresh() {
        activeRestaurantId?.let { loadHourlyProfile(it, forceRefresh = true) }
    }
}
