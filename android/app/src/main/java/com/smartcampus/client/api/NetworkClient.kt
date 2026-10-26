package com.smartcampus.client.api

import android.content.Context
import com.smartcampus.client.security.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.smartcampus.client.BuildConfig

object NetworkClient {

    // Uses ADB reverse port forwarding (adb reverse tcp:8080 tcp:8080) to connect directly to host localhost.
    private val BASE_URL = BuildConfig.BASE_URL

    /**
     * Builds the shared HTTP client used by the app's API screens.
     *
     * The interceptor adds authentication and handles expired sessions, while
     * Retrofit converts JSON responses into Kotlin data classes.
     */
    fun createService(
        context: Context,
        onUnauthorized: (() -> Unit)? = null
    ): SmartCampusApiService {
        val tokenManager = TokenManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager, onUnauthorized))
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartCampusApiService::class.java)
    }
}