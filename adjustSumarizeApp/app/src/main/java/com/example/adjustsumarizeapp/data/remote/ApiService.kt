package com.example.adjustsumarizeapp.data.remote

import com.example.adjustsumarizeapp.data.model.LoginRequest
import com.example.adjustsumarizeapp.data.model.LoginResponse
import com.example.adjustsumarizeapp.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("user/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>
}
