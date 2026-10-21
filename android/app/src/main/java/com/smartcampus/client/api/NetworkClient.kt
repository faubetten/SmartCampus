package com.smartcampus.client.api

import android.content.Context
import com.smartcampus.client.security.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    // Retrofit requires the base URL to end with a slash when relative API paths are used.
    private const val BASE_URL = "http://127.0.0.1:8080/"

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