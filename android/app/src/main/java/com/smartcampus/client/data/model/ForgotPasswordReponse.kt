package com.smartcampus.client.data.model

data class ForgotPasswordResponse(
    val message: String,
    val debugOtp: String?
)