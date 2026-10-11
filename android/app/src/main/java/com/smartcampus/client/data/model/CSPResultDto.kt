package com.smartcampus.client.data.model

// CSP Allocation Result
data class CSPResultDto(
    val success: Boolean,
    val message: String,
    val assignedRoomId: Long?,
    val assignedRoomNumber: String?
)
