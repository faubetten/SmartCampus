package com.smartcampus.client.data.dto

import com.smartcampus.client.data.model.Equipment

data class RoomDetailsDto (
    val roomId: Long,
    val nodeId: Long,
    val roomName: String,
    val capacity: Int,
    val roomType: String,
    val roomNumber: String,
    val buildingName: String,
    val floorNumber: Int,
    val coordX: Double?,
    val coordY: Double?,
    val equipment: List<String> = emptyList()
)