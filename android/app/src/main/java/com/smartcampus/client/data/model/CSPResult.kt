package com.smartcampus.client.data.model

data class Room(
    val id: Long,
    val nodeId: Long,
    val roomNumber: String,
    val capacity: Int,
    val roomType: String
)

data class CSPResult(
    val success: Boolean,
    val assignedRoom: Room?,
    val message: String
)