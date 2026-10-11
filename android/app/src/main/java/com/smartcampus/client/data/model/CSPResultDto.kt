package com.smartcampus.client.data.model

data class RoomDto(
    val id: Long,
    val nodeId: Long,
    val roomNumber: String,
    val capacity: Int,
    val roomType: String
)

data class CSPResultDto(
    val success: Boolean,
    val assignedRoom: RoomDto?,
    val message: String
)