package com.example.adjustsumarizeapp.data.model

import com.example.adjustsumarizeapp.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("token")
    val token: String? = null
) {
    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            name = name,
            token = token
        )
    }
}

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("user")
    val user: UserDto? = null
)
