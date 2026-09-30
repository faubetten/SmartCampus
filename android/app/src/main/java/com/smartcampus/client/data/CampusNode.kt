package com.smartcampus.client.data

data class CampusNode(
    val id: Long,
    val buildingId: Long,
    val floorNumber: Int,
    val coordX: Double,
    val coordY: Double,
    val nodeType: String,
    val isAccessible: Boolean
)