package com.example.adjustsumarizeapp.ui.screen.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.model.AnalyticsResponseDto
import com.example.adjustsumarizeapp.data.model.ColabHealthResponse
import com.example.adjustsumarizeapp.data.model.ModelStatsDto
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOverviewState(
    val totalSummaries: Int = 0,
    val totalUsers: Int = 0,
    val totalWithFeedback: Int = 0,
    val feedbackRate: Double = 0.0,
    val modelStats: List<ModelStatsDto> = emptyList(),
    val modelDistribution: Map<String, Int> = emptyMap(),
    val ratingDistribution: Map<String, Int> = emptyMap(),
    val avgProcessingTimeMs: Double = 0.0,
    val colabStatus: String = "unknown",
    val gpuAvailable: Boolean? = null,
    val colabUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminOverviewViewModel @Inject constructor(
    private val repository: SummaryRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdminOverviewState())
    val state: StateFlow<AdminOverviewState> = _state.asStateFlow()
    
    init {
        loadOverview()
    }
    
    private fun loadOverview() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Gọi song song analytics, users, health
                val analyticsResult = repository.getAnalytics()
                val usersResult = repository.getAdminUsers()
                val healthResult = repository.checkColabHealth()
                
                analyticsResult.onSuccess { analytics ->
                    _state.value = _state.value.copy(
                        totalSummaries = analytics.totalSummaries,
                        totalWithFeedback = analytics.totalWithFeedback,
                        feedbackRate = analytics.feedbackRate,
                        modelStats = analytics.modelStats,
                        modelDistribution = analytics.modelDistribution,
                        ratingDistribution = analytics.ratingDistribution,
                        avgProcessingTimeMs = analytics.avgProcessingTimeMs
                    )
                }.onFailure { e ->
                    _state.value = _state.value.copy(
                        error = "Analytics: ${e.message}"
                    )
                }
                
                usersResult.onSuccess { users ->
                    _state.value = _state.value.copy(totalUsers = users.size)
                }
                
                healthResult.onSuccess { health ->
                    _state.value = _state.value.copy(
                        colabStatus = health.status,
                        gpuAvailable = health.gpuAvailable,
                        colabUrl = health.colabUrl
                    )
                }
                
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Không thể tải dữ liệu: ${e.message}"
                )
            }
        }
    }
    
    fun refresh() {
        loadOverview()
    }
}
