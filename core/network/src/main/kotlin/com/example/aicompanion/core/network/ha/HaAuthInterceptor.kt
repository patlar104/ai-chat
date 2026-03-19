package com.example.aicompanion.core.network.ha

import okhttp3.Interceptor
import okhttp3.Response

class HaAuthInterceptor : Interceptor {
    @Volatile
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val currentToken = token ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $currentToken")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
