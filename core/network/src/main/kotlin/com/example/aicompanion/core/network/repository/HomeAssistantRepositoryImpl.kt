package com.example.aicompanion.core.network.repository

import com.example.aicompanion.core.domain.error.AppError
import com.example.aicompanion.core.domain.logging.Logger
import com.example.aicompanion.core.domain.repository.HomeAssistantRepository
import com.example.aicompanion.core.network.ha.HaServiceRequest
import com.example.aicompanion.core.network.ha.HomeAssistantService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeAssistantRepositoryImpl @Inject constructor(
    private val haService: HomeAssistantService,
    private val logger: Logger,
) : HomeAssistantRepository {

    override suspend fun callService(
        domain: String,
        service: String,
        entityId: String,
        params: Map<String, Any>,
    ): Result<Unit> = try {
        val request = HaServiceRequest(
            entityId = entityId,
            brightness = (params["brightness"] as? Number)?.toInt(),
            temperature = (params["temperature"] as? Number)?.toFloat(),
        )
        val response = haService.callService(domain, service, request)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val reason = when (response.code()) {
                401 -> "Authentication failed — check your access token in Settings"
                404 -> "Service not found: $domain/$service"
                else -> "HTTP ${response.code()}"
            }
            Result.failure(AppError.HomeAssistant(reason))
        }
    } catch (e: java.io.IOException) {
        logger.e("HomeAssistantRepo", "Network error calling $domain/$service", e)
        Result.failure(AppError.Network(e))
    } catch (e: Exception) {
        logger.e("HomeAssistantRepo", "Unexpected error calling $domain/$service", e)
        Result.failure(AppError.HomeAssistant(e.message ?: "Unknown error"))
    }
}
