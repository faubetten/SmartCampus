package com.smartcampus.client.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)