package com.smartcampus.client.api

import com.smartcampus.client.data.model.CampusNode
import com.smartcampus.client.data.model.CSPResult
import com.smartcampus.client.data.model.ForgotPasswordRequest
import com.smartcampus.client.data.model.ForgotPasswordResponse
import com.smartcampus.client.data.model.JwtResponse
import com.smartcampus.client.data.model.LoginRequest
import com.smartcampus.client.data.model.RegisterRequest
import com.smartcampus.client.data.model.ResetPasswordRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SmartCampusApiService {

    // Retrofit maps these suspend functions to the backend's REST endpoints.
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<JwtResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResponseBody>

    @GET("api/navigation/route")
    suspend fun getRoute(
        @Query("startId") startId: Long,
        @Query("targetId") targetId: Long,
        @Query("accessibleOnly") accessibleOnly: Boolean = false
    ): Response<List<CampusNode>>

    @POST("api/scheduling/allocate")
    suspend fun allocateRoom(
        @Query("capacity") capacity: Int,
        @Query("roomType") roomType: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Response<CSPResult>
}