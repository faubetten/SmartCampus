package com.smartcampus.client.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SmartCampusApiService {
    @GET("api/navigation/route")
    suspend fun getRoute(
        @Query("startId") startId: Long,
        @Query("targetId") targetId: Long,
        @Query("accessibleOnly") accessibleOnly: Boolean
    ): Response<List<CampusNode>>

    companion object {
        // 10.0.2.2 is the special alias to reach host machine localhost from the Android emulator
        private const val BASE_URL = "http://192.168.1.69:8080/"

        fun create(): SmartCampusApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(SmartCampusApiService::class.java)
        }
    }
}