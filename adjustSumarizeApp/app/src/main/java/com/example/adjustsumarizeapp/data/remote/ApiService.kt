package com.example.adjustsumarizeapp.data.remote

import com.example.adjustsumarizeapp.data.model.LoginResponse
import com.example.adjustsumarizeapp.data.model.RefreshTokenRequest
import com.example.adjustsumarizeapp.data.model.RefreshTokenResponse
import com.example.adjustsumarizeapp.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    
    /**
     * Login endpoint - FastAPI OAuth2PasswordRequestForm expects x-www-form-urlencoded
     */
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,  // Email
        @Field("password") password: String
    ): Response<LoginResponse>
    
    /**
     * Get current user info
     */
    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authToken: String
    ): Response<UserDto>
    
    /**
     * Refresh access token
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>
}

