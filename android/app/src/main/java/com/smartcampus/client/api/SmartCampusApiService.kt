package com.smartcampus.client.api

import com.smartcampus.client.data.model.CampusNodeDto
import com.smartcampus.client.data.model.CSPResultDto
import com.smartcampus.client.data.model.JwtResponse
import com.smartcampus.client.data.model.LoginRequest
import retrofit2.Response
import retrofit2.http.*

interface SmartCampusApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<JwtResponse>

    @GET("api/navigation/route")
    suspend fun getRoute(
        @Query("startId") startId: Long,
        @Query("targetId") targetId: Long,
        @Query("accessibleOnly") accessibleOnly: Boolean = false
    ): Response<List<CampusNodeDto>>

    @POST("api/scheduling/allocate")
    suspend fun allocateRoom(
        @Query("capacity") capacity: Int,
        @Query("roomType") roomType: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Response<CSPResultDto>
}