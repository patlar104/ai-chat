package com.example.aicompanion.core.network.privacy

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * OkHttp application interceptor that enforces privacy mode.
 *
 * When [privacyModeEnabled] is true, any outbound request whose host
 * matches a cloud AI domain (e.g. generativelanguage.googleapis.com) is
 * blocked immediately and a synthetic 403 Forbidden response is returned.
 *
 * The privacy flag is held in a @Volatile field and updated externally by
 * SettingsViewModel after reading from DataStore — mirroring the pattern
 * used by HaAuthInterceptor for the HA access token and CloudAiService for
 * the API key. This keeps :core:network free of a :core:data (DataStore)
 * dependency.
 *
 * Requests to Home Assistant's local URL are NOT affected by this interceptor.
 */
class PrivacyInterceptor : Interceptor {

    @Volatile
    var privacyModeEnabled: Boolean = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (privacyModeEnabled && isCloudAiHost(request.url.host)) {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(403)
                .message("Privacy Mode Active")
                .body("Privacy Mode Active: cloud AI requests are blocked.".toResponseBody("text/plain".toMediaType()))
                .build()
        }

        return chain.proceed(request)
    }

    private fun isCloudAiHost(host: String): Boolean =
        host.endsWith("generativelanguage.googleapis.com") ||
            host.endsWith("aiplatform.googleapis.com")
}
