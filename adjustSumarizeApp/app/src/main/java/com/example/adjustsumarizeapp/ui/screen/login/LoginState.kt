package com.example.adjustsumarizeapp.ui.screen.login

import com.example.adjustsumarizeapp.domain.model.User

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val user: User? = null
)
