package com.example.adjustsumarizeapp.utils

object Constants {
    // API Configuration
    const val BASE_URL = "https://api.example.com/"
    
    // API Endpoints
    const val LOGIN_ENDPOINT = "auth/login"
    const val REGISTER_ENDPOINT = "auth/register"
    const val USER_ENDPOINT = "user"
    
    // Database
    const val DATABASE_NAME = "app_database"
    
    // SharedPreferences
    const val PREFS_NAME = "app_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    
    // Timeouts (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
