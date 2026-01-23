package com.example.adjustsumarizeapp.ui.screen.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    init {
        // Try to load user info from saved state or repository
        savedStateHandle.get<String>("userName")?.let { name ->
            _state.value = _state.value.copy(userName = name)
        } ?: loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().onSuccess { user ->
                _state.value = _state.value.copy(userName = user.name)
            }
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
