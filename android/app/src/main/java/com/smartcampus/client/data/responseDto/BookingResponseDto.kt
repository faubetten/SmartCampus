package com.smartcampus.client.data.responseDto

import com.smartcampus.client.data.dto.RoomDetailsDto

data class BookingResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val reservationId: Long? = null,
    val roomDetails: RoomDetailsDto? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String? = null
)

