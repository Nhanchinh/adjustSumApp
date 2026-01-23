package com.example.adjustsumarizeapp.data.remote

import com.example.adjustsumarizeapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * AuthInterceptor - Automatically adds Authorization header to all requests
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get access token
        val accessToken = tokenManager.getAccessToken()
        
        // If no token or request already has Authorization header, proceed as normal
        if (accessToken.isNullOrBlank() || originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        return chain.proceed(newRequest)
    }
}
