package com.example.adjustsumarizeapp.data.model

import com.example.adjustsumarizeapp.domain.model.User
import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for User from API
 */
data class UserDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("full_name")
    val fullName: String?,
    
    @SerializedName("role")
    val role: String = "user"
) {
    fun toDomain(token: String? = null): User {
        return User(
            id = id,
            email = email,
            name = fullName ?: email.substringBefore("@"),
            role = role,  // Map role from API
            token = token
        )
    }
}

/**
 * Login Request - FastAPI expects username (email) and password as form data
 * But we'll send as JSON for simplicity
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,  // FastAPI OAuth2 expects "username" field
    
    @SerializedName("password")
    val password: String
)

/**
 * Login Response from FastAPI
 * Returns: access_token, refresh_token, token_type, user
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String,
    
    @SerializedName("token_type")
    val tokenType: String = "bearer",
    
    @SerializedName("user")
    val user: UserDto
)

/**
 * Refresh Token Request
 */
data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

/**
 * Refresh Token Response
 */
data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("token_type")
    val tokenType: String = "bearer"
)
