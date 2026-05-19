package com.example.adjustsumarizeapp.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.domain.usecase.LoginUseCase
import com.example.adjustsumarizeapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }
    
    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }
    
    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }
    
    fun onFullNameChange(fullName: String) {
        _state.update { it.copy(fullName = fullName, error = null) }
    }
    
    fun toggleRegisterMode() {
        _state.update { 
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                error = null,
                // Clear register-specific fields when toggling
                confirmPassword = "",
                fullName = ""
            )
        }
    }
    
    fun onLoginClick() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            loginUseCase(
                email = _state.value.email,
                password = _state.value.password
            ).onSuccess { user ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        user = user
                    ) 
                }
            }.onFailure { exception ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Đăng nhập thất bại"
                    ) 
                }
            }
        }
    }
    
    fun onRegisterClick() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            registerUseCase(
                email = _state.value.email,
                password = _state.value.password,
                confirmPassword = _state.value.confirmPassword,
                fullName = _state.value.fullName
            ).onSuccess { user ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,  // Auto-login after register
                        user = user
                    ) 
                }
            }.onFailure { exception ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Đăng ký thất bại"
                    ) 
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun resetLoginSuccess() {
        _state.update { it.copy(isLoginSuccessful = false) }
    }
}
