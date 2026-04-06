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
    
    /**
     * Update user settings (consent, profile)
     */
    @PUT("auth/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<UserPublicDto>
    
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
    suspend fun checkColabHealth(): Response<ColabHealthResponse>
    
    /**
     * Generate gold reference summary using Gemini AI
     */
    @POST("summarization/generate-reference")
    suspend fun generateReference(
        @Body request: GenerateReferenceRequest
    ): Response<GenerateReferenceResponse>

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
    @GET("history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<HistoryResponse>
    
    /**
     * Save summary to history
     */
    @POST("history")
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
    
    // ==================== Analytics Endpoints ====================
    
    /**
     * Get analytics data (admin sees all consented users, user sees own)
     */
    @GET("history/analytics")
    suspend fun getAnalytics(): Response<AnalyticsResponseDto>
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Get all users (admin only)
     */
    @GET("admin/users")
    suspend fun getAdminUsers(): Response<List<UserPublicDto>>
    
    /**
     * Delete user (admin only)
     */
    @DELETE("admin/users/{user_id}")
    suspend fun deleteUser(
        @Path("user_id") userId: String
    ): Response<Unit>
}

