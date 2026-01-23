package com.example.adjustsumarizeapp.data.remote

import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * TokenAuthenticator - Automatically refreshes access token when receiving 401 Unauthorized
 * 
 * Uses Provider<ApiService> to avoid circular dependency:
 * OkHttpClient -> TokenAuthenticator -> ApiService -> Retrofit -> OkHttpClient
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Provider<ApiService>  // Use Provider to break circular dependency
) : Authenticator {
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // If this is already a retry, give up to avoid infinite loop
        if (response.request.header("X-Token-Retry") != null) {
            return null
        }
        
        // Get refresh token
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            // No refresh token available, user needs to login again
            tokenManager.clearAll()
            return null
        }
        
        // Try to refresh the access token
        return try {
            // Note: Using runBlocking here because Authenticator is synchronous
            // This is acceptable for token refresh as it's a critical operation
            val newToken = runBlocking {
                refreshAccessToken(refreshToken)
            }
            
            if (newToken != null) {
                // Save new access token
                tokenManager.saveAccessToken(newToken)
                
                // Retry the original request with new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Token-Retry", "true")  // Mark as retry to prevent infinite loop
                    .build()
            } else {
                // Refresh failed, clear tokens
                tokenManager.clearAll()
                null
            }
        } catch (e: Exception) {
            // Refresh failed, clear tokens
            tokenManager.clearAll()
            null
        }
    }
    
    /**
     * Call refresh token API
     */
    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            // Get ApiService from provider lazily
            val apiService = apiServiceProvider.get()
            
            val response = apiService.refreshToken(
                RefreshTokenRequest(refreshToken = refreshToken)
            )
            
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
