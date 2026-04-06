package com.example.adjustsumarizeapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.adjustsumarizeapp.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager - Manages secure storage and retrieval of authentication tokens
 * Uses EncryptedSharedPreferences for secure storage
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            // Create MasterKey for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create encrypted SharedPreferences
            EncryptedSharedPreferences.create(
                context,
                Constants.PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Save access token
     */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(Constants.KEY_ACCESS_TOKEN, token)
            .apply()
    }

    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    /**
     * Save refresh token
     */
    fun saveRefreshToken(token: String) {
        sharedPreferences.edit()
            .putString(Constants.KEY_REFRESH_TOKEN, token)
            .apply()
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    /**
     * Save user information
     */
    fun saveUserInfo(userId: String, email: String, name: String, role: String = "user") {
        sharedPreferences.edit()
            .putString(Constants.KEY_USER_ID, userId)
            .putString(Constants.KEY_USER_EMAIL, email)
            .putString(Constants.KEY_USER_NAME, name)
            .putString(Constants.KEY_USER_ROLE, role)  // ✅ SAVE ROLE
            .apply()
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null)
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null)
    }

    /**
     * Get user name
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, null)
    }

    /**
     * Get user role
     */
    fun getUserRole(): String {
        return sharedPreferences.getString(Constants.KEY_USER_ROLE, "user") ?: "user"
    }

    /**
     * Check if user is logged in (has valid access token)
     */
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    /**
     * Clear all stored data (logout)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Get authorization header value
     */
    fun getAuthorizationHeader(): String? {
        return getAccessToken()?.let { "Bearer $it" }
    }
}
