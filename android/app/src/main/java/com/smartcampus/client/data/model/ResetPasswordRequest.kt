package com.smartcampus.client.data.model

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)