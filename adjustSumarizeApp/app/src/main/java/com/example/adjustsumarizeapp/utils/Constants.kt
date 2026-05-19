package com.example.adjustsumarizeapp.utils

object Constants {
    // API Configuration
    // Use 10.0.2.2 for Android emulator to access localhost
    // Use your actual IP address for physical device (e.g., "http://192.168.1.100:8000/")
    const val BASE_URL = "http://192.168.30.101:8000/"
    
    // API Endpoints
    const val LOGIN_ENDPOINT = "auth/login"
    const val REGISTER_ENDPOINT = "auth/register"
    const val REFRESH_TOKEN_ENDPOINT = "auth/refresh"
    const val GET_CURRENT_USER_ENDPOINT = "auth/me"
    
    // Database
    const val DATABASE_NAME = "app_database"
    
    // SharedPreferences Keys
    const val PREFS_NAME = "app_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_ROLE = "user_role"  // ✅ THÊM
    
    // Timeouts (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
