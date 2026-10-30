package com.smartcampus.client.api

import com.smartcampus.client.data.dto.RoomDetailsDto
import com.smartcampus.client.data.model.CampusNode
import com.smartcampus.client.data.requestDto.ForgotPasswordRequestDto
import com.smartcampus.client.data.responseDto.ForgotPasswordResponse
import com.smartcampus.client.data.responseDto.JwtResponseDto
import com.smartcampus.client.data.requestDto.LoginRequestDto
import com.smartcampus.client.data.requestDto.RegisterRequestDto
import com.smartcampus.client.data.requestDto.ResetPasswordRequestDto
import com.smartcampus.client.data.responseDto.BookingResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SmartCampusApiService {

    // Retrofit maps these suspend functions to the backend's REST endpoints.
    @POST("api/auth/login")
    suspend fun authenticateUser(@Body request: LoginRequestDto): Response<JwtResponseDto>

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequestDto): Response<ResponseBody>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<ForgotPasswordResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<ResponseBody>

    @GET("api/navigation/route")
    suspend fun getRoute(
        @Query("startId") startId: Long,
        @Query("targetId") targetId: Long,
        @Query("accessibleOnly") accessibleOnly: Boolean = false
    ): Response<List<CampusNode>>

    @GET("api/scheduling/available")
    suspend fun findAvailableRooms(
        @Query("capacity") capacity: Int,
        @Query("roomType") roomType: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Response<List<RoomDetailsDto>>

    @POST("api/scheduling/book")
    suspend fun bookSpecificRoom(
        @Query("roomId") roomId: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Response<BookingResponseDto>
}