package com.example.adjustsumarizeapp.ui.screen.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.repository.UserRepository
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        // Try to load user info from saved state or repository
        savedStateHandle.get<String>("userName")?.let { name ->
            _state.value = _state.value.copy(userName = name)
        } ?: loadCurrentUser()
        
        // Auto-check Colab connection on startup
        checkColabConnection()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().onSuccess { user ->
                _state.value = _state.value.copy(userName = user.name)
            }
        }
    }
    
    fun checkColabConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(colabStatus = ColabConnectionStatus.CHECKING)
            
            summaryRepository.checkColabHealth().fold(
                onSuccess = { healthResponse ->
                    _state.value = _state.value.copy(
                        colabStatus = if (healthResponse.status == "connected") {
                            ColabConnectionStatus.CONNECTED
                        } else {
                            ColabConnectionStatus.DISCONNECTED
                        },
                        colabHealth = healthResponse
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(
                        colabStatus = ColabConnectionStatus.DISCONNECTED,
                        colabHealth = null
                    )
                }
            )
        }
    }
    
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout().onSuccess {
                onLogoutSuccess()
            }
        }
    }
}
