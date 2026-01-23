package com.example.adjustsumarizeapp.data.remote

import com.example.adjustsumarizeapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== Auth Endpoints ====================
    
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
    
    // ==================== Summarization Endpoints ====================
    
    /**
     * Summarize text
     */
    @POST("summarization/summarize")
    suspend fun summarize(
        @Body request: SummarizeRequest
    ): Response<SummarizeResponse>
    
    /**
     * Get available models
     */
    @GET("summarization/models")
    suspend fun getModels(): Response<List<ModelInfo>>
    
    /**
     * Check Colab health
     */
    @GET("summarization/health")
    suspend fun checkColabHealth(): Response<Map<String, Any>>
    
    // ==================== Evaluation Endpoints ====================
    
    /**
     * Evaluate summary quality
     */
    @POST("evaluation/single")
    suspend fun evaluateSummary(
        @Body request: EvaluateRequest
    ): Response<EvaluateResponse>
    
    // ==================== History Endpoints ====================
    
    /**
     * Get user's summary history
     */
    @GET("history/")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<HistoryResponse>
    
    /**
     * Save summary to history
     */
    @POST("history/")
    suspend fun saveHistory(
        @Body request: SaveHistoryRequest
    ): Response<HistoryItemDto>
    
    /**
     * Delete history item
     */
    @DELETE("history/{history_id}")
    suspend fun deleteHistory(
        @Path("history_id") historyId: String
    ): Response<Map<String, String>>
    
    /**
     * Get single history item
     */
    @GET("history/{history_id}")
    suspend fun getHistoryItem(
        @Path("history_id") historyId: String
    ): Response<HistoryItemDto>
}

