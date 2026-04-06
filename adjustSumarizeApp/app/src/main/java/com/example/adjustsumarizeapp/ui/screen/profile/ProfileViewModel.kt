package com.example.adjustsumarizeapp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.repository.UserRepository
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val userName: String = "",
    val email: String = "",
    val role: String = "user",
    val consentShareData: Boolean = true,
    val totalSummaries: Int = 0,
    val topModel: String = "-",
    val isLoading: Boolean = false,
    val isUpdatingConsent: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userRepository.getCurrentUser().onSuccess { user ->
                _state.update {
                    it.copy(
                        userName = user.name,
                        email = user.email,
                        role = user.role
                    )
                }
            }

            summaryRepository.getAnalytics().onSuccess { analytics ->
                val topModel = analytics.modelStats
                    .maxByOrNull { it.count }
                    ?.model?.uppercase() ?: "-"

                _state.update {
                    it.copy(
                        totalSummaries = analytics.totalSummaries,
                        topModel = topModel
                    )
                }
            }

            // Fetch current consent status from admin users list (if admin)
            // or from update settings with no changes to get current state
            summaryRepository.updateSettings().onSuccess { userPublic ->
                _state.update { it.copy(consentShareData = userPublic.consentShareData) }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun toggleConsent(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingConsent = true) }

            summaryRepository.updateSettings(consentShareData = enabled).onSuccess { userPublic ->
                _state.update {
                    it.copy(
                        consentShareData = userPublic.consentShareData,
                        isUpdatingConsent = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isUpdatingConsent = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
