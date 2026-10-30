package com.smartcampus.client.data.requestDto

data class ResetPasswordRequestDto(
    val email: String,
    val otp: String,
    val newPassword: String
)