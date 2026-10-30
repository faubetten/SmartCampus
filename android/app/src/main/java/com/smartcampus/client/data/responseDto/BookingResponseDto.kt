package com.smartcampus.client.data.responseDto

import com.smartcampus.client.data.dto.RoomDetailsDto

data class BookingResponseDto(
    val success: Boolean,
    val message: String,
    val reservationId: Long?,
    val room: RoomDetailsDto?,
    val startTime: String?,
    val endTime: String?,
    val status: String?
)

