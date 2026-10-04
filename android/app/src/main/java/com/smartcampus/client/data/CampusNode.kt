package com.smartcampus.client.data

data class CampusNode(
    // Mirrors the fields returned by the backend navigation endpoint.
    val id: Long,
    val buildingId: Long,
    val floorNumber: Int,
    val coordX: Double,
    val coordY: Double,
    val nodeType: String,
    val isAccessible: Boolean
)