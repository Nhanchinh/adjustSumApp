package com.example.adjustsumarizeapp.data.repository

import com.example.adjustsumarizeapp.data.model.LoginRequest
import com.example.adjustsumarizeapp.data.remote.ApiService
import com.example.adjustsumarizeapp.domain.model.User
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Simulate network delay for demo
            delay(1500)
            
            // For demo purposes, simulate successful login
            // In production, use actual API call:
            // val response = apiService.login(LoginRequest(email, password))
            
            if (email.isNotBlank() && password.length >= 6) {
                val demoUser = User(
                    id = "1",
                    email = email,
                    name = email.substringBefore("@"),
                    token = "demo_token_${System.currentTimeMillis()}"
                )
                Result.success(demoUser)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
            
            /* Production code:
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.toDomain()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
            */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // Clear local data, tokens, etc.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
