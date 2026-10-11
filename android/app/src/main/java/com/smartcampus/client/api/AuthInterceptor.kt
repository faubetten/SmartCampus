package com.smartcampus.client.api

import android.os.Handler
import android.os.Looper
import com.smartcampus.client.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val onUnauthorized: (() -> Unit)? = null
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val token = tokenManager.getToken()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            tokenManager.clearToken()
            if (onUnauthorized != null) {
                Handler(Looper.getMainLooper()).post {
                    onUnauthorized.invoke()
                }
            }
        }

        return response
    }
}