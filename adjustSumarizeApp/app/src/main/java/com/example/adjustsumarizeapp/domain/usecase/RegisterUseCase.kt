package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.data.repository.UserRepository
import com.example.adjustsumarizeapp.domain.model.User
import javax.inject.Inject

/**
 * Use case for user registration
 * Validates input then calls repository to register + auto-login
 */
class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String?
    ): Result<User> {
        // Validation
        if (email.isBlank()) {
            return Result.failure(Exception("Email không được để trống"))
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(Exception("Email không hợp lệ"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Mật khẩu không được để trống"))
        }
        
        if (password.length < 6) {
            return Result.failure(Exception("Mật khẩu phải có ít nhất 6 ký tự"))
        }
        
        if (password != confirmPassword) {
            return Result.failure(Exception("Mật khẩu xác nhận không khớp"))
        }
        
        return userRepository.register(
            email = email,
            password = password,
            fullName = fullName?.takeIf { it.isNotBlank() }
        )
    }
}
