package com.example.adjustsumarizeapp.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String = "user",  // admin or user
    val token: String? = null
)
