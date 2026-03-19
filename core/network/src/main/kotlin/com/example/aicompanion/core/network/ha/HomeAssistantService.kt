package com.example.aicompanion.core.network.ha

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface HomeAssistantService {
    @POST("api/services/{domain}/{service}")
    suspend fun callService(
        @Path("domain") domain: String,
        @Path("service") service: String,
        @Body request: HaServiceRequest,
    ): Response<List<HaStateResponse>>
}
