package com.ariaai.companion.core.network.ha

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor : Interceptor {
    @Volatile
    var baseUrl: HttpUrl? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val targetBaseUrl = baseUrl ?: return chain.proceed(chain.request())
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .scheme(targetBaseUrl.scheme)
            .host(targetBaseUrl.host)
            .port(targetBaseUrl.port)
            .build()
        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
