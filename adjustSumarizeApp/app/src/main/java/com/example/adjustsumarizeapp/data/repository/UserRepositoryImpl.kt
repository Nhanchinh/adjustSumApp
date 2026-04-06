package com.example.adjustsumarizeapp.data.repository

import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.remote.ApiService
import com.example.adjustsumarizeapp.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    fun isLoggedIn(): Boolean
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : UserRepository {
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Call login API
            val response = apiService.login(
                username = email,  // FastAPI expects "username" field
                password = password
            )
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Save tokens
                tokenManager.saveAccessToken(loginResponse.accessToken)
                tokenManager.saveRefreshToken(loginResponse.refreshToken)
                
                // Convert UserDto to User domain model
                val user = loginResponse.user.toDomain(token = loginResponse.accessToken)
                
                // Save user info
                tokenManager.saveUserInfo(
                    userId = user.id,
                    email = user.email,
                    name = user.name,
                    role = user.role  // ✅ SAVE ROLE
                )
                
                Result.success(user)
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Login failed"
                } catch (e: Exception) {
                    "Login failed"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // Clear all stored data
            tokenManager.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val authHeader = tokenManager.getAuthorizationHeader()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val response = apiService.getCurrentUser(authHeader)
            
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!
                val user = userDto.toDomain(token = tokenManager.getAccessToken())
                
                // Update cached user info
                tokenManager.saveUserInfo(
                    userId = user.id,
                    email = user.email,
                    name = user.name,
                    role = user.role  // ✅ SAVE ROLE
                )
                
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}
