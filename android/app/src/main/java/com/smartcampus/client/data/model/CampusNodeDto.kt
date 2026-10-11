package com.smartcampus.client.data.model

// Campus Graph Node
data class CampusNodeDto(
    val id: Long,
    val buildingId: Long,
    val floorNumber: Int,
    val coordX: Double,
    val coordY: Double,
    val nodeType: String,
    val isAccessible: Boolean
)